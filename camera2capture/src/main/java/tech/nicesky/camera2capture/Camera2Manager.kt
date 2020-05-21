package tech.nicesky.camera2capture

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import android.view.WindowManager.LayoutParams.TYPE_PHONE
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * Created by fairytale110@foxmail.com at 2020/4/1 11:05
 *
 * 参考：https://www.jianshu.com/u/1bda0082f088
 * 工控多路RK3399 https://blog.csdn.net/kris_fei/article/details/81508302
 * https://blog.csdn.net/u014674293/article/details/91986610
 *
 * Description：
 *
 * 拉取全部摄像头，
 * 全部打开，
 * 挨个拍照
 * 保存本地，
 * 回调给业务端
 *
 */
@Deprecated("此类仅用于开发期间调试用")
class Camera2Manager private constructor(private var context: Context) {
    private var manager: CameraManager? = null
    private var currentCameraIndex = 0
    private var cameraIds = arrayListOf<String>()
    private var cameraMap = mutableMapOf<String, CameraDevice>()

    private var textureViews = arrayListOf<AutoFitTextureView>()

    private var surfaceTexture: SurfaceTexture? = null
    private var photoSurface: Surface? = null
    private var textureSurface: Surface? = null
    private var imageReader: ImageReader? = null

    private var previewRequestBuilder: CaptureRequest.Builder? = null

    // 定义用于预览照片的捕获请求
    private var previewRequest: CaptureRequest? = null

    // 定义CameraCaptureSession成员变量
    private var captureSession: CameraCaptureSession? = null

    @SuppressLint("LongLogTag")
    private val photoReaderImgListener =
        ImageReader.OnImageAvailableListener { reader: ImageReader? ->
            reader?.let {
                Log.e("=== OnImageAvailableListener ===", "onImageAvailable() ==> ")
                // 保存图片，释放资源，继续下一个摄像头

                // 获取捕获的照片数据
                val image = reader.acquireNextImage()
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer[bytes]
                //create bitmap
                // val source = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                //int       degree      = ArtsConfig.getPreviewDegree(Photo2Activity.this);
                //Log.i(TAG,"rotate "+degree);
                //int       degree      = ArtsConfig.getPreviewDegree(Photo2Activity.this);
                //Log.i(TAG,"rotate "+degree);
                // val bitmap: Bitmap = Util.rotaingImageView(0, source)

                // ArtsConfig.Image.get(currentCamera) = null
                // ArtsConfig.Image.get(currentCamera) = bitmap


                val path: String = Util.getSaveBitmapPath(context)
                // 使用IO流将照片写入指定文件
                //File file = new File(getExternalFilesDir(null), "pic.jpg");
                // 使用IO流将照片写入指定文件
                //File file = new File(getExternalFilesDir(null), "pic.jpg");
                val file = File(path)
                var output: FileOutputStream? = null
                try {
                    output = FileOutputStream(file)
                    output.write(bytes)
                    output.flush()
                    Log.e("=== OnImageAvailableListener === ", "onImageAvailable() 保存: 成功")
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    image.close()
                    try {
                        output!!.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                releaseCamera()
            }
        }

    private var sessionCallback: CameraCaptureSession.StateCallback =
        object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                // 如果摄像头为null，直接结束方法
                if (null == cameraMap[cameraIds[currentCameraIndex]]) {

                    Log.e("Camera2Manager", "sessionCallback onConfigured()  device is null")
                    return
                }
                Log.e("Camera2Manager", "sessionCallback onConfigured() ")
                // 当摄像头已经准备好时，开始显示预览
                captureSession = cameraCaptureSession
                try {
                    // 设置自动对焦模式
                    previewRequestBuilder!!.set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                    )
                    // 设置自动曝光模式
                    previewRequestBuilder!!.set(
                        CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                    )
                    previewRequestBuilder!!.set(CaptureRequest.JPEG_ORIENTATION, 90)
                    // 开始显示相机预览
                    previewRequest = previewRequestBuilder!!.build()
                    // 设置预览时连续捕获图像数据
                    captureSession!!.setRepeatingRequest(previewRequest!!, null, null) // ④
                    Handler().postDelayed({ takePicture() }, 5)
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
            }

            override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {

                Log.e("Camera2Manager", "sessionCallback onConfigureFailed() !!")
                // Toast.makeText(this@Photo2Activity, "配置失败！", Toast.LENGTH_SHORT).show()
            }
        }

