本文Scala使用的版本是2.11.8

##第21章 隐式转换和隐式参数

###21.1 基本概念

所谓隐式转换函数（implicit conversion function）指的是那种以implicit关键字声明的带有单个参数的函数。这样的函数将被自动应用，将值从一种类型转换为另一种类型。

```
class Fraction() {

    private var n: Int = 0
    private var m: Int = 0

    def this(n: Int, m: Int) {
        this
        this.n = n
        this.m = m
    }

    def *(that: Fraction): Fraction = {
        Fraction(this.n * that.n, this.m * that.m)
    }

    override def toString() = {
        this.n + " " + this.m
    }
}

object Fraction {
    def apply(n: Int, m: Int) = {
        new Fraction(n, m)
    }
}

object C21_1 {

    // 定义隐式转换函数
    implicit def int2Fraction(n: Int) = Fraction(n, 1)


    def main(args: Array[String]) {
        // // 将调用int2Fraction，将整数3转换成一个Fraction对象。
        val result = 3 * Fraction(4, 5)
        println(result)
    }
}
```

###21.2 利用隐式转换丰富现有类库的功能

```
val contents = new File("README").read
```

提供上面功能，需要：

```
class RichFile(val from: File) {
    def read = Source.fromFile(from.getPath).mkString
}

// 再提供一个隐式转换
implicit def file2RichFile(from: File) = new RichFile(from)
```

###21.3 引入隐式转换

Scala会考虑如下的隐式转换函数：

1. 位于源或目标类型伴生对象中的隐式函数。
2. 位于当前作用域可以以单个标识符指代的隐式函数。

比如把int2Fraction函数放入FractionConversions对象中，而这个对象位于com.zw.impatient包，如果想引入，就像这样：

```
import com.zw.impatient.FractionConversions._
```

也可以局部引入，或将某个特定隐式转换排除（见第7章）。

###21.4 隐式转换规则

隐式转换在如下三种各不相同的情况会被考虑：

- 当表达式的类型与预期的类型不同时
- 当对象访问一个不存在的成员时
- 当对象调用某个方法，而该方法的参数声明与传入参数不匹配时

另一方面，有三种情况编译器不会尝试使用隐式转换：

- 如果代码能够在不使用隐式转换的前提下通过编译，则不会使用隐式转换。
- 编译器不会尝试同时执行多个转换，比如`convert1(convert2(a))*b`。
- 存在二义性的转换是个错误。例如，如果`convert1(a)*b`和`convert2(a)*b`都是合法的，编译器将会报错。

<font color=red>
通过以下编译选项，可以查看编译器使用了哪些隐式转换。

```
scalac -Xprint:typer MyProg.scala
```
</font>

###21.5 隐式参数

函数或方法可以带有一个标记为implicit的参数列表。这种情况下，编译器将会查找缺省值，提供给该函数或方法。

```
// 以下两种引入都可以
//import com.zw.scala.chapter.twentyone.FrenchPunctuation._
import com.zw.scala.chapter.twentyone.FrenchPunctuation.quoteDelimiters

/**
  * Created by zhangws on 17/2/14.
  */
case class Delimiters(left: String, right: String)

object FrenchPunctuation {
    implicit val quoteDelimiters = Delimiters("<<", ">>")
}

object C21_5 {

    def quote(what: String)(implicit delims: Delimiters) =
        println(delims.left + what + delims.right)

    def main(args: Array[String]) {

        quote("Bonjour le monde")(Delimiters("<", ">"))

        // 这种情况下，编译器将会查找一个类型为Delimiters的隐式值。这必须是一个被声明为implicit的值
        quote("Bonjour le monde")
    }
}
```

编译器将会在如下两个地方查找这样的一个对象：

- 在当前作用域所有可以用单个标识符指代的满足类型要求的val和def。
- 与所要求类型相关联的类型的伴生对象。相关联的类型包括所要求类型本身，以及它的类型参数（如果它是一个参数化的类型的话）。

###21.6 利用隐式参数进行隐式转换

隐式的函数参数也可以被用做隐式转换。

```
// 泛型函数
def smaller[T](a: T, b: T) = if (a < b) a else b

// 应该写成下面形式，否则编译器不会接受这个函数
def smaller[T](a: T, b: T)(implicit order: T => Ordered[T])
    = if (a < b) a else b
```

