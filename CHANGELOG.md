## [8.3.18] (2025-10-23)

### Fixes
#### Base

* API SIREN : Fixed error encountered during the data retrieval process for a specific SIRET.
* API SIREN : Change the default sireneUrl.

#### Account

* Invoice/InvoiceTerm: Fixed the change of invoice term's due date after invoice's due date change on a free payment condition.
* Move line: correct number of currency decimal scale in move line grid.
* Accounting report: fixed Aged balance report to use invoice payments instead of invoice terms for accurate balance.
* Move group: Fixed incorrect assignment to pfpValidateStatusSelect.
* Invoice : fixed unpaid filter for advance payment invoices.
* INVOICE / DEBTRECOVERY : Invoice linked to a move having ignoreInDebtRecoveryOk as true are now ignored.
* MOVE/MOVELINE:fixed advanced filter not displayed unless the whole page is refreshed
* PARTNER/PAYMENTCONDITION : Set accounting config default payment condition on partners.
* ACCOUNT : Correct display condition on vatSystemSelect and isTaxRequireOnMoveLine for tax type accounts.

#### Bank Payment

* BankOrder: fixed the technical error when the bank order is not created with all fields due to wrong import.

#### Business Project

* BUSINESSPROJECT : Fixed NPE when emptying the partner on a business project.

#### CRM

* Opportunity : set a default value for team and company and add filter for the team and the assignee user

#### Human Resource

* Expense Line: restrict displayed tasks to 'In progress' projects only.

#### Production

* Manuf Order: fixed issue where parent mo was not filled correctly on multi level planning.

#### Purchase

* Mrp: fixed notes to display on the purchase order are not automatically filled.

#### Sale

* Sale order : fixed end of pack line placement in sale order report.
* Sale order : fixed SubTotal cost price doesn't take into account the qty.
* Sale order : fixed an issue where timetable lines were not automatically emptied on sale order cancellation.

#### Stock

* Stock move: fixed display issue in form view.
* Stock dashboard: deliveries dashboards are now filtered on virtual stock location.

#### Supply Chain

* Sale order / Advance payment: fixed the advance payment amount at wizard opening.
* Order/AvancePayment: fixed the advance payment amount from sale/purchase order
* INVOICE : Set interco as true when generating invoice from an interco saleOrder / purchaseOrder.


### Developer

#### Base

UPDATE studio_app_base SET sirene_url = 'https://api.insee.fr/api-sirene/3.11';

#### Bank Payment

Added BankOrderCheckService in the BankOrderCreateService constructor. Added BankOrderCheckService in the BankOrderCreateServiceHr constructor. Changed the BankOrderCheckService.checkPreconditions(BankOrder bankOrder) in checkPreconditions(PaymentMode paymentMode, Integer partnerType, LocalDate bankOrderDate, Company senderCompany, BankDetails senderBankDetails)

#### Sale

- SaleOrderLineCostPriceComputeServiceImpl consturctor is updated to introduce SaleOrderLineProductService

---

- SaleOrderWorkflowServiceSupplychainImpl constructor is updated to introduce SaleOrderSupplychainService

#### Supply Chain

Removed CommonInvoiceService.createInvoiceLinesFromOrder Changed the parameter of PurchaseOrderInvoiceService.createInvoiceAndLines from (PurchaseOrder,List<PurchaseOrderLineTax>,Product,BigDecimal,int,Account) to (PurchaseOrder,Product,BigDecimal,int,Account) Changed the parameter of PurchaseOrderInvoiceService.createInvoiceLines from (Invoice,List<PurchaseOrderLineTax>,Product,BigDecimal) to (Invoice,List<PurchaseOrderLine>,Product,BigDecimal) Changed the parameter of InvoiceLineOrderService.getInvoiceLineGeneratorWithComputedTaxPrice from (Invoice,Product,BigDecimal,OrderLineTax,SaleOrderLine,PurchaseOrderLine) to (Invoice,Product,BigDecimal,SaleOrderLine,PurchaseOrderLine,BigDecimal,Set<TaxLine>) Changed the parameter of SaleOrderInvoiceService.createInvoiceAndLines from (SaleOrder,List<SaleOrderLineTax>,Product,BigDecimal,int,Account) to (SaleOrder,Product,BigDecimal,int,Account) Changed the parameter of SaleOrderInvoiceService.createInvoiceLines from (Invoice,List<SaleOrderLineTax>,Product,BigDecimal) to (Invoice,List<SaleOrderLine>,Product,BigDecimal)

## [8.3.17] (2025-10-09)

### Fixes
#### Base

* Product: fixed prices of generated company product variant .
* Abc analysis: fixed color on chart same as table.
* Price list: fixed price list filter in grid views.
* Update axelor utils dependency to 3.4.4

#### Account

* AnalyticMoveLine: Fixed the amount recompute when changing percentage.
* FEC Import : No longer remove test after '-' from move reference.
* Move: fixed issues where generating a counterpart without saving caused ghost lines, errors, or lost move lines.
* Fixed asset: corrected accounting value calculation with imported depreciation amounts.
* Invoice: Advanced invoice are not generated twice anymore when isVentilationSkipped is activated.
* ANALYTIC: Updated analytic journal select domain method.
* AccountingBatch/CuttOff : fixed the analytic management in cut off batch.

#### Bank Payment

* Move : fixed bank reconciled amount set when we don't have a linked bank reconciliation.
* BANKRECONCILIATION / BANKRECONCILIATIONLINE / BANKSTATEMENT : Now includeOtherBankStatements option only retrieve unreconciled statement lines from the past.

#### Budget

* Budget : fixed the missing compute of all fields after budget line changes
* GlobalBudget/BudgetLevel/Budget : update all amounts when updating budget lines values
* Budget/Invoice/Move/Order : change the budget distribution amount in mono budget when changing line price

#### Human Resource

* Leave Request: Fixed the duration when the 'To Date' field is empty.
* ExpenseLine : Remove action who doesn't exist

#### Mobile Settings

* App mobile settings: fixed deleted modules in web are bugged in mobile.

#### Production

* Manuf order: managed partial consumption in manuf order from consumption button too.
* Bill of material line: correct domain of product field in bill of material line grid.
* Operation order: fixed display condition of 'To invoice' to show only when 'Manage business production' is enable in app manufacturing.

#### Purchase

* Purchase order: fixed error when the supplier is cleared.

#### Quality

* Quality: fixed French translation in Quality improvement form view.

#### Sale

* Sale order: fixed error when customer is cleared.
* Sale order: fixed broken sale order printing when level is not set in sale order lines.

#### Stock

* Product: fixed Stock and Stock history charts to exclude cancelled stock moves and take real qty into account.
* Stock move: fixed shipping coef not working on supplier arrival having purchase order.
* Product: fixed quantity in stock report with correctly converted unit.

#### Supply Chain

* SaleOrder/Analytic : fixed the blocking of sale order when analytic template is required and there is no values
* Batch stock rules: fixed an issue where data could be skipped.

#### Intervention

* Duration: added missing intervention application type in selection.


### Developer

#### Account

DELETE FROM meta_action where name ='action-attrs-domain-onselect-journal-analytic-move-line';

---

Added AnalyticMoveLineRepository in MoveLineComputeAnalyticServiceImpl constructor. Added copyAnalyticsDataFromMoveLine method in MoveLineComputeAnalyticService.

#### Budget

DELETE FROM meta_action WHERE name = 'action-budget-budget-compute-amounts';

---

Added BudgetDistributionComputeService in PurchaseOrderLineGroupBudgetServiceImpl constructor. Added BudgetDistributionComputeService in BudgetInvoiceLineComputeServiceImpl constructor. Added BudgetDistributionComputeService in MoveLineGroupBudgetServiceImpl constructor. Added BudgetDistributionComputeService in SaleOrderLineComputeBudgetServiceImpl constructor. Change the PurchaseOrderLineService.compute to return a Map<String, Object> and not a Map<String, BigDecimal> anymore

#### Supply Chain

Added AnalyticToolSupplychainService in SaleOrderFinalizeSupplychainServiceImpl constructor. Added AnalyticToolSupplychainService in SaleOrderFinalizeBudgetServiceImpl constructor.

