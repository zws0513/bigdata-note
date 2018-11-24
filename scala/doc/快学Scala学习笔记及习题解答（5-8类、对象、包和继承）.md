本文scala使用的版本是2.11.7

##第五章 类
###5.1 基本操作

```
class Person {

    // Scala会生成一个私有的final字段和一个getter方法,但没有setter
    val timeStamp = new java.util.Date

    // 必须初始化字段
    private var privateAge = 0

    def increment() {
        privateAge += 1
    }

    // 方法默认是公有的
    def current() = privateAge

    // 自定义age的getter方法
    def age = privateAge

    // 自定义age的setter方法
    def age_= (newValue: Int) {
        if (newValue > privateAge) privateAge = newValue
    }
}

object Note1 extends App {
    val fred = new Person
    fred.age = 20
    println(fred.age)
    fred.age = 12
    println(fred.age)
}
```

###5.2 对象私有字段

在Scala（Java和C++也一样）中，方法可以访问该类的所有对象的私有字段，例如：

```
class Counter {
    private var value = 0
    // 可以访问另一个对象的私有字段
    def isLess(other: Counter) = value < other.value
    
    // 类似某个对象.value2这样的访问将不被允许
    // 此时Scala不会生成getter和setter方法
    private[this] var value2 = 0
}
```

###5.3 Bean属性

```
import scala.reflect.BeanProperty

class Person {
    @BeanProperty var name: String = _
}
```

Scala字段 | 生成的方法 | 何时使用
---- | ----- | -----
val/var name | 公有的name<br>name\_=（仅限var） | 实现一个可以被公开访问并背后是以字段形式保存的属性
@BeanProperty val/var name | 公有的name<br>getName()<br>name\_=（仅限于var）<br>setName(...)（仅限于var） | 与JavaBean互操作
private val/var name | 私有的name<br>name\_=（仅限于var） | 用于将字段访问限制在本类的方法
private[this] val/var name | 无 | 用于将字段访问限制在同一个对象上调用的方法
private[类名] val/var name | 依赖于具体实现 | 将访问权赋予外部类


###5.4 构造器

**辅助构造器**

  1. 辅助构造器的名称为this
  2. 每一个辅助构造器都必须以一个对先前已定义的其他辅助构造器或主构造器的调用开始

```
class Person {
    private var name = ""
    private var age = 0

    def this(name: String) {
        this  // 调用主构造器
        this.name = name
    }

    def this(name: String, age: Int) {
        this(name)
        this.age = age
    }
}
```

**主构造器**

  1. 主构造器的参数直接放置在类名之后

  ```
  class Person(val name: String, private var age: Int, addr: String)
  
  // 如果不带val或var的参数（例如addr）至少被一个方法所使用，它将被升格为字段。类似这样的字段等同于private[this] val字段效果。
  ```
  
  2. 主构造器会执行类定义中的所有语句
  
  ```
  class Person(val name: String, val age: Int) {
      println("创建Person对象")
      def desc = name + " is " + age + " years old"
  }
  
  // 上面的println就会被执行
  ```

###5.5 嵌套类

在Scala中，几乎可以在任何语法结构中内嵌任何语法结构。

```
class Network {
    class Member {
    }
}
```

###5.6 习题解答

http://ivaneye.iteye.com/blog/1829957

<font size =4 color=Blue>
1. 改进5.1节的Counter类,让它不要在Int.MaxValue时变成负数
</font>

```
class Counter {
  private var value = 0
  def increment() { if (value < Int.MaxValue) value += 1 }
  def current = value
}

val myCounter = new Counter
myCounter.increment
println(myCounter.current)
```

<font size =4 color=Blue>
2. 编写一个BankAccount类，加入deposit和withdraw方法，和一个只读的balance属性 
</font>

```
class BankAcount(val balance : Int = 0) {
  def deposit() {}
  def withdraw() {}
}
```

<font size =4 color=Blue>
3. 编写一个Time类，加入只读属性hours和minutes，和一个检查某一时刻是否早于另一时刻的方法before(other:Time):Boolean。Time对象应该以new Time(hrs,min)方式构建。其中hrs以军用时间格式呈现(介于0和23之间) 
</font>

```
class Time(val hours : Int, val minutes : Int) {
  def before(other : Time) : Boolean = {
    this.hours < other.hours || (this.hours == other.hours && this.minutes < other.minutes)
  }
}

val fir = new Time(10, 30)
val sec = new Time(10, 50)
val thi = new Time(12, 10)

sec.before(fir)
sec.before(thi)
```

