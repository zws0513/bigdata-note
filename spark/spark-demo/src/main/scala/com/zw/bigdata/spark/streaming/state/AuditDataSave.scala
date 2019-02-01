package com.zw.bigdata.spark.streaming.state

import java.sql.{Connection, PreparedStatement, Timestamp}

import com.zw.bigdata.spark.util.DateUtil
import com.zw.pool.MySQLManager
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by zhangws on 2019/2/1.
  */
object AuditDataSave {

  protected lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  private val HIVE_SQL =
    """
      insert into t_hive_access_dw(cmd_type, ugi, db_name, tbl_name, load_time, day)
      values (?, ?, ?, ?, ?, ?)
    """

  private val IMPALA_SQL =
    """
      insert into t_impala_access_dw(cmd_type, db_name, tbl_name, start_load, end_load,
        load_dura, start_fetch, end_fetch, fetch_dura, day)
      values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

  /**
    * 保存统计信息
    *
    * @param seq 统计信息
    */
  def save(jdbc: String, user: String, password: String, seq: Seq[Map[String, String]]): Unit = {
    var conn: Connection = null
    var insertHive: PreparedStatement = null
    var insertImpala: PreparedStatement = null
    try {
      conn = MySQLManager.getMysqlManager(jdbc, user, password).getConnection
      conn.setAutoCommit(false)

      insertHive = conn.prepareStatement(HIVE_SQL)
      insertImpala = conn.prepareStatement(IMPALA_SQL)

      seq.foreach(map => {
        map.get(ManagerConst.K_TYPE) match {
          case Some(ManagerConst.HIVE) =>
            if (map.contains(ManagerConst.K_CMD_TYPE) && map.contains(ManagerConst.K_LOAD_TIME)) {
              insertHive.setInt(1, map(ManagerConst.K_CMD_TYPE).toInt) // cmd_type
              insertHive.setString(2, map.getOrElse(ManagerConst.K_UGI, null)) // ugi
              insertHive.setString(3, map.getOrElse(ManagerConst.K_DB_NAME, null)) // db_name
              insertHive.setString(4, map.getOrElse(ManagerConst.K_TABLE_NAME, null)) // tbl_name
              insertHive.setTimestamp(5, Timestamp.valueOf(map(ManagerConst.K_LOAD_TIME))) // load_time
              insertHive.setDate(6, new java.sql.Date(DateUtil.toYMD(map(ManagerConst.K_DAY), DateUtil.YYYY_MM_DD).getTime)) // day
              insertHive.addBatch()
            } else {
              logger.warn("when save Hive's data, cmd_type and load_time must be not empty.")
            }

            insertHive.executeBatch()
            conn.commit()

          case Some(ManagerConst.IMPALA) =>
            if (map.contains(ManagerConst.K_CMD_TYPE) && map.contains(ManagerConst.K_START_LOAD)) {
              insertImpala.setInt(1, map(ManagerConst.K_CMD_TYPE).toInt) // cmd_type
              insertImpala.setString(2, map.getOrElse(ManagerConst.K_DB_NAME, null)) // db_name
              insertImpala.setString(3, map.getOrElse(ManagerConst.K_TABLE_NAME, null)) // tbl_name
              insertImpala.setTimestamp(4, Timestamp.valueOf(map(ManagerConst.K_START_LOAD))) // start_load
              insertImpala.setTimestamp(5, getOrElse(map, ManagerConst.K_END_LOAD, Timestamp.valueOf, null)) // end_load
              insertImpala.setInt(6, map.getOrElse(ManagerConst.K_LOAD_DURA, "0").toInt) // load_dura
              insertImpala.setTimestamp(7, getOrElse(map, ManagerConst.K_START_FETCH, Timestamp.valueOf, null)) // start_fetch
              insertImpala.setTimestamp(8, getOrElse(map, ManagerConst.K_END_FETCH, Timestamp.valueOf, null)) // end_fetch
              insertImpala.setInt(9, map.getOrElse(ManagerConst.K_FETCH_DURA, "0").toInt) // fetch_dura
              insertImpala.setDate(10, new java.sql.Date(DateUtil.toYMD(map(ManagerConst.K_DAY), DateUtil.YYYY_MM_DD).getTime)) // day
              insertImpala.addBatch()
            } else {
              logger.warn("when save Impala's data, cmd_type and load_time must be not empty.")
            }
            insertImpala.executeBatch()
            conn.commit()

          case _ => // nothing to do
            logger.warn("thi type not support.")
        }
      })

      conn.setAutoCommit(true)
    } catch {
      case e: Exception => logger.error("AuditDataSave Mysql Exception " + e)
    } finally {
      if (insertHive != null) {
        insertHive.close()
      }
      if (conn != null) {
        conn.close()
      }
    }
  }

  /**
    * 获取Map中的指定Key的值
    *
    * @param map Map
    * @param key 指定Key
    * @param fun 对Value作转换
    * @param default 默认值
    * @tparam V 默认值和返回值的类型
    * @return
    */
  def getOrElse[V](map: Map[String, String],
                   key: String,
                   fun: (String) => V,
                   default: V
                  ): V = {
    if (map.contains(key)) {
      fun(map(key))
    } else {
      default
    }
  }
}
