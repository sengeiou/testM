package com.mogujie.tt.api

/**
 * Created by wangru
 * Date: 2018/6/30  14:29
 * mail: 1902065822@qq.com
 * describe:
 */

object CodeError {
    fun isSuc(code: Int): Boolean =
            code == SUC

    /*    成功    */
    val SUC = 12000
}
