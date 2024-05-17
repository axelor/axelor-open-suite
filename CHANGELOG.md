## [7.1.21] (2024-05-03)

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

## [7.1.20] (2024-04-19)

### Fixes
#### Account

* Payment voucher: fixed required message at on new and fixed invoice refresh at confirm.
* Invoice: fixed report when invoice is linked to more than one stock move.
* Accounting report: fixed 'Fees declaration supporting file' report displaying records that should not appear.
* Financial Discount: fixed french translations for 'discount'.

#### Human Resource

* Expense: fixed expense accounting moves generation when expense line dates are different and tax amount is zero.

#### Sale

* Sale order: removed french translation from english file.

#### Supply Chain

* Mass stock move invoicing: fixed an issue where invoiced partners were not used to invoice stock moves, the other partner was used instead.
* Mass stock move invoicing: fixed an issue preventing to invoice customer returns.

## [7.1.19] (2024-04-04)

### Fixes
#### Account

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

## [7.1.18] (2024-03-21)

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
* Reconcile manager: fixed move lines selection.
* Accounting batch: fixed currency amounts on result moves in opening/closure.
* FEC Export: fixed technical error when journal is missing.

#### Budget

* Purchase order line: fixed wrong budget distribution when invoicing multiple purchase order lines.

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

* Inventory: fixed type in inventory demo data.

#### Supplier Management

* Supplier request: fixed 'JNPE' error on partner selection in Supplier request form.

## [7.1.17] (2024-03-07)

### Fixes

* The format of this file has been updated: the fixes are now separated by modules to improve readability, and a new `developer` section was added with technical information that could be useful for developers working on modules on top of Axelor Open Suite.

#### Account

* Account clearance: fixed issue when opening a generated move line from account clearance.
* Invoice payment: added missing french translation for error message.
* Period: fixed an issue when checking user permission where the roles in user's group were not checked.
* Reconcile: fixed passed for payment check at reconcile confirmation.
* Reconcile: fixed selected amount with negative values.
* Move: fixed missing label in accounting move printing.

#### CRM

* CRM App: fixed small typos in english titles.
* Lead: fixed anomaly which was forcing a lead to have a company.

#### Human Resource

* Payroll preparation: fixed 'the exported file is not found' error on payroll generation when the export dir is different from upload dir.

#### Production

* Sale order: fixed a NPE that occured at the manuf order generation.
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

- in `lead-form-view`, removed `required` from `enterpriseName`

## [7.1.16] (2024-02-22)

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
* Invoice: fixed display of delivery address on advance payment invoices generated from a sale order.
* Budget: remove all dependencies with other modules when the app budget is disabled.
* Computing amounts in employee bonus management now alert user if employee does not have a birth date or seniority date.
* Project: fixed opening gantt view per user.
* Accounting report: set readonly export button and add report type check to provide critical error.
* Operation order: finishing a manuf order from the operations correctly computes the cost sheet quantities.
* Sale order: fixed technical error preventing pack creation.
* Contract: reset sequence when duplicating contracts.
* Reconciliation: fixed invoice term imputation when PFP not validated.
* Manufacturing order: finishing a manufacturing order now correctly updates the cost price of a product.
* Stock move: fixed error when spliting in two a stock move.
* Inventory line: forbid negative values in inventories.
* Accounting export: fixed FEC export not taking journal into account.

## [7.1.15] (2024-02-01)

#### Fixed

* Stock: added quality control default stock location to stock config demo data to avoid exception case.
* Accounting batch: fixed anomaly generated when running the closing/opening accounts batch with option simulate generated moves option ticked.
* Project task: fixed the ticket task form views.
* Expense line: fixed an UI issue where some fields were not required in the form view of kilometric expense line.
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
* Partner: make the 'Default partner status' field required only when the 'Manage status on prospect' option is checked.
* Leave request: user in not required in an employee anymore to increase leave from batch.
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
* Birt template parameter: fixed a small UI issue by ordering of the list of parameters.
* Move: batch control move consistency exception management.
* Expense line: compute total amount and distance depending on kilometric type.

## [7.1.14] (2024-01-12)

#### Fixed

* Hotfix: add missing binding preventing server startup.

## [7.1.13] (2024-01-11)

#### Fixed

