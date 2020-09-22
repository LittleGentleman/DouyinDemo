//顶点着色器

attribute vec4 vPosition;//变量 float[4] 一个顶点 在顶点着色器中，java传过来的变量需要用arrtibute修饰

attribute vec2 vCoord;//纹理坐标  java传过来  像素纹理坐标

varying vec2 aCoord;// 传给片元着色器

uniform mat4 vMatrix;//矩阵，可以控制平移、缩放、旋转、错切


void main() {
    //内置变量：把坐标点复制给gl_Position 就OK了
    gl_Position = vPosition;
//    aCoord = vCoord;
    aCoord = (vMatrix * vec4(vCoord,1.0,1.0)).xy;//将transform变换后的纹理坐标传给片元着色器
}