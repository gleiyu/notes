## SQL开发基础

无特殊说明均默认为使用mysql8.0环境

### distinct

``` sql
-- distinct 可配合 avg，count，sum一起使用，只计算不重复的值
with test as (
    select 3 as id union all 
    select 4 as id union all 
    select 4 as id union all 
    select 3 as id union all 
    select 4 as id union all 
    select 8 as id  
)
select 
    sum(id) as sum_all,
    sum(distinct id) as sum_distinct,
    avg(id) as avg_all,
    avg(distinct id) as avg_distinct,
    count(id) as count_all,
    count(distinct id) as count_distinct
from test
```

### 集合操作

```sql
-- union 会去重,相当于union all后再全表去重
select 1 as id 
union all
select 1 as id 
union
select 1 as id
-- union all  不去重
select 1 as id 
union all
select 1 as id
-- minus 求差集 第一个表中存在，第二个表中不存在的数据
select 1 as id 
union all
select 2 as id
minus
select 1 as id
-- intersect  求交集
select 1 as id 
union all
select 2 as id
intersect 
select 1 as id
```

### update from

```sql
-- oracle 
update table2 t2 
set (t2.colb,t2.colc) = (select t1.colb,t1.colc from table1 t1 where t1.id = t2.id)
where t2.id > 1000
-- mysql 
update table1,table2
set table1.name = table2.name,
table1.colb = table2.colb
where table1.id = table2.id
and table2.id > 1000 ;

update table1 t1 inner join table2 t2
on t1.id = t2.id
set t1.name = t2.name,
t1.colb = t2.colb
where t2.id > 1000
-- sql server
update table1
set colb = t2.colb 
from table1 t1,table2 t2
where t1.id = t2.id;

update table1
set colb = t2.colb 
from table1 t1 inner join table2 t2
on t1.id = t2.id;
```

### 递归查询

```sql
-- 生成从1到100的连续数字
-- hive 
select 
    row_number() over() as rn 
from (select explode(split(repeat('1,',100),',')) as r) t1;
-- oracle
select rownum from dual connect by rownum <= 100;
select level as lv from dual connect by level <= 100;
-- mysql 8版本开始支持
with recursive tt as (
    select 1 as id 
    union all 
    select id+1 from tt where id <=99
)
select * from tt;

-- 递归查询父子关系
-- oracle 
select emp_id,
       lead_id,
       emp_name,
       prior emp_name as lead_name,
       level
  from employee
 start with lead_id = 0
connect by prior emp_id = lead_id
-- mysql
with recursive tt as (
    select emp_id,emp_name,lead_id,null as lead_name from employee where lead_id = 0
    union all 
    select e.emp_id,e.emp_name,e.emp_id as lead_id,t.emp_name as lead_name from employee e, tt t where e.lead_id = t.emp_id 
)
select * from tt;
```

### 行列转换

```sql
-- 行转列
with test as (
    select '2014' as a,'B' as b,9 as c 
    union all 
    select '2015' as a,'A' as b,8 as c 
    union all 
    select '2014' as a,'A' as b,10 as c 
    union all 
    select '2015' as a,'B' as b,7 as c 
)
select 
    a,
    max(col_a) as col_a,
    max(col_b) as col_b
from 
(
	select 
	    a,
	    case when b = 'A' then c else null end as col_a,
	    case when b = 'B' then c else null end as col_b
	from test
) t1 
group by a
;
-- 多行转一列
with test as (
    select '2014' as a,'B' as b,9 as c 
    union all 
    select '2015' as a,'A' as b,8 as c 
    union all 
    select '2014' as a,'A' as b,10 as c 
    union all 
    select '2015' as a,'B' as b,7 as c 
)
select 
    a,
    concat_ws(',',collect_set(b))
from test
group by a 
;
-- 列转行
with test as (
    select '2014' as a,10 as col_a,9 as col_b
    union all 
    select '2015' as a,8 as col_a,7 as col_b
)
select a,'A' as b, col_a as c from test
union all 
select a,'B' as b,col_b as c from test
;
-- explode 拆解单列转行
with test as (
    select 'a,b,c' as id,'1,2,3' as nums 
    union all 
    select 'd,e,f' as id,'7,8,9' as nums 
)
select 
    id,
    nums,
    e.num 
from test lateral view explode(split(nums,',')) e as num ;
-- posexplode 拆解多列转多行
with test as (
    select 'a,b,c' as id,'1,2,3' as nums 
    union all 
    select 'd,e,f' as id,'7,8,9' as nums 
)
select 
    t1.num1 as id,
    t1.num2 as num 
from 
(
    select 
        id,
        e1.rowid as rowid1,
        e1.num as num1,
        e2.rowid as rowid2,
        e2.num as num2
    from test 
    lateral view posexplode(split(nums,',')) e1 as rowid,num
    lateral view posexplode(split(id,',')) e2 as rowid,num
) t1 
where t1.rowid1 = t1.rowid2 
```

