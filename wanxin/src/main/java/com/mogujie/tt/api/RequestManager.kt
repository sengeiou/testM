package com.mogujie.tt.api

import com.app.common.BuildConfig
import com.app.common.api.interceptor.LogInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * Created by wangru
 * Date: 2018/6/28  19:03
 * mail: 1902065822@qq.com
 * describe:
 */

class RequestManager() {
    private val retrofit: Retrofit

    private object SingletonHolder {
        val INSTANCE = RequestManager()
    }

    init {
        val client = OkHttpClient.Builder().apply {
            connectTimeout(ApiConfig.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
            addInterceptor(CommonInterceptor())
            addInterceptor(LogInterceptor())
        }.build()

        retrofit = Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
//                .addConverterFactory(CustomGsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    //这里返回一个泛型类，主要返回的是定义的接口类
    fun <T> createService(clazz: Class<T>): T {
        return retrofit.create(clazz)
    }

    companion object {
        val instance: RequestManager
            get() = SingletonHolder.INSTANCE
        val instanceApi: ApiService
            get() = SingletonHolder.INSTANCE.createService(ApiService::class.java)

    }

    private inner class CommonInterceptor(var isToken: Boolean = false, var isParams: Boolean = true) : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val originalHttpUrl = original.url()
            val url = originalHttpUrl.newBuilder().apply {
                if (isParams) {
                    //版本号
                    addQueryParameter("ver", "${BuildConfig.VERSION_CODE}")
                    //平台 1 android 2 ios
                    addQueryParameter("os", "1")
                }
            }.build()

            // Request customization: add request headers
            val requestBuilder = original.newBuilder().apply {
                if (isToken) {
//                    addHeader("accessToken", UserData.token)
                }
            }.url(url)

            val request = requestBuilder.build()
            return chain.proceed(request)
        }
    }
}
