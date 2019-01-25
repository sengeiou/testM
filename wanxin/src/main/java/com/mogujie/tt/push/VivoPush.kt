package com.mogujie.tt.push

import android.app.Application
import android.content.Context
import com.app.common.logger.Logger
import com.leimo.wanxin.BuildConfig
import com.mogujie.tools.SysInfo
import com.vivo.push.PushClient


/**
 * Created by wr
 * Date: 2019/1/8  10:59
 * mail: 1902065822@qq.com
 * describe:
 */
object VivoPush : PushBase {

    override fun register(context: Application) {
        PushClient.getInstance(context).initialize()
        PushClient.getInstance(context).turnOnPush { state ->
            Logger.d("vivo:${if (state == 0) "开启推送成功#regId="+PushClient.getInstance(context).regId else "开启推送异常[$state]"}")
        }
        if (BuildConfig.DEBUG) {
            Logger.d("vivo:imei=${SysInfo().getImie(context)}")
        }
    }

    override fun unRegister(context: Application) {
        PushClient.getInstance(context).turnOffPush { state ->
            Logger.d("vivo:${if (state == 0) "关闭推送成功" else "关闭推送异常[$state]"}")
        }
    }

    override fun setAlias(context: Context, alias: String) {
        PushClient.getInstance(context).bindAlias(alias) { state ->
            Logger.d("vivo:${if (state == 0) "设置别名成功" else "设置别名异常[$state]"}")
        }
    }

    override fun deleteAlias(context: Context, alias: String) {
        PushClient.getInstance(context).unBindAlias(alias) { state ->
            Logger.d("vivo:${if (state == 0) "取消别名成功" else "取消别名异常[$state]"}")
        }
    }

    //设置标签
    override fun setTag(context: Context, tag: String) {
        PushClient.getInstance(context).setTopic(tag) { state ->
            Logger.d("vivo:${if (state == 0) "设置标签成功" else "设置标签异常[$state]"}")
        }
    }

    //删除标签
    override fun deleteTag(context: Context, tag: String) {
        PushClient.getInstance(context).delTopic(tag) { state ->
            Logger.d("vivo:${if (state == 0) "删除标签成功" else "删除标签异常[$state]"}")
        }
    }

    override fun openNotify(context: Context) {
        PushClient.getInstance(context).turnOnPush { state ->
            Logger.d("vivo:${if (state == 0) "打开push成功" else "打开push异常[$state]"}")
        }
    }

    override fun closeNotify(context: Context) {
        PushClient.getInstance(context).turnOffPush { state ->
            Logger.d("vivo:${if (state == 0) "关闭push成功" else "关闭push异常[$state]"}")
        }
    }
}