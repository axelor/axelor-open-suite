## [7.2.50] (2025-10-09)

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
* AccountingBatch/CuttOff : fixed the analytic management in cut off batch.
* ANALYTIC: Updated analytic journal select domain method.

#### Bank Payment

* Move : fixed bank reconciled amount set when we don't have a linked bank reconciliation.

#### Human Resource

* ExpenseLine : Remove action who doesn't exist

#### Mobile Settings

* App mobile settings: fixed deleted modules in web are bugged in mobile.

#### Production

* Manuf order: managed partial consumption in manuf order from consumption button too.
* Bill of material line: correct domain of product field in bill of material line grid.
* Operation order: fixed display condition of 'To invoice' to show only when 'Manage business production' is enable in app manufacturing.

#### Quality

* Quality: fixed French translation in Quality improvement form view.

#### Stock

* Product: fixed Stock and Stock history charts to exclude cancelled stock moves and take real qty into account.
* Stock move: fixed shipping coef not working on supplier arrival having purchase order.
* Product: fixed quantity in stock report with correctly converted unit.


### Developer

#### Account

Added AnalyticMoveLineRepository in MoveLineComputeAnalyticServiceImpl constructor. Added copyAnalyticsDataFromMoveLine method in MoveLineComputeAnalyticService.

---

DELETE FROM meta_action where name ='action-attrs-domain-onselect-journal-analytic-move-line';

## [7.2.49] (2025-09-25)

### Fixes
#### Base

* User: fixed trading name not filtered on company.

#### Account

* Deposit slip: fixed the date filter to use the cheque's due date rather than the deposit slip date.
* Move line: added an error message when trying to reconcile moves from incompatible accounts.
* Partner: fixed French translation for 'Unpaid invoices' and 'View all unpaid invoices'.
* Fixed asset: allow a number of depreciation of 0.
* Payment scheduler: fixed an issue where records were created without a sequence.
* Fixed asset: fixed fixed asset creation from invoice.
* ACCOUNT : Fix status mass update
* Payment schedule: fixed an issue where values were not reset during duplication.
* Accounting report: fixed domain filter for analytic accounts and analytic axis.
* Deposit slip: fixed display condition for deposit slip date.
* Invoice: fixed an issue where financial discount was not emptied when changing partner.

#### Contract

* Contract line: fixed display issue of 'Invoice from consumption' field.

#### CRM

* Lead: prevent users from reopening converted lead in kanban view.

#### Production

* Production API : fixed request to fetch consumed products was not working when 'Manage consumed products on operations' is enabled.

#### Sale

* Sale config: client box and legal note fields are now translatable.

#### Supply Chain

* Sale order: fixed delivery state computation to ignore sale order lines with qty 0.

#### Talent

* Sequence: added missing selection value for job position.


### Developer

#### Account

Removed action that is now useless since it will be replaced by the repository save.

DELETE FROM meta_action WHERE name = 'action-payment-schedule-payment-schedule-id';

## [7.2.48] (2025-09-11)

### Fixes
#### Base

* Partner: fixed accounting situations when merging partners.
* Databackup: fixed a potential security breach when restoring a backup.

#### Account

* Account: removed export button as it has no action linked.
* Fixed asset: fixed an issue where periodicity type was not copied if fiscal plan was not selected.
* Accounting report: fixed detailed customers balance report to exclude suppliers and supplier invoices.

#### Budget

* Budget app: fixed an issue on app installation.

#### Human Resource

* Expense : disable the multi currency management until 8.5
* Lunch voucher: fixed an issue where computation did not deduct ventilated or reimbursed expenses.


### Developer

#### Human Resource

If you have some expense with another currency than the company currency, you will need a script to reset it.

Script : 
UPDATE hr_expense e SET currency = c.currency
FROM base_company c WHERE c.id = e.company AND e.currency != c.currency;
DELETE FROM meta_action WHERE name = 'action-expense-attrs-kilometric-panel-visibility';

## [7.2.47] (2025-08-28)

### Fixes
#### Account

* Move : fix technical error when accounting an empty move
* Accounting report: fixed missing assets with none depreciation plan and net carrying amount on 'Gross value and depreciations' report.
* AnalyticDistributionTemplate : Remove the possibility of selecting duplicate on analytic distribution template fields.

#### Budget

* GlobalBudget/BudgetLevel/Budget : update amounts when updating budget lines values

#### CRM

* Lead: lead partner are now always prospect when converted.

#### Human Resource

* Expense: mail notification are now correctly sent when sending, validating or refusing an expense from API.

#### Production

* Manufacturing order: fixed an issue where the purchase order date was not set when generated automatically during planning.

#### Sale

* Sales order: fixed status filter on 'My Sales Orders' dashboard.

#### Stock

* Stock move: fixed an error when clicking 'Refresh the products net mass' without saving the record.

#### Supply Chain

* PurchaseOrderLine/Analytic: fixed an issue where the analytic was required on a title line.
* Sale order: prevent already invoiced lines to be invoiced again.
* Stock move: removed the toolbar from the 'Mass Stock Move Invoicing' wizard views.

## [7.2.46] (2025-08-14)

### Fixes
#### Account

* Fixed an issue where imported fixed assets did not calculate depreciation correctly when using prorata temporis
* Closure assistant : fixed outrun of year computation doesn't take into account all accountTypes.
* Account management: fixed interbank code issue on 'Direct Debit' payment mode.
* ANALYTICDISTRIBUTIONTEMPLATE : duplicated templates shouldn't be visible

#### Business Project

* PurchaseOrder : fixed technical error when saving a project or a business project on a purchase order.

#### Human Resource

* Expense: fixed display cancel button in form view.

#### Stock

* Product: fixed unit conversion for 'Stock history' chart.

#### Supply Chain

* Sale order: fixed stock location on change of company.


### Developer

#### Account

Changed the AccountService.computeBalance method parameter. Now using a list of account types instead of an account type.

---

Added AnalyticDistributionTemplateRepository and AnalyticMoveLineService in AnalyticAttrsServiceImpl.
Added AnalyticAttrsService in MoveLineAttrsServiceImpl.
Added parameter 'moveline' in MoveLineAttrsServiceImpl.addAnalyticDistributionTemplateDomain.
Added parameter 'moveLine' in MoveLineGroupServiceImpl.getAnalyticDistributionTemplateOnSelectAttrsMap.

DELETE FROM meta_action WHERE name LIKE 'action-purchase-order-line-attrs-set-domain-analytic-distribution-template';

## [7.2.45] (2025-07-31)

### Fixes
#### Base

* Event: creating an event now automatically fill the user field.

#### Bank Payment

* Bank payment: removed some files that are not necessary anymore.

#### Budget

* Budget: fixed the 'Display realized with no po' button filter

#### Contract

* Contract batch: fixed an issue on customer contract invoicing when grouped invoicing was false.

#### Project

* Project : fixed performance technical issue on project action-attrs

#### Sale

* Sale order:  fixed the issue where partner complementary product lines were being duplicated with each new version creation.
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
* Sale order: fixed invoiced partner and delivery partner when creating a sale order from interco, taking into account the partner relations.


### Developer

#### Sale

Added new PackLine parameter to SaleOrderLinePackService#fillPriceFromPackLine, SaleOrderLinePackService#getExTaxUnitPriceFromPackLine and SaleOrderLinePackService#getInTaxUnitPriceFromPackLine.
Added new Currency parameter to SaleOrderLinePackServiceImpl#getUnitPriceFromPackLine.

#### Supply Chain

Added the SaleOrderSupplychainService in the IntercoServiceImpl constructor

## [7.2.44] (2025-07-18)

### Fixes
#### Base

* App Base: fixed French translation for 'Daily Work Hours'
* Price list line: fixed the display issue with the 'Amount' title.

#### Account

* Partner/AccountingSituation : Correct anomaly causing account fields to be empty when generating automatically a new accounting situation on the partner (when adding a new company)
* AccountingBatch/BillOfExchange : Fixed the bill of exchange data type select.
* Invoice: fixed an issue where the payment date was not emptied when duplicate or generate from a paid invoice.

#### Bank Payment

* Invoice/BillOfExchange: fixed the fact the placement move was a Sale move and not a Payment move

#### Business Project

* Sale order: removed the unnecessary form view extension in business project
* Sale order: removed a duplicated extension in business project module.

#### Production

* Sales and Operations Planning: added missing title on 'Generate MPS forecasts' wizard
* MRP: fixed an issue where bill of material line marked as not stock managed where taken into account for computation.
* Cost calculation: prevent product creation.

#### Stock

* Stock: fixed stock location lines by product panel in stock details by product view.
* STOCK/LOGISTICALFORM : Fix html column headers titles on line grid

#### Supply Chain

* Sale / Purchase / Stock: fixed some views where quantity and price decimal config wasn't being used.


### Developer

#### Business Project

Removed SaleOrder form with id `business-project-sale-order-form` as the onLoad extension was deleted

---

Removed the onLoad extension from the SaleOrder form with id `business-project-sale-order-form` as it was a duplicate.

## [7.2.43] (2025-07-03)

### Fixes
#### Base

* Data Backup: fixed translations and added help for some fields.

#### Account

* AnalyticMoveLine/MoveLine : fixed the reset of analytic accounts on moveline when changing it on analytic move line
* MassEntryMove: fixed the error message list.

#### Human Resource

* Issue on Windows when we try to build the AOS project.

#### Purchase

* Purchase order line: fixed an issue where quantity was not reset to valid when managing multiple quantity.


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

## [7.2.42] (2025-06-26)

### Fixes
#### Base

* Product company: computed sale price on change of 'autoUpdateSalePrice' or 'managPriceCoef'.
* Product category: fixed sequence field display.

