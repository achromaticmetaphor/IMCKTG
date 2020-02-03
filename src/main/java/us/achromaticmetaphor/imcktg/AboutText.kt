package us.achromaticmetaphor.imcktg

import android.app.Activity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView

class AboutText : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        findViewById<TextView>(R.id.about).apply {
            movementMethod = ScrollingMovementMethod()
            setText(intent.extras!!.getInt("about_text"))
        }
    }
}