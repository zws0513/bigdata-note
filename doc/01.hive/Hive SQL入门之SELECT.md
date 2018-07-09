# 1. Select


1.1 语法
---

```SQL
[WITH CommonTableExpression (, CommonTableExpression)*] 
SELECT [ALL | DISTINCT] select_expr, select_expr, ...
   FROM table_reference
   [WHERE where_condition]
   [GROUP BY col_list]
   [ORDER BY col_list ASC | DESC]
   [CLUSTER BY col_list
    | [DISTRIBUTE BY col_list] [SORT BY col_list]
  ]
[LIMIT number]
```

1.2 示例
---

**示例一**

```SQL
select channelId, count(mac) as click_num , count(distinct mac)  as click_pnum
from detail.vodaction 
where partner = 'AH_CMCC'
and day between '2018-01-01' and '2018-01-31'
group by channelId
order by channelId
```

**示例二**

```SQL
with t1 as (select mac from bdw.p_prj_user where partner = 'AH_CMCC'),
        t2 as (select dev_mac as mac from bdw.user_device where partner_code = 'AH_CMCC')
select sum(case when t1.mac is not null then 1 else 0 end) as cnt 
from t2 left join t1 
on t2.mac = t1.mac;
```

好处：
- 代码简洁
- 优化查询，如果SQL中有同样的多个子查询（>1），则会只查询一遍，放入临时表中

**示例三**

```SQL
-- order by 全局排序
-- sort by 局部排序，再做一次归并排序，就可达到全局排序
-- distribute by控制map的输出在reducer是如何划分的
-- cluster by是distribute by和sort by的结合

-- SQL1
select partner, day, count(mac) as click_num, count(distinct mac)  as click_pnum
from detail.vodaction 
where 
partner in ('YNYDHW', 'YNYDZX')
and day between '20180701' and '20180704'
and action = 3
group by partner, day
distribute by partner
sort by partner, day asc

-- SQL2
select partner, day, count(mac) as click_num, count(distinct mac)  as click_pnum
from detail.vodaction 
where 
partner in ('YNYDHW', 'YNYDZX')
and day between '20180701' and '20180704'
and action = 3
group by partner, day
cluster by partner, day

-- cluster by只能降序，不能升序

-- SQL3
select partner, day, count(mac) as click_num, count(distinct mac)  as click_pnum
from detail.vodaction 
where 
partner in ('YNYDHW', 'YNYDZX')
and day between '20180701' and '20180704'
and action = 3
group by partner, day
order by partner, day
```

2. Join
===

2.1 计算引擎的Join类型
---

2.1.1 Map端Join
---

如果一张表的数据很大，另外一张表很少(<1000行)，那么我们可以将数据量少的那张表放到内存里面，在map端做join。

```
select /*+ MAPJOIN(time_dim) */ count(1) from
store_sales join time_dim on (ss_sold_time_sk = t_time_sk)
```

2.1.2 Reduce端Join
---

又叫Common Join或Shuffle Join。适用于两边数据量很大。

2.1.3 SMB（Sort-Merge-Buket） Join
---

目的主要是为了解决大表与大表间的 Join 问题，首先进行分桶（分桶其实就是把大表化成了“小表”），然后把相同Key都放到同一个bucket，较少无关项得扫描。这是典型的分而治之的思想。

```
set hive.auto.convert.sortmerge.join=true
set hive.optimize.bucketmapjoin=true;
set hive.optimize.bucketmapjoin.sortedmerge=true;
```

2.2 SQL的Join类型
---

**t1表**

id | name
--- | ---
1 | zhangsan
2 | lisi
3 | wangwu

**t2表**

id | age
--- | ---
1 | 30
2 | 29
4 | 21

2.2.1 内关联（join）
---

只返回能关联上的结果。

```SQL
SELECT a.id,
  a.name,
  b.age 
FROM t1 a 
join t2 b 
ON (a.id = b.id);
 
-- 执行结果
1 zhangsan 30
2 lisi 29
```

2.2.2 左外关联（left outer join）
---

以LEFT [OUTER] JOIN关键字前面的表作为主表，和其他表进行关联，返回记录和主表的记录数一致，关联不上的字段置为NULL。

```SQL
SELECT a.id,
  a.name,
  b.age 
FROM t1 a 
left join t2 b 
ON (a.id = b.id);
 
-- 执行结果：
1 zhangsan 30
2 lisi 29
3 wangwu NULL
```

<font color=red>注：如果左右表是1对n，则这一条记录会变成n条</font>

2.2.3 右外关联（right outer join）
---

和左外关联相反，以RIGTH [OUTER] JOIN关键词后面的表作为主表，和前面的表做关联，返回记录数和主表一致，关联不上的字段为NULL。

