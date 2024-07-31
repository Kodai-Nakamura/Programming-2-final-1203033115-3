package jp.ac.thers.myapplications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.HashSet;
import java.util.Set;

public class AppUsageService extends Service {

    private static final String TAG = "AppUsageService";
    private static final String CHANNEL_ID = "AppUsageServiceChannel";
    private Handler handler;
    private Runnable updateRunnable;
    private Set<String> selectedApps;
    private long lastUpdateTime;
    private long totalRemainingTime;
    private String lastForegroundApp;
    private WindowManager windowManager;
    private View overlayView;
    private boolean isOverlayVisible;
    private boolean isAppInForeground;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created, starting usage updates.");
        handler = new Handler();
        selectedApps = getSelectedApps();
        lastUpdateTime = System.currentTimeMillis();
        lastForegroundApp = "";
        isAppInForeground = false;

        // Initialize total remaining time from shared preferences
        SharedPreferences prefs = getSharedPreferences("remaining_time", MODE_PRIVATE);
        totalRemainingTime = prefs.getLong("total_remaining_time", 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "App Usage Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
            startForeground(1, createNotification());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        overlayView = inflater.inflate(R.layout.overlay_layout, null);

        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateAppUsage();
                handler.postDelayed(this, 1000); // 1 second interval
            }
        };
        handler.post(updateRunnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("add_remaining_time")) {
            long additionalTime = intent.getLongExtra("add_remaining_time", 0);
            addRemainingTime(additionalTime);
        }
        selectedApps = getSelectedApps();  // サービスの開始時に選択されたアプリを再ロードする
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Set<String> getSelectedApps() {
        SharedPreferences prefs = getSharedPreferences("selected_apps", MODE_PRIVATE);
        return prefs.getStringSet("apps", new HashSet<>());
    }

    private void saveRemainingTime(long time) {
        SharedPreferences prefs = getSharedPreferences("remaining_time", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("total_remaining_time", time);
        editor.apply();
    }

    private boolean canDrawOverlays() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void updateAppUsage() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastUpdateTime;

        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        long endTime = currentTime;
        long startTime = endTime - elapsedTime;

        UsageEvents usageEvents = usageStatsManager.queryEvents(startTime, endTime);

        isAppInForeground = false;
        Set<String> currentlyForegroundApps = new HashSet<>();

        UsageEvents.Event event = new UsageEvents.Event();
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                currentlyForegroundApps.add(event.getPackageName());
                lastForegroundApp = event.getPackageName();
                Log.d(TAG, "App moved to foreground: " + event.getPackageName());
            } else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                currentlyForegroundApps.remove(event.getPackageName());
                if (event.getPackageName().equals(lastForegroundApp)) {
                    lastForegroundApp = "";
                }
                Log.d(TAG, "App moved to background: " + event.getPackageName());
            }
        }

        for (String app : selectedApps) {
            if (currentlyForegroundApps.contains(app) || app.equals(lastForegroundApp)) {
                isAppInForeground = true;
                break;
            }
        }

        if (isAppInForeground) {
            Log.d(TAG, "Selected app is in foreground, decreasing remaining time.");
            totalRemainingTime -= 1000; // Reduce by 1 second for each update
            if (totalRemainingTime < 0) totalRemainingTime = 0; // Ensure remaining time is not negative
            saveRemainingTime(totalRemainingTime);
            updateNotification(); // Update the notification with new remaining time
        } else {
            Log.d(TAG, "No selected app is in foreground.");
        }

        if (totalRemainingTime <= 0 && isAppInForeground) {
            showTimeExpiredOverlay();
        } else {
            hideTimeExpiredOverlay();
        }

        lastUpdateTime = currentTime;

        // Log the total remaining time for debugging
        int hours = (int) (totalRemainingTime / 3600000);
        int minutes = (int) ((totalRemainingTime % 3600000) / 60000);
        int seconds = (int) ((totalRemainingTime % 60000) / 1000);
        Log.d(TAG, String.format("残り時間: %02d:%02d:%02d", hours, minutes, seconds));
    }

    private void addRemainingTime(long additionalTime) {
        totalRemainingTime += additionalTime;
        saveRemainingTime(totalRemainingTime);
        updateNotification(); // Update the notification with new remaining time
        Log.d(TAG, "Added remaining time: " + additionalTime);
        Log.d(TAG, "New total remaining time: " + totalRemainingTime);
    }

    private void updateNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(1, createNotification());
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText(String.format("残り時間: %02d:%02d:%02d",
                        totalRemainingTime / 3600000,
                        (totalRemainingTime % 3600000) / 60000,
                        (totalRemainingTime % 60000) / 1000))
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
    }

    private void showTimeExpiredOverlay() {
        if (!isOverlayVisible && canDrawOverlays()) {
            Log.d(TAG, "Showing overlay");

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);

            params.gravity = Gravity.CENTER;

            Button okButton = overlayView.findViewById(R.id.okButton);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "OK button clicked, hiding overlay and returning to home screen");
                    hideTimeExpiredOverlay();
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startMain);
                }
            });

            windowManager.addView(overlayView, params);
            isOverlayVisible = true;
        } else {
            Log.d(TAG, "Overlay not shown, either already visible or missing permission");
        }
    }

    private void hideTimeExpiredOverlay() {
        if (isOverlayVisible) {
            windowManager.removeView(overlayView);
            isOverlayVisible = false;
        }
    }
}
