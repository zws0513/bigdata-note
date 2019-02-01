package com.zw.bigdata.spark.util

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.{Calendar, Date, Locale}

import scala.collection.mutable.ListBuffer

/**
  * Created by zhangws on 2019/2/1.
  */
object DateUtil {

  val YYYY = "yyyy"
  val YYYYMM = "yyyyMM"

  val YYYYMMDD = "yyyyMMdd"
  val MMDD = "MMdd"
  val MM_DD = "MM-dd"

  val YYYY_MM_DD_HH_MI_SS = "yyyy-MM-dd HH:mm:ss"
  val YYYY_MM_DD_HH_MI_SS_MS = "yyyy-MM-dd HH:mm:ss.SSSSSS"

  val YYYYMMDDHHMISS = "yyyyMMddHHmmss"

  val YYYY_MM_DD = "yyyy-MM-dd"

  val YYYY_MM = "yyyy-MM"

  val DD = "dd"

  val HH = "HH"

  val HH_MM_SS = "HH:mm:ss"

  val DD_MMM_YYYY_HHMMSS = "dd/MMM/yyyy:HH:mm:ss"

  /**
    * 变换日期字符串的格式
    *
    * @param date      日期字符串
    * @param srcFormat 元格式
    * @param dstFormat 目标格式
    * @param locale    locale
    * @return 变换后的日期字符串
    */
  def toDate(date: String,
             srcFormat: String = DD_MMM_YYYY_HHMMSS,
             dstFormat: String = YYYYMMDDHHMISS,
             locale: Locale = Locale.ENGLISH): String = {
    new SimpleDateFormat(dstFormat).format(new SimpleDateFormat(srcFormat, locale).parse(date))
  }

  /**
    * 变换日期字符串的格式
    *
    * @param date      日期字符串
    * @param srcFormat 元格式
    * @param dstFormat 目标格式
    * @return 变换后的日期字符串
    */
  def month(date: String,
            srcFormat: String = YYYYMMDD,
            dstFormat: String = YYYYMM): String = {
    toDate(date, srcFormat, dstFormat)
  }

  /**
    * 字符串转日期
    *
    * @param date      字符串日期
    * @param srcFormat 原格式
    * @return 日期
    */
  def toYMD(date: String,
            srcFormat: String = YYYYMMDD): Date = {
    new SimpleDateFormat(srcFormat).parse(date)
  }

  /**
    * 日期字符串转年字符串
    *
    * @param date      字符串日期
    * @param srcFormat 原格式
    * @return 日期
    */
  def year(date: String, srcFormat: String = YYYYMMDD): String = {
    toDate(date, srcFormat, YYYY)
  }

  /**
    * 日期字符串转月日字符串
    *
    * @param date      字符串日期
    * @param srcFormat 原格式
    * @return 日期
    */
  def monthAndDay(date: String, srcFormat: String = YYYYMMDD): String = {
    toDate(date, srcFormat, MM_DD)
  }

  /**
    * 当前年
    *
    * @return 年
    */
  def nowYear(): String = {
    now(YYYY)
  }

  /**
    * 字符串转日期
    *
    * @param date      字符串日期
    * @param srcFormat 原格式
    * @param dstFormat 目标格式
    * @return 日期字符串
    */
  def toYMDStr(date: String,
               srcFormat: String = YYYYMMDD,
               dstFormat: String = YYYY_MM_DD): String = {
    new SimpleDateFormat(dstFormat).format(new SimpleDateFormat(srcFormat).parse(date))
  }

  /**
    * 字符串转日期
    *
    * @param date      字符串日期
    * @param dstFormat 目标格式
    * @return 日期字符串
    */
  def toYMDStr2(date: java.sql.Date,
                dstFormat: String = YYYY_MM_DD): String = {
    new SimpleDateFormat(dstFormat).format(date)
  }

  /**
    * 字符串转日期
    *
    * @param date      字符串日期
    * @param srcFormat 原格式
    * @param dstFormat 目标格式
    * @return 日期字符串
    */
  def toStr(date: String,
            srcFormat: String = YYYYMMDD,
            dstFormat: String = YYYY_MM_DD): String = {
    new SimpleDateFormat(dstFormat).format(new SimpleDateFormat(srcFormat).parse(date))
  }

  /**
    * 时间戳转日期
    *
    * @param time      时间戳（单位毫秒）
    * @param dstFormat 目标格式
    * @return 日期字符串
    */
  def time2Str(time: Long,
               dstFormat: String = YYYY_MM_DD_HH_MI_SS): String = {
    new SimpleDateFormat(dstFormat).format(new Date(time))
  }

