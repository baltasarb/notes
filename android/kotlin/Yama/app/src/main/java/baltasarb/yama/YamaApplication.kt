package baltasarb.yama

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.firebase.FirebaseApp
import baltasarb.yama.messaging.Chat
import baltasarb.yama.repositories.AvatarsRepository
import baltasarb.yama.services.localStrorage.YamaDatabase

class YamaApplication : Application() {

    lateinit var queue: RequestQueue
    lateinit var database: YamaDatabase
    lateinit var avatarsRepository: AvatarsRepository
    lateinit var chat: Chat

    lateinit var avatarUrl : String
    var token: String = ""
    var userId: String = ""

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        getPreferences()
        queue = Volley.newRequestQueue(this)
        database = Room
            .databaseBuilder(this, YamaDatabase::class.java, "yama-db")
            .build()

        avatarsRepository = AvatarsRepository(this)
        chat = Chat(this)
    }

    private fun getPreferences() {
        val preferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        this.token = preferences.getString(getString(R.string.preference_token), "")!!
        this.userId = preferences.getString(getString(R.string.preference_user_id), "")!!
    }

    fun savePreferences(token: String, userId: String) {
        val preferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(getString(R.string.preference_token), token)
        editor.putString(getString(R.string.preference_user_id), userId)
        editor.apply()
        this.token = token
        this.userId = userId
    }

    fun deletePreferences() {
        val preferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        preferences.edit().clear().apply()
        this.token = ""
        this.userId = ""
    }

    fun userIsLoggedIn() = token.isNotEmpty() && userId.isNotEmpty()

    fun userIdIsValid(responseUserId: String) = if (responseUserId == userId) true else {
        deletePreferences()
        false
    }

}