package me.relex.camerafilter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import me.relex.camerafilter.widget.CameraTextureView;

public class MainActivity extends AppCompatActivity {

    private CameraTextureView mCameraTextureView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraTextureView = (CameraTextureView) findViewById(R.id.camera);
        mCameraTextureView.setAspectRatio(1, 1);
        //mCameraTextureView.setAspectRatio(1, 1);
    }

    @Override protected void onResume() {
        super.onResume();
        mCameraTextureView.onResume();
    }

    @Override protected void onPause() {
        mCameraTextureView.onPause();
        super.onPause();
    }

    @Override protected void onDestroy() {
        mCameraTextureView.onDestroy();
        super.onDestroy();
    }
}