  /**
    * 获取当前时间
    *
    * @param format 时间字符串的格式
    * @return 格式化后的当前时间
    */
  def now(format: String): String = {
    val sdf = new SimpleDateFormat(format)
    sdf.format(new Date)
  }

  /**
    * 获取当前时间
    * <p>
    * 默认格式为: yyyyMMddHHmmss
    * </p>
    *
    * @return 格式化后的当前时间
    */
  def now(): String = now(YYYYMMDDHHMISS)

  /**
    * 日期转unixTime（单位：秒）
    *
    * @param day 日期（格式：yyyyMMdd)
    * @return unixTime（单位：秒）
    */
  def unixTime(day: String): Long = {
    new SimpleDateFormat(YYYYMMDD).parse(day).getTime / 1000
  }

  /**
    * 获取当前日期（格式：yyyyMMdd）
    *
    * @return 当前日期字符串
    */
  def nowDay: String = {
    val formatter = new SimpleDateFormat(YYYYMMDD)
    formatter.format(new Date)
  }

  /**
    * 获取指定日期前一周的周日00:00:00的时间戳
    * <p>
    * 如果未指定日期，则使用当前日期
    * </p>
    *
    * @param day 指定日期（格式：yyyyMMdd)
    * @return 时间戳（单位：秒）
    */
  def beforeWeek(day: String = null): Long = {
    val d =
      if (day == null || day.isEmpty)
        new SimpleDateFormat(YYYYMMDD).parse(nowDay)
      else
        new SimpleDateFormat(YYYYMMDD).parse(day)

    val cal = Calendar.getInstance
    cal.setTime(d)
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    cal.add(Calendar.DAY_OF_WEEK, 1 - dayOfWeek)
    cal.getTimeInMillis / 1000
  }

  /**
    * 获取指定日期前一月的最后一天00:00:00的时间戳
    * <p>
    * 如果未指定日期，则使用当前日期
    * </p>
    *
    * @param day 指定日期（格式：yyyyMMdd)
    * @return 时间戳（单位：秒）
    */
  def beforeMonth(day: String = null): Long = {
    val d =
      if (day == null || day.isEmpty)
        new SimpleDateFormat(YYYYMMDD).parse(nowDay)
      else
        new SimpleDateFormat(YYYYMMDD).parse(day)

    val cal = Calendar.getInstance
    cal.setTime(d)
    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
    cal.add(Calendar.DAY_OF_WEEK, -dayOfMonth)
    cal.getTimeInMillis / 1000
  }

  /**
    * 时间相减
    *
    * @param subtractor 减数
    * @param subtrahend 被减数
    * @param format     时间格式
    * @return 差（秒）
    */
  def diff(subtractor: String, subtrahend: String, format: String): Long = {
    val df = new SimpleDateFormat(format)
    try {
      val d1 = df.parse(subtractor)
      val d2 = df.parse(subtrahend)
      (d1.getTime - d2.getTime) / 1000
    } catch {
      case _: Exception => 0
    }
  }

  /**
    * 时间相减
    *
    * @param subtractor 减数（格式：yyyy-MM-dd HH:mm:ss.SSSSSS）
    * @param subtrahend 被减数（格式：yyyy-MM-dd HH:mm:ss.SSSSSS）
    * @return 差（毫秒）
    */
  def diffMs(subtractor: String, subtrahend: String): Long = {
    if (subtractor == null || subtrahend == null) {
      0
    } else {
      try {
        Timestamp.valueOf(subtractor).getTime - Timestamp.valueOf(subtrahend).getTime
      } catch {
        case _: Exception => 0
      }
    }
  }

  /**
    * 时间相减
    * <p>
    * 默认格式为："yyyyMMddHHmmss"
    * </p>
    *
    * @param subtractor 减数
    * @param subtrahend 被减数
    * @return 差（秒）
    */
  def diff(subtractor: String, subtrahend: String): Long = {
    diff(subtractor, subtrahend, YYYYMMDDHHMISS)
  }

  /**
    * 时间相减
    * <p>
    * 默认格式为："yyyyMMddHHmmss"
    * </p>
    *
    * @param subtractor 减数
    * @param subtrahend 被减数
    * @return 差（分）
    */
  def diffMin(subtractor: String, subtrahend: String): Long = {
    diff(subtractor, subtrahend, YYYYMMDDHHMISS) / 60
  }

