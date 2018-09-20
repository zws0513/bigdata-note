package com.zw.bigdata.hdfs.util;

/**
 * Created by zhangws on 2018/9/20.
 */
public class Test {

    public static void main(String[] args) throws Exception {
        System.out.println(isChinese("\\xC3\\xA7\\xC2\\xB2\\xC2\\xBE\\xC3\\xA9"));
        System.out.println(isChinese("\\xC3\\xA7\u0094\\xC2\\xB5\\xC3\\xA8\\xC2\\xA7\u0086\\xC3\\xA5\u0089\\xC2\\xA7"));
        System.out.println(isChinese("测试"));
        System.out.println(isChinese("abcdefghijklmnopqrstuvwxyz"));
        System.out.println(isChinese("测试0123456789"));
        System.out.println(isChinese("测试ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        System.out.println(isChinese("unknown"));
        System.out.println(isChinese("为你推荐---测试测试"));
        System.out.println(isChinese("乐趣咪咕"));
        System.out.println(isChinese("体育"));
        System.out.println(isChinese("儿 童"));
        System.out.println(isChinese("简洁版2.0"));
        System.out.println(isChinese("å½±è§"));
    }

    public static boolean isChinese(String string) {
        int n = 0;
        for (int i = 0; i < string.length(); i++) {
            n = (int) string.charAt(i);
            if (!(0x4e00 <= n && n <= 0x9fa5) // 汉字
                    && !(0x20 <= n && n <= 0x7e && n != 0x5c) // 除\以外的，所有ASCII可见字符
//                    && !(0x30 <= n && n <= 0x39) // 数字
//                    && !(0x61 <= n && n <= 0x7a) // 小写字母
//                    && !(0x41 <= n && n <= 0x5a) // 大写字母
//                    && !(0x41 <= n && n <= 0x5a) // 大写字母
                    ) {
                return false;
            }
        }
        return true;
    }
}
