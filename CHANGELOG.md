# Changelog
## [5.3.17] - 2021-11-02

#### Fixed

* ACCOUNT MOVE: fix copy feature by resetting more fields during copy.
* Advanced Export: add includeArchivedRecords boolean to manage archived records.
* Lead: remove useless action called on lead creation causing issues with permission.
* Account management: Add missing form and grid view to the field analytic distribution template.
* PURCHASEORDER: fixed an issue where purchase orders were wrongfully labeled as delivered.
* Forecast Recap: fix the display of bank details last update balance date in form view.
* Invoice: fix error happening during the creation of a new invoice after generating an invoice from a purchase order.
* BANK PAYMENT BATCH: fix java.lang.NoSuchMethodException error when trying to run the batch manually.
* Batch: Fixed duration which was computed in minutes instead of seconds.
* TimesheetLine: fix rounding issues during working hours computation.
* PRODUCT and PURCHASEORDERLINE: fix currency conversion when updating and using last purchase price.
* Product Category: fix wrong grid view used for parent product category.
* Extra hours: fix typo in french translation.
* Invoice line: in advance search, fix an error where it was not possible to select the field 'budget'.
* Advance payment invoice: prevent refund creation.
* Move: in move grid view, fix NPE when we click on the button 'delete' without any move selected.
* Fix french translation 'Personnaliser' to 'Personnalisé'.

## [5.3.16] - 2021-07-08

#### Fixed

* Accounting move printing: fix issue where lines were duplicated.
* SaleOrder: fix NPE on product selection when the current user does not have an active company.
* Stock move multi invoicing: fix IndexOutOfBoundsException when trying to invoice a stock move with no lines.
* Configurator creator: fix issue where attributes from a non active configurator model are displayed in others configurators.
* Invoice: fix rounding error on advance payment imputation.
* DATA BACKUP: Improve exception management & fix cases where the backup failed.
* Forecast recap type: fix sale order french translation to 'Commande client' instead of 'Commande'.
* Move line export: fix issue when exporting lines with special char in description.
* Stocks : Fixed an issue where dashboards 'Upcoming supplier arrivals' and 'Late supplier arrivals' would either be empty or displaying unrelevant data.
* Sale order report: qty column is displayed regardless of the line type.
* Sale order: fix button to print invoices from invoicing dashlet.
* Mrp: fix MRP process being stuck in a loop with wrong mrp line type configuration.
* Invoice: fix printing when cancelling advance.
* LeaveRequest: Block the approval when a leave request is already validated.
* MailMessage: fix sender user always being the same for all sent messages.
* Configurator creator attributes: fix issue where `onChange` field could not be emptied.
* Purchase order: fix error due to missing parameter when generating a purchase order printing for an email.
* Expense: fix ConstraintViolationException when validating an expense.

## [5.3.15] - 2021-02-16

#### Fixed

- TEAM TASK CATEGORY: Fix wrong french translation for form view tab.
- MRP: Filter out canceled or archived sale order in sale order lines selection.
- OPPORTUNITY: filter out lost opportunities in best open deals dashlet.
- ACCOUNT REVERSE MOVE: When generating a reverse move, keep references to analytic move lines.
- User: Change the french translation of 'All permissions'.
- Stock move: fix split into 2.

A stock move generated from split feature now correctly keeps the link to the order that generated it.

- Stock Move: fix server error in grid view when sorting by date.
- Cost Sheet Line: Fix rounding issue happening during computation.
- Configurator Creator: prevent the creation of duplicate attribute name.
- Invoice: Set due date readonly when selected payment condition is not free.
- TEAMTASK: Fix type default value.
- Stock Move Line: fix duplicate stock move lines appearing in sale order line delivery dashlet.
- StockConfig: all stock locations are now filtered per their company in the form view.
- COST SHEET REPORT: Hide cost sheet group column in printings when it is disabled in configuration.
- FORECAST RECAP: In the forecast recap view, the type of forecast displayed is correct now (before it was always ingoing transaction).
- Stock Move: fix split by unit duplicating stock move lines.
- Manuf Order: fix operation order name.

Fix issue where the operation order name starts with null when generated from a production order.
Update operation order name with manufacturing order sequence when the manufacturing order is planned.

- Stock Move Line: fix stock move line split.
- Leave request report: Manage the case where there are multiple leave requests for a single day.

## [5.3.14] - 2020-12-02

#### Changes

- EMPLOYEE: add french translations for employee resume printing.
- ACCOUNTING MOVE: change the debit and credit field positions in total calculation form view.
- USER: Add boolean to display the electronic signature on quotations.

#### Fixed

