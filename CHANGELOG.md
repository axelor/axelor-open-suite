## [6.1.5] (2022-02-10)

#### Changes

* Move / Move lines: add functional origin tag in header.

#### Fixed

* ADDRESS: fix zip code not filled in demo data.
* AdvancedExport: filter out transient fields in AdvancedExportLine form.
* GEONAMES: fix NPE while importing cities from geonames.
* PaymentVoucher: Fixed element to pay that was imputed to 0 when changing amount to pay and reloading due element.
* Fiscal Position: fix issue where reverse charge tax lines on purchase order were not correctly generated.
* Stock move: add server side check on status before realizing a stock move.
* Budget: Fix budget lines update on cancelling a purchase order.
* Inventory: Filling inventory line list will now correctly exclude the lines without stock if exclude out of stock option is true.
* Sale Order: Fix transaction issue when generating production order from sale order.
* AppLoader: fix dashletbuilder import with json field of real model.
* INVENTORY: change export filename format to `Inventory_<sequence>_<date>.csv` (for example: `Inventory_INV1220002_20211231.csv`).
* MRP: fix NPE during computation when having multiple BOM levels.
* BPM: Fix terminate and redeploy.

Fix unique process id error on redeployment of bpm diagram.


#### Removed

* PrintingSettings: remove company field from PrintingSettings and remove company condition from all permissions on PrintingSettings.

## [6.1.4] (2022-01-14)

#### Changes

* BPM: Add new property `compulsory` for DMN

The DMN node sometimes require a compulsory result, if no result are returned it should generate an error.
Add compulsory boolean property to make DMN complosory and it's value will be true by default.

* Chart Builder: Add support to open records on click.
* Geonames: Addition of error log while importing geonames.

#### Fixed

* BPM: Fix logo on bpm studio.
* INVENTORY: Fix exception when importing from a csv file with null values in quantity columns.
* Sequence: fixed issue where the sequence count would increment after an exception cancelled the process.
* Product: Remove blank space in purchase/costs tab in product form view.
* Fixed assets: Fix typo in french translation.
* PaymentVoucher: Fixed a bug where amount to pay in elements to pay were not correctly imputed.
* STOCK: Fix filters of stock forecast menu entries.
* Stock reservation: fix german translation of error message.
* Sequence: Fix full name and prefix or suffix issue on data init.
* Invoice: on ventilation, now refund automatically pays the supplier invoice. This was previously only working with customer invoice.
* Meta Translation: Add missing english translation.
* Event: fix NPE happening on start date update.
* Advance import: Correctly apply 'find new' option when it is activated.
* SaleOrder: fix details lines generation when adding a complementary product so the generated lines are displayed inside the title line.
* HR(Timesheet/LeaveRequest/Expense): fix employee selection by correctly filtering out employees with closed employment contract.

## [6.1.3] (2021-12-16)

#### Fixed

* STOCKMOVE/INVOICING: Correctly apply unit conversion when updating invoiced quantity in stock move lines.
* Move: fix wrong currency amount and rate computation in VAT move line.
* SALE ORDER: Blocking invoice generation when the full amount of the order has already been invoiced.
* Sale Order: Display discounts on reports even if the discount is positive.
* Translation: Remove wrong fr translation for 'Supplier arrivals' axelor-business-project module.
* Move: display status tag when editing move.
* PaymentVoucher: Fix concurrent update error message after modifying an element to pay in payment voucher.
* SaleOrderLine: Fix invoiced quantities computation when having refunds.

changed the method of checking invoiced or delivered quantities whenever quantity on a sale order line is changed

* Automatic creation of account configuration on payment modes.
* PERIODS: Removed overlapping periods in demo data.
* Payment Voucher: improve error message displayed when deleting a payment voucher linked to a move.
* Bank Statement: bank statement can now be deleted without having to delete linked bank statement lines.
* Geonames: Check the type of the uploaded file before manual import.
* Sequence: Take the most recent sequence version when there are multiple ones to be picked.
* STOCK DETAILS: Fixed issue where stock locations were sometimes not properly filtered.
* Disable new button on bank-details-cards.
* Budget: Fix budget lines update on cancelling an invoice.