#### Account

* Accounting export / FEC export payroll entry: fixed the issue where only accounted moves were returned.

#### Sale

* Partner: fixed wrong computation of total price in 'Sale details by product'.
* Partner: fixed error while opening form view of 'Sale details by product'.

#### Supply Chain

* Sale Order/Purchase Order/Invoice: fixed advance invoice amount on partial invoicing.

## [7.2.41] (2025-06-12)

### Fixes
#### Base

* Bank: fixed error when SWIFT address was empty.

#### Business Project

* Project: fixed error when trying to select new sale order line.

## [7.2.40] (2025-06-02)

### Fixes
#### Base

* Group: removed collaboration configuration since it is only available in AOP enterprise.

#### Account

* Invoice: prevent automatic mail and hide mail panel for supplier invoices.
* Invoice: fixed the display issue with the logo and address when 'Terms and Conditions' are included on the invoice BIRT report.
* Account: fixed unnecessary display of mass entry move lines.
* Account: fixed domain for result profit account and result loss account on account config by company.

#### Human Resource

* Lunch voucher: excluded former employees from lunch voucher calculation.

#### Stock

* Stock location: fixed last inventory date in stock location line when validating an inventory.

#### Supply Chain

* Sale order: fixed partial invoicing based on discounted price.

## [7.2.39] (2025-05-15)

### Fixes
#### Base

* Partner: fixed performance issues when opening emails tab on partner form view.

#### Account

* Mass entry: fixed critical error when we validate more than ten moves in mass entry process.
* Auto reconcile/Partner: restricted auto reconcile between customer and supplier invoices on partner with compensation enabled.

#### Bank Payment

* Bank payment config: removed the possibility to select view type account on internal and external bank to bank account.

#### Human Resource

* Expense: removed the possibility to duplicate an expense.
* Expense: fixed duplicate move when we confirmed a bank order from an expense.

#### Project

* Project: fixed Project activity dashboard to fetch only relevant messages, avoiding unnecessary loading and filtering.

#### Sale

* Sale order: reset the 'manual unlock' state when duplicating a sale order.
* Configurator formula: fixed message type to show an info message instead of an alert when the formula works correctly.

#### Supply Chain

* Stock move: fixed error message when checking available stock for requested and reserved quantities.


### Developer

#### Account

In `MoveLineToolService.getMoveExcessDueList`, changed `Long invoiceId` parameter to `Invoice invoice`.

## [7.2.38] (2025-04-30)

### Fixes
#### Base

* Product: fixed update product prices process to update company specific prices.
* Template: on creating a new template manually, keep email account empty by default so the generated message can use default email account.

#### Account

* Analytic move line: set analytic axis required when we create an analytic move line.

#### Budget

* Purchase order line: use the correct account when a purchase order line is set to 'fixed assets'.

#### Contract

* Contract invoicing batch: fixed an issue where it was not possible to generate more than one invoice for a same contract.

#### Human Resource

* Expense: fixed personal expense and personal expense amount french translation.
* User: hide create employee button if the partner has already an employee.

#### Stock

* Stock move: prevented generation of invoices when 'generate Invoice from stock move' configuration is disabled in supplychain app.

#### Supply Chain

* Declaration of exchanges: will now reset fiscal year and if necessary country on change of company.

### Developer

Fixed ControllerMethodInterceptor to avoid adding exception management on non void and non public methods in controller.


## [7.2.37] (2025-04-17)

### Fixes
#### Account

* Account management: fixed fields not required in editable grid of payment mode.
* Invoice: fixed price list not filled the first time we change the partner.

#### CRM

* Partner: fixed 'Generate Project' button still present even with job costing app disabled.

#### Project

* Project task: fixed NPE on project task gantt views.

#### Purchase

* Supplier catalog: fixed wrong fetched information for product without catalog.

#### Sale

* Sale order: fixed 'Generate production order' button displayed when app production is deactivated.
* Sale order: fixed the issue where stock location was overwritten upon changing the partner when trading name was null.

#### Supply Chain

* Sale order: fixed sale order invoicing state when a line has a negative amount.

## [7.2.36] (2025-04-03)

### Fixes
#### Account

* Bank order/National treasury transfer: fixed internal money transfer from bank order by inverting credit and debit.
* Accounting batch: fixed balance amount on close/open account batch.
* Invoice: fixed bad error handling when skipping validation is active.
* Fixed asset: fixed null value on moveline for ongoing cession.

#### Production

* Bill of material: fixed an issue where a component could not be deleted from the BOM line list after the tree view was opened.

#### Sale

* Sale order: fixed wrong behaviour on a prospect with accepted credit.

#### Stock

* Stock move: fixed average price not updated on product when stock move is canceled.
* Inventory: gap and real values computation now correctly takes into account company product cost/sale/purchase price.

## [7.2.35] (2025-03-20)

### Fixes
#### Account

* Journal: disabled archive to avoid possibility to generate a move on an archived journal.
* Move line: fixed a bug where generated credit note move line origin date was not consistent with its move date.

#### Budget

* Global budget: fixed the budgetKey computation process to always ensure an updated key value.

#### Purchase

* Supplier catalog: products with multiple catalogs now take the catalog with the most recent update date.

## [7.2.34] (2025-03-13)

### Fixes
#### Base

* Base: fixed some errors displayed as notification instead of popup.
* Signature: fixed broken grid view when selecting a certificate.
* User: made the title of the company set panel visible.

#### Account

* Accounting Batch: ignored check at reconcile when move only contains tax account.

#### CRM

* Partner: creating a new partner cannot create a prospect and a supplier at the same time.

#### Human Resource

* HR: fixed an error occurring when using 'Leave increment' batch and if employees do not have a main employment contract.

#### Sale

* Sale order line: fixed advanced filter not displayed unless the whole page is refreshed.

## [7.2.33] (2025-02-20)

### Fixes
#### Account

* Invoice: fixed contact being readonly when the partner is an individual.
* Payment session: fixed infinite loop when searching eligible invoice terms.
* Fiscal position: fixed NPE on adding account equivalence on new record.
* Invoice: fixed wrong default payment condition.
* Move: fixed the transactional error at move delete.

#### Contract

* Contract: fixed no analytic move lines on contract duplication.

#### Marketing

* Campaign: fixed FR translation for 'Event start'.

#### Quality

* QI Resolution: fixed wrong translation in English for the term default.

#### Sale

* Configurator product formula: disabled test button if formula is empty.

## [7.2.32] (2025-02-06)

### Fixes
#### Base

* Update Axelor Open Platform to 6.1.7.
* Advanced export: fixed NPE when target field is empty on advanced export line.
* Period: fixed inconsistency when filling dates on form view.

#### Account

* FEC import: fixed an issue during accounting entries import where the entries were validated without any checks.
* Move: fixed description is not inherited on move lines when they are generated from mass entry lines.

#### Business Project

* Sale order: fixed NPE on selecting a project.

#### Business Support

* Project version: fixed project filter to avoid conflicts on save.

#### Sale

* Sale order: fixed advance payment amount during copy.

#### Stock

* Stock move: fixed split into 2 total net mass issue.

## [7.2.31] (2025-01-23)

### Fixes
#### Base

* Currency: fixed 'codeISO' field emptying on load.
* Product: fixed the incorrect domain on historical orders to display validated orders today.
* User: made phone and email fields read-only as long as the user does not have a linked contact.

#### Account

* FEC Import: set VAT system on move lines during import.

#### CRM

* Partner: creating a new partner is no longer a prospect and a customer at the same time.

#### Sale

* Sale order: fixed impossible to invoice the remainder of an order when a deposit has already been paid and charged to a partial invoice.

#### Stock

* Stock move line: fixed the issue by making the availability column readonly.

#### Supply Chain

* Stock move: stock move mass invoicing now generates an invoice with the correct invoicing address.
* Stock move invoicing: missing translation in wizard on stock move lines.


### Developer

#### Account

Added `MoveLineTaxService` dependency to `FECImporter` class.

#### Supply Chain

Added 'AddressService' to 'SaleOrderCreateServiceSupplychainImpl'.

## [7.2.30] (2025-01-09)

### Fixes
#### Base

* Email account: fixed NPE that occurred when setting 'Default account' to false.
* City: fixed issue where country was not filled when adding a new city from address form.

#### Account

* Accounting batch: result move computation query takes into account accounted entries.
* Move / Moveline: added additional control to avoid unbalancing input of entries on general / special accounts at validation.
* Account: fixed hard coded db schema in native queries.
* Payment voucher: fixed unexpected pop-up mentioning no record selected while there is at least one.
* Invoice: removed incoherent mention of refund line in printing when it's not originated from actual invoice.
* Bank reconciliation: fixed the filter to display move lines with no function origin selected and hide it when already reconciled in different currency than move in 'Bank reconciliation statement' report.
* Accounting report type: fixed comparison in custom report types demo data.

#### Human Resource

* TimesheetLine/Project: fixed computation of durations.

#### Purchase

* Purchase request line: fixed domain for product.

#### Quality

* Quality improvement: fixed readonly condition for company.

#### Sale

* Sale configurator: fixed issue where formula was not added when verifying the groovy script then directly exiting the pop-up.

#### Stock

* Logistical form: fixed unique constraint violation error.

#### Supply Chain

* Stock move: fixed invoiced and remaining quantity in wizard when partially invoicing


### Developer

#### Supply Chain

`UnitConversionService` has been added to the constructor of `StockMoveInvoiceServiceImpl`.

## [7.2.29] (2024-12-20)

### Fixes
#### Account

* Invoice: fixed a regression preventing invoices refund.

## [7.2.28] (2024-12-19)

### Fixes
#### Base

