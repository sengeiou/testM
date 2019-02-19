package com.lemo.emojcenter.utils.download

/**
 *  Description :

 *  Author:fx

 *  Date: 2018/6/7
 */
class FaceNotNullUtils {
    fun <T1, T2> ifNotNull(value1: T1?, value2: T2?, bothNotNull: (T1, T2) -> (Unit)) {
        if (value1 != null && value2 != null) {
            bothNotNull(value1, value2)
        }
    }

    private fun <T1, T2> bothNotNull(value1: T1, value2: T2) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}