* Prospect: open grid view instead of kanban view by default.
* Product: make the product unit field required for stock managed items.
* Budget: fixed amount paid computation on move and invoices reconcile.
* Reconcile: fixed invoice term amount remaining with multi currency.
* Invoice/Move/Budget: manage the negative imputation when the invoice is a refund or an advance or when the move is a reverse move.
* Stock move: fixed unable to select external stock location on receipt.
* Invoice: Duplicating an invoice now correctly empties the origin date and the supplier invoice number.
* Invoice: fixed error when we change partner in supplier invoice with invoice lines.
* Account clearance: fixed the way tax and vat system are managed.
* Indicators: fixed results display.
* Sale order: keep attached files and internal notes when merging sale orders.

## [7.1.12] (2023-12-21)

#### Fixed

* Stock location: Issue on Edition of Location financial data report
* Wrong quote sequence number on finalize
* Invoice : Fix partner account related errors consistency when validating and ventilating
* Invoice : Fix invoice term sum check not being done
* [Ticket]: Fix Ticket timer buttons
* INVOICE : currency not updated on first partner onChange
* Move Line : Prevent from updating tax line when the move is accounted
* Custom accounting report : Disable group by account feature as it cannot be fixed until 8.1
* GDPR: Fix response email couldn't be changed.
* Custom accounting report : Fix an issue with custom rule priority
* MAINTENANCE ORDER : fix NPE when we try to plan
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
* PurchaseOrderLine : rework budget override
* MOVE TEMPLATE : Demo data - missing partner
* UNITCOSTCALCULATION : Irrelevant message when no product has been found
* Account clearance : Fix an issue where move lines could be created or edited there
* Reconcile : Fix an issue where a payment was created for a reconcile with an account not being used for partner balance
* Sale order : Fix technical error linked to budget management
* Analytic Move Line : Fix type select when we create a new analytic move line from orders, invoices, contract and moves

## [7.1.11] (2023-12-07)

#### Fixed

* Budget: added currency symbol for the amount of budget distribution field.
* Add a check on saleOrderLineList in SaleOrderProjectRepository.copy to remove the NPE.
* Purchase request: fixed reference to purchase order being copied on duplication.
* Opportunity: fixed "create Call" button so it correctly sets the type of event to Call.
* Studio: fixed wrong french translation for "Order By".
* Accounting batch: hide unnecessary payment mode information.
* MRP: fixed invalid type error when selecting a product from (sale/purchase) order line.
* Sale order: fixed wrong price update when generating quotation from template.
* Move line: hid cut off information when functional origin is 'opening', 'closure' or 'cut off'.
* Custom accounting report: fixed detailed line with fetching accounts from their code displaying the same value.
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
* FEC Import: fixed the way the currency amount is imported to make sure it is correctly signed.
* Account: forbid to select the account itself as parent and its child accounts.
* Mobile Settings: added a placeholder on every authorized roles.
* Bank order: highlight orders sent to bank but not realized.
* Payment session: fixed display of currency symbol in payment session.
* Move template line: hide and set required tax field when it is configured in financial account.
* Inventory line: fixed update of inventory line by taking into account stockLocation of line when possible, triggering update when necessary.
* Invoice: fixed partially paid invoices in bill of exchange management.
* Stock move: allow to select external stock location for deliveries.

## [7.1.10] (2023-11-23)

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
* Move mass entry: fixed line duplication at counterpart generation for a move generated by the mass entry process.
* SOP: added missing french translation.
* Period: fixed errors not being displayed correctly during the closure process.
* Move: fixed error when we set description on move without move lines.
* Payment session: fixed issue preventing from validating a session.
* SOP line: fixed an issue where SOP process does not fetch the product price from the product per company configuration.
* Contract: fixed prorata computation when invoicing a contract and end date contract version

When creating a new version of a contract, the end date of the previous version
is now the activation date of the new version minus one day.
If left empty, it will be today's date minus one day.

* Bank order: do not copy "has been sent to bank" on duplication.

## [7.1.9] (2023-11-09)

#### Fixed

* App builder: update studio dependency to 1.2.6 to get the following fix:

    - fixed NPE upon save of a custom model with a menu

