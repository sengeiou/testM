package com.mogujie.tt.ui.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.app.common.api.subscribeExtApi
import com.leimo.wanxin.R
import com.mogujie.tt.api.RequestManager
import com.mogujie.tt.api.composeDefault
import com.mogujie.tt.config.IntentConstant
import com.mogujie.tt.config.UrlConstant
import com.mogujie.tt.db.sp.LoginSp
import com.mogujie.tt.db.sp.SystemConfigSp
import com.mogujie.tt.imservice.event.LoginEvent
import com.mogujie.tt.imservice.event.SocketEvent
import com.mogujie.tt.imservice.service.IMService
import com.mogujie.tt.imservice.support.IMServiceConnector
import com.mogujie.tt.push.PushAllManager
import com.mogujie.tt.ui.base.TTBaseActivity
import com.mogujie.tt.utils.IMUIHelper
import com.mogujie.tt.utils.Logger
import de.greenrobot.event.EventBus
import kotlinx.android.synthetic.main.tt_activity_login.*


/**
 * @YM 1. 链接成功之后，直接判断是否loginSp是否可以直接登陆
 * true: 1.可以登陆，从DB中获取历史的状态
 * 2.建立长连接，请求最新的数据状态 【网络断开没有这个状态】
 * 3.完成
 *
 *
 * false:1. 不能直接登陆，跳转到登陆页面
 * 2. 请求消息服务器地址，链接，验证，触发loginSuccess
 * 3. 保存登陆状态
 */
class LoginActivity : TTBaseActivity() {

    private val logger = Logger.getLogger(LoginActivity::class.java)
    private val uiHandler = Handler()
    private var mNameView: EditText? = null
    private var mPasswordView: EditText? = null
    private var loginPage: View? = null
    private var splashPage: View? = null
    private var mLoginStatusView: View? = null
    private var mSwitchLoginServer: TextView? = null
    private var intputManager: InputMethodManager? = null


    private var mImService: IMService? = null
    private var autoLogin = true
    private var loginSuccess = false

    private val imServiceConnector = object : IMServiceConnector() {
        override fun onServiceDisconnected() {}

        override fun onIMServiceConnected() {
            IMServiceConnector.logger.d("login#onIMServiceConnected")
            mImService = this.imService
            try {
                do {
                    if (imService == null) {
                        //后台服务启动链接失败
                        break
                    }
                    val loginManager = imService!!.loginManager
                    val loginSp = imService!!.loginSp
                    if (loginManager == null || loginSp == null) {
                        // 无法获取登陆控制器
                        break
                    }

                    val loginIdentity = loginSp.loginIdentity
                            ?: // 之前没有保存任何登陆相关的，跳转到登陆页面
                            break

                    mNameView!!.setText(loginIdentity.loginName)
                    if (TextUtils.isEmpty(loginIdentity.pwd)) {
                        // 密码为空，可能是loginOut
                        break
                    }
                    mPasswordView!!.setText(loginIdentity.pwd)

                    if (autoLogin == false) {
                        break
                    }

                    handleGotLoginIdentity(loginIdentity)
                    return
                } while (false)

                // 异常分支都会执行这个
                handleNoLoginIdentity()
            } catch (e: Exception) {
                // 任何未知的异常
                IMServiceConnector.logger.w("loadIdentity failed")
                handleNoLoginIdentity()
            }

        }
    }


    /**
     * 跳转到登陆的页面
     */
    private fun handleNoLoginIdentity() {
        logger.i("login#handleNoLoginIdentity")
        uiHandler.postDelayed({ showLoginPage() }, 1000)
    }

