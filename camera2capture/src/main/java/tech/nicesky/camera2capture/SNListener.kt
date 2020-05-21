package tech.nicesky.camera2capture

/**
 * Created by fairytale110@foxmail.com at 2020/4/10 11:36
 *
 * Description：
 *
 */
interface SNListener {
    fun onStart()

    fun onFinish(imgs: ArrayList<ImageEntity>)

    fun onShooting(img: ImageEntity)
}