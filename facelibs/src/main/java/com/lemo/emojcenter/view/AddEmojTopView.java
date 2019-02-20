package com.lemo.emojcenter.view;


import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lemo.emojcenter.R;


/**
 * Description:自定义Toolbar
 * Author:wxw
 * Date:2018/1/24.
 */
public class AddEmojTopView extends RelativeLayout {
    private Context mContext;
    private LinearLayout mTopImageBack;
    private TextView mTextBack;
    private TextView mTopTitle;
    private TextView mImageSetting;
    private TextView mTopClose;
    private LinearLayout mTopBackLayout;
    private TextView mTopNum;


    public AddEmojTopView(Context context) {
        super(context, null, 0);
    }

    public AddEmojTopView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        if (isInEditMode()) {
            return;
        }
        initValue(context);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TopView);

        mTextBack.setText(typedArray.getString(R.styleable.TopView_face_back));
        mTopTitle.setText(typedArray.getString(R.styleable.TopView_face_title));
    }

    private void initValue(Context context) {
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.face_layout_view_add_emoj_topview, this, true);
        mTopImageBack = (LinearLayout) this.findViewById(R.id.top_image_back);
        mTopBackLayout = (LinearLayout) this.findViewById(R.id.top_back_layout);
        mTextBack = (TextView) this.findViewById(R.id.back_text);
        mTopTitle = (TextView) this.findViewById(R.id.top_title);
        mImageSetting = (TextView) this.findViewById(R.id.top_setting);
        mTopClose = (TextView) this.findViewById(R.id.top_close);
        mTopNum = (TextView) this.findViewById(R.id.top_num);
        mTopBackLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContext instanceof Activity) {
                    ((Activity) mContext).finish();
                }
            }
        });

        mImageSetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSettingCallBack != null) {
                    mSettingCallBack.settingBack();
                }
            }
        });
    }

    public void setTopTitle(String title) {
        mTopTitle.setText(title);
    }

    public void setTopNum(String numTitle) {
        mTopNum.setText(numTitle);
    }

    public void setTextBack(String textBack) {
        mTextBack.setText(textBack);
    }

    public void setRightText(String text) {
        mImageSetting.setText(text);
    }

    public AddEmojTopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private SettingCallBack mSettingCallBack;

    public void setSettingCallBack(SettingCallBack steeingCallBack) {
        mSettingCallBack = steeingCallBack;
    }

    public interface SettingCallBack {
        void settingBack();
    }

}
