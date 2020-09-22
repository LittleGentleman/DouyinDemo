package com.gmm.www.douyin.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * @author:gmm
 * @date:2020/7/9
 * @类说明:
 */
public class RecordButton extends AppCompatTextView {
    private OnRecordListener mListener;

    public RecordButton(Context context) {
        super(context);
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mListener == null) {

            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mListener.onRecordStart();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mListener.onRecordStop();
                break;
        }

        return true;
    }

    public void setOnRecordListener(OnRecordListener mListener) {
        this.mListener = mListener;
    }

    public interface OnRecordListener {
        void onRecordStart();

        void onRecordStop();
    }
}
