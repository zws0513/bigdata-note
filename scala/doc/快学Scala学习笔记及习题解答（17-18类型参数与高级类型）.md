本文Scala使用的版本是2.11.8

##第17章 类型参数

###17.1 泛型

**泛型类**

```
class Pair[T, S](val first: T, val second: S)

// 实例化
val p = new Pair(42, "String")
val p2 = new Pair[Any, Any](42, "String")
```

**泛型函数**

```
def getMiddle[T](a: Array[T]) = a(a.length / 2)

// 调用
getMiddle(Array("Mary", "had", "a", "little", "lamb"))

// 下面具体函数保存到f
val f = getMiddle[String] _
```

###17.2 界定

**类型变量界定**

1. 上界（`<:`）

  ```
  class Pair[T <: Comparable[T]](val first: T, val second: T) {
      def smaller = if (first.compareTo(second) < 0) first else second
  }
  ```

2. 下界（`>:`）

  ```
  def replaceFirst[R >: T](newFirst: R) = new Pair(newFirst, second)
  ```

**视图界定（`<%`）**

```
// <%关系意味着T可以被隐式转换成Comparable[T]
class Pair[T <% Comparable[T]]
```

**上下文界定**

上下文界定的形式为T:M，其中M是另一个泛型类。它要求必须存在一个类型为M[T]的“隐式值”。

```
class Pair[T: Ordering](val first: T, val second: T) {
    def smaller(implicit ord: Ordering[T]) = 
        if (ord.compare(first, second) < 0) first else second
}
```

**Manifest上下文界定**

使用基本类型实例化一个泛型Array[T]，需要一个Manifest[T]对象。

```
def makePair[T: Manifest](first: T, second: T) {
    val r = new Array[T](2)
    r(0) = first
    r(1) = second
    r
}
```

调用makePair(4, 9)，编译器将定位到隐式的Manifest[Int]并实际上调用makePair(4, 9)(intManifest)，这样返回基本类型的数组int[2]。

**多重界定**

```
// 可以同时有上界和下界，但不能同时又多个上界或多个下界
T >: Lower <: Upper

// 可以实现多个特质
T <: Comparable[T] with Serializable with Cloneable

// 多个视图界定
T <% Comparable[T] <% String

// 多个上下文界定
T : Ordering : Manifest
```

###17.3 类型约束

总共有三种关系：

- T =:= U
- T <:< U
- T <%< U

这些约束将会测试T是否等于U，是否为U的子类型，或能否被视图（隐式）转换为U。使用时需要添加“隐式类型证明参数”：

```
class Pair[T](val first: T, val second: T)(implicit ev: T <:< Comparable[T])
```

**在泛型类中定义只能在特定条件下使用的方法**

示例一：

```
class Pair[T](val first: T, val second: T) {
    def smaller(implicit ev: T <:< Ordered[T]) =
        if (first < second) first else second
}

// 可以构造出Pair[File]，但只有当调用smaller方法时，才会报错
```

示例二：Option类的orNull方法

```
val friends = Map("Fred" -> "Barney", ...)
val friendOpt = friends.get("Wilma") // Option[String]
val friendOrNull = friendOpt.orNull // 要么是String，要么是null

// 对于值类型，仍然可以实例化Option[Int]，只要别使用orNull就好
```

**改进类型推断**

```
def firstLast[A, C <: Iterable[A]](it: C) = (it.head, it.last)

// 当执行如下代码时：firstLast(List(1, 2, 3))，将推断出非期望的类型[Nothing, List[Int]]
// 因为是同一个步骤中匹配的A和C，要解决这个问题，必须先匹配C，然后再匹配A：
def firstLast[A, C](it: C)(implicit ev: C <:< Iterable[A]) = (it.head, it.last)
```

###17.4 型变

**协变：C[+T]**

如果A是B的子类，那么C[A]是C[B]的子类。

```
class Pair[+T](val first: T, val second: T)
```

`+`号意味着该类型是与T协变的，也就是说，它与T按同样的方向型变。由于Student是Person的子类型，Pair[Student]也是Pair[Person]的子类型。

**逆变：C[-T]**

如果A是B的子类，那么C[A]是C[B]的父类。

