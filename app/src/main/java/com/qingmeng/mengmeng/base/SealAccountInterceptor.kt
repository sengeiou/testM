package com.qingmeng.mengmeng.base

import android.util.Log
import com.qingmeng.mengmeng.BuildConfig
import okhttp3.*
import okio.Buffer
import java.io.EOFException
import java.net.URLDecoder
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
/**
 * Created by zq on 2018/9/6
 */
open class SealAccountInterceptor : Interceptor {
    private val UTF8 = Charset.forName("UTF-8")
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        //修改请求信息
//        request = addUrlInformation(request)
        //日志打印
//        if (BuildConfig.DEBUG) logOut(request)
        val response: Response = chain.proceed(request)
        val responseBody = response.body()
        responseBody?.let {
            val contentLength = it.contentLength()
            if (!bodyEncoded(response.headers())) {
                val source = it.source()
                source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
                val buffer = source.buffer()
                var charset: Charset? = UTF8
                val contentType = it.contentType()
                contentType?.let {
                    try {
                        charset = it.charset(UTF8)
                    } catch (e: UnsupportedCharsetException) {
                        return response
                    }
                }
                if (!isPlaintext(buffer)) {
                    return response
                }
//                if (contentLength != 0L) {
//                    val result = buffer.clone().readString(charset)
//                    try {
//                        val json = JSONObject(result)
//                        if (json.optInt("code") == 23000) {//重复登录
//                            loginOut()
//                        }
//                    } catch (e: JSONException) {
//                        e.printStackTrace()
//                        return response
//                    }
//                }
            }
        }
        return response
    }

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers.get("Content-Encoding")
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

    @Throws(EOFException::class)
    private fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size() < 64) buffer.size() else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (e: EOFException) {
            return false // Truncated UTF-8 sequence.
        }
    }

    //添加默认请求信息
    private fun addUrlInformation(original: Request, isToken: Boolean = false, isParams: Boolean = true): Request {
        val originalHttpUrl = original.url()
        //url添加get信息
        val url = originalHttpUrl.newBuilder().apply {
            if (isParams) {
                //版本号
                addQueryParameter("ver", "${BuildConfig.VERSION_CODE}")
                //平台 1 android 2 ios
                addQueryParameter("os", "1")
            }
        }.build()
        // Request customization: add request headers 添加Headers
        val requestBuilder = original.newBuilder().apply {
            if (isToken) {
                addHeader("accessToken", "")
            }
        }.url(url)
        return requestBuilder.build()
    }

    //日志输出
    private fun logOut(request: Request) {
        try {
            request.let {
                val method = it.method()
                val sb = StringBuilder()
                if ("POST".equals(method)) {
                    if (it.body() is FormBody) {
                        val body = it.body() as FormBody
                        for (i in 0..(body.size() - 1)) {
                            sb.append(body.encodedName(i) + "=" + body.encodedValue(i) + ",")
                        }
                        sb.delete(sb.length - 1, sb.length)
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
            }
        } catch (e: Exception) {
        }
    }
}