package baltasarb.yama.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import baltasarb.yama.utils.Resource
import baltasarb.yama.YamaApplication
import baltasarb.yama.model.Team
import baltasarb.yama.repositories.OrganizationTeamsRepository

class OrganizationTeamsViewModel(val application: YamaApplication): ViewModel() {

    fun getOrganizationTeams(organizationId: String): LiveData<Resource<Array<Team>>>
            = OrganizationTeamsRepository(application).getOrganizationTeams(organizationId)

}