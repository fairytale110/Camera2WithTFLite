package tech.nicesky.camera2capture.YUV

import android.graphics.Bitmap
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by fairytale110@foxmail.com at 2020/4/27 13:34
 *
 * Description：
 *
 */
class FileUtil {
    companion object{

        val SAVE_DIR = "/sdcard/DCIM/Camera2Client/"

        fun saveBytes(bytes: ByteArray?, imagePath: String?): Boolean {
            val file = File(imagePath)
            val parentFile: File = file.getParentFile()
            if (!parentFile.exists()) {
                parentFile.mkdirs()
            }
            try {
                val fos = FileOutputStream(file)
                fos.write(bytes)
                fos.flush()
                fos.close()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

        fun saveBitmap(bitmap: Bitmap?, imagePath: String?): Boolean {
            if (bitmap == null) {
                return false
            }
            val file = File(imagePath)
            val parentFile: File = file.getParentFile()
            if (!parentFile.exists()) {
                parentFile.mkdirs()
            }
            try {
                val fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }
    }
}