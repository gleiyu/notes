## Ambari2.7.3+HDP3.1.0.0部署

### 环境配置

1. 检查操作系统

```shell
# 操作系统为centos7.6 CentOS Linux release 7.6.1810 (Core)
cat /etc/redhat-release 
# 设置时区为Shanghai,若安装时已设置则跳过
cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
# 设置默认语言为英文，若安装时已设置则跳过
echo $LANG
echo 'export LANG=en_US.UTF-8'>> ~/.bashrc
```

2. 配置网络

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
UUID=9669ea13-5192-4675-a7f0-1bb9c325ce54
ONBOOT=yes
IPADDR=192.168.1.130
NETMASK=255.255.255.0
PREFIX=24
GATEWAY=192.168.1.1
DNS1=192.168.1.1
DEVICE=eth0

```

3. 配置hostname

```shell
#主机名称全部小写，不能有_字符
vi /etc/hosts
127.0.0.1   localhost
192.168.1.130	hdp

vi /etc/sysconfig/network
NETWORKING=yes
NETWORKING_IPV6=no
HOSTNAME=hdp
#检查hostname
hostname -i
hostname -f
```

4. 关闭防火墙

```shell
systemctl stop firewalld
systemctl disable firewalld
```

5. 禁用selinux

```shell
#更改selinux为宽容模式
setenforce 0
#disable selinux
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

#修改limits.conf,在文件最后追加
*  hard    nproc    65535
*  soft    nproc    65535
*  hard    nofile   65535
*  soft    nofile   65535

