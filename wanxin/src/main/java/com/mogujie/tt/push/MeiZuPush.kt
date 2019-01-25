package com.mogujie.tt.push

import android.app.Application
import android.content.Context
import com.meizu.cloud.pushinternal.DebugLogger
import com.meizu.cloud.pushsdk.PushManager

/**
 * Created by wr
 * Date: 2019/1/8  20:29
 * mail: 1902065822@qq.com
 * describe:
 */
object MeiZuPush : PushBase {

    override fun register(context: Application) {
        DebugLogger.initDebugLogger(context)
        PushManager.register(context, ConfigPushKeySecret.meizuAppId, ConfigPushKeySecret.meizuAppKey)
    }

    override fun unRegister(context: Application) {
        PushManager.unRegister(context, ConfigPushKeySecret.meizuAppId, ConfigPushKeySecret.meizuAppKey)
    }

    override fun setAlias(context: Context, alias: String) {
        PushManager.subScribeAlias(context, ConfigPushKeySecret.meizuAppId, ConfigPushKeySecret.meizuAppKey, PushManager.getPushId(context), alias)
    }

    override fun deleteAlias(context: Context, alias: String) {
        PushManager.unSubScribeAlias(context, ConfigPushKeySecret.meizuAppId, ConfigPushKeySecret.meizuAppKey, PushManager.getPushId(context), alias)
    }

    override fun setTag(context: Context, tag: String) {
        PushManager.subScribeTags(context, ConfigPushKeySecret.meizuAppId, ConfigPushKeySecret.meizuAppKey, PushManager.getPushId(context), tag)
    }

    override fun deleteTag(context: Context, tag: String) {
        PushManager.unSubScribeTags(context, ConfigPushKeySecret.meizuAppId, ConfigPushKeySecret.meizuAppKey, PushManager.getPushId(context), tag)
    }

    override fun openNotify(context: Context) {
        PushManager.switchPush(context, ConfigPushKeySecret.meizuAppId, ConfigPushKeySecret.meizuAppKey, PushManager.getPushId(context), 0, true)
    }

    override fun closeNotify(context: Context) {
        PushManager.switchPush(context, ConfigPushKeySecret.meizuAppId, ConfigPushKeySecret.meizuAppKey, PushManager.getPushId(context), 0, false)
    }
}