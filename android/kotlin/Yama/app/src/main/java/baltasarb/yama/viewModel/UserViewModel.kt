package baltasarb.yama.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import baltasarb.yama.utils.Resource
import baltasarb.yama.YamaApplication
import baltasarb.yama.model.User
import baltasarb.yama.repositories.UserRepository

class UserViewModel(val application: YamaApplication) : ViewModel() {

    fun getUser(): LiveData<Resource<User>> = UserRepository(application).getUser()

}