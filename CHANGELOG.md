## [8.4.6] (2025-09-11)

### Fixes
#### Base

* Partner: fixed accounting situations when merging partners.
* Databackup: fixed a potential security breach when restoring a backup.
* Product: will now always display procurement method.

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

#### Contract

* Contract: fixed an issue on contract history opening.

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

## [8.4.5] (2025-08-28)

### Fixes
#### Base

* Data backup : fixed wrong title display for integer and long fields.

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
* AnalyticAccount/MoveLine/InvoiceLine: fixed wrong required condition on the analytic axis accounts in free distribution.
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
* Sale order line: fixed a message notifying that no manufacturing order have been generated when all lines are blocked.
* Manufacturing order: fixed an issue where the purchase order date was not set when generated automatically during planning.
* Cost calculation: fixed an issue where the cost price was not divided by the calculation quantity when the BOM quantity was greater than 1.
* Manufacturing order: fixed an issue where an outsourced product's price from purchase order was not correctly priced in cost sheets.
* Manufacturing order: fixed an error where a LazyInitializationException occurred during partial finish.
* Cost calculation: fixed an issue where the child BOM quantity was not correctly multiplied when the BOM calculation quantity was greater than 1.

#### Purchase

* Call tender need: now set unit of the need when possible

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

## [8.4.4] (2025-08-14)

### Fixes
#### Base

* Partner: fixed error when merging two partners.

#### Account

* Closure assistant : fixed outrun of year computation doesn't take into account all accountTypes.
* Account management: fixed interbank code issue on 'Direct Debit' payment mode.
* Accounting report: fixed the issue related to amount in Analytic general ledger report.
* ANALYTICDISTRIBUTIONTEMPLATE : duplicated templates shouldn't be visible

#### Bank Payment

* Bank statement: fixed demo data to get dynamic dates and corrected interbank code.
* BankDetails/Umr : fixed the bank details domain on DD payment modes
* Bank statement rule: fixed partner fetch method demo data.

#### Budget

* Invoice/Move/Budget : Realized amounts needs to be computed with movelines datas
* MoveLine/Budget : fix budget distribution compute at budget change
* PurchaseOrder/Budget : fixed the use of order date to fetch the budget line associed to.

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
* Sale order : hide delivery panel when we select a service product.

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

#### Supply Chain

Added 'SaleOrderLineViewServiceSupplychain' to 'SaleOrderLineSupplychainOnLoadServiceImpl' constructor.

## [8.4.3] (2025-07-31)

### Fixes
#### Base

* Event: creating an event now automatically fill the user field.

#### Account

* PaymentSession/Refund : fixed the fact that refund were not being taken into account during the payment session
* FIXEDASSET : Issue with imported fixed assets using prorata temporis
* MoveLine: fixed invoice term due date when we update move line's due date with free payment condition.

#### Bank Payment

* Bank payment: removed some files that are not necessary anymore.
* Bank details: fixed an issue where a bank detail could not be selected with a direct debit payment mode

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
* Purchase order: fixed interco configuration when merging purchase orders.
* Timetable: removed unnecessary check when changing amount.
* Product: fixed wrong calculation of available stock when the 'Manage stock reservation' configuration is enabled.
* Sale order: fixed invoiced partner and delivery partner when creating a sale order from interco, taking into account the partner relations.

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

## [8.4.2] (2025-07-18)

### Fixes
#### Base

* Price list line: fixed the display issue with the 'Amount' title.
* Added new default theme.
* App Base: fixed French translation for some configuration settings.
* Company: Added support for dark logo
* Siren API: fixed message when having wrong credentials.
* Theme: fixed modern theme menu color.

#### Account

* INVOICE : Wrong title displayed on PDF for a credit note issued on an advance payment.
* Partner/AccountingSituation : Correct anomaly causing account fields to be empty when generating automatically a new accounting situation on the partner (when adding a new company)
* AccountingBatch/BillOfExchange : Fixed the bill of exchange data type select.
* Invoice: fixed an issue where the payment date was not emptied when duplicate or generate from a paid invoice.
* Accounting report: fixed moveStatusSelect filter display issue after a status is being selected.

#### Bank Payment

* Invoice/BillOfExchange: fixed the fact the placement move was a Sale move and not a Payment move
* Move/MassEntry : fixed error when changing partner bank details on a move line mass entry.

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
* Sale order: changing the bill of material and production process in a sale order line now correctly updates the sale order line details.
* MRP: fixed an issue where bill of material line marked as not stock managed where taken into account for computation.
* Prod process: Update stock locations on company change
* Bill of material: fixed the error message when updating cost price and the price was 0.
* Sale order: fixed cost price computation of operation sale order line details with a human and machine work center
* Cost calculation: prevent product creation.

#### Purchase

* Call for tender: now sorted by desc creation date
* Call for tender: fixed translations and scaling for prices and units.

#### Sale