<font size =4 color=Blue>
4. 重新实现前一个类中的Time类，将内部呈现改成午夜起的分钟数(介于0到24*60-1之间)。不要改变公有接口。也就是说，客户端代码不应因你的修改而受影响 
</font>

```
class Time(val hours : Int, val minutes : Int) {
  private val innerValue = hours * 60 + minutes;
  
  def before(other : Time) : Boolean = {
    this.innerValue < other.innerValue
  }
}

val fir = new Time(10, 30)
val sec = new Time(10, 50)
val thi = new Time(12, 10)

sec.before(fir)
sec.before(thi)
```

<font size =4 color=Blue>
5. 创建一个Student类，加入可读写的JavaBeans属性name(类型为String)和id(类型为Long)。有哪些方法被生产？(用javap查看。)你可以在Scala中调用JavaBeans的getter和setter方法吗？应该这样做吗？
</font>

```
import scala.beans.BeanProperty

class Student{
    @BeanProperty var name:String = _
    @BeanProperty var id:Long = _
}
```

<font size =4 color=Blue>
6. 在5.2节的Person类中提供一个主构造器,将负年龄转换为0
</font>

```
class Person(var age:Int){  
  age = if(age < 0) 0 else age  
} 
```

<font size =4 color=Blue>
7. 编写一个Person类，其主构造器接受一个字符串，该字符串包含名字，空格和姓，如new Person("Fred Smith")。提供只读属性firstName和lastName。主构造器参数应该是var,val还是普通参数？为什么？
</font>

```
必须为val。如果为var，则对应的此字符串有get和set方法，而Person中的firstName和lastName为只读的,所以不能重复赋值。如果为var则会重复赋值而报错 
```

<font size =4 color=Blue>
8. 创建一个Car类，以只读属性对应制造商，型号名称，型号年份以及一个可读写的属性用于车牌。提供四组构造器。每个构造器fc都要求制造商和型号为必填。型号年份和车牌可选，如果未填，则型号年份为-1，车牌为空串。你会选择哪一个作为你的主构造器？为什么？
</font>

```
class Car(val maker : String, val typeName : String, val year : Int = -1, var carlice : String = "") {

  override def toString : String = {
    maker + " " + typeName + " " + year + " " + carlice
  }
} 

val car = new Car("中汽", "t-xx")
```

<font size =4 color=Blue>
9. 在Java,C#或C++重做前一个练习。Scala相比之下精简多少？
</font>

这个不写了

<font size =4 color=Blue>
10. 考虑如下的类 

class Employ(val name:String,var salary:Double){ 
    def this(){this("John Q. Public",0.0)} 
} 

重写该类,使用显示的字段定义，和一个缺省主构造器。你更倾向于使用哪种形式？为什么？
</font>

```
class Employ{
    val name:String = "John Q. Public" 
    var salary:Double = 0.0
}
```

##第六章 对象
###6.1 基本概念

**单例对象**

Scala没有静态方法或静态字段，可以用object来达到同样目的。

```
object Accounts {
    private var lastNumber = 0
    def newUniqueNumber() = { lastNumber += 1; lastNumber }
}
```

对象本质上可以拥有类的所有特性，只有一个例外：不能提供构造器参数。

可以用对象实现：

  1. 作为存放工具函数或常量的地方
  2. 高效地共享单个不可变实例
  3. 需要用单个实例来协调某个服务时

**伴生对象**

在Scala中，可以通过定义和类同名的（伴生）对象来提供其他语言（例如Java）中既有实例方法又有静态方法的类。

类和它的伴生类可以互相访问私有特性，但必须定义在同一个源文件中。

**扩展类或特质的对象**

object可以扩展类以及一个或多个特质，结果是一个扩展了指定类以及特质的类的对象。

**apply方法**

举例来说，Array对象定义了apply方法，可以用以下形式创建数组

```
Array("Mary", "had", "a", "little", "lamb")
```

定义apply方法的示例

```
class Account private (val id: Int, initialBalance: Double) {
    private var balance = initialBalance

    override def toString = "id = " + id + " initialBalance = " + initialBalance
}

object Account {
    private var lastNumber = 0

    def newUniqueNumber() = { lastNumber += 1; lastNumber }

    def apply(initialBalance: Double) =
        new Account(newUniqueNumber(), initialBalance)
}

object Note1 extends App {
    val acct = Account(100.0)
    println(acct)
}
```

