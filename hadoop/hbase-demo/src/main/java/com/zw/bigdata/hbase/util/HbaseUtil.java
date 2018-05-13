package com.zw.bigdata.hbase.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * HBase操作演示程序
 */
public class HbaseUtil {

    private static Log log = LogFactory.getLog(HbaseUtil.class);

    static Configuration configuration = null;

    static {
        configuration = HBaseConfiguration.create();
        //configuration.set("hbase.zookeeper.quorum", "hsm01,hss01,hss02");
        configuration.addResource("hbase/hbase-site.xml");
    }

    public static void main(String[] args) throws IOException {
        //createTable("blog", new String[]{"author", "contents"});
        // dropTable("blog");

//        // 行键1
//        Map<String, Map<String, String>> map1 = new HashMap<>();
//        // 列族author的列值
//        Map<String, String> author1 = new HashMap<>();
//        author1.put("name", "张三");
//        author1.put("school", "MIT");
//        map1.put("author", author1);
//        // 列族contents的列值
//        Map<String, String> contents1 = new HashMap<>();
//        contents1.put("content", "吃饭了吗?");
//        map1.put("contents", contents1);
//        putData("blog", "rk11", map1);
//
//        // 行键2
//        Map<String, Map<String, String>> map2 = new HashMap<>();
//        // 列族author的列值
//        Map<String, String> author2 = new HashMap<>();
//        author2.put("name", "lisi");
//        author2.put("school", "Harvard");
//        map2.put("author", author2);
//        // 列族contents的列值
//        Map<String, String> contents2 = new HashMap<>();
//        contents2.put("content", "eat food.");
//        map2.put("contents", contents2);
//        putData("blog", "rk12", map2);


//        putFamily("blog", "note");

//        getData("blog", "rk1", null, null);
//        getData("blog", "rk1", "author", null);
//         getData("blog", "rk1", "author", new String[] { "name", "school" });

//        scan("blog");

//        deleteData("blog", "rk12", "author", new String[]{"name", "school"});
//        deleteData("blog", "rk11", "author", new String[]{"name"});
//        deleteData("blog", "rk10", "author", null);
//        deleteData("blog", "rk9", null, null);

//        count("blog");

        incr("scores", "lisi", "courses", "eng", 2);
    }

    /**
     * 创建表
     *
     * @param tableName   表名
     * @param familyNames 列族
     *
     * @throws IOException
     */
    public static void createTable(String tableName, String[] familyNames) throws IOException {

        try (Connection connection = ConnectionFactory.createConnection(configuration);
             Admin admin = connection.getAdmin()
        ) {
            TableName table = TableName.valueOf(tableName);
            if (admin.tableExists(table)) {
                log.info(tableName + " already exists");
            } else {
                HTableDescriptor hTableDescriptor = new HTableDescriptor(table);
                for (String family : familyNames) {
                    hTableDescriptor.addFamily(new HColumnDescriptor(family));
                }
                admin.createTable(hTableDescriptor);
            }
        }
    }

    /**
     * 删除表
     *
     * @param tableName 表名
     *
     * @throws IOException
     */
    public static void dropTable(String tableName) throws IOException {

        try (Connection connection = ConnectionFactory.createConnection(configuration);
             Admin admin = connection.getAdmin()
        ) {

            TableName table = TableName.valueOf(tableName);
            if (admin.tableExists(table)) {
                admin.disableTable(table);
                admin.deleteTable(table);
            }
        }
    }

    /**
     * 插入数据
     *
     * @param tableName 表名
     * @param rowKey    行键
     * @param familys   列族信息(Key: 列族; value: (列名, 列值))
     */
    public static void putData(String tableName, String rowKey, Map<String, Map<String, String>> familys)
            throws IOException {

        try (Connection connection = ConnectionFactory.createConnection(configuration);
             Table table = connection.getTable(TableName.valueOf(tableName))
        ) {
            Put put = new Put(Bytes.toBytes(rowKey));

            for (Map.Entry<String, Map<String, String>> family : familys.entrySet()) {
                for (Map.Entry<String, String> column : family.getValue().entrySet()) {
                    put.addColumn(Bytes.toBytes(family.getKey()),
                            Bytes.toBytes(column.getKey()), Bytes.toBytes(column.getValue()));
                }
            }
            table.put(put);
        }
    }

    /**
     * 修改表结构,增加列族
     *
     * @param tableName 表名
     * @param family    列族
     *
     * @throws IOException
     */
    public static void putFamily(String tableName, String family) throws IOException {
        try (Connection connection = ConnectionFactory.createConnection(configuration);
             Admin admin = connection.getAdmin()
        ) {
            TableName tblName = TableName.valueOf(tableName);
            if (admin.tableExists(tblName)) {
                admin.disableTable(tblName);
                HColumnDescriptor cf = new HColumnDescriptor(family);
                admin.addColumn(TableName.valueOf(tableName), cf);
                admin.enableTable(tblName);
            } else {
                log.warn(tableName + " not exist.");
            }
        }
    }

