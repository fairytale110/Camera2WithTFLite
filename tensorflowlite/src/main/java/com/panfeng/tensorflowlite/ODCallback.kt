package com.panfeng.tensorflowlite

import android.graphics.Bitmap
import com.panfeng.tensorflowlite.detect.Classifier

interface ODCallback {
    fun onSucceed(imagePath: String, bitmap: Bitmap,  mappedRecognitions: MutableList<Classifier.Recognition>)
    fun onFailed()
}