**应用程序对象**

每个Scala程序都必须从一个对象的main方法开始，类型为Array[String] => Unit

```
object Note1 {
    def main(args: Array[String]) {
        val acct = Account(100.0)
        println(acct)
    }
}
```

也可以扩展App特质，然后将程序代码放入构造器方法体内：

```
object Note1 extends App {
    // 可以通过args属性得到命令行参数
    println(args.length)
    
    val acct = Account(100.0)
    println(acct)
}
```

如果调用应用程序时设置了scala.time选项，则程序在退出时会显示逝去的时间。

###6.2 枚举

Scala并没有枚举类型，但是可以通过标准类库的Enumeration助手类，产生枚举。

```
object TrafficLightColor extends Enumeration {
    type TrafficLightColor = Value
    val Red, Yellow, Green = Value
}

object TrafficLightColor2 extends Enumeration {
    val Red = Value(0, "Stop")
    val Yellow = Value(10)  // 缺省名称为字段名
    val Green = Value("Go") // ID不指定时, 将在前一个枚举值基础上加一, 从零开始
}

// 使用示例
import com.zw.demo.six.TrafficLightColor._

object Note2 {

    def doWhat(color: TrafficLightColor) = {
        if (color == Red) "stop"
        else if (color == Yellow) "hurry up"
        else "go"
    }

    def main(args: Array[String]) {
        // values方法获得所有枚举值得集
        for (c <- TrafficLightColor2.values) println(c.id + ": " + c)

        // 通过枚举的ID或名称来进行查找定位
        println(TrafficLightColor(2))
        println(TrafficLightColor.withName("Red"))
    }
}
```

###6.3 习题解答

http://ivaneye.iteye.com/blog/1832806

<font size =4 color=Blue>
1. 编写一个Conversions对象，加入inchesToCentimeters、gallonsToLiters和milesToKilometers方法
</font>

```
object Conversions {
  def inchesToCentimeters() {
  }
  def gallonsToLiters() {
  }
  def milesToKilometers() {
  }
}
```

<font size =4 color=Blue>
2. 前一个练习不是很面向对象。提供一个通用的超类UnitConversion并定义扩展该超类的InchesToCentimeters、GallonsToLiters和MilesToKilometers对象 
</font>

```
abstract class UnitConversion {
  def inchesToCentimeters() {}
  def gallonsToLiters() {}
  def milesToKilometers() {}
}

object InchesToCentimeters extends UnitConversion{
  override def inchesToCentimeters() {}
}

object GallonsToLiters extends UnitConversion{
  override def gallonsToLiters() {}
}

object MilesToKilometers extends UnitConversion{
  override def milesToKilometers() {}
}
```

<font color=red>
注：超类class中必须有方法体{}，否则对象无法单独实现方法。
</font>

<font size =4 color=Blue>
3. 定义一个扩展自java.awt.Point的Origin对象。为什么说这实际上不是个好主意？(仔细看Point类的方法)
</font>

注：Point中的getLocation方法返回的是Point对象，如果想返回Origin对象，需要Origin类才行 

```
import java.awt.Point

object Origin extends Point with App {

  override def getLocation: Point = super.getLocation

  Origin.move(2,3)
  println(Origin.toString)
}
```

<font size =4 color=Blue>
4. 定义一个Point类和一个伴生对象,使得我们可以不用new而直接用Point(3,4)来构造Point实例
</font>

```
class Point(var x : Float, var y : Float) {
  override def toString: String = "x = " + x + " y = " + y
}

object Point {
  def apply(x : Float, y : Float)={  
    new Point(x, y)  
  }
}

val p = Point(1.2f, 2.4f)
```

<font size =4 color=Blue>
5. 编写一个Scala应用程序, 使用App特质, 以反序打印命令行参数, 用空格隔开。举例来说, scala Reverse Hello World应该打印World Hello 
</font>

```
class Reverse extends App {
  args.reverse.foreach(arg => print(arg  + " ")) 
}
```

<font size =4 color=Blue>
6. 编写一个扑克牌4种花色的枚举, 让其toString方法分别返回♣,♦,♥,♠ 
</font>

```
object Poker extends Enumeration {
  val M = Value("♣")
  val T = Value("♠")
  val H = Value("♥")
  val F = Value("♦")
}
```

