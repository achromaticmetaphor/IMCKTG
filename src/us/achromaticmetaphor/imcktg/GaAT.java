package us.achromaticmetaphor.imcktg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class GaAT extends Activity {

  private final String menuAbout = "About";

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

  public void inputToFile(View view) {
    startActivity(new Intent(this, ChooseFilename.class));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(menuAbout);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem mi) {
    super.onOptionsItemSelected(mi);
    if (mi.getTitle().equals(menuAbout))
      startActivity(new Intent(this, About.class));
    return true;
  }

}