* Product: fixed french translation for 'Product'.
* Partner: fixed so that customer can be converted to other partner types.
* Partner link: made partners required to avoid errors.

#### Account

* Accounting batch: fixed 'Realize fixed asset lines' batch doesn't work when start date and end date are same.
* Invoice: fixed payment voucher confirmation with auto reconcile config enabled.
* Invoice: added server-side controls to prevent forbidden status changes, for example cancelling a ventilated invoice.
* Accounting report: fixed blank values in 'Summary of gross values and depreciation' report.

#### Budget

* Invoice: fixed error when there is no product on the invoice line while budget computation.

#### Human Resource

* Expense API: fixed the error when an employee did not have a contract while creating an expense.

#### Production

* Manufacturing order: merging manufacturing orders now correctly takes into account scheduling configuration.

#### Quality

* Quality alert: fixed NPE when opening kanban view.

#### Sale

* Sale order: fixed concurrency error when adding a pack on a sale order.

#### Supply Chain

* Timetable: timetable are now correctly updated to not invoiced when a linked credit note has been ventilated.
* Sale order: a sale order with invoiced timetables cannot be edited anymore.
* Sale order invoicing: fixed the missing title 'To invoice' on the corresponding column when invoicing time table lines from a sale order.


### Developer

#### Account

`action-invoice-record-draft` was replaced by `action-invoice-method-back-to-draft`.

## [7.2.27] (2024-11-28)

### Fixes
#### Base

* Group view: fixed inexistant field 'canViewCollaboration' to display only with the Enterprise Edition.
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

#### Human Resource

* Lunch voucher: fixed computation for leaves with overlapping periods.
* Timesheet: fixed NPE because of daily limit configuration.

#### Production

* Production API: fixed error while fetching consumed products.

#### Sale

* Sale order line: fixed issue when opening a sale order line with analytic enabled.
* Sale order: fixed price list value on change of client partner.

#### Supply Chain

* Sale order: fixed sale order with a title line tagged as partially invoiced while it has been totally invoiced.
* Credit note: fixed an issue on stock move credit note with different purchase and stock unit.
* Purchase order line: fixed product name when generating purchase order from sale order.

## [7.2.26] (2024-11-14)

### Fixes
#### Base

* Updated Axelor Open Platform to 6.1.6.
* Updated studio module to version 2.0.8.

#### Account

* Analytic move line query: fixed always readonly button in 'Analytic move lines to reverse' dashlet.
* Accounting export: fixed skipped lines on accounting export when we have more than 10 000 lines.
* Accounting report: replaced move line partner full name with partner sequence and partner full name in reports.
* Move/Analytic: record analytic account on moveline on the reverse move
* Partner: fixed an issue where the partner balance was wrong after an unreconcile.
* FEC Import: fixed importing moves with a validation date.
* Bank reconciliation line: fixed an issue where too much memory could be used when filtering move lines.

#### Contract

* Contract/Invoice: fixed analytic wrong management on contract line.

#### Human Resource

* Expense: fixed an error occurring when cancelling an expense payment linked to a bank order.

#### Project

* Project: fixed NPE when opening project in project activity dashboard.

#### Sale

* Sale order: fixed display of 'Send email' button when record is not saved.

#### Stock

* Stock move line: fixed tracking number domain filter.

#### Supply Chain

* Sale order: fixed an issue where the invoicing of sale order lines was blocked.


### Developer

#### Supply Chain

- Added new arguments to SaleOrderInvoiceService.displayErrorMessageIfSaleOrderIsInvoiceable()
- Updated SaleOrderInvoiceService.computeAmountToInvoice visibility to protected and removed it from interface

## [7.2.25] (2024-10-31)

### Fixes
#### Base

* Sequence: fixed draft prefix when checking for the draft sequence number.

#### Account

* Accounting cut off batch: fixed wrong analytic distribution and axis on generated moves.
* Partner: fixed automatic account creation when partner is prospect based on 'Automatic partner account creation mode' in account config.
* Invoice: fixed an issue where too much memory could be used when displaying customer invoice lines.
* Invoice/Move: recompute currency rate of movelines after invoice ventilation.

#### CRM

* Opportunity: fixed filter on contact domain.

#### Human Resource

* Expense: fixed an issue preventing to go to reimbursed status with a payment mode generating a bank order.

#### Purchase

* Purchase order: fixed error on purchase order merge wizard.

#### Supply Chain

* Invoice: fixed invoice line price generated from stock move with different unit.

## [7.2.24] (2024-10-17)

### Fixes
#### Account

* Tax payment move line: fixed an issue where reverse taxes were not reverted, which was making VAT statement reports wrong.

#### Human Resource

* Lunch vouchers: fixed an issue where some employees were not included in lunch voucher computation.

#### Project

* Project: fixed code when generating project from sale order.

#### Sale

* Sale order: fixed an issue were sequence was reset when going back to draft by creating a new version.
* Complementary product selected: correctly prevent the user from modfying selected complementary product on a confirmed sale order.

#### Stock

* Stock API: fixed issue on stock correction creation request.


### Developer

#### Account

Please run this SQL script if you have the issue related to reverse taxes in VAT statement report:

```sql
  UPDATE account_tax_payment_move_line tpml
  SET tax_amount = -tax_amount
  WHERE tpml.fiscal_position IS NOT NULL
  AND EXISTS (
  SELECT 1
  FROM account_tax_equiv_reverse_charge_tax_set terc
  LEFT JOIN account_tax_equiv ate ON ate.id = terc.account_tax_equiv
  LEFT JOIN account_tax tax ON tax.id = terc.reverse_charge_tax_set 
  LEFT JOIN account_tax_line tl ON tax.id = tl.tax 
  WHERE ate.fiscal_position = tpml.fiscal_position AND tl.id = tpml.origin_tax_line
  );
```

## [7.2.23] (2024-10-03)

### Fixes
#### Base

* Batch: added french translation for '* %s anomaly(ies)'

#### Account

* Partner blocking: fixed 'blocking partner with late payment' feature which resulted in some processes being stuck.

#### Helpdesk

* Ticket: fixed the french translation  of 'Assign to me'.

#### Project

* Project: fixed issue related to contact partner when updating client partner.

#### Stock

* Stock move line: the advanced filter saved is not displayed unless we refresh the whole page.

#### Supply Chain

* Sale order invoicing: removed partially invoiced flag when invoice is fully refunded.

## [7.2.22] (2024-09-19)

### Fixes
#### Base

* Partner: fixed convert contact into individual partner error when address is null.
* Advanced export: fix export when multiple fields contain same sub path.
* Request: fixed french translation for 'Request'.

#### Account

* CutOff/Analytic: fixed cut off batch when using analytic on credit move line.

#### Business Project

* Project: prevented an error during project totals computation when parent task had no time unit.
* Project: prevented an error during project totals computation when spent time percentages were too low.

#### CRM

* CRM: added missing action for 'Calls monitoring' dashboard.

## [7.2.21] (2024-09-05)

### Fixes
#### Account

* Invoice payment: fixed move display in payment details grid view.
* Invoice: fixed an issue preventing invoice ventilation when pack feature is used.

#### Budget

* Purchase order line: fixed an issue where budget panel was editable on a confirmed purchase order.

#### Production

* Manufacturing order: fixed wrong priority on the sub manuf order.
* Manufacturing order: fixed title for produced quantity in produced products form view.

#### Supply Chain

* Invoicing: fixed an issue preventing stock moves/order/contracts invoicing with analytic accounting lines.
* Analytic panel: fixed display issue when product family is empty.

## [7.2.20] (2024-08-22)

### Fixes
#### Account

* Fixed asset: removed disposal info during copy.

#### Business Project

* Project: prevented an error during project totals computation when spent time percentages were too high.

#### Human Resource

* Employee bonus management: fixed computation process when there is no user linked with employee.

#### Supply Chain

* Supplychain config: fixed 'Update customer accepted credit' process updating the credit for all companies instead of the current one.

## [7.2.19] (2024-08-08)

### Fixes
#### Account

* AccountingBatch : fixed reconcile by balanced move mode in auto move lettering batch
* AccountingBatch : fixed auto move lettering batch due to negative credit move line amount remaining

#### Bank Payment

* BankOrder : fixed manual multi currency bank order's move generation

## [7.2.18] (2024-07-25)

### Fixes
#### Base

* Translation: fixed an issue where 'Canceled', 'Confirmed', 'Received' french translations were wrong.
* Product: reset the serial number on product duplication.

#### Account

* Move line mass entry: set description required following account configuration.
* Mass entry: fixed analytic axis empty on partner selection.
* Fixed asset: fixed the depreciation values panel readonly if 'Is equal to fiscal depreciation' is enabled.
* Analytics: fixed required analytic distribution template when the analytic distribution type is per Product/Family/Account.

#### Bank Payment

* Bank reconciliation: fixed total of selected move lines in multiple reconciles when currency is different from company currency.

#### Contract

* Contract: fixed batch contract revaluation process order.

#### Human Resource

* Expense line: fixed error when computing kilometric distance without choosing a type.

#### Project

* Project: fixed the typo in french translation for unit help.

#### Purchase

* Purchase order: fixed french typo for 'nouvelles version'.

#### Sale

* Configurator creator: fixed issue related to meta json field simple name.
* Partner: added missing french translation for 'generation type' in complementary product tab.
* Sale order: fixed sale order sequence when creating new version.
* Sale order: fixed an issue preventing from invoicing X% of a sale order as an advance payment where X was greater than the sale order total.

## [7.2.17] (2024-07-11)

### Fixes
#### Base

* Fixed an issue where the logo defined in properties was not used in the login page.
* Product: fixed NPE when duplicating and saving a product.

