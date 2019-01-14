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

    //获取静态数据
    @GET("api/get_system_static_info")
    fun getStaticInfo(@Header("VERSION") version: String, @Query("type") type: Int): Observable<BaseBean<StaticDataBean>>

    //获取首页推荐列表
    @GET("api/join/get_setting_brands")
    fun getRecommend(@Query("sysStaticId") sysStaticId: Int, @Query("pageNum") pageNum: Int): Observable<BaseBean<JoinRecommendBean>>

    //获取首页推荐列表
    @GET("api/banner/get_banner")
    fun getBanners(@Header("VERSION") version: String, @Query("type") type: Int): Observable<BaseBean<BannersBean>>

    //账号登录
    @POST("app/user/account_login")
    fun accountlogin(@Query("account") account: String, @Query("password") password: String): Observable<BaseBean<UserBean>>

    //短信登录
    @POST("app/user/msm_login")
    fun msmlogin(@Query("phone") phone: String, @Query("msmCode") msmCode: String): Observable<BaseBean<UserBean>>

    /**
     * =========================================我的板块=========================================
     */
    //获取我的页面信息
    @GET("api/personal/get_center_personal")
    fun myInformation(@Header("ACCESS-TOKEN") token: String): Observable<BaseBean<MyInformation>>

    //获取个人设置页面信息
    @GET("api/personal/get_personal")
    fun mySettingsUser(@Header("ACCESS-TOKEN") token: String): Observable<BaseBean<MySettingsUserBean>>

    //修改个人信息
    @POST("api/personal/personal_modify")
    @FormUrlEncoded
    fun updateMySettingsUser(@Header("ACCESS-TOKEN") token: String): Observable<BaseBean<MySettingsUserBean>>

    //我的关注
    @GET("api_my/get_attention")
    fun myFollow(@Query("userId") userId: Int, @Query("pageNum") pageNum: Int): Observable<BaseBean<MyMyFollowBean>>

    //修改密码
    @POST("app/user/update_password")
    @FormUrlEncoded
    fun updatePass(@Field("password") password: String, @Field("newPassword") newPassword: String, @Field("verifyPassword") verifyPassword: String, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<Any>>

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