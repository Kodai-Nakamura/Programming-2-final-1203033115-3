package jp.ac.thers.myapplications;

public class GameItem {
    private String appName;
    private String packageName;
    private boolean isChecked;

    public GameItem(String appName, String packageName, boolean isChecked) {
        this.appName = appName;
        this.packageName = packageName;
        this.isChecked = isChecked;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
