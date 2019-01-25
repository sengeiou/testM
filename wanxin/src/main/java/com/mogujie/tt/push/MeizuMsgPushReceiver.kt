package com.mogujie.tt.push

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.meizu.cloud.pushinternal.DebugLogger
import com.meizu.cloud.pushsdk.MzPushMessageReceiver
import com.meizu.cloud.pushsdk.handler.MzPushMessage
import com.meizu.cloud.pushsdk.notification.PushNotificationBuilder
import com.meizu.cloud.pushsdk.platform.message.*
import de.greenrobot.event.EventBus


/**
 * Created by wr
 * Date: 2019/1/8  20:01
 * mail: 1902065822@qq.com
 * describe:
 */
class MeizuMsgPushReceiver : MzPushMessageReceiver() {

    @Deprecated("")
    override fun onRegister(context: Context, s: String) {
        DebugLogger.i(TAG, "onRegister pushID $s")
        print(context, "receive pushID $s")
    }

    override fun onMessage(context: Context, s: String) {
        DebugLogger.i(TAG, "onMessage $s")
        //print(context,context.getPackageName() + " receive message " + s);
//        EventBus.getDefault().post(ThroughMessageEvent(s))
    }

    override fun onMessage(context: Context, intent: Intent) {
        Log.i(TAG, "flyme3 onMessage ")
        val content = intent.getExtras().toString()
        print(context, "flyme3 onMessage $content")
    }

    override fun onMessage(context: Context, message: String, platformExtra: String) {
        Log.i(TAG, "onMessage $message platformExtra $platformExtra")
        //print(context,context.getPackageName() + " receive message " + s);
//        EventBus.getDefault().post(ThroughMessageEvent(message + platformExtra))


        // 测试直达服务
        //        try {
        //            JSONObject all = new JSONObject(message);
        //            String notificationMessage = all.getString("push");
        //            DebugLogger.e(TAG,"notificationMessage "+notificationMessage);
        //            PlatformMessageSender.showQuickNotification(context,notificationMessage,platformExtra);
        //        } catch (JSONException e) {
        //            e.printStackTrace();
        //        }
    }

    @Deprecated("")
    override fun onUnRegister(context: Context, b: Boolean) {
        DebugLogger.i(TAG, "onUnRegister $b")
        print(context, context.getPackageName() + " onUnRegister " + b)
    }

    override fun onPushStatus(context: Context, pushSwitchStatus: PushSwitchStatus) {
        EventBus.getDefault().post(pushSwitchStatus)
    }

    override fun onRegisterStatus(context: Context, registerStatus: RegisterStatus) {
        DebugLogger.i(TAG, "onRegisterStatus " + registerStatus + " " + context.getPackageName())
        //print(this," onRegisterStatus " + registerStatus);
        EventBus.getDefault().post(registerStatus)
    }

    override fun onUnRegisterStatus(context: Context, unRegisterStatus: UnRegisterStatus) {
        DebugLogger.i(TAG, "onUnRegisterStatus " + unRegisterStatus + " " + context.getPackageName())
        EventBus.getDefault().post(unRegisterStatus)
    }

    override fun onSubTagsStatus(context: Context, subTagsStatus: SubTagsStatus) {
        DebugLogger.i(TAG, "onSubTagsStatus " + subTagsStatus + " " + context.getPackageName())
        EventBus.getDefault().post(subTagsStatus)
    }

    override fun onSubAliasStatus(context: Context, subAliasStatus: SubAliasStatus) {
        DebugLogger.i(TAG, "onSubAliasStatus " + subAliasStatus + " " + context.getPackageName())
        EventBus.getDefault().post(subAliasStatus)
    }

    override fun onUpdateNotificationBuilder(pushNotificationBuilder: PushNotificationBuilder?) {
        //pushNotificationBuilder.setmLargIcon(R.drawable.flyme_status_ic_notification);
//        pushNotificationBuilder!!.setmStatusbarIcon(R.drawable.mz_push_notification_small_icon)
        //        pushNotificationBuilder.setAppLabel("测试标签");
        //        if(pushNotificationBuilder.getContext()!= null){
        //            pushNotificationBuilder.setAppLargeIcon(BitmapFactory.decodeResource(pushNotificationBuilder.getContext().getResources(),R.drawable.upspush));
        //        }
//        DebugLogger.e(TAG, "current clickpacakge " + pushNotificationBuilder.getClickPackageName())
    }

    override fun onNotificationArrived(context: Context, mzPushMessage: MzPushMessage) {
        DebugLogger.i(TAG, "onNotificationArrived title " + mzPushMessage.title + "content "
                + mzPushMessage.content + " selfDefineContentString " + mzPushMessage.selfDefineContentString + " notifyId " + mzPushMessage.notifyId)
//        MainActivity.notifyIdList.add(mzPushMessage.notifyId)
//        DebugLogger.e(TAG, "current notifyid " + MainActivity.notifyIdList)
    }

    override fun onNotificationClicked(context: Context, mzPushMessage: MzPushMessage) {
        DebugLogger.i(TAG, "onNotificationClicked title " + mzPushMessage.title + "content "
                + mzPushMessage.content + " selfDefineContentString " + mzPushMessage.selfDefineContentString + " notifyId " + mzPushMessage.notifyId)
//        val intArray = IntArray(MainActivity.notifyIdList.size())
//        for (i in 0 until MainActivity.notifyIdList.size()) {
//            intArray[i] = MainActivity.notifyIdList.get(i)
//        }
//        DebugLogger.e(TAG, "clear notifyId $intArray")
//        PushManager.clearNotification(context, intArray)
//        MainActivity.notifyIdList.clear()
//
//        if (!TextUtils.isEmpty(mzPushMessage.selfDefineContentString)) {
//            print(context, " 点击自定义消息为：" + mzPushMessage.selfDefineContentString)
//        }
    }

    override fun onNotificationDeleted(context: Context, mzPushMessage: MzPushMessage) {
        DebugLogger.i(TAG, "onNotificationDeleted title " + mzPushMessage.title + "content "
                + mzPushMessage.content + " selfDefineContentString " + mzPushMessage.selfDefineContentString + " notifyId " + mzPushMessage.notifyId)
    }

    override fun onNotifyMessageArrived(context: Context, message: String) {
        DebugLogger.i(TAG, "onNotifyMessageArrived messsage $message")
    }

    private fun print(context: Context, info: String) {
        Handler(context.getMainLooper()).post(Runnable { Toast.makeText(context, info, Toast.LENGTH_LONG).show() })
    }

    companion object {
        private val TAG = javaClass.simpleName
    }

}