- SALE ORDER: Display fields in date panel on a finished sale order.
- PurchaseOrder: Fix error on requesting due to missing production module field in report.
- Timesheet, Expense: Fix filter after clicking "Show timesheets to be validated by my subordinates".
- Company: Prevent having twice the same active bank details.
- Fix Employees and expenses issues:
  - On kilometric log, the total distance travelled is now updated only if the expense line is added.
  - The kilometric log now has an unique constraint on employee and year.
  - Error message when missing year has been improved to display the year's type.
- Reconcile Group: add missing translation.
- Fix opportunity in demo data missing sequence value.
- INVOICE: Add product name translation in printing.
- Demo data: fix ICalendar permission that were not working.
- Invoice: display the date of the last ventilated invoice in the error message when ventilating an invoice anterior to the last ventilated invoice.
- FORECAST RECAP LINE TYPE: when the type is changed, the operation type field becomes empty.
- FORECAST RECAP LINE TYPE: title "Operaton Type" is replaced by "Operation type" and its french translation has been added.
- CONVERT LEAD WIZARD FORM: Add partner information translation.
- Team Task: Cannot select a closed project anymore.
- ADVANCED EXPORT: Extended selections are now correctly exported.
- Bank Statement Lines: line color is now accurate when importing a bank statement.
- Inventory: Add missing fields in demo data.
- PARTNER: Fix "employee field doesn't exist" after loading a partner if human-resource module is not intalled.
- FORECAST GENERATOR: Copying a forecast generator keeps only the values of fields filled at its creation and resets the other fields.
- Sale Order Report: fix warning appearing when launching the report.
- Frequency: fix the years of a frequency.
- Partner: Hide the generate project button in partner contact form view.
- YEAR: Fix sql error and hibernate error when closing a fiscal year.
- Copy analytic move lines when generating invoice from sale order and purchase order.
- Inventory: Add missing translations, fix header display and add inventory sequence on each report page.
- Stock Move Line: fixed conversion issue when changing values in editable-grid and form view.
- Cost Sheet Line: fix error if the product has no prices configured and is not purchasable.
- Stock Move: fix location planned quantity not updating on some cases on real quantity change in planned stock moves.
- EMPLOYMENT CONTRACT: fix "EmploymentContractTemplate doesn't exist" error when printing.
- AppCrm: Change the french translation of the field "Display customer description in opportunity".
- LogisticalFormLine: Fix stock move line domain.

## [5.3.13] - 2020-10-08
## Improvements
- USER FORM: Add search feature on user permission panel.
- Sale Order: Set team according to sale config.
- Stock move: "Refresh the products net mass" button placed in the "Tools" menu.
- Sale Order / Stock Move: remove available status for service type products.
- Declaration Of Exchanges: corrected wrong translation for product type.
- ACCOUNTING REPORT: Rework fixed asset summary report.
- Stock config: set all the booleans of the stock move printing settings section by default to true.
- ANALYTIC MOVE LINE: hide date, type and account type in form view opened from a invoice line.
- ANALYTIC MOVE LINE: hide date, type and account type in form view opened from a sale order or puchase order line.

## Bug Fixes
- AccountManagement: Fix NPE when product is missing in invoice line.
- BATCH RH: corrected payroll preparation batch, now the batch is runnable.
- Opportunity: Add sequence on demo data.
- BANK ORDER: Fix NPE when validating a bank order.
- Partner: fix supplier quality rating not being synchronized with supplier quality rating widget in partner form.
- LOGISTICAL FORM: Fix exception translation.
- Invoice Refund: fix refund not updating invoiced state of stock move and orders.
- SaleOrderLine: Not showing picking order info for services.
- Logistical form: Remove duplicate status select.
- Invoice: correctly hide discounts on the printing if the option is active.
- Tracking number: Fix wrong form view on search.
- Account Equiv: fix NPE and make accounts fields required.
- SMTP Account: the user can now configure the sending email address instead of using the login.
- App Supplychain: Hide configuration 'Block deallocation on availability request' if 'Manage stock reservation' is disabled.
- Stock location line: Fix display issue of button text on popup.
- ACCOUNTING BATCH: Fixed issue on closure/opening accounting batch due to a conflict with the configuration to require tax on move line.

## [5.3.12] - 2020-09-16
## Bug Fixes
- SALE ORDER: set end of validity date when creating a sale order from opportunity and project.
- Timesheet Line: fix NPE happening when project or task were emptied.
- BANK RECONCILIATION STATEMENT REPORT: corrected sql request error.

## [5.3.11] - 2020-09-10
## Improvements
- Tracking number: Added product / origin reference on error message when estimated delivery date is null.
- AnalyticDistribution: Autocomplete analyticDistribution details while creating SaleOrder / PurchaseOrder with interco = true.
- STOCK MOVE: Changed error message when trying to invoice a stockmove which has already been invoiced.
- PROJECT: Change financial report BIRT to match conventions.
- PAYMENT VOUCHER: Set default operation type select to 'Customer sale'.

