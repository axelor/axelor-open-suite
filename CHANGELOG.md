## [6.5.39] (2024-08-08)

### Fixes
#### Bank Payment

* BankOrder : fixed manual multi currency bank order's move generation

## [6.5.38] (2024-07-25)

### Fixes
#### Base

* Translation: fixed an issue where 'Canceled', 'Confirmed', 'Received' french translations were wrong.
* Product: reset the serial number on product duplication.

#### Bank Payment

* Bank reconciliation: fixed total of selected move lines in multiple reconciles when currency is different from company currency.

#### Human Resource

* Expense line: fixed error when computing kilometric distance without choosing a type.

#### Purchase

* Purchase order: fixed french typo for 'nouvelles version'.

#### Sale

* Configurator creator: fixed issue related to meta json field simple name.
* Sale order: fixed an issue preventing from invoicing X% of a sale order as an advance payment where X was greater than the sale order total.

## [6.5.37] (2024-07-11)

### Fixes
#### Base

* Product: fixed NPE when duplicating and saving a product.

#### Account

* Block customers with late payment batch: fixed an issue where the batch did not block some partners.
* Accounting situation: fixed VAT system display when partner is internal.

#### CRM

* Catalog: fixed an issue where the user could upload files other than PDF.

#### Sale

* Sale order template: fixed NPE when company is empty.

#### Stock

* Sales dashboard: Fixed stock location for customer deliveries.

#### Supply Chain

* Invoice: removed time table link when we merge or delete invoices, fixing an issue preventing invoice merge.

## [6.5.36] (2024-06-27)

### Fixes
#### Base

* Template: fixed translation for content panel title.
* Partner: fixed merging duplicates not working correctly.

#### Account

* Payment voucher: fixed technical error when the user's active company is null and there are more than one company in the app.
* Invoice: fixed foreign currency invoice duplication when current currency rate is different from the currency rate in the original invoice.
* Invoice: fixed an issue where due date was not updated correctly when saving an invoice.

#### Bank Payment

* Bank statement line: prevent user from creating a new bank statement line manually.
* Bank reconciliation: fixed an issue where move lines were not displayed even when they should.
* Bank reconciliation: fixed move line filter when using multiple reconciliations.

#### Contract

* Contract: fixed wrong tax check on supplier contract.

#### Human Resource

* Payroll Preparation: fixed an issue were leaves were not always displayed.

#### Purchase

* Purchase order: Fixed purchase order lines not displayed on 'Historical' menu.

#### Supply Chain

* Fixed forwarder partner domain filter.

## [6.5.35] (2024-06-07)

### Fixes
#### Account

* Account: fixed technical type select for journal type demo data.
* Move: fixed a technical error when adding a first line without invoice terms in sale or purchase move.
* Analytic distribution template: removed analytic percentage total verification when we add a new analytic distribution line.
* Invoice/AutoReconcile: removed tax moveline reconciliation in move excess or move due at ventilation.
* Payment session: fixed issue when validating the payment session causing amount being updated and not corresponding to the value of the amount being validated and equal to the bank order generated.
* Accounting report: fixed opening moves are not displayed on aged balance report.

#### Bank Payment

* Invoice payment: fixed payment remaining 'Pending' while bank order has been realized (while no accounting entry generated).

#### CRM

* Partner: fixed display condition for customer recovery button.

#### Human Resource

* Extra hours: fixed an issue where lines were filled with the connected employee instead of the employee filled in the form view.
* Payroll preparation: correctly empty lists on payroll preparation view when employee is changed.

## [6.5.34] (2024-05-24)

### Fixes
#### Base

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
* Accounting report: fixed bank reconciliation accounting report displaying already reconciled move lines

#### Business Project

* Timesheet line: fixed an issue preventing to invoice timesheet when the task has an activity.

#### Contract

* Contract: fixed 'nouvelle version' used as key instead of 'new version'.
* Contract: deleted version history while duplicating.

#### Human Resource

* Leave request: fixed issue where a leave request was not updated after sending it.

#### Maintenance

* Maintenance request: fixed impossible to create a maintenance request from the quick adding field.

#### Purchase

* Purchase request: added sequence for purchase request in demo data.

#### Sale

* Sale order merge: fixed an issue where it was not possible to select a price list in case of conflicts.

#### Supply Chain

* Customer invoice line: fixed default product unit on product change.
* Purchase order: when a purchase order is generated from a sale order, when the catalog does not have a code or name, it will use the product.

## [6.5.33] (2024-05-03)

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

## [6.5.32] (2024-04-19)

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

## [6.5.31] (2024-04-04)

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

Removed `action action-sale-order-line-record-progress`

## [6.5.30] (2024-03-21)

### Fixes
#### Base

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

## [6.5.29] (2024-03-07)

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

## [6.5.28] (2024-02-22)

#### Fixed

* Stock location: fixed wrong QR Code on copied stock location.
* Invoice: fixed an issue when returning to the refund list after creating a refund from an invoice.
* Bank order: fixed multi currency management.
* Cost calculation: fixed JNPE error on select of product.
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

## [6.5.27] (2024-02-01)

#### Fixed

* Stock: added quality control default stock location to stock config demo data to avoid exception case.
* Accounting batch: fixed anomaly generated when running the closing/opening accounts batch with option simulate generated moves option ticked.
* Project task: fixed the ticket task form views.
* Expense line: fixed an UI issue where some fields were not required in the form view of kilometric expense line.
* Accounting batch: added financial account with a technical type 'asset' when we open/close the year accounts.
* Expense line: fixed a bug where 'Type' was not properly required in kilometric expense line.
* Sale order: adjusted price and quantity precision according to the user-configured precision settings.
* Purchase order: fixed an issue where duplicated order did not get their order date updated.
* Contract: added control on invoice period start date and invoice period end date.
* Bank reconciliation: hid reverse move with the 'Hide move lines in bank reconciliation'.
* Debt recovery: fixed debt recovery batch error when no invoice term was found.
* Leave request: user in not required in an employee anymore to increase leave from batch.
* Product Details: fixed 'id to load is required for loading' error when stockLocationLine has no unit.
* Sale order line: fixed an issue where some products were not selectable in a line.
* Production process line: fixed 'NullPointerException' popup error while opening a line.
* Expense line: hid analytic panel when the expense is not validated.
* Invoicing project: invoice lines generated have now a correct unit price.
* Move: fixed canceled payments preventing moves from being updated.
* Bank reconciliation: fixed dates management in moveline query.
* Contract: emptied the contract line when emptying the product.
* Reconcile: prevent reconciliation if an involved move has a line with a tax account but no tax.
* Control point: fixed creation from the menu entry.
* Birt template parameter: fixed a small UI issue by ordering of the list of parameters.
* Move: batch control move consistency exception management.
* Expense line: compute total amount and distance depending on kilometric type.

## [6.5.26] (2024-01-12)

#### Fixed

* Hotfix: add missing binding preventing server startup.

## [6.5.25] (2024-01-11)

#### Fixed

