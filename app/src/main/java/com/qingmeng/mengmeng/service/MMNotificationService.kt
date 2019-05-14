package com.qingmeng.mengmeng.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.view.View
import com.lemo.emojcenter.FaceInitData.context
import com.mogujie.tt.config.DBConstant
import com.mogujie.tt.config.IntentConstant
import com.mogujie.tt.config.SysConstant
import com.mogujie.tt.db.sp.ConfigurationSp
import com.mogujie.tt.imservice.entity.UnreadEntity
import com.mogujie.tt.imservice.event.UnreadEvent
import com.mogujie.tt.imservice.manager.IMContactManager
import com.mogujie.tt.imservice.manager.IMGroupManager
import com.mogujie.tt.imservice.manager.IMLoginManager
import com.mogujie.tt.imservice.manager.IMNotificationManager
import com.mogujie.tt.imservice.service.IMService
import com.mogujie.tt.imservice.support.IMServiceConnector
import com.mogujie.tt.utils.IMUIHelper
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.assist.ImageSize
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.activity.MyMessageChatActivity
import com.qingmeng.mengmeng.utils.BadgeUtil
import de.greenrobot.event.EventBus
import org.jetbrains.anko.ctx

@Suppress("UNREACHABLE_CODE")
@SuppressLint("HandlerLeak")
class MMNotificationService : Service() {
    private lateinit var mConfigurationSp: ConfigurationSp
    private var badgeCount = 0

