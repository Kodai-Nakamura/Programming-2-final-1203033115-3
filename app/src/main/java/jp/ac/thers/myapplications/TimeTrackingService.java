package jp.ac.thers.myapplications;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TimeTrackingService extends Service {

    private Handler handler;
    private Runnable runnable;
    private Set<String> selectedApps;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        selectedApps = getSelectedApps();

        runnable = new Runnable() {
            @Override
            public void run() {
                if (isAnySelectedAppRunning()) {
                    decreaseRemainingTime();
                }
                handler.postDelayed(this, 60000); // 1 minute interval
            }
        };
        handler.post(runnable);
    }

    private Set<String> getSelectedApps() {
        SharedPreferences prefs = getSharedPreferences("selected_apps", MODE_PRIVATE);
        return prefs.getStringSet("apps", new HashSet<>());
    }

    private boolean isAnySelectedAppRunning() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
        long endTime = System.currentTimeMillis();
        long startTime = endTime - TimeUnit.MINUTES.toMillis(1);
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        for (UsageStats usageStats : usageStatsList) {
            if (selectedApps.contains(usageStats.getPackageName()) && usageStats.getLastTimeUsed() >= startTime) {
                return true;
            }
        }
        return false;
    }

    private void decreaseRemainingTime() {
        SharedPreferences prefs = getSharedPreferences("remaining_time", MODE_PRIVATE);
        int remainingTime = prefs.getInt("time", 0);
        if (remainingTime > 0) {
            remainingTime--;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("time", remainingTime);
            editor.apply();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.post(runnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
