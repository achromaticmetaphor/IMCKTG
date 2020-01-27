package us.achromaticmetaphor.imcktg;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.SparseBooleanArray;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.annotation.NonNull;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_select_contacts)
@OptionsMenu(R.menu.contacts)
public class SelectContacts extends Activity {

  private static final int REQUEST_CODE_READ_CONTACTS = 1;

  @ViewById ListView list;

  @AfterViews
  protected void load() {
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
  public void onRequestPermissionsResult(int rc, @NonNull String [] permissions, @NonNull int [] results) {
    if (rc == REQUEST_CODE_READ_CONTACTS && results.length == 1 && results[0] == PackageManager.PERMISSION_DENIED) {
      finish();
    }
    else {
      loadContacts();
    }
  }

  @OptionsItem
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

  @OptionsItem
  protected void selectAll() {
    selectAll(true);
  }

  @OptionsItem
  protected void selectNone() {
    selectAll(false);
  }

  @Click
  public void confirm() {
    ConfirmContacts_.intent(this)
      .selection(list.getCheckedItemIds())
      .start();
    finish();
  }
}
