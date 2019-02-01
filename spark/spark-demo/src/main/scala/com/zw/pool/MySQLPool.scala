package com.zw.pool

import java.sql.Connection

import com.mchange.v2.c3p0.ComboPooledDataSource

/**
  * Created by zhangws on 2019/2/1.
  */
class MySQLPool(jdbc: String, user: String, password: String) extends Serializable {
  private val cpds: ComboPooledDataSource = new ComboPooledDataSource(true)

  try {
    cpds.setJdbcUrl(jdbc)
    cpds.setDriverClass("com.mysql.jdbc.Driver")
    cpds.setUser(user)
    cpds.setPassword(password)
    cpds.setMaxPoolSize(100)
    cpds.setMinPoolSize(20)
    cpds.setAcquireIncrement(5)
    cpds.setMaxStatements(180)

    cpds.setAcquireRetryAttempts(5)
    cpds.setMaxIdleTime(500)

  } catch {
    case e: Exception => e.printStackTrace()
  }

  def getConnection: Connection = {
    try {
      cpds.getConnection()
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        null
    }
  }
}

object MySQLManager {
  var mysqlManager: MySQLPool = _

  def getMysqlManager(jdbc: String, user: String, password: String): MySQLPool = {
    if (mysqlManager == null) {
      synchronized {
        if (mysqlManager == null) {
          mysqlManager = new MySQLPool(jdbc, user, password)
        }
      }
    }
    mysqlManager
  }
}