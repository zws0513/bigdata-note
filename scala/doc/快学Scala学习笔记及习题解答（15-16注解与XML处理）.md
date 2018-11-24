本文Scala使用的版本是2.11.8

##第15章 注解

###15.1 基本概念

注解是那些你插入到代码中以便有工具可以对它们进行处理的标签。

在Scala里，可以为类、方法、字段、局部变量和参数添加注解。可以同时添加多个注解（先后次序没有影响）。

**主构造器**

需要将注解放置在构造器之前，如果不带参数的话，需加上一对圆括号。

```
class Credentials @Inject() (var username: String, var password: String)
```

**表达式**

需要在表达式后加上冒号，然后是注解本身。

```
(myMap.get(key): @unchecked) match { ... }
```

**类型参数**

```
class MyContainer[@speciallized T]
```

**实际类型**

```
String @cps[Unit] // 在类型名称之后，@cps带一个类型参数
```

###15.2 注解参数

可以有带名参数，但如果参数名为value，则该名称可以直接略去。如果注解不带参数，则圆括号可以略去。

Java注解的参数类型只能是：

- 数值型的字面量
- 字符串
- 类字面量
- Java枚举
- 其他注解
- 上述类型的数组（但不能使数组的数组）

Scala注解的参数可以是任何类型。

###15.3 注解实现

注解必须扩展Annotation特质。

```
class unchecked extends annotation.Annotation
```

注解类可以选择扩展StaticAnnotation或ClassfileAnnotation特质。StaticAnnotation在编译单元中可见——它将放置Scala特有的元数据到类文件中。而ClassfileAnnotation的本意是在类文件中生成Java注解元数据。

###15.4 针对Java特性的注解

**Java修饰符**

Scala注解 | Java修饰符 | 描述
----- | -------- | ------
@volatile | volatile | 字段可以被多个字段同时更新
@transient | transient | 字段不会被序列化
@strictfp | strictfp | 使用IEEE的double值进行浮点计算，而不是80位扩展精度（Intel处理器默认使用的实现）
@native | native | 标记那些在C或C++代码中实现的方法

**标记接口**

Scala注解 | Java接口 | 描述
------- | -------- | --------
@cloneable | Cloneable | 标记可被克隆的对象
@remote | java.rmi.Remote | 远程对象
@SerialVersionUID | Serialization | @SerialVersionUID已过时，需扩展scala.Serialization特质

**受检异常**

如果从Java代码中调用Scala的方法，其签名应包含那些可能被抛出的受检异常。用@throws注解来生成正确的签名。

```
class Book {
    @throws(classOf[IOException]) def read(fileName: String) {
        ...
    }
    ...
}
```

**变长参数**

```
def process(args: String*)

// 将被编译成
def process(args: Seq[String])
```

使用@varargs

```
@varargs def process(args: String*)

// 将被编译成如下Java方法
void process（String... args)
```

**JavaBeans**

添加上@scala.reflect.BeanProperty注解，编译器将生成JavaBean风格的getter和setter方法。

@BooleanBeanProperty生成带有is前缀的getter方法，用于Boolean。

###15.5 用于优化的注解

**尾递归**

递归调用有时能被转化为循环。

例如

```
def sum(xs: Seq[Int], partial: BigInt): BigInt = 
  if (xs.isEmpty) partial else sum(xs.tail, xs.head + partial)
```

Scala编译器会自动应用“尾递归”优化。

有时Scala编译器无法进行尾递归优化，则应该给你的方法加上@tailrec注解。

**跳转表生成与内联**

@switch注解让你检查Scala的match语句是否真的被编译成了跳转表

```
(n: @switch) match {
  case 0 => "Zero"
  case 1 => "One"
  case _ => "?"
}
```

@inline和@noinline来告诉Scala编译器要不要内联。

**可省略方法**

@elidable注解给那些可以在生产代码中移除的方法上打标记，elidable对象定义了如下数值常量：

- MAXIMUM 或 OFF = Int.MaxValue
- ASSERTION = 2000
- SERVERE = 1000
- WARNING = 900
- INFO = 800
- CONFIG = 700
- FINE = 500
- FINER = 400
- FINEST = 300
- MINIMUM 或 ALL = Int.MinValue

默认注解的值低于1000的方法会被省略，剩下SEVERE的方法和断言。

