package com.qingmeng.mengmeng.activity

import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.GlideCacheUtils
import com.qingmeng.mengmeng.utils.TimerHandler
import com.qingmeng.mengmeng.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_my_settings_updatephone.*
import kotlinx.android.synthetic.main.layout_head.*
import java.util.regex.Pattern

/**
 *  Description :设置 - 换绑手机

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
class MySettingsUpdatePhoneActivity : BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_my_settings_updatephone
    }

    override fun initObject() {
        super.initObject()

        timerHandler = TimerHandler(this, tvMySettingsUpdatePhoneGetMsg)

        setHeadName(getString(R.string.my_settings_updatePhone))
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            this.finish()
        }

        //获取验证码
        tvMySettingsUpdatePhoneGetMsg.setOnClickListener {
            val phone = etMySettingsUpdatePhoneNew.text.toString().trim()
            if (phone.isNotBlank()) {
                if (checkTel(phone)) {
                    //先验证手机号是否注册
//                    checkPhone(phone)

                    //倒计时
                    timerHandler.sendEmptyMessage(timing)
                } else {
                    ToastUtil.showShort(getString(R.string.phoneFormat_tips))
                }
            } else {
                ToastUtil.showShort(getString(R.string.phoneTips))
            }
        }

        //提交
        tvMySettingsUpdatePhoneSubmit.setOnClickListener {
            val phoneOld = etMySettingsUpdatePhoneOld.text.toString().trim()
            val phone = etMySettingsUpdatePhoneNew.text.toString().trim()
            val msg = etMySettingsUpdatePhoneMsg.text.toString().trim()
            if (phoneOld.isNotBlank() && phone.isNotBlank() && msg.isNotBlank()) {
                if (checkTel(phoneOld) && checkTel(phone)) {
                    //换绑手机
                    updatePhone(phone, msg)
                } else {
                    ToastUtil.showShort(getString(R.string.phoneFormat_tips))
                }
            } else {
                if (phoneOld.isBlank() || phone.isBlank()) {
                    ToastUtil.showShort(getString(R.string.phoneTips))
                } else if (msg.isBlank()) {
                    ToastUtil.showShort(getString(R.string.msgTips))
                }
            }
        }
    }

    //换绑手机接口
    private fun updatePhone(phone: String, msg: String) {

        ToastUtil.showShort(getString(R.string.updatePhone_success_tips))
    }

    /**
     * 正则验证手机号
     */
    private fun checkTel(tel: String): Boolean {
        val pattern = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$")
        val matcher = pattern.matcher(tel)
        return matcher.matches()
    }
}