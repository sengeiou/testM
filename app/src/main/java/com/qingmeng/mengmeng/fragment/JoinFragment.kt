package com.qingmeng.mengmeng.fragment

import android.content.Intent
import com.qingmeng.mengmeng.BaseFragment
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.activity.LoginRegisterActivity
import kotlinx.android.synthetic.main.fragment_join.*

class JoinFragment :BaseFragment(){
    override fun getLayoutId(): Int = R.layout.fragment_join

    override fun initListener() {
        mTextView.setOnClickListener { startActivity(Intent(context!!, LoginRegisterActivity::class.java)) }
    }
}