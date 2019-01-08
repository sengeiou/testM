package com.qingmeng.mengmeng.view.dialog

import android.app.Activity
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.adapter.CommonAdapter
import com.qingmeng.mengmeng.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_my_settings_user_citypop.view.*

/**
 *  Description :个人资料城市选择

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/01/05
 */
class PopCitySelect : PopupWindow {
    private var mActivity: Activity
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var mAdapter: CommonAdapter<String>
    private var mList = ArrayList<String>()                          //所有城市数据
    private var mMenuView: View
    private var mPointPosition = Point()                             //手指按下坐标
    private var record = arrayOf(0, 0)                               //存放手指按下坐标和时间戳
    private var defaultTop = 0                                       //弹框原始距离顶部位置

    //构造方法
    constructor(activity: Activity, mList: ArrayList<String>) : super(activity) {
        this.mActivity = activity
        this.mList = mList
        mMenuView = LayoutInflater.from(activity).inflate(R.layout.activity_my_settings_user_citypop, null)

        initListener()
        initAdapter()

        //取消按钮
        mMenuView.ivMySettingsUserPopClose.setOnClickListener {
            //销毁弹出框
            dismiss()
        }

        //顶部手势监听
        mMenuView.rlMySettingsUserPop.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //每次按下时记录坐标
                    mPointPosition.y = event.rawY.toInt()
                    record[0] = event.rawY.toInt()
                    record[1] = System.currentTimeMillis().toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    //每次重绘都会根据上一次最后触屏的mPointPosition.y坐标算出新移动的值
                    val dy = event.rawY.toInt() - mPointPosition.y
                    //变化中的顶部距离
                    val top = v.top + dy
                    //获取到layoutParams后改变属性 在设置回去
                    val layoutParams = v.layoutParams as RelativeLayout.LayoutParams
                    layoutParams.topMargin = top
                    v.layoutParams = layoutParams
                    //记录最后一次移动的位置
                    mPointPosition.y = event.rawY.toInt()
                }
                MotionEvent.ACTION_UP -> {
                    //先根据时间算 如果在0.5秒内的话且向下拉的话 就直接销毁弹框
                    if (System.currentTimeMillis().toInt() - record[1] < 500 && event.rawY.toInt() > record[0]) {
                        dismiss()
                    } else {    //然后再根据移动距离判断是否销毁弹框
                        //下移超过200就销毁 否则弹回去
                        if (event.rawY.toInt() - record[0] > 300) {
                            dismiss()
                        } else {
                            //获取到layoutParams后改变属性 在设置回去
                            val layoutParams = v.layoutParams as RelativeLayout.LayoutParams
                            layoutParams.topMargin = defaultTop
                            v.layoutParams = layoutParams
                        }
                    }
                    mMenuView.rlMySettingsUserPop.setIntercept(false)
                }
            }
            //刷新界面
            mMenuView.rlMySettingsUserPopAll.invalidate()
            true
        }

        //RecyclerView监听
        mMenuView.rvMySettingsUserPop.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //每次按下时记录坐标
                    mPointPosition.y = event.rawY.toInt()
                    record[0] = event.rawY.toInt()
                    record[1] = System.currentTimeMillis().toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    //下拉
                    if (event.rawY <= record[0]) {
                        mMenuView.rlMySettingsUserPop.setIntercept(false)
                    } else {
                        //滑到顶部了
                        if (!v.canScrollVertically(-1)) {
                            mMenuView.rlMySettingsUserPop.setIntercept(true)
                        }
                    }
                }
            }
            false
        }

        //设置SelectPicPopupWindow的View
        this.contentView = mMenuView
        //设置SelectPicPopupWindow弹出窗体的宽
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        //设置SelectPicPopupWindow弹出窗体的高
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        //设置SelectPicPopupWindow弹出窗体可点击
        this.isFocusable = true
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.animationStyle = R.style.bottomDialog_animStyle
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(ColorDrawable(-0x00000000))


        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener { _, event ->
            val height = mMenuView.rlMySettingsUserPop.top
            val y = event.y
            if (event.action == MotionEvent.ACTION_UP) {
                if (y < height) {
                    dismiss()
                }
            }
            true
        }
    }

    private fun initListener() {

    }

    private fun initAdapter() {
        mLayoutManager = LinearLayoutManager(mActivity)
        mMenuView.rvMySettingsUserPop.layoutManager = mLayoutManager
        mAdapter = CommonAdapter(mActivity, R.layout.activity_my_settings_user_citypop_item, mList, holderConvert = { holder, t, _, _ ->
            holder.apply {
                setText(R.id.tvMySettingsUserPopRvCity, t)
                getView<LinearLayout>(R.id.rlSelectDialogRvMenu).setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            //每次按下时记录坐标
                            mPointPosition.y = event.rawY.toInt()
                            record[0] = event.rawY.toInt()
                            record[1] = System.currentTimeMillis().toInt()
                        }
                    }
                    false
                }
            }
        }, onItemClick = { _, _, position ->
            //第一次点击
            if (mMenuView.rlMySettingsUserPopTopBottom.visibility == View.GONE) {
                mMenuView.rlMySettingsUserPopTopBottom.visibility = View.VISIBLE
                mMenuView.tvMySettingsUserPopOneTips.text = mList[position]
                mMenuView.tvMySettingsUserPopTips.text = "选择城市"
            } else if (mMenuView.tvMySettingsUserPopTwoDot.visibility == View.GONE) {  //选择第二个城市了
                mMenuView.tvMySettingsUserPopTwoDot.visibility = View.VISIBLE
                mMenuView.tvMySettingsUserPopTwoTips.visibility = View.VISIBLE
                mMenuView.tvMySettingsUserPopTwoTips.text = mList[position]
                mMenuView.tvMySettingsUserPopThreeTips.text = "请选择县"
                mMenuView.tvMySettingsUserPopTips.text = "选择区/县"
            } else {  //选择第三个县
                //调用回调

                ToastUtil.showShort(mList[position])
                dismiss()
            }
        })
        mMenuView.rvMySettingsUserPop.adapter = mAdapter
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        super.showAtLocation(parent, gravity, x, y)
        backgroundAlphaExt(0.5f)
    }

    override fun dismiss() {
        super.dismiss()
        backgroundAlphaExt(1f)
    }

    //改变背景亮度
    private fun backgroundAlphaExt(bgAlpha: Float) {
        val lp = mActivity.window.attributes
        //0.0-1.0
        lp?.alpha = bgAlpha
        mActivity.window.attributes = lp
    }
}