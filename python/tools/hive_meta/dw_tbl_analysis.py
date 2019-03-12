#!/usr/bin/env python
# -*- coding: utf-8 -*-


import sys
import subprocess
import datetime
import mysql_dw_tbl


MYSQL_CMD = "mysql -A hive_meta_db -h 10.10.xxx.xx -P3306 -uuser_id -ppassword -e \"%s; \" > %s "

# table基本信息
SQL_DW_BASE = """
SELECT 
  T2.NAME AS DB_NAME,
  T1.TBL_NAME,
  T1.TBL_TYPE,
  T2.OWNER_NAME AS DB_OWNER,
  T1.OWNER AS TBL_OWNER,
  T3.LOCATION,
  T4.PART_COLS,
  T5.COLS_NUM,
  FROM_UNIXTIME(T1.CREATE_TIME, '%Y-%m-%d') AS CREATE_TIME
FROM TBLS T1
LEFT JOIN DBS T2
  ON T1.DB_ID = T2.DB_ID
LEFT JOIN SDS T3
  ON T1.SD_ID = T3.SD_ID
LEFT JOIN (
  SELECT 
    TBL_ID,
    GROUP_CONCAT(PKEY_NAME ORDER BY INTEGER_IDX) AS PART_COLS
  FROM PARTITION_KEYS
  GROUP BY 
    TBL_ID
) T4
  ON T1.TBL_ID = T4.TBL_ID
LEFT JOIN (
  SELECT 
    CD_ID,
    COUNT(1) AS COLS_NUM
  FROM COLUMNS_V2
  GROUP BY 
    CD_ID
) T5
  ON T3.CD_ID = T5.CD_ID
"""

# table存储信息
SQL_PART_DETAIL = """
SELECT
  T2.NAME AS DB_NAME,
  T1.TBL_NAME,
  T3.LOCATION
FROM (
  SELECT
    T11.DB_ID,
    T11.TBL_ID,
    T11.TBL_NAME,
    T12.SD_ID
  FROM TBLS T11
  JOIN PARTITIONS T12
    ON T11.TBL_ID = T12.TBL_ID
) T1
LEFT JOIN DBS T2
  ON T1.DB_ID = T2.DB_ID
LEFT JOIN SDS T3
  ON T1.SD_ID = T3.SD_ID
"""


def out_log(msg):
    now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print("%s %s" % (now, msg))


def parse_base(file_path):
    """解析基础数据，生成字典实例（key：数据库.表名；value：基础数据内容）
    value数据项：DB_NAME, TBL_NAME, TBL_TYPE, DB_OWNER, TBL_OWNER, LOCATION, PART_COLS, COLS_NUM, CREATE_TIME

    :param file_path: 基础数据文件绝对路径
    :return: 字典实例
    """

    out_log("start parse the base data.")

    base_dict = {}

    with open(file_path) as f:
        row = 0
        for line in f.readlines():
            line = line.strip('\n')
            row += 1
            if row == 1 or len(line) <= 0:
                continue
            arr = line.split("\t")
            tbl = ".".join(arr[0: 2])
            base_dict[tbl] = arr

    return base_dict


def query():
    """

    :return:
    """
    out_log("start query the hive meta data.")

    cur_dir = sys.path[0]

    detail_path = cur_dir + "/dw_tbl_part_detail.csv"
    detail_cmd = MYSQL_CMD % (SQL_PART_DETAIL, detail_path)
    if subprocess.call(detail_cmd, shell=True) != 0:
        out_log("dw_tbl_part_detail fail.")
        return None

    base_path = cur_dir + "/dw_tbl_base.csv"
    base_cmd = MYSQL_CMD % (SQL_DW_BASE, base_path)
    if subprocess.call(base_cmd, shell=True) != 0:
        out_log("dw_tbl_base fail.")
        return None
    return base_path, detail_path


def scheduler(analyze_usage=False, parallel=4, fixed_tbls=None):
    """分析Hive表信息的调度程序

    1. 通过SQL直接查询Hive元数据（MySQL），提取表的基本信息和加载的分区信息；
    2. 对分区计算其所占用的磁盘容量，同时"按数据库.表"为Key，统计每个表的加载分区数和总磁盘占有；（这块多进程处理）
    3. 最后数据存入监控数据库

    :param analyze_usage: 是否分析分区的磁盘占有
    :param parallel: 并行度
    :param fixed_tbls: 指定表列表（以逗号分隔，例如：db1.tbl1,db2.tbl2）
    :return:
    """
    start_time = datetime.datetime.now()

    out_log("start script. analyze_usage = %s, parallel = %s, fixed_tbls = %s." % (analyze_usage, parallel, fixed_tbls))

    query_result = query()
    if query_result is not None:
        day = datetime.date.today().strftime("%Y-%m-%d")

        from HdfsUsage import HdfsUsage as Usage

        out_log("start usage analysis.")
        usage = Usage(day=day,
                      file_paths=query_result,
                      analyze_usage=analyze_usage,
                      parallel=parallel,
                      fixed_tbls=fixed_tbls)
        usage_datas = usage.run()

        end_time = datetime.datetime.now()
        out_log("end usage analysis, elapsed time: %s sec." % (end_time - start_time).seconds)

        # 保存基础数据
        out_log("start base data save. cnt = %s" % len(usage_datas[0]))
        mysql_dw_tbl.delete_base(day)
        mysql_dw_tbl.batch_insert_base(usage_datas[0])
        out_log("end base data save.")

        # 保存明细数据
        out_log("start detail data save. cnt = %s" % len(usage_datas[1]))
        mysql_dw_tbl.delete_detail(day)
        mysql_dw_tbl.batch_insert_detail(usage_datas[1])
        out_log("end detail data save.")
    else:
        out_log("data empty.")

    end_time = datetime.datetime.now()
    out_log("end script, elapsed time: %s sec." % (end_time - start_time).seconds)


if __name__ == "__main__":

    analyze = False
    if len(sys.argv) > 1:
        analyze = bool(sys.argv[1])

    par = 1
    if len(sys.argv) > 2:
        par = int(sys.argv[2])

    tbls = None
    if len(sys.argv) > 3:
        tbls = str(sys.argv[3])

    scheduler(analyze, par, tbls)