### json处理

```sql
-- json字符串：{"id":1,"token":"1234","user_name":"tom"}
-- get_json_object
select 
	get_json_object(jsonStr, '$.id') as id, 
	get_json_object(jsonStr, '$.token') as token, 
	get_json_object(jsonStr, '$.user_name') as user_name
from jsonDemo;
-- json_tuple
select 
  j.id,
  j.token,
  j.user_name
from jsonDemo lateral view json_tuple(jsonStr, 'id','token','user_name') j as id,token,user_name;
```

### 虚拟表

```sql
-- stack 
select stack(2, 0, 'tom',1,'jack') as (id, name)
-- with as
with test as (
  select 0 as id,'tom' as name
  union all 
  select 1 as id,'jack' as name 
)
select * from test;
```

### 窗口函数

```sql
-- sql中窗口函数执行仅在order by语句前，在where、group by 等语句后
-- 模拟数据
create table test as 
select stack(14, 
        'jack','2015-01-01','10','tony','2015-01-02','15','jack','2015-02-03','23',
        'tony','2015-01-04','29','jack','2015-01-05','46','jack','2015-04-06','42',
        'tony','2015-01-07','50','jack','2015-01-08','55','mart','2015-04-08','62',
        'mart','2015-04-09','68','neil','2015-05-10','12','mart','2015-04-11','75',
        'neil','2015-06-12','80','mart','2015-04-13','94') as (name, orderdate,cost);

-- unbounded perceding 表示起点  unbounded following 表示终点
select 
  name,
  orderdate,
  cost,
  sum(cost) over() as sample0,
  sum(cost) over(partition by name) as sample1,
  sum(cost) over(order by orderdate) as sample2,
  sum(cost) over(partition by name order by orderdate) as sample3,
  sum(cost) over(partition by name order by orderdate rows between 1 preceding and 1 following) as sample4,
  sum(cost) over(partition by name order by orderdate rows between 1 preceding and unbounded following) as sample5,
  sum(cost) over(partition by name order by orderdate rows between current row and 1 following) as sample6
from test ;

-- 序列函数 不支持windows子句
select
  name,
  orderdate,
  cost,
  ntile(3) over() as sample1,-- 全局数据切片
  ntile(3) over(partition by name) as sample2, -- 按照name分组后再组内切片
  ntile(3) over(order by cost) as smple3,-- 按照cost排序后全局切片
  ntile(3) over(partition by name order by cost) as sample4 -- 按照name分组cost排序后组内切片
from test;

-- row_number  从1开始生成顺序数字
-- rank  从1开始排序，如相同则并列，后续排名跳跃相同部分
-- dense_rank  从1开始排序，如相同则并列，后续排名连续
select
  name,
  cost,
  row_number() over(partition by name order by cost) as rn1,
  rank() over(partition by name order by cost) as rn2,
  dense_rank() over(partition by name order by cost) as rn3
from test;

-- lead 和 lag 取上下N行数据
select 
  name,
  orderdate,
  cost,
  lead(cost,2,0) over(partition by name order by orderdate) as lead_cost,
  lag(cost,2,0) over(partition by name order by orderdate) as lag_cost
from test;

-- first_value和last_value  分组排序后，截止到当前行，第一个和最后一个值
select 
  name,
  orderdate,
  cost,
  first_value(cost) over(partition by name order by orderdate) as first_cost,
  last_value(cost) over(partition by name order by orderdate) as last_cost
from test;

-- cume_dist 分组内小于等于当前值的行数/分组总行数
select 
  name,
  orderdate,
  cost,
  cume_dist() over(partition by name order by orderdate) as pr
from test;

-- percent_rank (分组内当前行rank值-1)/(分组内总行数-1)
select 
  name,
  orderdate,
  cost,
  percent_rank() over(partition by name order by orderdate) as pr
from test;
```

