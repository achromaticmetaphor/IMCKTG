package us.achromaticmetaphor.imcktg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ChooseFilename extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_choose_filename);
    findViewById(R.id.confirm).setOnClickListener(view -> {
      Intent intent = new Intent(this, ConfirmContacts.class);
      intent.putExtra("forDefault", true);
      intent.putExtra("toneString", ((TextView) findViewById(R.id.def_ringtone_text_box)).getText().toString());
      intent.putExtra("filename", ((TextView) findViewById(R.id.filename_text_box)).getText().toString());
      startActivity(intent);
      finish();
    });
  }
}
