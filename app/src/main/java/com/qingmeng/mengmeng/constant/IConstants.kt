package com.qingmeng.mengmeng.constant

import com.qingmeng.mengmeng.BuildConfig

/**
 * Created by zq on 2018/8/13
 */
object IConstants {
    val BASE_URL = BuildConfig.SERVER_IP
    val ERROR_MSG = "网络获取失败"
    val USER = "userBean"
    val HOME_PAGE = "homePageBean"
    val LOGIN_TIME = "loginTime"
    val LOGIN_PHONE = "loginPhone"
    val LOGIN_TYPE = "loginType"
    val LOGIN_PSW = "loginPsw"
    val FIRSTLOGIN = "first_login"

    val RESULT_CODE_TAKE_CAMERA = 101    //拍照
    val RESULT_CODE_OPEN_ALBUM = 102     //打开相册
    val TEST_ACCESS_TOKEN = "233:0w4R281R7075d6q8Va688N718979ea48O2c8D2yQrF483j465BT2a86J964ezM3F"   //模拟token

    val GET_IMAGE_CODE = BASE_URL + "api/captcha_app/image_app?account="
}