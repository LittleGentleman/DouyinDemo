package com.gmm.www.douyin.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.gmm.www.douyin.R;
import com.gmm.www.douyin.face.Face;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author:gmm
 * @date:2020/7/10
 * @类说明:
 */
public class BigEyeFilter extends AbstractFboFilter {
    private FloatBuffer left;
    private FloatBuffer right;

    int left_eye;
    int right_eye;
    Face face;

    public BigEyeFilter(Context context) {
        super(context, R.raw.base_vert,R.raw.bigeye_frag);

        //保存眼睛的坐标数据   一个眼睛的坐标(float x,float y)，float占4个字节，两个float所以占8个字节
        left = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder()).asFloatBuffer();
        right = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    @Override
    protected void initGL(Context context, int vertexShaderId, int fragmentShaderId) {
        super.initGL(context, vertexShaderId, fragmentShaderId);
        //片元着色器中变量的索引
        left_eye = GLES20.glGetUniformLocation(program,"left_eye");
        right_eye = GLES20.glGetUniformLocation(program,"right_eye");
    }

    @Override
    public void beforeDraw(FilterContext filterContext) {
        super.beforeDraw(filterContext);
        Face face = filterContext.face;
        if (face == null) {
            left.clear();
            left.put(0).put(0).position(0);
            GLES20.glUniform2fv(left_eye, 1, left);

            right.clear();
            right.put(0).put(0).position(0);
            GLES20.glUniform2fv(right_eye, 1, right);
            return;
        }

        float x = face.left_x / face.imgWidth ; // 0-1
        float y = 1.0f - face.left_y / face.imgHeight;

        left.clear();
        left.put(x).put(y).position(0);

        GLES20.glUniform2fv(left_eye, 1, left);


        x = face.right_x / face.imgWidth;
        y = 1.0f - face.right_y / face.imgHeight;

        right.clear();
        right.put(x).put(y).position(0);

        GLES20.glUniform2fv(right_eye, 1, right);


    }
}
