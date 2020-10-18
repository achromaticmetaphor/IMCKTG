package us.achromaticmetaphor.imcktg

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.CheckBox
import android.widget.TextView

private const val REQUEST_CODE_WRITE_SETTINGS = 1

class DefaultToneInput : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default_tone_input)
        findViewById<View>(R.id.confirm).setOnClickListener {
            startActivity(Intent(this, ConfirmContacts::class.java).apply {
                putExtra("toneString", (findViewById<View>(R.id.def_ringtone_text_box) as TextView).text.toString())
                putExtra("ringtone", (findViewById<View>(R.id.ringtone_checkbox) as CheckBox).isChecked)
                putExtra("notification", (findViewById<View>(R.id.notification_checkbox) as CheckBox).isChecked)
                putExtra("forDefault", true)
            })
            finish()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
            startActivityForResult(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }, REQUEST_CODE_WRITE_SETTINGS)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
                finish()
            }
        }
    }
}