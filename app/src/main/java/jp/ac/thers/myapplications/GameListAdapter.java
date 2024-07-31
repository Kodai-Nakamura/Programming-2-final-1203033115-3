package jp.ac.thers.myapplications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GameListAdapter extends RecyclerView.Adapter<GameListAdapter.ViewHolder> {

    private List<Game> gameList;

    public GameListAdapter(List<Game> gameList) {
        this.gameList = gameList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Game game = gameList.get(position);
        holder.gameName.setText(game.getName());
        holder.checkBox.setChecked(game.isChecked());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            game.setChecked(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView gameName;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            gameName = itemView.findViewById(R.id.gameName);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