## [8.3.16] (2025-09-25)

### Fixes
#### Base

* User: fixed trading name not filtered on company.
* Partner: fixed error when selecting price list and the partner was not saved.
* Update Axelor Utils to 3.4.2
* API Siren: updated current version.

#### Account

* Deposit slip: fixed the date filter to use the cheque's due date rather than the deposit slip date.
* Move line: added an error message when trying to reconcile moves from incompatible accounts.
* Partner: fixed French translation for 'Unpaid invoices' and 'View all unpaid invoices'.
* Fixed asset: allow a number of depreciation of 0.
* Payment scheduler: fixed an issue where records were created without a sequence.
* Fixed asset: fixed fixed asset creation from invoice.
* ACCOUNT : Fix status mass update
* Accounting report type: fixed company sync between M2O and M2M fields.
* Payment schedule: fixed an issue where values were not reset during duplication.
* Accounting report: fixed domain filter for analytic accounts and analytic axis.
* Deposit slip: fixed display condition for deposit slip date.
* Invoice: fixed an issue where financial discount was not emptied when changing partner.

#### Business Project

* Project task: fixed estimated time modification should not update the sold time.
* Project task: fixed sold time is not set when the task is linked to a task template without a product.

#### Contract

* Contract line: fixed display issue of 'Invoice from consumption' field.
* Contract line: fixed domain for product based on product per company.

#### CRM

* Lead: prevent users from reopening converted lead in kanban view.
* Event: fixed duration computation when creating a new event.

#### Production

* Production API : fixed request to fetch consumed products was not working when 'Manage consumed products on operations' is enabled.

#### Project

* Project: fixed an issue where the parent task was not assigned when clicking the create sub task button.

#### Sale

* Sale config: client box and legal note fields are now translatable.
* Sale order: fixed the sub sale order lines when merging the multiple sale orders.

#### Supply Chain

* Sale order: fixed delivery state computation to ignore sale order lines with qty 0.
* MRP: fixed an error occurring with manufacturing order in certain cases.

#### Talent

* Sequence: added missing selection value for job position.


### Developer

#### Base

Removed 'PartnerPriceListRepository' from 'PartnerPriceListDomainServiceImpl' constructor.

---

ALTER TABLE studio_app_base DROP COLUMN IF EXISTS sirene_token_generator_url;
ALTER TABLE studio_app_base DROP COLUMN IF EXISTS sirene_secret;
ALTER TABLE studio_app_base DROP COLUMN IF EXISTS sirene_access_token;

DELETE from meta_field
WHERE name IN ('sireneTokenGeneratorUrl', 'sireneSecret', 'sireneAccessToken')
  AND meta_model = (SELECT id FROM meta_model mm WHERE mm.full_name = 'com.axelor.studio.db.AppBase');

UPDATE studio_app_base SET sirene_url = 'https://api.insee.fr/api-sirene/3.11';

#### Account

Removed action that is now useless since it will be replaced by the repository save.

DELETE FROM meta_action WHERE name = 'action-payment-schedule-payment-schedule-id';

---

For AccountingReportTypes with `typeSelect != 3000`, the company M2O field is now synced
into the company M2M field. Existing inconsistent data should be corrected using the script below. 
-- Cleanup existing M2M entries for typeSelect != 3000
DELETE FROM account_accounting_report_type_company_set
WHERE account_accounting_report_type IN (
    SELECT id FROM account_accounting_report_type
    WHERE type_select != 3000
);
-- Insert new M2M entries based on the M2O value
INSERT INTO account_accounting_report_type_company_set (account_accounting_report_type, company_set)
SELECT art.id AS account_accounting_report_type, art.company AS company_set
FROM account_accounting_report_type art
WHERE art.type_select != 3000
  AND art.company IS NOT NULL;

## [8.3.15] (2025-09-11)

### Fixes
#### Base

* Partner: fixed accounting situations when merging partners.
* Databackup: fixed a potential security breach when restoring a backup.

#### Account

* Account: removed export button as it has no action linked.
* Fixed asset: fixed an issue where periodicity type was not copied if fiscal plan was not selected.
* Accounting report: fixed detailed customers balance report to exclude suppliers and supplier invoices.
* Accounting report type: fixed domain filter on accounting report and correct demo data for custom type.

#### Budget

* Budget app: fixed an issue on app installation.

#### Business Project

* Business project: added a closing control on 'Finished paid' status
* ProjectTask: fixed time unit conversion issue after computing project totals.
* Business project: fixed the closing rule condition

#### Human Resource

* Expense : disable the multi currency management until 8.5
* Lunch voucher: fixed an issue where computation did not deduct ventilated or reimbursed expenses.


### Developer

#### Business Project

Added UnitConversionForProjectService in ProjectTimeUnitServiceImpl constructor

---

Replace SaleOrderRepository by SaleOrderLineRepository in BusinessProjectClosingControlServiceImpl constructor Replace PurchaseOrderRepository by PurchaseOrderLineRepository in BusinessProjectClosingControlServiceImpl constructor Change BusinessProjectClosingControlServiceImpl.areSaleOrdersFinished to BusinessProjectClosingControlServiceImpl.areSaleOrderLinesFinished Change BusinessProjectClosingControlServiceImpl.arePurchaseOrdersInvoiced to BusinessProjectClosingControlServiceImpl.arePurchaseOrderLinesInvoiced Change BusinessProjectClosingControlServiceImpl.arePurchaseOrdersReceived to BusinessProjectClosingControlServiceImpl.arePurchaseOrderLinesReceived

#### Human Resource

If you have some expense with another currency than the company currency, you will need a script to reset it.

Script : 
UPDATE hr_expense e SET currency = c.currency
FROM base_company c WHERE c.id = e.company AND e.currency != c.currency;
DELETE FROM meta_action WHERE name = 'action-expense-attrs-kilometric-panel-visibility';

## [8.3.14] (2025-08-28)

### Fixes
#### Account

* Accounting report Analytic balance: fixed issues in Excel export.
* Move : fix technical error when accounting an empty move
* Accounting report Partner balance: fixed issues in Excel export.
* Accounting report Aged Balance: fixed issues in Excel export.
* Accounting report VAT Statement on amount received: fixed issues in Excel export.
* Accounting report Invoices which are due and unpaid: fixed issues in Excel export.
* Accounting report Payment differences: fixed issues in Excel export.
* Accounting report VAT Statement on invoices: fixed issues in Excel export.
* Accounting report Fees declaration supporting file: fixed issues in Excel export.
* Accounting report Detailed customers balance: fixed issues in Excel export.
* Accounting report General balance: fixed issues in the Excel export.
* Accounting report Analytic general ledger: fixed issues in Excel export.
* Accounting report Cash payments summary: fixed issues in Excel export.
* Accounting report Preparatory declaration DGI 2055 and Invoices with payment delay: fixed issues in Excel export.
* Accounting report : fixed domain on report type by adding company information.
* Accounting report General ledger: fixed issues in Excel export.
* AnalyticDistributionTemplate : Remove the possibility of selecting duplicate on analytic distribution template fields.
* Accounting report Partner general ledger: fixed issues in Excel export.
* Accounting report Preparatory Process for fees declaration: fixed issues in Excel export.
* Accounting report Custom state: fixed issues in Excel export.
* Accounting report Summary of gross values and depreciation: fixed issues in Excel export.
* Accounting report Acquisitions: fixed issues in Excel export.
* Accounting report Cheque deposit slip: fixed issues in Excel export.
* Accounting report General ledger (old presentation): fixed issues in Excel export.
* Accounting report Preparatory declaration DGI 2054: fixed issues in Excel export.
* Accounting report Journal: fixed issues in Excel export.

#### Budget

* GlobalBudget: fixed the remove of budget level when it is containing budget
* GlobalBudget/BudgetLevel/Budget : update amounts when updating budget lines values

#### CRM

* Lead: lead partner are now always prospect when converted.

#### Human Resource

* Expense: mail notification are now correctly sent when sending, validating or refusing an expense from API.

#### Production

