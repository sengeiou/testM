package com.qingmeng.mengmeng.utils

import android.annotation.SuppressLint
import com.qingmeng.mengmeng.base.Api
import com.qingmeng.mengmeng.base.SealAccountInterceptor
import com.qingmeng.mengmeng.constant.IConstants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by zq on 2018/8/13
 */
@SuppressLint("StaticFieldLeak")
object ApiUtils {
    fun getApi(): Api {
        val okClientBuilder = OkHttpClient.Builder().apply {
            this.addInterceptor(SealAccountInterceptor())
            connectTimeout(15, TimeUnit.SECONDS)
            readTimeout(60, TimeUnit.SECONDS)
            writeTimeout(60, TimeUnit.SECONDS)
        }
        return Retrofit.Builder()
                .baseUrl(IConstants.BASE_URL)
                .client(okClientBuilder.build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(Api::class.java)
    }
}