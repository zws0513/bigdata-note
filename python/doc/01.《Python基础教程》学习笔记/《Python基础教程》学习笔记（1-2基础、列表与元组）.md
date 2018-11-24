#1. 基础知识

##1.1 示例

```python
#!/usr/bin/python
# -*- coding: utf-8 -*-

# 第一行表明这是python脚本
# 第二行使脚本支持中文

print "数字和表达式"
# 除法
print 10 / 3
# 求余
print 10 % 3
# 浮点运算
print 10 / 3.
# 幂运算
print 2 ** 3

# 内建函数
print 10 + pow(2, 3)

# 引入模块
import math
print math.floor(32.9)

# 通过以下方式引入模块，函数前不用加模块名
from math import sqrt
print sqrt(9)

# 复数计算
import cmath
print cmath.sqrt(-1)
print (1+3j) * (9+4j)

# 用户输入
# input会假设用户输入的是合法的python表达式
# 这里如果输入字符需 "test"
y = input("y:")
print y
# raw_input所有输入当做原始数据
name = raw_input("what's your name? ")
print "Hello, " + name + "!"

# 原始字符串，使用r，字符串中的"\"不会被特殊处理
print "C:\npath"
print r"C:\npath"

# Unicode字符串
print u'Hello, world!'
```

结果

```
$ ./basedemo.py
数字和表达式
3
1
3.33333333333
8
18
32.0
3.0
1j
(-3+31j)
y:"tet"
tet
what's your name? demo
Hello, demo!
C:
path
C:\npath
Hello, world!
```

## 1.2 部分函数说明

函数 | 描述
--------- | ---------
abs(number) | 返回数字的绝对值
cmath.sqrt(number) | 返回平方根，可用于负数
float(object) | 将字符串和数字转换为浮点数
help() | 提供交互式帮助
input(prompt) | 获取用户输入，参数为合法的python表达式
int(object) | 将字符串和数字转换为整数
long(object) | 将字符串和数字转换为长整型数
math.ceil(number) | 向上取整，返回值为浮点数
math.floor(number) | 向下取整，返回值为浮点数
math.sqrt(number) | 平方根，不能用于负数
pow(x, y[, z]) | 返回x的y次幂（所得结果对z取模）
raw_input(prompt) | 获取用户输入，返回的类型为字符串
repr(object) | 返回值得字符串表示形式
round(number[, ndigiths]) | 根据给定的精度对数字进行四舍五入
str(object) | 将值转换为字符串

#2. 列表和元组

列表和元组的主要区别在于：列表可以修改，元组则不能。

##2.1 通用序列操作
###2.1.1 索引

```
H
#!/usr/bin/python
# -*- coding: utf-8 -*-

edward = ['Edward Gumby', 42]
print edward

greeting = 'Hello'
# 索引从0开始
print greeting[0]

# 索引为负时，从右开始计数。最后1个元素的索引是-1
print greeting[-1]

# 字符串也可直接使用索引
print 'Hello'[1]

# 函数调用返回一个序列，也可以直接使用索引操作
fourth = raw_input('Year: ')[3]
print fourth
```

结果

```
$ ./listandtuple.py
['Edward Gumby', 42]
H
o
e
Year: 2016
6
```

###2.1.2 分片

分片通过冒号相隔的两个索引实现，用于访问一定范围内的元素

```
#!/usr/bin/python
# -*- coding: utf-8 -*-

tag = '<a href="https://www.baidu.com">百度</a>'
print tag
print tag[9:30]
print tag[20 : -1]

numbers = [1,2,3,4,5,6,7,8,9,10]
# 第1个索引的元素是包含在分片内的，而第2个则不包含在分片内
print numbers[7:11]

# 从结尾开始计数，访问最后一个元素的方法
print numbers[-1:]
# 同样适用于序列开始的元素
print numbers[:3]

# 增加步长
print numbers[0:10:2]
# 步长为负时，从右到左提取元素
print numbers[8:3:-2]
```

结果

```
$ ./split.py
<a href="https://www.baidu.com">百度</a>
https://www.baidu.com
.baidu.com">百度</a
[8, 9, 10]
[10]
[1, 2, 3]
[1, 3, 5, 7, 9]
[9, 7, 5]
```

###2.1.3 序列相加

```
>>> [1,2,3] + [5,6]
[1, 2, 3, 5, 6]
```

<font color=red>
两种相同类型的序列才能进行连接操作
</font>

###2.1.4 乘法

```
>>> 'python' * 2
'pythonpython'
>>> [1,2] * 3
[1, 2, 1, 2, 1, 2]

>>> sequence = [None] * 10
>>> sequence
[None, None, None, None, None, None, None, None, None, None]
```

###2.1.5 成员资格

检查一个值是否在序列中，可以使用in运算符。

```
>>> '3' in tag
False

>>> raw_input('enter string: ') in tag
enter string: baidu
True
```

###2.1.6 长度、最小值和最大值

```
>>> numbers = [112, 2, 567]
>>> len(numbers)
3
>>> max(numbers)
567
>>> min(numbers)
2
>>> max(2,1,7,9)
9
```

##2.2 列表

元组和字符串是不变的，列表是可变的

###2.2.1 list函数

list函数适用于所有类型的序列，而不只是字符串

