package baltasarb.yama.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

@Entity(tableName = "avatar", primaryKeys = ["avatarUrl"])
class Avatar(
    val avatarUrl: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val img: Bitmap
)

class AvatarTypeConverters {

    @TypeConverter
    fun imgFromBitMap(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
        return stream.toByteArray()
    }

    @TypeConverter
    fun imgToBitMap(byteArray: ByteArray): Bitmap =
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

}