```
trait Friend[-T] {
    def befriend(someone: T)
}

// 函数
def makeFriendWith(s: Student, f: Friend[Student]) { f.befriend(s) }

// 下面关系
class Person extends Friend[Person]
class Student extends Person
val susan = new Student
val fred = new Person

// 可以使用以下调用，产出Student结果，可以被放入Array[Person]
makeFriendWith(susan, fred)
```

###17.5 协变和逆变点

```
class Pair[+T](var first: T, var second: T)
```

会报错，说在如下的setter方法中，协变的类型T出现在了逆变点：`first_=(value: T)`

参数位置是逆变点，而返回类型的位置是协变点。

在函数参数中，型变是反转过来的，参数是协变的。例如（Iterable[+A]的foldLeft方法）：

```
foldLeft[B](z: B)(op: (B, A) => B): B
               -       +  +     -   +
```

A现在位于协变点。

考虑以下不可变对偶的replaceFirst方法：

```
class Pair[+T](val first: T, val second: T) {
    def replaceFirst(newFirst: T) = new Pair[T](newFirst, second)
}
```

编译器会报错，因为类型T出现在了逆变点，解决方法是给方法加上另一个类型参数：

```
class Pair[+T](val first: T, val second: T) {
    def replaceFirst[R >: T](newFirst: R) = new Pair[R](newFirst, second)
}
```

###17.6 对象不能泛型

不能给对象添加类型参数。例如：

```
abstract class List[+T] {
    def isEmpty: Boolean
    def head: T
    def tail: List[T]
}

class Node[T](val head: T, val tail: List[T]) extends List[T] {
    def isEmpty = false
}

object Empty[T] extends List[T] // 错误

// 解决方法一：
class Empty[T] extends List[T] {
    def isEmpty = true
    def head = throw new UnsupportedOperationException
    def tail = throw new UnsupportedOperationException
}

// 解决方法二：
object Empty extends List[Nothing]
// 根据协变规则，List[Nothing]可以被转换成List[Int]
```

###17.7 类型通配符

`_`为Scala中使用的通配符。

```
// 协变
def process(people: java.util.List[_ <: Person]

// 逆变
import java.util.Comparator
def min[T](p: Pair[T])(comp: Comparator[_ >: T])
```

###17.8 习题解答

<font size =4 color=Blue>
1. 定义一个不可变类Pair[T,S], 带一个swap方法，返回组件交换过位置的新对偶。
</font>


<font size =4 color=Blue>
2. 定义一个可变类Pair[T]，带一个swap方法，交换对偶中组件的位置。
</font>


<font size =4 color=Blue>
3. 给定类Pair[T, S] ，编写一个泛型方法swap，接受对偶作为参数并返回组件交换过位置的新对偶。
</font>

<font size =4 color=Blue>
4. 在17.3节中，如果我们想把Pair[Person]的第一个组件替换成Student，为什么不需要给replaceFirst方法定一个下界？
</font>


<font size =4 color=Blue>
5. 为什么RichInt实现的是Comparable[Int]而不是Comparable[RichInt]?
</font>



<font size =4 color=Blue>
6. 编写一个泛型方法middle，返回任何Iterable[T]的中间元素。举例来说，middle(“World”)应得到’r’。
</font>


<font size =4 color=Blue>
7. 查看Iterable[+A]特质。哪些方法使用了类型参数A？为什么在这些方法中类型参数位于协变点？
</font>



<font size =4 color=Blue>
8. 在17.10节中，replaceFirst方法带有一个类型界定。为什么你不能对可变的Pair[T]定义一个等效的方法？

```
def replaceFirst[R >: T](newFirst: R) { first = newFirst } // 错误
```
</font>



<font size =4 color=Blue>
9. 在一个不可变类Pair[+T]中限制方法参数看上去可能有些奇怪。不过，先假定你可以在Pair[+T]定义

```
def replaceFirst(newFirst: T)
```

问题在于，该方法可能会被重写（以某种不可靠的方式）。构造出这样的一个示例。定义一个Pair[Double]的类型NastyDoublePair，重写replaceFirst方法，用newFirst的平方根来做新对偶。然后对实际类型为NastyDoublePair的Pair[Any]调用replaceFirst(“Hello”)。
</font>



