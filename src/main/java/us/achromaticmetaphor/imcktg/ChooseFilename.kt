package us.achromaticmetaphor.imcktg

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView

class ChooseFilename : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_filename)
        findViewById<View>(R.id.confirm).setOnClickListener {
            startActivity(Intent(this, ConfirmContacts::class.java).apply {
                putExtra("forDefault", true)
                putExtra("toneString", (findViewById<View>(R.id.def_ringtone_text_box) as TextView).text.toString())
                putExtra("filename", (findViewById<View>(R.id.filename_text_box) as TextView).text.toString())
            })
            finish()
        }
    }
}