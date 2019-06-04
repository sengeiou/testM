package com.qingmeng.mengmeng.activity

import AppManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import cn.bingoogolapple.bgabanner.BGABanner
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mogujie.tt.imservice.service.IMService
import com.mogujie.tt.imservice.support.IMServiceConnector
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.constant.IConstants.AVATAR
import com.qingmeng.mengmeng.constant.IConstants.FROM_TYPE
import com.qingmeng.mengmeng.constant.IConstants.LOGIN_BACK
import com.qingmeng.mengmeng.constant.IConstants.LOGIN_TYPE
import com.qingmeng.mengmeng.constant.IConstants.THIRD_USERNAME
import com.qingmeng.mengmeng.constant.IConstants.THREE_OPENID
import com.qingmeng.mengmeng.constant.IConstants.THREE_TOKEN
import com.qingmeng.mengmeng.constant.IConstants.THREE_TYPE
import com.qingmeng.mengmeng.constant.IConstants.TYPE
import com.qingmeng.mengmeng.constant.IConstants.WE_CHAT_UNIONID
import com.qingmeng.mengmeng.entity.Banner
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.BoxUtils
import com.qingmeng.mengmeng.utils.OpenMallApp
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.loginshare.QQThreeLogin
import com.qingmeng.mengmeng.utils.loginshare.bean.WxBean
import com.qingmeng.mengmeng.utils.loginshare.bean.WxInfoBean
import com.qingmeng.mengmeng.utils.loginshare.bean.WxTokenBean
import com.qingmeng.mengmeng.view.AlphaPageTransformer
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import de.greenrobot.event.EventBus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_log_main_login.*
import org.jetbrains.anko.*


/**
 * Created by mingyue
 * Date: 2019/1/11
 * mail: 153705849@qq.com
 * describe: 登录页面首页
 */
@SuppressLint("CheckResult")
class LoginMainActivity : BaseActivity(), BGABanner.Delegate<ImageView, Banner>, BGABanner.Adapter<ImageView, Banner> {
    private var from = 0 //1商品详情 0其他
    private var mVersion = ""
    private var mQQThreeLogin = QQThreeLogin()
    private var mImgList = ArrayList<Banner>()
    private var mWeChatApi: IWXAPI? = null       //微信api

    //完信相关
    private var mImService: IMService? = null
    private val imServiceConnector = object : IMServiceConnector() {
        override fun onServiceDisconnected() {}

        override fun onIMServiceConnected() {
            IMServiceConnector.logger.d("login#onIMServiceConnected")
            mImService = this.imService
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_log_main_login

    //初始化Object
    override fun initObject() {
        AppManager.instance.addActivity(this)
        //设置状态栏隐藏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
        from = intent.getIntExtra(FROM_TYPE, 0)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        imServiceConnector.connect(this)
    }

    override fun initData() {
        val bannerData = BoxUtils.getBannersByType(10)
        mImgList.addAll(bannerData)
        if (!mImgList.isEmpty() && mImgList.size > 0) {
            mVersion = mImgList[0].version
            setBanner()
        } else {
//            banner_login_main.setData(R.drawable.login_icon_banner1, R.drawable.login_icon_banner2, R.drawable.login_icon_banner3)
//            (0..2).forEach { banner_login_main.getItemImageView(it).scaleType = ImageView.ScaleType.FIT_CENTER }//CENTER_INSIDE
//            banner_login_main.setPageTransformer(AlphaPageTransformer())
//            banner_login_main.setAllowUserScrollable(false)
//            banner_login_main.viewPager.setPageChangeDuration(10000)
        }
        setBGABannerLogin()
    }

    //初始化Listener
    override fun initListener() {
        super.initListener()
        //返回
        iv_login_main_back.setOnClickListener { onBackPressed() }
        //账号密码登录
        btn_login_pw_main.setOnClickListener { startActivityForResult<LoginPwdActivity>(LOGIN_BACK, LOGIN_TYPE to 0, FROM_TYPE to from) }
        //短信验证登录
        btn_login_sms_main.setOnClickListener { startActivityForResult<LoginPwdActivity>(LOGIN_BACK, LOGIN_TYPE to 1, FROM_TYPE to from) }
        //立即注册
        tv_login_sign_up_now_main.setOnClickListener { startActivityForResult<LoginRegisterActivity>(LOGIN_BACK, FROM_TYPE to from) }
        //微信登录
        tv_login_other_login_wechat_main.setOnClickListener { wxLogin() }
        //qq登录
        tv_login_other_login_qq_main.setOnClickListener { qqLogin() }

    }

    //QQ第三方登录
    private fun qqLogin() {
        mQQThreeLogin.login(this, IConstants.APP_ID_QQ) { isSuc, qqDataBean, qqUserInfoBean ->
            if (isSuc) {
                threeLogin(WxInfoBean(qqDataBean?.openid ?: "", qqUserInfoBean?.figureurlQq2
                        ?: "", qqUserInfoBean?.nickname ?: ""), qqDataBean?.accessToken ?: "", 1)
            } else {
                ToastUtil.showShort(getString(R.string.please_click))
            }
        }
    }


    //微信登陆
    private fun wxLogin() {
        mWeChatApi = WXAPIFactory.createWXAPI(this, IConstants.APPID_WECHAT)
        if (!mWeChatApi!!.isWXAppInstalled) {
            // 提醒用户没有安装微信
            ToastUtil.showShort("未检测到微信客户端")
            return
        }
        mWeChatApi!!.registerApp(IConstants.APPID_WECHAT)
        val req = SendAuth.Req()
        req.scope = "snsapi_userinfo"
        req.state = "diandi_wx_login"
        mWeChatApi!!.sendReq(req)
    }

    fun onEvent(wxBean: WxBean) {
        if (!TextUtils.isEmpty(wxBean.code)) {
            EventBus.getDefault().removeStickyEvent(wxBean)
            loginCode(wxBean.code)
        }
    }

    fun loginCode(code: String) {
        myDialog.showLoadingDialog()
        ApiUtils.getApi().getWeChatToken(IConstants.APPID_WECHAT, IConstants.SECRET_WECHAT, code)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it != null && !TextUtils.isEmpty(it.access_token)) {
                        getWxInfo(it)
                    } else {
                        myDialog.dismissLoadingDialog()
                        ToastUtil.showShort("获取用户信息失败")
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    ToastUtil.showShort("获取用户信息失败")
                }, {}, { addSubscription(it) })
    }

