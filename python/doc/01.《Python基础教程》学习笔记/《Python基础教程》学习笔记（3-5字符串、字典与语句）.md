#3. 使用字符串

##3.1 基本字符串操作

所有标准的序列操作（索引、分片、乘法、判断成员资格、求长度、取最小值和最大值）对字符串同样适用。

<font color=red>
字符串是不可变的
</font>

##3.2 字符串格式化

使用字符串格式化操作符，即百分号%来实现。

<font color=red>
如果字符串中包含百分号，必须使用%%转义
</font>

如果右操作符是元组的话，则其中的每一个元素都会被单独格式化

```
>>> '%s plus %s equals %s' % (1,2,3)
'1 plus 2 equals 3'
```

1. %字符：标记转换说明符的开始
2. 转换标志（可选）：-表示左对齐；+表示在转换值之前要加上正负号；""表示正数之前保留空格；0表示转换值若位数不够则用0填充。
3. 最小字段宽度（可选）：转换后的字符串至少应该具有该值指定的宽度。如果是*，则宽度会从值元组中读取。
4. 点（.）后跟精度值（可选）：实数表示小数点后的位数。字符串则表示最大字段宽度。如果是*，精度将会从元组中读取。
5. 转换类型：

  转换类型 | 含义
  -------- | -----------
  d, i | 带符号十进制整数
  o | 不带符号八进制
  u | 不带符号十进制
  x | 不带符号十六进制（小写）
  X | 不带符号十六进制（大写）
  e | 科学计数法表示的浮点数（小写）
  E | 科学计数法表示的浮点数（大写）
  f, F | 十进制浮点数
  g | 如果指数大于-4或小于精度值则和e相同，否则和f相同
  G | 如果指数大于-4或小于精度值则和E相同，否则和f相同
  C | 单字符（接受整数或单字符字符串）
  r | 字符串（使用repr）
  s | 字符串（使用str)

###3.2.1 简单转换

```
>>> format = "Pi with three decimals: %.4f"
>>> import math
>>> print format % math.pi
Pi with three decimals: 3.1416
```

###3.2.2 字段宽度和精度

两个参数都是整数（字段宽度.精度），通过点号（.）分隔。

```
>>> import math
>>> '%10f' % math.pi
'  3.141593'
>>> '%10.2f' % math.pi
'      3.14'
>>> '%.3f' % math.pi
'3.142'
>>> '%.5s' % 'abcdefghijk'
'abcde'
>>> '%.*s' % (5, 'abcdefghijk')
'abcde'
```

###3.2.3 符号、对齐和用0填充

```
>>> from math import pi
>>> '%010.2f' % pi
'0000003.14'

>>> '%-10.2f' % pi
'3.14      '

>>> ('%+5d' % 10)
'  +10'
```

###3.2.4 模板字符串（string模块）

string模块提供另外一种格式化值得方法：模板字符串

```
>>> from string import Template
>>> s = Template('$x, glorious $x!')
>>> s.substitute(x='slurm')
'slurm, glorious slurm!'
>>> s.substitute(x="xxx")
'xxx, glorious xxx!'

# 如果只是替换单词一部分，那个参数名就必须用括号括起来
>>> s = Template("It's ${x}tastic!")
>>> s.substitute(x="xxx")
"It's xxxtastic!"

# 使用$$插入美元符号
>>> s = Template("Make $$ selling $x!")
>>> s.substitute(x="xxx")
'Make $ selling xxx!'

# 可以使用字典变量提供键-值对
>>> s = Template('A $thing must never $action.')
>>> d = {}
>>> d['thing'] = 'xxx'
>>> d['action'] = 'show his socks'
>>> s.substitute(d)
'A xxx must never show his socks.'
```

##3.3 字符串方法

###3.3.1 find

查找子字符串，返回所在位置最左端索引，如果没找到返回-1。

语法：
src.find(substr, start, end)

###3.3.2 join

使用指定分隔符连接序列为字符串

```
>>> str = ['1','2','3']
>>> sep = "+"
>>> sep.join(str)
'1+2+3'
```

###3.3.3 lower

返回字符串的小写版。

###3.3.4 replace

返回某字符串的所有匹配项均被替换之后的字符串。

###3.3.5 split

将字符串分割为序列。如果不提供任何分隔符，程序会使用（空格、制表、换行等）分隔字符串。

###3.3.6 strip

返回除去两侧空格的字符串。也可以指定需要去除的字符，将它们列为参数即可。

###3.3.7 translate

与replace一样，只是translate只处理单个字符，优势在于可以进行多个替换。

