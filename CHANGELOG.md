## [9.0.4] (2026-03-05)

### Fixes
#### Base

* Update Axelor Open Platform to 8.1.0
* Partner: fixed an error on the email dashlet when retrieving sent emails.
* Partner: added value translations for all fields to get field name in user language in search.

#### Account

* Deposit slip: fixed PDF regeneration after BIRT template update.
* Payment session: fixed payments not being generated after user confirms expired financial discount warning.
* Invoice: fixed wrong invoice term calculation on change of partner with different fiscal position.
* Accounting batch: fixed error on move export.
* Move line query: fixed unreconcile process displaying move lines with no reconcile.
* Payment session: fixed bill of exchange validation when session contains isolated refunds or invoice terms with prior partial payments.
* Invoice: fixed price computation in A.T.I. invoice.
* Accounting batch: fixed an issue occurring when launching cut-off accounting batch.
* Sale order: fixed third-party payer partner not set when generating an invoice from a sale order.
* Account: fixed payment session validation with credit notes (non-LCR) leaving incorrect payment amounts on invoice terms.
* Invoice: fixed note display on invoice report.
* Reconciliation: fixed thresholdDistanceFromRegulation not taken into account when reconciling more than 2 move lines, and fixed RECONCILE_BY_AMOUNT proposing unbalanced sets.
* Pfp menu: fixed domain filter when origin date is missing on supplier invoices.
* Accounting export: fixed 'Export journal entry -> Acc. Soft.' generating an empty CSV file when move lines are found.

#### Bank Payment

* Bank order: fixed duplicate file upload when generating bank order file.
* Bank reconciliation: fixed wrong starting balance when splitting reconciliation into multiple sessions.
* Bank statement line: fixed bank statement line print wizard.
* Invoice term: fixed the issue in bank payment on form view override.

#### Budget

* Budget : fixed issue where realized with po is not imputed when using invoice generated from stock move of sale/purchase order.

#### Cash Management

* Forecast recap: fixed error when populating with purchase orders.

#### Contract

* Contract: fixed analytic move lines forecast type incorrectly set to order forecast instead of contract forecast.
* Opportunity: fixed the issue in contract on form view override.
* Contract: fixed an error that could occur when invoicing contracts in batch with high volume.

#### Fleet

* Vehicle: fixed inconsistency between list view and form view on vehicle contract.

#### GDPR

* GDPR: fixed erasure failing on OneToMany fields without mappedBy.

#### Human Resource

* Project task: fixed an error when searching sprints from project task due to incorrect context casting.
* Expense: fixed the empty check on analytic move lines during expense ventilation.
* Timesheet: fixed the order of timesheet lines generated from the expected planning.
* Timesheet line: fixed project task not being cleared when changing the project.

#### Maintenance

* BOM/Prod process: fixed missing status change button for bill of material and production process.

#### Marketing

* Marketing: added English demo data translations.

#### Production

* Product: not setting purchasable to true by default when creating a new product from manufacturing menu entry.
* Manuf order: fixed stock move generation when updating planned quantity.
* Manuf order: fixed an issue where child manufacturing orders did not appear in the children MO dashlet when multi-level planning generated more than one level of depth.
* Prod process line: fixed an error when adding consumed products on an unsaved prod process line.
* Operation order: fixed NPE on planned end with waiting date when prod process line is null.
* Manuf order: fixed an error occurring when clearing the cancel reason field in the cancellation popup.
* Manufacturing order: fixed error when canceling with a cancel reason and stock reservation enabled.
* Manufacturing order: fixed a blocking error when updating planned or real quantities with consumption on operation.
* Sale order: fixed NPE when confirming sale order with production order having no manuf orders.
* Manuf order: fixed an error occurring when finishing a manufacturing order with operation containing stock moves.
* Prod process: fixed the product display when the isEnabledForAllProducts boolean is set to true.
* Sale order: fixed duplicated sale order line details for semi-finished BOM components in editable tree mode.
* Manuf order: fixed creation of unusable manuf order and blocking at planned status in multi level planning.

#### Project

* Implemented security check on task removal from project.

#### Purchase

* Purchase order: fixed max purchase price computation.
* Purchase order line: fixed an error when changing unit in certain cases.
* Purchase request: fixed auto completion of trading name on creation of request.

#### Quality

* Quality control: fixed display of quality corrective action.
* Quality: fixed an issue on QI Identification demo data.

#### Sale

* Sale order: fixed price recomputation when editable tree is enabled.
* Price list line: fixed decimal digits in amount and min qty.
* Sale order: fixed wrong discount value with A.T.I configuration is enabled.

#### Stock

