package baltasarb.yama.activities

import android.content.Intent
import android.os.Bundle
import baltasarb.yama.utils.OrganizationAdapter
import baltasarb.yama.R
import baltasarb.yama.utils.RequestResultWrapper
import baltasarb.yama.YamaApplication
import baltasarb.yama.model.Organization
import baltasarb.yama.viewModel.UserOrganizationsViewModel

class OrganizationsActivity : BaseActivity() {

    companion object {
        const val ORGANIZATION_NAME_MESSAGE = "baltasarb.yama.ORGANIZATION_NAME"
        const val ORGANIZATION_AVATAR_URL_MESSAGE = "baltasarb.yama.ORGANIZATION_AVATAR_URL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organizations)
        setSignOutListener()

        val model = UserOrganizationsViewModel(getYamaApplication())
        handleRequestedDataArray(model.getUserOrganizations())
    }

    private fun onOrganizationClickCallback(organization: Organization) {
        val intent = Intent(this, OrganizationTeamsActivity::class.java)

        intent.putExtra(ORGANIZATION_NAME_MESSAGE, organization.login)
        intent.putExtra(ORGANIZATION_AVATAR_URL_MESSAGE, organization.avatar_url)

        startActivity(intent)
    }

    override fun <T> requestSuccessHandler(result: RequestResultWrapper<T>) {
        val organizations = result.dataArray as Array<Organization>
        listViewOrganizations.adapter = OrganizationAdapter(getYamaApplication(), organizations)
        listViewOrganizations.setOnItemClickListener { _, _, position, _ ->
            onOrganizationClickCallback(organizations[position])
        }
    }

    override fun getYamaApplication(): YamaApplication {
        return this@OrganizationsActivity.application as YamaApplication
    }

}

