package com.lemo.emojcenter.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Description:sp存储文件
 * Author:wxw
 * Date:2018/2/6.
 */
public class SharedPreferencesUtils {

    private static SharedPreferencesUtils preferencesUtils;
    private static SharedPreferences preferences;

    private SharedPreferencesUtils() {
    }

    public static void init(Context context) {
        if (preferencesUtils == null) {
            preferencesUtils = new SharedPreferencesUtils();
        }
        if (preferences == null) {
            preferences = context.getSharedPreferences("biaoqingbao", Context.MODE_PRIVATE);
        }
    }

    public static SharedPreferencesUtils getinstance() {
        if (preferencesUtils == null) {
            preferencesUtils = new SharedPreferencesUtils();
        }
        return preferencesUtils;
    }

    public void setBooleanValue(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBooleanValue(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public void setStringValue(String key, String value) {

        preferences.edit().putString(key, value).apply();
    }

    public String getStringValue(String key, String str) {
        return preferences.getString(key, str);
    }

    public String getStringValue(String key) {
        return preferences.getString(key, "");
    }

    public void setIntValue(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    public int getIntValue(String key, int value) {
        return preferences.getInt(key, value);
    }

    public void setLongValue(String key, Long value) {
        preferences.edit().putLong(key, value).apply();
    }

    public long getLongValue(String key, long value) {
        return preferences.getLong(key, value);
    }
}
