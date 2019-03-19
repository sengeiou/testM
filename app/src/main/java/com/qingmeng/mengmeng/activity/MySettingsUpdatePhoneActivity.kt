package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_log_register.*
import kotlinx.android.synthetic.main.activity_my_settings_updatephone.*
import kotlinx.android.synthetic.main.layout_head.*
import org.json.JSONObject

/**
 *  Description :设置 - 换绑手机

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/3
 */
@SuppressLint("CheckResult")
class MySettingsUpdatePhoneActivity : BaseActivity() {

    override fun getLayoutId(): Int {
        return R.layout.activity_my_settings_updatephone
    }

    override fun initObject() {
        super.initObject()

        //极验初始化
        GeetestUtil.init(this)
        timerHandler = TimerHandler(this, tvMySettingsUpdatePhoneGetMsg)

        setHeadName(R.string.my_settings_updatePhone)

        //对当前手机号进行处理
        val phone = formatPhone(intent.getStringExtra("phone"))
        etMySettingsUpdatePhoneOld.setText(phone)
    }

    override fun initListener() {
        super.initListener()

        //返回
        mBack.setOnClickListener {
            onBackPressed()
        }

        //获取验证码
        tvMySettingsUpdatePhoneGetMsg.setOnClickListener {
            val phone = etMySettingsUpdatePhoneNew.text.toString().trim()
            if (phone.isNotBlank()) {
                if (InputCheckUtils.checkPhone(phone)) {
                    //先验证手机号是否注册
                    httpCheckPhone(phone)
                } else {
                    ToastUtil.showShort(getString(R.string.phoneFormat_tips))
                }
            } else {
                ToastUtil.showShort(getString(R.string.phoneTips))
            }
        }

        //提交
        tvMySettingsUpdatePhoneSubmit.setOnClickListener {
            val phone = etMySettingsUpdatePhoneNew.text.toString().trim()
            val msg = etMySettingsUpdatePhoneMsg.text.toString().trim()
            if (phone.isNotBlank() && msg.isNotBlank()) {
                if (InputCheckUtils.checkPhone(phone)) {
                    //换绑手机
                    httpUpdatePhone(phone, msg)
                } else {
                    ToastUtil.showShort(getString(R.string.phoneFormat_tips))
                }
            } else {
                if (phone.isBlank()) {
                    ToastUtil.showShort(getString(R.string.phoneTips))
                } else if (msg.isBlank()) {
                    ToastUtil.showShort(getString(R.string.msgTips))
                }
            }
        }
    }

    //验证手机号是否注册
    private fun httpCheckPhone(phone: String) {
        ApiUtils.getApi().hasRegistered(phone, 2)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.apply {
                        if (code == 12000) {
                            GeetestUtil.customVerity({ checkCodeType() }, { sendSmsCode(phone, it) })
                        } else {
                            ToastUtil.showShort(msg)
                        }
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    //校验极验是否可用
    private fun checkCodeType() {
        ApiUtils.getApi().checkCodeType()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    when {
                        bean.code == 12000 -> {
                            bean.data!!.new_captcha = true
                            GeetestUtil.showGeetest(bean.data!!.toJson())
                        }
                        bean.code == 25080 -> {
                            GeetestUtil.dismissGeetestDialog()
                            showImgCode()
                        }
                        else -> {
                            GeetestUtil.dismissGeetestDialog()
                            ToastUtil.showShort(bean.msg)
                        }
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    //展示图片验证码
    private fun showImgCode() {
        myDialog.showImageCodeDialog(mRegisterPhone.text.toString(), 4,
                { addSubscription(it) }, { timerHandler.sendEmptyMessage(timing) })
    }

    /**
     * 发送短信验证码
     * @param type 1极验验证  0图片验证码
     **/
    private fun sendSmsCode(phone: String, result: String) {
        val params = JSONObject(result)
        ApiUtils.getApi().sendSms(phone, 4, geetest_challenge = params.optString("geetest_challenge"),
                geetest_validate = params.optString("geetest_validate"), geetest_seccode = params.optString("geetest_seccode"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.code == 12000) {
                        //倒计时
                        timerHandler.sendEmptyMessage(timing)
                        GeetestUtil.showSuccessDialog()
                    } else {
                        GeetestUtil.showFailedDialog()
                        ToastUtil.showShort(it.msg)
                    }
                }, {
                    GeetestUtil.showFailedDialog()
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    //换绑手机接口
    private fun httpUpdatePhone(phone: String, msg: String) {
        myDialog.showLoadingDialog()
        ApiUtils.getApi()
                .updatePhone(phone, msg, MainApplication.instance.TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    myDialog.dismissLoadingDialog()
                    it.apply {
                        if (code == 12000) {
                            ToastUtil.showShort(getString(R.string.updatePhone_success_tips))
                            //把手机号返回回去
                            setResult(Activity.RESULT_OK, Intent().apply {
                                putExtra("phone", phone)
                            })
                            onBackPressed()
                        } else {
                            ToastUtil.showShort(this.msg)
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                }, {}, { addSubscription(it) })
    }

    //修改手机号格式 188******88格式
    private fun formatPhone(phone: String): String {
        var result = ""
        for (i in 0 until phone.length) {
            if (i in 3..8) {
                result = result + "*"
            } else if (i <= 11) {
                result = result + phone[i]
            }
        }
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        GeetestUtil.destroy()
    }
}