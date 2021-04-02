## [6.0.9] (2021-04-02)

#### Fixed

* INVOICE: allow to delete a draft invoice generated from business invoicing.
* Advanced import: Reset status, file and error log on copy.
* Stock move multi invoicing: fix IndexOutOfBoundsException when trying to invoice a stock move with no lines.
* Sale Order: archive stock moves that are automatically cancelled when editing a sale order.
* Move line export: fix issue when exporting lines with special char in description.
* Project DMS: Add missing french translation.
* PRODUCT: product variant generation sequence fix.
* ADVANCED IMPORT: header visibility fix.
* Invoice: fix printing when cancelling advance.
* Projected stock: Fix projected stock always displaying 'no records found'.
* MailMessage: fix sender user always being the same for all sent messages.
* Configurator creator attributes: fix issue where `onChange` field could not be emptied.
* DebtRecoveryMethodLine: we can now only select message templates that use debt recovery history.
* Purchase order: fix error due to missing parameter when generating a purchase order printing for an email.

## [6.0.8] (2021-03-17)

#### Changes

* CRM: Dashboards improvements:
  - Improve titles and translation.
  - Modify 'Average duration between lead and first opportunity' Chart so the data is shown by months.
* User: Restrict multiple employee creation for same partner in user form.
* Prod process: description list can now be sorted.
* Sale order: Remove autofill of 'Order date' during auto-generation of order.

#### Fixed

* Accounting move printing: fix issue where lines were duplicated.
* Configurator creator: fix issue where attributes from a non active configurator model are displayed in others configurators.
* Forecast recap type: fix sale order french translation to 'Commande client' instead of 'Commande'.
* Substitute pfp validator: add missing french translations.
* Purchase order: fix default payment mode when generated from sale order.
* Invoice: when generated from a purchase order, fill the project from the order to the invoice.
* Sale order line: the project label configuration now is applied for sale order lines.
* Sale order report: qty column is now displayed regardless of the line type.
* ADVANCED EXPORT: Hide 'advanced export' button when there is nothing to export.
* Configurator creator: fix on copy issues.
* Sale and purchase orders: Fix number formatting in printings.
* PurchaseOrderLine: fix NullPointerException while selecting supplier in supplier request panel.
* Expense: fix ConstraintViolationException when validating an expense.

## [6.0.7] (2021-02-25)

#### Changes

* App Mobile: add production related fields.
* Timesheet: Auto-fill Activity when using generation assistant.
* MrpLineType: Add field of application in data init.
* ACCOUNT CHART: Add 'tax authorized on move line' and 'tax required on move line' values in account demo data.
* Invoice printing: Move partner tax number under external reference.
* MANUF ORDER: Add outsourcing field on grid view.
* Team Task invoicing: initialize default invoicing quantity to 1 instead of 0.
* CRM - Partner: The partner reference is not displayed next to the partner name in the contact form view anymore.

#### Fixed

* Invoice: Fix issue where the tax number is missing when the invoice is generated from mass stock move invoicing feature.
* Job position: add missing french translations in form.
* Invoicing project: add missing french translations in form.
* ABC Analysis: Add missing translation.
* ABC Analysis: Add an alert popup for missing sequence before printing.
* Forcast Recap Form: add missing french translation.
* AppBase Config: Add missing french translation.
* AppBase Config: Add timesheet reminder batch in demo data.
* BUDGET LINE: Fix validation on 'To date' and 'From date' fields.
* MESSAGE: fix NullPointException while generating message.
* Employment contract: solve export employment contract NullPointerException issue.
* PurchaseOrderLine: Fix title line being impossible to save.
* Contract: Fix NullPointerException on clicking 'terminate' button.
* Opportunity: Use next sequence code on copy.
* MoveLine: fix exchange rate not being computed.
* Employee: add missing french translations of form and report.
* Accounting reports: fix truncated company name if the name is too long.
* Mrp line type: add missing french translation.
* Invoice: a product must be sellable/purchasable in order to be selected in a customer/supplier invoice line.
* PRINT TEMPLATE: Fix export print template.
* Job application: add missing french translations in form.
* LeaveRequest: Block the approval when a leave request is already validated.
* Import configuration: fix wrong title of import button in form.
* Product Company: fix weighted average price value when lines are auto generated.
* Vehicle: fix auto fill vehicle in vehicle service log, cost and contract popup and change french translation.
* Training: add missing french translations in form.
* Partner: fix database issue 'More than one row with given identifier was found' on partner save.

