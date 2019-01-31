package baltasarb.yama.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import baltasarb.yama.utils.Resource
import baltasarb.yama.YamaApplication
import baltasarb.yama.model.Organization
import baltasarb.yama.repositories.UserOrganizationsRepository

class UserOrganizationsViewModel(val application: YamaApplication): ViewModel() {

    fun getUserOrganizations(): LiveData<Resource<Array<Organization>>>
            = UserOrganizationsRepository(application).getUserOrganizations()

}