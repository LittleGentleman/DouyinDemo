package com.gmm.www.douyin.test;

import android.os.AsyncTask;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.FutureTask;

/**
 * @author:gmm
 * @date:2020/7/15
 * @类说明:
 */
public class QuickSort {

    //升序
    public static int[] sort(int[] array,int start,int end) {
        if (array.length < 1 || start < 0 || end > array.length || start > end)
            return null;

        int zoneIndex = partition(array,start,end);
        if (start < zoneIndex) {
            sort(array,start,zoneIndex-1);
        }
        if (zoneIndex < end) {
            sort(array,zoneIndex+1,end);
        }


        return array;

    }

    //分区
    private static int partition(int[] array, int start, int end) {

        //①随机选取一个基准数
        int povit = (int) (start + Math.random()*(end-start+1));
        //②基准数与数组尾元素交换
        swap(array,povit,end);
        //③定义分区指示器
        int zoneIndex = start-1;
        //遍历数组
        for (int i = start; i <= end; i++) {
            //④如果当前元素大于基准数不处理，当前元素小于或等于基准数再处理
            if (array[i] <= array[end]) {
                //⑤当前元素小于或等于基准数，分区指示器右移一位
                zoneIndex ++;
                //⑥如果当前元素下标小于等于分区指示器不处理，如果当前元素下标大于分区指示器，交换当前元素与分区指示器所指向的元素
                if (i > zoneIndex) {
                    swap(array,i,zoneIndex);
                }
            }
        }

        return zoneIndex;
    }

    private static void swap(int[] array,int a,int b) {
        int temp = array[a];
        array[a] = array[b];
        array[b] = temp;
    }

//    private Class<?> analysisClassInfo(Object object) {
//        Type type = object.getClass().getGenericSuperclass();
//        Type[] params = ((ParameterizedType) type).getActualTypeArguments();
//        return (Class<?>) params[0];
//    }

    public static void main(String[] args) {
        int[] array = {99,2,14,88,13,35,23,54,11,57};
        int[] sort = sort(array, 0, 9);
        for (int i = 0; i < sort.length; i++) {
            System.out.print(sort[i] + " ");
        }

    }
}
