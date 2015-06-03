#extension GL_OES_EGL_image_external : require
precision mediump float;

varying vec2 vTextureCoord;
varying vec2 vExtraTextureCoord;

uniform samplerExternalOES uTexture;
uniform sampler2D uExtraTexture;

void main() {
    vec4 base = texture2D(uTexture, vTextureCoord);
    vec4 overlay = texture2D(uExtraTexture, vExtraTextureCoord);
    gl_FragColor = base * (overlay.a * (base / base.a) + (2.0 * overlay * (1.0 - (base / base.a)))) + overlay * (1.0 - base.a) + base * (1.0 - overlay.a);
}