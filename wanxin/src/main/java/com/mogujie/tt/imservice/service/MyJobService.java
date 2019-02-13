package com.mogujie.tt.imservice.service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.mogujie.tt.imservice.manager.IMSocketManager;
import com.mogujie.tt.utils.Logger;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by wr
 * Date: 2019/1/18  11:27
 * mail: 1902065822@qq.com
 * describe:
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MyJobService extends JobService {
    private Logger logger = Logger.getLogger(MyJobService.class);
    public static final int MSG_JOB_START = 0;
    public static final int MSG_JOB_STOP = 1;
    public static final int MSG_JOB_SEND = 2;
    public static final String WORK_DURATION_KEY = "WORK_DURATION_KEY";
    private static final String TAG = MyJobService.class.getSimpleName();

    private Messenger mActivityMessenger;

    @Override
    public void onCreate() {
        super.onCreate();
        logger.d("Service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.d("Service destroyed");
    }

    // 当应用程序的MainActivity被创建时，它启动这个服务。
    // 这是为了使活动和此服务可以来回通信。 请参见“setUiCallback（）”
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.d("Service onStartCommand");
        mActivityMessenger = intent.getParcelableExtra("messenger");
        startScheduler(this);
        return START_STICKY;
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        // The work that this service "does" is simply wait for a certain duration and finish
        // the job (on another thread).
        logger.d("Service onStartJob");
        // 该服务做的工作只是等待一定的持续时间并完成作业（在另一个线程上）。
        sendMessage(MSG_JOB_START, params.getJobId());

        lunxun(params.getJobId());

        //        jobFinished(params, false);
        // 返回true，很多工作都会执行这个地方，我们手动结束这个任务
        return true;
    }

    private void lunxun(final int jobId) {
        Observable.interval(10, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .subscribe(new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object aLong) {
                sendMessage(MSG_JOB_SEND, jobId);
                if (!IMSocketManager.instance().isSocketConnect()) {
                    logger.d("未连接");
                    IMSocketManager.instance().onConnectMsgServerFail();
                } else {
                    logger.d("连接");
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //                    startScheduler(MyJobService.this);
                    //                    jobFinished(params, false);
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        logger.d("Service onStopJob");
        sendMessage(MSG_JOB_STOP, params.getJobId());
        logger.d("on stop job: " + params.getJobId());
        //        startScheduler(this);
        // 返回false来销毁这个工作
        return false;
    }

    private void sendMessage(int messageID, @Nullable Object params) {
        // 如果此服务由JobScheduler启动，则没有回调Messenger。
        // 它仅在MainActivity在Intent中使用回调函数调用startService()时存在。
        if (mActivityMessenger == null) {
            logger.d("Service is bound, not started. There's no callback to send a message to.");
            return;
        }

        Message m = Message.obtain();
        m.what = messageID;
        m.obj = params;
        try {
            mActivityMessenger.send(m);
        } catch (RemoteException e) {
            logger.d("Error passing service object back to activity.");
        }
    }

    public static void startScheduler(Context context) {
        Log.d(MyJobService.class.getSimpleName(), "startScheduler");
        //获取JobScheduler 他是一种系统服务
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancelAll();
        JobInfo.Builder builder = new JobInfo.Builder(1024, new ComponentName(context.getPackageName(), MyJobService.class.getName()));
        Long time = 15 * 60 * 1000L;
        Long timeMin = 20 * 1000L;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //setPeriodic android N之后时间必须在15分钟以上
            builder.setPeriodic(time);
            //            //执行的最小延迟时间
            //            builder.setMinimumLatency(timeMin);
            ////            //执行的最长延时时间
            //            builder.setOverrideDeadline(timeMin * 2);
        } else {
            builder.setPeriodic(60 * 1000);
        }
        //线性重试方案
        builder.setBackoffCriteria(time, JobInfo.BACKOFF_POLICY_LINEAR);
        // 设置设备重启时，执行该任务
        builder.setPersisted(true);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        int schedule = jobScheduler.schedule(builder.build());
        if (schedule <= 0) {
            Log.d(MyJobService.class.getSimpleName(), "schedule error！");
        }
    }
}