#### Account

* Block customers with late payment batch: fixed an issue where the batch did not block some partners.
* Analytic/InvoiceLine: remove analytic when account does not allow analytic in all configuration.
* Accounting situation: fixed VAT system display when partner is internal.
* MoveReverse: fixed imputation on reverse move invoice terms.

#### CRM

* Catalog: fixed an issue where the user could upload files other than PDF.

#### Human Resource

* Expense line: orphan expense line are now also digitally signed if there is a justification file

#### Production

* Production order: fixed production order sequence generated from product form not using the correct sequence in a multi company configuration.

#### Project

* Sale order: Fixed project generated with empty code which could trigger a exception

#### Sale

* Sale order template: fixed NPE when company is empty.

#### Stock

* Sales dashboard: Fixed stock location for customer deliveries.

#### Supply Chain

* Invoice: removed time table link when we merge or delete invoices, fixing an issue preventing invoice merge.


### Developer

#### Account

To fix existing data if you reversed a move related to an invoice, you can run the following script:

```sql
UPDATE account_invoice_term AS it 
SET amount_remaining = 0, company_amount_remaining = 0, is_paid = true
FROM account_move_line ml JOIN account_move m ON m.id = ml.move
WHERE ml.id = it.move_line AND ml.amount_remaining = 0 AND m.invoice IS NULL;
```

#### Project

If you have the issue on project generation from sale order, the fix requires to run the following sql request in order to fully work: `UPDATE project_project SET code = id where code IS NULL;`

## [7.2.16] (2024-06-27)

### Fixes
#### Base

* App base: removed admin module reference from modules field.
* Template: fixed translation for content panel title.
* Partner: fixed merging duplicates not working correctly.
* Birt Template: fixed wrong report used when getting report from source.

#### Account

* Move: fixed error popup changing date on unsaved move.
* Mass entry: fixed error when a user without an active company is selecting a company.
* Payment voucher: fixed technical error when the user's active company is null and there are more than one company in the app.
* Move: fixed blocking message due to a wrong tax check preventing some accounting moves validation.
* Mass entry: fixed today date settings in dev mode not working with created lines from mass entry form.
* Move/Analytic: fixed negative analytic amounts when generating cut off moves.
* Invoice: fixed foreign currency invoice duplication when current currency rate is different from the currency rate in the original invoice.
* Invoice: fixed an issue where due date was not updated correctly when saving an invoice.

#### Bank Payment

* Bank statement line: prevent user from creating a new bank statement line manually.
* Bank reconciliation: fixed move line filter when using multiple reconciliations.

#### Contract

* Contract: fixed an issue where the value of yearly ex tax total was not revalued.
* Contract: fixed tax error message related to supplier contracts.

#### Human Resource

* Payroll Preparation: fixed an issue were leaves were not always displayed.

#### Purchase

* Purchase order: fixed purchase order lines not displayed on 'Historical' menu.

#### Supply Chain

* Fixed forwarder partner domain filter.

## [7.2.15] (2024-06-07)

### Fixes
#### Account

* Move line: set the vat system editable for tax account.
* Account: fixed technical type select for journal type demo data.
* Move line/Reconcile/Tax: fixed error when you forbid tax line on tax account.
* Move: fixed a technical error when adding a first line without invoice terms in sale or purchase move.
* Invoice payment: fixed reconcile differences process for supplier invoices.
* Analytic distribution template: removed analytic percentage total verification when we add a new analytic distribution line.
* Invoice/AutoReconcile: removed tax moveline reconciliation in move excess or move due at ventilation.
* Payment session: fixed issue when validating the payment session causing amount being updated and not corresponding to the value of the amount being validated and equal to the bank order generated.
* Move line: select default account from partner configuration only for first line of the move instead of all lines.
* Move: date of analytic move lines will be updated on change of move's date.
* Accounting report: fixed opening moves are not displayed on aged balance report.

#### Bank Payment

* Invoice payment: fixed payment remaining 'Pending' while bank order has been realized (while no accounting entry generated).

#### Budget

* Global budget: fixed missing english translation for archiveBtn.

#### CRM

* Partner: fixed display condition for customer recovery button.

#### Human Resource

* Expense: fixed an issue that prevented to ventilate a expense.
* Expense API: improve requests permissions.
* Extra hours: fixed an issue where lines were filled with the connected employee instead of the employee filled in the form view.
* Payroll preparation: correctly empty lists on payroll preparation view when employee is changed.
* Timesheet: fixed an issue preventing a manager user to create timesheets for subordinates.

#### Marketing

* Campaign: fixed error preventing from sending reminder emails to leads when the list of partners is empty.


### Developer

#### Account

The constructor for MoveLineControlServiceImpl has been updated to include the additional parameter 'MoveLineGroupService moveLineGroupService'.

#### Human Resource

Created a new service `MoveLineTaxHRServiceImpl` that extends `MoveLineTaxServiceImpl` and override the method `isMoveLineTaxAccountRequired(MoveLine moveLine, int functionalOriginSelect)`

---

If you use the AOS API for Expense, please check that they still work with your current permission, as we fixed permission check to be more logic.
For example, the check is done on expense line instead of expense for updating expense line. Also the API now correctly checks the permission with the record, meaning that conditional permission will now correctly be applied.

## [7.2.14] (2024-05-24)

### Fixes
#### Base

* Update axelor-studio dependency to 1.3.5.
* ICalendar: fixed synchronization duration widget.
* Sale order/Purchase order/Invoice: fixed wrong column name displayed on discounted amount
* Birt Template: for developers, added hot reload for .rptdesign files.

#### Account

* Accounting report: update font size and improve lettering display on partner general ledger.
* Payment voucher: fixed an issue where some fields were not displayed in due element list.
* Invoice: fixed a bug where generated invoices from orders had wrong WT/ATI configuration.
* FEC Import: fixed partner not filled when the partner is only on the first line of an entry.
* Move line: fixed move fiscal position not being used for tax equivalence on account change.

#### Bank Payment

* Bank reconciliation: fixed wrong computation of balances when having more than 10 records.
* Bank statement line: fixed wrong balance on the demo data sample statement.
* Bank reconciliation: fixed move line filter and controls when reconciliating.
* Accounting report: fixed bank reconciliation accounting report displaying already reconciled move lines.

#### Business Project

* Timesheet line: fixed an issue preventing to invoice timesheet when the task has an activity.
* Project: fixed validation error missing timeUnit when creating a planned time planning while business-project module is not installed.

#### Contract

* Contract: fixed display condition of the Revaluation fields.
* Contract: fixed 'nouvelle version' used as key instead of 'new version'.
* Contract: fixed 'ID to load' error when we modifying supposed activation date without saving.
* Contract: deleted version history on duplication.

#### Human Resource

* Leave request: fixed issue where a leave request was not updated after sending it.

#### Maintenance

* Maintenance request: fixed impossible to create a maintenance request from the quick adding field.

#### Production

* Bill of materials: fixed blank printing.
* Manufacturing order: fixed NPE error on selecting a product in Consumed products.

#### Project

* Project: fixed a issue where a task was not displayed when using the sub-task button

#### Purchase

* Purchase request: added sequence for purchase request in demo data.

#### Sale

* Sale order merge: fixed an issue where it was not possible to select a price list in case of conflicts.

#### Stock

* Stock move line: fixed issue when changing the line type to title.

#### Supply Chain

* Customer invoice line: fixed default product unit on product change.
* Invoice: fixed 'FixedAsset' boolean never propagated to invoice line from purchase order line.
* Purchase order: when a purchase order is generated from a sale order, when the catalog does not have a code or name, it will use the product.

## [7.2.13] (2024-05-03)

### Fixes
#### Base

* Sequence: fixed NPE while checking yearly reset or monthly reset on unsaved sequence.
* Advanced export: Fixed export doesn't work when maximum export limit exceeds.

#### Account

* Invoice: fixed default financial discount from partner not being computed after invoice copy.
* Accounting export: Fixed error when replaying the accounting export.

#### Bank Payment

* Bank reconciliation: fixed tax move line generation when origin is payment.
* EBICS: bank statements are now correctly created after a FDL request.

#### Contract

* Contract: fixed prorata invoicing when invoicing a full period.
* Contract: correctly disable related configuration when disabling invoice management.
* Contract: Fixed error when using contract template with contract line to generate a contract.

#### Production

* Manufacturing order: fixed conversion of missing quantity of consumed products.
* Machine: fixed machine planning type when creating it from machine.

#### Purchase

* Purchase order report: fixed sequence order issue when title lines are present.

#### Sale

* Sale order: fixed refresh issue when saving a sale order.

#### Stock

* Tracking number: fixed available qty not displayed on grid view.
* Stock move: fixed printing settings when creating stock move from sale order.
* Stock configuration: fixed typo in french translation, 'incoterme' => 'incoterm'


### Developer

#### Production

Changed signature of `ProdProductProductionRepository.computeMissingQty(Long productId, BigDecimal qty, Long toProduceManufOrderId)`
to `ProdProductProductionRepository.computeMissingQty(Long productId, BigDecimal qty, Long toProduceManufOrderId, Unit targetUnit)`

## [7.2.12] (2024-04-19)

### Fixes
#### Base

* Birt template: fixed duplicated printouts generated when attach boolean is set to true.

#### Account

* Payment voucher: fixed required message at on new and fixed invoice refresh at confirm.
* Accounting report: fixed 'Fees declaration supporting file' report displaying records that should not appear.
* Financial Discount: fixed french translations for 'discount'.

#### Human Resource

* Expense: fixed expense accounting moves generation when expense line dates are different and tax amount is zero.

#### Sale

* Sale order: removed french translation from english file.

