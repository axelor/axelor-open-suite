## [8.2.30] (2025-11-20)

### Fixes
#### Account

* Reconcile: fixed manual reconcile in the specific reconcile view.
* Move: added origin in traceback while mass accounting during anomaly.
* Accounting report: fixed Aged balance and detailed customer balance report issue.
* Invoice term: fixed the scale of amount when computing the name.
* Payment session: fixed french translation for supplier and bank details in custom dashlet.
* Invoice: fixed attachment behavior when printing/regenerating to avoid duplicates and respect the attachment option, including on ventilation.
* MOVE : fixed inconsistant message when trying to delete a move

#### Business Project

* Business project: fixed internal server error while changing status.

#### Contract

* Contract: fixed the project domain based on the contarct types.
* Contract: fixed invoicing amounts not translated in french.

#### Production

* Product: fixed cost price and avg price when the product is manufactured for the first time.

#### Project

* Project: fixed the performance issue in project form with many projects linked to a user.

#### Stock

* Bill of material: fixed decimal digit number for bill of material line.
* Inventory: fixed an error occurring when there were more than 2 duplicated inventory lines.

#### Supply Chain

* Invoice/PurchaseOrder : fixed the link between an advance payment and an invoice from the same purchase order


### Developer

#### Account

- Removed the checkReconcile method from ReconcileCheckService.
- Removed the isEnoughAmountToPay method from InvoiceTermToolService.

Script to remove a deleted action : 
- DELETE FROM meta_action WHERE name = 'action-reconcile-method-check-reconcile';

#### Production

- Changed the ManufOrderWorkflowServiceImpl.updateProductCostPrice parameters to add a BigDecimal costPrice

## [8.2.29] (2025-11-06)

### Fixes
#### Base

* Currency conversion: updated old API for currency conversion.
* Bank details: fixed some iban fields when adding bank details from partner view.

#### Account

* Invoice: correct due dates with multi‑term payment conditions when Free is enabled on payment conditions
* Reconcile: avoid negative amount when duplicating a reconcile.
* Invoice: fixed an issue where Payment mode on invoice term is changed after the ventiation
* FixedAsset: fixed lines amount's after split
* Move line query: fixed selected move lines unreconcilation.


### Developer

#### Base

-- migration script to update bank_code, sort_code, account_nbr and bban_key in bank details table

UPDATE base_bank_details BankDetails
SET bank_code = SUBSTRING(BankDetails.iban FROM 5 FOR 5)
FROM base_bank Bank
WHERE BankDetails.bank = Bank.id
  AND Bank.bank_details_type_select = 1
  AND (BankDetails.bank_code IS NULL OR BankDetails.bank_code = '')
  AND BankDetails.iban IS NOT NULL;

UPDATE base_bank_details BankDetails
SET sort_code = SUBSTRING(BankDetails.iban FROM 10 FOR 5)
FROM base_bank Bank
WHERE BankDetails.bank = Bank.id
  AND Bank.bank_details_type_select = 1
  AND (BankDetails.sort_code IS NULL OR BankDetails.sort_code = '')
  AND BankDetails.iban IS NOT NULL;

UPDATE base_bank_details BankDetails
SET account_nbr = SUBSTRING(BankDetails.iban FROM 15 FOR 11)
FROM base_bank Bank
WHERE BankDetails.bank = Bank.id
  AND Bank.bank_details_type_select = 1
  AND (BankDetails.account_nbr IS NULL OR BankDetails.account_nbr = '')
  AND BankDetails.iban IS NOT NULL;

UPDATE base_bank_details BankDetails
SET bban_key = RIGHT(BankDetails.iban, 2)
FROM base_bank Bank
WHERE BankDetails.bank = Bank.id
  AND Bank.bank_details_type_select = 1
  AND (BankDetails.bban_key IS NULL OR BankDetails.bban_key = '')
  AND BankDetails.iban IS NOT NULL;

## [8.2.28] (2025-10-30)

### Fixes
#### Account

* InvoiceTerm/PfpValidateStatus : fixed a technical error by changing the Listener.
* Invoice: fixed the invoice generation.

#### Bank Payment

* BankOrder : fixed the bank code on the cfonb160 format
* Bank reconciliation: fixed balances compute takes a lot of time to finish.


### Developer

#### Account

Changed the checkOtherInvoiceTerms function from InvoiceTermPfpService to InvoiceTermPfpToolService.
Changed the checkOtherInvoiceTerms function from MoveInvoiceTermService to MovePfpToolService.

Added MovePfpToolService in MoveGroupServiceImpl constructor.
Added MovePfpToolService in MoveGroupBudgetServiceImpl constructor.
Added MovePfpToolService in MoveRecordUpdateServiceImpl constructor.

#### Bank Payment

- `getMoveLines(), computeMovesReconciledLineBalance() and computeMovesUnreconciledLineBalance()` methods from `BankReconciliationBalanceComputationServiceImpl` have been replaced by `computeBalances(Account)` method to compute moves reconciled/unreconciled balance.

## [8.2.27] (2025-10-23)

### Fixes
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

* Sale order : fixed an issue where timetable lines were not automatically emptied on sale order cancellation.

#### Stock

* Stock move: fixed display issue in form view.
* Stock dashboard: deliveries dashboards are now filtered on virtual stock location.

#### Supply Chain

* Sale order / Advance payment: fixed the advance payment amount at wizard opening.
* Order/AvancePayment: fixed the advance payment amount from sale/purchase order
* INVOICE : Set interco as true when generating invoice from an interco saleOrder / purchaseOrder.


### Developer

#### Bank Payment

Added BankOrderCheckService in the BankOrderCreateService constructor. Added BankOrderCheckService in the BankOrderCreateServiceHr constructor. Changed the BankOrderCheckService.checkPreconditions(BankOrder bankOrder) in checkPreconditions(PaymentMode paymentMode, Integer partnerType, LocalDate bankOrderDate, Company senderCompany, BankDetails senderBankDetails)

#### Sale

- SaleOrderWorkflowServiceSupplychainImpl constructor is updated to introduce SaleOrderSupplychainService

#### Supply Chain

