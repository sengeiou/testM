package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.text.TextUtils
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.constant.ImageCodeHandler
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.GeetestUtil
import com.qingmeng.mengmeng.utils.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_log_binding_phone.*
import kotlinx.android.synthetic.main.activity_log_register.*
import org.json.JSONObject

/**
 * Created by mingyue
 * Date: 2019/1/14
 * mail: 153705849@qq.com
 * describe: 绑定手机
 */
@SuppressLint("CheckResult")
class LoginBindingPhoneActivity : BaseActivity() {
    private var mRead = false
    override fun getLayoutId(): Int = R.layout.activity_log_binding_phone

    override fun initObject() {
        imgHandler = ImageCodeHandler(this, tv_get_code_binding_phone)
        GeetestUtil.init(this)
    }

    override fun initListener() {
        //是否同意用户协议
        img_read_binding_phone.setOnClickListener {
            mRead = if (!mRead) {
                img_read_binding_phone.setImageResource(R.drawable.login_icon_yes_read_s)
                true
            } else {
                img_read_binding_phone.setImageResource(R.drawable.login_icon_not_read_n)
                false
            }
        }
        //获取验证码
        tv_get_code_binding_phone.setOnClickListener {
            val userName = edt_input_username_binding_phone.text.toString()
            val phone = edt_input_phone_binding_phone.text.toString()
            when {
                TextUtils.isEmpty(userName) -> ToastUtil.showShort(getString(R.string.user_name_empty))
                TextUtils.isEmpty(phone) -> ToastUtil.showShort(getString(R.string.phone_empty))
                else -> hasRegistered(userName, phone, 1)
            }
        }
        //注册
        btn_login_binding_phone.setOnClickListener {
            val userName = edt_input_username_binding_phone.text.toString()
            val phone = edt_input_phone_binding_phone.text.toString()
            val code = edt_input_code_binding_phone.text.toString()
            val psw = edt_input_password_binding_phone.text.toString()
            val confirmPsw = edt_input_sure_password_binding_phone.text.toString()
            when {
                TextUtils.isEmpty(userName) -> ToastUtil.showShort(getString(R.string.user_name_empty))
                TextUtils.isEmpty(phone) -> ToastUtil.showShort(getString(R.string.phone_empty))
                TextUtils.isEmpty(code) -> ToastUtil.showShort(getString(R.string.input_code))
                psw.length < 6 || psw.length > 12 -> ToastUtil.showShort(getString(R.string.psw_hint))
                psw != confirmPsw -> ToastUtil.showShort(getString(R.string.psw_inconsistent))
                !mRead -> ToastUtil.showShort(getString(R.string.please_read_accept))
                else -> register(userName, phone, code, psw, confirmPsw)
            }
        }
    }

    /*
    *接口需等第三方登录之后再调试
    *
     */
    //注册
    private fun register(userName: String, phone: String, code: String, psw: String, confirmPsw: String) {
        ApiUtils.getApi().register(userName, phone, code, psw, confirmPsw, 2)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            MainApplication.instance.user = it
                            MainApplication.instance.TOKEN = it.token
                            it.upDate()
                        }
                        sharedSingleton.setString(IConstants.LOGIN_PHONE, phone)
                        sharedSingleton.setString(IConstants.LOGIN_PSW, psw)
                        finish()
                    } else {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    //验证手机号，用户名是否合格
    private fun hasRegistered(userName: String, phone: String, type: Int) {
        val check = if (type == 1) userName else phone
        ApiUtils.getApi().hasRegistered(check, type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        if (type == 1) {
                            hasRegistered(userName, phone, 2)
                        } else {
                            GeetestUtil.customVerity({ checkCodeType() }, { sendSmsCode(it) })
                        }
                    } else {
                        ToastUtil.showShort(bean.msg)
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
        myDialog.showImageCodeDialog(edt_input_phone_register.text.toString(), 1,
                { addSubscription(it) }, { imgHandler.sendEmptyMessage(timing) })
    }

    /**
     * 发送短信验证码
     * @param type 1极验验证  0图片验证码
     **/
    private fun sendSmsCode(result: String) {
        val phone = edt_input_phone_register.text.toString()
        val params = JSONObject(result)
        ApiUtils.getApi().sendSms(phone, 1, geetest_challenge = params.optString("geetest_challenge"),
                geetest_validate = params.optString("geetest_validate"), geetest_seccode = params.optString("geetest_seccode"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.code == 12000) {
                        imgHandler.sendEmptyMessage(timing)
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

    override fun onDestroy() {
        super.onDestroy()
        GeetestUtil.destroy()
    }
}