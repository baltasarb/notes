package baltasarb.yama.model

import androidx.room.Embedded
import androidx.room.Entity

@Entity(tableName = "teamMemberByTeamId", primaryKeys = ["teamId", "TeamMember_login"])
class TeamMemberByTeamId (
    val teamId: Int,
    @Embedded(prefix = "TeamMember_")
    val teamMember: TeamMember
)