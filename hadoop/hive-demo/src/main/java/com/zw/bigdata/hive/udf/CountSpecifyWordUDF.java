package com.zw.bigdata.hive.udf;

import org.apache.hadoop.hive.ql.exec.UDF;

/**
 *
 * 计算src中包含word的个数
 *
 * <p>
 *     UDF是作用于单个数据行，产生一个数据行;
 *     用户必须要继承UDF，且必须至少实现一个evalute方法，该方法并不在UDF中
 *     但是Hive会检查用户的UDF是否拥有一个evalute方法
 * </p>
 * <pre>
 *     <code>
 *         add jar /home/zkpk/doc/hive/hive-udf.jar;
 *         create temporary function countSpecifyWord com.zw.hive.w4.udf.CountSpecifyWordUDF;
 *         select countSpecifyWord(col) from table_name;
 *     </code>
 * </pre>
 *
 *
 * Created by zhangws on 16/8/27.
 */
public class CountSpecifyWordUDF extends UDF {

    /**
     * 计算src中包含word的个数
     * @param src src
     * @param word word
     * @return counter
     */
    public int evaluate(String src, String word) {
        try {
            int counter=0;
            if (!src.contains(word)) {
                return 0;
            }
            int pos;
            while((pos = src.indexOf(word)) != -1){
                counter++;
                src = src.substring(pos + word.length());
            }
            return counter;
        } catch (Exception e) {
            return 0;
        }
    }

}
