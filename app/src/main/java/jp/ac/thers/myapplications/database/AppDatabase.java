package jp.ac.thers.myapplications.database;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.room.Room;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import jp.ac.thers.myapplications.database.AppDatabase;
import jp.ac.thers.myapplications.database.Record;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StartActivity extends AppCompatActivity {

    private TextView exerciseTime;
    private TextView distanceCovered;
    private Button stopButton;
    private Button restartButton;
    private Button finishButton;

    private Handler handler;
    private long startTime, timeInMilliseconds, updatedTime, pauseTime;
    private int seconds, minutes;

    private boolean isRunning;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location lastLocation;
    private float totalDistance;

    private AppDatabase db;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        exerciseTime = findViewById(R.id.exerciseTime);
        distanceCovered = findViewById(R.id.distanceCovered);
        stopButton = findViewById(R.id.stopButton);
        restartButton = findViewById(R.id.restartButton);
        finishButton = findViewById(R.id.finishButton);

        handler = new Handler();
        startTime = 0L;
        timeInMilliseconds = 0L;
        updatedTime = 0L;
        pauseTime = 0L;

        totalDistance = 0f;
        lastLocation = null;

        // データベースインスタンスの取得
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "records-db").build();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (lastLocation != null) {
                        totalDistance += lastLocation.distanceTo(location);
                    }
                    lastLocation = location;
                    distanceCovered.setText(String.format("Distance: %.2f meters", totalDistance));
                }
            }
        };

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning) {
                    pauseTime = SystemClock.uptimeMillis();
                    handler.removeCallbacks(updateTimerThread);
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                    isRunning = false;
                    stopButton.setVisibility(View.GONE);
                    restartButton.setVisibility(View.VISIBLE);
                }
            }
        });

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime += SystemClock.uptimeMillis() - pauseTime;
                handler.postDelayed(updateTimerThread, 0);
                startLocationUpdates();
                isRunning = true;
                stopButton.setVisibility(View.VISIBLE);
                restartButton.setVisibility(View.GONE);
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(updateTimerThread);
                fusedLocationClient.removeLocationUpdates(locationCallback);

                // データを保存
                saveRecord();

                finish();
            }
        });

        // 運動を開始
        startExercise();
    }

    private void startExercise() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startTime = SystemClock.uptimeMillis();
            handler.postDelayed(updateTimerThread, 0);
            startLocationUpdates();
            isRunning = true;
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = pauseTime + timeInMilliseconds;

            seconds = (int) (updatedTime / 1000);
            minutes = seconds / 60;
            seconds = seconds % 60;

            exerciseTime.setText(String.format("Exercise Time: %02d:%02d", minutes, seconds));

            handler.postDelayed(this, 1000);
        }
    };

    private void saveRecord() {
        // 運動の終了時間を計算
        int totalTimeInMinutes = (int) (updatedTime / 60000);

        // 現在の日付を取得
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // レコードを作成
        Record record = new Record();
        record.date = currentDate;
        record.distance = totalDistance;
        record.exerciseTime = totalTimeInMinutes;
        record.resultTime = calculateResultTime(totalDistance, totalTimeInMinutes);

        // データベースに保存
        new Thread(new Runnable() {
            @Override
            public void run() {
                db.recordDao().insert(record);
            }
        }).start();
    }

    private int calculateResultTime(float distance, int exerciseTime) {
        // 任意の計算式で結果の時間を算出（例として単純に運動時間を返す）
        return exerciseTime;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startExercise();
            } else {
                // パーミッションが拒否された場合の処理
            }
        }
    }
}