<font size =4 color=Blue>
7. 实现一个函数, 检查某张牌的花色是否为红色
</font>

```
object Poker extends Enumeration {
  val M = Value("♣")
  val T = Value("♠")
  val H = Value("♥")
  val F = Value("♦")

  def color(kind : Poker.Value) : Boolean = {
    if (kind == Poker.H || kind == Poker.F) true else false
  }
}

Poker.color(Poker.H)
```

<font size =4 color=Blue>
8. 编写一个枚举, 描述RGB立方体的8个角。ID使用颜色值(例如:红色是0xff0000) 
</font>

```scala
object RGB extends Enumeration {
  val RED = Value(0xff0000, "Red")
  val BLACK = Value(0x000000, "Black")
  val GREEN = Value(0x00ff00, "Green")
  val CYAN = Value(0x00ffff, "Cyan")
  val YELLOW = Value(0xffff00, "Yellow")
  val WHITE = Value(0xffffff, "White")
  val BLUE = Value(0x0000ff, "Blue")
  val MAGENTA = Value(0xff00ff, "Magenta")
}

for (c <- RGB.values) println(c.id + ":" + c)
```

##第七章 包和引入
###7.1 包

Scala的包和其他作用域一样地支持嵌套。通过下面形式的定义，可以访问上层作用域中的名称。

```
package com {
    package horstmann {
        package impatient {
            class Employee
            ....
        }
    }
}
```

串联式包语句，限定了可见的成员。

```
package com.horstmann.impatient {
    // com和com.horstmann的成员在这里不可见
    package people {
        class Person
        ...
    }
}
```

使用文件顶部标记法，可以不带花括号。

###7.2 包对象

用package object定义包对象：

```
package com.horstmann.impatient

package object people {
    val defaultName = "影夜life"
}

package people {
    class Person {
        var name = defaultName // 从包对象拿到的常量
        ...
    }
}
```

<font color=red>
注意defaultName不需要加限定词，因为它位于同一个包内。在其他地方，这个常量可以用com.horstmann.impatient.people.defaultName
</font>

###7.3 包可见性

在Java中，没有被声明为public、private或protected的类成员在包含该类的包中可见。在Scala中，可以通过修饰符达到同样的效果。

```
package com.horstmann.impatient.people

class Person {
    // 在包impatient中可见
    private[impatient] def desc = "A person with name " + name
}
```

###7.4 引入

```
import java.awt.Color

// 引入包下所有成员
import java.awt._

// 引入类或对象的所有成员
import java.awt.Color._
```

而且import语句可以出现在任何地方。效果一直延伸到包含该语句的块末尾

```
class Manager {
    import scala.collection.mutable._
    val sub = new ArrayBuffer[Employee]
}
```

引入包中多个成员时，可以使用选取器（selector）

```
import java.awt.{Color, Font}
```

还可以重命名选到的成员

```
import java.util.{HashMap => JavaHashMap}
```

选取器HashMap => \_将隐藏某个成员

```
import java.util.{HashMap => _, _}
import scala.collection.mutable._

// 这样HashMap无二义地指向scala.collection.mutable.HashMap
```

每个scala程序都隐式地以如下代码开始

```
import java.lang._
import scala._
import Predef._

// 这里的引入，允许覆盖之前的引入（例如scala.StringBuilder会覆盖java.lang.StringBuilder而不是与之冲突。
```

###7.5 习题解答

http://ivaneye.iteye.com/blog/1840359

<font size =4 color=Blue>
1. 编写示例程序，展示为什么 
package com.horstmann.impatient 
不同于 
package com 
package horstmann 
package impatient 
</font>

```
package com {
  class T1() {}

  package horstmann {
    class T2(t: T1) {}

    package impatient {
      class T3(t1: T1, t2: T2) {}
    }
  }
}

package com.horstmann.impatient{
  class T4(t1:T1,t3:T3)      //无法找到T1
}
```

<font color=red>
注：scala的REPL无法执行
</font>

<font size =4 color=Blue>
2. 编写一段让你的Scala朋友们感到困惑的代码，使用一个不在顶部的com包 
</font>

```
package com {
  class T1() {}

  package horstmann {
    class T2(t: T1) {}

    package impatient {
      class T3(t1: T1, t2: T2) {}
    }
  }
}

import com._

class TT(t1:T1){

}
```

