package com.qingmeng.mengmeng.activity

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.mogujie.tt.config.UrlConstant
import com.mogujie.tt.db.sp.SystemConfigSp
import com.mogujie.tt.imservice.event.LoginEvent
import com.mogujie.tt.imservice.service.IMService
import com.mogujie.tt.imservice.support.IMServiceConnector
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.constant.ImageCodeHandler
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.GeetestUtil
import com.qingmeng.mengmeng.utils.ToastUtil
import de.greenrobot.event.EventBus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_log_sms_login.*
import kotlinx.android.synthetic.main.layout_head.*
import org.jetbrains.anko.startActivity
import org.json.JSONObject

/**
 * Created by mingyue
 * Date: 2019/1/11
 * mail: 153705849@qq.com
 * describe: 验证码登录
 */
class LogincodeActivity : BaseActivity() {
    private var mImService: IMService? = null

    //完信相关
    private val imServiceConnector = object : IMServiceConnector() {
        override fun onServiceDisconnected() {}

        override fun onIMServiceConnected() {
            IMServiceConnector.logger.d("login#onIMServiceConnected")
            mImService = this.imService
        }
    }

    override fun getLayoutId(): Int {


        return R.layout.activity_log_sms_login
    }

    //初始化Object
    override fun initObject() {
        super.initObject()
        imgHandler = ImageCodeHandler(this, tv_login_get_code_sms_login)

        //完信相关
        GeetestUtil.init(this)
        SystemConfigSp.instance().init(applicationContext)
        if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
            SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER, UrlConstant.ACCESS_MSG_ADDRESS)
        }
        imServiceConnector.connect(this)
        EventBus.getDefault().register(this)
    }


    //初始化Listener
    @RequiresApi(Build.VERSION_CODES.O)
    override fun initListener() {
        //设置标题
        setHeadName(getString(R.string.login))
        //标题栏注册
        mMenu.setText(getString(R.string.register))
        mMenu.setPadding(0, 0, 10, 0)
        super.initListener()
        //返回
        mBack.setOnClickListener {
            onBackPressed()
        }
        //点击页面其他地方取消EditText的焦点并且隐藏软键盘
        mlogsmslogin.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (null != this@LogincodeActivity.getCurrentFocus()) {
                    //点击取消EditText的焦点
                    mlogsmslogin.setFocusable(true);
                    mlogsmslogin.setFocusableInTouchMode(true);
                    mlogsmslogin.requestFocus();
                    /** * 点击空白位置 隐藏软键盘  */
                    val mInputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    return mInputMethodManager!!.hideSoftInputFromWindow(this@LogincodeActivity.getCurrentFocus()!!.getWindowToken(), 0)
                }
                return false
            }
        })
        //注册
        mMenu.setOnClickListener {
            startActivity<LoginRegisterActivity>()
        }
        //验证码框输入监听
        //当输入手机号验证码后登录按钮改变
        edt_login_code_sms_login.addTextChangedListener(object : TextWatcher {
            // 输入文本之前的状态
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            // 输入文字中的状态，count是一次性输入字符数
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (edt_login_code_sms_login.text.toString().trim().isNotBlank() && edt_login_phone_sms_login.text.toString().trim().isNotBlank()) {
                    btn_login_sms_login.setBackgroundColor(Color.parseColor("#5ab1e1"))
                } else {
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
                else -> msmlogin(phone, code)
            }
        }
        //获取验证码
        tv_login_get_code_sms_login.setOnClickListener {
            val phone = edt_login_phone_sms_login.text.toString()

            when {

                TextUtils.isEmpty(phone) -> ToastUtil.showShort(getString(R.string.phone_empty))
                else -> hasRegistered(phone)
            }
        }
        //使用账号密码登录
        tv_login_name_login_sms_login.setOnClickListener {
            startActivity<LoginpwActivity>()
        }
        //忘记密码
        tv_login_forget_password_sms_login.setOnClickListener {
            startActivity<LoginChangePswActivity>()
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
        myDialog.showLoadingDialog()
        ApiUtils.getApi()
                .smslogin(phone, msmCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    when (bean.code) {
                    //手机号没有注册
                        25088 -> {
                            myDialog.dismissLoadingDialog()
                            ToastUtil.showShort(getString(R.string.phone_not_registered))
                        }
                    //登录成功
                        12000 -> {
                            bean.data?.let {
                                MainApplication.instance.user = it
                                MainApplication.instance.TOKEN = it.token
                                it.upDate()
                                //取wxName和wxPwd登录完信
                                wanxinLogin(it.userInfo.wxName, it.userInfo.wxPwd)
                            }
                            sharedSingleton.setString(IConstants.LOGIN_PHONE, phone)
                        }
                    //参数有误
                        13000 -> {
                            myDialog.dismissLoadingDialog()
                            ToastUtil.showShort(bean.msg)
                        }
                    //验证码不正确
                        10000 -> {
                            myDialog.dismissLoadingDialog()
                            ToastUtil.showShort(bean.msg)
                        }
                    //验证码不正确
                        15002 -> {
                            myDialog.dismissLoadingDialog()
                            ToastUtil.showShort(bean.msg)
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                })
    }

    //完信登录
    private fun wanxinLogin(wxName: String, wxPwd: String) {
        ApiUtils.getApi()
                .wanxinlogin(wxName, wxPwd)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.apply {
                        when (code) {
                            12000 -> {
                                data?.let {
                                    MainApplication.instance.wanxinUser = it
                                    it.upDate()
                                    //还要登录完信..
                                    mImService?.loginManager?.login("${it.uId}", it.token)
                                }
                            }
                            else -> {
                                ToastUtil.showShort(msg)
                            }
                        }
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                })
    }

    //EventBus消费事件
    fun onEventMainThread(event: LoginEvent) {
        when (event) {
            LoginEvent.LOCAL_LOGIN_SUCCESS, LoginEvent.LOGIN_OK -> {
                myDialog.dismissLoadingDialog()
                ToastUtil.showShort(getString(R.string.login_success))
                this@LogincodeActivity.finish()
                //	如果是在应用内操作时提示跳转到登录页面的，登录成功后回到原页面；
                //  在我的/消息板块点击登录的回到盟盟首页；
            }
            LoginEvent.LOGIN_AUTH_FAILED, LoginEvent.LOGIN_INNER_FAILED -> {

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GeetestUtil.destroy()
        imServiceConnector.disconnect(this)
        EventBus.getDefault().unregister(this)
    }
}