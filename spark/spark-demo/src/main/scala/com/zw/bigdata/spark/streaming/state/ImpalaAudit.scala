package com.zw.bigdata.spark.streaming.state

import com.zw.bigdata.spark.util.DateUtil
import org.apache.spark.streaming.State

import scala.collection.mutable

/**
  * Created by zhangws on 2019/2/1.
  */
object ImpalaAudit extends Audit[String, String]{

  /**
    * 计算具体时间
    *
    * @param day  日期（格式：mmdd）
    * @param time 时间
    * @return 时间
    */
  private def date(day: String, time: String): String = {
    val now = DateUtil.nowDay
    val year = DateUtil.nowYear()

    var logDay = year + day.substring(day.length - 4)
    if (DateUtil.diff(now, logDay, DateUtil.YYYYMMDD) < 0) {
      logDay = year.toInt - 1 + day.substring(day.length - 4)
    }
    DateUtil.toYMDStr(logDay) + ManagerConst.S_SPACE + time
  }

  /**
    * 解析Hive日志
    *
    * @param message 日志内容
    * @return 元组（有效性标识, 数据内容Map）
    */
  override def parser(message: String): (String, Map[String, String]) = {
    val data = new mutable.HashMap[String, String]
    if (message.contains("Invalidating all metadata")) {
      val array = message.split(ManagerConst.S_SPACE)
      val time = date(array(0), array(1))

      data += ((ManagerConst.K_ID, if (array(2).isEmpty) array(3) else array(2)))
      data += ((ManagerConst.K_CMD_TYPE, ManagerConst.V_0))
      data += ((ManagerConst.K_START_LOAD, time))
      data += ((ManagerConst.K_DAY, time.substring(0, 10)))
    } else if (message.contains("Invalidated all metadata")) {
      val array = message.split(ManagerConst.S_SPACE)

      data += ((ManagerConst.K_ID, if (array(2).isEmpty) array(3) else array(2)))
      data += ((ManagerConst.K_CMD_TYPE, ManagerConst.V_0))
      data += ((ManagerConst.K_END_LOAD, date(array(0), array(1))))
    } else if (message.contains("Loading metadata for:")) { // start load table
      val array = message.split(ManagerConst.S_SPACE)
      val time = date(array(0), array(1))
      val db = array(array.length - 1).split(ManagerConst.S_DOT)

      data += ((ManagerConst.K_ID, if (array(2).isEmpty) array(3) else array(2)))
      data += ((ManagerConst.K_CMD_TYPE, ManagerConst.V_1))
      data += ((ManagerConst.K_DB_NAME, db(0)))
      data += ((ManagerConst.K_TABLE_NAME, db(1)))
      data += ((ManagerConst.K_START_LOAD, time))
      data += ((ManagerConst.K_DAY, time.substring(0, 10)))
    } else if (message.contains("Fetching partition metadata from the Metastore:")) {
      val array = message.split(ManagerConst.S_SPACE)
      val time = date(array(0), array(1))
      val db = array(array.length - 1).split(ManagerConst.S_DOT)

      data += ((ManagerConst.K_ID, if (array(2).isEmpty) array(3) else array(2)))
      data += ((ManagerConst.K_CMD_TYPE, ManagerConst.V_1))
      data += ((ManagerConst.K_DB_NAME, db(0)))
      data += ((ManagerConst.K_TABLE_NAME, db(1)))
      data += ((ManagerConst.K_START_FETCH, time))
    } else if (message.contains("Fetched partition metadata from the Metastore:")) {
      val array = message.split(ManagerConst.S_SPACE)
      val time = date(array(0), array(1))
      val db = array(array.length - 1).split(ManagerConst.S_DOT)

      data += ((ManagerConst.K_ID, if (array(2).isEmpty) array(3) else array(2)))
      data += ((ManagerConst.K_CMD_TYPE, ManagerConst.V_1))
      data += ((ManagerConst.K_DB_NAME, db(0)))
      data += ((ManagerConst.K_TABLE_NAME, db(1)))
      data += ((ManagerConst.K_END_FETCH, time))
    } else if (message.contains("Loaded metadata for:")) {
      val array = message.split(ManagerConst.S_SPACE)
      val time = date(array(0), array(1))
      val db = array(array.length - 1).split(ManagerConst.S_DOT)

      data += ((ManagerConst.K_ID, if (array(2).isEmpty) array(3) else array(2)))
      data += ((ManagerConst.K_CMD_TYPE, ManagerConst.V_1))
      data += ((ManagerConst.K_DB_NAME, db(0)))
      data += ((ManagerConst.K_TABLE_NAME, db(1)))
      data += ((ManagerConst.K_END_LOAD, time))
    }

    if (data.isEmpty) {
      (ManagerConst.GARBAGE, null)
    } else {
      data += ((ManagerConst.K_TYPE, ManagerConst.IMPALA))
      (ManagerConst.IMPALA + " " + data(ManagerConst.K_ID), data.toMap)
    }
  }

  /**
    * 状态处理
    *
    * @param state    状态缓存
    * @param newEvent 新数据
    * @return 输出值
    */
  override def state(state: State[Map[String, String]], newEvent: Map[String, String]): Map[String, String] = {
    val result = new mutable.HashMap[String, String]()

    state.getOption match {
      case Some(cache) =>
        if (newEvent.contains(ManagerConst.K_START_LOAD)) {
          for (v <- cache) result += v
          state.update(newEvent)

        } else if (newEvent.contains(ManagerConst.K_END_LOAD)) {

          for (v <- cache) result += v

          val startLoad = cache.getOrElse(ManagerConst.K_START_LOAD, null)
          val endLoad = newEvent.getOrElse(ManagerConst.K_END_LOAD, null)

          result += ((ManagerConst.K_END_LOAD, endLoad))
          result += ((ManagerConst.K_LOAD_DURA, DateUtil.diffMs(endLoad, startLoad).toString))

          if (state.exists()) {
            state.remove()
          }

        } else if (newEvent.contains(ManagerConst.K_START_FETCH)) {
          state.update(cache.updated(ManagerConst.K_START_FETCH, newEvent(ManagerConst.K_START_FETCH)))

        } else if (newEvent.contains(ManagerConst.K_END_FETCH)) {
          val newCache = new mutable.HashMap[String, String]()
          for (v <- cache) newCache += v

          val startFetch = cache.getOrElse(ManagerConst.K_START_FETCH, null)
          val endFetch = newEvent.getOrElse(ManagerConst.K_END_FETCH, null)

          newCache += ((ManagerConst.K_END_FETCH, endFetch))
          newCache += ((ManagerConst.K_FETCH_DURA, DateUtil.diffMs(endFetch, startFetch).toString))

          state.update(newCache.toMap)
        }
      case None => // 没有缓存
        if (!newEvent.contains(ManagerConst.K_END_LOAD)) {
          state.update(newEvent)
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
