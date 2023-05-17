UPDATE budget_budget_level SET parent_id = '0' WHERE parent_budget_level IS NULL;
UPDATE budget_budget_level SET parent_id = parent_budget_level WHERE parent_budget_level IS NOT NULL;