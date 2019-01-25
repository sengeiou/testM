package com.mogujie.tt.ui.widget.message;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.mogujie.tt.db.entity.MessageEntity;
import com.mogujie.tt.db.entity.UserEntity;
import com.leimo.wanxin.R;
import com.mogujie.tt.imservice.entity.ImageMessage;
import com.mogujie.tt.ui.widget.GifView;

/**
 * Created by zhujian on 15/3/26.
 */
public class GifImageRenderView extends  BaseMsgRenderView {
    private GifView messageContent;

    public GifView getMessageContent()
    {
        return messageContent;
    }
    public static GifImageRenderView inflater(Context context,ViewGroup viewGroup,boolean isMine){
        int resource = isMine? R.layout.tt_mine_gifimage_message_item :R.layout.tt_other_gifimage_message_item;
        GifImageRenderView gifRenderView = (GifImageRenderView) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        gifRenderView.setMine(isMine);
        return gifRenderView;
    }

    public GifImageRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        messageContent = (GifView) findViewById(R.id.message_image);
    }

    /**
     * 控件赋值
     * @param messageEntity
     * @param userEntity
     */
    @Override
    public void render(MessageEntity messageEntity, UserEntity userEntity,Context context) {
        super.render(messageEntity, userEntity,context);
        ImageMessage imageMessage = (ImageMessage) messageEntity;
        String url = imageMessage.getUrl();
        Glide.with(context).load(url).into(messageContent);
//        new GifLoadTask() {
//            @Override
//            protected void onPostExecute(byte[] bytes) {
//                messageContent.setBytes(bytes);
//                messageContent.startAnimation();
//            }
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//            }
//        }.execute(url);
    }

    @Override
    public void msgFailure(MessageEntity messageEntity) {
        super.msgFailure(messageEntity);
    }

    /**----------------set/get---------------------------------*/

    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }


    public void setParentView(ViewGroup parentView) {
        this.parentView = parentView;
    }


}
