本文Scala使用的版本是2.11.8

##第19章 解析

###19.1 文法

所谓文法（grammar）指的是一组用于产出所有遵循某个特定结构的字符串的规则。

文法通常以一种被称为巴科斯范式（BNF）的表示法编写：

```
op ::= "+" | "-" | "*"
expr ::= number | expr op expr | "(" expr ")"
```

这里的number并没有定义，可以像这样来定义它：

```
digit ::= "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
number ::= digit | digit number
```

实际中，更高效的做法是在解析开始之前就收集好数字，这个步骤叫词法分析（lexical analysis）。词法分析器（lexer）会丢弃空白和注释并形成词法单元（token）——标识符、数字或符号。

op和expr是结构化的元素，是文法的作者创造出来的，目的是产出正确的词法单元序列。被称为非终结符号。其中位于层级顶端的非终结符号也被称为起始符号。要产出正确格式的字符串，应该从起始符号开始，持续应用文法规则，知道所有的非终结符号都被替换掉，只剩下词法单元。例如：

<pre><code>
<b>expr</b> -> <b>expr</b> op expr -> number <b>op</b> expr -> number "+" <b>expr</b> ->
number "+" number
</code></pre>

最常用的是“扩展巴科斯范式（EBNF）”，允许给出可选元素和重复。使用正则操作符`?*+`来分别表示0个或1个、0个或多个、1个或更多。

```
// 支持操作符优先级的算术表达式的文法
expr ::= term( ( "+" | "-" ) expr )?
term ::= factor ( "*" factor )*
factor ::= number | "(" expr ")"
```

###19.2 组合解析器操作

示例扩展自Parsers特质的类并定义那些由基本操作组合起来的解析操作，基本操作包括：

- 匹配一个词法单元
- 在两个操作之间做选择（|)
- 依次执行两个操作（~）
- 重复一个操作（rep）
- 可选择地执行一个操作（opt）

```
class ExprParser extends RegexParsers {
    val number = "[0-9]".r
    
    def expr: Parser[Any] = term ~ opt(("+" | "-") ~ expr)
    def term: Parser[Any] = factor ~ rep("*" ~ factor)
    def factor: Parser[Any] = number | "(" ~ expr ~ ")"
}

// 运行
val parser = new ExprParser
val result = parser.parseAll(parser.expr, "3-4*5")
if (result.successful) println(result.get)

// 结果
((3~List())~Some((-~((4~List((*~5)))~None))))

// 结果解读
// 1. 字符串字面量和正则表达式返回String值。
// 2. p ~ q返回~样例类的一个实例，这个样例类和对偶很相似
// 3. opt(p)返回一个Option，要么是Some(...)，要么是None
// 4. rep(p)返回一个List

// 根据term的定义，它返回的结果是一个factor加上一个List。
// 这是个空表达式，因为在-的左边的子表达式中没有*
```

###19.3 解析器结果变换

```
// ^^ 操作符将函数{ _.toInt }应用到number对应的解析结果上
def factor: Parser[Int] = number ^^ { _.toInt } | "(" ~ expr ~ ")" ^^ { case _ ~ e ~ _ => e }

def expr: Parser[Int] = term ~ opt(("+" | "-") ~ expr) ^^ {
    case t ~ None => t
    case t ~ Some("+" ~ e) => t + e
    case t ~ Some("-" ~ e) => t - e
}

def term: Parser[Int] = factor ~ rep("*" ~ factor) ^^ {
    case f ~ r => f * r.map(_._2).product
}

// 运行结果
-17
```

###19.4 丢弃词法单元

`~>`和`<~`操作符可以用来匹配并丢弃词法单元。例如`"*" ~> factor`只是factor的计算结果，而不是`"*" ~ f`的值。

```
// term函数简化
def term = factor ~ rep("*" ~> factor) ^^ {
    case f ~ r => f * r.product
}

// 可以丢弃某个表达式外围的圆括号（箭头指向被保留下来的部分）
def factor = number ^^ { _.toInt } | "(" ~> expr <~ ")"
```

