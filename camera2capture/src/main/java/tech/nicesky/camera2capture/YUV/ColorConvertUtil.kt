package tech.nicesky.camera2capture.YUV

import android.graphics.Bitmap
import android.graphics.Matrix;
import android.util.Log;
import java.nio.ByteBuffer

/**
 * Created by fairytale110@foxmail.com at 2020/4/27 13:35
 *
 * Description：
 *
 */
class ColorConvertUtil {

    companion object {


        private val TAG = "ColorConvertUtil"

        fun yuv420pToBitmap(yuv420p: ByteArray?, width: Int, height: Int): Bitmap? {
            if (yuv420p == null || width < 0 || height < 0) {
                Log.e(TAG, "cropNv21ToBitmap failed: illegal para !")
                return null
            }
            val rgba = ByteArray(width * height * 4)
            ColorConvertUtil.yuv420pToRGBA(yuv420p, width, height, rgba)
            return byteArrayToBitmap(rgba, width, height)
        }

        fun yuv420pToRGBA(
            yuv420p: ByteArray?,
            width: Int,
            height: Int,
            rgba: ByteArray?
        ) {
            if (yuv420p == null || rgba == null) {
                Log.e(TAG, "yuv420pToRGBA failed: yuv420p or rgba is null ")
                return
            }
            if (yuv420p.size != width * height * 3 / 2) {
                Log.e(TAG, "yuv420p length: " + yuv420p.size)
                Log.e(TAG, "yuv420pToRGBA failed: yuv420p length error!")
                return
            }
            NativeLibrary.yuv420p2rgba(yuv420p, width, height, rgba)
        }

        /**
         * 将 rgba 的 byte[] 数据转换成 bitmap
         *
         * @param rgba   输入的 rgba 数据
         * @param width  图片宽度
         * @param height 图片高度
         * @return 得到的 bitmap
         */
        fun byteArrayToBitmap(rgba: ByteArray?, width: Int, height: Int): Bitmap {
            val buffer: ByteBuffer = ByteBuffer.wrap(rgba)
            val bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(buffer)
            return bitmap
        }

        fun rotateBitmap(bitmap: Bitmap?, rotate: Int, mirrorX: Boolean): Bitmap? {
            val matrix = Matrix()
            matrix.postRotate(rotate.toFloat())
            if (mirrorX) {
                matrix.postScale(-1f, 1f)
            }
            var rotateBitmap: Bitmap? = null
            if (bitmap != null) {
                rotateBitmap = Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    matrix,
                    false
                )
                bitmap.recycle() // 回收旧Bitmap
            }
            return rotateBitmap
        }
    }
}