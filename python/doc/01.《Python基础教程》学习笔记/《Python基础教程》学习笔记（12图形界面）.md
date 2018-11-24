#12. 图形界面
##12.1 丰富的平台

工具包 | 描述 | 网站
------ | ------- | -----
Tkinter | 使用Tk平台。很容易得到，半标准 | https://wiki.python.org/moin/TkInter
wxpython | 基于wxWindows。跨平台 | http://wxpython.org
PythonWin | 只能在Windows上使用，使用了本机的Windows GUI功能 | https://wiki.python.org/moin/PythonWin
JavaSwing | 只能用于Jython。使用本机上的Java GUI | http://docs.oracle.com/javase/tutorial/uiswing/
PyGTK | 使用GTK平台，在Linux上很流行 | http://www.pygtk.org
PyQt | 使用Qt平台，跨平台 | http://wiki.python.org/moin/PyQt

##12.2 下载和安装wxPython

注意：要下载对应Python版本的wxPython。

[下载页面](https://wxpython.org/download.php)

##12.3 创建示例GUI应用程序
###12.3.1 开始

```
import wx

app = wx.App()
app.MainLoop()
```

###12.3.2 窗口和组件

窗口（Window）也成为框架（Frame），它只是wx.Frame类的实例。wx框架中的部件都是有他们父部件作为构造函数的第一个参数创建的。如果没有父部件，使用None即可。

```
import wx

app = wx.App()
win = wx.Frame(None)
# 增加按钮
btn = wx.Button(win)
win.show()
app.MainLoop()
```

###12.3.3 标签、标题和位置

```
win = wx.Frame(None, title="Simple Editor", size=(410, 335))
loadButton = wx.Button(win, label='Open', pos=(225, 5), size=(80, 25))
saveButton = wx.Button(win, label='Save', pos=(315, 5), size=(80, 25))
filename = wx.TextCtrl(win, pos=(5, 5), size=(210, 25))
contents = wx.TextCtrl(win, pos=(5, 35), size=(390, 260),
                        style=wx.TE_MULTILINE | wx.HSCROLL)
```

###12.3.4 更智能的布局

```
import wx

app = wx.App()
win = wx.Frame(None, title="Simple Editor", size=(410, 335))
# 
bkg = wx.Panel(win)

loadButton = wx.Button(bkg, label='Open')
saveButton = wx.Button(bkg, label='Save')
filename = wx.TextCtrl(bkg)
contents = wx.TextCtrl(bkg, style=wx.TE_MULTILINE | wx.HSCROLL)

hbox = wx.BoxSizer()
# proportion：根据在窗口改变大小时所分配的空间设置比例
hbox.Add(filename, proportion=1, flag=wx.EXPAND)
hbox.Add(loadButton, proportion=0, flag=wx.LEFT, border=5)
hbox.Add(saveButton, proportion=0, flag=wx.LEFT, border=5)

vbox = wx.BoxSizer(wx.VERTICAL)
vbox.Add(hbox, proportion=0, flag=wx.EXPAND | wx.ALL, border=5)
# wx.EXPAND：确保组件会扩展到所分配的空间中
# wx.LEFT、wx.BOTTOM、wx.RIGHT、wx.ALL：决定边框参数应用于哪个边
# border：设置边缘宽度（间隔）
vbox.Add(contents, proportion=1, 
     flag=wx.EXPAND | wx.LEFT | wx.BOTTOM | wx.RIGHT, border=5)
bkg.SetSizer(vbox)
win.Show()

app.MainLoop()
```

###12.3.5 事件处理

```
# load函数被绑定
loadButton.Bind(wx.EVT_BUTTON, load)
```

###12.3.6 完成了的程序

```
def load(event):
    file = open(filename.GetValue())
    contents.SetValue(file.read())
    file.close()
    
def save(event):
    file = open(filename.GetValue(), 'w')
    file.write(contents.GetValue())
    file.close()
```

##12.4 其他GUI包

wxPython版本的示例

```
import wx

def hello(event):
    print "Hello, world!"
    
app = wx.App()

win = wx.Frame(None, title="Hello, wxPython!",
          size=(200, 100))
button = wx.Button(win, label="Hello")
button.Bind(wx.EVT_BUTTON, hello)
win.Show()
app.MainLoop()
```

###12.4.1 Tkinter

```
from Tkinter import *
def hello(): print "Hello, world!"
win = Tk()
win.title('Hello, Tkinter! ')
win.geometry('200x100')

btn = Button(win, text='Hello ', command=hello)
btn.pack(expand=YES, fill=BOTH)

mainloop()
```

###12.4.2 使用Jython和Swing

```
from javax.swing import *
import sys

def hello(event): print 'Hello, world! '
btn = JButton('Hello')
btn.actionPerformed = hello

win = JFrame('Hello, Swing!')
win.contentPane.add(btn)

# 因为关闭按钮在Java Swing中没有任何有用的默认行为
def closeHandler(event): sys.exit()
win.windowClosing = closeHandler

btn.size = win.size = 200, 100
win.show()

# 无须显式地进入主事件循环，因为它是和程序并行运行的（在不同的线程中）
```