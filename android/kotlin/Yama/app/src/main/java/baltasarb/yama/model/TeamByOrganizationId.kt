package baltasarb.yama.model

import androidx.room.Embedded
import androidx.room.Entity

@Entity(tableName = "teamByOrganizationId", primaryKeys = ["organizationId", "Team_id"])
class TeamByOrganizationId(
    val organizationId: String,
    @Embedded(prefix = "Team_")
    val team: Team
)
