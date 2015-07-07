package me.relex.camerafilter.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import me.relex.camerafilter.filter.FilterManager;
import me.relex.camerafilter.filter.FilterManager.FilterType;
import me.relex.camerafilter.gles.FullFrameRect;
import me.relex.camerafilter.gles.GlUtil;
import me.relex.camerafilter.video.EncoderConfig;
import me.relex.camerafilter.video.TextureMovieEncoder;
import me.relex.camerafilter.widget.CameraSurfaceView;

public class CameraRecordRenderer implements GLSurfaceView.Renderer {

    private static final int RECORDING_OFF = 0;
    private static final int RECORDING_ON = 1;
    private static final int RECORDING_RESUMED = 2;

    private final Context mApplicationContext;
    private final CameraSurfaceView.CameraHandler mCameraHandler;
    private int mTextureId = GlUtil.NO_TEXTURE;
    private FullFrameRect mFullScreen;
    private SurfaceTexture mSurfaceTexture;
    private final float[] mSTMatrix = new float[16];

    private FilterType mCurrentFilterType;
    private FilterType mNewFilterType;
    private TextureMovieEncoder mVideoEncoder;

    private boolean mRecordingEnabled;
    private int mRecordingStatus;
    private EncoderConfig mEncoderConfig;

    private float mMvpScaleX = 1f, mMvpScaleY = 1f;
    private int mSurfaceWidth, mSurfaceHeight;
    private int mIncomingWidth, mIncomingHeight;

    public CameraRecordRenderer(Context applicationContext,
            CameraSurfaceView.CameraHandler cameraHandler) {
        mApplicationContext = applicationContext;
        mCameraHandler = cameraHandler;
        mCurrentFilterType = mNewFilterType = FilterType.Normal;
        mVideoEncoder = TextureMovieEncoder.getInstance();
    }

    public void setEncoderConfig(EncoderConfig encoderConfig) {
        mEncoderConfig = encoderConfig;
    }

    public void setRecordingEnabled(boolean recordingEnabled) {
        mRecordingEnabled = recordingEnabled;
    }

    public void setCameraPreviewSize(int width, int height) {
        mIncomingWidth = width;
        mIncomingHeight = height;

        float scaleHeight = mSurfaceWidth / (width * 1f / height * 1f);
        float surfaceHeight = mSurfaceHeight;

        if (mFullScreen != null) {
            mMvpScaleX = 1f;
            mMvpScaleY = scaleHeight / surfaceHeight;
            mFullScreen.scaleMVPMatrix(mMvpScaleX, mMvpScaleY);
        }
    }

    @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Matrix.setIdentityM(mSTMatrix, 0);
        mRecordingEnabled = mVideoEncoder.isRecording();
        if (mRecordingEnabled) {
            mRecordingStatus = RECORDING_RESUMED;
        } else {
            mRecordingStatus = RECORDING_OFF;
            mVideoEncoder.initFilter(mCurrentFilterType);
        }
        mFullScreen = new FullFrameRect(
                FilterManager.getCameraFilter(mCurrentFilterType, mApplicationContext));
        mTextureId = mFullScreen.createTexture();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
    }

    @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;

        if (gl != null) {
            gl.glViewport(0, 0, width, height);
        }

        mCameraHandler.sendMessage(
                mCameraHandler.obtainMessage(CameraSurfaceView.CameraHandler.SETUP_CAMERA, width,
                        height, mSurfaceTexture));
    }

    @Override public void onDrawFrame(GL10 gl) {
        mSurfaceTexture.updateTexImage();
        if (mNewFilterType != mCurrentFilterType) {
            mFullScreen.changeProgram(
                    FilterManager.getCameraFilter(mNewFilterType, mApplicationContext));
            mCurrentFilterType = mNewFilterType;
        }
        mFullScreen.getFilter().setTextureSize(mIncomingWidth, mIncomingHeight);
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mFullScreen.drawFrame(mTextureId, mSTMatrix);

        videoOnDrawFrame(mTextureId, mSTMatrix, mSurfaceTexture.getTimestamp());
    }

    private void videoOnDrawFrame(int textureId, float[] texMatrix, long timestamp) {
        if (mRecordingEnabled && mEncoderConfig != null) {
            switch (mRecordingStatus) {
                case RECORDING_OFF:
                    mEncoderConfig.updateEglContext(EGL14.eglGetCurrentContext());
                    mVideoEncoder.startRecording(mEncoderConfig);
                    mVideoEncoder.setTextureId(textureId);
                    mVideoEncoder.scaleMVPMatrix(mMvpScaleX, mMvpScaleY);
                    mRecordingStatus = RECORDING_ON;

                    break;
                case RECORDING_RESUMED:
                    mVideoEncoder.updateSharedContext(EGL14.eglGetCurrentContext());
                    mVideoEncoder.setTextureId(textureId);
                    mVideoEncoder.scaleMVPMatrix(mMvpScaleX, mMvpScaleY);
                    mRecordingStatus = RECORDING_ON;
                    break;
                case RECORDING_ON:
                    // yay
                    break;
                default:
                    throw new RuntimeException("unknown status " + mRecordingStatus);
            }
        } else {
            switch (mRecordingStatus) {
                case RECORDING_ON:
                case RECORDING_RESUMED:
                    mVideoEncoder.stopRecording();
                    mRecordingStatus = RECORDING_OFF;
                    break;
                case RECORDING_OFF:
                    // yay
                    break;
                default:
                    throw new RuntimeException("unknown status " + mRecordingStatus);
            }
        }

        mVideoEncoder.updateFilter(mCurrentFilterType);
        mVideoEncoder.frameAvailable(texMatrix, timestamp);
    }

    public void notifyPausing() {

        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }

        if (mFullScreen != null) {
            mFullScreen.release(false);     // assume the GLSurfaceView EGL context is about
            mFullScreen = null;             // to be destroyed
        }
    }

    public void changeFilter(FilterType filterType) {
        mNewFilterType = filterType;
    }
}
