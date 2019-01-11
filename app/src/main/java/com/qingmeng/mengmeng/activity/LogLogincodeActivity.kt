package com.qingmeng.mengmeng.activity

import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
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
import kotlinx.android.synthetic.main.activity_log_login.*
import kotlinx.android.synthetic.main.activity_log_main_login.*
import kotlinx.android.synthetic.main.activity_log_password_login.*
import kotlinx.android.synthetic.main.activity_log_sms_login.*

import kotlinx.android.synthetic.main.layout_head.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity
import org.json.JSONObject
import java.util.regex.Pattern

/**
 * Created by mingyue
 * Date: 2019/1/11
 * mail: 153705849@qq.com
 * describe: 验证码登录
 */
class LogLogincodeActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        //设置标题
        setHeadName(getString(R.string.login))
        return R.layout.activity_log_sms_login
    }
    //初始化Object
    override fun initObject() {
        super.initObject()
        imgHandler = ImageCodeHandler(this, tv_login_get_code_sms_login)
        GeetestUtil.init(this)
    }


    //初始化Listener
    @RequiresApi(Build.VERSION_CODES.O)
    override fun initListener() {
        super.initListener()
        //返回
        mBack.setOnClickListener {
            this.finish()
        }
        //验证码框输入监听
        //当输入手机号验证码后登录按钮改变
        edt_login_code_sms_login.addTextChangedListener(object : TextWatcher {
            // 输入文本之前的状态
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            // 输入文字中的状态，count是一次性输入字符数
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (edt_login_code_sms_login.text.toString().trim().isNotBlank()&&edt_login_phone_sms_login.text.toString().trim().isNotBlank()) {
                    btn_login_sms_login.setBackgroundColor(Color.parseColor("#5ab1e1"))
                }else{
                    btn_login_sms_login.setBackgroundColor(Color.parseColor("#dcdcdc"))
                }
            }
            // 输入文字后的状态
            override fun afterTextChanged(s: Editable) {
            }
        })
        //验证码登录
        btn_login_sms_login.setOnClickListener {
            var phone = edt_login_phone_sms_login.text.toString()
            var code = edt_login_code_sms_login.text.toString()
            when {
                TextUtils.isEmpty(phone) -> ToastUtil.showShort(getString(R.string.phone_empty))
                TextUtils.isEmpty(code) -> ToastUtil.showShort(getString(R.string.scuuess_code))
                else ->  msmlogin(phone,code)
            }


        }
        //获取验证码
        tv_login_get_code_sms_login.setOnClickListener{
            val phone = edt_login_phone_sms_login.text.toString()

            when {

                TextUtils.isEmpty(phone) -> ToastUtil.showShort(getString(R.string.phone_empty))
                else ->     hasRegistered(phone)
           }
        }
        //使用账号密码登录
        tv_login_name_login_sms_login.setOnClickListener {
            startActivity<LogLoginpwActivity>()
        }
        //忘记密码
        tv_login_forget_password_sms_login.setOnClickListener {  }

    }
    //验证手机号是否注册
    private fun hasRegistered(phone: String) {

        ApiUtils.getApi().hasRegistered(phone, 2)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    //已注册
                    if (bean.code == 25089) {
                        GeetestUtil.customVerity({ checkCodeType() }, { sendSmsCode(it) })
                    }
                    //未注册去注册
                    else {
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
        myDialog.showImageCodeDialog(edt_login_phone_sms_login.text.toString(), 2,
                { addSubscription(it) }, { imgHandler.sendEmptyMessage(timing) })
    }
    /**
     * 发送短信验证码
     * @param type 1极验验证  0图片验证码
     **/
    private fun sendSmsCode(result: String) {
        val phone = edt_login_phone_sms_login.text.toString()
        val params = JSONObject(result)
        ApiUtils.getApi().sendSms(phone, 2, geetest_challenge = params.optString("geetest_challenge"),
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
    /**
     *短信登录
     * phone:手机号
     * msmCode：短信验证码
     */
    private fun msmlogin(phone: String, msmCode: String) {
        ApiUtils.getApi()
                .msmlogin(phone, msmCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    when (bean.code) {
                    //手机号没有注册
                        25088 -> {
                            ToastUtil.showShort(getString(R.string.phone_not_registered))
                        }
                    //登录成功
                        12000 -> {
                            bean.data?.let {
                                MainApplication.instance.user = it
                                it.upDate()
                            }
                            sharedSingleton.setString(IConstants.LOGIN_PHONE, phone)
                            ToastUtil.showShort(getString(R.string.login_success))
                            this.finish()
                            //	如果是在应用内操作时提示跳转到登录页面的，登录成功后回到原页面；
                            //  在我的/消息板块点击登录的回到盟盟首页；

                        }
                    //参数有误
                        13000 -> {
                            ToastUtil.showShort(bean.msg)
                        }
                    //验证码不正确
                        10000 -> {
                            ToastUtil.showShort(bean.msg)
                        }
                    //验证码不正确
                        15002 -> {
                            ToastUtil.showShort(bean.msg)
                        }
                    }
                })
    }
    override fun onDestroy() {
        super.onDestroy()
        GeetestUtil.destroy()
    }
}