* Manuf Order: fixed an issue that occured when manually removing a produced product with a tracking number.
* Manufacturing order: fixed an issue where the purchase order date was not set when generated automatically during planning.
* Cost calculation: fixed an issue where the cost price was not divided by the calculation quantity when the BOM quantity was greater than 1.
* Manufacturing order: fixed an issue where an outsourced product's price from purchase order was not correctly priced in cost sheets.
* Cost calculation: fixed an issue where the child BOM quantity was not correctly multiplied when the BOM calculation quantity was greater than 1.

#### Sale

* Sales order: fixed status filter on 'My Sales Orders' dashboard.
* Sale order: fixed price recomputation when editable tree is enabled.

#### Stock

* Stock move: fixed an error when clicking 'Refresh the products net mass' without saving the record.

#### Supply Chain

* PurchaseOrderLine/Analytic: fixed an issue where the analytic was required on a title line.
* Sale order: fixed wrong check on payment mode when changing partner.
* Sale order: prevent already invoiced lines to be invoiced again.
* Stock move: removed the toolbar from the 'Mass Stock Move Invoicing' wizard views.


### Developer

#### Account

DELETE FROM meta_action where name = 'action-accounting-report-record-empty-report-type';

#### Production

Method signature change: the `qtyRatio` parameter was removed from the 
`createUnitCostCalcLine` method in `UnitCostCalcLineServiceImpl`.

#### Sale

Added SubSaleOrderLineComputeService to SaleOrderCreateServiceImpl constructor.

## [8.3.13] (2025-08-14)

### Fixes
#### Account

* Closure assistant : fixed outrun of year computation doesn't take into account all accountTypes.
* Account management: fixed interbank code issue on 'Direct Debit' payment mode.
* Accounting report: fixed the issue related to amount in Analytic general ledger report.
* ANALYTICDISTRIBUTIONTEMPLATE : duplicated templates shouldn't be visible

#### Bank Payment

* Bank statement: fixed demo data to get dynamic dates and corrected interbank code.
* Bank statement rule: fixed partner fetch method demo data.

#### Budget

* Invoice/Move/Budget : Realized amounts needs to be computed with movelines datas
* MoveLine/Budget : fix budget distribution compute at budget change

#### Business Project

* PurchaseOrder : fixed technical error when saving a project or a business project on a purchase order.

#### Human Resource

* Expense: fixed display cancel button in form view.
* Timesheet: fixed error when generating lines from planning with custom time units

#### Production

* CostSheet: fixed issue where cost related to subcontractor did not appear the in cost sheets

#### Stock

* Product: fixed unit conversion for 'Stock history' chart.

#### Supply Chain

* Sale order: fixed stock location on change of company.

#### Intervention

* Equipment line: fixed the display of the tracking number on the form view.


### Developer

#### Account

Changed the AccountService.computeBalance method parameter. Now using a list of account types instead of an account type.

---

Added AnalyticDistributionTemplateRepository and AnalyticMoveLineService in AnalyticAttrsServiceImpl.
Added AnalyticAttrsService in MoveLineAttrsServiceImpl.
Added parameter 'moveline' in MoveLineAttrsServiceImpl.addAnalyticDistributionTemplateDomain.
Added parameter 'moveLine' in MoveLineGroupServiceImpl.getAnalyticDistributionTemplateOnSelectAttrsMap.

DELETE FROM meta_action WHERE name LIKE 'action-purchase-order-line-attrs-set-domain-analytic-distribution-template';

#### Budget

Delete updateBudgetLineAmounts and updateBudgetLineAmountWithPo from BudgetService
 Delete updateBudgetLineAmounts from BudgetLineComputeService
 Delete updateBudgetLinesFromInvoice and updateLineAmounts from BudgetInvoiceService
 Delete WorkflowCancelBudgetServiceImpl and WorkflowVentilationBudgetServiceImpl

 If you have manually changed amounts on some budget distribution on daybook moves related to invoices, you will need this script to recompute all amounts :

  UPDATE budget_budget_line bl SET realized_with_po = (SELECT COALESCE(SUM(bd.amount))
  FROM budget_budget_distribution bd
  JOIN budget_budget b ON b.id = bd.budget
  JOIN account_move_line ml ON ml.id = bd.move_line
  JOIN account_move m ON m.id = ml.move
  JOIN account_invoice i ON m.invoice = i.id
  WHERE bl.budget = b.id AND bd.move_line IS NOT NULL AND (i.purchase_order IS NOT NULL OR i.sale_order IS NOT NULL) AND bl.from_date < m.date_val AND bl.to_date >= m.date_val);
  
  UPDATE budget_budget_line bl SET realized_with_no_po = (SELECT COALESCE(SUM(bd.amount))
  FROM budget_budget_distribution bd
  JOIN budget_budget b ON b.id = bd.budget
  JOIN account_move_line ml ON ml.id = bd.move_line
  JOIN account_move m ON m.id = ml.move
  WHERE bl.budget = b.id AND bd.move_line IS NOT NULL AND bl.from_date < m.date_val AND bl.to_date >= m.date_val) - bl.realized_with_po;
  
  UPDATE budget_budget_line bl SET amount_committed = (SELECT COALESCE(SUM(bd.amount))
  FROM budget_budget_distribution bd
  JOIN budget_budget b ON b.id = bd.budget
  JOIN sale_sale_order_line sl ON sl.id = bd.sale_order_line
  JOIN sale_sale_order s ON s.id = sl.sale_order
  WHERE bl.budget = b.id AND bd.sale_order_line IS NOT NULL
  AND bl.from_date < s.order_date AND bl.to_date >= s.order_date
  AND (s.status_select = 3 OR s.status_select = 4)) - bl.realized_with_po;
  
  UPDATE budget_budget_line bl SET amount_committed = (SELECT COALESCE(SUM(bd.amount))
  FROM budget_budget_distribution bd
  JOIN budget_budget b ON b.id = bd.budget
  JOIN purchase_purchase_order_line pl ON pl.id = bd.purchase_order_line
  JOIN purchase_purchase_order p ON p.id = pl.purchase_order
  WHERE bl.budget = b.id AND bd.purchase_order_line IS NOT NULL
  AND bl.from_date < p.order_date AND bl.to_date >= p.order_date
  AND (p.status_select = 3 OR p.status_select = 4)) - bl.realized_with_po;
  
  UPDATE budget_budget_line bl SET amount_realized = realized_with_po + realized_with_no_po,
  available_amount = amount_expected - realized_with_no_po - realized_with_po,
  to_be_committed_amount = amount_expected - amount_committed;
  
  UPDATE budget_budget_line SET firm_gap = 0 WHERE available_amount > 0;
  UPDATE budget_budget_line SET firm_gap = -available_amount, available_amount = 0 WHERE available_amount < 0;
  UPDATE budget_budget_line SET to_be_committed_amount = 0 WHERE to_be_committed_amount < 0;
  
  UPDATE budget_budget b SET total_amount_committed = agg.totalAmountCommitted,
  total_amount_realized = agg.totalAmountRealized,
  available_amount = agg.availableAmount,
  realized_with_no_po = agg.realizedWithNoPo,
  realized_with_po = agg.realizedWithPo,
  total_firm_gap = agg.totalFirmGap
  FROM (SELECT bl.budget,
  SUM(bl.amount_committed) AS totalAmountCommitted,
  SUM(bl.amount_realized) AS totalAmountRealized,
  SUM(bl.available_amount) AS availableAmount,
  SUM(bl.realized_with_no_po) AS realizedWithNoPo,
  SUM(bl.realized_with_po) AS realizedWithPo,
  SUM(bl.firm_gap) AS totalFirmGap
  FROM budget_budget_line bl
  GROUP BY bl.budget
  ) agg WHERE b.id = agg.budget;
  
  UPDATE budget_budget_level bl SET total_amount_committed = agg.totalAmountCommitted,
  total_amount_realized = agg.totalAmountRealized,
  total_amount_available = agg.availableAmount,
  realized_with_no_po = agg.realizedWithNoPo,
  realized_with_po = agg.realizedWithPo,
  total_firm_gap = agg.totalFirmGap
  FROM (SELECT b.budget_level,
  COUNT(*) AS countBudget,
  SUM(b.total_amount_committed) AS totalAmountCommitted,
  SUM(b.total_amount_realized) AS totalAmountRealized,
  SUM(b.available_amount) AS availableAmount,
  SUM(b.realized_with_no_po) AS realizedWithNoPo,
  SUM(b.realized_with_po) AS realizedWithPo,
  SUM(b.total_firm_gap) AS totalFirmGap
  FROM budget_budget b
  GROUP BY b.budget_level
  ) agg WHERE bl.id = agg.budget_level AND countBudget > 0;
  
  UPDATE budget_budget_level parent SET total_amount_committed = agg.totalAmountCommitted,
  total_amount_realized = agg.totalAmountRealized,
  total_amount_available = agg.availableAmount,
  realized_with_no_po = agg.realizedWithNoPo,
  realized_with_po = agg.realizedWithPo,
  total_firm_gap = agg.totalFirmGap
  FROM (SELECT child.parent_budget_level,
  COUNT(*) AS countBudget,
  SUM(child.total_amount_committed) AS totalAmountCommitted,
  SUM(child.total_amount_realized) AS totalAmountRealized,
  SUM(child.total_amount_available) AS availableAmount,
  SUM(child.realized_with_no_po) AS realizedWithNoPo,
  SUM(child.realized_with_po) AS realizedWithPo,
  SUM(child.total_firm_gap) AS totalFirmGap
  FROM budget_budget_level child
  GROUP BY child.parent_budget_level
  ) agg WHERE parent.id = agg.parent_budget_level AND countBudget > 0;
  
  UPDATE budget_global_budget gb SET total_amount_committed = agg.totalAmountCommitted,
  total_amount_realized = agg.totalAmountRealized,
  total_amount_available = agg.availableAmount,
  realized_with_no_po = agg.realizedWithNoPo,
  realized_with_po = agg.realizedWithPo,
  total_firm_gap = agg.totalFirmGap
  FROM (SELECT b.global_budget,
  SUM(b.total_amount_committed) AS totalAmountCommitted,
  SUM(b.total_amount_realized) AS totalAmountRealized,
  SUM(b.available_amount) AS availableAmount,
  SUM(b.realized_with_no_po) AS realizedWithNoPo,
  SUM(b.realized_with_po) AS realizedWithPo,
  SUM(b.total_firm_gap) AS totalFirmGap
  FROM budget_budget b
  GROUP BY b.global_budget
  ) agg WHERE gb.id = agg.global_budget;