* Product: make the product unit field required for stock managed items.
* Reconcile: fixed invoice term amount remaining with multi currency.
* Stock move: fixed unable to select external stock location on receipt.
* Invoice: Duplicating an invoice now correctly empties the origin date and the supplier invoice number.
* Invoice: fixed error when we change partner in supplier invoice with invoice lines.
* Account clearance: fixed the way tax and vat system are managed.
* Indicators: fixed results display.
* Sale order: keep attached files and internal notes when merging sale orders.

## [6.5.24] (2023-12-21)

#### Fixed

* Stock location: Issue on Edition of Location financial data report
* Wrong quote sequence number on finalize
* Invoice : Fix partner account related errors consistency when validating and ventilating
* [Ticket]: Fix Ticket timer buttons
* INVOICE : currency not updated on first partner onChange
* Move Line : Prevent from updating tax line when the move is accounted
* Custom accounting report : Disable group by account feature as it cannot be fixed until 8.1
* Custom accounting report : Fix an issue with custom rule priority
* MAINTENANCE ORDER : fix NPE when we try to plan
* Move lettering : Fill the date of lettering when the status becomes Temporary
* EXPENSE LINE: fix totalTax is not in readonly in form view when expense product blocks the taxes
* Sale / Purchase / Invoice : Fix cases where price decimal config wasn't being used
* Fixed asset : fix depreciation date when failover is complete
* EVENT : some highlights conditions in grids use __datetime__
* Printing template / Printing setting: fix translation issue in position fields.
* Move line: Added reference of move line in sum's control error message
* Sale Order/Purchase Order: Error when emptying contact
* Advance Payment Invoice : Fix error when trying to pay
* Bank order: Fixed issue in code generating move
* Business Support: TaskDeadLine field not hidden when the app is installed anymore.
* UNITCOSTCALCULATION : Irrelevant message when no product has been found
* Account clearance : Fix an issue where move lines could be created or edited there
* Reconcile : Fix an issue where a payment was created for a reconcile with an account not being used for partner balance
* Analytic Move Line : Fix type select when we create a new analytic move line from orders, invoices, contract and moves

## [6.5.23] (2023-12-07)

#### Fixed

* Sale order: fixed JNPE error when copying a sale order without lines.
* Purchase request: fixed reference to purchase order being copied on duplication.
* Accounting batch: hide unnecessary payment mode information.
* Sale order: fixed wrong price update when generating quotation from template.
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
* Invoice: fixed reference to "Refusal to pay reason" being copied on invoice duplication.
* Timesheet: fixed timesheet line date check.
* Stock move: allow to create a tracking number directly on stock move line.
* Cost calculation: fixed an issue preventing an infinite loop in case of an error in bill of materials hierarchy.
* Account: forbid to select the account itself as parent and its child accounts.
* Bank order: highlight orders sent to bank but not realized.
* Payment session: fixed display of currency symbol in payment session.
* Move template line: hide and set required tax field when it is configured in financial account.
* Inventory line: fixed update of inventory line by taking into account stockLocation of line when possible, triggering update when necessary.
* Invoice: fixed partially paid invoices in bill of exchange management.
* Stock move: allow to select external stock location for deliveries.

## [6.5.22] (2023-11-23)

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
* Stock location line: fixed quantity displayed by indicators in form view.
* Invoice: fixed currency being emptied on operation type change.
* Bank statement: added missing french translation when we try to delete a bank statement.
* Invoice: fixed an issue where enabling pfp management and opening a previously created supplier invoice would make it look unsaved.
* Fixed asset: fixed inconsistency in accounting report.
* Bank reconciliation: selectable move line are now based on the currency amount.
* Move Template: hide move template line grid when journal or company is not filled.
* Project task: fixed broken time panel in project task.
* Expense: fixed an issue were employee was not modified in the lines when modifying the employee on the expense.
* City: fixed errors when importing city with geonames.
* Period: fixed status not reset on closure.
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

## [6.5.21] (2023-11-09)

#### Fixed

* App Base: set company-specific product fields domain.
* Accounting report: fixed an issue in Journal report (11) where debit and credit were not displayed in the recap by account table.
* Fixed asset: fixed an issue after selecting a category where set depreciations for economic and ifrs were not computed correctly.
* CRM: opening an event from custom view in prospect, leads or opportunity is now editable.
* Custom accounting report: fixed legacy report option being displayed for all account report types.
* Accounting dashboard: removed blank panel in "accounting details with invoice terms" dashboard.
* Cost sheet: fixed issue in the order of calculation on bill of materials.
* Configurator BOM: Fixed a concurrent error when generating a bill of materials from the same configurator.
* Employee: to fix bank details error, moved the field to main employment contract panel.
* Fixed asset: hide "isEqualToFiscalDepreciation" field when fiscal plan not selected.
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

## [6.5.20] (2023-10-27)

#### Fixed

* Debt Recovery: fixed error message on debt recovery generation to display correctly trading name.
* Invoice/Move: fixed due date when we set invoice date, move date or when we update payment condition.
* FEC Import: the header partner is now filled correctly

Multi-partner: no partner in header
Mono-partner: fill partner in header

* Analytic: fixed analytic distribution verification at validation/confirmation of purchase order/sale order when analytic is not managed.
* Payment Voucher: fixed display of load/reset imputation lines and payment voucher confirm button when paid amount is 0 or when we do not select any line.
* Period closure: fixed a bug where status of period was reset on error on closure.
* Manufacturing order: fixed an issue where outsourcing was not activated in operations while it was active on production process lines.
* Payment voucher: fixed error at payment voucher confirmation without move lines.
* Payment voucher: removed paid line control at payment voucher confirmation.
* Fixed asset: fixed popup error "Cannot get property 'fixedAssetType' on null object" displayed when clearing fixed asset category field.
* Cost sheet: replaced 'NullPointerException' error by a correct message when an applicable bill of materials is not found on cost calculation.

## [6.5.19] (2023-10-18)

#### Fixed

* Product: fixed stock indicators computation.
* Project: fixed french translation for 'Create business project from this template'.
* Payment session: set currency field readonly on payment session.
* Period: improved period closure process from year form.
* Sale order: fixed duplicated 'Advance Payment' panel.
* Manufacturing order: fixed "Circular dependency" error preventing to start operation orders.
* Purchase request: fixed 'Group by product' option not working when generating a purchase order.
* Sequence: fixed sequence duplication causing a NPE error.
* Move: fixed an error when we create a move line with a partner without any accounting situation.
* SOP/MPS: fixed a bug where an existing forecast was modified instead of creating a new one when the forecast was on a different date.

## [6.5.18] (2023-10-06)

#### Fixed

