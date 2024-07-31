package jp.ac.thers.myapplications;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.ViewHolder> {

    private Context context;
    private List<GameItem> gameList;
    private SharedPreferences prefs;

    public GameAdapter(Context context, List<GameItem> gameList) {
        this.context = context;
        this.gameList = gameList;
        this.prefs = context.getSharedPreferences("selected_apps", Context.MODE_PRIVATE);
    }

    public List<GameItem> getGameList() {
        return gameList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GameItem gameItem = gameList.get(position);
        holder.textView.setText(gameItem.getAppName());
        holder.checkBox.setOnCheckedChangeListener(null); // リスナーをリセット

        // チェックボックスの状態を設定
        holder.checkBox.setChecked(gameItem.isChecked());

        // チェックボックスの状態変更リスナーを設定
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            gameItem.setChecked(isChecked);
            saveItemCheckedState(gameItem);
        });
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    private void saveItemCheckedState(GameItem gameItem) {
        Set<String> selectedApps = prefs.getStringSet("apps", new HashSet<>());
        Set<String> newSelectedApps = new HashSet<>(selectedApps);

        if (gameItem.isChecked()) {
            newSelectedApps.add(gameItem.getPackageName());
        } else {
            newSelectedApps.remove(gameItem.getPackageName());
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("apps", newSelectedApps);
        editor.apply();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView); // `game_item.xml` の `TextView` の ID に一致
            checkBox = itemView.findViewById(R.id.checkBox); // `game_item.xml` の `CheckBox` の ID に一致
        }
    }
}
