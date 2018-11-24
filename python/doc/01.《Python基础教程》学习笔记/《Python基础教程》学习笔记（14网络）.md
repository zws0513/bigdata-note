#14. 网络编程
##14.1 少数几个网络设计模块
###14.1.1 socket模块

一个套接字就是一个socket模块中的socket类的实例。它的实例化需要3个参数：

- 第1个参数是地址族（默认：`socket.AF_INET`)；
- 第2个参数是流（默认：`socket.SOCK_STREAM`）或数据报（`socket.SOCK_DGRAM`）套接字；
- 第3个参数是使用的协议（默认0）。

服务器端套接字使用bind方法后，再调用listen方法去监听给定的地址。一个地址就是一个格式为`(host, port)`的元组。

客户端套接字使用connect方法连接到服务器在connect方法中使用的地址与bind方法中的地址相同。

listen方法只有一个参数，即服务器未处理的连接的长度（即允许排队等待的连接数目，这些连接在停止接收之前等待接收）。

服务器端开始监听后，使用accept方法接受客户端的连接。该方法会阻塞直到客户端连接，然后返回`(client, address)`的元组，client是一个客户端套接字，address是一个前面解释过的地址。

套接字有两个方法：send和recv（用于接收），用于传输数据。

```
# 服务器端
import socket

s = socket.socket()

host = socket.gethostname()
port = 1234
s.bind((host, port))

s.listen(5)
while True:
    c, addr = s.accept()
    print 'Got connection from', addr
    c.send('Thank you for connecting')
    c.close()
```

```
# 客户端
import socket

s = socket.socket()

host = socket.gethostname()
port = 1234

s.connect((host, port))
print s.recv(1024)
```

###14.1.2 urllib和urllib2模块

**打开远程文件**

```
from urllib import urlopen
webpage = urlopen('http://python.org')

# localfile = urlopen('file:C:\\somefile.txt')
```

urlopen返回的类文件对象支持close、read、readline、readlines方法，当然也支持迭代。

下例：想要提取前面打开的Python页中 “About” 连接的（相对）URL

```
import re
text = webpage.read()
m = re.search('<a href="([^"]+)" .*?>about</a>', text, re.IGNORECASE
m.group(1)
```

**获取远程文件**

如果想下载文件并在本地存储一个文件的副本，可以使用urlretrieve，它返回一个元组`(filename, headers)`，filename是本地文件的名字（由urllib自动创建），headers包含一些远程文件的信息。

```
# 第二个参数为下载后的文件名，如果没指定，则会存放在临时的位置，用open函数可以打开它。
# 要清理临时文件，可以调用urlcleanup函数。
urlretrieve('http://www.python.org', 'C:\\python_webpage.html')
```

###14.1.3 其他模块

模块 | 描述
------ | -------
asynchat | asyncore的增强版本
asyncore | 异步套接字处理程序
cgi | 基本的CGI支持
Cookie | Cookie对象操作，主要用于服务器
cookielib | 客户端cookie支持
email | E-mail消息支持（包括MIME）
ftplib | FTP客户端模块
gopherlib | gopher客户端模块
httplib | HTTP客户端模块
imaplib | IMAP4客户端模块
mailbox | 读取集中邮箱的格式
mailcap | 通过mailcap文件访问MIME配置
mhlib | 访问MH邮箱
nntplib | NNTP客户端
poplib | POP客户端
robotparser | 支持解析Web服务器的robot文件
SimpleXMLRPCServer | 一个简单的XML-RPC服务器
smtpd | SMTP服务器
smtplib | SMTP客户端
telnetlib | Telnet客户端
urlparse | 支持解释URL
xmlrpclib | XML-RPC的客户端支持

##14.2 SocketServer

SocketServer模块是标准库中很多服务器框架的基础，这些服务器框架包括BaseHTTPServer、SimpleHTTPServer、CGIHTTPServer、SimpleXMLRPCServer和DocXMLRPCServer。

SocketServer包含4个基本的类：

- 针对TCP套接字流的TCPServer；
- 针对UDP数据报套接字的UDPServer；
- 以及针对性不强的UnixStreamServer和UnixDatagramServer。

```
# 一个基于SocketServer的服务器
from SocketServer import TCPServer, StreamRequestHandler

class Handler(StreamRequestHandler):

    def handle(self):
        addr = self.request.getpeername()
        print 'Got connection from', addr
        # self.rfile用户读，self.wfile用户写入
        self.wfile.write('Thank you for connecting')
server = TCPServer(('', 1234), Handler)
server.serve_forever()
```

##14.3 多连接

以上讨论的都是同步的。下面同时处理多个连接。

有3中主要的方法能实现这个目的：分叉（forking）、线程（threading）以及异步IO（asynchronous I/O)。

###14.3.1 使用SocketServer进行分叉和县城处理

**使用了分叉技术的服务器**

```
from SocketServer import TCPServer, ForkingMixIn, StreamRequestHandler

class Server(ForkingMixIn, TCPServer): pass

class Handler(StreamRequestHandler):

    def handle(self):
        addr = self.request.getpeername()
        print 'Got connection from', addr
        self.wfile.write('Thank you for connecting')

server = Server(('', 1234), Handler)
server.serve_forever()
```

**使用线程处理的服务器**