* Period: set period status at open if closure fails.
* Move template: fixed description not being filled on moves generated from a percentage based template.
* Reconcile: fixed amount error triggering on unwanted cases.
* Interco invoice: fixed default bank details on invoice generated by interco.
* Invoice term: on removal, improved error message if invoice term is still linked to a object.
* Sale order line: fixed error when emptying product if the 'check stock' feature was enabled for sale orders.
* Invoice term: fixed wrong amount computation at invoice term generation with hold back.
* Sale order line: fixed an issue where updating quantity on a line did not updated the quantity of complementary products.
* Move: fixed counterpart generation with multi currency.
* Stock move: filled correct default stock locations on creating a new stock move.
* Stock move: fixed wrong french translation on stock move creation and cancelation.
* User: encrypt password when importing user data-init.
* Print template: iText dependency has been replaced by Boxable and PDFBox.
* Cost calculation: fixed the import of cost price calculation.
* HR: fixed an issue preventing people from creating a required new training.
* Task: improved performance of the batch used to update project tasks.
* Sale order: fixed an error occurring when generating analytic move line of complementary product.
* Invoicing dashboard: fixed error on turnover per month charts.
* Move: fixed reverse charge tax computation.
* Account move: fixed default currency not automatically filled on a new accounting move.
* Debt recovery: fixed missing letter template error message.
* GDPR request: changed UI for selecting search results.
* Project: fixed tracebacks not showing on project totals batch anomalies.
* Reconcile: fixed multiple move lines reconciliation.
* Move: fixed move lines consolidation.
* Irrecoverable: fixed tax computing.
* Move: fixed multi invoice term due dates being wrongly updated when accounting.
* Debt recovery batch: reduced the execution time.
* Bank reconciliation: fixed balance not considering move status.

## [6.5.17] (2023-09-21)

#### Fixed

* Follower: fixed an error occuring when sending a message while adding a follower on any form.
* Invoice payment: when validating a invoice payment from a bank order, the payment date will now be correctly updated to bank order date.
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
* Stock location: fixed new average price computation in the case of a unit conversion.
* Manufacturing order: when pausing an operation order, the manufacturing order will be paused if there is no "In progress" operation order
* BANKDETAILS / COMPANY : Fix prefill company data when creating a bankDetails
* Invoice: fixed error when modfying an analytic line percentage.
* Move template: fixed invoice terms not being created on a move template generation by amount.
* Stock correction: add demo data for Stock correction reason.

## [6.5.16] (2023-09-11)

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

## [6.5.15] (2023-08-24)

#### Fixed

* Webapp: update Axelor Open Platform dependency to 5.4.22.
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

* Move: The date is not correctly required in the form view.
* Payment voucher / Invoice payment: fixed generated payment move payment condition.
* Payment voucher: fixed excess payment.
* Invoice payment: add missing translation for field "total amount with financial discount".
* Invoice: fixed financial discount deadline date computation.
* Sale order line: fixed a bug where project not emptied on copy.
* Stock move printing: fixed an issue where lines with different prices were wrongly grouped.
* Stock details: fixed "see stock details" button in product and sale order line form views.
* Accounting batch: corrected cut off accounting batch preview record field title cut in half.
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

## [6.5.14] (2023-08-08)

#### Fixed

* Invoice : fix the way we check the awaiting payment
* Accounting batch : Improve user feedback on move consistency control when there are no anomalies
* PLANNING: Planning is now correctly filtered on employee and machine form
* SaleOrderLine: Description is now copied only if the configuration allows it
* PARTNER: Fixed a bug where button 'create sale quotation' was always displayed
* Custom accounting report : Excel sheets are now named after the analytic account
* Invoice term : Fixed wrong percentage sum check that was blocking move save
* Move : Fix automatic move line tax generation with reverse charge and multiple vat systems.
* PurchaseOrder and Invoice: Added widget boolean switch for interco field
* Invoice : Fix tax being empty on invoice line when it's required on account
* ManufOrder: Planning a cancelled MO now clears the real dates on operations orders and MO
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

* Analytic distribution template: Fix error when creating a new analytic distribution template without company
* Invoice : Remove payment voucher access on an advance payment invoice
* Payment session : Fix session total amount computation
* Move : Fix invoice term amount at percentage change with unsaved move
* Product: When changing costTypeSelect to 'last purchase price', the cost price will now be correctly converted.
* Bank order: Fixed a bug where bank order date was always overridden. Now bank order date is overridden only when it is before the current date and the user is warned.
* BUSINESS PROJECT BATCH: Fixed invoicing project batch

## [6.5.13] (2023-07-20)

#### Fixed

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
* Move line: filter analytic per company.
* Product: fixed wrong filter on analytic on product accounting panel.
* Sale order: improved performance when loading card views.
* Interco: fixed generated sale order/purchase order missing a fiscal position.

## [6.5.12] (2023-07-11)

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
* Accounting report config line: prevent from creating a line with an invalid groovy code.
* Move: fixed automatic fill of VAT system when financial account is empty.
* Move: fixed duplicate origin verification when move is not saved.
* Invoice: fixed error when cancelling an invoice payment.
* Product/Account Management: hide financial account when it is inactive on product account management.
* Reconcile group: added back "calculate" and "accounting reconcile" buttons on move line grid view.
* Forecast generator: fixed endless loading when no periodicity selected.
* Invoice: fixed an error preventing the invoice printing generation.
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
* Custom accounting report : Fix analytic values not taking result computation method into account
* Purchase Order/Sale Order/Contract : Remove wrong analytic line link after invoice generation
* Payment session: change titles related to emails on form view.
* Payment voucher: fixed invoice terms display when trading name is not managed or filled.
* Move: on change of company, currency is now updated to company currency even when partner filled.
* Contract: fixed an error occurring when invoicing a contract

An error occurred when invoicing a contract if time prorated Invoice was enabled and then periodic invoicing was disabled.

* Fixed asset: fixed being able to dispose a fixed asset while generating a sale move but with no tax line set.
* Move: removed verification on tax in move with cut off functional origin.
* Invoice term: Fixed company amount remaining on pfp partial validation to pay the right amount.
* Details stock location line: removed reserved quantity from the form view.
* Sale order: fixed totals in sales order printouts.
* Invoice: fixed view marked as dirty after invoice validation.
* Account printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Accounting report config line: filter analytic accounts with report type company.
* Invoice/Move: filled analytic axis on move when we ventilate an invoice.

## [6.5.11] (2023-06-22)

#### Features

* INVOICE : mandatory reference to the original invoice on the printing

The credit invoice now includes all the compulsory information on the original invoice. It also contain the credit note referring to the invoice it cancels or modifies. For example -  "In reimbursement of Invoice n°XXXX, issued on DD/MM/YYYY".


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
* Base printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Sequence: when configuring a sequence, end date of a sequence now cannot be prior to the starting date and vice versa.
* Invoice: fixed an issue where invoice terms information were displayed on the invoice printing even when the invoice term feature was disabled.
* Stock location history batch: deactivate re-computation stock location history batch.
* Product: "Control on receipt" and "custom codes" are now correctly managed per company (if the configuration is activated).
* Invoice: do not set financial discount on refunds.
* Reconcile: fixed an issue where letter button was shown if the group was unlettered.
* Invoice: added missing translation on an error message that can be shown during invoice ventilation.
* Sale order: fixed discount information missing on reports.
* Invoice: fixed an issue happening when we try to save an invoice with an analytic move line on invoice line.
* Stock Move: fixed a bug where future quantity was not correctly updated.
* Partner: fixed an issue where blocking date was not displayed
* Accounting report VAT invoicing/payment: fixed differences in display between reports.
* Move: fixed currency exchange rate wrongly set on counterpart generation.
* Accounting Batch: accounting cut-off batch now takes into account 'include not stock managed product' boolean for the preview.
* Sale order: fixed an issue when computing invoicing state where the invoiced was marked as not invoiced instead of partially invoiced.
* Trading name: fixed wrong french translation for trading name ('Nom commercial' -> 'Enseigne').

