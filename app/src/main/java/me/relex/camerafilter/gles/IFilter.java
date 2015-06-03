package me.relex.camerafilter.gles;

import java.nio.FloatBuffer;

public interface IFilter {
    int getTextureTarget();

    void onDraw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex, int vertexCount,
            int coordsPerVertex, int vertexStride, float[] texMatrix, FloatBuffer texBuffer,
            int textureId, int texStride);

    void releaseProgram();
}