* Sale order: fixed "NullPointerException" error when selecting a project.
* App Base: set company-specific product fields domain.
* Sale order / Invoice: fixed an issue when invoicing a sale order with multi currency & multi invoice terms.
* Accounting report: fixed an issue in Journal report (11) where debit and credit were not displayed in the recap by account table.
* Accounting dashboard: removed blank panel in "accounting details with invoice terms" dashboard.
* Move line: fixed error when we create a move line in multi currency.
* Move line: fixed error at first move line creation related to counter initialization.
* Move: fixed an error when we update invoice terms in move line.
* Move: fixed an error when we reconcile move lines without tax line.
* Move: hide company without account config on company list when we change move company.
* Move: added journal verification when we check duplicate origins.
* Fixed asset: fixed an issue after selecting a category where set depreciations for economic and ifrs were not computed correctly.
* Fixed asset: hide "isEqualToFiscalDepreciation" field when fiscal plan not selected.
* CRM: opening an event from custom view in prospect, leads or opportunity is now editable.
* Custom accounting report: fixed legacy report option being displayed for all account report types.
* HR Timesheet: fixed conversion problems while generating timesheet lines from project planning time.
* Project: fixed wrong domain on opening project report.
* Cost sheet: fixed issue in the order of calculation on bill of materials.
* Configurator BOM: Fixed a concurrent error when generating a bill of materials from the same configurator.
* Employee: to fix bank details error, moved the field to main employment contract panel.
* FEC Import: prevent potential errors when using demo data config file
* Contract: fixed "NullPointerException" error when emptying the product on a contract line.
* Manufacturing order: company is now required in the form view.
* Bank reconciliation: selectable move lines are now based on the currency amount.
* Sale order (Quotation): fixed "End of validity date" computation on copy.
* Debt recovery: fixed balance due in debt recovery accounting batch.
* Analytic: fixed display of analytic panel when it is not managed by account in sale order and purchase order.
* Stock: fixed stock dashboard.
* Lead: prevent the user from editing the postal code when a city is filled to avoid inconsistencies.
* Invoice term: fixed amount computation with multi currency.
* Contract: prorata is now correctly disabled when the config is off.
* CRM Event: when an event is created from CRM, correctly prefill "related to".
* Opportunity: company is now required in the form view.
* Sale: hide 'Timetable templates' entry menu on config.
* Maintenance: reset the status when duplicating a maintenance request.

## [7.1.8] (2023-10-27)

#### Fixed

* App builder: update studio dependency to 1.2.5 to get the following fix:

    - fixed StudioActionView duplication on every save

* Timesheet: fixed error when generating lines from expected planning.
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
* Fixed asset: fixed popup error "Cannot get property 'fixedAssetType' on null object" displayed when clearing fixed asset category field.
* Cost sheet: replaced 'NullPointerException' error by a correct message when an applicable bill of materials is not found on cost calculation.
* axelor-config.properties: enabled Modern theme by default.

## [7.1.7] (2023-10-18)

#### Fixed

* Update App Builder dependency to 1.2.4

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

* PFP: added verification if 'Passed For Payment' is enabled in company configuration and application configuration.
* Product: fixed stock indicators computation.
* Invoice: fixed an error while paying a customer invoice with holdback.
* Inventory: fixed error displayed when trying to open an inventory line.
* Payment voucher: fixed threshold payment when invoice term percentages have been customized.
* Project: fixed french translation for 'Create business project from this template'.
* Sale order: fixed error preventing sale order confirmation.
* Payment session: set currency field readonly on payment session.
* Period: improved period closure process from year form.
* Sale order: fixed duplicated 'Advance Payment' panel.
* ProdProcess Line: fixed a bug where work center durations were used instead of prod process line.
* Purchase request: fixed 'Group by product' option not working when generating a purchase order.
* Sequence: fixed sequence duplication causing a NPE error.
* Move: fixed budget exceed warning at accounting.
* Move: fixed an error when we create a move line with a partner without any accounting situation.
* Move: fixed error displayed when trying to open an invoice term.
* Advance payment invoice: fixed error preventing the payment of an advance payment invoice.
* SOP/MPS: fixed a bug where an existing forecast was modified instead of creating a new one when the forecast was on a different date.

## [7.1.6] (2023-10-06)

#### Fixed