<font color=red>
注：在同一个表达式中使用多个~、~>和<~时，如果必要，使用圆括号说明优先级
</font>

###19.5 生成解析树

通常使用样例类来实现。如下的类可以表示一个算术表达式：

```
class Expr
case class Number(value: Int) extends Expr
case class Operator(op: String, left: Expr, right: Expr) extends Expr

// 3 + 4 * 5将变换为
Operator("+", Number(3), Operator("*", Number(4), Number(5)))
```

在解释器中，这样的表达式可以被求值。在编译器中，它可以被用来生成代码。

要生成解析树，需要用`^^`操作符，给出产生树节点的函数，例如：

```
class ExprParser extends RegexParsers {
    val number = "[0-9]".r

    def expr: Parser[Expr] = term ~ opt(("+" | "-") ~ expr) ^^ {
        case t ~ None => t
        case t ~ Some("+" ~ e) => Operator("+", t, e)
        case t ~ Some("-" ~ e) => Operator("-", t, e)
    }
    def term: Parser[Expr] = (factor ~ opt("*" ~> term)) ^^ {
        case a ~ None => a
        case a ~ Some(b) => Operator("*", a, b)
    }
    def factor: Parser[Expr] = number ^^ (n => Number(n.toInt)) | "(" ~> expr <~ ")"
}
```

###19.6 避免左递归

如果解析器函数在解析输入之前就调用自己的话，就会一直递归下去。例如：

```
// 本意是解析由1组成的任意长度的序列
def ones: Parser[Any] = "1" ~ ones
```

这样的函数称之为左递归的。

以上面的算术表达式解析器为例：

```
def expr: Parser[Expr] = term ~ opt(("+" | "-") ~ expr)
```

解析`3 - 4 - 5`的结果为`3 - (4 - 5)`，如果把文法颠倒过来，就可以得到正确的解析树，但会产生左递归。

所以需要收集中间结果，然后按照正确的顺序组合起来：

```
def expr: Parser[Int] = term ~ rep(
    
    )
```

###19.7 更多的组合子

**用于表示重复的组合子**

repsep组合子匹配领个或多个重复项

```
// 一个以逗号分隔的数字列表可以被定义为
def numberList = number ~ rep("," ~> number)

// 或者更精简的版本
def numberList = repsep(number, ",")
```

表示重复项的组合子 | 描述 | 说明
--------- | -------- | --------
rep(p) | 0个或更多p的匹配项 | 
rep1(p) | 1个或多个p的匹配项 | rep1("[" ~> expr <~ "]")将产出一个被包在方括号内的表达式的列表——比如可以用来给出多维数组的界限
rep1(p, q) <br>其中p和q的类型为Parser[P]| 一个p的匹配项加上0个或更多q的匹配项
repN(n, p) | n个p的匹配项 | repN(4, number)将匹配一个由四个数字组成的序列，比如可以用来给出一个长方形
repsep(p, s) <br>rep1sep(p, s) <br>其中p的类型为Parser[P] | 0个或更多、1个或多个p的匹配项，以s的匹配项分隔开。结果是一个List[P]；s会被丢弃掉 | repsep(expr, ",")将产出一个由逗号分隔开的表达式的列表。对于解析函数调用中传入函数的参数列表很有用
chain1(p, s) | 和rep1sep类似，不过s必须在匹配到每个分隔符时产出一个二元函数用来组合相邻的两个值。如果p产出值v0、v1、v2、...，而s产出函数f1、f2，...，结果就是(v0f1v1)f2v2... | chain1(number ^^ { _.toInt }，"*" ^^^ { _ * _ })将会计算一个以\*分隔开的整数序列的乘积


**其他组合子**

into组合子可以存储先前组合子的信息到变量中供之后的组合子使用

```
def term: Parser[Any] = factor ~ rep("*" ~> factor)

// 将第一个因子存入变量
def term: Parser[Int] = factor into { first =>
    rep("*" ~> factor) ^^ { first * _.product }
}
```

