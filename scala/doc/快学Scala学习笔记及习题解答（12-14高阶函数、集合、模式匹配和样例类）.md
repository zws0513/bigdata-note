本文Scala使用的版本是2.11.8

##第12章 高阶函数
###12.1 基本用法

**作为值得函数**

```
import scala.math._

val num = 3.14
// ceil函数后的_表示这是个函数，而不是忘记传参
val fun = ceil _

// 调用
fun(num)

// 传递
Array(3.14, 1.42, 2.0).map(fun)
```

**匿名函数**

```
// 存放到变量
val triple = (x: Double) => 3 * x

// 传递给另一个函数
Array(3.14, 1.42, 2.0).map((x: Double) => 3 * x)
```

**定义接收函数参数的函数**

```
def valueAtOneQuarter(f: (Double) => Double) = f(0.25)

// 类型为((Double) => Double) => Double

// 也可以产生另一个函数
def mulBy(factor: Double) = (x: Double) => factor * x
// 类型为(Double) => ((Double) => Double)
```

**参数类型推断**

Scala会尽可能帮助推断类型信息。例如，如果参数在=>右侧只出现一次，可以用_替换它。

```
valueAtOneQuarter(3 * _)
```

**一些有用的高阶函数**

函数名 | 描述 
---- | -----
map | 将一个函数应用到某个集合的所有元素并返回结果
foreach | 将函数应用到每个元素，无返回值
filter | 输出所有匹配某个特定条件的元素
reduceLeft | 接受一个二元的函数，并将它应用到序列中的所有元素，从左到右
sortWith | 接受一个二元函数，进行排序

**闭包**

```
def mulBy(factor: Double) = (x: Double) => factor * x

val triple = mulBy(3)
val half = mulBy(0.5)
println(triple(14) + " " + half(14))  // 将打印42 7
```

每一个返回的函数都有自己的factor设置。这样的函数被称作闭包（closure)。闭包由代码和代码用到的任何非局部变量定义构成。

这些函数实际上是以类的对象方式实现的。

###12.2 SAM转换

在Scala中，每当想要告诉另一个函数做某件事时，会传一个函数参数给它。而Java是将动作放在一个实现某接口的类中，然后将该类的一个实例传递给另一个方法。

这些接口被称做SAM类型（single abstract method）。

Java实现

```
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.{JButton, JFrame}


object Note1 {

    implicit def makeAction(action: (ActionEvent) => Unit) = new ActionListener {
        override def actionPerformed(event: ActionEvent) {
            action(event)
        }
    }

    def main(args: Array[String]) {

        var data = 0
        val frame = new JFrame("SAM Testing");
        val jButton = new JButton("Counter")

        jButton.addActionListener(new ActionListener {
            override def actionPerformed(event: ActionEvent) {
                data += 1
                println(data)
            }
        })
    }
}
```

Scala实现

```
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.{JButton, JFrame}


object Note1 {

    // 定义隐式转换，把函数转换成一个ActionListener实例
    implicit def makeAction(action: (ActionEvent) => Unit) = new ActionListener {
        override def actionPerformed(event: ActionEvent) {
            action(event)
        }
    }

    def main(args: Array[String]) {

        var data = 0
        val frame = new JFrame("SAM Testing");
        val jButton = new JButton("Counter")

        // 传递函数参数
        jButton.addActionListener((event: ActionEvent) => {
            data += 1; println(data)
        })

        frame.setContentPane(jButton);
        frame.pack();
        frame.setVisible(true);
    }
}
```

###12.3 柯里化

柯里化（currying）指的是将原来接受两个参数的函数变成新的接受一个参数的函数的过程。新的函数返回一个以原有第二个参数做为参数的函数。

```
def mulOneAtATime(x: Int) = (y: Int) => x * y

// 调用
mulOneAtATime(6)(7)

// Scala支持如下简写来定义这样的柯里化函数
def mulOneAtATime(x: Int)(y: Int) = x * y
```

示例

```
val a = Array("Hello", "World")
val b = Array("hello", "world")
a.corresponds(b)(_.equalsIgnoreCase(_))
```

###12.4 控制抽象

```
/**
  * 如下: runInThread2方式定义函数就比runInThread要优雅很多
  * 调用时, 不用输入 () =>
  */
object Note2 {

    def runInThread(block: () => Unit): Unit = {
        new Thread {
            override def run() { block() }
        }.start()
    }

    def runInThread2(block: => Unit): Unit = {
        new Thread {
            override def run() { block }
        }.start()
    }

    def main(args: Array[String]) {
        runInThread { () => println("Hi"); Thread.sleep(10000); println("Bye") }

        runInThread2 { println("Hi"); Thread.sleep(10000); println("Bye") }
    }
}
```

通过上面这样的应用，可以构建控制抽象：看上去像是编程语言的关键字的函数。

示例：实现像while语句的函数

```
object Note2 {

    // 函数参数的专业术语叫换名调用参数, 和常规的参数不同, 函数在被调用时,
    // 参数表达式不会被求值, 如下 x == 0
    def until(condition: => Boolean) (block: => Unit): Unit = {
        if (!condition) {
            block
            until(condition)(block)
        }
    }

    def main(args: Array[String]): Unit = {
        var x = 10
        until (x == 0) {
            x -= 1
            println(x)
        }
    }
}
```

###12.5 return表达式

```
object Note2 {

    // 函数参数的专业术语叫换名调用参数, 和常规的参数不同, 函数在被调用时,
    // 参数表达式不会被求值, 如下 x == 0
    def until(condition: => Boolean)(block: => Unit): Unit = {
        if (!condition) {
            block
            until(condition)(block)
        }
    }

    // 如果在带名函数中使用return的话, 则需要给出其返回类型
    def indexOf(str: String, ch: Char): Int = {
        var i = 0
        until(i == str.length) {
            if (str(i) == ch) return i
            i += 1
        }
        -1
    }

    def main(args: Array[String]): Unit = {
        println(indexOf("test", 'x'))
    }
}
```

###12.6 习题解答
<font size =4 color=Blue>
1. 编写函数values(fun: (Int) => Int, low: Int, high: Int),该函数输出一个集合，对应给定区间内给定函数的输入和输出。比如，values(x => x * x, -5, 5)应该产出一个对偶的集合(-5, 25), (-4, 16), (-3, 9), …, (5, 25)
</font>

