## SQL开发基础
无特殊说明均默认为使用mysql环境
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