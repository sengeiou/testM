package com.qingmeng.mengmeng.activity

import AppManager
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TabWidget
import android.widget.TextView
import com.app.common.extensions.getAndroidID
import com.baidu.location.BDLocation
import com.baidu.location.BDLocationListener
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.mogujie.tt.config.UrlConstant
import com.mogujie.tt.db.sp.SystemConfigSp
import com.mogujie.tt.imservice.event.LoginEvent
import com.mogujie.tt.imservice.event.ReconnectEvent
import com.mogujie.tt.imservice.event.SocketEvent
import com.mogujie.tt.imservice.service.IMService
import com.mogujie.tt.imservice.support.IMServiceConnector
import com.mogujie.tt.utils.IMUIHelper
import com.mogujie.tt.utils.NetworkUtil
import com.qingmeng.mengmeng.BaseActivity
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.base.MainTab
import com.qingmeng.mengmeng.constant.IConstants
import com.qingmeng.mengmeng.entity.AccountEvent
import com.qingmeng.mengmeng.entity.MainTabBean
import com.qingmeng.mengmeng.entity.ProgressBean
import com.qingmeng.mengmeng.entity.UserBean
import com.qingmeng.mengmeng.service.UpdateService
import com.qingmeng.mengmeng.utils.ApiUtils
import com.qingmeng.mengmeng.utils.BadgeUtil
import com.qingmeng.mengmeng.utils.PermissionUtils
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.view.dialog.DialogCommon
import com.qingmeng.mengmeng.view.dialog.DialogCustom
import de.greenrobot.event.EventBus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask

@Suppress("DEPRECATION")
@SuppressLint("CheckResult")
class MainActivity : BaseActivity() {
    private var firstTime = 0L
    private var mMyLocationListener: MyLocationListener? = null
    private var mLocationClient: LocationClient? = null
    private var updateDialog: DialogCustom.UpdateDialog? = null

    //完信相关
    private var mImService: IMService? = null
    private val imServiceConnector = object : IMServiceConnector() {
        override fun onServiceDisconnected() {}

        override fun onIMServiceConnected() {
            IMServiceConnector.logger.d("MainActivity#onIMServiceConnected")
            mImService = this.imService
            if (mImService == null) {
                //why ,some reason
                return
            }
            //自动登录完信
            if (MainApplication.instance.user.wxUid != 0 && !TextUtils.isEmpty(MainApplication.instance.user.wxToken)) {
                mImService?.loginManager?.login("${MainApplication.instance.user.wxUid}", MainApplication.instance.user.wxToken)
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_main

    @SuppressLint("ObsoleteSdkInt")
    override fun initObject() {
        //设置状态栏隐藏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        tabhost.setup(this, supportFragmentManager, R.id.realtabcontent)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            tabhost.tabWidget.showDividers = TabWidget.SHOW_DIVIDER_NONE
        }
        initTabs()
        setShowBack(false)
        PermissionUtils.location(this) { initLocation() }
        //完信相关
        SystemConfigSp.instance().init(applicationContext)
        if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
            SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER, UrlConstant.ACCESS_MSG_ADDRESS)
        }
        imServiceConnector.connect(this)
    }

    override fun initData() {
        if (TextUtils.isEmpty(getAndroidID())) {
            return
        }
        var info: PackageInfo? = null
        try {
            info = packageManager.getPackageInfo(this.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val versionCode = info!!.versionCode
        ApiUtils.getApi().getVersionInfo(getAndroidID(), "$versionCode")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    if (bean.code == 12000) {
                        bean.data?.let {
                            myDialog.showVersionUpdateDialog(it) { dialog, view ->
                                PermissionUtils.readAndWrite(this) {
                                    startService(Intent(this@MainActivity, UpdateService::class.java).putExtra("downloadPath", it.link))
                                    if (it.forceUpdate == 0) {
                                        dialog.cancel()
                                    } else {
                                        view.isClickable = false
                                        updateDialog = dialog
                                        dialog.showProgress()
                                    }
                                }
                            }
                        }
                    }
                }, {
                }, {}, { addSubscription(it) })
    }

