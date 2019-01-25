package com.mogujie.tt.utils;

/**
 * Object 转换String int double boolean...
 * Created by wangru
 * Date: 2018/3/14  9:49
 * mail: 1902065822@qq.com
 * describe:
 */

public class ObjectTrans {
    /** Object 转 String */
    public static String getString(Object obj) {
        return getString(obj, "");
    }

    public static String getString(Object obj, String strDefault) {
        String str = strDefault;
        try {
            str = String.valueOf(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static int getInt(String str, int intDefault) {
        str = StringUtil.replaceNull(str, intDefault + "");
        str = StringUtil.getNumber(str);
        return Integer.parseInt(str);
    }

    public static int getInt(String str) {
        return getInt(str, 0);
    }

    public static int getInt(Object obj) {
        return getInt(obj, 0);
    }

    public static int getInt(Object obj, int defaultInt) {
        return getInt(getString(obj), defaultInt);
    }

    /** double */
    public static double getDouble(String str) {
        return getDouble(str, 0);
    }

    public static double getDouble(String str, double doubleDefault) {
        str = StringUtil.replaceNull(str, doubleDefault + "");
        str = StringUtil.getNumberDouble(str);
        return Double.parseDouble(str);
    }

    public static double getDouble(Object obj) {
        return getDouble(getString(obj), 0);
    }

    public static float getFloat(double dbl) {
        return Float.parseFloat(getString(dbl));
    }

    /** boolean*/
    public static boolean getBoolean(Object obj){
        return Boolean.parseBoolean(obj.toString());
    }

    /** long */
    public static Long getLong(Object obj){return Long.valueOf(obj.toString());}

}