* Production demo data: manage production order on sale order per default in demo data.
* Period: set period status at open if closure fails.
* Move template: fixed description not being filled on moves generated from a percentage based template.
* Reconcile: fixed amount error triggering on unwanted cases.
* Interco invoice: fixed default bank details on invoice generated by interco.
* Invoice term: on removal, improved error message if invoice term is still linked to a object.
* Sale order line: fixed error when emptying product if the 'check stock' feature is enabled for sale orders.
* Sale order line: fixed an issue where updating quantity on a line did not updated the quantity of complementary products.
* Invoice term: fixed wrong amount computation at invoice term generation with hold back.
* Move: fixed due dates not being recomputed on date change.
* Move: fixed counterpart generation with multi currency.
* Expense demo data: added missing currency on expense loaded from demo data.
* Stock move: filled correct default stock locations on creating a new stock move.
* Stock move: fixed wrong french translation on stock move creation and cancelation.
* Invoice: fixed default invoice birt template configuration by adding report type parameter.
* User: encrypt password when importing user data-init.
* Print template: iText dependency has been replaced by Boxable and PDFBox.
* Cost calculation: fixed the import of cost price calculation.
* Payment session: fixed validation when there is compensation.
* HR: fixed an issue preventing people from creating a required new training.
* Task: improved performance of the batch used to update project tasks.
* Mail template: fixed error when importing demo data.
* Sale order: fixed an error occurring when generating analytic move line of complementary product.
* Invoicing dashboard: fixed error on turnover per month charts.
* Move: fixed reverse charge tax computation.
* Debt recovery: fixed missing letter template error message.
* Project task: fixed blocking error when computing project totals when timeUnit is empty.
* Invoice payment: fixed move line amount error when we generate a payment with financial discount.
* GDPR request: changed UI for selecting search results.
* Project: fixed tracebacks not showing on project totals batch anomalies.
* Reconcile: fixed multiple move lines reconciliation.
* Move: fixed move lines consolidation.
* Irrecoverable: fixed tax computing.
* Move: fixed multi invoice term due dates being wrongly updated when accounting.
* Debt recovery batch: reduced the execution time.
* Bank reconciliation: fixed balance not considering move status.

## [7.1.5] (2023-09-21)

#### Fixed

* Prod process line: removed timing of implementation that was left in from view.
* Follower: fixed an error occuring when sending a message while adding a follower on any form.
* Invoice payment: when validating a invoice payment from a bank order, the payment date will now be correctly updated to bank order date.
* Move: fixed period permission error when changing the date.
* Configurator creator: complete demo data to include admin-fr in authorized users.
* Manufacturing order: fixed purchase order generation with unit name
* Product: "Economic manufacturing quantity" is now correctly hidden on components.
* Reconcile: fixed effective date computation when we generate payment move from payment voucher.
* Leave line: deleting every leave management now correctly computes remaining and acquired value.
* Mass entry: fixed date application on other move line mass entry.
* Invoice: fixed french translation for 'Advance payment invoice'.
* Manufacturing order: fixed 'No calculation' of production indicators on planned Manufacturing Order.
* Advanced export: change PDF generation.
* Accounting: disable financial discount from the application to prevent issues from its instability.
* Operation Order: On planning, fixed a bug where an operation order could only start at the same time as others with same priority.
* Production API: finishing or pausing every operation orders will change the parent manuf order status.
* Move: improved tax amount check at move accounting, adding debt and immobilisation account type lines to check.
* Debt recovery: fixed missing email reminder template messages for debt recovery method lines demo data.
* Stock location: fixed new average price computation in the case of a unit conversion.
* Manufacturing order: when pausing an operation order, the manufacturing order will be paused if there is no "In progress" operation order
* Move: fixed currency amounts not being signed after date change.
* Company: company data is now prefilled when creating a bank details from company form view.
* Invoice/AnalyticMoveLine : Fix error at percentage computation
* Move template: fixed invoice terms not being created on a move template generation by amount.
* Move: fixed critical error when manual misc operation journal is empty and PFP config is enabled.
* Sale/Purchase order line: fixed wrong error being displayed when emptying the product.
* Stock correction: add demo data for Stock correction reason.
* Operation order: fixed printing showing a blank page at the end.
* Prod process line configurator: fixed human duration missing on prod process lines generated by a configurator.

## [7.1.4] (2023-09-11)

#### Features

* API: improve response when creating object

#### Fixed

