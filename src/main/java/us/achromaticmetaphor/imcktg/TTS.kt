package us.achromaticmetaphor.imcktg

import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener
import us.achromaticmetaphor.imcktg.Tone.Companion.tmpRename
import us.achromaticmetaphor.imcktg.Tone.Companion.tmpfile
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore

class TTS @JvmOverloads constructor(private val tts: TextToSpeech, pitch: Float = 1.0f, private val srate: Float = 0.8f, private val repeatCount: Int = 0) : ToneGenerator(), OnUtteranceCompletedListener {
    private val semas = ConcurrentHashMap<String, Semaphore>()

    @Throws(IOException::class)
    override fun writeTone(tone: File, s: String, extend: Boolean) {
        val uid = UUID.randomUUID().toString()
        semas[uid] = Semaphore(0)
        val params = HashMap<String, String>()
        params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = uid
        tts.synthesizeToFile(s, params, tmpfile(tone).absolutePath)
        semas[uid]!!.acquireUninterruptibly()
        semas.remove(uid)
        tmpRename(tone)
        WAVETone(tone).apply{
            if (extend) appendSilence((2000 / srate).toInt())
            if (repeatCount > 0) repeat(repeatCount)
            close()
        }
    }

    @Throws(IOException::class)
    override fun writeTone(out: OutputStream, s: String) = throw IllegalArgumentException("method not implemented")
    override fun filenameTypePrefix() = "TextToSpeech:$repeatCount:"
    override fun onUtteranceCompleted(uid: String) = semas[uid]!!.release()
    override fun filenameExt() = ".wav"

    init {
        tts.setOnUtteranceCompletedListener(this)
        tts.setPitch(pitch)
        tts.setSpeechRate(srate)
    }
}