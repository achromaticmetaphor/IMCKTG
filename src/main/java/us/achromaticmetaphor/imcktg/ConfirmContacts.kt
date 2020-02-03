package us.achromaticmetaphor.imcktg

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Spinner
import android.widget.TextView
import java.io.IOException
import java.util.Locale

class ConfirmContacts : Activity(), OnInitListener {
    private var tts: TextToSpeech? = null
    private var previewText: String? = null
    private var pdia: ProgressDialog? = null
    var WPM_input: SeekBar? = null
    var FREQ_input: SeekBar? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_contacts)
        tts = TextToSpeech(this, this)
        previewText = if (intent.getBooleanExtra("forDefault", false)) intent.getStringExtra("toneString") else {
            val selection = intent.getLongArrayExtra("selection")!!
            if (selection.isEmpty()) "preview" else nameForContact(contactUriForID(selection[0]))
        }
        WPM_input = findViewById<SeekBar>(R.id.WPM_input).apply {
            setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    (findViewById<View>(R.id.WPM_hint) as TextView).text = "" + wpm() + " wpm"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }
        FREQ_input = findViewById<SeekBar>(R.id.FREQ_input).apply {
            setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    (findViewById<View>(R.id.FREQ_hint) as TextView).text = "" + freqRescaled(20, 4410) + "Hz / " + freqNote().toUpperCase(Locale.getDefault()) + freqOctave()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }
        findViewById<View>(R.id.generate).setOnClickListener { generateAndAssignTones(spinnerGen()) }
        findViewById<View>(R.id.preview).setOnClickListener { previewTone(spinnerGen()) }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.shutdown()
    }

    private fun dismissPdia() {
        runOnUiThread { pdia?.dismiss() }
    }

    private fun announceFailure(message: String?) {
        runOnUiThread {
            dismissPdia()
            AlertDialog.Builder(this)
                    .setMessage(message)
                    .setTitle("Something untoward has occurred.")
                    .setPositiveButton("Okay") { _, _ -> finish() }
                    .show()
        }
    }


    private fun nameForContact(contacturi: Uri): String {
        val cursor = contentResolver.query(contacturi, arrayOf(ContactsContract.Contacts.DISPLAY_NAME), null, null, null)!!
        cursor.moveToNext()
        return cursor.getString(0)
    }

    fun generateAndAssignTones(gen: ToneGenerator) {
        pdia = ProgressDialog.show(this, "Generating", "Please wait", true, false)
        if (intent.getBooleanExtra("forDefault", false)) generateDefaultTones(gen) else generateContactTones(gen)
    }

    protected fun generateDefaultTones(gen: ToneGenerator) {
        Thread(Runnable {
            try {
                Tone.generateTone(this, intent.getStringExtra("toneString"), gen, intent.getStringExtra("filename"))
                        .assignDefault(this, intent.getBooleanExtra("ringtone", false), intent.getBooleanExtra("notification", false), false)
            } catch (e: IOException) {
                announceFailure("Ringtone could not be generated.")
            }
            dismissPdia()
        }).start()
    }

    protected fun generateContactTones(gen: ToneGenerator) {
        Thread(Runnable {
            for (id in intent.getLongArrayExtra("selection")!!) {
                val contacturi = contactUriForID(id)
                val name = nameForContact(contacturi)
                try {
                    Tone.generateTone(this, name, gen, intent.getStringExtra("filename")).assign(this, contacturi)
                } catch (e: IOException) {
                    announceFailure("Ringtone could not be generated: $name")
                }
            }
            dismissPdia()
        }).start()
    }

    fun spinnerGen(): ToneGenerator {
        val sel = (findViewById<View>(R.id.format_spinner) as Spinner).selectedItem
        return if (sel == "Morse (WAV)") pcmGen() else if (sel == "Morse (iMelody)") imyGen() else ttsGen()
    }

    private inner class OAFCL(private val aman: AudioManager, private val preview: Tone) : OnAudioFocusChangeListener, OnCompletionListener {
        override fun onAudioFocusChange(focusChange: Int) {}
        override fun onCompletion(player: MediaPlayer) {
            player.release()
            aman.abandonAudioFocus(this)
            preview.delete(this@ConfirmContacts)
        }

    }


    private fun repeatCount(): Int {
        return try {
            (findViewById<View>(R.id.RC_input) as EditText).text.toString().toInt()
        } catch (nfe: NumberFormatException) {
            0
        }
    }

    private fun freqRescaled(min: Int, max: Int) = min + (max - min) * FREQ_input!!.progress / FREQ_input!!.max
    private fun contactUriForID(id: Long) = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, "" + id)
    private fun freqRescaled() = (freqRescaled(20, 4410) + 2195) / 2195.0f
    private fun wpm() = 1 + WPM_input!!.progress
    private fun pcmGen(): ToneGenerator = MorsePCM(freqRescaled(20, 4410), wpm(), repeatCount())
    private fun freqTone() = freqRescaled(0, 62)
    private fun freqOctave() = freqTone() / 7
    private fun freqNote() = "cdefgab".substring(freqTone() % 7).substring(0, 1)
    private fun imyGen(): ToneGenerator = MorseIMelody(freqOctave(), freqNote(), wpm(), repeatCount())
    private fun ttsGen(): ToneGenerator = TTS(tts!!, freqRescaled(), wpm() / 20.0f, repeatCount())

    fun previewTone(gen: ToneGenerator) {
        val aman = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val player: MediaPlayer
        try {
            val preview = Tone.generateTone(this, previewText!!, gen, Tone.tmpFilename())
            player = MediaPlayer.create(this, preview.contentUri())
            val oafcl = OAFCL(aman, preview)
            aman.requestAudioFocus(oafcl, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            player.setOnCompletionListener(oafcl)
            player.start()
        } catch (e: IOException) {
        }
    }

    override fun onInit(status: Int) {}
}