* App builder: update axelor-studio dependency from 1.2.1 to 1.2.3.
* API Stock: fixed an issue when creating a stock move line where the quantity was not flagged as modified manually.
* Account: added check for depreciation account in fixed asset category on fixed asset disposal.
* Purchase order: fixed 'NullPointerException' error happening when a line without a product was created.
* Fixed asset: fixed never ending depreciation lines generation if there is a gap of more than one year.
* Reconcile: fixed payment adjustment when the last invoice term paid is not the one with the latest date.
* Product: on product creation, fills the currency with the currency from the company of the logged user.
* Invoice: fixed wrong translations on ventilate error.
* Manufacturing API: improved behaviour on operation order status change.
* HR Dashboard: fixed expense dashboards.
* Account: fixed an issue preventing the user from deleting accounts that have compatible accounts.
* Move template: fixed filters on accounting accounts in move template lines.
* Contract line: added a warning when the user selects a product with a time unit.
* Operation order: fixed workflow issues

Resuming an operation order in a grid or a form has now the same process and will not affect other orders.
When pausing an operation order, if every other order is also in stand by, the manuf order will also be paused.

* Invoice line: set analytic accounting panel to readonly.
* Account chart: updated accounting config files to improve accounting import.
* Payment session: fixed the account at payment on payment session using compensation invoice/moves.
* Project: fixed error when opening the default dashboard view.
* Contract: hide revaluation related panel and fields when periodic invoicing is disabled.
* Sale/Purchase order and stock move: fixed wrong filters when selecting stock locations, the filters did not correctly followed the stock location configuration.
* Stock move: fixed 'NullPointerException' error when emptying product on stock move line.
* Payment session: fixed generated payment move lines on a partner balance account not having an invoice term.
* Manufacturing order: fixed an issue where some planning processes were not executed.
* Move template: fixed copied move template being valid.
* Invoice term: fixed wrong amount in invoice term generation.
* Journal: balance tag is now correctly computed.
* Contract: fixed 'NullPointerException' error when invoicing a contract.
* Manufacturing order: when generating a multi level manufacturing order, correctly fills the partner.

## [7.1.3] (2023-08-24)

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
* Move: fixed tax check error on daybook when taxes are compensated.
* Contract: fixed NullPointerException when invoicing a contract.
* Sale order: fixed a bug where the project was not filled in lines in sale order generated from a project.
* Stock details: in stock details by product, fixed production indicators visibility.
* Business project batch: fixed "id to load is required for loading" error when generating invoicing projects.
* Move/Invoice: added a verification of the sum of the percentages by analytic axis when we account a move.
* Period: fixed adjusting button not being displayed when adjusting the year.
* Fixed asset: fixed wrong depreciation value for degressive method.
* Move / Mass entry: fixed error when emptying the date on a move generated from mass entry.
* Payment session: filter invoice terms from accounted or daybook moves.
* Employee: fixed employees having their status set to 'active' while their hire and leave date were empty.
* Bank reconciliation: merge same bank statement lines to fix wrong ongoing balance due to auto reconcile.
* Invoice / Payment voucher: fixed generated adjusting moves with multi currency.
* Accounting report: dates on invoice reports are now required to prevent an error generating an empty report.
* Invoice: fixed anomaly causing payment not being generated

Correct anomaly causing payment not being generated on invoice when a new reconciliation is validated  
and the invoice's move has been reconciled with a shift to another account (LCR excluding Doubtful process).

* Import Batch: added missing translation.
* Move: The date is now correctly required in the form view.
* Payment voucher / Invoice payment: fixed generated payment move payment condition.
* Payment voucher: fixed excess payment.
* Accounting report: fixed payment differences report generation.
* Invoice payment: add missing translation for field "total amount with financial discount".
* Invoice: fixed financial discount deadline date computation.
* Sale order line: fixed a bug where the link to the project was not emptied on copy.
* Mass entry: fixed date update on move lines with the same move identification.
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

* Accounting config: fixed default bank details errors message.
* Invoice term: fixed negative amount on manual creation.
* CRM search: hide some panels to avoid NPE error.
* Mass entry: fixed error when we control duplicate origins in a journal where the origin is not required.
* Stock API: validating a Stock Correction with a real quantity equal to the database quantity now correctly throws an error.
* Manufacturing order: fixed a bug where sale order set was emptied on change of client partner and any change on saleOrderSet filled the partner.
* Forecast line: fixed company bank details filter.
* Contact: the filter on mainPartner now allows to select a company partner only, not an individual.
* Move: fixed due date issue when changing from single to multi invoice term payment condition.
* Stock move line: improve performance when selecting a tracking number if there are a large number of stock location lines.

## [7.1.2] (2023-08-08)

#### Fixed

