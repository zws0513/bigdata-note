- 通过直接读取Hive MetaStore中库表的数据，获取Hive中有哪些表，分区有哪些
- 通过`hdfs dfs -du xxx`获取每个目录的磁盘占有
- 最后统计入库，记录统计信息


```
-- 基本信息表
CREATE TABLE t_dw_tbl_base_info (
  id int(11) NOT NULL AUTO_INCREMENT,
  day date not null COMMENT '日期',
  db_name varchar(50) COMMENT '库名',
  tbl_name varchar(100) COMMENT '表名',
  tbl_type char(1) comment '0：内部表；1：外部表；2：未知',
  db_owner varchar(50) comment 'DB所属用户',
  tbl_owner varchar(50) comment 'TBL所属用户',
  location varchar(255) comment '建表目录',
  part_cols varchar(255) comment '分区列，逗号分隔',
  cols_num int comment '列数（不含分区列）',
  tbl_create_time datetime comment '建表时间',
  load_part_num int comment '加载的分区数',
  disk_usage DECIMAL(19, 3) comment '磁盘占用（MB）',

  create_time datetime default now() comment '创建时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```


```
-- Hive数据表磁盘占用详细信息
CREATE TABLE t_dw_tbl_part_detail (
  id int(11) NOT NULL AUTO_INCREMENT,
  day date not null COMMENT '日期',
  db_name varchar(50) COMMENT '库名',
  tbl_name varchar(100) COMMENT '表名',
  location varchar(255) comment '分区目录',
  disk_usage DECIMAL(19, 3) comment '磁盘占用（MB）',

  create_time datetime default now() comment '创建时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```