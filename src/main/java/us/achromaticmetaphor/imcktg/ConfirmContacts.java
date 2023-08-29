package us.achromaticmetaphor.imcktg;

import java.io.IOException;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class ConfirmContacts extends Activity implements TextToSpeech.OnInitListener {

  private TextToSpeech tts;
  private String previewText;
  private ProgressDialog pdia;

  SeekBar WPM_input;
  SeekBar FREQ_input;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_confirm_contacts);
    tts = new TextToSpeech(this, this);

    final Intent intent = getIntent();
    if (intent.getBooleanExtra("forDefault", false))
      previewText = getIntent().getStringExtra("toneString");
    else {
      long [] selection = intent.getLongArrayExtra("selection");
      if (selection.length == 0)
        previewText = "preview";
      else
        previewText = nameForContact(contactUriForID(selection[0]));
    }

    WPM_input = findViewById(R.id.WPM_input);
    WPM_input.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        ((TextView) findViewById(R.id.WPM_hint)).setText("" + wpm() + " wpm");
      }
      public void onStartTrackingTouch(SeekBar seekBar) {}
      public void onStopTrackingTouch(SeekBar seekBar) {}
    });

    FREQ_input = findViewById(R.id.FREQ_input);
    FREQ_input.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        ((TextView) findViewById(R.id.FREQ_hint)).setText("" + freqRescaled(20, 4410) + "Hz / " + freqNote().toUpperCase(Locale.getDefault()) + freqOctave());
      }
      public void onStartTrackingTouch(SeekBar seekBar) {}
      public void onStopTrackingTouch(SeekBar seekBar) {}
    });

    findViewById(R.id.generate).setOnClickListener(view -> generateAndAssignTones(spinnerGen()));
    findViewById(R.id.preview).setOnClickListener(view -> previewTone(spinnerGen()));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    tts.shutdown();
  }

  protected void dismissPdia() {
    runOnUiThread(pdia::dismiss);
  }

  protected void announceFailure(String message)
  {
    runOnUiThread(() -> {
      dismissPdia();
      new AlertDialog.Builder(this)
      .setMessage(message)
        .setTitle("Something untoward has occurred.")
        .setPositiveButton("Okay", (DialogInterface di, int b) -> finish())
        .show();
    });
  }

  private Uri contactUriForID(long id) {
    return Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, "" + id);
  }

  private String nameForContact(Uri contacturi) {
    Cursor cursor = getContentResolver().query(contacturi,
                                               new String[] {ContactsContract.Contacts.DISPLAY_NAME},
                                               null, null, null);
    cursor.moveToNext();
    return cursor.getString(0);
  }

  public void generateAndAssignTones(ToneGenerator gen) {
    pdia = ProgressDialog.show(this, "Generating", "Please wait", true, false);
    if (getIntent().getBooleanExtra("forDefault", false))
      generateDefaultTones(gen);
    else
      generateContactTones(gen);
  }

  protected void generateDefaultTones(ToneGenerator gen) {
    new Thread(() -> {
        try {
          Intent intent = getIntent();
          Tone.generateTone(this, intent.getStringExtra("toneString"), gen, intent.getStringExtra("filename"))
              .assignDefault(this, intent.getBooleanExtra("ringtone", false), intent.getBooleanExtra("notification", false), false);
        } catch (IOException e) {
          announceFailure("Ringtone could not be generated.");
        }
        dismissPdia();
    }).start();
  }

  protected void generateContactTones(ToneGenerator gen) {
    new Thread(() -> {
      Intent intent = getIntent();
      for (long id : intent.getLongArrayExtra("selection")) {
        final Uri contacturi = contactUriForID(id);
        final String name = nameForContact(contacturi);
        try {
          Tone.generateTone(this, name, gen, intent.getStringExtra("filename")).assign(this, contacturi);
        } catch (IOException e) {
          announceFailure("Ringtone could not be generated: " + name);
        }
      }
      dismissPdia();
    }).start();
  }

  public ToneGenerator spinnerGen() {
    Object sel = ((Spinner) findViewById(R.id.format_spinner)).getSelectedItem();
    return sel.equals("Morse (WAV)") ? pcmGen() : sel.equals("Morse (iMelody)") ? imyGen() : ttsGen();
  }

  private class OAFCL implements AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener {
    private final AudioManager aman;
    private final Tone preview;

    public OAFCL(AudioManager aman, Tone preview) {
      this.aman = aman;
      this.preview = preview;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {}

    @Override
    public void onCompletion(MediaPlayer player) {
      player.release();
      aman.abandonAudioFocus(this);
      preview.delete(ConfirmContacts.this);
    }
  }

  private int freqRescaled(int min, int max) {
    final int freq = FREQ_input.getProgress();
    final int pmax = FREQ_input.getMax();
    return min + ((max - min) * freq / pmax);
  }

  private float freqRescaled() {
    return (freqRescaled(20, 4410) + 2195) / 2195.0f;
  }

  private int wpm() {
    return 1 + WPM_input.getProgress();
  }

  private ToneGenerator pcmGen() {
    return new MorsePCM(freqRescaled(20, 4410), wpm(), repeatCount());
  }

  private int repeatCount() {
    try {
      return Integer.parseInt(((EditText) findViewById(R.id.RC_input)).getText().toString());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  private int freqTone() {
    return freqRescaled(0, 62);
  }

  private int freqOctave() {
    return freqTone() / 7;
  }

  private String freqNote() {
    return "cdefgab".substring(freqTone() % 7).substring(0, 1);
  }

  private ToneGenerator imyGen() {
    return new MorseIMelody(freqOctave(), freqNote(), wpm(), repeatCount());
  }

  private ToneGenerator ttsGen() {
    return new TTS(tts, freqRescaled(), wpm() / 20.0f, repeatCount(), this);
  }

  public void previewTone(ToneGenerator gen) {
    AudioManager aman = (AudioManager) getSystemService(AUDIO_SERVICE);
    MediaPlayer player;
    try {
      Tone preview = Tone.generateTone(this, previewText, gen, Tone.tmpFilename());
      player = MediaPlayer.create(this, preview.contentUri());
      OAFCL oafcl = new OAFCL(aman, preview);
      aman.requestAudioFocus(oafcl, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
      player.setOnCompletionListener(oafcl);
      player.start();
    } catch (IOException e) {
    }
  }

  @Override
  public void onInit(int status) {
  }
}
