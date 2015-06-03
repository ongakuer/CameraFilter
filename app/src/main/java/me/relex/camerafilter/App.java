package me.relex.camerafilter;

import android.app.Application;
import me.relex.camerafilter.video.TextureMovieEncoder;

public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();
        TextureMovieEncoder.initialize(getApplicationContext());
    }
}
