#8. 异常

##8.1 基本操作

**抛出异常**

```
raise Exception('异常演示')

# 结果
Traceback (most recent call last):
  File "/xxx/07.python/project/demo/__init__.py", line 8, in <module>
    raise Exception('异常演示')
Exception: 异常演示
```

**内建的异常**

可以在exceptions模块找到，使用dir函数列出来。

```
>>> import exceptions
>>> dir(exceptions)
['ArithmeticError', 'AssertionError', ...]
```

类名 | 描述
------ | -------
Exception | 所有异常的基类
AttributeError | 特性引用或赋值失败时引发
IOError | 试图打开不存在文件（包括其他情况）时引发
IndexError | 使用序列中不存在的索引
KeyError | 使用映射中不存在的键
NameError | 找不到名字（变量）
SyntaxError | 代码为错误形式
TypeError | 内建操作或函数应用于错误类型的对象
ValueError | 内建操作或函数应用于正确类型的对象，但该对象使用不合适的值
ZeroDivisionError | 除法或模除操作的第二个参数为0

##8.2 自定义异常

```
class SomeCustomException(Exception): pass
```

##8.3 捕捉异常

使用try/except：

```
try:
    x = input("first: ")
    y = input("second: ")
    print x/y
except ZeroDivisionError, e:
    # 访问异常对象本身
    print e
except (TypeError, NameError):  # 同时捕捉多个异常
    # 向上抛出异常
    raise

# 结果1
first: 1
second: 0
The second number cant be zero!

# 结果2
first: 1
second: "how"
Traceback (most recent call last):
  File "xxx/__init__.py", line 11, in <module>
    print x/y
TypeError: unsupported operand type(s) for /: 'int' and 'str'

# 结果3
first: 1
second: j
Traceback (most recent call last):
  File "/xxx/__init__.py", line 10, in <module>
    y = input("second: ")
  File "<string>", line 1, in <module>
NameError: name 'j' is not defined
```

##8.4 else、finally

```
while True:
    try:
        x = input("first: ")
        y = input("second: ")
        print x / y
    except:
        print '捕捉所有异常'
    else:
        print '未发生异常时执行'
        break
    finally:
        print "总是执行"
        
# 结果
first: 1
second: 
捕捉所有异常
总是执行
first: 1
second: 2
0
未发生异常时执行
总是执行
```

##8.5 新函数

函数 | 描述
-------- | ------
warnings.filterwarnings(actiong, ...) | 用于过滤警告

#9. 魔法方法、属性和迭代器

##9.1 准备工作

**新式类**

赋值语句__metaclass__=type放在模块最开始，或者子类化object。

```
class NewStyle(object):
```

##9.2 构造方法

```
class FooBar:
    def __init__(self):
        self.somevar = 42

f = FooBar()
print f.somevar

# 结果
42
```

###9.2.1 重写构造方法

**调用未绑定的超类构造方法**

```
class Bird:
    def __init__(self):
        self.hungry = True

    def eat(self):
        if self.hungry:
            print 'Aaaah...'
            self.hungry = False
        else:
            print 'No, tks'


class SongBird(Bird):
    def __init__(self):
        Bird.__init__(self)
        self.sound = 'Squawk!'

    def sing(self):
        print self.sound

sb = SongBird()
sb.sing()
sb.eat()
sb.eat()

# 结果
Squawk!
Aaaah...
No, tks
```

**使用super函数**

```
__metaclass__ = type


class Bird:
    def __init__(self):
        self.hungry = True

    def eat(self):
        if self.hungry:
            print 'Aaaah...'
            self.hungry = False
        else:
            print 'No, tks'


class SongBird(Bird):
    def __init__(self):
        super(SongBird, self).__init__()
        self.sound = 'Squawk!'

    def sing(self):
        print self.sound

sb = SongBird()
sb.sing()
sb.eat()
sb.eat()

# 结果
Squawk!
Aaaah...
No, tks
```

##9.3 成员访问
###9.3.1 基本的序列和映射规则

序列和映射是对象的集合。

  1. \_\_len\_(self)：返回集合中所含项目的数量。
  2. \_\_getitem\_\_(self, key)：返回与所给键对应的值。
  3. \_\_setitem\_\_(self, key, value)：设置和key相关的value。只能为可以修改的对象定义这个方法。
  4. \_\_delitem\_\_(self, key)：对一部分对象使用del语句时被调用，同时必须删除和元素相关的键。

<font color=red>
注意：

  1. 对于序列，如果键是负数，从末尾开始计数。
  2. 如果键是不合适的类型，会引发TypeError异常。
  3. 如果序列是正确的类型，但超出范围，引发IndexError异常。
</font>

###9.3.1 子类化列表、字典和字符串

通过子类化超类，进行扩展。

```
class CounterList(list):
    def __init__(self, *args):
        super(CounterList, self).__init__(*args)
        self.counter = 0

    def __getitem__(self, index):
        self.counter += 1
        return super(CounterList, self).__getitem__(index)


cl = CounterList(range(10))
print cl
cl.reverse()
print cl
del cl[3:6]
print cl
print cl.counter
print cl[4] + cl[2]
print cl.counter

# 结果
[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
[9, 8, 7, 6, 5, 4, 3, 2, 1, 0]
[9, 8, 7, 3, 2, 1, 0]
0
9
2
```

