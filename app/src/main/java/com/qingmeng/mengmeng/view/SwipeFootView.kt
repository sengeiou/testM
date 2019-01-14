package com.qingmeng.mengmeng.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView

import com.aspsine.swipetoloadlayout.SwipeLoadMoreTrigger
import com.aspsine.swipetoloadlayout.SwipeTrigger
import com.qingmeng.mengmeng.R

/**
 * 上拉的尾部
 */
class SwipeFootView : RelativeLayout, SwipeTrigger, SwipeLoadMoreTrigger {

    //    private View mTopView;
    // 箭头动画——向上
    private var mUpAnim: Animation? = null
    // 箭头动画——向下
    private var mDownAnim: Animation? = null
    // 箭头
    private var mArrowImg: ImageView? = null
    // 提示语
    private var mLoadTxt: TextView? = null
    // 没有更多
    private var mNoMoreTxt: TextView? = null
    // 菊花
    private var mProgressBar: ProgressBar? = null
    // 没有更多
    private var mIsEnd: Boolean = false
    // 控件高度
    private var mHeight: Int = 0
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
        View.inflate(context, R.layout.view_swipe_foot, this)
        mHeight = 50
        // mHeight = (int) getResources().getDimension(R.dimen.swipe_widget_height);
        mLoadTxt = findViewById(R.id.txt_loading)
        mProgressBar = findViewById(R.id.progress_bar)
        mArrowImg = findViewById(R.id.img_arrow)
        mArrowImg!!.rotation = -180f
        mNoMoreTxt = findViewById(R.id.txt_no_more)

        initAnimation()
    }

    // 初始化箭头动画
    private fun initAnimation() {
        this.mUpAnim = AnimationUtils.loadAnimation(context, R.anim.rotate_up)
        this.mDownAnim = AnimationUtils.loadAnimation(context, R.anim.rotate_down)
    }

    override fun onLoadMore() {
        mProgressBar!!.visibility = View.VISIBLE
        mArrowImg!!.clearAnimation()
        mArrowImg!!.visibility = View.INVISIBLE
        mLoadTxt!!.setText(R.string.txt_loading)
        invalidate()
    }

    override fun onPrepare() {
        mProgressBar!!.visibility = View.INVISIBLE
        mLoadTxt!!.setText(R.string.txt_before_load)
        mArrowImg!!.visibility = View.VISIBLE
        mNoMoreTxt!!.visibility = View.INVISIBLE
        invalidate()
    }

    override fun onMove(i: Int, b: Boolean, b1: Boolean) {
        if (!b) {
            mArrowImg!!.visibility = View.VISIBLE
            mProgressBar!!.visibility = View.GONE
            if (i < -mHeight) {
                mLoadTxt!!.setText(R.string.txt_loose_load)
                if (!mRotated) {
                    mArrowImg!!.clearAnimation()
                    mArrowImg!!.startAnimation(mUpAnim)
                    mRotated = true
                }
            } else if (i > -mHeight) {
                if (mRotated) {
                    mArrowImg!!.clearAnimation()
                    mArrowImg!!.startAnimation(mDownAnim)
                    mRotated = false
                }
                mLoadTxt!!.setText(R.string.txt_before_load)
            }
        }
    }

    override fun onRelease() {
        mProgressBar!!.visibility = View.VISIBLE
        mArrowImg!!.clearAnimation()
        mArrowImg!!.visibility = View.GONE
        mLoadTxt!!.setText(R.string.txt_loading)
        invalidate()
    }

    override fun onComplete() {
        mRotated = false
        mProgressBar!!.visibility = View.GONE
        mArrowImg!!.clearAnimation()
        mArrowImg!!.visibility = View.GONE
        if (mIsEnd) {
            mLoadTxt!!.visibility = View.GONE
            mNoMoreTxt!!.visibility = View.VISIBLE
        } else {
            mLoadTxt!!.setText(R.string.txt_loaded)
            mLoadTxt!!.visibility = View.VISIBLE
            mNoMoreTxt!!.visibility = View.GONE
        }
        invalidate()
    }

    override fun onReset() {
        mRotated = false
        mProgressBar!!.visibility = View.GONE
        mLoadTxt!!.visibility = View.VISIBLE
        mLoadTxt!!.setText(R.string.txt_before_load)
        mArrowImg!!.visibility = View.VISIBLE
        mNoMoreTxt!!.visibility = View.GONE
        invalidate()
    }

    /**
     * 列表设置为已满
     */
    fun noMore(isEnd: Boolean) {
        this.mIsEnd = isEnd
    }
}