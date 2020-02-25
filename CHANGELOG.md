# Changelog
## [Unreleased 5.3.1]
## Improvements
- INVOICES DASHBOARD: Turnover is now calculated using both sales and assets

## Bug Fixes

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
