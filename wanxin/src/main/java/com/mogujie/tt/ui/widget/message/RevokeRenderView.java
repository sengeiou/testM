package com.mogujie.tt.ui.widget.message;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leimo.wanxin.R;

/**
 * @author :
 * @email :
 *
 * 消息列表中的撤回气泡
 *
 */
public class RevokeRenderView extends LinearLayout {
    private TextView tvRevoke;

    public RevokeRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static RevokeRenderView inflater(Context context, ViewGroup viewGroup){
        RevokeRenderView revokeRenderView = (RevokeRenderView) LayoutInflater.from(context).inflate(R.layout.tt_message_revoke, viewGroup, false);
        return revokeRenderView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        tvRevoke = (TextView) findViewById(R.id.tvItemRevoke);
    }

    /**与数据绑定*/
    public void setContent(String msg){
        tvRevoke.setText(msg);
    }
}
