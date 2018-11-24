#1. Spring Boot概述
##1.1 核心功能

###1.1.1 独立运行的Spring项目

Spring Boot可以以jar包的形式进行独立的运行，使用：java -jar xx.jar 就可以成功的运行项目，或者在应用项目的主程序中运行main函数即可；

```bash
java -Xms512m -Xmx1024m  \
    -classpath xx/conf -Djava.ext.dirs="xx/lib" \
    -jar $PRG_PATH --spring.config.location $CONF_PATH
```

###1.1.2 内嵌Servlet容器（Tomcat、Jetty）

内嵌容器，使得我们可以执行运行项目的主程序main函数，是想项目的快速运行；

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * webapi启动类
 * Created by zhangws on 16/11/17.
 */
@SpringBootApplication
@ImportResource("classpath:dubbo-consumer.xml")
public class WebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApiApplication.class, args);
    }
}
```

###1.1.3 提供starter简化Maven配置

Spring Boot提供了一系列的starter pom用来简化我们的Maven依赖,下边是创建一个web项目中自动包含的依赖，使用的starter pom以来为：spring-boot-starter-web

![xx](http://img.blog.csdn.net/20161016141842188)

Spring Boot官网还提供了很多的starter pom，请参考：

http://docs.spring.io/spring-boot/docs/1.4.2.RELEASE/reference/htmlsingle/#using-boot-starter

![starter](http://img.blog.csdn.net/20161016142237995)

###1.1.4 自动配置Spring

Spring Boot会根据我们项目中类路径的jar包/类，为jar包的类进行自动配置Bean，这样一来就大大的简化了我们的配置。当然，这只是Spring考虑到的大多数的使用场景，在一些特殊情况，我们还需要自定义自动配置；

###1.1.5 应用监控

Spring Boot提供了基于http、ssh、telnet对运行时的项目进行监控；这个听起来是不是很炫酷！

示例：以SSH登录为例

1、首先，添加starter pom依赖

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-remote-shell</artifactId>
</dependency>
```

2、运行项目,此时在控制台中会出现SSH访问的密码：

![](http://img.blog.csdn.net/20161016143657624)

```
ssh -p 2000 user@127.0.0.1
```

###1.1.6 无代码生成和XML配置

Spring Boot神奇的地方不是借助于代码生成来实现的，而是通过条件注解的方式来实现的，这也是Spring 4.x的新特性。
