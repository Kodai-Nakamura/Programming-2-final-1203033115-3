package jp.ac.thers.myapplications;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StartActivity extends AppCompatActivity {

    private TextView timerTextView;
    private Button stopButton;
    private Button finishButton;
    private Handler handler = new Handler();
    private long startTime;
    private long elapsedTime;
    private boolean isRunning = false;
    private String startDateTime;

    private TextView timerMinutesTextView;
    private TextView timerSecondsTextView;
    private Button startTimerButton;
    private Button stopTimerButton;
    private Button resetTimerButton;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long timerDuration = 60000; // デフォルトのタイマー時間: 1分 (60000ミリ秒)

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                elapsedTime = System.currentTimeMillis() - startTime;
                int seconds = (int) (elapsedTime / 1000);
                int minutes = seconds / 60;
                int hours = minutes / 60;
                seconds = seconds % 60;
                minutes = minutes % 60;

                timerTextView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        timerTextView = findViewById(R.id.timerTextView);
        stopButton = findViewById(R.id.stopButton);
        finishButton = findViewById(R.id.finishButton);

        timerMinutesTextView = findViewById(R.id.timerMinutesTextView);
        timerSecondsTextView = findViewById(R.id.timerSecondsTextView);
        startTimerButton = findViewById(R.id.startTimerButton);
        stopTimerButton = findViewById(R.id.stopTimerButton);
        resetTimerButton = findViewById(R.id.resetTimerButton);

        startTime = System.currentTimeMillis();
        startDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(startTime));
        isRunning = true;
        handler.post(timerRunnable);

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning) {
                    isRunning = false;
                    stopButton.setText("リスタート");
                } else {
                    startTime = System.currentTimeMillis() - elapsedTime;
                    isRunning = true;
                    handler.post(timerRunnable);
                    stopButton.setText("ストップ");
                }
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRunning = false;
                showSurveyDialog();
            }
        });

        timerMinutesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetTimerDialog(true);
            }
        });

        timerSecondsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetTimerDialog(false);
            }
        });

        startTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
            }
        });

        stopTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
            }
        });

        resetTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });
    }

    private void showSurveyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("勉強の集中度を選択してください");
        String[] options = {"非常に集中できた", "集中できた", "あまり集中できなかった"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                float multiplier;
                String surveyResult;
                switch (which) {
                    case 0:
                        multiplier = 1.2f;
                        surveyResult = "非常に集中できた";
                        break;
                    case 1:
                        multiplier = 1.0f;
                        surveyResult = "集中できた";
                        break;
                    case 2:
                    default:
                        multiplier = 0.5f;
                        surveyResult = "あまり集中できなかった";
                        break;
                }
                saveRecord(multiplier, surveyResult);
                finish();
            }
        });
        builder.show();
    }

    private void saveRecord(float multiplier, String surveyResult) {
        SharedPreferences prefs = getSharedPreferences("records", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();

        List<Record> records = getRecords(prefs, gson);

        String endDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        float distance = 0; // ここに実際の距離計算を追加
        long remainingTime = (long) (elapsedTime * multiplier); // アンケート結果に基づいて残り時間を計算
        Record record = new Record(startDateTime, endDateTime, elapsedTime, distance, remainingTime, surveyResult);
        records.add(record);

        String json = gson.toJson(records);
        editor.putString("records", json);
        editor.apply();

        updateTotalRemainingTime(remainingTime);
    }

    private void updateTotalRemainingTime(long additionalTime) {
        SharedPreferences prefs = getSharedPreferences("remaining_time", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        long currentTotalTime = prefs.getLong("total_remaining_time", 0);
        long newTotalTime = currentTotalTime + additionalTime;
        editor.putLong("total_remaining_time", newTotalTime);
        editor.apply();

        // AppUsageServiceに通知して残り時間を更新する
        Intent intent = new Intent(this, AppUsageService.class);
        intent.putExtra("add_remaining_time", additionalTime);
        startService(intent);
    }

    private List<Record> getRecords(SharedPreferences prefs, Gson gson) {
        String json = prefs.getString("records", "");
        Type type = new TypeToken<ArrayList<Record>>() {}.getType();
        List<Record> records = gson.fromJson(json, type);
        if (records == null) {
            records = new ArrayList<>();
        }
        return records;
    }

    private void showSetTimerDialog(final boolean isMinutes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isMinutes ? "分を設定" : "秒を設定");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("設定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int value = Integer.parseInt(input.getText().toString());
                if (isMinutes) {
                    timerMinutesTextView.setText(String.format("%02d", value));
                } else {
                    timerSecondsTextView.setText(String.format("%02d", value));
                }
                updateTimerDuration();
            }
        });
        builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void updateTimerDuration() {
        int minutes = Integer.parseInt(timerMinutesTextView.getText().toString());
        int seconds = Integer.parseInt(timerSecondsTextView.getText().toString());
        timerDuration = (minutes * 60 + seconds) * 1000; // ミリ秒に変換
    }

    private void startTimer() {
        if (!isTimerRunning) {
            countDownTimer = new CountDownTimer(timerDuration, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long minutes = (millisUntilFinished / 1000) / 60;
                    long seconds = (millisUntilFinished / 1000) % 60;
                    timerMinutesTextView.setText(String.format("%02d", minutes));
                    timerSecondsTextView.setText(String.format("%02d", seconds));
                }

                @Override
                public void onFinish() {
                    isTimerRunning = false;
                    timerMinutesTextView.setText("00");
                    timerSecondsTextView.setText("00");
                    startTimerButton.setVisibility(View.VISIBLE);
                    stopTimerButton.setVisibility(View.GONE);
                }
            };
            countDownTimer.start();
            isTimerRunning = true;
            startTimerButton.setVisibility(View.GONE);
            stopTimerButton.setVisibility(View.VISIBLE);
        }
    }

    private void stopTimer() {
        if (isTimerRunning) {
            countDownTimer.cancel();
            isTimerRunning = false;
            startTimerButton.setText("リスタート");
            startTimerButton.setVisibility(View.VISIBLE);
            stopTimerButton.setVisibility(View.GONE);
        }
    }

    private void resetTimer() {
        if (isTimerRunning) {
            countDownTimer.cancel();
            isTimerRunning = false;
        }
        timerMinutesTextView.setText("00");
        timerSecondsTextView.setText("00");
        startTimerButton.setText("スタート");
        startTimerButton.setVisibility(View.VISIBLE);
        stopTimerButton.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isRunning && elapsedTime > 0) {
            stopButton.setText("リスタート");
        }
    }
}