## [6.0.6] (2021-02-08)

#### Changes

* ResourceBooking: form view change.
  * Removed 'Computed automatically if left empty' tag.
  * Name is now required.
  * Added missing translation.
* FEC Import: fetch account and journal from the company and code instead of only from the code.
* Helpdesk SLA dashboards: add translation and improve filters.
* Helpdesk Ticket dashboards: improve menu, tab and dashboard titles.
* MRP: Filter out canceled or archived sale order in sale order lines selection.
* Stock Deliveries dashboard changes:
  * Display country code instead of country alpha code.
  * Change legend name and series name in 'Customer average delivery delay'.
  * Add missing translations.
  * Add Date Range feature in some dashboards.
* Company: add missing translations in company form.
* Sync Contact: Change title to Contact synchronisation.
* MANUF ORDER: add qty and unit field on grid view.
* Citizenship: change french translation of 'citizenship' menu.
* CRM: CRM Dashboards improvements.
* Departments: change departments entry menu french translation.

#### Fixed

* OPPORTUNITY: filter out lost opportunities in best open deals dashlet.
* Menu: Add french translation of 'Partner price lists'.
* StockMove: add french translation of 'Please select the stock move(s) to print'.
* Project: add missing translations in project planning user select wizard form.
* Message: update french translations.
* TeamTask: Fix timer buttons never displaying.
* Production batch: add production batch tab french translation.
* Partner: Add missing french translation for 'Customer catalog lines'.
* Stock move: fix split into 2.

 A stock move generated from split feature now correctly keeps the link to the order that generated it.

* Global tracking log: add missing translations in wizard form.
* Stock Move: fix server error in grid view when sorting by date.
* Machine: Fix NullPointerException on machine creation.
* Project DMS : Add missing translation
* TICKET: Fix SLA policy viewer.
* DATA CONFIG LINE: add missing translations.
* Stock Move Line: fix duplicate stock move lines appearing in sale order line delivery dashlet.
* Move: fix wrong form view opened on reversing a move.
* COST SHEET REPORT: Hide cost sheet group column in printings when it is disabled in configuration.
* AccountingReport: fix detailed customer balance printing being empty.
* Fix Event calendar tab name's translation.
* ADVANCE IMPORT: add missing translations in advanced import form.
* Team task: Hide 'book resource' button if resource management is not activated.
* StockRules: Filter message template configuration so we can only select stock rules template.
* Stock Move: fix split by unit duplicating stock move lines.
* Convert demo file: add missing translation in wizard form.
* Move: Fix NullPointException error while creating move for doubtful customer.
* SaleOrder: Fix excel report.
* PRODUCT CATEGORY: Add translation for tree view.
* Partner: Fix java.lang.ClassCastException error on saving partner.
* Purchase Manager Dashboard: fix accounting family not displayed in 'Pos volume by buyer by accounting family'.
* PROJECT PLANNING TIME: Add translations for editor view.
* Fix Inventory file import.
* Printing settings: add missing translations in form view.
* Add 'Project/Business (Project)', 'Job Application' and 'Job applications' french translations.
* Data Backup: update missing translations.

## [6.0.5] (2021-01-22)

#### Changes

* Update spanish translation.
* AppBase: Add configuration to use free fixer API.

Add currency conversion functionality for non paid fixer API.

* Change dashlet title from 'Bad stock locations line' to 'Products whose future quantity is less than the minimum quantity allowed'.
* TIMESHEET REPORT: Hide blocked user in user list.
* PURCHASE MENU: Moving suppliers map under the new Maps menu.
* TeamTask: Add parent task template and team task category field on task template. Improve task tree creation for project generated from project template.
* Human Resource: change Dashboard name to 'Reportings'.
* Quality Dashboard: Fix titles and remove 'Control Points' dashlet.
* HR dashboards: Update titles french translations.
* TEAM TASK CATEGORY: Change translation for form view tab.

#### Fixed