  /**
    * 时间加常量
    *
    * @param dateStr 时间字符串
    * @param format  格式字符串
    * @param field   单位
    * @param num     被加算的值
    * @return 时间字符串
    */
  def addDate(dateStr: String, format: String, field: Int, num: Int): String = {
    val sdf = new SimpleDateFormat(format)
    try {
      val date = sdf.parse(dateStr)
      val calendar = Calendar.getInstance()
      calendar.setTime(date)

      calendar.add(field, num)
      sdf.format(calendar.getTime)
    } catch {
      case _: Exception => dateStr
    }
  }

  /**
    * 在秒级，时间加常量
    * <p>
    * 默认格式为："yyyyMMddHHmmss"
    * </p>
    *
    * @param dateStr 时间字符串
    * @param num     被加算的值
    * @return 时间字符串
    */
  def addDateSec(dateStr: String, num: Int): String = {
    addDate(dateStr, YYYYMMDDHHMISS, Calendar.SECOND, num)
  }

  /**
    * 在分级，时间加常量
    * <p>
    * 默认格式为："yyyyMMddHHmmss"
    * </p>
    *
    * @param dateStr 时间字符串
    * @param num     被加算的值
    * @return 时间字符串
    */
  def addDateMin(dateStr: String, num: Int): String = {
    addDate(dateStr, YYYYMMDDHHMISS, Calendar.MINUTE, num)
  }

  /**
    * 在日级，时间加常量
    * <p>
    * 默认格式为："yyyy-MM-dd"
    * </p>
    *
    * @param dateStr 时间字符串
    * @param num     被加算的值
    * @return 时间字符串
    */
  def addDateDay(dateStr: String, num: Int): String = {
    addDate(dateStr, YYYY_MM_DD, Calendar.DATE, num)
  }

  /**
    * 获取指定日对应周周一
    *
    * @param dateStr 日期（格式：yyyy-MM-dd）
    * @return
    */
  def monday(dateStr: String): String = {
    val cal = Calendar.getInstance()
    cal.setTime(toYMD(dateStr, YYYY_MM_DD))
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val df = new SimpleDateFormat(YYYY_MM_DD)
    df.format(cal.getTime)
  }

  /**
    * 获取指定日对应周周日
    *
    * @param dateStr 日期（格式：yyyy-MM-dd）
    * @return
    */
  def sunday(dateStr: String): String = {
    val cal = Calendar.getInstance()
    cal.setTime(toYMD(dateStr, YYYY_MM_DD))
    cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    cal.add(Calendar.DAY_OF_WEEK, 7)
    val df = new SimpleDateFormat(YYYY_MM_DD)
    df.format(cal.getTime)
  }

  /**
    * 获取指定日对应月第一天
    *
    * @param dateStr 日期（格式：yyyy-MM-dd）
    * @return
    */
  def firstOfMonth(dateStr: String): String = {
    val cal = Calendar.getInstance()
    cal.setTime(toYMD(dateStr, YYYY_MM_DD))
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val df = new SimpleDateFormat(YYYY_MM_DD)
    df.format(cal.getTime)
  }

  /**
    * 获取指定日对应月最后一天
    *
    * @param dateStr 日期（格式：yyyy-MM-dd）
    * @return
    */
  def lastOfMonth(dateStr: String): String = {
    val cal = Calendar.getInstance()
    cal.setTime(toYMD(dateStr, YYYY_MM_DD))
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.add(Calendar.MONTH, 1)
    cal.add(Calendar.DAY_OF_MONTH, -1)
    val df = new SimpleDateFormat(YYYY_MM_DD)
    df.format(cal.getTime)
  }

  /**
    * 获取昨天日期
    *
    * @param format 时间字符串的格式（默认：yyyy-MM-dd）
    * @return 昨天日期
    */
  def yesterday(format: String = YYYY_MM_DD): String = {
    val sdf = new SimpleDateFormat(format)
    val cal = Calendar.getInstance()
    cal.add(Calendar.DATE, -1)
    sdf.format(cal.getTime)
  }

  /**
    * 获取前一月第一天和最后一天
    *
    * @return (月，上一月第一天, 上一月最后一天)的元组
    */
  def beforeMonthRange(): (String, String, String) = {
    beforeMonthRange(now(YYYY_MM_DD))
  }

  /**
    * 获取指定日期所在月第一天和最后一天
    *
    * @param day    指定日期
    * @param format 时间字符串的格式（默认：yyyy-MM-dd）
    * @return (月，前一月第一天, 前一月最后一天)的元组
    */
  def beforeMonthRange(day: String, format: String = YYYY_MM_DD): (String, String, String) = {
    val cal = Calendar.getInstance()
    cal.setTime(toYMD(day, YYYY_MM_DD))
    cal.add(Calendar.MONTH, -1)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val df = new SimpleDateFormat(YYYY_MM_DD)
    val s = df.format(cal.getTime)

    cal.add(Calendar.MONTH, 1)
    cal.add(Calendar.DAY_OF_MONTH, -1)
    val e = df.format(cal.getTime)

    (s.substring(0, 7), s, e)
  }

