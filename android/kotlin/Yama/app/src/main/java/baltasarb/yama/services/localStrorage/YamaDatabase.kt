package baltasarb.yama.services.localStrorage

import androidx.room.*
import baltasarb.yama.model.*
import baltasarb.yama.services.localStrorage.daos.*

@Database(
    entities = [
        User::class,
        Organization::class,
        TeamByOrganizationId::class,
        TeamMemberByTeamId::class,
        Avatar::class
    ]
    , version = 6
)
@TypeConverters(AvatarTypeConverters::class)
abstract class YamaDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun organizationsDao(): OrganizationsDao
    abstract fun teamsDao(): TeamsDao
    abstract fun teamMembersDao(): TeamMembersDao
    abstract fun avatarsDao(): AvatarsDao

    fun clearDatabase() {
        userDao().deleteAll()
        organizationsDao().deleteAll()
        teamsDao().deleteAll()
        teamMembersDao().deleteAll()
        avatarsDao().deleteAll()
    }
}