## [6.5.10] (2023-06-08)

#### Fixed

* Business project, HR printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Configurator: fixed issue where sale order line generated from the configurator did not have a bill of materials.
* Deposit slip: fixed errors when loading selected lines.
* Invoice: allow supplier references (supplier invoice number and origin date) to be filled on a ventilated invoice.
* Invoice/Stock move: fixed an issue where invoice terms were not present on an invoice generated from a stock move.
* Invoice: fixed an issue where the button to print the annex was not displayed.
* Account config: hide 'Generate move for advance payment' field when 'Manage advance payment invoice' is enabled.
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
* Cost calculation: fixed calculation issue when computing cost from a bill of materials.
* Tracking number: fixed an issue preventing to select a product on a manually created tracking number.
* Reconcile: fixed an issue were it was possible to unreconcile already unreconciled move lines.
* Fixed asset: fixed JNPE error on disposal if account config customer sales journal is empty.
* Accouting report view: fixed an issue where the filter on payment mode was displayed on an analytic report type.

## [6.5.9] (2023-05-25)

#### Fixed

* Production, Sale, Purchase, Quality printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Invoice payment: disable financial discount process when the invoice is paid by a refund.
* Accounting batch: fixed close annual accounts batch when no moves are selectable and simulate generate move if needed.
* Configurator: fixed an issue where removing an attribute did not update the configurator form.
* Tax: fixed tax demo data missing accounting configuration and having wrong values.
* Sale order: fixed an issue during sale order validation when checking price list date validity.
* Printing settings: on orders and invoices, removed the filter on printing settings.
* Invoice payment: update cheque and deposit info on the invoice payment record when generated from Payment Voucher and Deposit slip.
* Purchase order: fixed an error occurring when generating an invoice from a purchase order with a title line.
* Accounting batch: fix duplicated moves in closure/opening batch.
* Bank reconciliation: fixed an issue in bank reconciliation printing where reconciled lines still appeared.
* RGPD search: fixed an issue where some filters in the search were not correctly taken into account.
* Bill of materials: fixed creation of personalized bill of materials.
* Invoice: added an error message when generating moves with no description when a description is required.
* Project: fixed an issue when creating a task in a project marked as "to invoice" where the task was not marked as "to invoice" by default.
* Manufacturing order: fixed filter on sale order.
* Bank order: fixed payment status update when we cancel a bank order and there are still pending payments on the invoice.
* Move: fixed an error that occured when selecting a partner with an empty company.
* Summary of gross values and depreciation accounting report: fixed wrong values for depreciation columns.
* Manufacturing order: when planning a manufacturing order, fixed the error message when the field production process is empty.
* Timesheet: when generating lines, get all lines from project instead of only getting lines from task.
* Accounting report DAS 2: fixed export not working if N4DS code is missing.
* Accounting report DAS 2: fixed balance.
* Bank order: fixed an issue where moves generated from a bank order were not accounted/set to daybook.
* Project task: when creating a new project task, the status will now be correctly initialized.
* Product: fixed an issue where activating the configuration "auto update sale price" did not update the sale price.
* Stock move: prevent cancellation of an invoiced stock move.
* Stock move: modifying a real quantity or creating an internal stock move from the mobile application will correctly indicate that the real quantity has been modified by an user.
* Bank order: fixed an issue where the process never ended when cancelling a bank order.
* Sale order: fixed popup error "Id to load is required for loading" when opening a new sale order line.
* Journal: fixed error message when the "type select" was not filled in the journal type.
* Account config: fixed UI and UX for payment session configuration.
* Account/Analytic: fixed analytic account filter in analytic lines.
* Invoice: fixed an error preventing from merging invoices.
* Expense: prevent deletion of ventilated expense.

## [6.5.8] (2023-05-11)

#### Fixed

* Invoice: fixed bank details being required for wrong payment modes.
* Invoice: fixed an issue blocking advance payment invoice creation when the lines were missing an account.
* Job application: fixed an error occuring when creating a job application without setting a manager.
* Bank reconciliation: added missing translation for "Bank reconciliation lines" in french.
* Product: fixed an issue preventing product copy when using sequence by product category.
* Bank reconciliation/Bank statement rule: added a control in auto accounting process to check if bank detail bank account and bank statement rule cash account are the same.
* Stock move: fixed an issue when creating tracking number from an unsaved stock move. If we do not save the stock move, tracking number are now correctly deleted.
* Sale order: fixed an issue where sale order templates were displayed from the 'Historical' menu entry.
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
* Project: Display "Ticket" instead of "Project Task" in Activities tab when the activity is from a ticket.
* Opportunity: added missing sequence on the Kanban and Card view.
* Payment session: select/unselect buttons are now hidden when status is not in progress.
* Analytic move line query: fixed filter on analytic account.
* Bank reconciliation: fixed initial and final balance when importing multiple statements.
* Accounting report: fixed translation of currency title.
* Inventory: fixed UI issue by preventing unit from being changed in inventory lines.
* Stock rules: now correctly apply stock rules when minimum quantity is zero.

## [6.5.7] (2023-04-27)

#### Fixed

* Move: removed save action when we change the move date.
* Purchase order: fixed fiscal position on a purchase order generated from a sale order.
* FEC import: fixed an error occuring when importing FEC using the format without taxes.
* Fixed asset: corrected JNPE error on disposal fixed asset with sale option and date error on disposal fixed asset with derogatory line.
* Debt recovery: fixed a a regression on demo data, the demo data should now have existing email templates.
* Batch bill of exchange: raise an anomaly when invoices are ready to be processed but bank details is inactive.
* Invoice: added a verification for analytics account on validate and ventilate button.
* Stock move: fixed an error occurring when emptying the product in a line.
* Move: fixed an error happening when regenerating invoice terms while the move is not saved.
* Analytic Rules: added a company filter on analytic account verification.
* Payment session: optimization done to improve performance of invoice term search process.
* Payment session: improved form view by removing blank spaces by adding a smaller dashlet.
* Group Menu Assistant: fixed an issue where an empty file was generated.
* Move line/Fixed asset: corrected wrong journal on fixed asset generated from move line.

## [6.5.6] (2023-04-21)

#### Fixed

* Sale order: fixed an issue where opening or saving a sale order without lines was impossible due to an SQL error.

## [6.5.5] (2023-04-20)

#### Features

* Accounting move: redesigned form view.

#### Changes

* Payment session: highlight in orange invoice terms with a financial discount.

#### Fixed

