package jp.ac.thers.myapplications;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class GameListActivity extends AppCompatActivity {

    private RecyclerView gameRecyclerView;
    private GameAdapter gameAdapter;
    private List<Game> gameList;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        gameRecyclerView = findViewById(R.id.gameRecyclerView);
        gameRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 仮のデータを追加
        gameList = new ArrayList<>();
        gameList.add(new Game("Game 1"));
        gameList.add(new Game("Game 2"));

        gameAdapter = new GameAdapter(gameList);
        gameRecyclerView.setAdapter(gameAdapter);
    }
}
