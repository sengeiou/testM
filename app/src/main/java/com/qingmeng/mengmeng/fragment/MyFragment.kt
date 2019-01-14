package com.qingmeng.mengmeng.fragment

import android.view.View
import com.qingmeng.mengmeng.BaseFragment
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.activity.*
import com.qingmeng.mengmeng.constant.IConstants.TEST_ACCESS_TOKEN
import com.qingmeng.mengmeng.entity.MyInformation
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.dp2px
import com.qingmeng.mengmeng.utils.getBarHeight
import com.qingmeng.mengmeng.utils.imageLoader.CacheType
import com.qingmeng.mengmeng.utils.imageLoader.GlideLoader
import com.qingmeng.mengmeng.utils.setMarginExt
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_my.*
import org.jetbrains.anko.support.v4.startActivity

/**
 * 我的板块
 */

class MyFragment : BaseFragment() {
    private lateinit var mMyInformation: MyInformation   //个人信息bean

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
        ivMySettings.setMarginExt(top = statusBarHeight + context!!.dp2px(15))

        swlMy.isRefreshing = true
        //用户信息查询
        httpLoad()
    }

    //点击事件
    override fun initListener() {
        super.initListener()

        //下拉刷新
        swlMy.setOnRefreshListener {
            httpLoad()
        }

        //头像
        ivMyHeadPortrait.setOnClickListener {

        }

        //设置
        ivMySettings.setOnClickListener {
            //跳转aty
            startActivity<MySettingsActivity>("avatar" to mMyInformation?.avatar, "userName" to mMyInformation?.userName)
        }

        //我的关注
        llMyMyFollow.setOnClickListener {
            startActivity<MyMyFollowActivity>("title" to tvMyMyFootprint.text as String)
        }

        //我的留言
        llMyMyLeavingMessage.setOnClickListener {
            startActivity<MyMyLeavingMessageActivity>()
        }

        //我的足迹
        llMyMyFootprint.setOnClickListener {
            startActivity<MyMyFollowActivity>("title" to tvMyMyFootprint.text as String)
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
            startActivity<MyMessageActivity>()
        }

        //登录
        tvMyLogin.setOnClickListener {
            startActivity<LogLoginMainActivity>()
        }
    }

    //查询用户接口
    private fun httpLoad() {
        ApiUtils.getApi()
                .myInformation(TEST_ACCESS_TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    swlMy.isRefreshing = false
                    if (it.code == 12000) {
                        llMyNoLogin.visibility = View.VISIBLE
                        tvMyLogin.visibility = View.GONE
                        //信息赋值
                        mMyInformation = it.data as MyInformation
                        //页面赋值
                        setData(it.data as MyInformation)
                    } else {
                        llMyNoLogin.visibility = View.GONE
                        tvMyLogin.visibility = View.VISIBLE
                    }
                }, {
                    swlMy.isRefreshing = false
                    llMyNoLogin.visibility = View.GONE
                    tvMyLogin.visibility = View.VISIBLE
                })
    }

    //页面内容赋值
    private fun setData(myInformation: MyInformation) {
        //头像
        GlideLoader.load(this, myInformation.avatar, ivMyHeadPortrait, cacheType = CacheType.All, placeholder = R.mipmap.ic_launcher)
        tvMyUserName.text = myInformation.userName
        tvMyMyFollowNum.text = "${myInformation.myAttention}"
        tvMyMyLeavingMessageNum.text = "${myInformation.myComment}"
        tvMyMyFootprintNum.text = "${myInformation.myFootprint}"
    }
}