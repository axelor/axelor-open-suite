## [5.4.10] (2021-11-02)

#### Changes

* STOCKLOCATION: Add new configuration to compute stock valuation by purchase value.

#### Fixed

* ACCOUNT MOVE: fix copy feature by resetting more fields during copy.
* Stock Move Line: unit price become readonly when generated from orders.
* Advanced Export: add includeArchivedRecords boolean to manage archived records.
* MRP: Use default procurement method configured in product per company.
* Lead: remove useless action called on lead creation causing issues with permission.
* FixedAsset: Fixed filter on company on Fixed asset category.
* INVOICE LINE: fixed an issue where the 'filter on supplier' field deactivated after a different modification in the form view.
* Account management: Add missing form and grid view to the field analytic distribution template.
* Product: Add unicity constraint on serial number.
* PURCHASEORDER: fixed an issue where purchase orders were wrongfully labeled as delivered.
* Forecast Recap: fix display of bank details last update balance date in form view.
* PRODUCT: Fix barcode image generation for Code\_39 and code\_128.
* Product Category: fix wrong grid view used for parent product category.
* Invoice: fix error happening during the creation of a new invoice after generating an invoice from a purchase order.
* Invoice Payment: fix NPE on payment cancel.
* Stocks: Fixed an issue where dashboards 'Upcoming supplier arrivals' and 'Late supplier arrivals' would either be empty or displaying unrelevant data.
* BANK PAYMENT BATCH: fix java.lang.NoSuchMethodException error when trying to run the batch manually.
* BatchLeaveManagement: Fix an issue where created leave lines had an empty name.
* Batch: Fixed duration which was computed in minutes instead of seconds.
* TimesheetLine: wrong rounded value fixed in working hours computation.
* Sale order line: When adding a pack on a quotation, correctly set analytic, supply and production information.
* Printings: fix printings issues when using an external birt runtime.
* PRODUCT and PURCHASEORDERLINE: fix currency conversion when updating and using last purchase price.
* Extra hours: fix typo in french translation.
* Invoice line: in advance search, fix an error where it was not possible to select the field 'budget'.
* Move: fix rounding issue display on totals during manual creation of a move line.
* Advance payment invoice: prevent refund creation.
* Move: in move grid view, fix NPE when we click on the button 'delete' without any move selected.
* Fix french translation 'Personnaliser' to 'Personnalisé'.

## [5.4.9] (2021-06-08)

#### Changes

* Email: do not block process when an error occurs on sending mail.

Automatic mail notification can be enabled on stock moves, invoices, and
manufacturing order. If we have an error when sending the message, the
process will now not be blocking but will still show the error to the user.

#### Fixed

* ManufOrder: make linked sale order sequence appears a origin in printing.
* Move reversion: Fill reversal date in analytical moveline on reversed move.
* SaleOrder: fix NPE on product selection when the current user does not have an active company.
* Invoice: fix rounding error on advance payment imputation.
* Purchase order: fix display of purchase order lines from menu entry Historical.
* StockLocation: content lines and detail lines are no longer duplicated on copy.
* Manufacturing order: fix an issue where the button to generate waste stock move was not clickable.
* TrackingNumberConfig: Fix sequence being required even if we do not generate automatically a tracking number for purchase.
* Email sending: fix NPE when sending email from scheduler.
* Sale order: fix button to print invoices from invoicing dashlet.
* MRP: fix MRP process being stuck in a loop with wrong mrp line type configuration.
* Invoice Line: Fix filter on supplier in supplier invoice.
* Product: Empty last purchase price on copy.

## [5.4.8] (2021-04-21)

#### Fixed

