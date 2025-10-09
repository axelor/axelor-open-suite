## [8.1.32] (2025-10-09)

### Fixes
#### Base

* Price list: fixed price list filter in grid views.

#### Stock

* Product: fixed Stock and Stock history charts to exclude cancelled stock moves and take real qty into account.
* Product: fixed quantity in stock report with correctly converted unit.

## [8.1.31] (2025-09-25)

### Fixes
#### Account

* Accounting report type: fixed company sync between M2O and M2M fields.


### Developer

#### Account

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

## [8.1.30] (2025-09-11)

### Fixes
#### Base

* Partner: fixed accounting situations when merging partners.
* Databackup: fixed a potential security breach when restoring a backup.

#### Account

* MoveLine: fixed invoice term due date when we update move line's due date with free payment condition.

#### Human Resource

* Lunch voucher: fixed an issue where computation did not deduct ventilated or reimbursed expenses.


### Developer

#### Account

Changed the InvoiceTermService.checkIfCustomizedInvoiceTerms parameter. Now using a list of invoice terms instead of an invoice.
New method in InvoiceTermService, called computeInvoiceTermsDueDates used to recompute the invoice terms due dates when 
changing the move line's due date on a free payment condition.
New method in InvoiceTermService, called recomputeFreeDueDates used to recompute the invoice terms due dates.

## [8.1.29] (2025-08-28)

### Fixes
#### CRM

* Lead: lead partner are now always prospect when converted.

#### Human Resource

* Expense: mail notification are now correctly sent when sending, validating or refusing an expense from API.

## [8.1.28] (2025-08-14)

### Fixes
#### Base

* Price list line: fixed the display issue with the 'Amount' title.
* Data Backup: fixed translations and added help for some fields.

#### Account

* Closure assistant : fixed outrun of year computation doesn't take into account all accountTypes.
* Account management: fixed interbank code issue on 'Direct Debit' payment mode.
* Accounting report: fixed the issue related to amount in Analytic general ledger report.
* ANALYTICDISTRIBUTIONTEMPLATE : duplicated templates shouldn't be visible

#### Bank Payment

* Bank statement: fixed demo data to get dynamic dates and corrected interbank code.

#### Business Project

* PurchaseOrder : fixed technical error when saving a project or a business project on a purchase order.

#### Human Resource

* Issue on Windows when we try to build the AOS project.
* Timesheet: fixed error when generating lines from planning with custom time units

#### Stock

* Product: fixed 'Stock history' chart.
* Inventory: fixed missing parameter for inventory birt template.
* STOCK/LOGISTICALFORM : Fix html column headers titles on line grid

#### Supply Chain

* Sale order: fixed stock location on change of company.
* Sale / Purchase / Stock: fixed some views where quantity and price decimal config wasn't being used.

#### Intervention

* Equipment: removed the default value on 'customerWarrantyOnPartEndDate' to keep it empty on new.


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

Changed the AccountService.computeBalance method parameter. Now using a list of account types instead of an account type.

---

Added AnalyticDistributionTemplateRepository and AnalyticMoveLineService in AnalyticAttrsServiceImpl.
Added AnalyticAttrsService in MoveLineAttrsServiceImpl.
Added parameter 'moveline' in MoveLineAttrsServiceImpl.addAnalyticDistributionTemplateDomain.
Added parameter 'moveLine' in MoveLineGroupServiceImpl.getAnalyticDistributionTemplateOnSelectAttrsMap.

DELETE FROM meta_action WHERE name LIKE 'action-purchase-order-line-attrs-set-domain-analytic-distribution-template';

#### Human Resource

`TimesheetProjectPlanningTimeServiceImpl` now has two new constructor parameters to support `UnitConversions`.

## [8.1.27] (2025-06-26)

### Fixes
#### Base

* Update Axelor Message to 3.1.4.
* Product company: computed sale price on change of 'autoUpdateSalePrice' or 'managPriceCoef'.
* Product category: fixed sequence field display.

#### Account

* Invoice: supplier invoice exchange rate is now based on origin date.
* Accounting export / FEC export payroll entry: fixed the issue where only accounted moves were returned.
* Invoice: fixed invoice term due date when we update invoice due date with free payment condition.
* Accounting report: fixed calculation of Original value and Net carrying amount in accounting report 'Summary of gross values and depreciation'.

#### Contract

* Contract batch: fixed issue where contracts and invoices were not displayed in batch due to missing batch association.

#### Sale

* Partner: fixed error while opening form view of 'Sale details by product'.
* Partner: fixed wrong computation of total price in 'Sale details by product'.

#### Supply Chain

* Sale Order/Purchase Order/Invoice: fixed advance invoice amount on partial invoicing.

## [8.1.26] (2025-06-12)

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

The InvoiceTermServiceImpl.computePercentageSum method now return the amount without scale to be used in computation. If you want to scale it, use the currencyScaleService.getScaledValue(invoice, amount)

## [8.1.25] (2025-06-02)

### Fixes
#### Base

* Group: removed collaboration configuration since it is only available in AOP enterprise.

#### Account

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


### Developer

#### Human Resource

`LunchVoucherMgtLineService#computeRestaurant` and `LunchVoucherMgtLineService#computeInvitation` have a new `Period` parameter.

## [8.1.24] (2025-05-15)

### Fixes
#### Base

* Partner: fixed performance issues when opening emails tab on partner form view.

#### Account

