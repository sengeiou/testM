package com.lemo.emojcenter.bean

/**
 * Description :
 *
 *
 * Author:fangxu
 *
 *
 * Email:634804858@qq.com
 *
 *
 * Date: 2018/2/5
 */

interface OssIconstants {
    companion object {

        //    String OSS_SIZE_FOODS_ITEM = "@!s100_100";//添加表情页面条目,这种规格需要后台自定义
        val OSS_SIZE_FOODS_ITEM = "?x-oss-process=image/resize,m_lfit,h_100,w_100/format,png"//添加表情页面条目
        val OSS_SIZE_EMOJ_ITEM = "?x-oss-process=image/resize,m_lfit,h_125,w_125/format,png"
    }
}