log组合子可以帮助调试文法

```
// 将解析器p替换为log(p)(str)，将在每次p被调用时得到一个日志输出
def factor: Parser[Int] = log(number)("number") ^^ { _.toInt } | ...
```

其他组合子 | 描述 | 说明
--------- | -------- | --------
p ^^^ v | 类似^^，不过返回一个恒定的结果 | 对于解析字面量很有用："true" ^^^ true
p into f或p >> f | f是一个以p的计算结果作为参数的函数。可用于将p的计算结果绑定到变量 | (number ^^ { \_.toInt }) >> { n => repN(n, number) }将解析一个数字的序列，其中第一个数字表示接下来还有多少个数字要一起解析处理
p ^? f <br> p ^? (f, error) | 类似^^，不过接受一个偏函数f作为参数。如果f不能被应用到p的结果时解析会失败。在第二个版本中，error是一个以p的结果为参数的函数，产出错误提示字符串 | ident ^? (symbols, "undefined symbol" +\_)将会在symbols映射中查找ident，如果映射中没有则报告错误。注意映射可以被转换成偏函数
log(p)(str) | 执行p并打印出日志消息 | log(number)("number") ^^ { \_.toInt }将会在每次解析到数字时打印一个消息
guard(p) | 调用p，可能成功，也可能失败，然后将输入恢复，就像p没有被调用过一样 | 对于向前看很有用。举例来说，为了区分变量和函数调用，你可以用guard(ident ~ "(")
not(p) | 调用p，如果p失败则成功，如果p成功则失败 | 
p ~! q | 类似~，不过如果第二个匹配失败，则失败会变成一个错误，将阻止当前表达式外围带\|的表达式的回溯解析 | 
accept(descr, f) | 接受被偏函数f接受的项，返回函数调用的结果。字符串descr用来在失败消息中描述预期的项 | accept("string literal", { case t: lexical.StringLit => t.chars })
success(v) | 对于值v总是成功 | 可用于添加值v到结果当中
failure(msg)<br>err(msg) | 以给定的错误提示失败 | 
phrase(p) | 如果p成功则成功，不留下已经解析过的输入 | 对于定义parseAll方法很有用
positioned(p) | 为p的结果添加位置信息（p的结果必须扩展自Positional） | 对于在解析完成后报告错误很有用

###19.8 避免回溯

每当二选一的`p | q`被解析而p失败时，解析器会用同样的输入尝试q。这样的回溯（backtracking）很低效。

通常可以通过重新整理文法规则来避免回溯，例如：

```
def expr: Parser[Any] = term ~ ("+" | "-") ~ expr | term
def term: Parser[Any] = factor ~ "*" ~ term | factor
def factor: Parser[Any] = "(" ~ expr ~ ")" | number

// 如果表达式(3+4)*5被解析，term将匹配整个输入。接下来“+”或“-”的匹配会失败，解析器回溯到第二个选项，再一次解析term。

// 重新整理文法规则
def expr: Parser[Any] = term ~ opt(("+" | "-") ~ expr)
def term: Parser[Any] = factory ~ rep("*" ~ factor)
```

可以用~!操作符而不是~来表示不需要回溯

```
def expr: Parser[Any] = term ~! opt(("+" | "-") ~! expr)
def term: Parser[Any] = factor ~! rep("*" ~! factor)
def factor: Parser[Any] = "(" ~! expr ~! ")" | number
```

###19.9 记忆式解析器

该解析器的算法会捕获到之前的解析结果。这样有两个好处：

- 解析时间可以确保与输入长度成比例关系。
- 解析器可以接受左递归的语法。

要在Scala中使用记忆式解析，需要：

1. 将PackratParsers特质混入解析器；
2. 使用val或lazy val而不是def来定义每个解析函数。这很重要，因为解析器会缓存这些值，且解析器有赖于他们始终是同一个这个事实。（def每次被调用会返回不同的值。）
3. 让每个解析方法返回PackratParser[T]而不是Parser[T]。
4. 使用PackratParser并提供parseAll方法（PackratParsers特质并不包含这个方法）。

