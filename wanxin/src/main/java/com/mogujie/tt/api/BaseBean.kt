package com.mogujie.tt.api

import java.io.Serializable

/**
 * Created by wr
 * Date: 2019/1/2  13:22
 * mail: 1902065822@qq.com
 * describe:
*/
class BaseBean<T> : Serializable {
    var code: Int = 0
    var msg: String = ""
    var data: T? = null
}