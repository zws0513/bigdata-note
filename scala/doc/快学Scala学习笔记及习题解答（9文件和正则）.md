本文scala使用的版本是2.11.7

##第九章 文件和正则表达式
###9.1 文件

**读取行**

```
import scala.io.Source

val source = Source.fromFile("myfile.txt", "UTF-8)

val lineIterator = source.getLines
for (l <- lineIterator) println(l)

// 也可以对迭代器应用toArray或toBuffer方法，将这些行放到数组或数组缓冲当中。
val lines = source.getLines.toArray

// 把整个文件读取成一个字符串
val contents = source.mkString
```

<font color=red>
用法Source对象后，记得要close
</font>

**读取字符**

```
for (c <- source) println(c)
```

如果只想查看某个字符但又不处理掉它，调用source对象的buffered方法。

```
val source = Source.fromFile("mfile.txt", "UTF-8")
val iter = source.buffered

while (iter.hasNext) {
    if (iter.head是符合预期的)
        处理 iter.next
    else ...
}
source.close
```

**读取词法单元和数字**

```
val tokens = source.mkString.split("\\s+")

// 可以用toInt或toDouble转换字符串为数字
val numbers = for (w <- tokens) yield w.toDouble
或者
val numbers = tokens.map(_.toDouble)

// 从控制台读取数字（如果输入不为数字，会抛出NumberFormatException）
val age = readInt()
// 或者使用readDouble或readLong
```

**从URL或其他源读取**

```
val source1 = Source.fromURL("https://baidu.com", "UTF-8")
// 从给定的字符串读取
val source2 = Source.fromString("Hello, World!")
// 从标准输入读取
val source3 = Source.stdin
```

**读取二进制文件**

Scala没有提供读取二进制文件的方法，需要用Java类库。

```
val file = new File(fileName)
val in = new FileInputStream(file)
val bytes = new Array[Byte](file.length.toInt)
in.read(bytes)
in.close()
```

**写入文本文件**

Scala没有内建的对写入文件的支持。可使用java.io.PrintWriter

```
val out = new PrintWriter("numbers.txt")
for (i <- 1 to 100) out.println(i)
out.close()
```

**访问目录**

Scala没有“正式的”用来访问某个目录中的所有文件，或递归遍历所有目录的类。

```
import java.io.File

def subdirs(dir: File): Iterator[File] = {
    val children = dir.listFiles.filter(_.isDirectory)
    children.toIterator ++ children.toIterator.flatMap(subdirs _)
}
```

或者

```
import java.nio.file._

implicit def makeFileVisitor(f: (Path) => Unit) = new SimpleFileVisitor[Path] {
    override def visitFile(p: Path, attrs: attribute.BasicFileAttributes) = {
        f(p)
        FileVisitResult.CONTINUE
    }
}

// 通过以下调用打印所有的子目录
Files.walkFileTree(dir.toPath, (f: Path) => println(f))
```

###9.2 序列化

```
@SerialVersionUID(42L) class Persion extends Serializable
```

###9.3 进程控制

scala.sys.process包提供了用于与shell程序交互的工具。

```
import sys.process._

"ls -al .." !  // !操作符返回的结果是被执行程序的返回值。成功为0，错误为非0值

// 使用!!，输出会以字符串的形式返回
val result = "ls -al .." !!

// 用#|操作符实现管道的功能
"ls -al .." #| "grep sec" !

// #>操作符表示重定向到文件，追加到末尾使用#>>
"ls -al .." #> new File("output.txt")

// 使用#<把某个文件内容作为输入
"grep sec" #< new File("output.txt")

// p #&& q（如果p成功，则执行q）；p #|| q（如果p不成功，则执行q）

// 设置环境变量，用Process对象的apply方法构造ProcessBuilder
val p = Process(cmd, new File(dirName), ("LANG", "en_US"))
"echo 42" #| p !
```

###9.4 正则表达式

scala.util.matching.Regex类。要构造Regex对象，用String类的r方法即可：

```
val numPattern = "[0-9]+".r
```

如果正则表达式包含反斜杠或引号的话，最好用“原始”字符串语法 """..."""

```
val wsnumwsPattern = """\s+[0-9]+\s+""".r
```

findAllIn方法返回遍历所有匹配项的迭代器。可以在for循环中使用。

```
for (matchString <- numPattern.findAllIn("99 bottles, 98 bottles"))

或者
val matchs = numPattern.findAllIn("99 bottles, 98 bottles").toArray
// Array(99, 98)
```

要找到收个匹配项，可使用findFirstIn，得到的结果是Option[String]。

要检查是否某个字符串的开始部分能匹配，可用findPrefixOf。

可以替换首个匹配项（repalceFirstIn），或替换所有（replaceAllIn）。

```
numPattern.repalceFirstIn("99 bottles, 98 bottles")
numPattern.repalceAllIn("99 bottles, 98 bottles")
```

###9.5 正则表达式组

分组可以方便地获取正则表达式的子表达式。在想要提取的子表达式两侧加上圆括号。

```
val numitemPattern = "([0-9]+) ([a-z]+)".r

// 要匹配组，可以把正则表达式对象当做“提取器”使用
val numitemPattern(num, item) = "99 bottles"
// 将num设为"99"，item设为“bottles"

// 多个匹配项中提取分组
for (numitemPattern(num, item) <- numitemPattern.findAllIn("99 bottles, 98 bottles")) 处理num和item
```

