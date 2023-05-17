ALTER TABLE account_analytic_distribution_line DROP COLUMN budget;
ALTER TABLE account_analytic_distribution_line DROP COLUMN analyticJournal;

DELETE FROM meta_view where name='budget-analytic-distribution-line-grid';
DELETE FROM meta_view where name='budget-analytic-distribution-line-form';