package com.shoroo.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    static boolean visible = false;
    private WebView web;

    private PendingIntent alarmIntent(boolean brk) {
        Intent i = new Intent(this, TimerReceiver.class);
        i.putExtra("brk", brk);
        return PendingIntent.getBroadcast(this, 0, i,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    class Bridge {
        @JavascriptInterface
        public void schedule(int seconds, boolean brk) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            PendingIntent show = PendingIntent.getActivity(MainActivity.this, 2,
                    new Intent(MainActivity.this, MainActivity.class),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            long when = System.currentTimeMillis() + seconds * 1000L;
            am.setAlarmClock(new AlarmManager.AlarmClockInfo(when, show), alarmIntent(brk));
            try {
                Intent si = new Intent(MainActivity.this, TimerService.class);
                si.putExtra("sec", seconds);
                si.putExtra("brk", brk);
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(si); else startService(si);
            } catch (Exception e) { /* alarm still works */ }
        }

        @JavascriptInterface
        public void cancel() {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.cancel(alarmIntent(false));
            stopService(new Intent(MainActivity.this, TimerService.class));
        }
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(0xFF0E7C74);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        web.addJavascriptInterface(new Bridge(), "Android");
        web.setWebViewClient(new WebViewClient());
        setContentView(web);
        if (b == null) web.loadUrl("file:///android_asset/index.html");
        else web.restoreState(b);
    }

    @Override
    protected void onResume() { super.onResume(); visible = true; }

    @Override
    protected void onPause() { super.onPause(); visible = false; }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