<font size =4 color=Blue>
10. 给定可变类Pair[S,T]，使用类型约束定义一个swap方法，当类型参数相同时可以被调用。
</font>



##第18章 高级类型

###18.1 单例类型

**子类示例**

<pre><code>
class Document {
    def setTitle(title: String): <font color=red>this.type</font> = { ..., this }
    def setAuthor(author: String) = { ..., this }
    ...
}

class Book extends Document {
    def addChapter(chapter: String) = { ...; this }
    ...
}
</code></pre>

注意上面红字，这样就可以进行下面这样的调用：

<pre><code>
val book = new Book()
book.<font color=red>setTitle("Scala for the Impatient")</font>.addChapter(chapter1)
</code></pre>

**对象示例**

<pre><code>
object Title

class Document {
    private var useNextArgAs: Any = null
    def set(<font color=red>obj: Title.type</font>): this.type = { useNextArgAs = obj; this }
    ...
}
</code></pre>

注意上面红字，如果使用`obj: title`则指代的是单例对象，而不是类型。

###18.2 类型投影

嵌套类从属于它的外部对象，如下：

```
import scala.collection.mutable.ArrayBuffer

class Network {
    class Member(val name: String) {
        val contacts = new ArrayBuffer[Member]
    }
    
    private val members = new ArrayBuffer[Member]
    
    def join(name: String) = {
        val m = new Member(name)
        members += m
        m
    }
}
```

这样每个网络实例都有它自己的Member类。

如果不想所有地方使用 “每个对象自己的内部类” 的话，可以用类型投影`Network#Member`意思是任何Network的Member。

```
class Network {
    class Member(val name: String) {
        val contacts = new ArrayBuffer[Network#Member]
    }
    ...
}
```

###18.3 路径

`com.demo.chatterobj.Member`这样的表达式被称之为路径。而且在最后的类型之前，路径的所有组成部分都必须是 “稳定的” ，即它必须指定到单个、有穷的范围，必须是以下当中的一种：

- 包
- 对象
- val
- this、super、super[S]、C.this、C.super或C.super[S]

###18.4 类型别名

可以用type关键字创建一个简单的别名：

```
class Book {
    import scala.collection.mutable._
    type Index = HashMap[String, (Int, Int)]
    ...
}
```

然后就可以用Book.Index代替scala.collection.mutable.HashMap[String, (Int, Int)]。

另外，类型别名必须被嵌套在类或对象中，它不能出现在Scala文件的顶层。

###18.5 结构类型

指的是一组关于抽象方法、字段和类型的规格说明。例如：

<pre><code>
def appendLines(target: <font color=red>{ def append(str: String): Any },</font> lines: Iterable[String]) {
    for (l <- lines) { target.append(l); target.append("\n") }
}
</code></pre>

这样可以对任何具备append方法的类的实例调用appendLines方法。

在幕后，Scala使用反射来调用target.append。

###18.6 复合类型

定义形式如下：

```
T1 with T2 with T3 ...
```

其中T1、T2、T3等是类型。要想成为该复合类型的实例，某个值必须满足每一个类型的要求才行。即这样的类型也被称做交集类型。

例如：

```
val image = new ArrayBuffer[java.awt.Shape with java.io.Serializable]
// 只能添加那些即时形状（Shape）也是可被序列化的对象。

val rect = new Rectangle(5, 10, 20, 30)
image += rect // ok，Rectangle是Serializable的
image += new Area(rect) // 错误，Area是Shape但不是Serializable的
```

###18.7 中置类型

一个带有两个类型参数的类型，以中置语法表示，类型名称写在两个类型参数之间，例如

```
String Map Int

// 而不是
Map[String, Int]
```

在Scala中，可以这样定义：

```
type x[A, B] = (A, B)

// 此后就可以写
String x Int
// 而不是
(String, Int)
```

另外，中置类型操作符都拥有相同的优先级，而且是左结合的。即：

```
String x Int x Int
// 意思是：
((String, Int), Int)
```

###18.8 存在类型

定义方式是在类型表达式之后跟上`forSome { ... }`，花括号中包含了type和val的声明。

