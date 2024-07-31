package jp.ac.thers.myapplications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    private Context context;
    private List<Record> records;

    public RecordAdapter(Context context, List<Record> records) {
        this.context = context;
        this.records = records;
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.record_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Record record = records.get(position);
        holder.dateTextView.setText(String.format("開始: %s\n終了: %s", record.getStartTime(), record.getEndTime()));
        holder.exerciseTimeTextView.setText(String.format("勉強時間: %s", record.getFormattedExerciseTime()));
        holder.surveyResultTextView.setText(String.format("アンケート結果: %s", record.getSurveyResult()));
        holder.remainingTimeTextView.setText(String.format("残り時間: %s", record.getFormattedRemainingTime()));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView exerciseTimeTextView;
        TextView surveyResultTextView;
        TextView remainingTimeTextView;

        ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            exerciseTimeTextView = itemView.findViewById(R.id.exerciseTimeTextView);
            surveyResultTextView = itemView.findViewById(R.id.surveyResultTextView);
            remainingTimeTextView = itemView.findViewById(R.id.remainingTimeTextView);
        }
    }
}
