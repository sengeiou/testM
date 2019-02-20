package com.lemo.emojcenter.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;

import com.lemo.emojcenter.R;

import java.lang.ref.WeakReference;

/**
 * Created by wangru
 * Date: 2017/12/26  16:41
 * mail: 1902065822@qq.com
 * describe:
 */

public class CollectAddTipPopupWindow extends PopupWindow {
    private int mWidth;
    private int mHeight;
    private View mContentView;
    private WeakReference<Activity> mActivityReference;

    private ImageButton imageButtonClose;
    private View.OnClickListener onClickListener;


    public CollectAddTipPopupWindow(Activity activity) {
        super(activity);// 没有会出错
        mActivityReference = new WeakReference<>(activity);
        mContentView = LayoutInflater.from(activity).inflate(R.layout.face_dialog_add_tip, null);
        setContentView(mContentView);
        initWidthAndHeight(activity.getApplicationContext());
        setWidth(mWidth);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setFocusable(false);
        setTouchable(true);
        setOutsideTouchable(false);
        setBackgroundDrawable(new BitmapDrawable());

        // 在PopupWindow里面就加上下面代码，让键盘弹出时，不会挡住pop窗口。
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        // addTimePop关闭的事件，主要是为了将背景透明度改回来
        setOnDismissListener(dismissPopupWindow);
        initView();
        setClose();
    }

    private void initView() {
        imageButtonClose = (ImageButton) mContentView.findViewById(R.id.ib_tip_sure);
    }

    public void noClose() {
        imageButtonClose.setVisibility(View.INVISIBLE);
    }

    private void setClose() {
        if (imageButtonClose != null) {
            imageButtonClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    missPopWindow();
                    if (onClickListener != null) {
                        onClickListener.onClick(v);
                    }
                }
            });
        }
    }


    public void setSubmitListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    private void initWidthAndHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mWidth = (int) (outMetrics.widthPixels * 0.9);
    }

    /**
     * 显示窗口
     *
     * @param parent 显示父view
     */
    public void openPopWindow(View parent) {
        showAtLocation(parent, Gravity.CENTER, 0, 0);
        backgroundAlpha(0.5f);
        // setAnimationStyle(R.style.animPopup);
    }

    //隐藏窗口
    public void missPopWindow() {
        dismiss();
        backgroundAlpha(1f);
    }

    private OnDismissListener dismissPopupWindow = new OnDismissListener() {
        @Override
        public void onDismiss() {
            backgroundAlpha(1f);
        }
    };

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    private void backgroundAlpha(float bgAlpha) {
        if (mActivityReference.get() != null) {
            WindowManager.LayoutParams layoutParams = mActivityReference.get().getWindow().getAttributes();
            layoutParams.alpha = bgAlpha;
            mActivityReference.get().getWindow().setAttributes(layoutParams);
        }
    }
}