```
class OnesPackratParser extends RegexParsers with PackratParsers {
    lazy val ones: PackratParser[Any] = ones ~ "1" | "1"
    
    def parseAll[T](p: Parser[T], input: String) = 
        phrase(p)(new PackratReader(new CharSequenceReader(input)))
}
```

###19.10 解析器到底是什么

从技术上讲，Parser[T]是一个带有单个参数的函数，参数类型为Reader[Elem]，而返回值的类型为ParseResult[T]。

类型Elem是Parsers特质的一个抽象类型。RegexParsers特质将Elem定义为Char，而StdTokenParsers特质将Elem定义为Token。

Reader[Elem]从某个输入源读取一个Elem值（即字符或词法单元）的序列，并跟踪他们的位置，用于报告错误。

当把读取器作为参数去调用Parser[T]时，它将返回ParseResult[T]的三个子类当中的一个的对象：

- Error：将终止解析器以及任何调用该解析器的代码。它可能在如下情况中发生：
	- 解析器`p ~! q`未能成功匹配q
	- commit(p)失败
	- 遇到了err(msg)组合子

- Failure只不过意味着某个解析器匹配失败；通常情况下它将会触发其外围带\|的表达式中的其他选项
- Success[T]最重要的是带有一个类型为T的result。它同时还带有一个名为next的Reader[Elem]，包含了匹配到的内容之外其他将被解析的输入。

示例：

```
val number = "[0-9]+".r
def expr = number | "(" ~ expr ~ ")"

// 解析器扩展自RegexParsers，该特质有一个从Regex到Parser[String]的隐式转换。
// 正则表达式number被转换成这个一个解析器——以Reader[Char]为参数的函数。
// 如果读取器中最开始的字符与正则表达式相匹配，解析器函数将返回Success[String]
// 返回对象中result属性是已匹配的输入，而next属性则为移除了匹配项的读取器。
// 如果读取器中最开始的字符与正则表达式不匹配，解析器函数将返回Failure对象。
```

###19.11 正则解析器

RegexParsers特质提供了两个用于定义解析器的隐式转换：

- literal从一个字符串字面量（比如"+"）做出一个Parser[String]。
- regex从一个正则表达式（比如"[0-9]".r）做出一个Parser[String]。

默认会跳过空白。如果不想跳过空白，则可以用：

```
override val whiteSpace = "".r
```

JavaTokenParsers特质扩展自RegexParsers并给出了五个词法单元的定义（因为没有与Java中的写法完全对应，因此这个特质的适用范围是有限的）：

词法单元 | 正则表达式
------ | -------
ident | [a-zA-Z_]\w*
wholeNumber | -?\d+
decimalNumber | (\d+(\\.\d\*)?\|\d\*\\.\d+)
stringLiteral | "([^"\p{Cntrl}\\\\]\|\\\\[\\\\/bfnrt]\|\\\\u[a-fA-F0-9]{4})\*"
floatingPointNumber | -?(\d+(\\.\d\*)?\|\d\*\\.\d+)([eE][+-]?\d+)?[fFdD]?

###19.12 基于词法单元的解析器

基于词法单元的解析器使用Reader[Token]而不是Reader[Char]。Token类型定义在特质scala.util.parsing.combinator.token.Tokens特质中。StdTokens子特质定义了四种在解析编程语言时经常会遇到的词法单元：

- Identifier（标识符）
- Keyword（关键字）
- NumericLit（数值字面量）
- StringLit（字符串字面量）

StandardTokenParsers类提供了一个产出这些词法单元的解析器。标识符由字母、数字或_组成，但不以数字开头。

数值字面量是一个数字的序列。字符串字面量被包括在"..."或'...'中，不带转义符。被包含在`/*...*/`中或者从//开始直到行尾的注释被当做空白处理。

当扩展该解析器时，可将任何需要用到的保留字和特殊词法单元分别添加到lexical.reserved和lexical.delimiters集中：

