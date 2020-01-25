package us.achromaticmetaphor.imcktg;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.list_layout)
public class GaAT extends AppCompatActivity {

  private static final Class<?>[] activities = {SelectContacts_.class, DefaultToneInput_.class, ChooseFilename_.class};
  private static final String menuAbout = "About";
  private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;

  @ViewById ListView cmdlist;

  @AfterViews
  protected void load() {
    String[] choices = {getString(R.string.for_contacts), getString(R.string.for_default), getString(R.string.for_tofile)};
    cmdlist.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, choices));
    cmdlist.setOnItemClickListener((AdapterView<?> av, View v, int pos, long id) -> startActivity(new Intent(this, activities[pos])));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String [] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
    }
  }

  @Override
  public void onRequestPermissionsResult(int rc, @NonNull String [] permissions, @NonNull int [] results) {
    if (rc == REQUEST_CODE_WRITE_EXTERNAL_STORAGE && results.length == 1 && results[0] == PackageManager.PERMISSION_DENIED) {
      finish();
    }
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