Removed CommonInvoiceService.createInvoiceLinesFromOrder Changed the parameter of PurchaseOrderInvoiceService.createInvoiceAndLines from (PurchaseOrder,List<PurchaseOrderLineTax>,Product,BigDecimal,int,Account) to (PurchaseOrder,Product,BigDecimal,int,Account) Changed the parameter of PurchaseOrderInvoiceService.createInvoiceLines from (Invoice,List<PurchaseOrderLineTax>,Product,BigDecimal) to (Invoice,List<PurchaseOrderLine>,Product,BigDecimal) Changed the parameter of InvoiceLineOrderService.getInvoiceLineGeneratorWithComputedTaxPrice from (Invoice,Product,BigDecimal,OrderLineTax,SaleOrderLine,PurchaseOrderLine) to (Invoice,Product,BigDecimal,SaleOrderLine,PurchaseOrderLine,BigDecimal,Set<TaxLine>) Changed the parameter of SaleOrderInvoiceService.createInvoiceAndLines from (SaleOrder,List<SaleOrderLineTax>,Product,BigDecimal,int,Account) to (SaleOrder,Product,BigDecimal,int,Account) Changed the parameter of SaleOrderInvoiceService.createInvoiceLines from (Invoice,List<SaleOrderLineTax>,Product,BigDecimal) to (Invoice,List<SaleOrderLine>,Product,BigDecimal)

## [8.2.26] (2025-10-09)

### Fixes
#### Base

* Product: fixed prices of generated company product variant .
* Abc analysis: fixed color on chart same as table.
* Price list: fixed price list filter in grid views.

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

## [8.2.25] (2025-09-25)

### Fixes
#### Base

* User: fixed trading name not filtered on company.
* Update Axelor Utils to 3.3.4

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

#### Contract

* Contract line: fixed display issue of 'Invoice from consumption' field.
* Contract line: fixed domain for product based on product per company.

#### CRM

* Lead: prevent users from reopening converted lead in kanban view.
* Event: fixed duration computation when creating a new event.

#### Production

* Production API : fixed request to fetch consumed products was not working when 'Manage consumed products on operations' is enabled.

#### Sale

* Sale config: client box and legal note fields are now translatable.
* Sale order: fixed the sub sale order lines when merging the multiple sale orders.

#### Supply Chain

* Sale order: fixed delivery state computation to ignore sale order lines with qty 0.
* MRP: fixed an error occurring with manufacturing order in certain cases.

#### Talent

* Sequence: added missing selection value for job position.


### Developer

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

## [8.2.24] (2025-09-11)

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
* Business project: fixed the closing rule condition
* ProjectTask: fixed time unit conversion issue after computing project totals.

#### Human Resource

* Expense : disable the multi currency management until 8.5
* Lunch voucher: fixed an issue where computation did not deduct ventilated or reimbursed expenses.

#### Sale

* Sale order: fixed price recomputation when editable tree is enabled.


### Developer

#### Business Project

Replace SaleOrderRepository by SaleOrderLineRepository in BusinessProjectClosingControlServiceImpl constructor Replace PurchaseOrderRepository by PurchaseOrderLineRepository in BusinessProjectClosingControlServiceImpl constructor Change BusinessProjectClosingControlServiceImpl.areSaleOrdersFinished to BusinessProjectClosingControlServiceImpl.areSaleOrderLinesFinished Change BusinessProjectClosingControlServiceImpl.arePurchaseOrdersInvoiced to BusinessProjectClosingControlServiceImpl.arePurchaseOrderLinesInvoiced Change BusinessProjectClosingControlServiceImpl.arePurchaseOrdersReceived to BusinessProjectClosingControlServiceImpl.arePurchaseOrderLinesReceived

---

Added UnitConversionForProjectService in ProjectTaskBusinessProjectServiceImpl constructor
Added UnitConversionForProjectService in ProjectTaskBusinessSupportServiceImpl constructor

#### Human Resource

If you have some expense with another currency than the company currency, you will need a script to reset it.

Script : 
UPDATE hr_expense e SET currency = c.currency
FROM base_company c WHERE c.id = e.company AND e.currency != c.currency;
DELETE FROM meta_action WHERE name = 'action-expense-attrs-kilometric-panel-visibility';

#### Sale

Added SubSaleOrderLineComputeService to SaleOrderCreateServiceImpl constructor.

## [8.2.23] (2025-08-28)

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

#### Sale

* Sales order: fixed status filter on 'My Sales Orders' dashboard.

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

## [8.2.22] (2025-08-14)

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

## [8.2.21] (2025-07-31)

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
* Sale order: fixed French translation on the customer deliveries button.

#### Stock

* Stock move: fixed the title of Generate invoice button.
* Tracking number: fixed the fields that were not displayed in wizard.
* Stock Move: removed 'invoiced' status on internal stock moves.
* Product: fixed 'Stock history' chart.
* Stock move: moved stock move line menu under 'Stock' menu.

#### Supply Chain

* Sale order: fixed an issue where some informations were not filled when generating a sale order from a purchase order.
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

## [8.2.20] (2025-07-18)

### Fixes
#### Base

* Price list line: fixed the display issue with the 'Amount' title.
* App Base: fixed French translation for some configuration settings.

#### Account

* INVOICE : Wrong title displayed on PDF for a credit note issued on an advance payment.
* Partner/AccountingSituation : Correct anomaly causing account fields to be empty when generating automatically a new accounting situation on the partner (when adding a new company)
* AccountingBatch/BillOfExchange : Fixed the bill of exchange data type select.
* Invoice: fixed an issue where the payment date was not emptied when duplicate or generate from a paid invoice.
* Accounting report: fixed moveStatusSelect filter display issue after a status is being selected.

#### Bank Payment

* Invoice/BillOfExchange: fixed the fact the placement move was a Sale move and not a Payment move

#### Business Project

* Sale order: removed the unnecessary form view extension in business project
* Sale order: removed a duplicated extension in business project module.

#### Contract

* Contract/Invoice/Credit Note : fixed the issue where the invoiced amount of the contract was positive even for credit note.

#### Human Resource

* Expense: fixed an error when emptying employee

#### Production

* Sales and Operations Planning: added missing title on 'Generate MPS forecasts' wizard
* Sale order: sublines are now correctly personalized.
* MRP: fixed an issue where bill of material line marked as not stock managed where taken into account for computation.
* Prod process: Update stock locations on company change
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


### Developer

#### Business Project

