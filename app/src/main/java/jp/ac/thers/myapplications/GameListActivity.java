package jp.ac.thers.myapplications;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GameAdapter gameAdapter;
    private Button backButton;
    private SharedPreferences prefs;
    private List<GameItem> gameItems;
    private List<GameItem> filteredGameItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        backButton = findViewById(R.id.backButton);
        SearchView searchView = findViewById(R.id.searchView);

        prefs = getSharedPreferences("selected_apps", MODE_PRIVATE);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSelectedApps();  // 保存は backButton のクリックで行う
                Intent intent = new Intent(GameListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterGameList(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterGameList(newText);
                return false;
            }
        });

        loadInstalledApps();
    }

    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        gameItems = new ArrayList<>();
        filteredGameItems = new ArrayList<>();
        Set<String> selectedApps = prefs.getStringSet("apps", new HashSet<>());

        for (ApplicationInfo app : apps) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null && (app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                boolean isChecked = selectedApps.contains(app.packageName);
                GameItem gameItem = new GameItem(app.loadLabel(pm).toString(), app.packageName, isChecked);
                gameItems.add(gameItem);
            }
        }

        // チェックされているゲームを上に、名前の昇順でソート
        Collections.sort(gameItems, new Comparator<GameItem>() {
            @Override
            public int compare(GameItem item1, GameItem item2) {
                if (item1.isChecked() != item2.isChecked()) {
                    return item1.isChecked() ? -1 : 1; // チェックされているものを上に
                }
                return item1.getAppName().compareToIgnoreCase(item2.getAppName()); // 名前の昇順でソート
            }
        });

        filteredGameItems.addAll(gameItems);
        gameAdapter = new GameAdapter(this, filteredGameItems);
        recyclerView.setAdapter(gameAdapter);
    }

    private void saveSelectedApps() {
        if (gameAdapter != null) {
            Set<String> selectedApps = new HashSet<>();
            for (GameItem item : gameAdapter.getGameList()) {
                if (item.isChecked()) {
                    selectedApps.add(item.getPackageName());
                }
            }
            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet("apps", selectedApps);
            editor.apply();
        }
    }

    private void filterGameList(String query) {
        filteredGameItems.clear();
        for (GameItem item : gameItems) {
            if (item.getAppName().toLowerCase().contains(query.toLowerCase())) {
                filteredGameItems.add(item);
            }
        }
        gameAdapter.notifyDataSetChanged();
    }
}
