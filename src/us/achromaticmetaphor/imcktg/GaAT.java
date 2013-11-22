package us.achromaticmetaphor.imcktg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class GaAT extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ga_at);
  }

  public void chooseContacts(View view) {
    startActivity(new Intent(this, SelectContacts.class));
  }

  public void inputDefaultString(View view) {
    startActivity(new Intent(this, DefaultToneInput.class));
  }

}
