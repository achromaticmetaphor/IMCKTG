package us.achromaticmetaphor.imcktg;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CheckBox;
import android.widget.TextView;

public class DefaultToneInput extends Activity {

  private static final int REQUEST_CODE_WRITE_SETTINGS = 1;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_default_tone_input);
    findViewById(R.id.confirm).setOnClickListener(view -> {
      Intent intent = new Intent(this, ConfirmContacts.class);
      intent.putExtra("toneString", ((TextView) findViewById(R.id.def_ringtone_text_box)).getText().toString());
      intent.putExtra("ringtone", ((CheckBox) findViewById(R.id.ringtone_checkbox)).isChecked());
      intent.putExtra("notification", ((CheckBox) findViewById(R.id.notification_checkbox)).isChecked());
      intent.putExtra("forDefault", true);
      startActivity(intent);
      finish();
    });
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
        finish();
      }
    }
  }
}