```
Array[T] forSome { type T <: jComponent }
```

Scala的类型通配符只不过是存在类型的“语法糖”，例如：

```
Map[_, _]
// 等同于
Map[T, U] forSome { type T; type U }
```

只不过forSome可以使用更复杂的关系：

```
def process[M <: n.Member forSome { val n: Network }](m1: M, m2: M) = (m1, m2)

// 该方法将会接受相同网络的成员，但拒绝那些来自不同网络的成员

val chatter = new Network
val myFace = new Network
val fred = chatter.join("Fred")
val wilma = chatter.join("Wilma")
val barney = myFace.join("Barney")
process(fred, wilma) // ok
process(fred, barney) // 错误
```

###18.9 Scala类型系统

类型 | 语法 | 说明
------ | --------- | --------
类或特质 | class C ..., trait C ... | 第5、10章
元组 | (T1, ..., Tn) | 第4.7节
函数类型 | (T1, ..., Tn) => T | 
带注解的类型 | T@A | 第15章 
参数化类型 | A[T1, ..., Tn] | 第17章
单例类型 | 值.type | 第18章
类型投影 | O#I | 第18章
复合类型 | T1 with T2 with ... with Tn { 声明 } | 第18章
中置类型 | T1 A T2 | 第18章
存在类型 | T forSome { type和val声明 } | 第18章

###18.10 自身类型

自身类型（self type）的声明来定义特质：`this: 类型 =>`，这样的特质只能被混入给定类型的子类当中。

```
trait Logged {
    def log(msg: String)
}

trait LoggedException extends Logged {
    this: Exception =>
      def log() { log(getMessage()) }
      // 可以调用getMessage，因为this是一个Exception
}
```

```
val f = new JFrame with LoggedException
// 错误：JFrame不是LoggedException的自身类型Exception的子类型
```

多个类型要求时，可以用复合类型：

```
this: T with U with ... =>
```

###18.11 依赖注入

在Scala中，可以通过特质和自身类型达到一个简单的依赖注入的效果。例如日志功能：

```
trait Logger { def log(msg: String) }

// 有该特质的两个实现，ConsoleLogger和FileLogger。

// 用户认证特质有一个对日志功能的依赖，用于记录认证失败：
trait Auth {
    this: Logger =>
        def login(id: String, password: String): Boolean
}

// 应用逻辑有赖于上述两个特质：
trait App {
    this: Logger with Auth =>
        ...
}

// 然后，可以像这样来组装我们的应用：
object MyApp extends App with FileLogger("test.log") with MockAuth("users.txt")
```

蛋糕模式给出了更好的设计，在这个模式当中，对每个服务都提供一个组件特质，该特质包含：

- 任何所依赖的组件，以自身类型表述
- 描述服务接口的特质
- 一个抽象的val，该val将被初始化成服务的一个实例
- 可以有选择性地包含服务接口的实现

```
trait LoggerComponent {
    trait Logger { ... }
    val logger: Logger
    class FileLogger(file: String) extends Logger { ... }
    ...
}

trait AuthComponent {
    this: LoggerComponent => // 让我们可以访问日志器
    
    trait Auth { ... }
    val auth: Auth
    class MockAuth(file: String) extends Auth { ... }
    ...
}

// 组件配置
object AppComponents extends LoggerComponent with AuthComponent {
    val logger = new FileLogger("test.log")
    val auth = new MockAuth("users.txt")
}
```

###18.12 抽象类型

类或特质可以定义一个在子类中被具体化的抽象类型（abstract type）。例如：

```
trait Reader {
  type Contents
  def read(fileName: String): Contents
}

// 在这里，类型Contents是抽象的，具体的子类需要指定这个类型：
class StringReader extends Reader {
  type Contents = String
  def read(fileName: String) = Source.fromFile(fileName, "UTF-8").mkString
}

class ImageReader extends Reader {
  type Contents = BufferedImage
  def read(fileName: String) = ImageIO.read(new File(fileName))
}
```

同样的效果也可以通过类型参数来实现：