```SQL
SELECT a.id,
  a.name,
  b.age 
FROM t1 a 
RIGHT OUTER JOIN t2 b 
ON (a.id = b.id);
 
-- 执行结果：
1 zhangsan 30
2 lisi 29
NULL NULL 21
```

2.2.4 全外关联（full outer join）
---

以两个表的记录为基准，返回两个表的记录去重之和，关联不上的字段为NULL。

<font color=red>注：FULL JOIN时候，Hive不会使用MapJoin来优化。</font>

```SQL
SELECT a.id,
  a.name,
  b.age 
FROM t1 a 
FULL OUTER JOIN t2 b 
ON (a.id = b.id);
 
-- 执行结果：
1 zhangsan 30
2 lisi 29
3 wangwu NULL
NULL NULL 21
```

2.2.5 半关联（left semi join）
---

以LEFT SEMI JOIN关键字前面的表为主表，返回主表的KEY也在副表中的记录。

```SQL
SELECT a.id,
  a.name 
FROM t1 a 
LEFT SEMI JOIN t2 b 
ON (a.id = b.id);
 
-- 执行结果：
1 zhangsan
2 lisi
 
-- 等价于：
SELECT a.id,
  a.name 
FROM t1 a 
WHERE a.id IN (SELECT id FROM t2);

--也等价于：
SELECT a.id,
  a.name 
FROM t1 a 
WHERE EXISTS (SELECT 1 FROM t2 b WHERE a.id = b.id);
```

2.2.6 笛卡尔积关联（cross join）
---

返回两个表的笛卡尔积结果，不需要指定关联键。

```SQL
SELECT a.id,
  a.name,
  b.age 
FROM t1 a 
CROSS JOIN t2 b;
 
--执行结果：
1 zhangsan 30
1 zhangsan 29
1 zhangsan 21
2 lisi 30
2 lisi 29
2 lisi 21
3 wangwu 30
3 wangwu 29
3 wangwu 21
```

3. 窗口函数
===

**t1**

cookieid | createtime | url
---- | --- | ---
cookie1 | 2015-04-10 10:00:02 | url2
cookie1 | 2015-04-10 10:00:00 | url1
cookie1 | 2015-04-10 10:03:04 | 1url3
cookie1 | 2015-04-10 10:50:05 | url6
cookie1 | 2015-04-10 11:00:00 | url7
cookie1 | 2015-04-10 10:10:00 | url4
cookie1 | 2015-04-10 10:50:01 | url5
cookie2 | 2015-04-10 10:00:02 | url22
cookie2 | 2015-04-10 10:00:00 | url11
cookie2 | 2015-04-10 10:03:04 | 1url33
cookie2 | 2015-04-10 10:50:05 | url66
cookie2 | 2015-04-10 11:00:00 | url77
cookie2 | 2015-04-10 10:10:00 | url44
cookie2 | 2015-04-10 10:50:01 | url55

3.1 first_value、last_value
---

first_value: 取分组内排序后，截止到当前行，第一个值
last_value: 取分组内排序后，截止到当前行，最后一个值

```SQL
SELECT cookieid,
createtime,
url,
ROW_NUMBER() OVER(PARTITION BY cookieid ORDER BY createtime) AS rn,
FIRST_VALUE(url) OVER(PARTITION BY cookieid ORDER BY createtime) AS first1,
LAST_VALUE(url) OVER(PARTITION BY cookieid ORDER BY createtime) AS last1
FROM t1;

cookieid createtime url rn first1  last1
---------------------------------------------------------
cookie1 2015-04-10 10:00:00   url1      1 url1    url1
cookie1 2015-04-10 10:00:02   url2      2 url1    url2
cookie1 2015-04-10 10:03:04   1url3    3 url1    1url3
cookie1 2015-04-10 10:10:00   url4      4 url1    url4
cookie1 2015-04-10 10:50:01   url5      5 url1     url5
cookie1 2015-04-10 10:50:05   url6      6 url1     url6
cookie1 2015-04-10 11:00:00   url7      7 url1     url7
cookie2 2015-04-10 10:00:00   url11    1 url11    url11
cookie2 2015-04-10 10:00:02   url22    2 url11    url22
cookie2 2015-04-10 10:03:04   1url33  3 url11    1url33
cookie2 2015-04-10 10:10:00   url44    4 url11     url44
cookie2 2015-04-10 10:50:01   url55    5 url11    url55
cookie2 2015-04-10 10:50:05   url66    6 url11    url66
cookie2 2015-04-10 11:00:00   url77    7 url11    url77
```

3.2 over
---

**t1**
cookieid | createtime | pv
---- | ---- | ----
cookie1 | 2015-04-10 | 1
cookie1 | 2015-04-11 | 5
cookie1 | 2015-04-12 | 7
cookie1 | 2015-04-13 | 3
cookie1 | 2015-04-14 | 2
cookie1 | 2015-04-15 | 4
cookie1 | 2015-04-16 | 4