* Sale order: sale orders with a 0 total amount are now correctly displayed as invoiced if they have an ventilated invoice.
* Account management: fixed an issue where global accounting cash account was not displayed.
* GDPR: fixed an error occurring when anonymizing objects with mail message or comment.
* GDPR: fix demo data - add faker for AOS data.
* GDPR: admin user is now excluded from the processing register
* GDPR: improve default configuration.
* Processing register: fixed an error blocking the process occurring sometimes when more than 10 objects were anonymized.
* Partner: fixed script error when opening partner contact form (issue happening only when axelor-base was installed without axelor-account).
* Invoice: fixed invoice payment when bank order confirmation is automatic.
* Operation order calendar: display operation orders with all status except operations in draft, cancelled or merged manufacturing orders.
* Payment session: removed sidebar in form view.
* Move/Simplified Move: hide counterpart generate button when functional origin is not sale, expense, fixed asset or empty.
* Customer/Prospect reporting: fixed an error occuring if we only have axelor-base installed when opening the dashboard.
* Move: fixed analytic move lines copy when reversing a move.
* Partner: fixed an error preventing the display of partner sale history when a product was sold with a quantity equals to 0.
* Operation order: fix UI issues when the user was modifying date time fields used for the planification.
* Invoice: fixed unbalanced move generation when we create an invoice with holdback.
* Payment session: keep linked invoice terms when invoice terms needs to be released from payment session, when refund or bank order process for example.
* Payment session: allow to update parameters and refresh invoice terms.
* Base batch: fixed an issue when clicking the button to run manually the "synchronize calendar" batch.
* Move/Invoice term: Skip invoice term with holdback computation, if the functional origin select of the move is not fixed asset, sale, or purchase.
* Payment session/bank order: fixed issue where payments on invoices remains in pending state when autoConfirmBankOrder on payment mode is true.
* Partner: fixed an error preventing the display of partner sale history when a product was sold with a quantity equals to 0.
* Move: fixed an issue where the form view is marked as dirty even when opening a form view in readonly.
* BPM: fixed view attribute issue for a sub-process.
* Invoice: fixed an error occurring when validating an invoice if invoice statements were not correctly configured.
* Stock move: fixed an issue when opening stock move line form from the invoicing wizard.
* Payment session: remove reload on invoice term dashlet for select/unselect buttons.
* Payment condition: added missing english demo data for payment condition line.
* Message: fixed an issue where emails automatically sent were not updated.
* Invoice: fixed filter on company bank details for factorized customer so we are able to select the bank details of the factor.
* Sale order: generating a purchase order from a sale order now correctly takes into account supplier catalog product code and name.
* Accounting report DAS2: fixed balance panel computation.
* Stock move: now prevent splitting action on stock move line that are associated with a invoice line.
* Bank reconciliation: fixed tax account retrieval given bank statement rule.
* Invoice: to avoid inconsistencies, now only canceled invoices can be deleted.
* Accounting period: fixed an issue where the user was able to reopen a closed period on a closed year.
* Bank details: fixed script error when opening bank details form (issue happening only when axelor-base was installed without axelor-account).
* Payment session: fill partner bank details on move generation accounted by invoice terms.

## [6.5.4] (2023-04-06)

#### Database change

* App account: fixed the configuration to activate invoice term feature.

For this fix to work, database changes have been made as a new boolean configuration `allowMultiInvoiceTerms` has been added in `AppAccount` and `hasInvoiceTerm` has been removed in `Account`.
If you do nothing, it will work but you will need to activate the new configuration in App account if you want to use invoice term feature. Else it is recommended to run the SQL script below before starting the server on the new version.

```sql
  ALTER TABLE base_app_account
  ADD COLUMN allow_multi_invoice_terms BOOLEAN;

  UPDATE base_app_account
  SET allow_multi_invoice_terms = EXISTS(
    SELECT 1 FROM account_account
    WHERE has_invoice_term IS TRUE
  );

  ALTER TABLE account_account
  DROP COLUMN has_invoice_term;
```

#### Deprecated

* Stock API: Deprecate API call to stock-move/internal/{id}

#### Changes

* Webapp: update AOP version to 5.4.19
* GDPR: added help on AppGdpr configuration fields.

#### Fixed

* Tracking number: fix inconsistent french translation.
* Stock: fixed an issue in some processes where an error would create inconsistencies.
* Contract: fixed an issue in some processes where an error would create inconsistencies.
* Sale: fixed an issue in some processes where an error would create inconsistencies.
* Studio: fixed an issue in some processes where an error would create inconsistencies.
* Supplier management: fixed an issue in some processes where an error would create inconsistencies.
* App base config: added missing french translation for "Manage mail account by company".
* Sequence: fixed sequences with too long prefix in demo data.
* Supplychain: fixed error while importing purchase order from demo data.
* Accounting report DGI 2055: fixed issues on both tables.
* Stock move line: modifying the expected quantity does not modify a field used by the mobile API anymore.
* Move: fixed an issue so the form is not automatically saved when updating the origin date and the due date.
* Invoice: fixed payment when bank order confirmation is automatic.
* Bank details: fixed error occurring when base module was installed without bank-payment module.
* Sale order: fixed the currency not updating when changing the customer partner.
* Payment session: fixed an issue where the field "partner for email" was not emptied on copy and after sending the mail.
* Account management: fixed typo in the title of the field "notification template" and filter this field on payment session template.
* Base batch: Removed "Target" action in form view as this process does not exist anymore.
* Move line: fixed retrieval of the conversion rate at the date of the movement.
* Company: correctly hide buttons to access config on an unsaved company.
* Message: fixed a bug that could occur when sending a mail with no content.
* Inventory: fixed a bug where inventory lines were not updated on import.
* Menu: fixed menu title from 'Template' to 'Templates'.
* Json field: added missing field 'readonlyIf' used to configure whether a json field is readonly.
* BPM: fixed timer event execution and optimised cache for custom model.
* Payment session: fixed buttons displaying wrongly if session payment sum total is inferior or equal to 0.
* Accounting report journal: fixed report having a blank page.
* Stock move: when updating a stock move line, can now set an unit with the stock API.
* Manufacturing order: fixed an issue where emptying planned end date would cause errors. The planned end date is now required for planned manufacturing orders.
* Sequence: fixed an issue where we could create sequences with over 14 characters by adding '%'.
* Reconcile: improve reconciliations performances with large move lines lists.
* Bank statement: fixed issue with balance check on files containing multiple bank details and multiple daily balances.
* Studio editor: fixed theme issue.
* SaleOrder: reintroduced send email button in the toolbar.
* Accounting report payment vat: fixed no lines in payment vat report sum by tax part and not lettered part.
* Account, Invoice and Move: Remove error message at analytic distribution template on change when no analytic rules is configured.
* Timesheet: fixed an issue preventing to select a project in the lines generation wizard.
* Purchase order supplier: fixed desired receipt date field on form view.
* Payment voucher: fixed status initialization on creation.
* Manufacturing order: in form view, fixed buttons appearing and disappearing during view load.
* Project: fixed errors occuring when business-project was not installed.
* GDPR: fixed issues when copying GDPRRequest and GDPRRegisterProcessing.
* City: fixed an error occurring when importing city with manual type.