通过maketrans创建替换表。translate的第一个参数是替换表，第二个参数指定需要删除的字符。

```
>>> table = string.maketrans('cs', 'kz')
>>> len(table)
256
>>> 'this is'.translate(table, ' ')
'thiziz'
```

#4. 字典

##4.1 创建和使用字典

键值之间用冒号（:）隔开，项之间用逗号（,）隔开，整个字典用一对大括号括起来。

字典的键是唯一的，而值并不唯一。

###4.1.1 dict

通过其他映射或键-值对序列建立字典。

```
>>> items = [('name', 'Gumby'), ('age', 42)]
>>> d = dict(items)
>>> d
{'age': 42, 'name': 'Gumby'}
>>> v = dict(name='Gumby', age=42)
>>> v
{'age': 42, 'name': 'Gumby'}
```

###4.1.2 基本字典操作

与序列（sequence）类似的操作：

 1. len(d)返回d中项的数量
 2. d[k]返回关联到键k上的值
 3. d[k]=v将值v关联到键k上
 4. del d[k]删除键为k的项
 5. k in d 检查d中是否有含有键为k的项

键类型：可以为任何不可变类型。

###4.1.3 字典的格式化字符串

在每个转换说明符中的%字符后面，可以加上（用圆括号括起来的）键。

```
>>> v
{'age': 42, 'name': 'Gumby'}
>>> "name is %(name)s, age is %(age)s." % v
'name is Gumby, age is 42.'
```

###4.1.4 字典方法

1. clear

清除字典中所有的项。

2. copy

返回一个具有相同键-值对的新字典（这个方法实现的是浅复制）。当在副本中替换值得时候，原始字典不受影响，但如果修改了某个值，原始的字典也会改变。

深复制使用deepcopy函数。

3. fromkeys

使用给定的键建立新的字典，每个键默认对应的值为None，也可以提供默认值。

```
>>> {}.fromkeys(['name', 'age'], 'demo')
{'age': 'demo', 'name': 'demo'}
```

4. get

访问字典项，如果访问不存在的项不会出错，而返回None值。也可以提供默认值，替换None。

```
>>> print v.get("xxx")
None
>>> print v.get("xxx", "test")
test
```

5. has_key

检查字典中是否含有给出的键。

6. items和iteritems

items将所有的字典项以列表方式返回，但项没有特殊的顺序。iteritems返回一个迭代器对象而不是列表。

7. keys和iterkeys

keys方法将字典中的键以列表形式返回，而iterkeys则返回键的迭代器。

8. pop

用于获得对应于给定键的值，然后将该键-值对从字典中移除。

9. popitem

随机弹出字典中的项。

10. setdefault

获取字典中的值。如果键不存在，则设定相应的键值，并返回该值。默认的值是None，也可以提供默认值。

11. update

可以利用一个字典项更新另一个字典。提供的字典中的项会被添加到旧的字典中，若有相同的键则会进行覆盖。

12. values和itervalues

values以列表的形式返回字典的值。itervalues返回值得迭代器。

#5. 条件、循环和其他语句

##5.1 print和import的更多信息
###5.1.1 使用逗号输出

```
# 使用逗号隔开，打印多个表达式，表达之间会以空格分隔。
>>> print 'Age', 42
Age 42

# 如果在结尾加上逗号，则后一条语句会与前一条在同一行打印。(需要在脚本文件中验证)
print 'hello,',
print 'world!'
```

###5.1.2 import别名

```
>>> import math as test1
>>> test1.sqrt(4)
2.0
>>> from math import sqrt as test2
>>> test2(4)
2.0
```

##5.2 赋值

###5.2.1 序列解包

```
>>> values = 1,2,3
>>> x,y,z = values
>>> print x, y, z
1 2 3
>>> x, y = y, x
>>> print x, y, z
2 1 3
```

###5.2.2 链式赋值

将同一个赋值给多个变量。

```
x = y = somefunction()
```

###5.2.3 增量赋值

将运算符放置在赋值运算符=的左边，例如"x += 1"。对于*、/、%等标准运算符都适用。

##5.3 语句块：缩排的乐趣

用冒号（:）标识语句块的开始，块中的每一个语句都是缩进的（缩进量相同）。当回退到和已经闭合的块一样的缩进量时，就标识当前块已经结束。

##5.4 条件和条件语句

下面值作为布尔表达式时，会被看做False

```
False  None 0 "" () [] {}
```

if语句的语法：

```
if condition1 :
  something1
elif condition2 :
  something2
else
  something3
```

