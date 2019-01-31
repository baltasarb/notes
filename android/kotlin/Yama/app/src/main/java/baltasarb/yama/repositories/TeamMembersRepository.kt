package baltasarb.yama.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import baltasarb.yama.utils.Resource
import baltasarb.yama.YamaApplication
import baltasarb.yama.model.TeamMember
import baltasarb.yama.services.GithubApiService
import baltasarb.yama.model.TeamMemberByTeamId
import baltasarb.yama.services.runAsync
import org.json.JSONArray

class TeamMembersRepository(val application: YamaApplication) {

    fun getTeamMembers(teamId: Int): LiveData<Resource<Array<TeamMember>>> {
        val resource = MutableLiveData<Resource<Array<TeamMember>>>()
        resource.value = Resource.loading()
        val local = application.database.teamMembersDao()

        runAsync {
            local.getTeamMembers(teamId)
        }.andThen {

            if (it.isNotEmpty())
                resource.value = Resource.success(it.map { it.teamMember }.toTypedArray())
            else
                GithubApiService(application).requestTeamMembers(
                    teamId,
                    {
                        val teamMembers = getTeamMembersFromResponse(it)
                        runAsync {
                            local.insertAll(*teamMembers.map { TeamMemberByTeamId(teamId, it) }.toTypedArray())
                        }
                        resource.value = Resource.success(teamMembers)
                    },
                    {
                        resource.value = Resource.error(it, null)
                    }
                )
        }

        return resource
    }

    private fun getTeamMembersFromResponse(response: JSONArray): Array<TeamMember> {
        val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)

        return mapper.readValue(response.toString(), Array<TeamMember>::class.java)
    }

}
