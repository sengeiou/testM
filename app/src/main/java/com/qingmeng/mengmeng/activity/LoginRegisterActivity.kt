package com.qingmeng.mengmeng.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
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
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.constant.IConstants.AVATAR
import com.qingmeng.mengmeng.constant.IConstants.FROM_TYPE
import com.qingmeng.mengmeng.constant.IConstants.THIRD_USERNAME
import com.qingmeng.mengmeng.constant.IConstants.THREE_OPENID
import com.qingmeng.mengmeng.constant.IConstants.THREE_TOKEN
import com.qingmeng.mengmeng.constant.IConstants.THREE_TYPE
import com.qingmeng.mengmeng.constant.IConstants.TYPE
import com.qingmeng.mengmeng.constant.IConstants.WE_CHAT_UNIONID
import com.qingmeng.mengmeng.constant.ImageCodeHandler
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.GeetestUtil
import com.qingmeng.mengmeng.utils.ToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_log_register.*
import org.jetbrains.anko.*
import org.json.JSONObject


@SuppressLint("CheckResult")
class LoginRegisterActivity : BaseActivity() {
    private var mRead = false
    private var from = 0
    private var contentType = 1//1注册，5绑定
    private var mUserName = ""
    private var mPhone = ""
    private var mCode = ""
    private var mPsw = ""
    private var mSurePsw = ""
    private var openId = ""
    private var token = ""
    private var weChatUnionId = ""
    private var thirdUserName = ""
    private var avatar = ""
    private var threeType = 1

    //完信相关
    private var mImService: IMService? = null
    private val imServiceConnector = object : IMServiceConnector() {
        override fun onServiceDisconnected() {}

        override fun onIMServiceConnected() {
            IMServiceConnector.logger.d("login#onIMServiceConnected")
            mImService = this.imService
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_log_register

    override fun initObject() {
        openId = intent.getStringExtra(THREE_OPENID) ?: ""
        token = intent.getStringExtra(THREE_TOKEN) ?: ""
        weChatUnionId = intent.getStringExtra(WE_CHAT_UNIONID) ?: ""
        thirdUserName = intent.getStringExtra(THIRD_USERNAME) ?: ""
        avatar = intent.getStringExtra(AVATAR) ?: ""
        threeType = intent.getIntExtra(THREE_TYPE, 0)
        from = intent.getIntExtra(FROM_TYPE, 0)
        contentType = intent.getIntExtra(TYPE, 1)
        if (contentType == 1) {
            setHeadName(R.string.register)
        } else {
            setHeadName(R.string.bind_phone)
        }
        imgHandler = ImageCodeHandler(this, mRegisterGetCode)
        GeetestUtil.init(this)
        //完信相关
        SystemConfigSp.instance().init(applicationContext)
        if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
            SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER, UrlConstant.ACCESS_MSG_ADDRESS)
        }
        imServiceConnector.connect(this)
    }

