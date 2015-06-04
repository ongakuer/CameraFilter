uniform mat4 uMVPMatrix;
uniform mat4 uTexMatrix;

attribute vec4 aPosition;

attribute vec4 aTextureCoord;
attribute vec4 aExtraTextureCoord;

varying vec2 vTextureCoord;
varying vec2 vExtraTextureCoord;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    vExtraTextureCoord = aExtraTextureCoord.xy;
    vTextureCoord = (uTexMatrix * aTextureCoord).xy;
}