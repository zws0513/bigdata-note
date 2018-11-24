#10. 模块相关

Python的标准安装包包括一组模块，称为标准库（standard library）。

##10.1 模块

###10.1.1 模块是程序

```
# hello.py
print "Hello, world!"

# 保存放在C:\python
# 告诉解释器在哪里寻找模块

>>> import sys
>>> sys.path.append('c:/python')

# 这样，解释器除了从默认的目录中寻找之外，还需要从目录c:\python中寻找模块
>>> import hello
Hello, world!
```

导入模块多次和导入一次的效果是一样的。如果坚持重新载入模块，可以使用内建的reload函数。

###10.1.2 模块用于定义

**在模块中定义函数**

```
# hello2.py
def hello():
    print "Hello, world
    
# 使用
import hello2
hello2.hello()
```

**在模块中增加测试代码**

为 “告知” 模块本身是作为程序运行还是导入到其他程序，需要使用`__name__`变量：

```
# hello4.py

def hello():
    print "Hello, world!"

def test():
    hello()

if __name__ == '__main__': test()
```

###10.1.3 让模块可用

**将模块放置在正确位置**

```
# 下面命令列出的路径都可以放置，但site-packages目录是最佳选择
>>> import sys, pprint
>>> pprint.pprint(sys.path)
```

**告诉编译器去哪里找**

除了编辑sys.path外，更通用的方法是设置环境变量`PYTHONPATH`

###10.1.4 包

当模块存储在文件中时（扩展名.py），包就是模块所在的目录。为了让Python将其作为包对待，它必须包含一个命名为`__init__py`的文件（模块）。如果将它作为普通模块导入的话，文件的内容就是包的内容。

```
vim constants/__init__.py

PI=3.14

# 别的地方引用
import constants
print constants.PI
```

##10.2 探究模块

###10.2.1 模块中有什么

**使用dir**

查看模块包含的内容，它会将对象（以及模块的所有函数、类、变量等）的所有特性列出。

```
# 导入模块
import copy

# 列表推导式是个包含dir(copy)中所有不以下划线开头的名字的列表。
[n for n in dir(copy)] if not n.startwith('_')]
```

**`__all__`变量**

这个变量包含一个列表，该列表与上一节的列表类似。

```
copy.__all__
```

它定义了模块的共有接口，在编写模块的时候，像设置`__all__`这样的技术是相当有用的。

```
__all__ = ["Error", "copy", "deepcopy"]
```

###10.2.2 用help获取帮助

使用help函数，获得帮助文本。

```
help(copy.copy)
```

###10.2.3 文档

