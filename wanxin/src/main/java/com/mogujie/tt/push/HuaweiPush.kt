package com.mogujie.tt.push

import android.app.Activity
import android.app.Application
import android.content.Context
import com.app.common.logger.Logger
import com.huawei.android.hms.agent.HMSAgent
import com.huawei.android.hms.agent.push.handler.DeleteTokenHandler
import com.huawei.android.hms.agent.push.handler.EnableReceiveNotifyMsgHandler


/**
 * Created by wr
 * Date: 2019/1/8  10:59
 * mail: 1902065822@qq.com
 * describe:
 */
object HuaweiPush : PushBase {

    override fun register(context: Application) {
        val initStatu = HMSAgent.init(context)
        Logger.d("HMSAgent init end success:$initStatu")
        getToken()
    }
    override fun unRegister(context: Application) {
        HMSAgent.destroy()
    }

    override fun setAlias(context: Context, alias: String) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteAlias(context: Context, alias: String) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setTag(context: Context, tag: String) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteTag(context: Context, tag: String) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun openNotify(context: Context) {
        HMSAgent.Push.enableReceiveNotifyMsg(true) { rst -> Logger.d("openNotify:end code=$rst") }
    }

    override fun closeNotify(context: Context) {
        HMSAgent.Push.enableReceiveNotifyMsg(false) { rst -> Logger.d("closeNotify:end code=$rst") }
    }


    fun connect(activity: Activity) {
        HMSAgent.connect(activity, { rst -> Logger.d("HMS connect end:$rst") })
    }

    /**
     * 获取token
     */
    private fun getToken() {
        HMSAgent.Push.getToken { rst -> Logger.d("get token: end$rst") }
    }

    private fun deleteToken(token:String) {
        HMSAgent.Push.deleteToken(token) { rst -> Logger.d("deleteToken:end code=$rst") }
    }
}