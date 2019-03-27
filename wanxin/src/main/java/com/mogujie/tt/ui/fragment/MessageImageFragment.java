package com.mogujie.tt.ui.fragment;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.common.utils.CacheImgUtil;
import com.app.common.utils.StorageUtils;
import com.leimo.wanxin.R;
import com.mogujie.tt.imservice.entity.ImageMessage;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.ui.view.DialogCommon_T;
import com.mogujie.tt.utils.FileUtil;
import com.mogujie.tt.utils.ImageLoaderUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.polites.android.GestureImageView;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class MessageImageFragment extends android.support.v4.app.Fragment {
    private View curView = null;
    protected GestureImageView view;
    protected GestureImageView newView;
    private ImageMessage messageInfo = null;
    private ProgressBar mProgressbar = null;
    private FrameLayout parentLayout = null;
    private IMService imService;
    private DialogCommon_T dialog;  //弹框

    public void setImService(IMService service) {
        this.imService = service;
    }

    public void setImageInfo(ImageMessage imageInfo) {
        messageInfo = imageInfo;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        try {
            if (null != curView) {
                if (null != curView.getParent()) {
                    ((ViewGroup) curView.getParent()).removeView(curView);
                }
            }
            curView = inflater.inflate(R.layout.fragment_message_image, null);
            initRes(curView);
            initData();
            initView();
            return curView;
        } catch (Exception e) {
            return null;
        }
    }

    private void initView() {
        dialog = new DialogCommon_T(getContext(), "", "是否保存图片？", "取消", "确定",
                new Function1<View, Unit>() {
                    @Override
                    public Unit invoke(View view) {
                        return null;
                    }
                },
                new Function1<View, Unit>() {
                    @Override
                    public Unit invoke(View view) {
                        CacheImgUtil.INSTANCE.downImage(getContext(), messageInfo.getUrl(), StorageUtils.INSTANCE.getPublicStorageDir("mengmeng", null) + "/image/" + System.currentTimeMillis() + "y.png", "com.qingmeng.mengmeng",
                                new Function1<Boolean, Unit>() {
                                    @Override
                                    public Unit invoke(Boolean success) {
                                        if (success) {
                                            Toast.makeText(getContext(), "保存成功", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), "保存失败", Toast.LENGTH_SHORT).show();
                                        }
                                        return null;
                                    }
                                });
                        return null;
                    }
                }, true, R.style.dialog_common_t);
    }

    private void initRes(View curView) {
        try {
            view = (GestureImageView) curView.findViewById(R.id.image);
            newView = (GestureImageView) curView.findViewById(R.id.new_image);
            parentLayout = (FrameLayout) curView.findViewById(R.id.layout);
            mProgressbar = (ProgressBar) curView.findViewById(R.id.progress_bar);
            mProgressbar.setVisibility(View.VISIBLE);
            view.setVisibility(View.VISIBLE);
            newView.setVisibility(View.GONE);
            view.setClickable(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentLayout.performClick();

                }
            });
            parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (isAdded()) {
                        getActivity().finish();
                        getActivity().overridePendingTransition(
                                R.anim.tt_stay, R.anim.tt_image_exit);
                    }
                }
            });
            parentLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!dialog.isShowing()) {
                        dialog.show();
                    }
                    return false;
                }
            });
        } catch (Exception e) {
        }
    }

    private void initData() {
        try {

            //@ZJ 破图的展示
            String imageUrl = messageInfo.getUrl();
            if (!TextUtils.isEmpty(messageInfo.getPath()) && FileUtil.isFileExist(messageInfo.getPath())) {
                imageUrl = "file://" + messageInfo.getPath();
            }

            ImageLoaderUtil.getImageLoaderInstance().displayImage(imageUrl, view, new DisplayImageOptions.Builder()
                    .cacheInMemory(false)
                    .cacheOnDisk(true)
                    .showImageOnLoading(R.drawable.tt_message_image_default)
                    .showImageOnFail(R.drawable.tt_message_image_error)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .showImageOnFail(R.drawable.tt_message_image_error)
                    .resetViewBeforeLoading(true)
                    .build(), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    closeProgressDialog(loadedImage, true);
                }

                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    closeProgressDialog(null, true);
                }
            });
        } catch (Exception e) {
        }
    }

    private void closeProgressDialog(Bitmap bitmap, boolean hideProgress) {
        try {
            if (isAdded()) {
                if (hideProgress) {
                    mProgressbar.setVisibility(View.GONE);
                }
                if (null == bitmap) {
                    return;
                }
                view.setVisibility(View.GONE);
                newView.setVisibility(View.VISIBLE);
                newView.setImageBitmap(bitmap);
                newView.setClickable(true);
                newView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        parentLayout.performClick();
                    }
                });
                newView.setOnTouchListener(new View.OnTouchListener() {
                    Point point = new Point();
                    boolean isLongClick = false;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                point.x = (int) v.getX();
                                point.y = (int) v.getY();
                                isLongClick = true;
                                //延时触发
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isLongClick) {
                                            parentLayout.performLongClick();
                                        }
                                    }
                                }, 600);
                                break;
                            case MotionEvent.ACTION_MOVE:
                                int x = (int) (point.x - v.getX());
                                int y = (int) (point.y - v.getY());
                                //移动位置超出正负5
                                if (x < -5 || x > 5 || y < -5 || y > 5) {
                                    isLongClick = false;
                                }
                                break;
                            default:
                                isLongClick = false;
                                break;
                        }
                        return false;
                    }
                });
            }
        } catch (Exception e) {
        }
    }
}