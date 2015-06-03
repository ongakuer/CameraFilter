package com.eaglesakura.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import com.eaglesakura.view.egl.DefaultEGLConfigChooser;
import com.eaglesakura.view.egl.EGLManager;
import com.eaglesakura.view.egl.SurfaceColorSpec;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/**
 * {@link SurfaceView} -> {@link TextureView} OpenGL ES 1.1 or OpenGL ES 2.0
 */
public class GLTextureView extends TextureView implements TextureView.SurfaceTextureListener {
    static final String TAG = GLTextureView.class.getSimpleName();

    /**
     * callback object
     */
    protected Renderer renderer = null;

    /**
     * OpenGL ES Version.
     */
    GLESVersion version = GLESVersion.OpenGLES11;

    /**
     *
     */
    protected EGLManager eglManager = null;

    /**
     * ConfigChooser
     */
    EGLConfigChooser eglConfigChooser = null;

    /**
     * rendering thread
     */
    RenderingThreadType renderingThreadType = RenderingThreadType.BackgroundThread;

    /**
     * lock object
     */
    protected final Object lock = new Object();

    /**
     * GL Object
     */
    GL11 gl11;

    /**
     *
     */
    Thread backgroundThread = null;

    /**
     * Surface Destroyed
     */
    boolean destroyed = false;

    /**
     * Thread Sleep
     */
    boolean sleep = false;

    boolean initialized = false;

    /**
     * surface texture width
     */
    int surfaceWidth = 0;

    /**
     * surface texture height
     */
    int surfaceHeight = 0;

    public GLTextureView(Context context) {
        super(context);
        setSurfaceTextureListener(this);
    }

