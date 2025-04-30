## [8.2.14] (2025-04-30)

### Fixes
#### Base

* Updated Axelor Open Platform to 7.2.7.
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

#### Production

* Manufacturing order: fixed an issue where producible quantity was taking into account component that were not managed in stock.
* Prod process line: fixed domain of stock location to take into account only those usable on production

#### Project

* Project planning: fixed english message in confirmation popup.

#### Stock

* Stock move: prevented generation of invoices when 'generate Invoice from stock move' configuration is disabled in supplychain app.

#### Supply Chain

* Declaration of exchanges: will now reset fiscal year and if necessary country on change of company.
* Supplychain: fixed total W.T. of invoices generated from a stock move of a purchase order with ATI.


### Developer

Fixed ControllerMethodInterceptor to avoid adding exception management on non void and non public methods in controller.

#### Business Project

Added `PartnerAccountService` to the constructor of `ProjectGenerateInvoiceServiceImpl`.

## [8.2.13] (2025-04-17)

### Fixes
#### Base

* Updated axelor-studio dependency to 3.3.13.

#### Account

* Reconcile: removed check on tax for opening/closure.
* Invoice: fixed an issue in the BIRT report where a partner or company partner had multiple accounting situations for different companies.
* Account management: fixed fields not required in editable grid of payment mode.
* Invoice: fix due date when we save with free payment condition
* Invoice: fixed price list not filled the first time we change the partner.

#### Bank Payment

* Bank reconciliation: fixed issue when try to load bank statement in different currency than bank details.

#### CRM

* Partner: fixed 'Generate Project' button still present even with job costing app disabled.

#### Project

* Project: fixed wrong compute of full name.
* Project task: fixed npe on project task gantt views.

#### Purchase

* Supplier catalog: fixed wrong fetched information for product without catalog.

#### Sale

* Sale order: fixed 'Generate production order' button displayed when app production is deactivated.
* Sale order: fixed the issue where stock location was overwritten upon changing the partner when trading name was null.
* Sale order: fixed the alert message before confirming the sale order.

#### Stock

* Stock move line: fixed total net mass calculation when real quantities are generated.
* Stock location: fixed quantity scaling in stock location line grid and form views.

#### Supply Chain

* Sale order: fixed sale order invoicing state when a line has a negative amount.


### Developer

#### Sale

The method `confirmCheckAlert` in `SaleOrderCheckService` now returns `List<String>` instead of `String`.

## [8.2.12] (2025-04-03)

### Fixes
#### Base

* Base batch: fixed issue in recompute all addresses batch.

#### Account

* Fixed an user interface issue where it was allowed to create a new tax where the user should only be able to select one.
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


### Developer

#### Account

Added `CurrencyService` and `CurrencyScaleService` to the constructor of `PaymentVoucherControlService`.

## [8.2.11] (2025-03-20)

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

## [8.2.10] (2025-03-13)

### Fixes
#### Base

* App Base: added password widget for the certificate password.
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

#### Production

* Sale order line: fixed an issue when syncing bill of materials lines dans sub sale order lines.

#### Sale

* Sale order line: fixed an issue where discount was not applied immediatly.
* Sale order line: fixed advanced filter not displayed unless the whole page is refreshed.

#### Stock

* Stock location: fixed error when emptying parent stock location.

## [8.2.9] (2025-02-20)

### Fixes
#### Account

* Invoice payment: forbid the creation of a 0 amount payment.
* Invoice: fixed contact being readonly when the partner is an individual.
* Payment session: fixed infinite loop when searching eligible invoice terms.
* Fiscal position: fixed NPE on adding account equivalence on new record.
* Move: fixed company bank details domain when no payment mode is selected.
* Product: added product demo data for late payement invoice.
* Invoice: fixed the issue that required saving before applying cutoff dates.
* Journal: fixed error while importing france chart of accounts.
* Move: fixed the transactional error at move delete.
* Accounting batch: fixed the issue where batch accounting cut-off does not link generated tracebacks.

#### Contract

* Contract: fixed no analytic move lines on contract duplication.
* Contract version: fixed the issue where the file was being duplicated during updates.
* Contract version: fixed file link is lost when creating a new amendment.

#### Human Resource

* Timesheet: fixed an issue in lines generation from project planning where end date was not taken into account.

#### Marketing

* Campaign: fixed FR translation for 'Event start'.

#### Production

* Sale order: fixed an issue occurring when adding a title subline.
* Bill of materials: fixed BOM name in demo data, causing an issue when customizing the BOM.

#### Quality

* QI Resolution: fixed wrong translation in English for the term default.

#### Sale

* Configurator: fixed default value that was not set with configurators
* Configurator product formula: disabled test button if formula is empty.
* Sale order line: fixed default value for prodProcess.

## [8.2.8] (2025-02-06)

### Fixes
#### Base

