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

[7.2.6]: https://github.com/axelor/axelor-open-suite/compare/v7.2.5...v7.2.6
[7.2.5]: https://github.com/axelor/axelor-open-suite/compare/v7.2.4...v7.2.5
[7.2.4]: https://github.com/axelor/axelor-open-suite/compare/v7.2.3...v7.2.4
[7.2.3]: https://github.com/axelor/axelor-open-suite/compare/v7.2.2...v7.2.3
[7.2.2]: https://github.com/axelor/axelor-open-suite/compare/v7.2.1...v7.2.2
[7.2.1]: https://github.com/axelor/axelor-open-suite/compare/v7.2.0...v7.2.1
[7.2.0]: https://github.com/axelor/axelor-open-suite/compare/v7.1.7...v7.2.0
