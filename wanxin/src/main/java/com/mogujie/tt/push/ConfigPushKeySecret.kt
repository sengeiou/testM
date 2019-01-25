package com.mogujie.tt.push

import android.content.Context
import android.content.pm.PackageManager
import com.meizu.cloud.pushinternal.DebugLogger
import com.mogujie.tt.push.XiaomiPush.TAG

/**
 * Created by wr
 * Date: 2019/1/4  13:47
 * mail: 1902065822@qq.com
 * describe:
 */
object ConfigPushKeySecret {
    //小米
    var xiaomiAppId = "2882303761517926618"
    var xiaomiAppKey = "5261792694618"
    var xiaomiSecret = "Kg6r1ELnZpdxEoEnAu2QMw=="

    //华为
    var huaweiAppId = "100572719"
    var huaweiAppSecret = "7c5bceec92997c0e09fdb462ca01bfdb"

    //魅族
    var meizuAppId = "1003557"
    var meizuAppKey = "f65aceedcbbf4ff9859b69f6922f9120"
    var meizuAppSecret = "d734032fe3044d13afceb8df0f118706"

    //vivo
    var vivoAppId = "10799"
    var vivoAppKey = "b8754b9e-e1bf-4ced-b796-6d73084a1c94"
    var vivoAppSecret = "839bb047-c800-4393-b98a-aeda65c1b5ec"

    //坑（2882303761517926618太长）
//    fun getXiaomiAppId(context: Context) = getMetaDataString(context, "XIAOMI_APP_ID")
//    fun getXiaomiAppKey(context: Context) = getMetaDataString(context, "XIAOMI_APP_KEY")
//    fun getHuaweiAppId(context: Context) = getMetaDataString(context, "HUAWEI_APP_ID")
//    fun getMeizuAppId(context: Context) = getMetaDataString(context, "MEIZU_APP_ID")
//    fun getMeizuAppKey(context: Context) = getMetaDataString(context, "MEIZU_APP_KEY")
//    fun getVivoAppId(context: Context) = getMetaDataString(context, "VIVO_APP_ID")
//    fun getVivoAppKey(context: Context) = getMetaDataString(context, "VIVO_APP_KEY")

    fun getMetaDataString(context: Context, tag: String): String? {
        var appKey: String? = null
        try {
            val appInfo = context.packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA)
            appKey = appInfo.metaData.getString(tag)
            if (appKey == null) {
                appKey = appInfo.metaData.getInt(tag).toString()
            }
            if (appKey == "0") {
                appKey = appInfo.metaData.getLong(tag).toString()
            }
            DebugLogger.e(TAG, "KeySecret#$tag=$appKey")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return appKey
    }
}