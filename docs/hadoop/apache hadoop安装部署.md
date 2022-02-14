## Apache Hadoop安装部署
### 环境配置
| hadoop版本 | java版本要求 |
| --- | --- |
| apache hadoop 3.3+ | java 8 和 java 11 |
| apache hadoop 3.0.x ~ 3.2.x  | java 8 |
| apache hadoop 2.7.x ~ 2.10.x  | java 7 和 java 8 |
#### java
```shell
#java环境安装
rpm -ivh jdk-8u281-linux-x64.rpm
#配置环境变量
vi /etc/profile
#set java env
JAVA_HOME=/usr/java/jdk1.8.0_281-amd64
CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$JRE_HOME/lib/rt.jar
PATH=$PATH:$JAVA_HOME/bin:$JRE_HOME/bin
export JAVA_HOME JRE_HOME CLASSPATH PATH
#生效
source /etc/profile
#验证
java -version
javac -version
```
#### ssh
```shell
# 检查ssh是否安装
yum install -y openssh openssh-clients openssh-server
which ssh
# 启动sshd服务
systemctl start sshd
systemctl status sshd
# 安装pdsh (建议)
yum -y install pdsh gcc gcc-c++
```
#### 防火墙
```shell
#关闭防火墙
systemctl stop firewalld
systemctl disable firewalld
```
#### selinux
```shell
#关闭selinux
vi /etc/selinux/config
# This file controls the state of SELinux on the system.
# SELINUX= can take one of these three values:
#     enforcing - SELinux security policy is enforced.
#     permissive - SELinux prints warnings instead of enforcing.
#     disabled - No SELinux policy is loaded.
SELINUX=disabled
# SELINUXTYPE= can take one of three values:
#     targeted - Targeted processes are protected,
#     minimum - Modification of targeted policy. Only selected processes are protected. 
#     mls - Multi Level Security protection.
SELINUXTYPE=targeted

```
#### 网络
```shell
vi /etc/sysconfig/network-scripts/ifcfg-eth0
TYPE=Ethernet
PROXY_METHOD=none
BROWSER_ONLY=no
BOOTPROTO=static
DEFROUTE=yes
IPV4_FAILURE_FATAL=no
IPV6INIT=yes
IPV6_AUTOCONF=yes
IPV6_DEFROUTE=yes
IPV6_FAILURE_FATAL=no
IPV6_ADDR_GEN_MODE=stable-privacy
NAME=eth0
UUID=b488747f-7069-4ae6-90f3-95861ee97c23
DEVICE=eth0
ONBOOT=yes
IPADDR=192.168.11.183
NETMASK=255.255.248.0
GATEWAY=192.168.10.1
DNS1=192.168.1.1
DNS2=223.6.6.6
```
#### hostname
```shell
#主机名称全部小写，不能有_字符
vi /etc/hosts
127.0.0.1   localhost
192.168.11.181	hadoop-master
192.168.11.182	hadoop-slave1
192.168.11.183	hadoop-slave2

vi /etc/sysconfig/network
NETWORKING=yes
NETWORKING_IPV6=no
HOSTNAME=hadoop-master
```
#### 时钟同步
[http://www.pool.ntp.org/zone/cn](http://www.pool.ntp.org/zone/cn) <br/>网站包含全球的标准时间同步服务，也包括对中国时间的同步，对应的URL为cn.pool.ntp.org
```shell
yum install ntp -y 
systemctl enable ntpd
systemctl start ntpd
```
#### ssh互信
```shell
#生成公钥文件（全部节点）
ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
#整合公钥文件（master）
ssh hadoop-master cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
ssh hadoop-slave1 cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
ssh hadoop-slave2 cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 0600 ~/.ssh/authorized_keys
#分发文件到各节点（master）
scp ~/.ssh/authorized_keys hadoop-slave1:~/.ssh/
scp ~/.ssh/authorized_keys hadoop-slave2:~/.ssh/
#测试互信
ssh hadoop-master
ssh hadoop-slave1
ssh hadoop-slave2
```
#### 初始化目录
```shell
mkdir -p /data/hdfs/name
mkdir -p /data/hdfs/data
```
### 安装hadoop
#### 下载
选择一个稳定版本下载，[下载地址](https://mirrors.bfsu.edu.cn/apache/hadoop/common/stable/)
#### 环境配置
```shell
#本次安装目录为/usr/local/hadoop，解压文件到目录下
tar -zxvf hadoop-3.3.0.tar.gz
mv hadoop-3.3.0 /usr/local/hadoop/
#修改hadoop.evn.sh 
vi /usr/local/hadoop/hadoop-3.3.0/etc/hadoop/hadoop-env.sh
export JAVA_HOME=/usr/java/jdk1.8.0_281-amd64
export HADOOP_HOME=/usr/local/hadoop/hadoop-3.3.0
#验证
cd /usr/local/hadoop/hadoop-3.3.0
bin/hadoop
```
#### 单机
```shell
#默认情况下，hadoop运行在非分布式模式下，即一个独立的java进程，用来debug。
vi ~/.bash_profile
#set hadoop env
HADOOP_HOME=/usr/local/hadoop/hadoop-3.3.0
PATH=$PATH:$HADOOP_HOME/bin
export PATH
#test
hadoop version
hadoop fs -ls 
#运行example
cd ~
mkdir input 
cp $HADOOP_HOME/etc/hadoop/*.xml input
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.3.0.jar grep input output 'dfs[a-z.]+'
cat output/*
```
#### 伪分布式
```shell
#伪分布式模式下，每个hadoop后台程序运行在一个独立的java进程中
vi $HADOOP_HOME/etc/hadoop/core-site.xml
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://localhost:9000</value>
    </property>
</configuration>

vim $HADOOP_HOME/etc/hadoop/hdfs-site.xml
<configuration>
    <property>
        <name>dfs.replication</name>
        <value>1</value>
    </property>
</configuration>

#配置ssh免密登录
ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 0600 ~/.ssh/authorized_keys

ssh localhost

#启动hdfs服务
#1.格式化文件系统,默认存储路径为/tmp/hadoop-<user>/dfs
hdfs namenode -format
#2.启动namenode和datanode进程
vi start-dfs.sh
#!/usr/bin/env bash
HDFS_DATANODE_USER=root
HADOOP_SECURE_DN_USER=hdfs
HDFS_NAMENODE_USER=root
HDFS_SECONDARYNAMENODE_USER=root

vi stop-dfs.sh
#!/usr/bin/env bash
HDFS_DATANODE_USER=root
HADOOP_SECURE_DN_USER=hdfs
HDFS_NAMENODE_USER=root
HDFS_SECONDARYNAMENODE_USER=root

start-dfs.sh 
WARNING: HADOOP_SECURE_DN_USER has been replaced by HDFS_DATANODE_SECURE_USER. Using value of HADOOP_SECURE_DN_USER.
Starting namenodes on [localhost]
Last login: Wed Mar  3 04:04:50 EST 2021 from 192.168.12.78 on pts/0
Starting datanodes
Last login: Wed Mar  3 04:15:39 EST 2021 on pts/0
Starting secondary namenodes [hadoop_single]
Last login: Wed Mar  3 04:15:42 EST 2021 on pts/0
hadoop_single: Warning: Permanently added 'hadoop_single,192.168.11.201' (ECDSA) to the list of known hosts.

#3.打开浏览器访问 http://localhost:9870/
```
#### 运行MR
- 本地方式运行MR
```shell
#1.创建mr任务目录
hdfs dfs -mkdir /user
hdfs dfs -mkdir /user/root

#2.拷贝文件到hdfs并运行示例程序
hdfs dfs -mkdir -p /tmp/input
hdfs dfs -put $HADOOP_HOME/etc/hadoop/*.xml /tmp/input
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.3.0.jar grep /tmp/input /tmp/output 'dfs[a-z.]+'
hdfs dfs -cat /tmp/output/*
#或者
hdfs dfs -get /tmp/output output
cat output/*
#3.停止hdfs集群
stop-dfs.sh
WARNING: HADOOP_SECURE_DN_USER has been replaced by HDFS_DATANODE_SECURE_USER. Using value of HADOOP_SECURE_DN_USER.
Stopping namenodes on [localhost]
Last login: Wed Mar  3 04:20:05 EST 2021 from 192.168.12.78 on pts/1
Stopping datanodes
Last login: Wed Mar  3 04:43:43 EST 2021 on pts/1
Stopping secondary namenodes [hadoop_single]
Last login: Wed Mar  3 04:43:44 EST 2021 on pts/1
```

- YRAN方式运行MR
```shell
#1.配置参数
vi $HADOOP_HOME/etc/hadoop/mapred-site.xml
<configuration>
  <property>
      <name>mapreduce.framework.name</name>
      <value>yarn</value>
  </property>
  <property>
      <name>mapreduce.application.classpath</name>
      <value>$HADOOP_MAPRED_HOME/share/hadoop/mapreduce/*:$HADOOP_MAPRED_HOME/share/hadoop/mapreduce/lib/*</value>
  </property>
</configuration>

vi $HADOOP_HOME/etc/hadoop/yarn-site.xml
<configuration>
  <property>
      <name>yarn.nodemanager.aux-services</name>
      <value>mapreduce_shuffle</value>
  </property>
  <property>
      <name>yarn.nodemanager.env-whitelist</name>
      <value>JAVA_HOME,HADOOP_COMMON_HOME,HADOOP_HDFS_HOME,HADOOP_CONF_DIR,CLASSPATH_PREPEND_DISTCACHE,HADOOP_YARN_HOME,HADOOP_MAPRED_HOME</value>
  </property>
  <!--日志聚集-->
  <property>
    <name>yarn.log-aggregation-enable</name>
    <value>true</value>
  </property>
  <property>
    <name>yarn.log-aggregation.retain-seconds</name>
    <value>604800</value>
  </property>
</configuration>

#2.配置jobhistory
vi $HADOOP_HOME/etc/hadoop/mapred-site.xml
<configuration>
  <property>
    <name>mapreduce.jobhistory.address</name>
    <value>hadoop101:10020</value>
  </property>
  <property>
    <name>mapreduce.jobhistory.webapp.address</name>
    <value>hadoop101:19888</value>
  </property>
  <property>
    <name>yarn.log.server.url</name>
    <value>http://hadoop101:19888/jobhistory/logs</value>
  </property>
</configuration>
#3.修改启动脚本
vi $HADOOP_HOME/sbin/start-yarn.sh
#!/usr/bin/env bash
YARN_RESOURCEMANAGER_USER=root
HADOOP_SECURE_DN_USER=yarn
YARN_NODEMANAGER_USER=root

vi $HADOOP_HOME/sbin/stop-yarn.sh
#!/usr/bin/env bash
YARN_RESOURCEMANAGER_USER=root
HADOOP_SECURE_DN_USER=yarn
YARN_NODEMANAGER_USER=root

#4.启动yarn服务
start-yarn.sh
Starting resourcemanager
Last login: Wed Mar  3 04:47:25 EST 2021 on pts/1
Starting nodemanagers
Last login: Wed Mar  3 04:56:46 EST 2021 on pts/1

#5.访问http://localhost:8088/
#6.测试wordcount
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.3.0.jar wordcount /tmp/input/core-site.xml /tmp/output/wordcount
#7.停止yarn服务
stop-yarn.sh
```
#### 集群模式
###### 资源划分
|  | hadoop-master | hadoop-slave1 | hadoop-slave2 |
| --- | --- | --- | --- |
| HDFS | NameNode，DataNode | DataNode | SecondaryNameNode，DataNode |
| YRAN | ResourceManager，NodeManager | NodeManager | NodeManager |

###### 参数配置
hadoop主要有两类配置文件，只需要修改/etc/hadoop目录下*-site.xml就可以

- 只读默认配置：core-default.xml, hdfs-default.xml, yarn-default.xml, mapred-default.xml
- 自定义集群配置：etc/hadoop/core-site.xml, etc/hadoop/hdfs-site.xml, etc/hadoop/yarn-site.xml, etc/hadoop/mapred-site.xml
###### 相关进程
| 进程 | 环境变量 | 文件 |
| --- | --- | --- |
| NameNode  |  HDFS_NAMENODE_OPTS  | hadoop-env.sh |
|  DataNode  |  HDFS_DATANODE_OPTS  | hadoop-env.sh |
|  Secondary NameNode  | HDFS_SECONDARYNAMENODE_OPTS  | hadoop-env.sh |
|  ResourceManager  | YARN_RESOURCEMANAGER_OPTS  | yarn-env.sh |
|  NodeManager  |  YARN_NODEMANAGER_OPTS  | yarn-env.sh |
|  WebAppProxy  |  YARN_PROXYSERVER_OPTS  | yarn-env.sh |
|  Map Reduce Job History Server  |  MAPRED_HISTORYSERVER_OPTS  | mapred-env.sh |
```shell
#如未具体说明则需在所有节点执行
#1.配置全局环境变量
vi /etc/profile
#set hadoop env
HADOOP_HOME=/usr/local/hadoop/hadoop-3.3.0
PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
export HADOOP_HOME PATH

#2.配置hadoop-env.sh
export JAVA_HOME=/usr/java/jdk1.8.0_281-amd64
export HADOOP_HOME=/usr/local/hadoop/hadoop-3.3.0

#3.配置hadoop各进程参数（可选）
#示例:配置namenode使用parallelGC和4G堆内存，修改hadoop-env.sh文件
export HDFS_NAMENODE_OPTS="-XX:+UseParallelGC -Xmx4g"

#4.配置hadoop
vi $HADOOP_HOME/etc/hadoop/core-site.xml
<configuration>
  <property>
    <name>fs.defaultFS</name>
    <value>hdfs://hadoop-master:9000</value>
  </property>
  <property>
    <name>io.file.buffer.size</name>
    <value>131072</value>
  </property>
  <property>
	<name>hadoop.tmp.dir</name>
	<value>/data/hdfs/tmp</value>
  </property>
</configuration>

vi $HADOOP_HOME/etc/hadoop/hdfs-site.xml
<configuration>
  <property>
    <name>dfs.replication</name>
    <value>3</value>
  </property>
  <property>
    <name>dfs.namenode.name.dir</name>
    <value>/data/hdfs/name</value>
  </property>
  <property>
    <name>dfs.blocksize</name>
    <value>128m</value>
  </property>
  <property>
    <name>dfs.namenode.handler.count</name>
    <value>100</value>
  </property>
  <property>
    <name>dfs.datanode.data.dir</name>
    <value>/data/hdfs/data</value>
  </property>
	<property>
	  <name>dfs.namenode.secondary.http-address</name>
	  <value>hadoop-slave2:50090</value>
	</property>
</configuration>

vi $HADOOP_HOME/etc/hadoop/yarn-site.xml
<configuration>
  <property>
    <name>yarn.resourcemanager.hostname</name>
    <value>hadoop-master</value>
  </property>
  <property>
    <name>yarn.nodemanager.aux-services</name>
    <value>mapreduce_shuffle</value>
  </property>
  <property>
    <name>yarn.nodemanager.pmem-check-enabled</name>
    <value>false</value>
  </property>
  <property>
    <name>yarn.nodemanager.vmem-check-enabled</name>
    <value>false</value>
  </property>
  <property>
    <name>yarn.nodemanager.env-whitelist</name>
    <value>JAVA_HOME,HADOOP_COMMON_HOME,HADOOP_HDFS_HOME,HADOOP_CONF_DIR,CLASSPATH_PREPEND_DISTCACHE,HADOOP_YARN_HOME,HADOOP_HOME,PATH,LANG,TZ,HADOOP_MAPRED_HOME</value>
  </property>
  <property>
    <name>yarn.log-aggregation.retain-seconds</name>
    <value>86400</value>
  </property>
  <property>
    <name>yarn.log-aggregation.retain-check-interval-seconds</name>
    <value>-1</value>
  </property>
</configuration>

vi $HADOOP_HOME/etc/hadoop/mapred-site.xml
<configuration>
  <property>
    <name>mapreduce.framework.name</name>
    <value>yarn</value>
  </property>
  <property>
    <name>mapreduce.jobhistory.address</name>
    <value>hadoop-master:10020</value>
  </property>
  <property>
    <name>mapreduce.jobhistory.webapp.address</name>
    <value>hadoop-master:19888</value>
  </property>
</configuration>

#配置slaves文件
#列出所有work节点的host或者ip，一个节点一行
vi $HADOOP_HOME/etc/hadoop/workers
hadoop-master
hadoop-slave1
hadoop-slave2

#配置日志文件
#一般默认即可
vi $HADOOP_HOME/etc/hadoop/log4j.properties

#修改启动脚本
vi $HADOOP_HOME/sbin/start-dfs.sh
#!/usr/bin/env bash
HDFS_DATANODE_USER=root
HADOOP_SECURE_DN_USER=hdfs
HDFS_NAMENODE_USER=root
HDFS_SECONDARYNAMENODE_USER=root

vi $HADOOP_HOME/sbin/stop-dfs.sh
#!/usr/bin/env bash
HDFS_DATANODE_USER=root
HADOOP_SECURE_DN_USER=hdfs
HDFS_NAMENODE_USER=root
HDFS_SECONDARYNAMENODE_USER=root

vi $HADOOP_HOME/sbin/start-yarn.sh
#!/usr/bin/env bash
YARN_RESOURCEMANAGER_USER=root
HADOOP_SECURE_DN_USER=yarn
YARN_NODEMANAGER_USER=root

vi $HADOOP_HOME/sbin/stop-yarn.sh
#!/usr/bin/env bash
YARN_RESOURCEMANAGER_USER=root
HADOOP_SECURE_DN_USER=yarn
YARN_NODEMANAGER_USER=root
```
###### 启动集群
```shell
#单独启动各集群进程
#第一次启动集群需要格式化文件系统
hdfs namenode -format
#启动namenode(master)
hdfs --daemon start namenode
#启动datanode(all node)
hdfs --daemon start datanode
#启动yarn resourcemanager(yarn node)
yarn --daemon start resourcemanager
#启动yarn nodemanager(all node)
yarn --daemon start nodemanager
#启动proxyserver(master)
yarn --daemon start proxyserver
#启动historyserver(master)
mapred --daemon start historyserver

#使用脚本批量启动
#启动所有datanode
#(master)
start-dfs.sh
start-yarn.sh
yarn --daemon start proxyserver
mapred --daemon start historyserver
#检查后台进程
jps
```
###### 关闭集群
```shell
#关闭namenode
hdfs --daemon stop namenode
#关闭datanode
hdfs --daemon stop datanode
#关闭resourcemanager
yarn --daemon stop resourcemanager
#关闭nodemanager
yarn --daemon stop nodemanager
#关闭proxyserver
yarn stop proxyserver
#关闭historyserver
mapred --daemon stop historyserver

#脚本批量关闭
stop-dfs.sh
stop-yarn.sh
```
#### WEB地址
| 进程 | web url |
| --- | --- |
| namenode | [http://hadoop-master:9870/](http://192.168.11.181:9870/) |
| resourceManager | [http://hadoop-master:8088/](http://192.168.11.181:8088/) |
| MapReduce JobHistory Server  | [http://hadoop-master:19888/jobhistory](http://192.168.11.181:19888/jobhistory) |

#### 默认参数
参数说明

| 参数 | 描述 | 默认值 |
| --- | --- | --- |
| HADOOP_LOG_DIR | 日志目录 | $HADOOP_HOME/logs |
| HADOOP_PID_DIR | 后台进程文件目录 | /tmp |
| HADOOP_HEAPSIZE_MAX | 最大堆内存大小，默认单位M | jvm会根据机器配置调整 |

