package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import com.mogujie.tt.config.UrlConstant
import com.mogujie.tt.db.sp.SystemConfigSp
import com.mogujie.tt.imservice.service.IMService
import com.mogujie.tt.imservice.support.IMServiceConnector
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.constant.IConstants.FROM_TYPE
import com.qingmeng.mengmeng.constant.ImageCodeHandler
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.GeetestUtil
import com.qingmeng.mengmeng.utils.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login_change_password.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.enabled
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.json.JSONObject


/**
 * Created by mingyue
 * Date: 2019/1/11
 * mail: 153705849@qq.com
 * describe: 忘记密码
 */
@SuppressLint("CheckResult")
class LoginChangePswActivity : BaseActivity() {
    private var mPhone = ""
    private var mCode = ""
    private var mPsw = ""
    private var mSurePsw = ""
    private var from = 0

    //完信相关
    private var mImService: IMService? = null
    private val imServiceConnector = object : IMServiceConnector() {
        override fun onServiceDisconnected() {}

        override fun onIMServiceConnected() {
            IMServiceConnector.logger.d("login#onIMServiceConnected")
            mImService = this.imService
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_login_change_password

    //初始化Object
    override fun initObject() {
        super.initObject()
        //设置标题
        setHeadName(R.string.retrieve_password)
        from = intent.getIntExtra(FROM_TYPE, from)
        imgHandler = ImageCodeHandler(this, mForgerGetCode)
        GeetestUtil.init(this)
        //完信相关
        SystemConfigSp.instance().init(applicationContext)
        if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
            SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER, UrlConstant.ACCESS_MSG_ADDRESS)
        }
        imServiceConnector.connect(this)
    }

    //初始化Listener
    override fun initListener() {
        super.initListener()
        //输入监听 当全部输入完成后确定按钮改变
        mForgetPhone.addTextChangedListener(ForgetTextWatcher())
        mForgerPsw.addTextChangedListener(ForgetTextWatcher())
        mForgetSurePsw.addTextChangedListener(ForgetTextWatcher())
        mForgerCode.addTextChangedListener(ForgetTextWatcher())
        //获取验证码
        mForgerGetCode.setOnClickListener {
            when {
                TextUtils.isEmpty(mPhone) -> ToastUtil.showShort(R.string.phoneTips)
                else -> hasRegistered(mPhone)
            }
        }
        //确定
        mForgetSure.setOnClickListener {
            when {
                TextUtils.isEmpty(mPhone) -> ToastUtil.showShort(R.string.phoneTips)
                TextUtils.isEmpty(mCode) -> ToastUtil.showShort(R.string.msgTips)
                mPsw.length < 6 || mPsw.length > 12 -> ToastUtil.showShort(R.string.please_input_password)
                mPsw != mSurePsw -> ToastUtil.showShort(R.string.psw_inconsistent)
                else -> forgetPsw(mPhone, mCode, mPsw, mSurePsw)
            }
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
                    if (bean.code != 12000) {
                        GeetestUtil.dismissGeetestDialog()
                    }
                    when (bean.code) {
                        12000 -> bean.data?.let {
                            it.new_captcha = true
                            GeetestUtil.showGeetest(it.toJson())
                        }
                        25080 -> showImgCode()
                        else -> ToastUtil.showShort(bean.msg)
                    }
                }, {
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    //展示图片验证码
    private fun showImgCode() {
        myDialog.showImageCodeDialog(mForgetPhone.text.toString(), 3,
                { addSubscription(it) }, { imgHandler.sendEmptyMessage(timing) })
    }

    //发送短信验证码
    private fun sendSmsCode(result: String) {
        val phone = mForgetPhone.text.toString()
        val params = JSONObject(result)
        ApiUtils.getApi().sendSms(phone, 3, geetest_challenge = params.optString("geetest_challenge"),
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
     * 忘记密码
     * phone:手机号
     * msmCode：短信验证码
     * password:新密码
     * surePassword：确认密码
     */
    private fun forgetPsw(phone: String, smsCode: String, password: String, surePassword: String) {
        myDialog.showLoadingDialog()
        ApiUtils.getApi().forgetpassword(phone, smsCode, password, surePassword)
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
                            changeOver()
                        }
                        //手机号没有注册
                        //参数有误
                        else -> ToastUtil.showShort(bean.msg)
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    private fun changeOver() {
        if (from == 0) {
            startActivity(intentFor<MainActivity>().newTask().clearTask())
        } else {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onDestroy() {
        imServiceConnector.disconnect(this)
        GeetestUtil.destroy()
        super.onDestroy()
    }

    inner class ForgetTextWatcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            mPhone = mForgetPhone.text.toString().trim()
            mCode = mForgerCode.text.toString().trim()
            mPsw = mForgerPsw.text.toString().trim()
            mSurePsw = mForgetSurePsw.text.toString().trim()
            mForgetSure.enabled = (!TextUtils.isEmpty(mPhone) && !TextUtils.isEmpty(mCode)
                    && !TextUtils.isEmpty(mPsw) && !TextUtils.isEmpty(mSurePsw))
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

    }
}