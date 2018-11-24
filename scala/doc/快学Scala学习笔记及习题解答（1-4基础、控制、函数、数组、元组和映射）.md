本文scala使用的版本是2.11.7

##第一章 基础
###1.1 基础

**常量和变量**

以val定义的值是一个常量，即无法改变它的内容：

```
scala> val counter: Int = 0
counter: Int = 0
```

以var定义的值是变量，可以改变它的内容：

```
scala> var v = 0
v: Int = 0

scala> v = 10
v: Int = 10
```

**常用类型**

7种数值类型：Byte、Char、Short、Int、Long、Float和Double，以及1个Boolean类型。而且它们的类型都是类。

RichInt、RichDouble、RichChar等，提供了它们堂兄弟——Int、Double、Char等所不具备的便捷方法。

BigInt和BigDecimal类，用于任意大小（但有穷）的数字。这些类分别对应java.math.BigInteger和java.math.BigDecimal。

Scala用底层的java.lang.String类来表示字符串。不过，它通过StringOps类给字符串追加了上百种操作。

**操作符重载**

```
a.方法(b)
可以简写为
a 方法 b

例如：
a.+(b)
可以简写为
a + b
```

**函数和方法**

不带参数的scala方法通常不使用圆括号。例如：

```
"Hello".distinct
```

###1.2 习题解答

http://ivaneye.iteye.com/blog/1815550

<font size =4 color=Blue>
1. 在Scala REPL中键入3, 然后按Tab键。有哪些方法可以被应用?
</font>
这个。。。。直接操作一遍就有结果了.此题不知是翻译的问题，还是原题的问题，在Scala REPL中需要按<font color=red>3.</font> 然后按Tab才会提示。 直接按3加Tab是没有提示的。下面是结果

```scala
!=             ##             %              &              *              +  
-              /              <              <<             <=             ==  
>              >=             >>             >>>            ^              asInstanceOf  
equals         getClass       hashCode       isInstanceOf   toByte         toChar  
toDouble       toFloat        toInt          toLong         toShort        toString  
unary_+        unary_-        unary_~        |  
```

列出的方法并不全，需要查询全部方法还是需要到Scaladoc中的Int,Double,RichInt,RichDouble等类中去查看。

<font size =4 color=Blue>
2. 在Scala REPL中，计算3的平方根,然后再对该值求平方。现在，这个结果与3相差多少？(提示:res变量是你的朋友) 
</font>

```scala
scala> math.sqrt(3)
res18: Double = 1.7320508075688772

scala> 3 - res18 * res18
res19: Double = 4.440892098500626E-16
```

<font size =4 color=Blue>
3. res变量是val还是var? 
</font>
val是不可变的，而var是可变的，只需要给res变量重新赋值就可以检测res是val还是var了 

```scala
scala> res19 = 3
<console>:12: error: reassignment to val
       res19 = 3
             ^
```

<font size =4 color=Blue>
4. Scala允许你用数字去乘字符串—去REPL中试一下"crazy"*3。这个操作做什么？在Scaladoc中如何找到这个操作? 
</font>

```
scala> "crazy"*3
res20: String = crazycrazycrazy
```

此方法在StringOps中。 
> def *(n: Int): String
Return the current string concatenated n times.

> **Definition Classes:** StringLike

<font size =4 color=Blue>
5. 10 max 2的含义是什么？max方法定义在哪个类中？
</font>

```
scala> 10 max 2
res21: Int = 10

scala> 2 max 10
res22: Int = 10
```

> def max(that: Int): Int
Returns this if this > that or that otherwise.

> **Implicit information:** This member is added by an implicit conversion from Int to RichInt performed by method intWrapper in scala.LowPriorityImplicits.

> **Definition Classes:** RichInt → ScalaNumberProxy

<font size =4 color=Blue>
6. 用BigInt计算2的1024次方 
</font>

```scala
scala> BigInt(2).pow(1024)
res23: scala.math.BigInt = 179769313486231590772930519078902473361797697894230657273430081157732675805500963132708477322407536021120113879871393357658789768814416622492847430639474124377767893424865485276302219601246094119453082952085005768838150682342462881473913110540827237163350510684586298239947245938479716304835356329624224137216
```

<font size =4 color=Blue>
7. 为了在使用probablePrime(100,Random)获取随机素数时不在probablePrime和Radom之前使用任何限定符，你需要引入什么？ 
</font>