    public GLTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSurfaceTextureListener(this);
    }

    public GLTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setSurfaceTextureListener(this);
    }

    public EGLManager getEGLManager() {
        return eglManager;
    }

    /**
     * Activity#onPause() || Fragment#onPause()
     */
    public void onPause() {
        sleep = true;
    }

    /**
     * Activity#onResume() || Fragment#onResume()
     */
    public void onResume() {
        sleep = false;
    }

    /**
     * check EGL Initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     *
     * @param renderer
     */
    public void setRenderer(Renderer renderer) {
        synchronized (lock) {
            if (isInitialized()) {
                throw new UnsupportedOperationException("GLTextureView Initialized");
            }
            this.renderer = renderer;
        }
    }

    /**
     *
     * @param version
     */
    public void setVersion(GLESVersion version) {
        synchronized (lock) {
            if (isInitialized()) {
                throw new UnsupportedOperationException("GLTextureView Initialized");
            }
            this.version = version;
        }
    }

    /**
     * EGL Config setup
     */
    public void setSurfaceSpec(SurfaceColorSpec color, boolean hasDepth, boolean hasStencil) {
        DefaultEGLConfigChooser chooser = new DefaultEGLConfigChooser();
        chooser.setColorSpec(color);
        chooser.setDepthEnable(hasDepth);
        chooser.setStencilEnable(hasStencil);
        setEGLConfigChooser(chooser);
    }

    /**
     * Config Chooser
     */
    public void setEGLConfigChooser(EGLConfigChooser eglConfigChooser) {
        synchronized (lock) {
            if (isInitialized()) {
                throw new UnsupportedOperationException("GLTextureView Initialized");
            }
            this.eglConfigChooser = eglConfigChooser;
        }
    }

    /**
     *
     * @param renderingThreadType
     */
    public void setRenderingThreadType(RenderingThreadType renderingThreadType) {
        synchronized (lock) {
            if (isInitialized()) {
                throw new UnsupportedOperationException("GLTextureView Initialized");
            }

            this.renderingThreadType = renderingThreadType;
        }
    }

    /**
     * start rendering
     * call {@link GLTextureView#onRendering()}
     */
    public void requestRender() {
        synchronized (lock) {
            if (!isInitialized()) {
                throw new UnsupportedOperationException("GLTextureView not initialized");
            }

            onRendering();
        }
    }

    /**
     *
     * @param runnable
     */
    public void requestAction(Runnable runnable) {
        synchronized (lock) {
            if (!isInitialized()) {
                throw new UnsupportedOperationException("GLTextureView not initialized");
            }

            eglManager.bind();
            runnable.run();
            eglManager.unbind();
        }
    }

    @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        synchronized (lock) {

            surfaceWidth = width;
            surfaceHeight = height;

            if (!isInitialized()) {
                eglManager = new EGLManager();

                if (eglConfigChooser == null) {
                    // make default spec
                    // RGBA8 hasDepth hasStencil
                    eglConfigChooser = new DefaultEGLConfigChooser();
                }

                eglManager.initialize(eglConfigChooser, version);

                if (version == GLESVersion.OpenGLES11) {
                    gl11 = eglManager.getGL11();
                }

                eglManager.resize(surface);

                if (renderingThreadType != RenderingThreadType.BackgroundThread) {
                    // UIThread || request
                    eglManager.bind();
                    renderer.onSurfaceCreated(gl11, eglManager.getConfig());
                    renderer.onSurfaceChanged(gl11, width, height);
                    eglManager.unbind();
                }
            } else {
                eglManager.resize(surface);

                if (renderingThreadType != RenderingThreadType.BackgroundThread) {
                    // UIThread || request
                    eglManager.bind();
                    renderer.onSurfaceChanged(gl11, width, height);
                    eglManager.unbind();
                }
            }

            initialized = true;
            if (renderingThreadType == RenderingThreadType.BackgroundThread) {
                // background
                backgroundThread = createRenderingThread();
                backgroundThread.start();
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        synchronized (lock) {

            surfaceWidth = width;
            surfaceHeight = height;

            eglManager.resize(surface);

            if (renderingThreadType != RenderingThreadType.BackgroundThread) {
                // UIThread || request
                eglManager.bind();
                renderer.onSurfaceChanged(gl11, width, height);
                eglManager.unbind();
            }
        }
    }

    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        destroyed = true;
        try {
            synchronized (lock) {

                if (renderingThreadType != RenderingThreadType.BackgroundThread) {
                    // UIThread || request
                    eglManager.bind();
                    renderer.onSurfaceDestroyed(gl11);
                    eglManager.releaseThread();
                }
            }

            if (backgroundThread != null) {
                try {
                    Log.d(TAG, "wait rendering thread");
                    // wait background thread
                    backgroundThread.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            eglManager.destroy();
        }

        // auto release
        return true;
    }

    /**
     *
     */
    @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    /**
     *
     */
    protected void onRendering() {
        eglManager.bind();
        {
            renderer.onDrawFrame(gl11);
        }
        eglManager.swapBuffers();
        eglManager.unbind();
    }

    /**
     * OpenGL ES Version
     */
    public enum GLESVersion {
        /**
         * OpenGL ES 1.0
         */
        OpenGLES11 {
            @Override public int[] getContextAttributes() {
                return null;
            }
        },

        /**
         * OpenGL ES 2.0
         */
        OpenGLES20 {
            @Override public int[] getContextAttributes() {
                return new int[] {
                        0x3098 /* EGL_CONTEXT_CLIENT_VERSION */, 2, EGL10.EGL_NONE
                };
            }
        };

        public abstract int[] getContextAttributes();
    }

    /**
     *
     */
    public interface EGLConfigChooser {

        /**
         *
         * @return
         */
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, GLESVersion version);
    }

    /**
     *
     */
    public interface Renderer {
        /**
         * created EGLSurface.
         * {@link #onSurfaceChanged(GL10, int, int)}
         */
        public void onSurfaceCreated(GL10 gl, EGLConfig config);

        /**
         * remake EGLSurface.
         */
        public void onSurfaceChanged(GL10 gl, int width, int height);

        /**
         * rendering.
         */
        public void onDrawFrame(GL10 gl);

        /**
         * destroyed
         */
        public void onSurfaceDestroyed(GL10 gl);
    }

    public enum RenderingThreadType {
        /**
         * Rendering on Background Loop
         */
        BackgroundThread,

        /**
         * Rendering on {@link GLTextureView#requestRendering()}
         */
        RequestThread,
    }

    protected Thread createRenderingThread() {
        return new Thread() {
            int width = 0;
            int height = 0;

            @Override public void run() {
                // created
                eglManager.bind();
                {
                    renderer.onSurfaceCreated(gl11, eglManager.getConfig());
                }
                eglManager.unbind();

                while (!destroyed) {
                    int sleepTime = 1;
                    if (!sleep) {
                        synchronized (lock) {
                            eglManager.bind();

                            if (width != surfaceWidth || height != surfaceHeight) {
                                width = surfaceWidth;
                                height = surfaceHeight;
                                renderer.onSurfaceChanged(gl11, width, height);
                            }

                            renderer.onDrawFrame(gl11);

                            // post
                            if (!destroyed) {
                                eglManager.swapBuffers();
                            }
                            eglManager.unbind();
                        }
                    } else {
                        sleepTime = 10;
                    }

                    try {
                        // sleep rendering thread
                        Thread.sleep(sleepTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // destroy
                synchronized (lock) {
                    eglManager.bind();
                    renderer.onSurfaceDestroyed(gl11);
                    eglManager.releaseThread();
                }
            }
        };
    }
}
