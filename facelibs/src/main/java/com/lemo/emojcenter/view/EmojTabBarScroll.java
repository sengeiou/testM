package com.lemo.emojcenter.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.lemo.emojcenter.R;
import com.lemo.emojcenter.bean.EmojGroupBean;
import com.lemo.emojcenter.bean.EmojInfoBean;
import com.lemo.emojcenter.constant.FaceLocalConstant;
import com.lemo.emojcenter.manage.FaceDownloadFaceManage;
import com.lemo.emojcenter.manage.FaceEmojManage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.Nullable;

public class EmojTabBarScroll extends RelativeLayout {
    private static final String TAG = "EmojTabBarScroll";
    private Context context;
    private HorizontalScrollView scrollView;
    private LinearLayout tabContainer;

    private List<ImageView> tabList = new ArrayList<>();
    private EaseScrollTabBarItemClickListener itemClickListener;

    public EmojTabBarScroll(Context context) {
        this(context, null);
    }

    public EmojTabBarScroll(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public EmojTabBarScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.face_ease_widget_emojicon_tab_bar, this);

        scrollView = (HorizontalScrollView) findViewById(R.id.scroll_view);
        tabContainer = (LinearLayout) findViewById(R.id.tab_container);
    }

    public void setData(List<EmojGroupBean> emojGroupBeanList) {
        tabContainer.removeAllViews();
        tabList.clear();
        for (EmojGroupBean emojGroupBean : emojGroupBeanList) {
            addTab(emojGroupBean);
        }
    }

    /**
     * add tabGeneratedAppGlideModule
     */
    public void addTab(EmojGroupBean emojGroupBean) {
        View tabView = View.inflate(context, R.layout.face_ease_scroll_tab_item, null);
        ImageView imageView = (ImageView) tabView.findViewById(R.id.iv_icon);
        final int position = tabList.size();
        String faceId = emojGroupBean.getFaceId();
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.skipMemoryCache(false);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        layoutParams.width = (int) (45 * Resources.getSystem().getDisplayMetrics().density);
        layoutParams.height = (int) (44 * Resources.getSystem().getDisplayMetrics().density);
        imageView.setLayoutParams(layoutParams);
        //设置icon
        if (TextUtils.equals(faceId, FaceLocalConstant.Companion.getFACE_ID_EMOJ())) {
            //经典表情
            int padding = (int) (8 * Resources.getSystem().getDisplayMetrics().density);
            imageView.setPadding(padding, padding, padding, padding);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setImageResource(R.drawable.face_keyboardxiaolian);
//            Glide.with(context).load(R.drawable.face_keyboardxiaolian).into(imageView);
        } else if (TextUtils.equals(faceId, FaceLocalConstant.Companion.getFACE_ID_COLLECT())) {
            //收藏
            int padding = (int) (8 * Resources.getSystem().getDisplayMetrics().density);
            imageView.setPadding(padding, padding, padding, padding);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setImageResource(R.drawable.face_keyboardxihuan);
//            Glide.with(context).load(R.drawable.face_keyboardxihuan).into(imageView);
        } else {
            //表情包
            final String path = emojGroupBean.getIcon();
            String url = emojGroupBean.getUrl();

            if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                url = path;
            }
            if (TextUtils.isEmpty(url)) {
                EmojInfoBean emojInfoBean = FaceEmojManage.Companion.getInstance().getEmojInfoByFaceId(emojGroupBean.getFaceId());
                if (emojInfoBean != null) {
                    url = emojInfoBean.getIcon();
                }
            }
            final String finalUrl = url;
            Glide.with(context).load(url).apply(requestOptions).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    if (!TextUtils.isEmpty(path) && path.contains("/") && path.startsWith("http")) {
                        String dir = path.substring(0, path.lastIndexOf("/"));
                        if (!new File(dir).exists()) {
                            new File(dir).mkdirs();
                        }
                        FaceDownloadFaceManage.INSTANCE.downFaceImage(finalUrl, path);
                    }
                    return false;
                }
            }).into(imageView);
        }
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(position);
                }
            }
        });
        tabContainer.addView(tabView);
        tabList.add(imageView);
    }

    /**
     * remove tab
     *
     * @param position
     */
    public void removeTab(int position) {
        tabContainer.removeViewAt(position);
        tabList.remove(position);
    }

    public void selectedTo(int position, boolean clicked) {
        if (!clicked) {
            scrollTo(position);
        }
        for (int i = 0; i < tabList.size(); i++) {
            if (position == i) {
                tabList.get(i).setBackgroundColor(getResources().getColor(R.color.emojicon_tab_selected));
            } else {
                tabList.get(i).setBackgroundColor(getResources().getColor(R.color.emojicon_tab_nomal));
            }
        }
    }

    private void scrollTo(final int position) {
        int childCount = tabContainer.getChildCount();
        if (position < childCount) {
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    int mScrollX = tabContainer.getScrollX();
                    View childView = tabContainer.getChildAt(position);
                    if (childView != null) {
                        int childX = (int) ViewCompat.getX(childView);
                        if (childX < mScrollX) {
                            scrollView.scrollTo(childX, 0);
                            Log.d(TAG, "scrollTo0: " + "mScrollX=" + mScrollX + " #childX=" + childX);
                            return;
                        }
                        int childWidth = childView.getWidth();
                        int hsvWidth = scrollView.getWidth();
                        int childRight = childX + childWidth;
                        int scrollRight = mScrollX + hsvWidth;
                        int scrollToX = childRight - scrollRight;
                        if (position == 0) {
                            scrollView.scrollTo(0, 0);
                            return;
                        }
                        Log.d(TAG, "scrollTo1: " + "mScrollX=" + mScrollX + " #childX=" + childX + " #hsvWidth=" + hsvWidth + " #childWidth=" + childWidth);
                        Log.d(TAG, "scrollTo2: " + "childRight=" + childRight + " #scrollRight=" + scrollRight + " #scrollToX=" + scrollToX);
                        if (childRight > scrollRight) {
                            scrollView.scrollTo(scrollToX, 0);
                        }
                    }
                }
            });
        }
    }


    public void setTabBarItemClickListener(EaseScrollTabBarItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


    public interface EaseScrollTabBarItemClickListener {
        void onItemClick(int position);
    }

}
