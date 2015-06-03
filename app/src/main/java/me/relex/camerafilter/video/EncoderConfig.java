package me.relex.camerafilter.video;

/**
 * Created by relex on 15/6/2.
 */

import android.opengl.EGLContext;
import java.io.File;

/**
 * Encoder configuration.
 * <p>
 * Object is immutable, which means we can safely pass it between threads without
 * explicit synchronization (and don't need to worry about it getting tweaked out from
 * under us).
 * <p>
 * TODO: make frame rate and iframe interval configurable?  Maybe use builder pattern
 * with reasonable defaults for those and bit rate.
 */
public class EncoderConfig {
    final File mOutputFile;
    final int mWidth;
    final int mHeight;
    final int mBitRate;
    EGLContext mEglContext;

    public EncoderConfig(File outputFile, int width, int height, int bitRate) {
        mOutputFile = outputFile;
        mWidth = width;
        mHeight = height;
        mBitRate = bitRate;
    }

    public void updateEglContext(EGLContext eglContext) {
        mEglContext = eglContext;
    }
    //@Override public String toString() {
    //    return "EncoderConfig: " + mWidth + "x" + mHeight + " @" + mBitRate +
    //            " to '" + mOutputFile.toString() + "' ctxt=" + mEglContext;
    //}
}