Removed SaleOrder form with id `business-project-sale-order-form` as the onLoad extension was deleted

---

Removed the onLoad extension from the SaleOrder form with id `business-project-sale-order-form` as it was a duplicate.

## [8.2.19] (2025-07-03)

### Fixes
#### Base

* Data Backup: fixed translations and added help for some fields.

#### Account

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

## [8.2.18] (2025-06-26)

### Fixes
#### Base

* Product company: computed sale price on change of 'autoUpdateSalePrice' or 'managPriceCoef'.
* Update Axelor Message to 3.2.3.
* Product category: fixed sequence field display.
* Update dependency to Axelor Studio to 3.3.15.

#### Account

* Invoice: supplier invoice exchange rate is now based on origin date.
* Accounting report: fixed calculation of Original value and Net carrying amount in accounting report 'Summary of gross values and depreciation'.
* Invoice: fixed invoice term due date when we update invoice due date with free payment condition.
* Accounting export / FEC export payroll entry: fixed the issue where only accounted moves were returned.
* Invoice/PFP: updated PFP validate status at invoice reconcile with advance payment or credit note.

#### Contract

* Contract batch: fixed issue where contracts and invoices were not displayed in batch due to missing batch association.

#### Production

* Sale order: fixed error in log when choosing a product.

#### Sale

* Partner: fixed error while opening form view of 'Sale details by product'.
* Partner: fixed wrong computation of total price in 'Sale details by product'.
* Sale order: trading name is correctly reset when changing company.

#### Supply Chain

* Sale order: invoicing state is correctly updated when editing lines quantity.
* Sale Order/Purchase Order/Invoice: fixed advance invoice amount on partial invoicing.


### Developer

#### Account

Added the `InvoiceTermPfpService` in the `ReconcileInvoiceTermComputationServiceImpl` and `ReconcileInvoiceTermComputationBudgetServiceImpl` constructor.

#### Supply Chain

SaleOrderInvoiceService#updateInvoicingState has been moved to SaleInvoicingStateService#updateInvoicingState.
By consequences, SaleInvoicingStateService has been added to SaleOrderServiceSupplychainServiceImpl, WorkflowCancelServiceSupplychainImpl and their respective extensions.

## [8.2.17] (2025-06-12)

### Fixes
#### Base

* Bank: fixed error when SWIFT address was empty.

#### Account

* DepositSlip/PaymentVoucher: fixed deposit slip management when using payment mode with accounting mode value for collection.
* Invoice/InvoiceTerm: fixed the amount init when manually splitting amounts.

#### Bank Payment

* Bank Reconciliation: fixed the balance computation when multiple reconcile where on the same move line.

#### Business Project

* Project: fixed error when trying to select new sale order line.


### Developer

#### Account

The `InvoiceTermServiceImpl.computePercentageSum` method now return the amount without scale to be used in computation. If you want to scale it, use the `currencyScaleService.getScaledValue(invoice, amount)`

## [8.2.16] (2025-06-02)

### Fixes
#### Base

* Group: removed collaboration configuration since it is only available in AOP enterprise.
* Purchase order: fixed main panel visibility to follow the 'Enable trading names management' configuration.

#### Account

* Invoice/PFP: fixed PFP status when all invoice terms are PFP validated.
* Invoice: prevent automatic mail and hide mail panel for supplier invoices.
* Invoice: fixed the display issue with the logo and address when 'Terms and Conditions' are included on the invoice BIRT report.
* Account: fixed unnecessary display of mass entry move lines.
* Account: fixed domain for result profit account and result loss account on account config by company.

#### Budget

* BudgetStructure/Budget: fixed the budget line import.

#### Human Resource

* Lunch voucher: fixed outdated expenses wrongly deducted in lunch vouchers calculation.
* Lunch voucher: excluded former employees from lunch voucher calculation.

#### Stock

* Stock location: fixed last inventory date in stock location line when validating an inventory.

#### Supply Chain

* Sale order: fixed partial invoicing based on discounted price.
* Sale/purchase order: allow trading name to be editable when a sale/purchase order is generated from interco.


### Developer

#### Account

Added `InvoiceTermPfpService` in the `InvoiceTermPfpValidateServiceImpl` constructor

#### Human Resource

`LunchVoucherMgtLineService#computeRestaurant` and `LunchVoucherMgtLineService#computeInvitation` have a new `Period` parameter.

## [8.2.15] (2025-05-15)

### Fixes
#### Base

* Partner: fixed performance issues when opening emails tab on partner form view.
* Partner: fixed form view for project panel.

#### Account

* BillOfExchange/PaymentSession: fixed technical error when cancelling bank order payment then pay the exactly same invoice term.
* Mass entry: fixed critical error when we validate more than ten moves in mass entry process.
* Accounting batch: fixed an issue where the generated result entry (move) was not correctly linked to the corresponding close/open account batch.
* Auto reconcile/Partner: restricted auto reconcile between customer and supplier invoices on partner with compensation enabled.

#### Bank Payment

* BankOrder/Umr: fixed the selection of the partner active umr in bank order confirmation.
* Bank payment config: removed the possibility to select view type account on internal and external bank to bank account.
* Bank order: fixed an error message on missing bank order encryption password even if 'Enable bank order file encryption' was disabled.

#### Budget

* PurchaseOrder/Budget: fixed budget exceed error when using mono budget on purchase order.
* Move/Budget: fixed an issue where only 'realized with no po' was imputed when creating budget on move line related to an invoice line.
* Move/Budget: fixed negative amounts on realized and committed on daybook moveline budget imputation.

#### Human Resource

* Expense: removed the possibility to duplicate an expense.
* Expense: fixed duplicate move when we confirmed a bank order from an expense.

#### Project

* Project: fixed Project activity dashboard to fetch only relevant messages, avoiding unnecessary loading and filtering.

#### Sale

* Sale order: fixed the issue of finalizing a sale order without sale order lines.
* Sale order: reset the 'manual unlock' state when duplicating a sale order.
* Configurator formula: fixed message type to show an info message instead of an alert when the formula works correctly.

#### Stock

* Stock location: fixed date time issue in location financial data report.

#### Supply Chain

* Stock move: fixed error message when checking available stock for requested and reserved quantities.
* Sale/Purchase order: fixed NPE when changing shipment mode directly without adding any order lines.