```SQL
SELECT cookieid,
createtime,
pv,
SUM(pv) OVER(PARTITION BY cookieid ORDER BY createtime) AS pv1, -- 默认为从起点到当前行
SUM(pv) OVER(PARTITION BY cookieid ORDER BY createtime ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS pv2, --从起点到当前行，结果同pv1 
SUM(pv) OVER(PARTITION BY cookieid) AS pv3, --分组内所有行
SUM(pv) OVER(PARTITION BY cookieid ORDER BY createtime ROWS BETWEEN 3 PRECEDING AND CURRENT ROW) AS pv4, --当前行+往前3行
SUM(pv) OVER(PARTITION BY cookieid ORDER BY createtime ROWS BETWEEN 3 PRECEDING AND 1 FOLLOWING) AS pv5, --当前行+往前3行+往后1行
SUM(pv) OVER(PARTITION BY cookieid ORDER BY createtime ROWS BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING) AS pv6 ---当前行+往后所有行  
FROM t1
order by createtime;

cookieid createtime pv pv1 pv2 pv3 pv4 pv5 pv6 
-----------------------------------------------------------------------------
cookie1 2015-04-10      1   1     1   26    1    6  26
cookie1 2015-04-11      5   6     6   26    6  13  25
cookie1 2015-04-12      7   13  13  26  13  16  20
cookie1 2015-04-13      3   16  16  26  16  18  13
cookie1 2015-04-14      2   18  18  26  17  21  10
cookie1 2015-04-15      4   22  22  26  16  20   8
cookie1 2015-04-16      4   26  26  26  13  13   4
```

注：可以使用sum、avg、count、min、max

3.3 rank、dense_rank、row_number
---

rank：在分组内的排名，排名相等会在名次中留下空位
dense_rank：在分组内的排名，排名相等会在名次中不会留下空位
row_number：从1开始，按照顺序生成分组内记录的排序

```
SELECT 
cookieid,
createtime,
pv,
RANK() OVER(PARTITION BY cookieid ORDER BY pv desc) AS rn1,
DENSE_RANK() OVER(PARTITION BY cookieid ORDER BY pv desc) AS rn2,
ROW_NUMBER() OVER(PARTITION BY cookieid ORDER BY pv DESC) AS rn3 
FROM analytics_2 
WHERE cookieid = 'cookie1';

cookieid day pv rn1 rn2 rn3 
-------------------------------------------------- 
cookie1 2015-04-12   7   1   1   1
cookie1 2015-04-11   5   2   2   2
cookie1 2015-04-15   4   3   3   3
cookie1 2015-04-16   4   3   3   4
cookie1 2015-04-13   3   5   4   5
cookie1 2015-04-14   2   6   5   6
cookie1 2015-04-10   1   7   6   7
```

4. 优化
===

4.1 使用分区剪裁、列剪裁
---

在SELECT中，只拿需要的列，如果有，尽量使用分区过滤，少用SELECT *。
在分区剪裁中，当使用外关联时，如果将副表的过滤条件写在Where后面，那么就会先全表关联，之后再过滤。

4.2 少用count distinct
---

数据量小的时候无所谓，数据量大的情况下，由于COUNT DISTINCT操作需要用一个Reduce Task来完成，这一个Reduce需要处理的数据量太大，就会导致整个Job很难完成，一般COUNT DISTINCT使用先GROUP BY再COUNT的方式替换。

4.3 减少map数（专家）
---

```
set mapred.max.split.size=100000000;
set mapred.min.split.size.per.node=100000000;
set mapred.min.split.size.per.rack=100000000;
set hive.input.format=org.apache.hadoop.hive.ql.io.CombineHiveInputFormat;
```

4.4 调整reduce个数（专家）
---

```
set hive.exec.reducers.bytes.per.reducer=500000000; （500M）
set mapred.reduce.tasks = 15;
```

4.5 中间结果压缩（专家）
---

```SQL
-- 中间Lzo,最终Gzip
set mapred.output.compress = true;  
set mapred.output.compression.codec = org.apache.hadoop.io.compress.GzipCodec;  
set mapred.output.compression.type = BLOCK; 

set mapred.compress.map.output = true;  
set mapred.map.output.compression.codec = org.apache.hadoop.io.compress.LzoCodec;  

set hive.exec.compress.output = true;  
set hive.exec.compress.intermediate = true;  
set hive.intermediate.compression.codec = org.apache.hadoop.io.compress.LzoCodec;  
```

5. 参考
===

[内置函数](https://blog.csdn.net/u013980127/article/details/52606024)
