## [7.0.28] (2024-06-07)

### Fixes
#### Account

* Move line: set the vat system editable for tax account.
* Account: fixed technical type select for journal type demo data.
* Move line/Reconcile/Tax: fixed error when you forbid tax line on tax account.
* Move: fixed a technical error when adding a first line without invoice terms in sale or purchase move.
* Analytic distribution template: removed analytic percentage total verification when we add a new analytic distribution line.
* Invoice/AutoReconcile: removed tax moveline reconciliation in move excess or move due at ventilation.
* Payment session: fixed issue when validating the payment session causing amount being updated and not corresponding to the value of the amount being validated and equal to the bank order generated.
* Move line: select default account from partner configuration only for first line of the move instead of all lines.
* Move : date of analytic move lines will be updated on change of move's date.
* Accounting report: fixed opening moves are not displayed on aged balance report.

#### Bank Payment

* Invoice payment: fixed payment remaining 'Pending' while bank order has been realized (while no accounting entry generated).

#### CRM

* Partner: fixed display condition for customer recovery button.

#### Human Resource

* Extra hours: fixed an issue where lines were filled with the connected employee instead of the employee filled in the form view.
* Payroll preparation: correctly empty lists on payroll preparation view when employee is changed.

#### Marketing

* Campaign: fixed error preventing from sending reminder emails to leads when the list of partners is empty.


### Developer

#### Account

The constructor for MoveLineControlServiceImpl has been updated to include the additional parameter 'MoveLineGroupService moveLineGroupService'.

## [7.0.27] (2024-05-24)

### Fixes
#### Base

* Update axelor-studio dependency to 1.0.6.
* ICalendar: fixed synchronization duration widget.
* Sale order/Purchase order/Invoice: fixed wrong column name displayed on discounted amount.

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

#### Contract

* Contract: 'nouvelle version' used as key instead of 'new version'.
* Contract: Deleted version history while duplicating.

#### Human Resource

* Leave request: fixed issue where a leave request was not updated after sending it.

#### Maintenance

* Maintenance request: fixed impossible to create a maintenance request from the quick adding field.

#### Purchase

* Purchase request: Added sequence for purchase request in demo data.

#### Sale

* Sale order merge: fixed an issue where it was not possible to select a price list in case of conflicts.

#### Supply Chain

* Customer invoice line: fixed default product unit on product change.
* Purchase order: when a purchase order is generated from a sale order, when the catalog does not have a code or name, it will use the product.

## [7.0.26] (2024-05-03)

### Fixes
#### Base

* Sequence: fixed NPE while checking yearly reset or monthly reset on unsaved sequence.
* Advanced export: Fixed export doesn't work when maximum export limit exceeds.

#### Account

* Invoice: fixed default financial discount from partner not being computed after invoice copy.
* Accounting export: Fixed error when replaying the accounting export.

#### Bank Payment

* Bank reconciliation: fixed tax move line generation when origin is payment.

#### Contract

* Contract: fixed prorata invoicing when invoicing a full period.
* Contract: correctly disable related configuration when disabling invoice management.
* Contract: Fixed error when using contract template with contract line to generate a contract.

#### Production

* Manufacturing order: fixed conversion of missing quantity of consumed products.
* Machine: fixed machine planning type when creating it from machine.

#### Purchase

* Purchase order report: fixed sequence order issue when title lines are present.

#### Stock

* Tracking number: fixed available qty not displayed on grid view.
* Stock move: fixed printing settings when creating stock move from sale order.
* Stock configuration: fixed typo in french translation, 'incoterme' => 'incoterm'


### Developer

#### Production

Changed signature of `ProdProductProductionRepository.computeMissingQty(Long productId, BigDecimal qty, Long toProduceManufOrderId)`
to `ProdProductProductionRepository.computeMissingQty(Long productId, BigDecimal qty, Long toProduceManufOrderId, Unit targetUnit)`

## [7.0.25] (2024-04-19)

### Fixes
#### Account

* Payment voucher: fixed required message at on new and fixed invoice refresh at confirm.
* Invoice: fixed report when invoice is linked to more than one stock move.
* Accounting report: fixed 'Fees declaration supporting file' report displaying records that should not appear.
* Financial Discount: fixed french translations for 'discount'.

#### Sale

* Sale order: removed french translation from english file.

#### Supply Chain

* Mass stock move invoicing: fixed an issue where invoiced partners were not used to invoice stock moves, the other partner was used instead.
* Mass stock move invoicing: fixed an issue preventing to invoice customer returns.

## [7.0.24] (2024-04-04)

### Fixes
#### Account

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


### Developer

#### Sale

Removed action `action-sale-order-line-record-progress`

## [7.0.23] (2024-03-21)

### Fixes
#### Base

