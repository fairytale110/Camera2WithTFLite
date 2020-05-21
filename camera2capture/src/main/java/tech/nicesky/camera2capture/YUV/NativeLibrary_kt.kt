package tech.nicesky.camera2capture.YUV

/**
 * Created by fairytale110@foxmail.com at 2020/4/27 13:36
 *
 * Description：
 *tech.nicesky.camera2capture.YUV.NativeLibrary.Companion.yuv420p2rgba
 */
class NativeLibrary_kt {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
        external fun yuv420p2rgba(
            yuv420p: ByteArray?,
            width: Int,
            height: Int,
            rgba: ByteArray?
        )
    }
}