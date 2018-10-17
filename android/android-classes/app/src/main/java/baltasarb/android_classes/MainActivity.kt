package baltasarb.android_classes

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    val MAIN_ACTIVITY_TAG = "Main Activity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(MAIN_ACTIVITY_TAG, "On Create")
    }

    fun openUrlWithBrowser(view: View) {
        val textBox = findViewById<TextView>(R.id.editText)
        val uri = Uri.parse(textBox.text.toString())

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri

        startActivity(intent)
    }

    fun startSecondaryActivity(view: View) {
        val intent = Intent(this, SecondaryActivity::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        Log.i(MAIN_ACTIVITY_TAG, "On Start")
    }

    override fun onResume() {
        super.onResume()
        Log.i(MAIN_ACTIVITY_TAG, "On Resume")
    }

    override fun onPause() {
        super.onPause()
        Log.i(MAIN_ACTIVITY_TAG, "On Pause")
    }

    override fun onStop() {
        super.onStop()
        Log.i(MAIN_ACTIVITY_TAG, "On Stop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(MAIN_ACTIVITY_TAG, "On Destroy")
    }

    override fun onRestart() {
        super.onRestart()
    }

}