## Bug Fixes
- INVENTORY: Added annual / not annual inventory type select on product demo data.
- BANK STATEMENT: corrected npe on bank details in import file function.
- Accounting report: Add translations to some reports.
- INVOICE: Add 'Periodic contract' sub type viewer in form.
- CLIENT STOCK MOVE: corrected unable to realize if real quantity > to quantity on a stock move line.
- FISCAL YEAR: corrected sql error.
- Sale Invoice Details: Fixed report generation.
- MRP: Fix data rollback on error.
- Account config: Fix translation.
- INVOICE: Changed french translation for Contact.
- Bank reconciliation line: "is posted" is now read only.
- Move line: Correctly display reconcile tab (credit or debit) when the account is reconcilable.
- INVOICE: Fix copy still having references to orders and stock moves.

## [5.3.10] - 2020-08-27
## Features
- CRM: Create new objects Catalog and Catalog type.
- Training session, register: mass register creation.

## Improvements
- EBICSTS: add new data init to bank statement file formats.
- Customer grid: show main address details.
- Forecast Recap: Visual changes to dashlet grid, some recap line type modified, recap take into account sale orders and purchase orders in addition to timetables, also added demo data.
- LEAD: Added description field on reports
- ACCOUNT MANAGEMENT: Change visiblity of product and product family.
- TemplateRuleService: service removed because not used.
- Invoice: Addition of new field to display delivery address on form and in report.
- Mail Notification: Send notification only if activate sending email is true in config & specify tag for message.

## Bug Fixes
- Forecast Report: Fix unit translation.
- ACCOUNTING REPORT: change translation.
- MRP: Shows error message instead of NPE when a product has no unit.
- ICalendar: Fix IndexOutOfBound Exception on event creation & Manage synchronisation select.
- CRM: Add menu industry sector in CRM config and changes in lead form view.
- Batch payroll preparation generation: corrected batch error no session.
- Fiscal year: corrected sql error on close function.
- Accounting batch: put to required mandatory fields for close annual year batch.
- Invoice: on invoice form view corrected display of empty column in invoice payment line grid viewer.
- Invoice line: corrected view error when trying to change an invoice line.
- BANK RECONCILIATION STATEMENT REPORT: change French translation of balance from "balance" to "solde".
- Sale order: removed the behavior of removing stock move with a status different than draft after clicking on edit sale order button.
- Sale Order: Adding Partner name and SaleOrder sequence in Customer credit traceback.
- PRODUCT: Format number of decimal digits for displayed quantities.
- PurchaseOrder: removed action which is setting dotted field explicitly.
- TRACKING MAIL NOTIFICATION: corrected wrong partner receiving email selected.
- MANUF ORDER: Fix number of decimal digit displayed for missing quantity on to consume production product list field.
- Invoice: added invoice reference on traceback when trying to ventilate invoice and exception occurs.
- AccountManagement demo data: sequences are now linked with the account managements.
- Stock Move: allocation on availability request is now per line instead of allocating the whole sale order.

## [5.3.9] - 2020-07-29
## Improvements
- USER: add a dashlet showing all user's permissions.
- EBICSTS: add new data init to bank statement file formats.
- Customer grid: show mainAddress details.

## Bug Fixes
- Report: Fix unit translation.
- TIMESHEET: Fix editor still being displayed even with a disabled config.
- BankDetails: Fix validation flow of iban.
- INVOICE: fix message on generate of subscription invoice.
- ACCOUNTING MOVE: remove all possibility to cancel a validated move.
- ICalendar: Update event synchronization.
- Template Maker: fix selection value translation based on locale.
- SaleOrder: Consider today's date also in invoice sale amount dashboard.
- CRM: set default nbrEmployees to 0.
- Conversion: corrected the case of conversion from void to void and improved message in case of conversion from void to unit or from unit to void.
- Accounting report: corrected bank reconciliation statement report, now display lines reconciled after date report.
- Invoice: Fix wrong translation in report.
- Project: Add missing translations.
- ACCOUNT CONFIG: Add missing translations.
- BANK PAYMENT: fix ICS number being linked to EBICS.
- INVOICE: Fix chart not taking refunds into account.
- INVOICE : Fix subscription invoice sub type change issue on ventilation.

## [5.3.8] - 2020-07-15
## Improvements
- ACCOUNT SITUATION: update account situation on invoice payment cancel and improve moveLine calculation.
- OPPORTUNITY: Change the data type of bestCase and worstCase fields from string to decimal.
- INVOICE: generate invoice from timetable list from purchase order.
- SaleOrder: Change title of numberOfPeriods.
- BankStatementLine: orderBy modification in AFB 120.
- Add tagCount for Expense, Timesheet, Leave request and Extra hours menus.

