#15. Python和万维网
##15.1 屏幕抓取
###15.1.1 Tidy和XHTML解析

**Tidy是什么**

Tidy（http://tidy.sf.net)是用来修复不规范且随意的HTML的工具。

**获取Tidy库**

Tidy可以官网下载，此外，还应该下载Python包装。uTidyLib可以从http://utidylib.berlios.de上获取，而mxTidy可以在http://egenix.com/products/python/mxExperimental/mxTidy上下载。

**在Python中使用命令行Tidy**

```
from subprocess import Popen, PIPE

text = open('messy.html').read()
tidy = Popen('tidy', stdin=PIPE, stdout=PIPE, stderr=PIPE)

tidy.stdin.write(text)
tidy.stdin.close(0

print tidy.stdout.read()
```

**为什么用XHTML**

- XHTML对于显示关闭所有元素要求更加严格。
- XHTML是XML的一种，所以可以使用XML的工具。

**使用HTMLParser**

使用HTMLParser的意思就是继承它，并且对`handle_starttag`或`handle_data`等事件处理方法进行覆盖。

HTMLParser的回调方法

回调方法 | 何时调用
------ | ------
handle_starttag(tag, attrs) | 找到开始标签时。attrs是`(名称, 值)`对的序列
handle_startendtag(tag, attrs) | 使用空标签时（默认分开处理开始和结束标签）
handle_endtag(tag) | 找到结束标签时
handle_data(data) | 使用文本数据时
handle_charref(ref) | 当使用`&#ref;`形式的实体引用时
handle_entityref(name) | 当使用`&name;`形式的实体引用时
handle_comment(data) | 注释时调用。只对注释内容调用
handle_decl(decl) | 申请<!...>形式时
handle_pi(data) | 处理指令时调用

示例：使用HTMLParser模块的屏幕抓取程序

```
from urllib import urlopen
from HTMLParser import HTMLParser

class Scraper(HTMLParser):

    in_h3 = False
    in_link = False
    
    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if tag == 'h3':
            self.in_h3 = True
            
        if tag == 'a' and 'href' in attrs:
            self.in_link = True
            self.chunks = []
            self.url = attrs['href']
            
    def handle_data(self, data):
        if self.in_link:
            self.chunks.append(data)
            
    def handle_endtag(self, tag):
        if tag == 'h3':
            self.in_h3 = False
        if tag == 'a':
            if self.in_h3 and self.in_link:
                print '%s (%s)' % (''.join(self.chunks), self.url)
                self.in_link = False
                
text = urlopen('http://python.org/community/jobs').read()
parser = Scraper()
parser.feed(text)
parse.close()
```

###15.1.2 Beautiful Soup

用来解析和检查经常在网上看到的那类乱七八糟而且不规范的HTML。

使用Beautiful Soup的屏幕抓取程序

```
from urllib import urlopen
from BeautifulSoup import BeautifulSoup

text = urlopen('http://python.org/community/jobs').read()
soup = BeautifulSoup(text)

jobs = set()
for header in soup('h3'):
    links = header('a', 'reference')
    if not links: continue
    link = links[0]
    jobs.add('%s (%s)' % (link.string, link['href']))
    
print '\n'.join(sorted(jobs, key=lambda s: s.lower()))
```

如果针对RSS feed进行分析可以使用另外一个工具叫 [Scrape 'N' Feed](http://crummy.com/software/ScrapeNFeed)

##15.2 使用CGI创建动态网页
###15.2.1 准备网络服务器

- 将脚本放在通过网络可以访问的目录中（例如cgi-bin）
- 把脚本文件扩展名改为`.cgi`

###15.2.2 加入Pound Bang行

必须第一行

```
#!/usr/bin/python
```

**Windows系统**

```
#!C:\Python22\python.exe
```

###15.2.3 设置文件许可

```
chmod 755 somescript.cgi
```

###15.2.4 简单的CGI脚本

```
#!/usr/bin/env python

print 'Content-type: text/plain'
print #打印空行，以结束首部

print 'Hello, world!'
```

###15.2.5 使用cgitb调试

Python 2.2的标准库中增加了叫做cgitb（用于CGI回溯）的模块。导入它并且调用它的enable函数，就能得到包含出错信息的十分有用的网页。

```
#!/usr/bin/env python

import cgitb; cgitb.enable()
print 'Content-type: text/plain'
print
print 1/0

print 'Hello, world!'
```

###15.2.6 使用cgi模块

```
#!/usr/bin/env python

import cgi
# 获取表单
form = cgi.FieldStorage()
name = form.getvalue('name', 'world')
print 'Content-type: text/plain'
print 

print 'Hello, %s!' % name
```

###15.2.7 简单的表单

示例：带有HTML表单的问候脚本（simple3.cgi)