    private fun initLocation() {
        try {
            mLocationClient = LocationClient(this)
            mMyLocationListener = MyLocationListener()
            mLocationClient?.registerLocationListener(mMyLocationListener)
            val option = LocationClientOption()
            option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy// 设置定位模式
            option.setCoorType("bd09ll")// 返回的定位结果是百度经纬度，默认值gcj02
            option.setScanSpan(10000)
            option.setIsNeedAddress(true)// 返回的定位结果包含地址信息
            mLocationClient?.locOption = option
            mLocationClient?.start()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun initTabs() {
        val tabs = MainTab.values()
        val size = tabs.size
        for (i in 0 until size) {
            val mainTab = tabs[i]
            val tab = tabhost.newTabSpec(getString(mainTab.resName))
            val indicator = layoutInflater.inflate(R.layout.tab_indicator, null)
            val icon: ImageView = indicator.findViewById(R.id.tab_icon)
            icon.setImageResource(mainTab.resIcon)
            val title: TextView = indicator.findViewById(R.id.tab_titile)
            title.text = getString(mainTab.resName)
            tab.setIndicator(indicator)
            tab.setContent { View(this) }
            tabhost.addTab(tab, mainTab.clz, null)
        }
    }

    fun onEvent(mainTabBean: MainTabBean) {
        tabhost.tabWidget.getChildTabViewAt(mainTabBean.tabIndex).performClick()
    }

    fun onEvent(progressBean: ProgressBean) {
        updateDialog?.setProgress(progressBean.progress)
    }

    /**
     * EventBus事件/完信掉线逻辑
     */
    fun onEventMainThread(loginEvent: LoginEvent) {
        when (loginEvent) {
            LoginEvent.LOCAL_LOGIN_SUCCESS, LoginEvent.LOGIN_OK -> { //登录成功
                Log.i("yang", "=====================登录成功")
                (AppManager.instance.currentActivity() as BaseActivity).let {
                    it.myDialog.dismissLoadingDialog()
                }
            }
            LoginEvent.LOGIN_AUTH_FAILED, LoginEvent.LOGIN_INNER_FAILED -> { //登录失败
                Log.i("yang", "=====================登录失败")
                (AppManager.instance.currentActivity() as BaseActivity).let {
                    it.myDialog.dismissLoadingDialog()
                }
                onLoginFailure(loginEvent)
            }
            LoginEvent.LOGINING -> {    //刷新数据
//                myDialog.showLoadingDialog()
            }
            LoginEvent.LOCAL_LOGIN_MSG_SERVICE -> {
            }
            LoginEvent.PC_ONLINE -> { //pc在线
                onPCLoginStatusNotify(true)
            }
            LoginEvent.PC_OFFLINE -> {  //pc下线
            }
            LoginEvent.KICK_PC_SUCCESS -> { //踢pc成功
                onPCLoginStatusNotify(false)
            }
            LoginEvent.KICK_PC_FAILED -> { //踢pc失败
                ToastUtil.showShort(getString(R.string.kick_pc_failed))
            }
            else -> {
                (AppManager.instance.currentActivity() as BaseActivity).let {
                    it.myDialog.dismissLoadingDialog()
                }
            }
        }
    }

    fun onEventMainThread(socketEvent: SocketEvent) {
        when (socketEvent) {
            SocketEvent.MSG_SERVER_DISCONNECTED -> handleServerDisconnected()

            SocketEvent.CONNECT_MSG_SERVER_FAILED -> {
            }
            SocketEvent.REQ_MSG_SERVER_ADDRS_FAILED -> {
                handleServerDisconnected()
                onSocketFailure(socketEvent)
            }
        }
    }

    fun onEventMainThread(reconnectEvent: ReconnectEvent) {
        when (reconnectEvent) {
            ReconnectEvent.DISABLE -> handleServerDisconnected()
        }
    }

    /**
     * 登录失败
     */
    private fun onLoginFailure(event: LoginEvent) {
        when (event) {
            LoginEvent.LOGIN_AUTH_FAILED -> {   //账号或密码错误
                //清除当前缓存账号
                mImService?.loginManager?.logOut()
                MainApplication.instance.user = UserBean()
                MainApplication.instance.TOKEN = ""
                sharedSingleton.setString(IConstants.USER)
                ToastUtil.showShort("自动登录失败，请重新登录哦")
            }
            LoginEvent.LOGIN_INNER_FAILED -> {  //网络延缓
                //不管它 会自动重登的
            }
            else -> { //
//                val errorTip = getString(IMUIHelper.getLoginErrorTip(event))
//                ToastUtil.showShort(errorTip)
            }
        }
    }

    /**
     * socket失败
     */
    private fun onSocketFailure(event: SocketEvent) {
        if (IMUIHelper.getSocketErrorTip(event) != -1) {
            val errorTip = getString(IMUIHelper.getSocketErrorTip(event))
            ToastUtil.showShort(errorTip)
        }
    }

    /**
     * 多端，PC端在线状态通知
     */
    private fun onPCLoginStatusNotify(isOnline: Boolean) {
        if (isOnline) { //pc在线
//            //添加踢出事件
//            **.setOnClickListener{
//                mImService?.loginManager?.reqKickPCClient()
//            }
            ToastUtil.showShort("pc已上线")
        } else {    //踢出pc后处理。。。
            ToastUtil.showShort("pc已下线")
        }
    }

    /**
     * 下线提示
     */
    private fun handleServerDisconnected() {
        //重连、断线、被其他移动端挤掉
        if (mImService != null) {
            if (mImService?.loginManager?.isKickout == true) {  //他人登录
                (AppManager.instance.currentActivity() as BaseActivity).let {
                    if (!it.mDialogCommon.isShowing) {
                        it.mDialogCommon.show()
                    }
                    it.mDialogCommon.setOnCallBack(object : DialogCommon.CallBack {
                        override fun onLeftClick(view: View) {
                            logOut()
                        }

                        override fun onRightClick(view: View) {
                            if (NetworkUtil.isNetWorkAvalible(this@MainActivity)) {
                                it.myDialog.showLoadingDialog()
                                val userName = if (TextUtils.isEmpty(sharedSingleton.getString(IConstants.login_name))) {
                                    sharedSingleton.getString(IConstants.login_pwd)
                                } else {
                                    sharedSingleton.getString(IConstants.login_name)
                                }
                                val userPass = sharedSingleton.getString(IConstants.login_pwd)
                                //取本地的账号密码登录，找不到直接跳登录页面
                                if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userPass)) {
                                    loginMengmeng(userName, userPass)
                                } else {
                                    toLoginAty()
                                }
//                                IMLoginManager.instance().relogin()
//                                //登录完信
//                                mImService?.loginManager?.login("${MainApplication.instance.user.wxUid}", MainApplication.instance.user.wxToken)
                            } else {
                                deleteUser()
                                ToastUtil.showShort("网络连接不可用")
                            }
                        }
                    })
                }
            } else {  //其他

            }
        }
    }

