package baltasarb.yama.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import baltasarb.yama.utils.Resource
import baltasarb.yama.YamaApplication
import baltasarb.yama.model.Team
import baltasarb.yama.services.GithubApiService
import baltasarb.yama.model.TeamByOrganizationId
import baltasarb.yama.services.runAsync
import org.json.JSONArray

class OrganizationTeamsRepository(val application: YamaApplication) {

    fun getOrganizationTeams(organizationId: String): LiveData<Resource<Array<Team>>> {
        val resource = MutableLiveData<Resource<Array<Team>>>()
        resource.value = Resource.loading()
        val local = application.database.teamsDao()

        runAsync {
            local
                .getTeams(organizationId)
        }.andThen {
            if (it.isNotEmpty())
                resource.value = Resource.success(it.map {
                    it.team
                }.toTypedArray())
            else
                GithubApiService(application).requestTeams(
                    organizationId,
                    {
                        val respTeams = getOrganizationTeamsFromResponse(it)
                        runAsync {
                            val teamByOrganizationId = respTeams
                                .map { TeamByOrganizationId(organizationId, it) }
                                .toTypedArray()
                            local.insertAll(*teamByOrganizationId)
                        }
                        resource.value = Resource.success(respTeams)
                    },
                    {
                        resource.value = Resource.error(it, null)
                    }
                )

        }

        return resource
    }

    private fun getOrganizationTeamsFromResponse(response: JSONArray): Array<Team> {
        val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)

        return mapper.readValue(response.toString(), Array<Team>::class.java)
    }

}
