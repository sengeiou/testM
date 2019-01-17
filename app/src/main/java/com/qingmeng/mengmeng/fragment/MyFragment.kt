package com.qingmeng.mengmeng.fragment

import android.app.Activity
import android.content.Intent
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
    private val REQUEST_MY = 746                         //下一页返回数据的requestCode

    companion object {
        var mSettingsOrUpdate: Int = 0                   //是设置密码或修改密码   1设置 2修改
    }

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

        slMy.isRefreshing = true
        if (mSettingsOrUpdate != 0) {
            httpLoad()
        } else {
            //先请求用户校验是设置密码还是修改密码接口
            settingsOrUpdatePass()
        }
    }

    //点击事件
    override fun initListener() {
        super.initListener()

        //下拉刷新
        slMy.setOnRefreshListener {
            //如果该字段不为空 那么就直接请求信息查询
            if (mSettingsOrUpdate != 0) {
                httpLoad()
            } else {
                settingsOrUpdatePass()
            }
        }

        //头像
        ivMyHeadPortrait.setOnClickListener {

        }

        //设置
        ivMySettings.setOnClickListener {
            //跳转aty
            startActivity<MySettingsActivity>("avatar" to mMyInformation.avatar, "userName" to mMyInformation.userName)
        }

        //我的关注
        llMyMyFollow.setOnClickListener {
            startActivityForResult(Intent(context, MyMyFollowActivity::class.java).putExtra("title", tvMyMyFollow.text), REQUEST_MY)
        }

        //我的留言
        llMyMyLeavingMessage.setOnClickListener {
            startActivityForResult(Intent(context, MyMyLeavingMessageActivity::class.java), REQUEST_MY)
        }

        //我的足迹
        llMyMyFootprint.setOnClickListener {
            startActivityForResult(Intent(context, MyMyFollowActivity::class.java).putExtra("title", tvMyMyFootprint.text), REQUEST_MY)
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
            startActivity<LoginMainActivity>()
        }
    }

    //查询用户接口
    private fun httpLoad() {
        ApiUtils.getApi()
                .myInformation(TEST_ACCESS_TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    slMy.isRefreshing = false
                    it.apply {
                        if (code == 12000) {
                            llMyNoLogin.visibility = View.VISIBLE
                            tvMyLogin.visibility = View.GONE
                            //信息赋值
                            mMyInformation = data as MyInformation
                            //页面赋值
                            setData(data as MyInformation)
                        } else {
                            llMyNoLogin.visibility = View.GONE
                            tvMyLogin.visibility = View.VISIBLE
                        }
                    }
                }, {
                    slMy.isRefreshing = false
                })
    }

    //校验是设置密码还是修改密码
    private fun settingsOrUpdatePass() {
        ApiUtils.getApi()
                .mySettingsOrUpdatePass(TEST_ACCESS_TOKEN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.apply {
                        if (code == 12000) {    //修改密码
                            mSettingsOrUpdate = 2
                        } else if (code == 30001) { //设置密码
                            mSettingsOrUpdate = 1
                        }
                    }
                    //请求下一个接口
                    httpLoad()
                }, {
                    slMy.isRefreshing = false
                })
    }

    //页面内容赋值
    private fun setData(myInformation: MyInformation) {
        //头像
        GlideLoader.load(this, myInformation.avatar, ivMyHeadPortrait, cacheType = CacheType.All)
        tvMyUserName.text = myInformation.userName
        tvMyMyFollowNum.text = "${myInformation.myAttention}"
        tvMyMyLeavingMessageNum.text = "${myInformation.myComment}"
        tvMyMyFootprintNum.text = "${myInformation.myFootprint}"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MY && resultCode == Activity.RESULT_OK) {
            val isDelete = data?.getBooleanExtra("isDelete", false) ?: false
            //如果下一页删掉过数据 就刷新下本页
            if (isDelete) {
                slMy.isRefreshing = true
                if (mSettingsOrUpdate != 0) {
                    httpLoad()
                } else {
                    settingsOrUpdatePass()
                }
            }
        }
    }
}