### Developer

#### Account

In `AccountingCloseAnnualService`, the method `generateResultMove` now returns a `Move` instead of `void`.

---

In `MoveLineToolService.getMoveExcessDueList`, changed `Long invoiceId` parameter to `Invoice invoice`.

## [8.2.14] (2025-04-30)

### Fixes
#### Base

* Updated Axelor Open Platform to 7.2.7.
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

#### Production

* Manufacturing order: fixed an issue where producible quantity was taking into account component that were not managed in stock.
* Prod process line: fixed domain of stock location to take into account only those usable on production

#### Project

* Project planning: fixed english message in confirmation popup.

#### Stock

* Stock move: prevented generation of invoices when 'generate Invoice from stock move' configuration is disabled in supplychain app.

#### Supply Chain

* Declaration of exchanges: will now reset fiscal year and if necessary country on change of company.
* Supplychain: fixed total W.T. of invoices generated from a stock move of a purchase order with ATI.


### Developer

Fixed ControllerMethodInterceptor to avoid adding exception management on non void and non public methods in controller.

#### Business Project

Added `PartnerAccountService` to the constructor of `ProjectGenerateInvoiceServiceImpl`.

## [8.2.13] (2025-04-17)

### Fixes
#### Base

* Updated axelor-studio dependency to 3.3.13.

#### Account

* Reconcile: removed check on tax for opening/closure.
* Invoice: fixed an issue in the BIRT report where a partner or company partner had multiple accounting situations for different companies.
* Account management: fixed fields not required in editable grid of payment mode.
* Invoice: fix due date when we save with free payment condition
* Invoice: fixed price list not filled the first time we change the partner.

#### Bank Payment

* Bank reconciliation: fixed issue when try to load bank statement in different currency than bank details.

#### CRM

* Partner: fixed 'Generate Project' button still present even with job costing app disabled.

#### Project

* Project: fixed wrong compute of full name.
* Project task: fixed npe on project task gantt views.

#### Purchase

* Supplier catalog: fixed wrong fetched information for product without catalog.

#### Sale

* Sale order: fixed 'Generate production order' button displayed when app production is deactivated.
* Sale order: fixed the issue where stock location was overwritten upon changing the partner when trading name was null.
* Sale order: fixed the alert message before confirming the sale order.

#### Stock

* Stock move line: fixed total net mass calculation when real quantities are generated.
* Stock location: fixed quantity scaling in stock location line grid and form views.

#### Supply Chain

* Sale order: fixed sale order invoicing state when a line has a negative amount.


### Developer

#### Sale

The method `confirmCheckAlert` in `SaleOrderCheckService` now returns `List<String>` instead of `String`.

## [8.2.12] (2025-04-03)

### Fixes
#### Base

* Base batch: fixed issue in recompute all addresses batch.

#### Account

* Fixed an user interface issue where it was allowed to create a new tax where the user should only be able to select one.
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


### Developer

#### Account

Added `CurrencyService` and `CurrencyScaleService` to the constructor of `PaymentVoucherControlService`.

## [8.2.11] (2025-03-20)

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

## [8.2.10] (2025-03-13)

### Fixes
#### Base

* App Base: added password widget for the certificate password.
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

#### Production

* Sale order line: fixed an issue when syncing bill of materials lines dans sub sale order lines.

#### Sale

* Sale order line: fixed an issue where discount was not applied immediatly.
* Sale order line: fixed advanced filter not displayed unless the whole page is refreshed.

#### Stock

* Stock location: fixed error when emptying parent stock location.

## [8.2.9] (2025-02-20)

### Fixes
#### Account

* Invoice payment: forbid the creation of a 0 amount payment.
* Invoice: fixed contact being readonly when the partner is an individual.
* Payment session: fixed infinite loop when searching eligible invoice terms.
* Fiscal position: fixed NPE on adding account equivalence on new record.
* Move: fixed company bank details domain when no payment mode is selected.
* Product: added product demo data for late payement invoice.
* Invoice: fixed the issue that required saving before applying cutoff dates.
* Journal: fixed error while importing france chart of accounts.
* Move: fixed the transactional error at move delete.
* Accounting batch: fixed the issue where batch accounting cut-off does not link generated tracebacks.

#### Contract

* Contract: fixed no analytic move lines on contract duplication.
* Contract version: fixed the issue where the file was being duplicated during updates.
* Contract version: fixed file link is lost when creating a new amendment.

#### Human Resource

* Timesheet: fixed an issue in lines generation from project planning where end date was not taken into account.

#### Marketing

* Campaign: fixed FR translation for 'Event start'.

#### Production

* Sale order: fixed an issue occurring when adding a title subline.
* Bill of materials: fixed BOM name in demo data, causing an issue when customizing the BOM.

#### Quality

* QI Resolution: fixed wrong translation in English for the term default.

#### Sale

* Configurator: fixed default value that was not set with configurators
* Configurator product formula: disabled test button if formula is empty.
* Sale order line: fixed default value for prodProcess.

## [8.2.8] (2025-02-06)

### Fixes
#### Base

* Update Axelor Open Platform to 7.2.6.
* Advanced export: fixed NPE when target field is empty on advanced export line.
* Unit conversion: fixed demo data for 'Box of 24 Pces'.
* Period: fixed inconsistency when filling dates on form view.
* File: fixed id to load is required for loading error.
* Pricing: use formula filtering also on linked pricing.
* City: added a helper to prevent user from getting wrong files for manual import.

#### Account

* FEC import: fixed an issue during accounting entries import where the entries were validated without any checks.
* Move: fixed description is not inherited on move lines when they are generated from mass entry lines.
* TaxPaymentMoveLine: fixed computation error when the tax line contains multi tax.

#### Budget

* Move: blocked budget distribution modification on daybooked moves.
* Budget: fixed button to display committed lines.

#### Business Production

* Operation order: fixed filter for employees.

#### Business Project

* Sale order/business project: fixed an issue on partially delivered sale order invoicing
* Sale order: fixed NPE on selecting a project.

#### Business Support

* Project version: fixed project filter to avoid conflicts on save.

#### Human Resource

* HR batch: fixed an error occurring when using 'Increment leave' batch.

#### Production

* Sale order: sublines bill of material are not customized anymore.

