package me.relex.camerafilter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import me.relex.camerafilter.widget.CameraSurfaceView;

public class MainActivity extends AppCompatActivity {

    private CameraSurfaceView mCameraSurfaceView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.camera);
        mCameraSurfaceView.setAspectRatio(1, 1);
        //mCameraTextureView.setAspectRatio(1, 1);
    }

    @Override protected void onResume() {
        super.onResume();
        mCameraSurfaceView.onResume();
    }

    @Override protected void onPause() {
        mCameraSurfaceView.onPause();
        super.onPause();
    }

    @Override protected void onDestroy() {
        mCameraSurfaceView.onDestroy();
        super.onDestroy();
    }
}