* Stock: fixed decimal points for different views.
* Stock move: fixed the issue with reserved quantity management for partial supplier arrivals.
* Stock move: removed control on receipt for stock move line with no quantity.
* Stock move: fixed purchase tracking splits for manual tracking assignment.
* Stock move: fixed wrong address mapping when selecting a partner on incoming stock move.
* Stock correction: fixed the error message related to the tracking number check.
* Stock move: fixed stock apis' total without tax calculation.
* Stock move line: fixed available quantity not displayed when selecting a tracking number from consumed products.
* Stock location: fixed performance issue causing slow grid loading.

#### Supply Chain

* MRP: fixed an error occurring when generating manufacturing proposals.
* Invoice: fixed error in invoice generated from stock move.
* Demo data import: fixed errors occurring when importing supplychain demo data.
* Stock move: fixed stock moves generation with tracking number from sale order.
* Declaration of exchanges: fixed filter on stock move displayed.
* Sale order: fixed subscription sale orders are completed while invoices are still to be generated.
* Purchase order: fixed an error when cancelling planned stock moves while editing an order.
* Invoice: fixed duplicated external reference when invoicing multiple stock moves from the same order.
* Stock reservation: requested reserved quantity is now based on expected quantity instead of real quantity.
* Purchase order: fixed an error occurring when generation stock move with product controlled at reception.


### Developer

#### Base

Migration scripts needed to be executed for the update.

For new AuditLog object :

```sql
CREATE SEQUENCE audit_log_seq START WITH 1 INCREMENT BY 100;
CREATE TABLE audit_log
(
    id             bigint       not null    primary key,
    archived       boolean,
    version        integer,
    created_on     timestamp(6),
    updated_on     timestamp(6),
    current_state  text,
    error_message  text,
    event_type     varchar(255) not null,
    previous_state text,
    processed      boolean,
    related_id     bigint       not null,
    related_model  varchar(255) not null,
    retry_count    integer,
    tx_id          varchar(255) not null,
    created_by     bigint   constraint fk_ru17orlmi6rjhpojmeqrxbiok references auth_user,
    updated_by     bigint   constraint fk_tbhw331n3l89dk629vmpxg4de references auth_user,
    user_id        bigint   constraint fk_pyjqqm7hglp6pnwp3h8whian8 references auth_user
);

CREATE INDEX audit_log_idx_processing
    ON audit_log (processed, tx_id, related_model, related_id, event_type, created_on, retry_count);
```
For new field receivedOn in MailMessage object :

```sql
ALTER TABLE mail_message ADD COLUMN received_on timestamp(6);
CREATE INDEX mail_message_received_on_idx ON mail_message (received_on);
UPDATE mail_message set received_on = created_on;
```

For more information, see https://docs.axelor.com/axelor-open-platform/8.1/migration-guide.html

#### Account

- Changed InvoiceTermReplaceService.replaceInvoiceTerms parameters from 
(invoice, newInvoiceTermList, invoiceTermListToRemove) to (invoice, newInvoiceTermList, invoiceTermListToRemove, paymentSession)

---

Added PartnerAccountService to SaleOrderInvoiceServiceImpl and services extending it.

#### Contract

-- script
UPDATE  account_analytic_move_line SET type_select = 4 WHERE contract_line IS NOT NULL AND type_select = 1;

#### Production

- Added SolDetailsBomUpdateService in the SaleOrderLineBomServiceImpl constructor

#### Sale

- Added SaleOrderLinePriceService and SaleOrderLineProductService to SubSaleOrderLineComputeServiceImpl constructor.
- Added new method updateSubSaleOrderLineList(SaleOrderLine, SaleOrder) in SubSaleOrderLineComputeService class.

## [9.0.3] (2026-02-19)

### Fixes
#### Base

* Scheduler: fixed batch origin not showing as 'Scheduled' for scheduler jobs.
* Partner price list: disabled '+' option on sale and purchase partner list.
* Partner: set the first bank account as default when none is selected.
* Partner: fixed copied partner keeping multiple related collections.

#### Account

* AnalyticMoveLine: Fix detached entity error on analytic move line reverse
* Payment voucher: cancel the payment voucher when reverse a payment move.
* Analytic/MoveLine Query: fixed date filtering broken by Hibernate upgrade.
* Fixed asset: removed the possibility to update the fixed asset lines at draft status.
* Payment session: fixed infinite loop and refund reconciliation issues with isolated refunds
* Payment session: fixed error during validate process.
* Invoice: fixed mail settings when generating an invoice automatically.
* Accounting report: fixed excel report printing.
* Invoice term: make due date editable on invoice term form.
* FEC import type: fixed missing XML bindings following the 'Allow to import FEC move lines with analytic'.
* Partner: fixed partner balance details domain to show correct move lines.
* Fixed assets: fixed disposal depreciation amount with degressive method and prorata temporis
* Partner: disabled account creation from accounting situation.
* Invoice term: fixed the calculation of paid amount when financial discount is not applied in payment session.
* Move line mass entry: disabled form view access to prevent error.
* Payment session: fixed email sent not saved in messages when a partner has no email address set.
* Payment voucher: fixed readonly condition of the confirm payment button.
* Mass entry: fixed the issue where mass entry move status is not updated after validation.
* Payment voucher: fixed payVoucherElementToPay update when selecting overdue moveline.

