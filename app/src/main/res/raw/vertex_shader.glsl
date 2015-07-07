uniform mat4 uMVPMatrix;  // MVP 的变换矩阵（整体变形）
uniform mat4 uTexMatrix;  // Texture 的变换矩阵 （只对texture变形）

attribute vec4 aPosition;
attribute vec4 aTextureCoord;

varying vec2 vTextureCoord;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    vTextureCoord = (uTexMatrix * aTextureCoord).xy;
}