#### Purchase

* Purchase order line: fixed advanced filter not displayed unless the whole page is refreshed.

#### Sale

* Product: Calculated the price based on the partner price list in APIs.
* Sale order line: added missing translation for 'Customize BOM' and 'Customize production process'.
* Sale order: fixed advance payment amount during copy.

#### Stock

* Stock move: fixed split into fulfilled line and unfulfilled one total net mass issue.
* Stock move: fixed split into 2 total net mass issue.

#### Supply Chain

* Stock move: fixed an error during mass customer stock move invoicing.


### Developer

#### Sale

Added a new argument `ProductPriceListService` in `ProductPriceServiceImpl.java` constructor.

## [8.2.7] (2025-01-23)

### Fixes
#### Base

* Update Axelor Open Platform to 7.2.5.
* Updated axelor-studio dependency to 3.3.10.
* Updated axelor-utils dependency to 3.3.2, fixing an issue that could prevent PDF generation in some cases.
* Updated bouncycastle dependency to improve security.
* Currency: fixed 'codeISO' field emptying on load.
* Data sharing referential: filtered the data sharing referential lines by model.
* Data backup: fixed backup of applications config.
* Partner: fixed error in customer situation report.
* Product: fixed the incorrect domain on historical orders to display validated orders today.
* User: made phone and email fields read-only as long as the user does not have a linked contact.

#### Account

* Move line query line: removed taxes from the grid to recover the broken grid view.
* Move Line: fixed vat system edition according to account type and move origin.
* FEC Import: set VAT system on move lines during import.
* Payment voucher: close popup when receipt is printed from invoice.
* Account config: added demo data and l10n for the foreign exchange accounts.
* Accounting config: fixed translations on new company creation.

#### Business Project

* Project: fixed display of 'frameworkContractPanel' panel.
* Project: set stock locations while generating sale quotation from project.
* Invoicing project: fix project task progress in invoicing project annex report.

#### Contract

* Contract: fixed the filter on partner field to select only customer/supplier respectively on customer/supplier contracts.

#### CRM

* Partner: creating a new partner is no longer a prospect and a customer at the same time.

#### Human Resource

* Expense API: fixed an error that could occur when adding expense line to an expense.

#### Production

* Sale order: fixed an error occurring when deleting a subline linked to a customized bill of material.

#### Project

* User: hid the active project field if the project module is not installed.

#### Sale

* Sale order: added a warning when adding a subline to a title line.
* Sale order line: increased the width of the tax column for ergonomic purposes.
* Sale order: fixed the description in the sale order to use the partner's sale order comments instead of the partner general note.
* Sale order line tree grid: added missing translation for 'Add a new sale order line' and fixed title.
* Product API: when querying multiple product prices using the `/aos/product/price` endpoint, if a configuration error is detected for one product, return the price for other products instead of only returning the error.

#### Stock

* Stock move line: fixed the issue by making the availability column readonly.
* Stock move: compute total net mass when splitting lines.

#### Supply Chain

* Purchase order: fixed fiscal position when creating a purchase order from sale order.
* Stock move: stock move mass invoicing now generates an invoice with the correct invoicing address.
* Stock move invoicing: missing translation in wizard on stock move lines.
* Sale order: enable stock reservation feature on sale order editable grid.


### Developer

#### Base

The dependency `'org.bouncycastle:bcprov-jdk15on:1.70'` was replaced by `'org.bouncycastle:bcpkix-jdk18on:1.78.1'`. If you are using an AOS module with other modules that depends on `'org.bouncycastle:bcprov-jdk15on:1.70'`, please change your gradle configuration to avoid a conflict or update your dependencies.

#### Account

`UserService` has been added to the constructor of `MoveValidateServiceImpl`.

---

Added `MoveLineTaxService` dependency to `FECImporter` class.

#### Business Project

The `ProjectBusinessServiceImpl` constructor signature was modified: it now includes `ProjectBusinessServiceImpl`.

#### Supply Chain

Added 'AddressService' to the constructor of 'SaleOrderCreateServiceSupplychainImpl'.

## [8.2.6] (2025-01-09)

### Fixes
#### Base

* Email account: fixed NPE that occurred when setting 'Default account' to false.
* City: fixed issue where country was not filled when adding a new city from address form.
* Registration number template: initialized 'Starting position in the registration number' to 1 on new record to prevent save errors.
* Partner: fixed error when duplicating a partner without a picture.
* Partner: hide 'Accounting situation' panel when the partner doesn't belong to any company.
* Birt reports: fixed hard coded db schema in native queries.
* Message: fixed data-grid and data-form in 'message.related.to.select'.

#### Account

* Payment voucher: fixed error when the invoice term has no related invoices.
* Accounting batch: result move computation query takes into account accounted entries.
* Move / Moveline: added additional control to avoid unbalancing input of entries on general / special accounts at validation
* Account: fixed hard coded db schema in native queries.
* Payment voucher: fixed unexpected pop-up mentioning no record selected while there is at least one.
* Invoice: removed incoherent mention of refund line in printing when it's not originated from actual invoice.
* Invoice payment: fixed financial discount when changing payment date.
* Bank reconciliation: fixed the filter to display move lines with no function origin selected and hide it when already reconciled in different currency than move in 'Bank reconciliation statement' report.
* Accounting report type: fixed comparison in custom report types demo data.

#### Budget

* Budget: fixed demo data for budget and budget level.

#### Contract

* Contract: fixed display of 'consumptionLineList' for supplier contracts.

#### Human Resource

* TimesheetLine/Project: fixed computation of durations.

#### Production

* Manufacturing Order: fixed a issue where a infinite loop could occur on planification if operation had no duration with finite capacity (now displays an error).
* Production order: fixed an issue with the quantities of the generated manuf orders from a sale order.

#### Purchase

* Purchase request line: fixed domain for product.

#### Quality

* Quality improvement: fixed readonly condition for company.

#### Sale

* Sale order: fixed scale and title line issue in message template demo data.
* Sale configurator: fixed issue where formula was not added when verifying the groovy script then directly exiting the pop-up

#### Stock

* Logistical form: fixed unique constraint violation error.
* Stock move: fixed the stock move merge process to work when there are no errors.
* Logistical form: fixed sequence error.

#### Supply Chain