    /**
     * 消息用到的
     */
    private var mImService: IMService? = null
    private val imServiceConnector = object : IMServiceConnector() {
        override fun onServiceDisconnected() {
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this)
            }
        }

        override fun onIMServiceConnected() {
            IMServiceConnector.logger.d("MMNotificationService#recent#onIMServiceConnected")
            mImService = this.imService
            if (mImService == null) {
                //why ,some reason
                return
            }
            //设置未读消息
            setNewMessagesCount()
        }
    }

    override fun onCreate() {
        super.onCreate()

        imServiceConnector.connect(this)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        val loginId = IMLoginManager.instance().loginId
        mConfigurationSp = ConfigurationSp.instance(ctx, loginId)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val notification = Notification.Builder(applicationContext, CHANNEL_ID_STRING).build()
//            startForeground(IMServiceNotificaId, notification)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * 未读消息
     */
    fun onEventMainThread(event: UnreadEvent) {
        when (event.event) {
            UnreadEvent.Event.UNREAD_MSG_RECEIVED -> {  //新消息接收
                setNewMessagesCount()
            }
            UnreadEvent.Event.UNREAD_MSG_LIST_OK -> {
            }
            UnreadEvent.Event.SESSION_READED_UNREAD_MSG -> {
                setNewMessagesCount()
            }
        }
    }

    private fun setNewMessagesCount() {
        val recentSessionList = mImService?.sessionManager?.recentListInfo
        var unReadCount = 0
        recentSessionList?.forEach {
            unReadCount += it.unReadCnt
        }
        badgeCount = unReadCount
        //设置桌面角标提示
//        ShortcutBadger.applyCount(this, unReadCount)
        BadgeUtil.setBadgeCount(this, unReadCount)
    }

    /**
     * 通知栏通知
     */
    fun onEvent(entity: UnreadEntity) {
        showNotification(entity)
    }

    private fun showNotification(unreadEntity: UnreadEntity) {
        // todo eric need to set the exact size of the big icon
        // 服务端有些特定的支持 尺寸是不是要调整一下 todo 100*100  下面的就可以不要了
        val targetSize = ImageSize(80, 80)
        val peerId = unreadEntity.peerId
        val sessionType = unreadEntity.sessionType
        var avatarUrl = ""
        var title = ""
        val content = unreadEntity.info
        val unit = ctx.getString(com.leimo.wanxin.R.string.msg_cnt_unit)
        val totalUnread = unreadEntity.unReadCnt

        if (unreadEntity.sessionType == DBConstant.SESSION_TYPE_SINGLE) {
            val contact = IMContactManager.instance().findContact(peerId)
            if (contact != null) {
                val name = if (!contact.mainName.isNullOrBlank()) contact.mainName else unreadEntity.nickName
                title = name
                avatarUrl = contact.avatar
            } else {
                title = "User_$peerId"
                avatarUrl = ""
            }
        } else {
            val group = IMGroupManager.instance().findGroup(peerId)
            if (group != null) {
                title = group.mainName
                avatarUrl = group.avatar
            } else {
                title = "Group_$peerId"
                avatarUrl = ""
            }
        }
        //获取头像
        avatarUrl = IMUIHelper.getRealAvatarUrl(avatarUrl)
        val ticker = String.format("[%d%s]%s: %s", totalUnread, unit, title, content)
        val notificationId = getSessionNotificationId(unreadEntity.sessionKey)
        val intent = Intent(ctx, MyMessageChatActivity::class.java)
        intent.putExtra(IntentConstant.KEY_SESSION_KEY, unreadEntity.sessionKey)
        intent.putExtra("title", title)

        val finalTitle = title
        ImageLoader.getInstance().loadImage(avatarUrl, targetSize, null, object : SimpleImageLoadingListener() {

            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                // holder.image.setImageBitmap(loadedImage);
                showInNotificationBar(finalTitle, ticker, loadedImage, notificationId, intent)
            }

            override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
                // 服务器支持的格式有哪些
                // todo eric default avatar is too small, need big size(128 * 128)
                val defaultBitmap = BitmapFactory.decodeResource(ctx.getResources(), IMUIHelper.getDefaultAvatarResId(unreadEntity.sessionType))
                showInNotificationBar(finalTitle, ticker, defaultBitmap, notificationId, intent)
            }
        })
    }

    private fun showInNotificationBar(title: String, ticker: String, iconBitmap: Bitmap?, notificationId: Int, intent: Intent) {
        val notifyMgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                ?: return
        val builder: NotificationCompat.Builder
        //判断是否是8.0Android.O
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan1 = NotificationChannel(IMNotificationManager.PRIMARY_CHANNEL,
                    "Primary Channel", NotificationManager.IMPORTANCE_DEFAULT)
            chan1.lightColor = Color.GREEN
            chan1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notifyMgr.createNotificationChannel(chan1)
            builder = NotificationCompat.Builder(ctx, IMNotificationManager.PRIMARY_CHANNEL)
        } else {
            builder = NotificationCompat.Builder(ctx)
        }
        builder.setContentTitle(title)
        builder.setContentText(ticker)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setTicker(ticker)
        builder.setWhen(System.currentTimeMillis())
        builder.setAutoCancel(true)

        // this is the content near the right bottom side
        // builder.setContentInfo("content info");

        if (mConfigurationSp.getCfg(SysConstant.SETTING_GLOBAL, ConfigurationSp.CfgDimension.VIBRATION)) {
            // delay 0ms, vibrate 200ms, delay 250ms, vibrate 200ms
            val vibrate = longArrayOf(0, 200, 250, 200)
            builder.setVibrate(vibrate)
        } else {
        }

        // sound
        if (mConfigurationSp.getCfg(SysConstant.SETTING_GLOBAL, ConfigurationSp.CfgDimension.SOUND)) {
            builder.setDefaults(Notification.DEFAULT_SOUND)
        } else {
        }
        if (iconBitmap != null) {
            builder.setLargeIcon(iconBitmap)
        } else {
            // do nothint ?
        }
        // if MessageActivity is in the background, the system would bring it to
        // the front
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent = PendingIntent.getActivity(ctx, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pendingIntent)
        val notification = builder.build()
        //如果机型是小米 就用这种方式加载桌面角标
        if (Build.MANUFACTURER.toLowerCase() == "xiaomi") {
//            ShortcutBadger.applyNotification(this, notification, badgeCount)
            BadgeUtil.applyNotification(this, notification, badgeCount)
        }
        notifyMgr.notify(notificationId, notification)
    }

    // come from
    // http://www.partow.net/programming/hashfunctions/index.html#BKDRHashFunction
    private fun hashBKDR(str: String): Long {
        val seed: Long = 131 // 31 131 1313 13131 131313 etc..
        var hash: Long = 0

        for (i in 0 until str.length) {
            hash = hash * seed + str[i].toLong()
        }
        return hash
    }

    private fun getSessionNotificationId(sessionKey: String): Int {
        return hashBKDR(sessionKey).toInt()
    }

    override fun onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        imServiceConnector.disconnect(context)
        super.onDestroy()
    }
}
