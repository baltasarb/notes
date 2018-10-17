package baltasarb.android_classes

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

class SecondaryActivity : AppCompatActivity() {

    val SECONDARY_ACTIVITY_TAG = "Secondary Activity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secondary)
        Log.i("Secondary Activity", "On Create")

    }

    fun backToMainActivity(view : View){
        finish()
    }


    override fun onStart() {
        super.onStart()
        Log.i(SECONDARY_ACTIVITY_TAG, "On Start")
    }

    override fun onResume() {
        super.onResume()
        Log.i(SECONDARY_ACTIVITY_TAG, "On Resume")
    }

    override fun onPause() {
        super.onPause()
        Log.i(SECONDARY_ACTIVITY_TAG, "On Pause")
    }

    override fun onStop() {
        super.onStop()
        Log.i(SECONDARY_ACTIVITY_TAG, "On Stop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(SECONDARY_ACTIVITY_TAG, "On Destroy")
    }
}