#### Bank Payment

* Bank statement file format: fixed the display of binding file for the concerned file format only.
* Bank reconciliation: fixed the description on generated moves to use the bank statement line description instead of the reconciliation name.

#### Budget

* Global budget: fixed the button 'Global budget commited lines' engaged with purchase order.
* Budget: fixed the calculation of firm gap on change the budget version.
* Invoice: fixed the duplication of budget distribution on invoice line after the move duplication.
* PurchaseOrder/Invoice: fixed budget repartition on partial invoicing.
* Purchase order: fixed performance issue while saving requested purchase order with budget.

#### Contract

* Contract: excluded consumption lines when duplicating a contract.

#### Human Resource

* Expense line: fixed error on selecting project task in expense line.
* Employee: moved typeSelect attribute of weeklyPlanning to production module.
* Employee: fixed employee status when leaving date is filled.
* Expense: fixed analytic axes not set when ventilating an expense.

#### Marketing

* Target list: fixed an error when opening partner/lead filters in readonly mode.

#### Production

* Operation order: fixed planned duration computation when changing planned end date.
* Tracking number search : empty lines in product field for tracking number search
* ManufOrderService: removed useless beans.get
* Manuf order: fixed error when starting a manufacturing order with stock moves realized on start.
* Bill of material: fixed component product filter to prevent infinite loop error.
* Production: fixed LazyInitializationException occurring on some actions in manuf order, purchase/sale order, stock.
* Manuf order: fixed error while updating planned dates.
* Product: fixed quantity used in bill of material panel in product form view.

#### Project

* Project: fixed the french translation for project sequence validation message.
* Project task: fixed inconsistency between the progress of parent and child tasks.

#### Purchase

* Purchase order: fixed the default type issue when supplier is also subcontractor.

#### Sale

* Sale order: fixed error when selecting trading name on merge sale order view.
* Sale order line: fixed pricing computation on sale order line.
* Cart: fixed performance issue when generating sale order from cart.
* Sale order line: fixed pricing scale in logs.
* Sale order: fixed NPE when the product account computed with the account management is null.

#### Stock

* Stock move: fixed available quantity and availability status when the move line unit differs from stock unit.
* Stock location: fixed empty location content printing for virtual stock locations.
* Inventory line: made stockLocation required.
* Stock location line: made unit readonly.
* Product: fixed quantity for external stock location in stock chart in product form view.
* Inventory: removed weird character from inventory form view.
* Inventory: fixed decimal quantity in export file.
* Stock move: fixed weighted average cost update on products when canceling moves at zero stock.
* Stock location: improved performance when fetching stock locations.
* Stock: fixed an error occurring when opening dashboard view.
* Stock move line: fixed split by tracking number not accessible on stock move lines.

#### Supply Chain

* Sale order: fixes potential permissions issues when generating Purchase order from Sale order.
* Mass stock move invoicing: fixed domain to get filtered invoices when switching from form to grid view.
* Purchase order: fixed error when generating stock moves with lines having different stock locations or delivery dates.

#### Intervention

* Equipment: fixed invalid domain for linked interventions panel.


### Developer

#### Account

- Added PaymentVoucherCancelService in the MoveReverseServiceImpl constructor
- Added PaymentVoucherCancelService in the MoveReverseServiceBankPaymentImpl constructor
- Added PaymentVoucherCancelService in the MoveReverseServiceBudgetImpl constructor
- Added PaymentVoucherCancelService in the ExpenseMoveReverseServiceImpl constructor

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

[9.0.4]: https://github.com/axelor/axelor-open-suite/compare/v9.0.3...v9.0.4
[9.0.3]: https://github.com/axelor/axelor-open-suite/compare/v9.0.2...v9.0.3
[9.0.2]: https://github.com/axelor/axelor-open-suite/compare/v9.0.1...v9.0.2
[9.0.1]: https://github.com/axelor/axelor-open-suite/compare/v9.0.0...v9.0.1
[9.0.0]: https://github.com/axelor/axelor-open-suite/compare/v8.5.9...v9.0.0
