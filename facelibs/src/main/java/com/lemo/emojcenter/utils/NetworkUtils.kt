package com.lemo.emojcenter.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telephony.TelephonyManager

/**
 * Description  网络管理类
 * Auther  Xingzai
 * Date  2016/9/22
 * Email 18772878712@163.com
 * create by XingZai
 */
object NetworkUtils {

    /**
     * 网络状态 -1 无网络
     */
    private var netWorkStatus = -1

    /**
     * 判断是wifi还是3g网络
     *
     * @param context
     * @return
     */
    fun isWifi(context: Context): Boolean {
        return netWorkStatus == ConnectivityManager.TYPE_WIFI
    }


    fun refresh(context: Context) {
        val cm = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkINfo = cm.activeNetworkInfo
        if (networkINfo != null) {
            netWorkStatus = networkINfo.type
        } else {
            netWorkStatus = -1
        }

    }

    /**
     * 判断是否是3G网络
     *
     * @param context
     * @return
     */
    fun is3rd(context: Context): Boolean {
        val cm = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkINfo = cm.activeNetworkInfo
        return if (networkINfo != null && networkINfo.type == ConnectivityManager.TYPE_MOBILE) {
            true
        } else false
    }

    /**
     * 判断WIFI是否打开
     *
     * @param context
     * @return
     */
    fun isWifiEnabled(context: Context): Boolean {
        val mgrConn = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mgrTel = context
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return mgrConn.activeNetworkInfo != null && mgrConn
                .activeNetworkInfo.state == NetworkInfo.State.CONNECTED || mgrTel
                .networkType == TelephonyManager.NETWORK_TYPE_UMTS
    }

    fun isNetworkAvailable(c: Context): Boolean {
        val context = c.applicationContext
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (connectivityManager == null) {
            return false
        } else {
            // 获取NetworkInfo对象
            val networkInfo = connectivityManager.allNetworkInfo

            if (networkInfo != null && networkInfo.size > 0) {
                for (aNetworkInfo in networkInfo) {
                    //          System.out.println(i + "===状态===" + networkInfo[i].getState());
                    //          System.out.println(i + "===类型===" + networkInfo[i].getTypeName());
                    // 判断当前网络状态是否为连接状态
                    if (aNetworkInfo.state == NetworkInfo.State.CONNECTED) {
                        return true
                    }
                }
            }
        }
        return false
    }
}
