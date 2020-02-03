package us.achromaticmetaphor.imcktg

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

abstract class ToneGenerator {
    @Throws(IOException::class)
    abstract fun writeTone(out: OutputStream, s: String)

    @Throws(IOException::class)
    fun writeTone(out: OutputStream, s: String, extend: Boolean) =
      writeTone(out, if (extend) s + Tone.morsePostPause else s)

    @Throws(IOException::class)
    open fun writeTone(out: File, s: String, extend: Boolean) =
      writeTone(BufferedOutputStream(FileOutputStream(out)), s, extend)

    abstract fun filenameExt(): String
    abstract fun filenameTypePrefix(): String
}