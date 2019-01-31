package baltasarb.yama.messaging

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class ChatMessage(

    val message: String?,
    val userId: String?,
    val avatarUrl : String?,
    //@created is populated by the server with the server's time of this object's creation
    @ServerTimestamp var created: Date? = null
) {
    //for firestore serialization on get
    constructor() : this(null, null,null, null)
}