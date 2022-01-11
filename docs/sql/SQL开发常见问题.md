## SQL开发常见问题
以下题目均出自letcode
### 连续问题
#### 601.体育馆的人流量
```text
表：Stadium
+---------------+---------+
| Column Name   | Type    |
+---------------+---------+
| id            | int     |
| visit_date    | date    |
| people        | int     |
+---------------+---------+
visit_date 是表的主键
每日人流量信息被记录在这三列信息中：序号 (id)、日期 (visit_date)、人流量 (people)
每天只有一行记录，日期随着 id 的增加而增加


编写一个 SQL 查询以找出每行的人数大于或等于 100 且 id 连续的三行或更多行记录。

返回按 visit_date 升序排列的结果表。

查询结果格式如下所示。

Stadium table:
+------+------------+-----------+
| id   | visit_date | people    |
+------+------------+-----------+
| 1    | 2017-01-01 | 10        |
| 2    | 2017-01-02 | 109       |
| 3    | 2017-01-03 | 150       |
| 4    | 2017-01-04 | 99        |
| 5    | 2017-01-05 | 145       |
| 6    | 2017-01-06 | 1455      |
| 7    | 2017-01-07 | 199       |
| 8    | 2017-01-09 | 188       |
+------+------------+-----------+

Result table:
+------+------------+-----------+
| id   | visit_date | people    |
+------+------------+-----------+
| 5    | 2017-01-05 | 145       |
| 6    | 2017-01-06 | 1455      |
| 7    | 2017-01-07 | 199       |
| 8    | 2017-01-09 | 188       |
+------+------------+-----------+
id 为 5、6、7、8 的四行 id 连续，并且每行都有 >= 100 的人数记录。
请注意，即使第 7 行和第 8 行的 visit_date 不是连续的，输出也应当包含第 8 行，因为我们只需要考虑 id 连续的记录。
不输出 id 为 2 和 3 的行，因为至少需要三条 id 连续的记录。
```
```sql
-- 连续问题通用解法，行号-分组行号 = K
-- 注意审题，是ID连续，不是日期连续
select 
    t3.id,
    t3.visit_date,
    t3.people
from 
(
    select 
        t2.id,
        t2.visit_date,
        t2.people,
        count(1) over(partition by t2.id - rown ) as rn
    from 
    (
        select 
            t1.id,
            t1.visit_date,
            t1.people,
            row_number() over(order by t1.id asc)  as rown
        from 
        (
            select 
                s.id,
                s.visit_date,
                s.people
            from stadium s
            where people >= 100
        ) t1
    ) t2 
) t3
where t3.rn >= 3
```
#### 180.连续出现的数字
```text
表：Logs

+-------------+---------+
| Column Name | Type    |
+-------------+---------+
| id          | int     |
| num         | varchar |
+-------------+---------+
id 是这个表的主键。
 

编写一个 SQL 查询，查找所有至少连续出现三次的数字。

返回的结果表中的数据可以按 任意顺序 排列。

查询结果格式如下面的例子所示：

Logs 表：
+----+-----+
| Id | Num |
+----+-----+
| 1  | 1   |
| 2  | 1   |
| 3  | 1   |
| 4  | 2   |
| 5  | 1   |
| 6  | 2   |
| 7  | 2   |
+----+-----+

Result 表：
+-----------------+
| ConsecutiveNums |
+-----------------+
| 1               |
+-----------------+
1 是唯一连续出现至少三次的数字。
```
```sql
-- 行号 - 分组行号 = K
-- 结果去重
select distinct num as ConsecutiveNums from (
select 
    num,
    row_number() over(order by id) as rn,
    row_number() over(partition by num order by id) as nrn
from logs) t 
group by num,rn-nrn having count(1) >=3
```

### 中位数
#### 569.员工薪水中位数
```text
Employee 表包含所有员工。Employee 表有三列：员工Id，公司名和薪水。

+-----+------------+--------+
|Id   | Company    | Salary |
+-----+------------+--------+
|1    | A          | 2341   |
|2    | A          | 341    |
|3    | A          | 15     |
|4    | A          | 15314  |
|5    | A          | 451    |
|6    | A          | 513    |
|7    | B          | 15     |
|8    | B          | 13     |
|9    | B          | 1154   |
|10   | B          | 1345   |
|11   | B          | 1221   |
|12   | B          | 234    |
|13   | C          | 2345   |
|14   | C          | 2645   |
|15   | C          | 2645   |
|16   | C          | 2652   |
|17   | C          | 65     |
+-----+------------+--------+
请编写SQL查询来查找每个公司的薪水中位数。挑战点：你是否可以在不使用任何内置的SQL函数的情况下解决此问题。

+-----+------------+--------+
|Id   | Company    | Salary |
+-----+------------+--------+
|5    | A          | 451    |
|6    | A          | 513    |
|12   | B          | 234    |
|9    | B          | 1154   |
|14   | C          | 2645   |
+-----+------------+--------+
```
```sql
-- 不论奇数偶数，中位数所在行数 = floor((总行数+1)/2) 或者 floor((总行数+2)/2) 
-- percentile 函数可直接计算出中位数
SELECT id,company,salary FROM (
SELECT id,company,salary,
row_number() over(PARTITION BY company ORDER BY salary ASC) AS rn,
COUNT(id) over(PARTITION BY company) AS total_num
FROM Employee) t
WHERE t.rn IN (FLOOR((t.total_num+1)/2),(FLOOR(t.total_num+2)/2))
```
#### 571.给定数字的频率查询中位数
```text
Numbers 表保存数字的值及其频率。

+----------+-------------+
|  Number  |  Frequency  |
+----------+-------------|
|  0       |  7          |
|  1       |  1          |
|  2       |  3          |
|  3       |  1          |
+----------+-------------+
在此表中，数字为 0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 2, 3，所以中位数是 (0 + 0) / 2 = 0。

+--------+
| median |
+--------|
| 0.0000 |
+--------+
请编写一个查询来查找所有数字的中位数并将结果命名为 median 。
```
```sql 
-- 数字排序后，中位数大于从前往后累加频率以及从后往前累加频率
SELECT 
    ROUND(AVG(num),4) AS median
FROM 
(
    SELECT 
        num,
        frequency,
        SUM(frequency) over(ORDER BY num ASC) AS asc_num,
        SUM(frequency) over(ORDER BY num DESC) AS desc_num,
        SUM(frequency) over() AS total
    FROM numbers
) t1 
WHERE t1.asc_num >= total / 2 
AND t1.desc_num >= total / 2 
```