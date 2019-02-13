package com.mogujie.tt.push

import android.content.Context
import android.util.Log
import com.vivo.push.model.UPSNotificationMessage
import com.vivo.push.sdk.OpenClientPushMessageReceiver

class VivoMsgPushReceiver : OpenClientPushMessageReceiver() {

    override fun onNotificationMessageClicked(context: Context, msg: UPSNotificationMessage) {
        val customContentString = msg.skipContent
        val notifyString = "通知点击 msgId " + msg.msgId + " ;customContent=" + customContentString
        Log.d(TAG, notifyString)

    }

    override fun onReceiveRegId(context: Context, regId: String) {
        val responseString = "onReceiveRegId regId = $regId"
        Log.d(TAG, responseString)
    }

    companion object {
        val TAG = VivoMsgPushReceiver::class.java.simpleName
    }
}