---

Add MoveLineToolBudgetService in MoveBudgetDistributionServiceImpl constructor.
 Add MoveLineToolBudgetService in MoveLineBudgetServiceImpl constructor
 Add a new boolean as parameter in MoveBudgetDistributionService.checkChanges

#### Human Resource

`TimesheetProjectPlanningTimeServiceImpl` now has two new constructor parameters to support `UnitConversions`.

## [8.3.12] (2025-07-31)

### Fixes
#### Base

* Event: creating an event now automatically fill the user field.

#### Account

* PaymentSession/Refund : fixed the fact that refund were not being taken into account during the payment session
* FIXEDASSET : Issue with imported fixed assets using prorata temporis
* MoveLine: fixed invoice term due date when we update move line's due date with free payment condition.

#### Bank Payment

* Bank payment: removed some files that are not necessary anymore.

#### Budget

* Budget: fixed the 'Display realized with no po' button filter

#### Business Project

* App business project: updated help of options under display panel

#### Contract

* Contract: fixed NPE when creating a contract line from contract templates.
* Contract batch: fixed an issue on customer contract invoicing when grouped invoicing was false.

#### Human Resource

* Project task: reload project task view after time sheet line creation.

#### Production

* Manufacturing Order: fixed NPE of multi-level planning
* WorkCenter: removed old references of product and hrProduct

#### Project

* Project : fixed performance technical issue on project action-attrs

#### Purchase

* PurchaseOrder : Filter partner selection according to purchase order currency

#### Sale

* Sale order:  fixed the issue where partner complementary product lines were being duplicated with each new version creation.
* Sale order: fixed the default value of the printing template for sale order report wizards when generating reports.
* Sale order: refresh origin sale order when splitting lines.
* Sale order: fixed pack currency conversion.
* Sale order: sub total cost price can now be changed when not using tree grid.
* Sale order: fixed French translation on the customer deliveries button.

#### Stock

* Stock move: fixed the title of Generate invoice button.
* Tracking number: fixed the fields that were not displayed in wizard.
* Stock Move: removed 'invoiced' status on internal stock moves.
* Product: fixed 'Stock history' chart.
* Stock move: moved stock move line menu under 'Stock' menu.

#### Supply Chain

* Sale order: fixed an issue where some informations were not filled when generating a sale order from a purchase order.
* Timetable: removed unnecessary check when changing amount.
* Product: fixed wrong calculation of available stock when the 'Manage stock reservation' configuration is enabled.
* Sale order: fixed invoiced partner and delivery partner when creating a sale order from interco, taking into account the partner relations.
* Purchase order: fixed interco configuration when merging purchase orders.

#### Intervention

* Equipment: fixed the duplicated equipment display on the tree view.
* Equipment: removed the default value on 'customerWarrantyOnPartEndDate' to keep it empty on new.


### Developer

#### Account

Changed the InvoiceTermService.checkIfCustomizedInvoiceTerms parameter. Now using a list of invoice terms instead of an invoice.
New method in InvoiceTermService, called computeInvoiceTermsDueDates used to recompute the invoice terms due dates when 
changing the move line's due date on a free payment condition.
New method in InvoiceTermService, called recomputeFreeDueDates used to recompute the invoice terms due dates.

#### Business Project

Migration script
```
UPDATE meta_help SET help_value = 'By enabling this option, you will display the purchase order lines attached to a business project in the financial datas of a business project.' WHERE field_name = 'showPurchaseOrderLineRelatedToProject' and meta_help.language = 'en';
UPDATE meta_help SET help_value = 'By enabling this option, you will display the sale order lines attached to a business project in the financial datas of a business project.' WHERE field_name = 'showSaleOrderLineRelatedToProject' and meta_help.language = 'en';
UPDATE meta_help SET help_value = 'By enabling this option, you will display the production orders attached to a business project in the financial datas of a business project.' WHERE field_name = 'showProductionOrderRelatedToProject' and meta_help.language = 'en';
UPDATE meta_help SET help_value = 'By enabling this option, you will display the expenses attached to a business project in the financial datas of a business project.' WHERE field_name = 'showExpenseLineRelatedToProject' and meta_help.language = 'en';
UPDATE meta_help SET help_value = 'By enabling this option, you will display the purchase invoice lines attached to a business project in the financial datas of a business project.' WHERE field_name = 'showPurchaseInvoiceLineRelatedToProject' and meta_help.language = 'en';
UPDATE meta_help SET help_value = 'By enabling this option, you will display the sale invoice lines attached to a business project in the financial datas of a business project.' WHERE field_name = 'showSaleInvoiceLineRelatedToProject' and meta_help.language = 'en';

UPDATE meta_help SET help_value = 'En activant cette option, vous afficherez les lignes de commandes fournisseurs rattachées à une affaire dans les données financières d''une affaire.' WHERE field_name = 'showPurchaseOrderLineRelatedToProject' and meta_help.language = 'fr';
UPDATE meta_help SET help_value = 'En activant cette option, vous afficherez les lignes de commandes clients rattachées à une affaire dans les données financières d''une affaire.' WHERE field_name = 'showSaleOrderLineRelatedToProject' and meta_help.language = 'fr';
UPDATE meta_help SET help_value = 'En activant cette option, vous afficherez les ordres de production rattachés à une affaire dans les données financières d''une affaire.' WHERE field_name = 'showProductionOrderRelatedToProject' and meta_help.language = 'fr';
UPDATE meta_help SET help_value = 'En activant cette option, vous afficherez les notes de frais rattachées à une affaire dans les données financières d''une affaire.' WHERE field_name = 'showExpenseLineRelatedToProject' and meta_help.language = 'fr';
UPDATE meta_help SET help_value = 'En activant cette option, vous afficherez les lignes de factures fournisseurs rattachées à une affaire dans les données financières d''une affaire.' WHERE field_name = 'showPurchaseInvoiceLineRelatedToProject' and meta_help.language = 'fr';
UPDATE meta_help SET help_value = 'En activant cette option, vous afficherez les lignes de factures clients rattachées à une affaire dans les données financières d''une affaire.' WHERE field_name = 'showSaleInvoiceLineRelatedToProject' and meta_help.language = 'fr';
```

