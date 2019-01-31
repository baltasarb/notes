package baltasarb.yama.services.localStrorage.daos

import androidx.room.*
import baltasarb.yama.model.TeamMemberByTeamId

@Dao
interface TeamMembersDao {

    @Query("SELECT * FROM teamMemberByTeamId WHERE teamId = :teamId")
    fun getTeamMembers(teamId: Int): List<TeamMemberByTeamId>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg teamMembers: TeamMemberByTeamId)

    @Query("DELETE FROM teamMemberByTeamId")
    fun deleteAll()

}