```
object One {

    def values(fun: (Int) => Int, low: Int, high: Int) = {
        var array = List[(Int, Int)]()
        low to high foreach {
            x => array = array :+ (x, fun(x))
        }
        array
    }

    def main(args: Array[String]) {
        println(values(x => x * x, -5, 5).mkString(" "))
    }
}

// 结果
(-5,25) (-4,16) (-3,9) (-2,4) (-1,1) (0,0) (1,1) (2,4) (3,9) (4,16) (5,25)
```

<font size =4 color=Blue>
2. 如何用reduceLeft得到数组中的最大元素?
</font>

```
object Two {

    def main(args: Array[String]) {
        val arr = Array(1, 333, 4, 6, 4, 4, 9, 32, 6, 9, 0, 2)
        val max = arr.reduceLeft((x, y) => {
            if (x > y) x else y
        })
        println(max)
    }
}

// 结果
333
```

<font size =4 color=Blue>
3. 用to和reduceLeft实现阶乘函数,不得使用循环或递归
</font>

```
object Three {

    def factorial(n: Int): Int = {
        if (n > 0) {
            1 to n reduceLeft (_ * _)
        } else if (n == 0) {
            1
        } else {
            throw new IllegalArgumentException("请输入非负数.")
        }
    }

    def main(args: Array[String]) {
        println(factorial(-1))
    }
}

// 结果
120
```

<font size =4 color=Blue>
4. 前一个实现需要处理一个特殊情况，即n<1的情况。展示如何用foldLeft来避免这个需要。（在scaladoc中查找foldLeft的说明。它和reduceLeft很像，只不过所有需要结合在一起的这些值的首值在调用的时候给出。）
</font>

```
object Four {

    def factorial(n: Int): Int = {
        if (n < 0) {
            throw new IllegalArgumentException("请输入非负数.")
        } else {
            (1 to n).foldLeft(1)(_ * _)
        }
    }

    def main(args: Array[String]) {
        println(factorial(0))
    }
}
```

<font size =4 color=Blue>
5. 编写函数largest(fun: (Int) => Int, inputs: Seq[Int])，输出在给定输入序列中给定函数的最大值。举例来说，largest(x => 10 * x - x * x, 1 to 10)应该返回25。不得使用循环或递归
</font>

```
object Five {

    def largest(fun: (Int) => Int, inputs: Seq[Int]): Int = {
        inputs.map(fun).max
    }

    def main(args: Array[String]) {
        println(largest(x => 10 * x - x * x, 1 to 10))
    }
}
```

<font size =4 color=Blue>
6. 修改前一个函数，返回最大的输出对应的输入。举例来说,largestAt(fun: (Int) => Int, inputs: Seq[Int])应该返回5。不得使用循环或递归
</font>

```
object Six {

    def largestAt(fun: (Int) => Int, inputs: Seq[Int]): Int = {
        inputs.map(x => (x, fun(x)))
            .reduceLeft((x, y) => if (x._2 > y._2) x else y)._1
    }

    def main(args: Array[String]) {
        println(largestAt(x => 10 * x - x * x, 1 to 10))
    }
}
```

<font size =4 color=Blue>
7. 要得到一个序列的对偶很容易，比如: `
val pairs = (1 to 10) zip (11 to 20)`
假定你想要对这个序列做某种操作，比如，给对偶中的值求和，但是你不能直接使用:
`pairs.map( + )`
函数 \_ + \_ 接受两个Int作为参数，而不是(Int, Int)对偶。编写函数adjustToPair,该函数接受一个类型为(Int, Int) => Int的函数作为参数，并返回一个等效的, 可以以对偶作为参数的函数。举例来说就是:adjustToPair(\_ * \_)((6, 7))应得到42。然后用这个函数通过map计算出各个对偶的元素之和
</font>

```
object Seven {

    def ajustToPair(fun: (Int, Int) => Int) = (x: (Int, Int)) => fun(x._1, x._2)

    def main(args: Array[String]) {
        val x = ajustToPair(_ * _)((6, 7))
        println(x)
        val pairs = (1 to 10) zip (11 to 20)
        println(pairs)
        val y = pairs.map(ajustToPair(_ + _))
        println(y)
    }
}

// 结果
42
Vector((1,11), (2,12), (3,13), (4,14), (5,15), (6,16), (7,17), (8,18), (9,19), (10,20))
Vector(12, 14, 16, 18, 20, 22, 24, 26, 28, 30)
```

<font size =4 color=Blue>
8. 在12.8节中，你看到了用于两组字符串数组的corresponds方法。做出一个对该方法的调用，让它帮我们判断某个字符串数组里的所有元素的长度是否和某个给定的整数数组相对应
</font>

```
object Eight {

    def main(args: Array[String]) {
        val a = Array("asd", "df", "abcd")
        val b = Array(3, 2, 4)
        val c = Array(3, 2, 1)
        println(a.corresponds(b)(_.length == _))
        println(a.corresponds(c)(_.length == _))

    }
}

// 结果
true
false
```

<font size =4 color=Blue>
9. 不使用柯里化实现corresponds。然后尝试从前一个练习的代码来调用。你遇到了什么问题？
</font>

```
没有柯里化则不能使用前一个练习里的代码方式来调用
```

<font size =4 color=Blue>
10. 实现一个unless控制抽象，工作机制类似if,但条件是反过来的。第一个参数需要是换名调用的参数吗？你需要柯里化吗？
</font>

```
// 需要换名和柯里化
object Ten {

    def unless(condition: => Boolean)(block: => Unit) {
        if (!condition) {
            block
        }
    }

    def main(args: Array[String]) {
        unless(0 > 1) {
            println("Unless!")
        }
    }
}
```

###第13章 集合
###13.1 主要概念

所有的集合都扩展自Iterable。有三类集合，分别是Seq、Set和Map。

Iterable提供了遍历一个集合的基本方式。

每个集合特质或类都有一个带有apply方法的伴生对象，可以用于构建该集合中的实例。

**Seq**

Seq是一个有先后次序的序列，比如数组和列表。IndexedSeq是它的子特质，允许通过下标访问元素。

**Set**

Set是没有次序的一组值，子特质SortedSet的元素则以某种排过序的顺序被访问。

**Map**

Map是一组对偶。SortedMap按照键的排序访问其中的实体。

**可变和不可变集合**

不可变的集合从不改变，因此可以安全地共享其引用。

###13.2 序列

**不可变序列**

