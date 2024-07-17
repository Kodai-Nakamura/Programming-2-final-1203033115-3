package jp.ac.thers.myapplications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private List<Game> gameList;

    public GameAdapter(List<Game> gameList) {
        this.gameList = gameList;
    }

    @Override
    public GameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.game_item, parent, false);
        return new GameViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GameViewHolder holder, int position) {
        Game game = gameList.get(position);
        holder.name.setText(game.getName());
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        public TextView name;

        public GameViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
        }
    }
}
