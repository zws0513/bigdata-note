package com.zw.bigdata.hive.udtf;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

import java.util.ArrayList;

/**
 *
 * <p>
 *     1. 继承org.apache.hadoop.hive.ql.udf.generic.GenericUDTF,实现initialize, process, close三个方法。
 *     2. UDTF首先会调用initialize方法，此方法返回UDTF的返回行的信息（返回个数，类型）。
 *     3. 初始化完成后，会调用process方法,真正的处理过程在process函数中，在process中，每一次forward()调用产生一行；
 *        如果产生多列可以将多个列的值放在一个数组中，然后将该数组传入到forward()函数。
 *     4. 最后close()方法调用，对需要清理的方法进行清理。
 *     5. 代码实例，实现的功能比较简单，将字符串（key1:20;key2:30;key3:40）按照分好拆分行按照冒号拆分列进行展示。
 * </p>
 *
 * <pre>
 *     <code>
 *         hive-demo.jar放到${HIVE_HOME}/auxli目录下
 *         CREATE TEMPORARY FUNCTION explode_map AS 'com.zw.hive.w4.udtf.ExplodeMapUDTF';
 *
 *         准备数据(hive_udtf_demo_data_1)
 *         key1:20;key2:30;key3:40
 *
           create external table udtf_demo_data_1 (
             value string
           );

 *         加载数据
 *           load data local inpath '/home/zkpk/doc/hive/udtf_demo_data_1' overwrite into table udtf_demo_data_1;
 *
 *         SELECT explode_map(value) AS (col1,col2) from udtf_demo_data_1;
 *     </code>
 * </pre>
 *
 * Created by zhangws on 16/9/18.
 */
public class ExplodeMapUDTF extends GenericUDTF {

    /**
     * 返回UDTF的返回行的信息（返回个数，类型）
     * @param args
     * @return
     * @throws UDFArgumentException
     */
    @Override
    public StructObjectInspector initialize(ObjectInspector[] args) throws UDFArgumentException {
        if (args.length != 1) {
            throw new UDFArgumentLengthException("ExplodeMap takes only one argument");
        }
        if (args[0].getCategory() != ObjectInspector.Category.PRIMITIVE) {
            throw new UDFArgumentException("ExplodeMap takes string as a parameter");
        }

        ArrayList<String> fieldNames = new ArrayList<String>();
        ArrayList<ObjectInspector> fieldOIs = new ArrayList<ObjectInspector>();
        fieldNames.add("col1");
        fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
        fieldNames.add("col2");
        fieldOIs.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);

        return ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, fieldOIs);
    }

    /**
     * 每一次forward()调用产生一行；如果产生多列可以将多个列的值放在一个数组中，然后将该数组传入到forward()函数。
     * @param args
     * @throws HiveException
     */
    @Override
    public void process(Object[] args) throws HiveException {
        String input = args[0].toString();
        String[] test = input.split(";");
        for (String aTest : test) {
            try {
                String[] result = aTest.split(":");
                forward(result);
            } catch (Exception e) {
                // nothing
            }
        }
    }

    /**
     * 对需要清理的方法进行清理。
     * @throws HiveException
     */
    @Override
    public void close() throws HiveException {

    }
}