    override fun initListener() {
        mRegisterUsername.addTextChangedListener(RegisterTextWatcher())
        mRegisterPhone.addTextChangedListener(RegisterTextWatcher())
        mRegisterCode.addTextChangedListener(RegisterTextWatcher())
        mRegisterPsw.addTextChangedListener(RegisterTextWatcher())
        mRegisterSurePsw.addTextChangedListener(RegisterTextWatcher())

        //用户协议
        mUserProtocol.setOnClickListener {
            startActivityForResult<LoginUserAgreementActivity>(0)
        }
        //是否同意用户协议
        mRegisterAgree.setOnClickListener {
            mRead = if (!mRead) {
                mRegisterAgree.setImageResource(R.drawable.login_icon_yes_read_s)
                true
            } else {
                mRegisterAgree.setImageResource(R.drawable.login_icon_not_read_n)
                false
            }
        }
        //获取验证码
        mRegisterGetCode.setOnClickListener {
            when {
                TextUtils.isEmpty(mUserName) -> ToastUtil.showShort(R.string.user_name_empty)
                TextUtils.isEmpty(mPhone) -> ToastUtil.showShort(R.string.phone_empty)
                else -> hasRegistered(mUserName, mPhone, 1)
            }
        }
        //注册
        mRegisterSure.setOnClickListener {
            val userName = mRegisterUsername.text.toString()
            val phone = mRegisterPhone.text.toString()
            val code = mRegisterCode.text.toString()
            val psw = mRegisterPsw.text.toString()
            val confirmPsw = mRegisterSurePsw.text.toString()
            when {
                TextUtils.isEmpty(userName) -> ToastUtil.showShort(R.string.user_name_empty)
                TextUtils.isEmpty(phone) -> ToastUtil.showShort(R.string.phone_empty)
                TextUtils.isEmpty(code) -> ToastUtil.showShort(R.string.input_code)
                psw.length < 6 || psw.length > 12 -> ToastUtil.showShort(R.string.psw_hint)
                psw != confirmPsw -> ToastUtil.showShort(R.string.psw_inconsistent)
                !mRead -> ToastUtil.showShort(R.string.please_read_accept)
                else -> if (contentType == 1) register() else bindPhone()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = data?.getExtras()?.getInt("agreement")
        if (result == 1) {
            mRead = true
            mRegisterAgree.setImageResource(R.drawable.login_icon_yes_read_s)
        } else if (requestCode == IConstants.LOGIN_BACK && resultCode == Activity.RESULT_OK) {
            if (from == 0) {
                startActivity(intentFor<MainActivity>().newTask().clearTask())
                finish()
            } else {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
//        else {
//            mRead = false
//            mRegisterAgree.setImageResource(R.drawable.login_icon_not_read_n)
//        }
    }

    //绑定手机
    private fun bindPhone() {
        myDialog.showLoadingDialog()
        ApiUtils.getApi().bindPhone(mPhone, mCode, openId, token, avatar,
                threeType, mPsw, mSurePsw, mUserName, weChatUnionId, thirdUserName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code != 12000) myDialog.dismissLoadingDialog()
                    if (bean.code == 12000) {
                        bean.data?.let {
                            MainApplication.instance.user = it
                            MainApplication.instance.TOKEN = it.token
                            it.upDate()
                            //还要登录完信..
                            mImService?.loginManager?.login("${it.wxUid}", it.wxToken)
                        }
                    } else {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    //注册
    private fun register() {
        myDialog.showLoadingDialog()
        ApiUtils.getApi().register(mUserName, mPhone, mCode, mPsw, mSurePsw, 2)
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
                            registerOver()
                        }
                    } else {
                        ToastUtil.showShort(bean.msg)
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    ToastUtil.showNetError()
                }, {}, { addSubscription(it) })
    }

    private fun registerOver() {
        startActivityForResult<MySettingsUserActivity>(IConstants.LOGIN_BACK)
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
        myDialog.showImageCodeDialog(mRegisterPhone.text.toString(), contentType,
                { addSubscription(it) }, { imgHandler.sendEmptyMessage(timing) })
    }

    //发送短信验证码
    private fun sendSmsCode(result: String) {
        val phone = mRegisterPhone.text.toString()
        val params = JSONObject(result)
        ApiUtils.getApi().sendSms(phone, contentType, geetest_challenge = params.optString("geetest_challenge"),
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
        imServiceConnector.disconnect(this)
        GeetestUtil.destroy()
        super.onDestroy()
    }

    inner class RegisterTextWatcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            mUserName = mRegisterUsername.text.toString().trim()
            mPhone = mRegisterPhone.text.toString().trim()
            mCode = mRegisterCode.text.toString().trim()
            mPsw = mRegisterPsw.text.toString().trim()
            mSurePsw = mRegisterSurePsw.text.toString().trim()
            mRegisterSure.enabled = (!TextUtils.isEmpty(mUserName) && !TextUtils.isEmpty(mPhone)
                    && !TextUtils.isEmpty(mCode) && !TextUtils.isEmpty(mPsw) && !TextUtils.isEmpty(mSurePsw))
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }
}