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

[8.2.1]: https://github.com/axelor/axelor-open-suite/compare/v8.2.0...v8.2.1
[8.2.0]: https://github.com/axelor/axelor-open-suite/compare/v8.1.9...v8.2.0
