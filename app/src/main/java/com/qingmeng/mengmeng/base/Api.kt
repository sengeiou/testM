package com.qingmeng.mengmeng.base

import com.qingmeng.mengmeng.entity.*
import com.qingmeng.mengmeng.utils.loginshare.bean.SinaUserBean
import com.qingmeng.mengmeng.utils.loginshare.bean.WxInfoBean
import com.qingmeng.mengmeng.utils.loginshare.bean.WxTokenBean
import io.reactivex.Observable
import io.reactivex.annotations.NonNull
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * Created by zq on 2018/8/13
 */
interface Api {
    /**
     * 下载文件
     */
    @Streaming
    @GET
    fun downLoadFile(@NonNull @Url url: String): Observable<ResponseBody>

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

    //第三方绑定
    @POST("app/user/third_party_phone_binding")
    @FormUrlEncoded
    fun bindPhone(@Field("phone") phone: String,
                  @Field("smsCode") smsCode: String,
                  @Field("openId") openId: String,
                  @Field("token") token: String,
                  @Field("weChatUnionId") weChatUnionId: String,
                  @Field("avatar") avatar: String,
                  @Field("bindingType") type: Int,
                  @Field("password") password: String,
                  @Field("isUserProtocol") isUserProtocol: Int = 1, //是否同意用户协议（0.否1.是）
                  @Field("userName") userName: String,
                  @Field("userType") userType: Int = 2, //用户类型（用户类型:1.企业，2.个人）
                  @Field("deviceType") deviceType: Int = 1, //设备登录类型（1.app(安卓) ，2:app(ios)）
                  @Field("thirdUserName") thirdUserName: String): Observable<BaseBean<UserBean>>

    //注册
    @POST("app/user/phone_register")
    @FormUrlEncoded
    fun register(@Field("userName") userName: String, @Field("phone") phone: String, @Field("smsCode") smsCode: String,
                 @Field("password") password: String,
                 @Field("type") type: Int, @Field("isUserProtocol") isUserProtocol: Int = 1, @Field("deviceType") deviceType: Int): Observable<BaseBean<UserBean>>

    //获取版本信息
    @GET("api/version/get_version_info")
    fun getVersionInfo(@Query("phoneLogo") phoneLogo: String, @Query("version") version: String,
                       @Query("type") type: Int = 1): Observable<BaseBean<UpdateBean>>

    /**
     * 获取静态数据
     * @param type 类型：1.首页banner8个icon 2.首页列表模块 3.列表筛选标题 4.综合排序 5.反馈类型
     */
    @GET("api/get_system_static_info")
    fun getStaticInfo(@Header("VERSION") version: String, @Query("type") type: Int): Observable<BaseBean<StaticDataBean>>

    //获取首页推荐列表
    @GET("api/join/get_setting_brands")
    fun getRecommend(@Query("sysStaticId") sysStaticId: Int, @Query("pageNum") pageNum: Int): Observable<BaseBean<JoinRecommendBean>>


    /**
     * 获取Banner图
     * @param type 类型 7、首页banner；8、头报banner；9、广告页 10、登录
     * @param mobilePhoneType 手机类型 1.全部、2.ios、3.安卓、4.iPhoneX
     **/
    @GET("api/banner/get_banner")
    fun getBanners(@Header("VERSION") version: String, @Query("type") type: Int,
                   @Query("mobilePhoneType") mobilePhoneType: Int = 3): Observable<BaseBean<BannerData>>

    //添加定位信息
    @POST("api/location/add")
    fun addLocation(@Header("ACCESS-TOKEN") token: String, @Query("latitude") latitude: String,
                    @Query("longitude") longitude: String, @Query("uuid") uuid: String): Observable<BaseBean<Any>>

    //品牌详情接口
    @GET("api/brand_detail")
    fun getBrandDetail(@Header("ACCESS-TOKEN") token: String, @Query("brandId") brandId: Int): Observable<BaseBean<BrandBean>>

    //添加我的关注
    @GET("api/my_attention/add_attention")
    fun addAttention(@Header("ACCESS-TOKEN") token: String, @Query("brandId") brandId: Int): Observable<BaseBean<Any>>

    //申请加盟接口
    @POST("api/add_comment")
    fun join(@Query("brandId") brandId: Int, @Query("name") name: String,
             @Query("phone") phone: String, @Query("message") message: String, @Query("type") type: Int): Observable<BaseBean<Any>>

    //账号登录
    @POST("app/user/account_login")
    fun accountLogin(@Query("account") account: String, @Query("password") password: String, @Query("deviceType") deviceType: Int): Observable<BaseBean<UserBean>>

