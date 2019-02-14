package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import com.mogujie.tt.config.UrlConstant
import com.mogujie.tt.db.sp.SystemConfigSp
import com.mogujie.tt.imservice.service.IMService
import com.mogujie.tt.imservice.support.IMServiceConnector
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.constant.IConstants.FROM_TYPE
import com.qingmeng.mengmeng.constant.IConstants.LOGIN_BACK
import com.qingmeng.mengmeng.constant.IConstants.LOGIN_TYPE
import com.qingmeng.mengmeng.constant.ImageCodeHandler
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.GeetestUtil
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.view.dialog.DialogCommon
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_log_password_login.*
import kotlinx.android.synthetic.main.layout_head.*
import org.jetbrains.anko.*
import org.json.JSONObject


/**
 * Created by mingyue
 * Date: 2019/1/11
 * mail: 153705849@qq.com
 * describe: 密码登录
 */
@SuppressLint("CheckResult")
class LoginPwdActivity : BaseActivity() {
    private lateinit var mDialog: DialogCommon   //弹框
    private var loginType = 0 //0账号密码登录 1验证码登录
    private var from = 0
    private var mUsername = ""
    private var mPassword = ""
    private var mCode = ""

    //完信相关
    private var mImService: IMService? = null
    private val imServiceConnector = object : IMServiceConnector() {
        override fun onServiceDisconnected() {}

        override fun onIMServiceConnected() {
            IMServiceConnector.logger.d("login#onIMServiceConnected")
            mImService = this.imService
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_log_password_login

    //初始化Object
    override fun initObject() {
        //设置标题
        setHeadName(R.string.login)
        //标题栏注册
        mMenu.text = getString(R.string.register)
        loginType = intent.getIntExtra(LOGIN_TYPE, 0)
        from = intent.getIntExtra(FROM_TYPE, 0)
        setLoginType(loginType)
        GeetestUtil.init(this)
        imgHandler = ImageCodeHandler(this, mLoginGetCode)
        //完信相关
        SystemConfigSp.instance().init(applicationContext)
        if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
            SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER, UrlConstant.ACCESS_MSG_ADDRESS)
        }
        imServiceConnector.connect(this)
    }

    override fun onDestroy() {
        imServiceConnector.disconnect(this)
        GeetestUtil.destroy()
        super.onDestroy()
    }

    //初始化Listener
    override fun initListener() {
        //注册
        mMenu.setOnClickListener { startActivityForResult<LoginRegisterActivity>(LOGIN_BACK, FROM_TYPE to from) }
        mLoginGetCode.setOnClickListener {
            val phone = mPasswordPhone.text.toString()
            if (TextUtils.isEmpty(phone)) {
                ToastUtil.showShort(R.string.phone_empty)
            } else {
                hasRegistered(phone)
            }
        }
        //密码框输入监听
        //当输入账号密码后登录按钮改变
        mPasswordPhone.addTextChangedListener(LoginTextWatcher())
        mPasswordPsw.addTextChangedListener(LoginTextWatcher())
        mLoginCodeInput.addTextChangedListener(LoginTextWatcher())
        //账号登录
        mPasswordLogin.setOnClickListener { login(loginType) }
        //使用短信验证登录
        mPasswordSmsLogin.setOnClickListener {
            loginType = if (loginType == 0) 1 else 0
            setLoginType(loginType)
        }
        //忘记密码
        mPasswordForget.setOnClickListener { startActivityForResult<LoginChangePswActivity>(LOGIN_BACK, FROM_TYPE to from) }
    }

    private fun login(loginType: Int) {
        when {
            TextUtils.isEmpty(mUsername) -> ToastUtil.showShort(R.string.user_name_empty)
            loginType == 0 && (mPassword.length > 12 || mPassword.length < 6) -> ToastUtil.showShort(R.string.psw_hint)
            loginType == 1 && TextUtils.isEmpty(mCode) -> ToastUtil.showShort(R.string.scuuess_code)
            else -> if (loginType == 0) accountLogin(mUsername, mPassword) else codeLogin(mUsername, mCode)
        }
    }

