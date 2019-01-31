package baltasarb.yama.services.localStrorage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import baltasarb.yama.model.Avatar

@Dao
interface AvatarsDao {

    @Query("SELECT * FROM avatar WHERE avatarUrl = :avatar_url")
    fun getAvatar(avatar_url: String): Avatar?

    @Insert
    fun insertAll(vararg avatar: Avatar)

    @Query("DELETE FROM avatar")
    fun deleteAll()

}