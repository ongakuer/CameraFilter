#define SAMPLES 9

uniform mat4 uMVPMatrix;  // MVP 的变换矩阵（整体变形）
uniform mat4 uTexMatrix;  // Texture 的变换矩阵 （只对texture变形）

uniform float uTexelWidthOffset;
uniform float uTexelHeightOffset;

attribute vec4 aPosition;
attribute vec4 aTextureCoord;

varying vec2 vTextureCoord;
varying vec2 vBlurTextureCoord[SAMPLES];


void main() {
    gl_Position = uMVPMatrix * aPosition;
    vTextureCoord = (uTexMatrix * aTextureCoord).xy;

    int multiplier = 0;
    vec2 blurStep;
    vec2 offset = vec2(uTexelHeightOffset, uTexelWidthOffset);

    for (int i = 0; i < SAMPLES; i++)
    {
       multiplier = (i - ((SAMPLES-1) / 2));
       // ToneCurve in x (horizontal)
       blurStep = float(multiplier) * offset;
       vBlurTextureCoord[i] = vTextureCoord + blurStep;
    }
}