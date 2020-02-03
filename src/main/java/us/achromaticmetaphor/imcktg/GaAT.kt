package us.achromaticmetaphor.imcktg

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView

private val activities = arrayOf<Class<*>>(SelectContacts::class.java, DefaultToneInput::class.java, ChooseFilename::class.java)
private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1

class GaAT : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_layout)
        findViewById<ListView>(R.id.cmdlist).apply {
            adapter = ArrayAdapter(this@GaAT, android.R.layout.simple_list_item_1, arrayOf(getString(R.string.for_contacts), getString(R.string.for_default), getString(R.string.for_tofile)))
            onItemClickListener = OnItemClickListener { _, _, pos: Int, _ -> startActivity(Intent(this@GaAT, activities[pos])) }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.gaat, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onRequestPermissionsResult(rc: Int, permissions: Array<String>, results: IntArray) {
        if (rc == REQUEST_CODE_WRITE_EXTERNAL_STORAGE && results.size == 1 && results[0] == PackageManager.PERMISSION_DENIED) {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.about) {
            startActivity(Intent(this, About::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}