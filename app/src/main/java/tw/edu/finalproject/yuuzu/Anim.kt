package tw.edu.finalproject.yuuzu

import android.view.View
import android.view.animation.AnimationUtils
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import tw.edu.finalproject.R

fun View.slideLeftIn(animTime: Long, startOffset: Long) {
    val slideLeft = AnimationUtils.loadAnimation(context, R.anim.slide_left_in).apply {
        duration = animTime
        interpolator = FastOutSlowInInterpolator()
        this.startOffset = startOffset
    }
}

fun View.slideRightOut(animTime: Long, startOffset: Long) {
    val slideRight = AnimationUtils.loadAnimation(context, R.anim.slide_right_out).apply {
        duration = animTime
        interpolator = FastOutSlowInInterpolator()
        this.startOffset = startOffset
    }

    startAnimation(slideRight)
}

fun View.fadeIn(animTime: Long, startOffset: Long) {
    val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in).apply {
        duration = animTime
        interpolator = FastOutSlowInInterpolator()
        this.startOffset = startOffset
    }

    startAnimation(fadeIn)
}

fun View.fadeOut(animTime: Long, startOffset: Long) {
    val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out).apply {
        duration = animTime
        interpolator = FastOutSlowInInterpolator()
        this.startOffset = startOffset
    }

    startAnimation(fadeOut)
}
