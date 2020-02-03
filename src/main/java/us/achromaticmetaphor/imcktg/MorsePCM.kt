package us.achromaticmetaphor.imcktg

import us.achromaticmetaphor.imcktg.Morse.morse
import us.achromaticmetaphor.imcktg.Morse.numPulses
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Arrays;

private const val secondsPerMinute = 60

private fun ByteArray.mSHORTle(offset: Int, s: Int) = mSHORTle(offset, s.toShort())
private fun ByteArray.mSHORTle(offset: Int, s: Short) {
    this[offset] = s.toByte()
    this[offset + 1] = (s.toInt() shr 8).toByte()
}

private fun ByteArray.mINTle(offset: Int, i: Int) {
    mSHORTle(offset, i)
    mSHORTle(offset + 2, i shr 16)
}

class MorsePCM(private val freq: Int, private val sampleRate: Int, private val wpm: Int, private val repeatCount: Int) : ToneGenerator() {
    private val samplevec: Array<ByteArray>
    private val silent: ByteArray
    private val samplesPerPulse: Int = secondsPerMinute * sampleRate / wpm / Morse.unitsPerWord

    constructor(freq: Int, wpm: Int, repeatCount: Int) : this(freq, freq * 10, wpm, repeatCount) {}

    @Throws(IOException::class)
    fun writeWithWavHeader(out: OutputStream, s: String?) {
        val writer = MorseWriter(out)
        val mcs: Iterable<String> = morse(s!!)
        writer.writeWavHeader(numPulses(mcs) * samplesPerPulse * (repeatCount + 1))
        writer.writeMorse(mcs)
        writer.flush()
    }

    private inner class MorseWriter(out: OutputStream) {
        private val out: OutputStream
        private var pulsesWritten: Int
        @Throws(IOException::class)
        fun writeWavHeader(samples: Int) {
            out.write(ByteArray(44).apply{
                mINTle(0, 0x46464952) // RIFF
                mINTle(4, samples + size - 8) // length of rest of stream
                mINTle(8, 0x45564157) // WAVE
                mINTle(12, 0x20746d66) // fmt<sp>
                mINTle(16, 16) // size of "fmt " subchunk
                mSHORTle(20, 1) // audio format
                mSHORTle(22, 1) // number of channels
                mINTle(24, sampleRate) // sample rate
                mINTle(28, sampleRate) // byte rate
                mSHORTle(32, 1) // bytes per sample * number of channels
                mSHORTle(34, 8) // bits per sample
                mINTle(36, 0x61746164) // data
                mINTle(40, samples) // size of "data" subchunk
            })
        }

        @Throws(IOException::class)
        private fun writeSamplePulse() {
            out.write(samplevec[pulsesWritten])
            pulsesWritten++
            pulsesWritten %= samplevec.size
        }

        @Throws(IOException::class)
        private fun writeSilentPulse() = out.write(silent)

        @Throws(IOException::class)
        private fun writePulse(pulse: Int) = if (pulse == 0) writeSilentPulse() else writeSamplePulse()

        @Throws(IOException::class)
        private fun writePulses(n: Int, pulses: Int) {
            var n = n
            var pulses = pulses
            while (n-- > 0) {
                writePulse(pulses and 1)
                pulses = pulses shr 1
            }
        }

        @Throws(IOException::class)
        private fun writeMorseChar(c: Char) {
            writePulses(if (c == '-') 4 else 2, if (c == '.') 1 else if (c == '-') 7 else 0)
        }

        @Throws(IOException::class)
        private fun writeMorseString(mcs: String) {
            for (element in mcs) writeMorseChar(element)
            writePulses(2, 0)
        }

        @Throws(IOException::class)
        fun writeMorse(morse: Iterable<String>) { for (i in 0..repeatCount) for (s in morse) writeMorseString(s) }

        @Throws(IOException::class)
        fun flush() = out.flush()

        init {
            this.out = BufferedOutputStream(out)
            pulsesWritten = 0
        }
    }

    @Throws(IOException::class)
    override fun writeTone(out: OutputStream, s: String) = writeWithWavHeader(out, s)
    override fun filenameExt() = ".wav"
    override fun filenameTypePrefix() = "RIFF.WAV:$freq:$wpm:$repeatCount:"

    init {
        silent = ByteArray(samplesPerPulse)
        Arrays.fill(silent, Byte.MAX_VALUE)
        samplevec = Array(sampleRate / freq) { ByteArray(samplesPerPulse) }
        val scale = Math.PI * 2 * freq / sampleRate
        for (i in samplevec.indices) for (j in 0 until samplevec[i].count()) samplevec[i][j] = (Byte.MAX_VALUE + Byte.MAX_VALUE * Math.sin((i * samplevec[i].count() + j) * scale)).toByte()
    }
}