```
class MyLanguageParser extends StandardTokenParser {
    lexical.reserved += ("auto", "break", ...)
    lexical.delimiters += ("=", "<", ...)
    ...
}
```

当解析器遇到保留字时，该保留字将成为Keyword而不是Identifier。

解析器根据“最大化匹配”原则拣出定界符（delimiter）。举例来说，如果输入包含<=，你将会得到单个词法单元，而不是一个<加上=的序列。

ident函数解析标识符；而numericLit和stringLit解析字面量。

```
// 以下是使用StandardTokenParsers实现的算法表达式文法
class ExprParser extends StandardTokenParsers {
    lexical.delimiters += ("+", "-", "*", "(", ")")
    
    def expr: Parser[Any] = term ~ rep(("+" | "-") ~ term
    def term: Parser[Any] = factor ~ rep("*" ~> factor)
    def factor: Parser[Any] = numericLit | "(" ~> expr <~ ")"
    
    def parseAll[T](p: Parser[T], in: String): ParseResult[T] = 
        phrase(p)(new lexical.Scanner(in))
}

// 注意需要提供parseAll方法，这个方法在StandardTokenParsers特质中并未定义。
// 在该方法中，用的是lexical.Scanner，这个StdLexical特质提供的Reader[Token]。
```

###19.13 错误处理

如果有多个失败点，最后访问到的那个将被报告。解决方案是添加一个failure语句，显示地给出错误提示：

```
def value: Parser[Any] = numericLit | "true" | "false" | failure("Not a valid value")
```

如果解析器失败了，parseAll方法将返回Failure结果。它的msg属性是一个错误提示，让你显示给用户。而next属性是指向失败发生时还未解析的输入的Reader。要显示行号和列。可以通过next.pos.line和next.pos.column得到。最后，next.first是失败发生时被处理的词法元素。

###19.14 习题解答

<font size =4 color=Blue>
1. 为算术表达式求值器添加/和%操作符。
</font>


<font size =4 color=Blue>
2. 为算术表达式求值器添加^操作符。在数学运算当中，^应该比乘法的优先级更高，并且它应该是右结合的。也就是说，4^2^3应该得到4^(2^3)，即65536。
</font>


<font size =4 color=Blue>
3. 编写一个解析器，将整数的列表（比如(1, 23, -79)）解析为List[Int]。
</font>

<font size =4 color=Blue>
4. 编写一个能够解析ISO 8601中的日期和时间表达式的解析器。你的解析器应返回一个java.util.Date对象。
</font>


<font size =4 color=Blue>
5. 编写一个解析XML子集的解析器。要求能够处理如下形式的标签：`<ident> ... </ident>`或`<ident/>`。标签可以嵌套。处理标签中的属性。属性值可以以单引号或双引号定界。你不需要处理字符数据（即位于标签中的文本或CDATA段）。你的解析器应该返回一个Scala XML的Elem值。难点是要拒绝不匹配的标签。提示：into、accept
</font>



<font size =4 color=Blue>
6. 假定19.5节中的那个解析器用如下代码补充完整：

```
class ExprParser extends RegexParsers {
    def expr: Parser[Expr] = (term ~ opt(("+" | "-") ~ expr)) ^^ {
        case a ~ None => a
        case a ~ Some(op ~ b) => Operator(op, a, b)
    }
    ...
}
```

可惜这个解析器计算出来的表达式树是错误的——同样优先级的操作符按照从右到左的顺序求值。修改该解析器，使它计算出正确的表达式树。举例来说，`3-4-5`应得到`Operator("-", 3, 4), 5)`
</font>


<font size =4 color=Blue>
7. 假定在19.6节中，我们首先将expr解析成一个带有操作的值得~列表：

```
def expr: Parser[Int] = term ~ rep(("+" | "-") ~ term ^^ { ... }
```

要得到结果，我们需要计算

