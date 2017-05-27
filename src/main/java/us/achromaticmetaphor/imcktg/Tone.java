package us.achromaticmetaphor.imcktg;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;

public class Tone {

  private static final String extrakeyprefix = "us.achromaticmetaphor.imcktg";
  public static final String extrakeyRingtone = extrakeyprefix + ".ringtone";
  public static final String extrakeyNotification = extrakeyprefix + ".notification";
  public static final String extrakeyAlarm = extrakeyprefix + ".alarm";

  private File file;
  private Uri contenturi;

  public File file() {
    return file;
  }

  public Uri contentUri() {
    return contenturi;
  }

  private Tone(File f) {
    file = f;
  }

  protected static final String morsePostPause = "        ";
  private static final char[] hexdig = "0123456789ABCDEF".toCharArray();

  private static String hexPP(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(hexdig[b & 0xf]);
      sb.append(hexdig[(b >> 4) & 0xf]);
    }
    return sb.toString();
  }

  protected static String filenameTransform(String s) {
    try {
      MessageDigest dig = MessageDigest.getInstance("SHA-256");
      return hexPP(dig.digest(s.getBytes()));
    } catch (NoSuchAlgorithmException e) {
      return s;
    }
  }

  protected static String tmpFilename() {
    return filenameTransform("us.achromaticmetaphor.imcktg.preview");
  }

  private static File getToneFilename(Context c, String s, String ext, String typePrefix, Intent i) {
    String userFilename = i.getStringExtra(ConfirmContacts.extrakeyFilename);
    File rtdir;
    File tone;
    if (userFilename == null) {
      rtdir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES);
      tone = new File(rtdir, filenameTransform("us.achromaticmetaphor.imcktg:" + typePrefix + s) + ext);
    } else {
      rtdir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "IMCKTG");
      tone = new File(rtdir, userFilename + ext);
    }
    rtdir.mkdirs();
    return tone;
  }

  protected static File tmpfile(File f) {
    return new File(f.getAbsolutePath() + ".tmp");
  }

  protected static Tone generateTone(Context c, String s, ToneGenerator gen, File file) throws IOException {
    Tone tone = new Tone(file);
    gen.writeTone(tone.file(), s, true);
    tone.generateToneTail(c, s);
    return tone;
  }

  protected static Tone generateTone(Context c, String s, ToneGenerator gen, Intent i) throws IOException {
    return generateTone(c, s, gen, getToneFilename(c, s, gen.filenameExt(), gen.filenameTypePrefix(), i));
  }

  protected static void tmpRename(File tone) {
    tmpfile(tone).renameTo(tone);
  }

  protected void expunge(Context c) {
    c.getContentResolver().delete(toneStoreUri(c), MediaStore.Audio.Media.DATA + " = ?",
                                  new String[] {file().getAbsolutePath()});
  }

  protected void delete(Context c) {
    expunge(c);
    file().delete();
  }

  protected Uri toneStoreUri(Context c) {
    return MediaStore.Audio.Media.getContentUriForPath(file().getAbsolutePath());
  }

  protected void generateToneTail(Context c, String s) {
    expunge(c);
    ContentValues storevalues = new ContentValues();
    storevalues.put(MediaStore.Audio.Media.DATA, file().getAbsolutePath());
    storevalues.put(MediaStore.Audio.Media.TITLE, s);
    storevalues.put(MediaStore.Audio.Media.IS_MUSIC, false);
    storevalues.put(MediaStore.Audio.Media.IS_ALARM, false);
    storevalues.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
    storevalues.put(MediaStore.Audio.Media.IS_RINGTONE, false);
    contenturi = c.getContentResolver().insert(toneStoreUri(c), storevalues);
  }

  protected void assign(Context c, Uri contacturi) {
    ContentValues values = new ContentValues();
    values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, contentUri().toString());
    c.getContentResolver().update(contacturi, values, null, null);
  }

  protected void assignDefault(Context context, Intent intent) {
    if (intent.getBooleanExtra(extrakeyRingtone, false))
      RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, contentUri());
    if (intent.getBooleanExtra(extrakeyNotification, false))
      RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, contentUri());
    if (intent.getBooleanExtra(extrakeyAlarm, false))
      RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM, contentUri());
  }
}
