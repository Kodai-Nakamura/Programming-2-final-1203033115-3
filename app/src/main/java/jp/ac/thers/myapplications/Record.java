package jp.ac.thers.myapplications;

public class Record {
    private String startTime;
    private String endTime;
    private long exerciseTime; // ミリ秒単位
    private float distance; // キロメートル単位
    private long remainingTime; // ミリ秒単位
    private String surveyResult; // アンケート結果

    public Record(String startTime, String endTime, long exerciseTime, float distance, long remainingTime, String surveyResult) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.exerciseTime = exerciseTime;
        this.distance = distance;
        this.remainingTime = remainingTime;
        this.surveyResult = surveyResult;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public long getExerciseTime() {
        return exerciseTime;
    }

    public float getDistance() {
        return distance;
    }

    public long getRemainingTime() {
        return remainingTime;
    }

    public String getSurveyResult() {
        return surveyResult;
    }

    public String getFormattedExerciseTime() {
        int seconds = (int) (exerciseTime / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public String getFormattedDistance() {
        return String.format("%.2f km", distance);
    }

    public String getFormattedRemainingTime() {
        int totalMinutes = (int) (remainingTime / 60000); // ミリ秒 -> 分
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        int seconds = (int) ((remainingTime % 60000) / 1000); // ミリ秒 -> 秒
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