注意order是一个带有单个参数的函数，被打上了implicit标签，并且有一个以单个标识符出现的名称。因此，它不仅是一个隐式参数，它还是一个隐式转换。

###21.7 上下文界定

类型参数可以有一个形式为`T: M`的上下文界定，其中M是另一个泛型类型。它要求作用域中存在一个类型为M[T]的隐式值。例如：

```
class Pair[T: Ordering]
```

要求存在一个类型为Ordering[T]的隐式值。该隐式值可以被用在该类的方法当中：

```
class Pair[T: Ordering](val first: T, val second: T) {
    def smaller(implicit ord: Ordering[T]) =
        if (ord.compare(first, second) < 0) first else second
}
```

如果`new Pair(40, 2)`，编译器将推断出我们需要一个Pair[Int]。由于Predef作用域中有一个类型为Ordering[Int]的隐式值，因此Int满足上下文界定。这个Ordering[Int]就成为该类的一个字段，被传入需要该值得方法当中。

如果愿意，也可以用Predef类的implicitly方法获取该值：

```
class Pair[T: Ordering](val first: T, val second: T) {
    def smaller = if (implicitly[Ordering[T]].compare(first, second) < 0) first else second
}
```

implicitly函数在Predef.scala中定义如下：

```
def implicitly[T](implicit e: T) = e
// 用于从冥界召唤隐式值
```

或者，也可以利用Ordered特质中定义的从Ordering到Ordered的隐式转换。一旦引入了这个转换，就可以使用关系操作符：

```
class Pair[T: Ordering](val first: T, val second: T) {
    def smaller = {
        import Ordered._;
        if (first < second) first else second
    }
}
```

重要的是可以随时实例化Pair[T]，只要满足存在类型为Ordering[T]的隐式值的条件即可。例如，想要Pair[Point]，则可以组织一个隐式的Ordering[Point]值：

```
implicit object PointOrdering extends Ordering[Point] {
    def compare(a: Point, b: Point) = ...
}
```

###21.8 类型证明

```
def firstLast[A, C](it: C)(implicit ev: C <:< Iterable[A]) = 
    (it.head, it.last)
```

`=:=、<:<和<%<`是带有隐式值的类，定义在Predef对象当中。例如，`<:<`从本质上讲就是：

```
abstract class <:<[-From, +To] extends Function1[From, To]

object <:< {
    implicit def conforms[A] = new (A <:< A) { def apply(x: A) = x }
}
```

假定编辑器需要处理约束`implicit ev: String <:< AnyRef`。它会在伴生对象中查找类型为String <:< AnyRef的隐式对象。因此如下对象：

```
<:<.conforms[String]
```

可以被当做`String <:< AnyRef`的实例使用。

我们把ev称做 “类型证明对象” ——它的存在证明了如下事实：以本例来说，String是AnyRef的子类型。

这里的类型证明对象是恒等函数（即永远返回参数原值的函数）。恒等函数是必需的原因如下：

<pre><code>
def firstLast[A, C](it: C)<font color=red>(implicit ev: C <:< Iterable[A])</font> = 
    (it.head, it.last)
</code></pre>

编译器实际上并不知道C是一个Iterable[A]——`<:<`并不是语言特性，而只是一个类。因此，像it.head和it.last这样的调用并不合法。当ev是一个带有单个参数的函数，因此也是一个从C到Iterable[A]的隐式转换。编译器将会应用这个隐式转换，计算ev(it).head和ev(it).last。

###21.9 @implicitNotFound注解

@implicitNotFound注解告诉编译器在不能构造出带有该注解的类型的参数时给出错误提示。例如：

```
@implicitNotFound(msg = "Cannot prove that ${From} <:< ${To}."
abstract class <:<[-From, +To] extends Function1[From, To]

// 如下调用
firstLast[String, List[Int]](List(1, 2, 3))

// 则错误提示为
Cannot prove that List[Int] <:< Iterable[String]
```

其中`${From}`和`${To}`将被替换成被注解类的类型参数From和To。

###21.10 CanBuildFrom解读

map是一个Iterable[A, Repr]的方法，实现如下：

```
def map[B, That](f: (A) => B)(implicit bf: CanBuildFrom[Repr, B, That]): That = {
    val builder = bf()
    val iter = iterator()
    while (iter.hasNext) builder += f(iter.next())
    builder.result
}
```