* Stock move multi invoicing: fix IndexOutOfBoundsException when trying to invoice a stock move with no lines.
* Sale Order: archive stock moves that are automatically cancelled when editing a sale order.
* Purchase order: fix default payment mode when generated from sale order.
* Product company: add database constraint to prevent having multiple lines in a product with the same company.
* Move line export: fix issue when exporting lines with special char in description.
* PRODUCT: fix generation of product variant sequence.
* ADVANCED IMPORT: header visibility fix.
* Invoice: fix printing when cancelling advance.
* Fix unescaped quotes in translation csv causing issue when reloading views.
* MailMessage: fix sender user always being the same for all sent messages.
* Configurator creator attributes: fix issue where `onChange` field could not be emptied.
* Purchase order: fix error due to missing parameter when generating a purchase order printing for an email.

## [5.4.7] (2021-03-17)

#### Changes

* App Mobile: add production related fields.
* CRM: Dashboards improvements:
  - Improve titles and translation.
  - Modify 'Average duration between lead and first opportunity' Chart so the data is shown by months.
* Prod process: description list can now be sorted.
* Timesheet: Auto-fill activity when using generation assistant.
* Account chart: Add 'tax authorized on move line' and 'tax required on move line' values in account demo data.
* Invoice: a product must be sellable/purchasable in order to be selected in a customer/supplier invoice line.
* Invoice printing: Move partner tax number under external reference.
* Sale and purchase orders: Fix number formatting in printings.
* Sale order: Remove autofill of 'Order date' during auto-generation of order.

#### Fixed

* Job position : add missing fr translations in form.
* Accounting move printing: fix issue where lines were duplicated.
* Invoicing project: add missing french translations in form.
* AppBase config: Add missing french translation.
* AppBase config: Add timesheet reminder batch in demo data.
* Configurator creator: fix issue where attributes from a non active configurator model are displayed in others configurators.
* Forecast recap type: fix sale order french translation to 'Commande client' instead of 'Commande'.
* Employment contract: solve export employment contract NullPointerException issue.
* Opportunity: Use next sequence code on copy.
* Move line: fix exchange rate not being computed.
* Employee: add missing french translations of form and report.
* Sale order report: qty column is displayed regardless of the line type.
* Configurator creator: fix on copy issues.
* Job application: add missing french translations in form.
* Leave request: Block the approval when a leave request is already validated.
* Product company: fix weighted average price value when lines are auto generated.
* Vehicle: fix auto fill vehicle in vehicle service log, cost and contract popup and change french translation.
* Training: add missing french translations in form.
* Expense: fix ConstraintViolationException when validating an expense.

## [5.4.6] (2021-02-08)

#### Changes

* TeamTask: Add parent task template and team task category field on task template. Improve task tree creation for project generated from project template.
* ResourceBooking: form view change.
  * Removed 'Computed automatically if left empty' tag.
  * Name is now required.
  * Added missing translation.
* Helpdesk SLA dashboards: add translation and improve filters.
* Helpdesk Ticket dashboards: improve menu, tab and dashboard titles.
* Stock Deliveries dashboard changes:
  * Display country code instead of country alpha code.
  * Change legend name and series name in 'Customer average delivery delay'.
  * Add missing translations.
  * Add Date Range feature in some dashboards.
* Company: add missing translations in company form.
* Sync Contact: Change title to Contact synchronisation.
* MANUF ORDER: add qty and unit field on grid view.
* Stock Location Reporting: change name from 'Bad stock locations line' dashlet to 'Products whose future quantity is less than the minimum quantity allowed'.
* Citizenship: change Fr translation of 'citizenship' menu
* CRM: CRM Dashboards changes.
* PURCHASE MENU: Moving suppliers map under the new Maps menu.
  * Making Suppliers Map a sub menu, and moving under new Menu Maps.
  * Changing Suppliers Map name to Suppliers.
* Human Resource: Change dashboard name to Reportings
* Quality Dashboard: improve dashlets titles.
* Departments: change departments entry menu fr translation.
* HR Dashboards: Improve french translations and hide leave per employee dashlet.

#### Fixed

