package com.gmm.www.douyin.filter;

import com.gmm.www.douyin.face.Face;

import java.util.List;

/**
 * @author:gmm
 * @date:2020/7/10
 * @类说明:
 */
public class FilterChain {
    public FilterContext filterContext;
    private List<AbstractFilter>  filters;
    private int index;

    public FilterChain(List<AbstractFilter> filters,int index,FilterContext filterContext) {
        this.filters = filters;
        this.index = index;
        this.filterContext = filterContext;
    }

    public int proceed(int textureId) {
        if (index >= filters.size()) {
            return textureId;
        }

        FilterChain newFilterChain = new FilterChain(filters,index+1,filterContext);
        AbstractFilter abstractFilter = filters.get(index);
        return abstractFilter.onDraw(textureId,newFilterChain);
    }

    public void setSize(int width,int height) {
        filterContext.setSize(width,height);
    }

    public void setTransformMatrix(float[] mtx) {
        filterContext.setTransformMatrix(mtx);
    }

    public void setFace(Face face) {
        filterContext.setFace(face);
    }

    public void release() {
        for (AbstractFilter filter : filters) {
            filter.release();
        }
    }
}