* Quality: Fix control point dashboard sql error.
* ACCOUNT REVERSE MOVE: When generating a reverse move, keep references to analytic move lines.
* User: Change the french translation of 'All permissions'.
* Cost Sheet Line: Fix rounding issue happening during computation.
* Add process for export type 'Silae' in Payroll export batch.
* Configurator Creator: prevent the creation of duplicate attribute name.
* Invoice: Set due date readonly when selected payment condition is not free.
* AppBase DATA INIT: Fix axelor-tool dependency module name.
* EVENT: Hide past date warning after record is saved.
* StockConfig: all stock locations are now filtered per their company in the form view.
* ProjectTemplate: Fix error happening when generating project with users.
* INVOICE: In printing, hide qty for title invoice lines.
* Fix english messages in App view.
* EMPLOYMENT CONTRACT SUB TYPE: Use string widget for description field.
* Stock move mass invoicing: correctly generate a refund when the price is 0.
* Cancel Reason: Add missing french translation for 'free text'.

#### Removed

* Menu: Remove purchase orders entry menu under reportings.

## [6.0.4] (2021-01-05)

#### Features

* AppBase: Add currency conversion web service using fixer api.

#### Changes

* Stock Location Line: improve error when updating a line without unit.
* INVOICING PROJECT: Add mass update for deadline date field.
* Project: Add a button to delete team task from task tree.
* Configurator: check the condition before generating sub bill of materials.
* ACCOUNTING REPORT: add new filters for analytic reports.
* USER: Allow to select partner type when generating partner linked to the user.
* ACCOUNTING REPORT: add in analytic general ledger origin and description to analytic lines.
* INVOICING PROJECT: Add total lines for logtimes, expense lines, and team tasks.
* INVOICE LINE: add changes tracking in invoice line.

#### Fixed

* Configurator: allow groovy string interpolation in scripts.
* Global tracking: fix script in demo data to avoid NPE.
* Production Config: Workshop sequence is now only managed when 'Manage workshop' configuration is enabled.
* Followers: When selecting followers in any form view, correctly filter out followers using permissions.
* TEAMTASK: Fix type default value.
* TeamTask: Fix display issue for task dead line.
* FORECAST RECAP: In the forecast recap view, the type of forecast displayed is correct now (before it was always ingoing transaction).
* Refund: on ventilation, fix the printing file name to correctly indicate a refund.
* Move Template: fix french demo data.
* Fix import issues from geonames and new field added for geonames url.
* TEAMTASK: Fix team not filled by default when the task is created from subscribed team's menu.
* MRP: Fix english typo and add missing french translation.
* MRP PRINTING: fix empty unit column.
* MRP: fix null pointer exception when running calculation on missing procurement method.
* Manuf Order: fix operation order name.

Fix issue where the operation order name starts with null when generated from a production order.
Update operation order name with manufacturing order sequence when the manufacturing order is planned.

* Stock Move Line: fix stock move line split.
* Reconcile: Display code of reconcile group.
* Leave request: manage the case of multiple leave requests for a single day.

## [6.0.3] (2020-12-03)

#### Changes

* Inventory: create tracking number on inventory lines import if it does not exist.
* Reconcile: change amount field title
* Project: ProjectVersion can now be linked with multiple projects.
* Configurator: allows to configure the quantity in the generated sale order line when we generate a product.
* Bill of Materials: create copy of components on bill of materials copy.
* LeaveRequest: Change Reason reference type to LeaveReason.
* Configurator formula: add help panel with documentation about script syntax.
* EMPLOYEE: add french translations for employee resume printing.
* DebtRecovery: Sorting grid based on date, company and partner.
* Campaign: Allow to generate targets without filling event information.
* Campaign: Add end date in form view instead of duration for event generation.
* ProdProcessLine: Make work center editable from prod process line form view.
* MRP: change titles of mrp process related menu, grid and form view.
* TeamTask: replace the list of categories by a list of tags stored in a new table for team task tags.
* Business project invoicing update batch: mark timesheet lines as needing to be invoiced if they are attached to a task with a parent that should be invoiced.
* AppProduction: Change french title for 'Manage cost sheet group'.
* Project grid view has been improved and displays now project progress and status

#### Fixed