  /**
    * 获取当前月第一天和最后一天
    *
    * @return (月，第一天, 最后一天)的元组
    */
  def currentMonthRange(): (String, String, String) = {
    currentMonthRange(now(YYYY_MM_DD))
  }

  /**
    * 获取指定日期所在月第一天和最后一天
    *
    * @param day    指定日期
    * @param format 时间字符串的格式（默认：yyyy-MM-dd）
    * @return (月，第一天, 最后一天)的元组
    */
  def currentMonthRange(day: String, format: String = YYYY_MM_DD): (String, String, String) = {
    val cal = Calendar.getInstance()
    cal.setTime(toYMD(day, YYYY_MM_DD))
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val df = new SimpleDateFormat(YYYY_MM_DD)
    val s = df.format(cal.getTime)

    cal.add(Calendar.MONTH, 1)
    cal.add(Calendar.DAY_OF_MONTH, -1)
    val e = df.format(cal.getTime)

    (s.substring(0, 7), s, e)
  }

  /**
    * 获取前一周第一天和最后一天
    * <p>
    * 该月第几周：yyyy-MM-x（x为当月第几周，按周一归属分类）
    * </p>
    *
    * @return (该月第几周，前一周第一天, 前一周最后一天)的元组的元组
    */
  def beforeWeekRange(): (String, String, String) = {
    beforeWeekRange(now(YYYY_MM_DD))
  }

  /**
    * 获取指定日期前一周第一天和最后一天
    * <p>
    * 该月第几周：yyyy-MM-x（x为当月第几周，按周一归属分类）
    * </p>
    *
    * @param day    指定日期
    * @param format 时间字符串的格式（默认：yyyy-MM-dd）
    * @return (该月第几周，前一周第一天, 前一周最后一天)的元组的元组
    */
  def beforeWeekRange(day: String, format: String = YYYY_MM_DD): (String, String, String) = {
    val cal = Calendar.getInstance()
    cal.setTime(toYMD(day, YYYY_MM_DD))

    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    // 指定日期为周日时，需要特殊处理（系统默认周日到周六为一周，而需要周一到周日为一周）
    val offset = if (dayOfWeek == 0) 7 else dayOfWeek
    cal.add(Calendar.DATE, -offset)
    val df = new SimpleDateFormat(YYYY_MM_DD)

    val e = df.format(cal.getTime)
    cal.add(Calendar.DATE, -6)
    val s = df.format(cal.getTime)

    val week = cal.get(Calendar.WEEK_OF_MONTH)

    // 如果1号是周日（系统默认周日到周六为一周），则需要特殊处理
    val month = s.substring(0, 8)
    cal.setTime(toYMD(month + "01", YYYY_MM_DD))

    val w = if (cal.get(Calendar.DAY_OF_WEEK) == 1) {
      week
    } else {
      week - 1
    }

    (month + w, s, e)
  }

  /**
    * 获取当天所在周第一天和最后一天
    * <p>
    * 该月第几周：yyyy-MM-x（x为当月第几周，按周一归属分类）
    * </p>
    *
    * @return (该月第几周，当天所在周第一天yyyy-MM-dd, 当天所在周最后一天yyyy-MM-dd)的元组的元组
    */
  def currentWeekRange(): (String, String, String) = {
    currentWeekRange(now(YYYY_MM_DD))
  }

  /**
    * 获取指定日期所在周第一天和最后一天
    * <p>
    * 该月第几周：yyyy-MM-x（x为当月第几周，按周一归属分类）
    * </p>
    *
    * @param day    指定日期
    * @param format 时间字符串的格式（默认：yyyy-MM-dd）
    * @return (该月第几周，该周第一天yyyy-MM-dd, 该周最后一天yyyy-MM-dd)的元组的元组
    */
  def currentWeekRange(day: String, format: String = YYYY_MM_DD): (String, String, String) = {
    val cal = Calendar.getInstance()
    cal.setTime(toYMD(day, YYYY_MM_DD))

    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    // 指定日期为周日时，需要特殊处理（系统默认周日到周六为一周，而需要周一到周日为一周）
    val offset = if (dayOfWeek == 0) 6 else dayOfWeek - 1
    cal.add(Calendar.DATE, 6 - offset)
    val df = new SimpleDateFormat(YYYY_MM_DD)

    val e = df.format(cal.getTime)
    cal.add(Calendar.DATE, -6)
    val s = df.format(cal.getTime)

    val week = cal.get(Calendar.WEEK_OF_MONTH)

    // 如果1号是周日（系统默认周日到周六为一周），则需要特殊处理
    val month = s.substring(0, 8)
    cal.setTime(toYMD(month + "01", YYYY_MM_DD))

    val w = if (cal.get(Calendar.DAY_OF_WEEK) <= 2) {
      week
    } else {
      week - 1
    }

    (month + w, s, e)
  }

