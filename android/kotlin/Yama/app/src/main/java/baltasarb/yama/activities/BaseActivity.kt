package baltasarb.yama.activities

import android.content.Intent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import baltasarb.yama.R
import baltasarb.yama.utils.RequestResultWrapper
import baltasarb.yama.utils.Resource
import baltasarb.yama.YamaApplication
import baltasarb.yama.services.runAsync

abstract class BaseActivity : AppCompatActivity() {

    private var spinnerId: Int = R.id.spinnerProgressBar

    protected fun setSignOutListener() {
        //set the listener to sign out button
        val signOutButton = findViewById<View>(R.id.signOutButton)
        signOutButton.setOnClickListener { signOutOnClick() }
    }

    private fun signOutOnClick() {
        getYamaApplication().deletePreferences()
        runAsync {
            getYamaApplication().database.clearDatabase()
        }
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    protected fun <T> handleRequestedData(data: LiveData<Resource<T>>) {
        data.observe(this, Observer {
            when (it.status) {
                Resource.Status.LOADING -> {
                    requestLoadingHandler()
                }
                Resource.Status.ERROR -> {
                    stopSpinner()
                    requestErrorHandler(it.message!!)
                }
                Resource.Status.SUCCESS -> {
                    stopSpinner()
                    requestSuccessHandler(RequestResultWrapper(it.data!!))
                }
            }
        })
    }

    protected fun <T> handleRequestedDataArray(data: LiveData<Resource<Array<T>>>) {
        data.observe(this, Observer {
            when (it.status) {
                Resource.Status.LOADING -> {
                    requestLoadingHandler()
                }
                Resource.Status.ERROR -> {
                    stopSpinner()
                    requestErrorHandler(it.message!!)
                }
                Resource.Status.SUCCESS -> {
                    stopSpinner()
                    requestSuccessHandler(RequestResultWrapper(it.data!!))
                }
            }
        })
    }

    protected fun startSpinner() {
        val spinner = findViewById<ProgressBar>(spinnerId)
        spinner.visibility = View.VISIBLE
    }

    protected fun stopSpinner() {
        val spinner = findViewById<ProgressBar>(spinnerId)
        spinner.visibility = View.GONE
    }

    protected open fun requestLoadingHandler() {
        val errorContainer = findViewById<ConstraintLayout>(R.id.errorInclude)
        if (errorContainer.visibility == View.VISIBLE) {
            errorContainer.visibility = View.GONE
        }
        startSpinner()
    }

    protected open fun requestErrorHandler(errorMessage: String) {
        val errorContainer = findViewById<ConstraintLayout>(R.id.errorInclude)
        if (errorContainer.visibility == View.GONE) {
            errorContainer.visibility = View.VISIBLE
            findViewById<TextView>(R.id.errorDetailMessage).text = errorMessage
        }
    }

    protected abstract fun getYamaApplication(): YamaApplication

    protected abstract fun <T> requestSuccessHandler(result: RequestResultWrapper<T>)

}