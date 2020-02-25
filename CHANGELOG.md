
# Changelog
## [Unreleased 5.3.1]
## Improvements
- CUSTOMER INFORMATIONS : Indicate that Payment delay is in days

## Bug Fixes

## [5.3.0] - 2020-02-25
## Features
- ACCOUNTING REPORT : group by and subtotal of analyticDistributionTemplate.
- INVOICE : created new field payment date in invoice in order to use it in advance search.

## Improvements
- STOCK RULE : add comment field
- Sale Order: Desired delivery date is used to generate stock move if estimated date is empty.
- BILL OF MATERIAL : display product field before the production process field.
- UNIT CONVERSION : used large width for unit conversion form view.
- LEAVE REQUEST : change in email template.

## Bug Fixes
- MANUF ORDER : Display sale order comment in manufacturing order printing.
- Invoice payment: fix issue in invoice payment form when invoice due date is empty.
- AppServiceImpl: Fix open resource with try-with-resources
- ObjectDataExportServiceImpl: Fix open resource with try-with-resources
- ImportAccountChart: Fix open resource with try-with-resources
- DataBackupCreateService: Fix open resource with try-with-resources
- MRP: Desired delivery date in sale/purchase orders is used when estimated date is empty.
- EMPLOYEESERVICE: Fix computation of working days.
- StockMove : Fix issue of generated invoice with empty invoiceLines
- DEBT RECOVERY : Don't create debt recovery line if no email address in debtRecovery.
- SaleOrderInvoicing : impossible to InvoiceAll if one invoice has been already generated
- Invoice: fix error on ventilation when sequence reset is per year.
- PROJECT : Replace required attribute on code field with readOnly if generateProjectSequence is true
- Stock Move: Do not modify wap when generating a new line in customer return linked to an order.
- REPORTS: Fix issue for reports which split the report on many tab on excel.
- PRODUCT : display button 'Update stock location' only for storable and stock managed products.
- ADDRESS : addressL4 is emptied when zip is filled
- INVOICE Report : Fixed issue in displaying proforma invoice comment from grid button

## [5.2.4] - 2020-02-05
## Improvements
- BankOrder: Display of Signatory ebics user and Sending date time in report.
- ACCOUNTING REPORT: new filter for analytic distribution.
- Timesheet: alert to check manufOrder is finished or not on timesheetLine.
- PaymentMode: Add sequence field on account settings grid view.
- Stock Move Line: store purchase price in stock move line to use this information in the declaration of exchanges.
- INVOICE: add specific note of company bank details on invoice report.
- Message: Improved performance when generating mail messages from templates.
- ACCOUNTING CUT OFF: display warning message when batch has been already launched with the same move date.
- BANKPAYMENT: Update condition to display field ics number.
- PURCHASE REQUEST: add new tab to see related purchase orders.
- ANALYTIC MOVE LINE: add id and move line to analytic move line grid.
- Subrogation release: improved visibility of unpaid invoices.
- INVOICE: Filling number of copies for invoice printing is now required.
- Stock Move: stock reservation management without sale order.
- Manuf Order: manage stock reservation from stock move.
- Invoice: Add control to avoid cancelation of ventilated invoice.
- BALANCE TRANSLATION: Translate "Balance" in french by "Solde".
- EXPENSE: add new printing design.
- Invoice printing: remove space between invoice lines without description.
- INVOICE: Add translation for "Canceled payment on" and "Pending payment" and update list of payment viewer in invoice form.
- Configurator: generate bill of material when creating a sale order line from a configurator.

## Bug Fixes
- INVOICE: Fixed payment mode on mass invoicing refund.
- MESSAGE: correction of sending a message and correctly update status of message.
- BankOrder: change translation of partnerTypeSelect field.
- Account: Add missing translations.
- BankOrder: Fix domain issue of signatoryEbicsUser field.
- INVENTORY: Fix issue of realQty when copying inventory.
- TIMESHEET: Remove leave days and holidays when changing end date.
- CAMPAIGN: Fix filter value each time changes while Generating targets from TargetList.
- Configurator: Fix sale order line not being created from a configurator.
- Configurator: Generate bill of material on generating a sale order line from a configurator.
- SaleOrderLine: Hide qty cell in SaleOrder report when the sale order line is a title line.
- INVOICE: now the date verification of the ventilation process depends of invoices of the same company.
- MOVE: corrected sequence generation, now use correctly the date of the move and not the date of validation.
- Stock Move partial invoicing: manage correctly the invoicing status when we refund the invoice.
- ANALYTIC REPORT: fix issue where wrong reports were printed.
- ACCOUNTING REPORT: improved analytic general ledger.
- Subrogative release: corrected the possibility to create two subrogation transmitted or accounted with the same invoices.
- Configurators: fix issue in configurator menu with latest AOP version.
- Stock Move: Do not modify wap when generating customer return.
- ADDRESS: Fix error message when clicking on ViewMap Btn of a new address.
- Configurator BOM: Correctly make the difference between components and sub bill of material.
- ACCOUNTING REPORT : corrected several issues with values on the summary of gross values and depreciation report.
- ACCOUNTING REPORT : in the summary of gross values and depreciation report corrected the problem of the apparition of line with an acquisition date after the report dates.

