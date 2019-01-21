package com.qingmeng.mengmeng.activity

import android.content.Intent
import android.util.Log
import android.widget.ImageView
import cn.bingoogolapple.bgabanner.BGABanner
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.entity.Banner
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.BoxUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.tencent.connect.common.Constants
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_log_main_login.*
import org.jetbrains.anko.startActivity
import org.json.JSONException
import org.json.JSONObject


/**
 * Created by mingyue
 * Date: 2019/1/11
 * mail: 153705849@qq.com
 * describe: 登录页面首页
 */
class LoginMainActivity : BaseActivity(), BGABanner.Delegate<ImageView, Banner>, BGABanner.Adapter<ImageView, Banner> {
    //banner加载图片
    override fun fillBannerItem(banner: BGABanner?, itemView: ImageView, model: Banner?, position: Int) {
        model?.let {
            Glide.with(this).load(it.imgUrl).apply(RequestOptions()
                    .placeholder(R.drawable.image_holder).error(R.drawable.image_holder)
                    .centerCrop()).into(itemView)
        }
    }

    lateinit var openid: String

    //banner点击事件
    override fun onBannerItemClick(banner: BGABanner?, itemView: ImageView?, model: Banner, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val mImgList = ArrayList<Banner>()
    override fun getLayoutId(): Int {
        //设置标题
        //     setHeadName(getString(R.string.login))
        return R.layout.activity_log_main_login
    }

    override fun initData() {
        val bannerData = BoxUtils.getBannersByType(5)
        mImgList.addAll(bannerData)
        if (!mImgList.isEmpty()){
            setBGABannerLogin(mImgList[0].version)
        }else setBGABannerLogin("")


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
        //返回
        iv_login_main_back.setOnClickListener { this.finish() }
        //账号密码登录
        btn_login_pw_main.setOnClickListener { startActivity<LoginpwActivity>() }
        //短信验证登录
        btn_login_sms_main.setOnClickListener { startActivity<LogincodeActivity>() }
        //立即注册
        tv_login_sign_up_now_main.setOnClickListener { startActivity<LoginRegisterActivity>() }
        //微信登录
        tv_login_other_login_wechat_main.setOnClickListener {
            wxLogin()
        }
        //qq登录
        tv_login_other_login_qq_main.setOnClickListener {
            qqLogin()
        }

    }

    //QQ第三方登录
    fun qqLogin() {
        var mTencent: Tencent = Tencent.createInstance("123123123", getApplicationContext()) //将123123123改为自己的AppID
        mTencent.login(this@LoginMainActivity, "all", BaseUiListener());
        openid = mTencent.openId
    }


    //微信登陆
    fun wxLogin() {
        if (!MainApplication().mWxApi.isWXAppInstalled()) {
            ToastUtil.showShort("您还未安装微信客户端")
            return
        }
        val req = SendAuth.Req()
        req.scope = "snsapi_userinfo"
        req.state = "diandi_wx_login"
        MainApplication().mWxApi.sendReq(req)

    }


    //设置登录页面BGAbanner
    fun setBGABannerLogin(version:String) {
        ApiUtils.getApi().getbanner(version, 5)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            if (!it.banners.isEmpty()) {
                                if (!mImgList.isEmpty()) {
                                    BoxUtils.removeBanners(mImgList)
                                    mImgList.clear()
                                }
                                it.setVersion()
                                mImgList.addAll(it.banners)
                                BoxUtils.saveBanners(mImgList)
                                setBanner()
                            }
                        }
                    } else {
                        ToastUtil.showShort(bean.msg)
                    }
                })
    }

    private fun setBanner() {
        banner_login_main.setAdapter(this)//必须设置此适配器，否则不会调用接口方法来填充图片
        banner_login_main.setDelegate(this)//设置点击事件，重写点击回调方法
        banner_login_main.setData(mImgList, null)
        if (mImgList.size > 1) {
            banner_login_main.setAutoPlayAble(true)
        } else {
            banner_login_main.setAutoPlayAble(false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Tencent.onActivityResultData(requestCode, resultCode, data, BaseUiListener());

        if (requestCode == Constants.REQUEST_API) {
            if (resultCode == Constants.REQUEST_LOGIN) {
                Tencent.handleResultData(data, BaseUiListener());
            }

        }
    }
}

private class BaseUiListener : IUiListener, BaseActivity() {
    override fun getLayoutId(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var openidString: String = ""
    private val mTencent: Tencent? = null
    private var access_token: String? = null
    override fun onComplete(response: Any) {
        try {
            //获得的数据是JSON格式的，获得你想获得的内容
            //如果你不知道你能获得什么，看一下下面的LOG
            Log.v("----TAG--", "-------------" + response.toString())
            openidString = (response as JSONObject).getString("openid")
            mTencent!!.openId = openidString
            mTencent.setAccessToken(response.getString("access_token"), response.getString("expires_in"))


            Log.v("TAG", "-------------" + openidString!!)
            access_token = response.getString("access_token")
            ApiUtils.getApi().thirdlogin(openidString, 1)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ bean ->
                        if (bean.code == 12000) {
                            bean.data?.let {
                                MainApplication.instance.user = it
                                MainApplication.instance.TOKEN = it.token
                                it.upDate()
                            }
                            if (bean.code == 25093) {
                                ToastUtil.showShort(bean.msg)
                                startActivity<LoginBindingPhoneActivity>()
                            }
                        } else {
                            ToastUtil.showShort(bean.msg)
                        }
                    })
            //expires_in = ((JSONObject) response).getString("expires_in");
        } catch (e: JSONException) {
            // TODO Auto-generated catch block
            e.printStackTrace()


        }

    }


    override fun onError(uiError: UiError) {

    }

    override fun onCancel() {

    }

}