    private fun codeLogin(phone: String, code: String) {
        myDialog.showLoadingDialog()
        ApiUtils.getApi()
                .smslogin(phone, code)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    myDialog.dismissLoadingDialog()
                    if (bean.code == 12000) {
                        bean.data?.let {
                            MainApplication.instance.user = it
                            MainApplication.instance.TOKEN = it.token
                            it.upDate()
                            //还要登录完信..
                            mImService?.loginManager?.login("${it.wxUid}", it.wxToken)
                            loginOver()
                        }
                    } else {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    ToastUtil.showNetError()
                    myDialog.dismissLoadingDialog()
                }, {}, { addSubscription(it) })
    }

    /**
     * 账号登录
     * @param username 用户名
     * @param password 密码
     */
    private fun accountLogin(username: String, password: String) {
        myDialog.showLoadingDialog()
        ApiUtils.getApi()
                .accountLogin(username, password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    myDialog.dismissLoadingDialog()
                    when (bean.code) {
                        //登录成功
                        12000 -> bean.data?.let {
                            MainApplication.instance.user = it
                            MainApplication.instance.TOKEN = it.token
                            it.upDate()
                            //还要登录完信..
                            mImService?.loginManager?.login("${it.wxUid}", it.wxToken)
                            loginOver()
                        }
                        //错误次数
                        15001 -> ToastUtil.showShort("${bean.msg},还有${bean.data}次机会")
                        //密码错误三次以上
                        25094 -> {
                            //找回密码弹窗
                            mDialog = DialogCommon(this, bean.msg, leftText = getString(R.string.cancel),
                                    rightText = getString(R.string.retrieve_password), onRightClick = {
                                startActivity<LoginChangePswActivity>()
                            })
                            mDialog.show()
                        }
                        //手机号不存在
                        25091 -> {
                            //提示“该手机号尚未注册，是否前去注册？” “注册”和“取消”两个按钮
                            mDialog = DialogCommon(this, bean.msg, leftText = getString(R.string.cancel),
                                    rightText = getString(R.string.register), onRightClick = {
                                startActivityForResult<LoginRegisterActivity>(LOGIN_BACK, FROM_TYPE to from)
                            })
                            mDialog.show()
                        }
                        else -> ToastUtil.showShort(bean.msg)
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    private fun loginOver() {
        if (from == 0) {
            startActivity(intentFor<MainActivity>().newTask().clearTask())
        } else {
            setResult(Activity.RESULT_OK)
            finish()
        }
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
                    } else {
                        ToastUtil.showShort(getString(R.string.phone_not_register))
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
        myDialog.showImageCodeDialog(mUsername, 2,
                { addSubscription(it) }, { imgHandler.sendEmptyMessage(timing) })
    }

    //发送短信验证码
    private fun sendSmsCode(result: String) {
        val phone = mPasswordPhone.text.toString()
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

    private fun setLoginType(type: Int) {
        mPasswordPhone.setText("")
        mPasswordPsw.setText("")
        mLoginCodeInput.setText("")
        if (type == 0) {
            mLoginPsw.visibility = View.VISIBLE
            mLoginCode.visibility = View.GONE
            mPasswordPhone.setHint(R.string.please_input_user_name_or_phone_num)
            mPasswordSmsLogin.setText(R.string.use_sms_verify_login)
        } else {
            mLoginPsw.visibility = View.GONE
            mLoginCode.visibility = View.VISIBLE
            mPasswordPhone.setHint(R.string.please_input_phone_num)
            mPasswordSmsLogin.setText(R.string.use_name_login)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_BACK && resultCode == Activity.RESULT_OK) {
            setResult(resultCode)
            finish()
        }
    }

    inner class LoginTextWatcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            mUsername = mPasswordPhone.text.toString().trim()
            mPassword = mPasswordPsw.text.toString().trim()
            mCode = mLoginCodeInput.text.toString().trim()
            if (loginType == 0) {
                mPasswordLogin.enabled = !TextUtils.isEmpty(mUsername) && !TextUtils.isEmpty(mPassword)
            } else {
                mPasswordLogin.enabled = !TextUtils.isEmpty(mUsername) && !TextUtils.isEmpty(mCode)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }
}