* Customer: fixed error when loading customer map.
* Fixed wrong french translation of 'Application' (was 'Domaine d'applicabilité').
* Language: fixed an issue where getting default language did not use the configuration 'application.locale'.
* App Base: fixed wrong currency conversion line in demo data.

#### Account

* Accounting batch: fixed result move functional origin in closure/open batch.
* Accounting batch: fixed the block customer message when no result.
* Reconcile: fixed passed for payment check at reconcile confirmation.
* Reconcile manager: fixed move lines selection.
* Accounting batch: fixed currency amounts on result moves in opening/closure.
* FEC Export: fixed technical error when journal is missing.

#### Contract

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

* Stock move: set 'Filter on available products' to true in new stock moves.
* Inventory: fixed type in inventory demo data.

#### Supplier Management

* Supplier request: fixed 'JNPE' error on partner selection in Supplier request form.

## [7.0.22] (2024-03-07)

### Fixes

* The format of this file has been updated: the fixes are now separated by modules to improve readability, and a new `developer` section was added with technical information that could be useful for developers working on modules on top of Axelor Open Suite.

#### Account

* Account clearance: fixed issue when opening a generated move line from account clearance.
* Move: added back missing french translation for 'Simplified Moves'.
* Invoice payment: added missing french translation for error message.
* Period: fixed an issue when checking user permission where the roles in user's group were not checked.
* Move: fixed missing label in accounting move printing.

#### CRM

* CRM App: fixed small typos in english titles.

#### Production

* Sale order: fixed a NPE that occured at the manuf order generation.
* Manufacturing order: fixed a bug a producible quantity was not correctly computed when a component was not available.

#### Sale

* Sale order: improve performance on sale order save.

#### Supply Chain

* Stock move: fixed a bug that prevented to totally invoice a stock move when partial invoicing for out stock move was activated.
* Supplychain configuration: fixed default value for "Generation of out stock move for products".


### Developer

#### Account

- Removal of `action-method-account-clearance-show-move-lines` and creation of `action-account-clearance-view-move-lines` for its replacement
- `showAccountClearanceMoveLines` has been removed from `AccountClearanceController`

## [7.0.21] (2024-02-22)

#### Fixed

* Stock location: fixed wrong QR Code on copied stock location.
* Move: fixed currency rate errors in move line view.
* Invoice: fixed an issue when returning to the refund list after creating a refund from an invoice.
* Bank order: fixed multi currency management.
* Cost calculation: fixed JNPE error on select of product.
* Product: replaced stock history panel which is showing empty records by a panel to stock location line history.
* Employee: fixed error happening while deleting employee.
* Invoice: fixed an error on invoice ventilation when the invoice had an advance payment in previous period.
* Sale order: removed the possibility to mass update fields on sale order, as it caused inconsistencies.
* Fixed asset: fixed purchase account move domain in fixed asset form view.
* Invoice: fixed display of delivery address on advance payment invoices generated from a sale order.
* Computing amounts in employee bonus management now alert user if employee does not have a birth date or seniority date.
* Project: fixed opening gantt view per user.
* Accounting report: set readonly export button and add report type check to provide critical error.
* Operation order: finishing a manuf order from the operations correctly computes the cost sheet quantities.
* Contract: reset sequence when duplicating contracts.
* Reconciliation: fixed invoice term imputation when PFP not validated.
* Manufacturing order: finishing a manufacturing order now correctly updates the cost price of a product.
* Inventory line: forbid negative values in inventories.
* Accounting export: fixed FEC export not taking journal into account.

## [7.0.20] (2024-02-01)

#### Fixed

* Stock: added quality control default stock location to stock config demo data to avoid exception case.
* Accounting batch: fixed anomaly generated when running the closing/opening accounts batch with option simulate generated moves option ticked.
* Project task: fixed the ticket task form views.
* Expense line: fixed an UI issue where some fields were not required in the form view of kilometric expense line.
* Move: fixed critical error when we create a move without company currency.
* Accounting batch: added financial account with a technical type 'asset' when we open/close the year accounts.
* Quantity: fixed quantity fields in Invoice line/Purchase order line/Sale order line/Product company/Supplier catalog grid view so they display the configured number of decimals instead of 2.
* Expense line: fixed a bug where 'Type' was not properly required in kilometric expense line.
* Sale order: adjusted price and quantity precision according to the user-configured precision settings.
* Purchase order: fixed an issue where duplicated order did not get their order date updated.
* Contract: added control on invoice period start date and invoice period end date.
* Move line: hid VAT System when journal is not purchases or sales and set it not editable when account is not charge or income.
* Bank reconciliation: hid reverse move with the 'Hide move lines in bank reconciliation'.
* Debt recovery: fixed debt recovery batch error when no invoice term was found.
* Leave request: user in not required in an employee anymore to increase leave from batch.
* Product Details: fixed 'id to load is required for loading' error when stockLocationLine has no unit.
* Sale order line: fixed an issue where some products were not selectable in a line.
* Production process line: fixed 'NullPointerException' popup error while opening a line.
* Expense line: hid analytic panel when the expense is not validated.
* Move: fixed due dates not computed when we change the move date.
* Invoicing project: invoice lines generated have now a correct unit price.
* Bank reconciliation: fixed dates management in moveline query.
* Move: fixed canceled payments preventing moves from being updated.
* Contract: emptied the contract line when emptying the product.
* Reconcile: prevent reconciliation if an involved move has a line with a tax account but no tax.
* Control point: fixed creation from the menu entry.
* Birt template parameter: fixed a small UI issue by ordering of the list of parameters.
* Move: batch control move consistency exception management.
* Expense line: compute total amount and distance depending on kilometric type.

## [7.0.19] (2024-01-12)

#### Fixed

* Hotfix: add missing binding preventing server startup.

## [7.0.18] (2024-01-11)

#### Fixed

* Product: make the product unit field required for stock managed items.
* Reconcile: fixed invoice term amount remaining with multi currency.
* Stock move: fixed unable to select external stock location on receipt.
* Invoice: Duplicating an invoice now correctly empties the origin date and the supplier invoice number.
* Invoice: fixed error when we change partner in supplier invoice with invoice lines.
* Account clearance: fixed the way tax and vat system are managed.
* Indicators: fixed results display.
* Sale order: keep attached files and internal notes when merging sale orders.

## [7.0.17] (2023-12-21)

#### Fixed

* Stock location: Issue on Edition of Location financial data report
* Wrong quote sequence number on finalize
* Invoice : Fix partner account related errors consistency when validating and ventilating
* [Ticket]: Fix Ticket timer buttons
* INVOICE : currency not updated on first partner onChange
* Move Line : Prevent from updating tax line when the move is accounted
* Custom accounting report : Disable group by account feature as it cannot be fixed until 8.1
* GDPR: Fix response email couldn't be changed.
* Custom accounting report : Fix an issue with custom rule priority
* MAINTENANCE ORDER : fix NPE when we try to plan
* Move lettering : Fill the date of lettering when the status becomes Temporary
* Reconcile : Fix amount error triggering on unwanted cases
* EXPENSE LINE: fix totalTax is not in readonly in form view when expense product blocks the taxes
* Sale / Purchase / Invoice : Fix cases where price decimal config wasn't being used
* Fixed asset : fix depreciation date when failover is complete
* Move line : Fix move function original select being wrongly displayed
* EVENT : some highlights conditions in grids use __datetime__
* Invoice payment : Fix an issue where it was possible to set a higher amount that maximum one allowed
* Move : Fix counterpart generation with multi currency
* Printing template / Printing setting: fix translation issue in position fields.
* Move line: Added reference of move line in sum's control error message
* Sale Order/Purchase Order: Error when emptying contact
* Advance Payment Invoice : Fix error when trying to pay
* Bank order: Fixed issue in code generating move
* Business Support: TaskDeadLine field not hidden when the app is installed anymore.
* Invoice term : Fix wrong amount computation at invoice term generation with hold back
* UNITCOSTCALCULATION : Irrelevant message when no product has been found
* Account clearance : Fix an issue where move lines could be created or edited there
* Reconcile : Fix an issue where a payment was created for a reconcile with an account not being used for partner balance
* Analytic Move Line : Fix type select when we create a new analytic move line from orders, invoices, contract and moves

## [7.0.16] (2023-12-07)

#### Fixed

* Sale order: fixed JNPE error when copying a sale order without lines.
* Purchase request: fixed reference to purchase order being copied on duplication.
* Accounting batch: hide unnecessary payment mode information.
* Sale order: fixed wrong price update when generating quotation from template.
* Move line: removed critical error when we open move line form.
* Team task: fixed view reload error.
* Indicator generator: fixed indicators generators demo data.
* Invoice: fixed reference to subrogation release being copied on duplication.
* Message: fixed encoding errors happening with accented characters when sending an email.
* Fixed asset: accounting report now correctly takes into account fiscal already depreciated amount.
* Configurator: fixed EN demo data for configurator.
* Bank Details: fixed balance display for bank details on card view and form view.
* Invoice: fixed error popup before opening a payment voucher from an invoice.
* Invoice: fixed invoice term generation when skip ventilation is enabled in invoicing configuration.
* Contract: fixed UI issue by hiding some part of the view while the partner is empty.
* Account management: use functional origin instead of journal to determine tax account.
* Invoice: fixed error at new invoice term creation.
* Invoice: fixed reference to "Refusal to pay reason" being copied on invoice duplication.
* Timesheet: fixed timesheet line date check.
* Stock move: allow to create a tracking number directly on stock move line.
* Cost calculation: fixed an issue preventing an infinite loop in case of an error in bill of materials hierarchy.
* Account: forbid to select the account itself as parent and its child accounts.
* Mobile Settings: added a placeholder on every authorized roles.
* Bank order: highlight orders sent to bank but not realized.
* Payment session: fixed display of currency symbol in payment session.
* Move template line: hide and set required tax field when it is configured in financial account.
* Inventory line: fixed update of inventory line by taking into account stockLocation of line when possible, triggering update when necessary.
* Invoice: fixed partially paid invoices in bill of exchange management.
* Stock move: allow to select external stock location for deliveries.

## [7.0.15] (2023-11-23)

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


#### Fixed

* MRP Line: fixed an issue preventing to open a partner from a MRP line.
* Invoice: fixed description on move during invoice validation.
* Move template: fixed due date not being set on generated move.
* Stock location line: fixed quantity displayed by indicators in form view.
* Invoice: fixed currency being emptied on operation type change.
* Move: fixed autofill fields at move line creation according to partner accounting config.
* Bank statement: added missing french translation when we try to delete a bank statement.
* Invoice: fixed an issue where enabling pfp management and opening a previously created supplier invoice would make it look unsaved.
* Fixed asset: fixed inconsistency in accounting report.
* Bank reconciliation: selectable move line are now based on the currency amount.
* Move Template: hide move template line grid when journal or company is not filled.
* Project task: fixed broken time panel in project task.
* Expense: fixed an issue were employee was not modified in the lines when modifying the employee on the expense.
* City: fixed errors when importing city with geonames.
* Period: fixed status not reset on closure.
* Analytic move line: fixed required on analytic axis when the sum of analytic move line percentage is equal to 100.
* Invoice / Reconcile: fixed issue preventing from paying an invoice with a canceled payment.
* Unit cost calculation: fixed error when trying to select a product.
* InvoiceTerm: fixed rounding issue when we unreconcile an invoice payment.
* Forecast: fixed a bug that occured when changing a forecast date from the MRP.
* Fixed asset category: fixed wrong filter on ifrs depreciation and charge accounts not allowing to select special type accounts.
* Appraisal: fixed appraisal generation from template.
* Move: fixed error when we set origin on move without move lines.
* Advance payment: fixed invoice term management and advance payment imputation values in multi currency.
* Bill of exchange: fixed managing invoice terms in placement and payment.
* SOP: fixed quantity field not following the "number of decimal for quantity fields" configuration.
* Contract template: fixed wrong payment mode in contract template from demo data.
* Period/Move: fixed new move removal at period closure.
* SOP: added missing french translation.
* Period: fixed errors not being displayed correctly during the closure process.
* Move: fixed error when we set description on move without move lines.
* Move template: fixed filters on accounting accounts in move template lines.
* SOP line: fixed an issue where SOP process does not fetch the product price from the product per company configuration.
* Contract: fixed prorata computation when invoicing a contract and end date contract version

When creating a new version of a contract, the end date of the previous version
is now the activation date of the new version minus one day.
If left empty, it will be today's date minus one day.

* Bank order: do not copy "has been sent to bank" on duplication.
* Product details: fixed a bug occuring on stock details.

## [7.0.14] (2023-11-09)

#### Fixed

* App builder: update studio dependency to 1.0.5 to get the following fix:

    - fixed NPE upon save of a custom model with a menu

* App Base: set company-specific product fields domain.
* Accounting report: fixed an issue in Journal report (11) where debit and credit were not displayed in the recap by account table.
* Accounting dashboard: removed blank panel in "accounting details with invoice terms" dashboard.
* Move line: fixed error when we create a move line in multi currency.
* Move line: fixed error at first move line creation related to counter initialization.
* Move: fixed an error when we update invoice terms in move line.
* Move: fixed an error when we reconcile move lines without tax line.
* Move: added journal verification when we check duplicate origins.
* Fixed asset: fixed an issue after selecting a category where set depreciations for economic and ifrs were not computed correctly.
* Fixed asset: hide "isEqualToFiscalDepreciation" field when fiscal plan not selected.
* CRM: opening an event from custom view in prospect, leads or opportunity is now editable.
* Custom accounting report: fixed legacy report option being displayed for all account report types.
* Cost sheet: fixed issue in the order of calculation on bill of materials.
* Configurator BOM: Fixed a concurrent error when generating a bill of materials from the same configurator.
* Employee: to fix bank details error, moved the field to main employment contract panel.
* FEC Import: prevent potential errors when using demo data config file.
* Contract: fixed "NullPointerException" error when emptying the product on a contract line.
* Manufacturing order: company is now required in the form view.
* Sale order (Quotation): fixed "End of validity date" computation on copy.
* Debt recovery: fixed balance due in debt recovery accounting batch.
* Analytic: fixed display of analytic panel when it is not managed by account in sale order and purchase order.
* Lead: prevent the user from editing the postal code when a city is filled to avoid inconsistencies.
* CRM Event: when an event is created from CRM, correctly prefill "related to".
* Move: added journal verification when we check duplicate origins.
* Opportunity: company is now required in the form view.
* Sale: hide 'Timetable templates' entry menu on config.
* Maintenance: reset the status when duplicating a maintenance request.

## [7.0.13] (2023-10-27)

#### Fixed

* App builder: update studio dependency to 1.0.4 to get the following fix:

    - fixed StudioActionView duplication on every save

* Debt Recovery: fixed error message on debt recovery generation to display correctly trading name.
* Invoice/Move: fixed due date when we set invoice date, move date or when we update payment condition.
* FEC Import: fixed issue when importing FEC with empty date of lettering.
* FEC Import: the header partner is now filled correctly

    - Multi-partner: no partner in header
    - Mono-partner: fill partner in header

* Analytic: fixed analytic distribution verification at validation/confirmation of purchase order/sale order when analytic is not managed.
* Payment Voucher: fixed display of load/reset imputation lines and payment voucher confirm button when paid amount is 0 or when we do not select any line.
* Period closure: fixed a bug where status of period was reset on error on closure.
* Manufacturing order: fixed an issue where outsourcing was not activated in operations while it was active on production process lines.
* Payment voucher: fixed error at payment voucher confirmation without move lines.
* Move: removed period verification when we opening a move.
* Payment voucher: removed paid line control at payment voucher confirmation.
* Fixed asset: fixed popup error "Cannot get property 'fixedAssetType' on null object" displayed when clearing fixed asset category field.
* Cost sheet: replaced 'NullPointerException' error by a correct message when an applicable bill of materials is not found on cost calculation.
* axelor-config.properties: enabled Modern theme by default.

## [7.0.12] (2023-10-18)

#### Fixed

* Update App Builder dependency to 1.0.3

This version provides a fix for an issue causing the application to create a new file for each app on startup.

If you have this issue and want to clear existing files in your server, please follow these steps:

* Shutdown tomcat
* Execute the SQL script below
* Clear all application images from the server disk
* Restart tomcat

```sql
UPDATE studio_app
SET image = null
WHERE image in
      (SELECT id
       FROM meta_file
       where file_path LIKE 'app-%'
         AND file_type = 'image/png');

DELETE
FROM meta_file
WHERE file_path LIKE 'app-%'
  AND file_type = 'image/png';
```

* Product: fixed stock indicators computation.
* Project: fixed french translation for 'Create business project from this template'.
* Payment session: set currency field readonly on payment session.
* Period: improved period closure process from year form.
* Sale order: fixed duplicated 'Advance Payment' panel.
* Manufacturing order: fixed "Circular dependency" error preventing to start operation orders.
* Purchase request: fixed 'Group by product' option not working when generating a purchase order.
* Sequence: fixed sequence duplication causing a NPE error.
* Move: fixed an error when we create a move line with a partner without any accounting situation.
* Move: fixed error displayed when trying to open an invoice term.
* SOP/MPS: fixed a bug where an existing forecast was modified instead of creating a new one when the forecast was on a different date.

## [7.0.11] (2023-10-06)

#### Fixed

* Period: set period status at open if closure fails.
* Move template: fixed description not being filled on moves generated from a percentage based template.
* Interco invoice: fixed default bank details on invoice generated by interco.
* Invoice term: on removal, improved error message if invoice term is still linked to a object.
* Sale order line: fixed error when emptying product if the 'check stock' feature is enabled for sale orders.
* Sale order line: fixed an issue where updating quantity on a line did not updated the quantity of complementary products.
* Move: fixed due dates not being recomputed on date change.
* Stock move: filled correct default stock locations on creating a new stock move.
* Stock move: fixed wrong french translation on stock move creation and cancelation.
* User: encrypt password when importing user data-init.
* Print template: iText dependency has been replaced by Boxable and PDFBox.
* Cost calculation: fixed the import of cost price calculation.
* HR: fixed an issue preventing people from creating a required new training.
* Task: improved performance of the batch used to update project tasks.
* Mail template: fixed error when importing demo data.
* Sale order: fixed an error occurring when generating analytic move line of complementary product.
* Invoicing dashboard: fixed error on turnover per month charts.
* Move: fixed reverse charge tax computation.
* Debt recovery: fixed missing letter template error message.
* GDPR request: changed UI for selecting search results.
* Project: fixed tracebacks not showing on project totals batch anomalies.
* Reconcile: fixed multiple move lines reconciliation.
* Move: fixed move lines consolidation.
* Irrecoverable: fixed tax computing.
* Move: fixed multi invoice term due dates being wrongly updated when accounting.
* Debt recovery batch: reduced the execution time.
* Bank reconciliation: fixed balance not considering move status.

## [7.0.10] (2023-09-21)

#### Fixed

* Follower: fixed an error occuring when sending a message while adding a follower on any form.
* Invoice payment: when validating a invoice payment from a bank order, the payment date will now be correctly updated to bank order date.
* Move: fixed period permission error when changing the date.
* Configurator creator: complete demo data to include admin-fr in authorized users.
* Manufacturing order: fixed purchase order generation with unit name
* Product: "Economic manufacturing quantity" is now correctly hidden on components.
* Reconcile: fixed effective date computation when we generate payment move from payment voucher.
* Leave line: deleting every leave management now correctly computes remaining and acquired value.
* Invoice: fixed french translation for 'Advance payment invoice'.
* Manufacturing order: fixed 'No calculation' of production indicators on planned Manufacturing Order.
* Advanced export: change PDF generation.
* Accounting: disable financial discount from the application to prevent issues from its instability.
* Operation Order: On planning, fixed a bug where an operation order could only start at the same time as others with same priority.
* Production API: finishing or pausing every operation orders will change the parent manuf order status.
* Debt recovery: fixed missing email reminder template messages for debt recovery method lines demo data.
* Stock location: fixed new average price computation in the case of a unit conversion.
* Manufacturing order: when pausing an operation order, the manufacturing order will be paused if there is no "In progress" operation order
* Company: company data is now prefilled when creating a bank details from company form view.
* Invoice: fixed error when modfying an analytic line percentage.
* Move template: fixed invoice terms not being created on a move template generation by amount.
* Stock correction: add demo data for Stock correction reason.

## [7.0.9] (2023-09-11)

#### Changes

* API: improve response message on object creation.

#### Fixed

* API Stock: fixed an issue when creating a stock move line where the quantity was not flagged as modified manually.
* Fixed asset: fixed never ending depreciation lines generation if there is a gap of more than one year.
* Product: on product creation, fills the currency with the currency from the company of the logged user.
* Invoice: fixed wrong translations on ventilate error.
* Manufacturing API: improved behaviour on operation order status change.
* Account: fixed an issue preventing the user from deleting accounts that have compatible accounts.
* Operation order: fixed workflow issues

Resuming an operation order in a grid or a form has now the same process and will not affect other orders.
When pausing an operation order, if every other order is also in stand by, the manuf order will also be paused.

* Invoice line: set analytic accounting panel to readonly.
* Sale/Purchase order and stock move: fixed wrong filters when selecting stock locations, the filters did not correctly followed the stock location configuration.
* Stock move: fixed 'NullPointerException' error when emptying product on stock move line.
* Payment session: fixed generated payment move lines on a partner balance account not having an invoice term.
* Manufacturing order: fixed an issue where some planning processes were not executed.
* Move template: fixed copied move template being valid.
* Invoice term: fixed wrong amount in invoice term generation.
* Journal: balance tag is now correctly computed.
* Manufacturing order: when generating a multi level manufacturing order, correctly fills the partner.

## [7.0.8] (2023-08-24)

#### Fixed

* Webapp: update Axelor Open Platform version to 6.1.5.
* Move: fixed reverse process to fill bank reconciled amount only if 'Hide move lines in bank reconciliation' is ticked and if the account type is 'cash'

To fix existing records, the following script must be executed:

```sql
UPDATE account_move_line moveLine
SET bank_reconciled_amount = 0
FROM account_account account
JOIN account_account_type accountType
ON account.account_type = accountType.id
WHERE moveLine.account = account.id
AND accountType.technical_type_select <> 'cash';
```

* SOP/PMS: Gap is now really a percentage

Changed computation of "Gap" fields so the result will be percentage (e.g. 13% instead of 0.13). The following script can be used to fix existing SOP/PMS:

```sql
UPDATE production_sop_line SET sop_manuf_gap = sop_manuf_gap*100;
UPDATE production_sop_line SET sop_sales_gap = sop_sales_gap*100;
UPDATE production_sop_line SET sop_stock_gap = sop_stock_gap*100; 
```

* Payment voucher: fixed remaining amount not being recomputed on reset.
* Payment voucher: fixed being able to pay PFP refused invoice terms.
* Contract: fixed NullPointerException when invoicing a contract.
* Stock details: in stock details by product, fixed production indicators visibility.
* Business project batch: fixed "id to load is required for loading" error when generating invoicing projects.
* Period: fixed adjusting button not being displayed when adjusting the year.
* Fixed asset: fixed wrong depreciation value for degressive method.
* Payment session: filter invoice terms from accounted or daybook moves.
* Employee: fixed employees having their status set to 'active' while their hire and leave date were empty.
* Bank reconciliation: merge same bank statement lines to fix wrong ongoing balance due to auto reconcile.
* Accounting report: dates on invoice reports are now required to prevent an error generating an empty report.
* Invoice: fixed anomaly causing payment not being generated

Correct anomaly causing payment not being generated on invoice when a new reconciliation is validated  
and the invoice's move has been reconciled with a shift to another account (LCR excluding Doubtful process).

* Move: The date is now correctly required in the form view.
* Payment voucher / Invoice payment: fixed generated payment move payment condition.
* Payment voucher: fixed excess payment.
* Invoice payment: add missing translation for field "total amount with financial discount".
* Invoice: fixed financial discount deadline date computation.
* Sale order line: fixed a bug where project not emptied on copy.
* Stock move printing: fixed an issue where lines with different prices were wrongly grouped.
* Stock details: fixed "see stock details" button in product and sale order line form views.
* Accounting batch: corrected cut off accounting batch preview record field title cut in half.
* Invoice line: fixed error when emptying the tax line.
* Invoice: fixed invoice term due date not being set before saving.
* Reconcile: fixed inconsistencies when copying reconciles.
* Tax number: translated and added an helper for 'Include in DEB' configuration.
* Leave request: fixed employee filter

A HR manager can now create a leave request for every employee.
Else, if an employee is not a manager, he can only create leave request for himself or for employees he is responsible of.

* Stock API: validating a Stock Correction with a real quantity equal to the database quantity now correctly throws an error.
* Manufacturing order: fixed a bug where sale order set was emptied on change of client partner and any change on saleOrderSet filled the partner.
* Forecast line: fixed company bank details filter.
* Contact: the filter on mainPartner now allows to select a company partner only, not an individual.
* Move: fixed due date issue when changing from single to multi invoice term payment condition.
* Stock move line: improve performance when selecting a tracking number if there are a large number of stock location lines.

## [7.0.7] (2023-08-08)

#### Fixed

* Invoice : fix the way we check the awaiting payment
* Accounting batch : Improve user feedback on move consistency control when there are no anomalies
* PLANNING: Planning is now correctly filtered on employee and machine form
* SaleOrderLine: Description is now copied only if the configuration allows it
* PARTNER: Fixed a bug where button 'create sale quotation' was always displayed
* Custom accounting report : Excel sheets are now named after the analytic account
* Move : Fix automatic move line tax generation with reverse charge and multiple vat systems.
* PurchaseOrder and Invoice: Added widget boolean switch for interco field 
* Invoice : Fix tax being empty on invoice line when it's required on account
* ManufOrder: Planning a cancelled MO now clears the real dates on operations orders and MO
* Move : Fix display of analytic axis accounts when we change it on analytic move lines
* Product/ProductFamily : set analytic distribution template readonly if the config analytic type is not by product
* Move : Fix currency amount of automatically generated reverse charge move line not being computed
* Invoice: Fixed french translations
* INVOICE : fix form view - added blank spaces before the company field and move the originDate field
* SOP/MPS: Fixed a bug where real datas were never increased
* Supplychain batch : Fixed bugs that prevented display of processed stock moves
* SOP/MPS: Fixed SOP/MPS Wrong message on forecast generation
* PAYMENT SESSION : corrected accounting trigger from payment mode overriding accounting trigger from payment session on bank order generated from payment session.
* Move : Fix tax computation when we have two financial accounts with the same VAT system
* Debt Recovery: Fix error message on debt recovery batch to display correctly trading name
* Move : Fixed a bug that was opening a move in edit mode instead of read only
* Period : Fixed an issue where a false warning was displayed preventing the user for re-opening a closed period
* Invoice: Fixed a bug where subscription invoice was linked to unrelated advance payment invoice

When creating an invoice auto complete advance payement invoice with no internal reference to an already existing sale order.

* Move/MoveLine : empty taxLine when changing the account of a moveLine to an account without tax authorized
* Invoice : Remove payment voucher access on an advance payment invoice
* Payment session : Fix session total amount computation
* Move : Fix invoice term amount at percentage change with unsaved move
* Product: When changing costTypeSelect to 'last purchase price', the cost price will now be correctly converted.
* Move : Set readonly move form when period is closed or doesn't exist
* Bank order: Fixed a bug where bank order date was always overridden. Now bank order date is overridden only when it is before the current date and the user is warned.
* BUSINESS PROJECT BATCH: Fixed invoicing project batch

## [7.0.6] (2023-07-20)

#### Fixed

* Account/Move/Invoice: fixed analytic check when required on move line, invoice line.
* Manufacturing order: fixed JNPE error when merging manufacturing orders missing units.
* Cost sheet: fixed wrong bill of materials used for cost calculation.
* Move: set description required on move line when it is enabled in company account configuration.
* Stock move line: fixed display issues with the button used to generate tracking numbers in stock move lines.
* Custom accounting report: added a tab listing anomalies that are preventing generation.
* Operation order: correctly filter work center field on work center group (when the feature is activated).
* Payment condition: improved warning message when modifying an existing payment condition.
* Stock move: fixed issue preventing the display of invoicing button.
* Supplychain batch: fixed an error occurring when invoicing outgoing stock moves.
* Invoice: fixed unwanted financial discount on advance payment invoice.
* Bank payment printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Accounting report: fixed error on N4DS export when the partner address has no city filled.
* Fixed asset: improved the error message shown when an exception occurs during a mass validation.
* Analytic distribution template: fixed error when creating a new analytic distribution template.
* Product: fixed wrong filter on analytic on product accounting panel.
* Faker API: update documentation in help message.
* Sale order: improved performance when loading card views.
* Interco: fixed generated sale order/purchase order missing a fiscal position.

## [7.0.5] (2023-07-11)

#### Fixed

* Business project: Automatic project can be enabled only if project generation/selection for order is enabled

The helper for the project generation/selection for order has been changed. To update it, a script must be executed:

```sql
DELETE FROM meta_help WHERE field_name = 'generateProjectOrder';
```

* App configuration: remove YOURS API from Routing Services

If using the distance computation with web services in expenses, users should select the OSRM API in App base config.

* Custom accounting report: fixed percentage value when total line is not computed during the first iteration.
* Custom accounting report: fixed various issues related to report parameters.
* Custom accounting report: fixed number format.
* Custom accounting report: order accounts and analytic accounts alphabetically when detailed.
* Custom accounting report: fixed analytic values not taking result computation method into account.
* Accounting report config line: prevent from creating a line with an invalid groovy code.
* Move: fixed automatic fill of VAT system when financial account is empty.
* Move: fixed duplicate origin verification when move is not saved.
* Invoice: fixed error when cancelling an invoice payment.
* Move: on change of company, currency is updated to company currency even when the partner is filled.
* Product/Account Management: hide financial account when it is inactive on product account management.
* Reconcile group: added back "calculate" and "accounting reconcile" buttons on move line grid view.
* Forecast generator: fixed endless loading when no periodicity selected.
* Stock move line: fixed a issue when emptying product from stock move line would create a new stock move line.
* Prod process line: fixed an issue where capacity settings panel was hidden with work center group feature activated.
* Inventory: fixed wrong gap value on inventory.
* Accounting report: fixed impossibility to select a payment move line in DAS2 grid even if code N4DS is not empty.
* Fixed asset: fixed move line amount 0 error on sale move generation.
* Stock rules demo data: fixed wrong repicient in message template.
* Accounting batch: fixed error when we try to run credit transfer batch without bank details.
* Sale order template: fixed error when selecting a project.
* Move: allow tax line generation when move is daybook status.
* Credit transfer batch: fixed duplicate payments & bank orders.
* Purchase Order/Sale Order/Contract : Remove wrong analytic line link after invoice generation
* Payment session: change titles related to emails on form view.
* Payment voucher: fixed invoice terms display when trading name is not managed or filled.
* Contract: fixed an error occurring when invoicing a contract

An error occurred when invoicing a contract if time prorated Invoice was enabled and then periodic invoicing was disabled.

* Fixed asset: fixed being able to dispose a fixed asset while generating a sale move but with no tax line set.
* Move: removed verification on tax in move with cut off functional origin.
* Invoice term: Fixed company amount remaining on pfp partial validation to pay the right amount.
* Details stock location line: removed reserved quantity from the form view.
* Sale order: fixed totals in sales order printouts.
* Email address: Remove the unused field 'lead' from email address.
* Invoice: fixed view marked as dirty after invoice validation.
* Account printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Accounting report: fixed bank reconciliation statement.
* Accounting report config line: filter analytic accounts with report type company.
* Invoice/Move: filled analytic axis on move when we ventilate an invoice.

## [7.0.4] (2023-06-22)

#### Features

* INVOICE : mandatory reference to the original invoice on the printing

The credit invoice include all the compulsory information on the original invoice. It also contain the credit note referring to the invoice it cancels or modifies. For example -  "In reimbursement of Invoice n°XXXX, issued on DD/MM/YYYY".


#### Fixed

* Analytic rules: fixed issue when retrieving analytic rules from the company to check which analytics accounts are authorized.
* Sale order line: fixed the view, move the hidden fields to a separate panel which avoids unnecessary blank space and the product field appears in its proper position.
* Accounting batch: removed period check on consistency accounting batch.
* Stock move: date of realisation of the stock move will be emptied when planning a stock move.
* Move template: fixed invoice terms not being created when generating a move from a template.
* Move: added missing translation when a move is deleted.
* Bank reconciliation line: prevent new line creation outside of a bank reconciliation.
* Job position: fixed english title "Responsible" instead of "Hiring manager".
* Account: fixed misleading error message when company has no partner.
* Stock quality control: when default stock location for quality control is not set and needed, a proper error message is now displayed.
* Sequence: when configuring a sequence, end date of a sequence now cannot be prior to the starting date and vice versa.
* Invoice: fixed an issue where invoice terms information were displayed on the invoice printing even when the invoice term feature was disabled.
* Stock location history batch: deactivate re-computation stock location history batch.
* Move line: prevent from updating partner when move has a partner already set.
* Product: "Control on receipt" and "custom codes" are now correctly managed per company (if the configuration is activated).
* Invoice: do not set financial discount on refunds.
* Move: fixed functional origin error when emptying the journal on a move form view.
* Reconcile: fixed an issue where letter button was shown if the group was unlettered.
* Invoice: added missing translation on an error message that can be shown during invoice ventilation.
* Sale order: fixed discount information missing on reports.
* Invoice: fixed an issue happening when we try to save an invoice with an analytic move line on invoice line.
* Stock Move: fixed a bug where future quantity was not correctly updated.
* Partner: fixed an issue where blocking date was not displayed.
* Accounting report VAT invoicing/payment: fixed differences in display between reports.
* Move: fixed currency exchange rate wrongly set on counterpart generation.
* Accounting Batch: accounting cut-off batch now takes into account 'include not stock managed product' boolean for the preview.
* Sale order: fixed an issue when computing invoicing state where the invoiced was marked as not invoiced instead of partially invoiced.
* Trading name: fixed wrong french translation for trading name ('Nom commercial' -> 'Enseigne').

## [7.0.3] (2023-06-08)

#### Fixed

* Business project, HR printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Configurator: fixed issue where sale order line generated from the configurator did not have a bill of materials.
* Deposit slip: fixed errors when loading selected lines.
* Move: fixed error when selecting a journal with no authorized functional origin.
* Invoice: allow supplier references (supplier invoice number and origin date) to be filled on a ventilated invoice.
* Move: fixed move lines tax computation on fiscal position change.
* Invoice/Stock move: fixed an issue where invoice terms were not present on an invoice generated from a stock move.
* Invoice: fixed an issue where the button to print the annex was not displayed.
* Account config: hide 'Generate move for advance payment' field when 'Manage advance payment invoice' is enabled.
* Invoice: fixed JNPE on invoice term form when the form is openened from the invoice.
* Lead: fixed event tab display on lead form view.
* Move line: fixed duplicate invoice terms when move has no payment condition.
* Move: fill automatically vat system when we change account.
* Move template: fixed creation of unbalanced move with a move template of type percent.
* GDPR: fixed demo data error for anonymizer configuration.
* Leave request: fixed an issue on hilite color in leave request validate grid.
* Birt template parameter: fixed french translation issue where two distinct technical terms were both translated as 'Décimal'.
* Budget distribution: fixed an issue where the budget were not negated on refund.
* Move: fixed auto tax generation via fiscal position when no reverse charge tax is configured.
* Sale order line form: fixed an UI issue on form view where the product field was not displayed.
* Move line query: fixed balance computation.
* Supplier portal and customer portal: add missing permissions on demo data.
* Move line: fixed an issue where analytic distribution template were not filtered per company.
* Project: when creating a new resource booking from a project form, the booking is now correctly filled with information from the project.
* Partner: correctly select a default value for tax system on a generated accounting situation.
* Move line: prevent from changing analytic account when a template is set.
* Move/Holdback: fixed invoice term generation at counterpart generation with holdback payment condition.
* MRP: UI improvements on form view by hiding unnecessary fields.
* Stock: fixed an error occurring when updating stock location on a product with tracking number.
* Move: fixed reverse process.
* Move: fixed multiple errors when opening a move line.
* Move: fixed due date not filled when generating moves from an invoice.
* Move: fixed not being able to select a company when it is not automatically set.
* Cost calculation: fixed calculation issue when computing cost from a bill of materials.
* Tracking number: fixed an issue preventing to select a product on a manually created tracking number.
* Reconcile: fixed an issue were it was possible to unreconcile already unreconciled move lines.
* Fixed asset: fixed JNPE error on disposal if account config customer sales journal is empty.
* Move/move line: fixed filter when we select analytic distribution template in move line or payment mode/partner bank details/trading name in move.
* Move: fixed exception when selecting an account on a move line where cutoff dates are filled.
* Accouting report view: fixed an issue where the filter on payment mode was displayed on an analytic report type.

## [7.0.2] (2023-05-25)

#### Fixed

* Updated Axelor Open Plateform dependency to 6.1.3. See the [Changelog](https://github.com/axelor/axelor-open-platform/blob/v6.1.3/CHANGELOG.md#613-2023-05-15) for more details.
* Updated axelor-studio version to 1.0.2, this fixes following issues:
  - fixed an error causing DMN to crash.
  - fixed an issue preventing access to the application in the case where a parameter was missing in `axelor-config.properties`.

* Invoice payment: disable financial discount process when the invoice is paid by a refund.
* Accounting batch: fixed close annual accounts batch when no moves are selectable and simulate generate move if needed.
* Configurator: fixed an issue where removing an attribute did not update the configurator form.
* Tax: fixed tax demo data missing accounting configuration and having wrong values.
* Sale order: fixed an issue during sale order validation when checking price list date validity.
* Printing settings: on orders and invoices, removed the filter on printing settings.
* Accounting move: fixed an issue where we were not able to change currency on a move.
* Invoice payment: update cheque and deposit info on the invoice payment record when generated from Payment Voucher and Deposit slip.
* Purchase order: fixed an error occurring when generating an invoice from a purchase order with a title line.
* Accounting batch: fix duplicated moves in closure/opening batch.
* Bank reconciliation: fixed an issue in bank reconciliation printing where reconciled lines still appeared.
* GDPR search: fixed an issue where some filters in the search were not correctly taken into account.
* GDPR: add UI improvement and data-init to make the module configuration easier.
* Bill of materials: fixed creation of personalized bill of materials.
* Invoice: added an error message when generating moves with no description when a description is required.
* Project: fixed an issue when creating a task in a project marked as "to invoice" where the task was not marked as "to invoice" by default.
* Manufacturing order: fixed filter on sale order.
* Bank order: fixed payment status update when we cancel a bank order and there are still pending payments on the invoice.
* Move: fixed an error that occured when selecting a partner with an empty company.
* Summary of gross values and depreciation accounting report: fixed wrong values for depreciation columns.
* Manufacturing order: when planning a manufacturing order, fixed the error message when the field production process is empty.
* Accounting move line: fixed filter on partner.
* Timesheet: when generating lines, get all lines from project instead of only getting lines from task.
* Accounting report DAS 2: fixed export not working if N4DS code is missing.
* Accounting report DAS 2: fixed balance.
* Bank order: fixed an issue where moves generated from a bank order were not accounted/set to daybook.
* Project task: when creating a new project task, the status will now be correctly initialized.
* Product: fixed an issue where activating the configuration "auto update sale price" did not update the sale price.
* Stock move: prevent cancellation of an invoiced stock move.
* Payment condition: add controls on payment condition when moves are created.
* Stock move: modifying a real quantity or creating an internal stock move from the mobile application will correctly indicate that the real quantity has been modified by an user.
* Bank order: fixed an issue where the process never ended when cancelling a bank order.
* Sale order: fixed popup error "Id to load is required for loading" when opening a new sale order line.
* Journal: fixed error message when the "type select" was not filled in the journal type.
* Account config: fixed UI and UX for payment session configuration.
* Account/Analytic: fixed analytic account filter in analytic lines.
* Account/Analytic: fix analytic account domain in analytic lines
* Move line: fixed error when emptying account on move line.
* Invoice: fixed an error preventing from merging invoices.
* Expense: prevent deletion of ventilated expense.

## [7.0.1] (2023-05-11)

#### Fixed

* Update axelor-studio to version 1.0.1 with multiples fixes made to the app builder.
* Invoice: fixed bank details being required for wrong payment modes.
* Invoice: fixed an issue blocking advance payment invoice creation when the lines were missing an account.
* Job application: fixed an error occuring when creating a job application without setting a manager.
* Bank reconciliation: added missing translation for "Bank reconciliation lines" in french.
* Product: fixed an issue preventing product copy when using sequence by product category.
* Bank reconciliation/Bank statement rule: added a control in auto accounting process to check if bank detail bank account and bank statement rule cash account are the same.
* Tracking number search: fixed an error occurring when using the tracking number search.
* Stock move: fixed an issue when creating tracking number from an unsaved stock move. If we do not save the stock move, tracking number are now correctly deleted.
* Sale order: fixed an issue where sale order templates were displayed from the 'Historical' menu entry.
* Bank reconciliation: fixed issue preventing to select move lines to reconcile them. 
* Accounting payment vat report: fixed wrong french translations.
* MRP: fixed an JNPE error when deleting a purchase order generated by a MRP.
* Partner: added missing translation on partner size selection.
* Public holiday events planning: set the holidays calendar in a dynamic way to avoid it become outdated in the demo data.
* VAT amount received accounting report: fixed height limit and 40 page interval break limit.
* Invoice payment: fixed payment with different currencies.
* Accounting report das 2: fixed currency required in process.
* Payment Voucher: fixed excess on payment amount, generate an unreconciled move line with the difference.
* Bank reconciliation: fixed tax computation with auto accounting.
* Sale, Stock, CRM, Supplychain printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Accounting batch: added missing filter on year.
* Move line: fixed analytic account domain when no analytic rules are based on this account.
* Purchase order: stock location is not required anymore if there are no purchase order lines with stock managed product.
* Custom accounting reports: fixed accounting reports "Display Details" feature.
* Accounting situation: display VAT system select when the partner is internal.
* Invoice: fixed wrong alert message when invoiced quantity was superior to delivered or ordered qty.
* Project report: fixed error preventing the generation of the PDF report for projects.
* Project: Display "Ticket" instead of "Project Task" in Activities tab when the activity is from a ticket.
* Opportunity: added missing sequence on the Kanban and Card view.
* Payment session: select/unselect buttons are now hidden when status is not in progress.
* Analytic move line query: fixed filter on analytic account.
* Move: fixed default currency selection, now the currency from the partner is automatically selected, and if the partner is missing the currency from the company is used.
* Bank reconciliation: fixed initial and final balance when importing multiple statements.
* Accounting report: fixed translation of currency title.
* Bank order: fixed an error preventing the validation of a bank order.
* Inventory: fixed UI issue by preventing unit from being changed in inventory lines.
* Stock rules: now correctly apply stock rules when minimum quantity is zero.

#### Removed

* Because of a refactor, action-record-initialize-permission-validation-move is not used anymore and is now deleted.

* Delete action 'action-record-analytic-distribution-type-select' since its not used anymore.

You can run following SQL script to update your database:

```sql
DELETE FROM meta_action WHERE name='action-record-initialize-permission-validation-move';

DELETE FROM meta_action WHERE name='action-record-analytic-distribution-type-select';
```


## [7.0.0] (2023-04-28)


#### Upgrade to AOP 6.1

* See [AOP migration guides](https://docs.axelor.com/adk/6.1/migrations.html) for AOP migration details
* Upgraded most libraries dependencies.
* Group: new option to enable collaboration
* Studio, BPM, Message and Tools leave Axelor Open Suite to become AOP addons
    * axelor-tools became axelor-utils
* Studio and BPM upgraded
    * Merge Studio and BPM module to a single module and create two different apps
* Apps logic is integrated into the studio
    * apps definition using YAML
    * auto-installer moved from Base to Studio
    * Add new types for apps (Standard, Addons, Enterprise, Custom and Others)
* Web app: application.properties renamed to axelor-config.properties and completed.
    * See [AOP documentation](https://docs.axelor.com/adk/latest/migrations/migration-6.0.html#configurations-naming) for parameter changes related to AOP.
    *  See details for parameter changes related to AOS
        <details>
        `aos.api.enable` is renamed `utils.api.enable` and is now true by default.
        `aos.apps.install-apps` is renamed `studio.apps.install`
        `axelor.report.use.embedded.engine` is renamed `reports.aos.use-embedded-engine`
        `axelor.report.engine` is renamed `reports.aos.external-engine`
        `axelor.report.resource.path` is renamed `reports.aos.resource-path`
        </details>

#### Features

* Swagger
    * API: implement OpenAPI with Swagger UI.
        <details>
            Complete the properties `aos.swagger.enable` and `aos.swagger.resource-packages` in the axelor-config.properties to enable the API documentation menu in Technical maintenance.
        </details>
* Mobile settings
New module to configure the new [Axelor Open Mobile](https://github.com/axelor/axelor-mobile)
* TracebackService: automatically use tracebackservice on controller exceptions
Now, for every controller methods in AOS packages ending with `web`, any
exception will create a traceback.

#### Changes

* Supplychain module: remove bank-payment dependency
* AxelorMessageException: Moved from Message module to Base
* Add order to all menus
    * Add a gap of 100 between menus
    * Negative value for root menus and positive for others
* Stock: reworked all menus in stock module menus
* Account: rework accounting move form view to optimize responsiveness.
* CRM: App configurations are not required anymore.
closedWinOpportunityStatus, closedLostOpportunityStatus, salesPropositionStatus can now be left empty in the configuration. If the related features are used, a message will be shown to inform the user that the configuration must be made.
* Change several dates to dateTime
    * axelor-base: Period
    * axelor-purchase: PurchaseOrder
    * axelor-account: Invoice, InvoiceTerm, Reconcile, ReconcileGroup, ClosureAssistantLine
    * axelor-bank-payment: BankReconciliation
    * axelor-stock: Inventory
    * axelor-production: UnitCostCalculation
    * axelor-human-resources: Timesheet, PayrollPreparation, LeaveRequest, Expense, LunchVoucherMgt
    * axelor-contract: ContractVersion
* Date format:
    * Add a new locale per company
    * Use company locale and/or user locale instead of hard coded date format
* Business project: exculdeTaskInvoicing renamed to excludeTaskInvoicing.
* Template: Add 'help' tab for mail templates.
* New french admin-fr user in demo data
* Add tracking in different forms (app configurations, ebics, etc...)

#### Removed

* Removed deprecated IException interfaces (replaced by new ExceptionMessage java classes)
* Removed all translations present in source code except english and french.
* Removed axelor-project-dms module.
* Removed axelor-mobile module (replaced by axelor-mobile-settings).
* Removed Querie model.
* SaleOrder: removed following unused fields:
    * `invoicedFirstDate`
    * `nextInvPeriodStartDate`
* PaymentSession: removed cancellationDate field.
* Account: removed unused configuration for ventilated invoices cancelation.

#### Fixed

* Password : passwords fields are now encrypted
    <details>
        Concerned models : Ebics User, Calendar and Partner.
        You can now encrypt old fields by using this task :
        `gradlew database --encrypt`
    </details>


[7.0.28]: https://github.com/axelor/axelor-open-suite/compare/v7.0.27...v7.0.28
[7.0.27]: https://github.com/axelor/axelor-open-suite/compare/v7.0.26...v7.0.27
[7.0.26]: https://github.com/axelor/axelor-open-suite/compare/v7.0.25...v7.0.26
[7.0.25]: https://github.com/axelor/axelor-open-suite/compare/v7.0.24...v7.0.25
[7.0.24]: https://github.com/axelor/axelor-open-suite/compare/v7.0.23...v7.0.24
[7.0.23]: https://github.com/axelor/axelor-open-suite/compare/v7.0.22...v7.0.23
[7.0.22]: https://github.com/axelor/axelor-open-suite/compare/v7.0.21...v7.0.22
[7.0.21]: https://github.com/axelor/axelor-open-suite/compare/v7.0.20...v7.0.21
[7.0.20]: https://github.com/axelor/axelor-open-suite/compare/v7.0.19...v7.0.20
[7.0.19]: https://github.com/axelor/axelor-open-suite/compare/v7.0.18...v7.0.19
[7.0.18]: https://github.com/axelor/axelor-open-suite/compare/v7.0.17...v7.0.18
[7.0.17]: https://github.com/axelor/axelor-open-suite/compare/v7.0.16...v7.0.17
[7.0.16]: https://github.com/axelor/axelor-open-suite/compare/v7.0.15...v7.0.16
[7.0.15]: https://github.com/axelor/axelor-open-suite/compare/v7.0.14...v7.0.15
[7.0.14]: https://github.com/axelor/axelor-open-suite/compare/v7.0.13...v7.0.14
[7.0.13]: https://github.com/axelor/axelor-open-suite/compare/v7.0.12...v7.0.13
[7.0.12]: https://github.com/axelor/axelor-open-suite/compare/v7.0.11...v7.0.12
[7.0.11]: https://github.com/axelor/axelor-open-suite/compare/v7.0.10...v7.0.11
[7.0.10]: https://github.com/axelor/axelor-open-suite/compare/v7.0.9...v7.0.10
[7.0.9]: https://github.com/axelor/axelor-open-suite/compare/v7.0.8...v7.0.9
[7.0.8]: https://github.com/axelor/axelor-open-suite/compare/v7.0.7...v7.0.8
[7.0.7]: https://github.com/axelor/axelor-open-suite/compare/v7.0.6...v7.0.7
[7.0.6]: https://github.com/axelor/axelor-open-suite/compare/v7.0.5...v7.0.6
[7.0.5]: https://github.com/axelor/axelor-open-suite/compare/v7.0.4...v7.0.5
[7.0.4]: https://github.com/axelor/axelor-open-suite/compare/v7.0.3...v7.0.4
[7.0.3]: https://github.com/axelor/axelor-open-suite/compare/v7.0.2...v7.0.3
[7.0.2]: https://github.com/axelor/axelor-open-suite/compare/v7.0.1...v7.0.2
[7.0.1]: https://github.com/axelor/axelor-open-suite/compare/v7.0.0...v7.0.1
[7.0.0]: https://github.com/axelor/axelor-open-suite/compare/v6.5.7...v7.0.0
