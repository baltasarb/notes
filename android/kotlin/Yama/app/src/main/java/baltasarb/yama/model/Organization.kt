package baltasarb.yama.model

import androidx.room.Entity

@Entity(tableName = "organization", primaryKeys = ["id"])
data class Organization(
    val login: String,
    val id: Int,
    val url: String,
    val members_url: String,
    val avatar_url: String,
    val description: String?
)