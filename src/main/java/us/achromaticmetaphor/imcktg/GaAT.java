package us.achromaticmetaphor.imcktg;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.list_layout)
public class GaAT extends AppCompatActivity {

  private static final Class<?>[] activities = {SelectContacts_.class, DefaultToneInput_.class, ChooseFilename_.class};
  private static final String menuAbout = "About";

  @ViewById ListView cmdlist;

  @AfterViews
  protected void load() {
    String[] choices = {getString(R.string.for_contacts), getString(R.string.for_default), getString(R.string.for_tofile)};
    cmdlist.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, choices));
    cmdlist.setOnItemClickListener((AdapterView<?> av, View v, int pos, long id) -> startActivity(new Intent(this, activities[pos])));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    MenuItem about = menu.add(menuAbout);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
      about.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    about.setIcon(R.drawable.ic_action_about);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem mi) {
    super.onOptionsItemSelected(mi);
    if (mi.getTitle().equals(menuAbout))
      About_.intent(this).start();
    return true;
  }
}
