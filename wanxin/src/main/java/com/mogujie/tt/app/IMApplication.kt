package com.mogujie.tt.app

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Message
import android.os.Messenger
import android.support.multidex.MultiDex
import com.facebook.stetho.Stetho
import com.mogujie.tt.imservice.service.IMService
import com.mogujie.tt.imservice.service.MyJobService
import com.mogujie.tt.push.PushAllManager
import com.mogujie.tt.utils.ImageLoaderUtil
import com.mogujie.tt.utils.Logger


class IMApplication : Application() {
    private val logger = Logger.getLogger(IMApplication::class.java)

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        logger.i("Application starts")
        application = this
        startIMService()
        ImageLoaderUtil.initImageLoaderConfig(applicationContext)

        //调试
        Stetho.initializeWithDefaults(this)

        //        UmengPush.INSTANCE.init(this);
        PushAllManager.push.register(this)
    }

    private fun startIMService() {
        logger.i("start IMService")
        val intent = Intent()
        intent.setClass(this, IMService::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

//        val serviceIntent = Intent(this, MyJobService::class.java)
//        serviceIntent.putExtra("messenger", Messenger(mHandler))
//        startService(serviceIntent)
//        MyJobService.startScheduler(applicationContext)
    }

    var mHandler: Handler = object : Handler(/* default looper */) {
        override fun handleMessage(msg: Message) {
            logger.i("MyJobService:what" + msg.what + "")

        }
    }

    companion object {
        private var application: IMApplication? = null

        /**
         * @param args
         */
        @JvmStatic
        fun main(args: Array<String>) {
        }

        var gifRunning = true//gif是否运行

        val instance: Application?
            get() = application
    }
}
