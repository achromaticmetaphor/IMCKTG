package us.achromaticmetaphor.imcktg;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class SelectContacts extends Activity {

  private static final int REQUEST_CODE_READ_CONTACTS = 1;

  ListView list;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_select_contacts);
    list = findViewById(R.id.list);
    findViewById(R.id.confirm).setOnClickListener(view -> {
      Intent intent = new Intent(this, ConfirmContacts.class);
      intent.putExtra("selection", list.getCheckedItemIds());
      startActivity(intent);
      finish();
    });
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED)) {
      requestPermissions(new String [] {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
    }
    else {
      loadContacts();
    }
  }

  private void loadContacts() {
    Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                                               new String[] {ContactsContract.Contacts._ID,
                                                             ContactsContract.Contacts.DISPLAY_NAME,
                                                             ContactsContract.Contacts.HAS_PHONE_NUMBER},
                                               ContactsContract.Contacts.DISPLAY_NAME + " is not null and " + ContactsContract.Contacts.HAS_PHONE_NUMBER,
                                               null,
                                               ContactsContract.Contacts.DISPLAY_NAME + " asc");
    list.setAdapter(new SimpleCursorAdapter(this,
                                                     android.R.layout.simple_list_item_checked,
                                                     cursor,
                                                     new String[] {ContactsContract.Contacts.DISPLAY_NAME},
                                                     new int[] {android.R.id.text1}));
  }

  @Override
  public void onRequestPermissionsResult(int rc, String [] permissions, int [] results) {
    if (rc == REQUEST_CODE_READ_CONTACTS && results.length == 1 && results[0] == PackageManager.PERMISSION_DENIED) {
      finish();
    }
    else {
      loadContacts();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.contacts, menu);
    return super.onCreateOptionsMenu(menu);
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == R.id.invertSelection) {
      invertSelection();
      return true;
    }
    if (itemId == R.id.selectAll) {
      selectAll(true);
      return true;
    }
    if (itemId == R.id.selectNone) {
      selectAll(false);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  protected void invertSelection() {
    final int count = list.getCount();
    final SparseBooleanArray selected = list.getCheckedItemPositions();
    for (int i = 0; i < count; i++)
      list.setItemChecked(i, !selected.get(i));
  }

  private void selectAll(boolean b) {
    final int count = list.getCount();
    for (int i = 0; i < count; i++)
      list.setItemChecked(i, b);
  }
}
