
package com.mogujie.tt.ui.widget.message;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.leimo.wanxin.R;
import com.mogujie.tt.config.DBConstant;

/**
 * A popup window that can be used to display an arbitrary view
 * OnItemClickListener
 */
public class MessageOperatePopup implements View.OnClickListener, View.OnTouchListener {

    private PopupWindow mPopup;
    private static MessageOperatePopup messageOperatePopup;
    private OnItemClickListener mListener;
    private int mPosition;  //点击下标

    private int mWidth;
    private int mHeight;

    private int mParentTop;
    private TextView copyBtn, resendBtn, speakerBtn, revokeBtn, deleteBtn;
    private boolean bcopyShow, bresendShow, bspeakerShow;

    private Context context = null;

    public static MessageOperatePopup instance(Context ctx, View parent) {
        if (null == messageOperatePopup) {
            synchronized (MessageOperatePopup.class) {
                messageOperatePopup = new MessageOperatePopup(ctx, parent);
            }
        }
        return messageOperatePopup;
    }

    public void hidePopup() {
        if (messageOperatePopup != null) {
            messageOperatePopup.dismiss();
        }
    }


    @SuppressWarnings("deprecation")
    private MessageOperatePopup(Context ctx, View parent) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.tt_popup_list,
            null);
        this.context = ctx;

        // popView = (LinearLayout) view.findViewById(R.id.popup_list);

        copyBtn = (TextView) view.findViewById(R.id.copy_btn);
        copyBtn.setOnClickListener(this);
        copyBtn.setOnTouchListener(this);
        //        copyBtn.setPadding(0, 13, 0, 8);

        resendBtn = (TextView) view.findViewById(R.id.resend_btn);
        resendBtn.setOnClickListener(this);
        resendBtn.setOnTouchListener(this);
        //        resendBtn.setPadding(0, 13, 0, 8);

        speakerBtn = (TextView) view.findViewById(R.id.speaker_btn);
        speakerBtn.setOnClickListener(this);
        speakerBtn.setOnTouchListener(this);
        //        speakerBtn.setPadding(0, 13, 0, 8);

        revokeBtn = (TextView) view.findViewById(R.id.revoke_btn);
        revokeBtn.setOnClickListener(this);
        revokeBtn.setOnTouchListener(this);
        //        revokeBtn.setPadding(0, 13, 0, 8);

        deleteBtn = (TextView) view.findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(this);
        deleteBtn.setOnTouchListener(this);
        //        deleteBtn.setPadding(0, 13, 0, 8);

        mWidth = (int) context.getResources().getDimension(
            R.dimen.message_item_popup_width_single_short);
        mHeight = (int) context.getResources().getDimension(
            R.dimen.message_item_popup_height);

        int[] location = new int[2];
        parent.getLocationOnScreen(location);
        mParentTop = location[1];
        mPopup = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // mPopup.setFocusable(true);
        // 设置允许在外点击消失
        mPopup.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        mPopup.setBackgroundDrawable(new BitmapDrawable());
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        mListener = l;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public void show(View item, int type, boolean bResend, boolean bSelf, boolean bRevoke, int position) {
        if (mPopup == null || mPopup.isShowing()) {
            return;
        }

        //下标赋值
        mPosition = position;

        boolean showTop = true;

        int[] location = new int[2];
        item.getLocationOnScreen(location);
        // 默认在item上面弹出
        if (location[1] - mParentTop/* - mHeight */ <= 0) {
            // showTop = false;
        } else {
            // 如果不是在最顶部，显示的距离要上移10
            location[1] = location[1] - 10;
        }

        //下面全用回调 todo

        // 语音类型
        if (type == DBConstant.SHOW_AUDIO_TYPE) {
//            speakerBtn.setVisibility(View.VISIBLE);
//            if (AudioPlayerHandler.getInstance().getAudioMode(context) == AudioManager.MODE_NORMAL) {
//                speakerBtn.setText(R.string.call_mode);
//            } else {
//                speakerBtn.setText(R.string.speaker_mode);
//            }
            bspeakerShow = true;
        } else {
            speakerBtn.setVisibility(View.GONE);
            bspeakerShow = false;
        }

        // 自己消息重发
        // 自己的消息
        // 非自己的消息
        // 图片语音
        // 文本
        if (bResend && bSelf) {     //自己的没有发出去的
            resendBtn.setVisibility(View.VISIBLE);
            revokeBtn.setVisibility(View.GONE);
            bresendShow = true;
            if (type == DBConstant.SHOW_ORIGIN_TEXT_TYPE) {
                copyBtn.setVisibility(View.VISIBLE);
                bcopyShow = true;
            } else {
                copyBtn.setVisibility(View.GONE);
                bcopyShow = false;
            }
        } else if (!bResend && bSelf) {     //自己的已经发出去的
            resendBtn.setVisibility(View.GONE);
            //是否可以撤回
            if (bRevoke) {
                revokeBtn.setVisibility(View.VISIBLE);
            } else {
                revokeBtn.setVisibility(View.GONE);
            }
            bresendShow = false;
            if (type == DBConstant.SHOW_ORIGIN_TEXT_TYPE) {
                copyBtn.setVisibility(View.VISIBLE);
                bcopyShow = true;
            } else {
                copyBtn.setVisibility(View.GONE);
                bcopyShow = false;
            }
        } else {    //不是自己的
            revokeBtn.setVisibility(View.GONE);
            if (type == DBConstant.SHOW_ORIGIN_TEXT_TYPE) {
                copyBtn.setVisibility(View.VISIBLE);
                bcopyShow = true;
            } else {
                copyBtn.setVisibility(View.GONE);
                bcopyShow = false;
            }
        }

        //出现的位置
        if (showTop) {
            if (location[1] - mParentTop/* - mHeight */ > 0) {
                mPopup.showAtLocation(item, Gravity.NO_GRAVITY, location[0]
                    + (item.getWidth() / 2 - mWidth / 2), location[1]
                    - mHeight);
            } else {
                mPopup.showAtLocation(item, Gravity.NO_GRAVITY, location[0]
                    + (item.getWidth() / 2 - mWidth / 2), 0 + mHeight / 2);
            }
        } else {
            // TODO: 在下面弹出的时候需要翻转背景
            mPopup.showAtLocation(item, Gravity.NO_GRAVITY,
                location[0] + (item.getWidth() / 2 - mWidth / 2),
                location[1] + item.getHeight());
        }
    }

    public void dismiss() {
        if (mPopup == null || !mPopup.isShowing()) {
            return;
        }

        mPopup.dismiss();
    }

    public interface OnItemClickListener {
        void onCopyClick();

        void onResendClick();

        void onSpeakerClick();

        void onRevokeClick(int position);

        void onDeleteClick(int position);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();

        if (R.id.copy_btn == id) {
            dismiss();
            if (mListener != null) {
                mListener.onCopyClick();
            }
        } else if (R.id.resend_btn == id) {
            dismiss();
            if (mListener != null) {
                mListener.onResendClick();
            }
        } else if (R.id.speaker_btn == id) {
            dismiss();
            if (mListener != null) {
                mListener.onSpeakerClick();
            }
        } else if (R.id.revoke_btn == id) {
            dismiss();
            if (mListener != null) {
                mListener.onRevokeClick(mPosition);
            }
        } else if (R.id.delete_btn == id) {
            dismiss();
            if (mListener != null) {
                mListener.onDeleteClick(mPosition);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        Resources resource = context.getResources();
        //2个点击背景
        Drawable drawableNormal = resource.getDrawable(R.drawable.tt_bg_popup_normal_item);
        Drawable drawablepressed = resource.getDrawable(R.drawable.tt_bg_popup_pressed_item);

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (drawableNormal != null) {
                if (R.id.copy_btn == v.getId()) {
                    copyBtn.setBackgroundDrawable(drawableNormal);
                } else if (R.id.resend_btn == v.getId()) {
                    resendBtn.setBackgroundDrawable(drawableNormal);
                } else if (R.id.speaker_btn == v.getId()) {
                    speakerBtn.setBackgroundDrawable(drawableNormal);

                } else if (R.id.revoke_btn == v.getId()) { //撤回
                    revokeBtn.setBackgroundDrawable(drawableNormal);
                } else if (R.id.delete_btn == v.getId()) { //删除
                    deleteBtn.setBackgroundDrawable(drawableNormal);
                }
            }
        } else {
            if (drawablepressed != null) {
                if (R.id.copy_btn == v.getId()) {
                    copyBtn.setBackgroundDrawable(drawablepressed);
                } else if (R.id.resend_btn == v.getId()) {
                    resendBtn.setBackgroundDrawable(drawablepressed);
                } else if (R.id.speaker_btn == v.getId()) {
                    speakerBtn.setBackgroundDrawable(drawablepressed);

                } else if (R.id.revoke_btn == v.getId()) { //撤回
                    revokeBtn.setBackgroundDrawable(drawablepressed);
                } else if (R.id.delete_btn == v.getId()) { //删除
                    deleteBtn.setBackgroundDrawable(drawablepressed);
                }
            }
        }
        return false;
    }
}
