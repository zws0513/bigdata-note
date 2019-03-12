#!/usr/bin/env python
# -*- coding: utf-8 -*-


import pymysql


CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "user",
    "passwd": "password",
    "db": "db_name",
    "charset": "utf8",
    "connect_timeout": 60,
    "ssl": False
}

INSERT_DW_TBL_BASE_SQL = """insert into t_dw_tbl_base_info(day, db_name, tbl_name, tbl_type, db_owner, tbl_owner, 
  location, part_cols, cols_num, tbl_create_time, load_part_num, disk_usage) 
             value (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
          """
INSERT_DW_TBL_PART_DETAIL_SQL = """insert into t_dw_tbl_part_detail(day, db_name, tbl_name, location, disk_usage) 
             value (%s, %s, %s, %s, %s)
          """


def delete_base(day):
    """删除t_dw_tbl_base_info表指定day的数据

    :param day: 日期
    :return:
    """

    connect = pymysql.connect(**CONFIG)
    connect.autocommit(False)
    try:
        with connect.cursor() as cursor:
            cursor.execute("delete from t_dw_tbl_base_info where day = %s", day)
            connect.commit()
    except pymysql.Error, e:
        print e.message
        connect.rollback()
    finally:
        connect.close()


def delete_detail(day):
    """删除t_dw_tbl_part_detail表指定day的数据

    :param day: 日期
    :return:
    """

    connect = pymysql.connect(**CONFIG)
    connect.autocommit(False)
    try:
        with connect.cursor() as cursor:
            cursor.execute("delete from t_dw_tbl_part_detail where day = %s", day)
            connect.commit()
    except pymysql.Error, e:
        print e.message
        connect.rollback()
    finally:
        connect.close()


def batch_insert_base(lt):
    """批量插入数据到t_dw_tbl_base_info

    :param lt: 待插入的数据
    :return:
    """

    connect = pymysql.connect(**CONFIG)
    connect.autocommit(False)
    try:
        with connect.cursor() as cursor:
            cursor.executemany(INSERT_DW_TBL_BASE_SQL, lt)
            connect.commit()
    except pymysql.Error, e:
        print e.message
        connect.rollback()
    finally:
        connect.close()


def batch_insert_detail(lt):
    """批量插入数据到t_dw_tbl_part_detail

    :param lt: 待插入的数据
    :return:
    """

    connect = pymysql.connect(**CONFIG)
    connect.autocommit(False)
    try:
        with connect.cursor() as cursor:
            cursor.executemany(INSERT_DW_TBL_PART_DETAIL_SQL, lt)
            connect.commit()
    except pymysql.Error, e:
        print e.message
        connect.rollback()
    finally:
        connect.close()