* BillOfExchange/PaymentSession: fixed technical error when cancelling bank order payment then pay the exactly same invoice term.
* Mass entry: fixed critical error when we validate more than ten moves in mass entry process.
* Accounting batch: fixed an issue where the generated result entry (move) was not correctly linked to the corresponding close/open account batch.
* Auto reconcile/Partner: restricted auto reconcile between customer and supplier invoices on partner with compensation enabled.

#### Bank Payment

* BankOrder/Umr: fixed the selection of the partner active umr in bank order confirmation.
* Bank payment config: removed the possibility to select view type account on internal and external bank to bank account.

#### Budget

* PurchaseOrder/Budget: fixed budget exceed error when using mono budget on purchase order.

#### Human Resource

* Expense: removed the possibility to duplicate an expense.
* Expense: fixed duplicate move when we confirmed a bank order from an expense.

#### Project

* Project: fixed Project activity dashboard to fetch only relevant messages, avoiding unnecessary loading and filtering.

#### Sale

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

## [8.1.23] (2025-04-30)

### Fixes
#### Base

* Update Axelor Open Platform to 7.1.14.
* Product: fixed update product prices process to update company specific prices.
* Template: on creating a new template manually, keep email account empty by default so the generated message can use default email account.

#### Account

* Analytic move line: set analytic axis required when we create an analytic move line.

#### Budget

* Purchase order line: use the correct account when a purchase order line is set to 'fixed assets'.

#### Business Project

* Invoice: fixed third-party payer when generating an invoice from an invoicing project.

#### Contract

* Contract invoicing batch: fixed an issue where it was not possible to generate more than one invoice for a same contract.

#### Human Resource

* Expense: fixed personal expense and personal expense amount french translation.
* User: hide create employee button if the partner has already an employee.

#### Production

* Manufacturing order: fixed an issue where producible quantity was taking into account component that were not managed in stock.
* Prod process line: fixed domain of stock location to take into account only those usable on production.

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

## [8.1.22] (2025-04-17)

### Fixes
#### Base

* Updated axelor-studio dependency to 3.1.11.

#### Account

* Reconcile: removed check on tax for opening/closure.
* Account management: fixed fields not required in editable grid of payment mode.
* Invoice: fixed due date when we save with free payment condition.
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

#### Stock

* Stock move line: fixed total net mass calculation when real quantities are generated.

#### Supply Chain

* Sale order: fixed sale order invoicing state when a line has a negative amount.

## [8.1.21] (2025-04-03)

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

* Invoicing project: fixed an issue where non validated timesheet lines were included.

#### Contract

* Contract line: fixed domain for product based on contract target type.

#### Production

* Bill of material: fixed an issue where a component could not be deleted from the BOM line list after the tree view was opened.

#### Sale

* Sale order: fixed wrong behaviour on a prospect with accepted credit.

#### Stock

* Stock move: fixed average price not updated on product when stock move is canceled.
* Inventory: gap and real values computation now correctly takes into account company product cost/sale/purchase price.


### Developer

#### Account

Added `CurrencyService` and `CurrencyScaleService` to the constructor of `PaymentVoucherControlService`.

## [8.1.20] (2025-03-20)

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

#### Purchase

* Supplier catalog: products with multiple catalogs now take the catalog with the most recent update date.

## [8.1.19] (2025-03-13)

### Fixes
#### Base

* Base: fixed some errors displayed as notification instead of popup.
* Signature: fixed broken grid view when selecting a certificate.
* User: made the title of the company set panel visible.

#### Account

* Accounting batch: ignored check at reconcile when move only contains tax account.

#### Business Project

* Invoice: fixed fiscal position when generating an invoice from an invoicing project.

#### CRM

* Partner: creating a new partner cannot create a prospect and a supplier at the same time.

#### Human Resource

* HR: fixed an error occurring when using 'Leave increment' batch and if employees do not have a main employment contract.

#### Sale

* Sale order line: fixed advanced filter not displayed unless the whole page is refreshed.

#### Stock

* Stock location: fixed error when emptying parent stock location.

#### Supply Chain

* Purchase order: fixed an issue where it was possible to invoice a purchase order already invoiced.

## [8.1.18] (2025-02-20)

### Fixes

#### Account

* Invoice: fixed contact being readonly when the partner is an individual.
* Invoice payment: forbid the creation of a 0 amount payment.
* Payment session: fixed infinite loop when searching eligible invoice terms.
* Fiscal position: fixed NPE on adding account equivalence on new record.
* Move: fixed company bank details domain when no payment mode is selected.
* Invoice: fixed wrong default payment condition.
* Journal: fixed error while importing france chart of accounts.
* Move: fixed the transactional error at move delete.

#### Contract

* Contract: fixed no analytic move lines on contract duplication.
* Contract version: fixed file link is lost when creating a new amendment.

#### Human Resource

* Timesheet: fixed an issue in lines generation from project planning where end date was not taken into account.

#### Marketing

* Campaign: fixed FR translation for 'Event start'.

#### Quality

* QI Resolution: fixed wrong translation in English for the term default.

#### Sale

* Configurator: fixed default value that was not set with configurators
* Configurator product formula: disabled test button if formula is empty.

## [8.1.17] (2025-02-06)

### Fixes
#### Base

* Update Axelor Open Platform to 7.1.12.
* Advanced export: fixed NPE when target field is empty on advanced export line.
* Period: fixed inconsistency when filling dates on form view.
* Pricing: use formula filtering also on linked pricing.

#### Account

* FEC import: fixed an issue during accounting entries import where the entries were validated without any checks.
* Move: fixed description is not inherited on move lines when they are generated from mass entry lines.
* Accounting config: fixed translations on new company creation.

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

#### Sale

