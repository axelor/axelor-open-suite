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

[7.2.2]: https://github.com/axelor/axelor-open-suite/compare/v7.2.1...v7.2.2
[7.2.1]: https://github.com/axelor/axelor-open-suite/compare/v7.2.0...v7.2.1
[7.2.0]: https://github.com/axelor/axelor-open-suite/compare/v7.1.7...v7.2.0
