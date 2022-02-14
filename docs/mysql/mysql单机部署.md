## Mysql 单机部署

### windows

1. 下载安装包
   https://downloads.mysql.com/archives/community/
2. 解压到安装目录 <br/>
   D:\Program Files\mysql
3. 编辑配置文件
    ```text
    ## D:\Program Files\mysql\my.ini
    [mysqld]
    basedir=D:\Program Files\mysql
    datadir=D:\Program Files\mysql\data
    port = 3306
    max_connections=200
    max_allowed_packet=10M
    lower_case_table_names=1
    character-set-server=utf8mb4
    default-storage-engine=INNODB
    server-id=1
    log-bin=mysql-bin
    binlog_format=row
    
    [mysql]
    default-character-set=utf8mb4
    ```
4. 配置环境变量 <br/>
   path中增加D:\Program Files\mysql\bin
5. 初始化
    ```shell
    ## 管理员权限，注意记录初始密码
   mysqld --initialize --console
    ```
6. 注册服务
   ```shell
   ## 卸载服务 mysqld --remove 
    mysqld --install 
   ```
7. 启动mysql<br/>
   服务列表中找到mysql右键启动
8. 修改初始密码
    ```mysql-sql
   mysql -u root -p 
   alter user user() identified by "new_password";
    ```

### linux

#### 环境规划

|IP|操作系统|安装目录|安装版本| 
|:---:|:---:|:---|:---:|
|192.168.1.103|centos6.7|/user/local/mysql|5.7.24|

#### 环境配置

1. YUM配置
   ```shell
   #1.拷贝拷贝光盘镜像到本地
   #2.创建软链接
   ln –s /opt/centos_6.7_dvd /var/www/html/centos
   #3.启动http服务
   service http start
   #打开浏览器输入 http://192.16.1.103/centos 能访问到即可。
   #4.创建repo文件
   #备份移动/etc/yum.repos.d/目录下的文件，新建centos.repo文件
   vi centos.repo
   
   [centos-6.7]
   name=centos-6.7
   baseurl=http://192.16.1.103/centos
   gpgcheck=0
   enable=1
   ```
2. 安装依赖包
   ```shell
   #1.检查依赖包
   yum list | grep libaio
   #2.安装依赖包
   yum install –y libaio
   ```
3. 创建用户和组
   ```shell
   groupadd mysql
   useradd -r -g mysql -s /bin/false mysql
   ```
4. 创建目录
   ```shell
   mkdir -p /usr/local/mysql/data
   chown -R mysql:mysql mysql/
   ```
5. 修改系统限制
   ```shell
   vi /etc/security/limits.conf

   *       hard    nofile  65535
   *       soft    nofile  65535
   ```
6. 关闭防火墙
   ```shell
   service iptables stop
   chkconfig iptables off
   ```
7. 关闭selinux
   ```shell
   vi /etc/selinux/config
   
   # This file controls the state of SELinux on the system.
   # SELINUX= can take one of these three values:
   #     enforcing - SELinux security policy is enforced.
   #     permissive - SELinux prints warnings instead of enforcing.
   #     disabled - No SELinux policy is loaded.
   SELINUX=disabled
   # SELINUXTYPE= can take one of these two values:
   #     targeted - Targeted processes are protected,
   #     mls - Multi Level Security protection.
   SELINUXTYPE=targeted
   ```

#### 安装

1. 解压安装包
   ```shell
   #下载
   wget https://downloads.mysql.com/archives/get/p/23/file/mysql-5.7.24-linux-glibc2.12-x86_64.tar.gz
   #解压
   tar -zxvf mysql-5.7.24-linux-glibc2.12-x86_64.tar.gz
   #复制到安装目录
   cp -r mysql-5.7.24-linux-glibc2.12-x86_64 /usr/local/mysql
   ```
2. 初始化数据库
   ```shell
   # 5.7.6版本之前
   ./bin/mysql_install_db --user=mysql --basedir=/usr/local/mysql/ --datadir=/usr/local/mysql/data/
   # 5.7.6版本之后
   ./bin/mysqld --initialize --user=mysql --basedir=/usr/local/mysql/ --datadir=/usr/local/mysql/data/
   # 初始化数据库时，命令会打印出数据库root用户的临时密码，此处需记住密码
   ```
3. 编辑my.cnf
   ```shell
   vi /etc/my.cnf
   
   [mysqld]
   basedir=/usr/local/mysql/
   datadir=/usr/local/mysql/data/
   socket=/tmp/mysql.sock
   user=mysql
   port=3306
   max_connections=200
   max_allowed_packet=10M
   character_set_server=utf8mb4
   lower_case_table_names=1
   default-storage-engine=INNODB
   server-id=1
   log-bin=mysql-bin
   binlog_format=row
   
   # Disabling symbolic-links is recommended to prevent assorted security risks
   symbolic-links=0
   
   [mysqld_safe]
   log-error=/var/log/mysqld.log
   pid-file=/var/run/mysqld/mysqld.pid
   
   [mysql]
   default-character-set=utf8mb4
   ```
4. 注册系统服务
   ```shell
   cp -a support-files/mysql.server /etc/init.d/mysqld
   ```
5. 启动数据库
   ```shell
   service mysqld start
   #关闭服务
   service mysqld stop 
   ```
6. 配置环境变量
   ```shell
   vi /root/.bash_profile
   MYSQL_HOME=/usr/local/mysql
   PATH=$PATH:$MYSQL_HOME/bin
   export PATH
   ```
7. 设置开机自启动
   ```shell
   chkconfig --add mysqld
   chkconfig |grep mysql
   ```

8. 重置root用户密码
   ```mysql-sql
   # 使用临时密码登录mysql后，可使用如下两种方式重置root密码
   mysql –u root –p
   set password = password('123456'); 
   alter user user() identified by "123456";
   ```