package com.shoroo.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class TimerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        c.stopService(new Intent(c, TimerService.class));
        if (MainActivity.visible) return; // app is open: the page already shows the result
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        String ch = "timer";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel x = new NotificationChannel(ch, "Timer", NotificationManager.IMPORTANCE_HIGH);
            x.enableVibration(true);
            x.setVibrationPattern(new long[]{0, 400, 200, 400});
            nm.createNotificationChannel(x);
        }
        boolean brk = i.getBooleanExtra("brk", false);
        Intent open = new Intent(c, MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(c, 1, open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(c, ch) : new Notification.Builder(c);
        b.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(brk ? "استراحتت تمام شد" : "جلسهٔ تمرکز تمام شد")
                .setContentText(brk ? "برگرد سر کار" : "آفرین! حالا ۵ دقیقه استراحت کن")
                .setContentIntent(pi)
                .setAutoCancel(true);
        if (Build.VERSION.SDK_INT < 26) {
            b.setDefaults(Notification.DEFAULT_ALL).setPriority(Notification.PRIORITY_HIGH);
        }
        nm.notify(1, b.build());
    }
}