    /**
     * 自动登陆
     */
    private fun handleGotLoginIdentity(loginIdentity: LoginSp.SpLoginIdentity?) {
        logger.i("login#handleGotLoginIdentity")

        uiHandler.postDelayed({
            logger.d("login#start auto login")
            if (mImService == null || mImService?.loginManager == null) {
                Toast.makeText(this@LoginActivity, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                showLoginPage()
            }
            mImService?.loginManager?.login(loginIdentity)
        }, 500)
    }


    private fun showLoginPage() {
        splashPage!!.visibility = View.GONE
        loginPage!!.visibility = View.VISIBLE
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        intputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        logger.d("login#onCreate")

        SystemConfigSp.instance().init(applicationContext)
        if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
            SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER, UrlConstant.ACCESS_MSG_ADDRESS)
        }

        imServiceConnector.connect(this@LoginActivity)
        EventBus.getDefault().register(this)

        setContentView(R.layout.tt_activity_login)
        mSwitchLoginServer = findViewById(R.id.sign_switch_login_server) as TextView
        mSwitchLoginServer!!.setOnClickListener {
            val builder = AlertDialog.Builder(ContextThemeWrapper(this@LoginActivity, android.R.style.Theme_Holo_Light_Dialog))
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val dialog_view = inflater.inflate(R.layout.tt_custom_dialog, null)
            val editText = dialog_view.findViewById(R.id.dialog_edit_content) as EditText
            editText.setText(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))
            val textText = dialog_view.findViewById(R.id.dialog_title) as TextView
            textText.setText(R.string.switch_login_server_title)
            builder.setView(dialog_view)
            builder.setPositiveButton(getString(R.string.tt_ok)) { dialog, which ->
                if (!TextUtils.isEmpty(editText.text.toString().trim { it <= ' ' })) {
                    SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER, editText.text.toString().trim { it <= ' ' })
                    dialog.dismiss()
                }
            }
            builder.setNegativeButton(getString(R.string.tt_cancel)) { dialogInterface, i -> dialogInterface.dismiss() }
            builder.show()
        }

        mNameView = findViewById(R.id.name) as EditText
        mPasswordView = findViewById(R.id.password) as EditText
        mPasswordView!!.setOnEditorActionListener(TextView.OnEditorActionListener { textView, id, keyEvent ->
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })
        mLoginStatusView = findViewById(R.id.login_status)
        sign_in_button.setOnClickListener {
            intputManager!!.hideSoftInputFromWindow(mPasswordView!!.windowToken, 0)
            attemptLogin()
        }
        initAutoLogin()
    }

    private fun initAutoLogin() {
        logger.i("login#initAutoLogin")

        splashPage = findViewById(R.id.splash_page)
        loginPage = findViewById(R.id.login_page)
        autoLogin = shouldAutoLogin()

        splashPage!!.visibility = if (autoLogin) View.VISIBLE else View.GONE
        loginPage!!.visibility = if (autoLogin) View.GONE else View.VISIBLE

        loginPage!!.setOnTouchListener { v, event ->
            if (mPasswordView != null) {
                intputManager!!.hideSoftInputFromWindow(mPasswordView!!.windowToken, 0)
            }

            if (mNameView != null) {
                intputManager!!.hideSoftInputFromWindow(mNameView!!.windowToken, 0)
            }

            false
        }

        if (autoLogin) {
            val splashAnimation = AnimationUtils.loadAnimation(this, R.anim.login_splash)
            if (splashAnimation == null) {
                logger.e("login#loadAnimation login_splash failed")
                return
            }

            splashPage!!.startAnimation(splashAnimation)
        }
    }

    // 主动退出的时候， 这个地方会有值,更具pwd来判断
    private fun shouldAutoLogin(): Boolean {
        val intent = intent
        if (intent != null) {
            val notAutoLogin = intent.getBooleanExtra(IntentConstant.KEY_LOGIN_NOT_AUTO, false)
            logger.d("login#notAutoLogin:%s", notAutoLogin)
            if (notAutoLogin) {
                return false
            }
        }
        return true
    }


    override fun onDestroy() {
        super.onDestroy()

        imServiceConnector.disconnect(this@LoginActivity)
        EventBus.getDefault().unregister(this)
        splashPage = null
        loginPage = null
    }


    fun attemptLogin() {
        var loginName = mNameView!!.text.toString()
        var mPassword = mPasswordView!!.text.toString()
        var cancel = false
        var focusView: View? = null

        if (TextUtils.isEmpty(mPassword)) {
            Toast.makeText(this, getString(R.string.error_pwd_required), Toast.LENGTH_SHORT).show()
            focusView = mPasswordView
            cancel = true
        }

        if (TextUtils.isEmpty(loginName)) {
            Toast.makeText(this, getString(R.string.error_name_required), Toast.LENGTH_SHORT).show()
            focusView = mNameView
            cancel = true
        }

        if (cancel) {
            focusView!!.requestFocus()
        } else {
            showProgress(true)
            if (mImService != null) {
                //				boolean userNameChanged = true;
                //				boolean pwdChanged = true;
                loginName = loginName.trim { it <= ' ' }
                mPassword = mPassword.trim { it <= ' ' }
                mImService?.loginManager?.login(loginName, mPassword)
            }
        }
    }

    private fun login(loginName: String, mPassword: String) {
        RequestManager.instanceApi
                .login(loginName, mPassword)
                .compose(composeDefault())
                .subscribeExtApi({
                    mImService?.loginManager?.login(it.uId, it.token)
                }, context = this)
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            mLoginStatusView!!.visibility = View.VISIBLE
        } else {
            mLoginStatusView!!.visibility = View.GONE
        }
    }

    // 为什么会有两个这个
    // 可能是 兼容性的问题 导致两种方法onBackPressed
    override fun onBackPressed() {
        logger.d("login#onBackPressed")
        //imLoginMgr.cancel();
        // TODO Auto-generated method stub
        super.onBackPressed()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        //        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        //            LoginActivity.this.finish();
        //            return true;
        //        }
        return super.onKeyDown(keyCode, event)
    }


    override fun onStop() {
        super.onStop()
    }

    /**
     * ----------------------------event 事件驱动----------------------------
     */
    fun onEventMainThread(event: LoginEvent) {
        when (event) {
            LoginEvent.LOCAL_LOGIN_SUCCESS, LoginEvent.LOGIN_OK -> onLoginSuccess()
            LoginEvent.LOGIN_AUTH_FAILED, LoginEvent.LOGIN_INNER_FAILED -> if (!loginSuccess) {
                onLoginFailure(event)
            }
        }
    }


    fun onEventMainThread(event: SocketEvent) {
        when (event) {
            SocketEvent.CONNECT_MSG_SERVER_FAILED, SocketEvent.REQ_MSG_SERVER_ADDRS_FAILED -> if (!loginSuccess) {
                onSocketFailure(event)
            }
        }
    }

    private fun onLoginSuccess() {
        logger.i("login#onLoginSuccess")
        loginSuccess = true
//        UmengPush.setAlias(applicationContext, LoginSp.instance().loginIdentity.loginId.toString())
        PushAllManager.push.setAlias(applicationContext, LoginSp.instance().loginIdentity.loginId.toString())
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        this@LoginActivity.finish()
    }

    private fun onLoginFailure(event: LoginEvent) {
        logger.e("login#onLoginError -> errorCode:%s", event.name)
        showLoginPage()
        val errorTip = getString(IMUIHelper.getLoginErrorTip(event))
        logger.d("login#errorTip:%s", errorTip)
        mLoginStatusView!!.visibility = View.GONE
        Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show()
    }

    private fun onSocketFailure(event: SocketEvent) {
        logger.e("login#onLoginError -> errorCode:%s,", event.name)
        showLoginPage()
        val errorTip = getString(IMUIHelper.getSocketErrorTip(event))
        logger.d("login#errorTip:%s", errorTip)
        mLoginStatusView!!.visibility = View.GONE
        Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show()
    }
}
