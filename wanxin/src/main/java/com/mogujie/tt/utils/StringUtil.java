package com.mogujie.tt.utils;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.EditText;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangru
 * Date: 2018/3/14  9:50
 * mail: 1902065822@qq.com
 * describe:
 */

public class StringUtil {
    /**
     * 转成Encode编码
     * get请求不能有空格、换行、&、"、等特殊字符，需要转换编码后服务器解码，如果服务器不解码ios可能显示的就是乱码
     * @param param 内容
     * @return 转码后的内容
     */
    public static String toEncode(String param) {
        try {
            param = URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return param;
    }

    public static boolean isEmpty(String str) {
        return isEmpty(str, false);
    }

    public static boolean isEmpty(String str, boolean trim) {
        if (str == null || str.equals("") || str.equalsIgnoreCase("null")) {
            return true;
        }
        if (trim) {
            str = str.trim();
        }
        return str.length() <= 0;
    }



    /********** 验证格式start *************/

    /** 判断密码是否合符规范 */
    public static boolean isPwd(String pwdString) {
        String pwd = "^[\u0000-\u00FF]+$";// 半角字符（英文、数字、英文字符）
        return Pattern.matches(pwd, pwdString);
    }

    /** 验证手机格式 */
    public static boolean isMobile(String mobiles) {
        /*
         * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188 联通：130、131、132、152、155、156、185、186
		 * 电信：133、153、180、189、（1349卫通） 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
		 */
        String telRegex = "[1][358]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobiles)) {
            return false;
        } else {
            return mobiles.matches(telRegex);
        }
    }

    /** 判断正整数 */
    public static boolean isNumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    /** 是否包含数字 */
    public static boolean isExistNum(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (Character.isDigit(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /********** 验证格式end **************/

    /************************* 格式化start ********************/

    /** EditText只能输入小数点后几位 */
    public static void getIntFomat(final EditText edt, final int count) {
        edt.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source.equals(".") && dest.toString().length() == 0) {
                    return "0.";
                }
                if (dest.toString().contains(".")) {
                    int index = dest.toString().indexOf(".");
                    int mlength = dest.toString().substring(index).length();
                    if (mlength == (count + 1)) {
                        return "";
                    }
                }

                return null;
            }
        }});
    }

    /** double 型输出整数不显示小数点和其后的0 */
    public static String doubleTrans(double d) {
        if (d % 1.0 == 0) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }

    /** 替换空格为转义字符%20 */
    public static String replaceSpaces(String string) {
        if (string != null) {
            string = string.replaceAll(" ", "%20");// 空格用%20 转义,不然会报错
        }
        return string;
    }

    /** 去空格 */
    public static String removeSpaces(String string) {
        if (string != null) {
            string = string.replaceAll(" ", "");// 空格用%20 转义,不然会报错
        }
        return string;
    }

    /** 将空替换成-1 */
    public static String replaceNull(String str) {
        return replaceNull(str, "-1");
    }

    /** 将空替换成 strDefault */
    public static String replaceNull(String str, String strDefault) {
        return isEmpty(str) ? strDefault : str;
    }

    /** 超过长度的用...代替 */
    public static String setTextMaxLength(String name, int maxLength) {
        if (name.length() > maxLength) {
            name = name.substring(0, maxLength + 1) + "...";
        }
        return name;
    }

    /** 电话号码中间用...代替 */
    public static String setTel(String telname) {
        telname = telname.substring(0, 3) + "..." + telname.substring(9, 11);
        return telname;
    }

    /************************* 格式化end ********************/

    // 提取特殊字符<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    /** 去掉string内所有非数字类型字符 */
    public static String getNumber(Object object) {
        return getNumber(ObjectTrans.getString(object));
    }

    /** 去掉string内所有非数字类型字符 */
    public static String getNumber(CharSequence cs) {
        return getNumber(ObjectTrans.getString(cs));
    }

    /** 去掉string内所有非数字类型字符 */
    public static String getNumber(String s) {
        if (isEmpty(s, true)) {
            return "";
        }
        String numberString = "";
        String single;
        for (int i = 0; i < s.length(); i++) {
            single = s.substring(i, i + 1);
            if (isNumeric(single)) {
                numberString += single;
            }
        }
        return numberString;
    }

    public static String getNumberDouble(String s) {
        if (isEmpty(s, true)) {
            return "";
        }
        String numberString = "";
        String single;
        for (int i = 0; i < s.length(); i++) {
            single = s.substring(i, i + 1);
            if (isNumeric(single) || single.equals(".")) {
                numberString += single;
            }
        }
        return numberString;
    }

    /**
     * 截取2个字符之间的字符串
     * @param before 要截取字符之前的字符串
     * @param after  要截取字符之后的字符串
     * @param str    要截取字符
     * @return 截取后的字符串
     */
    public static List<String> matchBeforeAfter(String before, String after, String str) {
        List<String> results = new ArrayList<String>();
        Pattern p = Pattern.compile(before + "([\\w/\\.]*)" + after);
        Matcher m = p.matcher(str);
        while (!m.hitEnd() && m.find()) {
            results.add(m.group(1));
        }
        return results;
    }

    /**
     * 反转
     * @param str
     * @return
     */
    public static String reverse(String str) {
        StringBuffer sb = new StringBuffer(str);
        sb = sb.reverse();
        return sb.toString();
    }
    // 提取特殊字符>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    /**
     * 是json格式
     * @param json
     * @return
     */
    public static boolean isJsonFormat(String json) {
        try {
            new JsonParser().parse(json);
            return true;
        } catch (JsonParseException e) {
            System.out.println("bad json: " + json);
            return false;
        }
    }

}
