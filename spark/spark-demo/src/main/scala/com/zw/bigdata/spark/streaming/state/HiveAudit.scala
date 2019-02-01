package com.zw.bigdata.spark.streaming.state

import com.zw.bigdata.spark.util.DateUtil
import org.apache.spark.streaming.State

import scala.collection.mutable

/**
  * Created by zhangws on 2019/2/1.
  */
object HiveAudit extends Audit[String, String] {

  /**
    * 解析Hive日志
    *
    * @param message 日志内容
    * @return 元组（有效性标识, 数据内容Map）
    */
  override def parser(message: String): (String, Map[String, String]) = {
    val suffix = new mutable.StringBuilder()
    val data = new mutable.HashMap[String, String]
    var pos = message.indexOf("HiveMetaStore.audit:")
    if (pos > 0) {
      if (message.contains("get_all_databases")) {
        val array = message.split(ManagerConst.S_SPACE)
        val time = array(0) + ManagerConst.S_SPACE + array(1).replace(ManagerConst.R_COMMA, ManagerConst.R_DOT)

        data += ((ManagerConst.K_CMD_TYPE, ManagerConst.V_0))
        data += ((ManagerConst.K_LOAD_TIME, time))
        data += ((ManagerConst.K_DAY, array(0)))
      } else if (message.indexOf("get_table : db=", pos) > 0 && message.indexOf("db=cloudera", pos) < 0) {
        val array = message.replace(ManagerConst.R_TAB, ManagerConst.S_SPACE).split(ManagerConst.S_SPACE)
        val time = array(0) + ManagerConst.S_SPACE + array(1).replace(ManagerConst.R_COMMA, ManagerConst.R_DOT)

        data += ((ManagerConst.K_CMD_TYPE, ManagerConst.V_1))
        for (t <- array) {
          val temp = t.split(ManagerConst.S_EQUAL)
          if (temp(0) == ManagerConst.K_UGI) data += ((ManagerConst.K_UGI, temp(1)))
          if (temp(0) == ManagerConst.K_DB) data += ((ManagerConst.K_DB_NAME, temp(1)))
          if (temp(0) == ManagerConst.K_TBL) data += ((ManagerConst.K_TABLE_NAME, temp(1)))
        }
        suffix.append(data(ManagerConst.K_DB_NAME))
          .append(ManagerConst.R_DOT)
          .append(data(ManagerConst.K_TABLE_NAME))

        data += ((ManagerConst.K_LOAD_TIME, time))
        data += ((ManagerConst.K_DAY, array(0)))
      }
    }

    pos = message.indexOf("HiveMetaStore:")
    if (pos > 0) {
      if (message.indexOf("Not adding partition", pos) > 0) {
        val array = message.replace(ManagerConst.R_TAB, ManagerConst.S_SPACE).split(ManagerConst.S_SPACE)
        val time = array(0) + ManagerConst.S_SPACE + array(1).replace(ManagerConst.R_COMMA, ManagerConst.R_DOT)

        data += ((ManagerConst.K_CMD_TYPE, ManagerConst.V_2))
        for (t <- array) {
          val temp = t.split(ManagerConst.S_COLON)
          if (temp(0) == ManagerConst.K_DB2) {
            data += ((ManagerConst.K_DB_NAME, temp(1).replace(ManagerConst.R_COMMA, ManagerConst.S_EMPTY)))
          }
          if (temp(0) == ManagerConst.K_TBL2) {
            data += ((ManagerConst.K_TABLE_NAME, temp(1).replace(ManagerConst.R_COMMA, ManagerConst.S_EMPTY)))
          }
        }
        suffix.append(data(ManagerConst.K_DB_NAME))
          .append(ManagerConst.R_DOT)
          .append(data(ManagerConst.K_TABLE_NAME))

        data += ((ManagerConst.K_LOAD_TIME, time))
        data += ((ManagerConst.K_DAY, array(0)))
      }
    }

    if (data.isEmpty) {
      (ManagerConst.GARBAGE, null)
    } else {
      data += ((ManagerConst.K_TYPE, ManagerConst.HIVE))
      (ManagerConst.HIVE + " " + suffix.toString(), data.toMap)
    }
  }

  /**
    * 状态处理
    * @param state 状态缓存
    * @param newEvent 新数据
    * @return 输出值
    */
  override def state(state: State[Map[String, String]], newEvent: Map[String, String]): Map[String, String] = {
    val result = new mutable.HashMap[String, String]()

    state.getOption match {
      case Some(cache) =>
        newEvent.get(ManagerConst.K_CMD_TYPE) match {
          case Some(ManagerConst.V_0) =>
            for (v <- newEvent) result += v
          case Some(ManagerConst.V_1) =>
            val duration = DateUtil.diffMs(newEvent(ManagerConst.K_LOAD_TIME), cache(ManagerConst.K_LOAD_TIME))
            if (duration > 1000) {
              for (v <- cache) result += v
              state.update(newEvent)
            }
          case Some(ManagerConst.V_2) =>
            val duration = DateUtil.diffMs(newEvent(ManagerConst.K_LOAD_TIME), cache(ManagerConst.K_LOAD_TIME))
            if (duration > 1000) {
              for (v <- cache) result += v
              if (state.exists()) {
                state.remove()
              }
            } else {
              if (state.exists()) {
                state.remove()
              }
            }

          case _ => // nothing to do
        }

      case None => // 没有缓存
        newEvent.get(ManagerConst.K_CMD_TYPE) match {
          case Some(ManagerConst.V_0) =>
            for (v <- newEvent) result += v
          case Some(ManagerConst.V_1) =>
            state.update(newEvent)
          case _ => // nothing to do
        }
    }
    result.toMap
  }

  /**
    * 状态处理，超时时的输出
    */
  override def timeout(): Unit = {

  }
}