```
import scala.math.BigInt._  
import scala.util.Random  
  
probablePrime(3, Random)
```

<font size =4 color=Blue>
8. 创建随机文件的方式之一是生成一个随机的BigInt，然后将它转换成三十六进制，输出类似"qsnvbevtomcj38o06kul"这样的字符串。查阅Scaladoc，找到在Scala中实现该逻辑的办法。 
到BigInt中查找方法。 
</font>

```scala
scala> BigInt(util.Random.nextInt).toString(36)
res26: String = vfhdsd
```

> def toString(radix: Int): String
Returns the String representation in the specified radix of this BigInt.

<font size =4 color=Blue>
9. 在Scala中如何获取字符串的首字符和尾字符？
</font>

```scala
//获取首字符  
"Hello"(0)  
"Hello".take(1)  
//获取尾字符  
"Hello".reverse(0)  
"Hello".takeRight(1) 
```

<font size =4 color=Blue>
10. take,drop,takeRight和dropRight这些字符串函数是做什么用的？和substring相比，他们的优点和缺点都是哪些？ 
</font>
take是从字符串首开始获取字符串；drop是从字符串首开始去除字符串。
takeRight和dropRight是从字符串尾开始操作。 
这四个方法都是单方向的。 如果我想要字符串中间的子字符串，那么需要同时调用drop和dropRight，或者使用substring 

```scala
scala> "Hello".take(2)
res31: String = He

scala> "Hello".drop(3)
res32: String = lo

scala> "Hello".takeRight(3)
res33: String = llo

scala> "Hello".dropRight(2)
res34: String = Hel

scala> "Hello".substring(2,3)
res35: String = l
```

##第二章 控制结构和函数
###2.1 控制结构

**条件表达式**

在Scala中if/else表达式是有值的，即跟在if或else之后的表达式的值。

```
val v = if (x > 0) "positive" else -1

// 其中一个分支是java.lang.String，而另一个分支是Int。它们的公共超类型叫做Any。
```

Unit类型，写做()，可以当做Java或C++中的void。

```
if (x > 1) 1

等价于
if (x > 1) 1 else ()
```

**语句终止**

在Scala中，行尾不需要分号。如果单行中写了多个语句，就需要用分号隔开。

**块表达式**

在Scala中，{}块包含一系列表达式，其结果也是一个表达式。块中最后一个表达式的值就是块的值。

赋值语句的值是Unit类型，所以不要串接赋值。例如下面是不行的：

```
x = y = 1
```

**输入输出**

打印使用print或println函数，而后者会追加一个换行。也可以用printf来格式化字符串。

```
printf("Hello, %s! You are %d years old.\n", "Fred", 42)
```

可以用readLine函数从控制台读取一行输入。如果读取数字、Boolean或者字符，可以用readInt、readDouble、readByte、readShort、readLong、readFloat、readBoolean或readChar。与其他方法不同，readLine带一个参数作为提示字符串：

```
val name = readLine("Your name: ")
```

**循环**

Scala拥有与Java和C++相同的while和do循环。例如：

```
while (n > 0) {
    r = r * n
    n -= 1
}
```

for语句有些不一样，`for (i <- 表达式)`让变量i遍历<-右边的表达式的所有值。

```
val s = "Hello"
var sum = 0
for (i <- 0 until s.lenght) sum += s(i)

// until方法返回一个不包含上限的区间，如果用to方法，则包含上限。
```

可以以`变量 <- 表达式`的形式提供多个生成器，用分号将它们隔开。

```
for (i <- 1 to 3; j <- 1 to 3) print((10 * i + j) + " ")
// 结果：11 12 13 21 22 23 31 32 33
```

每个生成器都可以带一个守卫，以if开头的Boolean表达式：

```
for (i <- 1 to 3; j <- 1 to 3 if i != j) print((10 * i + j) + " ")
// 结果：12 13 21 23 31 32
```

<font color=red>
注意在if之前并没有分号
</font>

可以在循环中使用变量：

```
for (i <- 1 to 3; from = 4 - i; j <- from to 3) print((10 * i + j) + " ")
```

for循环体以yield开始，则该循环会构造一个集合，每次迭代生成集合中的一个值：

```
scala> for (i <- 1 to 10) yield i % 3
res4: scala.collection.immutable.IndexedSeq[Int] = Vector(1, 2, 0, 1, 2, 0, 1, 2, 0, 1)
```