* Update Axelor Open Platform to 7.2.6.
* Advanced export: fixed NPE when target field is empty on advanced export line.
* Unit conversion: fixed demo data for 'Box of 24 Pces'.
* Period: fixed inconsistency when filling dates on form view.
* File: fixed id to load is required for loading error.
* Pricing: use formula filtering also on linked pricing.
* City: added a helper to prevent user from getting wrong files for manual import.

#### Account

* FEC import: fixed an issue during accounting entries import where the entries were validated without any checks.
* Move: fixed description is not inherited on move lines when they are generated from mass entry lines.
* TaxPaymentMoveLine: fixed computation error when the tax line contains multi tax.

#### Budget

* Move: blocked budget distribution modification on daybooked moves.
* Budget: fixed button to display committed lines.

#### Business Production

* Operation order: fixed filter for employees.

#### Business Project

* Sale order/business project: fixed an issue on partially delivered sale order invoicing
* Sale order: fixed NPE on selecting a project.

#### Business Support

* Project version: fixed project filter to avoid conflicts on save.

#### Human Resource

* HR batch: fixed an error occurring when using 'Increment leave' batch.

#### Production

* Sale order: sublines bill of material are not customized anymore.

#### Purchase

* Purchase order line: fixed advanced filter not displayed unless the whole page is refreshed.

#### Sale

* Product: Calculated the price based on the partner price list in APIs.
* Sale order line: added missing translation for 'Customize BOM' and 'Customize production process'.
* Sale order: fixed advance payment amount during copy.

#### Stock

* Stock move: fixed split into fulfilled line and unfulfilled one total net mass issue.
* Stock move: fixed split into 2 total net mass issue.

#### Supply Chain

* Stock move: fixed an error during mass customer stock move invoicing.


### Developer

#### Sale

Added a new argument `ProductPriceListService` in `ProductPriceServiceImpl.java` constructor.

## [8.2.7] (2025-01-23)

### Fixes
#### Base

* Update Axelor Open Platform to 7.2.5.
* Updated axelor-studio dependency to 3.3.10.
* Updated axelor-utils dependency to 3.3.2, fixing an issue that could prevent PDF generation in some cases.
* Updated bouncycastle dependency to improve security.
* Currency: fixed 'codeISO' field emptying on load.
* Data sharing referential: filtered the data sharing referential lines by model.
* Data backup: fixed backup of applications config.
* Partner: fixed error in customer situation report.
* Product: fixed the incorrect domain on historical orders to display validated orders today.
* User: made phone and email fields read-only as long as the user does not have a linked contact.

#### Account

* Move line query line: removed taxes from the grid to recover the broken grid view.
* Move Line: fixed vat system edition according to account type and move origin.
* FEC Import: set VAT system on move lines during import.
* Payment voucher: close popup when receipt is printed from invoice.
* Account config: added demo data and l10n for the foreign exchange accounts.
* Accounting config: fixed translations on new company creation.

#### Business Project

* Project: fixed display of 'frameworkContractPanel' panel.
* Project: set stock locations while generating sale quotation from project.
* Invoicing project: fix project task progress in invoicing project annex report.

#### Contract

* Contract: fixed the filter on partner field to select only customer/supplier respectively on customer/supplier contracts.

#### CRM

* Partner: creating a new partner is no longer a prospect and a customer at the same time.

#### Human Resource

* Expense API: fixed an error that could occur when adding expense line to an expense.

#### Production

* Sale order: fixed an error occurring when deleting a subline linked to a customized bill of material.

#### Project

* User: hid the active project field if the project module is not installed.

#### Sale

* Sale order: added a warning when adding a subline to a title line.
* Sale order line: increased the width of the tax column for ergonomic purposes.
* Sale order: fixed the description in the sale order to use the partner's sale order comments instead of the partner general note.
* Sale order line tree grid: added missing translation for 'Add a new sale order line' and fixed title.
* Product API: when querying multiple product prices using the `/aos/product/price` endpoint, if a configuration error is detected for one product, return the price for other products instead of only returning the error.

#### Stock

* Stock move line: fixed the issue by making the availability column readonly.
* Stock move: compute total net mass when splitting lines.

#### Supply Chain

* Purchase order: fixed fiscal position when creating a purchase order from sale order.
* Stock move: stock move mass invoicing now generates an invoice with the correct invoicing address.
* Stock move invoicing: missing translation in wizard on stock move lines.
* Sale order: enable stock reservation feature on sale order editable grid.


### Developer

#### Base

The dependency `'org.bouncycastle:bcprov-jdk15on:1.70'` was replaced by `'org.bouncycastle:bcpkix-jdk18on:1.78.1'`. If you are using an AOS module with other modules that depends on `'org.bouncycastle:bcprov-jdk15on:1.70'`, please change your gradle configuration to avoid a conflict or update your dependencies.

