新建一个容器

```
docker run --name master -h master --add-host master:172.17.0.2 --add-host slave01:172.17.0.3 --add-host slave02:172.17.0.4 -it centos:7
```

安装软件

```
yum install vim openssh-server
```

运行

```
docker run --name c7de85ce0e1e -i -t -p 2222:22 centos:7 /usr/sbin/sshd -D
```

命令 | 描述
---- | -----
docker kill $(docker ps -a -q) | 杀死所有正在运行的容器
docker rm $(docker ps -a -q) | 删除所有已经停止的容器
docker rmi $(docker images -q -f dangling=true) | 删除所有未打 dangling 标签的镜像
docker rmi $(docker images -q) | 删除所有镜像