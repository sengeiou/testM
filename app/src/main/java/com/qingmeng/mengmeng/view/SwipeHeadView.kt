package com.qingmeng.mengmeng.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

import com.aspsine.swipetoloadlayout.SwipeRefreshTrigger
import com.aspsine.swipetoloadlayout.SwipeTrigger
import com.qingmeng.mengmeng.R

/**
 * 下拉头部
 */
class SwipeHeadView : FrameLayout, SwipeTrigger, SwipeRefreshTrigger {

    // 箭头动画——向上
    private var mUpAnim: Animation? = null
    // 箭头动画——向下
    private var mDownAnim: Animation? = null
    // 文本
    private lateinit var mHeadTxt: TextView
    // 菊花
    private lateinit var mProgressBar: ProgressBar
    // 控件高度
    private var mHeight: Int = 0
    // 箭头
    private var mArrowImg: ImageView? = null
    //
    private var mRotated = false

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    // 设置视图
    private fun initView(context: Context) {
        View.inflate(context, R.layout.view_swipe_head, this)
        mHeight = resources.getDimension(R.dimen.swipe_widget_height).toInt()
        mArrowImg = findViewById(R.id.img_arrow)
        mHeadTxt = findViewById(R.id.txt_refresh_head)
        mProgressBar = findViewById(R.id.progress_bar)
        initAnimation()
    }

    // 初始化箭头动画
    private fun initAnimation() {
        this.mUpAnim = AnimationUtils.loadAnimation(context, R.anim.rotate_up)
        this.mDownAnim = AnimationUtils.loadAnimation(context, R.anim.rotate_down)
    }

    override fun onPrepare() {
        mHeadTxt.text = context.resources.getString(R.string.txt_before_refresh)
        mProgressBar.visibility = View.GONE
        mArrowImg!!.visibility = View.VISIBLE
        invalidate()
    }

    override fun onMove(i: Int, b: Boolean, b1: Boolean) {
        if (!b) {
            mArrowImg!!.visibility = View.VISIBLE
            mProgressBar.visibility = View.INVISIBLE
            if (i > mHeight) {
                mHeadTxt.setText(R.string.txt_loose_refresh)
                if (!mRotated) {
                    mArrowImg!!.clearAnimation()
                    mArrowImg!!.startAnimation(mUpAnim)
                    mRotated = true
                }
            } else if (i < mHeight) {
                if (mRotated) {
                    mArrowImg!!.clearAnimation()
                    mArrowImg!!.startAnimation(mDownAnim)
                    mRotated = false
                }
                mHeadTxt.setText(R.string.txt_before_refresh)
            }
        }
    }

    override fun onRefresh() {
        mHeadTxt.text = resources.getString(R.string.txt_refreshing)
        mArrowImg!!.clearAnimation()
        mArrowImg!!.visibility = View.GONE
        mProgressBar.visibility = View.VISIBLE
        invalidate()
    }

    override fun onRelease() {
        mHeadTxt.text = resources.getString(R.string.txt_refreshing)
        mArrowImg!!.visibility = View.GONE
        mProgressBar.visibility = View.VISIBLE
        invalidate()
    }

    override fun onComplete() {
        mRotated = false
        mHeadTxt.text = resources.getString(R.string.txt_refreshed)
        mArrowImg!!.clearAnimation()
        mArrowImg!!.visibility = View.GONE
        mProgressBar.visibility = View.GONE
        invalidate()
    }

    override fun onReset() {
        mRotated = false
        mHeadTxt.text = resources.getString(R.string.txt_before_refresh)
        mArrowImg!!.visibility = View.VISIBLE
        mProgressBar.visibility = View.GONE
    }
}
