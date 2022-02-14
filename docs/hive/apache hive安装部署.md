## Apache Hive 安装部署

### 环境要求

- java 1.7
- hadoop 2.x/3.x,1.x在hive2.0.0后不再支持
- linux

### 安装

1. 下载
   <br/>以3.1.2版本为例 [下载地址](https://dlcdn.apache.org/hive/hive-3.1.2/apache-hive-3.1.2-bin.tar.gz)
2. 解压
   ```shell
   tar xxvf apache-hive-3.1.2-bin.tar.gz
   mv apache-hive-3.1.2-bin /usr/local/hadoop/hive-3.1.2
   ```
3. 设置环境变量
    ```shell
   vi /etc/profile
   
    #set hive env 
    HIVE_HOME=/usr/local/hadoop/hive-3.1.2
    PATH=$PATH:$HIVE_HOME/bin
    export HIVE_HOME PATH
    ```
4. 刷新环境变量
    ```shell
    source /etc/profile
    ```
5. 编辑hive配置文件
   ```shell
   cd /usr/local/hadoop/hive-3.1.2/
   cp hive-default.xml.template hive-site.xml
   vi hive-default.xml
   # 主要修改jdbc和thrift用户相关参数
   <configuration>
      <!--新增-->
      <property>
       <name>system:java.io.tmpdir</name>
       <value>/usr/local/hadoop/hive-3.1.2/tmp</value>
     </property>
     <property>
       <name>system:user.name</name>
       <value>root</value>
     </property>
   
     <!--修改-->
      <property>
       <name>javax.jdo.option.ConnectionUserName</name>
       <value>root</value>
       <description>Username to use against metastore database</description>
      </property>
      <property>
       <name>javax.jdo.option.ConnectionPassword</name>
       <value>123456</value>
       <description>password to use against metastore database</description>
      </property>
      <property>
       <name>javax.jdo.option.ConnectionURL</name>
       <value>jdbc:mysql://192.168.1.121:3306/hive</value>
       <description>
         JDBC connect string for a JDBC metastore.
         To use SSL to encrypt/authenticate the connection, provide database-specific SSL flag in the connection URL.
         For example, jdbc:postgresql://myhost/db?ssl=true for postgres database.
       </description>
     </property>
      <property>
       <name>javax.jdo.option.ConnectionDriverName</name>
       <value>com.mysql.cj.jdbc.Driver</value>
       <description>Driver class name for a JDBC metastore</description>
     </property>
     <property>
       <name>hive.server2.thrift.client.user</name>
       <value>root</value>
       <description>Username to use against thrift client</description>
     </property>
     <property>
       <name>hive.server2.thrift.client.password</name>
       <value>root</value>
       <description>Password to use against thrift client</description>
     </property>
   </configuration>
   ```
6. 上传mysql jdbc jar
   ```shell
   # 上传文件到hive的lib目录下
   cd /usr/local/hadoop/hive-3.1.2/lib
   
   ls | grep mysql
   mysql-connector-java-8.0.27.jar
   ```
7. 创建schema
   ```shell
   # 运行前需在mysql中创建好hive数据库
   cd /usr/local/hadoop/hive-3.1.2/bin
   ./schematool -dbType mysql -initSchema
   ```

### 运行 hive

_hive依赖hadoop，确保在path中存在hadoop相关的信息_

1. 创建hdfs目录
    ```shell
    hdfs dfs -mkdir -p /user/hive/warehouse
    hdfs dfs -mkdir /tmp
    hdfs dfs -chmod g+w /tmp
    hdfs dfs -chmod g+w /user/hive/warehouse
    ```
2. 运行HIVE CLI
   ```shell
   $ $HIVE_HOME/bin/hive
   ```
3. 运行HiveServer2和Beeline
   ```shell
   # 后台启动hiveserver2
   $ $HIVE_HOME/bin/hiveserver2 nohup hive --service hiveserver2 &
   # 嵌入模式
   $ $HIVE_HOME/bin/beeline -u jbdc:hive2://
   # 远程模式
   $ $HIVE_HOME/bin/beeline -u jbdc:hive2://localhost:10000 -n root 
   ```

### 常见问题

1. 启动hive报错
   ```text
   Exception in thread "main" java.lang.NoSuchMethodError: com.google.common.base.Preconditions.checkArgument(ZLjava/lang/String;Ljava/lang/Object;)V
	at org.apache.hadoop.conf.Configuration.set(Configuration.java:1380)
	at org.apache.hadoop.conf.Configuration.set(Configuration.java:1361)
	at org.apache.hadoop.mapred.JobConf.setJar(JobConf.java:536)
	at org.apache.hadoop.mapred.JobConf.setJarByClass(JobConf.java:554)
	at org.apache.hadoop.mapred.JobConf.<init>(JobConf.java:448)
	at org.apache.hadoop.hive.conf.HiveConf.initialize(HiveConf.java:5141)
	at org.apache.hadoop.hive.conf.HiveConf.<init>(HiveConf.java:5099)
	at org.apache.hadoop.hive.common.LogUtils.initHiveLog4jCommon(LogUtils.java:97)
	at org.apache.hadoop.hive.common.LogUtils.initHiveLog4j(LogUtils.java:81)
	at org.apache.hadoop.hive.cli.CliDriver.run(CliDriver.java:699)
	at org.apache.hadoop.hive.cli.CliDriver.main(CliDriver.java:683)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.apache.hadoop.util.RunJar.run(RunJar.java:323)
	at org.apache.hadoop.util.RunJar.main(RunJar.java:236)
   ```
   ```text
   #hadoop和hive的两个guava.jar版本不一致，将高版本复制到低版本目录中，删除低版本
   ls /usr/local/hadoop/hadoop-3.3.0/share/hadoop/common/lib/ | grep guava
   guava-27.0-jre.jar
   
   ls /usr/local/hadoop/hive-3.1.2/lib/ | grep guava
   guava-19.0.jar
   
   cp /usr/local/hadoop/hadoop-3.3.0/share/hadoop/common/lib/guava-27.0-jre.jar /usr/local/hadoop/hive-3.1.2/lib/
   rm /usr/local/hadoop/hive-3.1.2/lib/guava-19.0.jar
   ```
2. 初始化schema错误
   ```text
   Exception in thread "main" java.lang.RuntimeException: com.ctc.wstx.exc.WstxParsingException: Illegal character entity: expansion character (code 0x8
   at [row,col,system-id]: [3215,96,"file:/usr/local/hadoop/hive-3.1.2/conf/hive-site.xml"]
   at org.apache.hadoop.conf.Configuration.loadResource(Configuration.java:3051)
   at org.apache.hadoop.conf.Configuration.loadResources(Configuration.java:3000)
   at org.apache.hadoop.conf.Configuration.getProps(Configuration.java:2875)
   at org.apache.hadoop.conf.Configuration.get(Configuration.java:1484)
   at org.apache.hadoop.hive.conf.HiveConf.getVar(HiveConf.java:4996)
   at org.apache.hadoop.hive.conf.HiveConf.getVar(HiveConf.java:5069)
   at org.apache.hadoop.hive.conf.HiveConf.initialize(HiveConf.java:5156)
   at org.apache.hadoop.hive.conf.HiveConf.<init>(HiveConf.java:5104)
   at org.apache.hive.beeline.HiveSchemaTool.<init>(HiveSchemaTool.java:96)
   at org.apache.hive.beeline.HiveSchemaTool.main(HiveSchemaTool.java:1473)
   at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
   at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
   at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
   at java.lang.reflect.Method.invoke(Method.java:498)
   at org.apache.hadoop.util.RunJar.run(RunJar.java:323)
   at org.apache.hadoop.util.RunJar.main(RunJar.java:236)
   Caused by: com.ctc.wstx.exc.WstxParsingException: Illegal character entity: expansion character (code 0x8
   at [row,col,system-id]: [3215,96,"file:/usr/local/hadoop/hive-3.1.2/conf/hive-site.xml"]
   at com.ctc.wstx.sr.StreamScanner.constructWfcException(StreamScanner.java:621)
   at com.ctc.wstx.sr.StreamScanner.throwParseError(StreamScanner.java:491)
   at com.ctc.wstx.sr.StreamScanner.reportIllegalChar(StreamScanner.java:2456)
   at com.ctc.wstx.sr.StreamScanner.validateChar(StreamScanner.java:2403)
   at com.ctc.wstx.sr.StreamScanner.resolveCharEnt(StreamScanner.java:2369)
   at com.ctc.wstx.sr.StreamScanner.fullyResolveEntity(StreamScanner.java:1515)
   at com.ctc.wstx.sr.BasicStreamReader.nextFromTree(BasicStreamReader.java:2828)
   at com.ctc.wstx.sr.BasicStreamReader.next(BasicStreamReader.java:1123)
   at org.apache.hadoop.conf.Configuration$Parser.parseNext(Configuration.java:3347)
   at org.apache.hadoop.conf.Configuration$Parser.parse(Configuration.java:3141)
   at org.apache.hadoop.conf.Configuration.loadResource(Configuration.java:3034)
   ... 15 more
   ```
   ```text
   # hive-site.xml 文件中3215行特殊字符删除即可
   vi /usr/local/hadoop/hive-3.1.2/conf/hive-site.xml
   ```