package com.eaglesakura.view.egl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.SurfaceTexture;
import android.os.Looper;

import com.eaglesakura.view.GLTextureView.EGLConfigChooser;
import com.eaglesakura.view.GLTextureView.GLESVersion;

public class EGLManager {
    /**
     * ロックオブジェクト
     */
    final private Object lock = new Object();

    /**
     * EGLオブジェクト
     */
    EGL10 egl = null;

    /**
     * レンダリング用ディスプレイ
     */
    EGLDisplay eglDisplay = null;

    /**
     * レンダリング用サーフェイス
     */
    EGLSurface eglSurface = null;

    /**
     * レンダリング用コンテキスト
     */
    EGLContext eglContext = null;

    /**
     * config情報
     */
    EGLConfig eglConfig = null;

    /**
     * システムがデフォルトで使用しているEGLDisplayオブジェクト
     */
    EGLDisplay systemDisplay = null;

    /**
     * システムがデフォルトで使用しているEGLSurface(Read)
     */
    EGLSurface systemReadSurface = null;

    /**
     * システムがデフォルトで使用しているEGLSurface(Draw)
     */
    EGLSurface systemDrawSurface = null;

    /**
     * システムがデフォルトで使用しているコンテキスト
     */
    EGLContext systemContext = null;

    /**
     * GL10 object
     * only OpenGL ES 1.1
     */
    GL11 gl11 = null;

    public EGLManager() {
    }

    /**
     * 初期化を行う
     */
    public void initialize(final EGLConfigChooser chooser, final GLESVersion version) {
        synchronized (lock) {
            if (egl != null) {
                throw new RuntimeException("initialized");
            }

            egl = (EGL10) EGLContext.getEGL();

            // システムのデフォルトオブジェクトを取り出す
            {
                systemDisplay = egl.eglGetCurrentDisplay();
                systemReadSurface = egl.eglGetCurrentSurface(EGL10.EGL_READ);
                systemDrawSurface = egl.eglGetCurrentSurface(EGL10.EGL_DRAW);
                systemContext = egl.eglGetCurrentContext();
            }

            // ディスプレイ作成
            {
                eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
                if (eglDisplay == EGL10.EGL_NO_DISPLAY) {
                    throw new RuntimeException("EGL_NO_DISPLAY");
                }

                if (!egl.eglInitialize(eglDisplay, new int[2])) {
                    throw new RuntimeException("eglInitialize");
                }
            }
            // コンフィグ取得
            {
                eglConfig = chooser.chooseConfig(egl, eglDisplay, version);
                if (eglConfig == null) {
                    throw new RuntimeException("chooseConfig");
                }
            }

            // コンテキスト作成
            {
                eglContext = egl.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT,
                        version.getContextAttributes());

                if (eglContext == EGL10.EGL_NO_CONTEXT) {
                    throw new RuntimeException("eglCreateContext");
                }
            }

            if (version == GLESVersion.OpenGLES11) {
                gl11 = (GL11) eglContext.getGL();
            }
        }
    }

    /**
     * get GL11 object
     * @return
     */
    public GL11 getGL11() {
        if (gl11 == null) {
            throw new UnsupportedOperationException("OpenGL ES 1.1 only");
        }
        return gl11;
    }

    public EGLConfig getConfig() {
        return eglConfig;
    }

    public EGLSurface getSurface() {
        return eglSurface;
    }

    public EGLContext getContext() {
        return eglContext;
    }

    /**
     * リサイズを行う
     * @param view
     */
    public void resize(SurfaceTexture surface) {
        synchronized (lock) {
            // 既にサーフェイスが存在する場合は廃棄する
            if (eglSurface != null) {
                egl.eglDestroySurface(eglDisplay, eglSurface);
            }

            // レンダリング用サーフェイスを再度生成
            {
                eglSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, surface, null);
                if (eglSurface == EGL10.EGL_NO_SURFACE) {
                    throw new RuntimeException("eglCreateWindowSurface");
                }
            }
        }
    }

    /**
     * 解放処理を行う
     */
    public void destroy() {
        synchronized (lock) {
            if (egl == null) {
                return;
            }

            if (eglSurface != null) {
                egl.eglDestroySurface(eglDisplay, eglSurface);
                eglSurface = null;
            }
            if (eglContext != null) {
                egl.eglDestroyContext(eglDisplay, eglContext);
                eglContext = null;
            }
            eglConfig = null;
            egl = null;
        }
    }

    /**
     * ES20コンテキストを専有する
     */
    public void bind() {
        synchronized (lock) {
            egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
        }
    }

    /**
     * UIスレッドで呼び出された場合trueを返す。
     * @return
     */
    public boolean isUIThread() {
        return Thread.currentThread().equals(Looper.getMainLooper().getThread());
    }

    /**
     * コンテキストの専有を解除する
     */
    public void unbind() {
        synchronized (lock) {
            if (isUIThread()) {
                // UIスレッドならばシステムのデフォルトへ返す
                egl.eglMakeCurrent(systemDisplay, systemDrawSurface, systemReadSurface, systemContext);
            } else {
                // それ以外ならば、null状態に戻す
                egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            }
        }
    }

    /**
     * 最後のリリースタイミングではNO_SURFACEで問題ない。
     */
    public void releaseThread() {
        synchronized (lock) {
            egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        }
    }

    /**
     * レンダリング内容をフロントバッファへ転送する
     */
    public boolean swapBuffers() {
        synchronized (lock) {
            return egl.eglSwapBuffers(eglDisplay, eglSurface);
        }
    }
}
