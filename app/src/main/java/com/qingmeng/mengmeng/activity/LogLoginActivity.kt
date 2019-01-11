package com.qingmeng.mengmeng.activity

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

import kotlinx.android.synthetic.main.layout_head.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity
import org.json.JSONObject
import java.util.regex.Pattern

/**
 * Created by mingyue
 * Date: 2019/1/9
 * mail: 153705849@qq.com
 * describe: 登录页面
 */
class LogLoginActivity : BaseActivity() {
    //登录模式,密码登录1,短信验证2
    var loginmode: Int = 1

    override fun getLayoutId(): Int {
        //设置标题
        setHeadName(getString(R.string.login))
        //默认隐藏返回按钮
        var mboolean = false
        setShowBack(mboolean)
        return R.layout.activity_log_login
    }

    //初始化Object
    override fun initObject() {
        imgHandler = ImageCodeHandler(this, btn_login_get_code)
        GeetestUtil.init(this)
    }

    //初始化Listener
    @RequiresApi(Build.VERSION_CODES.O)
    override fun initListener() {
        super.initListener()
        //获取验证隐藏
        btn_login_get_code.visibility = View.GONE
        //密码文本框内容监听
        setEditTextContentListener(edt_login_pwd)
        //密码焦点监听
        setOnFocusChangeListener(edt_login_pwd)
        //返回
        mBack.setOnClickListener {
            this.finish()
        }

        //使用短信验证登录
        tv_login_sms_login.setOnClickListener {
            loginmode = 2
            btn_login_get_code.visibility = View.VISIBLE

        }
        //忘记密码
        tv_login_forget_password.setOnClickListener {

        }
        //获取验证码，默认隐藏
        btn_login_get_code.setOnClickListener {
            val phone = edt_login_account.text.toString()

            when {

                TextUtils.isEmpty(phone) -> ToastUtil.showShort(getString(R.string.phone_empty))
                else ->  //hasRegistered("aaavvvv222", phone, 1)
                    hasRegistered(phone)
            //           GeetestUtil.customVerity({ checkCodeType() }, { sendSmsCode(it) })
            //GeetestUtil.customVerity({ checkCodeType() }, { sendSmsCode("1") })
            }


        }

        //登录
        btn_login.setOnClickListener {
            var username = edt_login_account.text.toString()
            var password = edt_login_pwd.text.toString()
            when {
                TextUtils.isEmpty(username) -> ToastUtil.showShort(getString(R.string.user_name_empty))
                TextUtils.isEmpty(password) -> ToastUtil.showShort(getString(R.string.password_empty))
            //密码登录/短信登录
                loginmode == 1 -> accountlogin(username, password)
                loginmode == 2 -> msmlogin(username, password)
            }


        }
        //QQ登录
        tv_login_other_login_QQ.setOnClickListener {

        }
        //微信登录
        tv_login_other_login_wechat.setOnClickListener {

        }
        //立即注册
        tv_login_sign_up_now.setOnClickListener {
            startActivity<RegisterActivity>()
        }
    }

    //验证手机号是否注册
    private fun hasRegistered(phone: String) {

        ApiUtils.getApi().hasRegistered(phone, 2)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 25089) {
                        GeetestUtil.customVerity({ checkCodeType() }, { sendSmsCode(it) })
                    } else {
                        GeetestUtil.customVerity({ checkCodeType() }, { sendSmsCode(it) })
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
        myDialog.showImageCodeDialog(edt_login_account.text.toString(), 2,
                { addSubscription(it) }, { imgHandler.sendEmptyMessage(timing) })
    }

    /**
     * 发送短信验证码
     * @param type 1极验验证  0图片验证码
     **/
    private fun sendSmsCode(result: String) {
        val phone = edt_login_account.text.toString()
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
     *账号登录
     * username:用户名
     * password：密码
     */
    private fun accountlogin(username: String, password: String) {
        ApiUtils.getApi()
                .accountlogin(username, password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    when (bean.code) {
                    //登录成功
                        12000 -> {
                            bean.data?.let {
                                MainApplication.instance.user = it
                                MainApplication.instance.TOKEN = it.userInfo.token
                                it.upDate()
                            }
                            sharedSingleton.setString(IConstants.USER, username)
                            sharedSingleton.setString(IConstants.LOGIN_PSW, password)
                            finish()
                            ToastUtil.showShort(getString(R.string.login_success))
                        }
                    //错误次数
                        15001 -> {
                            ToastUtil.showShort("$bean.msg,还有${bean.data}次机会")
//                            if (count == 0) {
//                                ToastUtil.showShort(getString(R.string.login_fail_recover_password))
//                            }
                        }
                    //密码错误三次以上
                        25094 -> {
                            ToastUtil.showShort(bean.msg)
                            //找回密码弹窗
                        }
                    //参数有误
                        13000 -> {
                            ToastUtil.showShort(bean.msg)
                        }
                    //用户名不存在
                        25092 -> {
                            ToastUtil.showShort(bean.msg)
                        }
                    //手机号不存在
                        25091 -> {
                            //提示“该手机号尚未注册，是否前去注册？” “注册”和“取消”两个按钮
                            this.finish()
                        }
                    }
                })
    }

    /**
     *短信登录
     * phone:手机号
     * msmCode：短信验证码
     */
    private fun msmlogin(phone: String, msmCode: String) {
        ApiUtils.getApi()
                .accountlogin(phone, msmCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    when (bean.code) {
                    //手机号没有注册
                        25088 -> {
                            ToastUtil.showShort(getString(R.string.phone_not_registered))
                            this.finish()
                        }
                    //登录成功
                        12000 -> {
                            bean.data?.let {
                                MainApplication.instance.user = it
                                it.upDate()
                            }
                            sharedSingleton.setString(IConstants.LOGIN_PHONE, phone)
                            finish()
                            ToastUtil.showShort(getString(R.string.login_success))
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

    /**
     *文本框内容监听
     */
    private fun setEditTextContentListener(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var temp: CharSequence? = null
            private var editStart: Int = 0
            private var editEnd: Int = 0
            // 输入文本之前的状态
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                temp = s
            }

            // 输入文字中的状态，count是一次性输入字符数
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            // 输入文字后的状态
            override fun afterTextChanged(s: Editable) {
                editStart = editText.selectionStart
                editEnd = editText.selectionEnd
                // 限制最大输入字数
                if (temp!!.length > 12) {
                    s.delete(editStart - 1, editEnd)
                    val tempSelection = editStart
                    editText.text = s
                    editText.setSelection(tempSelection)
                    ToastUtil.showShort(getString(R.string.passMax_tips))
                }
            }
        })
    }

    /**
     *文本框焦点监听
     */
    private fun setOnFocusChangeListener(editText: EditText) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {

                //失去焦点
            } else {
                if (editText.text.toString().trim().isNotBlank()) {
                    if (checkPass(editText.text.toString().trim())) {

                    } else {
                        ToastUtil.showShort(getString(R.string.passFormat_tips))
                    }
                }
            }
        }
    }

    /**
     * 6到12位区分大小写密码
     */
    private fun checkPass(pass: String): Boolean {
        val pattern = Pattern.compile("^[a-zA-Z0-9]{6,12}$")
        val matcher = pattern.matcher(pass)
        return matcher.matches()
    }

    override fun onDestroy() {
        super.onDestroy()
        GeetestUtil.destroy()
    }
}