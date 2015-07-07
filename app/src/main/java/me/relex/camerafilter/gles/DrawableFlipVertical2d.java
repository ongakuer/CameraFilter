//package me.relex.camerafilter.gles;
///*
// * Copyright 2014 Google Inc. All rights reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//import java.nio.FloatBuffer;
//
//public class DrawableFlipVertical2d {
//    private static final int SIZEOF_FLOAT = 4;
//    private static final float FULL_RECTANGLE_COORDS[] = {
//            -1.0f, -1.0f,   // 0 bottom left
//            1.0f, -1.0f,   // 1 bottom right
//            -1.0f, 1.0f,   // 2 top left
//            1.0f, 1.0f,   // 3 top right
//    };
//    private static final float FULL_RECTANGLE_TEX_COORDS[] = {
//            0.0f, 1.0f,   //
//            1.0f, 1.0f,   //
//            0.0f, 0.0f,   //
//            1.0f, 0.0f    //
//    };
//    private static final FloatBuffer FULL_RECTANGLE_BUF =
//            GlUtil.createFloatBuffer(FULL_RECTANGLE_COORDS);
//    private static final FloatBuffer FULL_RECTANGLE_TEX_BUF =
//            GlUtil.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);
//
//    private FloatBuffer mVertexArray;
//    private FloatBuffer mTexCoordArray;
//    private int mVertexCount;
//    private int mCoordsPerVertex;
//    private int mVertexStride;
//    private int mTexCoordStride;
//
//    public DrawableFlipVertical2d() {
//        mVertexArray = FULL_RECTANGLE_BUF;
//        mTexCoordArray = FULL_RECTANGLE_TEX_BUF;
//        mCoordsPerVertex = 2;
//        mVertexStride = mCoordsPerVertex * SIZEOF_FLOAT;
//        mVertexCount = FULL_RECTANGLE_COORDS.length / mCoordsPerVertex;
//        mTexCoordStride = 2 * SIZEOF_FLOAT;
//    }
//
//    /**
//     * Returns the array of vertices.
//     * <p>
//     * To avoid allocations, this returns internal state.  The caller must not modify it.
//     */
//    public FloatBuffer getVertexArray() {
//        return mVertexArray;
//    }
//
//    /**
//     * Returns the array of texture coordinates.
//     * <p>
//     * To avoid allocations, this returns internal state.  The caller must not modify it.
//     */
//    public FloatBuffer getTexCoordArray() {
//        return mTexCoordArray;
//    }
//
//    /**
//     * Returns the number of vertices stored in the vertex array.
//     */
//    public int getVertexCount() {
//        return mVertexCount;
//    }
//
//    /**
//     * Returns the width, in bytes, of the data for each vertex.
//     */
//    public int getVertexStride() {
//        return mVertexStride;
//    }
//
//    /**
//     * Returns the width, in bytes, of the data for each texture coordinate.
//     */
//    public int getTexCoordStride() {
//        return mTexCoordStride;
//    }
//
//    /**
//     * Returns the number of position coordinates per vertex.  This will be 2 or 3.
//     */
//    public int getCoordsPerVertex() {
//        return mCoordsPerVertex;
//    }
//}