#### Sale

Added new PackLine parameter to SaleOrderLinePackService#fillPriceFromPackLine, SaleOrderLinePackService#getExTaxUnitPriceFromPackLine and SaleOrderLinePackService#getInTaxUnitPriceFromPackLine.
Added new Currency parameter to SaleOrderLinePackServiceImpl#getUnitPriceFromPackLine.

#### Supply Chain

Added the SaleOrderSupplychainService in the IntercoServiceImpl constructor

## [8.3.11] (2025-07-18)

### Fixes
#### Base

* Product: fixed products per companies to be copied during duplication.
* Price list line: fixed the display issue with the 'Amount' title.
* App Base: fixed French translation for some configuration settings.
* Siren API: fixed message when having wrong credentials.

#### Account

* INVOICE : Wrong title displayed on PDF for a credit note issued on an advance payment.
* Partner/AccountingSituation : Correct anomaly causing account fields to be empty when generating automatically a new accounting situation on the partner (when adding a new company)
* AccountingBatch/BillOfExchange : Fixed the bill of exchange data type select.
* Invoice: fixed an issue where the payment date was not emptied when duplicate or generate from a paid invoice.
* Accounting report: fixed moveStatusSelect filter display issue after a status is being selected.

#### Bank Payment

* Invoice/BillOfExchange: fixed the fact the placement move was a Sale move and not a Payment move

#### Business Project

* Sale order: removed a duplicated extension in business project module.

#### Contract

* Contract/Invoice/Credit Note : fixed the issue where the invoiced amount of the contract was positive even for credit note.

#### Human Resource

* Expense: fixed an error when emptying employee

#### Production

* Cost Calculation : Wrong calculation when Calculation quantity from the BOM is > 1
* Prod Process: fixed NPE during creation of a phases
* Sales and Operations Planning: added missing title on 'Generate MPS forecasts' wizard
* Sale order: sublines are now correctly personalized.
* MRP: fixed an issue where bill of material line marked as not stock managed where taken into account for computation.
* Prod process: Update stock locations on company change
* Bill of material: fixed the error message when updating cost price and the price was 0.
* Cost calculation: prevent product creation.

#### Sale

* Sale: fixed error message when currency conversion is not found.

#### Stock

* Stock: fixed stock location lines by product panel in stock details by product view.
* Incoterm: set incoterm required in sale order only for company client partner.
* STOCK/LOGISTICALFORM : Fix html column headers titles on line grid

#### Supply Chain

* Sale / Purchase / Stock: fixed some views where quantity and price decimal config wasn't being used.
* Sale order: fixed interco configuration when merging sale orders.
* Sale order: timetable amount take into account taxes or not depending on the configuration.


### Developer

#### Business Project

Removed the onLoad extension from the SaleOrder form with id `business-project-sale-order-form` as it was a duplicate.

#### Production

UnitCostCalcLineService.createUnitCostCalcLine takes qtyRatio as argument

## [8.3.10] (2025-07-03)

### Fixes
#### Base

* Data Backup: fixed translations and added help for some fields.

#### Account

* MoveLine/TaxSet: fixed technical error generating movelines without taxes in some process
* AnalyticMoveLine/MoveLine : fixed the reset of analytic accounts on moveline when changing it on analytic move line
* MassEntryMove: fixed the error message list.
* Move: improve move validation time fixing global audit tracker

#### Human Resource

* Issue on Windows when we try to build the AOS project.

#### Purchase

* Purchase order line: fixed an issue where quantity was not reset to valid when managing multiple quantity.

#### Stock

* Inventory: fixed missing parameter for inventory birt template.


### Developer

#### Base

Migration script -

```
UPDATE meta_field
SET label = 'Relative dates',
description = 'Allows exporting dates by calculating the difference with the export date. During import, the data will be updated based on the import date and the previously saved offset.'
WHERE name = 'isRelativeDate' AND meta_model IN (SELECT id FROM meta_model WHERE name = 'DataBackup');

UPDATE meta_field
SET description = 'Batch size used when reading data. Allows you to optimize performance based on database volume.'
WHERE name = 'fetchLimit' AND meta_model IN (SELECT id FROM meta_model WHERE name = 'DataBackup');

UPDATE meta_field
SET description = 'Can be used in order to keep a fixed reference to update the current existing database. Not required for loading into another database.'
WHERE name = 'updateImportId' AND meta_model IN (SELECT id FROM meta_model WHERE name = 'DataBackup');
```

#### Account

Migration script -

```
ALTER TABLE account_move ALTER COLUMN mass_entry_errors TYPE text;
```

## [8.3.9] (2025-06-26)

### Fixes
#### Base

* Product category: fixed sequence field display.
* Partner price list: an exclusive sale/purchase partner price list can now be chosen by one partner.
* Update dependency to Axelor Studio to 3.4.5.
* Product: on product copy, purchase price and cost price are now correctly copied.
* Update Axelor Message to 3.2.3.
* Product company: computed sale price on change of 'autoUpdateSalePrice' or 'managPriceCoef'.

#### Account

* Account: ensured consistent VAT system display and editability.
* Invoice/PFP: updated PFP validate status at invoice reconcile with advance payment or credit note.
* Invoice: supplier invoice exchange rate is now based on origin date.
* Accounting report: fixed calculation of Original value and Net carrying amount in accounting report 'Summary of gross values and depreciation'.
* Accounting export / FEC export payroll entry: fixed the issue where only accounted moves were returned.
* Move line tax: fixed tax line generation when two lines have the same taxes but different VAT system.
* AccountingBatch: fixed performance issue when taking all accounts in closure/opening batch

#### Bank Payment

* Bank Reconciliation: blocked the reconciliation of multiple lines on a single move line.

#### Business Project

* InvoicingProject: fixed technical error when invoicing project with some lines to invoice at 0.

#### Contract

* Contract batch: fixed issue where contracts and invoices were not displayed in batch due to missing batch association.

#### CRM

* Agency: fixed agencies menu entry French translation.

#### Human Resource

* Leave request: fixed leave request confirm when using the leave reason as negative values.

#### Production

* Sale order: fixed error in log when choosing a product.
* Sale order lines details: fixed scale error for cost price.

#### Sale

* Partner: fixed wrong computation of total price in 'Sale details by product'.
* Sale order: trading name is correctly reset when changing company.
* Sale order: fixed empty sale order lines on sale order report when lines are generated from configurator.
* Partner: fixed error while opening form view of 'Sale details by product'.

#### Supply Chain

* Sale Order/Purchase Order/Invoice: fixed advance invoice amount on partial invoicing.
* Interco: manually created purchase order from sale order are now tagged as interco when it should.
* Sale order: invoicing state is correctly updated when editing lines quantity.
* Invoice: fixed currency conversion to use the exchange rate based on the orderDate when generating invoice from purchase order.


### Developer

#### Account

Added the `InvoiceTermPfpService` in the `ReconcileInvoiceTermComputationServiceImpl` and `ReconcileInvoiceTermComputationBudgetServiceImpl` constructor.

#### Sale

Added `SaleOrderLineComputeService` as an argument for `ConfiguratorSaleOrderDuplicateServiceImpl` constructor.

#### Supply Chain

Added 'AppSupplychainService' and 'IntercoService' to SaleOrderPurchaseServiceImpl and its extensions.

---

SaleOrderInvoiceService#updateInvoicingState has been moved to SaleInvoicingStateService#updateInvoicingState.
By consequences, SaleInvoicingStateService has been added to SaleOrderServiceSupplychainServiceImpl, WorkflowCancelServiceSupplychainImpl and their respective extensions.

---

The method `getInvoiceLineGeneratorWithComputedTaxPrice` in `InvoiceLineOrderService` has changed its signature from 