    @SuppressLint("LongLogTag")
    inner class CusSurfaceTextureListener(var camerId: String) :
        TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture?,
            width: Int,
            height: Int
        ) {
            Log.e("=== surfaceTextureListener ===", "onSurfaceTextureSizeChanged()")
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            Log.e("=== surfaceTextureListener ===", "onSurfaceTextureUpdated()")
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            Log.e("=== surfaceTextureListener ===", "onSurfaceTextureDestroyed()")
            return false
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            Log.e("=== surfaceTextureListener ===", "onSurfaceTextureAvailable()")
            openCamera(camerId)
        }
    }

    init {
        Log.e("Camera2Manager", "init() ")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager = context.getSystemService(CameraManager::class.java)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            manager = context.getSystemService(CAMERA_SERVICE) as CameraManager?
        } else {
            // TODO 使用Camera 旧版API
        }
        val arr = manager?.cameraIdList ?: arrayOf<String>()
        for (id in arr.iterator()) {
            cameraIds.add(id)
        }

        createPreviews()
    }

    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    private fun openCamera(camerId: String) {

        Log.e("Camera2Manager", "openCamera() id: $camerId")
        setUpCameraOutputs(camerId)
        // 打开摄像头
        if (ActivityCompat.checkSelfPermission(context, permission.CAMERA) !== PERMISSION_GRANTED) {
            Log.e("CM", "==== NO CAMERA Permission ====")
            return
        }
        manager?.openCamera(camerId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                Log.e("CM openCamera()", "onOpened ${camera.id}")
                cameraMap.put(camerId, camera)
                createCameraPreviewSession()
            }

            override fun onDisconnected(camera: CameraDevice) {
                Log.e("CM openCamera()", "onDisconnected ${camera.id}")
                cameraMap.remove(camera.id)
            }

            override fun onError(camera: CameraDevice, error: Int) {
                Log.e("CM openCamera()", "onError ${camera.id} code = $error")
                camera.close()
                cameraMap.remove(camera.id)
            }
        }, null)
    }

    private fun setUpCameraOutputs(camerId: String) {

        Log.e("Camera2Manager", "setUpCameraOutputs() ==>")
        val characteristics = manager?.getCameraCharacteristics(camerId);
        val streamConfigurationMap =
            characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

        imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 2)
        imageReader?.setOnImageAvailableListener(photoReaderImgListener, null)
    }

    private fun createCameraPreviewSession() {

        Log.e("Camera2Manager", "createCameraPreviewSession() ==>")
        surfaceTexture = textureViews[currentCameraIndex].surfaceTexture
        textureSurface = Surface(surfaceTexture)
        photoSurface = imageReader?.surface
        // 创建作为预览的CaptureRequest.Builder
        // 创建作为预览的CaptureRequest.Builder
        previewRequestBuilder = cameraMap.get(cameraIds[currentCameraIndex])
            ?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        // 将textureView的surface作为CaptureRequest.Builder的目标
        // 将textureView的surface作为CaptureRequest.Builder的目标
        previewRequestBuilder?.addTarget(textureSurface!!)

        cameraMap.get(cameraIds[currentCameraIndex])?.createCaptureSession(
            mutableListOf(textureSurface, photoSurface),
            sessionCallback,
            null
        )
    }

    private fun takePicture() {

        Log.e("Camera2Manager", "takePicture() ==>")
        try {
            if (cameraMap[cameraIds[currentCameraIndex]] == null) {

                Log.e("Camera2Manager", "camera device is null !!!")
                return
            }

            // 创建作为拍照的CaptureRequest.Builder
            val captureRequestBuilder: CaptureRequest.Builder =
                cameraMap[cameraIds[currentCameraIndex]]!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            // 将imageReader的surface作为CaptureRequest.Builder的目标
            captureRequestBuilder.addTarget(imageReader!!.surface)
            // 设置自动对焦模式
            captureRequestBuilder.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            // 设置自动曝光模式
            captureRequestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
            // 获取设备方向
            val rotation: Int =
                (context as Activity).getWindowManager().getDefaultDisplay().getRotation()
            // 根据设备方向计算设置照片的方向
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90)
            // 停止连续取景
            captureSession!!.stopRepeating()
            // 捕获静态图像
            //captureSession.capture(captureRequestBuilder.build(), captureCallback, null);
            captureSession!!.capture(captureRequestBuilder.build(), null, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    private fun closeCamera(camerId: String) {
        cameraMap.get(camerId)?.close()
        cameraMap.remove(camerId)
    }

    private fun createPreviews() {
        Log.e("Camera2Manager", "createPreviews() ==>")
        textureViews.clear()
        for (index in 0 until cameraIds.size) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val textureView = AutoFitTextureView(context)
            val layoutParams = WindowManager.LayoutParams(
                1,
                1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = TYPE_APPLICATION_OVERLAY
            } else {
                layoutParams.type = TYPE_PHONE
            }
            layoutParams.gravity = Gravity.START or Gravity.TOP
            windowManager.addView(textureView, layoutParams)
            textureViews.add(textureView)
        }
        currentCameraIndex = 0
        textureViews[currentCameraIndex].surfaceTextureListener =
            CusSurfaceTextureListener(cameraIds[currentCameraIndex])
    }

    private fun checkCCT(camerId: String) {
        val cameraCharacteristics = manager?.getCameraCharacteristics(camerId)
        val streamConfigurationMap =
            cameraCharacteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val supportedSize = streamConfigurationMap?.getOutputSizes(SurfaceTexture::class.java)
    }

    private fun releaseCamera() {
        Log.e("Camera2Manager", "releaseCamera() ==>")
        Util.releaseImageReader(imageReader)
        Util.releaseCameraSession(captureSession)
        Util.releaseCameraDevice(cameraMap[cameraIds[currentCameraIndex]])
        textureViews[currentCameraIndex].surfaceTextureListener = null
        previewRequestBuilder = null
        previewRequest = null
        surfaceTexture?.release()
        surfaceTexture = null
        photoSurface?.release()
        photoSurface = null
        textureSurface?.release()
        textureSurface = null
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        for (view in textureViews) {
            windowManager.removeViewImmediate(view)
        }
    }

    companion object {
        fun instance(context: Context): Camera2Manager {
            return Camera2Manager(context)
        }
    }
}