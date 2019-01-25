package com.app.common.extensions

/**
 * Created by wr
 * Date: 2018/11/9  14:45
 * describe:
 */


fun String?.equalsExt(str: String?, isIgnoerNull: Boolean = true) = this == str || (isIgnoerNull && (this == null && str == "") || (this == "" && str == null))

fun String?.equalsNotExt(str: String?, isIgnoerNull: Boolean = true) = !equalsExt(str, isIgnoerNull)