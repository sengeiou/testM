package com.qingmeng.mengmeng.fragment

import android.app.Activity
import android.content.Intent
import android.view.View
import com.dragger2.activitytest0718.util.SharedPreferencesHelper
import com.qingmeng.mengmeng.BaseFragment
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.activity.*
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.constant.IConstants.MY_TO_MESSAGE
import com.qingmeng.mengmeng.entity.MyInformation
import com.qingmeng.mengmeng.utils.*
import com.qingmeng.mengmeng.utils.imageLoader.CacheType
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_my.*
import org.jetbrains.anko.support.v4.startActivity

/**
 * 我的板块
 */

class MyFragment : BaseFragment() {
    private lateinit var spf: SharedPreferencesHelper
    private var mLoginSuccess = false                    //登录状态
    private var mMyInformation = MyInformation()         //个人信息bean
    private val REQUEST_MY = 746                         //下一页返回数据的requestCode

    override fun getLayoutId(): Int {
        return R.layout.fragment_my
    }

    //view初始化
    override fun initObject() {
        super.initObject()

        // 获得状态栏高度
        val statusBarHeight = getBarHeight(context!!)
        //给布局的高度重新设置一下 加上状态栏高度
        rlMyTop.layoutParams.height = rlMyTop.layoutParams.height + getBarHeight(context!!)
        ivMySettings.setMarginExt(top = statusBarHeight + context!!.dp2px(10))

        spf = SharedPreferencesHelper(context!!, "myFragment")

        //设置缓存数据
        getCacheData()
    }

    //点击事件
    override fun initListener() {
        super.initListener()

        //下拉刷新
        srlMy.setOnRefreshListener {
            httpSelect()
        }

        //头像
        ivMyHeadPortrait.setOnClickListener {
            toNextAty {
                startActivityForResult(Intent(context, MySettingsUserActivity::class.java), REQUEST_MY)
            }
        }

        //设置
        ivMySettings.setOnClickListener {
            toNextAty {
                //跳转aty
                startActivityForResult(Intent(context, MySettingsActivity::class.java).apply {
                    putExtra("avatar", mMyInformation.avatar)
                    putExtra("userName", mMyInformation.userName)
                    putExtra("phone", mMyInformation.phone)
                    putExtra("isUpdatePass", spf.getSharedPreference("isUpdatePass", false) as Boolean)
                }, REQUEST_MY)
            }
        }

        //我的关注
        llMyMyFollow.setOnClickListener {
            toNextAty {
                startActivityForResult(Intent(context, MyMyFollowActivity::class.java).putExtra("title", tvMyMyFollow.text), REQUEST_MY)
            }
        }

        //我的留言
        llMyMyLeavingMessage.setOnClickListener {
            if (mLoginSuccess) {
                startActivityForResult(Intent(context, MyMyLeavingMessageActivity::class.java), REQUEST_MY)
            } else {
                startActivityForResult(Intent(context, LoginMainActivity::class.java), IConstants.LOGIN_BACK)
            }
        }

        //我的足迹
        llMyMyFootprint.setOnClickListener {
            toNextAty {
                startActivityForResult(Intent(context, MyMyFollowActivity::class.java).putExtra("title", tvMyMyFootprint.text), REQUEST_MY)
            }
        }

        //企业入驻
        llMyEnterpriseEntry.setOnClickListener {
            startActivity<MyEnterpriseEntryActivity>()
        }

        //第三方绑定
        llMyThreeBinding.setOnClickListener {
            startActivity<MyThreeBindingActivity>()
        }

        //消息
        llMyMessage.setOnClickListener {
            startActivity<MyMessageActivity>(MY_TO_MESSAGE to true)
        }

        //登录
        tvMyLogin.setOnClickListener {
            startActivityForResult(Intent(context, LoginMainActivity::class.java), IConstants.LOGIN_BACK)
        }
    }