### cube

```sql
-- grouping sets 根据group by的维度的不同组合进行聚合，等价于不同维度的group by结果进行union all
with test as (
    select 'tom' as name,'2020-01' as month,100 as cost 
    union all 
    select 'tom' as name,'2020-01' as month,101 as cost 
    union all 
    select 'tom' as name,'2020-03' as month,105 as cost 
    union all 
    select 'jack' as name,'2020-01' as month,100 as cost 
    union all 
    select 'jack' as name,'2020-01' as month,110 as cost 
    union all 
    select 'jack' as name,'2020-02' as month,150 as cost 
    union all 
    select 'jack' as name,'2020-02' as month,170 as cost 
)
select 
    name,
    month,
    sum(cost) as total_cost,
    grouping__id
from test
group by name,month 
grouping sets(name,month,(name,month))
;

-- cube 根据group by的所有维度组合进行聚合
with test as (
    select 'tom' as name,'2020-01' as month,100 as cost 
    union all 
    select 'tom' as name,'2020-01' as month,101 as cost 
    union all 
    select 'tom' as name,'2020-03' as month,105 as cost 
    union all 
    select 'jack' as name,'2020-01' as month,100 as cost 
    union all 
    select 'jack' as name,'2020-01' as month,110 as cost 
    union all 
    select 'jack' as name,'2020-02' as month,150 as cost 
    union all 
    select 'jack' as name,'2020-02' as month,170 as cost 
)
select 
    name,
    month,
    sum(cost) as total_cost,
    grouping__id
from test
group by name,month 
with cube -- 汇总组合顺序为name,month、name、month、全局
;

-- rollup cube的子集，以最左侧维度为主，从该维度进行层级聚合
with test as (
    select 'tom' as name,'2020-01' as month,100 as cost 
    union all 
    select 'tom' as name,'2020-01' as month,101 as cost 
    union all 
    select 'tom' as name,'2020-03' as month,105 as cost 
    union all 
    select 'jack' as name,'2020-01' as month,100 as cost 
    union all 
    select 'jack' as name,'2020-01' as month,110 as cost 
    union all 
    select 'jack' as name,'2020-02' as month,150 as cost 
    union all 
    select 'jack' as name,'2020-02' as month,170 as cost 
)
select 
    name,
    month,
    sum(cost) as total_cost,
    grouping__id
from test
group by name,month 
with rollup -- 先按name,month汇总，再按name汇总，再全局汇总
;
```

### 排序

```sql
-- 以下为hive中的排序规则
-- order 全局排序
select * from test order by id desc

-- sort redurce组内排序  全局乱序
select * from test sort by id desc

-- distribute by sort by 按key分布后再组内排序，当distribute和sort的key相同时等同于cluster by 
select * from test distribute by name sort by id desc

-- cluster by 不能指定降序，只能默认升序
select * from test cluster by id
```

### 数据抽样

```sql 
-- hive 
-- rand() 随机抽样
select * from table_name where col in ('xx') distribute by rand() sort by rand() limit 10000;
select * from table_name where col in ('xx') order by rand() limit 10000;
-- tablesample() 块抽样
select * from table_name tablesample(10 percent);
select * from table_name tablesample(10000 rows);
select * from table_name tablesample(10M);
-- 分桶抽样 tablesample(bucket x out of y [on colName]) 将表按colName分y桶抽样第x桶
select * from table_name tablesample(bucket 2 out of 8 on rand());

-- oracle 
-- sample 抽样
-- 全表扫表随机抽样20%再随机取前5条
select * from table_name sample(20) where rownum <= 5;
-- 采样扫描20%再随机取前5条
select * from table_name sample block(20) where rownum <= 5;
-- 采样扫描20%再随机取前5条，使用相同seed会返回固定结果集
select * from table_name sample(20) seed(8) where rownum <= 5;
-- dbms_random.value 
select * from (select * from table_name order by dbms_random.value()) where rownum <= 5;

-- mysql 
select * from table_name where col in ('xx') order by rand() limit 10000;
-- 整表抽样10%
select * from table_name where rand() < 0.1
```