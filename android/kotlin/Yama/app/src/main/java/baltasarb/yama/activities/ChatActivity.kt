package baltasarb.yama.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import baltasarb.yama.R
import baltasarb.yama.YamaApplication
import baltasarb.yama.messaging.ChatMessage
import baltasarb.yama.messaging.ChatMessageListAdapter
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val teamId = intent.getStringExtra(TeamMembersActivity.TEAM_ID_MESSAGE)

        val app = this@ChatActivity.application as YamaApplication
        val chat = app.chat
        chat.loadTeamChat(teamId)

        messageRecyclerView.layoutManager = LinearLayoutManager(this)

        chat.messages.observe(this, Observer {
            messageRecyclerView.adapter = ChatMessageListAdapter(app, it)
            messageRecyclerView.scrollToPosition(it.size - 1)
        })

        sendButton.setOnClickListener {
            val messageToSend = messageToPost.text.toString()
            val message = ChatMessage(messageToSend, app.userId, app.avatarUrl)

            if (messageToSend.isNotBlank()) {
                chat.post(message, teamId)
                messageToPost.text.clear()
            }
        }
    }

}