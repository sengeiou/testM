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
    val JOIN_RECOMMEND = "joinRecommend"
    val RED_SHOP = "redShop"
    val NEWS_PAGER = "NewsPager"
    val SEACH_RESULT = "seachResult"
    val HOME_PAGE = "homePageBean"
    val LOGIN_TIME = "loginTime"
    val LOGIN_PHONE = "loginPhone"
    val LOGIN_TYPE = "loginType"
    val LOGIN_PSW = "loginPsw"
    val FIRSTLOGIN = "first_login"
    val BRANDID = "brandId"
    val POSITION = "position"
    val articleUrl = "articleUrl"
    val IMGS = "imgs"
    val detailUrl = "detailUrl"
    val title = "title"

    val GET_IMAGE_CODE = BASE_URL + "api/captcha_app/image_app?account="

    /**
     * ==============================路径==============================
     */
    const val ROOT_NAME = BuildConfig.APP_DIR
    //语音路径
    val DIR_AUDIO_STR = StorageUtils.getPublicStorageDir("${ROOT_NAME}/audio")
}