```
trait Reader[C] {
  def read(fileName: String): C
}

class StringReader extends Reader[String] {
  def read(fileName: String) = Source.fromFile(fileName, "UTF-8").mkString
}

class ImageReader extends Reader[BufferedImage] {
  def read(fileName: String) = ImageIO.read(new File(fileName))
}
```

Scala的经验法则是：

- 如果类型是在类被实例化时给出，则使用类型参数
- 如果类型是在子类中给出的，则使用抽象类型

当有多个类型依赖时，抽象类型用起来更方便——可以避免使用一长串类型参数：

```
trait Reader {
  type In
  type Contents
  def read(in: In): Contents
}

class ImageReader extends Reader {
  type In = File
  type Contents = BufferedImage
  def read(file: In) = ImageIO.read(file)
}
```

抽象类型可以有类型界定

```
trait Listener {
  type Event <: java.util.EventObject
  ...
}

// 子类必须提供一个兼容的类型
trait ActionListener extends Listener {
  type Event = java.awt.event.ActionEvent  // OK，这是一个子类型
}
```

###18.13 家族多态

以客户端Java的事件处理为例：

**泛型类型示例**

```
trait Listener[E] {
    def occurred(e: E): Unit
}

// 事件源需要一个监听器的集合，和一个触发这些监听器的方法
trait Source[E, L <: Listener[E]] {
    private val listeners = new ArrayBuffer[L]
    def add(l: L) { listeners += l }
    def remove(l: L) { listeners -= l }
    def fire(e: E) {
        for (l <- listeners) l.occurred(e)
    }
}

// 按钮触发动作事件，监听器类型
trait ActionListener extends Listener[ActionEvent]

// Button类混入Source特质
class Button extends Source[ActionEvent, ActionListener] {
    def click() {
        fire(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "click"))
    }
}

// ActionEvent类将事件源设置为this，但是事件源的类型为Object。可以用自身类型来让它也是类型安全的：
trait Event[S] {
    var source: S = _
}

trait Listener[S, E <: Event[S]] {
    def occurred(e: E): Unit
}

trait Source[S, E <: Event[S], L <: Listener[S, E]] {
    this: S =>
       private val listeners = new ArrayBuffer[L]
       def add(l: L) { listeners += l }
       def remove(l: L) { listeners -= l }
       def fire(e: E) {
           e.source = this // 这里需要自身类型
           for (l <- listeners) l.occurred(e)
       }
}

// 定义按钮
class ButtonEvent extends Event[Button]

trait ButtonListener extends Listener[Button, ButtonEvent]

class Button extends Source[Button, ButtonEvent, ButtonListener] {
    def click() { fire(new ButtonEvent) }
}
```

**抽象类型示例**

```
trait ListenerSupport {
    type S <: Source
    type E <: Event
    type L <: Listener
    
    trait Event {
        var Source: S = _
    }
    
    trait Listener {
        def occurred(e: E): Unit
    }
    
    trait Source {
        this: S =>
            private val listeners = new ArrayBuffer[L]
            def add(l: L) { listeners += l }
            def remove(l: L) { listeners -= l }
            def fire(e: E) {
                e.source = this
                for (l <- listeners) l.occurred(e)
            }
    }
}

// 
object ButtonModule extends ListenerSupport {
    type S = Button
    type E = ButtonEvent
    type L = ButtonListener
    
    class ButtonEvent extends Event
    trait ButtonListener extends Listener
    class Button extends Source {
        def click() { fire(new ButtonEvent) }
    }
}

// 使用按钮
object Main {
    import ButtonModule._
    
    def main(args: Array[String]) {
        val b = new Button
        b.add(new ButtonListener {
            override def occurred(e: ButtonEvent) { println(e) }
        })
        b.click()
    }
}
```

###18.14 高级类型