## [6.5.3] (2023-03-23)

#### Changes

* Webapp: update AOP version to 5.4.19

#### Fixed

* Bank reconciliation: fixed incorrect behaviour while correcting a validated bank reconciliation.
* Tracking number configuration : 'Auto select sale tracking Nbr.' is now correctly taken into account when creating a stock move from a sale order.
* Accounting report: For all reports, remove the 10000 and 40 lines limit before page break.
* Accounting batch: hide "bank details" filter for batch Moves consistency control.
* Production: fixed an issue in some processes where an error would create inconsistencies.
* Bank payment: fixed an issue in some processes where an error would create inconsistencies.
* Account: fixed an issue in some processes where an error would create inconsistencies.
* HR: fixed an issue in some processes where an error would create inconsistencies.
* Account: hide analytic settings panel when analytic management is not activated on the company.
* Analytic distribution line: corrected error '0' when analytic account is selected.
* Payment session: accounting method and move accounting date are now correctly readonly on a canceled payment session.
* Invoice: fixed PFP check when paying multiple supplier invoices.
* Helpdesk: fixed error when saving tickets on an instance using demo data.
* Accounting batch: reset cut off move status when on journal change.
* Payment session: fixed an issue where a payment session retrieved day book moves with "retrieve daybook moves in payment session" configuration deactivated.
* Payment session: fixed filter on payment session for invoice terms to retrieve invoice terms linked to refunds.
* Template: fix html widget for SMS templates.
* Template: fix "Emailing" french translation.
* Stock move: fixed an error occurring when opening a stock move line in a different tab.
* Stock move: fixed an issue where "to address" was not correctly filled on a generated reversion stock move.
* Stock move: supplier arrivals now correctly computes the WAP when the unit is different in stock move and stock location.
* Invoice: fixed an issue preventing from paying invoices and refunds.
* Product: fixed demo data of service so they are not managed in stock.
* Doubtful customer batch: fix success count on batch completion.
* HR: fixed typo "Managment" => "Managment".
* MRP: generating proposals now correctly generates every purchase order lines.
* Partner: prevent isCustomer from being unticked automatically if there are existing customer records in database.
* Move line: fixed an issue where duplicated analytic lines were generated.
* Financial discount: fixed french help translation.
* Mail message: fixed an issue preventing follower selection after a recipient has already been selected.

## [6.5.2] (2023-03-09)

#### Changes

* Debt recovery method line: add demo data email messages for B2C and B2B reminder recovery methods.

#### Fixed

* Analytic account: fixed demo data so analytic account imported are link to the company.
* GDPR: fixed demo data.
* Move: fixed error on move company change that happened if the journal was not filled in company configuration.
* Analytic/Move line: forbid move line validation if all the axis are not filled.
* Accounting Batch: prevent general ledger generation when an anomaly is thrown during the batch execution.
* BPM Editor: fix impossibility to save bpm process with subprocesses.
* Accounting Batch: fix technical error when we launch doubtful customer accounting batch that prevented the batch execution.
* Sale order: incoterm is not required anymore if it contains only services
* Account Config: fixed account chart data so imported accounts are now active.
* Move line: enabled VAT System modification when its a simulated move.
* Invoice: when the PFP feature was disabled, fixed an issue where the menu "supplier invoices to pay" was not displaying any invoices.
* Employee: in the user creation wizard, prevent validation when login is empty or if the activation and expiration dates are inconsistent.
* Invoice: fixed an error where invoice term percentage computation was blocking ventilation.
* Move/InvoiceTerm: removed possibility to add new invoiceTerm from grid.
* Base: fixed an issue in some processes where an error would create inconsistencies.
* Purchase order: fixed an error occurring when selecting a supplier partner.
* Account, Invoice and Move: add more consistency checks on analytic distribution template, to prevent unauthorized analytic distribution from being set.
* Accounting report 2054: Gross value amount of a fixed asset bought and disposed in the same year must appear in columns B and C.
* Demo data: update year and period date to have the next year present in demo data.
* Project task: fixed an issue where setting project task category would not update invoicing type.
* Mail message: use tracking subject instead of template subject when adding followers or posting comments.
* Accounting report: fixed error preventing analytic balance printing when the currency was not filled.
* Lead: fixed Lead report printings.
* Account config: fixed an issue where clicking "import chart button" was not possible until the field "Account code nbr. char" was filled.
* Move/Invoice/PurchaseOrder/SaleOrder: hide analytic panel when it is not managed on the selected company.
* Business: fixed an issue in some processes where an error would create inconsistencies.
* Invoice: ventilating an invoice refund correctly now correctly creates an invoice payment.
* Logistical Form: filter stock moves on company on logistical forms.
* CRM: fixed an issue in some processes where an error would create inconsistencies.
* Stock rules: alert checkbox is no longer displayed when use case is set to 'MRP'.
* Fixed asset: warning message translated in FR when trying to realize a line with IFRS depreciation plan.
* Fixed asset: fix typos in french translation.
* Fixed asset: fixed an issue where 'Generate a fixed asset from this line' box disappeared after selecting a fixed asset category.
* Freight carrier mode: fixed typo in french translation.
* Invoice: fixed an issue preventing to change the partner with existing invoice lines.
* Sale order: allow to select direct order stock locations from the company as the sale order stock location.
* Accounting/Invoicing: fixed typos in the configuration of "statements for item category".
* Project: fixed the display of description in Kanban view.
* HR Batch: fixed error making the batch process crash when using batch with a scheduler.
* Configurator: in the help panel for writing groovy scripts, fix external link so it is opened on a new tab by default.
* Invoice: remove the possibility to cancel a ventilated invoice.

Cancelling a ventilated invoice is not possible anymore. 
Reversing a move linked to an invoice doesn't cancel this invoice anymore.
Remove the config allowing to cancel ventilated invoice.

* Invoice: does not block the advance payment invoice validation if an account is not selected on the invoice.
* Accounting report: it is no longer required to fill the year to generate DAS 2 reports.
* Invoice: printing a report will open the correct one even if another report have the same file name.

## [6.5.1] (2023-02-24)

#### Fixed

