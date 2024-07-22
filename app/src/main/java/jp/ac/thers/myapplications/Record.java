package jp.ac.thers.myapplications;

public class Record {
    private String date;
    private double distance;
    private int exerciseTime;
    private int resultTime;

    public Record(String date, double distance, int exerciseTime, int resultTime) {
        this.date = date;
        this.distance = distance;
        this.exerciseTime = exerciseTime;
        this.resultTime = resultTime;
    }

    public String getDate() {
        return date;
    }

    public double getDistance() {
        return distance;
    }

    public int getExerciseTime() {
        return exerciseTime;
    }

    public int getResultTime() {
        return resultTime;
    }
}