    //短信登录
    @POST("app/user/msm_login")
    fun smslogin(@Query("phone") phone: String, @Query("smsCode") smsCode: String, @Query("deviceType") deviceType: Int): Observable<BaseBean<UserBean>>

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

    @GET("api/get_city_all")
    fun getCityStatic(@Header("VERSION") version: String): Observable<BaseBean<AllCityBean>>

    //修改个人信息
    @POST("api/personal/personal_modify")
    @FormUrlEncoded
    fun updateMySettingsUser(
            @Field("avatar") avatar: String?,//头像
            @Field("name") name: String,//真实姓名*
            @Field("sex") sex: Int?,//年龄
//            @Field("phone") phone: String,//手机号*
            @Field("telephone") telephone: String?,//固定电话
            @Field("wx") wx: String?,//微信
            @Field("qq") qq: String?,//qq
            @Field("email") email: String?,//邮箱
            @Field("provinceId") provinceId: Int,//省id*
            @Field("cityId") cityId: Int,//市id*
            @Field("districtId") districtId: Int,//区id*
            @Field("capitalId") capitalId: Int,//创业资本*
            @Field("industryOfInterest") industryOfInterest: String,//感兴趣行业*
            @Field("wxUid") wxUid: Int,//完信uId*
            @Header("ACCESS-TOKEN") token: String//token*
    ): Observable<BaseBean<MySettingsUserBean>>