* Sale order line: added missing translation for 'Customize BOM' and 'Customize production process'.
* Sale order: fixed advance payment amount during copy.

#### Stock

* Stock move: fixed split into fulfilled line and unfulfilled one total net mass issue.
* Stock move: fixed split into 2 total net mass issue.

#### Supply Chain

* Stock move: fixed an error during mass customer stock move invoicing.

## [8.1.16] (2025-01-23)

### Fixes
#### Base

* Updated axelor-utils dependency to 3.1.5, fixing an issue that could prevent PDF generation in some cases.
* Updated bouncycastle dependency to improve security.
* Currency: fixed 'codeISO' field emptying on load.
* Product: fixed the incorrect domain on historical orders to display validated orders today.
* User: made phone and email fields read-only as long as the user does not have a linked contact.

#### Account

* Move line query line: removed taxes from the grid to recover the broken grid view.
* FEC Import: set VAT system on move lines during import.

#### Business Project

* Invoicing project: fix project task progress in invoicing project annex report.

#### Contract

* Contract: fixed the filter on partner field to select only customer/supplier respectively on customer/supplier contracts.

#### CRM

* Partner: creating a new partner is no longer a prospect and a customer at the same time.

#### Project

* User: Hid the active project field if the project module isn't installed.

#### Sale

* Sale order: fixed impossible to invoice the remainder of an order when a deposit has already been paid and charged to a partial invoice.

#### Stock

* Stock move line: fixed the issue by making the availability column readonly.
* Stock move: compute total net mass when splitting lines.

#### Supply Chain

* Purchase order: fixed fiscal position when creating a purchase order from sale order.
* Stock move: stock move mass invoicing now generates an invoice with the correct invoicing address.
* Stock move invoicing: missing translation in wizard on stock move lines.


### Developer

#### Base

The dependency `'org.bouncycastle:bcprov-jdk15on:1.70'` was replaced by `'org.bouncycastle:bcpkix-jdk18on:1.78.1'`. If you are using an AOS module with other modules that depends on `'org.bouncycastle:bcprov-jdk15on:1.70'`, please change your gradle configuration to avoid a conflict or update your dependencies.

#### Account

Added `MoveLineTaxService` dependency to `FECImporter` class.

## [8.1.15] (2025-01-09)

### Fixes
#### Base

* Email account: fixed NPE that occurred when setting 'Default account' to false.
* City: fixed issue where country was not filled when adding a new city from address form.
* Registration number template: initialized 'Starting position in the registration number' to 1 on new record to prevent save errors.
* Partner: fixed error when duplicating a partner without a picture.

#### Account

* Accounting batch: result move computation query takes into account accounted entries.
* Move / Moveline: added additional control to avoid unbalancing input of entries on general / special accounts at validation
* Account: fixed hard coded db schema in native queries.
* Payment voucher: fixed unexpected pop-up mentioning no record selected while there is at least one.
* Invoice: removed incoherent mention of refund line in printing when it's not originated from actual invoice.
* Bank reconciliation: fixed the filter to display move lines with no function origin selected and hide it when already reconciled in different currency than move in 'Bank reconciliation statement' report.
* Accounting report type: fixed comparison in custom report types demo data.

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

* Sale configurator: fixed issue where formula was not added when verifying the groovy script then directly exiting the pop-up

#### Stock

* Logistical form: fixed unique constraint violation error.

#### Supply Chain

* Stock move: fixed invoiced and remaining quantity in wizard when partially invoicing
* MRP: purchase orders generated by the MRP have the fiscal position correctly filled.


### Developer

#### Supply Chain

`UnitConversionService` has been added to the constructor of `StockMoveInvoiceServiceImpl`.

## [8.1.14] (2024-12-20)

### Fixes
#### Account

* Invoice: fixed a regression preventing invoices refund.

## [8.1.13] (2024-12-19)

### Fixes
#### Base

* Updated Axelor Open Platform to 7.1.11.
* Updated studio module to 3.1.10.
* Product: fixed french translation for 'Product'.
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

#### Contract

* Contract template: fixed recurring product display for supplier contract.

#### CRM

* Partner: fixed the display issue in event panel when images are attached in the event description.

#### Human Resource

* Lunch voucher: leave request with hour leave reason type are now ignored.
* HR API: fill expense type select on creation
* Expense API: fixed the error when an employee did not have a contract while creating an expense.

#### Production

* Purchase order: fixed wrong domain for the dashlet on outsourcing tab.
* Manufacturing order: merging manufacturing orders now correctly takes into account scheduling configuration.

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

## [8.1.12] (2024-11-28)

### Fixes
#### Base

* Updated studio module to 3.1.7.
* Template: changed title from 'Print Template' to 'Print template'.
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

#### Contract

* Contract: fixed error preventing from contract copy.
* Contract: prorata is now based on invoice period start date instead of contract start date.

#### Human Resource

* Lunch voucher: fixed computation for leaves with overlapping periods.
* Employee: checked unicity constraints when creating user at the end of employee creation process.
* HR batch: fixed an error occurring when launching Leave Management Reset Batch.
* Leave request: fixed future quantity day computation when sending a leave request.
* Timesheet: fixed NPE because of daily limit configuration.

#### Production

* Production API: fixed error while fetching consumed products.

#### Sale

* Sale order line: fixed issue when opening a sale order line with analytic enabled.
* Sale order: fixed price list value on change of client partner.
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

Renamed `action-condition-user-validCode` to `action-user-method-validate-code`.

## [8.1.11] (2024-11-14)

### Fixes
#### Base

