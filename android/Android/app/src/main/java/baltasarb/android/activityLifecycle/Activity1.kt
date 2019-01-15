package baltasarb.android.activityLifecycle

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import baltasarb.android.R

/*
    To test activity lifecycle this activity should be set
    as the main entry point of the project
 */
class Activity1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_1)
        Log.i("Lifecycle Activity 1", "On create.")
    }

    override fun onStart() {
        super.onStart()
        Log.i("Lifecycle Activity 1", "On start.")
    }

    override fun onResume() {
        super.onResume()
        Log.i("Lifecycle Activity 1", "On resume.")
    }

    override fun onPause() {
        super.onPause()
        Log.i("Lifecycle Activity 1", "On pause.")
    }

    override fun onStop() {
        super.onStop()
        Log.i("Lifecycle Activity 1", "On stop.")
    }

    override fun onRestart() {
        super.onRestart()
        Log.i("Lifecycle Activity 1", "On restart.")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("Lifecycle Activity 1", "On destroy.")
    }

    public fun lifecycleGoToActivity2Button(view: View) {
        val intent = Intent(this, Activity2::class.java)
        startActivity(intent)
    }
}
