package us.achromaticmetaphor.imcktg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.speech.tts.TextToSpeech;

public class TTS extends ToneGenerator implements TextToSpeech.OnUtteranceCompletedListener {

  private TextToSpeech tts;
  private Map<String, Semaphore> semas;
  private final int repeatCount;
  private final float srate;
  private Context context;

  public TTS(TextToSpeech tts, Context ctx) {
    this(tts, 1.0f, 0.8f, 0, ctx);
  }

  public TTS(TextToSpeech tts, float pitch, float srate, int repeatCount, Context ctx) {
    this.tts = tts;
    this.repeatCount = repeatCount;
    tts.setOnUtteranceCompletedListener(this);
    tts.setPitch(pitch);
    tts.setSpeechRate(srate);
    semas = new ConcurrentHashMap<String, Semaphore>();
    this.srate = srate;
    this.context = ctx;
  }

  @Override
  public void writeTone(File tone, String s, boolean extend) throws IOException {
    String uid = UUID.randomUUID().toString();
    semas.put(uid, new Semaphore(0));
    HashMap<String, String> params = new HashMap<String, String>();
    params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, uid);
    tts.synthesizeToFile(s, params, Tone.tmpfile(tone).getAbsolutePath());
    semas.get(uid).acquireUninterruptibly();
    semas.remove(uid);
    Tone.tmpRename(tone);
    WAVETone wavetone = new WAVETone(tone);
    if (extend)
      wavetone.appendSilence((int) (2000 / srate));
    if (repeatCount > 0)
      wavetone.repeat(repeatCount);
    wavetone.close();
  }

  @Override
  public String filenameExt() {
    return ".wav";
  }

  @Override
  public void writeTone(OutputStream out, String s) throws IOException {
    File tmpfile = new File(context.getCacheDir(), "IMCKTG.speech");
    writeTone(tmpfile, s, false);
    try (FileInputStream input = new FileInputStream(tmpfile)) {
      byte[] buffer = new byte[8192];
      int bytes;
      do {
        bytes = input.read(buffer);
        out.write(buffer, 0, bytes);
      } while (bytes > 0);
    }
  }

  @Override
  public String filenameTypePrefix() {
    return "TextToSpeech:" + repeatCount + ":";
  }

  @Override
  public void onUtteranceCompleted(String uid) {
    semas.get(uid).release();
  }
}