这里Repr的意思是 “展现类型” 。该参数将让我们选择合适的构建器工厂来构建诸如Range或String这样的非常规集合。

CanBuildFrom[From, E, To]特质将提供类型证明，可以创建一个类型为To的集合，握有类型为E的值，并且和类型From兼容。

CanBuildFrom特质带有一个apply方法，产出类型为Builder[E, To]的对象。Builder类型带有一个+=方法用来将元素添加到一个内部的缓冲，还有一个result方法用来产出锁要求的集合。

```
trait Builder[-E, +To] {
    def +=(e: E): Unit
    def result(): To
}

trait CanBuildFrom[-From, -E, +To] {
    def apply(): Builder[E, To]
}
```

因此，map方法只是构造出一个目标类型的构建器，为构建器填充函数f的值，然后产出结果的集合。

每个集合都在其伴生对象中提供了一个隐式的CanBuildFrom对象。考虑如下简化版的ArrayBuffer类：

```
class Buffer[E: Manifest] extends Iterable[E, Buffer[E]] with Builder[E, Buffer[E]] {
    private var elems = new Array[E](10)
    ...
    def iterator = { 
        ...
        private var i = 0
        def hasNext = i < length
        def next() = { i += 1; elems(i - 1) }
    }
    def +=(e: E) { ... }
    def result() = this
}

object Buffer {
    implicit def canBuildFrom(E: Manifest] 
      = new CanBuildFrom[Buffer[_], E, Buffer[E]] {
        def apply() = new Buffer[E]
    }
}
```

看看如果调用buffer.map(f)会发生什么，其中f是一个类型为A => B的函数。首先，通过调用Buffer伴生对象中的canBuildFrom[B]方法，可以得到隐式的bf参数。它的apply方法返回了构建器，即Buffer[E]。

由于Buffer类碰巧已经有一个+=方法，而它的result方法也被定义为返回它自己。因此，Buffer就是它自己的构建器。

然而，Range类的构建器并不返回一个Range，而且它显然也不能返回Range。举例来说，`(1 to 10).map(x => x * x)`的结果并不是一个Range。在实际的Scala类库中，Range扩展自IndexedSeq[Int]，而IndexedSeq的伴生对象定义了一个构建Vector的构建器。

以下是一个简化版的Range类，提供了一个Buffer作为其构建器：

```
class Range(val low: Int, val high: Int) extends Iterable[Int, Range] {
    def iterator() = ...
}

object Range {
    implicit def canBuildFrom[E: Manifest] 
      = new CanBuildFrom[Range, E, Buffer[E]] {
        def apply() = new Buffer[E]
    }
}
```

如下调用：`Rang(1, 10).map(f)`。这个方法需要一个`implicit bf: CanBuildFrom[Repr, B, That]`。由于Repr就是Range，因此相关联的类型有CanBuildFrom、Range、B和未知的That。其中Range对象可以通过调用器canBuildFrom[B]方法产出一个匹配项，该方法返回一个CanBuildFrom[Range, B, Buffer[B]]。这个匹配项就成为bf；其中apply方法将产出Buffer[B]，用于构建结果。

正如刚才看到的，隐式参数CanBuildFrom[Repr, B, That]将会定位到一个可以产出目标集合的构建器的工厂对象。这个构建器工厂是定义在Repr伴生对象中的一个隐式值。

###21.11 习题解答

<font size =4 color=Blue>
1. ->的工作原理是什么？或者说，"Hello" -> 42和42 -> "Hello"怎么会和对偶("Hello", 42)和(42, "Hello")扯上关系呢？提示：Predef.any2ArrowAssoc
</font>


<font size =4 color=Blue>
2. 定义一个操作符+%，将一个给定的百分比添加到某个值。举例来说，120 +% 10应得到132。提示：由于操作符的方法，而不是函数，你需要提供一个implicit。
</font>


<font size =4 color=Blue>
3. 定义一个!操作符，计算某个整数的阶乘。举例来说，5!应得到120。你将会需要一个经过丰富的类和一个隐式转换。
</font>

<font size =4 color=Blue>
4. 有些人很喜欢那些读起来隐约像英语句子的 “流利API”。创建一个这样的API，用来从控制台读取整数、浮点数以及字符串。例如：

```
Read in aString askingFor "Your name" and anInt askingFor "Your age" and aDouble askingFor "Your weight"。
```
</font>


