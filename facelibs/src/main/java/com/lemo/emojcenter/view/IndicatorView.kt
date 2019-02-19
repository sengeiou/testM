package com.lemo.emojcenter.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.lemo.emojcenter.R
import com.lemo.emojcenter.utils.DisplayUtils
import java.util.*

/**
 * Description:自定义表情底部指示器
 */
class IndicatorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    private var contexts: Context? = null

    private var dotViews: MutableList<ImageView> = ArrayList()
    private var dotWidthHeight: Int = 0
    private var margin: Int = 0

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : this(context, null) {}

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        this.contexts = context
        dotWidthHeight = DisplayUtils.dp2px(context, DOT_WIDTH_HEIGHT.toFloat())
        margin = DisplayUtils.dp2px(context, DOT_MARGIN.toFloat())
        gravity = Gravity.CENTER_HORIZONTAL
    }

    fun init(count: Int) {
        dotViews = ArrayList()
        for (i in 0 until count) {
            val rl = RelativeLayout(context)
            val params = LinearLayout.LayoutParams(dotWidthHeight, dotWidthHeight)
            val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
            val imageView = ImageView(context)

            if (i == 0) {
                imageView.setImageResource(R.drawable.face_shape_bg_indicator_point_select)
                rl.addView(imageView, layoutParams)
            } else {
                imageView.setImageResource(R.drawable.face_shape_bg_indicator_point_nomal)
                rl.addView(imageView, layoutParams)
            }
            this.addView(rl, params)
            dotViews.add(imageView)
        }
    }

    fun updateIndicator(count: Int) {
        //        if (dotViews == null) {
        //            return;
        //        }
        for (i in dotViews.indices) {
            if (i >= count) {
                dotViews[i].visibility = View.GONE
                (dotViews[i].parent as View).visibility = View.GONE
            } else {
                dotViews[i].visibility = View.VISIBLE
                (dotViews[i].parent as View).visibility = View.VISIBLE
            }
        }
        if (count > dotViews.size) {
            val diff = count - dotViews.size
            for (i in 0 until diff) {
                val rl = RelativeLayout(context)
                val params = LinearLayout.LayoutParams(dotWidthHeight, dotWidthHeight)
                val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(margin, 0, 0, 0)
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
                val imageView = ImageView(context)
                imageView.setImageResource(R.drawable.face_shape_bg_indicator_point_nomal)

                rl.addView(imageView, layoutParams)
                rl.visibility = View.GONE
                imageView.visibility = View.GONE
                this.addView(rl, params)
                dotViews.add(imageView)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    fun selectTo(position: Int) {
        for (iv in dotViews) {
            iv.setImageResource(R.drawable.face_shape_bg_indicator_point_nomal)
        }
        dotViews[position].setImageResource(R.drawable.face_shape_bg_indicator_point_select)
    }


    fun selectTo(startPosition: Int, targetPostion: Int) {
        val startView = dotViews[startPosition]
        val targetView = dotViews[targetPostion]
        startView.setImageResource(R.drawable.face_shape_bg_indicator_point_nomal)
        targetView.setImageResource(R.drawable.face_shape_bg_indicator_point_select)
    }

    companion object {
        //指示器宽高
        private val DOT_WIDTH_HEIGHT = 8
        //间距
        private val DOT_MARGIN = 6
    }


}
