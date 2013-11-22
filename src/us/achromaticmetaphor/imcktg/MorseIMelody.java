package us.achromaticmetaphor.imcktg;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MorseIMelody extends ToneGenerator {

  private static final int defaultWordsPerMinute = 20;
  private static final int defaultOctave = IMelodyFormat.defaultOctave;
  private static final String defaultTone = "a";

  public static void main(String [] argV) throws IOException {
    (new MorseIMelody()).writeIMelody((OutputStream) System.out, argV[0]);
  }

  public void writeIMelody(OutputStream out, String s) throws IOException {
    PrintStream pout = new PrintStream(out);
    writeIMelody(pout, s);
    if (pout.checkError())
      throw new IOException();
  }

  public void writeIMelody(PrintStream out, String s) {
    writeIMelodyHeader(out, s);
    morseMelody(out, Morse.morse(s));
    writeIMelodyFooter(out);
  }

  private void writeIMelodyHeader(PrintStream out, String s) {
    IMelodyFormat.writeRequiredHeaders(out);
    IMelodyFormat.writeNameHeaderMangled(out, s);
    IMelodyFormat.writeBeatHeader(out, beat);
    IMelodyFormat.writeStyleHeader(out, IMelodyFormat.styleContinuous);
    IMelodyFormat.writeVolumeHeader(out, IMelodyFormat.volumeMax);
  }

  private static void writeIMelodyFooter(PrintStream out) {
    IMelodyFormat.writeRequiredFooters(out);
  }

  private final Map<Character, String> tones;
  private final int beat;

  public MorseIMelody() {
    this(defaultWordsPerMinute);
  }

  public MorseIMelody(int wpm) {
    this(defaultOctave, defaultTone, wpm);
  }

  public MorseIMelody(int octave, String tone, int wpm) {
    if (wpm < 1 || wpm > 144)
      throw new IllegalArgumentException("invalid wpm : " + wpm);

    final int duration = wpm <= 36 ? 3 : wpm <= 72 ? 4 : 5;
    // The format defines BEAT as beats per minute, at common (4/4) time.
    beat = wpm * Morse.unitsPerWord * 4 / (1 << duration);

    final String note = IMelodyFormat.note(octave, tone);
    final String rest = IMelodyFormat.rest;
    final String dot_duration = IMelodyFormat.duration(duration);
    final String dash_duration = IMelodyFormat.durationDotted(duration - 1);
    final String pause_duration = dot_duration;
    final String double_pause_duration = IMelodyFormat.duration(duration - 1);

    final String dot = note + dot_duration;
    final String dash = note + dash_duration;
    final String pause = rest + pause_duration;
    final String double_pause = rest + double_pause_duration;

    Map<Character, String> m = new HashMap<Character, String>();
    m.put(Morse.dotChar, dot + pause);
    m.put(Morse.dashChar, dash + pause);
    m.put(Morse.pauseChar, double_pause);
    tones = Collections.unmodifiableMap(m);
  }

  private void morseMelody(PrintStream out, Iterable<String> mcs) {
    IMelodyFormat.writeBeginMelody(out);
    for (String s : mcs)
      morseMelody(out, s);
    IMelodyFormat.writeEndMelody(out);
  }

  private void morseMelody(PrintStream out, String s) {
    for (int i = 0; i < s.length(); i++)
      out.print(tones.get(s.charAt(i)));
    out.print(tones.get(Morse.pauseChar));
    out.print(IMelodyFormat.lineContinuation);
  }

  @Override
  public void writeTone(OutputStream out, String s, boolean extend) throws IOException {
    writeIMelody(out, extend ? s + Tone.morsePostPause : s);
  }

  @Override
  public String filenameExt() {
    return ".imy";
  }

  @Override
  public String filenameTypePrefix() {
    return "iMelody:" + beat + ":" + tones.get(Morse.dotChar) + ":";
  }

}
