uniform mat4 uMVPMatrix;
uniform mat4 uTexMatrix;

attribute vec4 aPosition;

attribute vec4 aTextureCoord;
attribute vec4 aExtraTextureCoord;

varying vec2 vTextureCoord;
varying vec2 vExtraTextureCoord;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    vExtraTextureCoord = vec2(aExtraTextureCoord.x, 1.0 - aExtraTextureCoord.y);  //OpenGL纹理系统坐标 与 Android图像坐标 Y轴是颠倒的。这里旋转过来
    vTextureCoord = (uTexMatrix * aTextureCoord).xy;
}