package com.panfeng.tensorflowlite

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.widget.Toast
import com.panfeng.tensorflowlite.detect.Classifier
import com.panfeng.tensorflowlite.detect.TFLiteObjectDetectionAPIModel
import com.panfeng.tensorflowlite.env.ImageUtils
import java.io.FileInputStream
import java.util.*

class ObjectDetection(private val context: Context,private val callBack: ODCallback) {
    enum class DetectorMode {
        TF_OD_API
    }

    companion object{

    }
    val TF_OD_API_INPUT_SIZE            = 900
    private val TF_OD_API_IS_QUANTIZED          = true
    private val TF_OD_API_MODEL_FILE            = "detect-0521.tflite"
    private val TF_OD_API_LABELS_FILE           = "file:///android_asset/labelmap.txt"
    private val MODE                            = DetectorMode.TF_OD_API
    val MINIMUM_CONFIDENCE_TF_OD_API    = 0.5f
    private val MAINTAIN_ASPECT                 = false
    private var detector : Classifier?          = null
    private var sensorOrientation: Int          = 0
    private var previewWidth                    = 0
    private var previewHeight                   = 0

    private var frameToCropTransform: Matrix?   = null
    var cropToFrameTransform: Matrix?   = null
    private var rgbFrameBitmap: Bitmap?         = null
    private var croppedBitmap: Bitmap?          = null

    init {
        try {
            detector = TFLiteObjectDetectionAPIModel.create(
                    context.assets,
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_LABELS_FILE,
                    TF_OD_API_INPUT_SIZE,
                    TF_OD_API_IS_QUANTIZED
                )
        } catch (e: Exception) {
            e.printStackTrace()
            val toast = Toast.makeText(
                context, "Classifier could not be initialized", Toast.LENGTH_SHORT
            )
            toast.show()
        }
    }

    fun detect(imagaPath : String){
        if (imagaPath.isNullOrEmpty()){
            println("图片不能空")
            callBack.onFailed()
            release()
            return
        }
        val cropSize    = TF_OD_API_INPUT_SIZE
        val fis             = FileInputStream(imagaPath);
        rgbFrameBitmap      = BitmapFactory.decodeStream(fis)
        previewWidth        = rgbFrameBitmap?.width ?: 0
        previewHeight       = rgbFrameBitmap?.height ?: 0
        sensorOrientation   = getImageOrientation(imagaPath)
        croppedBitmap       = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888)

        frameToCropTransform = ImageUtils.getTransformationMatrix(
            previewWidth, previewHeight,
            cropSize, cropSize,
            sensorOrientation, MAINTAIN_ASPECT
        )

        cropToFrameTransform = Matrix()
        frameToCropTransform?.invert(cropToFrameTransform)
        val canvas = Canvas(croppedBitmap!!)
        rgbFrameBitmap?.let {
            canvas.drawBitmap(it, frameToCropTransform!!, null)
        }?: kotlin.run {
            callBack.onFailed()
            release()
            return
        }

        detector?.let {
            val results = it.recognizeImage(croppedBitmap)
            for (result in results.iterator()){
                println("结果：${result.toString()}")
            }

            // mark
            var minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API
            minimumConfidence = when (MODE) {
                DetectorMode.TF_OD_API -> MINIMUM_CONFIDENCE_TF_OD_API
            }
            val paint = Paint()
            paint.color = Color.YELLOW
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.0f
            paint.isAntiAlias = true
            paint.textSize = 15F
            val mappedRecognitions: MutableList<Classifier.Recognition> =  LinkedList()
            for (result in results) {
                val location = result.location
                if (location != null && result.confidence >= minimumConfidence) {
                    paint.color = Color.YELLOW
                    canvas.drawRect(location, paint)
                    val v = String.format("%.1f%%", result.confidence * 100.0f)
                    val txt = "${result.title} $v"
                    paint.color = Color.RED
                    canvas.drawText(txt, location.left, location.bottom, paint)

                    cropToFrameTransform?.mapRect(location)
                    result.location = location
                    mappedRecognitions.add(result)
                }
            }
            callBack.onSucceed(imagaPath,croppedBitmap!!, mappedRecognitions)
        }?: run {
            println("结果为空")
            callBack.onFailed()
        }
        release()
    }

    private fun getImageOrientation(imagePath : String): Int{
        var degree = 0
        try {
            val exifInterface = ExifInterface(imagePath);
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 ->
                    degree = 90;
                ExifInterface.ORIENTATION_ROTATE_180 ->
                    degree = 180;
                ExifInterface.ORIENTATION_ROTATE_270 ->
                    degree = 270;
                else -> degree = 0
            }
        } catch (e: Exception) {
            e.printStackTrace();
        }
        return degree;
    }

    private fun release(){
        rgbFrameBitmap          = null
        croppedBitmap           = null
        frameToCropTransform    = null
        cropToFrameTransform    = null
    }
}