* Stock move: fixed invoiced and remaining quantity in wizard when partially invoicing
* MRP: purchase orders generated by the MRP have the fiscal position correctly filled.


### Developer

#### Stock

Method signature have changed in StockMoveMergingService.class :

```java
public String canMerge(List<StockMove> stockMoveList);
```

became

```java
public List<String> canMerge(List<StockMove> stockMoveList);
```

#### Supply Chain

`UnitConversionService` has been added to the constructor of `StockMoveInvoiceServiceImpl`.

## [8.2.5] (2024-12-20)

### Fixes
#### Account

* Invoice: fixed an issue preventing advance payment invoice refund.
* Invoice: fixed a regression preventing invoices refund.

## [8.2.4] (2024-12-19)

### Fixes
#### Base

* Updated Axelor Open Platform to 7.2.4.
* Updated studio module to 3.3.8.
* Product: fixed french translation for 'Product'.
* Partner: fixed siren, nic and tax number computation for demo data.
* Partner: fixed hidden panels when partner is not only prospect.
* Partner: fixed so that customer can be converted to other partner types.
* Partner link: made partners required to avoid errors.

#### Account

* Accounting batch: fixed 'Realize fixed asset lines' batch doesn't work when start date and end date are same.
* Invoice: fixed payment voucher confirmation with auto reconcile config enabled.
* Invoice: added server-side controls to prevent forbidden status changes, for example cancelling a ventilated invoice.
* Accounting report: fixed blank values in 'Summary of gross values and depreciation' report.
* Invoice: fixed display of company bank details when we create an invoice payment.
* Accounting report: fixed 'Aged balance' report doesn't display the values of due amounts in the delay columns.
* Invoice payment: fixed an issue where payment amount wrongly reset to 0 after changing payment date two times.
* Invoice: fixed an issue with amount remaining being reinitialized from ventilated invoice.

#### Budget

* Invoice: fixed error when there is no product on the invoice line while budget computation.

#### Business Project

* Business project: fixed an error occurring when opening sale order line in financial data
* Project/Project task: fixed planned and spent time when time units are not the same.

#### Contract

* Contract template: fixed recurring product display for supplier contract.
* Contract: cut off dates are now correctly filled in invoice lines.

#### CRM

* Partner: fixed the display issue in event panel when images are attached in the event description.

#### Human Resource

* Lunch voucher: leave request with hour leave reason type are now ignored.
* HR API: fill expense type select on creation
* Expense API: fixed the error when an employee did not have a contract while creating an expense.

#### Production

* Manufacturing order: fixed no such field 'typeSelect' error when selecting bill of material and prod process when maintenance module is missing.
* Purchase order: fixed wrong domain for the dashlet on outsourcing tab.
* Manufacturing order: merging manufacturing orders now correctly takes into account scheduling configuration.

#### Project

* Project template: fixed duplicated invoicing tab when creating a project template as a business project.
* Gantt view: fixed an issue where changing the progression did not change it on the project task.

#### Quality

* Quality alert: fixed NPE when opening kanban view.

#### Sale

* Sale order: fixed concurrency error when adding a pack on a sale order.

#### Supply Chain

* Supplychain: invoices generated from a stock move of a sale order are now correctly generated with the advance payment of the sale order.
* Timetable: timetable are now correctly updated to not invoiced when a linked credit note has been ventilated.
* Sale order: a sale order with invoiced timetables cannot be edited anymore.
* Sale order invoicing: fixed the missing title 'To invoice' on the corresponding column when invoicing time table lines from a sale order.


### Developer

#### Account

`action-invoice-record-draft` was replaced by `action-invoice-method-back-to-draft`

#### Contract

`AppAccountService` has been added to the constructor of `ContractInvoicingServiceImpl`.

## [8.2.3] (2024-11-28)

### Fixes
#### Base

* Updated studio module to 3.3.6.
* Template: changed title from 'Print Template' to 'Print template'.
* Group view: fixed inexistant field 'canViewCollaboration' to display only with the Enterprise Edition.
* Rest API: fixed response message translation.
* City: fixed geonames import errors.

#### Account

* Fixed Asset: fixed degressive computation with prorata temporis of fixed asset starting in february.
* Move: fixed condition to display payment voucher and payment session according to functional origin.
* Invoice: fixed wrong total gross amount on birt report.
* Fixed Asset: fixed accounting value when we validate fixed asset without depreciation plan.
* FEC Import: fixed move line without accounting date when importing from fec import
* Move: fixed description when we generate invoice move.

#### Budget

* Budget: fixed help of 'Committed amount' in budget level and global budget.

#### Business Project

* Project template: show the generated business project with the right view.

#### Contract

* Contract: prorata is now based on invoice period start date instead of contract start date.

#### Human Resource

* Lunch voucher: fixed computation for leaves with overlapping periods.
* Timesheet: fixed timeseet duplicate creation.
* Employee: checked unicity constraints when creating user at the end of employee creation process.
* HR batch: fixed an error occurring when launching Leave Management Reset Batch.
* ProjectPlanningTime: added missing computation duration at change of time units.
* Leave request: fixed future quantity day computation when sending a leave request.
* HR: added employee and employment contract in 'Related to' of message.
* Timesheet: fixed NPE because of daily limit configuration.

#### Production

* Production API: fixed error while fetching consumed products.

#### Purchase

* Purchase order: fixed an error occurring when generating purchase request if the order was not saved

#### Sale

* Sale API: fixed issue related to complementary products price computation.
* Sale order: a partner blocked from sale order is now correctly filtered out from the list of customers
* Sale order: fixed error related to budget when finalizing a quotation.

#### Stock

* Stock move line: fixed the tracking number issue during the inline split.

#### Supply Chain

* Sale order: fixed sale order with a title line tagged as partially invoiced while it has been totally invoiced.
* Invoice: fixed the note and proforma comments on the invoice based on stock moves generated from sale orders.
* Credit note: fixed an issue on stock move credit note with different purchase and stock unit.
* Purchase order line: fixed product name when generating purchase order from sale order.


### Developer

#### Human Resource

Added the employeeRepository in the TimesheetQueryServiceImpl constructor

---

Renamed `action-condition-user-validCode` to `action-user-method-validate-code`.

---

Removed `action-project-planning-time-record-start-time-onchange`, replaced by `action-project-planning-time-method-compute-planned-time`.