这种循环叫做for推导式。

###2.2 函数

方法对对象进行操作，函数不是。Scala编译器可以通过=符号右侧的表达式的类型推断出返回类型。但递归函数必须制定返回类型。

**默认参数和带名参数**

```
// 默认参数
def decorate(str: String, left: String = "[", right: String = "]") = left + str + right

// 带名参数
decorate("Hello", right = ">>>", left = "<<<")
```

**可变长参数列表**

```
def sum(args: Int*) = {
    var result = 0
    for (arg <- args) result += arg
    result
}
```

通过追加:_*，告诉编译器把参数当做参数序列处理。

```
def recursiveSum(args: Int*): Int = {
    if (args.length == 0) 0
    else args.head + recursiveSum(args.tail: _*)
}
```

<font color=red>
序列的head是它的首个元素，而tail是所有其他元素的序列，这又是一个Seq，用:_*将它转换为参数序列。
</font>

###2.3 懒值和异常

**懒值**

当val被声明为lazy时，它的初始化将被推迟，直到首次对它取值。

```
// 在words被定义时即被取值
val words = scala.io.Source.fromFile("/usr/words").mkString

// 在words被首次使用时取值
lazy val words = scala.io.Source.fromFile("/usr/words").mkString

// 在每次words被使用时取值
def words = scala.io.Source.fromFile("/usr/words").mkString
```

**异常**

和Java一样，抛出的对象必须是java.lang.Throwable的子类。不过，与Java不同的是，Scala没有“受检”异常。

throw表达式的类型是Nothing。在if/else中，如果一个分支是Nothing，则if/else表达式的类型就是另一个分支的类型。

捕获异常

```
try {
    xxx
} catch {
    case _: MalformedURLException => println("Bad URL: " + url)
    case ex: IOException => ex.printStackTrace()
} finally {
    ...
}
```

###2.4 习题解答

http://ivaneye.iteye.com/blog/1820647

<font size =4 color=Blue>
1. 一个数字如果为正数，则它的signum为1;如果是负数,则signum为-1;如果为0,则signum为0.编写一个函数来计算这个值 
</font> 

```scala
def signum(num : Int) : Int = { if (num > 0) 1 else if (num < 0 ) -1 else 0 }
``` 

Scala中已经有此方法了，刚才查找API的时候，应该在scala.Int类能看到

```scala
scala> 2.signum
res0: Int = 1
```

> 
def signum: Int
Returns the signum of this.

>**Implicit information:**  This member is added by an implicit conversion from Int to RichInt performed by method intWrapper in scala.LowPriorityImplicits.

>**Definition Classes:** RichInt → ScalaNumberProxy

<font size =4 color=Blue>
2. 一个空的块表达式{}的值是什么？类型是什么？ 
在REPL中就能看出来了 
</font>

```scala
scala> val v = {}
v: Unit = ()
```

值是()类型是Unit

<font size =4 color=Blue>
3.  指出在Scala中何种情况下赋值语句x=y=1是合法的。(提示：给x找个合适的类型定义) 
</font>
题目已经给了明确的提示了。本章节中已经说过了，在scala中的赋值语句是Unit类型。所以只要x为Unit类型就可以了。 

```scala
scala> var y=4;  
y: Int = 4  
  
scala> var x={}  
x: Unit = ()  
  
scala> x=y=7  
x: Unit = ()  
```

这也再次证明了{}是Unit类型

<font size =4 color=Blue>
4. 针对下列Java循环编写一个Scala版本:
</font>

```java
for (int i = 10; i >= 0; i--) System.out.println(i); 
```

```scala
for (i <- 0 to 10 reverse) println(i)
```

<font size =4 color=Blue>
5. 编写一个过程countdown(n:Int)，打印从n到0的数字 
</font>

```scala
def countdown(n : Int) { for (i <- 0 to n reverse) println(i) }
```

<font size =4 color=Blue>
6. 编写一个for循环,计算字符串中所有字母的Unicode代码的乘积。举例来说，"Hello"中所有字符串的乘积为9415087488L 
</font>

```scala
scala> var t:Long=1
t: Long = 1

scala> for(i <- "Hello") {
     | t = t * i.toLong
     | }

scala> t
res4: Long = 9415087488
```

<font size=4 color=Blue>
7. 同样是解决前一个练习的问题，但这次不使用循环。（提示：在Scaladoc中查看StringOps）
</font>

