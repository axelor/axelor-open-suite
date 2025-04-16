## [8.3.3] (2025-04-03)

### Fixes
#### Base

* Update to Axelor Open Platform 7.3.4.
* Day planning: added sequence in demo data.
* Base batch: fixed issue in recompute all addresses batch.
* Fixed SIRET check before creation using partner Sirene API call.
* Partner: when creating/filling a partner from Sirene API, fill the NIC.
* Partner: fixed wrong registration number check warning translation.

#### Account

* Fixed an user interface issue where it was allowed to create a new tax where the user should only be able to select one.
* Invoice: show 'Price excl. Tax' on invoice report even if it is 0.
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

* Business project task: fixed an issue at task creation when employee has a product.
* Invoicing project: fixed an issue where non validated timesheet lines were included.

#### Contract

* Contract line: fixed domain for product based on contract target type.
* Contract: added french translation for the 'Related Equipment' panel title.

#### Human Resource

* Project: fixed an error occurring when creating a timesheet and the user had no employee.
* Timesheet line: fixed an issue where a timesheet line was not linked to the employee's timesheet.

#### Production

* Bill of material: fixed an issue where a component could not be deleted from the BOM line list after the tree view was opened.
* Sale order: fill default prod process of bill of material when choosing a product.

#### Sale

* Sale order: fixed the alert when finalizing an order and the price list is not valid.
* Sale order: fixed wrong behaviour on a prospect with accepted credit.
* App sale: fixed French translation for line list display type helper.
* Sale order: fixed NPE when creating a sale order without active company.

#### Stock

* Stock move: fixed average price not updated on product when stock move is canceled.
* Inventory: gap and real values computation now correctly takes into account company product cost/sale/purchase price.

#### Supply Chain

* Sale order: fixed NPE when invoicing a sale order with title line.


### Developer

#### Base

Added `PartnerService` and `AppBaseService` to the constructor of `PartnerGenerateService`.

#### Account

Added `CurrencyService` and `CurrencyScaleService` to the constructor of `PaymentVoucherControlService`.

## [8.3.2] (2025-03-20)

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

#### Project

* Project: fixed an issue where 'Invoicing timesheet' field was not displayed.

#### Purchase

* Supplier catalog: products with multiple catalogs now take the catalog with the most recent update date.

#### Sale

* Sale order: fixed the issue where lines were not printed with the pack feature.

#### Supply Chain

* MRP: a warning is now displayed when trying to delete a MRP to explain that the MRP is reset instead of removed.


### Developer

#### Sale

Added `SaleOrderLineComputeService` to the constructor of `SaleOrderServiceImpl`.

## [8.3.1] (2025-03-13)

### Fixes
#### Base

* Update to Axelor Open Platform 7.3.3.
* Fixed a critical issue preventing the application from starting.
* Base: removed API Configuration menu entry.
* App base: added password widget for the certificate password.
* Base: fixed some errors displayed as notification instead of popup.
* Signature: fixed broken grid view when selecting a certificate.
* Message: fixed an issue where the attached birt document name changed when sent via email.
* User: made the title of the company set panel visible.

#### Account

* Accounting batch: ignored check at reconcile when move only contains tax account.

#### Business Project

* Invoice: fixed fiscal position when generating an invoice from an invoicing project.

#### CRM

* Partner: creating a new partner cannot create a prospect and a supplier at the same time.

#### Human Resource

* HR: fixed an error occurring when using 'Leave increment' batch and if employees do not have a main employment contract.
* Timesheet: fixed error on opening a timesheet preventing employee field from being set readonly.

#### Production

* Sale order line: fixed an issue when syncing bill of materials lines dans sub sale order lines.

#### Purchase

* Purchase request: added missing API endpoint to get back to draft.

#### Sale

* Sale order line: fixed an issue where discount was not applied immediatly.
* Sale order line: fixed icon of edit configurator button.
* Sale order line: fixed advanced filter not displayed unless the whole page is refreshed.

#### Stock

* Stock location: fixed error when emptying parent stock location.


### Developer

#### Base

Menu 'referential-conf-api-configuration' and action 'referential.conf.api.configuration' have been removed. 

```sql
DELETE FROM meta_menu WHERE name = 'referential-conf-api-configuration';
DELETE FROM meta_action WHERE name = 'referential.conf.api.configuration';
```

## [8.3.0] (2025-03-07)

### Features
#### Base