## [6.1.2] (2021-11-19)

#### Fixed

* Payment Voucher: Removed new button on payment voucher element lists in payment voucher form.
* Payment Voucher: Changing partner will now empty list of invoice to pay and reset list of due element.
* Payment Voucher: Close button in paymentVoucher opened from invoice no longer creates a draft payment voucher.
* LeaveLine: Fixed demo data where leave lines did not have a name.
* Human resources: restored chart view for leave per employee.
* PRINTING SETTINGS: Fix display error when pdfHeader is not filled.
* Expense Line: Prevent negative and inconsistent amounts in expense lines.
* PRODUCTION/STOCK: Add missing / fix wrong translations.
* INTERCO: Fix analytical distribution model of generated interco invoice.
* Partner relations types: correctly manage translation for partner relation types labels.

## [6.1.1] (2021-11-05)

#### Changes

* Group: add a new configuration to allow or forbid grid customization per group.
* Product Category: fix wrong grid view used for parent product category.
* ACCOUNTING SITUATION: Add filters on customer account and supplier account.

#### Fixed

* ACCOUNT MOVE: fix copy feature by resetting more fields during copy.
* PaymentVoucher: Fix remaining amount update when updating amount in imputation lines.
* Improve module application cards design: fix spacing and margin cards for better UI/UX.
* FORECAST GENERATOR: fix issue when generating outgoing forecast.
* Advanced Export: add includeArchivedRecords boolean to manage archived records.
* Lead: remove useless action called on lead creation causing issues with permission.
* Account management: Add missing form and grid view to the field analytic distribution template.
* Forecast Recap: fix display of bank details last update balance date in form view.
* ANALYTIC DISTRIBUTION TEMPLATE: add missing french translation for an error message.
* Accounting Report: fix custom accounting report xlsx format.
* Invoice: fix error happening during the creation of a new invoice after generating an invoice from a purchase order.
* BANK PAYMENT BATCH: fix java.lang.NoSuchMethodException error when trying to run the batch manually.
* BatchLeaveManagement: Fix an issue where created leave lines had an empty name.
* Batch: Fixed duration which was computed in minutes instead of seconds.
* Contract: Fixed quantity scale not using config when generating an invoice from a contract with prorata.
* PRODUCT and PURCHASEORDERLINE: fix currency conversion when setting last purchase price as product cost price.
* Sale Order: correctly hide availability request label when stock module is not included.
* Product: fix stock details never showing any stock location line.
* Move: in move grid view, fix NPE when we click on the button 'delete' without any move selected.
* Fix french translation 'Personnaliser' to 'Personnalisé'.

## [6.1.0] (2021-10-14)

#### Features

* BPM: Axelor BPM feature

New module that allows management of business processes by using BPM2.0 standard.
It adds features to create, deploy, run, edit and monitor the entire business process.

* TRADING NAME:
    * Add list of trading names that can sell / buy on a product, add configuration to filter the products available on sale order / purchase order lines based on trading names.
    * In purchase and sale orders, add default stock location depending on the trading name.
    * Add new quality control default stock location in trading name.
    * Debt recovery: Added the possibility to handle the debt recovery process by trading name instead of by company.
    * PARTNER: Add the possibility to link a partner to a trading name.
    * Upgrade the accounting entries to be able to distribute charges and turnover by trading name.
    * Add trading name on the user so the trading name is filled by default.
* Debt Recovery: Add a printing for payment reminder.
* Add type to Deposit Slip.
    * Cheque deposit slip is now Deposit slip with a selectable type.
    * Available type are Cheque, Bill of exchange & Cash.
* Partner: Generate partner VAT number from registration code.
* Partner: set automatically the siren and nic from registration code.
* Add Project task editor.
* EMPLOYEE : Implement new employees and former employees feature
    * Add "Leaving date" on Employee.
    * If this date is in the past or if the hire date is in the futur, exclude this employee from automatic process.
    * Modify Employee's form and card view to quickly see if an employee is former or new.
    * Add three new advanced searchs based on this new field.
    * Updated demo data to fill this new field.
