package com.mogujie.tt.api

import com.mogujie.tt.bean.LoginBean
import com.mogujie.tt.bean.RegBean
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Part


/**
 * 所有请求
 * Created by wangru
 * Date: 2018/7/6  15:02
 * mail: 1902065822@qq.com
 * describe:
 */
interface ApiService {
    //注册
    @POST("register")
    @FormUrlEncoded
    fun register(@Field("customAccount") customAccount: String, @Field("projectId") projectId: Int = 1, @Field("appName") appName: String = "wanxin"): Observable<RegBean>

    //登录
    @POST("login")
    @FormUrlEncoded
    fun login(@Field("wxUserName") wxUserName: String, @Field("wxPassWord") wxPassWord: String, @Field("wxProjectId") wxProjectId: Int = 1): Observable<LoginBean>

    //撤回消息
    @POST("message/withdraw")
    @FormUrlEncoded
    fun msgRevoke(@Field("wxFromId") wxUserName: Int, @Field("wxToId") wxPassWord: Int, @Field("wxMsgId") wxProjectId: Int): Observable<BaseBean<Any>>

    //删除消息
    @POST("message/deletemsg")
    @FormUrlEncoded
    fun msgDelete(@Field("wxFromId") wxUserName: Int, @Field("wxToId") wxPassWord: Int, @Field("wxMsgId") wxProjectId: Int): Observable<BaseBean<Any>>

    @POST("message/deletemsg")
    @FormUrlEncoded
    fun addImg(@Part file: MultipartBody.Part): Observable<BaseBean<Any>>
}