###9.6 习题解答

http://ivaneye.iteye.com/blog/1843331

<font size =4 color=Blue>
1. 编写一小段Scala代码，将某个文件中的行倒转顺序(将最后一行作为第一行,依此类推) 
</font>

```
import io.Source
import java.io.PrintWriter

val path = "test.iml"
val reader = Source.fromFile(path).getLines()
val result = reader.toArray.reverse
val pw = new PrintWriter("test.rev")
result.foreach(line => pw.write(line + "\n"))
pw.close()
```

<font size =4 color=Blue>
2. 编写Scala程序,从一个带有制表符的文件读取内容,将每个制表符替换成一组空格,使得制表符隔开的n列仍然保持纵向对齐,并将结果写入同一个文件 
</font>

```
import io.Source
import java.io.PrintWriter

val path = "demo"
val reader = Source.fromFile(path).getLines()
val result = for ( t <- reader) yield t.replaceAll("\\t","    ")  
val pw = new PrintWriter("demo.rev")
result.foreach(line => pw.write(line + "\n"))
pw.close()
```

<font size =4 color=Blue>
3. 编写一小段Scala代码,从一个文件读取内容并把所有字符数大于12的单词打印到控制台。如果你能用单行代码完成会有额外奖励 
</font>

```
import io.Source  
  
Source.fromFile("demo").mkString.split("\\s+").foreach(arg => if(arg.length > 12) println(arg)) 
```

<font size =4 color=Blue>
4. 编写Scala程序，从包含浮点数的文本文件读取内容，打印出文件中所有浮点数之和，平均值，最大值和最小值 
</font>

```
import io.Source

val nums = Source.fromFile("demo").mkString.split("\\s+")

var total = 0d
nums.foreach(total += _.toDouble)

println(total)
println(total/nums.length)
println(nums.max)
println(nums.min)
```

<font size =4 color=Blue>
5. 编写Scala程序，向文件中写入2的n次方及其倒数，指数n从0到20。对齐各列: 
  1         1 
  2         0.5 
  4         0.25 
...         ... 
</font>

```
import java.io.PrintWriter

val pw = new PrintWriter("test.txt")

for (n <- 0 to 20){
  val t = BigDecimal(2).pow(n)
  pw.write(t.toString())
  pw.write("\t\t")
  pw.write((1/t).toString())
  pw.write("\n")
}

pw.close()
```

<font size =4 color=Blue>
6. 编写正则表达式,匹配Java或C++程序代码中类似"like this,maybe with \" or\\"这样的带引号的字符串。编写Scala程序将某个源文件中所有类似的字符串打印出来 
</font>

```
import io.Source

val source = Source.fromFile("test.txt").mkString
val pattern = "\\w+\\s+\"".r
pattern.findAllIn(source).foreach(println)
```

<font size =4 color=Blue>
7. 编写Scala程序，从文本文件读取内容，并打印出所有的非浮点数的词法单位。要求使用正则表达式 
</font>

```
import io.Source

val source = Source.fromFile("test.txt").mkString
val pattern = """[^((\d+\.){0,1}\d+)^\s+]+""".r
pattern.findAllIn(source).foreach(println)
```

<font size =4 color=Blue>
8. 编写Scala程序打印出某个网页中所有img标签的src属性。使用正则表达式和分组 
</font>

```
import io.Source

val source = Source.fromFile("D:\\ProgramCodes\\ScalaTest\\src\\test.txt").mkString
val pattern = """<img[^>]+(src\s*=\s*"[^>^"]+")[^>]*>""".r

for (pattern(str) <- pattern.findAllIn(source)) println(str)
```

<font size =4 color=Blue>
9. 编写Scala程序，盘点给定目录及其子目录中总共有多少以.class为扩展名的文件 
</font>

```
import java.io.File

val path = "."
val dir = new File(path)

def subdirs(dir:File):Iterator[File]={
  val children = dir.listFiles().filter(_.getName.endsWith("class"))
  children.toIterator ++ dir.listFiles().filter(_.isDirectory).toIterator.flatMap(subdirs _)
}

val n = subdirs(dir).length

println(n)
```

<font size =4 color=Blue>
10. 扩展那个可序列化的Person类，让它能以一个集合保存某个人的朋友信息。构造出一些Person对象，让他们中的一些人成为朋友，然后将Array[Person]保存到文件。将这个数组从文件中重新读出来，校验朋友关系是否完好 
</font>

注意,请在main中执行。脚本执行无法序列化。

```
package com.z

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import scala.collection.mutable.ArrayBuffer

class Person(var name: String) extends Serializable {

  val friends = new ArrayBuffer[Person]()

  def addFriend(friend: Person) {
    friends += friend
  }

  override def toString() = {
    var str = "My name is " + name + " and my friends name is "
    friends.foreach(str += _.name + ",")
    str
  }
}

object Test extends App {
  val p1 = new Person("Ivan")
  val p2 = new Person("F2")
  val p3 = new Person("F3")

  p1.addFriend(p2)
  p1.addFriend(p3)
  println(p1)

  val out = new ObjectOutputStream(new FileOutputStream("test.txt"))
  out.writeObject(p1)
  out.close()

  val in = new ObjectInputStream(new FileInputStream("test.txt"))
  val p = in.readObject().asInstanceOf[Person]
  println(p)
}
```
