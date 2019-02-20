package com.lemo.emojcenter.constant

import com.lemo.emojcenter.FaceConfigInfo

/**
 * Description :接口地址
 *
 *
 * Author:fangxu
 *
 *
 * Email:634804858@qq.com
 *
 *
 * Date: 2018/1/27
 */

interface FaceIConstants {
    companion object {
        const val APP_KEY = "APP-KEY"

        //线下
        const val HOME_URL_DEBUG = "http://bq.pingpingapp.com"//线下地址2
        const val APP_KEY_VALUE_DEBUG = "gVTXMOWz"
        //线上
        const val HOME_URL_RELEASE = "http://bq.pingpingapp.com"
        const val APP_KEY_VALUE_RELEASE = "gVTXMOWz"

        //关于线程池的一些配置
        val CPU_COUNT = Runtime.getRuntime().availableProcessors()
        val CORE_POOL_SIZE = Math.max(3, CPU_COUNT / 2)
        val MAX_POOL_SIZE = CORE_POOL_SIZE * 2
        val KEEP_ALIVE_TIME = 0L


        /**
         * 收藏的表情
         */
        val COLLECT_EMOJ = FaceConfigInfo.homeUrl + "/api/v1/collect/list"//收藏的表情
        val COLLECT_EMOJ_UP = FaceConfigInfo.homeUrl + "/api/v1/collect/save"//收藏的表情_上传
        val COLLLECT_EMOJ_SORT = FaceConfigInfo.homeUrl + "/api/v1/collect/update"//收藏的表情_排序
        val COLLLECT_EMOJ_DELETE = FaceConfigInfo.homeUrl + "/api/v1/collect/delete"//收藏的表情_删除
        /**
         * 精选表情
         */

        val RECOMMEND_EMOJ = FaceConfigInfo.homeUrl + "/api/v1/chosen/page"//推荐表情
        val EMOJ_DETAIL = FaceConfigInfo.homeUrl + "/api/v1/detail/page"//表情包详情
        val BANNER = FaceConfigInfo.homeUrl + "/api/v1/banner/list"//精选表情页面banner图
        /**
         * 设置,我的表情相关
         */
        val MY_EMOJ = FaceConfigInfo.homeUrl + "/api/v1/history/page"//我的表情,我的下载记录
        val REMOVE_MY_EMOJ = FaceConfigInfo.homeUrl + "/api/v1/history/remove"//移除我的表情
        /**
         * 上传图片获取临时权限
         */
        val GET_OSS_PERMISSION = FaceConfigInfo.homeUrl + "/upload/getsts/write"
        val GETUPLOADPERMISSION = FaceConfigInfo.homeUrl + "/upload/getsts/read"
        //    String GET_OSS_PERMISSION=ConfigInfo.getHomeUrl()+"/upload/get";//获取oss权限测试账号链接
        val UPLOAD = FaceConfigInfo.homeUrl + "/upload/getsts/getsignurl"//生成签名URL接口
        val DOWNLOADFACE = FaceConfigInfo.homeUrl + "/api/v1/history/downloadface"//下载表情接口
        val DETAIL = FaceConfigInfo.homeUrl + "/api/v1/detail/getdetails"//表情详情-根据id查询
        val HOSTFACE = FaceConfigInfo.homeUrl + "/api/v1/face/get"//用户已下载过的表情和收藏的表情
        val ALL_EMOJ = FaceConfigInfo.homeUrl + "/api/v1/face/getall"//所有表情
    }

}