    /**
     * 获取指定数据
     * <p>
     * columns为空, 检索指定列族的全部数据;
     * family为空时, 检索指定行键的全部数据;
     * </p>
     *
     * @param tableName 表名
     * @param rowKey    行键
     * @param family    列族
     * @param columns   列名集合
     *
     * @throws IOException
     */
    public static void getData(String tableName, String rowKey, String family, String[] columns)
            throws IOException {

        try (Connection connection = ConnectionFactory.createConnection(configuration);
             Table table = connection.getTable(TableName.valueOf(tableName))
        ) {
            Get get = new Get(Bytes.toBytes(rowKey));
            Result result = table.get(get);
            if (null != family && !"".equals(family)) {
                if (null != columns && columns.length > 0) { // 表里指定列族的列值
                    for (String column : columns) {
                        byte[] rb = result.getValue(Bytes.toBytes(family), Bytes.toBytes(column));
                        System.out.println(Bytes.toString(rb));
                    }
                } else { // 指定列族的所有值
                    Map<byte[], byte[]> columnMap = result.getFamilyMap(Bytes.toBytes(family));
                    for (Map.Entry<byte[], byte[]> entry : columnMap.entrySet()) {
                        System.out.println(Bytes.toString(entry.getKey())
                                + " "
                                + Bytes.toString(entry.getValue()));
                    }
                }
            } else { // 指定行键的所有值
                Cell[] cells = result.rawCells();
                for (Cell cell : cells) {
                    System.out.println("family => " + Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength()) + "\n"
                            + "qualifier => " + Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()) + "\n"
                            + "value => " + Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
                }
            }

        }
    }

    /**
     * 全表扫描
     *
     * @param tableName 表名
     *
     * @throws IOException
     */
    public static void scan(String tableName) throws IOException {
        try (Connection connection = ConnectionFactory.createConnection(configuration);
             Table table = connection.getTable(TableName.valueOf(tableName))
        ) {
            Scan scan = new Scan();
//            Filter filter = new Fi
//            scan.setFilter(filter);
            ResultScanner resultScanner = table.getScanner(scan);
            for (Result result : resultScanner) {
                List<Cell> cells = result.listCells();
                for (Cell cell : cells) {
                    System.out.println("row => " + Bytes.toString(CellUtil.cloneRow(cell)) + "\n"
                            + "family => " + Bytes.toString(CellUtil.cloneFamily(cell)) + "\n"
                            + "qualifier => " + Bytes.toString(CellUtil.cloneQualifier(cell)) + "\n"
                            + "value => " + Bytes.toString(CellUtil.cloneValue(cell)));
                }
            }
        }
    }

    /**
     * 删除指定数据
     * <p>
     * columns为空, 删除指定列族的全部数据;
     * family为空时, 删除指定行键的全部数据;
     * </p>
     *
     * @param tableName 表名
     * @param rowKey    行键
     * @param family    列族
     * @param columns   列集合
     *
     * @throws IOException
     */
    public static void deleteData(String tableName, String rowKey, String family, String[] columns)
            throws IOException {
        try (Connection connection = ConnectionFactory.createConnection(configuration);
             Table table = connection.getTable(TableName.valueOf(tableName))
        ) {
            Delete delete = new Delete(Bytes.toBytes(rowKey));

            if (null != family && !"".equals(family)) {
                if (null != columns && columns.length > 0) { // 删除指定列
                    for (String column : columns) {
                        delete.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
                    }
                } else { // 删除指定列族
                    delete.addFamily(Bytes.toBytes(family));
                }
            } else { // 删除指定行
                // empty, nothing to do
            }
            table.delete(delete);
        }
    }

    /**
     * 统计行数
     *
     * @param tableName 表名
     *
     * @return 行数
     *
     * @throws IOException
     */
    public static long count(String tableName) throws IOException {

        final long[] rowCount = {0};

        try (Connection connection = ConnectionFactory.createConnection(configuration);
             Table table = connection.getTable(TableName.valueOf(tableName))
        ) {
            Scan scan = new Scan();
            scan.setFilter(new FirstKeyOnlyFilter());
            ResultScanner resultScanner = table.getScanner(scan);
            resultScanner.forEach(result -> {
                rowCount[0] += result.size();
            });
        }
        System.out.println("行数: " + rowCount[0]);
        return rowCount[0];
    }

    /**
     * 计数器自增
     *
     * @param tableName 表名
     * @param rowKey    行键
     * @param family    列族
     * @param column    列
     * @param value     增量
     *
     * @throws IOException
     */
    public static void incr(String tableName, String rowKey, String family, String column,
                            long value) throws IOException {
        try (Connection connection = ConnectionFactory.createConnection(configuration);
             Table table = connection.getTable(TableName.valueOf(tableName))
        ) {
            long count = table.incrementColumnValue(Bytes.toBytes(rowKey), Bytes.toBytes(family),
                    Bytes.toBytes(column), value);
            System.out.println("增量后的值: " + count);
        }
    }

    public static void query() {
        Query query = new Query() {
            @Override
            public Map<String, Object> getFingerprint() {
                return null;
            }

            @Override
            public Map<String, Object> toMap(int maxCols) {
                return null;
            }
        };
    }

}
