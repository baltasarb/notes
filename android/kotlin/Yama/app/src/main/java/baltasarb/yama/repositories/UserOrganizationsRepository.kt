package baltasarb.yama.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import baltasarb.yama.utils.Resource
import baltasarb.yama.YamaApplication
import baltasarb.yama.model.Organization
import baltasarb.yama.services.GithubApiService
import baltasarb.yama.services.runAsync
import org.json.JSONArray

class UserOrganizationsRepository(val application: YamaApplication) {

    fun getUserOrganizations(): LiveData<Resource<Array<Organization>>> {
        val resource = MutableLiveData<Resource<Array<Organization>>>()
        resource.value = Resource.loading()
        val local = application.database.organizationsDao()

        runAsync {
            local.getOrganization()
        }.andThen {

            if (it.isNotEmpty())
                resource.value = Resource.success(it.toTypedArray())
            else
                GithubApiService(application).requestUserOrganizations(
                    {
                        val organizations = getUserOrganizationsFromResponse(it)
                        runAsync {
                            local.insertAll(*organizations)
                        }
                        resource.value = Resource.success(organizations)
                    },
                    {
                        resource.value = Resource.error(it, null)
                    }
                )
        }

        return resource
    }

    private fun getUserOrganizationsFromResponse(response: JSONArray): Array<Organization> {
        val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)

        return mapper.readValue(response.toString(), Array<Organization>::class.java)
    }

}