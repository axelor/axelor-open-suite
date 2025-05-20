## [8.3.6] (2025-05-15)

### Fixes
#### Base

* Update Axelor Open Platform to 7.3.7.
* Partner: fixed performance issues when opening emails tab on partner form view.
* Partner: when checking for duplicated registration code, correctly ignore whitespaces.
* PriceList: Set the currency to match the currency of the active company.
* Partner: fixed form view for project panel.

#### Account

* Accounting configuration/Move line: ensured consistent tax display and editability.
* BillOfExchange/PaymentSession: fixed technical error when cancelling bank order payment then pay the exactly same invoice term.
* Invoice: fixed invoice term due date when we update invoice due date with free payment condition.
* Mass entry: fixed critical error when we validate more than ten moves in mass entry process.
* Accounting batch: fixed an issue where the generated result entry (move) was not correctly linked to the corresponding close/open account batch.
* Journal: fixed issue allowing moves or mass entry sessions to be created from journal buttons on inactive journals.
* Auto reconcile/Partner: restricted auto reconcile between customer and supplier invoices on partner with compensation enabled.

#### Bank Payment

* BankOrder/Umr: fixed the selection of the partner active umr in bank order confirmation.
* Bank payment config: removed the possibility to select view type account on internal and external bank to bank account.
* Bank order: fixed an error message on missing bank order encryption password even if 'Enable bank order file encryption' was disabled.

#### Budget

* PurchaseOrder/Budget: fixed budget exceed error when using mono budget on purchase order.
* Move/Budget: fixed an issue where only 'realized with no po' was imputed when creating budget on move line related to an invoice line.
* Move/Budget: fixed negative amounts on realized and committed on daybook moveline budget imputation.

#### Contract

* Contract: fixed NPE when ventilating an invoice linked to a contract that has additional benefits.

#### Human Resource

* Expense: removed the possibility to duplicate an expense.
* Allocation line: added x-order on from date for period field.
* Timesheet line: fixed the error when creating a timesheet without an employee from the project view.
* Expense: fixed duplicate move when we confirmed a bank order from an expense.

#### Production

* Configurator: fixed an issue where bill of material and prod process were generated twice.
* Sale order: display an error message when trying to delete a line linked to manufacturing orders.

#### Project

* Project: added project time unit in demo data and set it to 'Day'.
* Project: fixed Project activity dashboard to fetch only relevant messages, avoiding unnecessary loading and filtering.
* Project/SaleOrder: fixed name computation when generating business project.
* App project: fixed issue where custom fields for Project/Task caused save errors when name was not entered first with type 'select' or 'multiselect'.

#### Sale

* Sale order: fixed the issue of finalizing a sale order without sale order lines.
* Sale order: reset the 'manual unlock' state when duplicating a sale order.
* Configurator formula: fixed message type to show an info message instead of an alert when the formula works correctly.
* Sale order: fixed an error occurring when adding lines to an order without production module.

#### Stock

* Stock location: fixed date time issue in location financial data report.

#### Supply Chain

* Stock move: fixed error message when checking available stock for requested and reserved quantities.


### Developer

#### Account

In `AccountingCloseAnnualService`, the method `generateResultMove` now returns a `Move` instead of `void`.

---

In `MoveLineToolService.getMoveExcessDueList`, changed `Long invoiceId` parameter to `Invoice invoice`.

#### Project

Creation of a new service `ProjectNameComputeService`, and added `ProjectNameComputeService` in the `ProjectService` constructor.

## [8.3.5] (2025-04-30)

### Fixes
#### Base

* Updated Axelor Open Platform to 7.3.6.
* Product: fixed domain filter for product variant values.
* Product: fixed update product prices process to update company specific prices.
* Template: on creating a new template manually, keep email account empty by default so the generated message can use default email account.
* Fixed rounding mode in some quantities computation.
* Sale order line: fixed an issue where warnings related to sale order line (like stock control) were not displayed to the user and blocked the process.

#### Account

* Bank reconciliation: fixed NPE when validating a bank reconciliation without journal while having an account on a bank reconciliation line.
* Invoice: fixed 'Terms and Conditions','Client box in invoice' and 'Legal note on sale invoices' to support HTML tags in BIRT report.
* Analytic move line: set analytic axis required when we create an analytic move line.

#### Budget

* Purchase order line: use the correct account when a purchase order line is set to 'fixed assets'.

#### Business Project

* Business Project Task: fixed an issue where total costs was not computed on unit cost change.
* Invoice: fixed third-party payer when generating an invoice from an invoicing project.

#### Contract

* Contract invoicing batch: fixed an issue where it was not possible to generate more than one invoice for a same contract.

#### Human Resource

