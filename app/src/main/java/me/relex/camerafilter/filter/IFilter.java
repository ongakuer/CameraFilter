package me.relex.camerafilter.filter;

import java.nio.FloatBuffer;

public interface IFilter {
    int getTextureTarget();

    void setTextureSize(int width, int height);

    void onDraw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex, int vertexCount,
            int coordsPerVertex, int vertexStride, float[] texMatrix, FloatBuffer texBuffer,
            int textureId, int texStride);

    void releaseProgram();
}