#### Account

`UserService` has been added to the constructor of `MoveValidateServiceImpl`.

---

Added `MoveLineTaxService` dependency to `FECImporter` class.

#### Business Project

The `ProjectBusinessServiceImpl` constructor signature was modified: it now includes `ProjectBusinessServiceImpl`.

#### Supply Chain

Added 'AddressService' to the constructor of 'SaleOrderCreateServiceSupplychainImpl'.

## [8.2.6] (2025-01-09)

### Fixes
#### Base

* Email account: fixed NPE that occurred when setting 'Default account' to false.
* City: fixed issue where country was not filled when adding a new city from address form.
* Registration number template: initialized 'Starting position in the registration number' to 1 on new record to prevent save errors.
* Partner: fixed error when duplicating a partner without a picture.
* Partner: hide 'Accounting situation' panel when the partner doesn't belong to any company.
* Birt reports: fixed hard coded db schema in native queries.
* Message: fixed data-grid and data-form in 'message.related.to.select'.

#### Account

* Payment voucher: fixed error when the invoice term has no related invoices.
* Accounting batch: result move computation query takes into account accounted entries.
* Move / Moveline: added additional control to avoid unbalancing input of entries on general / special accounts at validation
* Account: fixed hard coded db schema in native queries.
* Payment voucher: fixed unexpected pop-up mentioning no record selected while there is at least one.
* Invoice: removed incoherent mention of refund line in printing when it's not originated from actual invoice.
* Invoice payment: fixed financial discount when changing payment date.
* Bank reconciliation: fixed the filter to display move lines with no function origin selected and hide it when already reconciled in different currency than move in 'Bank reconciliation statement' report.
* Accounting report type: fixed comparison in custom report types demo data.

#### Budget

* Budget: fixed demo data for budget and budget level.

#### Contract

* Contract: fixed display of 'consumptionLineList' for supplier contracts.

#### Human Resource

* TimesheetLine/Project: fixed computation of durations.

#### Production

* Manufacturing Order: fixed a issue where a infinite loop could occur on planification if operation had no duration with finite capacity (now displays an error).
* Production order: fixed an issue with the quantities of the generated manuf orders from a sale order.

#### Purchase

* Purchase request line: fixed domain for product.

#### Quality

* Quality improvement: fixed readonly condition for company.

#### Sale

* Sale order: fixed scale and title line issue in message template demo data.
* Sale configurator: fixed issue where formula was not added when verifying the groovy script then directly exiting the pop-up

#### Stock

* Logistical form: fixed unique constraint violation error.
* Stock move: fixed the stock move merge process to work when there are no errors.
* Logistical form: fixed sequence error.

#### Supply Chain

* Stock move: fixed invoiced and remaining quantity in wizard when partially invoicing
* MRP: purchase orders generated by the MRP have the fiscal position correctly filled.


### Developer

#### Stock

Method signature have changed in StockMoveMergingService.class :

```java
public String canMerge(List<StockMove> stockMoveList);
```

became

```java
public List<String> canMerge(List<StockMove> stockMoveList);
```

#### Supply Chain

`UnitConversionService` has been added to the constructor of `StockMoveInvoiceServiceImpl`.

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

[8.2.14]: https://github.com/axelor/axelor-open-suite/compare/v8.2.13...v8.2.14
[8.2.13]: https://github.com/axelor/axelor-open-suite/compare/v8.2.12...v8.2.13
[8.2.12]: https://github.com/axelor/axelor-open-suite/compare/v8.2.11...v8.2.12
[8.2.11]: https://github.com/axelor/axelor-open-suite/compare/v8.2.10...v8.2.11
[8.2.10]: https://github.com/axelor/axelor-open-suite/compare/v8.2.9...v8.2.10
[8.2.9]: https://github.com/axelor/axelor-open-suite/compare/v8.2.8...v8.2.9
[8.2.8]: https://github.com/axelor/axelor-open-suite/compare/v8.2.7...v8.2.8
[8.2.7]: https://github.com/axelor/axelor-open-suite/compare/v8.2.6...v8.2.7
[8.2.6]: https://github.com/axelor/axelor-open-suite/compare/v8.2.5...v8.2.6
[8.2.5]: https://github.com/axelor/axelor-open-suite/compare/v8.2.4...v8.2.5
[8.2.4]: https://github.com/axelor/axelor-open-suite/compare/v8.2.3...v8.2.4
[8.2.3]: https://github.com/axelor/axelor-open-suite/compare/v8.2.2...v8.2.3
[8.2.2]: https://github.com/axelor/axelor-open-suite/compare/v8.2.1...v8.2.2
[8.2.1]: https://github.com/axelor/axelor-open-suite/compare/v8.2.0...v8.2.1
[8.2.0]: https://github.com/axelor/axelor-open-suite/compare/v8.1.9...v8.2.0
