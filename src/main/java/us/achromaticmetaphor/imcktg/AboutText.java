package us.achromaticmetaphor.imcktg;

import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_about)
public class AboutText extends AppCompatActivity {

  @ViewById TextView about;
  @Extra int about_text = 0;

  @AfterViews
  protected void load() {
    about.setMovementMethod(new ScrollingMovementMethod());
    about.setText(about_text);
  }
}