<font size =4 color=Blue>
3. 编写一个包random,加入函数nextInt():Int、nextDouble():Double、setSeed(seed:Int):Unit。生成随机数的算法采用线性同余生成器: 
后值 = (前值 * a + b)mod 2^n 
其中,a = 1664525, b=1013904223, n = 32, 前值的初始值为seed 
</font>

```scala
package random {

  package object random {

    var seed: Int = _
    val a = BigDecimal(1664525)
    val b = BigDecimal(1013904223)
    val n = 32

    def nextInt(): Int = {
      val temp = (seed * a + b) % BigDecimal(2).pow(n)
      seed = temp.toInt
      seed
    }

    def nextDouble(): Double = {
      val temp = (seed * a + b) % BigDecimal(2).pow(n)
      seed = temp.toInt
      temp.toDouble
    }
  }

}

package test {

  import random.random

  object Test extends App {
    random.seed = 4
    println(random.nextDouble())
    println(random.nextDouble())
    println(random.nextDouble())
    println(random.nextDouble())
    println(random.nextInt())
    println(random.nextInt())
    println(random.nextInt())
    println(random.nextInt())
  }
}
```

<font size =4 color=Blue>
4. 在你看来Scala的设计者为什么要提供package object语法而不是简单的让你将函数和变量添加到包中呢？
</font>

JVM不支持。。。 

<font size =4 color=Blue>
5. private[com] def giveRaise(rate:Double)的含义是什么？有用吗？
</font>

除了com包可访问，其他包都不能访问。 

<font size =4 color=Blue>
6. 编写一段程序,将Java哈希映射中的所有元素拷贝到Scala哈希映射。用引入语句重命名这两个类。
</font>

```
import java.util.{HashMap => JavaHashMap}

import scala.collection.mutable.HashMap

object Test extends App {

  val map = new JavaHashMap[String, String]()
  map.put("1", "a")
  map.put("2", "b")
  map.put("3", "c")

  val smap = new HashMap[String, String]()

  for (key <- map.keySet().toArray) {
    smap += (key.toString -> map.get(key))
  }

  println(smap.mkString)
}
```

<font size =4 color=Blue>
7. 在前一个练习中，将所有引入语句移动到尽可能小的作用域里 
</font>

```
object Test extends App {

  import java.util.{HashMap => JavaHashMap}

  val map = new JavaHashMap[String, String]()
  map.put("1", "a")
  map.put("2", "b")
  map.put("3", "c")

  import scala.collection.mutable.HashMap

  val smap = new HashMap[String, String]()

  for (key <- map.keySet().toArray) {
    smap += (key.toString -> map.get(key))
  }

  println(smap.mkString)
}
```

<font size =4 color=Blue>
8. 以下代码的作用是什么？这是个好主意吗？ 
import java._ 
import javax._ 
</font>

导入java和javax下的所有类。而java和javax下是没有类的。所以此代码无用

<font size =4 color=Blue>
9. 编写一段程序，引入java.lang.System类，从user.name系统属性读取用户名，从Console对象读取一个密码,如果密码不是"secret"，则在标准错误流中打印一个消息；如果密码是"secret"，则在标准输出流中打印一个问候消息。不要使用任何其他引入，也不要使用任何限定词(带句点的那种) 
</font>

```
import java.lang.System

object Test extends App {
  var password = Console.readLine()

  if (password equals "secret") System.out.println("Hello " + System.getProperty("user.name"))
  else System.err.println("password error!")
}
```

<font size =4 color=Blue>
10. 除了StringBuilder,还有哪些java.lang的成员是被scala包覆盖的？
</font>

直接比对java.lang下的类和scala包下的类即可 

##第八章 继承
###8.1 基本概念

**扩展类**

```
class Employee extends Person

// 可以用final修饰类、方法或字段，表明它们不能被重写。
```

**重写方法**

重写非抽象方法必须使用override修饰符。

调用超类方法，使用super关键字。

**类型检查和转换**

```
if (p.isInstanceOf[Employee]) { // 类型检查
    val s = p.asInstanceOf[Employee]  // 类型转换
}
```

  1. 如果p为null，p.isInstanceOf[Employee]为false，且p.asInstanceOf[Employee]返回null；
  2. 如果p不是Employee，则p.asInstanceOf[Employee]将抛出异常

如果测试p指向的是一个Employee对象但又不是其子类的话，可以用

```
if (p.getClass == classOf[Employee])
```

使用模式匹配也可以

