package us.achromaticmetaphor.imcktg

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView

private val cmds = arrayOf("About app", "About icons")
private val texts = intArrayOf(R.string.about_code, R.string.about_icons)

class About : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listview)
        findViewById<ListView>(R.id.cmdlist).apply {
            adapter = ArrayAdapter(this@About, android.R.layout.simple_list_item_1, cmds)
            onItemClickListener = OnItemClickListener { _, _, pos: Int, _ ->
                startActivity(Intent(this@About, AboutText::class.java).apply { putExtra("about_text", texts[pos]) })
            }
        }
    }
}