## [5.2.3] - 2020-01-23
## Features
- INVOICING PROJECT: consolidation of invoicing project.
- INVOICING BATCH: consolidation of phases.

## Improvements
- Invoice: Removal of companyBankDetails comment in invoice form.
- BANKSTATEMENT: import multiple records in a single line.
- Opportunity: set sale order defaults on new.
- Typos in PurchaseRequestLine domain file.
- PurchaseRequestLine: cacheable removed for this entity.
- BANKSTATEMENT: on copy reset statusSelect.
- STOCK: fromAddress in stock-move supplier arrival is now required.
- CARD VIEWS: Display non square images with the right proportions.
- InvoiceLine: filter on taxLine.
- EBICS : Support of ARKEA bank:
Defined the KeyUsage attribute as critical on self-signed certificate.
Defined the KeyUsage attribute with KeyEncipherment on Encryption certificate.
- EBICS : Support bank statements of LA BANQUE POSTALE bank:
This bank return the code `EBICS_OK` instead of the correct code `EBICS_DOWNLOAD_POSTPROCESS_DONE` when we fetch a file (HPD, FDL...)
In this case, the file is correctly retrieved from the bank server, but not saved in database. Now we don't throw an exception when the return code is `EBICS_OK`.
- Add checks in services on app configuration, now more services are only called if the corresponding app is enabled.

## Bug Fixes
- Sale Order Invoicing: take in consideration refund invoice when checking the invoiced amount.
- LEAVE REQUEST: Updated calendar filter.
- Forecast recap: Displaying selected value's title instead of value on error message.
- MOVE REMOVE SERVICE: corrected error that occurred when several lines were found.
- BANK ORDER: the date field is now again read-only on bank order generated automatically.
- Advanced Import: Fix config line import.
- MOVE LINE: removed the possibility to delete a move line in a move when the move line is reconciled.
- SUBROGATION RELEASE / INVOICE: corrected npe apearring in log when opening a new subrogation release.
- FIXED ASSET: set Deprecation Date in Move generated from FixedAsset and fix last Day Of Month FixedAsset.
- Invoice: Fix wrong attribute name used in grid views.
- Ebics Partner: set editable for bank order services list.
- INVOICE: Fixed invoice refund ventilation issue.
- EBICS BANK: Fixed typo issue on form view on the X509 fields.
- DataBackup: DataBackup non-persistable class issue Fix.
- FIXED ASSET: correction of prorata temporis.
- MULTI INVOICING: add control on generateMultiInvoiceBtn when invoice has already been created.
- INVOICE PAYMENT: fix an issue where an invoice payment is taken into account twice.
- GEONAME: fix city import.

## [5.2.2] - 2020-01-09
## Features
- TOOLS: added utility class for interacting with SFTP.
- PAYROLL PREPARATION: add new Payroll Preparation Export type "SILAE".
- Advance data import: Add action apply support.

## Improvements
- COMPANY: mass update enabled for some fields.
- BANKDETAILS: mass update enabled for currency and active field.
- PRODUCT: added tracking on code and name fields.
- EBICS USER : Group and sort by bank and partner in grid view.
- INVOICE: Display PFP validator and status in invoice supplier refund grid.
- EBICSBANK: Set tracking for all fields on update.
- METASCHEDULE : added batchServiceSelect option for Contract Batch.
- INVOICING PROJECT: provide menu for invoicing project grid for mass invoicing.
- EBICS USER: Add field serial number (CORP).
- EmployeeFile: Add new date field to store the date of latest upload.
- SALE ORDER/PURCHASE ORDER: add button "Back to confirmed order" and "Back to validated order" respectively.
- Budget: Addition of new value for periodDurationSelect.
- Stock move invoicing: when generating an invoice, the user can now only select quantity not present in generated invoices.
- SALE ORDER: change title "Description to display" of field 'description'.
- ACCOUNTING REPORT: display popup message on click of 'exportBtn'.
- INVOICING PROJECT: added field "teamTask" in timesheet line form related to project.
- FIXES ASSET: add analytic distribution template.
- FIXES ASSET CATEGORY: add analytic distribution template.
- STOCK CORRECTION: Change status to draft on copy.
- EBICS BANK: now X509 Extensions for auto signed certification are managed independently.
- STOCK CORRECTION: change error message on validate.
- EBICS USER: replacing Listener object with ImporterListener for EbicsUser Import.
- STOCK MOVE: Add default supplier partner in mrp line grid.
- STOCK MOVE: Maximized pop up of projected stock and counter.
- STOCK MOVE: store invoicing status in database.

