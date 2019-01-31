package baltasarb.yama.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import baltasarb.yama.utils.Resource
import baltasarb.yama.YamaApplication
import baltasarb.yama.model.User
import baltasarb.yama.services.GithubApiService
import baltasarb.yama.services.runAsync
import org.json.JSONObject

class UserRepository(val application: YamaApplication) {

    fun getUser(): LiveData<Resource<User>> {
        val resource = MutableLiveData<Resource<User>>()
        resource.value = Resource.loading()
        val local = application.database.userDao()

        runAsync {
            local.getUser()
        }.andThen {

            if (it != null)
                resource.value = Resource.success(it)
            else
                GithubApiService(application).requestUser(
                    {
                        val user = getUserFromResponse(it)
                        runAsync {
                            local.insertUser(user)
                        }
                        resource.value = Resource.success(user)
                    },
                    {
                        resource.value = Resource.error(it, null)
                    }
                )

        }
        return resource
    }

    private fun getUserFromResponse(response: JSONObject): User {
        val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        return mapper.readValue(response.toString(), User::class.java)
    }

}