```

6. 禁用交换分区

```shell
sysctl vm.swappiness=0
echo vm.swappiness=0 >> /etc/sysctl.conf
```

7. 设置umask

```shell
umask 0022
echo umask 0022 >> /etc/profile
source /etc/profile
```

8. 配置ssh互信

```shell
#生成公钥文件（全部节点）
ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
#整合公钥文件（master）
ssh hdp cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 0600 ~/.ssh/authorized_keys
#测试互信
ssh hdp
```

9. 配置os镜像源

```shell
#备份文件
mv /etc/yum.repos.d/*.repo /etc/yum.repos.d/bak
#下载repo文件
wget http://mirrors.aliyun.com/repo/Centos-7.repo
#本地缓存
yum list all 
yum makecache
```

10. 安装依赖软件

```shell
yum groupinstall "Infrastructure Server" --setopt=group_package_types=mandatory,default,optional -y
yum groupinstall "Development Tools" --setopt=group_package_types=mandatory,default,optional -y
```

11. 安装java

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

12. 配置yum源

```shell
# 安装httpd和createrepo
yum install httpd createrepo -y

# 开启httpd服务
service httpd start
#验证httpd状态
systemctl status httpd
# 设置开机启动
systemctl enable httpd

#配置rpm
tar zxvf ambari-2.7.3.0-centos7.tar.gz
mv ambari /var/www/html
tar zxvf HDP-3.1.0.0-centos7-rpm.tar.gz
mv HDP /var/www/html

#创建repo仓库
cd /var/www/html/ambari/centos7/2.7.3.0-139 && createrepo .
cd /var/www/html/HDP/centos7/3.1.0.0-78 && createrepo .

#创建repo文件
vi /etc/yum.repos.d/hdp.repo

[ambari]
name=ambari
baseurl=http://192.168.1.130/ambari/centos7/2.7.3.0-139
enabled=1
gpgcheck=0
[hdp]
name=hdp
baseurl=http://192.168.1.130/HDP/centos7/3.1.0.0-78
enabled=1
gpgcheck=0

# 在浏览器中可正常访问即配置成功
```

13. NTP时钟设置

```shell
# 检查是否安装ntp
rpm -qa|grep ntp
# 安装 ntp 服务
yum install ntp -y
#开机启动
systemctl enable ntpd
#配置ntp server
vi /etc/ntp.conf
# Hosts on local network are less restricted.
restrict 192.168.1.1  mask 255.255.255.0  nomodify notrap     
#允许内网其他机器同步时间，IP address1为当前网关，IP address2为子网掩码

# Use public servers from the pool.ntp.org project.
# Please consider joining the pool (http://www.pool.ntp.org/join.html).
#server 0.centos.pool.ntp.org iburst        #注释掉原时钟服务器
#server 1.centos.pool.ntp.org iburst
#server 2.centos.pool.ntp.org iburst
#server 3.centos.pool.ntp.org iburst

server 192.168.1.130                   #设置本机为时钟服务器
fudge 192.168.1.130 stratum 10

#重启ntp服务
systemctl restart ntpd.service

#在其他服务器中配置ntp client 
vi /etc/ntp.conf

# Hosts on local network are less restricted.
restrict 192.168.1.1  mask 255.255.255.0 nomodify notrap

# Use public servers from the pool.ntp.org project.
# Please consider joining the pool (http://www.pool.ntp.org/join.html).
#server 0.centos.pool.ntp.org iburst
#server 1.centos.pool.ntp.org iburst
#server 2.centos.pool.ntp.org iburst
#server 3.centos.pool.ntp.org iburst
server 192.168.1.130  prefer                      #设置为ntpserver的ip           

#重启ntp服务
systemctl restart ntpd.service
#5分钟后验证ntp时钟同步
ntpstat
#ntp server状态
synchronised to local net at stratum 6
   time correct to within 12 ms
   polling server every 64 s
#ntp client状态
synchronised to NTP server 192.168.1.130 at stratum 7
   time correct to within 41 ms
   polling server every 1024 s
```

14. 创建用户（可选）

```shell
groupadd bigdata
useradd -g bigdata -d /home/bigdata bigdata
echo "bigdata" | passwd --stdin bigdata
#bigdata设置root权限
cp /etc/sudoers /etc/sudoers_bak
echo "bigdata ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers
```

### 安装ambari

1. 安装数据库

```shell
# 安装mysql（略）
#创建用户和数据库
create database ambari;
use ambari;
create user 'ambari'@'%' identified by '123456';
grant all privileges on *.* to 'ambari'@'%' identified by '123456';
flush privileges;

```

2. 安装ambari-server

```shell
yum install hdp-select -y
yum install ambari-server
#设置ambari连接配置
ambari-server setup --jdbc-db=mysql --jdbc-driver=/usr/share/java/mysql-connector-java.jar
#配置ambari
ambari-server setup

Using python  /usr/bin/python
Setup ambari-server
Checking SELinux...
SELinux status is 'disabled'
Customize user account for ambari-server daemon [y/n] (n)? n
Adjusting ambari-server permissions and ownership...
Checking firewall status...
Checking JDK...
[1] Oracle JDK 1.8 + Java Cryptography Extension (JCE) Policy Files 8
[2] Custom JDK
==============================================================================
Enter choice (1): 2
WARNING: JDK must be installed on all hosts and JAVA_HOME must be valid on all hosts.
WARNING: JCE Policy files are required for configuring Kerberos security. If you plan to use Kerberos,please make sure JCE Unlimited Strength Jurisdiction Policy Files are valid on all hosts.
Path to JAVA_HOME: /usr/java/jdk1.8.0_281-amd64
Validating JDK on Ambari Server...done.
Check JDK version for Ambari Server...
JDK version found: 8
Minimum JDK version is 8 for Ambari. Skipping to setup different JDK for Ambari Server.
Checking GPL software agreement...
GPL License for LZO: https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html
Enable Ambari Server to download and install GPL Licensed LZO packages [y/n] (n)? Y
Completing setup...
Configuring database...
Enter advanced database configuration [y/n] (n)? Y
Configuring database...
==============================================================================
Choose one of the following options:
[1] - PostgreSQL (Embedded)
[2] - Oracle
[3] - MySQL / MariaDB
[4] - PostgreSQL
[5] - Microsoft SQL Server (Tech Preview)
[6] - SQL Anywhere
[7] - BDB
==============================================================================
Enter choice (1): 3
Hostname (localhost): localhost    
Port (3306): 3306
Database name (ambari): ambari
Username (ambari): ambari
Enter Database Password (bigdata): 
Re-enter password: 
Configuring ambari database...
Should ambari use existing default jdbc /usr/share/java/mysql-connector-java.jar [y/n] (y)? y
Configuring remote database connection properties...
WARNING: Before starting Ambari Server, you must run the following DDL directly from the database shell to create the schema: /var/lib/ambari-server/resources/Ambari-DDL-MySQL-CREATE.sql
Proceed with configuring remote database connection properties [y/n] (y)? 
Extracting system views...
....ambari-admin-2.7.3.0.139.jar

Ambari repo file doesn't contain latest json url, skipping repoinfos modification
Adjusting ambari-server permissions and ownership...
Ambari Server 'setup' completed successfully.

```

3. 安装hadoop

登录ambari-server 用户密码：admin/admin
[http://192.168.1.130:8080/#/login](http://192.168.1.130:8080/#/login) <br/>
![image.png](/docs/image/hadoop/ambari-01.png)
![image.png](/docs/image/hadoop/ambari-02.png)

```shell
#获取主机的ssh密钥
cat ~/.ssh/id_rsa
```

![image.png](/docs/image/hadoop/ambari-03.png)
![image.png](/docs/image/hadoop/ambari-04.png)
![image.png](/docs/image/hadoop/ambari-05.png)
![image.png](/docs/image/hadoop/ambari-06.png)
![image.png](/docs/image/hadoop/ambari-07.png)
![image.png](/docs/image/hadoop/ambari-08.png)
![image.png](/docs/image/hadoop/ambari-09.png)
![image.png](/docs/image/hadoop/ambari-10.png)
![image.png](/docs/image/hadoop/ambari-11.png)
![image.png](/docs/image/hadoop/ambari-12.png)
![image.png](/docs/image/hadoop/ambari-13.png)
![image.png](/docs/image/hadoop/ambari-14.png)
![image.png](/docs/image/hadoop/ambari-15.png)

#### 常用命令

```shell
#查看ambari服务状态
ambari-server status
#启动ambari-server
ambari-server start
```