## Bug Fixes
- EXTRA HOURS: Fix missing translation.
- ACCOUNTING REPORT: Add missing translations in report.
- MRP: Remove duplicate information.
- ACCOUNTING INFORMATION: Add missing translation.
- TeamTaskCategory: Added translation for title of grid view and fields.
- AccountingBatch: Adding partner name in the bankDetails' not active anomaly message.
- Application config: Add missing translations.
- UnitCostCalculation: check NPE on product select.
- SaleOrder: Remove one externalReference which appears twice in grid view.
- AdvancedExport: Fix the bug of records being exported twice.
- INVOICE: corrected the possibility to add a payment when the amount remaining is inferior or equal to 0.
- EMPLOYEE: add first name search in advance search.
- Invoice: Fix multiple invoices not ventilating when generated from sale order with advance payment.
- TRAINING: Added domain to 'Required training' so it does not display itself.

## [5.3.7] - 2020-06-26
## Improvements
- LEAVE REQUEST: remove field duration length constraint
- Studio: Added selection for context field and context field value.
- EMPLOYMENT CONTRACT: Add new sequence on save.

## Bug Fixes
- INVENTORY: prevent having more than one line for the same product and the same tracking number.
- EMPLOYEE: add translation of "Employee PhoneBook".
- CRM/SALES: Contact menu also shows contacts from prospects.
- Mail message: fix an issue where some emails are not being sent in a batch.
- ADVANCED SEARCH: Added display condition on fields using app config settings.
- BANK STATEMENT LINE: change orderBy of afb120 bank statement line grid.
- BANK DETAILS: Display balance in card view and form view.
- Configurator creator: fix demo data import.
- INTERBANK CODE: added translation of Reject/Return code.
- PAYMENT MODE: Fix wrong translation.
- ACCOUNT: Fix wrong move line grid view.
- TIMESHEET: Fix error message showing when there were no mail templates.
- SALE ORDER: Set duration when created from opportunity.
- Advance import: Fix multiple search on same field.
- Bill of Material: Replace NPE by an explicit message to the user when product unit is not configured.
- EMPLOYEE: add button for Employee.rptdesign report on form.
- Employee email address: prevent the selection of existing email addresses.

## [5.3.6] - 2020-05-26
## Improvements
- OPPORTUNITY: add sequence.
- Add civility for partner contact in birt report printing.
- DURATION: rearranged fields in grid and form view.
- Add configuration to activate partial invoicing for sale order and purchase order.
- ACCOUNTING REPORT: Printing Information panel UI light changes.
- PURCHASE ORDER SUPPLIER LINE: fix for decimal digit scale and precision.
- MOVE LINE: removed massUpdate from move-line-grid.
- LEAVE LINE: remove fields length constraints.
- Accounting Partner General Ledger: Manage title visibility.

## Bug Fixes
- CONTRACT: Add filter on payment mode.
- CONTRACT: fix for unit price null on invoice.
- SALEORDER: copy description when generating task by line from sale order.
- Business Project: set toInvoice for timesheet line based on parent task instead of a task.
- FORECAST RECAP: many anomalies resolved.
- Bank details: corrected the possibility to get an inactive bank details.
- Back Order: corrected link between back order and origin.
- DEMO DATA: fixed issue in demo data.
- PARTNER: industrySector is now displayed when customer is individual.
- Fixed asset: corrected amortization calculation process.
- BANK STATEMENT: Fix order by in afb120 bank statement lines.
- ICalendarEvent: make subject, location & subjectTeam field large.

## [5.3.5] - 2020-05-11
## Features
- CONTACT: Add import from Google contact.

## Improvements
- WEEKLY PLANNING: leaveCoef now has default and init values.
- BudgetLine: make dates field required.
- Product: Change french translation of value product in productTypeSelect.
- SUPPLY CHAIN: Improve title and translation of field.
- SALEORDERLINE: Change french translation of 'Available status'.
- ACCOUNT: Take in consideration numbers prefix when generating automaticaly a customer account.
- BANK STATEMENT: Bank Statement ID added in grid view.
- BANK STATEMENT LINE: Bank Statement Reference added on separated grid view and on form view.
- Invoice: made all dashboard's charts and grid based on ventilated invoices.
- DETAIL PER PRODUCT: modify filter of product on details by product form and changed default value for stock managed boolean of product.
- OPPORTUNITY: set canNew false for saleOrderList.

