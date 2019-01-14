package com.qingmeng.mengmeng.base

import com.qingmeng.mengmeng.entity.*
import io.reactivex.Observable
import retrofit2.http.*

/**
 * Created by zq on 2018/8/13
 */
interface Api {
    /**
     * 验证手机用户名是否注册
     * @param type 1 用户名 2 手机
     */
    @GET("api/validate/validate_info")
    fun hasRegistered(@Query("content") content: String, @Query("type") type: Int): Observable<BaseBean<Any>>

    //极验一次验证
    @GET("api/geetest_app/geetest_app_aptcha")
    fun checkCodeType(): Observable<BaseBean<CodeBean>>

    /**
     * 获取短信验证码接口
     * @param type 1.注册2.登陆3.找回密码4.修改手机号5.绑定手机号
     **/
    @POST("api/sms/sms_send")
    @FormUrlEncoded
    fun sendSms(@Field("phone") phone: String, @Field("type") type: Int, @Field("code") code: String = "",
                @Field("geetest_challenge") geetest_challenge: String = "", @Field("geetest_validate") geetest_validate: String = "",
                @Field("geetest_seccode") geetest_seccode: String = ""): Observable<BaseBean<Any>>

    @POST("app/user/phone_register")
    @FormUrlEncoded
    fun register(@Field("userName") userName: String, @Field("phone") phone: String, @Field("msmCode") msmCode: String,
                 @Field("password") password: String, @Field("verifyPassword") verifyPassword: String,
                 @Field("type") type: Int, @Field("isUserProtocol") isUserProtocol: Int = 1): Observable<BaseBean<UserBean>>

    //我的关注
    @GET("api_my/get_attention")
    fun myFollow(@Query("userId") userId: Int, @Query("pageNum") pageNum: Int): Observable<BaseBean<MyMyFollowBean>>

    //修改密码
    @POST("app/user/update_password")
    fun updatePass(@Query("password") password: String, @Query("newPassword") newPassword: String, @Query("verifyPassword") verifyPassword: String, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<Any>>
    //账号登录
    @POST("app/user/account_login")
    fun accountlogin( @Query("account") account: String,@Query("password") password: String): Observable<BaseBean<UserBean>>
    //短信登录
    @POST("app/user/msm_login")
    fun msmlogin( @Query("phone") phone: String,@Query("msmCode") msmCode: String): Observable<BaseBean<UserBean>>
    /**
     * 获取banner图信息
     * @param  type 1、首页；3、头报 5.登录banner 6.引导页
     **/
    @POST("api/banner/get_banner")
    fun getbanner( @Query("VERSION") version: String,@Query("type") type: Int): Observable<BaseBean<BannerData>>
    //忘记密码
    @POST("app/user/forget_password")
    fun forgetpassword(@Query("phone") phone: String, @Query("msmCode") msmCode: String, @Query("password") password: String, @Header("notarizePassword") notarizePassword: String): Observable<BaseBean<UserBean>>
}