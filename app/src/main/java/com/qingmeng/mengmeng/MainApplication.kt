package com.qingmeng.mengmeng

import AppManager
import android.annotation.SuppressLint
import android.app.Application
import android.text.TextUtils
import com.qingmeng.mengmeng.entity.UserBean
import com.qingmeng.mengmeng.utils.SharedSingleton
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

/**
 * Created by zq on 2018/8/6
 */
@SuppressLint("CheckResult")
class MainApplication : Application() {
    var TOKEN: String = ""
    lateinit var user: UserBean
    lateinit var mWxApi :IWXAPI
    private lateinit var sharedSingleton: SharedSingleton

    init {
        AppManager.instance
    }
    /*
* 注册到微信
*/
    private fun registToWX()
    {
          mWxApi = WXAPIFactory.createWXAPI(this, "APP_ID", false);//此处的APP_ID替换为你在微信开放平台上申请到的APP_ID
        mWxApi.registerApp("APP_ID");
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
        sharedSingleton = SharedSingleton.instance
        user = UserBean.fromString()
        TOKEN = user.token
        initBugly()
        registToWX();

    }

    private fun initBugly() {
        val context = applicationContext
        // 获取当前包名
        val packageName = context.packageName
        // 获取当前进程名
        val processName = getProcessName(android.os.Process.myPid())
        // 设置是否为上报进程
        val strategy = CrashReport.UserStrategy(context)
        strategy.isUploadProcess = processName == null || processName == packageName
        strategy.isBuglyLogUpload = true
        CrashReport.initCrashReport(context, BuildConfig.BUGLY_APPID, true, strategy)
        CrashReport.setIsDevelopmentDevice(context, BuildConfig.DEBUG)
    }

    companion object {
        lateinit var instance: MainApplication
    }

    private fun getProcessName(pid: Int): String? {
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(FileReader("/proc/$pid/cmdline"))
            var processName = reader.readLine()
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim { it <= ' ' }
            }
            return processName
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        } finally {
            try {
                reader?.close()
            } catch (exception: IOException) {
                exception.printStackTrace()
            }

        }
        return null
    }
}