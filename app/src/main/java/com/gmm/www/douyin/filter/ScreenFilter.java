package com.gmm.www.douyin.filter;

import android.content.Context;

import com.gmm.www.douyin.R;

/**
 * @author:gmm
 * @date:2020/7/8
 * @类说明:  最后真正渲染显示的Filter，直接显示到屏幕上，不用写入FBO中
 */
public class ScreenFilter extends AbstractFilter {

    public ScreenFilter(Context context) {
        super(context,R.raw.base_vert,R.raw.base_frag);
    }

}
