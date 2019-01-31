package baltasarb.yama.services.localStrorage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import baltasarb.yama.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM user")
    fun getUser(): User?

    @Update
    fun updateUser(user: User)

    @Insert
    fun insertUser(user: User)

    @Query("DELETE FROM user")
    fun deleteAll()
}