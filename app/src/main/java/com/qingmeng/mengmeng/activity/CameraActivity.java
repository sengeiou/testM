package com.qingmeng.mengmeng.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lemo.emojcenter.utils.SDPathUtil;
import com.luck.picture.lib.tools.ScreenUtils;
import com.mogujie.tt.utils.ULogToDevice;
import com.qingmeng.mengmeng.R;
import com.qingmeng.mengmeng.utils.ApiUtils;
import com.qingmeng.mengmeng.utils.PermissionUtils;
import com.qingmeng.mengmeng.utils.ToastUtil;
import com.qingmeng.mengmeng.utils.camera.PathUtils2;
import com.qingmeng.mengmeng.utils.camera.RecorderVideoUtils;
import com.qingmeng.mengmeng.view.camera.CameraManager;
import com.qingmeng.mengmeng.view.camera.FocusImageView;
import com.qingmeng.mengmeng.view.camera.SensorControler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static android.media.MediaRecorder.VideoSource.CAMERA;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

/**
 * Created by 周治东 on 2017/5/31.
 */

public class CameraActivity extends AppCompatActivity implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {
    final public static int REQUEST_CODE_CAMERA = 123;
    final public static int VIDEO_CHAT_CAMERA_SIZE_MAX = 30 * 1024 * 1024;
    private static final String TAG = "CameraActivity";
    private static Handler mHandler = new Handler();
    int mDefaultVideoFrameRate = -1;
    private SurfaceView mSurfaceView;
    private ImageView mIvTakePhoto;
    private TextView mIvConfirm;
    private TextView mIvTakeAgain;
    private SurfaceHolder mSurfaceholder;
    private boolean mIsPreview;
    private Camera mCamera;
    private File mPhotoFile;
    private String mVideoPath;
    private String mImagePath;
    private boolean mIsRecording;
    private MediaRecorder mMediaRecorder;
    private TextView mTvPhoto;
    private TextView mTvVideo;
    private ImageView mIvTakeVideo;
    private String[] mPerms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO};
    private Chronometer mChronometer;
    private ImageView mIvRecorderStop;// 停止录制按钮
    private CameraManager.CameraDirection mCameraId; //0是后置摄像头，1是前置摄像头
    private Camera.Parameters mCameraParams;
    private Camera.Size mBestPictureSize;
    private Camera.Size mBestPreviewSize;
    private SensorControler mSensorControler;
    private int mRotation;
    private CameraManager mCameraManager;
    private SwitchCameraCallBack mSwitchCameraCallBack;
    /**
     * 触摸屏幕时显示的聚焦图案.
     */
    private FocusImageView mFocusImageView;
    private final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            //聚焦之后根据结果修改图片
            if (success) {
                mFocusImageView.onFocusSuccess();
            } else {
                //聚焦失败显示的图片，由于未找到合适的资源，这里仍显示同一张图片
                //                mFocusImageView.onFocusFailed();
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //一秒之后才能再次对焦
                    mSensorControler.unlockFocus();
                }
            }, 1000);
        }
    };
    //    private TextView mTvFlashLight;
    private ImageView mIvCameraDireation;
    private int mRotationResult;
    private int mPhotoOrVideo = 0;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mIvTakeAgain.setVisibility(View.VISIBLE);
            mIvConfirm.setVisibility(View.VISIBLE);
            Log.d(TAG, "test111");
            /* 显示使用照片或者使用视频 */
            switch (mPhotoOrVideo) {
                case 0:
                    mIvConfirm.setText(getString(R.string.use_picture));
                    break;
                case 1:
                    mIvConfirm.setText(getString(R.string.use_video));
                    break;
                default:
                    mIvConfirm.setText(getString(R.string.use_picture));
                    break;
            }
        }
    };
    private ImageView mIvFlashLightOpen;
    private ImageView mIvFlashLightClose;
    private ImageView mIvFlashLightAuto;
    private RelativeLayout mRlFlashLight;
    private ImageView mIvRecorderPlay;
    private LinearLayout mLlSelectPhotoOrVideo;
    private int mVideoWidth;
    private int mVideoHeight; //播放视频的宽高.
    private MediaPlayer mMediaplayer;
    private Display mCurrDisplay;
    private CameraManager.FlashLigthStatus mLightStatusBefore;
    private int previewWidth;
    private int previewHeight;
    private boolean needCameraPermission = true;
    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mIvTakeAgain.setVisibility(View.GONE);
            mIvConfirm.setVisibility(View.GONE);
            initPreview();
            setFocusWay();

            mIsPreview = true;
            //            mMediaplayer = new MediaPlayer();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            //        mCameraParams.setRotation(90);
            //        mCameraParams.set("orientation","portrait");
            //        if (mCameraParams.getSupportedFocusModes().contains(
            //                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            //            mCameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            //        }
            //        mCameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            //                if (mCameraParams.getSupportedFocusModes().contains(
            //                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            //                    mCameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            //                }
            //                mCamera.cancelAutoFocus();


        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            closeCamera();
            mIsPreview = false;
            //                mIvTakeAgain.setVisibility(View.GONE);
            //                mIvConfirm.setVisibility(View.GONE);
        }
    };
    private int type = 1000;//1照片 2视频
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //前置照片进行270°旋转，使其竖直，后置90度
            mPhotoFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (mPhotoFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }
            mIvTakeAgain.setVisibility(View.VISIBLE);
            mIvConfirm.setVisibility(View.VISIBLE);
            /* 显示使用照片或者使用视频 */
            switch (mPhotoOrVideo) {
                case 0:
                    mIvConfirm.setText(getString(R.string.use_picture));
                    break;
                case 1:
                    mIvConfirm.setText(getString(R.string.use_video));
                    break;
                default:
                    mIvConfirm.setText(getString(R.string.use_picture));
                    break;
            }
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            if (mCameraId == CameraManager.CameraDirection.CAMERA_FRONT) {
                matrix.preRotate(mRotationResult + 180);
            } else if (mCameraId == CameraManager.CameraDirection.CAMERA_BACK) {
                matrix.preRotate(mRotationResult);
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap
                .getHeight(), matrix, true);


            try {
                FileOutputStream fos = new FileOutputStream(mPhotoFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                //                fos.write(data);
                //                fos.close();
                Log.d(TAG, "save picture success");
                //notify
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Intent mediaScanIntent = new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = ApiUtils.INSTANCE.getFileUri(CameraActivity.this, mPhotoFile);
                    mediaScanIntent.setData(contentUri);
                    CameraActivity.this.sendBroadcast(mediaScanIntent);
                } else {
                    sendBroadcast(new Intent(
                        Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://"
                            + Environment.getExternalStorageDirectory())));
                }
                mCamera.reconnect();

            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ivTakephoto:
                    mPhotoOrVideo = 0;
                    if (mIsPreview) {
                        mIvTakePhoto.setClickable(false);
                        type = 1;
                        mSensorControler.lockFocus();
                        mIvCameraDireation.setVisibility(View.GONE);
                        mLlSelectPhotoOrVideo.setVisibility(View.GONE);
                        mIvTakePhoto.setVisibility(View.GONE);
                        mCamera.takePicture(null, null, mPicture);
                        ToastUtil.INSTANCE.showShort("正在处理图片，请稍后...");
                        mIsPreview = false;

                    } else {
                        mIvCameraDireation.setVisibility(View.VISIBLE);
                        mIvTakeAgain.setVisibility(View.GONE);
                        mIvConfirm.setVisibility(View.GONE);
                    }
                    SDPathUtil.INSTANCE.updateImageSysStatu(getApplicationContext(), mImagePath);
                    break;
                case R.id.ivTakeVideo:
                    type = 2;
                    mIvCameraDireation.setVisibility(View.VISIBLE);
                    mIvRecorderStop.setVisibility(View.VISIBLE);
                    mIvTakeVideo.setVisibility(View.GONE);
                    mIvCameraDireation.setVisibility(View.GONE);
                    startMediaRecorder();
                    mIsPreview = false;
                    break;
                case R.id.iv_recorder_stop:
                    if (getChronometerSeconds(mChronometer) < 2) {
                        Toast.makeText(CameraActivity.this, "录制时间最短2秒", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mChronometer.stop();// 复位键
                    stopMediaRecorder();
                    processStopRecorder();
                    break;
                case R.id.iv_recorder_play:
                    Intent playIntent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = ApiUtils.INSTANCE.getFileUri(CameraActivity.this, new File(mVideoPath));
                    playIntent.setDataAndType(uri, "video/mp4");
                    startActivityForResult(playIntent, 788);
                    //                    mIvTakeAgain.setVisibility(View.VISIBLE);
                    //                    mIvConfirm.setVisibility(View.VISIBLE);

                    //                    Intent playIntent = new Intent(CameraActivity.this, PlayerActivity.class);
                    //                    playIntent.putExtra("path", mVideoPath);
                    //                    startActivityForResult(playIntent,788);

                    //                    setMediaPlayer();
                    break;
                case R.id.tv_confirm:   //使用照片/视频
                    Intent intent = new Intent();
                    intent.putExtra("mImagePath", mImagePath);
                    intent.putExtra("mVideoPath", mVideoPath);
                    Log.e(TAG, "Camare:ImagePath==null :   " + (mImagePath == null));
                    Log.e(TAG, "Camare:Camera==null :   " + (mCamera == null));
                    intent.putExtra("type", type);//1图片，2视频
                    setResult(RESULT_OK, intent);
                    finish();

                    break;
                case R.id.tv_take_again:    //重拍
                    //                    if (mPhotoFile.exists()) {
                    //                        mPhotoFile.delete();
                    //                    }
                    if (runnable != null) {
                        mHandler.removeCallbacks(runnable);//取消播放视频后延迟设置显示重新录制和确定按钮的runnable
                    }
                    mIvTakePhoto.setClickable(true);
                    Log.e(TAG,
                        "Camare:again:            ImagePath==null :   " + (mImagePath == null));
                    if (mCamera != null) {
                        closeCamera();
                        initPreview();
                        setFocusWay();
                        //                        initCamera();
                        mCamera.startPreview();
                        turnLight(mCameraManager.getLightStatus());  //设置闪光灯
                    } else {
                        initPreview();
                    }
                    mIsPreview = true;
                    mIvCameraDireation.setVisibility(View.VISIBLE);
                    mIvTakeAgain.setVisibility(View.GONE);
                    mIvConfirm.setVisibility(View.GONE);
                    mIvTakePhoto.setVisibility(View.VISIBLE);
                    mTvPhoto.setClickable(true);
                    if (mPhotoOrVideo == 1) {//如果是在拍视频
                        mLlSelectPhotoOrVideo.setVisibility(View.VISIBLE);
                        mIvRecorderPlay.setVisibility(View.GONE);
                        mIvTakeVideo.setVisibility(View.VISIBLE);
                        mChronometer.setBase(SystemClock.elapsedRealtime());// 复位
                        turnLight(mLightStatusBefore);//之前的闪光灯状态
                        mRlFlashLight.setVisibility(View.VISIBLE);//显示闪光灯图标
                    }
                    mLlSelectPhotoOrVideo.setVisibility(View.VISIBLE);
                    break;
                case R.id.tvPhoto:
                    mPhotoOrVideo = 0;
                    mChronometer.setVisibility(View.GONE);
                    mTvPhoto.setSelected(true);
                    mTvVideo.setSelected(false);
                    mIvCameraDireation.setVisibility(View.VISIBLE);
                    mIvTakePhoto.setVisibility(View.VISIBLE);
                    mIvTakeVideo.setVisibility(View.GONE);
                    mIvRecorderStop.setVisibility(View.GONE);
                    //                    mTvPhoto.setClickable(false);
                    break;
                case R.id.tvVideo:
                    setVideo();
                    break;
                //                case R.id.switch_btn:

                default:
                    break;
            }
        }
    };

    private static void sortSizes(List<Camera.Size> sizes) {
        Collections.sort(sizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                return b.height * b.width - a.height * a.width;
            }
        });
    }

    public static int getChronometerSeconds(Chronometer cmt) {
        int totalss = 0;
        String string = cmt.getText().toString();
        if (string.length() == 7) {

            String[] split = string.split(":");
            String string2 = split[0];
            int hour = Integer.parseInt(string2);
            int Hours = hour * 3600;
            String string3 = split[1];
            int min = Integer.parseInt(string3);
            int Mins = min * 60;
            int SS = Integer.parseInt(split[2]);
            totalss = Hours + Mins + SS;
            return totalss;
        } else if (string.length() == 5) {

            String[] split = string.split(":");
            String string3 = split[0];
            int min = Integer.parseInt(string3);
            int Mins = min * 60;
            int SS = Integer.parseInt(split[1]);

            totalss = Mins + SS;
            return totalss;
        }
        return totalss;


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "CameraActivity被创建了");
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_camera);
        initView();
        initData();
        initListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSensorControler.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //        mSurfaceholder.removeCallback(callback);
        if (mSensorControler != null) {
            mSensorControler.onStop();
        }
        closeCamera();
        if (mMediaplayer != null && mMediaplayer.isPlaying()) {
            mMediaplayer.stop();
            mMediaplayer.release();
            mMediaplayer = null;
        }
    }

    public void onCameraFocus(final Point point, boolean needDelay) {
        long delayDuration = needDelay ? 300 : 0;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mSensorControler.isFocusLocked()) {
                    if (onFocus(point, autoFocusCallback)) {
                        mSensorControler.lockFocus();
                        //                        mFocusImageView.startFocus(point);

                        //播放对焦音效
                        //                        if(mFocusSoundPrepared) {
                        //                            mSoundPool.play(mFocusSoundId, 1.0f, 0.5f, 1, 0, 1.0f);
                        //                        }
                    }
                }
            }
        }, delayDuration);
    }

    /**
     * 手动聚焦
     *
     * @param point 触屏坐标
     */
    protected boolean onFocus(Point point, Camera.AutoFocusCallback callback) {
        if (mCamera == null) {
            return false;
        }

        Camera.Parameters parameters = null;
        try {
            parameters = mCamera.getParameters();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //不支持设置自定义聚焦，则使用自动聚焦，返回

        if (Build.VERSION.SDK_INT >= 14) {

            if (parameters.getMaxNumFocusAreas() <= 0) {
                return focus(callback);
            }

            Log.i(TAG, "onCameraFocus:" + point.x + "," + point.y);

            List<Camera.Area> areas = new ArrayList<Camera.Area>();
            int left = point.x - 300;
            int top = point.y - 300;
            int right = point.x + 300;
            int bottom = point.y + 300;
            left = left < -1000 ? -1000 : left;
            top = top < -1000 ? -1000 : top;
            right = right > 1000 ? 1000 : right;
            bottom = bottom > 1000 ? 1000 : bottom;
            areas.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
            parameters.setFocusAreas(areas);
            try {
                mCamera.setParameters(parameters);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }


        return focus(callback);
    }

    private boolean focus(Camera.AutoFocusCallback callback) {
        try {
            mCamera.autoFocus(callback);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void closeCamera() {
        if (mCamera != null) {
            mCamera.cancelAutoFocus();
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 切换摄像头
     */

    public void switchCamera() {
        mCameraId = mCameraId.next();
        closeCamera();
        setUpCamera(mCameraId, mCameraId == CameraManager.CameraDirection.CAMERA_BACK);
    }

    /**
     * 设置当前的Camera 并进行参数设置
     */
    private void setUpCamera(CameraManager.CameraDirection mCameraId, boolean isSwitchFromFront) {
        int facing = mCameraId.ordinal();
        try {
            mCamera = mCameraManager.openCameraFacing(facing);
            //重置对焦计数
            mSensorControler.restFoucs();
        } catch (Exception e) {
            //            Utils.displayToastCenter((Activity) mContext, R.string.tips_camera_forbidden);
            e.printStackTrace();
        }
        if (mCamera != null) {
            setFocusWay();
            initCamera();
            mIsPreview = true;
            if (facing == 0) {//0是后置
                mRlFlashLight.setVisibility(View.VISIBLE);
            } else if (facing == 1) {
                mRlFlashLight.setVisibility(View.GONE);
            }

            mIvTakeAgain.setVisibility(View.GONE);
            mIvConfirm.setVisibility(View.GONE);
            mCameraManager.setCameraDirection(mCameraId);
            if (mCameraId == CameraManager.CameraDirection.CAMERA_FRONT) {
                mSensorControler.lockFocus();
            } else {
                mSensorControler.unlockFocus();
            }
        } else {
            //            toast("切换失败，请重试！", Toast.LENGTH_LONG);
        }

        if (mSwitchCameraCallBack != null) {
            mSwitchCameraCallBack.switchCamera(isSwitchFromFront);
        }
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        switch (what) {
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED://最大文件长度限制10M，播放器会结束，此时不一定已经结束了
                ToastUtil.INSTANCE.showShort(getString(R.string.max_video_stop));
                mChronometer.stop();
                stopMediaRecorder();
                processStopRecorder();
                break;
        }
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {

    }

    public void setSwitchCameraCallBack(SwitchCameraCallBack mSwitchCameraCallBack) {
        this.mSwitchCameraCallBack = mSwitchCameraCallBack;
    }

    public void switchFlashMode() {
        CameraManager.FlashLigthStatus next = mCameraManager.getLightStatus().next();
        if (mPhotoOrVideo == 1) {
            next = mCameraManager.getLightStatus().next();
        }
        turnLight(next);
    }

    public boolean isBackCamera() {
        return mCameraId == CameraManager.CameraDirection.CAMERA_BACK;
    }

    private void initView() {
        mLlSelectPhotoOrVideo = (LinearLayout) findViewById(R.id.llSelectPhotoOrVideo);
        mIvRecorderPlay = (ImageView) findViewById(R.id.iv_recorder_play);
        //        mTvFlashLight = (TextView) findViewById(R.id.tv_flashlight);
        mRlFlashLight = (RelativeLayout) findViewById(R.id.rl_flashlight);
        mIvFlashLightOpen = (ImageView) findViewById(R.id.iv_flashlight_open);
        mIvFlashLightClose = (ImageView) findViewById(R.id.iv_flashlight_close);
        mIvFlashLightAuto = (ImageView) findViewById(R.id.iv_flashlight_auto);
        mIvCameraDireation = (ImageView) findViewById(R.id.iv_camera_direction);
        mFocusImageView = (FocusImageView) findViewById(R.id.focusImageView);
        mSurfaceView = (SurfaceView) findViewById(R.id.sv_surfaceview);
        mIvTakePhoto = (ImageView) findViewById(R.id.ivTakephoto);
        mIvTakeVideo = (ImageView) findViewById(R.id.ivTakeVideo);
        mIvConfirm = (TextView) findViewById(R.id.tv_confirm);
        mIvTakeAgain = (TextView) findViewById(R.id.tv_take_again);
        mTvPhoto = (TextView) findViewById(R.id.tvPhoto);
        mTvPhoto.setSelected(true);
        mTvVideo = (TextView) findViewById(R.id.tvVideo);
        mTvVideo.setSelected(false);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mIvRecorderStop = (ImageView) findViewById(R.id.iv_recorder_stop);
        mSensorControler = SensorControler.getInstance();
        mCameraManager = CameraManager.getInstance(this);
        mCameraId = mCameraManager.getCameraDirection();
    }

    private void initData() {
        mSurfaceholder = mSurfaceView.getHolder();
        mSurfaceholder.addCallback(callback);
        mSurfaceholder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        if (getIntent().getIntExtra("type", 0) == 1) {
            mTvPhoto.setVisibility(View.GONE);
            mTvVideo.setVisibility(View.GONE);
            setVideo();
        }
    }

    private void setVideo() {
        mPhotoOrVideo = 1;
        mIvCameraDireation.setVisibility(View.VISIBLE);
        mChronometer.setVisibility(View.VISIBLE);
        mIvTakeVideo.setVisibility(View.GONE);
        mIvTakeAgain.setVisibility(View.GONE);
        mIvConfirm.setVisibility(View.GONE);
        mTvPhoto.setSelected(false);
        mTvVideo.setSelected(true);
        mIvTakePhoto.setVisibility(View.GONE);
        mIvTakeVideo.setVisibility(View.VISIBLE);
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void initPreview() {
        try {
            //权限提示
            PermissionUtils.INSTANCE.camera(CameraActivity.this, () -> {
                needCameraPermission = false;
                if (mCamera == null) {
                    mCamera = Camera.open(
                        mCameraId == CameraManager.CameraDirection.CAMERA_BACK ? 0 : 1);
                }
                initCamera();
                setFocusWay();
                mIsPreview = true;
                mIvTakeAgain.setVisibility(View.GONE);
                mIvConfirm.setVisibility(View.GONE);
                if (mPhotoOrVideo == 1) {
                    startMediaRecorder();
                }
                return null;
            });
        } catch (RuntimeException e) {
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
            finish();
            e.printStackTrace();
        }

        // 获取摄像头的所有支持的分辨率
        List<Camera.Size> resolutionList = RecorderVideoUtils.getResolutionList(mCamera);
        if (resolutionList != null && resolutionList.size() > 0) {
            Collections.sort(resolutionList, new RecorderVideoUtils.ResolutionComparator());

            Camera.Size previewSize = null;
            boolean hasSize = false;
            for (int i = 0; i < resolutionList.size(); i++) {
                Camera.Size size = resolutionList.get(i);
                if (size != null) {
                    Log.d(TAG, "Camera 支持: " + size.width + "x" + size.height);
                }
            }
            // 如果摄像头支持640*480，那么强制设为640*480  1280x720
            for (int i = 0; i < resolutionList.size(); i++) {
                Camera.Size size = resolutionList.get(i);
                if (size != null) {
                    boolean is720 = (size.width == 720 && size.height == 1280) || (size.width == 1280 && size.height == 720);
                    boolean is480 = (size.width == 480 && size.height == 640) || (size.width == 640 && size.height == 480);
                    //                    if (is720){
                    //                        previewSize = size;
                    //                        previewWidth = previewSize.width;
                    //                        previewHeight = previewSize.height;
                    //                        hasSize = true;
                    //                    } else
                    if (is480) {
                        previewSize = size;
                        previewWidth = 640;
                        previewHeight = 480;
                        hasSize = true;
                    }
                }
            }
            // 如果不支持设为中间的那个
            if (!hasSize) {
                int mediumResolution = resolutionList.size() / 2;
                if (mediumResolution >= resolutionList.size()) {
                    mediumResolution = resolutionList.size() - 1;
                }
                previewSize = resolutionList.get(mediumResolution);
                previewWidth = previewSize.width;
                previewHeight = previewSize.height;
            }
            ULogToDevice.d("test", TAG, "摄像头支持 宽:" + previewWidth + " #高=" + previewHeight);
        }
    }

    private void setFocusWay() {
        if (mPhotoOrVideo == 0) {
            mSensorControler.restFoucs();
            if (mCameraId == CameraManager.CameraDirection.CAMERA_FRONT) {//0后置，1前置
                mSensorControler.lockFocus();
            } else {
                mSensorControler.unlockFocus();
            }
        } else if (mPhotoOrVideo == 1) {//录像
            mSensorControler.lockFocus();//禁用传感器
        }
    }

    private void initCamera() {
        mCameraParams = mCamera.getParameters();
        setCameraDisplayOrientation(this,
            mCameraId == CameraManager.CameraDirection.CAMERA_BACK ? 0 : 1, mCamera);
        if (mCameraId == CameraManager.CameraDirection.CAMERA_BACK) {
            if (mPhotoOrVideo == 1) {
                mCameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else {
                mCameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
        }

        mCameraParams.setPictureFormat(ImageFormat.JPEG);//分别设置图片格式，以及对焦模式。
        //重置对焦计数
        //        mCameraParams.setRotation(90);
        //        mCameraParams.set("orientation","portrait");
        //        if (mCameraParams.getSupportedFocusModes().contains(
        //                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
        //            mCameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        //        }
        //        mCameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);


        //        if (mCameraParams.getSupportedFocusModes().contains(
        //                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
        //            mCameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        //        }
        //        mCamera.cancelAutoFocus();
        setPicSize();
        setPrevSize();
        //调整控件的布局  防止预览被拉伸
        adjustView(mCameraParams.getPreviewSize());
        try {
            mCamera.setParameters(mCameraParams);
            mCamera.setPreviewDisplay(mSurfaceholder);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
        turnLight(mCameraManager.getLightStatus());  //设置闪光灯
        handleSurfaceChanged();
    }

    private void handleSurfaceChanged() {
        if (mCamera == null) {
            finish();
            return;
        }
        boolean hasSupportRate = false;

        //获取受支持的预览帧速率。
        List<Integer> supportedPreviewFrameRates = mCamera.getParameters()
            .getSupportedPreviewFrameRates();

        if (supportedPreviewFrameRates != null && supportedPreviewFrameRates.size() > 0) {
            Collections.sort(supportedPreviewFrameRates);
            for (int i = 0; i < supportedPreviewFrameRates.size(); i++) {
                int supportRate = supportedPreviewFrameRates.get(i);
                if (supportRate == 15) {
                    hasSupportRate = true;
                }
            }
            if (hasSupportRate) {
                mDefaultVideoFrameRate = 15;
            } else {
                mDefaultVideoFrameRate = supportedPreviewFrameRates.get(0);
            }
        }

        // 获取摄像头的所有支持的分辨率
        List<Camera.Size> resolutionList = RecorderVideoUtils.getResolutionList(mCamera);
        if (resolutionList != null && resolutionList.size() > 0) {
            Collections.sort(resolutionList, new RecorderVideoUtils.ResolutionComparator());

            Camera.Size previewSize = null;
            boolean hasSize = false;

            // 如果摄像头支持640*480，那么强制设为640*480
            for (int i = 0; i < resolutionList.size(); i++) {
                Camera.Size size = resolutionList.get(i);
                if (size != null && size.width == 640 && size.height == 480) {
                    previewSize = size;
                    hasSize = true;
                    break;
                }
            }
            // 如果不支持设为中间的那个
            if (!hasSize) {
                int mediumResolution = resolutionList.size() / 2;
                if (mediumResolution >= resolutionList.size()) {
                    mediumResolution = resolutionList.size() - 1;
                }
                previewSize = resolutionList.get(mediumResolution);
            }

        }
    }

    /**
     * 谷歌提供的设置竖屏拍照要调用的方法，默认横屏的
     */
    public void setCameraDisplayOrientation(Activity activity,//然后因为我这里是竖屏拍照，所以还需要对Camera旋转90度。
                                            int cameraId, Camera camera) {
        Camera.CameraInfo info =
            new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        mRotation = activity.getWindowManager().getDefaultDisplay()//旋转的角度，待会要按照这个角度旋转照片
            .getRotation();
        int degrees = 0;
        switch (mRotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mRotationResult = (info.orientation + degrees) % 360;
            mRotationResult = (360 - mRotationResult) % 360;  // compensate the mirror
        } else {  // back-facing
            mRotationResult = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(mRotationResult);
        Log.d(TAG, "横竖屏: " + mRotationResult);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            mCamera.setDisplayOrientation(0);
        } else {
            //竖屏
            mCamera.setDisplayOrientation(90);
        }
    }

    private void initListener() {

        mIvTakePhoto.setOnClickListener(onClickListener);
        mIvTakeVideo.setOnClickListener(onClickListener);
        mIvRecorderStop.setOnClickListener(onClickListener);
        mIvRecorderPlay.setOnClickListener(onClickListener);
        mIvConfirm.setOnClickListener(onClickListener);
        mIvTakeAgain.setOnClickListener(onClickListener);
        mTvPhoto.setOnClickListener(onClickListener);
        mTvVideo.setOnClickListener(onClickListener);
        mIvFlashLightOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //                mCameraManager.setLightStatus(CameraManager.FlashLigthStatus.LIGTH_ON);
                switchFlashMode();
            }
        });
        mIvFlashLightAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //                mCameraManager.setLightStatus(CameraManager.FlashLigthStatus.LIGHT_AUTO);
                switchFlashMode();
            }
        });
        mIvFlashLightClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //                mCameraManager.setLightStatus(CameraManager.FlashLigthStatus.LIGHT_AUTO);
                switchFlashMode();
            }
        });
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
            }
        });
        mSensorControler.setCameraFocusListener(new SensorControler.CameraFocusListener() {
            @Override
            public void onFocus() {
                int screenWidth = ScreenUtils.getScreenWidth(CameraActivity.this);
                Point point = new Point(screenWidth / 2, screenWidth / 2);

                onCameraFocus(point);
            }
        });
        mIvCameraDireation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIvCameraDireation.setClickable(false);
                switchCamera();

                //500ms后才能再次点击
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIvCameraDireation.setClickable(true);
                    }
                }, 500);
            }
        });
    }

    /**
     * 闪光灯开关   开->关->自动
     */
    private void turnLight(CameraManager.FlashLigthStatus ligthStatus) {
        if (CameraManager.mFlashLightNotSupport.contains(ligthStatus)) {
            turnLight(ligthStatus.next());
            return;
        }

        if (mCamera == null || mCamera.getParameters() == null
            || mCamera.getParameters().getSupportedFlashModes() == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> supportedModes = mCamera.getParameters().getSupportedFlashModes();

        switch (ligthStatus) {//顺序，on off auto
            case LIGHT_AUTO:
                //            case LIGTH_OFF:
                if (supportedModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    mIvFlashLightAuto.setVisibility(View.VISIBLE);
                    mIvFlashLightOpen.setVisibility(View.GONE);
                    mIvFlashLightClose.setVisibility(View.GONE);
                }
                break;
            case LIGTH_OFF:
                //            case LIGHT_ON:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mIvFlashLightAuto.setVisibility(View.GONE);
                mIvFlashLightOpen.setVisibility(View.GONE);
                mIvFlashLightClose.setVisibility(View.VISIBLE);
                break;
            case LIGHT_ON://
                //            case LIGHT_AUTO://
                if (supportedModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                } else if (supportedModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                } else if (supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
                mIvFlashLightAuto.setVisibility(View.GONE);
                mIvFlashLightOpen.setVisibility(View.VISIBLE);
                mIvFlashLightClose.setVisibility(View.GONE);
                break;
        }
        try {
            mCamera.setParameters(parameters);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        mCameraManager.setLightStatus(ligthStatus);
    }

    /**
     * 相机对焦  默认不需要延时
     */
    private void onCameraFocus(final Point point) {
        onCameraFocus(point, false);
    }

    private void setPicSize() {
        // 设置pictureSize
        // 短边比长边
        final float ratio = (float) mSurfaceView.getWidth() / mSurfaceView.getHeight();
        List<Camera.Size> pictureSizes = mCameraParams.getSupportedPictureSizes();
        //        if (mBestPictureSize == null) {
        mBestPictureSize = findBestPictureSize(pictureSizes, mCameraParams.getPictureSize(), ratio);
        //        }
        mCameraParams.setPictureSize(mBestPictureSize.width,//2240,3968//3968,2240//3264,1840
            mBestPictureSize.height);//2592,1456//4160.2336
    }

    /**
     * 找到短边比长边大于于所接受的最小比例的最大尺寸
     *
     * @param sizes       支持的尺寸列表
     * @param defaultSize 默认大小
     * @param minRatio    相机图片短边比长边所接受的最小比例
     * @return 返回计算之后的尺寸
     */
    private Camera.Size findBestPictureSize(List<Camera.Size> sizes, Camera.Size defaultSize,
                                            float minRatio) {
        final int MIN_PIXELS = 320 * 480;

        sortSizes(sizes);
        Camera.Size rightSize = sizes.get(0);
        Camera.Size tempSize = sizes.get(0);//H2976 W3968
        float preRatioDiff = Math.abs(
            (float) rightSize.height / rightSize.width - minRatio);//初始的比率差值//0.593
        Iterator<Camera.Size> it = sizes.iterator();
        while (it.hasNext()) {
            Camera.Size size = it.next();
            //移除不满足比例的尺寸
            //            if ((float) size.height / size.width < minRatio) {
            //                it.remove();
            //                continue;
            //            }
            float currentRatioDiff = Math.abs((float) size.height / size.width - minRatio);//比率差值
            if (currentRatioDiff < 0.02) {
                return size;
            }
            if (currentRatioDiff < preRatioDiff) {//如果当前的比率比之前存的比率更接近minRatio，就取当前比率
                rightSize = size;
                preRatioDiff = currentRatioDiff;
            }

            //移除太小的尺寸
            if (size.width * size.height <= MIN_PIXELS) {
                it.remove();
            }
        }

        if (rightSize.width != tempSize.width
            || rightSize.height != tempSize.height) {//如果有合适的size就返回最大的那个//返回更接近的那个
            //            int height = rightSize.height;
            //            rightSize.height = rightSize.width;//颠倒
            //            rightSize.width = height;
            return rightSize;
        }
        // 返回符合条件中最大尺寸的一个
        if (!sizes.isEmpty()) {
            //            int height = rightSize.height;
            //            tempSize.height = rightSize.width;//颠倒
            //            tempSize.width = height;
            return tempSize;
        }
        //        int height = defaultSize.height;
        //        defaultSize.height = defaultSize.width;//颠倒
        //        defaultSize.width = height;
        return defaultSize;
    }

    private void setPrevSize() {
        // 设置previewSize
        // 短边比长边//反了
        final float ratio = (float) mSurfaceView.getWidth() / mSurfaceView.getHeight();
        List<Camera.Size> previewSizes = mCameraParams.getSupportedPreviewSizes();
        //        if (mBestPreviewSize == null) {
        mBestPreviewSize = findBestPreviewSize(previewSizes, mCameraParams.getPreviewSize(),
            mBestPictureSize, ratio);
        //        }
        //        mCameraParams.setPreviewSize(mBestPreviewSize.width, mBestPreviewSize.height);
        mCameraParams.setPreviewSize(mBestPreviewSize.width,//960,544//1920,1080
            mBestPreviewSize.height);//1280.720//1920.1088


    }

    /**
     * @param pictureSize 图片的大小
     * @param minRatio    preview短边比长边所接受的最小比例
     */
    private Camera.Size findBestPreviewSize(List<Camera.Size> sizes, Camera.Size defaultSize,
                                            Camera.Size pictureSize, float minRatio) {
        final int pictureWidth = pictureSize.width;
        final int pictureHeight = pictureSize.height;
        boolean isBestSize = (pictureHeight / (float) pictureWidth) > minRatio;
        sortSizes(sizes);
        Camera.Size rightSize = sizes.get(0);
        Camera.Size tempSize = sizes.get(0);
        float preRatioDiff = Math.abs(
            (float) rightSize.height / rightSize.width - minRatio);//初始的比率差值
        Iterator<Camera.Size> it = sizes.iterator();
        while (it.hasNext()) {
            Camera.Size size = it.next();
            //            if ((float) size.height / size.width < minRatio) {
            //                it.remove();
            //                continue;
            //            }
            float currentRatioDiff = Math.abs((float) size.height / size.width - minRatio);//初始的比率差值
            if (currentRatioDiff < preRatioDiff) {//如果当前的比率比之前存的比率更接近minRatio，就取当前比率
                rightSize = size;
                preRatioDiff = currentRatioDiff;
            }
            // 找到同样的比例，直接返回
            if (isBestSize && size.width * pictureHeight == size.height * pictureWidth) {
                return size;
            }
            if (currentRatioDiff < 0.02) {
                return size;
            }
        }
        if (rightSize.width != tempSize.width
            && rightSize.height != tempSize.height) {//如果有合适的size就返回最大的那个//返回更接近的那个
            return rightSize;
        }
        // 未找到同样的比例的，返回尺寸最大的
        if (!sizes.isEmpty()) {
            return tempSize;
        }

        return defaultSize;
    }

    /**
     * 调整SurfaceView的宽高
     */
    private void adjustView(Camera.Size adapterSize) {
        int width = ScreenUtils.getScreenWidth(this);
        int height = width * adapterSize.width / adapterSize.height;

        //让surfaceView的中心和FrameLayout的中心对齐

        RelativeLayout.LayoutParams params =
            (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
        params.topMargin = -(height - width) / 2;
        params.width = width;
        params.height = height;
        mSurfaceView.setLayoutParams(params);
    }

    /**
     * 请求自动对焦
     */
    private void requestFocus() {
        if (mCamera == null) {
            return;
        }
        mCamera.autoFocus(null);
    }

    private void stop() {
        mIvRecorderStop.setEnabled(false);
        // 停止拍摄
        stopMediaRecorder();

        mChronometer.stop();
        mIvTakeVideo.setVisibility(View.VISIBLE);
        mIvRecorderStop.setVisibility(View.INVISIBLE);
    }

    private void processStopRecorder() {
        mLightStatusBefore = mCameraManager.getLightStatus();
        turnLight(CameraManager.FlashLigthStatus.LIGTH_OFF);//关闪光灯
        if (mCamera != null) {
            mCamera.stopPreview();
        }
        mRlFlashLight.setVisibility(View.GONE);//隐藏闪光灯图标
        mIsPreview = false;
        mIvCameraDireation.setVisibility(View.GONE);
        mIvRecorderPlay.setVisibility(View.VISIBLE);
        mIvTakeVideo.setVisibility(View.GONE);//play展示，其他隐藏
        mIvRecorderStop.setVisibility(View.GONE);
        mIvTakeAgain.setVisibility(View.VISIBLE);
        mIvConfirm.setVisibility(View.VISIBLE);
        mIvTakePhoto.setVisibility(View.GONE);
        /* 显示使用照片或者使用视频 */
        switch (mPhotoOrVideo) {
            case 0:
                mIvConfirm.setText(getString(R.string.use_picture));
                break;
            case 1:
                mIvConfirm.setText(getString(R.string.use_video));
                break;
            default:
                mIvConfirm.setText(getString(R.string.use_picture));
                break;
        }

        mLlSelectPhotoOrVideo.setVisibility(View.GONE);
        mIvRecorderStop.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 788) {
            //            if (callback != null) {
            //                mSurfaceholder.addCallback(callback);
            //            }

            mHandler.postDelayed(runnable, 100);
        }
    }

    private File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), "Camera App");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("linc", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        //        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mImagePath = PathUtils2.getPhotoPath() + File.separator + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(System.currentTimeMillis()) + ".jpg";
            Log.e(TAG,
                "Camare:创建路径：PathUtilsEaseui.getPhotoPath()==null :   " + (PathUtils2.getPhotoPath()
                    == null));
            Log.e(TAG, "Camare:创建路径：EMClient.getInstance().getCurrentUser()==null");
            Log.e(TAG, "Camare:创建路径：ImagePath==null :   " + (mImagePath == null));
            //            mImagePath = Environment.getExternalStorageDirectory().getPath() + "/" + System
            // .currentTimeMillis() + ".jpg";


            //            mImagePath = mediaStorageDir.getPath() + File.separator +
            //                    "IMG_"+ timeStamp + ".jpg";
            mediaFile = new File(mImagePath);
        } else if (type == MEDIA_TYPE_VIDEO) {
            mVideoPath = PathUtils2.getVideoPath() + File.separator + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(System.currentTimeMillis()) + ".mp4";
            //            mVideoPath = Environment.getExternalStorageDirectory().getPath() + "/" + System
            // .currentTimeMillis() + ".mp4";
            //            mVideoPath = mediaStorageDir.getPath() + File.separator +
            //                    "VID_"+ timeStamp + ".mp4";
            mediaFile = new File(mVideoPath);
        } else {
            return null;
        }

        return mediaFile;
    }

    private void startMediaRecorder() {

        if (mCamera == null) {
            return;
        }
        //        closeCamera();//关闭相机，再重新初始化，否则无法设置参数
        mPhotoOrVideo = 1;
        //        initPreview();
        //        setFocusWay();
        //        initCamera();
        mCamera.unlock();//一定要调用，否则start报错
        mCamera.stopPreview();//一定要调用

        //        mCamera.setParameters(mCameraParams);
        mIsRecording = true;
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();
        mMediaRecorder.setCamera(mCamera);
        if (mCameraId == CameraManager.CameraDirection.CAMERA_FRONT) {
            mMediaRecorder.setOrientationHint(270);
        } else {
            mMediaRecorder.setOrientationHint(90);
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(CAMERA);// 设置录制视频源为Camera（相机）
        // 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        // 设置录制的视频编码h263 h264
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        // 设置视频的比特率
        //        mMediaRecorder.setVideoEncodingBitRate(384 * 1024);
        mMediaRecorder.setVideoEncodingBitRate(1 * 1024 * 1024);// 设置帧频率，然后就清晰了
        if (mDefaultVideoFrameRate != -1) {
            mMediaRecorder.setVideoFrameRate(mDefaultVideoFrameRate);
        }
        //        CamcorderProfile mCamcorderProfile = CamcorderProfile.get(Camera.CameraInfo
        // .CAMERA_FACING_BACK,
        //                CamcorderProfile.QUALITY_HIGH);
        //        mMediaRecorder.setProfile(mCamcorderProfile);
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
        //        mMediaRecorder.setMaxDuration(30000);
        if (getIntent().getIntExtra("type", 0) == 1) {
            mMediaRecorder.setMaxDuration(3 * 60 * 1000);//佛友圈3分钟限制文件大小
        } else {
            mMediaRecorder.setMaxFileSize(VIDEO_CHAT_CAMERA_SIZE_MAX);//10M限制文件大小
        }
        mMediaRecorder.setPreviewDisplay(mSurfaceholder.getSurface());
        //花屏的解决方案：在预览的时候，开启预览，不解锁相机，在拍摄的时候，关闭预览，解锁相机。
        //        mMediaRecorder.setVideoSize(mBestPreviewSize.width, mBestPreviewSize.height);
        mMediaRecorder.setVideoSize(previewWidth, previewHeight);
        Log.d(TAG, "startMediaRecorder: previewWidth=" + previewWidth + " #previewHeight=" + previewHeight);
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.setOnInfoListener(this);
            mMediaRecorder.setOnErrorListener(this);
            mMediaRecorder.start();

        } catch (Exception e) {
            mIsRecording = false;
            //            Toast.makeText(this, "fail", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            mCamera.lock();
        }
        mChronometer.setBase(SystemClock.elapsedRealtime());//复位
        mChronometer.start();
    }

    private void stopMediaRecorder() {
        if (mMediaRecorder != null) {
            if (mIsRecording) {
                try {
                    mMediaRecorder.stop();
                    mCamera.lock();
                    mMediaRecorder.reset();
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                    mIsRecording = false;

                    mCamera.reconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //把申请权限的回调交由PermissionUtils处理
        PermissionUtils.INSTANCE.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        return super.shouldShowRequestPermissionRationale(permission);

    }

    private void setMediaPlayer() {
        if (mSurfaceView == null) {
            return;
        }

        //下面开始实例化MediaPlayer对象
        mMediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {


            }
        });
        mMediaplayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        Log.e("Play Error:::", "MEDIA_ERROR_SERVER_DIED");
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        Log.e("Play Error:::", "MEDIA_ERROR_UNKNOWN");
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        mMediaplayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                //      当一些特定信息出现或者警告时触发
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                        break;
                    case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                        break;
                    case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                        break;
                    case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                        break;
                }
                return false;
            }
        });
        mMediaplayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // 当prepare完成后，该方法触发，在这里我们播放视频
                //给ui 界面发送消息 这里有个延时是设置 如果不设置延时 会出现 获得视频的高宽为零的文件//无用

                //首先取得video的宽和高
                mVideoWidth = mMediaplayer.getVideoWidth();
                mVideoHeight = mMediaplayer.getVideoHeight();

                if (mVideoWidth > mCurrDisplay.getWidth() || mVideoHeight > mCurrDisplay.getHeight()) {
                    //如果video的宽或者高超出了当前屏幕的大小，则要进行缩放
                    float wRatio = (float) mVideoWidth / (float) mCurrDisplay.getWidth();
                    float hRatio = (float) mVideoHeight / (float) mCurrDisplay.getHeight();

                    //选择大的一个进行缩放
                    float ratio = Math.max(wRatio, hRatio);

                    mVideoWidth = (int) Math.ceil((float) mVideoWidth / ratio);
                    mVideoHeight = (int) Math.ceil((float) mVideoHeight / ratio);

                    //设置surfaceView的布局参数
                    mSurfaceView.setLayoutParams(new LinearLayout.LayoutParams(mVideoWidth, mVideoHeight));

                    //然后开始播放视频

                    mMediaplayer.start();
                } else {
                    mMediaplayer.start();
                }
            }
        });
        //        mMediaplayer.setOnSeekCompleteListener(this);
        //        mMediaplayer.setOnVideoSizeChangedListener(this);

        try {
            mMediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaplayer.setDataSource(mVideoPath);
            Log.v("Next:::", "surfaceDestroyed called");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //然后，我们取得当前Display对象
        mCurrDisplay = this.getWindowManager().getDefaultDisplay();
        // 当SurfaceView中的Surface被创建的时候被调用
        //在这里我们指定MediaPlayer在当前的Surface中进行播放
        //        mSurfaceholder.removeCallback(callback);
        mMediaplayer.setDisplay(mSurfaceView.getHolder());
        //        surfaceHolder.setFixedSize(100, 100);
        //        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //在指定了MediaPlayer播放的容器后，我们就可以使用prepare或者prepareAsync来准备播放了
        mMediaplayer.prepareAsync();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "CameraActivity被销毁了");
    }

    public interface SwitchCameraCallBack {
        void switchCamera(boolean isSwitchFromFront);
    }
}
