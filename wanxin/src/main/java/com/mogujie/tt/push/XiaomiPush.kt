package com.mogujie.tt.push

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process
import android.util.Log
import com.xiaomi.channel.commonutils.logger.LoggerInterface
import com.xiaomi.mipush.sdk.Logger
import com.xiaomi.mipush.sdk.MiPushClient

/**
 * Created by wr
 * Date: 2019/1/8  16:24
 * mail: 1902065822@qq.com
 * describe:
 */
object XiaomiPush : PushBase {
    val TAG = "PushXiaomi"
    override fun register(context: Application) {
        // 注册push服务，注册成功后会向DemoMessageReceiver发送广播
        // 可以从DemoMessageReceiver的onCommandResult方法中MiPushCommandMessage对象参数中获取注册信息
        if (shouldInit(context)) {
            Log.d(TAG, "PushXiaomi init")
            MiPushClient.registerPush(context, ConfigPushKeySecret.xiaomiAppId, ConfigPushKeySecret.xiaomiAppKey)
        }
        initLog(context)
    }

    override fun unRegister(context: Application) {
        MiPushClient.unregisterPush(context)
    }

    override fun setAlias(context: Context, alias: String) {
        MiPushClient.setAlias(context, alias, null)
    }
    override fun deleteAlias(context: Context, alias: String) {
        MiPushClient.unsetAlias(context, alias, null)
    }
    override fun setTag(context: Context, tag: String) {
        MiPushClient.setUserAccount(context, tag, null)
    }

    override fun deleteTag(context: Context, tag: String) {
        MiPushClient.unsetUserAccount(context, tag, null)
    }

    override fun openNotify(context: Context) {
        MiPushClient.resumePush(context, null)
    }

    override fun closeNotify(context: Context) {
        MiPushClient.pausePush(context, null)
    }

    private fun shouldInit(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val processInfos = am?.runningAppProcesses
        val myPid = Process.myPid()
        processInfos?.forEach {
            if (it.pid == myPid && context.packageName == it.processName) {
                return true
            }
        }
        return false
    }

    private fun initLog(context: Application) {
        val newLogger = object : LoggerInterface {
            override fun setTag(tag: String) {}
            override fun log(content: String, t: Throwable) {
                Log.d(TAG, content, t)
            }
            override fun log(content: String) {
                Log.d(TAG, content)
            }
        }
        Logger.setLogger(context, newLogger)
    }
}