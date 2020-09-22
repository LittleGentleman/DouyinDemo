package com.gmm.www.douyin.test;

import android.content.Intent;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author:gmm
 * @date:2020/7/21
 * @类说明:
 */
public class Test {

    public static void main(String[] args) {

        System.out.println(value("ZBACD") + "");

        System.out.println(getResult("aabbccdd"));

    }

    public static int value(String str) {
        if (str == null || str.equals("")) {
            return 0;
        }

        int result  = 0;
        for (int i = 0; i < str.length(); i++) {
            result += (str.substring(i,i+1).charAt(0)-'A')*Math.pow(10,str.length()-1-i);
        }

        return result;
    }


    public static String getResult(String string) {
        StringBuffer sb = new StringBuffer();
        char ch = string.charAt(0);
        sb.append(ch);
        for (int i = 1; i < string.length(); i++) {
            if (ch != string.charAt(i)) {
                sb.append(string.charAt(i));
                ch = string.charAt(i);
            }
        }

        return sb.toString();
    }
}
