本文Scala使用的版本是2.11.8

##第10章 特质

###10.1 基本使用

特质可以同时拥有抽象方法和具体方法，而类可以实现多个特质。

```
import java.util.Date

trait AbsLogged {
    // 特质中未被实现的方法默认就是抽象的.
    def log(msg: String)
}

trait Logged extends AbsLogged {
    // 重写抽象方法, 此处为空实现
    override def log(msg: String) { }
}

trait ConsoleLogger extends Logged {
    override def log(msg: String) { println("ConsoleLogger: " + msg) }
}

trait FileLog extends Logged {
    override def log(msg: String) { println("FileLog: " + msg) }
}

trait TimeLog extends Logged {
    override def log(msg: String): Unit = {
        super.log(new Date() + " " + msg)
    }
}

trait ShortLogger extends Logged {
    val maxLength = 15
    override def log(msg: String): Unit = {
        super.log(
            if (msg.length < maxLength) msg else msg.substring(0, maxLength - 3) + "..."
        )
    }
}

// 如果需要的特质不止一个,可以用with关键字添加额外的特质
class SavingsAccount extends Logged with Cloneable with Serializable {
    def withdraw(amount: Double): Unit = {
        if (amount > 10.0) log("Insufficient funds")
        else log("else")
    }
}

// 测试
object Test {
    def main(args: Array[String]) {
        // 如下可以混入不同的特质
        val acct1 = new SavingsAccount with ConsoleLogger
        acct1.log("acct1")
        val acct2 = new SavingsAccount with FileLog
        acct2.log("acct2")

        // 可以叠加多个特质, 一般从最后一个开始被处理
        val acct3 = new SavingsAccount with ConsoleLogger with TimeLog with ShortLogger
        acct3.withdraw(11)
        val acct4 = new SavingsAccount with ConsoleLogger with ShortLogger with TimeLog
        acct4.withdraw(11)
    }
}

// 运行结果
ConsoleLogger: acct1
FileLog: acct2
ConsoleLogger: Tue Nov 22 07:10:15 CST 2016 Insufficient...
ConsoleLogger: Tue Nov 22 0...
```

###10.2 当做富接口使用的特质

特质可以包含大量工具方法，而这些方法可以依赖一些抽象方法来实现。

```
trait Logger {
    def log(msg: String)
    def info(msg: String) { log("INFO: " + msg) }
    def warn(msg: String) { log("WARN: " + msg) }
    def error(msg: String) { log("ERROR: " + msg) }
}
```

###10.3 特质中的字段

给出初始值的是具体字段；否则为抽象字段，子类必须提供该字段。
这些字段不是被子类继承，而是简单地被加到子类中。

###10.4 特质的构造顺序

和类一样，特质也可以有构造器，由字段的初始化和其他特质中的语句构成。

构造器以如下顺序执行：

  1. 首先调用超类的构造器。
  2. 特质构造器在超类构造器之后、类构造器之前执行。
  3. 特质由左到右被构造。
  4. 每个特质当中，父特质先被构造。
  5. 如果多个特质共用一个父特质，而这个父特质已经被构造，则不会再次构造。
  6. 所有特质构造完毕，子类被构造。

###10.5 初始化特质中的字段

特质不能有构造器参数。每个特质都有一个无参数的构造器。而且构造顺序的问题，在子类中初始化特质的字段，可能会有陷阱。

```
import java.io.PrintStream

trait Logger {
    def log(msg: String) {}
    def info(msg: String) { log("INFO: " + msg) }
    def warn(msg: String) { log("WARN: " + msg) }
    def error(msg: String) { log("ERROR: " + msg) }
}

trait FileLogger extends Logger {
    val fileName: String
    val out = new PrintStream(fileName)

    override def log(msg: String) { out.println(msg); out.flush() }
}

class SavingsAccount2 extends Logger {
    def withdraw(amount: Double): Unit = {
        if (amount > 10.0) log("Insufficient funds")
        else log("else")
    }
}

// 类的提前定义
class TestAccount extends {
    val fileName = "test.log"
} with SavingsAccount2 with FileLogger

object Test2 {

    def main(args: Array[String]) {
        // 特质的提前定义
        val acct = new {
            val fileName = "myapp.log"
        } with SavingsAccount2 with FileLogger
        acct.withdraw(11)

        // 类的提前定义
        val test = new TestAccount
        test.withdraw(1)
    }
}
```