可以使用-Xelide-below修改省略等级

```
scalac -Xelide-below INFO myprog.scala
```

<font color=red>
对被省略的方法调用，编译器会替换成Unit对象。如果使用了返回值，则会抛出ClassCastException。
</font>

**基本类型的特殊化**

```
def allDifferent[T](x: T, y: T, z: T) = x != y && x != z && y != z
```

如果调用allDifferent(2, 3, 4)时，每个整数值都被包装成一个java.lang.Integer。当然可以给出重载版本，以及其他7个基本类型的重载方法。

而使用@specialized注解，编译器会自动生成这些方法。

```
def allDifferent[@specialized T](x: T, y: T, z: T) = x != y && x != z && y != z

// 或者限定子集
def allDifferent[@specialized(Long, Double) T](x: T, y: T, z: T) = x != y && x != z && y != z
```

###15.6 用于错误和警告的注解

@deprecated注解，当编译器遇到对这个特性的使用时都会生成一个警告信息。

```
@deprecated(message="Use factorial(n: BigInt) instead"
def factorial(n: Int): Int = ...
```

@deprecatedName可以被应用到参数上。

@implicitNotFound注解用于在某个隐式参数不存在的时候生成有意义的错误提示。

@unchecked注解用于在匹配不完整时取消警告信息。

@uncheckedVariance注解会取消与型变相关的错误提示。

###15.7 习题解答

<font color=red>未完待续</font>

<font size =4 color=Blue>
1. 编写四个JUnit测试用例，分别使用带或不带某个参数的@Test注解。用JUnit执行这些测试。
</font>

```
// 实体类
package com.zw.demo.fifteen

/**
  * Created by zhangws on 17/1/28.
  */
object One {

    def main(args: Array[String]) {

    }
}

class ScalaTest {

    def test1(): Unit = {

    }

    def test2(): Unit = {

    }
}

// JUnit测试类
package com.zw.demo.fifteen

import org.junit.Test

/**
 * Created by zhangws on 17/1/28.
 */
class ScalaTestTest {

    @Test
    def testTest1() {

        System.out.println("test1");
    }


    @Test(timeout = 1L)
    def testTest2() {

        System.out.println("test2");
    }
}
```


<font size =4 color=Blue>
2. 创建一个类的示例，展示注解可以出现的所有位置。用@deprecated作为你的示例注解。
</font>

```
@deprecated
class Two {

    @deprecated
    var t: String = _

    @deprecated(message = "unuse")
    def hello() {
        println("hello")
    }
}

@deprecated
object Two extends App {
    val t = new Two()
    t.hello()
    t.t
}
```

<font size =4 color=Blue>
3. Scala类库中的哪些注解用到了元注解@param,@field,@getter,@setter,@beanGetter或@beanSetter?
</font>

略

<font size =4 color=Blue>
4. 编写一个Scala方法sum,带有可变长度的整型参数，返回所有参数之和。从Java调用该方法。
</font>

```
// Four.scala
class Four {
    @varargs def sum(nums: Int*): Int = {
        nums.sum
    }
}

// FourTest.java
public class FourTest {
    public static void main(String[] args) {
        Four t = new Four();
        System.out.println(t.sum(1, 2, 3));
    }
}
```

<font size =4 color=Blue>
5. 编写一个返回包含某文件所有行的字符串的方法。从Java调用该方法。
</font>

```
// Five.scala
class Five {

    def read() = {
        Source.fromFile("Four.scala").mkString
    }
}

// FiveTest.java
public class FiveTest {

    public static void main(String[] args) {
        Five t = new Five();
        System.out.println(t.read());
    }
}
```

<font size =4 color=Blue>
6. 编写一个Scala对象，该对象带有一个易失(volatile)的Boolean字段。让某一个线程睡眠一段时间，之后将该字段设为true，打印消息，然后退出。而另一个线程不停的检查该字段是否为true。如果是，它将打印一个消息并退出。如果不是，则它将短暂睡眠，然后重试。如果变量不是易失的，会发生什么？
</font>

```
```


<font size =4 color=Blue>
7. 给出一个示例，展示如果方法可被重写，则尾递归优化为非法
</font>



<font size =4 color=Blue>
8. 将allDifferent方法添加到对象，编译并检查字节码。@specialized注解产生了哪些方法?
</font>