    //登录盟盟
    private fun loginMengmeng(username: String, password: String) {
        ApiUtils.getApi()
                .accountLogin(username, password, 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ bean ->
                    when (bean.code) {
                        12000 -> bean.data?.let {
                            //登录成功
                            MainApplication.instance.user = it
                            MainApplication.instance.TOKEN = it.token
                            sharedSingleton.setString(IConstants.login_name, username)
                            sharedSingleton.setString(IConstants.login_pwd, password)
                            sharedSingleton.setInt(IConstants.wx_id, it.wxUid)
                            it.upDate()
                            //还要登录完信..
                            mImService?.loginManager?.login("${it.wxUid}", it.wxToken)
                            EventBus.getDefault().postSticky(AccountEvent(true))
                        }
                        else -> {  //TOKEN过期或失败
                            //跳登录页面
                            toLoginAty()
                        }
                    }
                }, {
                    toLoginAty()
                }, {}, { addSubscription(it) })
    }

    //清空本地信息
    private fun deleteUser() {
        mImService?.loginManager?.logOut()
        MainApplication.instance.user = UserBean()
        MainApplication.instance.TOKEN = ""
        sharedSingleton.setString(IConstants.USER)
        //清空桌面角标
        BadgeUtil.setBadgeCount(this, 0)
    }

    //跳登录页面
    private fun toLoginAty() {
        (AppManager.instance.currentActivity() as BaseActivity).let {
            it.myDialog.dismissLoadingDialog()
        }
        deleteUser()
        startActivity(Intent(this, LoginMainActivity::class.java))
        ToastUtil.showShort("自动登录失败，请手动登录")
    }

    //退出登录
    private fun logOut() {
        deleteUser()
        when (AppManager.instance.currentActivity()) {
            is RedShopSeach, is RedShopSeachResult, is ShopDetailActivity, is VideoDetailActivity, is WebViewActivity, is HeadDetailsActivity, is MyEnterpriseEntryActivity -> {    //如果当前页面在 搜索页、搜索结果页、品牌详情页、图片预览页、3个webView页面，退出时就停留在当前页面,不做处理

            }
            else -> {
                startActivity(intentFor<MainActivity>().newTask().clearTask())
            }
        }
    }

    override fun onRestart() {
        super.onRestart()

        //点开应用就清空桌面角标
//        ShortcutBadger.applyCount(this, 0)
        BadgeUtil.setBadgeCount(this, 0)
    }

    override fun onBackPressed() {
        val secondTime = System.currentTimeMillis()
        if (secondTime - firstTime > 2000) {//如果两次按键时间间隔大于2秒，则不退出
            ToastUtil.showShort(getString(R.string.exit_app))
            firstTime = secondTime//更新firstTime
        } else {//两次按键小于2秒时，退出应用
            AppManager.instance.appExit(this)
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        imServiceConnector.disconnect(this)
        super.onDestroy()
    }

    /**
     * 实现定位回调监听
     */
    private inner class MyLocationListener : BDLocationListener {
        /**
         * 在欢迎页获取城市 先获取城市列表 定位获取城市 无该城市 默认为上海 作为默认城市，加载数据 下一次登录以默认城市加载数据
         * 定位的数据不一样提示用户切换城市
         */
        override fun onReceiveLocation(location: BDLocation?) {
            mLocationClient?.unRegisterLocationListener(mMyLocationListener)
            mLocationClient?.stop()
            mLocationClient = null
            location?.apply {
                if (!TextUtils.isEmpty(city) && !TextUtils.isEmpty(getAndroidID())) {
                    addLocation("$latitude", "$longitude", getAndroidID())
                }
            }
        }
    }

    private fun addLocation(latitude: String, longitude: String, uuid: String) {
        ApiUtils.getApi().addLocation(MainApplication.instance.TOKEN, latitude, longitude, uuid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({}, {}, {}, { addSubscription(it) })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}