package com.zw.bigdata.hive.udaf;

import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.udf.generic.AbstractGenericUDAFResolver;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;

/**
 * 计算分组中的最大值(获取计算器)
 * <pre>
 *     <code>
 *         add jar /home/zkpk/doc/hive-demo.jar;
 *         CREATE TEMPORARY FUNCTION collectList AS 'com.zw.hive.w4.udaf.CollectListUDAFResolver';
 *
 *
 *         数据准备(udaf_demo_data_1)
 *             1,a
 *             1,a
 *             1,b
 *             2,c
 *             2,d
 *             2,d
 *         创建表
            create external table hive_udaf_data_1 (
              id int,
              value string
            )
            comment 'UDAF演示表'
            row format delimited fields terminated by ','
            stored as textfile location '/hw/hive/udaf/1';
 *
 *         加载数据
 *           load data local inpath '/home/zkpk/doc/hive/udaf_demo_data_1' overwrite into table hive_udaf_data_1;
 *
 *         执行SQL
 *           SELECT id, collectList(value) FROM hive_udaf_data_1 GROUP BY id;
 *     </code>
 * </pre>
 * <p>
 * Created by zhangws on 16/9/18.
 */
public class CollectListUDAFResolver extends AbstractGenericUDAFResolver {

    /**
     * 返回计算器
     *
     * @param parameters
     *
     * @return
     *
     * @throws SemanticException
     */
    @Override
    public GenericUDAFEvaluator getEvaluator(TypeInfo[] parameters)
            throws SemanticException {
        if (parameters.length != 1) {
            throw new UDFArgumentTypeException(parameters.length - 1, "Exactly one argument is expected.");
        }
        if (parameters[0].getCategory() != ObjectInspector.Category.PRIMITIVE) {
            throw new UDFArgumentTypeException(0, "Only primitive type arguments are accepted.");
        }
        return new CollectListUDAFEvaluator();
    }
}
