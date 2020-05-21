package tech.nicesky.camera2capture

import android.content.Context
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

/**
 * Created by fairytale110@foxmail.com at 2020/4/2 13:49
 *
 * Description：
 *
 */
class ShootingNinjas : CaptureListener {

    private lateinit var context: Context
    private var manager: CameraManager? = null
    private val cameras = mutableListOf<Camera2DeviceWithYUV>()
    private val imgs = arrayListOf<ImageEntity>()

    constructor(context: Context) {
        this.context = context
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager = context.getSystemService(CameraManager::class.java)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        } else {
            // 运维安装时
            throw UnsupportedClassVersionError("只能在Android5.0及以上运行！")
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.canDrawOverlays(context)) {
//                // 运维安装时
//                throw UninitializedPropertyAccessException(" 显示在应用上层功能未打开！")
//            }
//        }
    }

    private var currentCameraIndex = 0;

    fun shoot() {
        Log.e("sn","shoot() ")
        cameras.clear()
        imgs.clear()
        val arr = manager?.cameraIdList ?: arrayOf<String>()
        for (id in arr.iterator()) {
            cameras.add(Camera2DeviceWithYUV(context, id))
        }
        Log.e("sn","shoot() size: ${cameras.size}")
        this.shootListener?.onStart()
        if (cameras.size > 0) {
            currentCameraIndex = 0
            cameras[currentCameraIndex].shoot(this)
        } else {
            this.shootListener?.onFinish(imgs)
        }
    }

    override fun onFinish(success: Boolean, cameraId: String, imgPath: String) {
        Log.e("ShootingNinjas", "finish state = $success, camId = $cameraId, imgPath = $imgPath")
        var ent = ImageEntity("", cameraId, false)
        if (success) {
            ent = ImageEntity(imgPath, cameraId, true)
        }
        imgs.add(ent)
        this.shootListener?.onShooting(ent)
        try {
            Thread.sleep(100)
        } catch (e: Exception) {
        }
        currentCameraIndex++
        if (currentCameraIndex < cameras.size) {
            cameras[currentCameraIndex].shoot(this)
        } else {
            Log.e("ShootingNinjas", "finish ALL ")
            this.shootListener?.onFinish(imgs)
            cameras.clear()
        }
    }

    private var shootListener: SNListener? = null

    fun shootListener(listener: SNListener): ShootingNinjas {
        this@ShootingNinjas.shootListener = listener
        return this@ShootingNinjas
    }

    fun cameraCount(): UInt {
        return manager?.cameraIdList?.size?.toUInt() ?: 0.toUInt()
    }

}