```scala
scala> t = 1
t: Long = 1

scala> "Hello".foreach(t *= _.toLong)

scala> t
res6: Long = 9415087488
```

<font size=4 color=Blue>
8. 编写一个函数product(s:String)，计算前面练习中提到的乘积
</font>

```scala
def product(s : String) : Long = {  
    var t : Long = 1  
    for(i <- s){  
        t *= i.toLong  
    }  
    t  
}
```

<font size=4 color=blue>
9. 把前一个练习中的函数改成递归函数 
</font>

```scala
实现一：
def product(s : String) : Long = {  
    if (s.length == 1) return s.charAt(0).toLong  
    else s.take(1).charAt(0).toLong * product(s.drop(1))  
} 
```

```scala
实现二：
scala> def product(s : String) : Long = {
     | if (s.length == 0) 1
     | else s.head * product(s.tail)
     | }
product: (s: String)Long

scala> product("Hello")
res8: Long = 9415087488
```

<font size=4 color=blue>
10. 编写函数计算$x^n$, 其中n是整数, 使用如下的递归定义:   
$x^n = y^2$, 如果n是正偶数的话, 这里的$y = x^{n/2}$
$x^n = x*x^{n-1}$, 如果n是正奇数的话 
$x^0 = 1$
$x^n = 1/x^{-n}$,如果n是负数的话 
不得使用return语句
</font>

```scala
def xn(x : Double, n : Int) : Double = {
    if (n == 0) 1
    else if (n > 0 && n % 2 == 0) xn(x, n / 2) * xn(x, n / 2)
    else if (n > 0 && n % 2 == 1) x * xn(x, n - 1)
	else 1 / xn(x, -n) 
}
```

##第三章 数组相关操作
###3.1 基本操作

**定长数组**

使用Array申明定长数组。

```
// 10个元素的字符串数组，所有元素初始化为null
val a = new Array[String](10)

// 长度为2的Array[String]
val s = Array("hello", "world")

// 使用()访问元素
s(0) = "goodbye"
```

**变长数组**

使用ArrayBuffer定义变长数组。

```
import scala.collection.mutable.ArrayBuffer

val b = ArrayBuffer[Int]()
// 在尾端添加元素
b += 1
// 在尾端添加多个元素
b += (1, 2, 3, 5)
// 追加任何集合
b ++= Array(8, 13, 21)
// 移除最后5个元素
b.trimEnd(5)

// 在下标2之前插入
b.insert(2, 6)
// 插入任意多元素
b.insert(2, 7, 8, 9)

// 第二个参数是要移除多少个元素
b.remove(2)
b.remove(2, 3)
```

ArrayBuffer的toArray方法可以转Array；Array的toBuffer方法可以转ArrayBuffer

**遍历数组和数组缓存**

```
for (i <- 0 until b.length) println(i + ": " + b(i))

// 也可以增加步长
for (i <- 0 until (b.length, 2)) println(i + ": " + b(i))

// 从数组的尾端开始遍历
for (i <- (0 until b.length).reverse) println(i + ": " + b(i))

// 如果在循环体重不需要用数组下标，则可以直接访问
for (elem <- b) println(elem)
```

**数组转换**

```
// 使用for推导式生成一个全新的数组
val a = Array(2, 3, 5, 7, 11)
val result = for (elem <- a) yield 2 * elem
for (elem <- result) println(elem)
```

示例：给定一个整数的数组缓存，移除除第一个负数之外的所有负数。

```
// 收集需要保留的元素的下标
var first = true
val indexes = for (i <- 0 until a.length if first || a(i) > 0) yield { if (a(i) < 0) first = false; i }

// 将元素移到该去的位置，并截断尾端：
for (j <- 0 until indexes.length) a(j) = a(indexes(j))
a.trimEnd(a.length - indexes.length)
```

###3.3 多维数组

```
// 定义3行4列的二位数组，类型为Array[Array[Double]]
val matrix = Array.ofDim[Double](3, 4)

// 访问
matrix(row)(column) = 42
```

创建不规则的数组，每一行的长度各不相同

```
val triangle = new Array[Array[Int]](10)
for (i <- 0 until triangle.length) triangle(i) = new Array[Int](i + 1)
```

###3.4 与Java的互操作

使用scala.collection.JavaConversions包中的隐式转换。

**Scala到Java的转换**