* COST SHEET: fix the pictures on cost sheet tree view.
* PROD PROCESS: fix required condition in order to save and print record properly.
* Sequence: avoid errors when two users call a service that updates the same sequence at the same time.
* Timesheet, Expense: Fix filter after clicking "Show timesheets/expenses to be validated".
* Company: Prevent having twice the same active bank details.
* Reconcile Group: add missing translation.
* Fix opportunity in demo data missing sequence value.
* BASE APP: Showing mail template associations in a new panel.
* INVOICE: Add product name translation in printing.
* App Production: add missing translations.
* PRODUCT: fix wrong quantity display in production information "Where-used list", in product form.
* Campaign Reminder: Adding missing translation.
* MANUF ORDER: Check if BOM and ProdProcess are applicables when planning or starting a manufacturing order.
* Team Task: Cannot chose a closed project from task project view anymore.
* Stock Move: Generate line for each 'Purchase Qty by tracking' without considering boolean 'Generate new purchase auto tracking Nbr'.
* Configurator: link the generated bill of materials to the generated sale order line even if we do not generate a product.
* ACCOUNT MOVE: set origin for new move line.
- Frequency: fix the years of a frequency.
* Configurator Creator: correctly show only the name and the button on a new configurator creator form.
* INVOICE PAYMENT: fill date using the company of the linked invoice
* Leave request: Fix 'Show leaves to be validated by my subordinates' button issue.
* Demo data: do not set products cost price to 0 when importing bill of materials components.
* SaleOrder: Fill creation date using company.
* BASE APP: Adding missing translations.
* MANUF ORDER: Set manufacturing order's outsourcing from production process's outsourcing.
* INVOICE REPORT: Fixing period format.
* BASE APP: Changing company specific product fields from list to set.
* Campaign: Adding missing translation.
* Configurator: add missing special variables in script.
* INVOICE: fix "Guice configuration errors" error when clicking "update lines with selected project".
* Cost Sheet Line: fix error if the product has no prices configured and is not purchasable.
* MAIL MESSAGE: Use of custom template on mail generated by a comment post.
* Product Company: fix missing values in demo data.
* Campaign: Hide tool button if all child items are hidden.
* Product: Empty last purchase price on copy.

## [6.0.2] (2020-11-16)

#### Changes

* MRP: add error log panel.
* Stock Config: Add inventory valuation type configuration.
* Project: When generating sale order from project changed name of the generated tab from 'Sale order' to 'Sale quotation'.
* PURCHASE REQUEST: Show purchase order with its status and receipt state in follow-up panel and remove purchase orders.
* Add exclude task filter in demo data for job costing app.
* OperationOrder: Modify the machineWorkCenter field name to machine & Resolve calendar color issue.
* USER: Add boolean to display the electronic signature on quotations.
* Refund: the file name now indicates 'Refund' instead of 'Invoice' if we print a refund.
* PURCHASE REQUEST: Two new fields requester and validator added.

#### Fixed

* Invoice and Purchase Order: Set project to invoice and purchase order lines when generated from sale order.
* Stock Move Line: unit price becomes readonly if the stock move is generated from orders.
* PurchaseOrder: Fix error on requesting due to missing production module field in report.
* PROJECT: Add filter for project field in project planning time line editor.
* LEAVE REQUEST: Show validation and refusal fields only if filled.
* App: prevent user from deleting or adding an app from interface.
* Invoice: changed error message when ventilating an invoice anterior to the last ventilated invoice.
* Invoice : Date of the last ventilated invoice is now displayed
* FORECAST RECAP LINE TYPE: when the type is changed, the operation type field become empty.
* FORECAST RECAP LINE TYPE: the title Operaton Type is replaced by Operation type and its french translation has been added.
* CONVERT LEAD WIZARD FORM: Add partner information translation.
* PURCHASE REQUEST: Delete value from status select.
* Project: show task invoicing dashlet only on business projects.
* ADVANCED EXPORT: Extended selections are not exported.
* Bank Statement Lines: line color is now accurate when importing a bank statement.
* Configurator: fix issues in import/export

Add missing product family formula in demo data.
Fix imported attributes of meta json field missing updated configurator creator id.

