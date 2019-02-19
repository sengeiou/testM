package com.lemo.emojcenter.constant

/**
 * Created by wangru
 * Date: 2018/4/3  13:36
 * mail: 1902065822@qq.com
 * describe:
 */

interface FaceNetConstant {
    //0 已下载 1.未下载
    interface IsDown {
        companion object {
            val DOWN = 0
            val DOWN_NOT = 1
        }
    }

    interface CollectType {
        companion object {
            val ADD = 1
            val DEFAULT = 2
        }
    }
}
