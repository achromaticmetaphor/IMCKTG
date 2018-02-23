package us.achromaticmetaphor.imcktg;

import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.TextView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_default_tone_input)
public class DefaultToneInput extends AppCompatActivity {

  @ViewById TextView def_ringtone_text_box;
  @ViewById CheckBox ringtone_checkbox;
  @ViewById CheckBox notification_checkbox;

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