* Invoice : fix the way we check the awaiting payment
* Accounting batch : Improve user feedback on move consistency control when there are no anomalies
* PLANNING: Planning is now correctly filtered on employee and machine form
* SaleOrderLine: Description is now copied only if the configuration allows it
* PARTNER: Fixed a bug where button 'create sale quotation' was always displayed
* Custom accounting report : Excel sheets are now named after the analytic account
* Move : Fix automatic move line tax generation with reverse charge and multiple vat systems.
* Stock move: reversed stock move now correctly reverses stock locations
* Mass entry : Fix fill of payment condition when journal isn't type 'other'
* Move : Fix invoice terms amount at percentage change with negative currency amount
* PurchaseOrder and Invoice: Added widget boolean switch for interco field 
* Employee dashboard: added condition on app
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
* MOVE/INVOICE/ORDERS : Compute budget distribution when object are not saved
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

The following script can be executed to clean old actions
```sql
DELETE FROM meta_action
WHERE name IN (
  'action-invoice-term-method-compute-total-payment-session',
  'action-payment-session-record-set-session-total-amount-btn',
  'action-method-payment-session-compute-total'
);
```

* Move : Fix invoice term amount at percentage change with unsaved move
* Project: fix real costs computing in tasks and subtasks
* Accounting report: Fixed a bug where unreconcilied move lines were not displayed
* Product: When changing costTypeSelect to 'last purchase price', the cost price will now be correctly converted.
* Move : Set readonly move form when period is closed or doesn't exist
* Bank order: Fixed a bug where bank order date was always overridden. Now bank order date is overridden only when it is before the current date and the user is warned.
* PaymentSession : Fix compensation invoice terms amount when select using buttons
* BUSINESS PROJECT BATCH: Fixed invoicing project batch

## [7.1.1] (2023-07-20)

#### Fixed

* App builder: updated axelor-studio dependency from 1.2.0 to 1.2.1.
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
* Payment session/Bank order: fix of compensation in payment session process with bank order.
* Sale order: fixed missing actions on loading sale order form views.
* Fixed asset: improved the error message shown when an exception occurs during a mass validation.
* Analytic distribution template: fixed error when creating a new analytic distribution template.
* Product: fixed wrong filter on analytic on product accounting panel.
* Supplychain batch: fixed an error occurring when invoicing outgoing stock moves without sale order.
* Faker API: update documentation in help message.
* Interco: fixed generated sale order/purchase order missing a fiscal position.

## [7.1.0] (2023-07-12)

### Features/Changes

#### Base

* Faker API Field: 
- Add the possibility to use the api faker method parameters
- Add data type to filter API fields in the anonymizer lines

* Base Batch: New batch to force users password change
* BirtTemplate: It is now possible to choose the .rptdesign file used to generate the attached file when sending a generated email. Parameters sent to the BIRT engine can also now be computed using groovy scripting.

#### Accounting

* Move: 
    - New mass entry feature
    - Add management of currency amount negative value for credit
    - Add PFP process like on Invoices
    - Extend automatic cut off dates process to move confirmation
    - On move creation or generation, originDate is filled with onChange of moveDate.
    - Add an allowed tax gap on company account config and check consistency of tax values on daybooking, simulating or accounting.
    - Add new technical origin for move generated from mass entry process

* Move, Move line: 

Cut off dates are no longer required for functional origin: closure, opening, cut off

* Cut off batches: Add three new columns displaying the number of days considered in the calculation for Cut-Off processing of Advance Accrued Expenses (CCA) and Advance Accrued Revenues (PCA), allowing users to verify the consistency of the days used in the calculation.
* Move, Move Template: Set partner as required when journal is sale or expense
* Reconcile: Add an effective date to the reconcile which will be used instead of reconciliation date for generated payment and moves. This effective date is computed from information in reconciled moves.
* Reconcile: reconciliation of different accounts and payment differences management
    - Added new fields in account config, one account for debit difference, one account for credit difference.
    - When you pay an invoice term and the remaining amount is less than the threshold distance, a new payment will be made of the remaining amount.
    - Reconciliation of different accounts are now managed, using a new payment move which reconciles both invoice terms.

* Custom accounting report: 
    - Fetch parent account when higher level analytic accounts are detailed
    - Add previous start balance
    - Can now filter result with accounts and analytic accounts from the report
    - Allow to display debit or credit values instead of only the difference between them

