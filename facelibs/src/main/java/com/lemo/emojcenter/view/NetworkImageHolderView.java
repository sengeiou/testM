package com.lemo.emojcenter.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.bigkoo.convenientbanner.holder.Holder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lemo.emojcenter.R;


public class NetworkImageHolderView implements Holder<String> {
    private ImageView imageView;

    @Override
    public View createView(Context context) {
        //你可以通过layout文件来创建，也可以像我一样用代码创建，不一定是Image，任何控件都可以进行翻页
        imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        return imageView;
    }

    @Override
    public void UpdateUI(Context context, int position, String data) {
        imageView.setImageResource(R.mipmap.face_ic_default_adimage);
        //        ImageLoader.getInstance().displayImage(data,imageView);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.mipmap.face_banner_error);
        Glide.with(context.getApplicationContext()).load(data).apply(requestOptions).into(imageView);

    }
}
