UPDATE budget_budget_level bl set company = (
	select company from budget_budget_level lvl
	where level_type_select LIKE 'global' AND bl.parent_budget_level = lvl.id) where level_type_select LIKE 'group';
	
UPDATE budget_budget_level bl set company = (
	select company from budget_budget_level lvl
	where level_type_select LIKE 'group' AND bl.parent_budget_level = lvl.id) where level_type_select LIKE 'section';
	
UPDATE account_budget b set company = (
	select company from budget_budget_level lvl
	where level_type_select LIKE 'section' AND b.budget_level = lvl.id);