* Updated Axelor Open Platform to 7.1.10.
* Updated studio module to version 3.1.6.
* Updated message module to version 3.1.3.
* Updated utils module to version 3.1.3.
* Partner: tax number will be displayed for suppliers too along with customers.
* Pricing: fixed the product category filter in pricing formula.

#### Account

* Payment voucher: fixed disable apply financial on payment voucher.
* Analytic move line query: fixed always readonly button in 'Analytic move lines to reverse' dashlet.
* Accounting export: fixed skipped lines on accounting export when we have more than 10 000 lines.
* Accounting report: replaced move line partner full name with partner sequence and partner full name in reports.
* Invoice: fixed NPE when clicking on print button on grid lines.
* Move/Analytic: record analytic account on moveline on the reverse move
* Partner: fixed an issue where the partner balance was wrong after an unreconcile.
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

#### Supply Chain

- Added new arguments to SaleOrderInvoiceService.displayErrorMessageIfSaleOrderIsInvoiceable()
- Updated SaleOrderInvoiceService.computeAmountToInvoice visibility to protected and removed it from interface

## [8.1.10] (2024-10-31)

### Fixes
#### Base

* Sequence: fixed draft prefix when checking for the draft sequence number.
* Birt report: fixed number formatting for excel format.
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

* Manufacturing order: fixed issue when updating quantity in manufacturing order.

#### Purchase

* Purchase order: fixed value of 'total without tax' in birt report.

#### Sale

* Sale order: fixed an issue preventing from editing a sale order with editable grid and pending order modification enabled

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

## [8.1.9] (2024-10-17)

### Fixes
#### Base

* Webapp: updated Axelor Open Platform dependency to 7.1.7.
* Update axelor-studio dependency to 3.1.4.
* Price list: fixed check on dates on save.
* Partner: when duplicating a partner, do not copy partner addresses and correctly create a copy of the original partner's picture.
* Home action: fixed display issue in user and group form views.

#### Account

* Invoice: fixed error when we duplicate an invoice and change any field on invoice line.
* Account: computed amounts when we change invoice date.
* Tax payment move line: fixed an issue where reverse taxes were not reverted, which was making VAT statement reports wrong.
* Invoice: fixed rounding issue on taxes on ventilated move accounting.
* Accounting report: fixed amounts with currency decimal digits in accounting report 'Revenue and expenditure state'.
* Accounting report: fixed NPE when selecting report type on form opened from year closure.

#### Human Resource

* Timesheet API: fixed an error occurring when creating a timesheet without timer
* Lunch vouchers: fixed an issue where some employees were not included in lunch voucher computation.

#### Project

* Project: fixed code when generating project from sale order.

#### Sale

* Sale Order: fixed an issue were sequence was reset when going back to draft by creating a new version.
* Sale order: added discounted unit price to editable grid.
* Complementary product selected: correctly prevent the user from modfying selected complementary product on a confirmed sale order.

#### Stock

* Stock API: fixed issue on stock correction creation request.
* Inventory: after an import, display filename instead of file path in the confirmation message.
* Stock location: added a filter to not select itself or any sub stock locations as parent stock location.


### Developer

#### Account

Method signature have changed in InvoiceLineService.class :

```java
public void compute(Invoice invoice, InvoiceLine invoiceLine) throws AxelorException;
```

became

```java
public Map<String, Object> compute(Invoice invoice, InvoiceLine invoiceLine) throws AxelorException;
```

---

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

#### Stock

Created new interface and class `StockLocationAttrsService' and 'StockLocationAttrsServiceImpl`. 
In `stock-location-form` changed parentStockLocation onSelect action from `action-stock-location-attrs-set-parent-stock-location-domain`
to `action-stock-location-method-set-parent-stock-location-domain`, the former action has not been removed.

## [8.1.8] (2024-10-03)

### Fixes
#### Base

* Webapp: updated Axelor Open Platform dependency to 7.1.5.
* ABC analysis: fixed an issue where printing an unsaved analysis was not setting correctly the sequence,and added sequence in demo data.
* Batch: added french translation for '* %s anomaly(ies)'

#### Account

* Accounting report: added filter on company for report type in custom reports if companies are empty.
* Partner blocking: fixed 'blocking partner with late payment' feature which resulted in some processes being stuck.

#### Bank Payment

* Bank order/ Bank statement: fixed broken reports.

#### Business Project

* Invoicing project annex report: fixed empty values in multiple columns.

#### Contract

* Contract template: removed unused condition related to statusSelect field.

#### Helpdesk

* Ticket: fixed the french translation  of 'Assign to me'.

#### Human Resource

* Payroll preparation: improved export to Silae by adding the number of lunch vouchers and employee bonus.
* Payroll preparation: fixed employee bonus amount computation by correctly ignoring lines that were not computed.
* Leave request: fixed the leave reason domain when employee is null.
* Project planning time: fixed error while selecting unit when time unit is null.
* Expense/ Leave request/ Timesheet/ TSTimer: fixed creation with empty employee when connected user doesn't have employee.
* Payroll preparation: improved export to Nibelis by adding the number of working days in the period.

#### Project

* Project: fixed issue related to contact partner when updating client partner.

#### Stock

* Logistical form line: fixed wrong calculation of net weight.
* Stock move line: the advanced filter saved is not displayed unless we refresh the whole page.

#### Supply Chain

* Sale order invoicing: removed partially invoiced flag when invoice is fully refunded.

#### Intervention

* Equipment: fixed contract domain to have only billing contracts.

## [8.1.7] (2024-09-19)