```
trait Container[E] {
    def += (e: E): Unit
}

// Iterable依赖一个类型构造器来生成结果，以C[X]表示。这使得Iterable成为一个高等类型
trait Iterable[E, C[X] <: Container[X]] {
    def iterator(): Iterator[E]
    def build[F](): C[F]
    def map[F](f: (E) => F): C[F] = {
        val res = build[F]()
        val iter = iterator()
        while (iter.hasNext) res += f(iter.next())
        res
    }
}

class Range(val low: Int, val high: Int) extends Iterable[Int, Buffer] {
    def iterator() = new Iterator[Int] {
        private var i = low
        def hasNext = i <= high
        def next() = { i += 1; i - 1 }
    }
    
    def build[F]() = new Buffer[F]
}

class Buffer[E: Manifest] extends Iterable[E, Buffer] with Container[E] {
    private var capacity = 10
    private var length = 0
    private var elems = new Array[E](capacity)
    
    def iterator() = new Iterator[E] {
        private var i = 0
        def hasNext = i < length
        def next() = { i += 1; elems(i - 1) }
    }
    
    def build[F]() = new Buffer[F]
    
    def +=(e: E) {
        if (length == capacity) {
            capacity = 2 * capacity
            val nelems = new Array[E](capacity)
            for (i <- 0 until length) nelems(i) = elems(i)
            elems = nelems
        }
        elems(length) = e
        length += 1
    }
}
```

###18.15 习题解答

<font size =4 color=Blue>
1. 实现一个Bug类，对沿着水平线爬行的虫子建模。move方法向当前方向移动，turn方法让虫子转身，show方法打印出当前的位置。让这些方法可以被串接调用。例如：

```
bugsy.move(4).show().move(6).show().turn().move(5).show()
```

上述代码应显示4 10 5。
</font>

```
```

<font size =4 color=Blue>
2. 为前一个练习中的Bug类提供一个流利接口，达到能编写如下代码的效果：

```
bugsy move 4 and show and then move 6 and show turn around move 5 and show
```
</font>

```
```

<font size =4 color=Blue>
3. 完成18.1节中的流利接口，以便我们可以做出如下调用：

```
book set Title to “Scala for the Impatient” set Author to “Cay Horstmann”
```
</font>

```
```

<font size =4 color=Blue>
4. 实现18.2节中被嵌套在Network类中的Member类的equals方法。两个成员要想相等，必须属于同一个网络。
</font>

```
```

<font size =4 color=Blue>
5. 考虑如下类型别名

```
type NetworkMember = n.Member forSome { val n : Network }
```

和函数

```
def process(m1: NetworkMember, m2: NetworkMember) = (m1, m2)
```

这与18.8节中的process函数有什么不同？
</font>

与18.8不同，允许不同网络作为参数

<font size =4 color=Blue>
6. Scala类库中的Either类型可以被用于要么返回结果，要么返回某种失败信息的算法。编写一个带有两个参数的函数：一个已排序整数数组和一个整数值。要么返回该整数值在数组中的下标，要么返回最接近该值的元素的下标。使用一个中置类型作为返回类型。
</font>

```
```

<font size =4 color=Blue>
7. 实现一个方法，接受任何具备如下方法的类的对象和一个处理该对象的函数。
调用该函数，并在完成或有任何异常发生时调用close方法。

```
def close(): Unit
```
</font>

<font size =4 color=Blue>
8. 编写一个函数printValues，带有三个参数f、from和to，打印出所有给定区间范围内的输入值经过f计算后的结果。这里的f应该是任何带有接受Int产出Int的apply方法的对象。例如：

```
printValues((x: Int) => x*x, 3, 6) //将打印 9 16 25 36
printValues(Array(1, 1, 2, 3, 5, 8, 13, 21, 34, 55), 3, 6) //将打印 3 5 8 13
```
</font>

<font size =4 color=Blue>
9. 考虑如下对物理度量建模的类：

```
abstract class DimT {
    protected def create(v: Double): T
    def +(other: Dim[T]) = create(value + other.value)
    override def toString() = value + “ “ + name
}
```

以下是具体子类：

```
class Seconds(v: Double) extends DimSeconds {
    override def create(v: Double) = new Seconds(v)
}
```

但现在不清楚状况的人可能会定义

```
class Meters(v: Double) extends DimSeconds {
    override def create(v: Double) = new Seconds(v)
}
```

允许米（Meters）和秒（Seconds）相加。使用自身类型来防止发生这样的情况。
</font>

```
```

<font size =4 color=Blue>
10. 自身类型通常可以被扩展自身的特质替代，但某些情况下使用自身类型会改变初始化和重写的顺序。构造出这样的一个示例。
</font>

```
```