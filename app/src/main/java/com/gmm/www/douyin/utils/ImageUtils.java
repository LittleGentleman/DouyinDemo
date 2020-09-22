package com.gmm.www.douyin.utils;

import android.graphics.ImageFormat;

import java.nio.ByteBuffer;

import androidx.camera.core.ImageProxy;

/**
 * @author:gmm
 * @date:2020/7/10
 * @类说明:
 */
public class ImageUtils {

    static ByteBuffer yuv420;

    //将摄像头采集的图像数据进行处理，将ByteBuffer转换成字节数组
    public static byte[] getBytes(ImageProxy image) {
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            throw new IllegalStateException("根据文档，Camerax图像分析返回的就是YUV420!");
        }
        ImageProxy.PlaneProxy[] planes = image.getPlanes();//三个，分别是 Y、U、V
        /**
         * YUV_420_888 例：4 x 4
         * YYYY
         * YYYY
         * YYYY
         * YYYY
         * UU
         * UU
         * VV
         * VV
         *
         * size = 4*4+2*2+2*2 = 4 * 4 * 3 / 2
         */
        int size = image.getWidth() * image.getHeight() * 3/2;
        // todo 避免内存抖动.
        if (yuv420 == null || yuv420.capacity() < size) {
            yuv420 = ByteBuffer.allocate(size);
        }
        yuv420.position(0);//从0开始

        /**
         * Y数据
         */
        ImageProxy.PlaneProxy plane = planes[0];
        //pixelStride = 1 : 取值无间隔
        //pixelStride = 2 : 间隔1个字节取值
        // y的此数据应该都是1
        int pixelStride = plane.getPixelStride();//返回1或2，1表示这个plane只存在一种数据，2表示这个plane存在多种数据
        //大于等于宽， 表示连续的两行数据的间隔
        //  如：640x480的数据，
        //  可能得到640
        //  可能得到650，表示每行最后10个字节为补位的数据
        int rowStride = plane.getRowStride();//返回的步长有可能大于width，因为存在补齐字节
        ByteBuffer buffer = plane.getBuffer();//所有的Y数据，以及可能存在的补齐字节
        byte[] row = new byte[image.getWidth()];//这里的都是有效数据，无补齐字节
        //每行要排除无效数据，但是需要注意：实际测试中，最后一行没有补位字节数据
        byte[] skipRow = new byte[rowStride-image.getWidth()];//这里的都是补齐字节
        for (int i = 0; i < image.getHeight(); i++) {
            buffer.get(row);//从ByteBuffer中只获取有效的字节
            yuv420.put(row);
            //不是最后一行，就有可能存在补齐字节，把补齐字节放入skipRow中
            if (i < image.getHeight()-1) {
                buffer.get(skipRow);
            }

        }

        /**
         * U V数据
         */
        for (int i = 1; i < 3; i++) {
            plane = planes[i];
            pixelStride = plane.getPixelStride();
            // uv数据的rowStride可能是
            // 如：640的宽
            // 可能得到320， pixelStride 为1
            // 可能大于320同时小于640，有为了补位的无效数据  pixelStride 为1
            // 可能得到640 uv数据在一起，pixelStride为2
            // 可能大于640，有为了补位的无效数据 pixelStride为2
            rowStride = plane.getRowStride();//有效数据+补位字节
            buffer = plane.getBuffer();
            int uvWidth = image.getWidth()/2;//有效数据
            int uvHeight = image.getHeight()/2;

            for (int j = 0; j < uvHeight; j++) {
                for (int k = 0; k < rowStride; k++) {
                    //最后一行，是没有补齐字节的
                    if (j == uvHeight-1) {
                        //只存在U 或者 只存在V 数据
                        if (pixelStride == 1) {
                            // 结合外层if 则表示：
                            //  如果是最后一行，我们就不管结尾的占位数据了
                            if (k >= uvWidth) {
                                break;
                            }
                        } else if (pixelStride == 2) {
                            if (k >= image.getWidth() -1) {
                                break;
                            }
                        }
                    }
                    byte b = buffer.get();
                    if (pixelStride == 2) {
                        //打包格式 uv在一起,偶数位取出来是U数据： 0 2 4 6
                        if (k < image.getWidth() && k%2 == 0) {//结合上面的if，说明这个buffer中的数据都是有效数据
                            yuv420.put(b);
                        }
                    } else if (pixelStride == 1) {
                        if (k < uvWidth) {
                            yuv420.put(b);
                        }
                    }
                }
            }
        }

        byte[] result = yuv420.array();
        return result;
    }
}