[参考](http://python.org/doc)

###10.2.4 使用源代码

方案一：检查sys.path，然后自己找。

方案二：检查模块的`__file__`属性

##10.3 标准库

###10.3.1 sys

sys这个模块能够访问与Python解释器联系紧密的变量和函数。部分重要函数和变量如下：

函数/变量 | 描述
------ | --------
argv | 命令行参数，包括传递到Python解释器的参数，脚本名称
exit([arg]) | 退出当前的程序，可选参数为给定的返回值或错误信息
modules | 映射模块名字到载入模块的字典
path | 查找模块所在目录的目录名列表
platform | 类似sunos5或win32的平台标识符
stdin | 标准输入流——一个类文件（file-like）对象
stdout | 标准输出流
stderr | 标准错误流

###10.3.2 os

os模块提供了访问多个操作系统服务的功能。下表列出一些最有用的函数和变量。另外，os和它的子模块os.path还包含一些用于检查、构造、删除目录和文件的函数，以及一些处理路径的函数（例如，os.path.split和os.path.join让你在大部分情况下都可以忽略os.pathsep）。

函数/变量 | 描述
------ | --------
environ | 对环境变量进行映射
system(command) | 在子shell中执行操作系统命令
sep | 路径中的分隔符
pathsep | 分隔路径的分隔符
linesep | 行分隔符
urandom(n) | 返回n个字节的加密强随机数据

###10.3.3 fileinput

fileinput模块能够轻松地遍历文本文件的所有行。

函数/变量 | 描述
------ | --------
input([files[, inplace[, backup]]]) | 便于遍历多个输入流中的行
filename() | 返回当前文件的名称
lineno() | 返回当前（累计）的行数
filelineno() | 返回当前文件的行数
isfirstline() | 检查当前行是否是文件的第一行
isstdin() | 检查最后一行是否来自sys.stdin
nextfile() | 关闭当前文件，移动到下一个文件
close() | 关闭序列

为Python脚本添加行号

```
# numberlines.py

import fileinput

for line in fileinput.input(inplcae=True)
  line = line.rstrip()
  num = fileinput.lineno()
  print '%-40s # %2i' % (line, num)
```

###10.3.4 集合、堆和双端队列

**集合** 

Set类位于sets模块中。非重复、无序的序列。

**堆**

堆（heap）是优先队列的一种。使用优先队列能够以任意顺序增加对象，并且能在任何时间找到最小的元素，也就是说它比用于列表的min方法要有效率得多。下面是heapq模块中重要的函数：

函数 | 描述
----- | ------
heappush(heap, x) | 将x入堆
heappop(heap) | 将堆中最小的元素弹出
heapify(heap) | 将heap属性强制应用到任意一个列表，将其转换为合法的堆
heapreplace(heap, x) | 将堆中最小的元素弹出，同时将x入堆
nlargest(n, iter) | 返回iter中第n大的元素
nsmallest(n, iter) | 返回iter中第n小的元素

元素虽然不是严格排序的，但是也有规则：i位置处的元素总比`2*i`以及`2*i+1`位置处的元素小。这是底层堆算法的基础，而这个特性称为堆属性（heap property）。

**双端队列（以及其他集合类型）**

双端队列（Double-ended queue）在需要按照元素增加的顺序来移除元素时非常有用。它能够有效地在开头增加和弹出元素，这是在列表中无法实现的，除此之外，使用双端队列的好处还有：能够有效地旋转（rotate）元素。deque类型包含在collections模块。

###10.3.5 time

time模块所包含的函数能够实现以下功能：获得当前时间、操作时间和日期、从字符串读取时间以及格式化时间为字符串。日期可以用实数或者包含有9个整数的元组。元组意义如下：

索引 | 字段 | 值
---- | ---- | ----
0 | 年 | 比如2000等
1 | 月 | 范围1~12
2 | 日 | 范围1~31
3 | 时 | 范围0~23
4 | 分 | 范围0~59
5 | 秒 | 范围0~61（应付闰秒和双闰秒）
6 | 周 | 当周一为0时，范围0~6
7 | 儒历日 | 范围1~366
8 | 夏令日 | 0、1、-1

time的重要函数：

函数 | 描述
------ | -------
asctime([tuple]) | 将时间元组转换为字符串
localtime([secs]) | 将秒数转换为日期元组，以本地时间为准
mktime(tuple) | 将时间元组转换为本地时间
sleep(secs) | 休眠secs秒
strptime(string[, format]) | 将字符串解析为时间元组
time() | 当前时间（新纪元开始后的秒数，以UTC为准）

###10.3.6 random

random模块包括返回随机数的函数，可以用于模拟或者用于任何产出随机输出的程序。

如果需要真的随机数，应该使用os模块的urandom函数。random模块内的SystemRandom类也是基于同样功能。

函数 | 描述
----- | -------
random() | 返回`0 <= n < 1`之间的随机实数n，其中`0 < n <=1`
getrandbits(n) | 以长整型形式返回n个随机位
uniform(a, b) | 返回随机实数n，其中 `a <= n < b`
randrange([start], stop, [step]) | 返回range(start, stop, step)中的随机数
choice(seq) | 从序列seq中返回随机元素
shuffle(seq[, random]) | 原地指定序列seq
sample(seq, n) | 从序列seq中选择n个随机且独立的元素

示例一：

```
from random import *
from time import *

date1 = (2008, 1, 1, 0, 0, 0, -1, -1, -1)
time1 = mktime(date1)
date2 = (2009, 1, 1, 0, 0, 0, -1, -1, -1)
time2 = mktime(date2)

random_time = uniform(time1, time2)
print asctime(localtime(random_time))
```

###10.3.7 shelve

提供一个存储方案。shelve的open函数返回一个Shelf对象，可以用它来存储内容。只需要把它当做普通的字典来操作即可，在完成工作之后，调用close方法。

```
import shelve

s = shelve.open('test.dat')
s['x'] = ['a', 'b', 'c']
# 下面代码，d的添加会失败
# s['x'].append('d')
# s['x']

# 正确应该使用如下方法：
temp = s['x']
temp.append('d')
s['x'] = temp
```

###10.3.8 re

re模块包含对正则表达式的支持。

**正则表达式**

1. `.`号只能匹配一个字符（除换行符外的任何单个字符）。
2. `\`为转义字符
3. 字符集：使用`[]`括起来，例如`[a-zA-Z0-9]`，使用`^`反转字符集
4. 选择符（`|`）和子模式`()`：例如`'p(ython|erl)'`
5. 可选项（在子模式后面加上问号）和重复子模式：例如`r'(http://)?(www\.)?python\.org`，问号表示出现一次或根本不出现。
  - (pattern)*：允许模式重复0次或多次
  - (pattern)+：允许模式重复1次或多次
  - (pattern){m,n}：允许模式重复m~n次
6. 字符串的开始（`^`）和结尾（`$`） 
7. 所有的重复运算符都可以通过在其后面加上一个问号变成非贪婪版本，例如：`r'\*\*(.+?)\*\*'`

**re模块的内容**

re模块中一些重要的函数

函数 | 描述
-------- | --------
compile(pattern[, flags]) | 根据包含正则表达式的字符串创建模式对象
search(pattern, string[, flags]) | 在字符串中寻找模式
match(pattern, string[, flags]) | 在字符串的开始处匹配模式
split(pattern, string[, maxsplit=0]) | 根据模式的匹配项来分隔字符串
findall(pattern, string) | 列出字符串中模式的所有匹配项
sub(pat, repl, string[, count=0]) | 将字符串中所有pat的匹配项用repl替换
escape(string) | 将字符串中所有特殊正则表达式字符转义

**匹配对象和组**

对于re模块中那些能够对字符串进行模式匹配的函数而言，当能找到匹配项时，返回MatchObject对象。包含了哪个模式匹配了子字符串的哪部分的信息。——这些“部分”叫做组。

组就是放置在圆括号内的子模式。组的序号取决于它左侧的括号数。组0就是整个模式。

re匹配对象的一些方法：

方法 | 描述
--------- | ---------
group([group1, ...]) | 获取给定子模式（组）的匹配项
start([group]) | 返回给定组的匹配项的开始位置
end([group]) | 返回给定组的匹配项的结束位置（和分片一样，不包括组的结束位置）
span([group]) | 返回一个组的开始和结束位置


**作为替换的组号和函数**

示例：假设要把`'*something*'`用`<em>something</em>`替换掉：

```
emphasis_pattern = r'\*([^\*]+)\*'

# 或者用VERBOSE标志加注释，它允许在模式中添加空白。

emphasis_pattern = re.compile(r'''
          \*      # 开始的强调标签
          (       # 组开始
          [^\*]+  # 除了星号的所有字符
          )       # 组结束
          \*      # 结束的强调标签
          ''', re.VERBOSE)
          
re.sub(emphasis_pattern, r'<em>\1</em>', 'Hello, *world*!')

# 结果
'Hello, <em>world</em>!'
```

**找出Email的发信人**

```
# 示例一
# 匹配内容：From: Foo Fie <foo@bar.baz>
# find_sender.py
import fileinput, re
pat = re.compile('From: (.*) <.*?>$')
for line in fileinput.input():
  m = pat.match(line)
  if m: print m.group(1)

# 执行
$ python find_sender.py message.eml

# 示例二
# 列出所有Email地址
import fileinput, re
pat = re.compile(r'[a-z\-\.]+@[a-z\-\.]+', re.IGNORECASE)
addresses = set()
for line in fileinput.input():
  for address in pat.findall(line):
    addresses.add(address)
for address in sorted(addresses):
  print address
```

**模板系统示例**

模板是一种通过放入具体值从而得到某种已完成文本的文件。

示例：把所有`'[somethings]'`（字段）的匹配项替换为通用Python表达式计算出来的something结果

```
'The sum of 7 and 9 is [7 + 9].'
应该翻译成
'The sum of 7 and 9 is 16.'

同时，可以在字段内进行赋值
'[name="Mr. Gumby"]Hello, [name]'
应该翻译成
'Hello, Mr. Gumby'
```

代码如下

```
# templates.py


#!/usr/bin/python
# -*- coding: utf-8 -*-

import fileinput, re

# 匹配中括号里的字段
field_pat = re.compile(r'\[(.+?)\]')

# 我们将变量收集到这里
scope = {}

# 用于re.sub中
def replacement(match):
  code = match.group(1)
  try:
    # 如果字段可以求值，返回它：
    return str(eval(code, scope))
  except SyntaxError:
    # 否则执行相同作用域内的赋值语句......
    exec code in scope
    # ......返回空字符串
    return ''
# 将所有文本以一个字符串的形式获取
lines = []
for line in fileinput.input():
  lines.append(line)
text = ''.join(lines)

# 将field模式的所有匹配项都替换掉
print field_pat.sub(replacement, text)
```

###10.3.9 其他标准模块

- functools：能够通过部分参数来使用某个函数（部分求值），稍后再为剩下的参数提供数值。
- difflib：可以计算两个序列的相似程度。还能从一些序列中（可供选择的序列列表）找出和提供的原始序列“最像”的那个。可以用于创建简单的搜索程序。
- hashlib：可以通过字符串计算小“签名”。
- csv：处理CSV文件
- timeit、profile和trace：timeit（以及它的命令行脚本）是衡量代码片段运行时间的工具。它有很多神秘的功能，应该用它代替time模块进行性能测试。profile模块（和伴随模块pstats）可用于代码片段效率的全面分析。trace模块（和程序）可以提供总的分析（覆盖率），在写测试代码时很有用。
- datetime：支持特殊的日期和时间对象，比time的接口更直观。
- itertools：有很多工具用来创建和联合迭代器（或者其他可迭代对象），还包括实现以下功能的函数：将可迭代的对象链接起来、创建返回无限连续整数的迭代器（和range类似，但没有上限），从而通过重复访问可迭代对象进行循环等等。
- logging：输出日志文件。
- getopt和optparse：在UNIX中，命令行程序经常使用不同的选项或开关运行。getopt为解决这个问题的。optparse则更新、更强大。
- cmd：可以编写命令行解释器。可以自定义命令。

##10.4 新函数

函数 | 描述
----- | --------
dir(obj) | 返回按字母顺序排序的属性名称列表
help([obj]) | 
reload(module) | 

#11. 文件
##11.1 打开文件

open函数用来打开文件，语法如下：

```
open(name[, mode[, buffering]])
```

###11.1.1 文件模式

默认只读打开。

值 | 描述
------ | ------
'r' | 读模式
'w' | 写模式
'a' | 追加模式
'b' | 二进制模式（可添加到其他模式中使用）
'+' | 读/写模式（可添加到其他模式中使用）

###11.1.2 缓存

open函数的第三个参数（可选）控制文件的缓冲。有缓冲时，只有使用flush或close时才会更新硬盘上的数据。

值 | 描述
------ | -----
0或False | 无缓冲
1或True | 有缓冲
大于1的数字 | 缓冲区大小（字节）
-1或负数 | 默认的缓冲区大小

##11.2 基本文件方法

###11.2.1 读和写

```
>>> f = open('somefile.txt', 'w')
>>> f.write('Hello, ')
>>> f.write('World!')
>>> f.close()

>>> f = open('somefile.txt', 'r')
>>> f.read(4)  # 读取的字符数（字节）
'Hell'
>>> f.read()
'o, World!'
```

###11.2.2 管式输出

```
# somescript.py
import sys
text = sys.stdin.read()
words = text.split()
wordcount = len(words)
print 'Wordcount:' wordcount

# 执行
# cat somefile.txt | python somescript.py | sort
```

###11.2.3 读写行

函数 | 描述
----- | -------
readline([num]) | 读取单独一样，参数可选，指读取的字符（或字节）的最大值
readlines | 读取一个文件中的所有行并将其作为列表返回
writelines | 把字符串的列表（序列或可迭代的对象）写入文件（或流）

###11.2.4 关闭文件

使用close方法关闭文件。

```
# Open your file here
try:
    # Write data to your file
finally:
    file.close()

# 或者使用with语句打开文件并且将其赋值到变量上，
# 文件在语句结束后会被自动关闭，即使由于异常引起的结束也是如此。
with open("somefile.txt") as somefile:
    do_something(somefile)
```

##11.3 对文件内容进行迭代

```
def process(string):
    print 'Processing: ', string
```

###11.3.1 按字节处理

```
# 示例一：
f = open(filename)
char = f.read(1)
while char:
    process(char)
    char = f.read(1)
f.close()

# 示例二：
f = open(filename)
while True:
    char = f.read(1)
    if not char: break
    process(char)
f.close()
```

###11.3.2 按行操作

```
f = open(filename)
while True:
    line = f.readline()
    if not line: break
    process(line)
f.close()
```

###11.3.3 读取所有内容

```
# 示例一：
f = open(filename)
for char in f.read():
    process(char)
f.close()

# 示例二：
f = open(filename)
for line in f.readlines():
    process(line)
f.close()
```

###11.3.4 使用fileinput实现懒惰行迭代

```
import fileinput
for line in fileinput.input(filename):
    process(line)
```

###11.3.5 文件迭代器

```
f = open(filename)
for line in f:
    process(line)
f.close()

# 或者进一步简化
for line in open(filename):
    process(line)
```

sys.stdin也可以迭代

```
import sys
for line in sys.stdin
    process(line)
```

可以对文件迭代器执行和普通迭代器相同的操作

```
f = open('somefile.txt', 'w')
f.write('first line\n')
f.write('second line\n')
f.write('third line\n')
f.close()

# 转换成字符串列表，达到和readlines一样的效果
lines = list(open('somefile.txt'))
# 执行结果
>>> lines
['first line\n', 'second line\n', 'third line\n']

first, second, third = open('somefile.txt')
# 执行结果
>>> first
'first line\n'
>>> second
'second line\n'
>>> third
'third line\n'
```

##11.4 新函数

函数 | 描述
----- | -----
file(name[, mode[, buffering]]) | 打开一个文件并返回一个文件对象
open(name[, mode[, buffering]]) | file的别名；在打开文件时，使用open而不是file