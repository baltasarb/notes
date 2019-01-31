package baltasarb.yama.repositories

import baltasarb.yama.YamaApplication
import baltasarb.yama.model.Avatar
import baltasarb.yama.services.runAsync
import baltasarb.yama.utils.UrlToBitmap

class AvatarsRepository(val application: YamaApplication) {

    fun getAvatar(avatar_url: String, cb: (avatar: Avatar) -> Unit) {
        val local = application.database.avatarsDao()

        runAsync {
            local.getAvatar(avatar_url)
        }.andThen { avatar ->
            if (avatar != null)
                cb(avatar)
            else {
                UrlToBitmap(cb = {
                    val avatarRes = Avatar(avatar_url, it)
                    runAsync {
                        local.insertAll(avatarRes)
                    }
                    cb(avatarRes)
                }).execute(avatar_url)
            }
        }
    }
}