* Accounting report: fixed advanced search feature on grid view.
* Payment session: Fix compute financial discount when the accounting date is linked to the original document
* French translation: corrected several spelling mistakes.
* Stock move: fixed error on invoice creation when we try to invoice a stock move generated from a purchase order.
* Stock move: fixed an error blocking stock move planification when app supplychain is not initialized.
* Stock move: fixed error message when trying to create partial invoice with a quantity equals to 0.
* App base config: added missing translation for Nb of digits for tax rate.
* GDPR: 'label' translation changed.
* Leave request: fixed the message informing the user of a negative number of leaves available.
* Followers: fixed a bug where a NPE could occur if default mail message template was null.
* Invoice: fixed the duplicate supplier invoice warning so it does not trigger for an invoice and its own refund.
* Invoice: fixed an issue occurring when computing financial discount deadline date.
* Invoice: fixed an error that happened when selecting a Partner.
* Payment session: fixed move generation on payment session when we use global accounting method.
* MRP: Improve proposals generation process performance.
* Supplychain: improved error management to avoid creating inconsistencies in database.
* Move template line: selecting a partner is now correctly filtered on non-contact partners.
* Price lists: in a sale order, correctly check if the price list is active before allowing it to be selected.
* Move: improve error messages at tax generation.
* Stock location line: updating the WAP from the stock location line will now correctly update the WAP on the product.
* Unify the sale orders and deliveries menu entries: now the menu entries at the top are the same as the menu entries at the side.
* BPM | DMN: Make it able to get model if modified and fix model change issue in DMN editor.
* Move: correctly retrieves the analytic distribution template when reversing a move.
* Advanced export: fix duplicate lines when exporting a large amount of data.
* Production batch: fixed running 'Work in progress valuation' batch process from the form view.
* Accounting Batch: fixed trading name field display.
* Account: fix invalid xml file in the source code.

## [6.5.0] (2022-02-14)

#### Features

* GDPR: add new module Axelor GDPR.
* Anonymizer : Add a new configurable setting to pseudonymize the data backup
* Faker API: Add link to a fake api in order to pseudonymize data

See faker api documentation : http://dius.github.io/java-faker/apidocs/index.html

* Fixed Asset : Manage negative values of asset values
* Period: New status 'closure in progress'
* Invoice: New configuration to display statements about the products in reports.

A new configuration is available in the accounting configuration by company, in the invoice tab. This configuration is available to display statements depending on the products category in the invoice reports.

* Invoice : New configuration to display the partner’s siren number on the invoice reports.

A new configuration is available in the accounting configuration by company, in the invoice tab.

* Forecast Recap Line Type: Two fields are added to filter forecast recap.

Forecast Recap could be filtered by functional origin and/or journals using the new fields journalSet and functionalOriginSelect from the Forecast Recap Line Type.

* Accounting report type : improve custom report types

  - Revamp report values to be computed in Java.
  - Add data cube in report and allow to create dynamic columns
  - Add percentage type column
  - Add group type column
  - Add custom period comparison
  - Add previous year computation

* Fixed asset line and move: Align depreciation dates for generated account moves with the end of period ( fiscal year or month) dates based on the depreciation plan instead of the depreciation date.
* Fixed asset: specific behavior for Depreciated/Transferred imported records
* Accounting batch and report: The general ledger may be filtered by journal and the report date is fixed.
* Analytic move lines: Analytic move lines are automatically linked to the order lines or contract lines on invoice generation.
* Bank reconciliation: The analytic distribution of the account is filled on the moves lines during the generation of the moves, if the account has analytic distribution required on move lines
* Bank reconciliation: Add dynamic display of selection balance for bank reconciliation lines and unreconciled move lines.
* Bank reconciliation: Add a boolean in account settings to fill move line cut off periods at move generation
* Bank reconciliation: Bank statement rule new specific tax field, or account default tax is applied on move lines during the move generation.
* PFP: New view for awaiting PFP in accounting menu entry
A new configuration is also added in order to take into account whether or not the daybook moves invoice terms in the new view.
* Accounting batch: The "Close/Open the annual accounts" batch may exclude specials and commitment accounts from the general ledger if "Include special accounts" is unchecked.
* Fiscal position : Prevent the creation of multiple account equivalences with the same account to replace
* Pricing scale: Versioning

Added a pricing scale history, it is now possible to historise the current pricing scale to change the values taken into account in the pricing scale lines and keep the current values in memory. It is also possible to revert to a previous version of a scale. A tracker has also been added in the scale rules to follow the evolution of the calculation rule which is not historised with the scale.

* Sale Order: Lines with a zero cost or sale price may be taken into account for the margin calculation.

New configuration is added to the sale’s app to activate this feature.

* WAP: New configuration to prevent significant value changes.

A new configuration “Tolerance on WAP changes (%).” is available in the stock configuration by company. This configuration allows you to define a tolerance limit when calculating the WAP. If the change in the WAP exceeds the defined percentage, the user will be alerted.

* Manufacturing order: Display producible quantity

The producible quantity is displayed on manufacturing order view in order to inform the user how much quantity he can produce with his current stock and his selected bill of materials.

 * CRM: added customization of lead and opportunity status, lead, prospect and opportunity scoring, and modified third party and lead forms.

* CRM: New features

  - Opportunity: The opportunity type became a string field
  - Opportunity: Allow to create opportunity from convert lead
  - Opportunity: Manage recurring
  - Opportunity: New gesture of status

* Removed static selection field salesStageSelect and replaced it with M2O field opportunityStatus
* Lead: New object LeadStatus to create your own status
* CRM Reporting : add new object 'CRM reporting'
* Create new object 'Agency'.
* Add agency field in User, Partner and Lead.
* Create CrmReporting object for filter lead/partner.
* Lead and Partner: Remove partner type from partner form and change process for next scheduled event on lead and partner form.

#### Changes

* WAP History: WAP history is now deprecated and has been replaced with Stock location line history
* Fixed Asset Category: Rename fields in fixed assets category to fit with display labels

provisionTangibleFixedAssetAccount -> provisionFixedAssetAccount

appProvisionTangibleFixedAssetAccount -> appProvisionFixedAssetAccount

wbProvisionTangibleFixedAssetAccount -> wbProvisionFixedAssetAccount


* Journal: Make journal type mandatory on journal
* Role: Add two new panel tabs to view the linked users and groups
* Sale and Purchase Order: The default email template and language are now selected according to the partner language.
* Invoice: displaying delivery address on invoice report is now enable by default
* Accounting Report: The report reference is now displayed on the report print.
* Accounting report: Add global totals in the general and partner general ledger.
* Accounting report type: Split the accounting report type menu entry in two (export and report)
* Supplier catalog: Improve the creation of supplier catalog.

The supplier catalog price is set by default with the purchase price of the product and its minimum quantity is set by default to 1.
The supplier catalog price fills the purchase price of the product if “Take product purchase price” is checked.

* Move template: Moves generated from move template are still created even if their accounting is in error

When we try to create moves with some move templates and we want to account for them in the same process, if the created moves can't be accounted for, they should still be created.
All the exceptions will be displayed in a single info message at the end of the process.

* Leave request: Company is now required in leave request.
* Move Line: Add new fields to in order to fix some information

New fields : companyCode, companyName, journalCode, journalName, currencyCode, companyCurrencyCode, fiscalYearCode, accountingDate, adjustingMove

* Manufacturing order: Links to BoM, production process and product are now open in a new tab
* Production Process: renamed subcontractors to subcontractor
* Account: Alert the user when some configurations are done on the account.

The user is alerted if he unchecks analytic and tax settings and some already existing moves use these configurations.

The user is alerted if he checks the reconcile option. He also has the possibility to reconcile move lines from a specific date when he activates this option.

