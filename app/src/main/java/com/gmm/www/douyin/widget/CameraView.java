package com.gmm.www.douyin.widget;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

/**
 * @author:gmm
 * @date:2020/7/8
 * @类说明:
 */
public class CameraView extends GLSurfaceView {

    private Speed mSpeed = Speed.MODE_NORMAL;

    public enum Speed {
        MODE_EXTRA_SLOW,MODE_SLOW,MODE_NORMAL,MODE_FAST,MODE_EXTRA_FAST
    }

    private CameraRender render;

    public CameraView(Context context) {
        this(context,null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //使用OpenGL ES2.0 context
        setEGLContextClientVersion(2);

        //设置渲染回调接口
        render = new CameraRender(this);
        setRenderer(render);

        /**
         * 刷新方式：
         *     RENDERMODE_WHEN_DIRTY 手动刷新，調用requestRender();
         *     RENDERMODE_CONTINUOUSLY 自動刷新，大概16ms自動回調一次onDraw方法
         */
        //注意必须在setRenderer 后面。
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        render.onSurfaceDestroyed();
    }

    public void setSpeed(Speed speed) {
        mSpeed = speed;
    }

    public void startRecord() {
        //速度 speed小于1就是放慢，大于1就是加快
        float speed = 1;
        switch (mSpeed) {
            case MODE_EXTRA_SLOW:
                speed = 0.3f;
                break;

            case MODE_SLOW:
                speed = 0.5f;
                break;

            case MODE_NORMAL:
                speed = 1.0f;
                break;

            case MODE_FAST:
                speed = 2.f;
                break;

            case MODE_EXTRA_FAST:
                speed = 3.f;
                break;
        }
        render.startRecord(speed);
    }

    public void stopRecord() {
        render.stopRecord();
    }
}