###5.4.1 比较运算符

表达式 | 描述
------ | ------
x == y | x等于y
x < y | x小于y
x > y | x大于y
x >= y | x大于等于y
x <= y | x小于等于y
x != y | x不等于y
x is y | x和y是同一个对象
x is not y | x和y是不同的对象
x in y | x是y容器的成员
y not in y | x不是y容器的成员

几个运算符可以连在一起使用，比如：0 < age < 100

```
>>> "alpha" < "beta"
True
>>> [2, [1, 4]] < [2, [1, 5]]
True
```

###5.4.2 布尔运算符

运算符 | 描述
------ | ------
and | 与
or | 或
not | 非

###5.4.3 三元运算符

"a if b else c"：如果b为真，返回a，否则返回c。

###5.4.4 断言

条件后可以添加字符串，用来解释断言：

```
>>> age = -1
>>> assert 0 < age < 100, 'The age must be realistic'
Traceback (most recent call last):
  File "<stdin>", line 1, in <module>
AssertionError: The age must be realistic
```

##5.5 循环

###5.5.1 while

```
while condition :
  something
print 'while demo'
```

###5.5.2 for

```
for value in range :
  something
print 'for demo'
```

###5.5.3 迭代工具

**并行迭代**

```
names = ['a', 'b', 'c']
ages = [2,3,4]
for i in range(len(names)) :
    print names[i], "is", ages[i], 'years old'

# 结果 
a is 2 years old
b is 3 years old
c is 4 years old
```

zip函数可以把多个序列“压缩”在一起，然后返回一个元组的列表，序列不等长时，当最短的序列用完时就会停止。

```
>>> zip(range(5), xrange(1000),range(6))
[(0, 0, 0), (1, 1, 1), (2, 2, 2), (3, 3, 3), (4, 4, 4)]
```

<font color=red>
上面的例子range会计算所有的数字；而xrange只会计算前5个。
</font>

**编号迭代**

```
for index, string in enumerate(strings) : 
    if 'xxx' in string :
        strings[index] = '[censored]'
        
# enumerate函数可以在提供索引的地方迭代索引-值对。
```

**翻转和排序迭代**

reversed：返回翻转后的版本。
sorted：返回排序后的版本。

不过不能直接对它使用索引、分片以及调用list方法。可以使用list类型转换返回的对象。

```
>>> list(reversed('hello,world!'))
['!', 'd', 'l', 'r', 'o', 'w', ',', 'o', 'l', 'l', 'e', 'h']
```

###5.5.4 跳出循环

break：结束循环；
continue：结束当前迭代。

###5.5.5 循环中else子句

仅在没有调用break时执行。

```
#!/usr/bin/python
# -*- coding: utf-8 -*-

from math import sqrt

for n in range(99, 81, -1):
    root = sqrt(n)
    if root == int(root):
        print n
        break
else:
    print "Didn't find it"
```

##5.6 列表推导式

```
# 列表由range(10)中每个x的平方组成
>>> [x * x for x in range(10)]
[0, 1, 4, 9, 16, 25, 36, 49, 64, 81]

# 添加if部分
>>> [x * x for x in range(10) if x%3 == 0]
[0, 9, 36, 81]

# 多个for
>>> [(x,y) for x in range(3) for y in range(3)]
[(0, 0), (0, 1), (0, 2), (1, 0), (1, 1), (1, 2), (2, 0), (2, 1), (2, 2)]
```

##5.7 三人行

###5.7.1 pass什么都没发生

pass用于空代码块占位

```
if condition1:
  print "test1"
elif condition2:
  # 还没完...
  pass
else:
  print "test2"
```

###5.7.2 使用del删除

它不仅会移除一个对象的引用，也会移除那个名字本身。

```
x = 1
del x
```

###5.7.3 使用exec和eval执行和求值字符串

**exec：执行一个字符串**

```
>>> exec "print 'hello, world!'"
hello, world!
```

通过增加 in "scope"起到命名空间的作用。

```
>>> from math import sqrt
>>> scope = {}
>>> exec 'sqrt = 1' in scope
>>> sqrt(4)
2.0
>>> scope['sqrt']
1
```

**eval：计算Python表达式，并返回结果值**

```
>>> raw_input("enter: ")
enter: 6 + 19 +2
'6 + 19 +2'
>>> eval(raw_input("enter: "))
enter: 6+19+2
27
```

##5.8 本章新函数

函数 | 描述
------ | ------
chr(n) | 当传入序号n时，返回n所代表的字符（0<=n<256)
ord(c) | 返回单个字符的int值