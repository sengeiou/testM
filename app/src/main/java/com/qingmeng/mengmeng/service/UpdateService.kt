package com.qingmeng.mengmeng.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.support.v4.app.NotificationCompat.Builder
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.util.Log
import android.widget.RemoteViews
import com.qingmeng.mengmeng.R
import com.qingmeng.mengmeng.entity.ProgressBean
import com.qingmeng.mengmeng.utils.PathUtils
import de.greenrobot.event.EventBus
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

@Suppress("DEPRECATION")
@SuppressLint("HandlerLeak")
class UpdateService : Service() {

    private val TAG = "UpdateService"
    private val ACTION_NAME = "update_cancel"
    private val notifyId = 1
    private var path_apk: String? = null
    private var myIntentFilter: IntentFilter? = null
    private var mBroadcastReceiver: BroadcastReceiver? = null
    private var nm: NotificationManager? = null
    private var mBuilder: Builder? = null
    private var view: RemoteViews? = null
    private var notify: Notification? = null

    private val WHAT_START = 0
    private val WHAT_PROGRESS = 1
    private val WHAT_SUCCESS = 2
    private val WHAT_FAIL = 3
    private val KEY_APK_URL = "downloadPath"
    /**
     * 取消
     */
    private var cancel = false

    private var currProgress = 0


    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                WHAT_START -> {
                    initNotification()
                    EventBus.getDefault().post(ProgressBean(0))
                }
                WHAT_PROGRESS -> {
                    updateNotification(msg.arg1)
                    EventBus.getDefault().post(ProgressBean(msg.arg1))
                }
                WHAT_SUCCESS -> {
                    installApkFile(this@UpdateService, path_apk)
                    nm!!.cancel(notifyId)
                    stopSelf()
                }
                WHAT_FAIL -> {
                    nm!!.cancel(notifyId)
                    stopSelf()
                }
            }

        }
    }


    override fun onCreate() {
        super.onCreate()
        nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        myIntentFilter = IntentFilter()
        myIntentFilter!!.addAction(ACTION_NAME)
        mBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                EventBus.getDefault().post(ProgressBean(-1))
                val file = File(path_apk)
                file.delete()
                nm!!.cancel(notifyId)
                cancel = true
                stopSelf()
            }
        }
        registerReceiver(mBroadcastReceiver, myIntentFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.hasExtra(KEY_APK_URL)) {
            val url = intent.getStringExtra(KEY_APK_URL)
            if (TextUtils.isEmpty(url) || url == "null") {
                stopSelf()
            } else {
                Thread(Runnable { doDownload(url) }).start()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun doDownload(url: String) {
        val request = Request.Builder().url(url).build()
        val clint = OkHttpClient()
        clint.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "onFailure: " + e.message)
                mHandler.sendEmptyMessage(WHAT_FAIL)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                mHandler.sendEmptyMessage(WHAT_START)
                var stream: InputStream? = null
                path_apk = PathUtils.apkPath
                if (TextUtils.isEmpty(path_apk)) {
                    return
                }
                val file = File(path_apk)
                file.createNewFile()

                val buf = ByteArray(2048)
                var fos: FileOutputStream? = null
                var len: Int
                try {
                    val total = response.body()!!.contentLength()
                    var sum: Long = 0
                    stream = response.body()!!.byteStream()
                    fos = FileOutputStream(file)
                    len = stream!!.read(buf)
                    while (len != -1 && !cancel) {
                        fos.write(buf, 0, len)
                        sum += len.toLong()
                        len = stream.read(buf)

                        val progress = (sum.toFloat() * 1.0f * 100f / total).toInt()
                        if (progress >= currProgress + 1) {
                            currProgress = progress
                            val msg = Message.obtain()
                            msg.what = WHAT_PROGRESS
                            msg.arg1 = currProgress
                            mHandler.sendMessage(msg)
                        }
                    }
                    fos.flush()
                } catch (e: Exception) {
                    mHandler.sendEmptyMessage(WHAT_FAIL)
                } finally {
                    stream?.close()
                    fos?.close()
                }
                if (!cancel) {
                    mHandler.sendEmptyMessage(WHAT_SUCCESS)
                }
            }
        })
    }

    private fun initNotification() {
        mBuilder = Builder(this)
        view = RemoteViews(packageName, R.layout.layout_notification_update)
        val intent = Intent(ACTION_NAME)
        val intent_cancel = PendingIntent.getBroadcast(this, 0, intent, 0)
        view!!.setOnClickPendingIntent(R.id.notification_update_img_delete, intent_cancel)
        view!!.setProgressBar(R.id.notification_update_pb_rate, 100, 0, false)
        mBuilder!!.setContent(view)
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setTicker(getString(R.string.mengmeng_version_updating))
                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
        notify = mBuilder!!.build()
        notify!!.flags = Notification.FLAG_ONGOING_EVENT
        nm!!.notify(notifyId, notify)
    }

    private fun updateNotification(rate: Int) {
        view!!.setProgressBar(R.id.notification_update_pb_rate, 100, rate, false)
        nm!!.notify(notifyId, notify)
    }

    fun installApkFile(context: Context, filePath: String?) {
        val intent = Intent(Intent.ACTION_VIEW)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val contentUri = FileProvider.getUriForFile(context, "com.qingmeng.mengmeng.fileprovider", File(filePath))
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
        } else {
            intent.setDataAndType(Uri.fromFile(File(filePath)), "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    override fun onDestroy() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver)
        }

        cancel = true
        super.onDestroy()
    }
}
