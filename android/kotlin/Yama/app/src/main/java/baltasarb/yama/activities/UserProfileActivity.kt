package baltasarb.yama.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import baltasarb.yama.R
import baltasarb.yama.utils.RequestResultWrapper
import baltasarb.yama.YamaApplication
import baltasarb.yama.model.User
import kotlinx.android.synthetic.main.activity_user_profile.*
import android.graphics.Bitmap


class UserProfileActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val user = intent.getParcelableExtra<User>(SignInActivity.USER_MESSAGE)
        setView(user)
    }

    private fun setView(user: User) {
        getYamaApplication().avatarUrl = user.avatar_url
        setAvatar(user)
        nameTextView.text = user.name
        followersTextView.text = user.followers.toString()
        followingTextView.text = user.following.toString()
        if (user.email != null)
            emailTextView.text = user.email
        setSignOutListener()
    }

    private fun setAvatar(user: User) {
        getYamaApplication().avatarsRepository.getAvatar(user.avatar_url) {
            val myBitmap = it.img
            val emptyBitmap = Bitmap.createBitmap(myBitmap.width, myBitmap.height, myBitmap.config)
            if (!myBitmap.sameAs(emptyBitmap))
                avatarImageView.setImageBitmap(myBitmap)
        }
    }

    fun openOrganizations(view: View) {
        val intent = Intent(this, OrganizationsActivity::class.java)
        startActivity(intent)
    }

    override fun getYamaApplication(): YamaApplication {
        return this@UserProfileActivity.application as YamaApplication
    }

    override fun <T> requestSuccessHandler(result: RequestResultWrapper<T>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