## Bug Fixes
- STOCK MOVE: Fix hiding company on multi company disabled.
- Invoice: qty check on ventilation now deals with different units between invoice line and stock move line.
- Product: Product name is readable even when too long.
- Stock config: Add missing translation.
- App stock: Fix missing translation.
- SALEORDERLINE: Readonly allocate and deallocate button on already delivered lines.
- ICalendar: fix issue when syncing between two dates.
- SALEORDERLINE: Empty available status on already delivered lines.
- Accounting: fix using wrong tax account when ventilating an invoice with reverse charge tax.
- Sale order: fix duration language in report.
- SALEORDER: Fix generating twice the same invoice from subscription sale order.
- BankOderEconomicReason: name is now filled in demo data.
- ACCOUNTING BATCH: Fix NPE on 'close annual accounts' option.
- ACCOUNTING BATCH: Fix issue which did block the save.
- PRODUCT VARIANTS: fix for error in product variant creation.
- STOCK LOCATION LINE: Fix blank screen issue in stock correction process.
- ACCOUNT: Fixed inconsistency of analytic distribution settings and tax settings.

## [5.3.4] - 2020-04-29
## Improvements
- COST SHEET: in batch computing work in progress valuation, compute cost for ongoing manuf orders at the valuation date.
- ACCOUNTING BATCH: add start and end date of realizing fixed asset line.
- Improve consistency of grid and card view menus.
- Stock Move: if present, use trading name logo in printing.
- PRODUCTION: do not hide manufacturing order in operation order form opened in a popup.

## Bug Fixes
- Budget amount: Correctly manage the case of refund invoices.
- USER: fix filter for activeCompany.
- WORKSHOP STOCK LOCATION: Fix config being ignored.
- TeamTask: Set default invoicingType to 'No invoicing'.
- TeamTask: Set toInvoice is true when selected InvoicingType is 'Package'.
- TeamTask: Fix fields disappearing on save.
- PURCHASE: Fix fields where negative values shouldn't be allowed
- SALES: Fix fields where negative values shouldn't be allowed
- Manuf Order: add missing translations.
- STOCK MOVE: Product translation in birt.
- Invoice Line: Fix hidden price field in form.
- Account: Fix incomplete sequences in english data init.
- INVOICELINE: fix hideIf for discountAmount.
- Product form: fix typo in help message.
- Production: add missing translations.
- Studio: Fix custom model's 'formWidth' default value.
- Stock Move: add missing trading name in form view.
- Batch Outgoing Stock Move Invoicing: fix query, process and view.
- Sale Order: set printing settings from trading name when generated from opportunity.
- Inventory: fix error on stock location select when company is not filled.
- Product: Fix NPE while sending the email from product-activity-form,product-expense-form.
- City import: Fix server error during import.
- PRODUCTION: hide workshopStockLocation in grid views based on manageWorkshop configuration.
- EMPLOYEE: Fixed contactPartner full name compute.

## [5.3.3] - 2020-04-14
## Improvements
- PAYROLL PREP: Net salary and social charges fields are now editable.
- MESSAGE: Changed the order of fields Language & Template when sending a message from an object.
- STOCK LOCATION LINE: now shows wap dashlet when accessing line from a product.
- CONTROL POINT: change french translation of name field.
- INVOICE: less margins between the customer name/address zone and the invoice lines table on Birt printout.
- AnalyticMoveLine: Validate total percentage.

## Bug Fixes
- Manuf Order: fix issue when printing multiple manufacturing orders.
When printing multiple manufacturing orders, operations from all orders were printed for each one.
- Purchase Request: Add missing translation.
- Purchase Request Line: fix product domain.
- Availability request: do not ask to allocate stock if the product is not managed in stock.
- Bank statement: fix on import the problem of random bank details chosen by request.
- SUPPLIER INVOICE: supplier invoices to pay can now be selected when activate passed for payment config is disabled.
- Company: Add missing translations.
- OPPORTUNITY: Fix address being incorrect when creating a partner from a lead.
- CostSheet: Add exception when purchase currency is needed in computation and missing in product.
- INVENTORY: Fix NPE on change of product field.
- Partner: Fix customer situation report display value of contact partner jobTitle.
- SaleOrder - PurchaseOrder: generate correct quotation's title according to its status.
- EmailAddress: Fix email address pattern.
- BANK ORDER REPORT: fix the problem of empty report if bank order lines sequences are too big.
- COST SHEET: properly take purchase unit into account.
- Partner: fix view marked as dirty when an archived partner exists with the same name.
- INVENTORY: Fixed an issue whith tracking number where the currrent quantity was not based on the tracking number.
- INVOICE: Company currency is now visible on new invoice.
- Cost sheet: Fix print button being readonly.
- BANK STATEMENT LINE AFB 120: Fix wrong order by in bank statement dashlet.
- Opportunity: Fix email not being duplicated when creating a partner from a lead.
- LEAD: fix function not being displayed in readonly mode.
- PRODUCT: fix position of Variant button.
- Project: fix some fields not being hidden properly.
- PARTNER: hide panels related to invoice when invoice app is disabled.

