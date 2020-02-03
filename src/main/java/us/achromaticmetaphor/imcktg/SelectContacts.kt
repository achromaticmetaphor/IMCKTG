package us.achromaticmetaphor.imcktg

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.SimpleCursorAdapter

private const val REQUEST_CODE_READ_CONTACTS = 1

class SelectContacts : Activity() {
    var list: ListView? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_contacts)
        list = findViewById(R.id.list)
        findViewById<View>(R.id.confirm).setOnClickListener {
            startActivity(Intent(this, ConfirmContacts::class.java).apply {
                putExtra("selection", list!!.checkedItemIds)
            })
            finish()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS), REQUEST_CODE_READ_CONTACTS)
        } else {
            loadContacts()
        }
    }

    private fun loadContacts() {
        val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, arrayOf(ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER),
                ContactsContract.Contacts.DISPLAY_NAME + " is not null and " + ContactsContract.Contacts.HAS_PHONE_NUMBER,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " asc")
        list!!.adapter = SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_checked,
                cursor, arrayOf(ContactsContract.Contacts.DISPLAY_NAME), intArrayOf(android.R.id.text1))
    }

    override fun onRequestPermissionsResult(rc: Int, permissions: Array<String>, results: IntArray) {
        if (rc == REQUEST_CODE_READ_CONTACTS && results.size == 1 && results[0] == PackageManager.PERMISSION_DENIED) {
            finish()
        } else {
            loadContacts()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.contacts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        val list = list!!
        if (itemId == R.id.invertSelection) {
            for (i in 0 until list.count) list.setItemChecked(i, !list.checkedItemPositions[i])
            return true
        }
        if (itemId == R.id.selectAll) {
            for (i in 0 until list.count) list.setItemChecked(i, true)
            return true
        }
        if (itemId == R.id.selectNone) {
            for (i in 0 until list.count) list.setItemChecked(i, false)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}