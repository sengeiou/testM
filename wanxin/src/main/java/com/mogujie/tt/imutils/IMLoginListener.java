package com.mogujie.tt.imutils;

/**
 * Created by wangru
 * Date: 2017/8/29  19:22
 * mail: 1902065822@qq.com
 * describe:
 */
public interface IMLoginListener {
    void onLoginSuccess();
    void onLoginFail(String errorTip);
}
