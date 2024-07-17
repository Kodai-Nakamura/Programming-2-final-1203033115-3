package jp.ac.thers.myapplications.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "records")
public class Record {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String date;
    public float distance;
    public int exerciseTime;
    public int resultTime;

    // コンストラクタ、ゲッター、セッターを追加
}