```
p match {
    case s: Employee => ... // 将s作为Employee处理
    case _ => // p不是Employee
}
```

**受保护字段和方法**

和Java一样，protected的字段与方法可以被任何子类访问，但不能从其他位置看到。

与Java不同，protected的成员对于类所属的包不可见。

protected[this]将访问权限定在当前的对象，类似private[this]。

**超类的构造**

```
class Employee(name: String, age: Int, val salary: Double) extends Person(name, age)

// 扩展Java类时，主构造器必须调用Java超类的某一个构造器
class Square(x: Int, y: Int, width: Int) extends java.awt.Rectangle(x, y, width, width)
```

**重写字段**

```
abstract class Person {
    def id: Int
}

class Student(override val id: Int) extends Person
```

<font color=red>
注：

  1. def只能重写另一个def
  2. val只能重写另一个val或不带参数的def
  3. var只能重写另一个抽象的var
</font>

**匿名子类**

```
val alien = new Person("Fred") {
    def greeting = "Hello"
}

def meet(p: Person{def greeting: String}) {
    println(p.name + "says: " + p.greeting)
}
```

**抽象类**

用abstract关键字标识。抽象方法不需要abstract标识，只需省去方法体。

子类重写超类的抽象方法时，不需要使用override关键字。

**抽象字段**

抽象字段就是没有初始值的字段。和方法一样，子类重写抽象字段，不需要override关键字。

###8.2 构造顺序和提前定义

**构造顺序**

超类的构造器调用子类的方法时，由于构造顺序，可能会产生非预期的结果。如下：

```
class Creature {
    val range: Int = 10
    val env: Array[Int] = new Array[Int](range)
}

class Ant extends Creature {
    override val range = 2
}
```

有如下解决方式：

  1. 将val生命为final
  2. 在超类中将val生命为lazy
  3. 在子类中使用提前定义语法

**提前定义**

将val字段放在位于extends之后的一个块中。

```
class Ant extends {
    override val range = 2
} with Creature
```

###8.3 Scala继承层级

基本类型和Unit类型扩展自AnyVal；所有其他类都是AnyRef（相当于Java的Object）的子类。

AnyVal和AnyRef扩展自Any类。

Any类定义了isInstanceOf、asInstanceOf、以及相等性判断和哈希码的方法。

AnyVal没有任何方法，只是一个标记。

AnyRef追加了Object类的监视方法wait和notify、notifyAll。同时提供了一个带函数参数的方法synchronized。等同于Java中的同步块。

所有Scala类都实现ScalaObject这个标记接口。

Null类型的唯一实例是null值。可以将null赋值给任何引用，但不能赋值给值类型。

Nothing类型没有实例。它对泛型结构时常有用。例如，空列表Nil的类型是List[Nothing]，它是List[T]的子类型，T可以是任何类。