    private fun getWxInfo(tokenBean: WxTokenBean) {
        ApiUtils.getApi().getWeChatInfo(tokenBean.access_token, tokenBean.openid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it != null && !TextUtils.isEmpty(it.openid)) {
                        threeLogin(it, tokenBean.access_token, 2)
                    } else {
                        myDialog.dismissLoadingDialog()
                        ToastUtil.showShort("获取用户信息失败")
                    }
                }, {
                    myDialog.dismissLoadingDialog()
                    ToastUtil.showShort("获取用户信息失败")
                }, {}, { addSubscription(it) })
    }

    private fun threeLogin(infoBean: WxInfoBean, token: String, type: Int) {
        ApiUtils.getApi().loginThree(infoBean.openid, infoBean.unionid, type,1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ response ->
                    myDialog.dismissLoadingDialog()
                    when {
                        response.code == 12000 -> response.data?.let {
                            MainApplication.instance.user = it
                            MainApplication.instance.TOKEN = it.token
                            it.upDate()
                            mImService?.loginManager?.login("${it.wxUid}", it.wxToken)
                            loginOver()
                        }
                        response.code == 25093 -> infoBean.apply {
                            startActivityForResult<LoginRegisterActivity>(LOGIN_BACK, FROM_TYPE to from, TYPE to 5, THREE_TYPE to type,
                                    THREE_OPENID to openid, THREE_TOKEN to token, WE_CHAT_UNIONID to unionid,
                                    THIRD_USERNAME to nickname, AVATAR to headimgurl)
                        }
                        else -> ToastUtil.showShort(response.msg)
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

    //设置登录页面BGABanner
    private fun setBGABannerLogin() {
        ApiUtils.getApi().getBanners(mVersion, 10)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            if (it.banners.isNotEmpty()) {
                                BoxUtils.removeBanners(mImgList)
                                it.setVersion()
                                mVersion = it.version
                                mImgList = it.banners as ArrayList<Banner>
                                BoxUtils.saveBanners(mImgList)
                                setBanner()
                            }
                        }
                    } else if (bean.code != 20000) {
                        ToastUtil.showShort(bean.msg)
                    }
                }, { }, { }, { addSubscription(it) })
    }

    private fun setBanner() {
        banner_login_main.setAdapter(this)//必须设置此适配器，否则不会调用接口方法来填充图片
        banner_login_main.setDelegate(this)//设置点击事件，重写点击回调方法
        banner_login_main.setData(mImgList, null)
        banner_login_main.setPageTransformer(AlphaPageTransformer())
        banner_login_main.setAutoPlayAble(mImgList.size > 1)
        banner_login_main.setAllowUserScrollable(false)
        banner_login_main.viewPager.setPageChangeDuration(10000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mQQThreeLogin.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_BACK && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    //banner加载图片
    override fun fillBannerItem(banner: BGABanner?, itemView: ImageView, model: Banner?, position: Int) {
        model?.let {
            Glide.with(this).load(it.imgUrl).apply(RequestOptions().dontAnimate().fitCenter()).into(itemView)
            itemView.scaleType = ImageView.ScaleType.FIT_CENTER
        }
    }

    //banner点击事件
    override fun onBannerItemClick(banner: BGABanner?, itemView: ImageView?, model: Banner, position: Int) {
        if (!mImgList.isEmpty()) {
            mImgList[position].apply {
                when (skipType) {
                    2 -> startActivity<WebViewActivity>(IConstants.title to "详情", IConstants.detailUrl to url)
                    3 -> startActivity<HeadDetailsActivity>("URL" to url)
                    4 -> startActivity<ShopDetailActivity>(IConstants.BRANDID to interiorDetailsId)
                    5 -> {
                        try {
                            OpenMallApp.open(this@LoginMainActivity, exteriorUrl)
                        } catch (e: OpenMallApp.NotInstalledException) {
                            startActivity<WebViewActivity>(IConstants.title to "详情", IConstants.detailUrl to url)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        imServiceConnector.disconnect(this)
        EventBus.getDefault().unregister(this)
        super.onDestroy()
        //解除第三方微信的引用，彻底关闭此aty
        mWeChatApi?.detach()
    }
}