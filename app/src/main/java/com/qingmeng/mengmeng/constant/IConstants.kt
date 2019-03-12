package com.qingmeng.mengmeng.constant

import com.qingmeng.mengmeng.BuildConfig
import com.qingmeng.mengmeng.utils.photo.StorageUtils

/**
 * Created by zq on 2018/8/13
 */
object IConstants {
    val BASE_URL = BuildConfig.SERVER_IP
    val ERROR_MSG = "网络获取失败"
    val USER = "userBean"
    val WANXIN_USER = "wanxinUserBean"
    val JOIN_RECOMMEND = "joinRecommend"       //首页推荐缓存
    val RED_SHOP = "redShop"                   //红铺缓存
    val NEWS_PAGER = "newsPager"               //头报缓存
    val SEACH_RESULT = "seachResult"           //搜索结果
    val SEACH_RESULT_CACHE = "seachResultCache"           //搜索结果缓存
    val SEACH_RESULT_FOOD = "seachResultFood"  //搜索结果餐饮类型缓存
    val SEACH_RESULT_AREA = "seachResultArea"  //搜索结果加盟区域缓存
    val HOME_PAGE = "homePageBean"
    val LOGIN_TIME = "loginTime"
    val LOGIN_PHONE = "loginPhone"
    val LOGIN_PSW = "loginPsw"
    val LOGIN_TYPE = "loginType"               //登录类型：0：账号密码登录  1：短信验证登录
    val TYPE = "type"                           //登录类型：0：账号密码登录  1：短信验证登录
    val FROM_TYPE = "from"                     //登录类型：0：其他  1：商品详情
    val THREE_OPENID = "openId"               //QQopenid 或 微信openid
    val THREE_TOKEN = "token"                 //QQtoken 或 微信token
    val WE_CHAT_UNIONID = "weChatUnionId"   //微信UnionId
    val THIRD_USERNAME = "thirdUserName"   //微信UnionId
    val AVATAR = "avatar"                     //头像
    val THREE_TYPE = "threeType"             //登录类型：1:QQ 2微信
    val FIRSTLOGIN = "first_login"
    val BRANDID = "brandId"                  //商品id
    val REDSHOPID = "red_shop_ID"            //红铺id
    val REDSHOPFATHERID = "red_shop_fatherId"            //红铺名称
    val POSITION = "position"
    val articleUrl = "articleUrl"
    val articleId = "articleId"          //头报文章id
    val IMGS = "imgs"
    val detailUrl = "detailUrl"           //详情链接
    val title = "title"                   //标题
    val firstLevel = "firstLevel"         //一级分类id
    val secondLevel = "secondLevel"       //二级分类id
    val THREE_LEVEL = "threeLevel"        //展示名称
    val BACK_SEACH = "backSeach"          //回显关键字
    val LOGIN_BACK = 10010
    var login_name = "mengUser"           //账号密码登录时保存账号
    var login_phone = "mengPhone"         //保存手机号
    var login_pwd = "mengPass"            //账号密码登录时保存密码
    val TO_MESSAGE = 10055                //页面进聊天
    var MESSAGE_BACK_BRAND_ID = ""        //聊天界面返回的品牌详情id
    var ENTER_BRAND_NUM = 0               //第几次进详情
    var MY_TO_MESSAGE = "myToMessage"     //是从我的板块进的消息列表->消息的
    var MYFRAGMENT_TO_MESSAGE = false     //我的板块->消息列表
    var MESSAGE_TO_CHAT = false           //消息列表->聊天
    //图片验证码
    val GET_IMAGE_CODE = BASE_URL + "api/captcha_app/image_app?account="

    /**
     * ==============================路径==============================
     */
    const val ROOT_NAME = BuildConfig.APP_DIR
    //语音路径
    val DIR_AUDIO_STR = StorageUtils.getPublicStorageDir("${ROOT_NAME}/audio")

    //微信
    val APPID_WECHAT = "wx9eaf08161137b52e"
    val SECRET_WECHAT = "90f41ca172fbdeb216e1a03044b3ca27"
    //QQ
    val APP_ID_QQ = "1106680659"
    val APP_KEY_QQ = "bwh1Tr7jyhDZqtYU"
}