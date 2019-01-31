package baltasarb.yama.utils

import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.os.AsyncTask
import java.io.BufferedInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class UrlToBitmap(val cb: (bitmap: Bitmap) -> Unit) : AsyncTask<String, Void, Bitmap>() {

    override fun doInBackground(vararg params: String?): Bitmap? {
        val urldisplay = params[0]
        val url: URL
        url = URL(urldisplay)
        val urlConnection: HttpURLConnection?
        return try {
            urlConnection = url.openConnection() as HttpURLConnection
            val contentStream = BufferedInputStream(urlConnection.inputStream)
            val result = BitmapFactory.decodeStream(contentStream)
            contentStream.close()
            urlConnection.disconnect()
            result
        } catch (e: IOException) {
            null
        }
    }

    override fun onPostExecute(result: Bitmap?) {
        super.onPostExecute(result)
        if (result != null) {
            cb(result)
        }
    }

}