package com.app.common.api.interceptor


import android.util.Log
import okhttp3.FormBody
import java.net.URLDecoder

/**
 * 日志拦截
 * Created by wangru
 * mail: 1902065822@qq.com
 * describe:
 */

class CacheInterceptor : BaseInterceptor(requestCallback = {
        val method = it.method();
        val sb = StringBuilder()
        if ("POST".equals(method)) {
            if (it.body() is FormBody) {
                val body = it.body() as FormBody
                for (i in 0..(body.size() - 1)) {
                    sb.append(body.encodedName(i) + "=" + body.encodedValue(i) + ",")
                }
                sb.delete(sb.length - 1, sb.length);
            }
        }

        val key = "${it.method()}#${it.url()}" + if (it.method() == "POST" && it.body() is FormBody) {
            val paramSB = StringBuilder()
            paramSB.append("#")
            val body = it.body() as FormBody
            for (i in 0..(body.size() - 1)) {
                paramSB.append(body.encodedName(i) + "=" + body.encodedValue(i) + ",")
            }
            paramSB.delete(sb.length - 1, sb.length)
            paramSB.toString()
        } else ""

})