* Debt recovery: improve debt recovery and debt recovery history attachments management

* Deposit slip: handle bank details and deposit date in deposit slip.

* Payment session: 
    - Show buttons to select all / unselect all.
    - add compensation field on partner to compensate customer invoices on supplier payment or supplier invoices on customer payment.
    - add partner set field to filter partners linked to these invoice terms.

* Analytic distribution template: trading name now has an analytic distribution template field:

In Account configuration, if the Analytic distribution type is 'Per trading name', 
Analytic distribution template will be retrieved from the trading name when creating Purchase order, Sale order and invoice lines.

#### Budget

* Budget: integrate new budget module

#### Stock

* Stock location: Stock location can be now configured from the stock move line
* Stock Move: Canceling a stock move will now set the wap price to its former value if nothing happened since the realization.
* Stock moves: Change origin architecture from refSelect to M2O

#### Supply Chain
* Sale order: Sale orders and sale order lines can now be filtered with invoicing state.


#### Production

* Manufacturing order: 

When planning a manufacturing order, operation orders dates now take into account available dates of the machine. 

* Operation order: 

New fields to distinguish human and machine planned times on operations.
In the calendar view, the operation orders will now be updated (if needed) on change of another operation order.

* Work Center & Prod process Line : 

Moved starting, setup and ending duration from Machine to Work Center.
Human resources panel refactoring

* Machine : 

Machine form view refactoring

#### CRM

* CRM: Redesign of the CRM menus architecture and multiple menus domains.
* CRM Search: Added a powerful searching tool in multiple object type (opportunities, leads, event, etc …). Using keywords and configurable mapping. 
* Opportunities: 
Added a control on opportunities winning process that displays on-going opportunities for selected clients. With multiple closing functions. 
Add controls on decimal and integer values in opportunities for process maintainability.
* Partner, Leads, Opportunities: 
Refactor on last event and next event on partner, leads, opportunities (to make it work properly). 
Added back a drag and drop function on lead/opportunities/prospect status grid view.
* Lead conversion: Added a new conversion process for leads.
* Lead, Prospect: Added a Kanban view.
* Improve emails display in all CRM views.
* Added an automatic conversion function for similar leads (name, email, etc…) when converting one. 
* Added a catalog email sending function in catalog grid view. 
* Opportunity: added a panel in opportunity view displaying events related to the opportunity.
* Partner: added partner status to the app. This feature permits the user to follow a prospect using a custom process. It follows the leads process before converting the prospect to customer. 
* Partner: added related contacts in contact view (contacts with the same email address domain).


#### Project

* Project: Allowed deletion of planned times from the dashlet in Project.
* Project: Added Time follow-up and Financial follow-up on Tasks, aggregated on Project level.
* ProjectTask: Changed planning UX and constraints to fit with the new flow of project management.
* Project: Added a batch to compute project and task’s time and financial reporting. Action can also be triggered from Project’s Tools menu. Invoicing Project Batches were renamed to Business Project Batches
* Project: Removed obsolete time fields on Project and Tasks.
* Project: Added demo data for Business Project (Task by line & Task by line using subtasks models) to allow demonstration of the new reporting.
* Project: Added a batch to historize project reporting, as well as visual indicators to compare current values to last historized values.
* Business Project: Improved App configuration UX
* Project: Manage project time units (Hours, Days) and conversions to allow to manage a project in Hours or in Days, as well as to manage individual tasks in both units. Planning has also been changed to reflect these features.
* Contract: invoicing pro rata per line
* Contract: reevaluating contract yearly prices based on index values
* Contract: added discount on contract lines.

#### Sales

* Sale Order: Improve UX and help for project generation.
* Sale Order: Generating task with subtask models has been fixed and improved

#### Helpdesk

* Helpdesk: 

Added a new field fullName to the ticket which will be used as namecolumn
Add API to update ticket status

#### GDPR

* Processing register: option to not archive

In GDPR Processing Register, add a boolean to enable/disable archiving data during the data processing.

* App GDPR: New config to exclude fields from the access request.
* Erasure Request: Add the possibility to break links in an anonymised object.

#### HR

* New employee and manager dashboard with custom views
* Reorganize HR menu items and add new HR menu with a new dashboard
* Files :

Panel review
Linked to skills

* Lunch voucher: Process improvement (links with expenses, refacto, form review)
* Employee: 

New titles
Dependent children

