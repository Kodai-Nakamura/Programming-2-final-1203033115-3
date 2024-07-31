package jp.ac.thers.myapplications;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TextView timeRemaining;
    private Button startButton;
    private Button recordButton;
    private Button gameListButton;
    private Handler handler;
    private static final int UPDATE_INTERVAL = 1000; // 1秒ごとに更新

    private static final int REQUEST_USAGE_STATS_PERMISSION = 1001;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeRemaining = findViewById(R.id.timeRemaining);
        startButton = findViewById(R.id.startButton);
        recordButton = findViewById(R.id.recordButton);
        gameListButton = findViewById(R.id.gameListButton);

        handler = new Handler();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StartActivity.class);
                startActivity(intent);
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                startActivity(intent);
            }
        });

        gameListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GameListActivity.class);
                startActivity(intent);
            }
        });

        if (checkUsageStatsPermission()) {
            startService(new Intent(MainActivity.this, AppUsageService.class));
        } else {
            requestUsageStatsPermission();
        }

        startUpdatingTimeRemaining();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUpdatingTimeRemaining();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUpdatingTimeRemaining();
    }

    private void startUpdatingTimeRemaining() {
        handler.post(updateRunnable);
    }

    private void stopUpdatingTimeRemaining() {
        handler.removeCallbacks(updateRunnable);
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimeRemaining();
            handler.postDelayed(this, UPDATE_INTERVAL);
        }
    };

    private void updateTimeRemaining() {
        SharedPreferences prefs = getSharedPreferences("remaining_time", MODE_PRIVATE);
        long totalRemainingTime = prefs.getLong("total_remaining_time", 0);
        int hours = (int) (totalRemainingTime / 3600000);
        int minutes = (int) ((totalRemainingTime % 3600000) / 60000);
        int seconds = (int) ((totalRemainingTime % 60000) / 1000);
        timeRemaining.setText(String.format("残り時間 %02d:%02d:%02d", hours, minutes, seconds));
        Log.d(TAG, "残り時間: " + totalRemainingTime);
    }

    private boolean checkUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void requestUsageStatsPermission() {
        Toast.makeText(this, "Please enable Usage Access", Toast.LENGTH_LONG).show();
        startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), REQUEST_USAGE_STATS_PERMISSION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_USAGE_STATS_PERMISSION) {
            if (checkUsageStatsPermission()) {
                startService(new Intent(this, AppUsageService.class));
            } else {
                Toast.makeText(this, "Usage Access Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
