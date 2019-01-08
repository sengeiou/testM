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
}