## [8.2.5] (2024-12-20)

### Fixes
#### Account

* Invoice: fixed an issue preventing advance payment invoice refund.
* Invoice: fixed a regression preventing invoices refund.

## [8.2.4] (2024-12-19)

### Fixes
#### Base

* Updated Axelor Open Platform to 7.2.4.
* Updated studio module to 3.3.8.
* Product: fixed french translation for 'Product'.
* Partner: fixed siren, nic and tax number computation for demo data.
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

#### Business Project

* Business project: fixed an error occurring when opening sale order line in financial data
* Project/Project task: fixed planned and spent time when time units are not the same.

#### Contract

* Contract template: fixed recurring product display for supplier contract.
* Contract: cut off dates are now correctly filled in invoice lines.

#### CRM

* Partner: fixed the display issue in event panel when images are attached in the event description.

#### Human Resource

* Lunch voucher: leave request with hour leave reason type are now ignored.
* HR API: fill expense type select on creation
* Expense API: fixed the error when an employee did not have a contract while creating an expense.

#### Production

* Manufacturing order: fixed no such field 'typeSelect' error when selecting bill of material and prod process when maintenance module is missing.
* Purchase order: fixed wrong domain for the dashlet on outsourcing tab.
* Manufacturing order: merging manufacturing orders now correctly takes into account scheduling configuration.

#### Project

* Project template: fixed duplicated invoicing tab when creating a project template as a business project.
* Gantt view: fixed an issue where changing the progression did not change it on the project task.

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

#### Contract

`AppAccountService` has been added to the constructor of `ContractInvoicingServiceImpl`.

## [8.2.3] (2024-11-28)

### Fixes
#### Base

* Updated studio module to 3.3.6.
* Template: changed title from 'Print Template' to 'Print template'.
* Group view: fixed inexistant field 'canViewCollaboration' to display only with the Enterprise Edition.
* Rest API: fixed response message translation.
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

#### Business Project

* Project template: show the generated business project with the right view.

#### Contract

* Contract: prorata is now based on invoice period start date instead of contract start date.

#### Human Resource

* Lunch voucher: fixed computation for leaves with overlapping periods.
* Timesheet: fixed timeseet duplicate creation.
* Employee: checked unicity constraints when creating user at the end of employee creation process.
* HR batch: fixed an error occurring when launching Leave Management Reset Batch.
* ProjectPlanningTime: added missing computation duration at change of time units.
* Leave request: fixed future quantity day computation when sending a leave request.
* HR: added employee and employment contract in 'Related to' of message.
* Timesheet: fixed NPE because of daily limit configuration.

#### Production

* Production API: fixed error while fetching consumed products.

#### Purchase

* Purchase order: fixed an error occurring when generating purchase request if the order was not saved

#### Sale

* Sale API: fixed issue related to complementary products price computation.
* Sale order: a partner blocked from sale order is now correctly filtered out from the list of customers
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

Added the employeeRepository in the TimesheetQueryServiceImpl constructor

---

Renamed `action-condition-user-validCode` to `action-user-method-validate-code`.

---

Removed `action-project-planning-time-record-start-time-onchange`, replaced by `action-project-planning-time-method-compute-planned-time`.

## [8.2.2] (2024-11-14)

### Fixes
#### Base

* Updated Axelor Open Platform to 7.2.3.
* Updated studio module to version 3.3.5.
* Updated message module to version 3.2.2.
* Updated utils module to version 3.3.0.
* Partner: tax number will be displayed for suppliers too along with customers.
* Tag: added default color tag
* Unit conversion: fixed NPE when getting the coefficient for a unit conversion of type coeff with value zero.
* Tag: allowed to select tag when concerned model is empty on tag.
* Template: fixed an issue where 'Test template' button was always readonly with custom model.
* Pricing: fixed the product category filter in pricing formula.

#### Account

* Invoice: fixed invoice printing that does not work without business project module.
* Analytic move line query: fixed always readonly button in 'Analytic move lines to reverse' dashlet.
* Accounting export: fixed skipped lines on accounting export when we have more than 10 000 lines.
* Accounting report: replaced move line partner full name with partner sequence and partner full name in reports.
* Invoice: fixed NPE when clicking on print button on grid lines.
* Move/Analytic: record analytic account on moveline on the reverse move
* Partner: fixed an issue where the partner balance was wrong after an unreconcile.
* Move: blocked generation of reverse move if reverse move date is before the date of the move to reverse.
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

#### Production

* Sale order: no personalized BOM will be created if the option is not enabled.

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

#### Base

- Action "action-tag-method-set-concerned-model" have been replaced by "action-tag-method-on-new".
- And setDefaultConcernedModel method in TagController have been renamed by onNew

#### Supply Chain

- Added new arguments to SaleOrderInvoiceService.displayErrorMessageIfSaleOrderIsInvoiceable()
- Updated SaleOrderInvoiceService.computeAmountToInvoice visibility to protected and removed it from interface

## [8.2.1] (2024-10-31)

### Fixes
#### Base

* Sequence: fixed draft prefix when checking for the draft sequence number.
* Birt report: fixed number formatting for excel format.
* Partner: added check on parent partner to avoid same partner as parent.
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

