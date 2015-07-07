#extension GL_OES_EGL_image_external : require
#define KERNEL_SIZE 9

precision highp float; //指定默认精度

varying vec2 vTextureCoord;
uniform samplerExternalOES uTexture;

uniform float uKernel[KERNEL_SIZE];
uniform vec2 uTexOffset[KERNEL_SIZE];
uniform float uColorAdjust;

void main() {
    int i = 0;
    vec4 sum = vec4(0.0);

    for (i = 0; i < KERNEL_SIZE; i++) {
        vec4 texc = texture2D(uTexture, vTextureCoord + uTexOffset[i]);
        sum += texc * uKernel[i];
    }
    sum += uColorAdjust;

    gl_FragColor = sum;
}