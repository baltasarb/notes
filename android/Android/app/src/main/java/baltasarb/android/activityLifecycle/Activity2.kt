package baltasarb.android.activityLifecycle

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import baltasarb.android.R

class Activity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)
        Log.i("Lifecycle Activity 2", "On create.")
    }

    override fun onStart() {
        super.onStart()
        Log.i("Lifecycle Activity 2", "On start.")
    }

    override fun onResume() {
        super.onResume()
        Log.i("Lifecycle Activity 2", "On resume.")
    }

    override fun onPause() {
        super.onPause()
        Log.i("Lifecycle Activity 2", "On pause.")
    }

    override fun onStop() {
        super.onStop()
        Log.i("Lifecycle Activity 2", "On stop.")
    }

    override fun onRestart() {
        super.onRestart()
        Log.i("Lifecycle Activity 2", "On restart.")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("Lifecycle Activity 2", "On destroy.")
    }

    public fun lifecycleGoToActivity1Button(view : View){
        val intent = Intent(this, Activity1::class.java)
        intent.putExtra("fieldName", "value")
        startActivity(intent)
    }
}