### Fixes
#### Base

* Update axelor-studio dependency to 3.1.3.
* Partner: fixed convert contact into individual partner error when address is null.
* Advanced export: fix export when multiple fields contain same sub path.
* Request: fixed french translation for 'Request'.
* Address: in form view, do not empty the ZIP code when filling the city, and automatically fill the city when the ZIP code is filled.

#### Account

* CutOff/Analytic: fixed cut off batch when using analytic on credit move line.

#### Budget

* Budget level: fixed scales in the tree view.
* Global budget: fixed duplication without budget level.

#### Business Project

* Invoicing project: attach annex to invoice when 'Attach the Annex to the invoice' is enabled while generating invoice.
* Project: prevented an error during project totals computation when parent task had no time unit.
* Project task: fixed product changing on assignedTo when linked to a framework contract.
* Project: prevented an error during project totals computation when spent time percentages were too low.

#### Contract

* Contract: fixed an error when opening a product in an amendment.
* Contract: fixed error while generating contract from opportunities.

#### CRM

* CRM: added missing action for 'Calls monitoring' dashboard.

#### Helpdesk

* Ticket API: fixed an issue where condition in permission were not evaluated correctly to update tickets.

#### Human Resource

* Timesheet line: fixed issue related to default product value.

#### Mobile Settings

* Mobile Dashboard API: fixed an issue where condition in permission were not evaluated correctly to get mobile chart information.

#### Production

* Sequence per workshop config: improve UI to select workshop stock location and manuf order sequence.

#### Purchase

* Purchase order line tax: fixed reverse charge feature on purchase order.

#### Sale

* Configurator: added field 'attributes' and 'indicators' in grid view.

#### Stock

* Stock: fixed an error occurring when splitting a stock move line with a real quantity greater than the expected quantity.

#### Intervention

* Company: fixed display of intervention config button on company when module is uninstalled.
* Contract: 'On-call planning' field will be required if 'On-call management' is true.

## [8.1.6] (2024-09-05)

### Fixes
#### Base

* Partner: fixed an address not linked to a partner in demo data.

#### Account

* Invoice: fixed an issue where tab title for credit note was wrong.
* Invoice payment: fixed move display in payment details grid view.
* Payment Voucher: fixed wrong amount on generated invoice payment when using financial discount.
* Invoice: fixed an issue preventing invoice ventilation when pack feature is used.
* Mass entry move line: increased width of some columns.

#### Budget

* Purchase order line: fixed an issue where budget panel was editable on a confirmed purchase order.

#### Contract

* Contract: fixed an issue where payment mode and payment condition were not filled by default.
* Contract: fixed display issue of 'isPeriodicInvoicing' field.

#### Human Resource

* Timesheet: opened timesheets are no longer dirty when opening the form view.

#### Maintenance

* Menu: fixed icon for 'Configuration' menu.

#### Production

* Manufacturing order: fixed unit conversion when computing missing components label.
* Manufacturing order API: fixed an issue where condition in permission were not evaluated correctly to see and add products from a manufacturing order.
* Operation order API: fixed an issue where condition in permission were not evaluated correctly to update operation orders.
* Manufacturing order: fixed wrong priority on the sub manuf order.
* Manufacturing order: fixed title for produced quantity in produced products form view.
* Manufacturing order: fixed an issue occurring when adding produced products.

#### Supply Chain

* Invoicing: fixed an issue preventing stock moves/order/contracts invoicing with analytic accounting lines.
* Invoicing: fixed internal reference on invoices generated from delivery stock move missing sale order reference.
* Analytic panel: fixed display issue when product family is empty.

## [8.1.5] (2024-08-22)

### Fixes
#### Base

* Address: removed unused fields related to invoicing/delivery/default address in base address form.

#### Account

* Mass entry move line: increased width of the date columns.
* MoveLine: fixed tax management in form view
* Fixed asset: removed disposal info during copy.
* Accounting report: fixed wrong amounts when selecting accounts with parent accounts.
* Invoice: fixed unable to display invoice lines (grid view) when one or more invoices are selected.

#### Bank Payment

* Bank statement: fixed name when loading bank statement lines.

#### Budget

* StockMove/Invoice: fixed technical error when invoicing a stock move line with a value of zero.

#### Business Project

* Project: prevented an error during project totals computation when spent time percentages were too high.
* Project/Kanban: use the same controls as when you change the project status in kanban view.

#### Contract

* Contract: removed unused action.

#### Helpdesk

* Ticket type: fixed 'Ticket per status' chart.

#### Human Resource

* App Project: added missing french translation.
* Employee bonus management: fixed computation process when there is no user linked with employee.
* Project: added missing french translation in the planning tab.
* Leave reason: fixed typo in french translation.

#### Mobile Settings

* Added missing english translation 'Order'.

#### Sale

* Sale order line: take product application start date into account when selecting product.
* Sale config: fixed 'default holdback percentage' display.
* Complementary product: removed field 'optional' from partner views.

#### Stock

* Stock config: fixed translation for 'Send email on supplier arrival cancellation'
* Stock API: fixed an issue where condition in permission were not evaluated correctly to read stocks and modify/create stock moves.
* Inventory API: fixed an issue where condition in permission were not evaluated correctly to update an inventory.

#### Supply Chain

* Supplychain config: fixed 'Update customer accepted credit' process updating the credit for all companies instead of the current one.

## [8.1.4] (2024-08-08)

### Fixes
#### Base

* Partner : fixed error on change of partner type.

#### Account