  /**
    * 判断指定日期是否是周一
    *
    * @param day 指定日期（yyyy-MM-dd）
    * @return
    */
  def isMonday(day: String): Boolean = {
    val cal = Calendar.getInstance()
    cal.setTime(toYMD(day, YYYY_MM_DD))

    cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
  }

  /**
    * 判断指定日期是否是周日
    *
    * @param day 指定日期（yyyy-MM-dd）
    * @return
    */
  def isSunday(day: String): Boolean = {
    val cal = Calendar.getInstance()
    cal.setTime(toYMD(day, YYYY_MM_DD))

    cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
  }

  /**
    * 判断指定日期是否是当月第一天
    *
    * @param day 指定日期（yyyy-MM-dd）
    * @return
    */
  def isFirstDayOfMonth(day: String): Boolean = {
    val cal = Calendar.getInstance()
    cal.setTime(toYMD(day, YYYY_MM_DD))

    cal.get(Calendar.DAY_OF_MONTH) == 1
  }

  /**
    * 判断指定日期是否是当月最后一天
    *
    * @param day 指定日期（yyyy-MM-dd）
    * @return
    */
  def isLastDayOfMonth(day: String): Boolean = {
    val cal = Calendar.getInstance()
    cal.setTime(toYMD(day, YYYY_MM_DD))

    cal.get(Calendar.DAY_OF_MONTH) == cal.getActualMaximum(Calendar.DAY_OF_MONTH)
  }

  /**
    * 判断指定日期是否是当年最后一天
    *
    * @param day 指定日期（yyyy-MM-dd）
    * @return
    */
  def isLastDayOfYear(day: String): Boolean = {
    val cal = Calendar.getInstance()
    cal.setTime(toYMD(day, YYYY_MM_DD))

    cal.get(Calendar.DAY_OF_YEAR) == cal.getActualMaximum(Calendar.DAY_OF_YEAR)
  }

  /**
    * 获取当前年第一天和最后一天
    *
    * @return (年，第一天, 最后一天)的元组
    */
  def currentYearRange(): (String, String, String) = {
    currentYearRange(now(YYYY_MM_DD))
  }

  /**
    * 获取指定日期所在年第一天和最后一天
    *
    * @param day    指定日期
    * @param format 时间字符串的格式（默认：yyyy-MM-dd）
    * @return (年，第一天, 最后一天)的元组
    */
  def currentYearRange(day: String, format: String = YYYY_MM_DD): (String, String, String) = {
    val cal = Calendar.getInstance()
    cal.setTime(toYMD(day, YYYY_MM_DD))
    val year = cal.get(Calendar.YEAR).toString

    (year, year + "-01-01", year + "-12-31")
  }

  /**
    * 获取两个日期之间的日期
    *
    * @param start 开始日期（格式：yyyy-MM-dd）
    * @param end   结束日期（格式：yyyy-MM-dd）
    * @return 日期集合
    */
  def betweenDates(start: String, end: String): Seq[String] = {
    val startData = new SimpleDateFormat(YYYY_MM_DD).parse(start)
    val endData = new SimpleDateFormat(YYYY_MM_DD).parse(end)

    val dateFormat: SimpleDateFormat = new SimpleDateFormat(YYYY_MM_DD)
    var buffer = new ListBuffer[String]
    buffer += dateFormat.format(startData.getTime)
    val tempStart = Calendar.getInstance()

    tempStart.setTime(startData)
    tempStart.add(Calendar.DAY_OF_YEAR, 1)

    val tempEnd = Calendar.getInstance()
    tempEnd.setTime(endData)
    while (tempStart.before(tempEnd)) {
      // result.add(dateFormat.format(tempStart.getTime()))
      buffer += dateFormat.format(tempStart.getTime)
      tempStart.add(Calendar.DAY_OF_YEAR, 1)
    }
    buffer += dateFormat.format(endData.getTime)
    buffer.toList
  }
}