* Recruitment: Application form review
* Expense:

Multi-currency expenses
Analytic distribution template on employee form
Add justication to expense line grid view

#### Cash management

* Forecast generator: add fortnightly and weekly periodicity in forecast generator.

#### Mobile

* Mobile Settings: add Helpdesk config for the mobile app

### Fixed

#### Sale/Purchase

* Sale and Purchase order: Change default view for historic menu, from card view to grid view

#### Stock

* Stock move: the field “filter on available products” is now stored in database.

#### Project

* Project and Task: Split ProjectStatus between ProjectStatus for project and TaskStatus for task.

#### Base/HR

* User, Employee: Improved behavior to avoid links errors and inconsistencies and for more coherence.
* Partner: change companyStr length
* Company: Add all java timezone in timezone selection in Company.
* Training skill: graduation date and end of validity date are not required anymore.

#### Helpdesk

* Ticket: Renamed the customer field into customerPartner

#### Accounting

* Accounting report analytic config line : Rule level maximum value is now the highest existing analytic level.
* Custom accounting report: fixed comparison period order.
* Move: origin date is now always filled in automatic creation.
* Invoice term: rework partial pfp validation in invoice terms to be more user friendly and fix financial discount in second invoice term generation.
* Journal type: field technicalTypeSelect is now compulsory.

### Removed

* App base configuration: remove OSM Routing API selection

Now, when computing distance for Kilometric Allowances, if using the Open Street Map mapping service provider, 
it will use the OSRM API by default.

* Leave management: remove unused 'user' column in data model.
* Alarms: removed everything related to alarms.
* CRM : Leads : Remove 'Import Leads' feature.
* [EXPENSE] Remove multi user expense.
* Object data config: removed unused domain object data config.
* WAP History: removed WAP history  (replaced by a new table “StockLocationLineHistory” with more information.
* Skills : experienceSkillSet and skillSet are now removed from employee form.
* Simplified moves: removed in favor of mass entry.


[7.1.21]: https://github.com/axelor/axelor-open-suite/compare/v7.1.20...v7.1.21
[7.1.20]: https://github.com/axelor/axelor-open-suite/compare/v7.1.19...v7.1.20
[7.1.19]: https://github.com/axelor/axelor-open-suite/compare/v7.1.18...v7.1.19
[7.1.18]: https://github.com/axelor/axelor-open-suite/compare/v7.1.17...v7.1.18
[7.1.17]: https://github.com/axelor/axelor-open-suite/compare/v7.1.16...v7.1.17
[7.1.16]: https://github.com/axelor/axelor-open-suite/compare/v7.1.15...v7.1.16
[7.1.15]: https://github.com/axelor/axelor-open-suite/compare/v7.1.14...v7.1.15
[7.1.14]: https://github.com/axelor/axelor-open-suite/compare/v7.1.13...v7.1.14
[7.1.13]: https://github.com/axelor/axelor-open-suite/compare/v7.1.12...v7.1.13
[7.1.12]: https://github.com/axelor/axelor-open-suite/compare/v7.1.11...v7.1.12
[7.1.11]: https://github.com/axelor/axelor-open-suite/compare/v7.1.10...v7.1.11
[7.1.10]: https://github.com/axelor/axelor-open-suite/compare/v7.1.9...v7.1.10
[7.1.9]: https://github.com/axelor/axelor-open-suite/compare/v7.1.8...v7.1.9
[7.1.8]: https://github.com/axelor/axelor-open-suite/compare/v7.1.7...v7.1.8
[7.1.7]: https://github.com/axelor/axelor-open-suite/compare/v7.1.6...v7.1.7
[7.1.6]: https://github.com/axelor/axelor-open-suite/compare/v7.1.5...v7.1.6
[7.1.5]: https://github.com/axelor/axelor-open-suite/compare/v7.1.4...v7.1.5
[7.1.4]: https://github.com/axelor/axelor-open-suite/compare/v7.1.3...v7.1.4
[7.1.3]: https://github.com/axelor/axelor-open-suite/compare/v7.1.2...v7.1.3
[7.1.2]: https://github.com/axelor/axelor-open-suite/compare/v7.1.1...v7.1.2
[7.1.1]: https://github.com/axelor/axelor-open-suite/compare/v7.1.0...v7.1.1
[7.1.0]: https://github.com/axelor/axelor-open-suite/compare/v7.0.5...v7.1.0
