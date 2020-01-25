package us.achromaticmetaphor.imcktg;

import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_choose_filename)
public class ChooseFilename extends AppCompatActivity {

  @ViewById TextView def_ringtone_text_box;
  @ViewById TextView filename_text_box;

  @Click
  public void confirm() {
    ConfirmContacts_.intent(this)
      .forDefault(true)
      .toneString(def_ringtone_text_box.getText().toString())
      .filename(filename_text_box.getText().toString())
      .start();
    finish();
  }
}