## [5.3.2] - 2020-04-01
## Improvements
- CLIENT-PORTAL: Chart now only shows invoices corresponding the client partner.
- PARTNER : add url widget for website.
- MrpFamily: make 'name' required field.
- LEAD: new design for view form.
- TARGET CONFIGURATION: end off date must now be superior to start date.
- TAX LINE: run off date must now be superior to start date.
- OPPORTUNITY: removed help in buttons from form view.
- DURATION: remove question mark in type selection title and add translation.
- Sale Order Invoicing Wizard: When searching for already invoiced lines, only takes customer invoices.
- ACCOUNTING REPORT: improved bank statement report.
- Product: set sequence while creating copy.
- MARKETING: end off date must now be superior to start date.
- Improve convert lead wizard form view layout.
- Target Configuration: improve naming & translation.
- Target: improve naming & translation.
- Fixed asset: add EU and US prorata temporis.
- DEMO DATA: add analytic journals to demo data.
- Accounting Report: add the possibility to filter the ledger report to only see not completely lettered move lines.
- LEAD: company name is now more highlighted.
- LEAD: fill region and country automatically on change of city in lead.

## Bug Fixes
- ORDER LINE: add missing french translation for "freeze fields".
- ORDER/STOCK: add missing translation for ISPM15.
- FORECAST RECAP: add translation for Forecast Recap line Type(s).
- SALE ORDER: Fix NPE when interco sale order is being finalized.
- SALE CONFIG: Fixed "Action not allowed" error when we try to update customer's accepted credit.
- TIMESHEET: Fix auto-generation of leave days not generating the first day.
- CLIENT-PORTAL: fixed the NPE when the user does not correspond to any partner.
- Partner: Invoice copy number selection field is now displayed when the partner is a supplier.
- MANUF ORDER: add missing translation.
- STOCK CORRECTION: Add missing translations.
- LEAD: Fix form view of primaryCity.
- EXCEPTION ORIGIN: Split selection values per module.
- PURCHASE REQUEST: Fix new product name not showing on line grid view.
- INVENTORY: add missing translations.
- SALEORDER: Fixed NPE when trying to select a customer with a company with no linked partner.
- BANK RECONCILIATION: corrected error with bank statement load where no other statements were loaded.
- LEAD: Fix "action does not exist" error on LEAD convert.
- MOVE TEMPLATE: Add missing translation.
- STOCK LOCATION: Add missing translation.
- MRP FAMILY: Fix wrong case in views title.
- INVOICELINE: Fix account not filtered depending on fixedAssets boolean.
- CONTACT: fix for Main Company not set for Contact from Partner.
- Account Config: display correct form view on clicking products.
- Stock Move invoicing: Fix NPE on opening invoicing wizard when a line has no product.
- Product: prevent the update of salePrice if autoUpdateSalePrice is disabled.
- Logistical Form: Fix NPE when computing volume.
- WORK CENTER: Fix fields not set to null for specific types of work center.
- MOVE TEMPLATE: Move templates without an end of validity date are now appearing in wizard.
- Fix a french word in an english message file.
- Fixed asset: corrected calculation of amortization.
- Production Order: fix typo in french translation.
- Accounting Situation: fix computation of balance due debt recovery.
- Stock Move: Fix 'Invoiced' tag displaying in internal stock moves and stock move lines.
- Stock Move: empty all references to orders and invoices on copy.
- MANUFACTURING ORDER: On consumed product, no longer display tracking numbers if available quantity equals 0.
- MOVE: Add missing translation.
- Sale Order Report: fix title being shown above address when there is only one address.
- LEAD: Fix display issue for description field on lead event grid view.
- User: Add domain filter on icalendar field in user preferences form view.
- LEAD: Fix error on import demo data.
- LEAD: Fixed the blank pdf when printing.
- BudgetLine: Resolve NPE when trying to validate an invoice with budget lines missing a date.
- EMPLOYEE CONFIG: Export code is now only visible and required if we choose to include the record in the export. Change made for EmployeeBonusType, LeaveReason, and ExtraHoursType.
- TRADING NAME: Fill default company printing settings if trade name printing setttings is not there in company.
- EMPLOYEE: set maidenName visibility by sexSelect select.
- TIMETABLE: Already invoiced timetable are now readonly.
- APP LEAVE: remove unused boolean overtimeManagement.
- EBICS CERTIFICATE: Fix serial number not saved.