##9.4 属性
###9.5.1 property函数

property函数有4个参数，分别为fget、fset、fdel和doc

```
class Rectangle:
    def __init__(self):
        self.width = 0
        self.height = 0

    def setSize(self, size):
        self.width, self.height = size

    def getSize(self):
        return self.width, self.height

    size = property(getSize, setSize)

r = Rectangle()
r.width = 10
r.height = 5
print r.size
r.size = 150, 100
print r.width

# 结果
(10, 5)
150
```

###9.5.2 静态方法和类成员方法

静态方法和类成员方法分别在创建时被装入Staticmethod类型和Classmethod类型的对象中。

```
class MyClass:

    # 可以使用@staticmethod修饰器
    def smeth():
        print "this is a static method"
    smeth = staticmethod(smeth)

    # 可以使用@classmethod修饰器
    def cmeth(cls):
        print 'this is a class method of', cls
    cmeth = classmethod(cmeth)

MyClass.smeth()
MyClass.cmeth()

# 结果
this is a static method
this is a class method of <class '__main__.MyClass'>
```

###9.5.3 \_\_getattr\_\_、\_\_setattr\_\_

1. \_\_getattribute\_\_(self, name)：当特性name被访问时自动被调用（只能在新式类中使用）。
2. \_\_getattr\_\_(self, name)：当特性name被访问且对象没有相应的特性时被自动调用。
3. \_\_setattr\_\_(self, name, value)：当试图给特性name赋值时会被自动调用。
4. \_\_delattr\_\_(self, name)：当试图删除特性name时被自动调用。

##9.5 迭代器
###9.5.1 迭代器规则

\_\_iter\_\_方法返回一个迭代器（iterator），如果next方法被调用，但迭代器没有值可以返回，就会引发一个StopIteration异常。

```
class Fibs:
    def __init__(self):
        self.a = 0
        self.b = 1

    def next(self):
        self.a, self.b = self.b, self.a + self.b
        return self.a

    def __iter__(self):
        return self

fibs = Fibs()

for f in fibs:
    if f > 1000:
        print f
        break

// 结果
1597
```

###9.5.2 从迭代器得到序列

示例：使用list构造方法显式地将迭代器转化为列表

```
class TestIterator:
    value = 0

    def next(self):
        self.value += 1
        if self.value > 10:
            raise StopIteration
        return self.value

    def __iter__(self):
        return self

ti = TestIterator()
print list(ti)

// 结果
[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
```

##9.5 生成器

生成器是一种用普通的函数语法定义的迭代器。

###9.5.1 创建生成器

任何包含yield语句的函数称为生成器。每次产生值（使用yield语句），函数就会被冻结：即函数停在那点等待被激活。函数被激活后就从停止的那点开始执行。

```
def flattern(nested):
    for sublist in nested:
        for element in sublist:
            yield element

nested = [[1, 2], [3, 4], [5]]
for num in flattern(nested):
    print num

print list(flattern(nested))

// 结果
1
2
3
4
5
[1, 2, 3, 4, 5]
```

###9.5.2 递归生成器

```
def flattern(nested):
    try:
        for sublist in nested:
            for element in flattern(sublist):
                yield element
    except TypeError:
        yield nested

print list(flattern([[[1], 2], 3, 4, [5, [6, 7]], 8]))

// 结果
[1, 2, 3, 4, 5, 6, 7, 8]
```

###9.5.3 生成器方法

生成器和“外部世界”进行交流的渠道，要注意以下两点：

  1. 外部作用域访问生成器的send方法，就像访问next方法一样，只不过前者使用一个参数（要发送的“消息”——任意对象）。
  2. 在内部则挂起生成器，yield现在作为表达式而不是语句使用，换句话说，当生成器重新运行的时候，yield方法返回一个值，也就是外部通过send方法发送的值。如果next方法被使用，那么yield方法返回None。

```
def repeater(value):
    while True:
        new = (yield value)
        if new is not None:
            value = new

r = repeater(43)
print r.next()
print r.send("Hello, world!")

// 结果
43
Hello, world!
```

生成器还有其他两个方法：

  1. throw方法用于在生成器内引发一个异常。
  2. close方法用于停止生成器。

###9.5.4 模拟生成器

使用普通的函数模拟生成器。

```
def flattern(nested):
    result = []
    try:
        try: nested + ' '
        except TypeError: pass
        else: raise TypeError
        for sublist in nested:
            for element in flattern(sublist):
                result.append(element)
    except TypeError:
        result.append(nested)
    return result

print list(flattern([[[1], 2], 3, 4, [5, [6, 7]], 8]))

// 结果
[1, 2, 3, 4, 5, 6, 7, 8]
```

##9.6 新函数

函数 | 描述
------ | -----
iter(obj) | 从一个可迭代的对象得到迭代器
property(fget, fset, fdel, doc) | 返回一个属性，所有的参数都是可选的
super(class, obj) | 返回一个类的超类的绑定实例