```java
InvoiceLineGenerator getInvoiceLineGeneratorWithComputedTaxPrice(
    Invoice invoice,
    Product invoicingProduct,
    BigDecimal percentToInvoice,
    OrderLineTax orderLineTax);
```
to
```java
    InvoiceLineGeneratorSupplyChain getInvoiceLineGeneratorWithComputedTaxPrice(
    Invoice invoice,
    Product invoicingProduct,
    BigDecimal percentToInvoice,
    OrderLineTax orderLineTax,
    SaleOrderLine saleOrderLine,
    PurchaseOrderLine purchaseOrderLine);
```

## [8.3.8] (2025-06-12)

### Fixes
#### Base

* Bank: fixed error when SWIFT address was empty.

#### Account

* InvoiceTerm/Invoice/Pfp: fixed automatic pfp validate status on invoice when using the invoice term to validate menu entry.
* DepositSlip/PaymentVoucher: fixed deposit slip management when using payment mode with accounting mode value for collection.
* Invoice/InvoiceTerm: fixed the amount init when manually splitting amounts.

#### Bank Payment

* Bank Reconciliation: fixed the balance computation when multiple reconcile where on the same move line.

#### Business Project

* Project: fixed error when trying to select new sale order line.


### Developer

#### Account

The InvoiceTermServiceImpl.computePercentageSum method now return the amount without scale to be used in computation. If you want to scale it, use the currencyScaleService.getScaledValue(invoice, amount)

## [8.3.7] (2025-06-02)

### Fixes
#### Base

* Base: removed useless column in base product demo data.
* Group: removed collaboration configuration since it is only available in AOP enterprise.
* Purchase order: fixed main panel visibility to follow the 'Enable trading names management' configuration.

#### Account

* Invoice/PFP: fixed PFP status when all invoice terms are PFP validated.
* Invoice: prevent automatic mail and hide mail panel for supplier invoices.
* Invoice: fixed the display issue with the logo and address when 'Terms and Conditions' are included on the invoice BIRT report.
* Account: fixed unnecessary display of mass entry move lines.
* Payment mode: updated demo data for wire transfer.
* Account: fixed domain for result profit account and result loss account on account config by company.

#### Budget

* BudgetStructure/Budget: fixed the budget line import.

#### Human Resource

* Lunch voucher: fixed outdated expenses wrongly deducted in lunch vouchers calculation.
* Lunch voucher: excluded former employees from lunch voucher calculation.

#### Production

* Sale order: fixed an error that could occur when the bill of material linked to a line was personalized.

#### Sale

* Sale order: fixed global discount to be readonly when finalized.

#### Stock

* Stock location: fixed last inventory date in stock location line when validating an inventory.

#### Supply Chain

* Sale order: fixed the incoterm popup display when it was required.
* Sale order: fixed partial invoicing based on discounted price.
* Sale/purchase order: allow trading name to be editable when a sale/purchase order is generated from interco.


### Developer

#### Account

Added `InvoiceTermPfpService` in the `InvoiceTermPfpValidateServiceImpl` constructor

#### Human Resource

`LunchVoucherMgtLineService#computeRestaurant` and `LunchVoucherMgtLineService#computeInvitation` have a new Period parameter.

#### Production

The constructor of SaleOrderLineBomSyncServiceImpl now requires `AppSaleRepository`.

#### Supply Chain

Removed the `SaleOrderCheckSupplychainServiceImpl` class.

## [8.3.6] (2025-05-15)

### Fixes
#### Base

* Update Axelor Open Platform to 7.3.7.
* Partner: fixed performance issues when opening emails tab on partner form view.
* Partner: when checking for duplicated registration code, correctly ignore whitespaces.
* PriceList: Set the currency to match the currency of the active company.
* Partner: fixed form view for project panel.

#### Account

* Accounting configuration/Move line: ensured consistent tax display and editability.
* BillOfExchange/PaymentSession: fixed technical error when cancelling bank order payment then pay the exactly same invoice term.
* Invoice: fixed invoice term due date when we update invoice due date with free payment condition.
* Mass entry: fixed critical error when we validate more than ten moves in mass entry process.
* Accounting batch: fixed an issue where the generated result entry (move) was not correctly linked to the corresponding close/open account batch.
* Journal: fixed issue allowing moves or mass entry sessions to be created from journal buttons on inactive journals.
* Auto reconcile/Partner: restricted auto reconcile between customer and supplier invoices on partner with compensation enabled.

#### Bank Payment

* BankOrder/Umr: fixed the selection of the partner active umr in bank order confirmation.
* Bank payment config: removed the possibility to select view type account on internal and external bank to bank account.
* Bank order: fixed an error message on missing bank order encryption password even if 'Enable bank order file encryption' was disabled.

#### Budget

* PurchaseOrder/Budget: fixed budget exceed error when using mono budget on purchase order.
* Move/Budget: fixed an issue where only 'realized with no po' was imputed when creating budget on move line related to an invoice line.
* Move/Budget: fixed negative amounts on realized and committed on daybook moveline budget imputation.

#### Contract

* Contract: fixed NPE when ventilating an invoice linked to a contract that has additional benefits.

#### Human Resource

* Expense: removed the possibility to duplicate an expense.
* Allocation line: added x-order on from date for period field.
* Timesheet line: fixed the error when creating a timesheet without an employee from the project view.
* Expense: fixed duplicate move when we confirmed a bank order from an expense.

#### Production

* Configurator: fixed an issue where bill of material and prod process were generated twice.
* Sale order: display an error message when trying to delete a line linked to manufacturing orders.

#### Project

* Project: added project time unit in demo data and set it to 'Day'.
* Project: fixed Project activity dashboard to fetch only relevant messages, avoiding unnecessary loading and filtering.
* Project/SaleOrder: fixed name computation when generating business project.
* App project: fixed issue where custom fields for Project/Task caused save errors when name was not entered first with type 'select' or 'multiselect'.

#### Sale

* Sale order: fixed the issue of finalizing a sale order without sale order lines.
* Sale order: reset the 'manual unlock' state when duplicating a sale order.
* Configurator formula: fixed message type to show an info message instead of an alert when the formula works correctly.
* Sale order: fixed an error occurring when adding lines to an order without production module.

#### Stock

* Stock location: fixed date time issue in location financial data report.

#### Supply Chain

* Stock move: fixed error message when checking available stock for requested and reserved quantities.


### Developer

#### Account

In `AccountingCloseAnnualService`, the method `generateResultMove` now returns a `Move` instead of `void`.

---

In `MoveLineToolService.getMoveExcessDueList`, changed `Long invoiceId` parameter to `Invoice invoice`.

#### Project

Creation of a new service `ProjectNameComputeService`, and added `ProjectNameComputeService` in the `ProjectService` constructor.

## [8.3.5] (2025-04-30)

### Fixes
#### Base

* Updated Axelor Open Platform to 7.3.6.
* Product: fixed domain filter for product variant values.
* Product: fixed update product prices process to update company specific prices.
* Template: on creating a new template manually, keep email account empty by default so the generated message can use default email account.
* Fixed rounding mode in some quantities computation.
* Sale order line: fixed an issue where warnings related to sale order line (like stock control) were not displayed to the user and blocked the process.

#### Account

* Bank reconciliation: fixed NPE when validating a bank reconciliation without journal while having an account on a bank reconciliation line.
* Invoice: fixed 'Terms and Conditions','Client box in invoice' and 'Legal note on sale invoices' to support HTML tags in BIRT report.
* Analytic move line: set analytic axis required when we create an analytic move line.

#### Budget

* Purchase order line: use the correct account when a purchase order line is set to 'fixed assets'.

#### Business Project

* Business Project Task: fixed an issue where total costs was not computed on unit cost change.
* Invoice: fixed third-party payer when generating an invoice from an invoicing project.

#### Contract

* Contract invoicing batch: fixed an issue where it was not possible to generate more than one invoice for a same contract.

#### Human Resource

* Expense: fixed personal expense and personal expense amount french translation.
* User: hide create employee button if the partner has already an employee.

#### Mobile Settings

* Mobile permissions: fixed demo data.

#### Production