    //查询用户接口
    private fun httpLoad() {
        ApiUtils.getApi()
                .myInformation(MainApplication.instance.TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    srlMy.isRefreshing = false
                    it.apply {
                        if (code == 12000) {
                            llMyNoLogin.visibility = View.VISIBLE
                            tvMyLogin.visibility = View.GONE
                            //数据库删除
                            BoxUtils.removeMyInformation(mMyInformation)
                            //信息赋值
                            mMyInformation = data as MyInformation
                            //数据库保存
                            BoxUtils.saveMyInformation(mMyInformation)
                            //页面赋值
                            setData(mMyInformation)
                            mLoginSuccess = true
//                            ToastUtil.showShort("${MainApplication.instance.user.wxUid} ${MainApplication.instance.user.wxToken}")
                        } else {
                            loginFail()
                        }
                    }
                }, {
                    srlMy.isRefreshing = false
                    mLoginSuccess = (MainApplication.instance.TOKEN != "")
                    if (mLoginSuccess) {
                        tvMyLogin.visibility = View.GONE
                    } else {
                        loginFail()
                    }
                }, {}, { addSubscription(it) })
    }

    private fun httpSelect() {
//        tvMyTest.text = SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE, context!!,"[呲牙]测试咯[你好]嘿嘿[鼓掌]",tvMyTest)
        //如果该字段是修改密码 那么就直接请求信息查询
        if (spf.getSharedPreference("isUpdatePass", false) as Boolean) {
            httpLoad()
        } else {
            settingsOrUpdatePass()
        }
    }

    //校验是设置密码还是修改密码
    private fun settingsOrUpdatePass() {
        ApiUtils.getApi()
                .mySettingsOrUpdatePass(MainApplication.instance.TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.apply {
                        if (code == 12000) {    //修改密码
                            spf.put("isUpdatePass", true)
                        } else if (code == 30001) { //设置密码
                            spf.put("isUpdatePass", false)
                        } else {
                        }
                    }
                    //请求下一个接口
                    httpLoad()
                }, {
                    srlMy.isRefreshing = false
                }, {}, { addSubscription(it) })
    }

    //获取缓存数据
    private fun getCacheData() {
        Observable.create<MyInformation> {
            mMyInformation = BoxUtils.getMyInformation()!!
            it.onNext(mMyInformation)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    llMyNoLogin.visibility = View.VISIBLE
                    tvMyLogin.visibility = View.GONE
                    //页面赋值
                    setData(it)
                    //自动下拉刷新请求接口
//                    srlMy.isRefreshing = true
                    httpSelect()
                }, {
                    //                    srlMy.isRefreshing = true
                    httpSelect()
                }, {}, { addSubscription(it) })
    }

    //页面内容赋值
    private fun setData(myInformation: MyInformation) {
        //头像
        GlideLoader.load(this, myInformation.avatar, ivMyHeadPortrait, cacheType = CacheType.All, placeholder = R.drawable.default_img_icon)
        tvMyUserName.text = myInformation.userName
        tvMyMyFollowNum.text = "${myInformation.myAttention}"
        tvMyMyLeavingMessageNum.text = "${myInformation.myComment}"
        tvMyMyFootprintNum.text = "${myInformation.myFootprint}"
    }

    //该往哪个页面跳
    private fun toNextAty(toNext: () -> Unit) {
        if (mLoginSuccess) {
            toNext()
        } else {
            startActivityForResult(Intent(context, LoginMainActivity::class.java), IConstants.LOGIN_BACK)
        }
    }

    //登录失败默认ui设置
    private fun loginFail() {
        llMyNoLogin.visibility = View.GONE
        tvMyLogin.visibility = View.VISIBLE
        //设置默认名称头像等。
        tvMyUserName.text = getString(R.string.my_username)
        ivMyHeadPortrait.setImageResource(R.drawable.default_img_icon)
        tvMyMyFollowNum.text = getString(R.string.my_defaultNum)
        tvMyMyLeavingMessageNum.text = getString(R.string.my_defaultNum)
        tvMyMyFootprintNum.text = getString(R.string.my_defaultNum)
        mLoginSuccess = false
        //数据库删除
        BoxUtils.removeMyInformation(mMyInformation)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            httpSelect()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == REQUEST_MY || requestCode == IConstants.LOGIN_BACK) && resultCode == Activity.RESULT_OK) {
            val isDelete = data?.getBooleanExtra("isDelete", false) ?: false
            val mPhoneChange = data?.getBooleanExtra("mPhoneChange", false) ?: false
            //如果下一页删掉过数据 或改变过手机号 设置过密码 就刷新本页
            if (isDelete || mPhoneChange || requestCode == IConstants.LOGIN_BACK) {
//                srlMy.isRefreshing = true
                httpSelect()
            }
        }
    }
}