<font size =4 color=Blue>
9. Range.foreach方法被注解为@specialized(Unit)。为什么？通过以下命令检查字节码：

```
javap -classpath /path/to/scala/lib/scala-library.jar scala.collection.immutable.Range
```

并考虑Function1上的@specialized注解。点击Scaladoc中的Function1.scala链接进行查看。
</font>


<font size =4 color=Blue>
10. 添加assert(n >= 0)到factorial方法。在启用断言的情况下编译并校验factorial(-1)会抛异常。在禁用断言的情况下编译。会发生什么？用javap检查该断言调用
</font>

```

```

##第16章 XML处理

处理XML的jar包（[参考](https://github.com/scala/scala-xml)）：

```
<dependency>
    <groupId>org.scala-lang.modules</groupId>
    <artifactId>scala-xml_${scala.binary.version}</artifactId>
    <version>1.0.6</version>
</dependency>
<dependency>
    <groupId>org.scala-lang.modules</groupId>
    <artifactId>scala-parser-combinators_${scala.binary.version}</artifactId>
    <version>1.0.4</version>
</dependency>
<dependency>
    <groupId>org.scala-lang.modules</groupId>
    <artifactId>scala-swing_${scala.binary.version}</artifactId>
    <version>1.0.2</version>
</dependency>
```

###16.1 XML字面量

Scala对XML有内建支持。

```
val doc = <html><head><title>XML字面量</title></head><body>...</body></html>
```

doc的类型为scala.xml.Elem，表示一个XML元素。

也可以表示一系列节点，如下类型为scala.xml.NodeSeq。

```
val items = <li>节点一</li><li>节点二</li>
```

###16.2 XML节点

![XML节点](http://img.blog.csdn.net/20170203212839857?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMzk4MDEyNw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

###16.3 元素属性

可以使用attributes属性访问某个元素的属性键和值，它产出一个类型为MetaData的对象。

```
val elem = <a href="https://scala-lang.org/">The Scala Language</a>
val url = elem.attributes("href")
```

<font color=red>但如果文本中有无法解析的字符时，会有问题</font>

如果不存在未被解析的实体，可以使用text方法。

```
val url = elem.attributes("href").text

// 如果不喜欢处理null，可以使用get，它返回Option[Seq[Node]]
val url = elem.attributes.get("href").getOrElse(Text(""))
```

遍历所有属性

```
for (attr <- elem.attributes)
    // 处理attr.key和attr.value.text
    
// 或者
val map = elem.attributes.asAttrMap
```

###16.4 内嵌表达式

可以在XML字面量中包含Scala代码块，动态地计算出元素内容，例如：

```
def main(args: Array[String]) {
    val s = "Hello"
    val v = <ul>{for (i <- 0 until s.length) yield <li>{i}</li>}</ul>
    println(v)
}
```

###16.5 在属性中使用表达式

如果内嵌代码块返回null或None，该属性不会被设置。

```
<img alt={if (description == "TODO" null else description} ... />
```

###16.6 特殊节点类型

**PCData**

如果输出中带有CDATA，可以包含一个PCData节点。

```
val code = """if (temp < 0) alert("Cold!")"""
val js = <script>{PCData(code)}</script>
```

**Unparsed**

可以在Unparsed节点中包含任意文本，它会被原样保留。

```
val n1 = <xml:unparsed><&></xml:unparsed>
val n2 = Unparsed("<&>")
```

**group**

```
val g1 = <xml:group><li>Item 1</li><li>Item 2</li></xml:group>
val g2 = Group(Seq(<li> Item 1</li>, <li>Item 2</li>))
```

遍历这些组时，它们会自动被解开：

```
val items = <li>Item 1</li><li>Item 2</li>

for (n <- <xml:group>{items}</xml:group>) yield n
    // 产生出两个li元素
    // <li>Item 1</li>
    // <li>Item 2</li>
    
for (n <- <col>{items}</col>) yield n
    // 产生一个col元素
    // <col><li>Item 1</li><li>Item 2</li></col>
```

###16.7 类XPath表达式

NodeSeq类提供了类似XPath中`/`和`//`操作符的方法，只不过在Scala中用`\`和`\\`来代替。

`\`定位某个节点或节点序列的直接后代：

```
val list = <dl><dt>Java</dt><dd>Gosling</dd><dt>Scala</dt><dd>Odersky</dd></dl>
println(list \ "dt")

// 结果
<dt>Java</dt><dt>Scala</dt>
```

通配符可以匹配任何元素：

```
doc \ "body" \ "_" \ "li"
// 将找到所有li元素
```

`\\`可以定位任何深度的后代：

```
doc \\ "img"
// 将定位doc中任何位置的所有img元素
```

`@`开头的字符串可以定位属性

```
img \ "@alt"
// 将返回给定节点的alt属性

doc \\ "@alt"
// 将定位到doc中任何元素的所有alt属性
```

###16.8 模式匹配

```
node match {
    case <img/> => ... // 匹配带有任何属性但没有后代的img元素
    case <li>{_}</li> => ... // 匹配单个后代
    case <li>{_*}</li> => ... // 匹配任意多的项
    case <li>{child}</li> => ... // 使用变量名，成功匹配到的内容会被绑定到该变量上
    case <li>{Text(item)}</li> => item // 匹配一个文本节点
    case <li>{children @ _*}</li> => for (c <- children) yield c // 把节点序列绑到变量，children是一个Seq[Node]
    case <p>{_*}</p><br/> => ... // 非法，只能用一个节点
    case <img alt="TODO"/> => ... // 非法，不能带有属性
    case n @ <img/> if (n.attributes("alt").text == "TODO") => ... // 使用守卫，匹配属性
}
```

###16.9 修改元素和属性

在Scala中，XML节点和节点序列是不可变。所以编辑只能通过拷贝（copy方法），它有五个带名参数：label、attributes、child、prefix和scope。

```
val list = <ul><li>Fred</li><li>Wilma</li></ul>
val list2 = list.copy(label = "ol")
// 结果
// <ol><li>Fred</li><li>Wilma</li></ol>

// 添加后代
list.copy(child = list.child ++ <li>Another item</li>)

// 添加或修改一个属性，使用%操作符
val image = <img src="hamster.jpg"/>
val image2 = image % Attribute(null, "alt", "An image of a hamster", scala.xml.Null)
// 第一个参数命名空间，最后一个是额外的元数据列表

// 添加多个属性
val image3 = image % Attribute(null, "alt", "An image of a frog", Attribute(null, "src", "frog.jpg", scala.xml.Null))
```

###16.10 XML变换

XML类库提供了一个RuleTransformer类，该类可以将一个或多个RewriteRule实例应用到某个节点及其后代。

例如：把文档中所有的ul节点都修改为ol

```
val rule1 = new RewriteRule {
    override def transform(n: Node) = n match {
        case e @ <ul>{_*}</ul> => e.asInstanceOf[Elem].copy(label = "ol")
        case _ => n
    }
}

val transformer = new RuleTransformer(rule1, rule2, rule3);
// transform方法遍历给定节点的所有后代，应用所有规则，最后返回经过变换的树
```

###16.11 加载和保存

####16.11.1 加载

**loadFile**

```
import scala.xml.XML

val root = XML.loadFile("myfile.xml")
```

**InputStream、Reader或URL加载**

```
val root2 = XML.load(new FileInputStream("myfile.xml"))
val root3 = XML.load(new InputStreamReader(new FileInputStream("myfile.xml", "UTF-8"))
val root4 = XML.load(new URL("http://horstmann.com/index.html"))
```

**ConstructingParser**

该解析器可以保留注释、CDATA节和空白（可选）：

```
import scala.xml.parsing.ConstructingParser
import java.io.File

val parser = ConstructingParser.fromFile(new File("myfile.xml"), perserveWS = true)
val doc = parser.document
val root = doc.docElem
```

####16.11.2 保存

**save**

```
XML.save("myfile.xml", root)
有以下三个可选参数：
- enc指定编码（默认“IOS-8859-1”）
- xmlDecl用来指定输出中最开始是否要生成XML声明（ <?xml...?> ）（默认为false）
- doctype是样例类scala.xml.dtd.DocType的对象（默认为null）

// 示例
XML.save("myfile.xhtml", root,
  enc = "UTF-8",
  xmlDecl = true,
  doctype = DocType("html", 
    PublicID("-//W3C//DTD XHTML 1.0 Strict//EN",
      "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"),
    Nil))
```

**Writer**

```
XML.write(writer, root, "UTF-8", false, null)

// 没有内容的元素不会被写成自结束的标签，默认样式
// <img src="hamster.jpg"></img>
// 如果想要<img src="hamster.jpg"/>，使用
val str = xml.Utility.toXML(node, minimizeTags = true)

// 如果想排版美观，可以用PrettyPrinter类
val printer = new PrettyPrinter(width = 100, step = 4)
val str = printer.formatNodes(nodeSeq)
```

###16.12 命名空间

XML的命名空间是一个URI（通常也是URL）。xmlns属性可以声明一个命名空间：

```
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>...</head>
    <body>...</body>
</html

// html元素及其后代（head、body等）将被放置在这个命名空间当中。
```

可以引入自己的命名空间

```
<svg xmlns="http://www.w3.org/2000/svg" ...>
    ...
</svg>
```

在Scala中，每个元素都有一个scope属性，类型为NamespaceBinding。该类的uri属性将输出命名空间的URI。

**命名空间前缀**

```
<html xmlns="http://www.w3.org/1999/xhtml"
    xmlns:svg="http://www.w3.org/2000/svg">
    
<svg:svg width="100" height="100">
    ...
</svg>
```

**编程生成XML元素**

```
val scope = new NamespaceBinding("svg", "http://www.w3.org/2000/svg", TopScope)
val attrs = Attribute(null, "width", 100,
  Attribute(null, "height", 100, Null))
val elem = Elem(null, "body", Null, TopScope,
  Elem("svg", "svg", attrs, scope))
```

###16.13 习题解答

<font color=red>未完待续</font>

<font size =4 color=Blue>
1. `<fred/>(0)`得到什么？`<fred/>(0)(0)`呢？为什么？
</font>

```
scala> println(<fred/>(0))
<fred/>

scala> println(<fred/>(0)(0))
<fred/>
```

因为都是scala.xml.Node，是NodeSeq的子类，等同于长度为1的序列。

<font size =4 color=Blue>
2. 如下代码的值是什么？

```
<ul>
  <li>Opening bracket: [</li>
  <li>Closing bracket: ]</li>
  <li>Opening bracket: {</li>
  <li>Closing bracket: }</li>
</ul>
```
</font>

```
<ul>
  <li>Opening bracket: [</li>
  <li>Closing bracket: ]</li>
  <li>Opening bracket: {{</li>
  <li>Closing bracket: }}</li>
</ul>
```

花括号作为字面量，需要连写两个。

<font size =4 color=Blue>
3. 对比

```
<li>Fred</li> match { case <li>{Text(t)}</li> => t }
```

和

```
<li>{"Fred"}</li> match { case <li>{Text(t)}</li> => t }
```

为什么它们的行为不同？
</font>

前一个输出Fred，后一个异常`scala.MatchError: <li>Fred</li> (of class scala.xml.Elem)`

<font size =4 color=Blue>
4. 读取一个XHTML文件并打印所有不带alt属性的img元素。
</font>

```
```

<font size =4 color=Blue>
5. 打印XHTML文件中所有图像的名称，即打印所有位于img元素内的src属性值。
</font>

```
```

<font size =4 color=Blue>
6. 读取XHTML文件并打印一个包含了文件中给出的所有超链接及其url的表格。即，打印所有a元素的child文本和href属性。
</font>

```
```

<font size =4 color=Blue>
7. 编写一个函数，带一个类型为Map[String,String]的参数，返回一个dl元素，其中针对映射中每个键对应有一个dt，每个值对应有一个dd，例如：

```
Map(“A”->”1”,”B”->”2”)
```

应该产出

```
<dl><dt>A</dt><dd>1</dd><dt>B</dt><dd>2</dd></dl>
```
</font>

```
```

<font size =4 color=Blue>
8. 编写一个函数，接受dl元素，将它转成Map[String,String]。该函数应该是前一个练习中的反向处理，前提是所有dt后代都是唯一的。
</font>

```
```

<font size =4 color=Blue>
9. 对一个XHTML文档进行变换，对所有不带alt属性的img元素添加一个alt=”TODO”属性，其他内容完全不变。
</font>

```
```

<font size =4 color=Blue>
10. 编写一个函数，读取XHTML文档，执行前一个练习中的变换，并保存结果。确保保留了DTD以及所有CDATA内容。
</font>

```
```