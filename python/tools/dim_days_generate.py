#!/usr/bin/env python
# -*- coding: utf-8 -*-


import datetime
import sys


def main():
    """
    生成日期维度，以"|"分割
    格式：yyyy-MM-dd|yyyy|yyyy-MM|yyyy-MM-n
    其中n为第几周（从1开始），周一属于那个月，则该周归属对应月

    :return:
    """
    if len(sys.argv) < 3:
        print """
          lost parameter。python script.py start end
          
          start: 开始日期（格式：yyyyMMdd）
          end:   结束日期（格式：yyyyMMdd）
        """

    start = datetime.datetime.strptime(sys.argv[1], "%Y%m%d").date()
    end = datetime.datetime.strptime(sys.argv[2], "%Y%m%d").date()

    fo = open('dim_days.txt', 'w', 1024)

    for i in range((end - start).days + 1):
        date = start + datetime.timedelta(days=i)

        y = date.strftime("%Y")
        m = date.strftime("%Y-%m")
        d = str(date)

        w = ""
        # 当月第一天
        first_day_of_cur_month = datetime.datetime(date.year, date.month, 1)
        week_of_first_day = first_day_of_cur_month.weekday()
        if week_of_first_day > 0:  # 当月第一天不是周一的话（需要多减一周）
            # 上一个月的第一天
            first_day_of_pre_month = datetime.datetime(date.year, date.month - 1, 1) if date.month > 1 \
                else datetime.datetime(date.year - 1, 12, 1)
            # 上一个月的最后一天
            last_day_of_pre_month = first_day_of_cur_month + datetime.timedelta(days=-1)

            if first_day_of_pre_month.weekday() == 0:  # 上一个月的第一天是周一

                if date.day + week_of_first_day > 7:  # 当月第一周
                    s = int(first_day_of_cur_month.strftime("%W"))
                    e = int(date.strftime("%W"))
                    w = m + "-" + str(e - s)

                else:  # 当前日在5日及以前
                    s = int(first_day_of_pre_month.strftime("%W"))
                    e = int(last_day_of_pre_month.strftime("%W"))
                    w = last_day_of_pre_month.strftime("%Y-%m") + "-" + str(e - s + 1)
            else:
                if date.day + week_of_first_day > 7:  # 当月第一周
                    s = int(first_day_of_cur_month.strftime("%W"))
                    e = int(date.strftime("%W"))
                    w = m + "-" + str(e - s)
                else:
                    s = int(first_day_of_pre_month.strftime("%W"))
                    e = int(last_day_of_pre_month.strftime("%W"))
                    w = last_day_of_pre_month.strftime("%Y-%m") + "-" + str(e - s)

        else:  # 当月第一天是周一
            s = int(first_day_of_cur_month.strftime("%W"))
            e = int(date.strftime("%W"))
            w = m + "-" + str(e - s + 1)

        text = "%s|%s|%s|%s" % (d, y, m, w)
        print text
        fo.write(text)
        fo.write("\r\n")

    fo.close()

    print "生成日期维度完毕"


if __name__ == '__main__':
    main()
