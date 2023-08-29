package us.achromaticmetaphor.imcktg;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class AboutText extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);
    final TextView about = findViewById(R.id.about);
    about.setMovementMethod(new ScrollingMovementMethod());
    about.setText(getIntent().getExtras().getInt("about_text"));
  }
}
