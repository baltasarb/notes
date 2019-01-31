package baltasarb.yama.services.localStrorage.daos

import androidx.room.*
import baltasarb.yama.model.TeamByOrganizationId

@Dao
interface TeamsDao {

    @Query(value = "SELECT * FROM teamByOrganizationId WHERE organizationId = :organizationId")
    fun getTeams(organizationId: String): List<TeamByOrganizationId>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg teamsByOrgId: TeamByOrganizationId)

    @Query("DELETE FROM teamByOrganizationId")
    fun deleteAll()

}
