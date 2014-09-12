package me.relex.camerafilter;

import android.app.Activity;
import android.os.Bundle;
import me.relex.camerafilter.camrea.widget.CameraTextureView;

public class MainActivity extends Activity {

    private CameraTextureView mCameraTextureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraTextureView = (CameraTextureView) findViewById(R.id.camera);
    }

    @Override protected void onResume() {
        super.onResume();
        mCameraTextureView.onResume();
    }

    @Override protected void onPause() {
        mCameraTextureView.onPause();
        super.onPause();
    }
}
