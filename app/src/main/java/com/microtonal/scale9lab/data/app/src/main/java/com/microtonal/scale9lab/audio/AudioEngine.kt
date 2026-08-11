package com.microtonal.scale9lab.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.microtonal.scale9lab.data.WaveformType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin

class AudioEngine {
    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null
    @Volatile private var isRunning = false

    private val voiceIdCounter = AtomicLong(0L)

    private class Voice(
        val id: Long,
        val noteIndex: Int,
        val frequency: Double,
        val startSample: Long
    ) {
        @Volatile var phase: Double = 0.0
        @Volatile var releaseSample: Long? = null
    }

    private val activeVoices = ConcurrentHashMap<Long, Voice>()
    @Volatile private var globalSampleCount: Long = 0L

    @Volatile var masterVolume = 0.8f
    @Volatile var releaseTimeMs = 300.0
    @Volatile var sustainEnabled = false
    @Volatile var waveform = WaveformType.PURE_SINE

    fun start() {
        if (isRunning) return
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
        isRunning = true

        thread(name = "MicrotonalAudioThread") {
            val chunkSamples = 256
            val shortBuffer = ShortArray(chunkSamples)

            while (isRunning) {
                for (i in 0 until chunkSamples) {
                    globalSampleCount++
                    val currentFrame = globalSampleCount
                    var mixedSample = 0.0

                    val attackSamples = (sampleRate * 0.010).toLong() // 10ms attack
                    val releaseSamples = (sampleRate * (releaseTimeMs / 1000.0)).toLong().coerceAtLeast(1L)

                    val iterator = activeVoices.entries.iterator()
                    var activeCount = 0

                    while (iterator.hasNext()) {
                        val entry = iterator.next()
                        val voice = entry.value
                        activeCount++

                        var env = 1.0
                        val age = currentFrame - voice.startSample
                        if (age < attackSamples && attackSamples > 0) {
                            env = age.toDouble() / attackSamples.toDouble()
                        }

                        val relSample = voice.releaseSample
                        if (relSample != null) {
                            val relAge = currentFrame - relSample
                            if (relAge >= releaseSamples) {
                                iterator.remove()
                                continue
                            }
                            env *= (1.0 - (relAge.toDouble() / releaseSamples.toDouble())).coerceIn(0.0, 1.0)
                        }

                        val phaseInc = (2.0 * PI * voice.frequency) / sampleRate
                        voice.phase += phaseInc
                        if (voice.phase >= 2.0 * PI) voice.phase -= 2.0 * PI

                        val rawSample = generateSample(voice.phase, waveform)
                        mixedSample += rawSample * env
                    }

                    // Polyphonic headroom scaling
                    val headroomFactor = if (activeCount > 1) 1.0 / Math.sqrt(activeCount.toDouble()) else 1.0
                    val finalSample = (mixedSample * headroomFactor * masterVolume * 0.5 * 32767.0)
                        .coerceIn(-32767.0, 32767.0)

                    shortBuffer[i] = finalSample.toInt().toShort()
                }

                audioTrack?.write(shortBuffer, 0, chunkSamples)
            }
        }
    }

    private fun generateSample(phase: Double, type: WaveformType): Double {
        return when (type) {
            WaveformType.PURE_SINE -> sin(phase)
            WaveformType.SOFT_SYNTH -> {
                0.7 * sin(phase) + 0.2 * sin(2.0 * phase) + 0.1 * sin(3.0 * phase)
            }
            WaveformType.ORGAN -> {
                0.5 * sin(phase) + 0.3 * sin(2.0 * phase) + 0.15 * sin(4.0 * phase) + 0.05 * sin(8.0 * phase)
            }
        }
    }

    fun noteOn(noteIndex: Int, frequency: Double): Long {
        val id = voiceIdCounter.incrementAndGet()
        val voice = Voice(id, noteIndex, frequency, globalSampleCount)
        activeVoices[id] = voice
        return id
    }

    fun noteOff(noteIndex: Int) {
        if (sustainEnabled) return
        val currentFrame = globalSampleCount
        activeVoices.values.forEach { voice ->
            if (voice.noteIndex == noteIndex && voice.releaseSample == null) {
                voice.releaseSample = currentFrame
            }
        }
    }

    fun releaseAllSustainedNotes() {
        val currentFrame = globalSampleCount
        activeVoices.values.forEach { voice ->
            if (voice.releaseSample == null) {
                voice.releaseSample = currentFrame
            }
        }
    }

    fun stop() {
        isRunning = false
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        activeVoices.clear()
    }
}