* AccountingBatch : fixed auto move lettering batch proposals
* AccountingBatch : fixed reconcile by balanced move mode in auto move lettering batch
* AccountingBatch : fixed auto move lettering batch due to negative credit move line amount remaining

#### Bank Payment

* BankOrder : fixed manual multi currency bank order's move generation

## [8.1.3] (2024-07-25)

### Fixes
#### Base

* Webapp: updated Axelor Open Platform dependency to 7.1.4.
* Update axelor-studio dependency to 3.1.2
* Address Template: fixed defaut address template so zip code should come before the city.
* Partner: fixed error popup when opening partner without accounting situations.
* Partner: in demo data, set 'Is internal' on partner Axelor.
* Translation: fixed an issue where 'Canceled', 'Confirmed', 'Received' french translations were wrong.
* Product: reset the serial number on product duplication.

#### Account

* Invoice/Advance Payment: fixed an issue preventing invoice ventilation if multiple advance payments existed.
* Move: fixed debit/credit scale when we change move line currency amount.
* Payment voucher: fixed an issue preventing the payment of invoices with financial discount.
* Move: fixed issue preventing partner selection if the move has a journal with multiple compatible partners.
* Invoice: fixed an issue where an exonerated tax had a VAT system filled.
* Move line mass entry: set description required following account configuration.
* Mass entry: fixed analytic axis empty on partner selection.
* Fixed asset: fixed the depreciation values panel readonly if 'Is equal to fiscal depreciation' is enabled.
* Analytic: fixed required analytic distribution template when the analytic distribution type is per Product/Family/Account.

#### Bank Payment

* Bank reconciliation: fixed total of selected move lines in multiple reconciles when currency is different from company currency.

#### Business Project

* Invoicing project: fixed timesheet line invoicing filter.

#### Contract

* Contract: fixed batch contract revaluation process order.

#### CRM

* Opportunity status: fixed data init to use french status name instead of english.
* Opportunity type: remove unused reference from data init config, this will remove a warning when loading the application on a new database.

#### Human Resource

* Expense line: fixed error when computing kilometric distance without choosing a type.
* Employee: fixed card view display when using dark theme.

#### Production

* Translations: fixed french translation for prod process and bom, form view title was plural instead of singular.
* Product: fixed action from production referenced in base.

#### Project

* Project: fixed planning panel display for unsaved record.
* Project: fixed the typo in french translation for unit help.
* Project API: fixed an issue where condition in permission were not evaluated correctly to access unit and planning time of a project.

#### Purchase

* Purchase order: fixed french typo for 'nouvelles version'.

#### Quality

* Control entry API: fixed an issue where condition in permission were not evaluated correctly to access a control entry.

#### Sale

* Configurator creator: fixed issue related to meta json field simple name.
* Partner: added missing french translation for 'generation type' in complementary product tab.
* Sale order: fixed sale order sequence when creating new version.
* Sale order: fixed an issue preventing from invoicing X% of a sale order as an advance payment where X was greater than the sale order total.

#### Supply Chain

* Timetable: fixed the scale issue to compute amount on change of percentage.
* Stock move/Invoice: fixed unique invoice generation from stock move reversion.
* Sale order: fixed partial invoice generation with title line.


### Developer

#### Production

Created a new action group `action-product-group-production-onload` which will be run onLoad of the product form view,
replacing `action-group-product-onload`: if you override `onLoad` of product form in your module, please check related change.

## [8.1.2] (2024-07-11)

### Fixes
#### Base

* Webapp: updated Axelor Open Platform dependency to 7.1.3.
* Update axelor-studio, axelor-message and axelor-utils dependency to 3.1.1. This is fixing an issue where, with a default gradle configuration, the latest AOP snapshot was used instead of the version set in the webapp.
* Partner: fixed issue related to view extension in CRM, causing an error when reloading views.
* Currency conversion line: fixed an error preventing exchange rate update.
* Fixed an issue where the logo defined in properties was not used in the login page.
* City: fix city demo data import
* Product: Fixed display of tracking number panel.
* Product: fixed NPE when duplicating and saving a product.
* Printing Template: technical refactor to be able to extend in other modules.
* BIRT template: fixed error message when required BIRT parameter is missing.

#### Account

* Unreconcile: fixed invoice terms amount on both move lines.
* Accounting report: fixed missing assets and disposal column value on 'Gross value and depreciations' report.
* Accounting report: fixed Summary table of VAT Statement on invoices report which was not displaying all data.
* Block customers with late payment batch: fixed an issue where the batch did not block some partners.
* Analytic/InvoiceLine: removed analytic when account does not allow analytic in all configuration.
* Accounting situation: fixed VAT system display when partner is internal.
* MoveReverse: fixed imputation on reverse move invoice terms.
* Accounting report: fixed summary table on the first page of the VAT Statement on payments displaying wrong values.

#### Bank Payment

* BankStatementLine: Set demo statement with operation dates/value date in 2024

#### Budget

* Purchase order line: fixed an issue where changing the account in tab budget changed the analytic configuration on the line.

#### Contract

* Contract line: fixed an issue where analytic distribution panel was never hidden.

#### CRM

* Catalog: fixed an issue where the user could upload files other than PDF.

#### Human Resource

* Expense line: orphan expense line are now also digitally signed if there is a justification file
* Employee: Added demo Data for HR employee
* Timesheet line: Fixed timesheet line deletion when it is linked to timer.

#### Production

* Production order: fixed production order sequence generated from product form not using the correct sequence in a multi company configuration.

#### Project

* Sale order: Fixed project generated with empty code which could trigger a exception

#### Sale