## [5.3.1] - 2020-03-16
## Improvements
- InvoiceLine: add company and status fields in advanced search.
- LEAVE REQUEST: Allow sending a leave request in the past.
- CUSTOMER INFORMATIONS: Indicate that payment delay is in days.
- INVOICES DASHBOARD: Turnover is now calculated using both sales and assets.
- PRODUCT: Quantity field digits length is now based on nbDecimalDigitForQty in base config.
- TIMESHEET PRINTING: Manage visibility for task and activity column.
- STUDIO: Add panel on custom model demo data.
- ACCOUNTING REPORT: add account filter to summary and gross value report.
- Accounting Config: clarifying the field lineMinBeforeLongReportGenerationMessageNumber.
- Stock Move Line: Do not allow user to remove allocated stock move line.
- STUDIO: Set default value for form width to large.
- Block the creation of duplicate accounts.
- EBICSPARTNER: mass update on testMode field.
- PURCHASE REQUEST: translate "Purchase Request Lines" in french "Ligne de demandes d'achat".
- Ebics user: Display associated user in list view.
- STOCK CONFIG: Add three boolean fields to configure the display of product code, price, order reference and date in stock move report.
- SaleOrderLine/PurchaseOrderLine: Add transient boolean field to freeze price, qty, productName.
- HR BATCH: set email template for batch 'Email reminder for timesheets'.
- Workflow: Add support to select real status fields.
- QUALITY CONTROL: update the quality control report.
- USER: Add user's email signature, an html text field.
- MESSAGE TEMPLATE: New possibility to add an email signature from a user directly or an email account with a formula.
- SUPPLYCHAIN: In menu stock details by product, company field now autofills with the user's active company.

## Bug Fixes
- INVOICE LINE: add grid view and form view of budgetDistributionListPanel in form.
- PURCHASE ORDER REPORT: Fixed value of payment condition from PurchaseOrder's payment condition instead of using partner.
- EMPLOYEE: update the employee records in demo data so the creation process is finished.
- CAMPAIGN: add exception message on partner and lead at invalid domain in target list.
- SALEORDER: fixed bug causing the margins to be rounded to the unit.
- Fix exception happening when a timesheet reminder batch is run.
- Stock Move Line reservation: correctly set qty requested flag when generated from a sale order line.
- Stock Move: Delete empty date field in form view.
- Advance data import: Fix search issue on main object to import.
- LEAD: removed non persistable field wrongly appearing on the form view.
- LEAVEREQUEST: Fix the NPE when no leave request is selected to be edited.
- Project: Resolve issue in computation of time spent.
- EBICS: Display correctly hash code in certificates EBICS.
- Move: Fix exception message when saving a new record.
- SUPPLIER INVOICE: fix the problem of amount not updated in supplier invoice after use of mass invoice payment function.
- CLIENT PORTAL: Take user permissions into account for TeamTask counters.
- TimesheetLine: Fill duration on data import
- MRP: Fixed issue when user try to copy an existing MRP record.
- LEAVE REQUEST: corrected error when trying to change user.
- Base Batch: Fix the issue when user run calendar synchronization batch.
- LOGIN: Fixed js code page redirection.
- Fix exception happening in sale order line form when group is empty in user.
- DEBT RECOVERY: rollback debt recovery process if to recipients is empty or not in generated message.
- PROJECT: Fix NPE when generate Business project with SaleOrderTypeSelect as title.
- PROJECT: Fix NPE when generate Business project with projectGeneratorType Task by line and Task by product.
- MRP: do not copy sequence on MRP copy.
- TEAM TASK: Fixed issue on copying line from project view.
- INVOICE: Fix quantity and discount not displayed on printing.
- SALE ORDER: Fix unit code not displayed on printing.

## [5.3.0] - 2020-02-25
## Features
- Add Pack Feature in sale order.
- Remove Pack Feature from Product.
- FLEET: Manage rental cars and minor fixes.
- Studio: New features - Label with color, multiline string, grid column sequence, form width, spacer and order by properties.
- Add DMS Import.
- FORECAST RECAP LINE TYPE : create new object ForecastRecapLineType
- JSON-MODEL-FORM : add tracking on json fields
- Export studio app: email action - email template
- Export Studio app: export actions created with meta-action-from
- STOCK RULE: New boolean alert when orderAlertSelect is not alert and stockRuleMessageTemplate added.
- Studio : Added validIf property for custom field.
- Studio: MetaAction and MetaSelect menus with group by on app.
- META-MODEL-FORM: add tracking on json fields.
- CITY: Import automatically from Geonames files.
- MRP: Freeze proposals after manually modifying them.
- Added a global configuration to base app to define number of digits for quantity fields.
- Address: Addition of boolean 'isSharedAddress' in base config to check addresses are shared or not.
- BANK STATEMENT LINE: order by operation date and sequence in AFB120 grid view.
- BANK DETAILS: add search button on bank-details-bank-order-company-grid.

