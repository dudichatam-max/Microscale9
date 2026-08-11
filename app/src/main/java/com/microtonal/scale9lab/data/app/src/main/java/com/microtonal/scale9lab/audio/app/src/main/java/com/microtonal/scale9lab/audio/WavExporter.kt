package com.microtonal.scale9lab.audio

import com.microtonal.scale9lab.data.RecordedNoteEvent
import com.microtonal.scale9lab.data.WaveformType
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.sin

object WavExporter {
    fun exportToWav(
        outputFile: File,
        events: List<RecordedNoteEvent>,
        waveform: WaveformType = WaveformType.PURE_SINE,
        sampleRate: Int = 44100
    ) {
        if (events.isEmpty()) return

        val releaseDurationMs = 300L
        val maxEndTime = events.maxOf { it.timestampMs + it.durationMs + releaseDurationMs }
        val totalSamples = ((maxEndTime / 1000.0) * sampleRate).toInt().coerceAtLeast(sampleRate / 2)

        val pcmData = ShortArray(totalSamples)

        val attackSamples = (sampleRate * 0.010).toInt() // 10ms
        val releaseSamples = ((releaseDurationMs / 1000.0) * sampleRate).toInt()

        events.forEach { event ->
            val startSample = ((event.timestampMs / 1000.0) * sampleRate).toInt()
            val durSamples = ((event.durationMs / 1000.0) * sampleRate).toInt()

            var phase = 0.0
            val phaseInc = (2.0 * PI * event.frequencyHz) / sampleRate

            val totalEventSamples = durSamples + releaseSamples
            for (i in 0 until totalEventSamples) {
                val sampleIdx = startSample + i
                if (sampleIdx >= totalSamples) break

                var env = 1.0
                if (i < attackSamples && attackSamples > 0) {
                    env = i.toDouble() / attackSamples.toDouble()
                }

                if (i >= durSamples) {
                    val relProgress = (i - durSamples).toDouble() / releaseSamples.toDouble()
                    env *= (1.0 - relProgress).coerceIn(0.0, 1.0)
                }

                phase += phaseInc
                if (phase >= 2.0 * PI) phase -= 2.0 * PI

                val sampleVal = when (waveform) {
                    WaveformType.PURE_SINE -> sin(phase)
                    WaveformType.SOFT_SYNTH -> 0.7 * sin(phase) + 0.2 * sin(2.0 * phase) + 0.1 * sin(3.0 * phase)
                    WaveformType.ORGAN -> 0.5 * sin(phase) + 0.3 * sin(2.0 * phase) + 0.15 * sin(4.0 * phase)
                }

                val existingPcm = pcmData[sampleIdx].toInt()
                val newPcm = (existingPcm + (sampleVal * env * 0.4 * 32767.0)).toInt()
                    .coerceIn(-32767, 32767).toShort()
                pcmData[sampleIdx] = newPcm
            }
        }

        FileOutputStream(outputFile).use { fos ->
            writeWavHeader(fos, totalSamples, sampleRate)
            val byteBuffer = ByteArray(pcmData.size * 2)
            for (i in pcmData.indices) {
                val s = pcmData[i].toInt()
                byteBuffer[i * 2] = (s and 0x00FF).toByte()
                byteBuffer[i * 2 + 1] = ((s shr 8) and 0x00FF).toByte()
            }
            fos.write(byteBuffer)
        }
    }

    private fun writeWavHeader(out: FileOutputStream, totalSamples: Int, sampleRate: Int) {
        val channels = 1
        val bitsPerSample = 16
        val dataSize = totalSamples * channels * (bitsPerSample / 8)
        val chunkSize = 36 + dataSize
        val byteRate = sampleRate * channels * (bitsPerSample / 8)

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (chunkSize and 0xff).toByte()
        header[5] = ((chunkSize shr 8) and 0xff).toByte()
        header[6] = ((chunkSize shr 16) and 0xff).toByte()
        header[7] = ((chunkSize shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0 // PCM
        header[22] = channels.toByte(); header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * (bitsPerSample / 8)).toByte(); header[33] = 0
        header[34] = bitsPerSample.toByte(); header[35] = 0
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (dataSize and 0xff).toByte()
        header[41] = ((dataSize shr 8) and 0xff).toByte()
        header[42] = ((dataSize shr 16) and 0xff).toByte()
        header[43] = ((dataSize shr 24) and 0xff).toByte()

        out.write(header, 0, 44)
    }
}