* Sale: fixed error message when currency conversion is not found.
* Sale order: fixed read only condition for add pack button when there are no sale order lines and corrected action.
* Sale order: fixed read only condition for add line from configurator button when there are no sale order lines.

#### Stock

* Stock: fixed stock location lines by product panel in stock details by product view.
* Incoterm: set incoterm required in sale order only for company client partner.
* STOCK/LOGISTICALFORM : Fix html column headers titles on line grid

#### Supply Chain

* Sale / Purchase / Stock: fixed some views where quantity and price decimal config wasn't being used.
* Sale order: timetable amount take into account taxes or not depending on the configuration.
* SaleOrderLine : fixed technical error at the line creation
* Sale order: fixed interco configuration when merging sale orders.


### Developer

#### Base

Migration script to add theme logo mode and logo fields-

```
  ALTER TABLE base_company ADD COLUMN dark_logo BIGINT;
  ALTER TABLE base_company ADD COLUMN light_logo BIGINT;

  ALTER TABLE meta_theme ADD COLUMN logo_mode VARCHAR(255);

  CREATE INDEX base_company_dark_logo_idx ON base_company USING btree (dark_logo);
  CREATE INDEX base_company_light_logo_idx ON base_company USING btree (light_logo);

  ALTER TABLE base_company ADD CONSTRAINT fk_5mjhnp9wy6tb96qgfjlfvfp5m 
    FOREIGN KEY (dark_logo) REFERENCES meta_file(id) NOT VALID;
  ALTER TABLE base_company VALIDATE CONSTRAINT fk_5mjhnp9wy6tb96qgfjlfvfp5m;

  ALTER TABLE base_company ADD CONSTRAINT fk_fxn0yfj1un0siwp3yqo3kymbo 
    FOREIGN KEY (light_logo) REFERENCES meta_file(id) NOT VALID;
  ALTER TABLE base_company VALIDATE CONSTRAINT fk_fxn0yfj1un0siwp3yqo3kymbo;

```

#### Business Project

Removed the onLoad extension from the SaleOrder form with id `business-project-sale-order-form` as it was a duplicate.

#### Production

UnitCostCalcLineService.createUnitCostCalcLine takes qtyRatio as argument

## [8.4.1] (2025-07-03)

### Fixes
#### Base

* Data Backup: fixed translations and added help for some fields.

#### Account

* MoveLine/TaxSet: fixed technical error generating movelines without taxes in some process
* AnalyticMoveLine/MoveLine : fixed the reset of analytic accounts on moveline when changing it on analytic move line
* MassEntryMove: fixed the error message list.
* Move: improve move validation time fixing global audit tracker
* MassEntryMove: fixed the counterpart process without saving the move.

#### Human Resource

* Issue on Windows when we try to build the AOS project.

#### Production

* Sale order: display quantity and total duration for operation sale order line details.

#### Purchase

* Purchase order line: fixed an issue where quantity was not reset to valid when managing multiple quantity.
* Call for tender: added an option to activate the feature.

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

---

MassEntryMoveCreateService.createMoveFromMassEntryList now take a move instead of the move id

#### Purchase

The following script must be played if the database is not auto updated:
```
ALTER TABLE studio_app_purchase ADD COLUMN manage_call_for_tender boolean;
```

## [8.4.0] (2025-06-26)

### Features
#### Base

* Updated Axelor Open Platform to 7.4. You can find all information on this release [here](https://github.com/axelor/axelor-open-platform/blob/7.4/CHANGELOG.md).
* Updated Axelor Studio dependency to 3.5. You can find all information on this release [here](https://github.com/axelor/axelor-studio/blob/release/3.5/CHANGELOG.md).


#### Purchase

* Added a new call for tenders feature.

#### Sale

* Sale order: allowed for partial validation of quotation line. Quotation and orders are separated if the configuration is activated.
* Sale order: managed carrier prices by freight carrier mode.

#### Account

* Partner: added different UMRs for different bank details on the same partner.
* Analytic Move line: in the move line reverse values, added a percentage on each line to be able to create multiple lines per axis.
* Fixed Asset: implemented mass disposal process.
* Invoice: managed company tax number on the invoice.

#### Production

* Sale order: added automatic creation of production order from sale order.

#### Budget

* Budget: allowed to split the amount on multiple periods.

 
[8.4.6]: https://github.com/axelor/axelor-open-suite/compare/v8.4.5...v8.4.6
[8.4.5]: https://github.com/axelor/axelor-open-suite/compare/v8.4.4...v8.4.5
[8.4.4]: https://github.com/axelor/axelor-open-suite/compare/v8.4.3...v8.4.4
[8.4.3]: https://github.com/axelor/axelor-open-suite/compare/v8.4.2...v8.4.3
[8.4.2]: https://github.com/axelor/axelor-open-suite/compare/v8.4.1...v8.4.2
[8.4.1]: https://github.com/axelor/axelor-open-suite/compare/v8.4.0...v8.4.1
[8.4.0]: https://github.com/axelor/axelor-open-suite/compare/v8.3.9...v8.4.0