```
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions.bufferAsJavaList

object Note1 {

    def main(args: Array[String]) {
        val command = ArrayBuffer("ls", "-al", "/home/cay")
        // Scala到Java的转换
        val pd = new ProcessBuilder(command)
    }
}
```

**Java到Scala的转换**

```
import scala.collection.JavaConversions.{asScalaBuffer, bufferAsJavaList}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Note1 {

    def main(args: Array[String]) {
        val command = ArrayBuffer("ls", "-al", "/home/cay")
        // Scala到Java的转换
        val pd = new ProcessBuilder(command)

        val cmd: mutable.Buffer[String] = pd.command() // Java到Scala的转换
    }
}
```

###3.5 习题解答

<font size =4 color=Blue>
1. 编写一段代码，将a设置为一个n个随机整数的数组，要求随机数介于0(包含)和n(不包含)之间
</font>
方式一：

```scala
import scala.math.random  
  
def randomArray(n:Int)={  
  for(i <- 0 until n) yield (random * n).toInt  
}  
  
println(randomArray(10).mkString(",")) 
```

> scala.math包中的函数
> 
> def random: Double
Returns a double value with a positive sign, greater than or equal to 0.0 and less than 1.0.

方式二：

```
import scala.util.Random  
  
def randomArray2(n:Int)={  
  for(i <- 0 until n) yield Random.nextInt(n)  
}  
  
println(randomArray2(10).mkString(","))
```

<font size =4 color=Blue>
2. 编写一个循环，将整数数组中相邻的元素置换。例如，Array(1,2,3,4,5)经过置换后变为Array(2,1,4,3,5) 
</font>
方式一：

```
def reorderArray(arr:Array[Int]):Array[Int]={  
  val t = arr.toBuffer  
  for(i <- 1 until (t.length, 2); tmp = t(i); j <- i - 1 until i) {  
    t(i) = t(j)  
    t(j) = tmp  
  }  
  t.toArray  
}  
  
println(reorderArray(Array(1,2,3,4,5)).mkString(",")) 
```

方式二：

```
def swapArray(arr : Array[Int]) : Array[Int]={   
  for(i <- 1 until (arr.length, 2); tmp = arr(i)) { 
    arr(i) = arr(i - 1)  
    arr(i - 1) = tmp  
  }  
  arr  
}  
  
println(swapArray(Array(1,2,3,4,5)).mkString(",")) 
```

<font size =4 color=Blue>
3. 重复前一个练习，不过这一次生成一个新的值交换过的数组。用for/yield 
</font>

```
def reorderArray(arr : Array[Int]) : Array[Int] = {  
  (for(i <- 0 until (arr.length, 2)) yield { 
      if (i + 1 < arr.length) Array(arr(i + 1), arr(i)) 
      else Array(arr(i))
    }
  ).flatten.toArray
}  
  
println(reorderArray(Array(1,2,3,4,5)).mkString(","))
```

分步执行：

```
scala> val arr = Array(1,2,3,4,5)
arr: Array[Int] = Array(1, 2, 3, 4, 5)

scala> val test = for(i <- 0 until (arr.length, 2)) yield { if (i + 1 < arr.length) Array(arr(i + 1), arr(i)) else Array(arr(i)) }
test: scala.collection.immutable.IndexedSeq[Array[Int]] = Vector(Array(2, 1), Array(4, 3), Array(5))

scala> test.flatten.toArray
res37: Array[Int] = Array(2, 1, 4, 3, 5)
```

> flatten 顺序连接二维数组的所有行，使之变为一维的数组

<font size =4 color=Blue>
4. 给定一个整数数组，产生一个新的数组，包含元数组中的所有正值，以原有顺序排列，之后的元素是所有零或负值，以原有顺序排列 
</font>

```
// 使用循环
import scala.collection.mutable.ArrayBuffer

def loopSplitArray(arr : Array[Int]) : Array[Int] = {  
    val a = ArrayBuffer[Int]()  
    val b = ArrayBuffer[Int]()  
    arr.foreach(arg => if(arg > 0) a += arg else b += arg)  
    a ++= b  
    a.toArray  
}
println(loopSplitArray(Array(1, -4, -3, 4, -5, 0, 1, 3, 2)).mkString(","))
```

> def <font color=red>+=</font>(elem: A): ArrayBuffer.this.type
Appends a single element to this buffer and returns the identity of the buffer.
> 
> def <font color=red>++=</font>(xs: TraversableOnce[A]): ArrayBuffer.this.type
Appends a number of elements provided by a traversable object. The identity of the buffer is returned.

