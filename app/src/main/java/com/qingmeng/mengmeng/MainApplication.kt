package com.qingmeng.mengmeng

import AppManager
import android.annotation.SuppressLint
import android.app.Application
import android.text.TextUtils
import android.util.Log
import com.qingmeng.mengmeng.entity.MyObjectBox
import com.qingmeng.mengmeng.entity.UserBean
import com.qingmeng.mengmeng.utils.SharedSingleton
import com.tencent.bugly.crashreport.CrashReport
import io.objectbox.BoxStore
import io.objectbox.android.AndroidObjectBrowser
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
    private lateinit var sharedSingleton: SharedSingleton

    init {
        AppManager.instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        sharedSingleton = SharedSingleton.instance
        user = UserBean.fromString()
        TOKEN = user.token
        initBox()
        initBugly()
    }

    private fun initBox() {
        boxStore = MyObjectBox.builder().androidContext(this).build()
        if (BuildConfig.DEBUG) {
            boxStore.let {
                //可以理解为初始化连接浏览器(可以在浏览器中查看数据，下面再说)
                val started = AndroidObjectBrowser(boxStore).start(this)
                Log.i("ObjectBrowser", "Started: " + started)
            }
        }
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
        lateinit var boxStore: BoxStore
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