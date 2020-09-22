package com.gmm.www.douyin.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.gmm.www.douyin.R;

/**
 * @author:gmm
 * @date:2020/7/9
 * @类说明:  最开始的图像Filter，不会真正渲染到屏幕上，第一个接收摄像头数据的地方
 */
public class CameraFilter extends AbstractFboFilter {
    private float mtx[];
    private int vMatrix;


    public CameraFilter(Context context) {
        super(context, R.raw.camera_vert, R.raw.camera_frag);
    }

    @Override
    protected void initGL(Context context, int vertexShaderId, int fragmentShaderId) {
        super.initGL(context, vertexShaderId, fragmentShaderId);
        vMatrix = GLES20.glGetUniformLocation(program,"vMatrix");
    }

    @Override
    public int onDraw(int texture, FilterChain filterChain) {
        mtx = filterChain.filterContext.cameraMtx;
        return super.onDraw(texture, filterChain);
    }

    @Override
    public void beforeDraw(FilterContext filterContext) {
        super.beforeDraw(filterContext);

        //给片元着色器设置vMatrix矩阵
        GLES20.glUniformMatrix4fv(vMatrix,1,false,mtx,0);
    }

}