* OPPORTUNITY: filter out lost opportunities in best open deals dashlet.
* Message: update french translations
* FEC Import: fetch account and journal from the company and code instead of only from the code.
* Partner: Add missing french translation for 'Customer catalog lines'
* Quality: Fix control point dashboard sql error issue.
* ACCOUNT REVERSE MOVE: When generating a reverse move, keep references to analytic move lines.
* Project: add missing translations in project planning user select wizard form
* Menu: Add french translation of 'Partner price lists'
* MRP: Filter out canceled or archived sale order in sale order lines selection.
* User: Change the french translation of 'All permissions'
* Stock move: fix split into 2.

 A stock move generated from split feature now correctly keeps the link to the order that generated it.

* Global tracking log: add missing translations in wizard form.
* Stock Move: fix server error in grid view when sorting by date.
* Cost Sheet Line: Fix rounding issue happening during computation.
* Configurator Creator: prevent the creation of duplicate attribute name.
* StockMove: add french translation of 'Please select the stock move(s) to print'
* EVENT: Hidding past date warning after record is saved.
* TICKET: Fix SLA policy viewer.
* DATA CONFIG LINE: add missing translations.
* Stock Move Line: fix duplicate stock move lines appearing in sale order line delivery dashlet.
* StockConfig: all stock locations are now filtered per their company in the form view.
* COST SHEET REPORT: Hide cost sheet group column in printings when it is disabled in configuration.
* ProjectTemplate: Fix error happening when generating project with users.
* AccountingReport: fix detailed customer balance printing being empty.
* Fix Event calendar tab name's translation.
* ADVANCE IMPORT: add missing translations in advanced import form.
* Move: fix wrong form view opened on reversing a move.
* Team task: Hide 'book resource' button if resource management is not activated.
* StockRules: Filter message template configuration so we can only select stock rules template.
* Stock Move: fix split by unit duplicating stock move lines.
* Convert demo file: add missing translation in wizard form.
* PRODUCT CATEGORY: Add translation for tree view.
* Production batch: add production batch tab french translation.
* Stock move mass invoicing: correctly generate a refund when the price is 0.
* Purchase Manager Dashboard: fix accounting family not displayed in 'Pos volume by buyer by accounting family'
* PROJECT PLANNING TIME: Add translations for editor view.
* Cancel Reason: Add missing french translation for 'free text'.
* Printing settings: add missing translations in form view.
* TEAM TASK CATEGORY: Fix wrong french translation for form view tab.
* Add 'Project/Business (Project)', 'Job Application' and 'Job applications' french translations.
* Data Backup: update missing translations.

#### Removed

* Menu: Remove purchase orders entry menu under reportings.

## [5.4.5] (2021-01-11)

#### Changes

* Project: Add a button to delete team task from task tree.
* Configurator: check the condition before generating sub bill of materials
* ACCOUNTING REPORT: add new filters for analytic reports.
* ACCOUNTING REPORT: add in analytic general ledger origin and description to analytic lines.
* Update spanish translation.

#### Fixed

* Configurator: allow groovy string interpolation in scripts.
* Global tracking: fix script in demo data to avoid NPE.
* Invoice: Set due date readonly when selected payment condition is not free.
* TEAMTASK: Fix type default value.
* FORECAST RECAP: In the forecast recap view, the type of forecast displayed is correct now (before it was always ingoing transaction).
* Move Template: fix french demo data.
* MRP: Fix english typo and add missing french translation.
* MRP PRINTING: fix empty unit column.
* MRP: fix null pointer exception when running calculation on missing procurement method.
* Manuf Order: fix operation order name.

Fix issue where the operation order name starts with null when generated from a production order.
Update operation order name with manufacturing order sequence when the manufacturing order is planned.


## [5.4.4] (2020-12-04)

#### Changes

* Reconcile: change amount field title.
* EMPLOYEE: add french translations for employee resume printing.
* DebtRecovery: Sorting grid based on date, company and partner.

#### Fixed

