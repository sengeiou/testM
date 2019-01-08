package com.qingmeng.mengmeng.activity

import android.text.TextUtils
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity() {
    override fun getLayoutId(): Int = R.layout.activity_register

    override fun initListener() {
        mRegisterGetCode.setOnClickListener {
            val userName = mRegisterUserName.text.toString()
            val phone = mRegisterPhone.text.toString()
            when {
                TextUtils.isEmpty(userName) -> ToastUtil.showShort(getString(R.string.user_name_empty))
                TextUtils.isEmpty(phone) -> ToastUtil.showShort(getString(R.string.phone_empty))
                else -> hasRegistered(userName, phone)
            }
        }
    }

    private fun hasRegistered(userName: String, phone: String) {

    }
}