![这里写图片描述](http://img.blog.csdn.net/20161125002050448)

###8.4 对象相等性

AnyRef的eq方法检查两个引用是否指向同一个对象。AnyRef的equals方法调用eq。

当自定义equals时，同时也要自定义hashCode。在计算哈希码时，只应使用那些用来做相等性判断的字段。

在应用程序中，只要使用==操作符就好，对于引用类型，它会做完必要的null检查后调用equals方法。

###8.5 习题解答

http://ivaneye.iteye.com/blog/1842712

<font size =4 color=Blue>
1. 扩展如下的BankAccount类，新类CheckingAccount对每次存款和取款都收取1美元的手续费

```
class BankAccount(initialBalance:Double) { 
    private var balance = initialBalance 
    def deposit(amount:Double) = { balance += amount; balance} 
    def withdraw(amount:Double) = {balance -= amount; balance} 
} 
```

```
class CheckingAccount(initialBalance:Double) extends BankAccount(initialBalance) {
  override def deposit(amount: Double): Double = super.deposit(amount - 1)
  override def withdraw(amount: Double): Double = super.withdraw(amount + 1)
}
```

</font>

<font size =4 color=Blue>
2. 扩展前一个练习的BankAccount类，新类SavingsAccount每个月都有利息产生(earnMonthlyInterest方法被调用)，并且有每月三次免手续费的存款或取款。在earnMonthlyInterest方法中重置交易计数。
</font>

```
class SavingsAccount(initialBalance:Double) extends BankAccount(initialBalance){
  private var num:Int = _

  def earnMonthlyInterest()={
    num = 3
    super.deposit(1)
  }

  override def deposit(amount: Double): Double = {
    num -= 1
    if(num < 0) super.deposit(amount - 1) else super.deposit(amount)
  }

  override def withdraw(amount: Double): Double = {
    num -= 1
    if (num < 0) super.withdraw(amount + 1) else super.withdraw(amount)
  }
}
```

<font size =4 color=Blue>
3. 翻开你喜欢的Java或C++教科书，一定会找到用来讲解继承层级的实例，可能是员工，宠物，图形或类似的东西。用Scala来实现这个示例。
</font>

java代码

```java
class Art{
    Art(){System.out.println("Art constructor");}
}

class Drawing extends Art{
    Drawing() {System.out.println("Drawing constructor");}
}

public class Cartoon extends Drawing{
    public Cartoon() { System.out.println("Cartoon constructor");}
}
```

scala代码

```
class Art{
  println("Art constructor")
}

class Drawing extends Art{
  println("Drawing constructor")
}

class Cartoon extends Drawing{
  println("Cartoon constructor")
}
```

<font size =4 color=Blue>
4. 定义一个抽象类Item,加入方法price和description。SimpleItem是一个在构造器中给出价格和描述的物件。利用val可以重写def这个事实。Bundle是一个可以包含其他物件的物件。其价格是打包中所有物件的价格之和。同时提供一个将物件添加到打包当中的机制，以及一个适合的description方法
</font>

```
import collection.mutable.ArrayBuffer

abstract class Item{
  def price():Double
  def description():String

  override def toString():String={
    "description:" + description() + "  price:" + price()
  }
}

class SimpleItem(val price:Double,val description:String) extends Item{

}

class Bundle extends Item{

  val items = new ArrayBuffer[Item]()

  def addItem(item:Item){
    items += item
  }

  def price(): Double = {
    var total = 0d
    items.foreach(total += _.price())
    total
  }

  def description(): String = {
    items.mkString(" ")
  }
}
```

<font size =4 color=Blue>
5. 设计一个Point类，其x和y坐标可以通过构造器提供。提供一个子类LabeledPoint，其构造器接受一个标签值和x,y坐标,比如:new LabeledPoint("Black Thursday",1929,230.07) 
</font>

```
class Point(val x : Int, val y : Int) {
}

class LabeledPoint(val label : String, x : Int, y : Int) extends Point(x, y) {
}
```

<font size =4 color=Blue>
6. 定义一个抽象类Shape，一个抽象方法centerPoint，以及该抽象类的子类Rectangle和Circle。为子类提供合适的构造器，并重写centerPoint方法
</font>

```
abstract class Shape{
  def centerPoint()
}

class Rectangle(startX:Int,startY:Int,endX:Int,endY:Int) extends Shape{
  def centerPoint() {}
}

class Circle(x:Int,y:Int,radius:Double) extends Shape{
  def centerPoint() {}
}
```

<font size =4 color=Blue>
7. 提供一个Square类，扩展自java.awt.Rectangle并且是三个构造器：一个以给定的端点和宽度构造正方形，一个以(0,0)为端点和给定的宽度构造正方形，一个以(0,0)为端点,0为宽度构造正方形 
</font>

```
import java.awt.{Point, Rectangle}


class Square(point:Point, width:Int) extends Rectangle(point.x,point.y,width,width){

  def this(){
    this(new Point(0,0),0)
  }

  def this(width:Int){
    this(new Point(0,0),width)
  }
}
```

<font size =4 color=Blue>
8. 编译8.6节中的Person和SecretAgent类并使用javap分析类文件。总共有多少name的getter方法？它们分别取什么值？(提示：可以使用-c和-private选项) 
</font>

总共两个。Person中取得的是传入的name,而SecretAgent中取得的是默认的"secret" 

<font size =4 color=Blue>
9. 在8.10节的Creature类中，将val range替换成一个def。如果你在Ant子类中也用def的话会有什么效果？如果在子类中使用val又会有什么效果？为什么？
</font>

在Ant中使用def没有问题。但是如果使用val则无法编译。因为val只能重写不带参数的def。这里的def是带参数的

<font size =4 color=Blue>
10. 文件scala/collection/immutable/Stack.scala包含如下定义: 
class Stack[A] protected (protected val elems: List[A]) 
请解释protected关键字的含义。(提示：回顾我们在第5章中关于私有构造器的讨论) 
</font>
此构造方法只能被其子类来调用,而不能被外界直接调用
