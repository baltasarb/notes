package baltasarb.yama.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import baltasarb.yama.R
import baltasarb.yama.utils.RequestResultWrapper
import baltasarb.yama.utils.Resource
import baltasarb.yama.YamaApplication
import baltasarb.yama.model.User
import baltasarb.yama.viewModel.UserViewModel
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : BaseActivity() {

    companion object {
        const val USER_MESSAGE: String = "baltasarb.yama.USER_MESSAGE"
    }

    private lateinit var model: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = getYamaApplication()
        model = UserViewModel(app)

        if (app.userIsLoggedIn()) {
            setContentView(R.layout.sign_in_saved_user)
            getExistingUser()
        } else {
            setContentView(R.layout.activity_sign_in)
        }
    }

    private fun getExistingUser() {
        model.getUser().observe(this, Observer {
            when (it.status) {
                Resource.Status.LOADING -> {
                    requestLoadingHandler()
                }
                Resource.Status.ERROR -> {
                    errorMessageHandler(it.message!!)
                    getYamaApplication().deletePreferences()
                    startActivity(Intent(this, SignInActivity::class.java))
                    finish()
                }
                Resource.Status.SUCCESS -> {
                    requestSuccessHandler(RequestResultWrapper(it.data!!))
                }
            }
        })
    }

    fun signIn(view: View) {
        val userId = editTextUserId.text.toString()
        val token = editTextToken.text.toString()

        if (userId.isEmpty() || token.isEmpty()) {
            errorMessageHandler("User Id and Token required.")
            return
        }

        getYamaApplication().savePreferences(token, userId)
        handleRequestedData(model.getUser())
    }

    private fun errorMessageHandler(error: String) {
        val duration = Toast.LENGTH_LONG
        val toast = Toast.makeText(this, error, duration)
        toast.show()
    }

    override fun getYamaApplication(): YamaApplication {
        return this@SignInActivity.application as YamaApplication
    }

    override fun <T> requestSuccessHandler(result: RequestResultWrapper<T>) {
        val user = result.data as User
        if (!getYamaApplication().userIdIsValid(user.login)) {
            errorMessageHandler("Token does not match with user")
            return
        }

        val intent = Intent(this, UserProfileActivity::class.java)
        intent.putExtra(USER_MESSAGE, user)
        startActivity(intent)
        finish()
    }

    override fun requestErrorHandler(errorMessage: String) {
        getYamaApplication().deletePreferences()
        errorMessageHandler(errorMessage)
    }

    override fun requestLoadingHandler() {
        startSpinner()
    }

}
