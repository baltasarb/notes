package baltasarb.yama.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import baltasarb.yama.R
import baltasarb.yama.utils.RequestResultWrapper
import baltasarb.yama.YamaApplication
import baltasarb.yama.model.Team
import baltasarb.yama.utils.OrganizationTeamsAdapter
import baltasarb.yama.viewModel.OrganizationTeamsViewModel
import kotlinx.android.synthetic.main.activity_organization_teams.*

class OrganizationTeamsActivity : BaseActivity() {

    companion object {
        const val TEAM_ID_MESSAGE = "baltasarb.yama.TEAM_ID"
        const val TEAM_NAME_MESSAGE = "baltasarb.yama.TEAM_NAME"
        const val TEAM_DESCRIPTION_MESSAGE = "baltasarb.yama.TEAM_DESCRIPTION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization_teams)
        setSignOutListener()

        val model = OrganizationTeamsViewModel(getYamaApplication())

        val organizationName = intent.getStringExtra(OrganizationsActivity.ORGANIZATION_NAME_MESSAGE)
        organizationNameTextView.text = organizationName
        requestAndSetAvatar()

        handleRequestedDataArray(model.getOrganizationTeams(organizationName))
    }

    private fun requestAndSetAvatar() {
        val avatarUrl = intent.getStringExtra(OrganizationsActivity.ORGANIZATION_AVATAR_URL_MESSAGE)
        getYamaApplication().avatarsRepository.getAvatar(avatarUrl) {
            val myBitmap = it.img
            val emptyBitmap = Bitmap.createBitmap(myBitmap.width, myBitmap.height, myBitmap.config)
            if (!myBitmap.sameAs(emptyBitmap))
                selectedOrganizationAvatarImageView.setImageBitmap(myBitmap)
        }
    }

    private fun onTeamClickCallback(team: Team) {
        val intent = Intent(this, TeamMembersActivity::class.java)

        intent.putExtra(TEAM_NAME_MESSAGE, team.name)
        intent.putExtra(TEAM_DESCRIPTION_MESSAGE, team.description)
        intent.putExtra(TEAM_ID_MESSAGE, team.id)

        startActivity(intent)
    }

    override fun getYamaApplication(): YamaApplication {
        return this@OrganizationTeamsActivity.application as YamaApplication
    }

    override fun <T> requestSuccessHandler(result: RequestResultWrapper<T>) {
        val teams = result.dataArray as Array<Team>
        organizationTeamsListView.adapter = OrganizationTeamsAdapter(getYamaApplication(), teams)
        organizationTeamsListView.setOnItemClickListener { _, _, position, _ ->
            onTeamClickCallback(teams[position])
        }
    }

}