* Updated Axelor Open Platform to 7.3. You can find all information on this release [here](https://github.com/axelor/axelor-open-platform/blob/7.3/CHANGELOG.md).
* Updated Axelor Studio dependency to 3.4. You can find all information on this release [here](https://github.com/axelor/axelor-studio/blob/release/3.4/CHANGELOG.md).
* Partner: added a new connector to Sirene API to fetch partner information.
* Price list: added currency management in price lists.
* Product: managed multiple barcodes on product.

#### HR

* HR: added new menu entries to help filling existing requests.
* Leave request: added number of days available on the requested leave date.
* Leave request: added a new wizard to create multiple leave requests with different types.
* Timesheet: added a new wizard to create timesheet from project planning.

#### Purchase

* Purchase order: added subconctractor management.
* Purchase order: added receipt stock location by lines.
* Supplier: added carriage paid feature on suppliers and purchase orders.
* Supplier catalog: added unit management in supplier catalogs.

#### Sale

* Sale order: added the possibility to configure a discount on a single order that will be applied to each line.
* Sale order line: allow to duplicate a sale order line. 
* Sale order line: added delivery address in sale order lines.
* Sale order line: managed multi line in sale order printings.
* Configurator: on a sale order line, allow to modify the form values that were previously filled, and run the computation again to update sale order line values.
* Configurator: allow tu duplicate a sale order line, using the existing configurator to generate the duplicated line.
* Configurator: managed versioning on configurators.

#### Account

* Tax: changed the way non deductible taxes are configured.
* Journal: added a button to generate accounting entries for the current journal in the form view.

#### Stock

* Stock move: added customer delivery split line support.

#### Production

* Sale order line: added details line on sale order. On a given sale order line, it will display information related to the bill of materials related to this line.
* Cost sheet: managed launch quantity in cost computation.

#### Project

* Project: added sprint for project management.
* Project: added allocation management to manage resource allocation on given periods.
* Dashboard: added a new resource management dashboard.
* Project planning: automatic generation of project planning from project tasks.
* Sale order: allow to generate a project from a sale order and a project template.

#### Business Production

* Business project: added manufacturing order generation from business project.
 
#### Mobile Settings

* APK: added possibility to upload the .apk of the current app version to manage deployment on new devices.
* DMS: added new configuration to manage DMS on mobile application.
* Purchase: added new configurations to manage purchase requests.

### Changes

#### Base

* Partner: added the possibility to link multiple trading names to a partner.
* Partner: added a warning on creation if a partner with the same registration number (SIRET for France) already exists.
* Partner: registration number check is made on change and not on save like before.
* App base: shortcut management for active company/trading name/project is now a single configuration.
* Year/Period: allow period generation on period of 1 or 2 weeks.
* Address: improved address templates.

#### CRM

* Lead: replaced the existing address fields by a link to a full Address object.

#### HR

* Timesheet: added a dashlet to see planned time from projects on a period.
* Leave request: took into account the end date to compute quantity available.
* Leave request: created a new API endpoint to create multiple leave requests.
* Leave request: created a new API endpoint to fetch number of available days for a leave type.
* Leave request: created a new API endpoint to check if requested days are available.
* Leave request: created a new API endpoint to update leave request status.

#### Purchase

* Purchase request: created a new API endpoint to update purchase request status.

#### Sale

* Sale order: in multi line sale order, added icon to see the product type on each line.
* Sale order: new configuration to activate or disabled price computation from sub lines.
* Sale order: added a button to open multi lines in a separate form view.
* Sale order: managed modifications on a sale order already partially invoiced through timetable.

#### Account

* Accounting batch: added an option to open/close every accounts instead of having to select everything.

#### Bank payment

* Bank order: added CSV export on bank order lines.

#### Stock

* Logistical form: managed different stock locations on lines.
* Stock move: managed tracking number taking into account configuration per company.

#### Production

* Sale order line: added a new tag to display the production status on sale order line linked to manufacturing orders.
* Sale order line: added quantity to produce.

#### Project

* Project: added link to company.
* Batch: added new batches to update project task status.
* App project: added a new configuration to activate/deactivate the planification.
* Project task: generate project planning lines during task generation from template.
* Project task: use signature widget.

#### Business project

* Business project: New dashlet to see related purchase orders.

### Fixes

#### Base

* Product: when generating product variant, copy the product instead of only copying some fields.

#### HR

* Extra hours: fixed filter on employee selection.
* Expense: fixed analytic accounting information display.
* Weekly/Events planning: remove duplicated configurations in HR, the configurations in the company are now used everywhere.
* Timesheet: in timesheet form view, displayed "is completed" in a tag.

#### Sale

* Sale order: removed sale order line tree feature, it is replaced by sale order line details.

#### Account

* Move line: added accounting date on move line form view.
* Move: fixed reverse charge feature on a multi tax accounting entry.

#### Budget

* Budget distribution: added origin on budget distribution.

#### Project

* Project task invoicing: use the price in the task instead of the product price.
* Task section: removed task section, it is replaced by category.
* Project: added Time follow-up panel.
* Project version: version name is now required and unique per project.

#### Business project

* App business project: removed configurations related to time management in app business project (time units and default hours per day) to use the configurations already present in app base.
* Project financial data: added a link to the project in project financial data view.

[8.3.3]: https://github.com/axelor/axelor-open-suite/compare/v8.3.2...v8.3.3
[8.3.2]: https://github.com/axelor/axelor-open-suite/compare/v8.3.1...v8.3.2
[8.3.1]: https://github.com/axelor/axelor-open-suite/compare/v8.3.0...v8.3.1
[8.3.0]: https://github.com/axelor/axelor-open-suite/compare/v8.2.9...v8.3.0
