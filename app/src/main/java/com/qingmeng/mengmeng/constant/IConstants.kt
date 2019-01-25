package com.qingmeng.mengmeng.constant

import com.qingmeng.mengmeng.BuildConfig

/**
 * Created by zq on 2018/8/13
 */
object IConstants {
    val BASE_URL = BuildConfig.SERVER_IP
    val ERROR_MSG = "网络获取失败"
    val USER = "userBean"
    val JOIN_RECOMMEND = "joinRecommend"
    val HOME_PAGE = "homePageBean"
    val LOGIN_TIME = "loginTime"
    val LOGIN_PHONE = "loginPhone"
    val LOGIN_TYPE = "loginType"
    val LOGIN_PSW = "loginPsw"
    val FIRSTLOGIN = "first_login"

    val RESULT_CODE_TAKE_CAMERA = 101    //拍照
    val RESULT_CODE_OPEN_ALBUM = 102     //打开相册
    val TEST_ACCESS_TOKEN = "233:3qN4hC319LG23vu0rtXZfZT9Id69K26Gix8Pq459M1870ND6vu7pAA16tX5SSZ9Z"   //模拟token

    val GET_IMAGE_CODE = BASE_URL + "api/captcha_app/image_app?account="
}