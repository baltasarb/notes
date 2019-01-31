package baltasarb.yama.messaging

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import baltasarb.yama.R
import baltasarb.yama.YamaApplication

class ChatMessageListAdapter(private val app: YamaApplication, private val chatMessages: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private enum class ViewType { SENT_MESSAGE, RECEIVED_MESSAGE }
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    // Determines the appropriate ViewType according to the sender of the message.
    override fun getItemViewType(position: Int): Int {
        val message = chatMessages[position]

        return if (message.userId == app.userId) {
            // If the current user is the sender of the message
            ViewType.SENT_MESSAGE.ordinal
        } else {
            // If some other user sent the message
            ViewType.RECEIVED_MESSAGE.ordinal
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View

        if (viewType == ViewType.SENT_MESSAGE.ordinal) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.sent_message, parent, false)
            return SentMessageHolder(view)
        }

        view = LayoutInflater.from(parent.context).inflate(R.layout.received_message, parent, false)
        return ReceivedMessageHolder(view)
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = chatMessages[position]

        when (holder.itemViewType) {
            ViewType.SENT_MESSAGE.ordinal -> (holder as SentMessageHolder).bind(message)
            ViewType.RECEIVED_MESSAGE.ordinal -> (holder as ReceivedMessageHolder).bind(message)
        }
    }

    private inner class SentMessageHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var messageText: TextView = itemView.findViewById(R.id.text_message_body)
        internal var timeText: TextView = itemView.findViewById(R.id.text_message_time)

        internal fun bind(chatMessage: ChatMessage) {
            messageText.text = chatMessage.message
            timeText.text = chatMessage.created.toString()
        }
    }

    private inner class ReceivedMessageHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var messageText: TextView = itemView.findViewById(R.id.text_message_body)
        internal var timeText: TextView = itemView.findViewById(R.id.text_message_time)
        internal var nameText: TextView = itemView.findViewById(R.id.text_message_name)
        internal var avatarImage: ImageView = itemView.findViewById(R.id.image_message_profile)

        internal fun bind(chatMessage: ChatMessage) {
            messageText.text = chatMessage.message
            timeText.text = chatMessage.created.toString()
            nameText.text = chatMessage.userId
            app.avatarsRepository.getAvatar(chatMessage.avatarUrl!!) {
                avatarImage.setImageBitmap(it.img)
            }
        }
    }

}