## [8.2.2] (2024-11-14)

### Fixes
#### Base

* Updated Axelor Open Platform to 7.2.3.
* Updated studio module to version 3.3.5.
* Updated message module to version 3.2.2.
* Updated utils module to version 3.3.0.
* Partner: tax number will be displayed for suppliers too along with customers.
* Tag: added default color tag
* Unit conversion: fixed NPE when getting the coefficient for a unit conversion of type coeff with value zero.
* Tag: allowed to select tag when concerned model is empty on tag.
* Template: fixed an issue where 'Test template' button was always readonly with custom model.
* Pricing: fixed the product category filter in pricing formula.

#### Account

* Invoice: fixed invoice printing that does not work without business project module.
* Analytic move line query: fixed always readonly button in 'Analytic move lines to reverse' dashlet.
* Accounting export: fixed skipped lines on accounting export when we have more than 10 000 lines.
* Accounting report: replaced move line partner full name with partner sequence and partner full name in reports.
* Invoice: fixed NPE when clicking on print button on grid lines.
* Move/Analytic: record analytic account on moveline on the reverse move
* Partner: fixed an issue where the partner balance was wrong after an unreconcile.
* Move: blocked generation of reverse move if reverse move date is before the date of the move to reverse.
* FEC Import: fixed importing moves with a validation date.
* Bank reconciliation line: fixed an issue where too much memory could be used when filtering move lines.
* Analytic move line query: optimized filter to handle high data volumes efficiently.

#### Bank Payment

* Bank statement: fixed error when importing bank statement with negative final balance.
* Move/Payment session: added bank order origin in the generated move if needed.

#### Contract

* Contract/Invoice: fixed analytic wrong management on contract line.

#### Human Resource

* Expense API: expense line creation with a manual distance is no longer overriden by computed distance.
* Expense: fixed an error occurring when cancelling an expense payment linked to a bank order.

#### Production

* Sale order: no personalized BOM will be created if the option is not enabled.

#### Project

* Project: fixed NPE when opening project in project activity dashboard.

#### Purchase

* Purchase order lines: fixed an issue where the warning about default supplier was not displayed.

#### Quality

* Quality: fixed translation of 'Check conformity'.

#### Sale

* Sale order: fixed display of 'Send email' button when record is not saved.

#### Stock

* Stock move line: fixed tracking number domain filter.

#### Supply Chain

* Stock move: fixed issue where partial invoicing was not working.
* Sale order: fixed an issue where the invoicing of sale order lines was blocked.


### Developer

#### Base

- Action "action-tag-method-set-concerned-model" have been replaced by "action-tag-method-on-new".
- And setDefaultConcernedModel method in TagController have been renamed by onNew

#### Supply Chain

- Added new arguments to SaleOrderInvoiceService.displayErrorMessageIfSaleOrderIsInvoiceable()
- Updated SaleOrderInvoiceService.computeAmountToInvoice visibility to protected and removed it from interface

## [8.2.1] (2024-10-31)

### Fixes
#### Base

* Sequence: fixed draft prefix when checking for the draft sequence number.
* Birt report: fixed number formatting for excel format.
* Partner: added check on parent partner to avoid same partner as parent.
* Partner: fixed NPE when manually adding an accounting situation.

#### Account

* Move: fixed blocked accounting when missing budget alert is required and account type is not one of the charge, income, immobilisation.
* Accounting batch: fixed multiple auto lettering.
* Accounting cut off batch: fixed wrong analytic distribution and axis on generated moves.
* Partner:  fixed automatic account creation when partner is prospect based on 'Automatic partner account creation mode' in account config.
* Invoice: fixed an issue where too much memory could be used when displaying customer invoice lines.
* Invoice/Move: recompute currency rate of movelines after invoice ventilation.

#### Budget

* Purchase order: removed required condition on company department.

#### Contract

* Contract: fixed invoicing contract with revaluation and prorata enabled.
* Contract: fixed a issue when generating a sale order from a contract

#### CRM

* Opportunity: fixed filter on contact domain.
* Lead: fixed an issue preventing lead conversion when having only CRM module.
* Lead: fixed address while converting a lead to a prospect.

#### Human Resource

* Expense: fixed an issue preventing to go to reimbursed status with a payment mode generating a bank order.

#### Production

* Prod process: added workflow buttons instead of clickable status.
* Manufacturing order: fixed issue when updating quantity in manufacturing order.

#### Purchase

* Purchase order: fixed value of 'total without tax' in birt report.

#### Sale

* Sale order: fixed an issue preventing from editing a sale order with editable grid and pending order modification enabled
* Sale order: fixed sale order printing when only sale module is used, without supplychain.

#### Stock

* Stock move: fixed an error occurring when splitting a stock move line.

#### Supply Chain

* Invoice: fixed invoice line price generated from stock move with different unit.


### Developer

#### Budget

SaleOrderDummyBudgetServiceImpl class was removed, as well as following actions:

- `action-budget-group-purchase-order-on-new-actions`
- `action-budget-purchase-order-record-load-budget-key-config`
- `action-group-budget-saleorder-onload`
- `action-budget-sale-order-record-load-budget-key-config`

## [8.2.0] (2024-10-18)

### Features
#### Base