* Sale order template: fixed NPE when company is empty.
* Sale order template: fixed NPE when currency or partner is empty.

#### Stock

* Sales dashboard: fixed stock location for customer deliveries.

#### Supply Chain

* Invoice: removed time table link when we merge or delete invoices, fixing an issue preventing invoice merge.
* Translation: fixed alert message related to partner language always showing due to localization.


### Developer

#### Base

Added the possibility to extend `base.printing.template.type.select` easily from other modules so any module can add new classes used to generate PDF from AOS models.

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

## [8.1.1] (2024-06-27)

### Fixes
#### Base

* App base: removed admin module reference from modules field.
* Partner: fixed number of decimals displayed on debit balance.
* Template: Fixed translation for content panel title.
* BIRT Template: fixed default BIRT parameter templates so they can be used in mail templates.
* Partner: fixed merging duplicates not working correctly.
* BIRT Template: fixed wrong report used when getting report from source.

#### Account

* Move: fixed error popup changing date on unsaved move.
* Move: fixed blocking message due to a wrong tax check preventing some accounting moves validation.
* Invoice payment: fixed wrong scale for prices in form view.
* Mass entry: fixed error when a user without an active company is selecting a company.
* Payment voucher: fixed technical error when the user's active company is null and there are more than one company in the app.
* Invoice/MoveLine: fixed financial discount amount and currency amount scaling.
* Mass entry: fixed today date settings in dev mode not working with created lines from mass entry form.
* Tax: fixed display of non deductible tax fields on account management form.
* Account config: improve the interface to prevent customer blocking if 'Late payment account blocking' configuration is disabled.
* Move/Analytic: fixed negative analytic amounts when generating cut off moves.
* Invoice: fixed foreign currency invoice duplication when current currency rate is different from the currency rate in the original invoice.
* Invoice: fixed an issue where due date was not updated correctly when saving an invoice.

#### Bank Payment

* Bank statement line: prevent user from creating a new bank statement line manually.
* Bank reconciliation: fixed move line filter when using multiple reconciliations.

#### Budget

* Budget: fixed an issue where the default budget structure was not fetched correctly on budget creation.

#### Contract

* Contract: fixed an issue where the value of yearly ex tax total was not revalued.
* Contract: fixed missing Contract type select in Contract template english demo data.
* Contract: fixed a bug where it was impossible to activate a contract when automatic invoicing is enabled.
* Contract: fixed tax error message related to supplier contracts.

#### Human Resource

* Payroll Preparation: fixed an issue were leaves were not always displayed.
* Increment leave batch: fixed an issue where the batch did not update some employees.

#### Purchase

* Purchase order: Fixed purchase order lines not displayed on 'Historical' menu.

#### Supply Chain

* Fixed forwarder partner domain filter.

#### Intervention

* Sequence: fixed sequence import in intervention demo data.


### Developer

#### Base

Replaced `action-partner-record-set-positive-balance` by `action-partner-method-set-positive-balance` in Partner form.

## [8.1.0] (2024-06-14)

### Features
#### Base