![这里写图片描述](http://img.blog.csdn.net/20170213154349796?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMzk4MDEyNw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

用折叠（参见第13章）实现这个运算。
</font>



<font size =4 color=Blue>
8. 给计算器程序添加变量和赋值操作。变量在首次使用时被创建。未初始化的变量为零。打印某值的方法是将它赋值给一个特殊的变量out。
</font>



<font size =4 color=Blue>
9. 扩展前一个练习，让它变成一个编程语言的解析器，支持变量赋值、Boolean表达式，以及if/else和while语句。
</font>



<font size =4 color=Blue>
10. 为前一个练习中的编程语言添加函数定义。
</font>



##第20章 Actor

###20.1 创建和启动Actor

```
import scala.actors.Actor

case class Charge(creditCardNumber: Long, merchant: String, amount: Double)

// actor是扩展自Actor特质的类。该特质带有一个抽象方法act。可以重写这个方法来指定该actor的行为
class HiActor extends Actor {
    override def act(): Unit = {
        while (true) {
            receive {
                case "Hi" => println("Hello")
                case Charge(ccnum, merchant, amount) => println(ccnum + merchant + amount)
            }
        }
    }
}

// 启动
object C20_1 {

    def main(args: Array[String]) {
        val actor1 = new HiActor
        actor1.start()
    }
}

// 需要临时创建actor而不是定义一个类时。Actor伴生对象带有一个actor方法来创建和启动actor
val actor2 = actor {
    while (true) {
        receive {
            case "Hello" => println("world")
            case Charge(ccnum, merchant, amount) => println(ccnum + merchant + amount)
        }
    }
}
```

###20.2 发送消息

可以用为actor定义的!操作符：

```
actor1 ! "Hi"

actor1 ! Charge(411111111, "样例类测试", 19.95)
```

###20.3 接收消息

发送到actor的消息被存放在一个“邮箱”中。receive方法从邮箱获取下一条消息并将它传递给它的参数，该参数是一个偏函数。

```
receive {
    // receive方法的参数
    case Deposit(amount) => ...
    case Withdraw(amount) => ...
    // 为了防止邮箱被那些不与任何case语句匹配的消息占满，添加这个分支。
    case _ => ...
}
```

这个代码块被转换成一个类型为PartialFunction[Any, T]的对象，其中T是case语句=>操作符右边的表达式的计算结果的类型。

如果邮箱中没有任何消息可以被偏函数处理，则对receive方法的调用也会阻塞，知道一个可以匹配的消息抵达。

邮箱会串行化消息。actor运行在单个线程中。

###20.4 向其他Actor发送消息

不同actor并发时，可以把结果存入一个线程安全的数据结构中，比如一个并发的哈希映射；或者把结果向另一个actor发送消息。

这里有几个设计选择：

1. 可以有一些全局的actor。缺点是数量很多时，伸缩性不好。
2. actor可以构造成带有指向一个或更多actor的引用。
3. actor可以接收带有指向另一个actor的引用的消息。

  ```
  // continuation是另一个actor
  actor ! Compute(data, continuation)
  ```
  
4. actor可以返回消息给发送方。receive方法会把sender字段设为当前消息的发送方。

###20.5 消息通道

除了共享引用的做法，还可以共享消息通道给它们。这样有两个好处：

1. 消息通道是类型安全的——只能发送或接受某个特定类型的消息。
2. 不会不小心通过消息通道调用到某个actor的方法。

消息通道可以是一个OutputChannel（带有!方法），也可以是一个InputChannel（带有receive货react方法）。Channel类同时扩展OutputChannel和InputChannel特质。

要构造一个消息通道，可以提供一个actor，如果不提供，则会绑定到当前执行的这个actor上。

```
val channel = new Channel[Int][someActor]
```

详细示例

```
import scala.actors.{!, Channel, Actor, OutputChannel}
import scala.actors.Actor._

case class Compute(input: Seq[Int], result: OutputChannel[Int])

class Computer extends Actor {
    override def act(): Unit = {
        while (true) {
            receive {
                case Compute(input, out) => { val answer = 3; out ! answer }
            }
        }
    }
}

/**
  * Created by zhangws on 17/2/13.
  */
object C20_5 {

    def main(args: Array[String]) {

        val computeActor: Computer = new Computer
        computeActor.start()

        val channel = new Channel[Int]
        val input: Seq[Int] = Seq(1, 2, 3)

        computeActor ! Compute(input, channel)

        // 匹配channel的receive
        channel.receive {
            case x => println(x)
        }

        actor {
            val computeActor2: Computer = new Computer
            computeActor2.start()

            val channel2 = new Channel[Int]
            val input2: Seq[Int] = Seq(1, 2, 3)

            computeActor2 ! Compute(input2, channel2)

            // 匹配actor自己的receive
            receive {
                case !(channel2, x) => println(x)
            }
        }
    }
}
```

###20.6 同步消息和Future

actor可以发送一个消息并等待回复，用!?操作符即可：

```
import scala.actors.Actor

case class Deposit(amount: Int)
case class Balance(bal: Double)

class AccountActor extends Actor {

    private var balance = 0.0

    override def act(): Unit = {
        while (true) {
            receive {
                case Deposit(amount) => {
                    balance += amount
                    // 这两种发送都可以
//                    sender ! Balance(balance)
                    reply(Balance(balance))
                }
            }
        }
    }
}

/**
  * Created by zhangws on 17/2/13.
  */
object C20_6 {
    def main(args: Array[String]) {
        val account: AccountActor = new AccountActor
        account.start()

        val reply = account !? Deposit(1000)
        reply match {
            case Balance(bal) => println("Current Balance: " + bal)
        }
    }
}
```

可以用receiveWithin方法指定超时时间。在规定时间内没有收到消息，将会收到一个Actor.TIMEOUT对象。

```
actor {
    worker ! Task(data, self)
    receiveWithin(seconds * 1000) {
        case Result(data) => ...
        case TIMEOUT => log(...)
    }
}
```

除了等待对方返回结果外，也可以选择接收一个future——这是一个将在结果可用时产出结果的对象。使用!!方法即可做到：

```
val replyFuture = account !! Deposit(1000)

// isSet方法会检查结果是否已经可用。要接收该结果，使用函数调用的表示法：
val reply = replyFuture()
// 这个调用将会阻塞，直到回复被发送
```

###20.7 共享线程

要为每个actor创建单独的线程开销会很大，如果actor的大部分时间都用于等待消息，不如用一个线程来执行多个actor的消息处理函数。

方案一：

```
def act() {
    react {
        case Withdraw(amount) => {
            println("Withdrawing " + amount)
            act()
        }
    }
}

// 这个递归并不会占用很大的栈空间。每次对react的调用都会抛出异常，从而清栈。
```

方案二：

```
// 使用loop组合子可以制作一个无穷循环
def act() {
    loop {
        react {
            case Withdraw(amount) => process(amount)
        }
    }
}

// 如果需要一个循环条件，可以用loopWhile
loppWhile(count < max) {
    react {
        ...
    }
}
```

eventloop方法可以制作一个无穷循环套react的简化版，不过前提是偏函数不会再次调用react

```
def act() {
    eventloop {
        case Withdraw(amount) => println("Withdrawing " + amount)
    }
}
```

###20.8 Actor的生命周期

actor的act方法在actor的start方法被调用时开始执行。通常，actor接下来做的事情是进入某个循环。

actor在如下情形之一会终止执行：

1. act方法返回。
2. act方法由于异常被终止。
3. actor调用exit方法。（该方法参数可以为空或接受一个参数描述退出原因）

当actor因一个异常终止时，退出原因就是UncaughtException样例类的一个实例。该样例类有如下属性：

- actor：抛出异常的actor
- message：Some(msg)，其中msg是该actor处理的最后一条消息；或者None，如果actor在没来得及处理任何消息之前就挂掉的话。
- sender：Some(channel)，其中channel是代表最后一条消息的发送方的输出消息通道；或者None，如果actor在没来得及处理任何消息之前就挂掉的话。
- thread：actor退出时所在的线程。
- cause：相应的异常。

###20.9 将多个Actor链接在一起

如果将两个actor链接在一起，则每一个都会在另一个终止执行的时候得到通知。要建立这个关联关系，只要简单地以另一个actor的引用调用link方法即可。

```
def act() {
    link(master)
    ...
}
```

链接是双向的。工作actor挂掉时，监管actor应该知道，以便重新分配响应任务。反过来，如果监管actor挂掉了，工作actor也应该知道，以便可以停止工作。

默认，只要当前actor链接到的actor中有一个以非'normal原因退出，当前actor就会终止。这种情况下，退出原因和链接到的那个actor的退出原因相同。

actor可以改变这种行为，做法是设置trapExit为true。这样修改后，actor会接收到一个类型为Exit的消息，该消息包含了那个正在终止的actor和退出原因。

```
override def act() {
    trapExit = true
    link(worker)
    while (...) {
        receive {
            ...
            case Exit(linked, UncaughtException(_, _, _, _, cause)) => ...
            case Exit(linked, reason) => ...
        }
    }
}
```

###20.10 Actor的设计

一些建议：

1. 避免使用共享状态。通过消息交互数据，但小心消息当中的可变状态。
2. 不要调用actor的方法。否则会引入传统并发的锁。
3. 保持每个actor的简单。
4. 将上下文数据包含在消息中。
5. 最小化给发送方的回复。
6. 最小化阻塞调用。
7. 尽可能使用react。使用react的actor可以共享线程。只要消息处理器的工作是执行某个任务，然后退出，就可以使用react。
8. 建立失败区。actor失败是OK的，监管actor应该只负责管理失败的actor。

###20.11 习题解答

<font size =4 color=Blue>
1. 编写一个程序，生成由n个随机数组成的数组（其中n是一个很大的值，比如1000000），然后通过将工作分发给多个actor的同时计算这些数的平均值，每个actor计算区间内的值之和，将结果发送给一个能组合出结果的actor。
如果你在双核或四核处理器上运行这个程序，和单线程的解决方案相比，会快多少？
双核上差不多
</font>



<font size =4 color=Blue>
2. 编写一个程序，读取一个大型图片到BufferedImage对象中，用javax.imageio.ImangeIo.read方法。使用多个actor，每一个actor对图形的某一个条带区域进行反色处理。当所有条带都被反色后，输出结果。
</font>



<font size =4 color=Blue>
3. 编写一个程序，对给定目录下所有子目录的所有文件中匹配某个给定的正则表达式的单词进行计数。对每一个文件各采用一个actor，另外再加上一个actor用来遍历所有子目录，还有一个actor将结果汇总到一起。

</font>

<font size =4 color=Blue>
4. 修改前一个练习的程序，显示所有匹配的单词。
</font>

<font size =4 color=Blue>
5. 修改前一个练习的程序，显示所有匹配的单词，每一个都带有一个包含它的文件的列表。
</font>

<font size =4 color=Blue>
6. 编写一个程序，构造100个actor，这些actor使用while(true)/receive循环，当接收到‘Hello消息时，调用println(Thread.currentThread)，同时构造另外100个actor，他们做同样的事，不过采用loop/react。将它们全部启动，给它们全部都发送一个消息。第一种actor占用了多少线程，第二种actor占用了多少线程？
</font>

<font size =4 color=Blue>
7. 给练习3的程序添加一个监管actor，监控读取文件的actor并记录任何因IOException退出的actor。尝试通过移除那些计划要被处理的文件的方式触发IOException。
</font>

<font size =4 color=Blue>
8. 展示一个基于actor的程序是如何在发送同步消息时引发死锁的。
</font>

<font size =4 color=Blue>
9. 做出一个针对练习3的程序的有问题的实现，在这个实现当中，actor将更新一个共享的计数器。你能展现出程序运行是错误的吗？
</font>

<font size =4 color=Blue>
10. 重写练习1的程序，使用消息通道来进行通信。
</font>