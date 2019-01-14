package com.qingmeng.mengmeng.activity

import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import kotlinx.android.synthetic.main.layout_head.*

/**
 * Created by leimo on 2019/1/14.
 */
class JoinFeedbackActivity: BaseActivity() {

    override fun getLayoutId(): Int {

        return R.layout.activity_join_feedback
    }

    //初始化Listener
    override fun initListener() {
        super.initListener()
        //设置标题
        setHeadName(getString(R.string.join_feedback))
        //标题栏提交
        mMenu.setText(getString(R.string.submit))
    }
}