![不可变序列](http://img.blog.csdn.net/20161126114830507)

Vector是ArrayBuffer的不可变版本：可下标访问，支持快速随机访问。它是以树形结构的形式实现的，每个节点可以有不超过32个子节点。

Range表示一个整数序列，它并不存储所有值，而只是起始值、结束值和增值。

**可变序列**

![可变序列](http://img.blog.csdn.net/20161126140338264)

###13.3 列表

**不可变列表**

列表要么是Nil（即空表），要么是一个head元素加上一个tail，而tail又是一个列表。

```
val digits = List(4, 2)
// digits.head值是4，digits.tail是List(2)。
// digits.tail.head是2，而digits.tail.tail是Nil
```

::操作符从给定的头和尾创建一个新的列表

```
9 :: List(4, 2)
// 返回 List(9, 4, 2)
// 也可以如下（::是右结合的）
9 :: 4 :: 2 :: Nil
```

列表遍历，可以使用迭代器、递归或者模式匹配。下面是模式匹配示例：

```
def sum(lst: List[Int]): Int = lst match {
    case Nil => 0
    case h :: t => h + sum(t)
}
```

**可变列表**

可变的LinkedList和不可变List相似。不过，可以通过对elem引用赋值来修改其头部，对next引用赋值修改其尾部。

还提供了DoubleLinkedList，区别是多带一个prev引用。

<font color=red>
如果要把某个节点变成列表中的最后一个节点，不能将next引用设为Nil，而是设置为LinkedList.empty
</font>

###13.4 集

集市不重复元素的集合。集并不保留元素插入的顺序，缺省以哈希集实现的。

链式哈希集可以记住元素被插入的顺序。

```
val weekdays = scala.collection.mutable.LinkedHashSet("Mo", "Tu", "We", "Th", "Fr")
```

已排序的集（用红黑树实现）：

```
scala.collection.immutable.SortedSet(1, 2, 3, 4, 5)
```

位集（BitSet）是以一个字位序列的方式存放非负整数。如果集中有i，则第i个字位是1。提供了可变和不可变的实现。

contains方法检查某个集是否包含给定的值。subsetOf方法检查某个集是否是另一个集的子集。

```
val digits = Set(1, 7, 2, 9)
digits contains 0 // false
Set(1, 2) subsetOf digits // true
```

union、intersect和diff方法执行通常的集操作。也可以写作`|`、`&`和`&~`。还可以将联合（union）写作`++`，将差异（diff）写作`--`。

```
val primes = Set(2, 3, 5, 7)
digits union primes 等于 Set(1, 2, 3, 5, 7, 9)
digits & primes 等于 Set(2, 7)
digits -- primes 等于 Set(1, 9)
```

###13.5 添加和去除元素的操作符

操作符 | 描述 | 集合类型
----- | ----- | -----
coll :+ elem <br> elem +: coll | 有elem被追加到尾部或头部的与coll类型相同的集合 | Seq
coll + elem <br> coll + (e1, e2, ...) | 添加了给定元素的与coll类型相同的集合 | Set、Map
coll - elem <br> coll - (e1, e2, ...) | 给定元素被移除的与coll类型相同的集合 | Set、Map、ArrayBuffer
coll ++ coll2 <br> coll2 ++: coll | 与coll类型相同的集合，包含了两个集合的元素 | Iterable
coll -- coll2 | 移除了coll2中元素的与coll类型相同的集合（用diff来处理序列） | Set、Map、ArrayBuffer
elem :: lst <br> lst2 ::: lst | 被向前追加了元素或给定列表的列表。和+:以及++:的作用相同 | List
list ::: list2 | 等同于list ++: list2 | List
set \| set2 <br> set & set2 <br> set &~ set2 | 并集、交集和两个集的差异。\|等于++，&~等于-- | Set
coll += elem <br> coll += (e1, e2, ...) <br> coll ++= coll2 <br> coll -= elem <br> coll -= (e1, e2, ...) <br> coll --= coll2 | 通过添加或移除给定元素来修改coll | 可变集合
elem +=: coll <br> coll2 ++=: coll | 通过向前追加给定元素或集合来修改coll | ArrayBuffer

<font color=red>
一般，+用于将元素添加到无先后次序的集合，而+:和:+则是将元素添加到有先后次序的集合的开头或末尾。
</font>

```
Vector(1, 2, 3) :+ 5 // Vector(1, 2, 3, 5)
1 +: Vector(1, 2, 3) // Vector(1, 1, 2, 3)
```

<font color=red>
和其他以冒号结尾的操作符一样，+:是右结合的。
</font>

**提示汇总如下**

  1. 向后（:+）或向前（+:）追加元素到序列当中。
  2. 添加（+）元素到无先后次序的集合中。
  3. 用-移除元素。
  4. 用++和--来批量添加和移除元素。
  5. 对于列表，优先使用::和:::。
  6. 改值操作用+=、++=、-=和--=。
  7. 对于集合，更喜欢++、&和--。
  8. 尽量不用++:、+=:和++=:。
  9. 对于列表可以用+:而不是::来保持与其他集合操作的一致性。但有一个例外：模式匹配（case h::t）不认+:操作符。

###13.6 常用方法

**Iterable特质**

方法 | 描述
----- | ------
head、last、headOption、lastOption | 返回第一个或最后一个元素，或者以Option返回
tail、init | 返回除第一个或最后一个元素外其余部分
length、isEmpty | 返回集合长度；或者，当长度为零时返回true
map(f)、foreach(f)、flatMap(f)、collect(pf) | 将函数用到所有元素
reduceLeft(op)、reduceRight(op)、foldLeft(init)(op)、foldRight(init)(op) | 以给定顺序将二元操作应用到所有元素
reduce(op)、fold(init)(op)、aggregate(init)(op, combineOp) | 以非特定顺序将二元操作应用到所有元素
sum、product、max、min | 返回和或乘积（前提元素类型可被隐式转换为Numeric特质）；或者最大值、最小值（前提可隐式转换成Ordered特质）
count(pred)、forall(pred)、exists(pred) | 返回满足前提表达式的元素计数；所有元素都满足时返回true；或者至少有一个元素满足时返回true。
filter(pred)、filterNot(pred)、partition(pred) | 返回所有满足前提表达式的元素；所有不满足的元素；或者，这两组元素组成的对偶
takeWhile(pred)、dropWhile(pred)、span(pred) | 返回满足前提表达式的一组元素（直到遇到第一个不满足的元素）；所有其他元素；或者，这两组元素组成的对偶
take(n)、drop(n)、splitAt(n) | 返回头n个元素；所有其他元素；或者，这两组元素组成的对偶
takeRight(n)、dropRight(n) | 返回最后n个元素，或者所有其他元素
slice(from, to) | 返回从from到to这个区间内的所有元素
zip(coll2)、zipAll(coll2, fill, fill2)、zipWithIndex | 返回由本集合元素和另一个集合的元素组成的对偶
grouped(n)、sliding(n) | 返回长度为n的子集迭代器；grouped产出下标为0 until n的元素，然后是下标为n until 2 * n的元素，以此类推；sliding产出下标为0 until n的元素，然后是下标为1 until n + 1的元素，以此类推
mkString(before, between, after)、addString(sb, before, between, after) | 做出一个有所有元素组成的字符串，将给定字符串分别添加到首个元素之前、每个元素之间，以及最后一个元素之后。第二个方法将该字符串追加到字符串构建器当中
toIterable、toSeq、toIndexedSeq、toArray、toList、toStream、toSet、toMap | 将集合转换成指定类型集合
copyToArray(arr)、copyToArray(arr, start, length)、copyToBuffer(buf) | 将元素拷贝到数组或缓冲当中

**Seq特质**

方法 | 描述
----- | ------
contains(elem)、containsSlice(seq)、startsWith(seq)、endsWith(seq) | 返回true，如果该序列：包含给定元素；包含给定序列；以给定序列开始；或者，以给定序列结束
indexOf(elem)、lastIndexOf(elem)、indexOfSlice(seq)、lastIndexOfSlice(seq) | 返回给定元素或序列在当前序列中的首次或末次出现的下标
indexWhere(pred) | 返回满足pred的首个元素的下标
prefixLength(pred)、segmentLength(pred, n) | 返回满足pred的最长元素序列的长度，从当前序列的下标0或n开始查找
padTo(n, fill) | 返回当前序列的一个拷贝，将fill的内容向后追加，直到新序列长度达到n
intersect(seq)、diff(seq) | 返回交集；或者差值
reverse | 当前序列反向
sorted、sortWith(less)、sortBy(f) | 使用元素本身的大小、二元函数less，或者将每个元素映射成一个带先后次序的类型的值得函数f，对当前序列进行排序后的新序列
premutations、combinations(n) | 返回一个遍历所有排列或组合（长度为n的子序列）的迭代器

###13.7 将函数映射到集合

```
val names = List("Peter", "Paul", "Mary")

names.map(_.toUpperCase) // 结果：List("PETER", "PAUL", "MARY")
// 等效于以下操作
for (n <- names) yield n.toUpperCase

// 将所有值连接到一块
def ulcase(s: String) = Vector(s.toUpperCase(), s.toLowerCase())

names.map(ulcase)
// 得到 List(Vector("PETER", "peter"), Vector("PAUL", "paul"), Vector("MARY", "mary"))

names.flatMap(ulcase)
// 得到 List("PETER", "peter", "PAUL", "paul", "MARY", "mary")
```

collect方法用于偏函数（partial function）

```
"-3+4".collect { case '+' => 1; case '-' => -1 }
// 得到 Vector(-1, 1)
```

foreach

```
names.foreach(println)
```

###13.8 化简、折叠和扫描

主要说明方法用二元函数来组合集合中的元素。

```
List(1, 7, 3, 9).reduceLeft(_ - _)
// 相当于 ((1 - 7) - 2) - 9 = -17

List(1, 7, 3, 9).reduceRight(_ - _)
// 相当于 1 - (7 - (2 - 9)) = -13

// 以不同于集合首元素的初始元素计算通常也很有用
List(1, 7, 2, 9).foldLeft(0)(_ - _)
// 相当于(((0 - 1) - 7) - 2) - 9
// 也可以像下面这样代替foldLeft操作
(0 /: List(1, 7, 2, 9))(_ - _)

// 同样也提供了foldRight或:\

// scanLeft和scanRight方法将折叠和映射操作结合在一起
scala> (1 to 10).scanLeft(0)(_ + _)
res0: scala.collection.immutable.IndexedSeq[Int] = Vector(0, 1, 3, 6, 10, 15, 21, 28, 36, 45, 55)

scala> (1 to 10).scanRight(0)(_ + _)
res1: scala.collection.immutable.IndexedSeq[Int] = Vector(55, 54, 52, 49, 45, 40, 34, 27, 19, 10, 0)
```

###13.9 拉链操作

拉链操作指的是两个集合，把它们相互对应的元素结合在一起。

```
val prices = List(5.0, 20.0, 9.95)
val quantities = List(10, 2, 1)

// zip方法将它们组合成一个对偶的列表
prices zip quantities
// 得到List[(Double, Int)] = List((5.0, 10), (20.0, 2), (9.95, 1))

// 获得价格的列表
(prices zip quantities) map { p => p._1 * p._2 }
// 所有物件的总价
((prices zip quantities) map { p => p._1 * p._2 }).sum

// 如果元素长度不等，则返回最终元素的个数与较短的一致

// zipAll方法可以指定较短列表的缺省值
List(5.0, 20.1, 9.4).zipAll(List(10, 2), 1.2, 1)
// 其中zipAll第二个参数用于填充前面的列表；而第三个参数填充后面的列表

// zipWithIndex返回对偶列表，其中每个对偶中第二个组成部分是每个元素的下标
"Scala".zipWithIndex
res4: scala.collection.immutable.IndexedSeq[(Char, Int)] = Vector((S,0), (c,1), (a,2), (l,3), (a,4))
```

###13.10 迭代器

可以用iterator方法从集合获得一个迭代器。

对于完整构造需要很大开销的集合而言，迭代器很有用。

Source.fromFile产出一个迭代器。另外Iterable中的一些方法（grouped或sliding）产生迭代器。

###13.11 流

流（stream）提供的是一个不可变的替代品，是一个尾部被懒计算的不可变列表。

```
def numsFrom(n: BigInt): Stream[BigInt] = n #:: numsFrom(n + 1)
// 通过#::构建流

val tenOrMore = numsFrom(10)
// 返回 Stream(10, ?)
tenOrMore.tail.tail.tail
// 返回 Stream(13, ?)

// 强制求所有值
tenOrMore.take(5).force

// 可以从迭代器构造一个流
val words = Source.fromFile("/usr/share/dict/words").getLines.toStream
words // Stream(A, ?)
words(5) // Aachen
words // Stream(A, A's, AOL, AOL's, Aachen, ?)

// 迭代器对于每行只能访问一次，而流将缓存访问过的行，允许重新访问
```

###13.12 懒视图

使用view方法，同样可以获得一个被懒执行的集合。

```
import scala.math._
val powers = (0 until 1000).view.map(pow(10, _))
// powers: scala.collection.SeqView[Double,Seq[_]] = SeqViewM(...)

powers(2)
// res12: Double = 100.0

// 也可以用force方法进行强制求值。
```

###13.13 与Java集合的互操作

**从Scala集合到Java集合的转换**

隐式函数 | scala.collection类型 | java.util类型
---- | ----- | -----
asJavaCollection | Iterable | Collection
asJavaIterable | Iterable | Iterable
asJavaIterator | Iterator | Iterator
asJavaEnumeration | Iterator | Enumeration
seqAsJavaList | Seq | List
mutableSeqAsjavaList | mutable.Seq | List
bufferAsJavaList | mutable.Buffer | List
setAsJavaSet | Set | Set
mutableSetAsJavaSet | mutable.Set | Set
mapAsJavaMap | Map | Map
mutableMapAsJavaMap | mutable.Map | Map
asJavaDictionary | Map | Dictionary
asJavaConcurrentMap | mutable.ConcurrentMap | concurrent.ConcurrentMap

**从Java集合到Scala集合的转换**

隐式函数 | java.util类型 | scala.collection类型
---- | ----- | -----
collectionAsScalaIterable | Collection | Iterable
IterableAsScalaIterable | Iterable | Iterable
asScalaIterator | Iterator | Iterator
enumerationAsScalaIterator | Enumeration | Iterator
asScalaBuffer | List | mutable.Buffer
asScalaSet | Set | mutable.Set
mapAsScalaMap | Map | mutable.Map
dictionaryAsScalaMap | Dictionary | mutable.Map
propertiesAsScalaMap | Properties | mutable.Map
asScalaConcurentMap | concurrent.ConcurrentMap | mutable.ConcurrentMap

###13.14 线程安全的集合

Scala类库提供了六个特质，可以将它们混入集合，让集合的操作变成同步的：

```
SynchronizedBuffer
SynchronizedMap
SynchronizedPriorityQueue
SynchronizedQueue
SynchronizedSet
SynchronizedStack
```

示例

```
val scores = new scala.collection.mutable.HashMap[String, Int] with scala.collection.mutable.SynchronizedMap[String, Int]
```

###13.15 并行集合

par方法产出集合的一个并行实现。

```
// 通过par并行化for循环，结果是按线程产出的顺序输出的
scala> for (i <- (0 until 100).par) print(i + " ")
50 25 0 75 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 26 27 28 29 30 31 32 33 34 35 36 37 51 52 53 54 55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 38 39 40 41 42 43 44 45 46 47 48 49 87 88 89 90 91 92 93 94 95 96 97 98 99 81 82 83 84 85 86 78 79 80 76 77

// 在for／yield循环中，结果是依次组装的
scala> for (i <- (0 until 100).par) yield i + " "
res15: scala.collection.parallel.immutable.ParSeq[String] = ParVector(0 , 1 , 2 , 3 , 4 , 5 , 6 , 7 , 8 , 9 , 10 , 11 , 12 , 13 , 14 , 15 , 16 , 17 , 18 , 19 , 20 , 21 , 22 , 23 , 24 , 25 , 26 , 27 , 28 , 29 , 30 , 31 , 32 , 33 , 34 , 35 , 36 , 37 , 38 , 39 , 40 , 41 , 42 , 43 , 44 , 45 , 46 , 47 , 48 , 49 , 50 , 51 , 52 , 53 , 54 , 55 , 56 , 57 , 58 , 59 , 60 , 61 , 62 , 63 , 64 , 65 , 66 , 67 , 68 , 69 , 70 , 71 , 72 , 73 , 74 , 75 , 76 , 77 , 78 , 79 , 80 , 81 , 82 , 83 , 84 , 85 , 86 , 87 , 88 , 89 , 90 , 91 , 92 , 93 , 94 , 95 , 96 , 97 , 98 , 99 )
```

par方法返回的并行集合的类型为扩展自ParSeq、ParSet或ParMap特质的类型，所有这些特质都是ParIterable的子类型。但这些并不是Iterable的子类型，因此不能讲并行集合传递给预期Iterable、Seq、Set或Map的方法。

###13.16 习题解答

<font size =4 color=Blue>
1. 编写一个函数，给定字符串，产出一个包含所有字符的下标的映射。举例来说：indexes(“Mississippi”)应返回一个映射，让’M’对应集{0}，’i’对应集{1,4,7,10}，依此类推。使用字符到可变集的映射。另外，你如何保证集是经过排序的？
</font>

```
import scala.collection.mutable

object One {

    def indexes(str: String): mutable.HashMap[Char, mutable.SortedSet[Int]] = {
        val map = new mutable.HashMap[Char, mutable.SortedSet[Int]]
        var i = 0
        str.foreach {
            c =>
                map.get(c) match {
                    case Some(value) => map(c) = value + i
                    case None => map += (c -> mutable.SortedSet {
                        i
                    })
                }
                i += 1
        }

        map
    }

    def main(args: Array[String]) {
        println(indexes("Mississippi"))
    }
}

// 结果
Map(M -> TreeSet(0), s -> TreeSet(2, 3, 5, 6), p -> TreeSet(8, 9), i -> TreeSet(1, 4, 7, 10))
```

<font size =4 color=Blue>
2. 重复前一个练习，这次用字符到列表的不可变映射。
</font>

```
import scala.collection.{immutable, mutable}

object Two {

    def indexes(str: String): mutable.HashMap[Char, immutable.ListSet[Int]] = {
        val map = new mutable.HashMap[Char, immutable.ListSet[Int]]
        var i = 0
        str.foreach {
            c =>
                map.get(c) match {
                    case Some(value) => map(c) = value + i
                    case None => map += (c -> immutable.ListSet {
                        i
                    })
                }
                i += 1
        }
        map
    }

    def main(args: Array[String]) {
        println(indexes("Mississippi"))
    }
}

// 结果
Map(M -> ListSet(0), s -> ListSet(6, 5, 3, 2), p -> ListSet(9, 8), i -> ListSet(10, 7, 4, 1))
```

<font size =4 color=Blue>
3. 编写一个函数，从一个整型链表中去除所有的零值。
</font>

```
object Three {

    def removeZero(list: List[Int]): List[Int] = {
        list.filter(_ != 0)
    }

    def main(args: Array[String]) {
        println(removeZero(List(3, 25, 0, 2, 0, 0)))
    }
}
```

<font size =4 color=Blue>
4. 编写一个函数，接受一个字符串的集合，以及一个从字符串到整数值的映射。返回整型的集合，其值为能和集合中某个字符串相对应的映射的值。举例来说，给定Array(“Tom”,”Fred”,”Harry”)和Map(“Tom”->3,”Dick”->4,”Harry”->5)，返回Array(3,5)。提示：用flatMap将get返回的Option值组合在一起
</font>

```
object Four {

    def filterMap(arr: Array[String], map: Map[String, Int]) = {
        // map.get返回Some(v), 才会被返回
        arr.flatMap(map.get)
    }

    def main(args: Array[String]) {
        println(filterMap(Array("Tom", "Fred", "Harry"),
            Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5)).mkString(","))
    }
}
```

<font size =4 color=Blue>
5. 实现一个函数，作用与mkString相同，使用reduceLeft。
</font>

```
import scala.collection.mutable

trait MktString {
    this: mutable.Iterable[String] =>
    def mktString(split: String = "") = if (this != Nil) this.reduceLeft(_ + split + _)
}

object Five {

    def main(args: Array[String]) {
        var test = new scala.collection.mutable.HashSet[String] with MktString
        test += "1"
        test += "2"
        test += "3"
        println(test.mktString(","))
    }
}

// 结果
3,1,2
```

<font size =4 color=Blue>
6. 给定整型列表lst，(lst :\ List\[Int\]())(\_ :: \_)得到什么?(List\[Int\]() /: lst)(\_ :+ \_)又得到什么？如何修改它们中的一个，以对原列表进行反向排序？
</font>

```
object Six {

    def main(args: Array[String]) {
        val lst = List(1, 2, 3, 4, 5)
        // foldRight方法, List[Int]()是初始值(为空)被增加到右边
        // :: 与 +:相同, 头部添加
        println((lst :\ List[Int]()) (_ :: _))
        // foldLeft方法, List[Int]()是初始值(为空)被增加到左边
        // :+ 尾部添加
        println((List[Int]() /: lst) (_ :+ _))
        println((List[Int]() /: lst) ((a, b) => b :: a))
    }
}

// 结果
List(1, 2, 3, 4, 5)
List(1, 2, 3, 4, 5)
List(5, 4, 3, 2, 1)
```

<font size =4 color=Blue>
7. 在13.11节中，表达式(prices zip quantities) map { p => p.1 * p._2}有些不够优雅。我们不能用(prices zip quantities) map { _ * _ }，因为 _ * _ 是一个带两个参数的函数，而我们需要的是一个带单个类型为元组的参数的函数，Function对象的tupled方法可以将带两个参数的函数改为以元组为参数的函数。将tupled应用于乘法函数，以使我们可以用它来映射由对偶组成的列表。
</font>

```
object Seven {
    def main(args: Array[String]) {
        val prices = List(5.0, 20.0, 9.95)
        val quantities = List(10, 2, 1)
        println((prices zip quantities) map {
            Function.tupled(_ * _)
        })

    }
}
```

<font size =4 color=Blue>
8. 编写一个函数。将Double数组转换成二维数组。传入列数作为参数。举例来说，Array(1,2,3,4,5,6)和三列，返回Array(Array(1,2,3),Array(4,5,6))。用grouped方法。
</font>

```
object Eight {

    def toMultiDim(arr: Array[Double], n: Int): Array[Array[Double]] = {
        // grouped产出下标为[0, n)的元素,然后是[n, 2*n)的元素
        arr.grouped(n).toArray
    }

    def main(args: Array[String]) {
        val arr = Array(1.0, 2, 3, 4, 5, 6)
        toMultiDim(arr, 3).foreach(a => println(a.mkString(",")))
    }
}
```

<font size =4 color=Blue>
9. Harry Hacker写了一个从命令行接受一系列文件名的程序。对每个文件名，他都启动一个新的线程来读取文件内容并更新一个字母出现频率映射，声明为：

```
val frequencies = new scala.collection.multable.HashMap[Char,Int] 
     with scala.collection.mutable.SynchronizedMap[Char,Int]
```

当读到字母c时，他调用

```
frequencies(c) = frequencies.getOrElse(c,0) + 1
```

为什么这样做得不到正确答案？如果他用如下方式实现呢：

```
import scala.collection.JavaConversions.asScalaConcurrentMap

val frequencies:scala.collection.mutable.ConcurrentMap[Char,Int] = new java.util.concurrent.ConcurrentHashMap[Char,Int]
```
</font>

```
并发修改集合不安全。
```

<font size =4 color=Blue>
10. Harry Hacker把文件读取到字符串中，然后想对字符串的不同部分用并行集合来并发地更新字母出现频率映射。他用了如下代码：

```
val frequencies = new scala.collection.mutable.HashMap[Char,Int]
for(c <- str.par) frequencies(c) = frequencies.getOrElse(c,0) + 1
```

为什么说这个想法很糟糕？要真正地并行化这个计算，他应该怎么做呢？（提示：用aggregate）
</font>

并行修改共享变量，结果无法估计。

```
object Ten {
    def main(args: Array[String]) {
        val str = "abdcsdcd"
        // aggregate将操作符应用于集合的不同部分
        val frequencies = str.par.aggregate(HashMap[Char, Int]())(
            {
                (a, b) =>
                    a + (b -> (a.getOrElse(b, 0) + 1))
            }
            , {
                (map1, map2) =>
                    (map1.keySet ++ map2.keySet).foldLeft(HashMap[Char, Int]()) {
                        (result, k) =>
                            result + (k -> (map1.getOrElse(k, 0) + map2.getOrElse(k, 0)))
                    }
            }
        ).foreach(println)
    }
}

// 结果
(s,1)
(a,1)
(b,1)
(c,2)
(d,3)
```

###第14章 模式匹配和样例类
###14.1 模式匹配

**基本用法**

```
str(i) match {
    case '+' => sign = 1
    case '-' => sign = -1
    // 可以增加守卫，同样也可以用变量
    case ch if Character.isDigit(ch) => digit = Character.digit(ch, 10)
    case _ => sign = 0  // 与switch的default等效
}

// 如果没有模式能匹配，代码会抛出MatchError

// match也是表达式，而不是语句
sign = ch match {
    case '+' => 1
    case '-' => -1
    case _ => 0
}
```

变量模式可能会与常量表达式冲突，例如：

```
import scala.math._

x match {
    case Pi => ... // Pi是常量，变量必须以小写字母开头
    case `test` => ... // 如果有一个小写字母开头的常量，需要将它包仔反引号中
    ...
}
```

**类型模式**

```
obj match {
    case x: Int => x
    case s: String => Integer.parseInt(s)
    case m: Map[String, Int] => ... // 由于泛型在Java虚拟机中会被擦掉，所以别这样做
    case m: Map[_, _] => ... // OK
    case _: BigInt => Int.maxValue
    case _ => 0
}
```

**匹配数组、列表或元组**

数组

```
arr match {
    case Array(0) => "0"  // 匹配包含0的数组
    case Array(x, y) => x + " " + y // 匹配任何带有两个元素的数组，并绑定变量
    case Array(0, _*) => "0 ..." // 匹配任何以零开始的数组
    case _ => "something else"
}
```

列表

```
lst match {
    case 0 :: Nil => "0"
    case x :: y :: Nil => x + " " + y
    case 0 :: tail => "0 ..."
    case _ => "something else"
}
```

元组

```
pair match {
    case (0, _) => "0 ..."
    case (y, 0) => y + " 0"
    case _ => "neither is 0"
}
```

**提取器**

模式是如何匹配数组、列表和元组的，背后是提取器（extractor）机制，即带有从对象中提取值的unapply或unapplySeq方法的对象。

正则表达式是另一个适合使用提取器的场景。

```
val pattern = "([0-9]+) ([a-z]+)".r
"99 bottles" match {
    case pattern(num, item) => ...
}
```

**变量声明中的模式**

```
scala> val (x, y) = (1, 2)
x: Int = 1
y: Int = 2

scala> val (q, r) = BigInt(10) /% 3
q: scala.math.BigInt = 3
r: scala.math.BigInt = 1

scala> val Array(first, second, _*) = Array(1, 2, 3, 4, 5)
first: Int = 1
second: Int = 2
```

**for表达式中的模式**

```
import scala.collection.JavaConversions.propertiesAsScalaMap

for ((k, v) <- System.getProperties()) println(k + " -> " + v)

// 打印值为空白的键
for ((k, "") <- System.getProperties()) println(k)
// 同样可以使用守卫
for ((k, v) <- System.getProperties() if v == "") println(k)
```

###14.2 样例类

样例类是一种特殊的类，经过优化已被用于模式匹配。

**基本用法**

```
// 扩展自常规类的样例类
abstract class Amount
case class Dollar(value: Double) extends Amount
case class Currency(value: Double, unit: String) extends Amount

// 针对单例的样例对象
case object Nothing extends Amount

// 示例
amt match {
    case Dollar(v) => "$" + v
    case Currency(_, u) => "Oh noes, I got " + u
    case Nothing => ""
}
```

当声明样例类时，如下几件事自动发生：

  1. 构造器中的每一个参数都成为val－－除非它呗显示地声明为var。
  2. 在伴生对象中提供apply方法以便不用new关键字创建对象。
  3. 提供unapply方法让模式匹配可以工作
  4. 将生成toString、equals、hashCode和copy方法

**copy方法和带名参数**

```
val amt = Currency(29.94, "EUR")
// copy方法创建一个与现有对象相同的新对象
val price = amt.copy()

// 也可以用带名参数来修改某些属性
val price = amt.copy(value = 19.21)
```

**case语句中的中值表示法**

如果unapply方法产出一个对偶，则可以在case语句中使用中置表示法。

```
case class ::[E](head: E, tail: List[E]) extends List[E]

lst match { case h :: t => ... }
// 等同于case ::(h, t)，将调用::.unapply(lst)
```

**匹配嵌套结构**

例如，某个商店售卖的物品。有时，我们会将物品捆绑在一起打折销售

```
abstract class Item
case class Article(description: String, price: Double) extends Iten
case class Bundle(description: String, discount: Double, item: Item*) extends Item

// 使用
Bundle("Father's day special", 20.0,
  Article("Scala for the Impatient", 39.95),
  Bundle("Anchor Distillery Sampler", 10.0)
    Article("Old potrero Straight Rye Whisky", 69.21),
    Article("Junipero Gin", 79.95),

// 模式可以匹配到特定的嵌套
case Bundle(_, _, Article(descr, _), _*) => ...

// 可以使用@表示法将嵌套的值绑到变量
// 以下是一个计算某Item价格的函数
def price(it: Item): Double = it match {
    case Article(_, p) => p
    case Bundle(_, disc, its @ _*) => its.map(price _).sum - disc
}
```

**密封类**

超类声明为sealed，就是密封类，其子类都必须在与该密封类相同的文件中定义。而且在编译期所有子类都是可知的，因而编译器可以检查模式语句的完整性。

```
sealed abstract class Amount
case class Dollar(value: Double) extends Amount
case class Currency(value: Double, unit: String) extends Amount
```

**模拟枚举**

```
sealed abstract class TrafficLightColor
case object Red extends TrafficLightColor
case object Yellow extends TrafficLightColor
case object Green extends TrafficLightColor

color match {
    case Red => "stop"
    case Yellow => "hurry up"
    case Green => "go"
}
```

###14.3 Option类型

标准类库中的Option类型用样例类来表示那种可能存在、也可能不存在的值。

样例子类Some包装了某个值，例如：Some("Fred")。而样例对象None表示没有值。

Option支持泛型。例如Some("Fred")的类型为Option[String]。

###14.4 偏函数

被包在花括号内的一组case语句是一个便函数，即一个并非对所有输入值都有定义的函数。它是PartialFunction[A, B]类的一个实例。A是参数类型，B是返回类型。该类有两个方法：apply方法从匹配到的模式计算函数值，而isDefinedAt方法在输入至少匹配其中一个模式时返回true。

```
val f: PartialFunction[Char, Int] = { case '+' => 1; case '-' => -1 }
f('-')  // 调用f.apply('-')，返回-1
f.isDefinedAt('0') // false
f('0') // 抛出MatchError
```

###14.5 习题解答

<font size =4 color=Blue>
1. JDK发行包有一个src.zip文件包含了JDK的大多数源代码。解压并搜索样例标签(用正则表达式case [^:]+:)。然后查找以//开头并包含[Ff]alls?thr的注释，捕获类似// Falls through或// just fall thru这样的注释。假定JDK的程序员们遵守Java编码习惯，在该写注释的地方写下了这些注释，有多少百分比的样例是会掉入到下一个分支的？
</font>

略

<font size =4 color=Blue>
2. 利用模式匹配，编写一个swap函数，接受一个整数的对偶，返回对偶的两个组成部件互换位置的新对偶
</font>

```
object Two extends App {
    def swap[S, T](tup: (S, T)) = {
        tup match {
            case (a, b) => (b, a)
        }
    }

    println(swap[String, Int](("1", 2)))
}
```

<font size =4 color=Blue>
3. 利用模式匹配，编写一个swap函数，交换数组中的前两个元素的位置，前提条件是数组长度至少为2
</font>

```
object Three extends App {

    def swap(arr: Array[Any]) = {
        arr match {
            case Array(first, second, rest@_*) => Array(second, first) ++ rest
            case _ => arr
        }
    }

    println(swap(Array("1", "2", "3", "4")).mkString(","))
}

// 结果
2,1,3,4
```

<font size =4 color=Blue>
4. 添加一个样例类Multiple，作为Item的子类。举例来说，Multiple(10,Article(“Blackwell Toster”,29.95))描述的是10个烤面包机。当然了，你应该可以在第二个参数的位置接受任何Item，无论是Bundle还是另一个Multiple。扩展price函数以应对新的样例。
</font>

```
object Four extends App {

    abstract class Item

    case class Multiple(num: Int, item: Item) extends Item

    case class Article(description: String, price: Double) extends Item

    case class Bundle(description: String, discount: Double, item: Item*) extends Item

    def price(it: Item): Double = it match {
        case Article(_, a) => a
        case Bundle(_, disc, its@_*) => its.map(price).sum - disc
        case Multiple(n, t) => n * price(t)
    }

    val p = price(Multiple(10, Article("Blackwell Toster", 29.95)))
    println(p)
}

// 结果
299.5
```

<font size =4 color=Blue>
5. 我们可以用列表制作只在叶子节点存放值的树。举例来说，列表((3 8) 2 (5))描述的是如下这样一棵树:

```
     .
   / | \
  .  2  .
 / \    |
3   8   5
```

不过，有些列表元素是数字，而另一些是列表。在Scala中，你不能拥有异构的列表，因此你必须使用List[Any]。编写一个leafSum函数，计算所有叶子节点中的元素之和，用模式匹配来区分数字和列表。
</font>

```
object Five extends App {
    def leafSum(list: List[Any]): Int = {
        var total = 0
        list.foreach {
            case l: List[Any] => total += leafSum(l)
            case i: Int => total += i
        }
        total
    }

    val l: List[Any] = List(List(3, 8), 2, List(5))
    println(leafSum(l))
}

// 结果
18
```

<font size =4 color=Blue>
6. 制作这样的树更好的做法是使用样例类。我们不妨从二叉树开始。

```
sealed abstract class BinaryTree
case class Leaf(value : Int) extends BinaryTree
case class Node(left : BinaryTree,right : BinaryTree) extends BinaryTree
```

编写一个函数计算所有叶子节点中的元素之和。
</font>

```
object Six extends App {

    sealed abstract class BinaryTree

    case class Leaf(value: Int) extends BinaryTree

    case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree

    def leafSum(tree: BinaryTree): Int = {
        tree match {
            case Node(a, b) => leafSum(a) + leafSum(b)
            case Leaf(v) => v
        }
    }

    val r = Node(Leaf(3), Node(Leaf(3), Leaf(9)))
    println(leafSum(r))
}

// 结果
15
```

<font size =4 color=Blue>
7. 扩展前一个练习中的树，使得每个节点可以有任意多的后代，并重新实现leafSum函数。第五题中的树应该能够通过下述代码表示：

```
Node(Node(Leaf(3),Leaf(8)),Leaf(2),Node(Leaf(5)))
```
</font>

```
object Seven extends App {

    sealed abstract class BinaryTree

    case class Leaf(value: Int) extends BinaryTree

    case class Node(tr: BinaryTree*) extends BinaryTree

    def leafSum(tree: BinaryTree): Int = {
        tree match {
            case Node(r @ _*) => r.map(leafSum).sum
            case Leaf(v) => v
        }
    }

    val r = Node(Node(Leaf(3), Leaf(8)), Leaf(2), Node(Leaf(5)))
    println(leafSum(r))
}

// 结果
18
```

<font size =4 color=Blue>
8. 扩展前一个练习中的树，使得每个非叶子节点除了后代之外，能够存放一个操作符。然后编写一个eval函数来计算它的值。举例来说：

```
     +
   / | \
  *  2  -
 / \    |
3   8   5
```

上面这棵树的值为(3 * 8) + 2 + (-5) = 21
</font>

```
object Eight extends App {

    sealed abstract class BinaryTree

    case class Leaf(value: Int) extends BinaryTree

    case class Node(op: Char, leafs: BinaryTree*) extends BinaryTree

    def eval(tree: BinaryTree): Int = {
        tree match {
            case Node(op, leafs@_*) => op match {
                case '+' => leafs.map(eval).sum
                case '-' => -leafs.map(eval).sum
                case '*' => leafs.map(eval).product
            }
            case Leaf(v) => v
        }
    }

    val x = Node('+', Node('*', Leaf(3), Leaf(8)), Leaf(2), Node('-', Leaf(5)))
    println(x)
    println(eval(x))
}

// 结果
Node(+,WrappedArray(Node(*,WrappedArray(Leaf(3), Leaf(8))), Leaf(2), Node(-,WrappedArray(Leaf(5)))))
21
```

<font size =4 color=Blue>
9. 编写一个函数，计算List[Option[Int]]中所有非None值之和。不得使用match语句。
</font>

```
object Nine extends App {
    def sum(lst: List[Option[Int]]) = lst.map(_.getOrElse(0)).sum

    val x = List(Some(1), None, Some(2), None, Some(3))
    println(sum(x))
}
```

<font size =4 color=Blue>
10. 编写一个函数，将两个类型为Double=>Option[Double]的函数组合在一起，产生另一个同样类型的函数。如果其中一个函数返回None，则组合函数也应返回None。例如：

```
def f(x : Double) = if ( x >= 0) Some(sqrt(x)) else None
def g(x : Double) = if ( x != 1) Some( 1 / ( x - 1)) else None
val h = compose(f,g)
```

h(2)将得到Some(1)，而h(1)和h(0)将得到None
</font>

```
object Ten extends App {
    def compose(f: Double => Option[Double], g: Double => Option[Double]) = {
        (x: Double) =>
            if (f(x).isEmpty || g(x).isEmpty) None
            else g(x)
    }

    import scala.math.sqrt

    def f(x: Double) = if (x >= 0) Some(sqrt(x)) else None

    def g(x: Double) = if (x != 1) Some(1 / (x - 1)) else None

    val h = compose(f, g)
    println(h(0))
    println(h(1))
    println(h(2))
}

// 结果
Some(-1.0)
None
Some(1.0)
```

##参考

[习题解答](https://github.com/vernonzheng/scala-for-the-Impatient)