package jp.ac.thers.myapplications;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;

public class TimeExpiredOverlayActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display a toast message to inform the user that time has expired
        Toast.makeText(this, "Your allocated time has expired!", Toast.LENGTH_LONG).show();

        // Configure the window layout parameters for overlay
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        );

        params.gravity = Gravity.CENTER;
        getWindow().setAttributes(params);

        // Optionally, you can set a custom view here
        // setContentView(R.layout.activity_time_expired_overlay);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Add any cleanup code here
    }
}
