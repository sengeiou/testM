package com.qingmeng.mengmeng.activity

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.view.dialog.DialogCommon
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_log_password_login.*
import kotlinx.android.synthetic.main.layout_head.*
import org.jetbrains.anko.startActivity
import java.util.regex.Pattern


/**
 * Created by mingyue
 * Date: 2019/1/11
 * mail: 153705849@qq.com
 * describe: 密码登录
 */
class LoginpwActivity : BaseActivity() {
    private lateinit var mDialog: DialogCommon   //弹框
    override fun getLayoutId(): Int {


        return R.layout.activity_log_password_login
    }

    //初始化Object
    override fun initObject() {
        super.initObject()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //初始化Listener
    override fun initListener() {
        super.initListener()
        //点击页面其他地方取消EditText的焦点并且隐藏软键盘
        mlogpasswordlogin.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (null != this@LoginpwActivity.getCurrentFocus()) {
                    //点击取消EditText的焦点
                    mlogpasswordlogin.setFocusable(true);
                    mlogpasswordlogin.setFocusableInTouchMode(true);
                    mlogpasswordlogin.requestFocus();
                    /** * 点击空白位置 隐藏软键盘  */
                    val mInputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    return mInputMethodManager!!.hideSoftInputFromWindow(this@LoginpwActivity.getCurrentFocus()!!.getWindowToken(), 0)
                }
                return false
            }
        })
        //设置标题
        setHeadName(getString(R.string.login))
        //标题栏注册
        mMenu.setText(getString(R.string.register))

        //密码文本框内容监听
        setEditTextContentListener(edt_login_pwd_password_login)
        //密码焦点监听
        setOnFocusChangeListener(edt_login_pwd_password_login)
        //返回
        mBack.setOnClickListener {
            this.finish()
        }
        //注册
        mMenu.setOnClickListener {
            startActivity<LoginRegisterActivity>()
        }
        //密码框输入监听
        //当输入账号密码后登录按钮改变
        edt_login_pwd_password_login.addTextChangedListener(object : TextWatcher {
            // 输入文本之前的状态
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            // 输入文字中的状态，count是一次性输入字符数
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (edt_login_pwd_password_login.text.toString().trim().isNotBlank() && edt_login_account_password_login.text.toString().trim().isNotBlank()) {
                    btn_login_password_login.setBackgroundColor(Color.parseColor("#5ab1e1"))
                } else {
                    btn_login_password_login.setBackgroundColor(Color.parseColor("#dcdcdc"))
                }
            }

            // 输入文字后的状态
            override fun afterTextChanged(s: Editable) {
            }
        })
        //手机号框焦点监听
        //当输入手机号后保留
        edt_login_account_password_login.setOnFocusChangeListener { _, hasFocus ->
            if (checkPhoneNum(edt_login_account_password_login.text.toString())) {
                sharedSingleton.setString(IConstants.LOGIN_PHONE, edt_login_account_password_login.text.toString())

            }
        }


        var musername = edt_login_account_password_login.text.toString()
        var mpassword = edt_login_pwd_password_login.text.toString()
        when {
            TextUtils.isEmpty(musername) == false && TextUtils.isEmpty(mpassword) == false -> btn_login_password_login.setBackgroundColor(Color.parseColor("#5ab1e1"))
            else -> btn_login_password_login.setBackgroundColor(Color.parseColor("#dcdcdc"))


        }
        //账号登录
        btn_login_password_login.setOnClickListener {

            var username = edt_login_account_password_login.text.toString()
            var password = edt_login_pwd_password_login.text.toString()
            when {
                TextUtils.isEmpty(username) -> ToastUtil.showShort(getString(R.string.user_name_empty))
                TextUtils.isEmpty(password) -> ToastUtil.showShort(getString(R.string.password_empty))
                else -> accountlogin(username, password)
            }

        }
        //使用短信验证登录
        tv_login_sms_login_password_login.setOnClickListener {
            startActivity<LogincodeActivity>()

        }
        //忘记密码
        tv_login_forget_password_password_login.setOnClickListener {
            startActivity<LoginChangePswActivity>()
        }


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
                                MainApplication.instance.TOKEN = it.token
                                it.upDate()
                            }
                            sharedSingleton.setString(IConstants.USER, username)
                            sharedSingleton.setString(IConstants.LOGIN_PSW, password)
                            finish()
                            //	如果是在应用内操作时提示跳转到登录页面的，登录成功后回到原页面；
                            //  在我的/消息板块点击登录的回到盟盟首页；
                            ToastUtil.showShort(getString(R.string.login_success))
                        }
                    //错误次数
                        15001 -> {
                            ToastUtil.showShort("$bean.msg,还有${bean.data}次机会")

                        }
                    //密码错误三次以上
                        25094 -> {
                            ToastUtil.showShort(bean.msg)
                            //找回密码弹窗
                            mDialog = DialogCommon(this, getString(R.string.retrieve_password), getString(R.string.cancel), onRightClick = {
                                startActivity<LoginChangePswActivity>()
                            })
                            mDialog.show()

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
                            mDialog = DialogCommon(this, getString(R.string.register), getString(R.string.cancel), onRightClick = {
                                startActivity<LoginChangePswActivity>()
                            })
                            mDialog.show()
                            this.finish()
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

    /***
     * 手机号码检测
     */
    fun checkPhoneNum(num: String): Boolean {
        val regExp = "^((13[0-9])|(15[^4])|(18[0-9])|(17[0-8])|(14[5-9])|(166)|(19[8,9])|)\\d{8}$"
        val p = Pattern.compile(regExp)
        val m = p.matcher(num)
        return m.matches()
    }


    /**
     * 6到12位区分大小写密码
     */
    private fun checkPass(pass: String): Boolean {
        val pattern = Pattern.compile("^[a-zA-Z0-9]{6,12}$")
        val matcher = pattern.matcher(pass)
        return matcher.matches()
    }
}