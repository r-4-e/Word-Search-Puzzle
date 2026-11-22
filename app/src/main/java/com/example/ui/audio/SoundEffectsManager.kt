package com.example.ui.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Synthesizes subtle, minimalist tactile audio cues completely offline via AudioTrack.
 * Uses soft sine/harmonic envelopes to match a calm editorial paper-and-ink aesthetic.
 */
class SoundEffectsManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sampleRate = 44_100

    @Volatile
    var isSoundEnabled: Boolean = true

    // Pentatonic frequencies (Hz) for gentle ascending grid-drag steps
    private val stepNotesHz = doubleArrayOf(
        523.25, // C5
        587.33, // D5
        659.25, // E5
        783.99, // G5
        880.00, // A5
        1046.50, // C6
        1174.66, // D6
        1318.51  // E6
    )

    private val stepBuffers: List<ShortArray> by lazy {
        stepNotesHz.map { freq ->
            synthesizeTone(
                freqHz = freq,
                durationMs = 24,
                amplitude = 0.14,
                decayRate = 160.0,
                harmonicMix = 0.12
            )
        }
    }

    private val wordFoundBuffer: ShortArray by lazy {
        val first = synthesizeTone(
            freqHz = 659.25, // E5
            durationMs = 95,
            amplitude = 0.18,
            decayRate = 28.0,
            harmonicMix = 0.18
        )
        val second = synthesizeTone(
            freqHz = 987.77, // B5
            durationMs = 170,
            amplitude = 0.20,
            decayRate = 18.0,
            harmonicMix = 0.15
        )
        concatWithOverlap(listOf(first, second), overlapMs = 35)
    }

    private val levelCompleteBuffer: ShortArray by lazy {
        val notes = listOf(523.25, 659.25, 783.99, 1046.50).mapIndexed { idx, freq ->
            synthesizeTone(
                freqHz = freq,
                durationMs = if (idx == 3) 260 else 110,
                amplitude = 0.20,
                decayRate = if (idx == 3) 12.0 else 22.0,
                harmonicMix = 0.16
            )
        }
        concatWithOverlap(notes, overlapMs = 45)
    }

    private val hintBuffer: ShortArray by lazy {
        synthesizeTone(
            freqHz = 880.0, // A5
            durationMs = 190,
            amplitude = 0.16,
            decayRate = 16.0,
            harmonicMix = 0.35
        )
    }

    private val invalidBuffer: ShortArray by lazy {
        synthesizeSweep(
            startFreqHz = 180.0,
            endFreqHz = 125.0,
            durationMs = 55,
            amplitude = 0.12,
            decayRate = 55.0
        )
    }

    fun playGridSelectionStep(stepIndex: Int) {
        if (!isSoundEnabled) return
        val idx = (stepIndex.coerceAtLeast(0)) % stepBuffers.size
        playBufferAsync(stepBuffers[idx])
    }

    fun playWordFound() {
        if (!isSoundEnabled) return
        playBufferAsync(wordFoundBuffer)
    }

    fun playLevelComplete() {
        if (!isSoundEnabled) return
        playBufferAsync(levelCompleteBuffer)
    }

    fun playHint() {
        if (!isSoundEnabled) return
        playBufferAsync(hintBuffer)
    }

    fun playInvalidRelease() {
        if (!isSoundEnabled) return
        playBufferAsync(invalidBuffer)
    }

    fun release() {
        scope.cancel()
    }

    private fun playBufferAsync(samples: ShortArray) {
        scope.launch {
            playPcm(samples)
        }
    }

    private fun playPcm(samples: ShortArray) {
        if (samples.isEmpty()) return
        var track: AudioTrack? = null
        try {
            val byteCount = samples.size * 2
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(byteCount)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(samples, 0, samples.size)
            track.play()
            val durationMs = (samples.size * 1000L) / sampleRate
            Thread.sleep(durationMs + 25L)
        } catch (_: Throwable) {
            // Gracefully ignore audio hardware unavailability on headless/restricted devices
        } finally {
            try {
                track?.stop()
            } catch (_: Throwable) {
            }
            try {
                track?.release()
            } catch (_: Throwable) {
            }
        }
    }

    private fun synthesizeTone(
        freqHz: Double,
        durationMs: Int,
        amplitude: Double,
        decayRate: Double,
        harmonicMix: Double
    ): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        val attackSamples = (sampleRate * 0.004).coerceAtLeast(1.0) // 4ms soft attack
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val attack = (i.toDouble() / attackSamples).coerceAtMost(1.0)
            val env = attack * exp(-decayRate * t)
            val fundamental = sin(2.0 * PI * freqHz * t)
            val harmonic = sin(2.0 * PI * (freqHz * 2.0) * t)
            val signal = (fundamental + harmonicMix * harmonic) / (1.0 + harmonicMix)
            val sample = (signal * env * amplitude * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            out[i] = sample.toShort()
        }
        return out
    }

    private fun synthesizeSweep(
        startFreqHz: Double,
        endFreqHz: Double,
        durationMs: Int,
        amplitude: Double,
        decayRate: Double
    ): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val progress = i.toDouble() / count.coerceAtLeast(1)
            val freq = startFreqHz + (endFreqHz - startFreqHz) * progress
            phase += 2.0 * PI * freq / sampleRate
            val t = i.toDouble() / sampleRate
            val env = exp(-decayRate * t)
            val sample = (sin(phase) * env * amplitude * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            out[i] = sample.toShort()
        }
        return out
    }

    private fun concatWithOverlap(parts: List<ShortArray>, overlapMs: Int): ShortArray {
        val overlapSamples = (sampleRate * overlapMs) / 1000
        val totalSize = parts.sumOf { it.size } - (parts.size - 1).coerceAtLeast(0) * overlapSamples
        val out = ShortArray(totalSize.coerceAtLeast(1))
        var cursor = 0
        for ((index, part) in parts.withIndex()) {
            for (i in part.indices) {
                val targetIdx = cursor + i
                if (targetIdx in out.indices) {
                    val mixed = out[targetIdx].toInt() + part[i].toInt()
                    out[targetIdx] = mixed.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            cursor += (part.size - if (index < parts.lastIndex) overlapSamples else 0).coerceAtLeast(1)
        }
        return out
    }
}
