package jp.ac.thers.myapplications;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RecordActivity extends AppCompatActivity {

    private RecyclerView recordListView;
    private RecordAdapter recordAdapter;
    private List<Record> recordList;
    private Button backButton;
    private Button clearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        recordListView = findViewById(R.id.recordListView);
        recordListView.setLayoutManager(new LinearLayoutManager(this));
        backButton = findViewById(R.id.backButton);
        clearButton = findViewById(R.id.clearButton);

        recordList = getRecords();
        recordAdapter = new RecordAdapter(this, recordList);
        recordListView.setAdapter(recordAdapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecordActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearRecords();
            }
        });
    }

    private List<Record> getRecords() {
        SharedPreferences prefs = getSharedPreferences("records", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("records", "");
        Type type = new TypeToken<ArrayList<Record>>() {}.getType();
        List<Record> records = gson.fromJson(json, type);
        if (records == null) {
            records = new ArrayList<>();
        }
        return records;
    }

    private void clearRecords() {
        SharedPreferences prefs = getSharedPreferences("records", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("records");
        editor.apply();

        recordList.clear();
        recordAdapter.notifyDataSetChanged();
    }
}
