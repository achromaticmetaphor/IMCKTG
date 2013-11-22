package us.achromaticmetaphor.imcktg;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class SelectContacts extends ListActivity {

  private final String menuSelectAll = "Select all";
  private final String menuSelectNone = "Select none";
  private final String menuInvertSelection = "Invert selection";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_select_contacts);
    Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                                               new String[] {ContactsContract.Contacts._ID,
                                                             ContactsContract.Contacts.DISPLAY_NAME,
                                                             ContactsContract.Contacts.HAS_PHONE_NUMBER},
                                               ContactsContract.Contacts.DISPLAY_NAME + " is not null and " + ContactsContract.Contacts.HAS_PHONE_NUMBER,
                                               null,
                                               ContactsContract.Contacts.DISPLAY_NAME + " asc");
    getListView().setAdapter(new SimpleCursorAdapter(this,
                                                     android.R.layout.simple_list_item_checked,
                                                     cursor,
                                                     new String[] {ContactsContract.Contacts.DISPLAY_NAME},
                                                     new int[] {android.R.id.text1}));
  }

  private void invertSelection() {
    final ListView lv = getListView();
    final int count = lv.getCount();
    final SparseBooleanArray selected = lv.getCheckedItemPositions();
    for (int i = 0; i < count; i++)
      lv.setItemChecked(i, ! selected.get(i));
  }

  private void selectAll(boolean b) {
    final ListView lv = getListView();
    final int count = lv.getCount();
    for (int i = 0; i < count; i++)
      lv.setItemChecked(i, b);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(menuSelectAll);
    menu.add(menuInvertSelection);
    menu.add(menuSelectNone);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem mi) {
    super.onOptionsItemSelected(mi);
    if (mi.getTitle().equals(menuInvertSelection))
      invertSelection();
    if (mi.getTitle().equals(menuSelectAll))
      selectAll(true);
    if (mi.getTitle().equals(menuSelectNone))
      selectAll(false);
    return true;
  }

  public void confirmContacts(View view) {
    long [] selection = getListView().getCheckedItemIds();
    Intent intent = new Intent(this, ConfirmContacts.class);
    intent.putExtra(ConfirmContacts.extrakeySelection, selection);
    startActivity(intent);
    finish();
  }

}
