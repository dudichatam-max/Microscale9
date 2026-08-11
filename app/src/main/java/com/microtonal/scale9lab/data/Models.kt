package com.microtonal.scale9lab.data

import kotlin.math.pow
import kotlin.math.log2

enum class WaveformType(val displayName: String) {
    PURE_SINE("Pure Sine"),
    SOFT_SYNTH("Soft Synth"),
    ORGAN("Organ-like")
}

data class ScaleNote(
    val id: Int,
    val nameHebrew: String,
    val ratioNumerator: Int,
    val ratioDenominator: Int,
    val baseFrequencyHz: Double
) {
    fun getCents(rootFrequencyHz: Double): Double {
        return 1200.0 * log2(baseFrequencyHz / rootFrequencyHz)
    }

    fun getEffectiveFrequency(rootFrequencyHz: Double, octaveOffset: Int): Double {
        return baseFrequencyHz * 2.0.pow(octaveOffset.toDouble())
    }

    companion object {
        fun calculateCentsFromFreq(freq: Double, rootFreq: Double): Double {
            return 1200.0 * log2(freq / rootFreq)
        }

        fun calculateFreqFromCents(cents: Double, rootFreq: Double): Double {
            return rootFreq * 2.0.pow(cents / 1200.0)
        }
    }
}

data class Scale(
    val id: String,
    val name: String,
    val rootFrequencyHz: Double = 261.6256, // C4
    val notes: List<ScaleNote>
) {
    companion object {
        fun getDefault9NoteScale(): Scale {
            val root = 261.6256
            val notes = listOf(
                ScaleNote(0, "דו", 1, 1, root * (1.0 / 1.0)),
                ScaleNote(1, "רה", 9, 8, root * (9.0 / 8.0)),
                ScaleNote(2, "מי", 5, 4, root * (5.0 / 4.0)),
                ScaleNote(3, "פה", 4, 3, root * (4.0 / 3.0)),
                ScaleNote(4, "סול", 3, 2, root * (3.0 / 2.0)),
                ScaleNote(5, "לה", 5, 3, root * (5.0 / 3.0)),
                ScaleNote(6, "סי", 7, 4, root * (7.0 / 4.0)),
                ScaleNote(7, "אב", 15, 8, root * (15.0 / 8.0)),
                ScaleNote(8, "אל", 31, 16, root * (31.0 / 16.0))
            )
            return Scale("default_9_note", "Standard 9-Note Microtonal", root, notes)
        }
    }
}

data class RecordedNoteEvent(
    val noteIndex: Int,
    val noteName: String,
    val frequencyHz: Double,
    val octaveOffset: Int,
    val timestampMs: Long,
    val durationMs: Long
)