```
>>> list('hello')
['h', 'e', 'l', 'l', 'o']
```

###2.2.2 基本的列操作

```
# 元素赋值
>>> x = [1,1,1]
>>> x
[1, 1, 1]
>>> x[1] = 2
>>> x
[1, 2, 1]

# 删除元素
>>> del x[1]
>>> x
[1, 1]

# 分片赋值
>>> name = list('Perl')
>>> name
['P', 'e', 'r', 'l']
>>> name[2:] = list('arr')
>>> name
['P', 'e', 'a', 'r', 'r']

# 实现插入功能
>>> name[0:0] = ['T', 'S']
>>> name
['T', 'S', 'P', 'e', 'a', 'r', 'r']

# 实现删除功能
>>> name
['T', 'S', 'S', 'e', 'X', 'r', 'r']
>>> name[0:5] = []
>>> name
['r', 'r']
```

###2.2.3 列表方法

调用方式：对象.方法(参数)

1. append
  用于在列表末尾追加新的对象

  ```
  >>> name
  ['r', 'r']
  >>> name.append(4)
  >>> name
  ['r', 'r', 4]
  ```

  <font color=red>
  append方法是直接修改原来的列表
  </font>

2. count
  统计某个元素在列表中出现的次数

  ```
  >>> name
  ['r', 'r', 4]
  >>> name.count('r')
  2
  >>> name.count('s')
  0
  ```

3. extend
  在列表末尾一次性追加另一个序列中的多个值

  ```
  >>> name
  ['r', 'r', 4]
  >>> name.extend(list('text'))
  >>> name
  ['r', 'r', 4, 't', 'e', 'x', 't']
  ```

  <font color=red>
  extend方法是直接修改被扩展的序列，而原始的连接操作是返回一个全新的列表。
  </font>

4. index
  从列表中找出某个值第一个匹配的索引位置。

  ```
  >>> name
  ['r', 'r', 4, 't', 'e', 'x', 't']
  >>> name.index('r')
  0
  >>> name.index('a')
  Traceback (most recent call last):
    File "<stdin>", line 1, in <module>
  ValueError: 'a' is not in list
  ```

  <font color=red>
  找到返回索引，而没找到引发异常。
  </font>

5. insert
  将对象插入到列表中

  ```
  >>> name
  ['r', 'r', 4, 't', 'e', 'x', 't']
  >>> name.insert(3, 'insert')
  >>> name
  ['r', 'r', 4, 'insert', 't', 'e', 'x', 't']
  ```

6. pop
  移除列表中的一个元素（默认最后一个），并返回该元素

  ```
  >>> name
  ['r', 'r', 4, 'insert', 't', 'e', 'x', 't']
  >>> name.pop()
  't'
  >>> name
  ['r', 'r', 4, 'insert', 't', 'e', 'x']
  >>> name.pop(2)
  4
  >>> name
  ['r', 'r', 'insert', 't', 'e', 'x']
  ```

7. remove
  移除列表中某个值的第一个匹配项

  ```
  >>> name
  ['r', 'r', 'insert', 't', 'e', 'x']
  >>> name.remove('t')
  >>> name
  ['r', 'r', 'insert', 'e', 'x']
  >>> name.remove('r')
  >>> name
  ['r', 'insert', 'e', 'x']
  ```

8. reverse
  将列表中的元素反向存放

  ```
  >>> name
  ['r', 'insert', 'e', 'x']
  >>> name.reverse()
  >>> name
  ['x', 'e', 'insert', 'r']
  ```

9. sort
  排序列表

  ```
  >>> name
  ['x', 'e', 'insert', 'r']
  >>> y = name[:]
  >>> y.sort()
  >>> name
  ['x', 'e', 'insert', 'r']
  >>> y
  ['e', 'insert', 'r', 'x']

  # 可以使用sorted函数排序列表，并返回新的列表
  >>> sorted(name)
  ['e', 'insert', 'r', 'x']
  >>> name
  ['x', 'e', 'insert', 'r']
  ```

###2.3 元组

语法：用逗号分隔了一些值，就自动创建了元组。

```
>>> 1,2,3
(1, 2, 3)
>>> ()
()
>>> 42,
(42,)
>>> 3*(40+2,)
(42, 42, 42)
```

###2.3.1 tuple函数

以一个序列作为参数并把它转换为元组。

```
>>> tuple('abc')
('a', 'b', 'c')
>>> tuple((1,2,3))
(1, 2, 3)
```

###2.3.2 基本元组操作

可以参照其他类型的序列来实现：

```
>>> x = 1,2,3
>>> x[1]
2
>>> x[0:2]
(1, 2)
>>> len(x)
3
```

###2.3.3 意义

1. 元组可以在映射中当作键使用——而列表则不行；
2. 元组作为很多内建函数和方法的返回值存在

##2.4 部分函数

函数 | 描述
------- | -------
cmp(x, y) | 比较两个值
len(seq) | 返回序列的长度
list(seq) | 把序列转换成列表
max(args) | 返回序列或参数集合中的最大值
min(args) | 返回序列或参数集合中的最小值
reversed(seq) | 对序列进行反向迭代
sorted(seq) | 返回已排序的包含seq所有元素的列表
tuple(seq) | 把序列转换成元组