* Timesheet, Expense: Fix filter after clicking "Show timesheets/expenses to be validated".
* Project grid view has been improved and displays now project progress and status.
* Company: Prevent having twice the same active bank details.
* Reconcile Group: add missing translation.
* Fix opportunity in demo data missing sequence value.
* INVOICE: Add product name translation in printing.
* App Production: add missing translations.
* PRODUCT: fix wrong quantity display in production information "Where-used list", in product form.
* Campaign Reminder: Add missing translation.
* Team Task: Cannot chose a closed project from task project view anymore.
* Stock Move: Generate line for each 'Purchase Qty by tracking' without considering boolean 'Generate new purchase auto tracking Nbr'.
* Configurator: link the generated bill of materials to the generated sale order line even if we do not generate a product.
- Frequency: fix the years of a frequency.
* Configurator Creator: correctly show only the name and the button on a new configurator creator form.
* Demo data: do not set products cost price to 0 when importing bill of materials components.
* INVOICE REPORT: Fixing period format.
* Campaign: Add missing translation.
* Configurator: add missing special variables in script.
* Cost Sheet Line: fix error if the product has no prices configured and is not purchasable.
* Product Company: fix missing values in demo data.
* Campaign: Hide tool button if all child items are hidden.
* Leave request report: Manage the case where there are multiple leave requests for a single day.

## [5.4.3] (2020-11-17)

#### Changes

* MRP: add error log panel.
* USER: Add boolean to display the electronic signature on quotations.

#### Fixed

* Invoice and Purchase Order: Set project to invoice and purchase order lines when generated from sale order.
* PurchaseOrder: Fix error on requesting due to missing production module field in report.
* App: prevent user from deleting or adding an app from interface.
* Invoice: changed error message when ventilating an invoice anterior to the last ventilated invoice.
* FORECAST RECAP LINE TYPE: when the type is changed, the operation type field become empty.
* FORECAST RECAP LINE TYPE: the title Operaton Type is replaced by Operation type and its french translation has been added.
* CONVERT LEAD WIZARD FORM: Add partner information translation.
* ADVANCED EXPORT: Extended selections are not exported.
* Bank Statement Lines: line color is now accurate when importing a bank statement.
* Inventory: Add missing fields in demo data.
* PAYMENT: Fix error message when multi bank details is enabled.
* FORECAST GENERATOR: Copying a forecast generator keeps only the values of fields filled at its creation and resets the other fields.
* Opportunity : Fix sequence on demo data
* Partner: Hide the generate project button in partner contact form view.
* Inventory: Add missing translations, fix header display and add inventory sequence on each report page.
* Stock Move Line: fixed conversion issue when changing values in editable-grid and form view.
* Stock Move: fix location planned quantity not updating on some cases on real quantity change in planned stock moves.
* Prevent NPE on current user when some services were called from a scheduler.
* Analytic Move Line: Change project depending on the state of the parent order or invoice.
* EMPLOYMENT CONTRACT: fixed EmploymentContractTemplate doesn't exist error when printing
* AppCrm: Change french translation of the configuration to display customer description in opportunity.

## [5.4.2] (2020-10-23)

#### Features

* Account: Add import of FEC file exported from accounting export.

#### Changes

* Partner stock settings: add default external stock location.

Add default external stock location in partner configuration, that will be
used as a destination for sales and a from location for purchases.

* EbicsUser: Manage fields visibilty.

In EbicsUser form view display serial number (CORP) and show required
password only if user type is signatory and ebics partner mode is ebics TS,

* TICKET: Addition of color on selection in ticket grid and form view.
* QUALITY ALERT: Addition of color on selection in quality alert grid and form view.
* ANALYTIC MOVE LINE: date and analytic account are now mandatory.
* ManufOrder: Add color to priority field.
* ANALYTIC MOVE LINE: add tracking on all fields.
* DEBT RECOVERY METHOD LINE: make debt recovery level required.
* In Sale order and Stock move, hide/readonly allocation related entities for product type service.
* Project: When generating sale order from project changed name of the generated tab from 'Sale order' to 'Sale quotation'.
* ACCOUNTING MOVE: change the debit and credit field positions in total calculation form view.
* Invoice: set unit price value according to hide discount value for invoice report.
* LEAVELINE: change menu name 'All employees's leave lines' to 'Leave accounts'.
* SALE ORDER: Make visible some fields on sale order finished in date panel.

