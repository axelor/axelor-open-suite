
# Changelog
## [Unreleased 5.3.1]
## Improvements
## Bug Fixes

## [5.3.0] - 2020-02-25
## Features
ACCOUNTING REPORT : group by and subtotal of analyticDistributionTemplate.

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
- LEAVE REQUEST : Allow sending a leave request in the past.


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
