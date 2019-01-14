package com.qingmeng.mengmeng.fragment

import android.view.View
import cn.bingoogolapple.bgabanner.BGABanner
import com.qingmeng.mengmeng.BaseFragment
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.dp2px
import com.qingmeng.mengmeng.utils.getBarHeight
import com.qingmeng.mengmeng.utils.setMarginExt
import com.qingmeng.mengmeng.view.dialog.ShareDialog
import kotlinx.android.synthetic.main.layout_head.*

class NewspaperFragment : BaseFragment() {
    private lateinit var mBGABaner: BGABanner
    private lateinit var mBottomDialog: ShareDialog
    private lateinit var mList: IntArray
    override fun getLayoutId(): Int = R.layout.fragment_head_newspaper
    override fun initObject() {
        super.initObject()
        mTitle.setText(R.string.tab_name_head_newspaper)
        // 获得状态栏高度
        val statusBarHeight = getBarHeight(context!!)
        //给布局的高度重新设置一下 加上状态栏高度
        mTopView.layoutParams.height = mTopView.layoutParams.height + getBarHeight(context!!)
        mTitle.setMarginExt(top = statusBarHeight + context!!.dp2px(60))
        mMenu.setBackgroundResource(R.drawable.icon_head_details_share)
        mBack.visibility = View.GONE
    }

    override fun initListener() {
        super.initListener()
        mMenu.setOnClickListener {
            mBottomDialog = ShareDialog(context!!)
            mBottomDialog.show()
        }
    }

    private fun setDate() {

    }

    override fun initData() {
        super.initData()
    }


}