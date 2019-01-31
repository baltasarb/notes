package baltasarb.yama.services

import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import baltasarb.yama.YamaApplication
import org.json.JSONArray

class GithubApiService(val application: YamaApplication) {

    companion object {
        private const val githubApiUrl = "https://api.github.com"
        private const val userRequestUrl = "$githubApiUrl/user"
        private const val orgsRequestUrl = "$userRequestUrl/orgs"
        private const val organizationTeamsUrl = "$githubApiUrl/orgs/%s/teams"
        private const val teamMembersUrl = "$githubApiUrl/teams/%s/members"
    }

    fun requestUser(successCallback: (JSONObject) -> Unit, errorMessageHandler: (String) -> Unit) {
        val jsonObjectRequest = JsonRequestFactory(
            userRequestUrl,
            successCallback,
            errorMessageHandler
        )
            .addHeader("Authorization", "token ${application.token}")
            .buildJsonObjectRequest()
            .setShouldCache(false)

        application.queue.add(jsonObjectRequest)
    }

    fun requestUserOrganizations(successCallback: (JSONArray) -> Unit, errorMessageHandler: (String) -> Unit) {
        val jsonObjectRequest = JsonRequestFactory(
            orgsRequestUrl,
            successCallback,
            errorMessageHandler
        )
            .addHeader("Authorization", "token ${application.token}")
            .buildJsonArrayRequest()

        application.queue.add(jsonObjectRequest)
    }

    fun requestTeamMembers(teamId: Int, successCallback: (JSONArray) -> Unit, errorMessageHandler: (String) -> Unit) {
        val teamUrl = String.format(teamMembersUrl, teamId)

        val jsonArrayRequest = JsonRequestFactory(
            teamUrl,
            successCallback,
            errorMessageHandler
        )
            .addHeader("Authorization", "token ${application.token}")
            .buildJsonArrayRequest()

        application.queue.add(jsonArrayRequest)
    }

    fun requestTeams(
        organizationName: String,
        successCallback: (JSONArray) -> Unit,
        errorMessageHandler: (String) -> Unit
    ) {
        val url = String.format(organizationTeamsUrl, organizationName)

        val jsonArrayRequest = JsonRequestFactory(url, successCallback, errorMessageHandler)
            .addHeader("Authorization", "token ${application.token}")
            .buildJsonArrayRequest()

        application.queue.add(jsonArrayRequest)
    }

    private class JsonRequestFactory<T>(
        val url: String,
        val successCallback: (T) -> Unit,
        val errorMessageHandler: (String) -> Unit
    ) {

        var newHeaders = HashMap<String, String>()

        fun buildJsonObjectRequest(): JsonObjectRequest {
            return object : JsonObjectRequest(
                Method.GET, url, null,
                Response.Listener { response -> successCallback.invoke(response as T) },
                Response.ErrorListener { error -> defaultErrorCallback(error) }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    return newHeaders
                }
            }
        }

        fun buildJsonArrayRequest(): JsonArrayRequest {
            return object : JsonArrayRequest(
                Method.GET, url, null,
                Response.Listener { response -> successCallback.invoke(response as T) },
                Response.ErrorListener { error -> defaultErrorCallback(error) }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    return newHeaders
                }
            }
        }

        fun addHeader(key: String, value: String): JsonRequestFactory<T> {
            newHeaders[key] = value
            return this
        }

        fun defaultErrorCallback(error: VolleyError) {
            when (error) {
                is TimeoutError -> //This indicates that the request has either time out or there is no connection
                    errorMessageHandler("The request has timed out.")
                is NoConnectionError -> errorMessageHandler("Connection not found.")
                is AuthFailureError -> //Error indicating that there was an Authentication Failure while performing the request
                    errorMessageHandler("Authentication error.")
                is ServerError -> //Indicates that the server responded with a error response
                    errorMessageHandler("Server error.")
                is NetworkError -> //Indicates that there was network error while performing the request
                    errorMessageHandler("Network error.")
                is ParseError -> // Indicates that the server response could not be parsed
                    errorMessageHandler("Error parsing the response from the server.")
            }
        }
    }
}