#### Fixed

* Fix Employees and expenses issues.
  - On kilometric log, the total distance travelled is now updated only if the expense line is added.
  - The kilometric log now has an unique constraint on employee and year.
  - Error message when missing year has been improved to display the year's type.
* ACCOUNTING BATCH: corrected conflict between boolean isTaxRequiredOnMoveLine and closure/opening accounting batch.
* Demo data: fix ICalendar permission that were not working.
* MRP: Stop the MRP computation if a loop in bill of materials component is happening.
* PARTNER: corrected "employee field doesn't exist" after loading a partner if human-resource module is not installed.
* Sale Order Report: fix warning appearing when launching the report.
* Fix `cannot be cast` exception on deleting some objects.
* YEAR: corrected sql error and hibernate error when closing a fiscal year.
* Copy analytic move lines when generate invoice from saleorder and purchaseorder.
* LogisticalFormLine: Fix stock move line domain.

## [5.4.1] - 2020-10-05
## Improvements
- USER FORM: Add search feature on user permission panel.
- Sale Order: Set team according to sale config.
- Stock move: "Refresh the products net mass" button placed in the "Tools" menu.
- Sale Order / Stock Move: remove available status for service type products.
- Declaration Of Exchanges: corrected wrong translation for product type select.
- ACCOUNTING REPORT: Rework fixed asset summary report.
- Stock config: set all the booleans of the stock move printing settings section by default to true.
- Base App Service: Manage correctly timezone in date fields without time using company configuration.
- DEBT RECOVERY CONFIGLINE: Partner category is now mandatory in debt recovery configuration.
- EMPLOYEE: add tracking on most fields.

## Bug Fixes
- Move: Fix NPE on date change.
- BATCH RH: corrected payroll preparation batch, now the batch is runnable.
- Studio: Manage Custom model menu in export / import operation.
- Fix concurrent modification error when adding contact to customer.
- Account management: Fix visibility issue on product and product family fields.
- Stock Move status change: improve performance on cancelling and planning stock move.
- Opportunity: Add sequence on demo data.
- BANK ORDER: Fix NPE when validating a bank order.
- Partner: fix supplier quality rating not being synchronized with supplier quality rating widget in partner form.
- LOGISTICAL FORM: Fix exception translation.
- Invoice Refund: fix refund not updating invoiced state of stock move and orders.
- Logistical form: Remove duplicate status select.
- SaleOrderLine: Not showing picking order info for services.
- FORECAST RECAP: Fix filter on invoices which was using bank details instead of company bank details.
- Tracking number: Fix wrong form view on search.
- Invoice: correctly hide discounts on the printing if the option is active.
- Account Equiv: fix NPE and make accounts fields required.
- Databackup: remove transient fields from backup.
- SMTP Account: the user can now configure the sending email address instead of using the login.
- App Supplychain: Hide configuration 'Block deallocation on availability request' if 'Manage stock reservation' is disabled.
- Stock location line: Fix display issue of button text on popup.

## [5.4.0] - 2020-09-16
## Features
- Add global tracking log feature.
- Update to Axelor Open Platform 5.3.
- Update to Gradle 5.6.4.
- HR: Added a leave line configuration menu in leave management.
- Move template: Add boolean to validate automatically the wizard generated moves.
- Move template: Add journal field to wizard.
- Move template: Add new field description.
- Move Template: Add totals for amount type.
- Move template: Add details button to grid view to display fields.
- Move template: Wizard dataInputList is now an editable grid.
- Move template: Add change track on update.
- Move template: Add demo data.
- Add CSRF protection for timesheet line editor and project planning editor.
- SUPPLIER PORTAL: Creation of supplier portal.
- Reports: Manage locale, date format and timezone following company configuration.
- Product: add products per company to configure different prices depending on the company.
- Studio: Add CSRF protection for every request header.
- Studio: Add support of menu creation for custom model and BPM node.
- Studio: Selection creation/update support.
- Studio: Added a selection builder to update existing selection or to create a new one.

