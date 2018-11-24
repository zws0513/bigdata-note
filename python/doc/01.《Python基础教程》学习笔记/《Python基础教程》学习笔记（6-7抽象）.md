#6. 抽象
##6.1 函数

使用def定义函数

```
def fibs(num):
    result = [0, 1]
    for i in range(num - 2):
        result.append(result[-2] + result[-1])
    return result

print fibs(10)
```

###6.1.1 记录函数

如果在函数的开头写下字符串，它就会作为函数的一部分进行存储，这称为文档字符串。通过__doc__函数属性访问。

```
def fibs(num):
    '计算斐波那契数列'
    result = [0, 1]
    for i in range(num - 2):
        result.append(result[-2] + result[-1])
    return result

print fibs.__doc__
help(fibs)
print fibs(10)

// 结果
计算斐波那契数列
Help on function fibs in module __main__:

fibs(num)
    计算斐波那契数列

[0, 1, 1, 2, 3, 5, 8, 13, 21, 34]
```

##6.2 参数

字符串（以及数字和元组）是不可变的，即在函数内为参数赋值不会改变外部任何变量的值。

**关键字参数**

```
def hello(name, greeting):
    print '%s, %s' % (greeting, name)

hello(greeting='hello', name='world')
```

**参数默认值**

```
def hello(name="world", greeting="hello"):
    print '%s, %s' % (greeting, name)

hello(greeting='greeting')
```

**可变参数**

通过参数前加“*”，表示可变参数，结果会变为元组。

通过参数前加“**”，表示可变关键字参数，结果会变为字典。

```
def print_params(x, y, z=3, *pospar, **keypar):
    print x, y, z
    print pospar
    print keypar

print_params(1, 2, 4, 5, 6, 7, foo=9, bar=10)

// 结果
1 2 4
(5, 6, 7)
{'foo': 9, 'bar': 10}
```

**反转过程**

```
>>> def addTest(x, y): return x+y
>>> params=(1, 2)
>>> print addTest(*params)
3
```

##6.5 作用域

```
x = 1


def change_global():
    global x
    x = x + 1

change_global()
print x

# 结果
2
```

##6.6 递归

###6.6.1 阶乘

**循环**

```
def factorial(n):
    result = n
    for i in range(1, n):
        result *= i
    return result

print factorial(5)
```

**递归**

```
def factorialRecursion(n):
    if n == 1:
        return 1
    else:
        return n * factorialRecursion(n - 1)

print factorialRecursion(4)
```

###6.6.2 幂

**循环**

```
def power(x, n):
    result = 1
    for i in range(n):
        result *= x
    return result

print power(2, 3)
```

**递归**

```
def powerRecursion(x, n):
    if n == 0:
        return 1
    else:
        return x * powerRecursion(x, n - 1)

print powerRecursion(3, 3)
```

###6.6.3 二元查找

```
def search(sequence, number, lower=0, upper=None):
    if upper is None:
        upper = len(sequence) - 1
    if lower == upper:
        assert number == sequence[upper]
        return upper
    else:
        middle = (lower + upper) // 2
        if number > sequence[middle]:
            return search(sequence, number, middle + 1, upper)
        else:
            return search(sequence, number, lower, middle)

seq = [34, 67, 8, 123, 4, 100, 95]
seq.sort()
print seq
print search(seq, 34)
print search(seq, 100)

# 结果
[4, 8, 34, 67, 95, 100, 123]
2
5
```

##6.7 本章新函数

函数 | 描述
----- | -------
map(func, seq [, seq, ...]) | 对序列中的每个元素应用函数
filter(func, seq) | 返回其函数为真的元素的列表
reduce(func, seq [, initial]) | 等同于func(func(func(seq[0], seq[1]), seq[2]), ...)
sum(seq) | 返回seq中所有元素的和
apply(func[, args[, kwargs]]) | 调用函数，可以提供参数

#7. 更加抽象
##7.1 创建类

```
class Person:

    def setName(self, name):
        self.name = name

    def getName(self):
        return self.name

    def greet(self):
        print "hello, world! I'm %s." % self.name

foo = Person()
bar = Person()
foo.setName('Luke Sky')
bar.setName('Ana Walker')
foo.greet()
bar.greet()

# 结果
hello, world! I'm Luke Sky.
hello, world! I'm Ana Walker.
```

<font color=red>
self参数事实上正是方法和函数的区别，方法将它们的第一个参数绑定到所属的实例上。
</font>

```
def func():
    print "I don't"

bar.greet = func

bar.greet()

# 结果
I don't
```

可以随意使用引用同一个方法的其他变量：

```
fooTest = foo.greet
fooTest()
# 结果
hello, world! I'm Luke Sky.
```

<font color=red>
没有严格意思上的私有化，可以使用单下划线定义内部数据，这样的名字不会被带星号的imports语句导入。
</font>

##7.2 继承

```
class Filter:
    def init(self):
        self.blocked = []

    def filter(self, sequence):
        return [x for x in sequence if x not in self.blocked]


class SPAMFilter(Filter):  # SPAMFilter是Filter的子类
    def init(self):  # 重写Filter超类中的init方法
        self.blocked = ['SPAM']


s = SPAMFilter()
s.init()
print s.filter(['SPAM', 'egg', 'SPAM', 'test'])

# 结果
['egg', 'test']
```

```
# 判断SPAMFilter是否是Filter的子类
print issubclass(SPAMFilter, Filter)

# 获取类的基类
print SPAMFilter.__bases__

# 检查对象是否是类的实例
s = SPAMFilter()
print isinstance(s, SPAMFilter)
print isinstance(s, Filter)

# 获取对象属于哪个类
print s.__class__
print type(s)

# 结果
True
(<class '__main__.Filter'>,)
True
True
<class '__main__.SPAMFilter'>
<class '__main__.SPAMFilter'>
```

##7.3 多重继承

```
class Calculator:
    def calculate(self, expression):
        self.value = eval(expression)


class Talker:
    def talk(self):
        print 'Hi, my value is', self.value


class TalkingCalculator(Calculator, Talker):
    pass

tc = TalkingCalculator()
tc.calculate('1+2*3')
tc.talk()


# 检查所需方法是否已经存在
print hasattr(tc, 'talk')
print hasattr(tc, 'fnord')

# 结果
Hi, my value is 7
True
False
```

##7.4 本章新函数

函数 | 描述
------ | -------
callable(object) | 确定对象是否可调用（比如函数或者方法）
getattr(object, name[, default]) | 确定特性的值，可以选择提供默认值
hasattr(object, name) | 确定对象是否有给定的特性
isinstance(object, class) | 确定对象是否是类的实例
issubclass(A, B) | 确定A是否为B的子类
random.choice(sequence) | 从非空序列中随机选择元素
setattr(object, name, value) | 设定对象的给定特性为value
type(object) | 返回对象的类型