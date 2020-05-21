package tech.nicesky.camera2capture

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.media.ImageReader
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by fairytale110@foxmail.com at 2020/4/1 10:57
 *
 * Description：
 *
 */
class Util {
    companion object{
        private const val REQUEST_PERMISSION_CODE: Int = 1
        private val REQUIRED_PERMISSIONS: Array<String> = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        /**
         * 判断我们需要的权限是否被授予，只要有一个没有授权，我们都会返回 false，并且进行权限申请操作。
         *
         * @return true 权限都被授权
         */
        private fun checkRequiredPermissions(context: Activity): Boolean {
            val deniedPermissions = mutableListOf<String>()
            for (permission in REQUIRED_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                    deniedPermissions.add(permission)
                }
            }
            if (deniedPermissions.isEmpty().not()) {
                requestPermissions(context, deniedPermissions.toTypedArray(), REQUEST_PERMISSION_CODE)
            }
            return deniedPermissions.isEmpty()
        }

        fun releaseImageReader(reader: ImageReader?) {
            var reader = reader
            if (reader != null) {
                reader.close()
                reader = null
            }
        }

        fun releaseCameraSession(session: CameraCaptureSession?) {
            var session = session
            if (session != null) {
                session.close()
                session = null
            }
        }

        fun releaseCameraDevice(cameraDevice: CameraDevice?) {
            var cameraDevice = cameraDevice
            if (cameraDevice != null) {
                cameraDevice.close()
                cameraDevice = null
            }
        }

        fun rotaingImageView(angle: Int, bitmap: Bitmap): Bitmap? {
            //旋转图片 动作
            val matrix = Matrix()
            matrix.postRotate(angle.toFloat())
            // 创建新的图片
            return Bitmap.createBitmap(
                bitmap, 0, 0,
                bitmap.width, bitmap.height, matrix, true
            )
        }

        fun getSaveBitmapPath(context: Context): String {
            return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                var path =
                    Environment.getExternalStorageDirectory().path + "/MImge/Temp/"
                val dirFile = File(path)
                if (!dirFile.exists()) {
                    dirFile.mkdirs()
                }
                /*if(dirFile.exists()){
                        File files[]=dirFile.listFiles();
                        //超过10个后删除
                        if(files.length>=150){
                            for (int i=0;i<files.length;i++){
                                files[i].delete();
                            }
                        }
                    }*/
                val time =
                    SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINESE).format(Date()) + ".jpg"
                val file = File(path, time)
                if (!file.exists()) {
                    try {
                        file.createNewFile()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                path = path + time
                path
            } else {
                context.filesDir.path
            }
        }

        private fun getRealPathFromURI(
            context: Context,
            contentUri: Uri
        ): String? {
            var cursor: Cursor? = null
            return try {
                val proj =
                    arrayOf(MediaStore.Images.Media.DATA)
                cursor = context.contentResolver.query(contentUri, proj, null, null, null)
                if (cursor == null) {
                    return ""
                }
                val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                cursor.getString(column_index)
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }
        }
    }
}