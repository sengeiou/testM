package com.qingmeng.mengmeng.entity

import java.io.Serializable

/**
 *  Description :

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/7
 */
class BaseBean<T> : Serializable {
    var code = 0
    var msg = ""
    var data: T? = null
}