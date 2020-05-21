package tech.nicesky.camera2client

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_mp.*
import tech.nicesky.camera2capture.AutoFitTextureView
import tech.nicesky.camera2capture.Camera2Device
import tech.nicesky.camera2capture.ShootingNinjas

/**
 * Created by fairytale110@foxmail.com at 2020/5/10 8:44
 *
 * Description：
 *
 */
class MuiltePreviewActivity : AppCompatActivity() {
    lateinit var s : ShootingNinjas
    private var devices = arrayListOf<Camera2Device>()
    private var previews = arrayListOf<AutoFitTextureView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mp)

        // https://blog.csdn.net/qq_24712507/article/details/92999760
        previews.addAll(arrayListOf<AutoFitTextureView>(pr1, pr2, pr3, pr4))
            //devices.add(Camera2Device(this, "0", pr1))
           // devices.add(Camera2Device(this, "1", pr2))
           // devices.add(Camera2Device(this, "2", pr3))
           // devices.add(Camera2Device(this, "3", pr4))
        s = ShootingNinjas(this)
//        s.muiltPreView(previews)

//        devices.get(0).previewOnly()
//        devices.get(1).previewOnly()
        btn_preview1.setOnClickListener {
            s.shoot()
        }
        btn_preview2.setOnClickListener {
            devices.get(1).shoot(null)
        }
        btn_preview3.setOnClickListener {
            //devices.get(2).previewOnly()
        }
        btn_preview4.setOnClickListener {
            //devices.get(3).previewOnly()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
//        s.releasePreview()
    }
}