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

[6.0.4]: https://github.com/axelor/axelor-open-suite/compare/v6.0.3...v6.0.4
[6.0.3]: https://github.com/axelor/axelor-open-suite/compare/v6.0.2...v6.0.3
[6.0.2]: https://github.com/axelor/axelor-open-suite/compare/v6.0.1...v6.0.2
[6.0.1]: https://github.com/axelor/axelor-open-suite/compare/v6.0.0...v6.0.1
[6.0.0]: https://github.com/axelor/axelor-open-suite/compare/v5.4.1...v6.0.0