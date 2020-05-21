package tech.nicesky.camera2capture

/**
 * Created by fairytale110@foxmail.com at 2020/4/10 11:40
 *
 * Description：
 *
 */
class ImageEntity {
    var path : String = ""
    var cameraId : String = ""
    var shootSucceed = false

    constructor(path: String, cameraId: String, shootSucceed: Boolean) {
        this.path = path
        this.cameraId = cameraId
        this.shootSucceed = shootSucceed
    }

    override fun toString(): String {
        return "ImageEntity ( path='$path', cameraId='$cameraId', shootSucceed=$shootSucceed)"
    }
}