* Inventory: Add missing fields in demo data.
* PAYMENT: Fix error message when multi bank details is enabled.
* FORECAST GENERATOR: Copying a forecast generator keeps only the values of fields filled at its creation and resets the other fields.
* Opportunity: Fix sequence on demo data.
* MRP form: remove duplicate product panel in filter.
* PURCHASE REQUEST CREATOR: Delete purchase request creator object.
* Partner: Hide the generate project button in partner contact form view.
* Inventory: Add missing translations, fix header display and add inventory sequence on each report page.
* Stock Move Line: fixed conversion issue when changing values in editable-grid and form view.
* Stock Move: fix location planned quantity not updating on some cases on real quantity change in planned stock moves.
* Prevent NPE on current user when some services were called from a scheduler.
* Analytic Move Line: Change project depending on the state of the parent Order or Invoice
* EMPLOYMENT CONTRACT: fixed EmploymentContractTemplate doesn't exist error when printing
* Manufacturing order: Display residual products in report printing only if products exist.
* ADVANCED EXPORT: Add error message when user language is null.
* AppCrm: Change french translation of the configuration to display customer description in opportunity.
* Product: fix bill of materials generated from product form view not appearing in bill of materials list.

## [6.0.1] (2020-10-22)

#### Features

* Account: Add import of FEC file exported from accounting export.

#### Changes

* Partner stock settings: add default external stock location.

Add default external stock location in partner configuration, that will be
used as a destination for sales and a from location for purchases.

* Email: do not block process when an error occurs on sending mail.

Automatic mail notification can be enabled on stock moves, invoices, and
manufacturing order. If we have an error when sending the message, the
process will now not be blocking but will still show the error to the user.

* EbicsUser: Manage fields visibilty.

In EbicsUser form view display serial number (CORP) and show required
password only if user type is signatory and ebics partner mode is ebics TS,

* UNIT: fill automatically the label.

Unit: If the label is empty then it fills automatically with the name.
* MAIL MESSAGE: add demo template for sale order update.
* TICKET: Addition of color on selection in ticket grid and form view.
* QUALITY ALERT: Addition of color on selection in quality alert grid and form view.
* Unit Conversion: make type required and hide other fields when the type is empty.
* ManufOrder: Add color to priority field.
* Period: make from date and to date fields required in form view.
* DEBT RECOVERY METHOD LINE: Debt recovery level is now required.
* In Sale order and Stock move, hide/readonly allocation related entities for product type service.
* TaxEquiv: Make taxes required in form view.
* ANALYTIC MOVE LINE: hide date, type and account type in form view opened from a invoice line.
* Accounting Year: make reported balance date required.
* ACCOUNTING MOVE: change the debit and credit field positions in total calculation form view.
* SOP: Rename categoryFamily field to productCategory.
* PaymentMode: make type and in or out select fields required in form view.
* TraceBackRepository: Remove deprecated constants.
* Invoice: set unit price value according to hide discount value for invoice report.
* LEAVELINE: change menu name 'All employees's leave lines' to 'Leave accounts'.
* ANALYTIC MOVE LINE: made some field mandatory and added tracking.
* IMPORT CONFIGURATION: add a status and process start date and end date.
* Tax: make type required in form view.
* ANALYTIC MOVE LINE: hide date, type and account type in form view opened from a sale order or puchase order line.
* Payment Condition: make type required in form view.
* Account: make account type and company required.
* Fixed Asset Category: make Degressive coef, Computation method and Number of depreciation fields required in form.
* Account Management: Make fields required in view if they are needed for the account management type.
* MoveTemplateType: type is now required.
* AnalyticMoveLine: make type required in form view.
* AccountMoveTemplate: make company field required.
* Move Template Line: Make debit, credit and percentage required in line form.
* INVOICE LINE: make type required in form view.

#### Fixed

* Fix Employees and expenses issues.
  - On kilometric log, the total distance travelled is now updated only if the expense line is added.
  - The kilometric log now has an unique constraint on employee and year.
  - Error message when missing year has been improved to display the year's type.
