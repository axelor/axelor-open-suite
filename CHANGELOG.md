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

[7.2.0]: https://github.com/axelor/axelor-open-suite/compare/v7.1.7...v7.2.0