另一种是在FileLogger构造器中使用懒值：

```
lazy val out = new PrintStream(fileName)
```

###10.6 扩展类的特质

```
// 特质可以扩展类
trait LoggedException extends Exception with Logger {
    def log() { log(getMessage) }
}

class UnhappyException extends LoggedException {
    override def getMessage = "arggh!"
}

// 如果类已经扩展了另一个类, 只要这个类是特质的超类的一个子类就可以
class UnhappyException2 extends IOException with LoggedException {
    override def getMessage = "UnhappyException2!"
}

object Test2 {

    def main(args: Array[String]) {

        val ex = new UnhappyException
        ex.log()

        val ex2 = new UnhappyException2
        ex2.log()
    }
}
```

###10.7 自身类型

当特质以如下代码开始定义时

```
this: 类型 =>
```

它便只能被混入指定类型的子类。

```
trait LoggedException2 extends Logged {
    this: Exception =>
        def log() { log(getMessage) }
}
```

下面这种特质可以被混入任何拥有getMessage方法的类。

```
trait LoggedException3 extends Logged {
    this: { def getMessage(): String } =>

    def log() { log(getMessage()) }
}
```

###10.8 习题解答

<font size =4 color=Blue>
1. java.awt.Rectangle类有两个很有用的方法translate和grow，但可惜的是像java.awt.geom.Ellipse2D这样的类中没有。在Scala中，你可以解决掉这个问题。定义一个RectangleLike特质，加入具体的translate和grow方法。提供任何你需要用来实现的抽象方法，以便你可以像如下代码这样混入该特质：


```
val egg = new java.awt.geom.Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
egg.translate(10, -10)
egg.grow(10, 20)
```

</font>

```
package com.zw.demo.tenth

import java.awt.geom.Ellipse2D

trait RectangleLike {
  this:Ellipse2D.Double =>
  def translate(dx : Int, dy : Int): Unit = {
    this.x += dx
    this.y += dy
  }
  def grow(h : Int, v : Int): Unit = {
    this.width = v
    this.height = h
  }
}

// 测试类
package com.zw.demo.tenth

object One {

  def main(args: Array[String]): Unit = {
    val egg = new java.awt.geom.Ellipse2D.Double(5, 10, 20, 30) with RectangleLike

    println("x = " + egg.getX + " y = " + egg.getY)
    egg.translate(10, -10)
    println("x = " + egg.getX + " y = " + egg.getY)

    println("w = " + egg.getWidth + " h = " + egg.getHeight)
    egg.grow(10, 21)
    println("w = " + egg.getWidth + " h = " + egg.getHeight)
  }
}

// 结果
x = 5.0 y = 10.0
x = 15.0 y = 0.0
w = 20.0 h = 30.0
w = 21.0 h = 10.0
```

<font size =4 color=Blue>
2. 通过把scala.math.Ordered[Point]混入java.awt.Point的方式，定义OrderedPoint类。按辞典编辑方式排序，也就是说，如果x<x'或者x=x'且y<y'则(x,y)<(x',y')

</font>

```
package com.zw.demo.tenth

import java.awt.Point

class OrderedPoint(
                  x:Int,
                  y:Int
                  ) extends Point(x:Int, y:Int) with Ordered[Point]{

  override def compare(that: Point): Int = {
    if (this.x <= that.x && this.y < that.y) -1
    else if (this.x == that.x && this.y == that.y) 0
    else 1
  }
}

// 测试类
package com.zw.demo.tenth

object Two {

  def main(args: Array[String]) {
    val arr : Array[OrderedPoint] = new Array[OrderedPoint](3)
    arr(0) = new OrderedPoint(4,5)
    arr(1) = new OrderedPoint(2,2)
    arr(2) = new OrderedPoint(4,6)
    val sortedArr = arr.sortWith(_ > _)
    sortedArr.foreach((point:OrderedPoint) => println("x = " + point.getX + " y = " + point.getY))
  }

}

// 结果
x = 4.0 y = 6.0
x = 4.0 y = 5.0
x = 2.0 y = 2.0
```

