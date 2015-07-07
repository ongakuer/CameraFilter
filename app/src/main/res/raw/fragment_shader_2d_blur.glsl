#define SAMPLES 9
precision highp float;
uniform sampler2D uTexture;

varying vec2 vTextureCoord;
varying vec2 vBlurTextureCoord[SAMPLES];

void main()
{
    vec3 sum = vec3(0.0);
    vec4 fragColor = texture2D(uTexture,vTextureCoord);


    sum += texture2D(uTexture, vBlurTextureCoord[0]).rgb * 0.05;
    sum += texture2D(uTexture, vBlurTextureCoord[1]).rgb * 0.09;
    sum += texture2D(uTexture, vBlurTextureCoord[2]).rgb * 0.12;
    sum += texture2D(uTexture, vBlurTextureCoord[3]).rgb * 0.15;
    sum += texture2D(uTexture, vBlurTextureCoord[4]).rgb * 0.18;
    sum += texture2D(uTexture, vBlurTextureCoord[5]).rgb * 0.15;
    sum += texture2D(uTexture, vBlurTextureCoord[6]).rgb * 0.12;
    sum += texture2D(uTexture, vBlurTextureCoord[7]).rgb * 0.09;
    sum += texture2D(uTexture, vBlurTextureCoord[8]).rgb * 0.05;

    gl_FragColor = vec4(sum,fragColor.a);
}