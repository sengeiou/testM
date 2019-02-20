package com.qingmeng.mengmeng.utils

import java.util.regex.Pattern

/**
 *  Description :输入格式验证

 *  Author:yang

 *  Email:1318392199@qq.com

 *  Date: 2019/1/17
 */
object InputCheckUtils {
    /**
     * 密码 6到16位区分大小写
     */
    fun checkPass6_16(pass: String): Boolean {
        val pattern = Pattern.compile("^[a-zA-Z0-9]{6,12}$")
        val matcher = pattern.matcher(pass)
        return matcher.matches()
    }

    /**
     * 任意字符 4到16位
     */
    fun checkString4_6(str: String): Boolean {
        return str.length in 4..16
    }

    /**
     * 手机号
     */
    fun checkPhone(phone: String): Boolean {
        val pattern = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$")
        val matcher = pattern.matcher(phone)
        return matcher.matches()
    }

    /**
     * 邮箱
     */
    fun checkEmail(email: String): Boolean {
        return email.matches("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*".toRegex())
    }

    /**
     * QQ 5到12位数字 不能以0开头
     */
    fun checkQQ(qq: String): Boolean {
        val pattern = Pattern.compile("^[1-9][0-9]{4,11}")
        val matcher = pattern.matcher(qq)
        return matcher.matches()
    }

    /**
     * 微信 6至20位字母 数字 下划线和减号（也可以是手机号）
     */
    fun checkWechat(wechat: String): Boolean {
        if (checkPhone(wechat)) {
            return true
        } else {
            val pattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]{5,19}$")
            val matcher = pattern.matcher(wechat)
            return matcher.matches()
        }
    }

    /**
     * 固定电话
     */
    fun checkTel(tel: String): Boolean {
//        val pattern = Pattern.compile("^0(10|2[0-5789]-|//d{3})-?//d{7,8}$")
        val pattern = Pattern.compile("^1\\d{10}\$|^(0\\d{2,3}-?|\\(0\\d{2,3}\\))?[1-9]\\d{4,7}(-\\d{1,8})?$")
        val matcher = pattern.matcher(tel)
        return matcher.matches()
    }

    /**
     * 5-6位数字
     */
    fun checkFiveOrSixNum(qq: String): Boolean {
        val pattern = Pattern.compile("^[0-9]{5,6}")
        val matcher = pattern.matcher(qq)
        return matcher.matches()
    }
}
