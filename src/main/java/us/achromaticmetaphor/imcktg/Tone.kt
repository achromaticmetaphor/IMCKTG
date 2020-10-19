package us.achromaticmetaphor.imcktg

import android.content.ContentValues
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Environment
import android.provider.ContactsContract
import android.provider.MediaStore
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

private val hexdig = "0123456789ABCDEF".toCharArray()

private fun ByteArray.hex(): String {
    val sb = StringBuilder()
    for (b in this) {
        sb.append(hexdig[b.toInt() and 0xf])
        sb.append(hexdig[b.toInt() shr 4 and 0xf])
    }
    return sb.toString()
}

private fun ByteArray.sha256() = MessageDigest.getInstance("SHA-256").digest(this)

private fun String.hexFN(): String {
    return try {
        this.toByteArray().sha256().hex()
    } catch (e: NoSuchAlgorithmException) {
        this
    }
}

private fun getToneFilename(c: Context, s: String, ext: String, typePrefix: String, userFilename: String?): File {
    val root = if (userFilename == null)
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES)
    else
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "IMCKTG")
    root.mkdirs()
    val filename = userFilename ?: "us.achromaticmetaphor.imcktg:$typePrefix$s".hexFN()
    return File(root, filename + ext)
}

class Tone private constructor(private val file: File) {
    private var contenturi: Uri? = null
    fun file(): File {
        return file
    }

    fun contentUri() = contenturi

    private fun expunge(c: Context) {
        c.contentResolver.delete(toneStoreUri(c)!!, MediaStore.Audio.Media.DATA + " = ?", arrayOf(file().absolutePath))
    }

    fun delete(c: Context) {
        expunge(c)
        file().delete()
    }

    private fun toneStoreUri(c: Context) = MediaStore.Audio.Media.getContentUriForPath(file().absolutePath)

    fun generateToneTail(gen: ToneGenerator, c: Context, s: String) {
        expunge(c)
        val storevalues = ContentValues().apply {
            if (!useScopedStorage) {
                put(MediaStore.Audio.Media.DATA, file().absolutePath)
            }
            put(MediaStore.Audio.Media.TITLE, s)
            put(MediaStore.Audio.Media.IS_MUSIC, false)
            put(MediaStore.Audio.Media.IS_ALARM, false)
            put(MediaStore.Audio.Media.IS_NOTIFICATION, true)
            put(MediaStore.Audio.Media.IS_RINGTONE, true)
        }
        if (useScopedStorage) {
            contenturi = c.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, storevalues)
            c.contentResolver.openOutputStream(contenturi!!).use { output ->
                gen.writeTone(output!!, s, true)
            }
        }
        else {
            contenturi = c.contentResolver.insert(toneStoreUri(c)!!, storevalues)
        }
    }

    fun assign(c: Context, contacturi: Uri) {
        val values = ContentValues()
        values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, contenturi.toString())
        c.contentResolver.update(contacturi, values, null, null)
    }

    private fun assignDefault(c: Context, type: Int, contenturi: Uri?) {
        RingtoneManager.setActualDefaultRingtoneUri(c, type, contenturi)
    }

    fun assignDefault(context: Context, ringtone: Boolean, notification: Boolean, alarm: Boolean) {
        if (ringtone) assignDefault(context, RingtoneManager.TYPE_RINGTONE, contenturi)
        if (notification) assignDefault(context, RingtoneManager.TYPE_NOTIFICATION, contenturi)
        if (alarm) assignDefault(context, RingtoneManager.TYPE_ALARM, contenturi)
    }

    companion object {
        const val morsePostPause = "        "

        @Throws(IOException::class)
        fun generateTone(c: Context, s: String, gen: ToneGenerator, file: File): Tone {
            return Tone(file).apply{
                if (!useScopedStorage) {
                    gen.writeTone(file(), s, false)
                }
                generateToneTail(gen, c, s)
            }
        }

        @Throws(IOException::class)
        fun generateTone(c: Context, s: String, gen: ToneGenerator, userFilename: String?): Tone {
            return generateTone(c, s, gen, getToneFilename(c, s, gen.filenameExt(), gen.filenameTypePrefix(), userFilename))
        }

        @JvmStatic
        fun tmpfile(f: File) = File(f.absolutePath + ".tmp")
        @JvmStatic
        fun tmpRename(tone: File) = tmpfile(tone).renameTo(tone)
        fun tmpFilename() = "us.achromaticmetaphor.imcktg.preview".hexFN()
    }

}