* Updated Axelor Open Platform to 7.1. You can find all information on this release [here](https://github.com/axelor/axelor-open-platform/blob/7.1/CHANGELOG.md).
* Partner: add a generic system to manage other type of company registration than SIRET.
* Product: configuration about perishability and warranty are moved to the tracking number.
* Print template: a new table "Print template" was added. This adds possibilities when printing any model from processes or from the interface, like adding a PDF page at the end of the PDF generated by BIRT, or adding a "Print" button on the interface simply by activating a template.
* Address template: for address specific to a country, it is now possible to configure exactly how the address should be written.
* Email template: manage multiple localizations in email template.
* Migration: add new tool to check if meta fields and meta models are still present after a migration.
* Migration: add new tool to find permissions that are on removed/renamed models.
* Quick menus: implement quick menus to manage active company change, and get technical information for dev instances.

#### Intervention

* Equipement management.

Add support to generate equipment manually or generated with templates. Each equipment can be one or more products. Add equipment family that is used to manage intervention and for a tree visualization.

* Interventions management.

Add a generic intervention logic, that can be used for multiple kind of services: technical maintenance, after-sales services, hardware repair, ...

They can be generated from a customer request or contracts. Each intervention can generate a configurable questionnaire: for example technical check on a machine, measurements, storing a signature, ...

#### Human Resource

* Bank card: add a new table for managing employee bank card.
* Expense: Manage expense for external employees (freelance).

#### Supply Chain

* Sale order: allow to mass invoice sale order lines from different sale orders.

#### Account

* Manage multi-taxes: link to tax are replaced by a link to multiple taxes. Invoice, orders as well as fiscal position and other configurations are modified.
* Manage non deductible taxes.
* Invoice: manage refund on advance payment invoices.
* Invoice: manage vat on advance payment invoices payment.
* Bill of exchange: add support of bill of exchange on payment session.
* Bill of exchange: allow to cancel a bill of exchange from the invoice.
* Custom accounting report: manage multi company.

#### Budget

* Add export to budget. An exported budget can be imported using advanced import feature.

#### Production

* SOP: new batch to compute real values.

#### Contract

* Addition of framework contracts and allows sale order from them.
* Addition of a new type of contract to manage year end bonus.
* File management on contracts has been reworked.

#### Project

* Management of progress between parent and children tasks.
* Automatic creation of events in calendar for planned times.

#### Business project

* Site: manage sites for business projects.
* Added holdbacks management on business projects.
* Implemented progress billing based on tasks from a project.
* Manage analytic accounting on projects.
* Manage multiple expenses invoicing from a business project, to invoice to the customer expenses made by employees.

### Changes

#### Base

* Pricing scale: add a type to manage generic pricing scale with specific pricing scale.
* Events planning: add a new tool to generate planning lines on a given period.
* Exchange rate: improve exchange rate computation to minimize rounding errors.

#### Account

* Partner: manage RUM per company
* Accounting move template: add functional origin.
* Fixed assets: allow to "import" manually a fixed asset through a new wizard.
* Accounting batch: when opening/closing accounts, add the possibility to choose the status of generated moves.
* Accounting batch: when closing a period, add a config to delete simulated moves.

#### Bank Payment

* Improve bank reconciliation view.
* Add a new bank statement format import (csv).

#### Human Resource

* Timesheet: get the timesheet product from the project task.
* Expense: improve expense declaration.

#### Stock

* Inventory: add the possibility to init product WAP for a product not already available in stock.
* Tracking number: manage origin of tracking numbers.
* Tracking number: add dimensions on tracking numbers.

#### Production

* MRP: rework manufacturing order proposal date computation to correctly manage prod process line with the same priority.
* Add a new configuration per company "Residual stock location".
* Prod process import: manage files.
* Outsourcing: manage outsourcing with planification.

#### Contract

* Manage title lines on contracts.
* Manage price list on contracts.
* Improve UI/UX on contract form view.

#### Project

* Manage default status from project configuration.

#### Business project

* Reworked sale order invoicing from a business project.

#### Support

Partner: add a panel in the form view to show tickets related to the partner.


### Fixes

#### Base

* Product: modified volume computation.
* Make sure that `__locale` parameter is used instead of `Locale` in birt report so the localization is always correctly applied in printings.

#### Human Resource

* Timesheet: Reworked automatic timesheet line addition when the employee is on leave.

#### Production

* Bill of materials: fixed namecolumn management in bill of materials so the user can write a name instead of having only a generated one.

[8.1.32]: https://github.com/axelor/axelor-open-suite/compare/v8.1.31...v8.1.32
[8.1.31]: https://github.com/axelor/axelor-open-suite/compare/v8.1.30...v8.1.31
[8.1.30]: https://github.com/axelor/axelor-open-suite/compare/v8.1.29...v8.1.30
[8.1.29]: https://github.com/axelor/axelor-open-suite/compare/v8.1.28...v8.1.29
[8.1.28]: https://github.com/axelor/axelor-open-suite/compare/v8.1.27...v8.1.28
[8.1.27]: https://github.com/axelor/axelor-open-suite/compare/v8.1.26...v8.1.27
[8.1.26]: https://github.com/axelor/axelor-open-suite/compare/v8.1.25...v8.1.26
[8.1.25]: https://github.com/axelor/axelor-open-suite/compare/v8.1.24...v8.1.25
[8.1.24]: https://github.com/axelor/axelor-open-suite/compare/v8.1.23...v8.1.24
[8.1.23]: https://github.com/axelor/axelor-open-suite/compare/v8.1.22...v8.1.23
[8.1.22]: https://github.com/axelor/axelor-open-suite/compare/v8.1.21...v8.1.22
[8.1.21]: https://github.com/axelor/axelor-open-suite/compare/v8.1.20...v8.1.21
[8.1.20]: https://github.com/axelor/axelor-open-suite/compare/v8.1.19...v8.1.20
[8.1.19]: https://github.com/axelor/axelor-open-suite/compare/v8.1.18...v8.1.19
[8.1.18]: https://github.com/axelor/axelor-open-suite/compare/v8.1.17...v8.1.18
[8.1.17]: https://github.com/axelor/axelor-open-suite/compare/v8.1.16...v8.1.17
[8.1.16]: https://github.com/axelor/axelor-open-suite/compare/v8.1.15...v8.1.16
[8.1.15]: https://github.com/axelor/axelor-open-suite/compare/v8.1.14...v8.1.15
[8.1.14]: https://github.com/axelor/axelor-open-suite/compare/v8.1.13...v8.1.14
[8.1.13]: https://github.com/axelor/axelor-open-suite/compare/v8.1.12...v8.1.13
[8.1.12]: https://github.com/axelor/axelor-open-suite/compare/v8.1.11...v8.1.12
[8.1.11]: https://github.com/axelor/axelor-open-suite/compare/v8.1.10...v8.1.11
[8.1.10]: https://github.com/axelor/axelor-open-suite/compare/v8.1.9...v8.1.10
[8.1.9]: https://github.com/axelor/axelor-open-suite/compare/v8.1.8...v8.1.9
[8.1.8]: https://github.com/axelor/axelor-open-suite/compare/v8.1.7...v8.1.8
[8.1.7]: https://github.com/axelor/axelor-open-suite/compare/v8.1.6...v8.1.7
[8.1.6]: https://github.com/axelor/axelor-open-suite/compare/v8.1.5...v8.1.6
[8.1.5]: https://github.com/axelor/axelor-open-suite/compare/v8.1.4...v8.1.5
[8.1.4]: https://github.com/axelor/axelor-open-suite/compare/v8.1.3...v8.1.4
[8.1.3]: https://github.com/axelor/axelor-open-suite/compare/v8.1.2...v8.1.3
[8.1.2]: https://github.com/axelor/axelor-open-suite/compare/v8.1.1...v8.1.2
[8.1.1]: https://github.com/axelor/axelor-open-suite/compare/v8.1.0...v8.1.1
[8.1.0]: https://github.com/axelor/axelor-open-suite/compare/v8.0.8...v8.1.0