<font size =4 color=Blue>
5. 提供执行21.6节中的下述运算所需要的代码：

```
smaller(Fraction(1, 7), Fraction(2, 9))
```

给出一个扩展自Ordered[Fraction]的RichFraction类。
</font>



<font size =4 color=Blue>
6. 比较java.awt.Point类的对象，按词典顺序比较（即依次比较x坐标和y坐标的值）。
</font>


<font size =4 color=Blue>
7. 继续前一个练习，根据两个点到原点的距离进行比较。你如何在两种排序之间切换？
</font>



<font size =4 color=Blue>
8. 在REPL中使用implicitly命令来召唤出21.5节及21.6节中的隐式对象。你得到了哪些对象？
</font>



<font size =4 color=Blue>
9. 在Predef.scala中查找=:=对象。解释它的工作原理。
</font>



<font size =4 color=Blue>
10. 表达式`"abc".map(_toUpper)`的结果是一个String，但`"abc".map(_toInt)`的结果是一个Vector。搞清楚为什么会这样。
</font>



##第22章 定界延续

###22.1 捕获并执行延续

延续是这样一种机制，它让你回到程序中之前的一个点。

首先，使用shift结构捕获一个延续。在shift当中，你必须讲明当一个延续被交个你之后，你想要做些什么事。

```
var cont: (Unit => Unit) = null
...
shift { k: (Unit => Unit) => // 延续被传递给了shift
    cont = k // 保存下来，以便之后能使用
}
```

在Scala中，延续是定界的——它只能延展到给定的边界。这个边界由reset { ... } 标出：

```
reset {
    ...
    shift { k: (Unit => Unit) =>
        cont = k
    } // 对cont的调用将从此处开始...
    ...
} // ...到此处结束
```

当你调用cont时，执行将从shift处开始，并一直延展到reset块的边界。

以下是一个完整的示例。将读取一个文件并捕获延续。

```
var cont: (Unit => Unit) = null
var filename = "myfile.txt"
var contents = ""

reset {
    while (contents == "") {
        try {
            contents = scala.io.Source.fromFile(filename, "UTF-8").mkString
        } catch { case _ => }
        shift { k: (Unit => Unit) =>
            cont = k
        }
    }
}

// 要重试的话，只需要执行延续即可：
if (contents == "") {
    print("Try another filename: ")
    filename = readLine()
    cont() // 跳回到shift
}
println(contents)
```

注：在Scala2.9中，需要启动延续插件才能编译使用延续的程序：

```
scalac -P:continuations:enable MyProg.scala
```

###22.2 “运算当中挖个洞”

要理解到底一个延续捕获了什么，我们可以把shift块想象成一个位于reset块中的 “洞”。当你执行延续时，你可以将一个值传到这个洞中，运算继续，就好像shift本就是那个值一样。

###22.3 reset和shift的控制流转

reset/shift有双重职责——一方面定义延续函数，另一方面又捕获延续函数。

当你调用reset时，它的代码体便开始执行。当执行遇到shift时，shift的代码体被调用，传入延续函数作为参数。当shift完成后，执行立即跳转到包含延续的reset块的末尾。

```
var cont: (Unit => Unit) = null
reset {
    println("Before shift")
    shift {
        k: (Unit => Unit) => {
            cont = k
            println("Insert shift") // 跳转到reset末尾
        }
    }
    println("After shift")
}
println("After reset")
cont()

// 当reset执行时，上述代码将打印
Before shift
Inside shift

// 然后它将退出reset块并打印
After reset

// 最后，当调用cont时，执行跳回到reset块，并打印出
After shift
```

###22.4 reset表达式的值

如果reset块退出时因为由于执行了shift，那么得到的值就是shift块的值：

```
val result = reset { shift { k: (String => String) => "Exit" }; "End" }

// result为“Exit”
```

如果reset块执行到自己的末尾的话，它的值就只是reset块的值——亦即块中最后一个表达式的值：

```
val result = reset { if (false) shift { k: (String => String) => "Exit" }; "End" }

// result为“End”
```

###22.5 reset和shift表达式的类型

reset和shift都是带有类型参数的方法，分别是reset[B, C]和shift[A, B, C]。

```
reset {
    shift前
    shift { k: (A => B) => // 由此处推断A和B
        shift中 // 类型C
    } // "洞"的类型为A
    shift后 // 必须产出类型B的值
}
```

