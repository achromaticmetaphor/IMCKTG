package us.achromaticmetaphor.imcktg

import java.text.Normalizer
import java.util.Locale

private var IMCmap: Map<Char, String> = mapOf(
    'a' to ".-",
    'b' to "-...",
    'c' to "-.-.",
    'd' to "-..",
    'e' to ".",
    'f' to "..-.",
    'g' to "--.",
    'h' to "....",
    'i' to "..",
    'j' to ".---",
    'k' to "-.-",
    'l' to ".-..",
    'm' to "--",
    'n' to "-.",
    'o' to "---",
    'p' to ".--.",
    'q' to "--.-",
    'r' to ".-.",
    's' to "...",
    't' to "-",
    'u' to "..-",
    'v' to "...-",
    'w' to ".--",
    'x' to "-..-",
    'y' to "-.--",
    'z' to "--..",
    '1' to ".----",
    '2' to "..---",
    '3' to "...--",
    '4' to "....-",
    '5' to ".....",
    '6' to "-....",
    '7' to "--...",
    '8' to "---..",
    '9' to "----.",
    '0' to "-----",
    '.' to ".-.-.-",
    ',' to "--..--",
    ':' to "---...",
    '?' to "..--..",
    '\'' to ".----.",
    '-' to "-....-",
    '/' to "-..-.",
    '(' to "-.--.",
    ')' to "-.--.-",
    '"' to ".-..-.",
    '=' to "-...-",
    '+' to ".-.-.",
    '@' to ".--.-.",
    ' ' to " ",
    'E' to "........"
)

private fun MutableList<String>.morse(s: String) {
    val chars = Normalizer.normalize(s, Normalizer.Form.NFKD)
            .toLowerCase(Locale.getDefault())
            .replace("\\s+".toRegex(), " ")
            .toCharArray()
    for (c in chars) if (IMCmap.containsKey(c)) add(IMCmap[c]!!)
}

private fun String.numPulses() = map { if (it == '-') 4 else 2 }.sum()

object Morse {
    const val unitsPerWord = 50 // PARIS method
    const val dotChar = '.'
    const val dashChar = '-'
    const val pauseChar = ' '

    @JvmStatic
    fun numPulses(mcs: Iterable<String>) = mcs.map { it.numPulses() + 2 }.sum()
    @JvmStatic
    fun morse(s: String): List<String> = ArrayList<String>(s.length).apply { morse(s) }
}