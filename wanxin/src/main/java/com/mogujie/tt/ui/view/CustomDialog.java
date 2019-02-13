package com.mogujie.tt.ui.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.Window;

import com.leimo.wanxin.R;

/**
 * Description :加载DIalog
 * <p>
 * Author:yang
 * <p>
 * Email:1318392199@qq.com
 * <p>
 * Date: 2018/12/29
 */

public class CustomDialog {
    private Dialog loadingDialog;
    private Context mContext;

    public CustomDialog(Context context) {
        this.mContext = context;
    }

    /**
     * 显示加载dialog
     */
    public void showLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            return;
        }
        View view = View.inflate(mContext, R.layout.layout_dialog_loading, null);
        loadingDialog = new Dialog(mContext, R.style.commondialogstyle);
        loadingDialog.setContentView(view);
        Window window = loadingDialog.getWindow();
        window.setGravity(Gravity.CENTER);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setCancelable(true);
        Activity activity = (Activity) mContext;
        if (activity.isFinishing()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed()) {
            return;
        }
        loadingDialog.show();
    }

    /**
     * 取消加载dialog
     */
    public void dismissLoadingDialog() {
        if (loadingDialog != null && !((Activity) mContext).isFinishing() && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
