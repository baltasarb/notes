package baltasarb.android.activityLifecycle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import baltasarb.android.R

class Activity1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_1)
    }
}
