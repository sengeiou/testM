package com.qingmeng.mengmeng.view

import android.support.v4.view.ViewCompat
import android.view.View
import cn.bingoogolapple.bgabanner.transformer.BGAPageTransformer

class AlphaPageTransformer : BGAPageTransformer() {

    override fun handleInvisiblePage(view: View, position: Float) {
        ViewCompat.setAlpha(view, 0f)
    }

    override fun handleLeftPage(view: View, position: Float) {
        view.translationX = -view.width * position
        when {
            1 + position == 1f -> view.alpha = 1f
//            1 + position > 0.5 -> view.alpha = 1 + position - 0.5f
            1 + position > 0.5 -> view.alpha = (1 + position) * 0.5f
            else -> view.alpha = 0f
        }
    }

    override fun handleRightPage(view: View, position: Float) {
        view.translationX = -view.width * position
        view.alpha = 1 - position
    }
}