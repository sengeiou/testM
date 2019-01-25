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

    //注册
    @POST("app/user/phone_register")
    @FormUrlEncoded
    fun register(@Field("userName") userName: String, @Field("phone") phone: String, @Field("smsCode") smsCode: String,
                 @Field("password") password: String, @Field("verifyPassword") verifyPassword: String,
                 @Field("type") type: Int, @Field("isUserProtocol") isUserProtocol: Int = 1): Observable<BaseBean<UserBean>>

    /**
     * 获取静态数据
     * @param type 类型：1.首页banner8个icon 2.首页列表模块 3.列表筛选标题 4.综合排序 5.反馈类型
     */
    @GET("api/get_system_static_info")
    fun getStaticInfo(@Header("VERSION") version: String, @Query("type") type: Int): Observable<BaseBean<StaticDataBean>>

    //获取首页推荐列表
    @GET("api/join/get_setting_brands")
    fun getRecommend(@Query("sysStaticId") sysStaticId: Int, @Query("pageNum") pageNum: Int): Observable<BaseBean<JoinRecommendBean>>

    //获取Banner图
    @GET("api/banner/get_banner")
    fun getBanners(@Header("VERSION") version: String, @Query("type") type: Int): Observable<BaseBean<BannerData>>

    //品牌详情接口
    @GET("api/brand_detail")
    fun getBrandDetail(@Header("ACCESS-TOKEN") token: String, @Query("brandId") brandId: Int): Observable<BaseBean<BrandBean>>

    //添加我的关注
    @GET("api/my_attention/add_attention")
    fun addAttention(@Header("ACCESS-TOKEN") token: String, @Query("brandId") brandId: Int): Observable<BaseBean<Any>>

    //申请加盟接口
    @GET("api/add_comment")
    fun join(@Query("brandId") brandId: Int, @Query("name") name: String,
                     @Query("phone") phone: String, @Query("brandId") message: String): Observable<BaseBean<Any>>

    //账号登录
    @POST("app/user/account_login")
    fun accountlogin(@Query("account") account: String, @Query("password") password: String): Observable<BaseBean<UserBean>>

    //短信登录
    @POST("app/user/sms_login")
    fun smslogin(@Query("phone") phone: String, @Query("smsCode") smsCode: String): Observable<BaseBean<UserBean>>

    /**
     * =========================================我的板块=========================================
     */
    //获取我的页面信息
    @GET("api/personal/get_center_personal")
    fun myInformation(@Header("ACCESS-TOKEN") token: String): Observable<BaseBean<MyInformation>>

    //校验是设置密码还是修改密码
    @GET("api/validate/validate_user_info?type=1")
    fun mySettingsOrUpdatePass(@Header("ACCESS-TOKEN") token: String): Observable<BaseBean<MyInformation>>

    //获取个人设置页面信息
    @GET("api/personal/get_personal")
    fun mySettingsUser(@Header("ACCESS-TOKEN") token: String): Observable<BaseBean<MySettingsUserBean>>

    //静态数据 创业资本
    @GET("api/get_capital")
    fun getMoneyStatic(): Observable<BaseBean<SelectDialogBean>>

    //静态数据 感兴趣行业
    @GET("api/get_industry")
    fun getInterestStatic(): Observable<BaseBean<SelectDialogBean>>

    //静态数据 所有城市
    @GET("api/get_city_all")
    fun getCityStatic(): Observable<BaseBean<AllCityBean>>

    //修改个人信息
    @POST("api/personal/personal_modify")
    @FormUrlEncoded
    fun updateMySettingsUser(
            @Field("avatar") avatar: String?,//头像
            @Field("name") name: String?,//真实姓名*
            @Field("sex") sex: Int?,//年龄
            @Field("phone") phone: String?,//手机号*
            @Field("telephone") telephone: String?,//固定电话
            @Field("wx") wx: String?,//微信
            @Field("qq") qq: String?,//qq
            @Field("email") email: String?,//邮箱
            @Field("districtId") districtId: Int?,//城市id*
            @Field("capitalId") capitalId: Int?,//创业资本*
            @Field("industryOfInterest") industryOfInterest: String?,//感兴趣行业*
            @Header("ACCESS-TOKEN") token: String//token*
    ): Observable<BaseBean<MySettingsUserBean>>

    //设置密码
    @GET("app/user/setting_password")
    fun setPass(@Query("name") name: String, @Query("password") password: String, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<Any>>

    //修改密码
    @POST("app/user/update_password")
    @FormUrlEncoded
    fun updatePass(@Field("password") password: String, @Field("newPassword") newPassword: String, @Field("verifyPassword") verifyPassword: String, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<Any>>

    /**
     * 获取banner图信息
     * @param  type 1、首页；3、头报 5.登录banner 6.引导页
     **/
    @POST("api/banner/get_banner")
    fun getbanner(@Query("VERSION") version: String, @Query("type") type: Int): Observable<BaseBean<BannerData>>

    //忘记密码
    @POST("app/user/forget_password")
    fun forgetpassword(@Query("phone") phone: String, @Query("msmCode") msmCode: String, @Query("password") password: String, @Header("notarizePassword") notarizePassword: String): Observable<BaseBean<UserBean>>

    //获取oss令牌
    @GET("http://oss.ilashou.com/oss/authorization_app?name=mm")
    fun getOssToken(): Observable<BaseBean<OssDataBean>>

    //我的反馈
    @POST("/api/feedback/add_feedback")
    fun join_feedback(@Header("ACCESS-TOKEN") token: String, @Query("brandId") brandId: Int, @Query("type") type: Int, @Query("content") content: String, @Query("urlList") urlList: ArrayList<String>): Observable<BaseBean<Any>>

    //第三方登录
    @POST("/app/user/third_party_login")
    fun thirdlogin(@Query("openId") openId: String, @Query("type") type: Int): Observable<BaseBean<UserBean>>

    //获取热门词汇
    @GET("api/join/hot_search")
    fun get_hot_search(@Header("VERSION") version: String): Observable<BaseBean<HotSearchBean>>

    //爱加盟首页搜索
    @POST("/api/join/get_search_brands")
    fun join_search_brands(@Query("keyWord") keyWord: String, @Query("fatherId") fatherId: Int,
                           @Query("typeId") typeId: Int, @Query("cityIds") cityIds: String, @Query("capitalIds") capitalIds: String,
                           @Query("modeIds") modeIds: String, @Query("integratedSortId") integratedSortId: Int, @Query("pageNum") pageNum: Int): Observable<BaseBean<SearchDtoListBean>>

    //我的关注
    @GET("api/my_attention/get_attention")
    fun myFollow(@Query("pageNum") pageNum: Int, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<MyMyFollowBean>>

    //取消我的关注
    @GET("api/my_attention/un_subscribe")
    fun deleteMyFollow(@Query("brandId") brandId: Int, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<Any>>

    //我的留言
    @GET("api/my_comment/get_comments")
    fun myLeavingMessage(@Query("pageNum") pageNum: Int, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<MyMyFollowBean>>

    //删除我的留言
    @GET("api/my_comment/del_comment")
    fun deleteMyLeavingMessage(@Query("commentId") commentId: Int, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<Any>>

    //我的足迹
    @GET("api/my_footprint/get_footprint")
    fun myFootprint(@Query("pageNum") pageNum: Int, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<MyMyFollowBean>>

    //删除我的足迹
    @GET("api/my_footprint/del_footprint")
    fun deleteMyFootprint(@Query("brandId") brandId: Int, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<Any>>
}