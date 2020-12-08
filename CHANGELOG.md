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

[5.4.4]: https://github.com/axelor/axelor-open-suite/compare/v5.4.3...v5.4.4
[5.4.3]: https://github.com/axelor/axelor-open-suite/compare/v5.4.2...v5.4.3
[5.4.2]: https://github.com/axelor/axelor-open-suite/compare/v5.4.1...v5.4.2
[5.4.1]: https://github.com/axelor/axelor-open-suite/compare/v5.4.0...v5.4.1
[5.4.0]: https://github.com/axelor/axelor-open-suite/compare/v5.3.12...v5.4.0
