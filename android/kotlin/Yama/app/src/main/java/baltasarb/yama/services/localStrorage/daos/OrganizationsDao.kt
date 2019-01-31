package baltasarb.yama.services.localStrorage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import baltasarb.yama.model.Organization

@Dao
interface OrganizationsDao {

    @Query("SELECT * FROM organization")
    fun getOrganization(): List<Organization>

    @Insert
    fun insertAll(vararg organization: Organization)

    @Query("DELETE FROM organization")
    fun deleteAll()
}