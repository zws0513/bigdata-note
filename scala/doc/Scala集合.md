#1. Scala集合类图

**scala.collection包下共用特质**

![Scala集合共用的特质](http://img.blog.csdn.net/20170319111634491?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMzk4MDEyNw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

**scala.collection.immutable包下不可变集合**

![Scala不可变集合](http://img.blog.csdn.net/20170319111506130?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMzk4MDEyNw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

**scala.collection.mutable包下可变集合**

![Scala可变集合](http://img.blog.csdn.net/20170319113951335?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMzk4MDEyNw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

类型 | 描述
------ | -------
IndexedSeq | 索引序列，按索引查找元素时，时间复杂度O(1) 
LinearSeq | 通过链表实现
Range | 有序的整数队列，元素间间隔相同
NumericRange | 等差队列，可以生成Int、BigInt、Short、Byte、Char、Long、Float、BigDecimal、Double类型的序列
Vector | 不可变的数据结构，实现为具有高分支因子的Trie树
BitSet | 使用bit的方式来代表正整数和0的集合
immutable.HashSet | 以Hash Trie树的方式实现。
mutable.HashSet | 以FlathashTable实现
ListSet | 以链表实现的Set
LinkedHashSet | 以hashtable存储数据，保持插入顺序
IntMap、LongMap | 基于Fast Mergeable Integer Maps算法实现的
HashMap | 以Hash Trie树的数据结构实现的Map
SortedMap、immutable.TreeMap | SortedMap定义了按键排序的Map，TreeMap是它的实现类。以红黑树实现可排序的Map。
ListMap | 以链表实现的Map
LinkedHashMap | 保持插入顺序，底层以HashTable实现，又实现了双向链表
MultiMap | 同一个键，可能有多个值与之对应
OpenHashMap | 基于开放寻址方案而实现的。
WeakHashMap | 对java.util.WeakHashMap类的包装，当Map中的键不再有强引用时，键值对会被移除
ListBuffer | 以immutable.List数据结构链表方式存储元素
ArrayBuffer | 使用ResizableArray实现
UnrolledBuffer | 内部是由多个固定大小的数组组成，数组间以链表的方式存在
MutableList | 可变列表，用来实现队列Queue
Option | 用来避免空指针异常。定义了隐式转换，可以转换为可迭代对象Iterable
Stack | 以immutable.List为存储结构实现的
ArrayStack | 以数组为存储结构实现的
mutable.Queue | 以MutableList数据结构实现
PriorityQueue | 优先级高的元素在队头，内部以堆的数据结构实现

#2. 共有特质
##2.1 Traversable

###2.1.1 foreach遍历

```
# 定义
def foreach[U](f: Elem => U): Unit

# 示例：
scala> val s = Traversable(1, 2, 3)  // 由伴生对象实例化
s: Traversable[Int] = List(1, 2, 3)

scala> s.foreach(x => println(x + 3))
4
5
6
```

###2.1.2 flatten平展

```
# 定义
def flatten[B]: Traversable[B]

# 示例
scala> val s = Traversable(Traversable(1, 2, 3), Traversable(2, 3, 4), Traversable(7, 8))
s: Traversable[Traversable[Int]] = List(List(1, 2, 3), List(2, 3, 4), List(7, 8))

scala> val s2 = s.flatten
s2: Traversable[Int] = List(1, 2, 3, 2, 3, 4, 7, 8)
```

注：

1. 元素类型不一致时，向最近的相同父类型转；
2. 普通类型和集合类型共存时，不能平展；
3. 集合嵌套集合时，不会深度平展。

###2.1.3 transpose转置

```
# 定义
def transpose[B](implicit asTraversable: (A) => GenTraversableOnce[B]): Traversable[Traversable[B]]


# 示例
scala> val matrix = Traversable(Traversable(1, 2, 3), Traversable(4, 5, 6), Traversable(7, 8, 9))
matrix: Traversable[Traversable[Int]] = List(List(1, 2, 3), List(4, 5, 6), List(7, 8, 9))

scala> val result = matrix.transpose
result: Traversable[Traversable[Int]] = List(List(1, 4, 7), List(2, 5, 8), List(3, 6, 9))
```

注：要求内部集合的元素个数相同

###2.1.4 unzip、unzip3拉链操作

将集合分为两部分或三部分

```
# 定义
def unzip[A1, A2](implicit asPair: (A) => (A1, A2)): (Traversable[A1], Traversable[A2])

def unzip3[A1, A2, A3](implicit asTriple: (A) => (A1, A2, A3)): (Traversable[A1], Traversable[A2], Traversable[A3])

# 示例
scala> val t = Traversable("a" -> 1, "b" -> 2, "c" -> 3)
t: Traversable[(String, Int)] = List((a,1), (b,2), (c,3))

scala> val (a, b) = t.unzip
a: Traversable[String] = List(a, b, c)
b: Traversable[Int] = List(1, 2, 3)


scala> val t = Traversable("ID, 评分, 名称", "0001, 6.2, 名称1", "0002, 7.3, 名称2")
t: Traversable[String] = List(ID, 评分, 名称, 0001, 6.2, 名称1, 0002, 7.3, 名称2)

scala> val (a, b, c) = t.unzip3(x => {val a = x.split(","); (a(0), a(1), a(2))})
a: Traversable[String] = List(ID, 0001, 0002)
b: Traversable[String] = List(" 评分", " 6.2", " 7.3")
c: Traversable[String] = List(" 名称", " 名称1", " 名称2")
```

###2.1.5 ++连接两个集合为一个集合

```
scala> val movieNames = Traversable("m1", "m2")
movieNames: Traversable[String] = List(m1, m2)

scala> import scala.collection.immutable.HashSet
import scala.collection.immutable.HashSet

scala> val movieIDs = HashSet(222, 111, 333)
movieIDs: scala.collection.immutable.HashSet[Int] = Set(333, 111, 222)

scala> val movies = movieNames ++ movieIDs
movies: Traversable[Any] = List(m1, m2, 333, 111, 222)

# 结果的类型与++左面的一样
scala> movieIDs ++ movieNames
res2: scala.collection.immutable.HashSet[Any] = Set(333, m2, m1, 111, 222)
```

###2.1.6 concat连接多个集合

```
import scala.collection.mutable

scala> val result = mutable.Traversable.concat(Traversable(1 to 3: _*), Traversable(4 to 5: _*), Traversable(6 to 8: _*))
result: scala.collection.mutable.Traversable[Int] = ArrayBuffer(1, 2, 3, 4, 5, 6, 7, 8)
```

###2.1.7 collect筛选元素

兼有filter和map的功能。

```
# 定义
def collect[B](pf: PartialFunction[A, B]): Traversable[B]

# 示例：
val t = Traversable(1,2,3,4,5,6,7,8,9)
def filterEven :PartialFunction[Int, Int] = {
    case x if x % 2 == 0 => x
}

val result = t collect filterEven

result: scala.collection.mutable.Traversable[Int] = ArrayBuffer(2, 4, 6, 8)
```

注：

collectFirst使用偏函数所得到的第一个值是Option类型，如果都不在偏函数处理的定义域时，返回None

###2.1.8 map、flatMap应用函数生成新的集合

**map**

```
# 定义
def map[B](f: (A) => B): Traversable[B]

# 示例
scala> val t = Traversable(1 to 5: _*)
t: Traversable[Int] = List(1, 2, 3, 4, 5)

scala> val result = t.map(x => x*x)
result: Traversable[Int] = List(1, 4, 9, 16, 25)
```

**flatMap**

可以看作map+flatten的联合方法

```
# 定义
def flatMap[B](f: (A) => GenTraversableOnce[B]): Traversable[B]

# 示例
scala> val s = """Builds a new collection
     |   to all
     |   test elements""".stripMargin
s: String =
Builds a new collection
  to all
  test elements

scala> val tt: Traversable[String] = for (x <- s.split("\r\n")) yield x
tt: Traversable[String] =
ArraySeq(Builds a new collection
  to all
  test elements)

scala> tt.flatMap(line => line split "\\W+")
res0: Traversable[String] = ArraySeq(Builds, a, new, collection, to, all, test, elements)
```

###2.1.9 scan、scanLeft、scanRight获取元素的阶乘

```
# 定义
def scan[B >: A, That](z: B)(op: (B, B) => B)(implicit cbf: CanBuildFrom[Traversable[A], B, That]): That

# 第一个参数是初值；第二个是运算函数op

# 示例
scala> val t = Traversable(1 to 5: _*)
t: Traversable[Int] = List(1, 2, 3, 4, 5)

scala> val result = t.scan(1)(_*_)
result: Traversable[Int] = List(1, 1, 2, 6, 24, 120)

scala> val result2 = t.scanRight(1)(_*_)
result2: Traversable[Int] = List(120, 120, 60, 20, 5, 1)
```

注：scan就是scanLeft方法；scanRight是从右开始计算的。

###2.1.10 fold、foldLeft、foldRight折叠元素

将初始值和第一个元素传给op，将结果和下一个元素再传给op，以此类推。

```
# 定义
def fold[A1 >: A](z: A1)(op: (A1, A1) => A1): A1

# 示例
scala> val t = Traversable("A", "B", "C", "D", "E")
t: Traversable[String] = List(A, B, C, D, E)

scala> val result = t.fold("z")(_ + _)
result: String = zABCDE

scala> val result2 = t.foldRight("z")(_+_)
result2: String = ABCDEz
```

注：foldLeft、foldRight方法对应的简写方法为`/:`和`:\`

foldLeft相当于

```
op(...op(op(z, x_1), x_2), ..., x_n)
```

foldRight相当于

```
op(x_1, op(x_2, ... op(x_n, z)...))
```

###2.1.11 isEmpty、nonEmpty、size、hasDefiniteSize

```
# 示例
val t = Traversable.empty
println(t.isEmpty)
println(!t.nonEmpty)
```

注：hasDefiniteSize返回集合是否拥有有限的大小。

###2.1.12 head、last、headOption、lastOption、find

head、last返回第一个元素和最后一个元素，如果元素不存在，则会抛出NoSuchElementException。

headOption、lastOption返回的类型为Option，如果元素存在，则返回Some(head)、Some(last)；如果不存在，则返回None。

**find**

查找第一个满足条件的元素

```
# 定义
def find(p: (A) => Boolean): Option[A]

# 示例
scala> val t = Traversable(1 to 10:_*)
t: Traversable[Int] = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

scala> t.find(_ % 2 == 0)
res0: Option[Int] = Some(2)
```

###2.1.13 tail、tails、init、inits

**tail**

除第一个元素外其他元素组成的集合

```
val t = Traversable（1 to 5:_*)
t.tail
println(t == (t.head :: t.tail.toList)
```

**tails**

迭代地生成tail，第一个值是原始集合，最后一个值是空集合，中间值是对前一个的tail。

```
for (i <- t.tails) println(i)
```

**init**

与tail相反，返回除最后一个元素外的其他元素。

**inits**

和tails类似，只不过中间值是前一个的init。

###2.1.14 slice子集

```
# 定义
def slice(from: Int, until: Int): Traversable[A]
# 返回左闭右开的一段元素

# 示例
val t = Traversable（1 to 5:_*)
scala> t.slice(1, 3)
res1: Traversable[Int] = List(2, 3)
```

###2.1.15 take、takeWhile前N个元素

take返回集合的前N个元素；

takeWhile返回从头开始，直到某个元素不满足断言。

```
scala> t.takeWhile(_ < 3)
res3: Traversable[Int] = List(1, 2)
```

###2.1.16 drop、dropWhile跳过前N个

drop获取除去开头前N个元素的剩余元素。

dropWhile从头开始，跳过开头满足断言的元素，一旦某个元素不满足断言，则返回剩余的元素。

```
scala> Traversable(1,2,6,3,4,8).dropWhile(_ < 6)
res4: Traversable[Int] = List(6, 3, 4, 8)
```

###2.1.17 filter、filterNot、withFilter筛选

filter返回满足断言的所有元素的集合；

filterNot选择不满足断言的元素；

withFilter的结果为FilterMonadic，它只包含map、flatMap、foreach和withFilter操作。在调用foreach前，都是延迟计算。

```
# 定义
def filter(p: (A) => Boolean): Traversable[A]
def filterNot(p: (A) => Boolean): Traversable[A]

# 示例
val t = Traversable(1 to 10:_*)

t.filter(_ < 6)
t.filterNot(_ < 3)

t withFilter(_ < 6) withFilter(_ > 2) withFilter(_ % 2 == 0) foreach println
```

###2.1.18 splitAt、span、partition、groupBy分组

**splitAt**

```
# 定义
# 根据位置将Traversable对象分成两部分
def splitAt(n: Int): (Traversable[A], Traversable[A])

# 示例
scala> t.splitAt(5)
res0: (Traversable[Int], Traversable[Int]) = (List(1, 2, 3, 4, 5),List(6, 7, 8, 9, 10))
```

**span**

```
# 定义
# 一直选取元素，直到某个元素不满足断言，然后将前面的元素分成一组，将后面的元素分成另一组
def span(p: (A) => Boolean): (Traversable[A], Traversable[A])

# 示例
scala> t.span(_ < 6)
res1: (Traversable[Int], Traversable[Int]) = (List(1, 2, 3, 4, 5),List(6, 7, 8, 9, 10))
```

**partition**

```
# 定义
# 将满足断言和不满足的分成两部分
def partition(p: (A) => Boolean): (Traversable[A], Traversable[A])

# 示例
scala> t.partition(_ % 2 == 0)
res2: (Traversable[Int], Traversable[Int]) = (List(2, 4, 6, 8, 10),List(1, 3, 5, 7, 9))
```

**groupBy**

```
# 定义
# 按照条件将元素分组
def groupBy[K](f: (A) => K): immutable.Map[K, Traversable[A]]

# 示例
scala> t.groupBy(_ % 3)
res3: scala.collection.immutable.Map[Int,Traversable[Int]] = Map(2 -> List(2, 5, 8), 1 -> List(1, 4, 7, 10), 0 -> List(3, 6, 9))
```

###2.1.19 forall、exists是否满足条件

```
# 定义
# 测试所有元素，断言都为true时，返回true
def forall(p: (A) => Boolean): Boolean
# 只要有一个元素满足，就返回true
def exists(p: (A) => Boolean): Boolean
```

###2.1.20 count计数

```
# 定义
def count(p: (A) =》 Boolean): Int
```

###2.1.21 reduce、reduceOption、reduceLeft、reduceLeftOption、reduceRight、reduceRightOption归约

```
# 定义
def reduce[A1 >: A](op: (A1, A1) => A1): A1

# 计算方式
op(op(...op(x_1, x_2), ..., x_{n-1}), x_n)
```

注：

1. reduce和reduceLeft相同，reduceOption和reduceLeftOption相同；
2. reduceLeft是从左到右开始计算，reduceRight相反；
3. reduceXXXOption会返回Option类型的结果。
4. 对于空集合，会抛出异常；只有一个元素时，返回该元素。

###2.1.22 sum、product、min、max、aggregate、maxBy、minBy聚合

函数 | 描述
----- | -----
sum | 求和
product | 求积
min | 最小值
max | 最大值

**maxBy、minBy**

```
# 定义
# 根据测量函数的值大小决定返回最大或最小值
def maxBy[B](f: (A) => B): A
def minBy[B](f: (A) => B): A
```

**aggregate**

```
# 定义
# 并不要求返回结果的类型是集合元素类型的父类
# z是初始值
# seqop在遍历分区的时候更新结果
# combop汇总计算各分区的结果
def aggregate[B](z: ⇒ B)(seqop: (B, A) => B, combop: (B, B) => B): B

# 示例
scala> List('a', 'b', 'c').aggregate(0)({ (sum, ch) => sum + ch.toInt }, { (p1, p2) => p1 + p2 })
res8: Int = 294
```

###2.1.23 mkString、addString、stringPrefix生成字符串

**mkString**

```
# 定义
# sep分隔符
# start前缀
# end后缀
def mkString: String
def mkString(sep: String): String
def mkString(start: String, sep: String, end: String): String

# 示例
scala> t.mkString("^", ",", "&")
res10: String = ^1,2,3,4,5,6,7,8,9,10&
```

**addString**

```
# 定义
# 将元素放入StringBuilder对象b中
# sep分隔符
# start前缀
# end后缀
def addString(b: StringBuilder): StringBuilder
def addString(b: StringBuilder, sep: String): StringBuilder
def addString(b: StringBuilder, start: String, sep: String, end: String): StringBuilder

# 示例
```

**stringPrefix**

```
# 定义
# 返回对象的实际类型的名称
def stringPrefix: String

# 示例
scala> t.repr.getClass.getName
res12: String = scala.collection.immutable.$colon$colon

scala> t.stringPrefix
res13: String = List
```

###2.1.24 集合类型转换

函数 | 定义
------ | ------
toArray | def toArray: Array[A]
toParArray | def toParArray: ParArray[T]
toBuffer | def toBuffer[B >: A]: Buffer[B]
toIndexedSeq | def toIndexedSeq: immutable.IndexedSeq[A]
toIterable | def toIterable: Iterable[A]
toIterator | def toIterator: Iterator[A]
toList | def toList: List[A]
toSeq | def toSeq: Seq[A]
toSet | def toSet[B >: A]: immutable.Set[B]
toStream | def toStream: Stream[A]
toVector | def toVector: Vector[A]
toTraversable | def toTraversable: Traversable[A]
toMap | def toMap[T, U]: Map[T, U]
to | def to[Col[_]]: Col[A]

```
# to示例
scala> t.to[scala.collection.immutable.Queue]
res15: scala.collection.immutable.Queue[Int] = Queue(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
```

注：

1. 为子类提供了转根特质的方法toTraversable；
2. toIterable返回scala.collection.Iterable，而toIterator返回迭代器scala.collection.Iterator
3. Traversable类的toIterable、toSeq默认和toStream相同，但子类可能会重写；
4. toIterator实际调用的是toStream.iterator
5. to是通用的转换方法，只要有一个隐式转换`implicit cbf: CanBuildFrom[Nothing, A, Col[A]]`
6. toMap要求集合的元素类型应该是Tuple2类型或子类型，否则编译错误。
7. toTraversable返回元集合，t.toTraversable eq t的结果为true。

###2.1.25 copyToArray、copyToBuffer复制元素到一个数组

```
# 定义
# xs目标数组
# start开始索引
# len长度（超过Traversable对象的长度时，以Traversable的长度为准）
def copyToArray(xs: Array[A], start: Int, len: Int): Unit
def copyToArray(xs: Array[A]): Unit
def copyToArray(xs: Array[A], start: Int): Unit

def copyToBuffer[B >: A](dest: Buffer[B]): Unit
```

###2.1.26 view获取对象的视图

```
# 定义
# 生成[from, until)的视图，是延迟计算的。
def view(from: Int, until: Int): TraversableView[A, Traversable[A]]
def view: TraversableView[A, Traversable[A]]
```

###2.1.27 repr得到对象的底层实现

```
# 定义
def repr: Traversable[A]

# 示例
scala> t.repr
res16: Traversable[Int] = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
```

###2.1.28 fill、iterate使用相同元素填充元素

**fill**

```
# 定义
# 其中CC是Traversable
def fill[A](n: Int)(elem: => A): CC[A]
def fill[A](n1: Int, n2: Int)(elem: => A): CC[CC[A]]
def fill[A](n1: Int, n2: Int, n3: Int)(elem: => A): CC[CC[CC[A]]]
def fill[A](n1: Int, n2: Int, n3: Int, n4: Int)(elem: => A): CC[CC[CC[CC[A]]]]
def fill[A](n1: Int, n2: Int, n3: Int, n4: Int, n5: Int)(elem: => A): CC[CC[CC[CC[CC[A]]]]]

# 示例
scala> val t1 = Traversable.fill(5)("A")
t1: Traversable[String] = List(A, A, A, A, A)

scala> val t1 = Traversable.fill(5,2)("A")
t1: Traversable[Traversable[String]] = List(List(A, A), List(A, A), List(A, A), List(A, A), List(A, A))
```

**iterate**

```
# 定义
# 使用指定的函数f来生成序列
# start是初始值
# len长度
# 计算方式：start, f(start), f(f(start)), ...
def iterate[A](start: A, len: Int)(f: A => A): CC[A]

# 示例
scala> val t4 = Traversable.iterate(1, 5)(_ * 10)
t4: Traversable[Int] = List(1, 10, 100, 1000, 10000)
```

###2.1.29 range生成指定间隔的队列

```
# 定义
# 生成的值域为[start, end)的队列
# step是元素间的步进，默认为1
def range[T: Integral](start: T, end: T): CC[T]
def range[T: Integral](start: T, end: T, step: T): CC[T]

# 示例
scala> val t5 = Traversable.range(1, 20, 5)
t5: Traversable[Int] = List(1, 6, 11, 16)
```

###2.1.30 tabulate

```
# 定义
# 使用函数生成指定数量的元素
# 不会使用前一个计算结果
# 可以将索引值传给函数
def tabulate[A](n: Int)(f: Int => A): CC[A]
def tabulate[A](n1: Int, n2: Int)(f: (Int, Int) => A): CC[CC[A]]
def tabulate[A](n1: Int, n2: Int, n3: Int)(f: (Int, Int, Int) => A): CC[CC[CC[A]]]
def tabulate[A](n1: Int, n2: Int, n3: Int, n4: Int)(f: (Int, Int, Int, Int) => A): CC[CC[CC[CC[A]]]]
def tabulate[A](n1: Int, n2: Int, n3: Int, n4: Int, n5: Int)(f: (Int, Int, Int, Int, Int) => A): CC[CC[CC[CC[CC[A]]]]]

# 示例
scala> Traversable.tabulate(5)(_ * 10)
res17: Traversable[Int] = List(0, 10, 20, 30, 40)

scala> Traversable.tabulate(5, 2)(_ * 10 + _)
res18: Traversable[Traversable[Int]] = List(List(0, 1), List(10, 11), List(20, 21), List(30, 31), List(40, 41))
```

###2.1.31 empty生成空对象

下面三种方法都可生成空的集合

```
val t1 = Traversable.empty[Int]
val t2 = Traversable[Int]()
val t3 = Nil
```

###2.1.32 seq、par获得串行对象和并行对象

seq获得串行对象；

par获得并行对象。

##2.2 Iterable
###2.2.1 grouped分组

```
# 定义
# 分成固定大小的组，使得除最后一组外
def grouped(size: Int): Iterator[Iterable[A]]
```

###2.2.2 sliding以滑动窗口的方式分组

```
# 定义
# size：窗口大小
# step：步数，默认1
def sliding(size: Int): Iterator[Iterable[A]]
def sliding(size: Int, step: Int): Iterator[Iterable[A]]
```

###2.2.3 zip两个集合

```
# 定义
# 组合另一个集合，将所对应的元素组合成一对（Tuple2），如果长度不等，以短的为准
def zip[B](that: GenIterable[B]): Iterable[(A, B)]
```

###2.2.4 zipAll两个长度不同的集合

```
# 定义
# thisElem：左边集合的默认值
# thatElem：右边集合的默认值
def zipAll[B](that: Iterable[B], thisElem: A, thatElem: B): Iterable[(A, B)]
```

###2.2.5 zipWithIndex使用本身的索引zip一个集合

```
# 定义
# 其索引值作为Tuple2的第二个值
def zipWithIndex: Iterable[(A, Int)]
```

###2.2.6 sameElements检查两个迭代器是否包含相同的元素

```
# 定义
# 迭代判断两个集合是否严格相等，即元素顺序是否一致并相等
def sameElements(that: GenIterable[A]): Boolean
```

###2.2.7 takeRight获得尾部N个元素

```
# 定义
def takeRight(n: Int): Iterable[A]
```

###2.2.8 dropRight去掉尾部N个元素

```
# 定义
def dropRight(n: Int): Iterable[A]
```

#3. 集合
#3.1 Seq
###3.1.1 indices获得索引集合

```
# 定义
# 索引值是从0开始的，等价于 0 until length方法
def indices: immutable.Range
```

###3.1.2 size、length、lengthCompare长度

size与length都是返回序列长度的方法

lengthCompare提供了序列的长度与指定的值len比较大小的功能。相等返回0；序列长度大则返回1，否则返回`seq.length - len`或`-1`

```
# 定义
def lengthCompare(len: Int): Int
```

###3.1.3 apply得到指定索引的元素

```
# 定义
def apply(idx: Int): A

# 示例
val t = Seq(1 to 5: _*)
t(2)
t.apply(2)
```

###3.1.4 indexOf、lastIndexOf寻找指定元素的索引

```
# 定义
# elem：被搜索元素
# from：起始点（含）
# end：结束点（含）
def indexOf(elem: A): Int
def indexOf(elem: A, from: Int): Int
def lastIndexOf(elem: A): Int
def lastIndexOf(elem: A, end: Int): Int
```

###3.1.5 indexWhere、lastIndexWhere寻找满足条件的元素索引

```
# 定义
# 返回满足断言的第一个元素的索引
# from：起始点（含）
# end：结束点（含）
def indexWhere(p: (A) => Boolean): Int
def indexWhere(p: (A) => Boolean, from: Int): Int
def lastIndexWhere(p: (A) => Boolean): Int
def lastIndexWhere(p: (A) => Boolean, end: Int): Int
```

###3.1.6 indexOfSlice、lastIndexOfSlice寻找指定的子序列

```
# 定义
# 如果含这个集合，返回子序列的第一个元素的索引
def indexOfSlice[B >: A](that: GenSeq[B]): Int
def indexOfSlice[B >: A](that: GenSeq[B], from: Int): Int
def lastIndexOfSlice[B >: A](that: GenSeq[B]): Int
def lastIndexOfSlice[B >: A](that: GenSeq[B], end: Int): Int
```

###3.1.7 segmentLength、prefixLength寻找满足条件的子序列的长度

```
# 定义
def segmentLength(p: (A) => Boolean, from: Int): Int
def prefixLength(p: (A) => Boolean): Int

# 示例
scala> val s = Seq(1 to 10: _*)
s: Seq[Int] = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

scala> s.segmentLength(_ < 5, 2)
res0: Int = 2

scala> s.prefixLength(_ < 5)
res1: Int = 4
```

###3.1.8 +:、:+、padTo增加元素到序列中

```
# 定义
# 添加到头部
def +:(elem: A): Seq[A]
# 添加到尾部
def :+(elem: A): Seq[A]
# 根据当前序列生成新的序列（len是长度，len比元序列长时，用elem填充）
def padTo(len: Int, elem: A): Seq[A]
```

###3.1.9 patch替换序列中的元素

```
# 定义
# from：起始点
# that：要替换后的元素
# replaced：要替换的元素的数量
# 替换与被替换不需要一一对应
def patch(from: Int, that: GenSeq[A], replaced: Int): Seq[A]

# 示例
scala> val t = Seq(1 to 10:_*)
t: Seq[Int] = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

scala> t.patch(5, Seq(10, 20), 1)
res2: Seq[Int] = List(1, 2, 3, 4, 5, 10, 20, 7, 8, 9, 10)
```

###3.1.10 updated、update更新指定位置的索引

```
# 定义
def updated(index: Int, elem: A): Seq[A]
def update(idx: Int, elem: A): Unit
```

###3.1.11 sorted、sortWith、sortBy排序

```
# 定义
# 使用隐式的math.Ordering进行排序
def sorted[B >: A](implicit ord: math.Ordering[B]): Seq[A]

# 比较两个元素，进行排序
def sortWith(lt: (A, A) => Boolean): Seq[A]

# 转换为可排序的对象
def sortBy[B](f: (A) => B)(implicit ord: math.Ordering[B]): Seq[A]
```

###3.1.12 reverse、reverseIterator、reverseMap反转序列

```
# 定义
def reverse: Seq[A]
def reverseIterator: Iterator[A]
def reverseMap[B](f: (A) => B): Seq[B]
```

###3.1.13 startsWith、endsWith是否包含某个前缀或后缀

```
# 定义
def startsWith[B](that: GenSeq[B]): Boolean
def startsWith[B](that: GenSeq[B], offset: Int): Boolean
def endsWith[B](that: GenSeq[B]): Boolean
```

###3.1.14 contains、containsSlice是否包含某子序列

```
# 定义
def contains[A1 >: A](elem: A1): Boolean
def containsSlice[B](that: GenSeq[B]): Boolean
```

###3.1.15 corresponds检查两个序列对应的元素是否满足断言

```
# 定义
def corresponds[B](that: GenSeq[B])(p: (A, B) => Boolean): Boolean
```

###3.1.16 intersect、diff、union集合操作

```
# 定义
# 交集
def intersect(that: Seq[A]): Seq[A]
# 差
def diff(that: Seq[A]): Seq[A]
# 并
def union(that: Seq[A]): Seq[A]
```

###3.1.17 distinct去重

```
# 定义
def distinct: Seq[A]
```

###3.1.18 permutations得到元素的各种排列

```
# 定义
def permutations: Iterator[Seq[A]]

# 示例
"abb".permutations = Iterator(abb, bab, bba)
```

###3.1.19 combinations得到序列的指定长度的元素的组合

```
# 定义
# 指定长度小于0或大于序列的长度，返回空的迭代器
def combinations(n: Int): Iterator[Seq[A]]

# 示例
"abbbc".combinations(2) = Iterator(ab, ac, bb, bc)
```

###3.1.20 mutable.Seq.transform将序列进行转换

```
# 定义
# 原地转换序列
# 转换后的类型不会变
def transform(f: (A) => A): Seq.this.type

# 示例

```

###3.1.21 andThen、applyOrElse、orElse、lift、runWith偏函数

**andThen**

在Seq上应用一个转换函数`k: (A) => C`，结果还是一个偏函数f1，因此f1(x)相当于k(seq(x))。先应用序列，然后将元素传递给第二个函数。

```
def andThen[C](k: (A) => C): PartialFunction[Int, C]

val s1 = Seq(1 to 5: _*)
val s2 = Seq(6 to 10: _*)
val s3: PartialFunction[Int, String] = {
    case x if x > 0 => "hello" + x
}

val pf = s1.asInstanceOf[PartialFunction[Int, Int]]
s1.andThen(s2)(2) // 9 = s2(s1(2))
s2.andThen(s3)(2) // "hello8" = s3(s2(2))
```

**applyOrElse**

使用指定的参数应用这个偏函数，如果参数不在这个偏函数定义域上，则返回默认值

```
def applyOrElse[A1 <: Int, B1 >: A](x: A1, default: (A1) => B1): B1

s1.applyOrElse(2, (_:Int) => 2) // 3 = s1(2)，2是s1的索引
s1.applyOrElse(10, (_:Int) => Int.MaxValue) // 2147483647，10超出索引范围
```

**orElse**

用来组合偏函数的，当序列这个偏函数没有定义时，才会应用所提供的偏函数。

```
def orElse[A1 <: Int, B1 >: A](that: PartialFunction[A1, B1]): PartialFunction[A1, B1]

s1.orElse(s2)(2)  // 3 = s1(2)
s1.orElse(s3)(8)  // "hello8" = s3(8)
```

**lift**

将偏函数转换成一个普通函数，也就是返回一个Option[A]类型的函数。

```
def lift: (Int) => Option[A]

s1.lift(2) // res10: Option[Int] = Some(3)
s1.lift(8) // res11: Option[Int] = None
```

**compose**

组合一个函数`g: (A) => Int`，得到scala.Function1的对象。当应用这个对象时，传入的参数先应用g(x)，然后将结果作为索引，得到序列此索引的元素。

```
def compose[A](g: (A) => Int): (A) => A

s1.compose((x: String) => x.length - 1)("hello")  // res12: Int = 5
```

**runWith**

使用一个action函数操作元素。如果这个偏函数有定义，则调用这个action并返回true，否则什么也不做，直接返回false。

```
def runWith[U](action: (A) => U): (Int) => Boolean

s1.runWith(println)(2)  // true
s1.runWith(println)(8)  // false
```

##3.2 Set
###3.2.1 contains、apply、subsetOf是否包含元素

```
# 定义
def contains(elem: A): Boolean
def apply(elem: A): Boolean

# 检查一个集合是否包含某个集合的子集合
def subsetOf(that: GenSet[A]): Boolean

val s = Set(1 to 5: _*)
Set(3, 4).subsetOf(s)  // true
```

###3.2.2 +、++、++:、+=、++=、add添加一个或多个元素

```
# 定义
def +(elem: A): Set[A]
def +(elem1: A, elem2: A, elems: A*): Set[A]
def ++(xs: GenTraversableOnce[A]): Set[A]
def ++[B](that: GenTraversableOnce[B]): Set[B]

def ++:[B >: A, That](that: collection.Traversable[B])(implicit bf: CanBuildFrom[Set[A], B, That]): That
def ++:[B](that: TraversableOnce[B]): Set[B]

def +=(elem: A): Set.this.type
def +=(elem1: A, elem2: A, elems: A*): Set.this.type
def ++=(xs: TraversableOnce[A]): Set.this.type

def add(elem: A): Boolean
```

###3.2.3 -、--、-=、--=、remove、retain、clear去掉一个或多个元素

```
# 定义
def -(elem: A): Set[A]
def -(elem1: A, elem2: A, elems: A*): Set[A]
def --(xs: GenTraversableOnce[A]): Set[A]

def -=(elem: A): Set.this.type
def -=(elem1: A, elem2: A, elems: A*): Set.this.type
def --=(xs: TraversableOnce[A]): Set.this.type

def remove(elem: A): Boolean

def retain(p: (A) => Boolean): Unit
def clear(): Unit
```

###3.2.4 &、intersect、|、union、&~、diff二元Set集合运算

```
# 定义
# 交集
def &(that: GenSet[A]): Set[A]
def intersect(that: GenSet[A]): Set[A]

# 并集
def |(that: GenSet[A]): Set[A]
def union(that: GenSet[A]): Set[A]

# 差集
def &~(that: GenSet[A]): Set[A]
def diff(that: GenSet[A]): Set[A]
```

###3.2.5 update更新可变集合

```
# 定义
# elem：增加或删除的元素
# included：是否包含或移除这个元素，true是增加。false是删除
def update(elem: A, included: Boolean): Unit
```

###3.2.6 mutable.Set.clone克隆

```
# 定义
def clone(): Set[A]
```

##3.3 Map

###3.3.1 apply、get、getOrElse、withDefault、withDefaultValue、getOrElseUpdate根据键值查找值

```
# 定义
def apply(key: A): B
def get(key: A): Option[B]
def getOrElse(key: A, default: => B): B

# 提供一个产生默认值的方法，它用来替换默认的default方法
def withDefault(d: (A) => B): Map[A, B]
# 提供一个常量用做默认值
def withDefaultValue(d: B): Map[A, B]

# 存在则，返回A对的值，否则用op计算，并保存这个键值对，然后返回该值
def getOrElseUpdate(key: A, op: => B): B
```

###3.3.2 contains、isDefinedAt包含

```
# 定义
def contains(key: A): Boolean
def isDefinedAt(key: A): Boolean
```

###3.3.3 +、++、++:、+=、++=、put增加新的键值对

```
# 定义
def +[B1 >: B](elem1: (A, B1), elem2: (A, B1), elems: (A, B1)*): Map[A, B1]
def +[B1 >: B](kv: (A, B1)): Map[A, B1]

def ++[B1 >: B](xs: GenTraversableOnce[(A, B1)]): Map[A, B1]
def ++[B](that: GenTraversableOnce[B]): Map[B]

def ++:[B >: (A, B), That](that: collection.Traversable[B])(implicit bf: CanBuildFrom[Map[A, B], B, That]): That
def ++:[B](that: TraversableOnce[B]): Map[B]

def +=(kv: (A, B)): Map.this.type
def +=(elem1: (A, B), elem2: (A, B), elems: (A, B)*): Map.this.type

def ++=(xs: TraversableOnce[(A, B)]): Map.this.type

def put(key: A, value: B): Option[B]
```

###3.3.4 -、--、-=、--=、remove、clear删除键

```
# 定义
def -(elem1: A, elem2: A, elems: A*): Map[A, B]
def -(key: A): Map[A, B]

def --(xs: GenTraversableOnce[A]): Map[A, B]

def -=(key: A): Map.this.type
def -=(elem1: A, elem2: A, elems: A*): Map.this.type

def --=(xs: TraversableOnce[A]): Map.this.type

def remove(key: A): Option[B]
def clear(): Unit
```

###3.3.5 updated、update、put根据键更新它的值

```
# 定义
# updated更新后返回新的Map
def updated[B1 >: B](key: A, value: B1): Map[A, B1]
def update(key: A, value: B): Unit
def put(key: A, value: B): Option[B]
```

###3.3.6 keys、keySet、keysIterator得到键的集合

```
# 定义
def keys: collection.Iterable[A]
def keySet: collection.Set[A]
def keysIterator: Iterator[A]
```

###3.3.7 values、valuesIterator得到值的集合

```
# 定义
def values: collection.Iterable[B]
def valuesIterator: Iterator[B]
```

###3.3.8 遍历

```
# 方法一：
for ((k, v) <- m)

# 方法二：
val it = m.iterator
while (it.hasNext)
    it.next
    
# 方法三
m.foreach(println)
```

###3.3.9 可变转不可变

使用toMap或`++`实现

###3.3.10 filterKeys、mapValues、transform转换函数

```
# 定义
# 针对键进行筛选，它只返回键满足断言的那些键值对
def filterKeys(p: (A) => Boolean): collection.Map[A, B]

# 对值进行处理，返回新的键值对集合
def mapValues[C](f: (B) => C): collection.Map[A, C]

# 与mapValues类似，当键值都会传给函数参数
def transform(f: (A, B) => B): Map.this.type
```

###3.3.11 andThen、applyOrElse、lift、orElse、runWith偏函数

```
# 定义
def andThen[C](k: (B) => C): PartialFunction[A, C]
def applyOrElse[A1 <: A, B1 >: B](x: A1, default: (A1) => B1): B1
def lift: (A) => Option[B]
def orElse[A1 <: A, B1 >: B](that: PartialFunction[A1, B1]): PartialFunction[A1, B1]
def runWith[U](action: (B) => U): (A) => Boolean

# 示例
val m = Map("a" -> 1, "b" -> 2, "c" -> 3)
m.andThen(_ * 100)("b")  // 200
m.applyOrElse("z", (x: String) => 100) // 100
m.lift("z")  // None

val pf: PartialFunction[String, Int] = {
    case x => x.charAt(0) - 'a'
}
m.orElse(pf)("z")  // 25
m.runWith(println(_))("z")  // false
```

###3.3.12 clone克隆

```
# 定义
def clone(): Map[A, B]
```

###3.3.13 翻转键值对

通过map方法产生新的映射

###3.3.14 Set转Map

```
# 方案一：
zipWithIndex

# 方案二
set.zip(Stream.from(1)).toMap
```

#4. 数组（Array）
##4.1 初始化

```
val t = new Array[Int](5)

# 二维数组
val a2 = Array.ofDim[String](2, 3)

# 同时Array提供了一维、二维、三维、四维、五维的ofDim的重载方法
```

##4.2 基本方法

方法 | 描述
---- | -----
size、length | 数组的长度
update | 更新数组
concat | 连接两个数组
copy | 复制数组
range | 生成等差数列
fill | 填充数组
tabulate | 和集合的tabulate相同，根据Range对象生成一个数组

##4.3 其他特质或类

类或特质 | 描述
------- | ------
ArrayOps | 通过隐式转换，给Array提供操作
Searching | 可以用来搜索一个排好序的序列类型的集合
WrappedArray | 对原始数组的包装，内部引用原始数组，各种操作还是转换成对原始数组的操作

#5. 字符串（String和StringBuilder）

可以使用`"""`方式实例化多行字符串，Scala会将中间的字符原封不动地赋值给变量和常量，不会进行转义。

使用`==`进行equals比较，和Java不同。使用eq、ne判断两个字符串是否引用同一个对象。

**格式化**

- 常规、字符、数字的转换格式：

  ```
  %[argument_index$][flags][width][.precision]conversion
  ```
  
- 时间、日期

  ```
  %[argument_index$][flags][width]conversion
  ```

描述：

  - argument_index：十进制整数，参数在参数列表中的位置
  - flags：修改输出的字符集合，合法的字符集合取决于要转换的类型
  - width：非负十进制整数，向输出写入的最少字符数
  - precision：非负十进制数，通常用于限制字符数。行为取决于要转换的类型
  - conversion：第一个字符是t或T，第二个字符指示应该采用哪种格式。

conversion转换列表

转换字符 | 描述
------ | -----
b、B | 常规，true或false
h、H | 常规，null或对象hash值的十六进制格式
s、S | 常规，null或字符串
c、C | 字符，Unicode字符
d | 十进制整数
o | 八进制整数
x、X | 十六进制整数
e、E | 以科学计数法表示的浮点数
f | 用小数表示的浮点数
g、G | 依赖其精度和四舍五入的值，可能以小数或科学计数法表示
a、A | 十六进制表示的浮点数
t、T | 日期、时间的前导符
% | 百分比符号
n | 平台相关的换行符

日期类型

时间转换字符 | 描述
------- | -------
H | 24小时制小时数，00 - 23
I | 12小时制小时数，01 - 12
k | 24小时制，0 - 23
l | 12小时制，1 - 12
M | 分钟，00 - 59
S | 秒，00 - 60
L | 毫秒，000 - 999
N | 纳秒，000000000 - 999999999
p | 上下午
z | RFC822定义的数字时区，如+0800
Z | 时区的简写，字符串形式
s | 自1-1 1970 00:00:00UTC起的秒数
Q | 自1-1 1970 00:00:00UTC起的毫秒数

日期转换字符 | 描述
------ | -------
B | 月份全程
b | 月份缩写
h | 同b
A | 星期全称
a | 星期简写
C | 月份除以100，格式化两位，00 - 99
Y | 年份，至少4位
y | 年份的后两位，00 - 99
j | 一年中的第几天， 001 - 366
m | 一年中的第几月，01 - 13
d | 月份中的第几天，01 - 31
e | 月份中的第几天，1 - 31

日期、时间转换字符 | 描述
------- | -------
R | 格式化24小时制时间为 "%tH:%tM"
T | 格式化24小时制时间为 "%tH%tM%tS"
r | 格式化12小时制时间为 "%tI:%tM:%tS %Tp"
D | 日期格式化为 "%tm/%td/%ty"
F | ISO 8601 完整的时间格式化为 "%tY-%tm-%td"
c | 日期和时间格式化为 "%ta %tb %td %tT %tZ %tY"

Flag的格式（y代表类型支持此种flag）

Flag | 常规  | 字符  | 整型 | 浮点型 | 日期/时间 | 描述
---- | ---- | ---- | ---- | ----- | -------- | ------
'-'  | y    | y    | y    | y     | y        | 左对齐
'#'  | y    |      | y    | y     |          | 结果应该使用依赖于转换类型的替换形式
'+'  |      |      | y    | y     |          | 包含正负号
' '  |      |      | y    | y     |          | 对于正数包含一个前导空格
'0'  |      |      | y    | y     |          | 补0
','  |      |      | y    | y     |          | 结果将包括特定于语言环境的组分隔符
'('  |      |      | y    | y     |          | 结果将是用圆括号括起来的负数

**strip**

方法 | 描述
------ | ------
stripLineEnd | 去掉尾部换行符
stripPrefix | 去除指定前缀
stripSuffix | 去除指定后缀
stripMargin | 多行字符中以空格、其他控制符以及分隔符（"|"）为前缀，去除这些符号

**字符串窜改**

```
# s窜改器
# 将上下文的变量值替换字符串中的变量引用
println(s"hello $name. 我知道你的名字长度为${name.length}")

# f窜改器
# 用来产生简单格式化的字符串，类似C语言的printf方法
val name = "姚明"
val height = 2.26d
import java.util.Date
val birthday = new Date(80, 8, 12)
println(f"${name} 身高是${height}%2.2f米 出生于${birthday}%tF")

# raw窜改器
# 类似与s窜改器，在定义正则表达式的时候非常有用
val name = "world"
val s = raw"hello\n$name"   // hello\nworld

# 自定义窜改器
import scala.util.parsing.json
implicit class JsonHelper(val sc: StringContext) extends AnyVal {
    def json(args: Any*): Option[Any] = {
        val s = sc.s(args:_*)
        JSON.parseFull(s)
    }
}
val name = "姚明"
val height = 2.26d
val person = json"""{ "name": "$name", "height": $height }"""
println(person.get)
```

#6. 列表

List是一个sealed abstract class，有两个case class子类：scala.Nil和scala::，它们实现了isEmpty、head和tail方法。

```
# 初始化
val l = List(1, 2, 3)
# 调用List的::方法初始化
val l2 = -2 :: -1 :: Nil

# 用:::将一个列表增加到另一个列表的头部
println(l2 ::: l)

# 左边的列表反转再和右边的列表合并
def reverse_:::(prefix: List[A]): List[A]
```

#7. 流（Stream）
##7.1 初始化、#::、#:::

```
# 构建Stream对象
def #::(hd: A): Stream[A]
def #:::(prefix: Stream[A]): Stream[A]
```

##7.2 流相关类

相关类 | 描述
------ | ------
class Stream | 提供了流的通用方法，主要是继承于集合的通用方法，它基于流的特点重写了很多方法
object Stream | 利用工厂模式提供了一些快速创建流的方法
object #:: | 提供了unapply方法，主要是为了定义模式匹配的抽取器
class Cons | 链表形式的流的子类，是流的具体实现
object cons | 提供了apply，方便生成Cons实例。unapply调用#::的unapply
ConsWrapper | 流的包装类，主要提供#::和#:::方法
object Empty | 空的流

#8. 集合总结
##8 性能特点

###8.1.1 序列类型集合

- C：O(1)
- eC：接近O(1)
- aC：平均O(1)
- Log：O(logn)
- L：线性
- -：不支持

xx | head | tail | apply | update | prepend | append | insert
---- | ---- | ----- | ---- | ----- | ----- | ----- | -----
**immutable** |   |   |   |   |   |   | 
List | C | C | L | L | C | L | -
Stream | C | C | L | L | C | L | -
Vector | eC | eC | eC | eC | eC | eC | -
Stack | C | C | L | L | C | C | L
Queue | aC | aC | L | L | L | C | -
Range | C | C | C | - | - | - | -
String | C | L | C | L | L | L | -
|   |   |   |   |   |   | 
**mutalbe** |   |   |   |   |   |   |
ArrayBuffer | C | L | C | C | L | aC | L
ListBuffer | C | L | L | L | C | C | L
StringBuilder | C | L | C | C | L | aC | L
MutableList | C | L | L | L | C | C | L
Queue | C | L | L | L | C | C | L
ArraySeq | C | L | C | C | - | - | -
Stack | C | L | L | L | C | L | L
ArrayStack | C | L | C | C | aC | L | L
Array | C | L | C | C | - | - | -

###8.1.2 Set和Map

 | lookup | add | remove | min
---- | ---- | --- | ----- | -----
**immutable** | | | |
HashSet、HashMap | eC | eC | eC | L
TreeSet、TreeMap | Log | Log | Log | Log
BitSet | C | L | L | eC
ListMap | L | L | L | L
 | | | |
**mutable** | | | |
HashSet、HashMap | eC | eC | eC | L
WeakHashMap | eC | eC | eC | L
BitSet | C | aC | C | eC
TreeSet | Log | Log | Log | Log

##8.2 Java集合类的转换

待转换类型 | 转换方法 | 返回类型
----- | ------- | ------
scala.collection.Iterator | asJava | java.util.Iterator
scala.collection.Iterator | asJavaEnumeration | java.util.Enumeration
scala.collection.Iterable | asJava | java.lang.Iterable
scala.collection.Iterable | asJavaCollection | java.util.Collection
scala.collection.mutable.Buffer | asJava | java.util.List
scala.collection.mutable.Seq | asJava | java.util.List
scala.collection.Seq | asJava | java.util.List
scala.collection.mutable.Set | asJava | java.util.Set
scala.collection.Set | asJava | java.util.Set
scala.collection.mutable.Map | asJava | java.util.Map
scala.collection.Map | asJava | java.util.Map
scala.collection.mutable.Map | asJavaDictionary | java.util.Dictionary
scala.collection.mutable.ConcurrentMap | asJavaConcurrentMap | java.util.concurrent.ConcurrentMap
java.util.Iterator | asScala | scala.collection.Iterator
java.util.Enumeration | asScala | scala.collection.Iterator
java.lang.Iterable | asScala | scala.collection.Iterable
java.util.Collection | asScala | scala.collection.Iterable
java.util.List | asScala | scala.collection.mutable.Buffer
java.util.Set | asScala | scala.collection.mutable.Set
java.util.Map | asScala | scala.collection.mutable.Map
java.util.concurrent.ConcurrentMap | asScala | scala.collection.mutable.ConcurrentMap
java.util.Dictionary | asScala | scala.collection.mutable.Map
java.util.Properties | asScala | scala.collection.mutable.Map[String, String]