* Updated Axelor Open Platform to 7.2. You can find all information on this release [here](https://github.com/axelor/axelor-open-platform/blob/7.2/CHANGELOG.md).
* Tag: added a new Tag model to help label other elements (present in lead, partner, project tasks, accounting reports, quality alert).
* Localization: improved localization support, managing date and numbers format.
* Product: added the possibility to get product prices managing currency and taxes from the API.

#### Sale

* Sale order: added a new editable tree view for sale order lines.

A new configuration is now available in App Sale to choose the normal grid view or the editable tree view for sale order lines. That means that, using this view, any sale order line can be subdivised into other sale order lines. This can be used to compute a sale order line price from its children lines. Children lines can be added manually, but are also automatically generated from information in the bill of materials.

* Cart: added a new cart feature, allowing an user to create a sale order by selecting products from other form.
* Sale order: updated the API to add the possibility to create sale order including lines, and update its status.
* Partner: added DSO (Days Sales Outstanding) in partner form. The computation is based on customer invoices.

#### Account

* Foreign exchange gains/losses: correctly generate extra accounting entries when currency rate has changed between the invoice date and the payment date.
* Tax: managed taxe by amount. This can be useful when tax amount in supplier invoices is not equal to the tax computation done by Axelor Open Suite.
* Invoice: added late payment interests invoice generation.
* Fixed asset: added a new method to split fixed assets by unit.

#### Stock

* Added "Mass stock move" form view, which is an helper to manage to move products from and to multiple stock locations.

#### Project

* Project task: managed task status by project category
* Project task: added progression computation from category and/or the task status.
* Project task: added a check list that will be available on project templates.
* Project: managed active project per user, with a quick menu to allow a user to change their active project.

#### Mobile Settings

* New mobile app Sales.
* New mobile app Project.
* Added a new chart type.

### Changes

#### Base

* Updated app icons on menus.
* Printing template: added a script to configure dynamically the name of files generated by the template.
* Data sharing: added new technical models to manage connectors configuration and synchronization.
* Site: managed trading name on sites.
* Address: updated our API to allow search/creation of an address.

#### CRM

* Lead: it is now possible to change the status of a closed lead to the default status.
* Tag: LeadTag is now replaced by Tag.

#### Purchase

* Purchase order: added the possibility to split a purchase order.

#### Sale

* Sale order: added a new button to recompute prices using pricing scales and price lists on the sale order.

#### Account

* Invoice: on supplier invoices, added a new wizard to fill invoice number and date before ventilation.
* Accounting report: added filters on tags on accounting report.
* Closure/Opening fiscal year batch: added a warning before launching the process if we have daybook entries.

#### Bank payment

* Bank reconciliation: allow users to generating accounting entries by using the button "Auto accounting".
* Bank order: added an option to encrypt the generated the bank order file on the server.

#### Stock

* Stock: updated the API with a new query to manage line splitting on supplier arrivals.

#### Supply Chain

* Partner: it is now possible, for a partner which is both a supplier and a customer, to have different payment condition for incoming and outgoing payments.

#### Production

* Prod process: added workflow buttons instead of clickable status.

#### Project

* Tag: replaced ProjectTaskTag by Tag.
* Project task: split personalized fields depending on where they are configured and added personalized fields for project categories.

#### Business project

* Split views between project and business projects to improve user interface.

#### Mobile Settings

* Mobile menu: it is now possible to manage mobile app menus from the web interface.
* Chart: added a new "indicator" type for mobile charts.

### Fixes

#### Base

* Trading name: fixed relationship between trading name and company: now a trading name only belongs to one company.
* Site: fixed relationship between site and company: now a site only belongs to one company.

#### HR

* Payroll preparation: multiple fixes.

#### Sale

* Sale order: technically many changes were made to refactor XML actions into single action-method.

#### Account

* Deposit slip: manage bank details in generated accounting entries.
* Payment: use correctly the payment date instead of today date when computing currency rate.

[8.2.30]: https://github.com/axelor/axelor-open-suite/compare/v8.2.29...v8.2.30
[8.2.29]: https://github.com/axelor/axelor-open-suite/compare/v8.2.28...v8.2.29
[8.2.28]: https://github.com/axelor/axelor-open-suite/compare/v8.2.27...v8.2.28
[8.2.27]: https://github.com/axelor/axelor-open-suite/compare/v8.2.26...v8.2.27
[8.2.26]: https://github.com/axelor/axelor-open-suite/compare/v8.2.25...v8.2.26
[8.2.25]: https://github.com/axelor/axelor-open-suite/compare/v8.2.24...v8.2.25
[8.2.24]: https://github.com/axelor/axelor-open-suite/compare/v8.2.23...v8.2.24
[8.2.23]: https://github.com/axelor/axelor-open-suite/compare/v8.2.22...v8.2.23
[8.2.22]: https://github.com/axelor/axelor-open-suite/compare/v8.2.21...v8.2.22
[8.2.21]: https://github.com/axelor/axelor-open-suite/compare/v8.2.20...v8.2.21
[8.2.20]: https://github.com/axelor/axelor-open-suite/compare/v8.2.19...v8.2.20
[8.2.19]: https://github.com/axelor/axelor-open-suite/compare/v8.2.18...v8.2.19
[8.2.18]: https://github.com/axelor/axelor-open-suite/compare/v8.2.17...v8.2.18
[8.2.17]: https://github.com/axelor/axelor-open-suite/compare/v8.2.16...v8.2.17
[8.2.16]: https://github.com/axelor/axelor-open-suite/compare/v8.2.15...v8.2.16
[8.2.15]: https://github.com/axelor/axelor-open-suite/compare/v8.2.14...v8.2.15
[8.2.14]: https://github.com/axelor/axelor-open-suite/compare/v8.2.13...v8.2.14
[8.2.13]: https://github.com/axelor/axelor-open-suite/compare/v8.2.12...v8.2.13
[8.2.12]: https://github.com/axelor/axelor-open-suite/compare/v8.2.11...v8.2.12
[8.2.11]: https://github.com/axelor/axelor-open-suite/compare/v8.2.10...v8.2.11
[8.2.10]: https://github.com/axelor/axelor-open-suite/compare/v8.2.9...v8.2.10
[8.2.9]: https://github.com/axelor/axelor-open-suite/compare/v8.2.8...v8.2.9
[8.2.8]: https://github.com/axelor/axelor-open-suite/compare/v8.2.7...v8.2.8
[8.2.7]: https://github.com/axelor/axelor-open-suite/compare/v8.2.6...v8.2.7
[8.2.6]: https://github.com/axelor/axelor-open-suite/compare/v8.2.5...v8.2.6
[8.2.5]: https://github.com/axelor/axelor-open-suite/compare/v8.2.4...v8.2.5
[8.2.4]: https://github.com/axelor/axelor-open-suite/compare/v8.2.3...v8.2.4
[8.2.3]: https://github.com/axelor/axelor-open-suite/compare/v8.2.2...v8.2.3
[8.2.2]: https://github.com/axelor/axelor-open-suite/compare/v8.2.1...v8.2.2
[8.2.1]: https://github.com/axelor/axelor-open-suite/compare/v8.2.0...v8.2.1
[8.2.0]: https://github.com/axelor/axelor-open-suite/compare/v8.1.9...v8.2.0
