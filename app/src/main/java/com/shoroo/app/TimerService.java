package com.shoroo.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

/** Keeps the app alive while a session runs and shows a live countdown in the notification shade. */
public class TimerService extends Service {
    private final Handler h = new Handler(Looper.getMainLooper());

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        if (i == null) { stopSelf(); return START_NOT_STICKY; }
        int sec = i.getIntExtra("sec", 0);
        boolean brk = i.getBooleanExtra("brk", false);
        String ch = "timer_run";
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel x = new NotificationChannel(ch, "Running timer", NotificationManager.IMPORTANCE_LOW);
            x.setSound(null, null);
            nm.createNotificationChannel(x);
        }
        Intent open = new Intent(this, MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 3, open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, ch) : new Notification.Builder(this);
        b.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(brk ? "استراحت در جریان است" : "جلسهٔ تمرکز در جریان است")
                .setContentText("زمان باقی‌مانده در کنار این اعلان")
                .setOngoing(true)
                .setContentIntent(pi)
                .setWhen(System.currentTimeMillis() + sec * 1000L)
                .setShowWhen(true)
                .setUsesChronometer(true);
        if (Build.VERSION.SDK_INT >= 24) b.setChronometerCountDown(true);
        Notification n = b.build();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(11, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(11, n);
        }
        h.removeCallbacksAndMessages(null);
        h.postDelayed(new Runnable() {
            @Override public void run() { stopForeground(true); stopSelf(); }
        }, sec * 1000L);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() { h.removeCallbacksAndMessages(null); super.onDestroy(); }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
