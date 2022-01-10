## SQL开发常见问题
### 连续问题

```sql
-- Logs 表：
-- +----+-----+
-- | Id | Num |
-- +----+-----+
-- | 1  | 1   |
-- | 2  | 1   |
-- | 3  | 1   |
-- | 4  | 2   |
-- | 5  | 1   |
-- | 6  | 2   |
-- | 7  | 2   |
-- +----+-----+
-- 
-- Result 表：
-- +-----------------+
-- | ConsecutiveNums |
-- +-----------------+
-- | 1               |
-- +-----------------+
-- 1 是唯一连续出现至少三次的数字
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
```sql
-- letcode 569
-- 重点规律：不论奇数偶数，中位数所在行数 = floor((总行数+1)/2) 或者 floor((总行数+2)/2) 
-- percentile 函数可直接计算出中位数
SELECT id,company,salary FROM (
SELECT id,company,salary,
row_number() over(PARTITION BY company ORDER BY salary ASC) AS rn,
COUNT(id) over(PARTITION BY company) AS total_num
FROM Employee) t
WHERE t.rn IN (FLOOR((t.total_num+1)/2),(FLOOR(t.total_num+2)/2))
-- letcode 571
-- 重点逻辑，数字排序后，中位数大于从前往后累加频率以及从后往前累加频率
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