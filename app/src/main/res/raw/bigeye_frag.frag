precision mediump  float;//数据精度
varying vec2 aCoord;// 当前要上颜色的像素点（x,y）

uniform sampler2D vTexture;  //图片
uniform vec2 left_eye; //float[2]-> x,y   //需要从java传过来
uniform vec2 right_eye;                    //需要从java传过来

//r:要画的点与眼睛的距离
//rmax: 最大作用半径
// 0.4： 放大系数：-1-》1 大于0就是放大
//返回缩放后的点到眼睛的距离
float fs(float r,float rmax) {
    return (1.0-pow(r/rmax-1.0,2.0)*0.4)*r;
}

//返回一个缩放后的新的或者原来的像素点坐标
vec2 newCoord(vec2 coord,vec2 eye,float rmax) {
    vec2 p = coord;
    //得到要画的点coord 与眼睛 eye的距离
    float r = distance(coord,eye);
    if (r < rmax) { //在范围之内，进行缩放处理
        //缩放后的点 与眼睛的距离
        float fsr = fs(r,rmax);
        // (缩放后的点p - 眼睛点坐标eye) / (原点coord-眼睛点坐标eye) = fsr/r
        p = fsr/r * (coord-eye) + eye;
    }
    return p;//如果在rmax范围内，返回新点p坐标，否则返回原点coord坐标
}


void main() {
//    gl_FragColor = texture2D(vTexture,aCoord);//从图片中找到这个像素点对应的颜色rgba

    float rmax = distance(left_eye,right_eye)/2.0;//超过这个距离的像素点不做缩放上色处理

    //aCoord： 要上色的这个像素点      left_eye：左眼坐标    rmax：最大距离范围
    vec2 p = newCoord(aCoord,left_eye,rmax);//如果在左眼范围，那就返回新点坐标，否则返回原来的坐标
    p = newCoord(p,right_eye,rmax);//如果不在左眼范围，有可能在右眼范围，那就返回新点坐标，否咋还是返回原来的坐标

    gl_FragColor = texture2D(vTexture,p);//从图片中找到这个缩放后新像素点对应的颜色rgba（与原来要绘制的颜色相同，但是像素的坐标位置因为缩放而改变）
}