package us.achromaticmetaphor.imcktg;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.CheckBox;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_default_tone_input)
public class DefaultToneInput extends Activity {

  @ViewById TextView def_ringtone_text_box;
  @ViewById CheckBox ringtone_checkbox;
  @ViewById CheckBox notification_checkbox;
  private static final int REQUEST_CODE_WRITE_SETTINGS = 1;

  @AfterViews
  protected void load() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
    }
  }

  @OnActivityResult(REQUEST_CODE_WRITE_SETTINGS)
  protected void writeSettingsManaged() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
      finish();
    }
  }

  @Click
  public void confirm() {
    ConfirmContacts_.intent(this)
      .toneString(def_ringtone_text_box.getText().toString())
      .ringtone(ringtone_checkbox.isChecked())
      .notification(notification_checkbox.isChecked())
      .forDefault(true)
      .start();
    finish();
  }
}
