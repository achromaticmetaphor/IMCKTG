package us.achromaticmetaphor.imcktg;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class GaAT extends Activity {

  private static final Class<?>[] activities = {SelectContacts.class, DefaultToneInput.class, ChooseFilename.class};
  private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;
  static final boolean useScopedStorage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.list_layout);
    String[] choices = {getString(R.string.for_contacts), getString(R.string.for_default), getString(R.string.for_tofile)};
    ListView cmdlist = findViewById(R.id.cmdlist);
    cmdlist.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, choices));
    cmdlist.setOnItemClickListener((AdapterView<?> av, View v, int pos, long id) -> startActivity(new Intent(this, activities[pos])));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !useScopedStorage && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String [] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.gaat, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public void onRequestPermissionsResult(int rc, String [] permissions, int [] results) {
    if (rc == REQUEST_CODE_WRITE_EXTERNAL_STORAGE && results.length == 1 && results[0] == PackageManager.PERMISSION_DENIED) {
      finish();
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.about) {
      startActivity(new Intent(this, About.class));
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