这里的A是延续的参数类型——“被填入洞中” 的值的类型。B是延续的返回类型——当有人执行延续的时候返回的值的类型。C是预期的reset表达式的类型——亦即从shift返回的值的类型。（如果reset块有可能返回一个类型为B或C的值，那么B必须是C的子类型。）

注意这些类型是很重要的。如果编译器无法正确地推断出它们，它将报告一个很隐晦的错误提示。

###22.6 CPS注解

在某些虚拟机中，延续的实现方式是抓取运行期栈的快照。当有人调用延续时，运行期栈被恢复成快照的样子。可惜Java虚拟机并不允许进行这样的操作。为了在JVM中提供延续，Scala编译器将对reset块中的代码进行 “延续传递风格”（CPS）的变换。

经过变换的代码与常规的Scala代码很不一样，而且你不能混用这两种风格。对于方法而言，这个区别尤为明显。如果方法包含shift，那它将不会被编译成常规的方法。你必须将它注解为一个 “被变换” 的方法。

上一节中的shift有三个类型参数——分别是延续函数的参数和返回类型，以及代码块的类型。要调用一个包含了shift[A, B, C]的方法，它必须被注解为@cpsParam[B, C]。在通常的情况，即B和C相同时，可以用注解@cps[B]。

```
def tryRead(): Unit @cps[Unit] = {
    while (contents == "") {
        try {
            contents = scala.io.Source.fromFile(filename, "UTF-8").mkString
        } catch { case _ => }
        shift { k: (Unit => Unit) =>
            cont = k
        }
    }
}
```

如果某方法调用带有@cps注解的方法，而该调用本身又不位于reset代码块的话，则该方法也必须被加上注解。换句话说，任何位于reset和shift之间的方法都必须加上注解。

###22.7 将递归访问转化为迭代

例如，如下方法将打印给定目录中所有子目录的所有文件：

```
def processDirectory(dir: File) {
    val files = dir.listFiles
    for (f <- files) {
        if (f.isDirectory)
            processDirectory(f)
        else 
            println(f)
    }
}
```

如果只是看前100个文件的话，没法在当中停掉递归。而用延续的话就简单了。每发现一个节点，我们就跳出递归。如果我们需要更多结果，我们就跳回去。

reset和shift方法尤其适合这种控制流转的模式。每当shift被执行，程序就退出包含它的reset。当被捕获的延续被调用时，程序又再返回shift的位置。

要实现这个设计，可以在访问应被中断的点上放置一个shift。

```
if (f.isDirectory)
    processDirectory(f)
else {
     shift {
         k: (Unit => Unit) => {
             cont = k
         }
    }
    println(f)
}
```

这里的shift承担两个职责。每当它被执行，它就会跳到reset的末尾，同时它会捕获到延续，这样我们才能回得来。

将整个过程的启动点用reset包起来，然后就可以以我们想要调用的次数来调用捕获到延续了：

```
reset {
    processDirectory(new File(rootDirName))
}
for (i <- 1 to 100) cont()
```

当然了，processDirectory方法需要一个CPS注解：

```
def processDirectory(dir: File): Unit @cps[Unit]
```

for循环会被翻译成一个对foreach的调用，而foreach并没有被注解为CPS，因此我们无法调用它。简单地改为while循环就好：

```
var i = 0;
while (i < files.length) {
    val f = files(i)
    i += 1
    ...
}
```

以下是完整的程序：

```
import java.io.File

import scala.util.continuations._

/**
  * Created by zhangws on 17/2/15.
  */
object C22_7 {

    var cont: (Unit => Unit) = null

    def processDirectory(dir: File): Unit@cps[Unit] = {
        val files = dir.listFiles
        var i = 0
        while (i < files.length) {
            val f = files(i)
            i += 1
            if (f.isDirectory) {
                processDirectory(f)
            } else {
                shift {
                    k: (Unit => Unit) => {
                        cont = k // 2
                    }
                } // 5
                println(f)
            }
        }
    }

    def main(args: Array[String]) {
        reset {
            processDirectory(new File("/")) // 1.
        } // 3
        for (i <- 1 to 100) cont() // 4
    }
}
```

在进入reset块时，processDirectory方法被调用1。一旦该方法找到第一个不是目录的文件，它将进入shift2。延续函数被保存到cont，程序跳到reset块的末尾3。

