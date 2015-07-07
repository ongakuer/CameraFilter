package me.relex.camerafilter.filter;

import android.opengl.GLES20;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import me.relex.camerafilter.gles.Drawable2d;
import me.relex.camerafilter.gles.GlUtil;

public class FilterGroup<T extends IFilter> implements IFilter {

    protected List<T> mFilters;
    protected int mIncomingWidth, mIncomingHeight;

    private Drawable2d mDrawableFlipVertical2d;
    private Drawable2d mDrawable2d;

    private int[] mFrameBuffers;
    private int[] mRenderBuffers;
    private int[] mFrameBufferTextures;

    public FilterGroup() {
        this(null);
    }

    public FilterGroup(List<T> filters) {
        if (filters != null) {
            mFilters = filters;
        } else {
            mFilters = new ArrayList<>();
        }
        mDrawableFlipVertical2d = new Drawable2d();
        mDrawable2d = new Drawable2d();
    }

    public void addFilter(T filter) {
        if (filter == null) {
            return;
        }
        mFilters.add(filter);
    }

    @Override public int getTextureTarget() {
        //return GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        return GLES20.GL_TEXTURE_2D;
    }

    @Override public void setTextureSize(int width, int height) {
        if (width == 0 || height == 0) {
            return;
        }
        if (width == mIncomingWidth && height == mIncomingHeight) {
            return;
        }
        mIncomingWidth = width;
        mIncomingHeight = height;

        if (mFrameBuffers != null) {
            destroyFrameBuffers();
        }

        for (T filter : mFilters) {
            filter.setTextureSize(width, height);
        }

        int size = mFilters.size();
        mFrameBuffers = new int[size - 1];
        mRenderBuffers = new int[size - 1];
        mFrameBufferTextures = new int[size - 1];

        for (int i = 0; i < size - 1; i++) {

            ///////////////// create FrameBufferTextures
            GLES20.glGenTextures(1, mFrameBufferTextures, i);
            GlUtil.checkGlError("glGenTextures");

            GLES20.glBindTexture(getTextureTarget(), mFrameBufferTextures[i]);
            GlUtil.checkGlError("glBindTexture " + mFrameBufferTextures[i]);

            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

            GLES20.glTexParameterf(getTextureTarget(), GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameterf(getTextureTarget(), GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameterf(getTextureTarget(), GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(getTextureTarget(), GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
            GlUtil.checkGlError("glTexParameter");

            ////////////////////////// create FrameBuffer
            GLES20.glGenFramebuffers(1, mFrameBuffers, i);
            GlUtil.checkGlError("glGenFramebuffers");

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[i]);
            GlUtil.checkGlError("glBindFramebuffer " + mFrameBuffers[i]);

            ////////////////////////// create DepthBuffer
            GLES20.glGenRenderbuffers(1, mRenderBuffers, 0);
            GlUtil.checkGlError("glRenderbuffers");

            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mRenderBuffers[i]);
            GlUtil.checkGlError("glBindRenderbuffer");

            GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width,
                    height);
            GlUtil.checkGlError("glRenderbufferStorage");
            /////////////

            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                    GLES20.GL_RENDERBUFFER, mRenderBuffers[i]);
            GlUtil.checkGlError("glFramebufferRenderbuffer");

            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D /*getTextureTarget()*/, mFrameBufferTextures[i], 0);

            GlUtil.checkGlError("glFramebufferTexture2D");

            int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
            if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                throw new RuntimeException("Framebuffer not complete, status=" + status);
            }

            // Switch back to the default framebuffer.
            GLES20.glBindTexture(getTextureTarget(), 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

            GlUtil.checkGlError("prepareFramebuffer done");
        }
    }

    @Override public void onDraw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
            int vertexCount, int coordsPerVertex, int vertexStride, float[] texMatrix,
            FloatBuffer texBuffer, int textureId, int texStride) {

        // TODO
        int size = mFilters.size();
        int previousTextureId = textureId;
        for (int i = 0; i < size; i++) {
            T filter = mFilters.get(i);
            boolean isNotLast = i < size - 1;

            if (isNotLast) {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[i]);
                GLES20.glClearColor(0, 0, 0, 0);
            }

            if (i == 0) {
                filter.onDraw(mvpMatrix, vertexBuffer, firstVertex, vertexCount, coordsPerVertex,
                        vertexStride, texMatrix, texBuffer, previousTextureId, texStride);
            } else if (i == size - 1) {
                filter.onDraw(mvpMatrix, mDrawable2d.getVertexArray(), firstVertex, vertexCount,
                        coordsPerVertex, vertexStride, texMatrix,
                        (size % 2 == 0) ? mDrawableFlipVertical2d.getTexCoordArray()
                                : mDrawable2d.getTexCoordArray(), previousTextureId, texStride);
            } else {
                filter.onDraw(mvpMatrix, mDrawable2d.getVertexArray(), firstVertex, vertexCount,
                        coordsPerVertex, vertexStride, texMatrix, mDrawable2d.getTexCoordArray(),
                        previousTextureId, texStride);
            }

            if (isNotLast) {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                previousTextureId = mFrameBufferTextures[i];
            }
        }
    }

    @Override public void releaseProgram() {
        destroyFrameBuffers();
        for (T filter : mFilters) {
            filter.releaseProgram();
        }
    }

    private void destroyFrameBuffers() {
        if (mFrameBufferTextures != null) {
            GLES20.glDeleteTextures(mFrameBufferTextures.length, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }
        if (mFrameBuffers != null) {
            GLES20.glDeleteFramebuffers(mFrameBuffers.length, mFrameBuffers, 0);
            mFrameBuffers = null;
        }

        if (mRenderBuffers != null) {
            GLES20.glDeleteRenderbuffers(mRenderBuffers.length, mRenderBuffers, 0);
            mRenderBuffers = null;
        }
    }
}