```
from SocketServer import TCPServer, ThreadingMixIn, StreamRequestHandler

class Server(ThreadingMinIn, TCPServer): pass

class Handler(StreamRequestHandler):

    def handle(self):
       addr = self.request.getpeername()
       print 'Got connection from', addr
       self.wfile.write('Thank you for connecting')
       
server = Server(('', 1234), Handler)
server.serve_forever()
```

###14.3.2 带有select和poll的异步I/O

select函数需要3个序列作为它的必选参数，此外还有一个可选的以秒为单位的超时时间作为第4个参数。

示例：使用了select的简单服务器

```
import socket, select

s = socket.socket()

host = socket.gethostname()
port = 1234
s.bind((host, port))

s.listen(5)
inputs = [s]
while True:
    rs, ws, es = select.select(inputs, [], [])
    for r in rs:
        if r is s:
            c, addr = s.accept()
            print 'Got connection from', addr
            inputs.append(c)
        else:
            try:
                data = r.recv(1024)
                disconnected = not data
            except socket.error:
                disconnected = True
                
            if disconnected:
                print r.getpeername(), 'disconnected'
                inputs.remove(r)
            else:
                print data
```

select模块中的polling事件常量

事件名 | 描述
----- | -------
POLLIN | 读取来自文件描述符的数据
POLLPRI | 读取来自文件描述符的紧急数据
POLLOUT | 文件描述符已经准备好数据，写入时不会发生阻塞
POLLERR | 与文件描述符有关的错误情况
POLLHUP | 挂起，连接丢失
POLLNVAL | 无效请求，连接没有打开

示例：使用poll的简单服务器

```
import socket, select

s = socket.socket()
host = socket.gethostname()
port = 1234
s.bind((host, port))

# 从文件描述符到套接字对象的映射
fdmap = {s.fileno(): s}

s.listen(5)
p = select.poll()
# 注册一个文件描述符（或带有fileno方法的对象）
p.register(s)
while True:
    events = p.poll()
    # fd是文件描述符，event则告诉发生了什么，是一个位掩码（见上表）
    for fd, event in events:
        if fd in fdmap:
            c, addr = s.accept()
            print 'Got connection from', addr
            p.register(c)
            fdmap[c.fileno()] = c
        elif event & select.POLLIN:
            data = fdmap[fd].recv(1024)
            if not data: # 没有数据——关闭连接
                print fdmap[fd].getpeername(), 'disconnected'
                # 移除注册的对象
                p.unregister(fd)
                del fdmap[fd]
            else:
                print data
```

##14.4 Twisted

Twisted是一个事件驱动的Python网络矿建。支持Web服务器、客户机、SSH2、SMTP、POP3、IMAP4、AIM、ICQ、IRC、MSN、Jabber、NNTP和DNS等等。

###14.4.1 下载并安装

[官网](http://twistedmatrix.com)点击下载。

```
# 运行Distutils脚本安装
python setup.py install
```

###14.4.2 编写Twisted服务器

**使用Twisted的简单服务器**

```
from twisted.internet from reactor
from twisted.internet.protocol import Protocol, Factory

class SimpleLogger(Protocol)

    # 得到一个连接后，会被调用
    def connectionMade(self):
        print 'Got connection from', self.transport.client
        
    # 丢失一个连接后，会被调用
    def connectionLost(self, reason):
        print self.transport.client, 'disconnected'
        
    # 通过dataReceived处理来自客户端的数据
    # 使用对象self.transport把数据发回到客户端，
    # 这个对象有一个write方法，也有一个包含客户机地址的client属性
    def dataReceived(self, data):
        print data
        
factory = Factory()
# 设置自定义协议
factory.protocol = SimpleLogger

reactor.listenTCP(1234, factory)
reactor.run()
```

**使用LineReceiver协议改进的记录服务器**

```
from twisted.internet import reactor
from twisted.internet.protocol import Factory
from twisted.protocols.basic import LineReceiver

class SimpleLogger(LineReceiver):

    def connectionMade(self):
        print 'Got connection from', self.transport.client
        
    def connectionLost(self, reason):
        print self.transport.client, 'disconnected'
        
    # 只要收到了一整行就调用事件处理程序lineReceived
    def lineReceived(self, line):
        print line
        
factory = Factory()
factory.protocol = SimpleLogger

reactor.listenTCP(1234, factory)
reactor.run()
```

##14.5 本章的新函数

函数 | 描述
------ | -------
urllib.urlopen(url[, data[, proxies]]) | 通过URL打开一个类文件对象
urllib.rulretrieve(url[, fname[, hook[, data]]]) | 通过URL下载一个文件
urllib.quote(string[, safe]) | 引用特定的URL字符
urllib.quote_plus(string[, safe]) | 和quote相同，但是将空格引用为+
urllib.unquote(string) | 和quote相反
urllib.unquote_plus(string) | 和quote_plus相反
urllib.urlencode(query[, doseq]) | 在CGI请求中使用的编码映射
select.select(iseq, oseq, eseq[, timeout]) | 找出准备好读/写的套接字
select.poll() | 为polling套接字创建一个poll对象
reactor.listenTCP(port, factory) | Twisted函数，监听连接
reactor.run() | Twisted函数，主服务器循环