* Stock Move: New fields to block invoicing.

    In stock move form, add fields to configure a temporary or definitive
    blocking of invoice generation for the stock move.

* ProductionOrder: Status

Add support for Production order Status : Draft, Planned, Started, Cancelled, Completed.
Update it according to related manufacturing orders status.

* Budget : Addition of budget line computation button
    * Allow to compute budget lines with a click of button.
    * Filtering of those budgets having status validated and budget lines not empty from Invoices and Purchase Orders.
* PRODUCT: Added the possibility to define complementary products to add to sales when selecting a product.
* Manufacturing order: Add the possibility to merge manufacturing order:
    * In manuf order, we can now merge manuf order into one.
    * For that we need to have the same product, workshop, and the draft or planned status.
    * We add a merged status.
    * On the merged manuf order we can see the list of manuf order that were merged.
    * On each of the manuf order that were merged, we can see the result of the fusion.
* STOCK MOVE: validation step for invoicing outgoing stock move:

Add new invoicing status for move line: 'delayed for invoicing' and 'validated for invoicing'.

* Fixed assets mass validation: it is now possible to mass validate fixed assets through their grid view.
* PRODUCT: created new bill of materials tree view for product.
* SaleOrder: Update pack feature in sale order.
* Message: Add SMS message type.
* Create new model: WorkCenterGroup that will have a list of work centers. Possibility to create a template to reuse them at anytime. They will be used on production process lines.
* PRICELIST: Add non-negotiable field which prevents editing price, unit and discount in sale order lines.
* Refactor message templates method to use platform template api: It enables full use of custom models and fields (from studio module) on the template.
* MRP: Add two new informative fields in mrp views, ideal and reorder quantities from the configured stock rules.
* Add partner relations/delegations for invoicing and delivery:

Add a new configuration to enable the use of different partners for invoicing and
delivery than the main partner.

* Partner: Add default partner category in company configuration.
* Add max discount in product category

Add a new field in product category allowing to force a maximum amount of
discount. In sale order line, the user cannot fill a discount superior to
this maximum except if the line is explicitly allowed to do so.

* SOP: Added a panel to generate SOP lines

In the sop form we can now generate the lines with the periods of the Year
specified. We can also compute sale forecast on sales history with a given year.

* Add maintenance module:

Add a new module which was previously in axelor-addons. The module add the
maintenance app, which will add new fields and new types to production
processes, bills of materials and manufacturing orders.

* Company: Add partner and workshop list
* Add EDIFACT import/export interface to unit, tax, payment mode and payment condition.
* Manufacturing Orders multi level planning:

There is now a button on manuf orders to recursively generate all sub manuf orders in case there is sub bill of materials.

* ACCOUNTING REPORT : Add custom report types
* ORDER AND PARTNER: Free shipping is available in shipment mode
    * Add possibility to use free shipping with shipment mode.
    * Orders that use shipment mode with free shipping add automatically an order line with the shipping cost.
    * If the order amount is greater than the free shipping threshold, the order line of the shipping cost is removed.
    * Partners that are not suppliers can override the shipment mode shipping cost and free shipping threshold.
* HR: Employment contract management

Manage following configurations related to employeement contract management:
Collective agreement, Qualification, Qualification level, Paygrid, Employments, SMIC value,
Contract types, Amendment types, Probation periods,
Health mutual rejection reason.

* Partner & SaleOrder: Manage complementary products for partner, generating sale order lines with configured products on every new sale order.
* MOVE: add new values for functional origin: Sale, Purchase, Payment, Cut Off and Fixed Asset.
* MetaMenu, MenuBuilder & ActionBuilder : Allow to edit xmlId.
* Custom app style: Add String field to specify custom css style in base app configration.
* Studio : New UI with real view attributes value change
* Studio: Mapper to create new single record from another record.

The mapper allows to map fields between source and target model.
It is used to create a new record or to update existing one.
It can be executed from any button by using single common action.

* SelectionBuilder: Add support to export and import with app builder.


#### Changes

