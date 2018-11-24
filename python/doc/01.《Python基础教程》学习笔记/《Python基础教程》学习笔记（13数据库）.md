#13. 数据库支持
##13.1 Python数据库API
###13.1.1 全局变量

任何支持2.0版本DB API的数据库模块都必须定义3个描述模块特性的全局变量。

变量 | 用途
------ | -----
apilevel | 所使用的Python DB API版本
threadsafety | 模块的线程安全等级，取值0~3。0：线程完全不共享模块，1：线程本身可共享模块，但不对连接共享，3：完全线程安全
paramstyle | 执行多次类似查询时，参数是如何被拼接到SQL查询中的。<br>'format'：标准的字符串格式`%s`<br>'pyformat'：扩展的格式代码，用于字典拼接中`%(foo)`<br>'qmark'：使用问号<br>'numeric'：使用`:1`或`:2`格式的字段（数字表示参数序号）<br>'named'：`:foobar`这样的字段，其中foobar为参数名

###13.1.2 异常

异常 | 超类 | 描述
------ | ------- | ------
StandardError | | 所有异常的泛型基类
Warning | StandardError | 在非致命错误发生时引发
Error | StandardError | 所有错误条件的泛型超类
InterfaceError | Error | 关于接口而非数据库的错误
DatabaseError | Error | 与数据库相关的错误的基类
DataError | DatabaseError | 与数据相关的问题，比如值超出范围
OperationalError | DatabaseError | 数据库内部操作错误
IntegrityError | DatabaseError | 关系完整性受到影响，比如键检查失败
InternalError | DatabaseError | 数据库内部错误，比如非法游标
ProgrammingError | DatabaseError | 用户编程错误，比如未找到表
NotSupportedError | DatabaseError | 请求不支持的特性（比如回滚）

###13.1.3 连接和游标

connect函数的常用参数

参数名 | 描述 | 是否可选
----- | ------ | --------
dsn | 数据源名称，给出该参数表示数据库依赖 | 否
user | 用户名 | 是
password | 用户密码 | 是
host | 主机名 | 是
database | 数据库名 | 是

connect函数返回连接对象，这个对象表示目前和数据库的会话。连接对象方法

方法名 | 描述
------ | ------
close() | 关闭连接之后，连接对象和它的游标均不可用
commit() | 如果支持的话就提交挂起的事务，否则不做任何事
rollback() | 回滚挂起的事务（可能不可用）
cursor() | 返回连接的游标对象

游标对象方法

名称 | 描述
------ | ------
callproc(name[, params]) | 使用给定的名称和参数（可选）调用已命名的数据库程序
close() | 关闭游标之后，游标不可用
execute(oper[, params]) | 执行SQL操作，可能使用参数
executemany（opera, pseq) | 对序列中的每个参数执行SQL操作
fetchone() | 把查询的结果集中的下一行保存为序列，或者None
fetchmany([size]) | 获取查询结果集中的多行，默认尺寸为arraysize
fetchall() | 将所有（剩余）的行作为序列的序列
nextset() | 跳至下一个可用的结果集（可选）
setinputsizes(sizes) | 为参数预先定义内存区域
setoutputsize(size[, col]]) | 为获取的大数据值设置缓冲区尺寸

游标对象特性

名称 | 描述
------ | ------
description | 结果列描述的序列，只读
rowcount | 结果中的行数，只读
arraysize | fetchmany中返回的行数，默认为1

###13.1.4 类型

DB API构造函数和特殊值

名称 | 描述
------ | ------
Date(year, month, day) | 创建保存日期值的对象
Time(hour, minute, second) | 创建保存时间值的对象
Timestamp(y, mon, d, h, min, s) | 创建保存时间戳的对象
DateFromTicks(ticks) | 创建保存自新纪元以来秒数的对象
TimeFromTicks(ticks) | 创建保存来自秒数的时间值的对象
TimestampFromTicks(ticks) | 创建保存来自秒数的时间戳值的对象
Binary(string) | 创建保存二进制字符串值的对象
STRING | 描述基于字符串的列类型（比如CHAR）
BINARY | 描述二进制列（比如LONG或RAW）
NUMBER | 描述数字列
DATETIME | 描述日期/时间列
ROWID | 描述行ID列

##13.2 SQLite和PySQLite
###13.2.1 入门

```
import sqlite3
conn = sqlite3.connect('somedatabase.db')
curs = conn.cursor()
conn.commit()
conn.close()
```

###13.2.2 数据库应用程序示例

**创建和填充表**

```
# 将数据导入数据库（importdata.py）

def convert(value):
    if value.startwith('~'):
        return value.strip('~')
    if not value:
        value = '0'
    return float(value)

conn = sqlite3.connect('food.db')
curs = conn.cursor()

curs.execute('''
CREATE TABLE food (
  id          TEXT    PRIMARY KEY,
  desc        TEXT,
  water       FLOAT,
  kcal        FLOAT,
  protein     FLOAT,
  fat         FLOAT,
  ash         FLOAT,
  carbs       FLOAT,
  fiber       FLOAT,
  sugar       FLOAT
)
''')

query = 'INSERT INTO food VALUES (?,?,?,?,?,?,?,?,?,?)'

for line in open('ABBREV.txt'):
    fields = line.split('^')
    vals = [convert(f) for f in fields[:field_count]]
    curs.execute(query, vals)

conn.commit()
conn.close()
```

**搜索和处理结果**

```
# 食品数据库查询程序（food_query.py）
import sqlite3
import sys

conn = sqlite3.connect('food.db')
curs = conn.cursor()

query = 'SELECT * FROM food WHERE %s' % sys.argv[1]
print query
curs.execute(query)
names = [f[0] for f in curs.description]
for row in curs.fetchall():
    for pair in zip(names, row):
        print '%s: %s' % pair
    print
```

##13.3 本章的新方法

函数 | 描述
------ | ------
connect(...) | 连接数据库，返回连接对象