* Contract batch : Display generated invoices and treated contracts for invoicing contract batch
* Contract batch : Display contracts that can be managed for invoicing contract batch mandatory field not underlined in red on contracts
* Contract: Re-invoicing contract after invoice cancellation
* Project module: improved usability and bug fixes.
* Project: Minor changes on project task, project phase, parent, child, project and timesheet
* Project: Add multiple lines of planification time
* Project: Task editor in project-form
* Project: improve planning panel in a project-form
* Project: Remove unused fields

Remove fields: totalSaleOrdersFinalized, totalSaleOrdersConfirmed, totalSaleOrdersInvoiced

* HR: New relational field with project on the HR timesheet editor

#### Fixed

* Maintenance order: Create new maintenance-manuf-order-form, maintenance-bill-of-material-form, maintenance-prod-process-form, maintenance-machine-form views and menus in order to dissociate views between manufacturing and maintenance modules.
* Manufacturing order: Remove actions and views linked to the maintenance process.
* Partner address: address is now required in the database.

This change does not mean that an address is required for a partner, but that we cannot add "empty" address lines in the partner anymore.

* Day planning: name field changed to nameSelect
* Interco/PurchaseOrder/SaleOrder: Changed some fields name and linked them in the interco process:

In Sale order, rename wrongly named column deliveryDate to estimatedShippingDate to match its title. We then added a new field estimatedDeliveryDate.

In Purchase Order, we changed deliveryDate name to estimatedReceiptDate.

We now correctly link in the interco process estimatedReceiptDate and estimatedDeliveryDate.

In Sale order line, rename wrongly named column estimatedDelivDate to estimatedShippingDate and add new field estimatedDeliveryDate. We also renamed desiredDelivDate to desiredDeliveryDate.

In Purchase Order line, change desiredDelivDate to desiredReceiptDate and change estimatedDelivDate to estimatedReceiptDate.

We now correctly link in the interco process estimatedReceiptDate and estimatedDeliveryDate for sale/purchase orders and their lines.

* Accounting Report: All reports will now filter the moves with the original currency and not the company currency

Also the currency field is moved to the filter tab and its title is renamed to "Original currency"

* Sale order and Purchase order: Rename dummy fields of invoicing wizard forms in order to follow standard guidelines

* CRM : Improvements

CRM: Review of some views: partner-grid, contact-form, lead-form-view, lead-form, lead-grid-view, lead-grid, partner-form

Opportunity Status: add label-help on some opportunities status in form

* Catalog: Add a configuration in CRM app configuration to display catalogs menu.
* Opportunity : corrected missing sales proposition status update in opportunity by adding the status in the crm app

#### Removes

* Sale Order: Remove event and email tabs from the sale order view
* Reported balance line: delete object, views and process
* Opportunity : Remove payingDate field
* Opportunity : Remove lead field
* CRM : remove Target and TargetConfiguration from CRM

[6.5.39]: https://github.com/axelor/axelor-open-suite/compare/v6.5.38...v6.5.39
[6.5.38]: https://github.com/axelor/axelor-open-suite/compare/v6.5.37...v6.5.38
[6.5.37]: https://github.com/axelor/axelor-open-suite/compare/v6.5.36...v6.5.37
[6.5.36]: https://github.com/axelor/axelor-open-suite/compare/v6.5.35...v6.5.36
[6.5.35]: https://github.com/axelor/axelor-open-suite/compare/v6.5.34...v6.5.35
[6.5.34]: https://github.com/axelor/axelor-open-suite/compare/v6.5.33...v6.5.34
[6.5.33]: https://github.com/axelor/axelor-open-suite/compare/v6.5.32...v6.5.33
[6.5.32]: https://github.com/axelor/axelor-open-suite/compare/v6.5.31...v6.5.32
[6.5.31]: https://github.com/axelor/axelor-open-suite/compare/v6.5.30...v6.5.31
[6.5.30]: https://github.com/axelor/axelor-open-suite/compare/v6.5.29...v6.5.30
[6.5.29]: https://github.com/axelor/axelor-open-suite/compare/v6.5.28...v6.5.29
[6.5.28]: https://github.com/axelor/axelor-open-suite/compare/v6.5.27...v6.5.28
[6.5.27]: https://github.com/axelor/axelor-open-suite/compare/v6.5.26...v6.5.27
[6.5.26]: https://github.com/axelor/axelor-open-suite/compare/v6.5.25...v6.5.26
[6.5.25]: https://github.com/axelor/axelor-open-suite/compare/v6.5.24...v6.5.25
[6.5.24]: https://github.com/axelor/axelor-open-suite/compare/v6.5.23...v6.5.24
[6.5.23]: https://github.com/axelor/axelor-open-suite/compare/v6.5.22...v6.5.23
[6.5.22]: https://github.com/axelor/axelor-open-suite/compare/v6.5.21...v6.5.22
[6.5.21]: https://github.com/axelor/axelor-open-suite/compare/v6.5.20...v6.5.21
[6.5.20]: https://github.com/axelor/axelor-open-suite/compare/v6.5.19...v6.5.20
[6.5.19]: https://github.com/axelor/axelor-open-suite/compare/v6.5.18...v6.5.19
[6.5.18]: https://github.com/axelor/axelor-open-suite/compare/v6.5.17...v6.5.18
[6.5.17]: https://github.com/axelor/axelor-open-suite/compare/v6.5.16...v6.5.17
[6.5.16]: https://github.com/axelor/axelor-open-suite/compare/v6.5.15...v6.5.16
[6.5.15]: https://github.com/axelor/axelor-open-suite/compare/v6.5.14...v6.5.15
[6.5.14]: https://github.com/axelor/axelor-open-suite/compare/v6.5.13...v6.5.14
[6.5.13]: https://github.com/axelor/axelor-open-suite/compare/v6.5.12...v6.5.13
[6.5.12]: https://github.com/axelor/axelor-open-suite/compare/v6.5.11...v6.5.12
[6.5.11]: https://github.com/axelor/axelor-open-suite/compare/v6.5.10...v6.5.11
[6.5.10]: https://github.com/axelor/axelor-open-suite/compare/v6.5.9...v6.5.10
[6.5.9]: https://github.com/axelor/axelor-open-suite/compare/v6.5.8...v6.5.9
[6.5.8]: https://github.com/axelor/axelor-open-suite/compare/v6.5.7...v6.5.8
[6.5.7]: https://github.com/axelor/axelor-open-suite/compare/v6.5.6...v6.5.7
[6.5.6]: https://github.com/axelor/axelor-open-suite/compare/v6.5.5...v6.5.6
[6.5.5]: https://github.com/axelor/axelor-open-suite/compare/v6.5.4...v6.5.5
[6.5.4]: https://github.com/axelor/axelor-open-suite/compare/v6.5.3...v6.5.4
[6.5.3]: https://github.com/axelor/axelor-open-suite/compare/v6.5.2...v6.5.3
[6.5.2]: https://github.com/axelor/axelor-open-suite/compare/v6.5.1...v6.5.2
[6.5.1]: https://github.com/axelor/axelor-open-suite/compare/v6.5.0...v6.5.1
[6.5.0]: https://github.com/axelor/axelor-open-suite/compare/v6.4.6...v6.5.0
