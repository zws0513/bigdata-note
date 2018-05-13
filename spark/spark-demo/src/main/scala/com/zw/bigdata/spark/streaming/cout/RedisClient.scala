package com.zw.bigdata.spark.streaming.cout

import redis.clients.jedis.{JedisPool, JedisPoolConfig}

/**
  * Created by zhangws on 16/11/13.
  */
object RedisClient extends Serializable {

  val REDIS_SERVER: String = "hss01"
  val REDIS_PORT: Int = 6379

  private var MAX_IDLE: Int = 200
  private var TIMEOUT: Int = 200
  private var TEST_ON_BORROW: Boolean = true

  lazy val config: JedisPoolConfig = {
    val config = new JedisPoolConfig
    config.setMaxIdle(MAX_IDLE)
    config.setTestOnBorrow(TEST_ON_BORROW)
    config
  }

  lazy val pool = new JedisPool(config, REDIS_SERVER,
    REDIS_PORT, TIMEOUT)

  lazy val hook = new Thread {
    override def run = {
      println("Execute hook thread: " + this)
      pool.destroy()
    }
  }
  sys.addShutdownHook(hook.run)
}
