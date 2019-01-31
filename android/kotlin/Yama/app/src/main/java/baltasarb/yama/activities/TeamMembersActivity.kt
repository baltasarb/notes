package baltasarb.yama.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import baltasarb.yama.R
import baltasarb.yama.utils.RequestResultWrapper
import baltasarb.yama.YamaApplication
import baltasarb.yama.model.TeamMember
import baltasarb.yama.utils.TeamMembersAdapter
import baltasarb.yama.viewModel.TeamMembersViewModel
import kotlinx.android.synthetic.main.activity_team_members.*

class TeamMembersActivity : BaseActivity() {

    companion object {
        const val TEAM_ID_MESSAGE = "baltasarb.yama.TEAM_ID"
        const val AVATAR_URL_MESSAGE: String = "baltasarb.yama.AVATAR_URL"
    }

    private lateinit var teamId: String
    lateinit var avatarUrl : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_members)
        setView()

        teamId = intent.getIntExtra(OrganizationTeamsActivity.TEAM_ID_MESSAGE, 0).toString()

        val model = TeamMembersViewModel(getYamaApplication())
        handleRequestedDataArray(model.getTeamMembers(teamId.toInt()))
    }

    private fun setView() {
        val teamName = intent.getStringExtra(OrganizationTeamsActivity.TEAM_NAME_MESSAGE)
        val teamDescription = intent.getStringExtra(OrganizationTeamsActivity.TEAM_DESCRIPTION_MESSAGE)

        teamNameTextView.text = teamName

        if (teamDescription != null && !teamDescription.isEmpty()) {
            teamDescriptionTextView.text = teamDescription
        }
        setSignOutListener()
    }

    override fun getYamaApplication(): YamaApplication {
        return this@TeamMembersActivity.application as YamaApplication
    }

    override fun <T> requestSuccessHandler(result: RequestResultWrapper<T>) {
        val teamMembers = result.dataArray as Array<TeamMember>
        teamMembersListView.adapter = TeamMembersAdapter(getYamaApplication(), teamMembers)
        chatButton.isEnabled = true
    }

    fun openChat(view: View) {
        val intent = Intent(getYamaApplication(), ChatActivity::class.java)
        intent.putExtra(TEAM_ID_MESSAGE, teamId)
        startActivity(intent)
    }

}