## Improvements
- JOURNAL: new viewer to display the balance.
- SALE ORDER LINE: Display availability status on sale order line grid view if sale order status is 'Confirmed'.
- Map: Filter out the data with empty address.
- Studio: sidebar option for panel.
- Studio: Tab display for panel tab.
- Studio: group by application on json model grid view.
- JSON FIELD FORM: add tracking in form fields.
- ExtraHoursLine: Add new field 'Type' referencing new domain ExtraHoursType.
- Account: Remove DirectDebitManagement.
- MENU BUILDER: Add selection support for icon and iconBackground.
- Custom Model: Hide menu panel and allows to create menu from menubuilder only.
- English language: Correction of errors in english words and change gender job word to genderless job word.
- Action Builder: Added option to update or use json field from real model.
- STUDIO: add 'attrs' for User.
- Studio: Added colSpan,title for the label and  visibleInGrid option for button.
- Studio: Added restriction for model and model field names, allowed only alphanumberic characters.
- Studio: Disable 'Visible in grid' option for spacer.
- STOCK MOVE LINE: display invoiced status at same place as available tag.
- Company: Replace the M2M bankDetailsSet with O2M.
- BANKDETAILS: Add tree and card view for bank details and balance viewer on company bank details.
- BANK STATEMENT: update automatically balance and date of bank details concerned by the bank statement when imported.
- ACTIONBUILDER: Update filter on valueJson and metaJsonField fields.
- MetaJsonField: show sequence and appBuilder field in json-field-grid.
- ACTIONBUILDER: Allow to add a condition at start in generated action-script.
- SEQUENCE: enable tracking for most fields.
- BANK ORDER: Bank order workflow pass from draft to validated when automatic transmission is not activated in payment mode.
- INVOICE: add specific note of company bank details on invoice report.

## Bug Fixes
- Studio: Fix access to json fields of base model in chart builder form.
- Fix "could not extract ResultSet" Exception on finalizing a sale order.
- Studio: Fixed display blank when you click on a field which is out of a panel.
- Studio: Fixed selection filter issue and sequence issue.
- StockMoveLine: Fixed empty popup issue while viewing stock move line record in form view.
- STOCK MOVE LINE: fix $invoiced tag displayed twice.
- LEAVE TEMPLATE: changed field fromDate and toDate name to fromDateT and toDateT.
- MRP: Fix error while generating all proposals.
- UI: Addition of onClick attributes in buttons.
- Sales dashboard: Fix chart not displayed.
- PRODUCT: Fix economicManufOrderQty displayed twice.


[5.3.17]: https://github.com/axelor/axelor-open-suite/compare/v5.3.16...v5.3.17
[5.3.16]: https://github.com/axelor/axelor-open-suite/compare/v5.3.15...v5.3.16
[5.3.15]: https://github.com/axelor/axelor-open-suite/compare/v5.3.14...v5.3.15
[5.3.14]: https://github.com/axelor/axelor-open-suite/compare/v5.3.13...v5.3.14
[5.3.13]: https://github.com/axelor/axelor-open-suite/compare/v5.3.12...v5.3.13
[5.3.12]: https://github.com/axelor/axelor-open-suite/compare/v5.3.11...v5.3.12
[5.3.11]: https://github.com/axelor/axelor-open-suite/compare/v5.3.10...v5.3.11
[5.3.10]: https://github.com/axelor/axelor-open-suite/compare/v5.3.9...v5.3.10
[5.3.9]: https://github.com/axelor/axelor-open-suite/compare/v5.3.8...v5.3.9
[5.3.8]: https://github.com/axelor/axelor-open-suite/compare/v5.3.7...v5.3.8
[5.3.7]: https://github.com/axelor/axelor-open-suite/compare/v5.3.6...v5.3.7
[5.3.6]: https://github.com/axelor/axelor-open-suite/compare/v5.3.5...v5.3.6
[5.3.5]: https://github.com/axelor/axelor-open-suite/compare/v5.3.4...v5.3.5
[5.3.4]: https://github.com/axelor/axelor-open-suite/compare/v5.3.3...v5.3.4
[5.3.3]: https://github.com/axelor/axelor-open-suite/compare/v5.3.2...v5.3.3
[5.3.2]: https://github.com/axelor/axelor-open-suite/compare/v5.3.1...v5.3.2
[5.3.1]: https://github.com/axelor/axelor-open-suite/compare/v5.3.0...v5.3.1
[5.3.0]: https://github.com/axelor/axelor-open-suite/compare/v5.2.5...v5.3.0

