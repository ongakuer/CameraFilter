###CameraFilter

研究 Android 用 OpenGL ES 2.0 处理相机预览和滤镜。


#####实时滤镜
使用 GLSurfaceView 显示相机画面，用 OpenGL 实现不同滤镜效果。


[android-gpuimage](https://github.com/CyberAgent/android-gpuimage) 使用 ```GL_TEXTURE_2D``` 作为纹理目标。 在处理相机预览画面时，需要将每帧的图像颜色由 YUV 转成 RBGA，画面不流畅。

这里使用 ```GL_TEXTURE_EXTERNAL_OES``` 作为纹理目标，绕过了 YUV 转 RBGA 步骤。 另外简单处理了一下预览画面的比例。


#####录制视频
使用 [grafika](https://github.com/google/grafika) 方案（需要Android 4.3)，用 MediaCodec、MediaMuxer 编码生成 MP4。使用 MediaCodec 的 [createInputSurface](http://developer.android.com/reference/android/media/MediaCodec.html#createInputSurface())，接收来自 OpenGL 渲染的画面。


#####图片滤镜
与 [android-gpuimage](https://github.com/CyberAgent/android-gpuimage) 一样，使用 ```GL_TEXTURE_2D``` 作为纹理目标，传入需要处理的图片，然后用滤镜渲染。不过因为纹理目标不同，不能直接套用相机的滤镜，导致每个滤镜分别要写两次……