## Bug Fixes
- Ebics User: resolve error getting on export and modify import config and export template to include BankOrderList and BankStatementList of EbicsPartner.
- BankOrder: Fix NPE on click of confirm for International transfer.
- BATCH: empty link to batch on copy for BankPaymentBatch and ContractBatch.
- Invoice Payment: resolve invoice amount due update when the generate accounting move option is not active.
- BANK ORDER: corrected the possibility to generate the same move twice.
- BANK ORDER: corrected the behavior of bank order, now bank order moves can be generated on validation or realization.
- BUSINESS PROJECT: fix negative refund in financial report.
- INVOICE: now comment on invoices is made from the concatenation of comment from partner and comment from company bank details.
- SALE ORDER: now on invoice generation from sale order action the generated invoices have their comment made from the concatenation of comment from partner and comment from company bank details.
- Mass invoicing stock move: fix generate one invoice from multiple stock moves.
- SALE ORDER: Fixed accounting situation not being set from the partner when generating the order from a partner form.
- USER: fix NPE on user creation when active team is null.
- Purchase Order: Fix NPE on copy of purchaseOrder when it has an empty purchaseOrderLineList.
- Contract: correct the translation of 'Fiscal position'.
- MRP: Do not show mrp lines from other MRPs when not displaying products without proposals.
- LEAVE REQUEST: No longer displays an error message when saving a leave request.
- EBICSUSER EXPORT: Fix for "Cannot get property 'code' on null object" error.
- Bank Payment: fix translation.
- OPPORTUNITY: On copy, clear sale order list.
- Fixed Asset: Fix issue of infinite value of depreciation rate.
- Campaign: Fix campaign form view.
- Purchase order: remove M2O invoice field.
- STOCKMOVE: display qty per tracking number and not total available qty for tracking number.
- HR: Fix typo.
- INVOICING PROJECT: Filter the records including deadlineDate.
- MRP calculation: fix NPE on calculation.
- MOVE LINE: Bank reconciliation amount is now read-only.
- MRP forecast: Reset status on copy.
- INVOICE: corrected the generation of comment with null display.
- Purchase Order: Fix NPE when company is null.
- SALE ORDER LINE: Rename "sale.order.line.type.select" selection to "line.type.select" and move it to base module.
- Stock Move mass invoicing: improve performance when selecting stock moves in wizard.
- Cut-off batch: filter already invoiced stock move to improve batch performance.

## [5.2.1] - 2019-12-16
## Features
- ACCOUNTING REPORT: add new report, bank reconciliation statement.

## Improvements
- INVOICE: new mandatory labeling: Head office address.
- Company: Add tree view for companies.
- AdvancedExportLine: Add translation for field orderByType.
- PURCHASE REQUEST: add new columns in purchase request grid view.
- MOVE: changed position of reconciliation tag in move form.
- BANK STATEMENT: add caption under bank statement line grid in bank statement form in order to explain the colors are used in bank statement line grid.
- PRODUCT: update translation for "Service" and "Product".
- STOCK MOVE: empty reservation date time on duplicate stock move.
- STOCK MOVE: Update stock move's form view.
- SALE ORDER PRINTING: rename title "Sale order" to "Order Acknowledgement" of report on condition.
- MOVE: Improved messages when there is an exception on trying to remove an accounting move.
- Partner Form: change the french translation of "Create sale quotation".
- STOCK MOVE: empty to and from stock location set on company change.
- STOCK MOVE: hide reserved qty when it is a supplier arrival or a customer return.
- STOCK MOVE: rename title of stock-move-form buttons related to PFP.
- STOCK MOVE: update pfp tags on stock move form.
- Invoicing project: unit conversion for "Duration adjust for customer".
- ACCOUNTING REPORT: change the title of "General ledger 2" from the selection.
- TAX: Show type select in grid view.
- Sale order/quotation: fix tab title when generating a quotation from an opportunity.

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

[Unreleased 5.3.1]: https://github.com/axelor/axelor-open-suite/compare/v5.3.0...dev
[5.3.0]: https://github.com/axelor/axelor-open-suite/compare/v5.2.5...v5.3.0
