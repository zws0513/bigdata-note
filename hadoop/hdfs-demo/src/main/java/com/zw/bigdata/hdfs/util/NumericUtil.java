package com.zw.util;

import java.text.NumberFormat;

/**
 *
 * 数值运算工具类
 *
 * Created by zhangws on 16/8/13.
 */
public class NumericUtil {

    /**
     * 计算百分比
     * <p>
     *     两位小数
     * </p>
     *
     * @param numerator 分子
     * @param denominator 分母
     * @return 百分比
     */
    public static String percent(int numerator, int denominator) {
        return percent(numerator, denominator);
    }

    /**
     * 计算百分比
     * <p>
     *     两位小数
     * </p>
     *
     * @param numerator 分子
     * @param denominator 分母
     * @return 百分比
     */
    public static String percent(long numerator, long denominator) {
        String str;
        double  p3  =  1.0 * numerator  /  denominator;
        NumberFormat nf  =  NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits( 2 );
        str  =  nf.format(p3);
        return  str;
    }
}
