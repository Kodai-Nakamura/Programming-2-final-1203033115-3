package jp.ac.thers.myapplications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {

    private List<Record> recordList;

    public RecordAdapter(List<Record> recordList) {
        this.recordList = recordList;
    }

    @Override
    public RecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.record_item, parent, false);
        return new RecordViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecordViewHolder holder, int position) {
        Record record = recordList.get(position);
        holder.date.setText(record.getDate());
        holder.distance.setText(String.valueOf(record.getDistance()));
        holder.exerciseTime.setText(String.valueOf(record.getExerciseTime()));
        holder.resultTime.setText(String.valueOf(record.getResultTime()));
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public static class RecordViewHolder extends RecyclerView.ViewHolder {
        public TextView date, distance, exerciseTime, resultTime;

        public RecordViewHolder(View view) {
            super(view);
            date = view.findViewById(R.id.date);
            distance = view.findViewById(R.id.distance);
            exerciseTime = view.findViewById(R.id.exerciseTime);
            resultTime = view.findViewById(R.id.resultTime);
        }
    }
}
