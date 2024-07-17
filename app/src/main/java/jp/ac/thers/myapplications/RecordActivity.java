package jp.ac.thers.myapplications;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecordActivity extends AppCompatActivity {

    private RecyclerView recordRecyclerView;
    private RecordAdapter recordAdapter;
    private List<Record> recordList;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        recordRecyclerView = findViewById(R.id.recordRecyclerView);
        recordRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 仮のデータを追加
        recordList = new ArrayList<>();
        recordList.add(new Record("2023-01-01", 5.0, 30, 50));
        recordList.add(new Record("2023-01-02", 3.0, 20, 30));

        recordAdapter = new RecordAdapter(recordList);
        recordRecyclerView.setAdapter(recordAdapter);
    }
}
