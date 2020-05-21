package tech.nicesky.camera2capture


import android.util.Size
import java.lang.Long.signum

import java.util.Comparator
/**
 * Created by fairytale110@foxmail.com at 2020/4/27 8:20
 *
 * Description：
 *
 */
internal class CompareSizesByArea : Comparator<Size> {

    // We cast here to ensure the multiplications won't overflow
    override fun compare(lhs: Size, rhs: Size) =
        signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
}