```
// 使用filter  
def filterSplitArray(arr : Array[Int]) : Array[Int] = {  
  val a = arr.filter(_ > 0).map(1 * _)  
  val b = arr.filter(_ <= 0).map(1 * _)  
  val c = a.toBuffer  
  c ++= b  
  c.toArray  
}
println(filterSplitArray(Array(1, -4, -3, 4, -5, 0, 1, 3, 2)).mkString(","))
```

> filter方法：def filter(p: (A) ⇒ Boolean): ArrayBuffer[A]
> 遍历this的所有元素，过滤掉不满足条件的元素，返回剩余元素。

> map方法：def map[B](f: (A) ⇒ B): ArrayBuffer[B]
> 参数为函数，对this串的所有元素使用该函数后，构建新的数组

<font size =4 color=Blue>
5. 如何计算Array[Double]的平均值？
</font>

```
def avgArrayDouble(arr : Array[Double]) : Double = {
    arr.sum / arr.length
}
println(avgArrayDouble(Array(1.2, 1.8, 3.0, 5.2)));
```
<font color=red>
注：缩进不要使用tab，会造成下面这样。当然没有影响结果
</font>

![这里写图片描述](http://img.blog.csdn.net/20160820112546154)

<font size =4 color=Blue>
6. 如何重新组织Array[Int]的元素将他们以反序排列？对于ArrayBuffer[Int]你又会怎么做呢？
</font>

```
def reverseArray(arr : Array[Int]) : Array[Int] = {  
  arr.reverse 
}
val test = reverseArray(Array(1, 2, 3, 4, 5, 5))
```

ArrayBuffer同样

<font size =4 color=Blue>
7. 编写一段代码，产出数组中的所有值，去掉重复项。(提示：查看Scaladoc)  
</font>

```
def distinctArray(arr : Array[Int]) : Array[Int] = {  
  arr.distinct
}
val test = distinctArray(Array(1, 2, 3, 4, 5, 5))
```

<font size =4 color=Blue>
8. 重新编写3.4节结尾的示例。收集负值元素的下标，反序，去掉最后一个下标，然后对每个下标调用a.remove(i)。比较这样做的效率和3.4节中另外两种方法的效率 
</font>

```
def removeArray(arr : Array[Int]) : Array[Int] = {
  val t = arr.toBuffer
  val idx = ArrayBuffer[Int]()
  for(i <- 0 until t.length){
    if (t(i) < 0) idx += i
  }
  idx.remove(0)
  idx.reverse.foreach(t.remove(_))
  t.toArray
} 
removeArray(Array(1, 2, 3, 4, 5, -5, 0, -4, -3, 9))
```

<font color=red>
注：idx.reverse返回新的数组
</font>

<font size =4 color=Blue>
9. 创建一个由java.util.TimeZone.getAvailableIDs返回时区集合，判断条件是它们在美洲。去掉"America/"前缀并排序
</font>

```
import java.util.TimeZone

val t = (for(i <- TimeZone.getAvailableIDs if TimeZone.getTimeZone(i).getID.startsWith("America")) yield TimeZone.getTimeZone(i).getDisplayName).sorted
```

<font size =4 color=Blue>
10. 引入java.awt.datatransfer._并构建一个类型为SystemFlavorMap类型的对象: 
val flavors = SystemFlavorMap.getDefaultFlavorMap().asInstanceOf[SystemFlavorMap] 然后以DataFlavor.imageFlavor为参数调用getNativesForFlavor方法，以Scala缓冲保存返回值。 (为什么用这样一个晦涩难懂的类？因为在Java标准库中很难找到使用java.util.List的代码) 
</font>

```
import java.awt.datatransfer._

val flavors = SystemFlavorMap.getDefaultFlavorMap().asInstanceOf[SystemFlavorMap]  
println(flavors.getNativesForFlavor(DataFlavor.imageFlavor).toArray.toBuffer.mkString("|"))
```

分步运行结果

```
scala> println(flavors.getNativesForFlavor(DataFlavor.imageFlavor).toArray)
[Ljava.lang.Object;@1738dce2

scala> println(flavors.getNativesForFlavor(DataFlavor.imageFlavor).toArray.toBuffer)
ArrayBuffer(PNG, JFIF, TIFF)
```

##第四章 映射与元组
###4.1 映射

**构造映射**

```
// 定义不可变映射
val scores1 = Map("Alice" -> 10, "Bob" -> 3)

// 定义可变映射
val scores2 = scala.collection.mutable.Map("Alice" -> 10, "Bob" -> 3)

// 定义空映射
val scores3 = new scala.collection.mutable.HashMap[String, Int]
```

**获取映射中的值**

```
println(scores1("Alice"))
// 如果包含键"Bill", 返回对应值, 否则返回默认值
println(scores1.getOrElse("Bill", 1))
```

**更新映射中的值**

```
scores2("Alice") = 11

// 添加关系
scores3 += ("Alice" -> 3, "Bob" -> 4)

// 移除某个键
scores3 -= "Bob"
```

<font color=red>
对不可变映射进行加减操作，可以获得一个新的映射。
</font>

**迭代映射**

```
for ((k, v) <- scores3) println("k = " + k + " v = " + v)

// 获得键集合
scores3.keySet
// 获得值集合
scores3.values
```

**已排序映射**

```
// 不可变的树形映射
val scores4 = scala.collection.immutable.SortedMap("Alice" -> 10, "Bob" -> 3)

// 目前Scala没有可变的树形映射，可以使用Java的TreeMap
```

**与Java的互操作**

```
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.JavaConversions.propertiesAsScalaMap
import scala.collection.JavaConversions.mapAsJavaMap
import java.awt.font.TextAttribute._

object Note2 {

    def main(args: Array[String]) {
        // Java映射转Scala映射
        val scores: scala.collection.mutable.Map[String, Int] = new java.util.TreeMap[String, Int]

        // 从java.util.Properties到Map[String, String]的转换
        val props: scala.collection.Map[String, String] = System.getProperties

        // Scala映射转Java映射
        val attrs = Map(FAMILY -> "Serif", SIZE -> 12)
        val font = new java.awt.Font(attrs)
    }
}
```

###4.2 元组

元组是不同类型的值得聚集。例如

```
val t = (1, 3.14, "Fred")
// 是类型如下的元组
Tuple3[Int, Double, java.lang.String]

// 可以用方法_1、_2、_3访问其组元，比如
val second = t._2

// 使用模式匹配来获取元组的组元
val (first, second, third) = t

// 也可以只取一部分，不需要的部件位置上使用_
val (first, second, _) = t
```

元组可以用于函数需要返回不止一个值得情况。例如，StringOps的partition方法返回的是一对字符串

```
scala> "New York".partition(_.isUpper)
res15: (String, String) = (NY,ew ork)
```

可以用zip方法把多个值绑到一起，以便一起被处理：

```
val symbols = Array("<", "-", ">")
val counts = Array(2, 10, 2)
val pairs = symbols.zip(counts)

for ((s, n) <- pairs) print(s * n)

// 结果
<<---------->>
```

###4.3 习题解答

http://ivaneye.iteye.com/blog/1828223

<font size =4 color=Blue>
1. 设置一个映射,其中包含你想要的一些装备，以及它们的价格。然后构建另一个映射，采用同一组键，但是价格上打9折 
</font>

```scala
scala> val map = Map("book" -> 10, "gun" -> 18, "ipad" -> 1000) 
map: scala.collection.immutable.Map[String,Int] = Map(book -> 10, gun -> 18, ipad -> 1000)

scala> for ((k, v) <- map) yield (k, v * 0.9)
res1: scala.collection.immutable.Map[String,Double] = Map(book -> 9.0, gun -> 16.2, ipad -> 900.0)
```

<font size =4 color=Blue>
2. 编写一段程序，从文件中读取单词。用一个可变映射来清点每个单词出现的频率。读取这些单词的操作可以使用java.util.Scanner: 
val in = new java.util.Scanner(new java.io.File("myfile.txt")) while(in.hasNext()) 处理 in.next() 或者翻到第9章看看更Scala的做法。 最后，打印出所有单词和它们出现的次数。
</font>

```scala
import scala.io.Source
import scala.collection.mutable.HashMap

val source = Source.fromFile("myfile.txt")
val tokens = source.mkString.split("\\s+")
val map = new HashMap[String,Int]

for(key <- tokens){
    map(key) = map.getOrElse(key,0) + 1
}

println(map.mkString(","))

t -> 1,s -> 1,ttt -> 2,sss -> 1,test -> 4
```

<font size =4 color=Blue>
3. 重复前一个练习，这次用不可变的映射
</font>

```scala
import scala.io.Source

val source = Source.fromFile("myfile.txt").mkString
val tokens = source.split("\\s+")
var map = Map[String,Int]()

for(key <- tokens){
  map += (key -> (map.getOrElse(key, 0) + 1))
}

println(map.mkString(","))

t -> 1,s -> 1,ttt -> 2,sss -> 1,test -> 4
```

<font size =4 color=Blue>
4. 重复前一个练习，这次使用已排序的映射，以便单词可以按顺序打印出来 
</font>

```
import scala.io.Source
import scala.collection.SortedMap

val source = Source.fromFile("myfile.txt").mkString
val tokens = source.split("\\s+")
var map = SortedMap[String, Int]()

for(key <- tokens){
  map += (key -> (map.getOrElse(key, 0) + 1))
}

println(map.mkString(","))

s -> 1,sss -> 1,t -> 1,test -> 4,ttt -> 2
```

<font size =4 color=Blue>
5. 重复前一个练习，这次使用java.util.TreeMap并使之适用于Scala API 
</font>

```scala
import scala.io.Source
import scala.collection.mutable.Map
import scala.collection.JavaConversions.mapAsScalaMap
import java.util.TreeMap

val source = Source.fromFile("myfile.txt").mkString
val tokens = source.split("\\s+")
val map : Map[String, Int] = new TreeMap[String, Int]

for(key <- tokens){
  map(key) = map.getOrElse(key, 0) + 1
}

println(map.mkString(","))

s -> 1,sss -> 1,t -> 1,test -> 4,ttt -> 2
```

<font size =4 color=Blue>
6. 定义一个链式哈希映射,将"Monday"映射到java.util.Calendar.MONDAY,依次类推加入其他日期。展示元素是以插入的顺序被访问的 
</font>

```scala
import scala.collection.mutable.LinkedHashMap
import java.util.Calendar

val map = new LinkedHashMap[String, Int]

map += ("Monday" -> Calendar.MONDAY)
map += ("Tuesday" -> Calendar.TUESDAY)
map += ("Wednesday" -> Calendar.WEDNESDAY)
map += ("Thursday" -> Calendar.THURSDAY)
map += ("Friday" -> Calendar.FRIDAY)
map += ("Saturday" -> Calendar.SATURDAY)
map += ("Sunday" -> Calendar.SUNDAY)

println(map.mkString(","))
```

<font size =4 color=Blue>
7. 打印出所有Java系统属性的表格
</font>

```scala
import scala.collection.JavaConversions.propertiesAsScalaMap

val props : scala.collection.Map[String, String] = System.getProperties()
val keys = props.keySet
val keyLengths = for(key <- keys) yield key.length

val maxKeyLength = keyLengths.max
for(key <- keys) {
  print(key)
  print(" " * (maxKeyLength - key.length))
  print(" | ")
  println(props(key))
}
```

<font size =4 color=Blue>
8. 编写一个函数minmax(values:Array[Int]),返回数组中最小值和最大值的对偶 
</font>

```scala
def minmax(values : Array[Int]) = {
  (values.max, values.min)
}
println(minmax(Array(3, 2, 5, 6, 7, 2, 8)))
```

<font size =4 color=Blue>
9. 编写一个函数Iteqgt(values:Array[int],v:Int),返回数组中小于v,等于v和大于v的数量，要求三个值一起返回 
</font>

```scala
def iteqgt(values : Array[Int], v : Int) = {
  val buf = values.toBuffer
  (values.count(_ < v), values.count(_ == v), values.count(_ > v))
}
println(iteqgt(Array(3, 2, 5, 6, 7, 2, 8), 3))
```

<font size =4 color=Blue>
10. 当你将两个字符串拉链在一起，比如"Hello".zip("World")，会是什么结果？想出一个讲得通的用例 
</font>

```bash
scala> "Hello".zip("World") 
res0: scala.collection.immutable.IndexedSeq[(Char, Char)] = Vector((H,W), (e,o), (l,r), (l,l), (o,d))  
```

StringOps中的zip定义如下 
abstract def zip(that: GenIterable[B]): StringOps[(A, B)] 
GenIterable是可遍历对象需要包含的trait，对于String来说，它是可遍历的。但是它的遍历是遍历单个字母。 所以拉链就针对每个字母来进行。
