## [9.0.2] (2026-02-05)

### Fixes
#### Base

* Update Axelor Open Platform to 8.0.6.
* Map: fixed an issue where filling in the 'PO Box' field in a customer address prevented the map from being displayed.
* Partner : display address type on readonly mode.
* Updated utils and message dependencies.
* Base: fixed some helper in severals form views.
* App base: added demo data for 'Sequence increment timeout'.
* Fixed possible errors when opening certain views.

#### Account

* Move: fixed JPQL filter syntax for archived field in mass action.
* Invoice: fixed credit note reconciliation with holdback invoices.
* Invoice/TaxLine : fixed the refresh tax account information into refresh vat system information.
* Invoice: fixed the display of the head office address in the BIRT report when the address position is set to 'right' in the printing settings.
* Invoice: fixed the issue where updating the generated move date removes the invoice term from the invoice.

#### Bank Payment

* Bank order: fixed the incorrect due date on direct debit bank orders.
* Payment session: fixed the order of bank order line creation and invoice term validation
* Bank order: fixed area D5 to accept alphanumeric values in the norm for cfonb160.
* Move: fix bank reconciliation impact when reversing and deleting moves.

#### Budget

* Budget/BudgetKey : fixed the budget key unique check after hibernate migration
* Budget: fixed demo data for budget key computation

#### CRM

* Opportunity: fixed partner domain to display only customers/prospects.

#### Human Resource

* Timesheet: fixed minutes calculation in timesheet line.
* Leave request batch: fixed an issue where cancellation email was sent instead of confirmation email
* App timesheet: fixed the form loading issue when there are thousands of timesheets.

#### Production

* Production order: fixed an error occurring when generating production order from sale order with auto plan option enabled.
* Unit cost calculation: fixed filter of Products.
* BOM printing: fixed priority sorting, sub-BOM indicator, and replaced ProdProcess column with BillOfMaterial
* Prod product: fixed an error occurring on select of tracking number.
* Manufacturing: fixed wrong cost sheet calculation on partial and complete finish.
* Manufacturing order: fixed an error occurring when updating actual quantities or partially finishing
* BOM tree: fixed an incorrect quantity in multi-level BOM tree view.

#### Sale

* Sale order: fixed unit price calculation for 'Replace' price lists when quantity falls below the minimum quantity threshold.

#### Stock

* Stock move: fixed grid/form views for saleOrderSet and purchaseOrderSet.
* Stock Location : Remove page break on Birt report.
* Stock location: include virtual sub stock location in list when enabled.
* Inventory: fixed an issue with inventory validation during demo data import.
* Stock move line: fixed unit price change at qty change.
* Stock location: fixed valuation discrepancy between form view and financial data report.
* Stock move: fixed wrong reserved qty in stock move and stock details by product.
* Stock move: fixed an error occurring when splitting into 2 a stock move line without quantity.
* Stock move: fixed error when filling the real quantities.
* Stock move: fixed error when splitting a stock move into 2.
* Inventory: fixed validation creating empty internal stock moves.
* Stock move: added english titles for 'delayedInvoice' and 'validatedInvoice' button.
* Stock correction: fixed product selection error
* Stock move: fixed unable to print picking order for stock move with large number of lines from form view.

#### Supply Chain

* Sale order/Purchase order: fixed an error occurring during advance payment generation with title lines.
* Stock move: fixed an error occurring when merging stock moves.
* Sale order: fixed delivered quantities after merging deliveries.
* Declaration of exchanges: fixed filter on stock move displayed.
* Supplychain batch: fixed an error occurring in 'Update stock history' batch.
* Sale order: fixed an error occurring when creating line if supplychain was not installed.
* App supplychain: added a warning message when both 'Generate invoice from sale order' and 'Generate invoice from stock move' are enabled to prevent double invoicing.


### Developer

#### Base

Dependency 'flying-saucer-pdf-openpdf:9.2.2' has been replaced by 'flying-saucer-pdf:10.0.6'

---

``` sql  

UPDATE studio_app_base SET sequence_increment_timeout = 5 WHERE COALESCE(sequence_increment_timeout, 0) < 1;

```

---

Changed AppBaseServiceImpl parent class from AppServiceImpl to ScriptAppServiceImpl.
Changed all classes constructor which have AppBaseServiceImpl as parent.

#### Account

- MoveDueService: new public method `getOrignalInvoiceMoveLinesFromRefund(Invoice invoice)` returning `List<MoveLine>` instead of single MoveLine.
- MoveExcessPaymentService: protected method `getOrignalInvoiceMoveLine(Invoice invoice)` renamed to `getOrignalInvoiceMoveLines(Invoice invoice)` and now returns `List<MoveLine>` instead of `MoveLine`.
- MoveCreateFromInvoiceServiceImpl: new protected method `isHoldbackMoveLine(MoveLine moveLine)` added.

---

- Added InvoiceTermRepository in the MoveLineInvoiceTermServiceImpl constructor

#### Bank Payment

MoveRemoveServiceBankPaymentImpl now injects BankReconciliationLineRepository to check
reconciliation links before blocking deletion.

## [9.0.1] (2026-01-22)

### Fixes
#### Base

