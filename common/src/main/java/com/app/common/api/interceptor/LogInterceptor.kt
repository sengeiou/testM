package com.app.common.api.interceptor


import android.util.Log
import okhttp3.FormBody
import okhttp3.Request
import java.net.URLDecoder

/**
 * 日志拦截
 * Created by wangru
 * mail: 1902065822@qq.com
 * describe:
 */

class LogInterceptor : BaseInterceptor(requestCallback = {
    try {
        val method = it.method();
        val sb = StringBuilder();
        if ("POST" == method) {
            if (it.body() is FormBody) {
                val body = it.body() as FormBody
                for (i in 0 until body.size()) {
                    sb.append(body.encodedName(i) + "=" + body.encodedValue(i) + ",")
                }
                sb.delete(sb.length - 1, sb.length);
            }
        }
        //URLDecoder.decode URLDecode解码
        val info = "Request{" +
                "method=[${it.method()}]" +
                ", url=[${it.url()}]" +
                ", headers=[" + it.headers().toString() + "]" +
                ", isHttps=" + it.isHttps +
                ", Params=[${URLDecoder.decode(sb.toString(), "utf-8")}]" +
                '}'
        Log.i("LogInterceptor", "intercept#request:\n$info")
    } catch (e: Exception) {
    }
},resultCallback={result: String, request: Request ->
    Log.i("LogInterceptor", "intercept#result: ###[${request.url().toString().replace("http://","").replace("https://","")}]###\n$result")
})