## Improvements
- Invoice: Change buttons color.
- User: add field in user-form to force a password change for the user.
- QUALITY TAG: add field to configure the color of a quality tag.
- LEAD, TICKET, OPPORTUNITY: hide 'Take charge' button in grid view if the assigned user is the current user.
- MRP: hide generate proposal button on mrp line grid when the line is not a proposal.
- PRODUCT: Add json field "productAttrs" displayed in main product form view.
- HRConfig: Import formula variables in demo data.
- Product: add product image in grid view.
- INVOICE : Added the possibility to add a watermark to the printings.
- BPM: Add overview of the result of the wkf.
- MRP: add configuration to ignore end date on incoming mrp line type.
- Bank details: Add new fields journal and bank account.
- EMPLOYEE: set seniority date to hire date by default when hire date is filled.
- DURATION: add new field applicationType to know on what record the duration is used.
- USER: Default User language is based on application.locale from application.properties.
- BASE: Cache memory performance improved by not stocking geographical entities anymore.
- Accounting move line: When creating a new line the default debit or credit is set in order to balance the move.
- Accounting Move Line: When debit/credit is filled the other field is set to 0 instead of being set to a readonly mode.
- Invoice/Orders: The printing filename has been changed to show the id of the printed order/invoice.
- Employee: renamed dateOfHire, timeOfHire, endDateContract, dateOfBirth fields to hireDate, hireTime, contractEndDate, birthDate.
- Removed block permission from demo data.
- SaleOrder/Invoice/PurchaseOrder Line: Unit is now required.
- TeamTask: add new field categorySet to link multiple categories to a team task.
- Studio: Make app builder optional.
- Invoice/Sale OrderReport : Fix unit translation.
- Forecast Recap: default value set to today for from date field.
- Product: Added possibility to add a color to the product variant value tag with the field displayColor.
- Partner: Deleting partner will not remove linked employee.
- Journal: Improve balance calculation.
- Invoice: Addition of new field to display deliveryAddress on form and in report.
- EBICS PARTNER: added tracking to the fields of the ebics partner object.
- Move: It is now possible to change date until the move is validated.
- Employee: added a view to the employee's main contract company in the top right in view form.
- EbicsUser: Enable searching on requestLog dashlet.

## Bug Fixes
- HR: A leave-line cannot be saved whitout employee or leave-reason.
- Lead: Fix city name and state name issue in report printing.
- Studio: Add and fixed attributes of model and fields for import and export app.
- Bank reconciliation: add management of case of several account management for account domain, journal domain, auto change of journal and cash account fields and now account and journal from bank details are prioritized.
- Invoice: Fix NullPointerException when the product is not filled in invoice line.

[5.4.10]: https://github.com/axelor/axelor-open-suite/compare/v5.4.9...v5.4.10
[5.4.9]: https://github.com/axelor/axelor-open-suite/compare/v5.4.8...v5.4.9
[5.4.8]: https://github.com/axelor/axelor-open-suite/compare/v5.4.7...v5.4.8
[5.4.7]: https://github.com/axelor/axelor-open-suite/compare/v5.4.6...v5.4.7
[5.4.6]: https://github.com/axelor/axelor-open-suite/compare/v5.4.5...v5.4.6
[5.4.5]: https://github.com/axelor/axelor-open-suite/compare/v5.4.4...v5.4.5
[5.4.4]: https://github.com/axelor/axelor-open-suite/compare/v5.4.3...v5.4.4
[5.4.3]: https://github.com/axelor/axelor-open-suite/compare/v5.4.2...v5.4.3
[5.4.2]: https://github.com/axelor/axelor-open-suite/compare/v5.4.1...v5.4.2
[5.4.1]: https://github.com/axelor/axelor-open-suite/compare/v5.4.0...v5.4.1
[5.4.0]: https://github.com/axelor/axelor-open-suite/compare/v5.3.12...v5.4.0
