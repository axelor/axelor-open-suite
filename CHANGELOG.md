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

[6.0.2]: https://github.com/axelor/axelor-open-suite/compare/v6.0.1...v6.0.2
[6.0.1]: https://github.com/axelor/axelor-open-suite/compare/v6.0.0...v6.0.1
[6.0.0]: https://github.com/axelor/axelor-open-suite/compare/v5.4.1...v6.0.0
