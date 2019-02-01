package com.zw.bigdata.spark.streaming.state

import org.apache.spark.streaming.State

/**
  * Created by zhangws on 2019/2/1.
  */
trait Audit[K, V] {

  /**
    * 解析Hive日志
    *
    * @param message 日志内容
    * @return 元组（有效性标识, 数据内容Map）
    */
  def parser(message: V): (K, Map[K, V])

  /**
    * 状态处理
    * @param state 状态缓存
    * @param newEvent 新数据
    * @return 输出值
    */
  def state(state: State[Map[K, V]], newEvent: Map[K, V]): Map[K, V]

  /**
    * 状态处理，超时时的输出
    */
  def timeout()
}