#### Supply Chain

* Mass stock move invoicing: fixed an issue where invoiced partners were not used to invoice stock moves, the other partner was used instead.
* Mass stock move invoicing: fixed an issue preventing to invoice customer returns.

## [7.2.11] (2024-04-04)

### Fixes
#### Account

* Invoice: fixed report when invoice is linked to more than one stock move.
* Move: fixed error when selecting a third party payer partner.
* Move template line: added missing tax in demo data.
* Payment Session: fixed error when selecting bank details where payment mode have empty accounting setting.
* Accounting Situation: fixed used credit calculation not taking in account completed sale orders.
* Accounting Batch: in closing account batch, prevented result move generation when no accounts are treated.
* Accounting batch: fixed move generation on account closing/opening batch.

#### Bank Payment

* Bank reconciliation: move lines to reconcile in other currencies are now displayed.

#### Business Project

* Invoicing project: fixed batch to prevent the generation of invoicing business project when there is nothing to invoice.

#### Contract

* Contract template/Contract version: fixed 'Additional Benefit Management' field display.
* Contract template: fixed an error occuring in the server logs on opening a contract template.

#### Human Resource

* Expense: fixed expenses creation for subordinates.
* Expense line: fixed project filter to use user related to employee instead of current user.

#### Production

* Manufacturing order: fixed unable to select a tracking number on a consumed product.

#### Sale

* Sale order line: fixed an error that occured when changing project of a line.
* Sale order: 'Total W.T' is no longer displayed twice in template grid view.

#### Talent

* App config: fixed demo data in the recruitment app config.


### Developer

#### Sale

Removed action `action-sale-order-line-record-progress`

## [7.2.10] (2024-03-21)

### Fixes
#### Base