接下来，cont被调用4，程序重新跳回递归5，进入到 “shift洞” 中。递归继续，直到下一个文件被找到，再次进入shift。在shift的末尾，程序将跳到reset的末尾，然后cont函数返回。

###22.8 撤销控制反转

一个很有前景的延续应用是撤销GUI或Web编程中的 “控制反转”。

示例如下：

在你发送完第一个页面给用户之后，你的程序就处于等待状态。最后当用户的响应抵达时，它必须被路由到发送第二个页面的那部分程序逻辑。当用户对第二个页面做出响应后，相关处理又会在另一个不同的地方发生。

基于延续的Web框架能够解决这个问题。当应用做出一个网页并等待用户响应时，一个延续会被保留下来。当用户响应抵达后，这个延续会被调用。

```
import java.awt.BorderLayout
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing._

import scala.util.continuations._

/**
  * Created by zhangws on 17/2/15.
  */
object C22_8 extends App {

    val frame = new JFrame
    val button = new JButton("Next")
    setListener(button) {
        run()
    }
    val textField = new JTextArea(10, 40)
    val label = new JLabel("Welcome to the demo app")

    frame.add(label, BorderLayout.NORTH)
    frame.add(textField)

    val panel = new JPanel
    panel.add(button)
    frame.add(panel, BorderLayout.SOUTH)
    frame.pack()
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.setVisible(true)

    def run(): Unit = {
        reset {
            val response1 = getResponse("What is your first name?")
            val response2 = getResponse("what is your last name?")
            process(response1, response2)
        }
    }

    def process(s1: String, s2: String): Unit = {
        label.setText("Hello, " + s1 + " " + s2)
    }

    var cont: Unit => Unit = null

    def getResponse(prompt: String): String@cps[Unit] = {
        label.setText(prompt)
        setListener(button) {
            cont()
        }
        shift {
            k: (Unit => Unit) => {
                cont = k
            }
        }
        setListener(button) {}
        textField.getText
    }

    def setListener(button: JButton)(action: => Unit): Unit = {
        for (l <- button.getActionListeners) button.removeActionListener(l)
        button.addActionListener(new ActionListener {
            override def actionPerformed(e: ActionEvent): Unit = {
                action
            }
        })
    }
}
```

###22.9 CPS变换

CPS变换会产出一些对象，这些对象会指定如何处理包含 “余下的运算” 的函数。如下的shift方法：

```
shift { 函数 }

// 返回一个对象
ControlContext[A, B, C](函数)
```

shift的代码体是一个类型为(A => B) => C的函数。它接受类型为A => B的延续作为参数，产出一个类型为C的值，该值被传递到包含延续的reset块之外。

一个控制上下文（ControlContext）描述了如何处理延续函数。通常，它会将函数推到一边，有时候它也会计算出某个值。

控制上下文并不知道如何计算延续函数。它有赖于别人来计算它。它只是预期接收延期而已。

shift被翻译成一个知道如何处理shift之后所有事情的控制上下文。

```
new ControlContext(k1 => fun(a => k1(f(a))))
```

这里的a => k1(f(a))首先运行f，然后再完成有k1指定的运算。而fun则按照通常的方式处理运算结果。

这种 “缓慢前行” 是控制上下文的基本操作，它被称做map。以下是map方法的定义：

```
class ControlContext[+A, -B, +C](val fun: (A => B) => C) {
    def map[A1](f: A => A1) = new ControlContext[A1, B, C](
        (k1: (A1 => B)) => fun(x: A => k1(f(x))))
    ...
}
```

cc.map(f)接受一个控制上下文，将它变成一个能处理f之后余下的运算的控制上下文。

reset正是如此定义的：

```
def reset[B, C](cc: ControlContext[B, B, C]) = cc.fun(x => x)
```

先看一个简单的示例：

```
reset {
    0.5 * { shift { k: (Int => Double) => cont = k } } + 1
}
```

拿本例来说，编译器可以用一步就计算出整个延续。即：

```
=> 0.5 * □ + 1
```

因此，我们得到

```
reset {
    new ControlContext[Int, Double, Unit](k => cont = k)
        .map(□ => 0.5 * □ + 1)
}
```

亦即：

```
reset {
    new ControlContext[Double, Double, Unit](k1 => 
        cont = k1(x: Int => 0.5 * x + 1)
}
```

