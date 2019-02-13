package com.mogujie.tt.imservice.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by wr
 * Date: 2019/1/16  20:40
 * mail: 1902065822@qq.com
 * describe:
 */
public class HelpService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//            NotificationChannel mChannel = new NotificationChannel(IMService.CHANNEL_ID_STRING, "聊天", NotificationManager.IMPORTANCE_HIGH);
//            notificationManager.createNotificationChannel(mChannel);
//            Notification notification = new Notification.Builder(getApplicationContext(), IMService.CHANNEL_ID_STRING).build();
            startForeground(IMService.IMServiceNotificaId, new Notification());
        }
        stopForeground(true);
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }
}