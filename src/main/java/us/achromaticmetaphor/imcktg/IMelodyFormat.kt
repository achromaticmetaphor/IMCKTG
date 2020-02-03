package us.achromaticmetaphor.imcktg

import java.io.PrintStream

private val validNotes = setOf("a", "#a", "&a", "b", "#b", "&b", "c", "#c", "&c", "d", "#d", "&d", "e", "#e", "&e", "f", "#f", "&f")
private val validStyles = setOf("S0", "S1", "S2")
private const val octavePrefix = "*"
private const val durationSuffixDotted = "."
private const val lineEnding = "\r\n"
private const val headerSeparator = ":"
private const val beatMin = 25
private const val beatMax = 900
private const val octaveMin = 0
private const val octaveMax = 8
private const val volumeMin = 0

private fun String.isValidNote() = validNotes.contains(this)
private fun PrintStream.writeHeader(key: String, value: String) = print(key + headerSeparator + value + lineEnding)

object IMelodyFormat {
    const val styleContinuous = "S1"
    const val defaultOctave = 4
    const val rest = "r"
    const val lineContinuation = "$lineEnding "
    const val volumeMax = 15

    fun note(octave: Int, tone: String): String {
        require(!(octave < octaveMin || octave > octaveMax)) { "invalid octave: $octave" }
        require(tone.isValidNote()) { "invalid tone: $tone" }
        return (if (octave == defaultOctave) "" else octavePrefix + octave) + tone
    }

    fun duration(dur: Int): String {
        require(!(dur < 0 || dur > 5))
        return "" + dur
    }

    fun writeRequiredHeaders(out: PrintStream) {
        out.apply {
            writeHeader("BEGIN", "IMELODY")
            writeHeader("VERSION", "1.2")
            writeHeader("FORMAT", "CLASS1.0")
        };
    }

    fun writeBeatHeader(out: PrintStream, beat: Int) {
        require(!(beat < beatMin || beat > beatMax)) { "invalid beat: $beat" }
        out.writeHeader("BEAT", "" + beat)
    }

    fun writeStyleHeader(out: PrintStream, style: String) {
        require(validStyles.contains(style)) { "invalid style: $style" }
        out.writeHeader("STYLE", style)
    }

    fun writeVolumeHeader(out: PrintStream, volume: Int) {
        require(!(volume < volumeMin || volume > volumeMax)) { "invalid volume: $volume" }
        out.writeHeader("VOLUME", "V$volume")
    }

    fun writeNameHeaderMangled(out: PrintStream, name: String) {
        out.writeHeader("NAME", name.replace("\n".toRegex(), ""))
    }

    fun writeBeginMelody(out: PrintStream) {
        out.apply {
            writeHeader("MELODY", "")
            print(" ")
        }
    }

    fun durationDotted(dur: Int) = duration(dur) + durationSuffixDotted
    fun writeEndMelody(out: PrintStream) = out.print(lineEnding)
    fun writeRequiredFooters(out: PrintStream) = out.writeHeader("END", "IMELODY")
    fun beginRepeatBlock(out: PrintStream) = out.print("(")

    fun endRepeatBlock(out: PrintStream, repeatCount: Int) {
        require(repeatCount >= 0) { "invalid repeat count: $repeatCount" }
        out.print("@$repeatCount)")
    }
}