这样，reset就可以被求值了，k1是一个恒等函数，结果为：

```
cont = x: Int => 0.5 * x + 1
```

调用reset只是设置cont，并没有其他效果。

<font color=red>
注：如果用`-Xprint:selectivecps`编译器标志编译，就可以看到CPS变换生成的代码。
</font>

###22.10 转换嵌套的控制上下文

示例：看看如何将一个递归的访问转化为迭代。简单起见，我们将访问一个链表，而不是一棵树：

```
def visit(a: List[String]): String @cps[String] = {
    if (a.isEmpty) "" else {
        shift {
            k: (Unit => String) => {
                cont = k
                a.head
            }
        }
        visit(a.tail)
    }
}
```

和之前一样，shift被转换成控制上下文：

```
new ControlContext[Unit, String, String](k => { cont = k; a.head })
```

不过这一次在shift之后还跟着一个对visit的调用，该调用返回另一个ControlContext

更确切地说，shift被替换成了()，因为延续函数的参数类型为Unit。这样，余下的运算就是：

```
() => visit(a.tail)
```

沿着第一个示例的思路，我们会把这个函数作为参数调用map。但由于它返回的是一个控制上下文，因此我们用flatMap：

```
if (a.isEmpty) new ControlContext(k => k(")) else 
    new ControlContext(k => { cont = k; a.head })
        .flatMap(() => visit(a.tail))
```

以下是flatMap的定义：

```
class ControlContext[+A, -B, +C](val fun: (A => B) => C) {
    ...
    def flatMap[A1, B1, C1 <:B](f: A => Shift[A1, B1, C1]) =
        new ControlContext[A1, B1, C](
            (k1: (A1 => B1)) => fun(x: A => f(x).fun(k1)))
}

// 上面代码的意思是：如果余下的运算是以另一个想要处理余下运算的余下部分的控制上下文开始的话，让它做。
// 这将定义出一个延续，由我们来处理。
```

我们来模拟一次调用：

```
val lst = List("Fred")
reset { visit(lst) }
```

由于lst不是空的，我们得到：

```
reset {
    new ControlContext(k => { cont = k; lst.head }).flatMap(() => visit(lst.tail))
}
```

根据flatMap的定义，我们得到：

```
reset {
    new ControlContext(k => { cont = () => visit(lst.tail).fun(k1); lst.head })
}
```

然后reset将k1设为恒等函数，我们将得到：

```
cont = () => visit(lst.tail).fun(x => x)
lst.head
```

现在我们调用cont。如果我们用了更长的列表，lst.tail不会是空的，于是我们再次得到同样的结果，不过这一次是visit(lst.tail.tail)。但由于我们已经把列表遍历完了，visit(lst.tail)将返回

```
new ControlContext(k => k(""))
```

应用恒等函数，得到结果""。

###22.11 习题解答

<font size =4 color=Blue>
1. 在22.1节的示例当中，假定并不存在文件myfile.txt。现在把filename设为另一个不存在的文件并调用cont。会发生什么？将filename设为一个存在的文件并再次调用cont。会发生什么？再多调用一次cont。会发生什么？首先，在脑海里过一遍控制流转，然后运行程序来验证你的想法。
</font>



<font size =4 color=Blue>
2. 改进22.1节的示例，让延续函数将下一个需要尝试的文件的名称作为参数传递。
</font>



<font size =4 color=Blue>
3. 将22.7节中的实例改成迭代器。迭代器的构造器应包含reset，而next方法应执行延续。
</font>

<font size =4 color=Blue>
4. 22.8节的示例代码看上去并不美观——应用程序开发人员能看到reset语句。将reset从run方法移到按钮监听器中。现在应用程序开发人员是不是很惬意地不知道延续的存在了呢？
</font>

<font size =4 color=Blue>
5. 考虑如下示例程序，它使用延续将迭代转化成迭代器：

```
object Main extends App {
    var cont: Unit => String = null
    val a = "mary has a little lamb".split(" ")
    reset {
        var i = 0
        while (i < a.length) {
            shift {
                k: (Unit => String) => {
                    cont = k
                    a(i)
                }
            }
            i += 1
        }
        ""
    }
    println(cont())
    println(cont())
}
```

用`-Xprint:selectivecps`标志编译并查看生成的代码。经过CPS变换的while语句是什么样子的？
</font>