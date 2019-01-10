package com.qingmeng.mengmeng.base

import com.qingmeng.mengmeng.entity.BaseBean

import com.qingmeng.mengmeng.entity.MyMyFollowBean
import com.qingmeng.mengmeng.entity.UserInfo
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by zq on 2018/8/13
 */
interface Api {

    //我的关注
    @GET("api_my/get_attention")
    fun myFollow(@Query("userId") userId: Int, @Query("pageNum") pageNum: Int): Observable<BaseBean<MyMyFollowBean>>

    //修改密码
    @POST("app/user/update_password")
    fun updatePass(@Query("password") password: String, @Query("newPassword") newPassword: String, @Query("verifyPassword") verifyPassword: String, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<Any>>
    //账号登录
    @POST("app/user/account_login")
    fun accountlogin( @Query("account") account: String,@Query("password") password: String): Observable<BaseBean<UserInfo>>
    //短信登录
    @POST("app/user/msm_login")
    fun msmlogin( @Query("phone") phone: String,@Query("msmCode") msmCode: String): Observable<BaseBean<UserInfo>>
}