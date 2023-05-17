ALTER TABLE account_budget
DROP CONSTRAINT IF EXISTS budget_unique_key;

ALTER TABLE account_budget
ADD CONSTRAINT budget_unique_key UNIQUE (code,parent_budget_level,from_date,to_date);

ALTER TABLE budget_budget_level
DROP CONSTRAINT IF EXISTS budget_level_unique_key;

ALTER TABLE budget_budget_level
ADD CONSTRAINT budget_level_unique_key UNIQUE (code,parent_id,from_date,to_date);