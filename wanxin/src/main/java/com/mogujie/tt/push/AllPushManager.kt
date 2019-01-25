package com.mogujie.tt.push

import android.app.Application
import android.content.Context
import com.mogujie.tt.push.util.OsUtil

/**
 * Created by wr
 * Date: 2019/1/8  18:40
 * mail: 1902065822@qq.com
 * describe:
 */
interface PushBase {
    fun register(context: Application)
    fun unRegister(context: Application)
    fun setAlias(context: Context, alias: String)
    fun deleteAlias(context: Context, alias: String)
    fun setTag(context: Context, tag: String)
    fun deleteTag(context: Context, tag: String)
    fun openNotify(context: Context)
    fun closeNotify(context: Context)
}

object PushAllManager {
    var push: PushBase = when {
        OsUtil.isEmui -> HuaweiPush
        OsUtil.isMiui -> XiaomiPush
        OsUtil.isFlyme -> MeiZuPush
        OsUtil.isVivo -> VivoPush
        else -> NullPush
    }
}