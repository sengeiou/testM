package com.qingmeng.mengmeng.view.dot

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.qingmeng.mengmeng.R

/**
 * 圆角Text
 */
class DotText : TextView {
    private val mGradientDrawable = GradientDrawable()
    private var dotNum: Int = 0             //提示数量
    private var backgroundColor: Int = 0    //背景颜色
    private var backgroundRadius: Int = 0   //背景圆角弧度
    private var strokeWidth: Int = 0        //边框宽度
    private var strokeColor: Int = 0        //边框颜色

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs, 0) {
        obtainAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    //获得属性
    private fun obtainAttributes(context: Context, attrs: AttributeSet?) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.DotText, 0, 0)
        dotNum = a.getInt(R.styleable.DotText_dotNum, 0)
        backgroundColor = a.getColor(R.styleable.DotText_backgroundColor, Color.RED)
        backgroundRadius = a.getDimensionPixelSize(R.styleable.DotText_backgroundRadius, 0)
        strokeWidth = a.getDimensionPixelSize(R.styleable.DotText_strokeWidth, 0)
        strokeColor = a.getColor(R.styleable.DotText_strokeColor, Color.TRANSPARENT)
        //回收
        a.recycle()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        //设置默认圆形
        if (backgroundRadius == 0) {
            backgroundRadius = height / 2
        }
        setDotNum(dotNum)
    }

    fun setDotNum(dotNum: Int) {
        this.dotNum = dotNum
        if (dotNum <= 0) {
            visibility = View.GONE
        } else {
            visibility = View.VISIBLE
            text = when (dotNum) {
                in 1..9 -> {
                    layoutParams.height = height
                    layoutParams.width = height
                    setPadding(0, 0, 0, dp2px(0.5f))
                    dotNum.toString()
                }
                in 10..99 -> {
                    layoutParams.height = height
                    layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                    setPadding(dp2px(4f), 0, dp2px(4f), dp2px(0.5f))
                    dotNum.toString()
                }
                else -> {
                    layoutParams.height = height
                    layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                    setPadding(dp2px(4f), 0, dp2px(4f), dp2px(0.5f))
                    "99+"
                }
            }
        }
        setBgSelector()
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        this.backgroundColor = backgroundColor
        setBgSelector()
    }

    fun setBackGroundRadius(cornerRadius: Int) {
        this.backgroundRadius = dp2px(cornerRadius.toFloat())
        setBgSelector()
    }

    fun setStrokeWidth(strokeWidth: Int) {
        this.strokeWidth = dp2px(strokeWidth.toFloat())
        setBgSelector()
    }

    fun setStrokeColor(strokeColor: Int) {
        this.strokeColor = strokeColor
        setBgSelector()
    }

    fun getDotNum(): Int {
        return dotNum
    }

    fun getBackgroundColor(): Int {
        return backgroundColor
    }

    fun getBackGroundRadius(): Int {
        return backgroundRadius
    }

    fun getStrokeWidth(): Int {
        return strokeWidth
    }

    fun getStrokeColor(): Int {
        return strokeColor
    }

    private fun dp2px(dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun sp2px(sp: Float): Int {
        val scale = this.context.resources.displayMetrics.scaledDensity
        return (sp * scale + 0.5f).toInt()
    }

    private fun setDrawable(gd: GradientDrawable, color: Int, strokeColor: Int) {
        gd.setColor(color)
        gd.cornerRadius = backgroundRadius.toFloat()
        gd.setStroke(strokeWidth, strokeColor)
    }

    private fun setBgSelector() {
        val bg = StateListDrawable()
        setDrawable(mGradientDrawable, backgroundColor, strokeColor)
        bg.addState(intArrayOf(-android.R.attr.state_pressed), mGradientDrawable)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {//16
            background = bg
        } else {
            setBackgroundDrawable(bg)
        }
    }
}