* Manufacturing order: fixed an issue where producible quantity was taking into account component that were not managed in stock.
* Sale order: fixed an error occurring when changing the price of a sale order line details.
* MRP: added a explicit error message instead of a NPE when prod process is null on manufacturing proposal.
* Sale order: fixed an error occurring when generating sale order line details with no cost price.
* Sale order line details: fixed the unit price getting recomputed wrongly when changing quantity.
* Prod process line: fixed domain of stock location to take into account only those usable on production

#### Project

* Project planning: fixed english message in confirmation popup.
* Sprint: added a sprint form and grid with editing and adding functionalities disabled.

#### Stock

* Stock move: prevented generation of invoices when 'generate Invoice from stock move' configuration is disabled in supplychain app.

#### Supply Chain

* Declaration of exchanges: will now reset fiscal year and if necessary country on change of company.
* Supplychain: fixed total W.T. of invoices generated from a stock move of a purchase order with ATI.

### Developer

Fixed ControllerMethodInterceptor to avoid adding exception management on non void and non public methods in controller.

#### Business Project

Added `PartnerAccountService` to the constructor of `ProjectGenerateInvoiceServiceImpl`.

## [8.3.4] (2025-04-17)

### Fixes
#### Base

* Updated Axelor Open Platform to 7.3.5.
* Updated axelor-studio dependency to 3.4.2.
* Base: fixed base roles are not imported.
* Partner: fixed agencies domain filter.

#### Account

* Reconcile: removed check on tax for opening/closure.
* Invoice: fixed an issue in the BIRT report where a partner or company partner had multiple accounting situations for different companies.
* Payment voucher: fixed translation for error message when move amounts have been changed since the imputation.
* Account management: fixed fields not required in editable grid of payment mode.
* Invoice: fixed due date when we save with free payment condition.
* Accounting report: set currency required for 'Bank reconciliation statement' report.
* Invoice: fixed price list not filled the first time we change the partner.

#### Bank Payment

* Bank reconciliation: fixed issue when try to load bank statement in different currency than bank details.

#### Business Project

* Business project task: fixed an issue where modifying quantity was resetting unit price.

#### CRM

* Partner: fixed 'Generate Project' button still present even with job costing app disabled.

#### Human Resource

* Timesheet: prevent 'To date' edition on completed timesheets.
* Project allocation: fixed initialization with planned time in mass generation.
* Leave Request: fixed the NPE caused by the employee's empty week planning.
* Leave request: leave reason and duration are now required when using multi-leave assistant.

#### Production

* Purchase order: fixed an issue where subcontracted order was reset to standard on validation.
* Sale order: fixed translation for 'Details Lines (Tree)'.

#### Project

* Project: fixed wrong compute of full name.
* Project task: fixed npe on project task gantt views.

#### Purchase

* Supplier catalog: fixed wrong fetched information for product without catalog.

#### Sale

* Sale order: fixed 'Generate production order' button displayed when app production is deactivated.
* Sale order: fixed the issue where stock location was overwritten upon changing the partner when trading name was null.
* Sale order: fixed missing error message on pack products.
* Sale order: fixed the alert message before confirming the sale order.

#### Stock

* Stock move line: fixed total net mass calculation when real quantities are generated.
* Stock location: fixed quantity scaling in stock location line grid and form views.

#### Supply Chain

* Sale Order: do not remove shipment cost line if the line is already invoiced.
* Sale order: fixed sale order invoicing state when a line has a negative amount.


### Developer

#### Sale

The method `confirmCheckAlert` in `SaleOrderCheckService` now returns `List<String>` instead of `String`.

#### Supply Chain

Added `InvoiceRepository` to the constructor of `SaleOrderShipmentServiceImpl`.

## [8.3.3] (2025-04-03)

### Fixes
#### Base

* Update to Axelor Open Platform 7.3.4.
* Day planning: added sequence in demo data.
* Base batch: fixed issue in recompute all addresses batch.
* Fixed SIRET check before creation using partner Sirene API call.
* Partner: when creating/filling a partner from Sirene API, fill the NIC.
* Partner: fixed wrong registration number check warning translation.

#### Account

* Fixed an user interface issue where it was allowed to create a new tax where the user should only be able to select one.
* Invoice: show 'Price excl. Tax' on invoice report even if it is 0.
* Bank order/National treasury transfer: fixed internal money transfer from bank order by inverting credit and debit.
* Accounting batch: fixed balance amount on close/open account batch.
* Invoice: fixed bad error handling when skipping validation is active.
* Invoice term: fixed display condition for PFP panel.
* Payment voucher: fixed control when we register payment in multicurrency.
* Fixed asset: fixed null value on moveline for ongoing cession.

#### Budget

* Budget: fixed domain issues for analytic axis and account in Budget from Budget structure.
* Global budget/Budget level/Budget: fixed the visibility of the simulated budget amount fields.
* Purchase order line: fixed budget domain in budget distribution.

#### Business Project

* Business project task: fixed an issue at task creation when employee has a product.
* Invoicing project: fixed an issue where non validated timesheet lines were included.

#### Contract

* Contract line: fixed domain for product based on contract target type.
* Contract: added french translation for the 'Related Equipment' panel title.

#### Human Resource

* Project: fixed an error occurring when creating a timesheet and the user had no employee.
* Timesheet line: fixed an issue where a timesheet line was not linked to the employee's timesheet.

#### Production

* Bill of material: fixed an issue where a component could not be deleted from the BOM line list after the tree view was opened.
* Sale order: fill default prod process of bill of material when choosing a product.

#### Sale

* Sale order: fixed the alert when finalizing an order and the price list is not valid.
* Sale order: fixed wrong behaviour on a prospect with accepted credit.
* App sale: fixed French translation for line list display type helper.
* Sale order: fixed NPE when creating a sale order without active company.

#### Stock

* Stock move: fixed average price not updated on product when stock move is canceled.
* Inventory: gap and real values computation now correctly takes into account company product cost/sale/purchase price.

#### Supply Chain

* Sale order: fixed NPE when invoicing a sale order with title line.


### Developer

#### Base

Added `PartnerService` and `AppBaseService` to the constructor of `PartnerGenerateService`.

#### Account

Added `CurrencyService` and `CurrencyScaleService` to the constructor of `PaymentVoucherControlService`.

## [8.3.2] (2025-03-20)

### Fixes
#### Base

* User: added missing translation for the notification message when switching active company.

#### Account

* Journal: disabled archive to avoid possibility to generate a move on an archived journal.
* Move line: fixed a bug where generated credit note move line origin date was not consistent with its move date.

#### Budget

* Global budget: fixed the budgetKey computation process to always ensure an updated key value.

#### Contract

* Contract: fixed a duplication on analytic move lines at invoicing.

#### CRM

* Lead: fixed an error appearing in logs when opening the email panel.

#### Project

* Project: fixed an issue where 'Invoicing timesheet' field was not displayed.

#### Purchase

* Supplier catalog: products with multiple catalogs now take the catalog with the most recent update date.

#### Sale

* Sale order: fixed the issue where lines were not printed with the pack feature.

#### Supply Chain

* MRP: a warning is now displayed when trying to delete a MRP to explain that the MRP is reset instead of removed.


### Developer

#### Sale

Added `SaleOrderLineComputeService` to the constructor of `SaleOrderServiceImpl`.

## [8.3.1] (2025-03-13)

### Fixes
#### Base

* Update to Axelor Open Platform 7.3.3.
* Fixed a critical issue preventing the application from starting.
* Base: removed API Configuration menu entry.
* App base: added password widget for the certificate password.
* Base: fixed some errors displayed as notification instead of popup.
* Signature: fixed broken grid view when selecting a certificate.
* Message: fixed an issue where the attached birt document name changed when sent via email.
* User: made the title of the company set panel visible.

#### Account

* Accounting batch: ignored check at reconcile when move only contains tax account.

#### Business Project

* Invoice: fixed fiscal position when generating an invoice from an invoicing project.

#### CRM

* Partner: creating a new partner cannot create a prospect and a supplier at the same time.

#### Human Resource

* HR: fixed an error occurring when using 'Leave increment' batch and if employees do not have a main employment contract.
* Timesheet: fixed error on opening a timesheet preventing employee field from being set readonly.