    //设置密码
    @GET("app/user/setting_password")
    fun setPass(@Query("name") name: String, @Query("password") password: String, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<Any>>

    //修改密码
    @POST("app/user/update_password")
    @FormUrlEncoded
    fun updatePass(@Field("password") password: String, @Field("newPassword") newPassword: String, @Field("verifyPassword") verifyPassword: String, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<Any>>

    //换绑手机
    @GET("app/user/exchange_Phone")
    fun updatePhone(@Query("newPhone") newPhone: String, @Query("smsCode") smsCode: String, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<Any>>

    //忘记密码
    @POST("app/user/forget_password")
    fun forgetpassword(@Query("phone") phone: String, @Query("smsCode") msmCode: String, @Query("password") password: String, @Query("notarizePassword") notarizePassword: String): Observable<BaseBean<UserBean>>

    //获取oss令牌
    @GET("https://oss.ilashou.com/oss/authorization_app?name=mm")
    fun getOssToken(): Observable<BaseBean<OssDataBean>>

    //我的反馈
    @POST("/api/feedback/add_feedback")
    fun feedback(@Header("ACCESS-TOKEN") token: String, @Query("brandId") brandId: Int,
                 @Query("type") type: Int, @Query("content") content: String,
                 @Query("urlList") urlList: String): Observable<BaseBean<Any>>

    //获取热门词汇
    @GET("api/join/hot_search")
    fun get_hot_search(@Header("VERSION") version: String): Observable<BaseBean<HotSearchBean>>

    //爱加盟首页搜索
    @POST("api/join/get_search_brands")
    @FormUrlEncoded
    fun join_search_brands(@Field("fatherId") fatherId: Int?,//餐饮类型父ID
                           @Field("typeId") typeId: Int?,//餐饮类型ID
                           @Field("cityIds") cityIds: String?,//爱加盟区域ID
                           @Field("capitalIds") capitalIds: String?,//投资金额ID
                           @Field("modeIds") modeIds: String?,//加盟模式ID
                           @Field("integratedSortId") integratedSortId: Int?,//综合排序（12345）
                           @Field("pageNum") pageNum: Int)//页数1页10条
            : Observable<BaseBean<SeachResult>>

    @POST("api/join/get_search_brands")
    @FormUrlEncoded
    fun join_search_brands(@Field("keyWord") keyWord: String?,//搜索关键字
                           @Field("cityIds") cityIds: String?,//爱加盟区域ID
                           @Field("capitalIds") capitalIds: String?,//投资金额ID
                           @Field("modeIds") modeIds: String?,//加盟模式ID
                           @Field("integratedSortId") integratedSortId: Int?,//综合排序（12345）
                           @Field("pageNum") pageNum: Int)//页数1页10条
            : Observable<BaseBean<SeachResult>>

    //我的关注
    @GET("api/my_attention/get_attention")
    fun myFollow(@Query("pageNum") pageNum: Int, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<MyMyFollowBean>>

    //取消我的关注
    @GET("api/my_attention/un_subscribe")
    fun deleteMyFollow(@Query("brandId") brandId: Int, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<Any>>

    //企业入驻
    @GET("api/my_company/company_page")
    fun myEnterpriseEntry(): Observable<BaseBean<Any>>

    //我的留言
    @GET("api/my_comment/get_comments")
    fun myLeavingMessage(@Query("pageNum") pageNum: Int, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<MyMyLeavingMessageBean>>

    //删除我的留言
    @GET("api/my_comment/del_comment")
    fun deleteMyLeavingMessage(@Query("commentId") commentId: Int, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<Any>>

    //我的足迹
    @GET("api/my_footprint/get_footprint")
    fun myFootprint(@Query("pageNum") pageNum: Int, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<MyMyFollowBean>>

    //删除我的足迹
    @GET("api/my_footprint/del_footprint")
    fun deleteMyFootprint(@Query("brandId") brandId: Int, @Header("ACCESS-TOKEN") token: String): Observable<BaseBean<Any>>

    //第三方绑定状态查询
    @GET("app/user/is_binding")
    fun threeBindingState(@Header("ACCESS-TOKEN") token: String): Observable<BaseBean<ThreeBindingBean>>

    //第三方绑定
    @POST("app/user/third_party_binding")
    @FormUrlEncoded
    fun threeBinding(@Field("type") type: Int, @Field("openId") openId: String, @Field("token") token: String, @Field("weChatUnionId") weChatUnionId: String, @Header("ACCESS-TOKEN") accessToken: String): Observable<BaseBean<Any>>

    //第三方解绑绑定 1.QQ 2.微信
    @GET("app/user/third_party_unbind")
    fun unThreeBinding(@Query("type") type: Int, @Header("ACCESS-TOKEN") accessToken: String): Observable<BaseBean<Any>>

    //获取系统消息
    @GET("api/chat/chat_Info")
    fun getMyMessage(): Observable<BaseBean<MyMessageBean>>

    //获取头报文章列表
    @GET("api/newspaper/article_list")
    fun getNewsHeadList(@Query("pageNum") pageNum: Int): Observable<BaseBean<NewsPagerListBean>>

    //红铺列表
    @GET("api/shop/popular_brands")
    fun getRedShopRight(@Query("type") type: Long, @Header("VERSION") version: String): Observable<BaseBean<RedShopLeftListBean>>

    //筛选栏投资金额
    @GET("api/get_capital")
    fun getSeachConditionMoney(): Observable<BaseBean<SeachConditionMoneyBean>>

    //筛选栏加盟模式
    @GET("api/get_mode")
    fun getSeachConditionJoinModel(): Observable<BaseBean<SeachConditionBean>>

    //筛选栏加盟区域
    @GET("api/get_municipality")
    fun getSeachJoinArea(@Header("VERSION") version: String): Observable<BaseBean<SeachJoinAreaBean>>

    //筛选栏餐饮类型
    @GET("api/get_food_filter")
    fun getSeachFoodType(@Header("VERSION") version: String): Observable<BaseBean<SeachFoodTypeBean>>

    //微信登录
    @POST("https://api.weixin.qq.com/sns/oauth2/access_token")
    fun getWeChatToken(@Query("appid") appid: String, @Query("secret") secret: String,
                       @Query("code") code: String, @Query("grant_type") grant_type: String = "authorization_code"): Observable<WxTokenBean>

    @POST("https://api.weixin.qq.com/sns/userinfo")
    fun getWeChatInfo(@Query("access_token") access_token: String, @Query("openid") openid: String): Observable<WxInfoBean>

    //第三方登录   type 1.QQ 2.微信
    @POST("app/user/third_party_login")
    fun loginThree(@Query("openId") openId: String,@Query("unionId") unionId: String, @Query("type") type: Int, @Query("deviceType") deviceType: Int): Observable<BaseBean<UserBean>>

    //分享获取信息
    @GET("api/share/share_information")
    fun getShareMessage(@Header("ACCESS-TOKEN") access_token: String, @Query("type") type: Int, @Query("id") id: Int): Observable<BaseBean<ShareBean>>

    //获取新浪
    @GET("https://api.weibo.com/2/users/show.json")
    fun getSinaInfo(@Query("access_token") access_token: String, @Query("uid") uid: String): Observable<SinaUserBean>

    //撤回删除消息 type 1撤回 2删除
    @POST("/api/msg_op/msg")
    @FormUrlEncoded
    fun msgRevokeDelete(@Field("wxFromId") wxUserName: Int, @Field("wxToId") wxPassWord: Int, @Field("wxMsgId") wxProjectId: Int, @Field("type") type: Int): Observable<BaseBean<Any>>

    //敏感词过滤
    @POST("api/sensitive_word")
    @FormUrlEncoded
    fun sensitiveWord(@Field("filterBefore") filterBefore: String): Observable<WordBean>

}