package com.mogujie.tt.push

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import com.app.common.logger.Logger
import com.app.common.rxbus2.RxBus
import com.app.common.save.Preference
import com.app.common.view.ToastX
import com.huawei.hms.support.api.push.PushReceiver
import com.mogujie.tt.push.util.OsUtil
import com.mogujie.tt.push.util.PushTokenEvent
import java.nio.charset.StandardCharsets

/**
 * 应用需要创建一个子类继承com.huawei.hms.support.api.push.PushReceiver，
 * 实现onToken，onPushState ，onPushMsg，onEvent，这几个抽象方法，用来接收token返回，push连接状态，透传消息和通知栏点击事件处理。
 * onToken 调用getToken方法后，获取服务端返回的token结果，返回token以及belongId
 * onPushState 调用getPushState方法后，获取push连接状态的查询结果
 * onPushMsg 推送消息下来时会自动回调onPushMsg方法实现应用透传消息处理。本接口必须被实现。 在开发者网站上发送push消息分为通知和透传消息
 *           通知为直接在通知栏收到通知，通过点击可以打开网页，应用 或者富媒体，不会收到onPushMsg消息
 *           透传消息不会展示在通知栏，应用会收到onPushMsg
 * onEvent 该方法会在设置标签、点击打开通知栏消息、点击通知栏上的按钮之后被调用。由业务决定是否调用该函数。
 */
class HuaweiPushReceiver : PushReceiver() {

    override fun onToken(context: Context?, token: String?, extras: Bundle?) {
        Logger.d("token:$token#extras:${extras.toString()}")
        context?.let {
            var tokenDB by Preference(context, "token", "")
            tokenDB = token ?: ""
        }


    }

    override fun onPushMsg(context: Context?, msgBytes: ByteArray?, extras: Bundle?): Boolean {
        msgBytes?.let {
            val content = String(msgBytes, StandardCharsets.UTF_8)
            Logger.d("收到PUSH透传消息,消息内容为:$content")

            context?.let {
                ToastX(context).info(content)
            }

        }
        return super.onPushMsg(context, msgBytes, extras)
    }

    override fun onEvent(context: Context?, event: Event?, extras: Bundle?) {
        if (PushReceiver.Event.NOTIFICATION_OPENED == event || PushReceiver.Event.NOTIFICATION_CLICK_BTN == event) {
            val notifyId = extras?.getInt(PushReceiver.BOUND_KEY.pushNotifyId, 0)
            Logger.d("收到通知栏消息点击事件,notifyId:$notifyId")
            if (0 != notifyId && notifyId != null) {
                val manager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                manager?.cancel(notifyId)
            }
        }
        val message = extras?.getString(PushReceiver.BOUND_KEY.pushMsgKey)
        Logger.d("收到,pushMsg:$message")
        super.onEvent(context, event, extras)
    }

    override fun onPushState(context: Context?, pushState: Boolean) {

    }
}