* Prod process: added workflow buttons instead of clickable status.
* Manufacturing order: fixed issue when updating quantity in manufacturing order.

#### Purchase

* Purchase order: fixed value of 'total without tax' in birt report.

#### Sale

* Sale order: fixed an issue preventing from editing a sale order with editable grid and pending order modification enabled
* Sale order: fixed sale order printing when only sale module is used, without supplychain.

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

## [8.2.0] (2024-10-18)

### Features
#### Base

* Updated Axelor Open Platform to 7.2. You can find all information on this release [here](https://github.com/axelor/axelor-open-platform/blob/7.2/CHANGELOG.md).
* Tag: added a new Tag model to help label other elements (present in lead, partner, project tasks, accounting reports, quality alert).
* Localization: improved localization support, managing date and numbers format.
* Product: added the possibility to get product prices managing currency and taxes from the API.

#### Sale

* Sale order: added a new editable tree view for sale order lines.

A new configuration is now available in App Sale to choose the normal grid view or the editable tree view for sale order lines. That means that, using this view, any sale order line can be subdivised into other sale order lines. This can be used to compute a sale order line price from its children lines. Children lines can be added manually, but are also automatically generated from information in the bill of materials.

* Cart: added a new cart feature, allowing an user to create a sale order by selecting products from other form.
* Sale order: updated the API to add the possibility to create sale order including lines, and update its status.
* Partner: added DSO (Days Sales Outstanding) in partner form. The computation is based on customer invoices.

#### Account

* Foreign exchange gains/losses: correctly generate extra accounting entries when currency rate has changed between the invoice date and the payment date.
* Tax: managed taxe by amount. This can be useful when tax amount in supplier invoices is not equal to the tax computation done by Axelor Open Suite.
* Invoice: added late payment interests invoice generation.
* Fixed asset: added a new method to split fixed assets by unit.

#### Stock

* Added "Mass stock move" form view, which is an helper to manage to move products from and to multiple stock locations.

#### Project

* Project task: managed task status by project category
* Project task: added progression computation from category and/or the task status.
* Project task: added a check list that will be available on project templates.
* Project: managed active project per user, with a quick menu to allow a user to change their active project.

#### Mobile Settings

* New mobile app Sales.
* New mobile app Project.
* Added a new chart type.

### Changes

#### Base

* Updated app icons on menus.
* Printing template: added a script to configure dynamically the name of files generated by the template.
* Data sharing: added new technical models to manage connectors configuration and synchronization.
* Site: managed trading name on sites.
* Address: updated our API to allow search/creation of an address.

#### CRM

* Lead: it is now possible to change the status of a closed lead to the default status.
* Tag: LeadTag is now replaced by Tag.

#### Purchase

* Purchase order: added the possibility to split a purchase order.

#### Sale

* Sale order: added a new button to recompute prices using pricing scales and price lists on the sale order.

#### Account

* Invoice: on supplier invoices, added a new wizard to fill invoice number and date before ventilation.
* Accounting report: added filters on tags on accounting report.
* Closure/Opening fiscal year batch: added a warning before launching the process if we have daybook entries.

#### Bank payment

* Bank reconciliation: allow users to generating accounting entries by using the button "Auto accounting".
* Bank order: added an option to encrypt the generated the bank order file on the server.

#### Stock

* Stock: updated the API with a new query to manage line splitting on supplier arrivals.

#### Supply Chain

* Partner: it is now possible, for a partner which is both a supplier and a customer, to have different payment condition for incoming and outgoing payments.

#### Production

* Prod process: added workflow buttons instead of clickable status.

#### Project

* Tag: replaced ProjectTaskTag by Tag.
* Project task: split personalized fields depending on where they are configured and added personalized fields for project categories.

#### Business project

* Split views between project and business projects to improve user interface.

#### Mobile Settings

* Mobile menu: it is now possible to manage mobile app menus from the web interface.
* Chart: added a new "indicator" type for mobile charts.

### Fixes

#### Base

* Trading name: fixed relationship between trading name and company: now a trading name only belongs to one company.
* Site: fixed relationship between site and company: now a site only belongs to one company.

#### HR

* Payroll preparation: multiple fixes.

#### Sale

* Sale order: technically many changes were made to refactor XML actions into single action-method.

#### Account

* Deposit slip: manage bank details in generated accounting entries.
* Payment: use correctly the payment date instead of today date when computing currency rate.

[8.2.5]: https://github.com/axelor/axelor-open-suite/compare/v8.2.4...v8.2.5
[8.2.4]: https://github.com/axelor/axelor-open-suite/compare/v8.2.3...v8.2.4
[8.2.3]: https://github.com/axelor/axelor-open-suite/compare/v8.2.2...v8.2.3
[8.2.2]: https://github.com/axelor/axelor-open-suite/compare/v8.2.1...v8.2.2
[8.2.1]: https://github.com/axelor/axelor-open-suite/compare/v8.2.0...v8.2.1
[8.2.0]: https://github.com/axelor/axelor-open-suite/compare/v8.1.9...v8.2.0
