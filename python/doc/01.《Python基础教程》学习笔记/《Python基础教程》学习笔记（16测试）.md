#16. 测试
##16.1 测试的4步

1. 指出需要的新特性。可以记录下来，然后为其编写一个测试。
2. 编写特性的概要代码，这样程序就可以运行而没有任何语法等方面的错误，但是测试会失败。这样就能确定测试可以失败。
3. 为特性的概要编写虚设代码（dummy code），能满足测试要求就行。不用准确地实现功能，只要保证测试可以通过即可。
4. 现在重写（或者重构，Refactor）代码，这样它就会做自己应该做的事，从而保证测试一直成功。

##16.2 测试工具
###16.2.1 doctest

```
# my_math.py

def square(x):
    '''
    求数字的平方，并返回结果
    
    >>> square(2)
    4
    >>> square(3)
    9
    '''
    return x*x
    
if __name__ == '__main__':
    import doctest, my_math
    doctest.testmod(my_math)
```

运行下面命令，可以看到详细信息（含测试信息）

```
python my_math.py -v
```

###16.2.2 unittest

unittest是基于Java的流行测试框架JUnit。

**模块（my_math.py）**

```
def product(x, y):
    return x*y
```

**测试（test_my_math.py）**

```
import unittest, my_math

class ProductTestCase(unittest.TestCase):

    def testIntegers(self):
        for x in xrange(-10, 10):
            for y in xrange(-10, 10):
                p = my_math.product(x, y)
                self.failUnless(p == x*y, 'Integer multiplication failed')
                
    def testFloats(self):
        for x in xrange(-10, 10):
            for y in xrange(-10, 10):
                x = x / 10.0
                y = y / 10.0
                p = my_math.product(x, y)
                self.failUnless(p == x*y, 'Float multiplication failed')
                
if __name__ == '__main__': unittest.main()
```

**一些有用的TestCase方法**

方法 | 描述
------ | --------
assert\_(expr[, msg]) | 如果表达式为假则失败
failUnless(expr[, msg]) | 同assert
assertEqual(x, y[, msg]) | 如果两个值不同则失败，在回溯中打印两个值
failUnlessEqual(x, y[, msg]) | 同assertEqual
assertNotEqual(x, y[, msg]) | 和assertEqual相反
failIfEqual(x, y[, msg]) | 同assertNotEqual
assertAlmostEqual(x, y[, places[, msg]]) | 类似assertEqual，但对于float值来说，与assertEqual不完全相同
failUnlessAlmostEqual(x, y[, places[, msg]]) | 同assertAlmostEqual
assertNotAlmostEqual(x, y[, places[, msg]]) | 和assertAlmostEqual相反
failIfAlmostEqual(x, y[, msg]) | 同assertNotAlmostEqual
assertRaises(exc, callable, ...) | 除非在*使用可选参数）调用时callable引发exc异常否则失败
failUnlessRaises(exc, callable, ...) | 同assertRaises
failIf(expr[, msg]) | 与assert\_相反
fail([msg]) | 无条件失败

##16.3 单元测试以外的内容
###16.3.1 使用PyChecker和Pylint检查源代码

PyCheck（http://pychecker.sf.net)是检查Python源代码、寻找提供的参数不满足函数要求等错误的工具。

PyLint（http://www.logilab.org/project/pylint）支持大多数PyChecker拥有的特性，以及相当多的其他功能（比如变量名称是否符合命名规范、是否遵循编码标准等）。

```
pychecker file1.py file2.py ...

pylint module
```

针对前面例子，使用subprocess模块调用外部检查模块

```
import unittest, my_math
from subprocess import Popen, PIPE

class ProductTestCase(unittest.TestCase):

    # 其他测试
    
    def testWithPyChecker(self):
        cmd = 'pychecker', '-Q', my_math.__file__.rstrip('c')
        pychecker = Popen(cmd, stdout=PIPE, stderr=PIPE)
        self.assertEqual(pychecker.stdout.read(), '')
        
    def testWithPyLint(self):
        cmd = 'pylint', '-rn', 'my_math'
        pylint = Popen(cmd, stdout=PIPE, stderr=PIPE)
        self.assertEqual(pylint.stdout.read(), '')
        
if __name__ = '__main__': unittest.main()
```

###16.3.2 分析

标准库中有一个叫profile的分析模块（还有一个C语言版本，叫hotshot）。

```
import profile
from my_math import product
profile.run('product(1, 2)')
```

这样会打印包括各个函数和方法调用的次数，以及每个函数所花费的时间。第二个参数是文件名（例如'my_math.profile'），那个结果就会保存到文件中。

之后可以用pstats模块检查分析结果：

```
import pstats
p = pstats.Stats('my_math.profile')
```

##16.4 本章的新函数

函数 | 描述
------ | ------
doctest.testmod(module) | 检查文档字符串中的例子（可以带有多个参数）
unittest.main() | 在当前模块中运行单元测试
profile.run(stmt[, filename]) | 执行并分析语句。可选择将结果存入filename文件中