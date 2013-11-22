package us.achromaticmetaphor.imcktg;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import android.speech.tts.TextToSpeech;

public class TTS extends ToneGenerator implements TextToSpeech.OnUtteranceCompletedListener {

  private TextToSpeech tts;
  private Map<String, Semaphore> semas;

  public TTS(TextToSpeech tts) {
    this(tts, 1.0f, 0.8f);
  }

  public TTS(TextToSpeech tts, float pitch, float srate) {
    this.tts = tts;
    tts.setOnUtteranceCompletedListener(this);
    tts.setPitch(pitch);
    tts.setSpeechRate(srate);
    semas = new ConcurrentHashMap<String, Semaphore>();
  }

  @Override
  public void writeTone(File tone, String s, boolean extend) throws IOException {
    String uid = tone.getAbsolutePath();
    semas.put(uid, new Semaphore(0));
    HashMap<String, String> params = new HashMap<String, String>();
    params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, uid);
    tts.synthesizeToFile(s, params, Tone.tmpfile(tone).getAbsolutePath());
    semas.get(uid).acquireUninterruptibly();
    semas.remove(uid);
    Tone.tmpRename(tone);
    if (extend)
      Tone.waveAppendSilence(tone, 2);
  }

  @Override
  public String filenameExt() {
    return ".wav";
  }

  @Override
  public void writeTone(OutputStream out, String s, boolean extend) throws IOException {
    throw new IllegalArgumentException("method not implemented");
  }

  @Override
  public String filenameTypePrefix() {
    return "TextToSpeech:";
  }

  @Override
  public void onUtteranceCompleted(String uid) {
    semas.get(uid).release();
  }

}
