package com.zw.bigdata.hive.udaf;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.io.Text;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 实现按分组中元素的出现次数降序排序，并将每个元素的在分组中的出现次数也一起返回，格式为：[data1, num1, data2, num2, ...]
 * <p>
 * Created by zhangws on 16/9/19.
 */
public class CollectListUDAFEvaluator extends GenericUDAFEvaluator {

    protected PrimitiveObjectInspector inputKeyOI;
    protected StandardListObjectInspector loi;
    protected StandardListObjectInspector internalMergeOI;

    /**
     * <pre>
     *     <code>
     *     m:
     *       PARTIAL1 和 COMPLETE 时, parameters为原始数据;
     *       PARTIAL2 和 FINAL 时, parameters仅为部分聚合数据（只有一个元素）
     *
     *       PARTIAL1 和 PARTIAL2 时, terminatePartial方法的返回值;
     *       FINAL 和 COMPLETE 时, terminate方法的返回值.
     *     </code>
     * </pre>
     *
     * @param m          模式
     * @param parameters 数据参数
     *
     * @return
     *
     * @throws HiveException
     */
    @Override
    public ObjectInspector init(Mode m, ObjectInspector[] parameters) throws HiveException {
        super.init(m, parameters);

        if (m == Mode.PARTIAL1) { // 从原始数据到部分聚合数据的过程（map阶段），将调用iterate()和terminatePartial()方法。
            inputKeyOI = (PrimitiveObjectInspector) parameters[0];
            return ObjectInspectorFactory.getStandardListObjectInspector(
                    ObjectInspectorUtils.getStandardObjectInspector(inputKeyOI));
        } else {
            if (parameters[0] instanceof StandardListObjectInspector) {
                internalMergeOI = (StandardListObjectInspector) parameters[0];
                inputKeyOI = (PrimitiveObjectInspector) internalMergeOI.getListElementObjectInspector();
                loi = (StandardListObjectInspector) ObjectInspectorUtils.getStandardObjectInspector(internalMergeOI);
                return loi;
            } else {
                inputKeyOI = (PrimitiveObjectInspector) parameters[0];
                return ObjectInspectorFactory.getStandardListObjectInspector(
                        ObjectInspectorUtils.getStandardObjectInspector(inputKeyOI));
            }
        }
    }

    static class MkListAggregationBuffer extends AbstractAggregationBuffer {
        List<Object> container = Lists.newArrayList();
    }


    /**
     * 返回存储临时聚合结果的AggregationBuffer对象。
     */
    @Override
    public AggregationBuffer getNewAggregationBuffer() throws HiveException {
        return new MkListAggregationBuffer();
    }

    /**
     * 重置聚合结果对象，以支持mapper和reducer的重用。
     *
     * @param agg
     */
    @Override
    public void reset(AggregationBuffer agg) throws HiveException {
        ((MkListAggregationBuffer) agg).container.clear();
    }

    /**
     * 迭代处理原始数据parameters并保存到agg中。
     *
     * @param agg
     * @param parameters 原始数据
     */
    @Override
    public void iterate(AggregationBuffer agg, Object[] parameters) throws HiveException {
        if (parameters == null || parameters.length != 1) {
            return;
        }
        Object key = parameters[0];
        if (key != null) {
            MkListAggregationBuffer myagg = (MkListAggregationBuffer) agg;
            putIntoList(key, myagg.container);
        }
    }

    private void putIntoList(Object key, List<Object> container) {
        Object pCopy = ObjectInspectorUtils.copyToStandardObject(key, this.inputKeyOI);
        container.add(pCopy);
    }

    /**
     * 以持久化的方式返回agg表示的部分聚合结果，这里的持久化意味着返回值只能Java基础类型、数组、
     * 基础类型包装器、Hadoop的Writables、Lists和Maps。
     *
     * @param agg
     *
     * @return partial aggregation result.
     */
    @Override
    public Object terminatePartial(AggregationBuffer agg) throws HiveException {
        MkListAggregationBuffer myagg = (MkListAggregationBuffer) agg;
        return Lists.newArrayList(myagg.container);
    }

    /**
     * 合并由partial表示的部分聚合结果到agg中。
     *
     * @param agg
     * @param partial
     */
    @Override
    public void merge(AggregationBuffer agg, Object partial) throws HiveException {
        if (partial == null) {
            return;
        }
        MkListAggregationBuffer myagg = (MkListAggregationBuffer) agg;
        List<Object> partialResult = (List<Object>) internalMergeOI.getList(partial);
        for (Object ob : partialResult) {
            putIntoList(ob, myagg.container);
        }
    }

    /**
     * 返回最终结果。
     *
     * @param agg
     *
     * @return final aggregation result.
     */
    @Override
    public Object terminate(AggregationBuffer agg) throws HiveException {
        MkListAggregationBuffer myagg = (MkListAggregationBuffer) agg;
        Map<Text, Integer> map = Maps.newHashMap();
        // 统计相同值得个数
        for (int i = 0; i < myagg.container.size(); i++) {
            Text key = (Text) myagg.container.get(i);
            if (map.containsKey(key)) {
                map.put(key, map.get(key) + 1);
            } else {
                map.put(key, 1);
            }
        }
        // 排序
        List<Map.Entry<Text, Integer>> listData = Lists.newArrayList(map.entrySet());
        Collections.sort(listData, (o1, o2) -> {
            if (o1.getValue() < o2.getValue())
                return 1;
            else if (o1.getValue().equals(o2.getValue()))
                return 0;
            else
                return -1;
        });

        // 合并输出
        List<Object> ret = Lists.newArrayList();
        for (Map.Entry<Text, Integer> entry : listData) {
            ret.add(entry.getKey());
            ret.add(new Text(entry.getValue().toString()));
        }
        return ret;
    }
}