* Expense: fixed personal expense and personal expense amount french translation.
* User: hide create employee button if the partner has already an employee.

#### Mobile Settings

* Mobile permissions: fixed demo data.

#### Production

* Manufacturing order: fixed an issue where producible quantity was taking into account component that were not managed in stock.
* Sale order: fixed an error occurring when changing the price of a sale order line details.
* MRP: added a explicit error message instead of a NPE when prod process is null on manufacturing proposal.
* Sale order: fixed an error occurring when generating sale order line details with no cost price.
* Sale order line details: fixed the unit price getting recomputed wrongly when changing quantity.
* Prod process line: fixed domain of stock location to take into account only those usable on production

#### Project

* Project planning: fixed english message in confirmation popup.
* Sprint: added a sprint form and grid with editing and adding functionalities disabled.

#### Stock

* Stock move: prevented generation of invoices when 'generate Invoice from stock move' configuration is disabled in supplychain app.

#### Supply Chain

* Declaration of exchanges: will now reset fiscal year and if necessary country on change of company.
* Supplychain: fixed total W.T. of invoices generated from a stock move of a purchase order with ATI.

### Developer

Fixed ControllerMethodInterceptor to avoid adding exception management on non void and non public methods in controller.

#### Business Project

Added `PartnerAccountService` to the constructor of `ProjectGenerateInvoiceServiceImpl`.

## [8.3.4] (2025-04-17)

### Fixes
#### Base

* Updated Axelor Open Platform to 7.3.5.
* Updated axelor-studio dependency to 3.4.2.
* Base: fixed base roles are not imported.
* Partner: fixed agencies domain filter.

#### Account

* Reconcile: removed check on tax for opening/closure.
* Invoice: fixed an issue in the BIRT report where a partner or company partner had multiple accounting situations for different companies.
* Payment voucher: fixed translation for error message when move amounts have been changed since the imputation.
* Account management: fixed fields not required in editable grid of payment mode.
* Invoice: fixed due date when we save with free payment condition.
* Accounting report: set currency required for 'Bank reconciliation statement' report.
* Invoice: fixed price list not filled the first time we change the partner.

#### Bank Payment

* Bank reconciliation: fixed issue when try to load bank statement in different currency than bank details.

#### Business Project

* Business project task: fixed an issue where modifying quantity was resetting unit price.

#### CRM

* Partner: fixed 'Generate Project' button still present even with job costing app disabled.

#### Human Resource

* Timesheet: prevent 'To date' edition on completed timesheets.
* Project allocation: fixed initialization with planned time in mass generation.
* Leave Request: fixed the NPE caused by the employee's empty week planning.
* Leave request: leave reason and duration are now required when using multi-leave assistant.

#### Production

* Purchase order: fixed an issue where subcontracted order was reset to standard on validation.
* Sale order: fixed translation for 'Details Lines (Tree)'.

#### Project

* Project: fixed wrong compute of full name.
* Project task: fixed npe on project task gantt views.

#### Purchase

* Supplier catalog: fixed wrong fetched information for product without catalog.

#### Sale

* Sale order: fixed 'Generate production order' button displayed when app production is deactivated.
* Sale order: fixed the issue where stock location was overwritten upon changing the partner when trading name was null.
* Sale order: fixed missing error message on pack products.
* Sale order: fixed the alert message before confirming the sale order.

#### Stock

* Stock move line: fixed total net mass calculation when real quantities are generated.
* Stock location: fixed quantity scaling in stock location line grid and form views.

#### Supply Chain

* Sale Order: do not remove shipment cost line if the line is already invoiced.
* Sale order: fixed sale order invoicing state when a line has a negative amount.


### Developer

#### Sale

The method `confirmCheckAlert` in `SaleOrderCheckService` now returns `List<String>` instead of `String`.

#### Supply Chain

Added `InvoiceRepository` to the constructor of `SaleOrderShipmentServiceImpl`.

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

[8.3.6]: https://github.com/axelor/axelor-open-suite/compare/v8.3.5...v8.3.6
[8.3.5]: https://github.com/axelor/axelor-open-suite/compare/v8.3.4...v8.3.5
[8.3.4]: https://github.com/axelor/axelor-open-suite/compare/v8.3.3...v8.3.4
[8.3.3]: https://github.com/axelor/axelor-open-suite/compare/v8.3.2...v8.3.3
[8.3.2]: https://github.com/axelor/axelor-open-suite/compare/v8.3.1...v8.3.2
[8.3.1]: https://github.com/axelor/axelor-open-suite/compare/v8.3.0...v8.3.1
[8.3.0]: https://github.com/axelor/axelor-open-suite/compare/v8.2.9...v8.3.0
