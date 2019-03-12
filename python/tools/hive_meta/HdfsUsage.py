#!/usr/bin/env python
# -*- coding: utf-8 -*-
import datetime
import os
import multiprocessing
import types
import copy_reg

HDFS_USAGE_CMD = "su - hdfs -c 'hdfs dfs -du -h \"%s\" '"
# HDFS_USAGE_CMD = "hdfs dfs -du -h %s"


def _pickle_method(m):
    if m.im_self is None:
        return getattr, (m.im_class, m.im_func.func_name)
    else:
        return getattr, (m.im_self, m.im_func.func_name)


copy_reg.pickle(types.MethodType, _pickle_method)


class HdfsUsage(object):

    @staticmethod
    def out_log(msg):
        now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        print("%s %s" % (now, msg))

    def __init__(self, day, file_paths, analyze_usage=True, parallel=1, fixed_tbls=None):
        """

        :param day: 日期
        :param file_paths: 文件路径
        :param analyze_usage: 是否分析HDFS磁盘占有
        :param parallel: 并行度（进程数）
        :param fixed_tbls: 只计算指定表（以逗号分隔，格式：db1.table1,db2.table2）
        """
        self.day = day
        self.file_paths = file_paths
        self.parallel = parallel
        self.analyze_usage = analyze_usage
        self.fixed_tbls = fixed_tbls

    def run(self):
        """把文件按行等分为parallel份，并行启动parallel个进程，同时统计数据。
           最后合并数据，并返回

        :return:
        """
        detail_part_array = []
        total_part_dict = {}

        detail_pool = multiprocessing.Pool(processes=self.parallel)
        detail_results = []

        start_time = datetime.datetime.now()
        self.out_log("start analysis the detail data.")

        part_dict = self.file_split(self.file_paths[1], self.parallel)

        tbls = None if self.fixed_tbls is None else str(self.fixed_tbls).split(",")

        for value in part_dict.values():
            detail_results.append(detail_pool.apply_async(self.analysis_detail, (value,
                                                                                 self.analyze_usage,
                                                                                 tbls,
                                                                                 self.day, )))

        detail_pool.close()
        detail_pool.join()

        count = 0
        for res in detail_results:
            total = res.get()[0]

            for key in total.keys():
                count += 1
                tbl_usage = total_part_dict.get(key, (0, -0.0))
                usage = total.get(key)
                total_part_dict[key] = (tbl_usage[0] + usage[0],
                                        tbl_usage[1] + (-0.0 if usage[1] is None else usage[1]))
                self.out_log("part_detail to reduce on %s." % count)

            detail_part_array = detail_part_array + res.get()[1]

        end_time = datetime.datetime.now()
        self.out_log("end analysis the detail data, elapsed time: %s sec." % (end_time - start_time).seconds)

        start_time = datetime.datetime.now()
        self.out_log("start analysis the base data.")
        base_pool = multiprocessing.Pool(processes=self.parallel)
        base_results = []

        base_array = []

        base_dict = self.file_split(self.file_paths[0], self.parallel)

        for value in base_dict.values():
            base_results.append(base_pool.apply_async(self.analysis_base, (value, total_part_dict, self.day, )))

        base_pool.close()
        base_pool.join()

        for res in base_results:
            base_array = base_array + res.get()

        end_time = datetime.datetime.now()
        self.out_log("end analysis the base data, elapsed time: %s sec." % (end_time - start_time).seconds)

        return base_array, detail_part_array

    def file_split(self, path, parallel):
        """按并行度，切割文件内容到字典中

        :param path:
        :param parallel:
        :return: key: 文件段；value：文件内容（字符串）
        """
        file_dict = {}

        with open(path) as f:
            row = 0
            for line in f.readlines():
                line = line.strip('\n')
                row += 1
                if row == 1 or len(line) <= 0:
                    continue
                key = row % parallel
                value = file_dict[key] if key in file_dict else []
                value.append(line)
                file_dict[key] = value

        self.out_log("file to split for %s" % parallel)

        return file_dict

    def analysis_base(self, base_datas, total_part_dict, day):
        """对数据库的表，计算总目大小

        :param base_datas: 数据集
        :param total_part_dict: 合计的分区数据
        :param day: 日期
        :return: base_data_array（(库名, 表名, 分区目录, 分区目录磁盘大小)）
        """
        base_data = []

        pid = os.getpid()
        total = len(base_datas)
        cnt = 0

        for line in base_datas:
            cnt += 1
            value = line.split("\t")
            key = ".".join(value[0: 2])
            tbl_type = 3
            load_part_num = 0
            disk_usage = -0.0
            if value[2] == "MANAGED_TABLE":
                tbl_type = 0
                if key in total_part_dict:
                    load_part_num = total_part_dict[key][0]
                disk_usage = self.hdfs_usage(value[5])
            elif value[2] == "EXTERNAL_TABLE":
                tbl_type = 1
                if key in total_part_dict:
                    load_part_num = total_part_dict[key][0]
                    disk_usage = total_part_dict[key][1]
                else:
                    disk_usage = self.hdfs_usage(value[5])
            elif value[2] == "VIRTUAL_VIEW":
                tbl_type = 2

            # value: DB_NAME, TBL_NAME, TBL_TYPE, DB_OWNER, TBL_OWNER, LOCATION, PART_COLS, COLS_NUM, CREATE_TIME
            # save: day, db_name, tbl_name, tbl_type, db_owner, tbl_owner, location, part_cols, cols_num,
            #       create_time, load_part_num, disk_usage
            base_data.append((day, value[0], value[1], tbl_type, value[3], value[4], value[5], value[6],
                              value[7], value[8], load_part_num,
                              round(-0.0 if disk_usage is None or disk_usage < 0.0 else disk_usage, 3)))
            self.out_log("base_data's parallel processing. pid = %s, total = %s, cur = %s" % (pid, total, cnt))

        return base_data

    def analysis_detail(self, datas, analyze_usage, fixed_tbls, day):
        """对数据库的表，计算分区目录大小，统计计算表合计分区的大小

        :param datas: 数据集
        :param analyze_usage 是否分析HDFS磁盘占有
        :param fixed_tbls 指定表（数组）
        :param day 日期
        :return: total_part_dict（key：表名；value：(分区数, 总磁盘大小)）
                 detail_part_array（(库名, 表名, 分区目录, 分区目录磁盘大小)）
        """
        detail_part_array = []
        total_part_dict = {}

        pid = os.getpid()
        total = len(datas)
        cnt = 0

        for line in datas:
            cnt += 1
            arr = line.split("\t")
            tbl = ".".join(arr[0: 2])
            usage = self.hdfs_usage(arr[2]) if analyze_usage and (fixed_tbls is None or tbl in fixed_tbls) else -1.0
            tbl_usage = total_part_dict.get(tbl, (0, -0.0))
            total_part_dict[tbl] = (tbl_usage[0] + 1, tbl_usage[1] + (-0.0 if usage is None else usage))

            # value: 库名, 表名, 分区目录, 分区目录磁盘大小
            detail_part_array.append((day, arr[0], arr[1], arr[2], round(-0.0 if usage is None else usage, 3)))
            self.out_log("part_detail's parallel processing. pid = %s, total = %s, cur = %s" % (pid, total, cnt))

        return total_part_dict, detail_part_array

    @staticmethod
    def hdfs_usage(path):
        """获取磁盘占用，单位：KB
        如果目录不存在，则为 -0.0

        :param path: HDFS目录
        :return: 占有量，单位：KB
        """
        hdfs_cmd = HDFS_USAGE_CMD % path
        try:
            total = -0.0
            for d in os.popen(hdfs_cmd).readlines():
                array = d.replace("\n", "").split(" ")
                if array[0].replace(".", "").isdigit():
                    if array[1] == "T":
                        total += float(array[0]) * 1024 * 1024 * 1024
                    elif array[1] == "G":
                        total += float(array[0]) * 1024 * 1024
                    elif array[1] == "M":
                        total += float(array[0]) * 1024
                    elif array[1] == "K":
                        total += float(array[0])
                    else:
                        total += round(float(array[0]) / 1024, 3)
                else:
                    break
            return total
        except Exception, e:
            print(e.message)
            return -0.0