#### Production

* Sale order line: fixed an issue when syncing bill of materials lines dans sub sale order lines.

#### Purchase

* Purchase request: added missing API endpoint to get back to draft.

#### Sale

* Sale order line: fixed an issue where discount was not applied immediatly.
* Sale order line: fixed icon of edit configurator button.
* Sale order line: fixed advanced filter not displayed unless the whole page is refreshed.

#### Stock

* Stock location: fixed error when emptying parent stock location.


### Developer

#### Base

Menu 'referential-conf-api-configuration' and action 'referential.conf.api.configuration' have been removed. 

```sql
DELETE FROM meta_menu WHERE name = 'referential-conf-api-configuration';
DELETE FROM meta_action WHERE name = 'referential.conf.api.configuration';
```

## [8.3.0] (2025-03-07)

### Features
#### Base

* Updated Axelor Open Platform to 7.3. You can find all information on this release [here](https://github.com/axelor/axelor-open-platform/blob/7.3/CHANGELOG.md).
* Updated Axelor Studio dependency to 3.4. You can find all information on this release [here](https://github.com/axelor/axelor-studio/blob/release/3.4/CHANGELOG.md).
* Partner: added a new connector to Sirene API to fetch partner information.
* Price list: added currency management in price lists.
* Product: managed multiple barcodes on product.

#### HR

* HR: added new menu entries to help filling existing requests.
* Leave request: added number of days available on the requested leave date.
* Leave request: added a new wizard to create multiple leave requests with different types.
* Timesheet: added a new wizard to create timesheet from project planning.

#### Purchase

* Purchase order: added subconctractor management.
* Purchase order: added receipt stock location by lines.
* Supplier: added carriage paid feature on suppliers and purchase orders.
* Supplier catalog: added unit management in supplier catalogs.

#### Sale

* Sale order: added the possibility to configure a discount on a single order that will be applied to each line.
* Sale order line: allow to duplicate a sale order line. 
* Sale order line: added delivery address in sale order lines.
* Sale order line: managed multi line in sale order printings.
* Configurator: on a sale order line, allow to modify the form values that were previously filled, and run the computation again to update sale order line values.
* Configurator: allow tu duplicate a sale order line, using the existing configurator to generate the duplicated line.
* Configurator: managed versioning on configurators.

#### Account

* Tax: changed the way non deductible taxes are configured.
* Journal: added a button to generate accounting entries for the current journal in the form view.

#### Stock

* Stock move: added customer delivery split line support.

#### Production

* Sale order line: added details line on sale order. On a given sale order line, it will display information related to the bill of materials related to this line.
* Cost sheet: managed launch quantity in cost computation.

#### Project

* Project: added sprint for project management.
* Project: added allocation management to manage resource allocation on given periods.
* Dashboard: added a new resource management dashboard.
* Project planning: automatic generation of project planning from project tasks.
* Sale order: allow to generate a project from a sale order and a project template.

#### Business Production

* Business project: added manufacturing order generation from business project.
 
#### Mobile Settings

* APK: added possibility to upload the .apk of the current app version to manage deployment on new devices.
* DMS: added new configuration to manage DMS on mobile application.
* Purchase: added new configurations to manage purchase requests.

### Changes

#### Base

* Partner: added the possibility to link multiple trading names to a partner.
* Partner: added a warning on creation if a partner with the same registration number (SIRET for France) already exists.
* Partner: registration number check is made on change and not on save like before.
* App base: shortcut management for active company/trading name/project is now a single configuration.
* Year/Period: allow period generation on period of 1 or 2 weeks.
* Address: improved address templates.

#### CRM

* Lead: replaced the existing address fields by a link to a full Address object.

#### HR

* Timesheet: added a dashlet to see planned time from projects on a period.
* Leave request: took into account the end date to compute quantity available.
* Leave request: created a new API endpoint to create multiple leave requests.
* Leave request: created a new API endpoint to fetch number of available days for a leave type.
* Leave request: created a new API endpoint to check if requested days are available.
* Leave request: created a new API endpoint to update leave request status.

#### Purchase

* Purchase request: created a new API endpoint to update purchase request status.

#### Sale

* Sale order: in multi line sale order, added icon to see the product type on each line.
* Sale order: new configuration to activate or disabled price computation from sub lines.
* Sale order: added a button to open multi lines in a separate form view.
* Sale order: managed modifications on a sale order already partially invoiced through timetable.

#### Account

* Accounting batch: added an option to open/close every accounts instead of having to select everything.

#### Bank payment

* Bank order: added CSV export on bank order lines.

#### Stock

* Logistical form: managed different stock locations on lines.
* Stock move: managed tracking number taking into account configuration per company.

#### Production

* Sale order line: added a new tag to display the production status on sale order line linked to manufacturing orders.
* Sale order line: added quantity to produce.

#### Project

* Project: added link to company.
* Batch: added new batches to update project task status.
* App project: added a new configuration to activate/deactivate the planification.
* Project task: generate project planning lines during task generation from template.
* Project task: use signature widget.

#### Business project

* Business project: New dashlet to see related purchase orders.

### Fixes

#### Base

* Product: when generating product variant, copy the product instead of only copying some fields.

#### HR

* Extra hours: fixed filter on employee selection.
* Expense: fixed analytic accounting information display.
* Weekly/Events planning: remove duplicated configurations in HR, the configurations in the company are now used everywhere.
* Timesheet: in timesheet form view, displayed "is completed" in a tag.

#### Sale

* Sale order: removed sale order line tree feature, it is replaced by sale order line details.

#### Account

* Move line: added accounting date on move line form view.
* Move: fixed reverse charge feature on a multi tax accounting entry.

#### Budget

* Budget distribution: added origin on budget distribution.

#### Project

* Project task invoicing: use the price in the task instead of the product price.
* Task section: removed task section, it is replaced by category.
* Project: added Time follow-up panel.
* Project version: version name is now required and unique per project.

#### Business project

* App business project: removed configurations related to time management in app business project (time units and default hours per day) to use the configurations already present in app base.
* Project financial data: added a link to the project in project financial data view.

[8.3.18]: https://github.com/axelor/axelor-open-suite/compare/v8.3.17...v8.3.18
[8.3.17]: https://github.com/axelor/axelor-open-suite/compare/v8.3.16...v8.3.17
[8.3.16]: https://github.com/axelor/axelor-open-suite/compare/v8.3.15...v8.3.16
[8.3.15]: https://github.com/axelor/axelor-open-suite/compare/v8.3.14...v8.3.15
[8.3.14]: https://github.com/axelor/axelor-open-suite/compare/v8.3.13...v8.3.14
[8.3.13]: https://github.com/axelor/axelor-open-suite/compare/v8.3.12...v8.3.13
[8.3.12]: https://github.com/axelor/axelor-open-suite/compare/v8.3.11...v8.3.12
[8.3.11]: https://github.com/axelor/axelor-open-suite/compare/v8.3.10...v8.3.11
[8.3.10]: https://github.com/axelor/axelor-open-suite/compare/v8.3.9...v8.3.10
[8.3.9]: https://github.com/axelor/axelor-open-suite/compare/v8.3.8...v8.3.9
[8.3.8]: https://github.com/axelor/axelor-open-suite/compare/v8.3.7...v8.3.8
[8.3.7]: https://github.com/axelor/axelor-open-suite/compare/v8.3.6...v8.3.7
[8.3.6]: https://github.com/axelor/axelor-open-suite/compare/v8.3.5...v8.3.6
[8.3.5]: https://github.com/axelor/axelor-open-suite/compare/v8.3.4...v8.3.5
[8.3.4]: https://github.com/axelor/axelor-open-suite/compare/v8.3.3...v8.3.4
[8.3.3]: https://github.com/axelor/axelor-open-suite/compare/v8.3.2...v8.3.3
[8.3.2]: https://github.com/axelor/axelor-open-suite/compare/v8.3.1...v8.3.2
[8.3.1]: https://github.com/axelor/axelor-open-suite/compare/v8.3.0...v8.3.1
[8.3.0]: https://github.com/axelor/axelor-open-suite/compare/v8.2.9...v8.3.0