* Partner: fixed the internal server error when creating a contact from a partner.
* Import: fixed the issue where imports clears the attachements folder.
* Sequence management: added missing French translations.
* Sequence: fixed an error occurring when saving a sequence.
* Sequence management: fixed an error occurring when generating sequence for some models.
* Advanced export: fixed advanced export in excel.
* Update Axelor Message and Axelor Utils to 4.0.1.
* Company: fixed alternative logos not working properly.
* Partner: fixed the company department field to be editable.

#### Account

* Move template: fixed missing analytic axis when generating moves.
* Invoice/InvoiceTerm : added an automatic PFP validator synchronization between invoice and invoice terms.
* Move: fixed VAT system not computed when account is set from partner defaults.
* InvoiceLine/MoveLine/Analytic: fixed wrong analytic axis requirement.
* Bank reconciliation: fixed empty analytic axis values on generated move lines.
* Payment Session: fixed offset increment in bill of exchange validation to only count processed invoice terms
* MassReconcile/MoveLine : added an info message when errors were encountered during process
* ACCOUNTINGBATCH / ANALYTICREVIEW : Ensure original sign is preserved when copying negative values.
* AccountingCutOff: fix inverted debit/credit move lines for deferred incomes cut-off.
* Payment Session: fixed detached entity error during bill of exchange validation with multiple invoice terms
* Payment session: fixed a query error while searching for due invoice terms.
* Move line consolidation: fixed an issue where the process could hang indefinitely when consolidating move lines with analytic distributions of the same size but different values.
* Fiscal Year: fixed closure of an fiscal year, when we have multiples companies
* Move line tax: fixed VAT system selection when creating tax move lines

#### Bank Payment

* BankOrderFile/CFONB: fixed the length of the ustrd to 140 to match CFONB norm.

#### CRM

* Event: fixed display of linked events in objects based on 'Related to' field.
* Partner: removed unused partner form view.

#### Production

* MRP line: fixed the issue where maturity date is not computed at the start of operations.
* Manuf order: fixed quantity conversion when BOM line unit differs from product unit.
* Manuf order: fixed many errors on manuf orders.
* Manufacturing: fixed the planning failure that occurred on subcontracted manufacturing orders.
* Manuf order: fixed estimated date for in/out stock moves on change of planned dates.

#### Project

* Planned charge dashboard: fixed errors on charts.

#### Purchase

* Purchase Order Line: Fixed missing Delivery panel for service in PO lines.

#### Quality

* Quality: remove dependency on axelor-production and handle Manufacturing fields from the Production module.
* Quality control: fixed birt report parameters to use generic timezone and local instead of project.

#### Sale

* Sale order: updating the delivery address also updates the address in sale order lines.
* Sale order line: fixed the level indicator on change of sale order lines order by drag & drop.

#### Stock

* Inventory: block stock moves when an inventory is in progress on a parent location.
* Stock move: fixed an error occurring when creating stock move line.

#### Supply Chain

* Sale order: fixed the update of sale order line's delivery address on change of delivered partner.
* Stock move line: fixed the real quantity value when 'autoFillDeliveryRealQty' or 'autoFillReceiptRealQty' is disabled in the Supplychain app.
* Sale order: fixed an issue with sale order editable grid.

#### Talent

* Events: fixed errors when creating events without a job application.
* Job application: fixed the Show all events button.

#### Intervention

* Intervention: error on non conforming tag.


### Developer

#### Account

- Added AnalyticLineService in the MoveTemplateServiceImpl constructor

---

- Added AnalyticLineService in the BankReconciliationMoveGenerationServiceImpl constructor

---

- Changed the MoveLineService.reconcileMoveLinesWithCacheManagement to return the number of errors encountered.
- Changed the PaymentService.useExcessPaymentOnMoveLinesDontThrow to return the number of errors encountered.

---

Refactored MoveLineConsolidateServiceImpl.findConsolidateMoveLine() method:
- Removed unnecessary while loop that could cause infinite iteration
- Added proper return statement when analytic move lines have same size but different values
- Simplified null checks for analytic move line lists

#### Production

- `ManufOrderOutsourceServiceImpl` and `ManufOrderStockMoveServiceImpl` injects `StockMoveRepository`.

#### Purchase

Replaced the attrs action `action-purchase-order-line-attrs-delivery-panel` with the method action `action-purchase-order-line-method-manage-delivery-panel-visibility`.

#### Stock

- Added StockLocationService in the StockMoveServiceImpl constructor

#### Supply Chain

- Added SaleOrderDeliveryAddressService in the SaleOrderOnChangeServiceImpl constructor

## [9.0.0] (2026-01-16)

### Features

#### Upgrade to Axelor Open Platform version 8.0

* Axelor Open Platform version 8 comes with an upgrade on the backend infrastructure and the support of cloud storage.
* See corresponding [Migration guide](https://docs.axelor.com/adk/8.0/migrations/migration-8.0.html) for information on breaking changes.

#### Base

* Updated Axelor Utils dependency to 4.0.
* Updated Axelor Studio dependency to 4.0.
* Updated Axelor Message dependency to 4.0.

### Changes

#### Base

* Updated deprecated Google API. Now using Google Compute Route API instead of Distance Matrix.

#### Project

* Project: improve task tree management.

[9.0.2]: https://github.com/axelor/axelor-open-suite/compare/v9.0.1...v9.0.2
[9.0.1]: https://github.com/axelor/axelor-open-suite/compare/v9.0.0...v9.0.1
[9.0.0]: https://github.com/axelor/axelor-open-suite/compare/v8.5.9...v9.0.0
