package com.lemo.emojcenter.utils

import android.annotation.SuppressLint
import android.text.TextUtils
import android.text.format.Time
import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtil {
    private val TAG = "DateUtil"

    /**
     * 获取当前系统时间的时间精确的毫秒
     *
     * @author andy_liu
     */
    val loginTime: Long
        get() {

            val sdf = SimpleDateFormat("yyyyMMddHHmmssSSS")
            val calendar = Calendar.getInstance()
            val str = sdf.format(calendar.time)

            try {
                return sdf.parse(str).time
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return 0L
        }

    //发布的时候选择时间对话框
    val loginTimeForFabu: Long
        get() {

            val sdf = SimpleDateFormat("yyyyMMdd")
            val calendar = Calendar.getInstance()
            val str = sdf.format(calendar.time)

            try {
                return sdf.parse(str).time
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return 0L
        }

    /**
     * 获取当前日期起的一周内时间
     *
     * @return
     */

    val requestDate: Array<String?>
        get() {
            val dates = arrayOfNulls<String>(7)
            val format = SimpleDateFormat("yyyy.MM.dd")
            val calendar = Calendar.getInstance()
            val dateWeeks = arrayOfNulls<String>(7)

            calendar.add(Calendar.DATE, 0)
            dates[0] = format.format(calendar.time)
            dateWeeks[0] = dates[0] + "," + getWeekday(dates[0], 1)
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1)
            dates[1] = format.format(calendar.time)
            dateWeeks[1] = dates[1] + "," + getWeekday(dates[1], 1)
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1)
            dates[2] = format.format(calendar.time)
            dateWeeks[2] = dates[2] + "," + getWeekday(dates[2], 1)
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1)
            dates[3] = format.format(calendar.time)
            dateWeeks[3] = dates[3] + "," + getWeekday(dates[3], 1)
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1)
            dates[4] = format.format(calendar.time)
            dateWeeks[4] = dates[4] + "," + getWeekday(dates[4], 1)
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1)
            dates[5] = format.format(calendar.time)
            dateWeeks[5] = dates[5] + "," + getWeekday(dates[5], 1)
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1)
            dates[6] = format.format(calendar.time)
            dateWeeks[6] = dates[6] + "," + getWeekday(dates[6], 1)
            return dateWeeks

        }


    //寅、卯、辰、巳、午、未、申、酉、戌、亥
    private val terrestrialBranch = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")

    //计算预产期是否设置正确
    val isDueDate: Boolean
        get() = false

    /**
     * 毫秒值转化为时间
     *
     * @param time
     * @return
     */
    fun getDateUtil(time: Long): String {
        val d = Date(time)
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(d)
    }

    /**
     * 毫秒值转化为日期到分钟
     *
     * @param time
     * @return
     */
    fun getDateMUtil(time: Long): String {
        val d = Date(time)
        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
        return sdf.format(d)
    }

    /**
     * 毫秒值转化为日期到秒
     *
     * @param time
     * @return
     */
    fun getDateMUtilToFolder(time: Long): String {
        val d = Date(time)
        val sdf = SimpleDateFormat("yyyyMMddHHmmss")
        return sdf.format(d)
    }

    /***
     * 时间转化为毫秒值
     *
     * @param date
     * @return
     */
    fun getDate2mis(date: String): Long {
        val sdf = SimpleDateFormat("yyyy.MM.dd")
        try {
            return sdf.parse(date).time
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return 0L
    }

    /**
     * 将日期转化为2012-08-09格式的工具类
     *
     * @param d
     * @return
     * @author andy_liu
     */
    fun dataUtil(d: String?): String {
        if (d == null) {
            return ""
        }
        val str = SimpleDateFormat("yyyy.MM")
        var dataString = ""
        try {
            val date = str.parse(d)
            dataString = str.format(date)
            return str.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return dataString
    }

    /**
     * 获取当前系统时间的工具类
     *
     * @author andy_liu
     */
    fun time(): String {
        val date = Date()
        val format = SimpleDateFormat("yyyy.MM")
        return format.format(date)
    }

    /**
     * 获取当前系统时间的工具类
     *
     * @author andy_liu
     */
    //	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
    //	System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
    fun timeH(): String {
        val date = Date()
        val format = SimpleDateFormat("HH")
        return format.format(date)
    }

    fun timeM(): String {
        val date = Date()
        val format = SimpleDateFormat("mm")
        return format.format(date)
    }

    /**
     * 获取当前系统时间的工具类
     *
     * @author andy_liu
     */
    fun timeSS(): String {

        val format = SimpleDateFormat("yyyy.MM.dd")
        val calendar = Calendar.getInstance()
        return format.format(calendar.time)
    }

    fun sStime(): String {

        val format = SimpleDateFormat("yyyy年MM月dd日")
        val calendar = Calendar.getInstance()
        return format.format(calendar.time)
    }

    fun dateYear(d: String): String {
        val str = SimpleDateFormat("yyyy")
        var dataString = ""
        try {
            val date = str.parse(d)
            dataString = str.format(date)
            return str.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return dataString
    }

    /**
     * 生日转年龄
     *
     * @param birthday
     * @return
     */
    fun birthdayToAge(birthday: String?): String {
        if (birthday == null) {
            return ""
        }
        var age = 0
        try {
            // 2015-08-07
            val year = birthday.substring(0, 4)
            val month = birthday.substring(5, 7)
            val day: String
            if (birthday.length > 9) {
                day = birthday.substring(8, 10)
            } else {
                day = "01"
            }
            val c = Calendar.getInstance()
            val currYear = c.get(Calendar.YEAR)
            val currMonth = c.get(Calendar.MONTH)
            val currDay = c.get(Calendar.DAY_OF_MONTH)
            age = currYear - Integer.parseInt(year)
            if (currMonth < Integer.parseInt(month)) {
                age--
            } else if (currMonth == Integer.parseInt(month)) {
                if (currDay < Integer.parseInt(day)) {
                    age--
                }
            }

        } catch (e: Exception) {

        }

        return if (age == 0) {
            ""
        } else age.toString()

    }

    /**
     * 日期转时长
     *
     * @param time
     * @return
     */

    fun timeToDurction(time: String?): String {
        var str = "未知"

        if (time == null) {
            return str
        }
        val date: Date
        try {
            date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time)
            var ducation = (System.currentTimeMillis() - date.time) / 1000
            if (ducation < 60) {
                if (ducation < 0) {
                    ducation = 0
                }
                str = ducation.toString() + "秒前发布"
            } else if (ducation < 60 * 60) {
                str = (ducation / 60).toString() + "分钟前发布"
            } else if (ducation < 23 * 60 * 60) {
                str = (ducation / 60 / 60).toString() + "小时前发布"
            } else {
                str = time.substring(0, 10)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return str
    }

    /**
     * 将日期转换成毫秒
     *
     * @param time 日期格式 2015-08-12 14:31:26
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    fun transform(time: String): String {
        var str = "未知"
        val date: Date
        try {
            date = SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(time)
            val ducation = System.currentTimeMillis() - date.time
            if (ducation < 60 * 60 * 1000) {
                str = (ducation / 60 / 60).toString() + "分钟"
            } else if (ducation < 23 * 60 * 60 * 1000) {
                str = (ducation / 60 / 60).toString() + "小时"
            } else {
                str = time.substring(0, 10)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return str
    }

    /**
     * 距离上次登录时间
     *
     * @param time
     * @return
     */
    fun timeLastLoginDurction(time: String?): String {
        var str = "未知"

        if (time == null) {
            return str
        }
        val date: Date
        try {
            date = SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(time)
            val ducation = (System.currentTimeMillis() - date.time) / 1000
            if (ducation < 60 * 60) {
                str = (ducation / 60).toString() + "分钟前登录"
            } else if (ducation < 23 * 60 * 60) {
                str = (ducation / 60 / 60).toString() + "小时前登录"
            } else {
                str = time.substring(0, 10)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return str
    }

    /**
     * 检测date1比date2小
     *
     * @param date1 "yyyy-MM-dd"或"yyyy-MM"
     * @return
     * @throws ParseException
     */
    @Throws(ParseException::class)
    fun checkedIsSmall(date1: String, date2: String?): Boolean {
        var date1 = date1
        var date2 = date2
        if (date2 != null && date2 == "至今") {
            return true
        }

        if (date1.length < 10) {
            date1 += "-01"
        }
        val l1 = SimpleDateFormat("yyyy-MM-dd").parse(date1).time
        val l2: Long
        if (date2 == null || date2.isEmpty()) {
            l2 = System.currentTimeMillis()
        } else {
            if (date2.length < 10) {
                date2 += "-01"
            }
            l2 = SimpleDateFormat("yyyy-MM-dd").parse(date2).time
        }
        return if (l1 <= l2) {
            true
        } else false

    }

    /**
     * 检验年龄是否有18周岁
     *
     * @param birthday
     * @return
     */
    fun birthdayChecked(birthday: String): Boolean {
        val age = birthdayToAge(birthday)
        return if (age.isEmpty() || Integer.parseInt(age) < 18) {

            false
        } else true
    }

    /**
     * 检测date1比date2大
     *
     * @return
     * @throws ParseException
     */
    @Throws(ParseException::class)
    fun checkedIsLarge(date1: String, date2: String?): Boolean {
        var date1 = date1
        var date2 = date2
        if (date1.length < 10) {
            date1 += "-01"
        }
        val l1 = SimpleDateFormat("yyyy-MM-dd").parse(date1).time
        val l2: Long
        if (date2 == null || date2.isEmpty()) {
            l2 = System.currentTimeMillis()
        } else {
            if (date2.length < 10) {
                date2 += "-01"
            }
            l2 = SimpleDateFormat("yyyy-MM-dd").parse(date2).time
        }
        return if (l1 >= l2) {
            true
        } else false
    }

    /**
     * 判断日期是周几的方法
     *
     * @param type 0{星期} 其他数字建议1{周一}
     */
    fun getWeekday(date: String?, type: Int): String {
        val strs = ArrayList<String>()
        if (date == null) {
            return ""
        }
        var start = 0
        var end = 0
        for (i in 0 until date.length) {
            if (date[i] == '.' || date[i] == '-') {
                start = end
                end = i
                strs.add(date.substring(start, end++))
            }
        }
        strs.add(date.substring(end, date.length).trim { it <= ' ' })
        val calendar = Calendar.getInstance()
        calendar.set(Integer.valueOf(strs[0])!!,
                Integer.valueOf(strs[1])!! - 1, Integer.valueOf(strs[2])!!)
        val number = calendar.get(Calendar.DAY_OF_WEEK)// 星期表示1-7，是从星期日开始，
        if (type == 0) {
            val str = arrayOf("", "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
            return str[number]
        } else {
            val str = arrayOf("", "周日", "周一", "周二", "周三", "周四", "周五", "周六")
            return str[number]
        }

    }

    @Throws(ParseException::class)
    fun getEffiveDay(endDate: String?): Int {
        if (endDate == null) {
            return -1
        }
        val date = Date()
        val a = date.time
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val b = sdf.parse(endDate).time
        return ((b - a) / (1000 * 60 * 60 * 24)).toInt()
    }

    /**
     * 时间转分钟
     */
    fun timeToint(time: String): Int {
        val min: Int
        val str = time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        min = Integer.parseInt(str[0]) * 60 + Integer.parseInt(str[1])
        return min
    }

    /**
     * 毫秒转时间
     */
    fun sToString(l: Long?): String {

        var str = ""
        var sdf: SimpleDateFormat? = null
        val day = (24 * 3600 * 1000).toLong()
        val left = l!! % day
        val curtime = Calendar.getInstance().timeInMillis
        val dt = curtime - l
        // 用总时间%每天的时间=发布时间超过一天的秒数 用当前时间-发布时间=时间 差，，
        // 如果时间差<（一天的秒数-发布时间超过一天的秒数=一天剩余的秒数）则表示是当天发的，如果时间差》这个同时《一天
        // 加上超过的时间则表示 为昨天，其他情况则为日期

        if (dt <= day - left) {
            sdf = SimpleDateFormat("HH:mm")
        } else if (dt > day - left && dt < day + left) {
            str = "昨天"
            return str
        } else {
            sdf = SimpleDateFormat("yyyy.MM.dd")
        }

        str = sdf.format(Date(l))

        return str
    }

    /**
     * 分钟转时间
     */
    fun minToTime(start: Int, end: Int): String {
        var str = ""
        var time1 = ""
        var time2 = ""
        val sb = StringBuffer()
        if (start % 60 == 0) {
            time1 = sb.append((start / 60).toString() + ":" + start % 60 + "0").toString()
                    .toString()
        } else {
            time1 = sb.append((start / 60).toString() + ":" + start % 60).toString()
                    .toString()
        }

        val sb1 = StringBuffer()
        if (end % 60 == 0) {
            time2 = sb1.append((end / 60).toString() + ":" + end % 60 + "0").toString()
                    .toString()
        } else {
            time2 = sb1.append((end / 60).toString() + ":" + end % 60).toString().toString()
        }

        val sb3 = StringBuffer()
        str = sb3.append(time1 + "~" + time2).toString()

        return str
    }

    fun dateToAge(str: String): Int {
        var realAge = 0
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        try {
            val date = sdf.parse(str)
            val l = date.time
            val ca = Calendar.getInstance()
            val now = ca.timeInMillis
            realAge = ((now - l) / 3600 / 1000 / 24 / 365).toInt()
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return realAge
    }

    /**
     * 判断时间是否早于当前时间
     */
    fun judgeTime(date: String, time: String): Boolean {
        val flag = false
        var chooseTime: Long = 0
        val str = StringBuffer().append(date + " " + time).toString()
        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm")
        try {
            chooseTime = sdf.parse(str).time
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return if (chooseTime < System.currentTimeMillis()) {
            true
        } else flag

    }

    /***
     * 时间转化为毫秒值
     *
     * @param date
     * @return
     */
    fun getDate2longtime(date: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        try {
            return sdf.parse(date).time
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return 0L
    }

    fun getDateByString(time: String?): Date? {
        var date: Date? = null
        if (time == null) {
            return date
        }
        val date_format = "yyyy-MM-dd HH:mm:ss"
        val format = SimpleDateFormat(date_format)
        try {
            date = format.parse(time)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return date
    }

    //根据预产期-280天操作
    fun calculateDueDate1(dueDate: String): String {
        if (TextUtils.isEmpty(dueDate)) {
            return ""
        }
        val date = dueDate.replace("-", ".")
        val dueMis = DateUtil.getDate2mis(date)//预产期的毫秒值
        //280天qian 的毫秒值
        val beforeMis = dueMis - 24 * 60 * 60 * 1000 * 280L//1480953600000+24192000000
        return DateUtil.timestampToStr2(beforeMis)
    }

    //根据最近月经日期+280天操作
    fun calculateDueDate2(lastestMenstruationDay: String): String {
        if (TextUtils.isEmpty(lastestMenstruationDay)) {
            return ""
        }
        val date = lastestMenstruationDay.replace("-", ".")
        val dueMis = DateUtil.getDate2mis(date)//最近月经日期的毫秒值
        //280天后的毫秒值
        val futureMis = dueMis + 24 * 60 * 60 * 1000 * 280L//1480953600000+24192000000
        return DateUtil.timestampToStr2(futureMis)
    }

    // Timestamp转化为String:
    fun timestampToStr(dateline: Long): String {
        val timestamp = Timestamp(dateline * 1000)
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")// 定义格式，不显示毫秒
        return df.format(timestamp)
    }

    // Timestamp转化为String:
    fun timestampToStr2(dateline: Long): String {
        val timestamp = Timestamp(dateline)
        val df = SimpleDateFormat("yyyy-MM-dd")// 定义格式，不显示毫秒
        return df.format(timestamp)
    }

    // Timestamp转化为String:
    fun timestampToStr3(dateline: Long): String {
        val timestamp = Timestamp(dateline * 1000)
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm")// 定义格式，不显示毫秒
        return df.format(timestamp)
    }

    // Timestamp转化为String:
    fun timestampToStr4(dateline: Long): String {
        val timestamp = Timestamp(dateline * 1000)
        val df = SimpleDateFormat("yyyy-MM-dd")// 定义格式，不显示毫秒
        return df.format(timestamp)
    }


    /**
     * 得到几天前
     *
     * @param
     * @return
     */
    fun getDateDetail(times: String): String? {//123456784444
        val str = java.lang.Long.parseLong(times) / 1000
        var shortstring: String? = null
        val time = timestampToStr(str)
        val date = getDateByString(time) ?: return shortstring
        val formatTime = timestampToStr3(str)
        val now = Calendar.getInstance().timeInMillis
        val deltime = (now - date.time) / 1000
        if (deltime > 7 * 24 * 60 * 60) {
            shortstring = formatTime
        } else if (deltime > 24 * 60 * 60) {
            shortstring = (deltime / (24 * 60 * 60)).toInt().toString() + "天前"
        } else if (deltime > 60 * 60) {
            shortstring = (deltime / (60 * 60)).toInt().toString() + "小时前"
        } else if (deltime > 60) {
            shortstring = (deltime / 60).toInt().toString() + "分钟前"
        } else {
            shortstring = "刚刚"
        }
        return shortstring
    }

    fun getYearMonth(date: String?): String {
        if (date == null) {
            return "2016-10"
        }
        var length = date.length
        for (i in length - 1 downTo 0) {
            if (date[i] == '.' || date[i] == '-') {
                length = i
                return date.substring(0, length)
            }
        }
        return "日期格式有误！系统错误"
    }

    fun getDay(date: String?): String {
        if (date == null) {
            return "11"
        }
        val length = date.length
        for (i in length - 1 downTo 0) {
            if (date[i] == '.' || date[i] == '-') {
                return date.substring(i + 1, length)
            }
        }
        return "日期格式有误！系统错误"
    }

    /**
     * 获取星期信息
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    fun getDayOfWeek(year: Int, month: Int, day: Int): String {
        val c = Calendar.getInstance()
        c.set(year, month, day)
        return getDayOfWeek(c)
    }

    /**
     * 获取星期信息
     *
     * @param c
     * @return
     */
    fun getDayOfWeek(c: Calendar): String {
        return getDayOfWeek(c.get(Calendar.DAY_OF_WEEK))
    }

    /**
     * 根据Calerdar的day_of_week取得的数字进行转换
     *
     * @param dayNumber
     * @return
     */
    fun getDayOfWeek(dayNumber: Int): String {
        when (dayNumber) {
            1 -> return "星期天"
            2 -> return "星期一"
            3 -> return "星期二"
            4 -> return "星期三"
            5 -> return "星期四"
            6 -> return "星期五"
            7 -> return "星期六"
            else -> {
            }
        }
        return ""
    }

    /**
     * 判断当前系统时间是否在指定时间的范围内
     *
     * @param beginHour 开始小时，例如22
     * @param beginMin  开始小时的分钟数，例如30
     * @param endHour   结束小时，例如 8
     * @param endMin    结束小时的分钟数，例如0
     * @return true表示在范围内，否则false
     */
    fun isCurrentInTimeScope(beginHour: Int, beginMin: Int, endHour: Int, endMin: Int): Boolean {
        var result = false
        val aDayInMillis = (1000 * 60 * 60 * 24).toLong()
        val currentTimeMillis = System.currentTimeMillis()

        val now = Time()
        now.set(currentTimeMillis)

        val startTime = Time()
        startTime.set(currentTimeMillis)
        startTime.hour = beginHour
        startTime.minute = beginMin

        val endTime = Time()
        endTime.set(currentTimeMillis)
        endTime.hour = endHour
        endTime.minute = endMin

        if (!startTime.before(endTime)) {
            // 跨天的特殊情况（比如22:00-8:00）
            startTime.set(startTime.toMillis(true) - aDayInMillis)
            result = !now.before(startTime) && !now.after(endTime) // startTime <= now <= endTime
            val startTimeInThisDay = Time()
            startTimeInThisDay.set(startTime.toMillis(true) + aDayInMillis)
            if (!now.before(startTimeInThisDay)) {
                result = true
            }
        } else {
            // 普通情况(比如 8:00 - 14:00)
            result = !now.before(startTime) && !now.after(endTime) // startTime <= now <= endTime
        }
        return result
    }

    fun getTerrestrialBranch(hour: Int): String {
        return terrestrialBranch[hour / 2] + "时"
    }
}