* Config: add configuration to choose default stock, sale and purchase units for new products.
* MRP : update the date on the manually updated line

When we manually update a quantity on a proposal and run again the mrp calculation
if the mrp line date is in the past, it updates with the today date.

* Add MRP Alert on days between firm and proposal purchase

Add a new field on MRP family to define the minimum days between firm and proposal purchase before showing an alert icon on Mrp lines.

* MOVE: change status Daybook to Accounted.
* ProjectTask: Add parent task template and project task category field on task template. Improve task tree creation for project generated from project template.
* Purchase order line: add warning message if the supplier by default of the product is different from the supplier on the order.
* In move line form view add a dashlet to display associated bank reconciliation lines.
* In bank statement form view add a dashlet to display associated move lines.
* TRACKING NUMBER: add new fields origin and note in tracking number.
* ProjectInvoicingAssistantBatch: Add deadline date and fill it on generated invoicing project's deadline date.
* DocuSign: Confirm sale order signed with DocuSign.
* Template Message: It is now possible to send an email from the template without saving the message.
* Automatic stock reservation for manufacturing orders components

Add a new configuration in supplychain to automatically request reservation
for consumed stock move lines.

* Sales: Add 2 new charts in customer dashbord (from sales):
  * Average markup per customer
  * Avergae margin rate per customer
* Sales: On the customer form view added a new indicator of products bought by the customer.
* Sale Order: new config to use delivery date for reservation.

It is now possible to choose if the date used to decide reservation priority
is delivery date or confirmation date with a new configuration in
supplychain.

* SALE: Remove 'Sales details' dashboard and add 'Average margin rate per customer' and 'Average markup per customer' dashlets on 'Sales manager details' dashboard.
* Change all static reference of file upload dir with dynamic reference
* Invoice Purchase dashboard changes:
    * Removed Unpaid Supplier's Invoices dashboard.
    * Removed Unpaid Invoice Bar from Invoice Purchase situation (tax incl.).
    * Added translation to Invoice Purchase situation (tax incl.).
    * Added Date Range feature in some dashboards.
* Invoice Sales dashboard changes
    * Changed the name of dashboard from 'Revenue Generated by Geographical Region' to 'Reveue generated by country'
    * Removed Ref. Unpaid bar from Invoice and Refunds Sale situation (tax incl.)
    * Removed Unpaid Inv. bar from Invoice Sale situation (tax incl.)
    * Added missing translations.
    * Added Date Range feature in some dashboards.
* Cash Management: changes and corrections
    * Added sequence for Manual Forecast.
    * View changes in Forecast grid and form.
    * Change Forecast Recap Line Type element from Forecast to Manual Forecast.
    * Consider all types of Forecast Recap Line Type of Invoice element while populating.
    * View changes in Forecast Recap form.
    * Fixing translations.
    * Showing Balance instead of Amount in Forecast Recap report.
* Lead: Remove tax field and allow to create new records for configuration fields (like city or state) directly from the form view.
* Date and number are now formatted according to user locale in viewers.
* Product Company: add purchasable and sellable fields per company.
* ProdProcessLine.WorkCenterGroup : Feature and corrections
    * Allow to create Work Center Group(not template) from Prod Process Line.
    * Allow to create, edit & remove work centers in Work Center Group from Prod Process Line.
    * Showing machine column for every Work Center Group type.
* Separate ProjectTask from TeamTask.
* Product Catalog Report: Display only products which are showed in the view.
* Configurator: the configurator can now fill lists in generated product or sale order line.
* MRP: Use left to consume qty for manuf order need.
* Add last purchase date to Product which will be set while validating a Purchase Order with this Product. Same logic as last purchase price.
* OPERATION ORDER: display related production  process line description in operation order form.
* ACCOUNT MOVE: Show reconcile group letter in move line list.
* MRP: creation of the field 'supplierPartner' on MrpLine, it take the default supplier partner of the product and becomes editable to choose an other supplier form the catalog of the product.
* Chart: Change in title and search fields for salesman dashboard.
* Invoice: set unit price value according to hide discount value for invoice report.
* Generate sub manufacturing orders from sale order

