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
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject

@SuppressLint("CheckResult")
class RegisterActivity : BaseActivity() {
    private var mRead = false
    override fun getLayoutId(): Int = R.layout.activity_register

    override fun initObject() {
        imgHandler = ImageCodeHandler(this, mRegisterGetCode)
        GeetestUtil.init(this)
    }

    override fun initListener() {
        //是否同意用户协议
        mRegisterAccept.setOnClickListener {
            mRead = if (!mRead) {
                mRegisterAccept.setImageResource(R.drawable.login_icon_refresh)
                true
            } else {
                mRegisterAccept.setImageResource(R.drawable.common_btn_back)
                false
            }
        }
        //获取验证码
        mRegisterGetCode.setOnClickListener {
            val userName = mRegisterUserName.text.toString()
            val phone = mRegisterPhone.text.toString()
            when {
                TextUtils.isEmpty(userName) -> ToastUtil.showShort(getString(R.string.user_name_empty))
                TextUtils.isEmpty(phone) -> ToastUtil.showShort(getString(R.string.phone_empty))
                else -> hasRegistered(userName, phone, 1)
            }
        }
        //注册
        mRegister.setOnClickListener {
            val userName = mRegisterUserName.text.toString()
            val phone = mRegisterPhone.text.toString()
            val code = mRegisterCode.text.toString()
            val psw = mRegisterPsw.text.toString()
            val confirmPsw = mRegisterConfirmPsw.text.toString()
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
        myDialog.showImageCodeDialog(mRegisterPhone.text.toString(), 1,
                { addSubscription(it) }, { imgHandler.sendEmptyMessage(timing) })
    }

    /**
     * 发送短信验证码
     * @param type 1极验验证  0图片验证码
     **/
    private fun sendSmsCode(result: String) {
        val phone = mRegisterPhone.text.toString()
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