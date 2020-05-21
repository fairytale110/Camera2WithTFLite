package tech.nicesky.camera2capture

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView

/**
 * Created by fairytale110@foxmail.com at 2020/4/1 16:35
 *
 * Description：
 *
 */
class AutoFitTextureView : TextureView {
    private var mRatioWidth = 0
    private var mRatioHeight = 0

    constructor(context: Context): super(context)
    constructor(context: Context, attributesets : AttributeSet): super(context, attributesets)
    constructor(context: Context, attributesets : AttributeSet, style : Int): super(context, attributesets, style)

    fun setAspectRatio(width: Int, height: Int) {
        require(!(width < 0 || height < 0)) { "Size cannot be negative." }
        mRatioWidth = width
        mRatioHeight = height
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height)
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth)
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height)
            }
        }
    }
}