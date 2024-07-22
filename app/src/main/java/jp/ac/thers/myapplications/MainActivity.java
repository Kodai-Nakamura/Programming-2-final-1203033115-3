package jp.ac.thers.myapplications;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView timeRemaining;
    private Button startButton;
    private Button recordButton;
    private Button gameListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeRemaining = findViewById(R.id.timeRemaining);
        startButton = findViewById(R.id.startButton);
        recordButton = findViewById(R.id.recordButton);
        gameListButton = findViewById(R.id.gameListButton);

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

        updateTimeRemaining(100);
    }

    private void updateTimeRemaining(int timeInMinutes) {
        int hours = timeInMinutes / 60;
        int minutes = timeInMinutes % 60;
        timeRemaining.setText(String.format("Time Remaining: %02d:%02d", hours, minutes));
    }
}
