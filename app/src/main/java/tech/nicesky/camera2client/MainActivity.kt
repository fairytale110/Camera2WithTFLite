package tech.nicesky.camera2client

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaCodec.MetricsConstants.MODE
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.panfeng.tensorflowlite.ODCallback
import com.panfeng.tensorflowlite.ObjectDetection
import com.panfeng.tensorflowlite.ObjectDetection.DetectorMode
import com.panfeng.tensorflowlite.detect.Classifier
import kotlinx.android.synthetic.main.activity_main.*
import tech.nicesky.camera2capture.ImageEntity
import tech.nicesky.camera2capture.SNListener
import tech.nicesky.camera2capture.ShootingNinjas


class MainActivity : AppCompatActivity(), ODCallback {
    private val max = 3
    private var cc = 0

    private lateinit var ss: ShootingNinjas
    private lateinit var detector: ObjectDetection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ss = ShootingNinjas(this).shootListener(object : SNListener {
            override fun onStart() {
                Log.d("shootListener", "onStart()")
            }

            override fun onShooting(img: ImageEntity) {
                Log.d("shootListener", "onShooting() ${img.path}")
                runOnUiThread {
                    when (cc) {
                        0 -> Glide.with(this@MainActivity).load(img.path).into(img0)
                        1 -> Glide.with(this@MainActivity).load(img.path).into(img1)
                        2 -> Glide.with(this@MainActivity).load(img.path).into(img2)
                        3 -> Glide.with(this@MainActivity).load(img.path).into(img3)
                        else -> println("onShooting out")
                    }
                    detector.detect(img.path)
                    cc++
                }
            }

            override fun onFinish(imgs: ArrayList<ImageEntity>) {
                Log.d("shootListener", "onFinish()")
                for (img in imgs) {
                    println(img.toString())
                }
            }
        })

        shoot.setOnClickListener {
            resetViews()
            Handler().postDelayed(Runnable {
                ss.shoot()
            }, 100)
        }

        pre.setOnClickListener {
            // TODO open four cameras preview
            // startActivity(Intent(this, MuiltePreviewActivity::class.java))
        }

        next.setOnClickListener {
            if (viewIndex < 0) {
                viewIndex = 0
            } else {
                viewIndex++
            }
            when (viewIndex) {
                0 -> {
                    img0.visibility = View.VISIBLE
                    img1.visibility = View.GONE
                    g1.visibility = View.VISIBLE
                    g2.visibility = View.GONE
                }
                1 -> {
                    img0.visibility = View.GONE
                    img1.visibility = View.VISIBLE
                    g1.visibility = View.VISIBLE
                    g2.visibility = View.GONE
                }
                2 -> {
                    img2.visibility = View.VISIBLE
                    img3.visibility = View.GONE
                    g1.visibility = View.GONE
                    g2.visibility = View.VISIBLE
                }
                3 -> {
                    img2.visibility = View.GONE
                    img3.visibility = View.VISIBLE
                    g1.visibility = View.GONE
                    g2.visibility = View.VISIBLE
                }
            }
        }
        ALL.setOnClickListener {
            img0.visibility = View.VISIBLE
            img1.visibility = View.VISIBLE
            img2.visibility = View.VISIBLE
            img3.visibility = View.VISIBLE
            g1.visibility = View.VISIBLE
            g2.visibility = View.VISIBLE
            viewIndex = -1
        }
        detector = ObjectDetection(this, this)
        resetViews()
    }

    private var viewIndex = -1

    private fun resetViews() {
        Glide.with(this@MainActivity).load(R.mipmap.placeholder).into(img0)
        Glide.with(this@MainActivity).load(R.mipmap.placeholder).into(img1)
        Glide.with(this@MainActivity).load(R.mipmap.placeholder).into(img2)
        Glide.with(this@MainActivity).load(R.mipmap.placeholder).into(img3)
        cc = 0
    }


    override fun onSucceed(
        imagePath: String,
        bitmap: Bitmap,
        mappedRecognitions: MutableList<Classifier.Recognition>
    ) {
        when (cc) {
            0 -> Glide.with(this@MainActivity).load(bitmap).into(img0)
            1 -> Glide.with(this@MainActivity).load(bitmap).into(img1)
            2 -> Glide.with(this@MainActivity).load(bitmap).into(img2)
            3 -> Glide.with(this@MainActivity).load(bitmap).into(img3)
            else -> println("onShooting out")
        }
    }

    override fun onFailed() {
    }
}
