package com.gmm.www.douyin.test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author:gmm
 * @date:2020/7/21
 * @类说明:
 */
public class View {

    public int x;
    public int y;
    public int width;
    public int height;
    public int area;
    public List<View> childViews = new ArrayList<>();

    public View(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static void main(String[] args) {
        View rootView = new View(0, 0, 480, 720);
//        View view0 = new View(0, 0, 300, 300);
        View view1 = new View(0, 0, 200, 200);
        View view2 = new View(200, 200, 20, 20);
        View view3 = new View(250, 250, 50, 50);

//        rootView.childViews.add(view0);
        rootView.childViews.add(view1);
        rootView.childViews.add(view2);
        rootView.childViews.add(view3);

        View view11 = new View(0, 0, 80, 80);
        View view12 = new View(100, 100, 100, 100);
        View view13 = new View(100, 100, 50, 50);

        view1.childViews.add(view11);
        view1.childViews.add(view12);
        view1.childViews.add(view13);


        assert (findMostVisibleView(rootView) == view11);
    }

    public static View findMostVisibleView(View rootView) {

        if (rootView.childViews.isEmpty()) {
            return rootView;
        }

        calArea(rootView);

        View maxView = getResult(rootView);
        System.out.println("最大view.x:" + maxView.x + ",y:" + maxView.y + ",width:" + maxView.width + ",height:" + maxView.height +
                ",可视面积：" + maxView.area);
        return maxView;
    }

    //计算可见视图面积
    public static void calArea(View rootView) {

        for (int i = 0; i < rootView.childViews.size(); i++) {
            View curView = rootView.childViews.get(i);
            if (i < rootView.childViews.size() - 1) {
                int j = i + 1;
                curView.area = curView.width * curView.height;
                while (j < rootView.childViews.size() && rootView.childViews.get(j) != null) {
                    View nextView = rootView.childViews.get(j);
                    //被下一个View完全覆盖
                    if (curView.x >= nextView.x && curView.width <= nextView.width && curView.y >= nextView.y && curView.height <= nextView.height) {
                        curView.area = 0;
                    } else {
                        if (isCover(curView, nextView)) {//有部分重叠，需要减去重叠面积
                            int coverWidth = 0;
                            int coverHeight = 0;
                            if (nextView.x >= curView.x) {
                                if (nextView.x + nextView.width > curView.x + curView.width) {
                                    coverWidth = curView.x + curView.width - nextView.x;
                                } else {
                                    coverWidth = nextView.width;
                                }
                            } else {
                                coverWidth = nextView.x + nextView.width - curView.x;
                            }
                            if (nextView.y >= curView.y) {
                                if (nextView.y + nextView.height > curView.y + curView.height) {
                                    coverHeight = curView.y + curView.height - nextView.y;
                                } else {
                                    coverHeight = nextView.height;
                                }
                            } else {
                                coverHeight = nextView.y + nextView.height - curView.y;
                            }
                            curView.area = curView.area - coverWidth * coverHeight;
                        }
                    }
                    j++;
                }

            } else {
                curView.area = curView.width * curView.height;
            }
            if (!curView.childViews.isEmpty()) {
                calChild(curView);
            }

        }


    }

    //嵌套情况处理
    public static void calChild(View view) {
        for (int i = 0; i < view.childViews.size(); i++) {
            if (!view.childViews.get(i).childViews.isEmpty()) {
                calChild(view.childViews.get(i));
            }
            view.area -= view.childViews.get(i).width * view.childViews.get(i).height;
            if (i > 0 && isCover(view.childViews.get(i - 1), view.childViews.get(i))) {
                int coverWidth = 0;
                int coverHeight = 0;
                if (view.childViews.get(i).x >= view.childViews.get(i - 1).x) {
                    if (view.childViews.get(i).x + view.childViews.get(i).width < view.childViews.get(i - 1).x + view.childViews.get(i - 1).width) {
                        coverWidth = view.childViews.get(i).width;
                    } else {
                        coverWidth = view.childViews.get(i - 1).x + view.childViews.get(i - 1).width - view.childViews.get(i).x;
                    }
                } else {
                    coverWidth = view.childViews.get(i).x + view.childViews.get(i).width - view.childViews.get(i - 1).x;
                }
                if (view.childViews.get(i).y < view.childViews.get(i - 1).y) {
                    coverHeight = view.childViews.get(i).y + view.childViews.get(1).height - view.childViews.get(i - 1).y;
                } else {
                    if (view.childViews.get(i).y + view.childViews.get(i).height < view.childViews.get(i - 1).y + view.childViews.get(i - 1).height) {
                        coverHeight = view.childViews.get(i).height;
                    } else {
                        coverHeight = view.childViews.get(i - 1).y + view.childViews.get(i - 1).height - view.childViews.get(i).y;
                    }

                }
                view.area += coverWidth * coverHeight;
            }

        }
//        view.area = area;
    }

    //判断同级View是否存在重叠
    public static boolean isCover(View view, View view1) {
        if (view.x <= view1.x && view.x + view.width <= view1.x) {
            return false;
        } else if (view1.x <= view.x && view1.x + view1.width <= view.x) {
            return false;
        } else if (view.y <= view1.y && view.y + view.height <= view1.y) {
            return false;
        } else if (view1.y <= view.y && view1.y + view1.height <= view.y) {
            return false;
        }
        return true;
    }

    public static View getResult(View view) {
        View result = view.childViews.get(0);
        for (int i = 0; i < view.childViews.size(); i++) {
            View curView = view.childViews.get(i);
            if(!view.childViews.get(i).childViews.isEmpty()) {
                curView = getResult(view.childViews.get(i));
            }

            if (result.area < curView.area) {
                result = curView;
            }
        }
        return result;
    }
}
