package me.relex.camerafilter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import java.io.File;
import me.relex.camerafilter.camera.CameraRecordRenderer;
import me.relex.camerafilter.filter.FilterManager.FilterType;
import me.relex.camerafilter.video.EncoderConfig;
import me.relex.camerafilter.video.TextureMovieEncoder;
import me.relex.camerafilter.widget.CameraSurfaceView;

public class VideoRecordActivity extends AppCompatActivity implements View.OnClickListener {

    private CameraSurfaceView mCameraSurfaceView;
    private Button mRecordButton;
    private boolean mIsRecordEnabled;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.camera);
        mCameraSurfaceView.setAspectRatio(3, 4);

        findViewById(R.id.filter_normal).setOnClickListener(this);
        findViewById(R.id.filter_tone_curve).setOnClickListener(this);
        findViewById(R.id.filter_soft_light).setOnClickListener(this);

        mRecordButton = (Button) findViewById(R.id.record);
        mRecordButton.setOnClickListener(this);

        mIsRecordEnabled = TextureMovieEncoder.getInstance().isRecording();
        updateRecordButton();
    }

    @Override protected void onResume() {
        super.onResume();
        mCameraSurfaceView.onResume();
        updateRecordButton();
    }

    @Override protected void onPause() {
        mCameraSurfaceView.onPause();
        super.onPause();
    }

    @Override protected void onDestroy() {
        mCameraSurfaceView.onDestroy();
        super.onDestroy();
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter_normal:
                mCameraSurfaceView.changeFilter(FilterType.Normal);
                break;
            case R.id.filter_tone_curve:
                mCameraSurfaceView.changeFilter(FilterType.ToneCurve);
                break;
            case R.id.filter_soft_light:
                mCameraSurfaceView.changeFilter(FilterType.SoftLight);
                break;
            case R.id.record:
                if (!mIsRecordEnabled) {
                    mCameraSurfaceView.queueEvent(new Runnable() {
                        @Override public void run() {
                            CameraRecordRenderer renderer = mCameraSurfaceView.getRenderer();
                            renderer.setEncoderConfig(new EncoderConfig(new File(
                                    FileUtil.getCacheDirectory(VideoRecordActivity.this, true),
                                    "video-" + System.currentTimeMillis() + ".mp4"), 480, 640,
                                    1024 * 1024 /* 1 Mb/s */));
                        }
                    });
                }
                mIsRecordEnabled = !mIsRecordEnabled;
                mCameraSurfaceView.queueEvent(new Runnable() {
                    @Override public void run() {
                        mCameraSurfaceView.getRenderer().setRecordingEnabled(mIsRecordEnabled);
                    }
                });
                updateRecordButton();
                break;
        }
    }

    public void updateRecordButton() {
        mRecordButton.setText(
                getString(mIsRecordEnabled ? R.string.record_stop : R.string.record_start));
    }
}
