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

[9.0.1]: https://github.com/axelor/axelor-open-suite/compare/v9.0.0...v9.0.1
[9.0.0]: https://github.com/axelor/axelor-open-suite/compare/v8.5.9...v9.0.0