* SALE ORDER: Make visible some fields on sale order finished in date panel.
* ACCOUNTING BATCH: corrected conflict between boolean isTaxRequiredOnMoveLine and closure/opening accounting batch.
* Account Move: make date and currency required.
* Demo data: fix ICalendar permission that were not working.
* MRP: Stop the MRP computation if a loop in bill of materials components is happening.
* PARTNER: corrected "employee field doesn't exist" after loading a partner if human-resource module is not installed.
* Sale Order Report: fix warning appearing when launching the report.
* Remove hard-coded locale to use the correct locale from the user in some template and export generation processes.
* Fix `cannot be cast` exception on deleting some objects.
* YEAR: corrected sql error and hibernate error when closing a fiscal year.
* Copy analytic move lines when generating invoice from sale order and purchase order.
* BANK ORDER: fix NPE error during file generation.
* LogisticalFormLine: Fix stock move line domain.
* Template: Fix missing action error when marketing module is not installed.

## [6.0.0] (2020-10-05)

#### Features

* PRINT TEMPLATE: create test button to check print template line expression.
* HR: add employment contract sub type.
* PRODUCTION: created buttons in product to create new bill of material and production process.
* Axelor DocuSign: add new module axelor-docusign.
* Axelor Project DMS: add new module axelor-project-dms.
* PRINT TEMPLATE: Rework print template feature.

Add new configurations for print template: print format, sequence, columns
    number, conditions, signature

* TEMPLATE: update template engine: the user can now choose between StringTemplate or groovy.
* MANUFACTURING: Sales & Operation Planning (PIC).
* MACHINE: Implement tool management on machines.
* MAIL MESSAGE: use template object for email generated from a notification message.
* Partner: Add a new partner type 'Subcontractor' and add field related to outsourcing in manufacturing.
* PRINT TEMPLATE: Add XML export and import.
* Production: Manage MPS (Master Production Schedule) process.
* PRINT TEMPLATE: Use Itext instead of birt to generate templates.
* PRODUCTION: Add Master production scheduling charge.
* New changelog management.

#### Changes

* PRINT TEMPLATE: Add header and footer settings in print template.
* Print Template: use locale based on selected language in Template.
* PRINT TEMPLATE LINE: add new field 'ignore the line'.
* Production: machine work center is now a machine instead of a work center.
* MPS/MRP: title before sequence to change depending on the type.
* Use relative path instead of absolute path in configuration file path fields.
* Production: Remove stock location in machine type.
* Project DMS: Move 'Project DMS' menu inside projects main menu.
* MANUF ORDER: Print residual products on report and add panel of residual products.
* PURCHASE ORDER LINE: Replace the min sale price field by a field that indicates the maximum purchase price recommended.
* USER: the admin can now force the user to change password on the next connection.
* Invoice: Add tracking for most fields.

#### Fixed

* Production configuration: fix stock location filter in workshop sequence config line and make the grid editable.
* Quality Alert: Show title of fields description, corrective actions and preventive actions.
* Email message template: remove from address in template.

Setting a custom `from` address per email template is now disabled, as the from
address should depend only on the SMTP account. The `from` should now always
be set in SMTP account configuration.

* LeaveReason: rename `leaveReason` field into `name`.
* JobPosition: Remove character limit on Profile Wanted field.

[6.0.9]: https://github.com/axelor/axelor-open-suite/compare/v6.0.8...v6.0.9
[6.0.8]: https://github.com/axelor/axelor-open-suite/compare/v6.0.7...v6.0.8
[6.0.7]: https://github.com/axelor/axelor-open-suite/compare/v6.0.6...v6.0.7
[6.0.6]: https://github.com/axelor/axelor-open-suite/compare/v6.0.5...v6.0.6
[6.0.5]: https://github.com/axelor/axelor-open-suite/compare/v6.0.4...v6.0.5
[6.0.4]: https://github.com/axelor/axelor-open-suite/compare/v6.0.3...v6.0.4
[6.0.3]: https://github.com/axelor/axelor-open-suite/compare/v6.0.2...v6.0.3
[6.0.2]: https://github.com/axelor/axelor-open-suite/compare/v6.0.1...v6.0.2
[6.0.1]: https://github.com/axelor/axelor-open-suite/compare/v6.0.0...v6.0.1
[6.0.0]: https://github.com/axelor/axelor-open-suite/compare/v5.4.1...v6.0.0