```
#!/usr/bin/env python

import cgi
# 获取表单
form = cgi.FieldStorage()
name = form.getvalue('name', 'world')
print """Content-type: text/plain

<html>
  <head>
    <title>Greeting Page</title>
  </head>
  <body>
    <h1>Hello, %s!</h1>
    
    <form action='simple3.cgi'>
      Change name <input type='text' name='name' />
      <input type='submit' />
    </form>
  </body>
</html>
""" % name
```

##15.3 mod_python

它是Apache网络服务器的扩展（[官网](http://modpython.org)）。它可以让Python解释器直接成为Apache的一部分。

- 允许使用mod_python解释器运行CGI脚本
- 允许用HTML以及Python代码混合编程创建可执行网页，或者Python服务器页面
- 发布处理程序，允许使用URL调用Python函数

###15.3.1 安装mod_python

**前提条件**

1. Python 2 (2.6 and up) or Python 3 (3.3 and up).
2. Apache 2.2 or later. Apache 2.4 is highly recommended over 2.2.

**在UNIX上安装**

1. 下载源码，编译
  
  ```
  ./configure --with-apxs=/usr/local/apache/bin/apxs
  
  make
  
  make install
  ```

**配置Apache**

```
vim httpd.conf

# UNIX
LoadModule python_module libexec/mod_python.so

# 测试
<Directory /some/directory/htdocs/test>
    AddHandler mod_python .py
    PythonHandler mptest
    PythonDebug On
</Directory>
```

[参考](http://modpython.org/live/current/doc-html/installation.html#configuring-apache)

###15.3.2 CGI处理程序

把下面代码放在CGI脚本所在目录中的`.htaccess`文件内：

```
SetHandler mod_python
PythonHandler mod_python.cgihandler

# 开启调试信息
PythonDebug On
```

###15.3.3 PSP

PSP文档是HTML以及Python代码的混合。

设置Apache支持PSP页面（`.htaccess`）：

```
AddHandler mod_python .psp
PythonHandler mod_python.psp
```

PSP标签有两类：一类用于语句，另外一类用于表达式。

```
<%
from random import choice
adjectives = ['beautiful', 'cruel']
%>
<html>
  <head>
    <title>Hello</title>
  </head>
  <body>
    <p>Hello, <%=choice(adjectives)%> world. My name is Mr. Gumby.</p>
  </body>
</html>
```

###15.3.4 发布

开启发布处理程序，编辑（`.htaccess`）

```
AddHandler mod_python .py
PythonHandler mod_python.publisher
```

示例：使用mod_python发布处理程序进行验证

```
from sha import sha

__auth_realm__ = "A simple test"

def __auth__(req, user, pswd):
    return user == "gumby" and sha(pwsd).hexdigest() == \
    'xxxxxxxxxx'
    
def __access__(req, user):
    return True
    
def index(req, name="world"):
    return "<html>Hello, %s!</html> % name
```

##15.4 网络应用程序框架

名称 | 网站
------ | ------
Albatross | http://object-craft.com.au/projects/albatross
CherryPy | http://cherrypy.org
Django | http://djangoproject.com
Plone | http://plone.org
Pylons | http://pylonshq.com
Quixote | http://quixote.ca
Spyce | http://spyce.sf.net
TurboGears | http://turbogears.org
web.py | http://webpy.org
Webware | http://webwareforpython.org
Zope | http://zope.org

##15.5 本章的新函数

函数 | 描述
----- | -----
cgitb.enable() | 在CGI脚本中启用回溯