If the option is activated, we now generate also sub manufacturing
orders on confirming a sale order instead of only creating the
parent manufacturing order.

* Mrp: In MRP purchase proposal lines, displays a warning if the purchase date would be too late due to supplier delay.
* Job Application: Improve job application grid and kanban view
    * Addition of resume MetaFile m2o field to job application.
    * Addition of button in grid view to show resume.
    * Addition of kanban view to job application which links to resume.
* Sale order: In a sale order, add a message when the external reference is already used in another sale order.
* Partner: Add registration code to demo data.
* SUPPLIER PORTAL: Improvements on supplier portal view.
* UNIT CONVERSION: add help text in form view.
* Accounting report: simplify which fields are displayed and required to generate journal entry export.
* Configurator: allow to fill many-to-many fields from a configurator.
* PAYMENT MODE: reset bankOrderFileFormat field on orderTypeSelect change in form.
* Partner: main activity is now a separated entity instead of just a code.
* SaleOrderLine: description in title line is now displayed.
* STOCK LOCATION: Add a boolean field to display or not virtual stock location lines in 'Location Lines' and 'Stock location details'.
* Address: when creating a new address, the default country is now determined from the logged user active company instead of always selecting France.
* MRP: Use correct quantity for purchase proposals when the quantity needs to be a multiple.
* Demo data: complete demo data with new products, accounting and app configurations.
* INVOICE PAYMENT: add type in invoice payment form.
* SALEORDER / EVENT: set more fields of event generated from SaleOrder.
* Extra hours: make extra hours line list readonly except for draft and confirmed status.
* Invoice: Add original invoice reference to refund printing.
* Unit Conversion: add data demo of a conversion using a script.
* Mrp: add possibility to select lines for proposal generation.
* ManufOrder: "plan only missing quantites" is now activated by default on manufacturing order form.
* Configurator: remove save when changing fields value in form view to improve performance.

#### Fixed

* Print template: add missing translations to form and grid views.
* PRODUCT: fix product grid price display for purchase, sale, stock and production menus.
* Resource reservation: Error message when reserving an already reserved resource.
* MRP: Link purchase order to the sale order when the purchase order is generated from MRP.
* DataBackup: avoid duplicate issue when reimporting a backup.
* DataBackup: Fix data backup stuck in 'In progress' status when exception occurs.
* APP BUILDER: fix wrong french translation in menu builder.
* Dark theme: fix focused item in suggestbox and phone country list.
* Studio: Add modules field on app builder

Just like the string field 'modules' on app, adding modules field on app builder allows to create real app properly.

* DataBackup: Manage class not found and required fields issue and logging it.
* SaleOrderLine, InvoiceLine: Reduce render delay while opening the view
* Studio: Fixed selection builder export and import with app builder.
* Accounting reports: fix truncated company name if the name is too long.
* Replace all `__date__` occurrences by a global `__config__.date` configuration to fix timezone related issues.
* Price list: exclude price list that are defined on exclusive partner price list

In price list, we fix the display to exclude list that are defined on an exclusive partner price list

#### Removed

* Removed Work center list from production module.
* Removed invoicing general reportings.
* Studio: On addition of axelor-bpm module the old Wkf and related models were removed.
* DebtRecoveryLevel: Remove object and replace references with integer sequence.
* Moved axelor docusign module from Axelor Open Suite to Axelor Addons repository.


[6.1.5]: https://github.com/axelor/axelor-open-suite/compare/v6.1.4...v6.1.5
[6.1.4]: https://github.com/axelor/axelor-open-suite/compare/v6.1.3...v6.1.4
[6.1.3]: https://github.com/axelor/axelor-open-suite/compare/v6.1.2...v6.1.3
[6.1.2]: https://github.com/axelor/axelor-open-suite/compare/v6.1.1...v6.1.2
[6.1.1]: https://github.com/axelor/axelor-open-suite/compare/v6.1.0...v6.1.1
[6.1.0]: https://github.com/axelor/axelor-open-suite/compare/v6.0.15...v6.1.0
