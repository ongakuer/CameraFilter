#extension GL_OES_EGL_image_external : require
precision mediump float;

varying vec2 vTextureCoord;
uniform samplerExternalOES uTexture;

void main() {
    vec4 tc = texture2D(uTexture, vTextureCoord);
    float color = ((tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11) - 0.5 * 1.5) + 0.8;
    gl_FragColor = vec4(color, color + 0.15, color, 1.0);
}