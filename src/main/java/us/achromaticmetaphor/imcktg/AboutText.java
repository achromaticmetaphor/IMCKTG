package us.achromaticmetaphor.imcktg;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class AboutText extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);
    TextView tv = (TextView) findViewById(R.id.about);
    tv.setMovementMethod(new ScrollingMovementMethod());
    tv.setText(getIntent().getIntExtra("about_text", 0));
  }
}