* Customer: fixed error when loading customer map.
* Fixed wrong french translation of 'Application' (was 'Domaine d'applicabilité').
* Language: fixed an issue where getting default language did not use the configuration 'application.locale'.
* App Base: fixed wrong currency conversion line in demo data.

#### Account

* Accounting batch: fixed result move functional origin in closure/open batch.
* Move: fixed mass entry technical origin missing in Move printing.
* Payment voucher: fixed paid amount selecting overdue move line.
* Accounting batch: fixed the block customer message when no result.
* Partner/AccountingSituation: added error label when multiple accounting situation for a company and a partner.
* Reconcile manager: fixed move lines selection.
* Accounting batch: fixed currency amounts on result moves in opening/closure.
* FEC Export: fixed technical error when journal is missing.

#### Budget

* Purchase order line: fixed wrong budget distribution when invoicing multiple purchase order lines.

#### Contract

* Contract line: fixed analytic lines creation using amount in company currency.
* Contract: fixed prorata invoicing when invoicing period was smaller than the invoicing frequency.

#### Helpdesk

* SLA: added missing translations inside 'reach in' in readonly.

#### Production

* Prod process line: added missing filter on type for work centers.
* Manufacturing order: fixed error on change of client partner for manuf orders without related sale orders.
* MPS: fixed quantity not editable on MPS proposal.
* Product: fixed an issue where 'economic manuf order qty' field was displayed twice.
* Product: fixed cost sheet group display on product form on semi-finished products.

#### Stock

* Inventory: fixed type in inventory demo data.

#### Supplier Management

* Supplier request: fixed 'JNPE' error on partner selection in Supplier request form.

## [7.2.9] (2024-03-07)

### Fixes

* The format of this file has been updated: the fixes are now separated by modules to improve readability, and a new `developer` section was added with technical information that could be useful for developers working on modules on top of Axelor Open Suite.

#### Account

* Account clearance: fixed issue when opening a generated move line from account clearance.
* Doubtful batch: fixed NPE errors on doubtful customer batch.
* Invoice payment: added missing french translation for error message.
* Period: fixed an issue when checking user permission where the roles in user's group were not checked.
* Reconcile: fixed passed for payment check at reconcile confirmation.
* Reconcile: fixed selected amount with negative values.
* Move: fixed missing label in accounting move printing.

#### CRM

* CRM App: fixed small typos in english titles.
* Lead: fixed anomaly which was forcing a lead to have a company.

#### Human Resource

* Leave request: fixed Payroll input display so it is visible for HR manager only.
* Payroll preparation: fixed 'the exported file is not found' error on payroll generation when the export dir is different from upload dir.

#### Production

* Sale order: fixed a NPE that occured at the manuf order generation.
* Manufacturing order: fixed an issue when the products to consume are by operation where the product list to consume was not filled when planning.
* Manufacturing order: real process are now correctly computed in cost sheet.
* Manufacturing order: fixed a bug a producible quantity was not correctly computed when a component was not available.

#### Sale

* Sale order: improve performance on sale order save.

#### Supply Chain

* Stock move: fixed a bug that prevented to totally invoice a stock move when partial invoicing for out stock move was activated.
* Supplychain configuration: fixed default value for "Generation of out stock move for products".
* Sale order: fixed a bug where sale orders waiting for stock move were not displayed.


### Developer

#### Account

- Removal of `action-method-account-clearance-show-move-lines` and creation of `action-account-clearance-view-move-lines` for its replacement
- `showAccountClearanceMoveLines` has been removed from `AccountClearanceController`

#### CRM

- In `lead-form-view`, removed `required` from `enterpriseName`

## [7.2.8] (2024-02-22)

#### Fixed

* Stock location: fixed wrong QR Code on copied stock location.
* Manufacturing order: fixed an issue where the stock location is incorrect for generated stock moves in manufacturing order using outsourcing.
* Move: fixed currency rate errors in move line view.
* Invoice: fixed an issue when returning to the refund list after creating a refund from an invoice.
* Bank order: fixed multi currency management.
* Purchase order: fixed JNPE error on purchase order historic opening.
* Cost calculation: fixed JNPE error on select of product.
* Product: replaced stock history panel which is showing empty records by a panel to stock location line history.
* Employee: fixed error happening while deleting employee.
* Invoice: fixed an error on invoice ventilation when the invoice had an advance payment in previous period.
* Sale order: removed the possibility to mass update fields on sale order, as it caused inconsistencies.
* Fixed asset: fixed purchase account move domain in fixed asset form view.
* PurchaseOrder/SaleOrder: fixed analytic distribution lines creation with company amount.
* Invoice: fixed display of delivery address on advance payment invoices generated from a sale order.
* Budget: remove all dependencies with other modules when the app budget is disabled.
* Accounting batch: take in account accounted moves in accounting report setting move status field.
* Computing amounts in employee bonus management now alert user if employee does not have a birth date or seniority date.
* Project: fixed opening gantt view per user.
* Accounting report: set readonly export button and add report type check to provide critical error.
* Operation order: finishing a manuf order from the operations correctly computes the cost sheet quantities.
* Sale order: fixed technical error preventing pack creation.
* Contract: reset sequence when duplicating contracts.
* Reconciliation: fixed invoice term imputation when PFP not validated.
* Manufacturing order: finishing a manufacturing order now correctly updates the cost price of a product.
* Stock move: fixed error when spliting in two a stock move.
* Budget level: fixed the budget dashlet from budget level grid view.
* Inventory line: forbid negative values in inventories.
* Accounting export: fixed FEC export not taking journal into account.
* Project: fixed critical error when we change purchase order line quantity on purchase order generated by project task.
* Medical visit: fixed JNPE error when saving a new medical visit.

## [7.2.7] (2024-02-01)

#### Fixed

* Stock: added quality control default stock location to stock config demo data to avoid exception case.
* Accounting batch: fixed anomaly generated when running the closing/opening accounts batch with option simulate generated moves option ticked.
* Project task: fixed the ticket task form views.
* Expense line: fixed an UI issue where some fields were not required in the form view of kilometric expense line.
* Production operation: fixed an issue where planned operations could not be moved from the calendar view.
* Move/MassEntry: fixed critical error when we create a move or mass entry move without company currency.
* Accounting batch: added financial account with a technical type 'asset' when we open/close the year accounts.
* Quantity: fixed quantity fields in Invoice line/Purchase order line/Sale order line/Product company/Supplier catalog grid view so they display the configured number of decimals instead of 2.
* Expense line: fixed a bug where 'Type' was not properly required in kilometric expense line.
* Sale order: adjusted price and quantity precision according to the user-configured precision settings.
* Manufacturing order: fixed a error that occured when adding manually a produced/consumed stock move line.
* Purchase order: fixed an issue where duplicated order did not get their order date updated.
* Contract: added control on invoice period start date and invoice period end date.
* Move line: hid VAT System when journal is not purchases or sales and set it not editable when account is not charge or income.
* Bank reconciliation: hid reverse move with the 'Hide move lines in bank reconciliation'.
* Debt recovery: fixed debt recovery batch error when no invoice term was found.
* Birt template: template file is now no longer filled with demo data.
* Partner: make the 'Default partner status' field required only when the 'Manage status on prospect' option is checked.
* Leave request: user in not required in an employee anymore to increase leave from batch.
* Contract: fixed Price and Qty scales in grid view.
* MRP: fixed an issue where some mrp lines were not taken into account during the computation.
* Product Details: fixed 'id to load is required for loading' error when stockLocationLine has no unit.
* Sale order line: fixed an issue where some products were not selectable in a line.
* Production process line: fixed 'NullPointerException' popup error while opening a line.
* Expense line: hid analytic panel when the expense is not validated.
* Budget Invoice/Move/Order: fixed the error message when the budget amount exceed the line amount.
* Move: fixed due dates not computed when we change the move date.
* Invoicing project: invoice lines generated have now a correct unit price.
* Bank reconciliation: fixed dates management in moveline query.
* Accounting move mass entry: fixed an issue where the journal was readonly after changing the company.
* Move: fixed canceled payments preventing moves from being updated.
* Contract: emptied the contract line when emptying the product.
* Reconcile: prevent reconciliation if an involved move has a line with a tax account but no tax.
* Invoice term: set PFP validator user to invoice one at invoice PFP validation.
* Move template: fixed demo data.
* Control point: fixed creation from the menu entry.
* Accounting report: fixed error occuring when we delete the report type.
* Birt template parameter: fixed a small UI issue by ordering of the list of parameters.
* Move: batch control move consistency exception management.
* Expense line: compute total amount and distance depending on kilometric type.

## [7.2.6] (2024-01-11)

#### Fixed

* Prospect: open grid view instead of kanban view by default.
* Invoice: fixed supplier refund imputation on invoices.
* Product: make the product unit field required for stock managed items.
* Accounting: fixed error preventing the import of demo data.
* Budget: fixed amount paid computation on move and invoices reconcile.
* Reconcile: fixed invoice term amount remaining with multi currency.
* Accounting: rename subrogation to third-party payer.
* Invoice/Move/Budget: manage the negative imputation when the invoice is a refund or an advance or when the move is a reverse move.
* Stock move: fixed unable to select external stock location on receipt.
* Invoice: Duplicating an invoice now correctly empties the origin date and the supplier invoice number.
* Invoice: fixed error when we change partner in supplier invoice with invoice lines.
* Account clearance: fixed the way tax and vat system are managed.
* Indicators: fixed results display.
* Sale order: keep attached files and internal notes when merging sale orders.

## [7.2.5] (2023-12-21)

#### Fixed

* Stock location: Issue on Edition of Location financial data report
* Wrong quote sequence number on finalize
* Invoice : Fix partner account related errors consistency when validating and ventilating
* Invoice : Fix invoice term sum check not being done
* [Ticket]: Fix Ticket timer buttons
* INVOICE : currency not updated on first partner onChange
* HR API - now computes total on kilometric expense line
* Move Line : Prevent from updating tax line when the move is accounted
* Custom accounting report : Disable group by account feature as it cannot be fixed until 8.1
* GDPR: Fix response email couldn't be changed.
* Doubtful customer : now works with multi currency
* Custom accounting report : Fix an issue with custom rule priority
* MAINTENANCE ORDER : fix NPE when we try to plan
* Deposit Slip : Fix critical error when we try to load no one payment line
* Move lettering : Fill the date of lettering when the status becomes Temporary
* EXPENSE LINE: fix totalTax is not in readonly in form view when expense product blocks the taxes
* Sale / Purchase / Invoice : Fix cases where price decimal config wasn't being used
* Fixed asset : fix depreciation date when failover is complete
* Move line : Fix move function original select being wrongly displayed
* EVENT : some highlights conditions in grids use __datetime__
* Invoice payment : Fix an issue where it was possible to set a higher amount that maximum one allowed
* Printing template / Printing setting: fix translation issue in position fields.
* Move line: Added reference of move line in sum's control error message
* Sale Order/Purchase Order: Error when emptying contact
* Bank order: Fixed issue in code generating move
* Business Support: TaskDeadLine field not hidden when the app is installed anymore.
* Sale order : Fix an issue when updating tax number
* PurchaseOrderLine : rework budget override
* Analytic account : Only accounts with an analytic level of 1 can be selected
* MOVE TEMPLATE : Demo data - missing partner
* UNITCOSTCALCULATION : Irrelevant message when no product has been found
* Account clearance : Fix an issue where move lines could be created or edited there
* Reconcile : Fix an issue where a payment was created for a reconcile with an account not being used for partner balance
* Sale order : Fix technical error linked to budget management
* Analytic Move Line : Fix type select when we create a new analytic move line from orders, invoices, contract and moves

## [7.2.4] (2023-12-07)

#### Fixed

* Quality Module: added demo data for quality module 7.2 features.
* Budget : Add currency symbol for budgetDistributionSumAmount field
* Sale order: fixed JNPE error when copying a sale order without lines.
* Purchase request: fixed reference to purchase order being copied on duplication.
* CRM: added demo data for the search functionality.
* Opportunity: fixed "create Call" button so it correctly sets the type of event to Call.
* Studio: fixed wrong french translation for "Order By".
* Accounting batch: hide unnecessary payment mode information.
* Sequence: prevented JNPE error on sequence form view when the prefix was empty.
* Translation: French translation conflict correction with 'Manual'.
* MRP: fixed invalid type error when selecting a product from (sale/purchase) order line.
* Sale order: fixed wrong price update when generating quotation from template.
* Move line: hid cut off information when functional origin is 'opening', 'closure' or 'cut off'.
* Custom accounting report: fixed detailed line with fetching accounts from their code displaying the same value.
* Team task: fixed view reload error.
* Indicator generator: fixed indicators generators demo data.
* Invoice: fixed reference to subrogation release being copied on duplication.
* Message: fixed encoding errors happening with accented characters when sending an email.
* Fixed asset: accounting report now correctly takes into account fiscal already depreciated amount.
* Custom accounting report: fixed some configs being displayed whilst not being used.
* Configurator: fixed EN demo data for configurator.
* Bank Details: fixed balance display for bank details on card view and form view.
* Invoice: fixed error popup before opening a payment voucher from an invoice.
* Doubtful customer: fixed the way invoice terms are managed.
* Invoice: fixed invoice term generation when skip ventilation is enabled in invoicing configuration.
* Contract: fixed UI issue by hiding some part of the view while the partner is empty.
* Account management: use functional origin instead of journal to determine tax account.
* Employee files: creating an Employee files from a Training Skill now correctly creates the PDF viewer.
* Invoice: fixed error at new invoice term creation.
* Invoice: fixed reference to "Refusal to pay reason" being copied on invoice duplication.
* Timesheet: fixed timesheet line date check.
* Stock move: allow to create a tracking number directly on stock move line.
* Cost calculation: fixed an issue preventing an infinite loop in case of an error in bill of materials hierarchy.
* FEC Import: fixed the way the currency amount is imported to make sure it is correctly signed.
* Account: forbid to select the account itself as parent and its child accounts.
* Mobile Settings: added a placeholder on every authorized roles.
* Bank order: highlight orders sent to bank but not realized.
* Quality Module: fixed register when no status are registered.
* Payment session: fixed display of currency symbol in payment session.
* Move template line: hide and set required tax field when it is configured in financial account.
* Inventory line: fixed update of inventory line by taking into account stockLocation of line when possible, triggering update when necessary.
* FEC import: fixed an issue where empty 'DateLet' or 'EcritureLet' column would create empty letterings.
* Invoice: fixed partially paid invoices in bill of exchange management.
* Stock move: allow to select external stock location for deliveries.

## [7.2.3] (2023-11-23)

#### :exclamation: Breaking Change

* InvoicePayment/Reconcile: Change the link between the models

The previous link between Reconcile and Invoice Payment was a list of reconcile in the invoice payment and a link to the payment in reconcile. Now, the link will be just a link to the reconcile in the payment.

This technical change means that existing payments on invoice must be migrated to avoid issues. Please run the following SQL script on your database:

```sql
ALTER TABLE account_invoice_payment ADD COLUMN IF NOT EXISTS reconcile bigint;

UPDATE account_invoice_payment payment SET reconcile = (SELECT id FROM account_reconcile reconcile WHERE reconcile.invoice_payment = payment.id) WHERE payment.reconcile IS NULL;

UPDATE account_invoice_payment payment SET reconcile = (
	SELECT MIN(reconcile.id) FROM account_reconcile reconcile 
	INNER JOIN account_invoice_term_payment itp ON itp.invoice_payment = payment.id 
	INNER JOIN account_invoice_term it ON it.id = itp.invoice_term
	INNER JOIN account_move_line ml ON ml.id = it.move_line
	WHERE reconcile.debit_move_line = ml.id OR reconcile.credit_move_line = ml.id) WHERE payment.reconcile IS NULL;

ALTER TABLE account_reconcile DROP COLUMN invoice_payment;
```


#### Change

* Appraisal: add demo data for appraisal feature.

#### Fixed

* MRP Line: fixed an issue preventing to open a partner from a MRP line.
* Configurator BOM: fixed a bug during bill of materials generation where lines were not generated.
* Invoice: fixed description on move during invoice validation.
* Opportunity: fixed a bug where contact field was not filled when we create an event from an opportunity.
* Move template: fixed due date not being set on generated move.
* Stock location line: fixed quantity displayed by indicators in form view.
* Invoice: fixed currency being emptied on operation type change.
* Move: fixed autofill fields at move line creation according to partner accounting config.
* Bank statement: added missing french translation when we try to delete a bank statement.
* Invoice: fixed an issue where enabling pfp management and opening a previously created supplier invoice would make it look unsaved.
* Budget key: fixed the computation in invoices/moves/orders to manage only axis related to budget key management in accounting configuration.
* Fixed asset: fixed inconsistency in accounting report.
* Move Template: hide move template line grid when journal or company is not filled.
* Project task: fixed broken time panel in project task.
* Expense: fixed an issue were employee was not modified in the lines when modifying the employee on the expense.
* City: fixed errors when importing city with geonames.
* Period: fixed status not reset on closure.
* Analytic move line: fixed required on analytic axis when the sum of analytic move line percentage is equal to 100.
* Invoice / Reconcile: fixed issue preventing from paying an invoice with a canceled payment.
* Fixed asset: fixed derogatory cession process not being triggered for a scrapping type disposal.
* Unit cost calculation: fixed error when trying to select a product.
* Global budget: fixed validate translation.
* InvoiceTerm: fixed rounding issue when we unreconcile an invoice payment.
* Forecast: fixed a bug that occured when changing a forecast date from the MRP.
* Fixed asset category: fixed wrong filter on ifrs depreciation and charge accounts not allowing to select special type accounts.
* Appraisal: fixed appraisal generation from template.
* Move: fixed error when we set origin on move without move lines.
* Advance payment: fixed invoice term management and advance payment imputation values in multi currency.
* Payment session: fixed demo data not allowing daybook moves to be retrieved in payment session despite that status being enabled.
* Bill of exchange: fixed managing invoice terms in placement and payment.
* SOP: fixed quantity field not following the "number of decimal for quantity fields" configuration.
* Contract template: fixed wrong payment mode in contract template from demo data.
* Period/Move: fixed new move removal at period closure.
* Move mass entry: fixed line duplication at counterpart generation for a move generated by the mass entry process.
* Account chart: fixed issues in data used in account chart import.
* SOP: added missing french translation.
* Period: fixed errors not being displayed correctly during the closure process.
* App Employee: remove "daily work hours" configuration in App Employee configuration as it is not used (the configuration used is the one in App Base).
* Move: fixed error when we set description on move without move lines.
* Budget distribution: fixed available amount using the availability check in the budget.
* Payment session: fixed issue preventing from validating a session.
* SOP line: fixed an issue where SOP process does not fetch the product price from the product per company configuration.
* Contract: fixed prorata computation when invoicing a contract and end date contract version

When creating a new version of a contract, the end date of the previous version
is now the activation date of the new version minus one day.
If left empty, it will be today's date minus one day.

* Bank order: do not copy "has been sent to bank" on duplication.

## [7.2.2] (2023-11-09)


#### Fixed

* App builder: update studio dependency to 1.3.4 to get the following fix:

    - fixed NPE upon save of a custom model with a menu

* Fixed an error during apps installation on a new database. GH-11451
* Sale order: fixed "NullPointerException" error when selecting a project.
* App Base: set company-specific product fields domain.
* Sale order / Invoice: fixed an issue when invoicing a sale order with multi currency & multi invoice terms.
* Accounting report: fixed display of field "detailed".
* Accounting report: fixed an issue in Journal report (11) where debit and credit were not displayed in the recap by account table.
* Accounting dashboard: removed blank panel in "accounting details with invoice terms" dashboard.
* Custom accounting report: fixed legacy report option being displayed for all account report types.
* Move line: fixed error at first move line creation related to counter initialization.
* Move line: fixed error when we create a move line in multi currency.
* Move: fixed an error when we update invoice terms in move line.
* Move: fixed an error when we reconcile move lines without tax line.
* Move: hide company without account config on company list when we change move company.
* Move: added journal verification when we check duplicate origins.
* Fixed asset: fixed an issue after selecting a category where set depreciations for economic and ifrs were not computed correctly.
* Fixed asset: hide "isEqualToFiscalDepreciation" field when fiscal plan not selected.
* CRM: opening an event from custom view in prospect, leads or opportunity is now editable.
* HR Timesheet: fixed conversion problems while generating timesheet lines from project planning time.
* Project: fixed wrong domain on opening project report.
* Cost sheet: fixed issue in the order of calculation on bill of materials.
* Configurator BOM: Fixed a concurrent error when generating a bill of materials from the same configurator.
* Employee: to fix bank details error, moved the field to main employment contract panel.
* FEC Import: prevent potential errors when using demo data config file
* Contract: fixed "NullPointerException" error when emptying the product on a contract line.
* Manufacturing order: company is now required in the form view.
* Bank reconciliation: selectable move lines are now based on the currency amount.
* Production: fixed 'at the latest scheduling' planification to support multi level planning.
* Sale order (Quotation): fixed "End of validity date" computation on copy.
* Debt recovery: fixed balance due in debt recovery accounting batch.
* Birt template: fixed wrong type for parameters of ClientSituation report.
* Stock: fixed stock dashboard.
* MRP: fixed MRP manufacturing orders generation and planning.
* Lead: prevent the user from editing the postal code when a city is filled to avoid inconsistencies.
* Invoice term: fixed amount computation with multi currency.
* Contract: prorata is now correctly disabled when the config is off.
* CRM Event: when an event is created from CRM, correctly prefill "related to".
* Opportunity: company is now required in the form view.
* Sale: hide 'Timetable templates' entry menu on config.
* Maintenance: reset the status when duplicating a maintenance request.

## [7.2.1] (2023-10-27)

#### Fixed

* App builder: update studio dependency to 1.3.3 to get the following fix:

    - fixed StudioActionView duplication on every save

* Timesheet: fixed error when generating lines from expected planning.
* Timesheet: fixed a regression on timesheet editor react view due to daily limit.
* Debt Recovery: fixed error message on debt recovery generation to display correctly trading name.
* Project task: fixed a bug where boolean to invoice via task was not working when generating a task from a sale order.
* Invoice/Move: fixed due date when we set invoice date, move date or when we update payment condition.
* FEC Import: fixed issue when importing FEC with empty date of lettering.
* FEC Import: the header partner is now filled correctly

    - Multi-partner: no partner in header
    - Mono-partner: fill partner in header

* Analytic: fixed analytic distribution verification at validation/confirmation of purchase order/sale order when analytic is not managed.
* Project: fixed project task time follow-up values computation.
* Payment Voucher: fixed display of load/reset imputation lines and payment voucher confirm button when paid amount is 0 or when we do not select any line.
* Period closure: fixed a bug where status of period was reset on error on closure.
* Custom accounting report: fixed demo data result computation in sum of accounts lines.
* Manufacturing order: fixed an issue where outsourcing was not activated in operations while it was active on production process lines.
* Payment voucher: fixed error at payment voucher confirmation without move lines.
* Move: removed period verification when we opening a move.
* Payment voucher: removed paid line control at payment voucher confirmation.
* Invoice / Move: fixed subrogation partner on invoice terms depending on parent move or invoice.
* Fixed asset: fixed popup error "Cannot get property 'fixedAssetType' on null object" displayed when clearing fixed asset category field.
* Cost sheet: replaced 'NullPointerException' error by a correct message when an applicable bill of materials is not found on cost calculation.
* axelor-config.properties: enabled Modern theme by default.
* Technical: fixed xml-apis dependency not excluded causing issue when building project.

## [7.2.0] (2023-10-23)

### Features

* App builder: update axelor-studio dependency from 1.2 to 1.3.2

Please see the corresponding [CHANGELOG](https://github.com/axelor/axelor-studio/blob/1.3.2/CHANGELOG.md) to see the changes.

#### Base

* BIRT templates: the configuration is now used inside existing processes generating documents from the ERP.
Any existing printings using BIRT can now be overriden from the configuration, adding new printings is also a lot easier.

#### Accounting

* Move / Invoice / Payment session: added subrogation

A partner can now have a "subrogation partner". When adding a payment, the subrogation partner will be the one used in the payment process.

#### CRM

* Partner: added a new type of partner "Corporate partner". Added a new panel to follow fidelity and opportunities won/lost of the partner.

#### Sale

* SaleOrder: Multi-level sale order lines

Enhance the functionality of the sale order line by introducing sub-components. These sub-components will be organized in a tree view, allowing users to effortlessly manage them. Users can easily delete existing sub-components or add new sub-elements to them as needed.

* SaleOrder: Improved versioning with past version recovery

#### Quality

A rework of the quality module has been done:

Within the company, "quality improvement" enables non-quality and continuous improvement to be managed according to the PDCA (Plan, Do, Check, Act) model:

* At Product level (directly or indirectly linked to a product sold)
* At System level (linked to the company's processes)

It can be used to carry out Hishikawa, PARETO or ABC-type analyses. It integrates real action plans with steering dashboards, following the following steps:

* Identify the problem
* Treat it in the short term
* Search for and correct root causes
* Monitor the implementation of preventive actions
* Capitalize for the future

#### Human Resource

* Expense: Digital signature for justification file

If the justification file is an image, it is converted to a pdf file.
Then the pdf file is digitally signed.

* Medical leave: added Medical Leave management in Employee.
* HR API: new paths to create/send/validate an Expense.
* Job application: creating an employee from job application now takes into account the title and every DMS files
* Expense: prevent payment if expense was paid with company card.
* Leaves, lunch voucher: added a new export format to Sage.

#### Supply Chain

* Disassociated MRP and MPS views.
* MPS: added "validate scenario" configuration to control which MPS proposal is taken into account in the MRP computation.
* Analytic line: added an Analytic line model to manage analytic between objects.

#### Contract

* Contract: Manage analytic accounts per axis and analytic move lines from contract line.
* Contract: added grouped invoicing for contracts.

#### Budget

* GlobalBudget/BudgetVersion: added global budget for a better level management and budget version.
* AccountingReport: New budget report 'Revenue and expenditure state'.

#### Production

* Added production planification scheduling feature.
* Added production planification capacity configuration and re-added infinite capacity features.
* Manufacturing order: added configuration for operation continuity

Operation continuity feature is preventing operations from starting until the previous operations are terminated, canceled or optional. It can be configured on the production process.

* Operation Order: added a configuration to generate timesheet line when a operation order stops.
* Bill of materials: Reworked bill of materials tree by introducing bill of materials line.

#### Business Project

* Project: added invoicing reporting.
* Project Task: generate purchase orders from tasks.

#### Mobile Settings

* Mobile Settings: added HR config for the mobile app.
* MetaJsonField: added configuration to show app builder fields in mobile application.

### Changes

#### Base

* Research request: added a button to open the result object.
* Sequence: letter sequence type has now an adjustable padding
* Sequence: the draft prefix can now be configured

Configure the sequence prefix using the 'Draft prefix' field in the app base.


#### Accounting

* Accounting report: added moves status filter.
* Invoice: added mandatory mentions in invoices addressed to a legal entity (professional customer).
* Move line: added more default filters on the grid view.
* Move Line: amount remaining is now signed, positive if debit line and negative if credit line.
* Accounting batch: added a list of journal for cut off batch.
* Accounting report / VAT Statements: change message box content
* Accounting configuration: allowed to generate fiscal year and period from accounting configuration.
* Custom accounting report: improved errors management.

#### Bank payment

* Bank details: change display for active field

#### CRM

* Lead: displayed the description on the kanban view.
* Lead: added duplicate verification.
* Lead/Prospect: added activity panel to lead and prospect.
* Event: improved event form view.

#### Sale

* Pricing scale: Possibility to choose between previous and next scale for the computation.

#### Purchase

* Displayed product purchase price history in the purchase line form and in the product form.
* Supplier catalog: added the possibility to manage multiple qty per supplier directly on the catalog.

#### Stock

* Stock Correction: added a comment field.
* API Stock: manage comments field on requests.

#### Human resource

* Lunch voucher: added percentage distribution for both formats.

New lunch voucher format "Both". Employee wil be able to choose the percentage of lunch voucher in paper format/card format.

* Timesheet: added a time limit per day.
* Employee: form view review.
* Added demo data for MyHR dashboard.
* Expense line: added an amount limit that can be configured on expense type, preventing expense line to be created if the value is too high.
* Expense: added related expense in the generated move.
* Expense: amount periodic limit for employee.
* Expense line: added Currency field (used only for mobile application).
* HR API: manage `toInvoice` field on expense line creation request.
* HR API: added new API request to check errors and warnings of Expense and ExpenseLine.
* HR API: added new API request to compute distance between two cities.

#### Supply Chain

* SOP/MPS: Rename `manufacturingYear` to `manufacturingPeriod` and added demo data.
* Purchase Order / Sale Order: managed analytic accounts per axis and analytic move lines from sale order line and purchase order line.
* Partner: Customer Situation reporting has been improved and completed with information on deliveries. Also, it is now possible to filter per date or company.

#### Cash management

* Forecast recap: added payment mode and company bank details to forecast recap.

#### Budget

* Budget: added new config in app budget to check if error must be throwed in invoice/moves/orders.

#### Production

* Configurator ProdProcessLine: When managing workcenter groups, work center can still be selected instead of having a default one.
* Cost calculation: the cost prices that were only in work center, can now be overridden by production process lines.
* Production process: added a new configuration to manage times in hundredth of an hour.
* Manufacturing order: added return to draft button in manufacturing order form.


#### Maintenance

* Maintenance request: Duration is now a number of hours in decimal, no longer an integer.

#### Business project

* Project: Improve UX of Task tree.

### Fixed

#### Base

* Modelization fix: added unique true to all one-to-one.

#### Accounting

* AnalyticDistributionTemplate/TradingName/Partner: fixed analytic distribution template management.
* Partner: moved analytic distribution template field to accounting situation.
* Fixed asset: Correct disposal fixed asset depreciation lines which were not generated properly when disposing from a fixed asset.
* Fixed asset: fixed wrong depreciation value for degressive method.
* AppInvoice: removed configuration "is invoice move consolidated" which was not used.
* Accounting report: re organize view form fields display.

#### CRM

* Added a configuration to activate "Search view" feature.

#### Human Resource

* Expense: added control on expense justification file

    - Alert user if some expense lines do not have a justification file.
    - Alert user to keep original document if the file is not a PDF nor an image.

#### Supply Chain

* SOP line: reviewed fields and grid behavior.
* SOP: Removed unused status on SOP line.
* MRP: MRP menu entry is now available from both Purchase and Stock menu.

#### Fleet

* Vehicle: change link between Vehicle and VehicleModel from a one-to-one to a many-to-one.

#### Production

* Manufacturing order: Creating manufacturing orders from sale order will now correctly link parent and child manufacturing orders.
* SOP: Removed unused stock location field on SOP Line.

#### Business Project

* Project: Using company currency symbols on reporting
* Business Project: improved task management and reporting, added a new forecast section.

[7.2.50]: https://github.com/axelor/axelor-open-suite/compare/v7.2.49...v7.2.50
[7.2.49]: https://github.com/axelor/axelor-open-suite/compare/v7.2.48...v7.2.49
[7.2.48]: https://github.com/axelor/axelor-open-suite/compare/v7.2.47...v7.2.48
[7.2.47]: https://github.com/axelor/axelor-open-suite/compare/v7.2.46...v7.2.47
[7.2.46]: https://github.com/axelor/axelor-open-suite/compare/v7.2.45...v7.2.46
[7.2.45]: https://github.com/axelor/axelor-open-suite/compare/v7.2.44...v7.2.45
[7.2.44]: https://github.com/axelor/axelor-open-suite/compare/v7.2.43...v7.2.44
[7.2.43]: https://github.com/axelor/axelor-open-suite/compare/v7.2.42...v7.2.43
[7.2.42]: https://github.com/axelor/axelor-open-suite/compare/v7.2.41...v7.2.42
[7.2.41]: https://github.com/axelor/axelor-open-suite/compare/v7.2.40...v7.2.41
[7.2.40]: https://github.com/axelor/axelor-open-suite/compare/v7.2.39...v7.2.40
[7.2.39]: https://github.com/axelor/axelor-open-suite/compare/v7.2.38...v7.2.39
[7.2.38]: https://github.com/axelor/axelor-open-suite/compare/v7.2.37...v7.2.38
[7.2.37]: https://github.com/axelor/axelor-open-suite/compare/v7.2.36...v7.2.37
[7.2.36]: https://github.com/axelor/axelor-open-suite/compare/v7.2.35...v7.2.36
[7.2.35]: https://github.com/axelor/axelor-open-suite/compare/v7.2.34...v7.2.35
[7.2.34]: https://github.com/axelor/axelor-open-suite/compare/v7.2.33...v7.2.34
[7.2.33]: https://github.com/axelor/axelor-open-suite/compare/v7.2.32...v7.2.33
[7.2.32]: https://github.com/axelor/axelor-open-suite/compare/v7.2.31...v7.2.32
[7.2.31]: https://github.com/axelor/axelor-open-suite/compare/v7.2.30...v7.2.31
[7.2.30]: https://github.com/axelor/axelor-open-suite/compare/v7.2.29...v7.2.30
[7.2.29]: https://github.com/axelor/axelor-open-suite/compare/v7.2.28...v7.2.29
[7.2.28]: https://github.com/axelor/axelor-open-suite/compare/v7.2.27...v7.2.28
[7.2.27]: https://github.com/axelor/axelor-open-suite/compare/v7.2.26...v7.2.27
[7.2.26]: https://github.com/axelor/axelor-open-suite/compare/v7.2.25...v7.2.26
[7.2.25]: https://github.com/axelor/axelor-open-suite/compare/v7.2.24...v7.2.25
[7.2.24]: https://github.com/axelor/axelor-open-suite/compare/v7.2.23...v7.2.24
[7.2.23]: https://github.com/axelor/axelor-open-suite/compare/v7.2.22...v7.2.23
[7.2.22]: https://github.com/axelor/axelor-open-suite/compare/v7.2.21...v7.2.22
[7.2.21]: https://github.com/axelor/axelor-open-suite/compare/v7.2.20...v7.2.21
[7.2.20]: https://github.com/axelor/axelor-open-suite/compare/v7.2.19...v7.2.20
[7.2.19]: https://github.com/axelor/axelor-open-suite/compare/v7.2.18...v7.2.19
[7.2.18]: https://github.com/axelor/axelor-open-suite/compare/v7.2.17...v7.2.18
[7.2.17]: https://github.com/axelor/axelor-open-suite/compare/v7.2.16...v7.2.17
[7.2.16]: https://github.com/axelor/axelor-open-suite/compare/v7.2.15...v7.2.16
[7.2.15]: https://github.com/axelor/axelor-open-suite/compare/v7.2.14...v7.2.15
[7.2.14]: https://github.com/axelor/axelor-open-suite/compare/v7.2.13...v7.2.14
[7.2.13]: https://github.com/axelor/axelor-open-suite/compare/v7.2.12...v7.2.13
[7.2.12]: https://github.com/axelor/axelor-open-suite/compare/v7.2.11...v7.2.12
[7.2.11]: https://github.com/axelor/axelor-open-suite/compare/v7.2.10...v7.2.11
[7.2.10]: https://github.com/axelor/axelor-open-suite/compare/v7.2.9...v7.2.10
[7.2.9]: https://github.com/axelor/axelor-open-suite/compare/v7.2.8...v7.2.9
[7.2.8]: https://github.com/axelor/axelor-open-suite/compare/v7.2.7...v7.2.8
[7.2.7]: https://github.com/axelor/axelor-open-suite/compare/v7.2.6...v7.2.7
[7.2.6]: https://github.com/axelor/axelor-open-suite/compare/v7.2.5...v7.2.6
[7.2.5]: https://github.com/axelor/axelor-open-suite/compare/v7.2.4...v7.2.5
[7.2.4]: https://github.com/axelor/axelor-open-suite/compare/v7.2.3...v7.2.4
[7.2.3]: https://github.com/axelor/axelor-open-suite/compare/v7.2.2...v7.2.3
[7.2.2]: https://github.com/axelor/axelor-open-suite/compare/v7.2.1...v7.2.2
[7.2.1]: https://github.com/axelor/axelor-open-suite/compare/v7.2.0...v7.2.1
[7.2.0]: https://github.com/axelor/axelor-open-suite/compare/v7.1.7...v7.2.0
