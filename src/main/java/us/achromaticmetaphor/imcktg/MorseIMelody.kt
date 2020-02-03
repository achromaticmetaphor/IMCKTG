package us.achromaticmetaphor.imcktg

import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream

private const val defaultWordsPerMinute = 20
private const val defaultTone = "a"

class MorseIMelody(octave: Int, tone: String, wpm: Int, private val repeatCount: Int) : ToneGenerator() {
    @Throws(IOException::class)
    fun writeIMelody(out: OutputStream, s: String) {
        val pout = PrintStream(out)
        writeIMelody(pout, s)
        if (pout.checkError()) throw IOException()
    }

    private fun writeIMelody(out: PrintStream, s: String) {
        writeIMelodyHeader(out, s)
        morseMelody(out, Morse.morse(s))
        writeIMelodyFooter(out)
    }

    private fun writeIMelodyHeader(out: PrintStream, s: String) {
        IMelodyFormat.apply {
            writeRequiredHeaders(out)
            writeNameHeaderMangled(out, s)
            writeBeatHeader(out, beat)
            writeStyleHeader(out, styleContinuous)
            writeVolumeHeader(out, volumeMax)
        }
    }

    private val tones: Map<Char, String>
    private val beat: Int

    constructor(repeatCount: Int) : this(defaultWordsPerMinute, repeatCount) {}
    constructor(wpm: Int, repeatCount: Int) : this(IMelodyFormat.defaultOctave, defaultTone, wpm, repeatCount) {}

    private fun morseMelody(out: PrintStream, mcs: Iterable<String>) {
        IMelodyFormat.apply {
            writeBeginMelody(out)
            if (repeatCount > 0) beginRepeatBlock(out)
            for (s in mcs) {
                s.map{ e -> tones[e] }.forEach{ t -> out.print(t) }
                out.print(tones[Morse.pauseChar])
                out.print(lineContinuation)
            }
            if (repeatCount > 0) endRepeatBlock(out, repeatCount)
            writeEndMelody(out)
        }
    }

    @Throws(IOException::class)
    override fun writeTone(out: OutputStream, s: String) = writeIMelody(out, s)
    override fun filenameExt() = ".imy"
    override fun filenameTypePrefix() = "iMelody:" + beat + ":" + tones[Morse.dotChar] + ":" + repeatCount + ":"

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(argV: Array<String>) = MorseIMelody(0).writeIMelody(System.out as OutputStream, argV[0])
        private fun writeIMelodyFooter(out: PrintStream) = IMelodyFormat.writeRequiredFooters(out)
    }

    init {
        require(!(wpm < 1 || wpm > 144)) { "invalid wpm : $wpm" }
        require(repeatCount >= 0) { "invalid repeat count : $repeatCount" }
        val duration = if (wpm <= 36) 3 else if (wpm <= 72) 4 else 5
        // The format defines BEAT as beats per minute, at common (4/4) time.
        beat = wpm * Morse.unitsPerWord * 4 / (1 shl duration)
        IMelodyFormat.apply {
            tones = mapOf(
                    Morse.dotChar to note(octave, tone) + duration(duration) + rest + duration(duration),
                    Morse.dashChar to note(octave, tone) + durationDotted(duration - 1) + rest + duration(duration),
                    Morse.pauseChar to rest + duration(duration - 1)
            )
        }
    }
}