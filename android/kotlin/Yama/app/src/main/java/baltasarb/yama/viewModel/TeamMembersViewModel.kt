package baltasarb.yama.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import baltasarb.yama.utils.Resource
import baltasarb.yama.YamaApplication
import baltasarb.yama.model.TeamMember
import baltasarb.yama.repositories.TeamMembersRepository

class TeamMembersViewModel(val application: YamaApplication): ViewModel(){

    fun getTeamMembers(teamId: Int): LiveData<Resource<Array<TeamMember>>>
            = TeamMembersRepository(application).getTeamMembers(teamId)

}