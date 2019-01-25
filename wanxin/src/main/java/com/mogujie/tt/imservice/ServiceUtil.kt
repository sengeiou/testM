package com.mogujie.tt.imservice

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.M
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS




/**
 * Created by wr
 * Date: 2019/1/21  8:54
 * mail: 1902065822@qq.com
 * describe:
 */
object ServiceUtil{
    @TargetApi(Build.VERSION_CODES.M)
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val packageName = context.getPackageName()
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName)
    }


    /**
     * 针对N以上的Doze模式
     *
     * @param activity
     */
    fun isIgnoreBatteryOption(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent()
                val packageName = activity.packageName
                val pm = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:$packageName")
                    activity.startActivityForResult(intent, 801)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}