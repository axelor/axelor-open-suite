## [8.0.0] (2024-02-07)

### Features

#### Upgrade to Axelor Open Platform version 7.0

* Axelor Open Platform version 7 is a full rewrite of our front-end. The previous version was written on top of AngularJs, it is now on top of React.
* See corresponding [Migration guide](https://docs.axelor.com/adk/7.0/migrations/migration-7.0.html) for information on breaking changes.

#### Base

* Management of prices scale per currency: now currency with no scale or with a scale of 3 are managed.
* Pricings: the "pricing" feature which was available for sale order line, can now be used for any model.
* Address: a new configuration is now available for addresses, and is used to have different address format per country.
* Localization: a new configuration "Localization" was added to manage language and country information for the user and the company.
The resulting locale will be used for translation, date and currency formats.
* Translation: add a new import/export feature to update translations more easily on a running instance.

#### CRM

* Tour: add a new model to plan tour for salespersons.

#### Purchase

* Purchase order: improve purchase order merge process.

#### Sale

* Sale order line: add a new configuration to enable editable grid on sale order line.
* Sale order: improve sale order merge process.

#### Account

* Invoice: improve invoice merge process.
* Invoice: it is now possible to invoice multiple sale orders with a single invoice.
* Financial discount: reworked financial discount feature.

#### Budget

* Improve budget structure to allow as many as budget level as wanted.
* Improve budget validation so each section can be validated.
* Add a budget generator that will help to build budgets, preview the result and generate a budget with all sub levels.
* Add a new reporting for a budget scenario.
* Add the possibility to link projects with budgets.

#### Stock

* Stock move: a new stock move can be generated from a return.
* Stock move: a new button has been added to split lines into a fulfilled line and a line with the remaining quantity.
* Stock location: stock location valuation can now be retrieved at a given date in the report.

#### Supply Chain

* MRP: add a new delay on purchase and manufacturing order proposal.
* Stock move: merging stock moves is now possible.

#### Production

* Bill of materials: add a new menu allowing to import bill of materials.
* Subcontracting: the subconctracting feature was reworked.
* Manufacturing order: add a new configuration to complete stock moves from manufacturing order. Produced products quantities are retrieved on the stock move when the manufacturing order is done.

#### Quality

* Quality control: this new feature allows to define controls, make them periodically, and visualize the result.

#### HR

* Expense: manage expense lines without parent expense to see lines created from the mobile app.

#### Mobile Settings

* Dashboard: a new menu is added to allow to create dashboards available on the mobile application.

### Change

#### Base

* Sequence: add alphanumeric pattern for sequence, allowing to create sequence like this: `320BMF -> 320BMG -> ... -> 320BMZ -> 320BNA -> ...`
* Sequence: allow to configure prefix/suffix with a groovy script.
* Tax number: add a configuration allowing to enable or disable this feature.

#### CRM

* Opportunity: the type of opportunity is now a selection instead of a simple string.

#### Purchase

* Supplier catalog: manage maximum quantity.

#### Sale

* Configurator: add demo data.

#### Account

* Accounting move: improve form view.
* FEC Import: add a new button to retrieve a default FEC import configuration.
* Accounting configuration: improve import for company accounting configuration.
* Journal: display a debit/credit indicator.
* Doubtful customer: add a preview to see accounting moves to shift to doubtful.
* Accounting custom report: add a new config to to hide empty lines.
* Accounting custom report: line detail now works with accounting account codes.
* Accounting custom report: remove characters limitation for accounting account codes.
* Accounting custom report: removed unused fields.
* Fixed assets: technical change to prefix fields related to import with `import`.

#### Budget

* Improve budget distribution.
* Add a new option to update the budget automatically on order/invoices/moves validation.
* Global budget: do not show indicators while budget is not saved.

#### Bank Payment

* Bank order: add a new configuration to switch the lines display, editable or list.
* EBICS: removed fields related to EBICS T.

#### Business project

* Rework menus order.

#### Stock

* Stock location valuation: add an option to see the valuation details per sub stock location.
* Stock rules: stock rules are now computed using a batch to avoid performance issues when realizing large stock moves.
* Tracking number: improve wizard to split lines per tracking number on a manual stock move.
* Tracking number: on a product, it is now possible to manage tracking number per company.
* Tracking number: add supplier on tracking number.
* Tracking number: improve the tracking number wizard on stock move to allow tracking number selection.
* Incoterm: add a configuration to activate or deactivate.

#### Supply Chain

* Add a new "Timetables" menu entry to see timetable invoicing for sale orders.

#### Production

* ProductionConfig: the configurations per company form view was reworked.
* Work center: if configured, the work center group is now used for the planification.
* Work center: add a chart and a calendar view on work center group to visualize related work centers planning.
* Work center: improve employee management on work centers.
* Work center: improve production process line configuration.
* Employee: add a new calendar view for employee.
* Machine: add a chart to visualize machine planning.

#### Helpdesk

* Ticket: make ticket status configurable.
* Ticket: remove unused fields `mailSubject` and `ticketLead`.
* SLA: improve SLA feature.

#### HR

* Leave request: improve leave configuration.
* Timesheet: rework timer feature and add a new configuration to manage multiple timers in parallel.
* Add a new API to create an expense.

#### Mobile Settings

* Stock: add new configurations to select which roles have access to which feature on the mobile application.
* Stock: add a new configuration to disable stock location management on the mobile application.
* Authentication: add a new API to fetch user permissions.
* HR: add new configuration to manage timesheets from the mobile application.

[8.0.0]: https://github.com/axelor/axelor-open-suite/compare/v7.2.7...v8.0.0