<font size =4 color=Blue>
3. 查看BitSet类，将它的所有超类和特质绘制成一张图。忽略类型参数（[...]中的所有内容）。然后给出该特质的线性化规格说明。
</font>

![类图](http://img.blog.csdn.net/20161028154547050)

Sorted、SetLike、SortedSetLike、Set、SortedSet、BitSetLike、BitSet

<font size =4 color=Blue>
4. 提供一个CryptoLogger类，将日志消息以凯撒密码加密。缺省情况下密钥为3，不过使用者可以重写它。提供缺省密钥和-3作为密钥时的使用示例。
</font>

```
package com.zw.demo.tenth

trait CryptoLogger {
  def crypto(str : String, key : Int = 3) : String = {
    for ( i <- str) yield
      if (key >= 0) (97 + ((i - 97 + key)%26)).toChar
      else (97 + ((i - 97 + 26 + key)%26)).toChar
  }
}

// 测试类
package com.zw.demo.tenth

/**
  * Created by zhangws on 16/10/28.
  */
object Three {

  def main(args: Array[String]) {
    val log = new CryptoLogger {}

    val plain = "abcdef"
    println("明文为：" + plain)
    println("加密后为：" + log.crypto(plain))
    println("加密后为：" + log.crypto(plain, -3))
  }
}

// 结果
明文为：abcdef
加密后为：defghi
加密后为：xyzabc
```

<font size =4 color=Blue>
5. JavaBeans规范里有一种提法叫做属性变更监听器（property change listener），这是bean用来通知其属性变更的标准方式。PropertyChangeSupport类对于任何想要支持属性变更监听器的bean而言是个便捷的超类。但可惜已有其他超类的类——比如JComponent——必须重新实现相应的方法。将PropertyChangeSupport重新实现为一个特质，然后将它混入到java.awt.Point类中。
</font>

```
package com.zw.demo.tenth

import java.beans.PropertyChangeSupport

trait PropertyChange {
  val propertyChangeSupport : PropertyChangeSupport
}

// 测试
package com.zw.demo.tenth

import java.awt.Point
import java.beans.{PropertyChangeSupport, PropertyChangeEvent, PropertyChangeListener}

object Five {

  def main(args: Array[String]) {
    val p = new Point() with PropertyChange {
      val propertyChangeSupport = new PropertyChangeSupport(this)
      propertyChangeSupport.addPropertyChangeListener(new PropertyChangeListener {
        override def propertyChange(evt: PropertyChangeEvent): Unit = {
          println(evt.getPropertyName
            + ": oldValue = " + evt.getOldValue
            + " newValue = " + evt.getNewValue)
        }
      })
    }
    val newX : Int = 20
    p.propertyChangeSupport.firePropertyChange("x", p.getX, newX)
    p.move(newX, 30)
  }
}

// 结果
x: oldValue = 0.0 newValue = 20
```

<font size =4 color=Blue>
6. 在Java AWT类库中,我们有一个Container类，一个可以用于各种组件的Component子类。举例来说，Button是一个Component,但Panel是Container。这是一个运转中的组合模式。Swing有JComponent和JContainer，但如果你仔细看的话，你会发现一些奇怪的细节。尽管把其他组件添加到比如JButton中毫无意义,JComponent依然扩展自Container。Swing的设计者们理想情况下应该会更倾向于图10-4中的设计。但在Java中那是不可能的。请解释这是为什么？Scala中如何用特质来设计出这样的效果?
</font>

Java只能单继承,JContainer不能同时继承自Container和JComponent。Scala可以通过特质解决这个问题.

<font size =4 color=Blue>
7. 做一个你自己的关于特质的继承层级，要求体现出叠加在一起的特质、具体的和抽象的方法，以及具体的和抽象的字段。
</font>

```
package com.zw.demo.tenth

trait Fly{
  def fly(){
    println("flying")
  }

  def flywithnowing()
}

trait Walk{
  def walk(){
    println("walk")
  }
}

class Bird{
  var name:String = _
}

class BlueBird extends Bird with Fly with Walk{
  def flywithnowing() {
    println("BlueBird flywithnowing")
  }
}

object Seven {

  def main(args: Array[String]) {
    val b = new BlueBird()
    b.walk()
    b.flywithnowing()
    b.fly()
  }
}
```

<font size =4 color=Blue>
8. 在java.io类库中，你可以通过BufferedInputStream修饰器来给输入流增加缓冲机制。用特质来重新实现缓冲。简单起见，重写read方法。
</font>

```
import java.io.{FileInputStream, InputStream}

trait Buffering {
    this: InputStream =>

    val BUF_SIZE: Int = 5
    val buf: Array[Byte] = new Array[Byte](BUF_SIZE)
    var bufsize: Int = 0 // 缓存数据大小
    var pos: Int = 0 // 当前位置

    override def read(): Int = {
        if (pos >= bufsize) { // 读取数据
            bufsize = this.read(buf, 0, BUF_SIZE)
            if (bufsize <= 0) return bufsize
            pos = 0
        }
        pos += 1 // 移位
        buf(pos - 1) // 返回数据
    }
}

object Eight {

    def main(args: Array[String]) {
        val f = new FileInputStream("myapp.log") with Buffering
        for (i <- 1 to 30) println(f.read())
    }
}
```

<font size =4 color=Blue>
9. 使用本章的日志生成器特质，给前一个练习中的方案增加日志功能，要求体现出缓冲的效果。
</font>

```
import java.io.{FileInputStream, InputStream}

trait Logger {
    def log(msg: String)
}

trait PrintLogger extends Logger {
    def log(msg: String) = println(msg)
}

trait Buffering {
    this: InputStream with Logger =>

    val BUF_SIZE: Int = 5
    val buf: Array[Byte] = new Array[Byte](BUF_SIZE)
    var bufsize: Int = 0 // 缓存数据大小
    var pos: Int = 0 // 当前位置

    override def read(): Int = {
        if (pos >= bufsize) { // 读取数据
            bufsize = this.read(buf, 0, BUF_SIZE)
            if (bufsize <= 0) return bufsize
            log("buffered %d bytes: %s".format(bufsize, buf.mkString(", ")))
            pos = 0
        }
        pos += 1 // 移位
        buf(pos - 1) // 返回数据
    }
}

object Eight {

    def main(args: Array[String]) {
        val f = new FileInputStream("myapp.log") with Buffering with PrintLogger
        for (i <- 1 to 20) println(f.read())
    }
}

// 结果
buffered 5 bytes: 73, 110, 115, 117, 102
73
110
115
117
102
buffered 5 bytes: 102, 105, 99, 105, 101
102
105
99
105
101
buffered 5 bytes: 110, 116, 32, 102, 117
110
116
32
102
117
buffered 4 bytes: 110, 100, 115, 10, 117
110
100
115
10
-1
```

<font size =4 color=Blue>
10. 实现一个IterableInputStream类，扩展java.io.InputStream并混入Iterable[Byte]特质。
</font>

```
import java.io.{FileInputStream, InputStream}

/**
  * Created by zhangws on 16/10/28.
  */
trait IterableInputStream extends InputStream with Iterable[Byte] {

    class InputStreamIterator(outer: IterableInputStream) extends Iterator[Byte] {
        def hasNext: Boolean = outer.available() > 0

        def next: Byte = outer.read().toByte
    }

    override def iterator: Iterator[Byte] = new InputStreamIterator(this)
}

object Ten extends App {
    val fis = new FileInputStream("myapp.log") with IterableInputStream
    val it = fis.iterator
    while (it.hasNext)
        println(it.next())
}
fis.close()
```

###10.9 参考

[《快学scala》习题解答-第十章-特质](http://vernonzheng.com/2015/02/02/%E3%80%8A%E5%BF%AB%E5%AD%A6scala%E3%80%8B%E4%B9%A0%E9%A2%98%E8%A7%A3%E7%AD%94-%E7%AC%AC%E5%8D%81%E7%AB%A0-%E7%89%B9%E8%B4%A8/)

##第11章 操作符

###11.1 基本使用

yield在Scala中是保留字，如果访问Java中的同名方法，可以用反引号

```
Thread.`yield`()
```

中置操作符

```
1 to 10 相当于 1.to(10)
1 -> 10 相当于 1.->(10)
```

使用操作符的名称定义方法就可以实现定义操作符

```
class Fraction(n: Int, d: Int) {
    val num: Int = n
    val den: Int = d
    def *(other: Fraction) = new Fraction(num * other.num, den * other.den)

    override def toString = {
        "num = " + num + "; den = " + den
    }
}

object StudyDemo extends App {
    val f1 = new Fraction(2, 3)
    val f2 = new Fraction(4, 5)
    val f3 = f1 * f2
    println(f3)
}
```

一元操作符

```
# 后置操作符
1 toString 等同于 1.toString()

# 前置操作符（例如+、-、!、~）
-a 转换为unary_操作符的方法调用 a.unary_-
```

赋值操作符

```
a 操作符= b 等同于 a = a 操作符 b
例如：a += b 等同于 a = a + b
```

<font color=red>
1. <=、>=和!=不是赋值操作符；
2. 以=开头的操作符不是赋值操作符（==、===、=/=等）；
3. 如果a有一个名为操作符=的方法，那么该方法会被直接调用。
</font>

结合性

除了以下操作符，其他都是左结合的

	1. 以冒号（:）结尾的操作符；
	2. 赋值操作符。

例如

```
2 :: Nil 等同于Nil.::(2)
```

###11.2 优先级

除复制操作符外，优先级由操作符的首字符决定。

<table>
<tr><td>最高优先级：除以下字符外的操作符字符</td></tr>
<tr><td>* / %</td></tr>
<tr><td>+ -</td></tr>
<tr><td>:</td></tr>
<tr><td>&lt; &gt;</td></tr>
<tr><td>! =</td></tr>
<tr><td>&</td></tr>
<tr><td>^</td></tr>
<tr><td>|</td></tr>
<tr><td>非操作符</td></tr>
<tr><td>最低优先级：赋值操作符</td></tr>
</table>
<br>
后置操作符优先级低于中置操作符：

```
a 中置操作符 b 后置操作符
等价于
(a 中置操作符 b) 后置操作符
```

###11.3 apply和update方法

```
f(arg1, arg2, ...)
```

如果f不是函数或方法，等同于

```
f.apply(arg1, arg2, ...)
```

表达式

```
f(arg1, arg2, ...) = value
```

等同于

```
f.update(arg1, arg2, ..., value)
```

apply常被用在伴生对象中，用来构造对象而不用显示地使用new。

```
class Fraction(n: Int, d: Int) {
    ...
}

object Fraction {
    def apply(n: Int, d: Int) = new Fraction(n, d)
}

val result = Fraction(3, 4) * Fraction(2, 5
```

###11.4 提取器

所谓提取器就是一个带有unapply方法的对象。可以当做伴生对象中apply方法的反向操作。

unapply接受一个对象，然后从中提取值。

```
val author = "Cay Horstmann"
val Name(first, last) = author // 调用Name.unapply(author)

object Name {
    def unapply(input: String) = {
        val pos = input.indexOf(" ")
        if (pos == -1) None
        else Some((input.substring(0, pos), input(substring(pos + 1))
    }
}
```

每一个样例类都自动具备apply和unapply方法。

```
case class Currency(value: Double, unit: String)
```

带单个参数的提取器

```
object Number {
    def unapply(input: String): Option[Int] = {
        try {
            Some(Integer.parseInt(input.trim))
        } catch {
            case ex: NumberFormatException => None
        }
    }
}

val Number(n) = "1223"
```

无参数的提取器

```
object IsCompound {
    def unapply(input: String) = input.contains(" ")
}

author match {
    // 作者Peter van der也能匹配
    case Name(first, last @ IsCompound()) => ...
    case Name(first, last) => ...
}
```

unapplySeq方法，提取任意长度的值得序列。

```
object Name {
    def unapplySeq(input: String): Option[Seq[String]] = {
        if (input.trim == "") None
        else Some(input.trim.split("\\s+"))
    }
}

// 这样就可以匹配任意数量的变量了

author math {
    case Name(first, last) => ...
    case Name(first, middle, last) => ...
    case Name(first, "van", "der", last) => ...
}
```

###11.5 习题解答

<font size =4 color=Blue>
1. 根据优先级规则，3+4->5和3->4+5是如何被求值的？
</font>

```
scala> 3 + 4 -> 5
res0: (Int, Int) = (7,5)

scala> 3 -> 4 + 5
<console>:12: error: type mismatch;
 found   : Int(5)
 required: String
       3 -> 4 + 5
                ^
```

<font size =4 color=Blue>
2. BitInt类有一个pow方法，但没有用操作符字符。Scala类库的设计者为什么没有选用**（像Fortran那样）或者^（像Pascal那样）作为乘方操作符呢？
</font>

因为优先级问题，在scala中*优先于^，但数学中乘方优先于乘法。所以没有提供^作为乘方的操作符。

<font size =4 color=Blue>
3. 实现Fraction类，支持 + - * / 操作。支持约分，例如将15/-6变成-5/2。除以最大公约数，像这样：

```
class Fraction(n: Int, d: Int) {
  private val num: Int = if (d == 0) 1 else n * sign(d) /gcd(n, d);
  private val den: Int = if (d == 0) 0 else d * sign(d) /gcd(n, d);
  override def toString = num + "/" + den
  def sign(a: Int) = if (a > 0) 1 else if (a < 0) -1 else 0
  def gcd(a: Int, b: Int): Int = if (b == 0) abs(a) else gcd(b, a % b)
  ...
}
```
</font>


```
import scala.math.abs

class Fraction(n: Int, d: Int) {
    private val num: Int = if (d == 0) 1 else n * sign(d) / gcd(n, d)
    private val den: Int = if (d == 0) 0 else d * sign(d) / gcd(n, d)

    override def toString = num + "/" + den

    // 正负号
    def sign(a: Int): Int = if (a > 0) 1 else if (a < 0) -1 else 0

    // 最大公约数
    def gcd(a: Int, b: Int): Int = if (b == 0) abs(a) else gcd(b, a % b)

    def +(other: Fraction): Fraction = {
        Fraction((this.num * other.den) + (other.num * this.den), this.den * other.den)
    }

    def -(other: Fraction): Fraction = {
        Fraction((this.num * other.den) - (other.num * this.den), this.den * other.den)
    }

    def *(other: Fraction): Fraction = {
        Fraction(this.num * other.num, this.den * other.den)
    }

    def /(other: Fraction): Fraction = {
        Fraction(this.num * other.den, this.den * other.num)
    }
}

object Fraction {
    def apply(n: Int, d: Int) = new Fraction(n, d)
}

object Three extends App {
    val f = new Fraction(15, -6)
    val p = new Fraction(20, 60)
    println(f)
    println(p)
    println(f + p)
    println(f - p)
    println(f * p)
    println(f / p)
}

// 结果
-5/2
1/3
-13/6
-17/6
-5/6
-15/2
```

<font size =4 color=Blue>
4. 实现一个Money类，加入美元和美分字段。提供+、-操作符以及比较操作符==和<。举例来说Money(1, 75) + Money(0, 50) == Money(2, 25)应为true。你应该同时提供*和/操作符吗？为什么？
</font>

金额的乘除没有实际意义。

```
class Money(val dollar: Int, val cent: Int) {
    def +(other: Money): Money = {
        Money(this.dollar + other.dollar, this.cent + other.cent)
    }

    def -(other: Money): Money = {
        Money(0, this.toCent - other.toCent)
    }

    def <(other: Money): Boolean = this.dollar < other.dollar || (this.dollar == other.dollar && this.cent < other.cent)

    def ==(other: Money): Boolean = this.dollar == other.dollar && this.cent == other.cent

    private def toCent = this.dollar * 100 + this.cent

    override def toString = { "dollar = " + this.dollar + " cent = " + this.cent}
}

object Money {
    def apply(dollar: Int, cent: Int) = {
        val d = dollar + cent / 100
        new Money(d, cent % 100)
    }
}

object Four extends App {
    val m1 = Money(1, 200)
    val m2 = Money(2, 2)
    println(m1 + m2)
    println(m1 - m2)
    println(m2 - m1)
    println(m1 == m2)
    println(m1 < m2)
    println(Money(1, 75) + Money(0, 50))
    println(Money(1, 75) + Money(0, 50) == Money(2, 25))
}

// 结果
dollar = 5 cent = 2
dollar = 0 cent = 98
dollar = 0 cent = -98
false
false
dollar = 2 cent = 25
true
```

<font size =4 color=Blue>
5. 提供操作符用于构造HTML表格。例如：

```
Table() | "Java" | "Scala" || "Gosling" | "Odersky" || "JVM" | "JVM, .NET"
```

应产出

```
<table><tr><td>Java</td><td>Scala</td></tr><tr><td>Gosling...
```
</font>

```
class Table {
    var s: String = ""

    def |(str: String): Table = {
        Table(this.s + "<td>" + str + "</td>")
    }

    def ||(str: String): Table = {
        Table(this.s + "</tr><tr><td>" + str + "</td>")
    }

    override def toString: String = {
        "<table><tr>" + this.s + "</tr></table>"
    }
}

object Table {
    def apply(): Table = {
        new Table
    }

    def apply(str: String): Table = {
        val t = new Table
        t.s = str
        t
    }
}

object Five extends App {
    println(Table() | "Java" | "Scala" || "Gosling" | "Odersky" || "JVM" | "JVM,.NET")
}

// 结果
<table><tr><td>Java</td><td>Scala</td></tr><tr><td>Gosling</td><td>Odersky</td></tr><tr><td>JVM</td><td>JVM,.NET</td></tr></table>
```

<font size =4 color=Blue>
6. 提供一个ASCIIArt类，其对象包含类似这样的图形：

<pre><code>
 /\_/\
( ' ' )
(  -  )
 | | |
(__|__)
</code></pre>

提供将两个ASCIIArt图形横向或纵向结合的操作符。选用适当优先级的操作符命名。纵向结合的实例：

<pre>
 /\\_/\     -----
( ' ' )  / Hello \
(  -  ) <  Scala |
 | | |   \ Coder /
(__|__)    -----
</pre>
</font>

```
import scala.collection.mutable.ArrayBuffer

class ASCIIArt(str: String) {
    val arr: ArrayBuffer[ArrayBuffer[String]] = new ArrayBuffer[ArrayBuffer[String]]()

    if (str != null && !str.trim.equals("")) {
        str.split("[\r\n]+").foreach(
            line => {
                val s = new ArrayBuffer[String]()
                s += line
                arr += s
            }
        )
    }

    def this() {
        this("")
    }

    /**
      * 横向结合
      * @param other
      * @return
      */
    def +(other: ASCIIArt): ASCIIArt = {
        val art = new ASCIIArt()
        // 获取最大行数
        val length = if (this.arr.length >= other.arr.length) this.arr.length else other.arr.length
        for (i <- 0 until length) {
            val s = new ArrayBuffer[String]()
            // 获取this中的行数据, 行数不足,返回空行
            val thisArr: ArrayBuffer[String] = if (i < this.arr.length) this.arr(i) else new ArrayBuffer[String]()
            // 获取other中的行数据, 行数不足,返回空行
            val otherArr: ArrayBuffer[String] = if (i < other.arr.length) other.arr(i) else new ArrayBuffer[String]()
            // 连接this
            thisArr.foreach(s += _)
            // 连接other
            otherArr.foreach(s += _)
            art.arr += s
        }
        art
    }

    /**
      * 纵向结合
      * @param other
      * @return
      */
    def *(other: ASCIIArt): ASCIIArt = {
        val art = new ASCIIArt()
        this.arr.foreach(art.arr += _)
        other.arr.foreach(art.arr += _)
        art
    }

    override def toString = {
        var ss: String = ""
        arr.foreach(ss += _.mkString(" ") + "\n")
        ss
    }
}

object Six extends App {
    // stripMargin: "|"符号后面保持原样
    val a = new ASCIIArt(
        """ /\_/\
          |( ' ' )
          |(  -  )
          | | | |
          |(__|__)
          | """.stripMargin)
    val b = new ASCIIArt(
        """   -----
          | / Hello \
          |<  Scala |
          | \ Coder /
          |   -----
          | """.stripMargin)
    println(a + b * b)
    println((a + b) * b)
    println(a * b)
}
```

<font size =4 color=Blue>
7. 实现一个BigSequence类，将64个bit的序列打包在一个Long值中。提供apply和update操作来获取和设置某个具体的bit。
</font>

```
class BigSequence(private var value: Long = 0) {

    def update(bit: Int, state: Int) = {
        if (state == 1) value |= (state & 1L) << bit % 64
        else value &= ~(1L << bit % 64)
    }

    def apply(bit: Int): Int = if (((value >> bit % 64) & 1L) > 0) 1 else 0

    override def toString = "%64s".format(value.toBinaryString).replace(" ", "0")
}

object Seven {
    def main(args: Array[String]) {
        val x = new BigSequence()
        x(5) = 1
        x(63) = 1
        x(64) = 1

        println(x(5))
        x(64) = 0
        println(x)
    }
}
```

<font size =4 color=Blue>
8. 提供一个Matrix类——你可以选择需要的是一个2x2的矩阵，任意大小的正方形矩阵，或是m x n的矩阵。支持+和\*操作。\*操作应同样适用于单值，例如mat\*2。单个元素可以通过mat(row, col)得到。
</font>

```
class Matrix(val m: Int, val n: Int) {
    private val value = Array.ofDim[Double](m, n)

    def update(x: Int, y: Int, v: Double) = value(x)(y) = v

    def apply(x: Int, y: Int) = value(x)(y)

    def +(other: Matrix): Matrix = {
        require(n == other.n)
        require(m == other.m)

        val res = new Matrix(m, n)
        for (i <- 0 until m; j <- 0 until n) {
            res(i, j) = this (i, j) + other(i, j)
        }
        res
    }

    def -(other: Matrix): Matrix = {
        this + other * (-1)
    }

    def *(factor: Double): Matrix = {
        val res = new Matrix(m, n)
        for (i <- 0 until m; j <- 0 until n) {
            res(i, j) = this (i, j) * factor
        }
        res
    }

    private def prod(other: Matrix, i: Int, j: Int) = {
        (for (k <- 0 until n) yield value(i)(k) * other.value(j)(k)).sum
    }

    def *(other: Matrix) = {
        require(n == other.m)

        val res = new Matrix(m, other.n)
        for (i <- 0 until m; j <- 0 until other.n) {
            res(i, j) = prod(other, i, j)
        }
        res
    }

    override def toString = value.map(_.mkString(" ")).mkString("\n")
}

object Eight extends App {
    val x = new Matrix(2, 2)
    x(0, 0) = 1
    x(0, 1) = 2
    x(1, 0) = 3
    x(1, 1) = 4
    println(x)
    println()
    println(x * 2)
    println()
    println(x * 2 - x)
    println()
    println((x * 2) * (x * 3))
}

// 结果
1.0 2.0
3.0 4.0

2.0 4.0
6.0 8.0

1.0 2.0
3.0 4.0

30.0 66.0
66.0 150.0
```

<font size =4 color=Blue>
9. 为RichFile类定义unapply操作，提取文件路径、名称和扩展名。举例来说，文件/home/cay/readme.txt的路径为/home/cay，名称为readme，扩展名为txt。
</font>

```
class RichFile1(val path: String) {}

object RichFile1 {
    def apply(path: String): RichFile1 = {
        new RichFile1(path)
    }

    def unapply(richFile1: RichFile1) = {
        if (richFile1.path == null) {
            None
        } else {
            val reg = "([/\\w+]+)/(\\w+)\\.(\\w+)".r
            val reg(r1, r2, r3) = richFile1.path
            Some((r1, r2, r3))
        }
    }
}

object Nine {
    def main(args: Array[String]) {
        val richFile1 = RichFile1("/home/cay/readme.txt")
        val RichFile1(r1, r2, r3) = richFile1
        println(r1)
        println(r2)
        println(r3)

    }
}
```

<font size =4 color=Blue>
10. 为RichFile类定义一个unapplySeq，提取所有路径段。举例来说，对于/home/cay/readme.txt，你应该产出三个路径段的序列：home、cay和readme.txt。
</font>

```
class RichFile2(val path: String) {}

object RichFile2 {
    def apply(path: String): RichFile2 = {
        new RichFile2(path)
    }

    def unapplySeq(richFile2: RichFile2): Option[Seq[String]] = {
        if (richFile2.path == null) {
            None
        } else {
            if (richFile2.path.startsWith("/")) {
                Some(richFile2.path.substring(1).split("/"))
            } else {
                Some(richFile2.path.split("/"))
            }
        }
    }
}

object Ten {
    def main(args: Array[String]) {
        val richFile2 = RichFile2("/home/cay/readme.txt")

        val RichFile2(r@_*) = richFile2
        println(r)
    }
}
```