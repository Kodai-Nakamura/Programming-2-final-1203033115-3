package jp.ac.thers.myapplications;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            CharSequence packageName = event.getPackageName();
            if (packageName != null) {
                Log.d(TAG, "Foreground app: " + packageName.toString());

                SharedPreferences prefs = getSharedPreferences("selected_apps", MODE_PRIVATE);
                Set<String> selectedApps = prefs.getStringSet("apps", new HashSet<>());

                if (selectedApps.contains(packageName.toString())) {
                    Log.d(TAG, "Selected app is in foreground, decreasing remaining time.");

                    SharedPreferences timePrefs = getSharedPreferences("remaining_time", MODE_PRIVATE);
                    long totalRemainingTime = timePrefs.getLong("total_remaining_time", 0);

                    totalRemainingTime -= 1000; // Reduce by 1 second

                    SharedPreferences.Editor editor = timePrefs.edit();
                    editor.putLong("total_remaining_time", totalRemainingTime);
                    editor.apply();

                    if (totalRemainingTime <= 0) {
                        Intent intent = new Intent(this, BlockingActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        // Required method, implement if necessary
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility Service connected");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        setServiceInfo(info);
    }
}
