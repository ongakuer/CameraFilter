package me.relex.camerafilter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.image_filter).setOnClickListener(this);
        findViewById(R.id.video_record).setOnClickListener(this);
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_filter:
                startActivity(new Intent(this, ImageFilterActivity.class));

                break;
            case R.id.video_record:
                startActivity(new Intent(this, VideoRecordActivity.class));
                break;
        }
    }
}
