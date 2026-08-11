package com.microtonal.scale9lab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.microtonal.scale9lab.audio.AudioEngine
import com.microtonal.scale9lab.audio.WavExporter
import com.microtonal.scale9lab.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ScaleRepository(application)
    val audioEngine = AudioEngine()

    private val _currentScale = MutableStateFlow(repository.getSavedScale())
    val currentScale: StateFlow<Scale> = _currentScale.asStateFlow()

    private val _currentOctave = MutableStateFlow(0)
    val currentOctave: StateFlow<Int> = _currentOctave.asStateFlow()

    private val _waveform = MutableStateFlow(WaveformType.PURE_SINE)
    val waveform: StateFlow<WaveformType> = _waveform.asStateFlow()

    private val _masterVolume = MutableStateFlow(0.8f)
    val masterVolume: StateFlow<Float> = _masterVolume.asStateFlow()

    private val _sustainEnabled = MutableStateFlow(false)
    val sustainEnabled: StateFlow<Boolean> = _sustainEnabled.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val recordedEvents = mutableListOf<RecordedNoteEvent>()
    private val activeNoteStartTimes = mutableMapOf<Int, Long>()
    private var recordingStartTimeMs: Long = 0L

    private val _lastExportedFile = MutableStateFlow<File?>(null)
    val lastExportedFile: StateFlow<File?> = _lastExportedFile.asStateFlow()

    init {
        audioEngine.start()
    }

    fun onNoteDown(noteIndex: Int) {
        val scale = _currentScale.value
        if (noteIndex < 0 || noteIndex >= scale.notes.size) return
        val note = scale.notes[noteIndex]
        val effectiveFreq = note.getEffectiveFrequency(scale.rootFrequencyHz, _currentOctave.value)

        audioEngine.noteOn(noteIndex, effectiveFreq)

        if (_isRecording.value) {
            val now = System.currentTimeMillis() - recordingStartTimeMs
            activeNoteStartTimes[noteIndex] = now
        }
    }

    fun onNoteUp(noteIndex: Int) {
        audioEngine.noteOff(noteIndex)

        if (_isRecording.value) {
            val startTime = activeNoteStartTimes.remove(noteIndex)
            if (startTime != null) {
                val endTime = System.currentTimeMillis() - recordingStartTimeMs
                val duration = (endTime - startTime).coerceAtLeast(50L)
                val scale = _currentScale.value
                val note = scale.notes[noteIndex]
                val effectiveFreq = note.getEffectiveFrequency(scale.rootFrequencyHz, _currentOctave.value)

                recordedEvents.add(
                    RecordedNoteEvent(
                        noteIndex = noteIndex,
                        noteName = note.nameHebrew,
                        frequencyHz = effectiveFreq,
                        octaveOffset = _currentOctave.value,
                        timestampMs = startTime,
                        durationMs = duration
                    )
                )
            }
        }
    }

    fun setOctave(octave: Int) {
        _currentOctave.value = octave.coerceIn(-2, 2)
    }

    fun setWaveform(type: WaveformType) {
        _waveform.value = type
        audioEngine.waveform = type
    }

    fun setVolume(volume: Float) {
        _masterVolume.value = volume
        audioEngine.masterVolume = volume
    }

    fun setSustain(enabled: Boolean) {
        _sustainEnabled.value = enabled
        audioEngine.sustainEnabled = enabled
        if (!enabled) {
            audioEngine.releaseAllSustainedNotes()
        }
    }

    fun updateNoteFrequency(noteIndex: Int, newFrequency: Double) {
        val current = _currentScale.value
        val updatedNotes = current.notes.mapIndexed { idx, note ->
            if (idx == noteIndex) {
                note.copy(baseFrequencyHz = newFrequency)
            } else note
        }
        val updatedScale = current.copy(notes = updatedNotes)
        _currentScale.value = updatedScale
        repository.saveScale(updatedScale)
    }

    fun setRootFrequency(newRoot: Double) {
        val current = _currentScale.value
        val ratioList = current.notes.map { it.baseFrequencyHz / current.rootFrequencyHz }
        val updatedNotes = current.notes.mapIndexed { idx, note ->
            note.copy(baseFrequencyHz = newRoot * ratioList[idx])
        }
        val updatedScale = current.copy(rootFrequencyHz = newRoot, notes = updatedNotes)
        _currentScale.value = updatedScale
        repository.saveScale(updatedScale)
    }

    fun resetScale() {
        val defaultScale = repository.resetToDefault()
        _currentScale.value = defaultScale
    }

    fun startRecording() {
        recordedEvents.clear()
        activeNoteStartTimes.clear()
        recordingStartTimeMs = System.currentTimeMillis()
        _isRecording.value = true
    }

    fun stopRecording() {
        _isRecording.value = false
    }

    fun exportRecordingToWav() {
        viewModelScope.launch {
            if (recordedEvents.isEmpty()) return@launch
            val outputDir = getApplication<Application>().getExternalFilesDir(null) ?: getApplication<Application>().filesDir
            val wavFile = File(outputDir, "micro_scale_rec_${System.currentTimeMillis()}.wav")
            WavExporter.exportToWav(
                outputFile = wavFile,
                events = recordedEvents,
                waveform = _waveform.value
            )
            _lastExportedFile.value = wavFile
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stop()
    }
}
