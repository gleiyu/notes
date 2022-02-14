## SQL常用函数

无特殊说明均默认为hive环境,[官方文档](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+UDF)

### 数学函数

|函数名|返回值|描述|示例|示例结果|
|---|---|---|---|---|
|round(double a [,int d])|int or double|四舍五入返回a的bigint值，或者返回指定d位小数的double值|`select round(1.5)`|2|
|bround(double a [,int d])|int or double|hive 1.3.0,2.0.0版本后支持，银行家四舍五入法，1~4：舍，6~9：进，5->前位数是偶：舍，5->前位数是奇：进|`select bround(2.5)`|2|
|floor(double a)|bigint|向下取整|`select floor(3.6)`|3|
|ceil(double a)|bigint|向上取整|`select ceil(3.6)`|4|
|greatest(T v1,T v2,...)|T|返回参数中的最大值，null为最小|`select greatest(null,1,2)`|2|
|least(T v1,T v2,...)|T|返回参数中的最小值，如果存在null值则返回null|`select least(null,1,2)`|null|
|rand([int seed])|double|返回一个0-1之间的随机值|`select round(rand(),2)`|0.73|
|exp(double&#124;decimal a)|double|返回e的a幂次方|`select exp(2)`|7.38905|
|ln(double&#124;decimal a)|double|以自然数为底的对数|`select ln(2)`|0.6931471805599453|
|log10(double&#124;decimal a)|double|以10为底的对数|`select log10(100)`|2.0|
|log2(double&#124;decimal a)|double|以2为底的对数|`select log2(8)`|3.0|
|log(double&#124;decimal base,double&#124;decimal a)|double|以base为底的对数|`select log(2,8)`|3.0|
|power(double a,double p)|double|a的p次幂|`select power(2,3)` |8.0|
|sqrt(double&#124;decimal a)|double|a的平方根|`select sqrt(3)`|1.7320508075688772|
|bin(bigint a)|string|计算a对应二进制的string值|`select bin(4)`|100|
|hex(bigint&#124;string&#124;binary a)|string|返回a对应的十六进制string值|`select hex(10)`|A|
|unhex(string a)|binary|hex的逆向方法|`select unhex(61)`|a|
|conv(bigint&#124;string num,int from_base,int to_base)|string|将number转换成另一种进制|`select conv(4,10,2)`|100|
|abs(double a)|double|返回a的绝对值|`select abs(-5.3)`|5.3|
|pmod(int a,int b)<br/>pmod(double a,double b)|int or double|返回a mod b 的正值|`select pmod(-5,2)`|1|
|mod(int a,int b)<br/> mod(double a,double b)|int or double|求余数|`select mod(-5,2)`|-1|
|sin(double&#124;decimal a)|double|求正弦值,参数单位为弧度|`select sin(1.57)`|0.999|
|asin(double&#124;decimal a)|double|求反正弦值|`select asin(0.999)`|1.526|
|cos(double&#124;decimal a)|double|求余弦值,参数单位为弧度|`select cos(1)`|0.5403|
|acos(double&#124;decimal a)|double|求反余弦值|`select acos(0.5403)`|1.000|
|tan(double&#124;decimal a)|double|求正切值,参数单位为弧度|`select tan(1)`|1.557|
|atan(double&#124;decimal a)|double|求反正切值|`select atan(1.557)`|0.999|
|degrees(double&#124;decimal a)|double|角度转弧度|`select degrees(1.570)`|89.95|
|radians(double&#124;decimal a)|double|弧度转角度|`select radians(90)`|1.570|
|positive(int&#124;double a)|int or double|返回a|`select positive(-2)`|-2|
|negative(int&#124;double a)|int or double|返回-a|`select negative(-2)`|2|
|sign(double&#124;decimal a)|int or double|如果a为整数则返回1.0，为负数则返回-1.0，否则返回0.0|`select sign(0.9)`|1.0|
|e()|double|返回常数e|`select e()`|2.7182818|
|pi()|double|返回常数pi|`select pi()`|3.1415936535|
|factorial(int a)|bigint|求a的阶乘|`select factorial(4)`|24|
|cbrt(double a)|double|求a的立方根|`select cbrt(8)`|2|
|shiftleft(tinyint&#124;smallint&#124;int a,int b)<br/>shiftleft(bigint a,int b)|int<br/>bigint|向左位移，hive1.2.0版本后支持|`select shiftleft(2,1)`|4|
|shiftright(tinyint&#124;smallint&#124;int a,int b)<br/>shiftright(bigint a,int b)|int<br/>bigint|向右位移，hive1.2.0版本后支持|`select shiftright(4,1)`|2|
|shiftrightunsigned(tinyint&#124;smallint&#124;int a,int b)<br/>shiftrightunsigned(bigint a,int b)|int<br/>bigint|无符号向右位移，hive1.2.0版本后支持|`select shiftrightunsigned(4,1)`|2|

### 集合函数

|函数名|返回值|描述|
|---|---|---|
|size(Map<K,V>)|int|求map的长度|
|size(Array<T>)|int|求数组的长度|
|map_keys(Map<K,V>)|array(K)|返回map的key数组|
|map_values(Map<K,V>)|array(V)|返回map的value数组|
|array_contains(Array<T>,value)|boolean|判断数组中是否包含value值|
|sort_array(Array<T>)|array<T>|按正序排序数组后返回|

### 类型转换函数

|函数名|返回值|描述|示例|示例结果|
|---|---|---|---|---|
|binary(stringt&#124;binary)|binary|将值转换为二进制|`select binary('a')`|a|
|cast(expr as <type>)|type|将expr转换为type类型，转换失败返回null|`select cast('10' as bigint)`|10|

### 日期函数

|函数名|返回值|描述|示例|示例结果|
|---|---|---|---|---|
|from_unixtime(bigint unixtime[,string format])|string|将秒值时间戳转换为format格式，默认为yyyy-MM-dd HH:mm:ss格式，结果和hive.local.time.zone的时区配置有关|`select from_unixtime(1237573801,'yyyy-MM-dd HH:mm:ss')`|2009-03-20 18:30:01|
|unix_timestamp()|bigint|返回当前时间的秒值时间戳，hive1.2.0后使用current_timestamp代替|`select unix_timestamp()`|1237573801|
|unix_timestamp(string date[,string pattern])|bigint|将时间字符串按格式转换为秒值时间戳，默认使用yyyy-MM-dd HH:mm:ss格式|`select unix_timestamp('2009-03-20 18:30:01')`|1237573801|
|to_date(string timestamp)|string 2.1.0版本前<br/> date 2.1.0版本之后|返回时间字符串的日期部分|`select to_date('1970-01-01 00:00:00')`|1970-01-01|
|year(string date)|int|返回时间字符串的年部分|`year("1970-01-01 00:00:00")`|1970|
|quarter(date&#124;timestamp&#124;string a)|int|返回时间字符串的季度部分,hive1.3.0版本后支持|`select quarter('2015-04-08')`|2|
|month(string date)|int|返回时间字符串的月份|`select month("1970-11-01 00:00:00")`|11|
|day(string date)<br/>dayofmonth(string date)|int|返回时间字符串的天部分|`select day("1970-11-01")`|1|
|hour(string date)|int|返回时间字符串的小时部分|`select hour('1970-11-01 00:00:00')`|0|
|minute(string date)|int|返回时间字符串的分钟部分|`select minute('1970-11-01 00:00:00')`|0|
|second(string date)|int|返回时间字符串的秒部分|`select second('1970-11-01 00:00:00')`|0|
|weekofyear(string date)|int|返回一年中的第几周|`select weekofyear("1970-11-01")`|44|
|extract(field from source)|int|截取时间中的field部分,hive2.2.0版本后支持,fields包含day, dayofweek, hour, minute, month, quarter, second, week和year|`select extract(dayofweek from '2016-10-20 05:06:07')`|5|
|datediff(string enddate,string startdate)|int|返回起始和结束时间的日期差|`select datediff('2021-01-01','2021-01-02 00:00:00')`|-1|
|date_add(date&#124;timestamp&#124;string startdate,tinyint&#124;smallint&#124;int days)|string 2.1.0版本前<br/> date 2.1.0版本之后|在开始时间上加days天|`select date_add('2021-01-01',1)`|2021-01-02|
|date_sub(date&#124;timestamp&#124;string startdate,tinyint&#124;smallint&#124;int days)|string 2.1.0版本前<br/> date 2.1.0版本之后|在开始时间上减去days天|`select date_sub('2021-01-03',1)`|2021-01-02|
|from_utc_timestamp()|timestamp|将utc时区的时间戳转换为指定时区的时间戳，hive0.8.0版本后支持|`select from_utc_timestamp(2592000000,'PST')`|1970-01-30 08:00:00|
|to_utc_timestamp()|timestamp|将指定时区的时间戳转换为utc时区的时间戳，hive0.8.0版本后支持|`select to_utc_timestamp(2592000000,'PST')`|1970-01-31 00:00:00|
|current_date|date|返回当前的日期，hive1.2.0版本后支持|`select current_date()`|2021-02-07|
|current_timestamp|timestamp|返回当前的timestamp，hive1.2.0版本后支持|`select current_timestamp()`|2021-02-07 08:29:13.234|
|add_months(string start_date,int num_months,output_date_format)|string|返回当前时间加n个月后的日期|`select add_months('2017-12-31 14:15:16', 2, 'YYYY-MM-dd HH:mm:ss')`|2018-02-28 14:15:16|
|last_day(string date)|string|返回月末的日期|`select last_day('2021-01-01')`|2021-01-31|
|next_day(string start_date,string day_of_week)|string|返回当前时间下个星期X对应的日期|`select next_day('2015-01-14','TU')`|2015-01-20|
|trunc(string date,string format)|string|返回时间最开始的年份或月份,支持格式有MONTH/MON/MM, YEAR/YYYY/YY,hive1.2.0版本后支持|`select trunc('2015-03-17','MM')`|2015-03-01|
|month_between(date1,date2)|double|返回两个时间之间的月份差，date1>date2返回正数，date1<date2返回负数，否则返回0.0|`select months_between('2021-04-02','2021-03-02')`|1|
|date_format(date/timestamp/string ts,string format)|string|根据指定格式格式化时间|`select date_format('2021-02-01','yyyyMM')`|202102|

### 条件函数

|函数名|返回值|描述|示例|示例结果|
|---|---|---|---|---|
|if(boolean condition,T valueTrue,T valueFalseOrNull)|T|根据条件判断true则返回第2个参数否则返回第3个参数|`select if(1=1,1,2)`|1|
|isnull(a)|boolean|如果a为null则返回true，否则返回false|`select isnull(1)`|false|
|isnotnull(a)|boolean|如果a不为null则返回true，否则返回false|`select isnotnull(1)`|true|
|nvl(T value,T defaultValue)|T|如果第一个参数为空则返回默认值，hive0.11版本后支持|`select nvl(null,5)`|5|
|coalesce(T v1, T v2, ...)|T|返回第一个不为空的参数|`select coalesce(null,null,5)`|5|
|CASE a WHEN b THEN c [WHEN d THEN e]* [ELSE f] END|T|当a=b时返回c，a=d时返回e，否则返回f|`select case 1 when 2 then 'a' when 1 then 'b' else 'c' end`|b|
|CASE WHEN a THEN b [WHEN c THEN d]* [ELSE e] END|T|当a为true时返回b，当c为true时返回d，否则返回f|`select case when 2=1 then 'a' when 1=1 then 'b' else 'c' end`|b|
|nullif(a,b)|T|当a=b时返回null，否则返回a|`select nullif(2,1)`|2|
|assert_true(boolean condition)|void|当条件为false时抛异常,hive0.8.0版本后支持|`select assert_true(1=2)`|org.apache.hive.service.cli.HiveSQLException: java.io.IOException: org.apache.hadoop.hive.ql.metadata.HiveException: ASSERT_TRUE(): assertion failed|

### 字符函数

|函数名|返回值|描述|示例|示例结果|
|---|---|---|---|---|
|ascii(string a)|int|返回字符串的ascii编码|`select ascii('a')`|97|
|base64(binary bin)|string|将二进制转换为base64字符串|`select base64(binary('abc'))`|YWJj|
|character_length(string str)<br/>char_length(string str)|int|返回UTF-8字符个数,HIVE2.2.0版本后支持|`select char_length('abc')`|4|
|chr(bigint&#124;double a)|string|返回数值对应的ascii码，如果数值超过256则返回对应余数的ascii码|`select chr(88)`|X|
|concat(string&#124;binary A, string&#124;binary B...)|string|字符串拼接,如果存在null值则返回null,hive2.0.0版本后可使用 &#124;&#124; 形式替代|`select concat('a','b',null)`|NULL|
|context_ngrams(array<array < string >>, array< string >, int K, int pf)|array<struct<string,double>>|统计二维数组中出现对应一维数组的topK词频,第4个可选参数控制精度，过大可能出现oom|`select context_ngrams(sentences('hello hive!hello hive!hi hive'),array(null,'hive'),1)`|[{"ngram":["hello"],"estfrequency":2.0}]|
|concat_ws(string SEP, string A, string B...)|string|指定分隔符拼接字符串|`select concat_ws('-','a','b')`|a-b|
|concat_ws(string SEP,array< string >)|string|指定分隔符拼接字符串数组|`select concat_ws('-',array('a','b'))`|a-b|
|decode(binary bin,string charset)|string|转换二进制数据为指定字符集的字符串，任意参数为null返回null，hive0.12.0版本后支持|`select decode(binary('a'),'UTF-8')`|a|
|elt(N int,str1 string,str2 string,str3 string,...)|string|返回指定index的字符串，当n<1或者大于参数个数则返回null|`select elt(2,'hello','world') `|world|
|encode(string src, string charset)|将字符串转换为指定字符集的二进制数据，任意参数为null则返回null|`select encode('a','UTF-8')`|a|
|field(val T,val1 T,val2 T,val3 T,...)|int|返回val所在对应参数的索引位置，参数为null或者找不到则均返回0|`select field('world','say','hello','world')`|3|
|find_in_set(string str, string strList)|int|返回str在strList中第一次出现的位置，strList为逗号分隔的字符串，参数为null或者找不到则均返回0|`select find_in_set('ab', 'abc,b,ab,c,def')`|3|
|format_number(number x, int d)|string|将数字格式化为字符串|`select format_number(123123.45,2)`|123,123.45|
|get_json_object(string json_string, string path)|string|根据path提取json中的内容|`select get_json_object('{"k1":"v1","k2":"v2"}','$.k2')`|v2|
|in_file(string str, string filename)|boolean|如果str为文件中的一行数据则返回true|||
|instr(string str, string substr)|int|返回substr在str中第一次出现的位置，参数为null或者找不到则均返回0|`select instr('abc','b')`|2|
|length(string A)|int|返回字符串的长度|`select length('你好')`|2|
|locate(string substr, string str[, int pos])|返回在pos位置后substr在str中第一次出现的位置|`select locate('b','abcabc',3)`|5|
|lower(string A) <br/> lcase(string A)|string|转换为小写字符串|`select lower('fOoBaR')`|foobar|
|lpad(string str, int len, string pad)|string|在字符串str左边拼接pad使其长度为len|`select lpad('a',3,'bcd')`<br/>`select lpad('aaaaa',3,'bcd')`|bca<br/>aaa|
|ltrim(string A)|string|去除字符串左边的空格|`select ltrim(' foobar ') `|foobar |
|ngrams(array<array<string>>, int N, int K, int pf)|array<struct<string,double>>|统计n个词语同时出现频次topk的字符串|`select ngrams(sentences('hello world!hello hive,hello hive'),1,2)`|[{"ngram":["hello"],"estfrequency":3.0},{"ngram":["hive"],"estfrequency":2.0}]|
|octet_length(string str)|int|返回utf-8编码保存str所需的八位字节数|`select octet_length('你')`|3|
|parse_url(string urlString, string partToExtract [, string keyToExtract])|string|解析指定部分url字符串中的数据，HOST, PATH, QUERY, REF, PROTOCOL, AUTHORITY, FILE和 USERINFO|`select parse_url('http://facebook.com/path1/p.php?k1=v1&k2=v2#Ref1', 'QUERY','k2')`<br/>`select parse_url('http://facebook.com/path1/p.php?k1=v1&k2=v2#Ref1', 'HOST')`|v2<br/>facebook.com|
|printf(String format, Obj... args)|string|格式化字符串|`select printf('%.2f%%',1/3)`|0.33%|
|regexp_extract(string subject, string pattern, int index)|string|根据正则提取字符串|`select regexp_extract('foothebar', 'foo(.*?)(bar)', 1)`|the|
|regexp_replace(string INITIAL_STRING, string PATTERN, string REPLACEMENT)|string|根据正则替换字符串|`select regexp_replace("foobar", "oo", "")`|fbar|
|repeat(string str, int n)|string|重复str字符串n次|`select repeats('a',3)`|aaa|
|rpad(string str, int len, string pad)|string|在字符串str右边拼接pad使其长度为len|`select rpad('a',3,'bcd')`<br/>`select rpad('aaaaa',3,'bcd')`|abc<br/>aaa|
|rtrim(string A)|string|去除字符串右边的空格|`select rtrim(' foobar ')`| foobar|
|sentences(string str, string lang, string locale)|array<array<string>>|字符串分词|`select sentences('Hello there! How are you?')`|[["Hello","there"],["How","are","you"]]|
|space(int n)|string|返回n个空格的字符串|`select space(5)`|     |
|split(string str, string pat)|array|将字符串str根据pat分隔为数组|`select split('abcabcabcabc','bc')`|["a","a","a","a",""] |
|str_to_map(text[, delimiter1, delimiter2])|map<string,string>|将字符串转换为map，默认分隔符为','和':'|`select str_to_map('k1:v1,k2:v2')`|{"k1":"v1","k2":"v2"}|
|substr(string&#124;binary A, int start) <br/> substring(string&#124;binary A, int start)|string|截取字符串|`select substr('foobar', 4)`|bar|
|substr(string&#124;binary A, int start, int len) substring(string&#124;binary A, int start, int len)|string|截取字符串|`select substr('foobar', 4, 1)`|b|
|substring_index(string A, string delim, int count)|string|将字符串按delim分隔后截取count计数部分|`select substring_index('www.apache.org', '.', 1)`|www|
|translate(string&#124;char&#124;varchar input, string&#124;char&#124;varchar from, string&#124;char&#124;varchar to)|将from字符串转换为to字符串，类似replace|`select translate('abc','bc','d')`|ad|
|trim(string A)|string|同时去除左右两边的空格|`select trim(' foobar ')`|foobar|
|unbase64(string str)|binary|转换base64字符串为二进制格式|`select unbase64('YWJj')`|abc|
|upper(string A)<br/> ucase(string A)|将字符串转换为大写|`select upper('fOoBaR')`|FOOBAR|
|initcap(string A)|string|将字符串转换为首字母大写格式|`select initcap('hello world')`|Hello World|
|levenshtein(string A, string B)|int|返回两个字符串的相似度距离|`select levenshtein('hello', 'hi')`|4|
|soundex(string A)|string|返回字符串的桑迪克码|`soundex('Miller')`|M460|
### 数据屏蔽函数

|函数名|返回值|描述|示例|示例结果|
|---|---|---|---|---|
|mask(string str[, string upper[, string lower[, string number]]])|string|返回屏蔽后的字符串|`select mask("abcd-EFGH-8765-4321")`<br/>`select mask("abcd-EFGH-8765-4321", "U", "l", "#") `|xxxx-XXXX-nnnn-nnnn<br/>llll-UUUU-####-####|
|mask_first_n(string str[, int n])|string|屏蔽前N个字符|`select mask_first_n("1234-5678-8765-4321", 3)`|nnn4-5678-8765-4321|
|mask_last_n(string str[, int n])|string|屏蔽后N个字符|`select mask_last_n("1234-5678-8765-4321", 3)`|1234-5678-8765-4nnn|
|mask_show_first_n(string str[, int n])|string|屏蔽除前N个的其他字符|`select mask_show_first_n("1234-5678-8765-4321", 4)`|1234-nnnn-nnnn-nnnn|
|mask_show_last_n(string str[, int n])|string|屏蔽除后N个的其他字符|`mask_show_last_n("1234-5678-8765-4321", 4)`|nnnn-nnnn-nnnn-4321|
|mask_hash(string&#124;char&#124;varchar str)|string|使用hash进行屏蔽字符串|`select mask_hash('1234-5678-8765-4321')`|0f2702a326f58ab2c230e2947c7e4f6eb0b33ceb0c1b3d010cc9a81a8c46fd89|

### 其他函数

|函数名|返回值|描述|示例|示例结果|
|---|---|---|---|---|
|java_method(class, method[, arg1[, arg2..]])|varies|reflect函数的同义词，调用java方法|
|reflect(class, method[, arg1[, arg2..]])|varies|调用java方法|
|hash(a1[, a2...])|int|返回hash值|`select hash('123','abc')`|1605744|
|current_user()|string|返回当前的用户名|`select current_user()`|root|
|logged_in_user()|string|返回当前会话的用户名|`select logged_in_user()`|root|
|current_database()|string|返回当前的数据库名称|`select current_database()`|default|
|md5(string&#124;binary)|string|返回md5值|`select md5('123456')`|e10adc3949ba59abbe56e057f20f883e|
|sha1(string&#124;binary)<br/>sha(string&#124;binary)|string|返回sha-1加密后的十六进制字符串|`select sha1('ABC')`|3c01bdbb26f358bab27f267924aa2c9a03fcfdb8|
|crc32(string&#124;binary)|bigint|返回循环冗余校验码|`select crc32('ABC')`|2743272264|
|sha2(string/binary, int)|string|使用sha-2算法加密数据|`sha2('ABC', 256)`|b5d4045c3f466fa91fe2cc6abe79232a1a57cdf104f7a26e716e0a1e2789df78|
|aes_encrypt(input string/binary, key string/binary)|binary|使用aes算法加密数据|`base64(aes_encrypt('ABC', '1234567890123456'))`|y6Ss+zCYObpCbgfWfyNWTw==|
|aes_decrypt(input binary, key string/binary)|binary|使用aes算法解密数据|`aes_decrypt(unbase64('y6Ss+zCYObpCbgfWfyNWTw=='), '1234567890123456')`|ABC|
|version()|string|返回hive的版本号|`select version()`|2.1.0.2.5.0.0-1245 r027527b9c5ce1a3d7d0b6d2e6de2378fb0c39232|
|surrogate_key([write_id_bits, task_id_bits])|bigint|为输入的数据生成代理键|||
### 内置UDAF

|函数名|返回值|描述|示例|示例结果|
|---|---|---|---|---|
|count(*), count(expr), count(DISTINCT expr[, expr...])|bigint|计算记录数|
|sum(col), sum(DISTINCT col)|double|计算总和|
|avg(col), avg(DISTINCT col)|double|计算平均值|
|min(col)|double|计算最小值|
|max(col)|double|计算最大值|
|variance(col), var_pop(col)|double|计算方差|
|var_samp(col)|double|计算样本方差|
|stddev_pop(col)|double|计算标准偏差|
|stddev_samp(col)|double|计算样本标准偏差|
|covar_pop(col1, col2)|double|计算协方差|
|covar_samp(col1, col2)|double|计算样本协方差|
|corr(col1, col2)|double|计算两数值列的相关系数|
|percentile(BIGINT col, p)|double|返回col的p%分位数|
|percentile(BIGINT col, array(p1 [, p2]...))|array<double>|返回col的p1%，p2%分位数|
|percentile_approx(DOUBLE col, p [, B])|double|返回col的近似百分位数|
|percentile_approx(DOUBLE col, array(p1 [, p2]...) [, B])|array<double>|返回col的多个近似百分位数|
|histogram_numeric(col, b)|array<struct {'x','y'}>|计算数字列的直方图|
|collect_set(col)|array|返回一个无重复值的对象列表|
|collect_list(col)|array|返回一个对象列表，可能有重复值|
|ntile(INTEGER x)|integer|数据分桶，返回值为桶号|
### 内置UDTF

|函数名|返回值|描述|示例|示例结果|
|---|---|---|---|---|
|explode(ARRAY<T> a)|T|将一个数组转换为多行|
|explode(MAP<Tkey,Tvalue> m)|Tkey,Tvalue|将map转换为多行|
|posexplode(ARRAY<T> a)|int,T|将数组转换为多行，且返回行号|
|inline(ARRAY<STRUCT<f1:T1,...,fn:Tn>> a)|T1,...,Tn|将结构体数组转为多行|
|stack(int r,T1 V1,...,Tn/r Vn)|T1,...,Tn/r|将值拆分为r行|
|json_tuple(string jsonStr,string k1,...,string kn)|string1,...,stringn|解析json字符串|
|parse_url_tuple(string urlStr,string p1,...,string pn)|string 1,...,stringn|解析url字符串|


