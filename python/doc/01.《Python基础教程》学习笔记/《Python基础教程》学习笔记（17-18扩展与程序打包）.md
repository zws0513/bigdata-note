#17. 扩展Python
##17.1 Jython和IronPython

Jython对应Java，IronPython对应C#和其他的.NET语言）。

**Java类（JythonTest.java）**

```
public class JythonTest {
    public void greeting() {
        System.out.println("Hello, world!");
    }
}

$ javac JythonTest.java

$ CLASSPATH=JythonTest.class jython

# 使用
import JythonTest
test = JythonTest()
test.greeting()
```

**C#类（IronPythonTest.cs）**

```
using System;
namespace FePyTest {
    public class IronPythonTest {
        public void greeting() {
            Console.WriteLine("Hello, world!");
        }
    }
}

# 编译
csc.exe /t:library IronPythonTest.cs

# 更新环境变量，然后使用
import clr
clr.AddReferenceToFile("IronPythonTest.dll")
import FePyTest
f = FePyTest.IronPythonTest()
f.greeting()
```

##17.2 编写C语言扩展
###17.2.1 SWIG

SWIG是简单包装盒接口生成器的缩写。

- 可通过它使用C语言或C++编写扩展代码；
- 它会自动包装那些代码，一遍能在一些高级语言中使用。

使用SWIG的过程很简单

1. 为代码写接口文件
2. 为了自动的产生C语言代码（包装代码）要在接口文件上运行SWIG
3. 把原来的C语言代码和产生的包装代码一起编译来产生共享库

**C语言版检测回文的函数（palindrome.c）**

```
#include <string.h>

int is_palindrome(char *text) {
    int i, n = strlen(text);
    for (i = 0; i <= n/2; ++i) {
        if (text[i] != text[n - i - i]) return 0;
    }
    return 1;
}
```

**Python版**

```
def is_palindrome(text):
    n = len(text)
    for i in range(len(text)//2):
        if text[i] != text[n - i - 1]:
            return False
    return True
```

**接口文件（palindrome.i）**

```
%module palindrome

%{
#include <string.h>
%}

extern int is_palindrome(char *text);
```

**运行SWIG**

```
swig -python palindrome.i

# 产生两个新文件：palindrome_wrap.c，另一个是palindrome.py
```

**编译、连接以及使用**

```
# Linux
$ gcc -c palindrome.c
$ gcc -I$PYTHON_HOME -I$PYTHON_HOME/Include -c palindrome_wrap.c
$ gcc -shared palindrome.o palindrome_wrap.o -o _palindrome.so

# Mac
$ gcc -dynamic -I$PYTHON_HOME/Include/python2.5 -c palindrome.c
$ gcc -dynamic -I$PYTHON_HOME/Include/python2.5 -c palindrome_wrap.c
$ gcc -dynamiclib palindrome_wrap.o palindrome.o -o _palindrome.so -Wl, -undefined, dynamic_lookup
```

使用

```
import _palindrome
```

###17.2.2 自己研究

**应用计数**

宏`Py_INCREF`和`Py_DECREF`分别来增加和减少一个对象的引用计数。

```
/* 必须首先被包含 */
#include <Python.h>

static PyObject *is_palindrome(PyObject *self, PyObject *args) {
    int i, n;
    const char *text;
    int result;
    if (!PyArg_ParseTuple(args, "s", &text)) {
        return NULL;
    }
    n = strlen(text);
    result = 1;
    for (i=0; i<=n/2; ++i) {
        if (text[i] != text[n-i-1]) {
            result = 0;
            break;
        }
    }
    return Py_BuildValue("i", result);
}

/* 方法/函数的列表 */
static PyMethodDef PalindromeMethods[] = {
    /* 名称、函数、参数类型和文档字符串 */
    {"is_palindrome", is_palindrome, METH_VARARGS, "Detect palindromes"}
    /* 一个列表结束的标记： */
    {NULL, NULL, 0, NULL}
};

/* 初始化模块的函数（名称很重要） */
PyMODINIT_FUNC initpalindrome() {
    Py_InitModule("palindrome", PalindromeMethods);
}
```

然后编译

```
$ gcc -I$PYTHON_HOME -I$PYTHON_HOME/Include -shared palindrome2.c -o palindrome.so
```

##17.3 本章的新函数

函数 | 描述
-------- | -------
Py_INCREF(obj) | 增加obj的引用计数
Py_DECREF(obj) | 减少obj的引用计数
PyArg_ParseTuple(args, fmt, ...) | 提取位置参数
PyArg_ParseTupleAndKeywords(args, kws, fmt, kwlist) | 提取位置和关健词参数
PyBuildValue(fmt, value) | 通过C语言值创建PyObject

#18. 程序打包
##18.1 Distutils基础

**简单的Distutils安装脚本（setup.py）**

```
from distutils.core import setup

setup(name='Hello',
      version='1.0',
      description='A simple example',
      author='z',
      py_modules=['hello'])
      
# 确保在同一目录下存在名为hello.py的模块文件
```

##18.2 打包
###18.2.1 建立存档文件

使用sdist命令（用于 “源代码发布” ）

```
python setup.py sdist
```

###18.2.2 创建Windows安装程序或RPM包

```
python setup.py bdist --formats=wininst
```

##18.3 编译扩展

假设已经在当前目录中放置了源文件palindrome2.c，下面的setup.py脚本可以用于编译（和安装）：

```
from distutils.core import setup, Extension

setup(name='palindrome',
      version='1.0',
      ext_modules = [
          Extension('palindrome', ['palindrome2.c'])
      ])
```

如果只想在当前目录编译扩展，可以使用以下命令：

```
python setup.py build_ext --inplace
```

编译SWIG

```
from distutils.core import setup, Extension

setup(name='palindrome',
      version='1.0',
      ext_modules = [
          Extension('palindrome', ['palindrome.c',
                                   'palindrome.i'])
      ])
```

##18.4 使用py2exe创建可执行程序

py2exe作为Distutils的扩展，可用来创建可执行的Windows程序。

```
# hello.py
print 'Hello, world!'
raw_input('Press<enter>')

# 将hello.py放入空目录，然后创建setup.py
from distutils.core import setup
import py2exe

setup(console=['hello.py'])

# 运行脚本
python setup.py py2exe
```

##18.5 本章的新函数

函数 | 描述
------ | -------
distutils.core.setup(...) | 用setup.py脚本中的关键字参数配置Distutils