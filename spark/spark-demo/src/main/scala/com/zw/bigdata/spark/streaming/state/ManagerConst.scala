package com.zw.bigdata.spark.streaming.state

/**
  * Created by zhangws on 2019/2/1.
  */
object ManagerConst {

  val GARBAGE = "-2"
  val NOT_SUPPORT = "-1"
  val IMPALA = "1"
  val HIVE = "2"

  val K_TYPE = "k_type"
  val K_ID = "k_id"
  val K_CMD_TYPE = "cmd_type"
  val K_DB_NAME = "db_name"
  val K_TABLE_NAME = "table_name"
  val K_START_LOAD = "start_load"
  val K_END_LOAD = "end_load"
  val K_LOAD_DURA = "load_dura"
  val K_START_FETCH = "start_fetch"
  val K_END_FETCH = "end_fetch"
  val K_FETCH_DURA = "fetch_dura"
  val K_DAY = "day"

  val K_LOAD_TIME = "load_time"
  val K_UGI = "ugi"
  val K_DB = "db"
  val K_DB2 = "dbName"
  val K_TBL = "tbl"
  val K_TBL2 = "tableName"

  val V_0 = "0"
  val V_1 = "1"
  val V_2 = "2"

  val S_SPACE = " "
  val S_EMPTY = ""
  val S_DOT = "\\."
  val S_EQUAL = "="
  val S_COLON = "\\:"

  val R_TAB = "\t"
  val R_COMMA = ","
  val R_DOT = "."
}
