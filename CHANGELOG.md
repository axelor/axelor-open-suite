## [8.5.16] (2026-04-16)

### Fixes
#### Base

* Role: fixed the form view not properly extending axelor-core view.
* Birt template config line: removed remaining data init.
* Permission assistant: fixed invalid imported permissions when the condition is left empty.
* Company: removed non-existing fields from demo data.
* Partner: block save when registration code is required for companies but empty.
* REST API: fixed an issue where some unauthorized actions were not blocked correctly.
* Base: fixed active company, trading name and active project quick menus displaying items in random order instead of alphabetical.
* Product: fixed duplicating a product copies price list content into existing price lists.
* Update Axelor Open Platform to 7.4.10

#### Account

* Accounting report type: fixed missing company records in demo data.
* Account: fixed payment session eligible terms including incompatible payment mode types.
* Mass entry: fixed error when trying to delete a move of type mass entry.
* FEC Import: fixed missing currency on imported moves when Idevise column is empty.
* Accounting report: fixed Aged balance report to use only validated invoice payments.
* Invoice: fix VAT system forced to payment on advance payment invoice tax lines.
* Accounting chart: fixed FRA_PCG tax import leaving activeTaxLine null due to incomplete importIds in account_tax.csv.
* Accounting situation: Fix missing debt recovery field on partner.
* Invoice: fixed company bank details not being auto-filled when selecting a factorized customer.
* Analytic: fixed analytic percentage validation bypass when saving a move in edit mode.
* Account: fixed NPE when sorting invoices with null date or id in notification process.
* Invoice : fixed VAT on advance payments are not included in cash VAT declaration report.
* Invoice term: fixed NPE on invoice term payment update when invoice term is not linked to an invoice.
* Payment session: fixed incorrect total displayed and compensation not applied with Bank Order Confirmation trigger.
* Invoice term: fixed Send notify email button not opening the email wizard on PFP form views.
* PFP: fix partial validation not creating a new invoice term when validated from the invoice term view.
* Account: fixed wrong designation on the BIRT report from draft credit note.
* Payment condition: added help on isFree field.
* Payment mode: fixed payment mode import files by removing reference to the removed invoiceLabel field.

#### Bank Payment

* Bank order: fixed impossible to cancel a bank order if the associated payment session has a refund.

#### Budget

* Budget: fixed mismatch between Realized with/without PO form amounts and listing filters.

#### Cash Management

* Forecast dashboard: fix chronological ordering of months across years in 'chart.forecast.in.out.total' chart.
* Forecast recap: apply payment terms on purchase and sale orders.
* Forecast recap: added non-blocking warning when overlapping forecast recaps/reports are detected for the same company, period and bank details.

#### Contract

* Contract: fixed error when 'Only invoice consumption before Invoice period end Date' is enabled and 'Periodic Invoicing' is disabled while invoicing contract.
* Contract: added validation to prevent the first period end date from being earlier than the supposed activation date.
* Contract: display the invoice moment field regardless of the automatic invoicing field.

#### CRM

* Prospect: fixed lost conversion error.
* Partner: fixed partner merge issue when duplicated partner is having events.

#### GDPR

* GDPR Request: fixed duplicate rows generated in CSV export due to collection fields.

#### Human Resource

* Expense: show the payment button on expense with company card.

#### Production

* Manufacturing order: fixed outsourced produced stock moves targeting the wrong stock location.
* Production: fixed automatic production order not generated for sale order lines created via product configurator.
* Sale order: fixed sublines being cleared when changing supply method on a sale order line without a bill of material.
* Production: fixed consume in stock moves button not working when consuming products by operation phase.
* Production: fixed LazyInitializationException when confirming sale order with automatic manufacturing order planning enabled.
* Sale order: fixed NPE error when adding a pack while editable tree view is activated.
* Production: fixed an error when creating a new consumed product from a manufacturing order.
* Production: fixed an issue where the real quantity could be changed on service product lines linked to a subcontracting purchase order.
* Configurator: fixed old bill of material not being deleted when regenerating a product.

#### Project

* Project: fixed dashboard tasks button label to 'View all tasks'.

#### Purchase

* Purchase order: fixed trading name field being read-only in the outsourcing purchase order.

#### Quality

* Quality: fixed EN demo-data import error on QIResolutionDecision.

#### Sale

* Sale order: fixed stock allocation sale order line duplication issue.
* Sale order: fixed freight carrier mode and carrier partner not copied from partner on sale order creation.
* Partner: fixed archived customers/prospects displayed on the map.
* Sale order: fixed deliveryAddressStr not recomputed on save when address content changes.
* Dashboard: fixed chart labels for translated select values.
* Sale order: fixed unnecessary Mapper.toMap usage on sale order line change.

#### Stock

* Stock location: changed stock location value from transient to formula field.
* Product: fixed quantity for external stock location in stock chart in product form view.
* Stock: refresh stock move availability badge after line update.
* Stock move: fixed an error when changing a stock move line type to title.
* Stock: fixed NPE on fromStockLocation when duplicating stock move.

#### Supply Chain

* Purchase order: fixed missing translation on cancel reason error message.
* Timetable: fixed error while generating an invoice from the menu form, when the timetable is not related to the order.
* MRP: fixed calculation staying stuck in started status when an error occurs.
* Supplychain: invoices generated from a stock move of a purchase order are now correctly generated with the purchase order and advance payments of the purchase order.
* Sale order: fixed delivery state on lines not initialized after order confirmation when automatic stock move generation is disabled.


### Developer

#### Base

Several REST controllers now execute their SecurityCheck chain correctly before continuing.

Custom code or integrations that relied on these endpoints succeeding despite missing access rights
will no longer work after this fix.

If a custom process was incorrectly using these API flows without the required permissions,
it must be updated to grant the proper access rights or to call the process with an authorized user.

#### Account

- InvoicePaymentMoveCreateServiceImpl and InvoicePaymentMoveCreateServiceImpl consturctors are updated to introduce MoveLineTaxService

---

-- migration script
DELETE FROM meta_action WHERE name = 'action-invoice-term-attrs-set-initial-pfp-amount';

#### Production

SaleOrderLineProductionService added to ConfiguratorServiceProductionImpl constructor.

---

Added action-stock-move-line-group-real-qty-onchange override in axelor-production StockMoveLine views.

#### Supply Chain

- MrpService.saveErrorInMrp(Mrp, Exception) signature changed to saveErrorInMrp(Mrp, Throwable).
- MrpServiceImpl.call() now throws Exception instead of AxelorException.
- MrpServiceImpl.onRunnerException(Exception) signature changed to onRunnerException(Throwable).
- MrpJob.runCalculationWithExceptionManagement(Mrp) now throws Exception instead of AxelorException.

---

Added InvoiceService in the StockMoveInvoiceBudgetServiceImpl constructor

## [8.5.15] (2026-04-02)

### Fixes
#### Base

* Tag: fixed tag domain not filtering by trading name.
* Partner: fixed IBAN validation not enforced on supplier and customer forms.
* Address: fixed possible npe on save of address.
* Address: fixed actionPanel visiblity when creating new record.

#### Account

* Payment session: fixed session total amount decreasing after validation with compensation.
* Accounting entries imported from FEC files now normalize negative debit or credit values.
* Payment voucher: fixed moves with 'ignore in debt recovery' not appearing in invoice terms retrieval.
* Accounting report: fixed general balance report displays empty account name and code for moves at new status.
* Accounting report: fixed domain filtering on journal, account and partner fields for custom multi-company reports.
* Invoice: fixed 1 cent gap between reverse charge tax lines.
* Invoice: fixed tax total computation difference with sale order in ATI mode.
* Invoice/Order: fixed warning message wrongly displayed when no tax is needed.
* Accounting entries: fixed the imported amount in currency during FEC imports.
* Invoice term: fixed missing move line link when creating a new invoice term on a ventilated invoice.
* Period: improved anomaly management during the close process so that moves failing validation are reported instead of blocking the entire closure.
* Invoice chart: fixed the turn over displayed on chart 'Customer Turnover history by month (on invoices)' which wrongly includes end of pack line when show total is true.

#### Bank Payment

* BankReconciliation: fixed ending balance computation to use currency amount instead of debit/credit when reconciling in a foreign currency.
* Move: added a warning when move lines are linked to a validated bank reconciliation.
* Bank order: fixed an issue where bank order file was accessible via DMSFile.
* Accounting report / Bank reconciliation statement: fixed bank statement lines not displayed on the report when issued from a csv bank statement.

#### Budget

* Budget: fixed the duplicated budget imputation when adding a budget on move linked to an invoice.

#### Cash Management

* Forecast recap: always apply estimated duration and payment condition on orders.

#### Contract

* Contract version: fixed duration selections to exclude unrelated values.
* Contract: fixed contract lines order not preserved on new version creation.

#### CRM

* Catalog: fixed an issue where the email form was not displayed after sending an email from a catalog.

#### Human Resource

* Expense: fixed kilometric distance counter reset and wrong rate bracket applied after reimbursement.
* Timesheet: fixed editor labels not being translated.
* Timesheet: fixed missing New and Delete buttons in the timesheet line editor.
* Project planning time: fixed synchronization issue between project planning time and calendar event.

#### Production

* Production: fixed wrong negative WAP calculation on manufacturing order finish after partial finish.
* Production: fixed an issue where using 'Consume in stock moves' on a Manufacturing Order could also realize finished-product stock moves.
* Production: fixed machine charge dashboard percentage precision by using seconds instead of truncated minutes.
* Sale order: fixed an error occurring when personalizing bill of material from sale order.
* Sale order: fixed operation quantity returning 0 for semi-finished product when exploding multi-level BOM.
* Manufacturing order: fixed an issue where cost sheet generated had a wrong status.
* Production: fixed circular self-injection of BillOfMaterialService in BillOfMaterialServiceImpl.
* Stock details by product: fixed projected stock not showing manufacturing order component consumptions.
* Cost sheet: fixed human resource cost not scaled by number of cycles when using per-hour cost type.
* Production: fixed wrong stock move line generation in operation order when updating planned quantity.

#### Purchase

* Purchase order: fixed trading name field being read-only when the purchase order was generated from a call for tenders.
* Purchase order lines: fixed the slowdown when selecting a product.

#### Quality

* Quality control / Quality Alert: fixed inconsistent date issue.

#### Sale

* Sale order: improved performance when generating report with large database.
* Configurator: fixed unique constraint violation when importing a configurator with numeric attribute names.
* Sale: fixed circular self-injection of ProductRestService in ProductRestServiceImpl.
* Sale: optimized margin computation by caching considerZeroCost flag outside loops.
* Configurator: fixed product code being overwritten on sale order line regeneration.

#### Stock

* Mass stock move invoicing: fixed partial stock move invoicing.
* Tracking number: fixed the display of available qty when selecting a tracking number from a stock move line with a child stock location.

#### Supply Chain

* Birt: fixed missing currency unit display on unit price and tax table columns in sale order, invoice, and purchase order reports.
* Sale/Purchase order: fixed advance payment invoice generated with multiple lines instead of a single one in case of same taxes.
* Sale/Purchase order: fixed invoiced amount not cumulating when ventilating multiple invoices.
* Stock move: fixed missing fiscal position on invoices generated from direct stock moves and backorders.
* Stock move: fixed an error preventing realization of a stock move with reserved sale order lines.


### Developer

#### Base

TagService#getTagDomain now takes an additional TradingName parameter.

#### Account

Changed the ImportMoveFecServiceImpl constructor, replacing the MoveLineToolService by ImportMoveLineAmountService

---

- PeriodServiceAccountImpl: added TraceBackRepository in constructor.
- PeriodServiceAccount: added new public methods getMoves(), getAnomalyCount(), and getAnomalies(String moveIds, int anomalyCount).
- MoveValidateService: changed accountingMultiple(Query<Move>) return type from void to Pair<List<Move>, Integer>.

#### Bank Payment

Added BankReconciliationLineRepository as parameter in the MoveAttrsBankPaymentServiceImpl constructor

#### CRM

- CatalogService: changed sendEmail(Catalog, Template, List<Partner>) return type from void to Message.

#### Production

`ManufOrderStockMoveService.updatePrices(ManufOrder, BigDecimal)` has been removed.
It is replaced by `updatePrices(ManufOrder, BigDecimal, Set<Long> stockMoveIds)`, which only
processes the outgoing stock moves whose IDs are provided. This prevents re-processing stock
moves already realized in previous partial finishes, which could cause a negative WAP.
Client overrides of the old method must be migrated to the new signature.

---

Changed some OperationOrderChartServiceImpl method's names.
- calculateNumberOfMinutesPerHour in calculateNumberOfSecondsPerHour
- getNumberOfMinutesMachineUsedTotal in getNumberOfSecondsMachineUsedTotal
- getNumberOfMinutesPerDay in calculatePercentagePerDay
- calculateMinutes in calculateSeconds

---

Added UnitRepository to BomLineCreationServiceImpl constructor.

---

Constructor of `OperationOrderStockMoveServiceImpl` has two new parameters:
`StockMoveProductionService stockMoveProductionService`,
`OperationOrderService operationOrderService`

#### Purchase

Changed the SupplierCatalogServiceImpl constructor to use AppBaseService appBaseService and inject SupplierCatalogRepository.

#### Sale

sale_order_line_proc was using a CASE which prevented PSQL to use index 'sale_sale_order_line_sale_order_idx'. So it was scanning the entire table and then filtering it in memory. Changed to a UNION ALL to permit the use of indexes. The rptdesign file needs to be replaced in the SaleOrder PDF BirtTemplate to get these changes.

#### Stock

Added SupplyChainConfigService and StockMoveRepository as parameter in StockMoveMultiInvoiceServiceImpl constructor

---

Added StockLocationLineFetchService as parameter in the StockMoveLineStockLocationServiceImpl constructor

## [8.5.14] (2026-03-19)

### Fixes
#### Base

* Update Axelor Open Platform to 7.4.9
* Demo data: added missing weight unit conversions (kg to g, kg to mg) in English CSV.
* Partner: fixed address created via API SIRENE not appearing on reports.
* Update studio dependency to 3.5.6

#### Account

* Accounting config template: fixed sequences not being linked to journals when importing chart of accounts for a new company.
* Move line: ensure partner is required in grid view when account uses partner balance, consistent with form view.
* Move: fixed tax validation exception raised when reversing a move with reverse charge tax.
* Move line: fixed vatSystemSelect readonly condition on tax account change.
* Reconcile: fixed reconciliation of tax move lines when partner is not set on OD moves.
* Use the account config of the company in actions
* Move: fixed Generate tax lines button visible for incompatible functional origins.
* Payment session: fixed bill of exchange session with credit note compensation losing remaining invoice term amount.
* Invoice: fixed the financial discount account configuration error on payment when no financial discount is used.
* Move/InvoiceTerm: fixed due date propagation for multiple invoice terms when editing move line due date.
* Invoice: hide the invoice terms panel on advance payment invoice view.
* Move: added a warning message when the move comes from an invoice.

#### Bank Payment

* Bank reconciliation: improved automatic reconciliation performance.
* Bank Order: fixed SEPA file generation to use company currency amount instead of bank order amount in InstdAmt.
* Payment session: fixed payment moves not generated when auto-confirm bank order is enabled with bank order confirmation accounting trigger.

#### Budget

* Sale order: Improved action 'action-budget-sale-order-method-fill-budget-str'

#### Contract

* Contract: fixed prorata ratio computation when invoicing period is shorter than invoicing duration.

#### Human Resource

* Timesheet line: fixed activity error when product is not passed.
* Expense: fixed payment move not being generated immediately when the payment mode uses bank order with immediate accounting trigger.
* Timesheet: fixed missing product field in lines generation wizard when activity is disabled.
* Employee: fixed employee planning button being displayed when axelor-business-production module is not installed.
* Expense type: fixed an error when creating an expense product while product codes are generated from categories.
* Timesheet: fixed wrong computation of timesheet lines generated from leaves for half days.
* ProjectTask/Planning: added sprint planning management on budgeted time change in planning panel

#### Production

* Manufacturing order: fixed consume stock moves not generated for operation orders when consumption is managed per operation.
* Sale order / Prod process: fix NPE when BoM has no production process.
* Manuf order: fixed error when planning an order that has no operations.
* Sale order line: fixed NPE when changing bill of material outside of tree-editable view.
* Manufacturing order: fixed an error when merging manufacturing orders with automatic planning after merge enabled.
* Outsourcing purchase orders: fixed supplier selection to only show subcontractors.
* MRP: fixed NPE during CBN process when bill of material or production process is missing.
* Manufacturing order: fixed work center change on operation order.
* Manufacturing order: fixed incorrect subcontracting cost and unit price on outgoing stock move during partial finish.
* Cost sheet: fixed inconsistent human cost valuation during final production cost calculation.
* Manufacturing Order: fixed incorrect unit price computed on outgoing stock move when produced quantity differs from planned quantity.
* Manuf order: fixed an issue where planning a manufacturing order generated multiple consumed stock move lines per tracking number when the bill of materials used a different unit than the product's stock unit.
* Cost sheet: fixed produced ratio when production is declared multiple times on the same day.
* Sale order: fixed an issue where sub-lines of 3rd level and beyond were not generated when selecting a product with a multi-level BOM.
* Production process: fixed number of decimals digits for BOM not taken into account while managing consumed product on phases.

#### Project

* Project: removed invoicing config booleans from project form.
* Project: made custom field type readonly when already used on existing tasks, and reset selection when changing to a non-select type.
* Project: fixed custom field selection items not being saved after initial creation.
* Project task: fixed the issue with the start date when a task is created using a task template that has a delay to start.

#### Purchase

* Message: added purchase order in related to selection.

#### Sale

* Sale order: fixed global discount calculation when sale order has total tax included.
* Partner: fixed error on customer form when sales product has null name.
* Sale order: fixed recalculate prices resetting unit and WT prices to zero when editable tree is enabled.
* Sale order: fixed reserved quantity not aligned with order line quantity when generating sale order from cart.
* Cart: filtered product selection to only show sellable products.

#### Stock

* Stock move: fixed availability status computed on expected quantity instead of real quantity.
* StockMove/StockLocation : fixed the future quantity error when using the split tracking number configuration
* Tracking number: fixed perishable and warranty settings not being pre-filled when manually creating a tracking number from a stock move line.
* Inventory: fixed an error occurring when exporting an inventory with lines having no real quantity filled in.
* Sequence: fixed invalid codeSelect values in demo data CSV preventing import of tracking number sequences and accounting report sequence.
* Stock move: fixed future qty computation to use expected qty instead of real qty.

#### Supply Chain

* Stock move: fixed the currency when creating a stock move from a purchase or sale order.
* Stock move: fixed error when opening a stock move with no linked sale or purchase orders.
* Supply chain: fixed demo data import failure caused by empty interco status select fields in AppSupplychain CSV.
* Sale order line: fixed an issue where duplicating a line would copy its delivery and invoicing state.

#### Intervention

* Intervention: fixed error when generating an intervention from a contract when the intervention type is configured to automatically generate a customer request.


### Developer

#### Base

PartnerGenerateServiceImpl constructor now takes an additional AddressRepository parameter.

#### Human Resource

TimesheetLineGenerationService.generateLines() now takes an additional boolean showActivity parameter.

---

Added LeaveRequestPlanningService and WeeklyPlanningService in the LeaveRequestComputeLeaveHoursServiceImpl constructor

#### Production

Changed constructor of `OperationOrderPlanningCommonService`: added `OperationOrderStockMoveService` parameter.

---

`CostSheetServiceImpl` constructor now requires an additional `StockMoveLineRepository` parameter.

#### Project

Script to remove the unused action : 
"DELETE FROM meta_action WHERE name = 'action-project-record-manage-timespent-reset-values';

#### Sale

- Replaced AppBaseService and AppSaleService with ProductSaleDomainService in SaleOrderLineDomainServiceImpl constructor.

#### Stock

- Added StockMoveService to the StockMoveLineServiceImpl constructor.
- Added StockMoveService to the StockMoveLineServiceSupplychainImpl constructor.
- Added StockMoveService to the StockMoveLineProductionServiceImpl constructor.

## [8.5.13] (2026-03-05)

### Fixes
#### Base

* Map: fixed an issue where filling in the 'PO Box' field in a customer address prevented the map from being displayed.
* Partner: added value translations for all fields to get field name in user language in search.

#### Account

* Deposit slip: fixed PDF regeneration after BIRT template update.
* Payment session: fixed payments not being generated after user confirms expired financial discount warning.
* Invoice: fixed wrong invoice term calculation on change of partner with different fiscal position.
* Move line query: fixed unreconcile process displaying move lines with no reconcile.
* Payment session: fixed bill of exchange validation when session contains isolated refunds or invoice terms with prior partial payments.
* Invoice: fixed price computation in A.T.I. invoice.
* Sale order: fixed third-party payer partner not set when generating an invoice from a sale order.
* Account: fixed payment session validation with credit notes (non-LCR) leaving incorrect payment amounts on invoice terms.
* Invoice: fixed note display on invoice report.
* Reconciliation: fixed thresholdDistanceFromRegulation not taken into account when reconciling more than 2 move lines, and fixed RECONCILE_BY_AMOUNT proposing unbalanced sets.
* Pfp menu: fixed domain filter when origin date is missing on supplier invoices.

#### Bank Payment

* Bank order: fixed duplicate file upload when generating bank order file.
* Bank reconciliation: fixed wrong starting balance when splitting reconciliation into multiple sessions.
* Bank statement line: fixed bank statement line print wizard.
* Invoice term: fixed the issue in bank payment on form view override.

#### Budget

* Budget : fixed issue where realized with po is not imputed when using invoice generated from stock move of sale/purchase order.

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
* Expense: fixed distance computation in certain cases.
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
* Manufacturing order: fixed a blocking error when updating planned or real quantities with consumption on operation.
* Sale order: fixed NPE when confirming sale order with production order having no manuf orders.
* Manuf order: fixed an error occurring when finishing a manufacturing order with operation containing stock moves.
* Prod process: fixed the product display when the isEnabledForAllProducts boolean is set to true.
* Manufacturing: fixed the planning failure that occurred on subcontracted manufacturing orders.
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

#### Account

- Changed InvoiceTermReplaceService.replaceInvoiceTerms parameters from 
(invoice, newInvoiceTermList, invoiceTermListToRemove) to (invoice, newInvoiceTermList, invoiceTermListToRemove, paymentSession)

---

Added PartnerAccountService to SaleOrderInvoiceServiceImpl and services extending it.

#### Contract

-- script
UPDATE  account_analytic_move_line SET type_select = 4 WHERE contract_line IS NOT NULL AND type_select = 1;

#### Production

- `ManufOrderOutsourceServiceImpl` and `ManufOrderStockMoveServiceImpl` injects `StockMoveRepository`.

---

- Added SolDetailsBomUpdateService in the SaleOrderLineBomServiceImpl constructor

#### Sale

- Added SaleOrderLinePriceService and SaleOrderLineProductService to SubSaleOrderLineComputeServiceImpl constructor.
- Added new method updateSubSaleOrderLineList(SaleOrderLine, SaleOrder) in SubSaleOrderLineComputeService class.

## [8.5.12] (2026-02-19)

### Fixes
#### Base

* Scheduler: fixed batch origin not showing as 'Scheduled' for scheduler jobs.
* Partner price list: disabled '+' option on sale and purchase partner list.
* Partner: set the first bank account as default when none is selected.
* Partner: fixed copied partner keeping multiple related collections.

#### Account

* AnalyticMoveLine: Fix detached entity error on analytic move line reverse
* Payment voucher: cancel the payment voucher when reverse a payment move.
* Fixed asset: removed the possibility to update the fixed asset lines at draft status.
* Payment session: fixed infinite loop and refund reconciliation issues with isolated refunds
* Payment session: fixed error during validate process.
* Invoice: fixed mail settings when generating an invoice automatically.
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
* Inventory: removed weird character from inventory form view.
* Inventory: fixed decimal quantity in export file.
* Stock move: fixed weighted average cost update on products when canceling moves at zero stock.
* Stock location: improved performance when fetching stock locations.
* Stock move line: fixed split by tracking number not accessible on stock move lines.

#### Supply Chain

* Sale order: fixed the value of 'Invoiced Amount (excl. tax)' in case of multi-order invoices.
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

#### Supply Chain

-- migration script to update amount_invoiced in Sale Order table

UPDATE sale_sale_order SaleOrder
SET amount_invoiced =
(
    CASE
      WHEN SaleOrder.currency <> Company.currency
       AND SaleOrder.company_ex_tax_total <> 0
      THEN SaleOrder.ex_tax_total *
          (
              (
                  COALESCE((
                      SELECT SUM(InvoiceLine.company_ex_tax_total)
                      FROM account_invoice_line InvoiceLine
                      JOIN account_invoice Invoice ON Invoice.id = InvoiceLine.invoice
                      WHERE InvoiceLine.sale_order_line IN (
                          SELECT id FROM sale_sale_order_line WHERE sale_order = SaleOrder.id
                      )
                      AND Invoice.operation_type_select = 3
                      AND Invoice.status_select = 3
                  ), 0)
                  -
                  COALESCE((
                      SELECT SUM(InvoiceLine.company_ex_tax_total)
                      FROM account_invoice_line InvoiceLine
                      JOIN account_invoice Invoice ON Invoice.id = InvoiceLine.invoice
                      WHERE InvoiceLine.sale_order_line IN (
                          SELECT id FROM sale_sale_order_line WHERE sale_order = SaleOrder.id
                      )
                      AND Invoice.operation_type_select = 4
                      AND Invoice.status_select = 3
                  ), 0)
              ) / SaleOrder.company_ex_tax_total
          )
      ELSE
          COALESCE((
              SELECT SUM(InvoiceLine.company_ex_tax_total)
              FROM account_invoice_line InvoiceLine
              JOIN account_invoice Invoice ON Invoice.id = InvoiceLine.invoice
              WHERE InvoiceLine.sale_order_line IN (
                  SELECT id FROM sale_sale_order_line WHERE sale_order = SaleOrder.id
              )
              AND Invoice.operation_type_select = 3
              AND Invoice.status_select = 3
          ), 0)
          -
          COALESCE((
              SELECT SUM(InvoiceLine.company_ex_tax_total)
              FROM account_invoice_line InvoiceLine
              JOIN account_invoice Invoice ON Invoice.id = InvoiceLine.invoice
              WHERE InvoiceLine.sale_order_line IN (
                  SELECT id FROM sale_sale_order_line WHERE sale_order = SaleOrder.id
              )
              AND Invoice.operation_type_select = 4
              AND Invoice.status_select = 3
          ), 0)
    END
)
FROM base_company Company
WHERE SaleOrder.company = Company.id;

## [8.5.11] (2026-02-05)

### Fixes
#### Base

* Partner : display address type on readonly mode.
* User: fixed the issue with the default value for 'Generate random password'.
* Update studio and message dependencies.
* App base: added demo data for 'Sequence increment timeout'.
* Company: fixed alternative logos not working properly.

#### Account

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

* Budget: fixed demo data for budget key computation

#### CRM

* Opportunity: fixed partner domain to display only customers/prospects.

#### Human Resource

* Timesheet: fixed minutes calculation in timesheet line.
* Leave request batch: fixed an issue where cancellation email was sent instead of confirmation email
* App timesheet: fixed the form loading issue when there are thousands of timesheets.

#### Production

* Production order: fixed an error occurring when generating production order from sale order with auto plan option enabled.
* BOM printing: fixed priority sorting, sub-BOM indicator, and replaced ProdProcess column with BillOfMaterial
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
* Stock move: fixed unable to print picking order for stock move with large number of lines from form view.

#### Supply Chain

* Sale order/Purchase order: fixed an error occurring during advance payment generation with title lines.
* Stock move: fixed an error occurring when merging stock moves.
* Sale order: fixed delivered quantities after merging deliveries.
* Declaration of exchanges: fixed filter on stock move displayed.
* Sale order: fixed an error occurring when creating line if supplychain was not installed.
* App supplychain: added a warning message when both 'Generate invoice from sale order' and 'Generate invoice from stock move' are enabled to prevent double invoicing.


### Developer

#### Base

``` sql  

UPDATE studio_app_base SET sequence_increment_timeout = 5 WHERE COALESCE(sequence_increment_timeout, 0) < 1;

```

#### Account

- MoveDueService: new public method `getOrignalInvoiceMoveLinesFromRefund(Invoice invoice)` returning `List<MoveLine>` instead of single MoveLine.
- MoveExcessPaymentService: protected method `getOrignalInvoiceMoveLine(Invoice invoice)` renamed to `getOrignalInvoiceMoveLines(Invoice invoice)` and now returns `List<MoveLine>` instead of `MoveLine`.
- MoveCreateFromInvoiceServiceImpl: new protected method `isHoldbackMoveLine(MoveLine moveLine)` added.

---

- Added InvoiceTermRepository in the MoveLineInvoiceTermServiceImpl constructor

#### Bank Payment

MoveRemoveServiceBankPaymentImpl now injects BankReconciliationLineRepository to check
reconciliation links before blocking deletion.

## [8.5.10] (2026-01-22)

### Fixes
#### Base

* Partner: fixed the internal server error when creating a contact from a partner.
* Import: fixed the issue where imports clears the attachements folder.
* Sequence management: added missing French translations.
* Sequence: fixed an error occurring when saving a sequence.
* Sequence management: fixed an error occurring when generating sequence for some models.
* Update Axelor Message to 3.3.1 and Axelor Utils to 3.5.1.
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
* Manuf order: fixed estimated date for in/out stock moves on change of planned dates.

#### Purchase

* Purchase Order Line: Fixed missing Delivery panel for service in PO lines.

#### Quality

* Quality control: fixed birt report parameters to use generic timezone and local instead of project.

#### Sale

* Sale order: updating the delivery address also updates the address in sale order lines.
* Sale order line: fixed the level indicator on change of sale order lines order by drag & drop.

#### Stock

* Inventory: block stock moves when an inventory is in progress on a parent location.
* Stock: fixed product tunnel code renaming issue on stock move printings.

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

#### Purchase

Replaced the attrs action `action-purchase-order-line-attrs-delivery-panel` with the method action `action-purchase-order-line-method-manage-delivery-panel-visibility`.

#### Stock

- Added StockLocationService in the StockMoveServiceImpl constructor

#### Supply Chain

- Added SaleOrderDeliveryAddressService in the SaleOrderOnChangeServiceImpl constructor

## [8.5.9] (2026-01-08)

### Fixes
#### Base

* User: fixed concurrent modification error when creating a contact for another user.
* Sequence: fixed unit tests after sequence management refactoring.
* Partner: fixed wrong registration code and VAT Number on the Axelor demo data partner.
* Sequence: added reserved sequence management system to address deadlock issues.

#### Account

* Invoice: fixed the default company tax number.
* Invoice: fixed the printing file generation on invoice ventilation when auto generation is enabled in invoice app, independent of the printing template toAttach flag.
* Sale invoice details: fixed the date format on report.
* Fixed asset: fixed the wrong depreciation move amount on the disposal year.
* MoveLine/VatSystem: fixed wrong vat system computation on generate counterpart action.

#### Budget

* Sale/Purchase order: fixed the error when auto computing budget distribution.
* BudgetDistribution : fixed the automatic compute via budget key when using multiple axis.
* Budget : fixed amount paid not updated at invoice payment (move reconcile).

#### Contract

* Contract template: fixed the display of 'Grouped Invoicing' and taxes on change of product on contract lines.

#### CRM

* Dashboard: removed duplicated dashlet 'dashlet.created.leads.by.industry.sector'.
* Dashboard: fixed the status issue on charts 'chart.leads.by.team.by.status.bar' and 'chart.leads.by.saleman.by.status.bar'.
* Dashboard: fixed the country issue on 'chart.leads.by.country.bar' chart.

#### GDPR

* GDPR: fixed an error occurring when retrieving data.

#### Helpdesk

* Ticket: fixed the display of some fields.

#### Human Resource

* Expense : changed expense so that could not be editable once validated.
* Timesheet: fixed an issue with timesheet completion when public holiday events planning is not set on the employee.
* Timesheet: fixed the generation of timesheet lines from the expected planning.

#### Production

* Prod process : fixed 'Stock move realize order select' doesn't take into account production config.
* Bom tree: fixed the qty scale based on nbDecimalDigitForBomQty.
* Manufacturing order: include per-piece work center cost in cost sheets.

#### Stock

* Stock move : fixed invoiced quantity issue on stock move lines of a new stock move from reversion.
* Stock move: fixed tracking number on back order.
* Inventory: Corrected PDF printing of inventories
* Stock move: fixed total without tax not updated.

#### Supply Chain

* SaleOrder : fixed set analytic on sale order line when loading pack.


### Developer

#### Base

Following the sequence management refactoring (ticket 105427), unit tests in TestSequenceService
were failing because they relied on mocked SequenceComputationService.

Changes made:
- Removed TestSequenceService.java: tests were redundant as the same functionality
  is now tested in SequenceComputationServiceTest
- Updated SequenceComputationServiceTest.java: added missing test cases migrated from
  TestSequenceService (uppercase/lowercase letter sequences, null lettersType handling)
- Removed unused method isSequenceAlreadyExisting() from SequenceService.java

---

Added new entity ReservedSequence to track sequence reservations with three statuses:
PENDING (0), CONFIRMED (1), RELEASED (2).

New services added:
- SequenceIncrementExecutor: executes sequence increments in isolated transactions
- SequenceReservationService: manages sequence reservations with transaction synchronization
- SequenceComputationService: computes sequence numbers (pure logic, no DB access)
- ReservedSequenceCleanupService: cleans up orphaned reservations

New batch added: Reserved sequence cleanup

New configuration in AppBase:
- sequenceIncrementTimeout: timeout in seconds for sequence increment (default: 5)

This feature can require a new base batch and a scheduler. 
There are init datas on the ticket PR for it : https://github.com/axelor/axelor-open-suite/pull/15416/files
Please add this to your instance and use the scheduler every day by default.

Database migration required - see SQL script below.

```sql
-- Create new table BASE_RESERVED_SEQUENCE
create table if not exists base_reserved_sequence
(
id                  bigint    not null primary key,
archived            boolean,
import_id           varchar(255) unique,
import_origin       varchar(255),
process_instance_id varchar(255),
version             integer,
created_on          timestamp,
updated_on          timestamp,
attrs               jsonb,
caller_class        varchar(255),
caller_field        varchar(255),
generated_sequence  text      not null,
reserved_at         timestamp not null,
reserved_num        bigint    not null,
status              integer,
created_by          bigint references auth_user,
updated_by          bigint references auth_user,
sequence            bigint    not null references base_sequence,
sequence_version    bigint    not null references base_sequence_version
);

create index if not exists base_reserved_sequence_sequence_idx
on base_reserved_sequence (sequence);

create index if not exists base_reserved_sequence_sequence_version_idx
on base_reserved_sequence (sequence_version);

create index if not exists idx_reserved_seq_seq_version_status
on base_reserved_sequence (sequence, sequence_version, status);

create index if not exists idx_reserved_seq_status_reserved_at
on base_reserved_sequence (status, reserved_at);

-- Add new column to studio_app_base
alter table studio_app_base
add if not exists sequence_increment_timeout integer
constraint studio_app_base_sequence_increment_timeout_check
check ((sequence_increment_timeout >= 1) AND (sequence_increment_timeout <= 60));

-- Add new columns to base_batch
alter table base_base_batch
add if not exists orphan_reservation_timeout_minutes integer
constraint base_base_batch_orphan_reservation_timeout_minutes_check
check (orphan_reservation_timeout_minutes >= 1);
alter table base_base_batch
add if not exists confirmed_reservation_retention_days integer
constraint base_base_batch_confirmed_reservation_retention_days_check
check (confirmed_reservation_retention_days >= 1);
alter table base_base_batch
add if not exists delete_old_confirmed_reservations boolean;
```

#### Account

- Changed method signature from printAndSave(Invoice invoice, Integer reportType, PrintingTemplate invoicePrintTemplate, String locale) to printAndSave(Invoice invoice, Integer reportType, PrintingTemplate invoicePrintTemplate, String locale, boolean toAttach) in InvoicePrintService

#### Human Resource

- Added WeeklyPlanningService, LeaveRequestService and PublicHolidayHrService in TimesheetProjectPlanningTimeServiceImpl constructor

## [8.5.8] (2025-12-18)

### Fixes
#### Base

* Security; updated a dependency to avoid security issue.
* Data backup: added 'archived' field in backup.
* Sale: fixed global discounts error having line with price set to zero.
* Printing template: fixed titles on printing birt report for correct French translation.

#### Account

* Move line mass entry: fixed the value of VAT system on change of account.
* ACCOUNTING EXPORT: fixed balances in balance panel not updated during FEC export.
* Move line: fixed the value of tax lines and VAT system on move lines generated from invoices.
* Invoice: fixed missing siren label on invoice report.
* Move: fixed performance issues on move creation.
* Journal: empty the sequence field when changing company to avoid inconsistency.
* Invoice: fixed wrong auto-validation PFP from credit note.
* Fixed asset: fixed wrong full derogatory entry generated when disposing an asset.
* Move line: fixed the value of VAT system if empty on account.
* Accounting batch: fixed doubtful customer batch issue by not removing old invoice terms.
* Invoice/Consolidate: fixed the consolidate process when two moveline have different analytic
* InvoiceLine/MoveLine: fill the invoiceline information on the non deductible tax moveline.
* ACCOUNTING REPORT : Fixed an error when opening general balance in excel format

#### Budget

* Move/Budget : retrieve the invoiceline when creating budget on a moveline
* Global budget : fixed the view to display the budget if no budget level is created
* Budget : set company on budgets generated by budget template.

#### Cash Management

* Forecast recap line: fixed the missing bank details in case of sale/purchase order with time tables.

#### Human Resource

* Expense/BankOrder: fixed the wrong bank details used on multi currency expense
* Move line: fixed the value of tax lines and VAT system on move lines generated from expenses.
* Job application: fixed the 'Hire Candidate' button when the status is 'Hired'.
* Expense: fixed wrong hidden condition on refuse button.

#### Production

* Manuf order: fixed the calculation of the required quantity to produce for semi-finished products.
* Manufacturing order: fixed missing quantity in to consumed product list.
* Manuf order: fixed planned end date when generating a manufacturing order from a production order with 'At the latest' scheduling.
* Fix incorrect quantity on ManufOrder generated from SaleOrder
* Invoice: fixed an error occurring on bill of material import validation.
* MRP: manufacturing proposals now consider economic manuf. qty
* Product: fixed an error occurring when deleting a product variant.
* Stock move: fixed production cost price anomaly on outgoing products.

#### Quality

* QI Analysis: added check in XML import.

#### Sale

* Sale order: fixed the stock location on change of partner.
* Opportunity: generated quotations now correctly inherit the customer contact.
* Sale order: fixed an error occurring when confirming an order with splitting quotation and order enabled.
* Sale order: disabled the possibility to update the quantity of a start pack line.
* Sale order: added reverse charge process.

#### Stock

* Logistical form: fixed the domain filter on stock move to block realized stock moves when 'Realize stock moves upon parcel/pallet collection' is enabled.
* Stock history line: fixed the number of decimals in quantity and price fields.
* Stock : prohibited selection of product model in inventory, stock correction and stock details by product.
* Stock correction: stock move now uses product average price when stock location line is not present

#### Supply Chain

* StockMove: fixed issue where total was not computed on fill real qty button.
* Mrp forecast: fixed domain filter on stock location in order to be able to select the right storable product.
* Sale order: fixed shipment mode not filled when creating directly from partner form.
* Purchase order: fixed the value of 'Invoiced Amount (excl. tax)' in case of multi-order invoices.
* Invoice: fixed cut off dates when the interco invoice is generated.
* Stock details by product: fixed duplicated entries in projected stock chart.
* Mrp forecast: fixed domain filter on stock location in order to be able to select the right stock location.


### Developer

#### Base

Upgraded the tika-core dependency to 3.2.3 to fix an important security breach.

#### Account

- Added MoveLineRecordService in the MoveLineCreateServiceImpl constructor

---

- Script to remove action deleted from the sources :
DELETE FROM meta_action WHERE name IN ('action-move-partner-onselect','action-move-method-trading-name-onselect','action-move-method-company-onselect','action-move-line-method-account-onselect','action-move-line-method-partner-onselect');

---

Changed action record name 'action-journal-record-reset-valid-account-set' to 'action-journal-record-reset-values'

---

Introduce PfpService in InvoiceTermPfpUpdateService and ReconcileInvoiceTermComputationService constructors.

---

- Added the AccountManagementService in the MoveLineCreateServiceImpl constructor.

#### Production

- ManufOrderStockMoveServiceImpl constructor is updated to introduce StockMoveToolService

#### Sale

- Added TaxService in SaleOrderLineCreateTaxLineServiceImpl constructor
- Added AppBaseService in SaleOrderLineCreateTaxLineServiceImpl constructor  
- Added a new boolean as parameter in the SaleOrderLineCreateTaxLineServiceImpl.getOrCreateLine  
- Added a new boolean as parameter in the SaleOrderLineCreateTaxLineServiceImpl.createSaleOrderLineTax

-- migration script to add column reverse_charged in sale_order_line_tax table
ALTER TABLE sale_sale_order_line_tax ADD COLUMN IF NOT EXISTS reverse_charged boolean;

#### Supply Chain

-- migration script to update amount_invoiced in Purchase Order table
UPDATE purchase_purchase_order PurchaseOrder
SET amount_invoiced =
(
    CASE
      WHEN PurchaseOrder.currency <> Company.currency
       AND PurchaseOrder.company_ex_tax_total <> 0
      THEN PurchaseOrder.ex_tax_total *
          (
              (
                  COALESCE((
                      SELECT SUM(InvoiceLine.company_ex_tax_total)
                      FROM account_invoice_line InvoiceLine
                      JOIN account_invoice Invoice ON Invoice.id = InvoiceLine.invoice
                      WHERE ((InvoiceLine.purchase_order_line IN (
                          SELECT id FROM purchase_purchase_order_line WHERE purchase_order = PurchaseOrder.id) AND Invoice.purchase_order IS NULL
                      ) OR Invoice.purchase_order = PurchaseOrder.id)
                      AND Invoice.operation_type_select = 3
                      AND Invoice.status_select = 3
                  ), 0)
                  -
                  COALESCE((
                      SELECT SUM(InvoiceLine.company_ex_tax_total)
                      FROM account_invoice_line InvoiceLine
                      JOIN account_invoice Invoice ON Invoice.id = InvoiceLine.invoice
                      WHERE ((InvoiceLine.purchase_order_line IN (
                          SELECT id FROM purchase_purchase_order_line WHERE purchase_order = PurchaseOrder.id) AND Invoice.purchase_order IS NULL
                      ) OR Invoice.purchase_order = PurchaseOrder.id)
                      AND Invoice.operation_type_select = 4
                      AND Invoice.status_select = 3
                  ), 0)
              ) / PurchaseOrder.company_ex_tax_total
          )
      ELSE
          COALESCE((
              SELECT SUM(InvoiceLine.company_ex_tax_total)
              FROM account_invoice_line InvoiceLine
              JOIN account_invoice Invoice ON Invoice.id = InvoiceLine.invoice
              WHERE ((InvoiceLine.purchase_order_line IN (
                  SELECT id FROM purchase_purchase_order_line WHERE purchase_order = PurchaseOrder.id) AND Invoice.purchase_order IS NULL
              ) OR Invoice.purchase_order = PurchaseOrder.id)
              AND Invoice.operation_type_select = 3
              AND Invoice.status_select = 3
          ), 0)
          -
          COALESCE((
              SELECT SUM(InvoiceLine.company_ex_tax_total)
              FROM account_invoice_line InvoiceLine
              JOIN account_invoice Invoice ON Invoice.id = InvoiceLine.invoice
              WHERE ((InvoiceLine.purchase_order_line IN (
                  SELECT id FROM purchase_purchase_order_line WHERE purchase_order = PurchaseOrder.id) AND Invoice.purchase_order IS NULL
              ) OR Invoice.purchase_order = PurchaseOrder.id)
              AND Invoice.operation_type_select = 4
              AND Invoice.status_select = 3
          ), 0)
    END
)
FROM base_company Company
WHERE PurchaseOrder.company = Company.id;

## [8.5.7] (2025-12-04)

### Fixes
#### Base

* Sequence: fixed full name computation on save.
* App base: fixed the display of testing panel.
* Partner: alert the user when using the API Sirene and the key was emtpy in the configuration.
* Account management : fixed fr translation on taxes
* Base: fixed data init error when creating a new database.

#### Account

* Invoice: note panel still visible even if invoiceCommentsPanel is empty in BIRT.
* Analytic move line: fixed the percentage validation logic for analytic reverse lines.
* Reconcile group: prevent move line reconciliation across different companies.
* Invoice term: added a check to prevent reconciling a holdback invoice term before other invoice terms are paid during a payment session or manual reconciliation.
* Account: fixed the issue where the status button title was incorrect when opening an account from the partner form.
* Reconcile: added missing translation for Debit and Credit move line amount remaining.
* Invoice/PurchaseOrder/SaleOrder: fixed the rounding issue on tax generation.
* Invoice: fixed the check for financial discount accounts when no financial discount is selected.

#### Bank Payment

* Bank reconciliation : fixed moves ongoing reconciled line balance to uses only one company.

#### Cash Management

* Forecast recap: fixed an issue related to dates for purchase order.

#### Contract

* Contract batch: fixed grouped invoicing in case of supplier contracts.

#### Production

* Operation order: fixed missing stock location for consumed stock move lines.
* Bill of material line: fixed the value of bill of material on change of product.
* MRP: added an explicit error message instead of a NPE when bill of material is missing on manufacturing proposal.
* Bill of material: fixed an issue where tree view was not displayed properly.

#### Purchase

* Purchase request: fixed NPE on generating purchase order if company is empty.

#### Quality

* Sequence : fixed missing translation for Required document.

#### Sale

* Sale order line: fixed issue where duplicating a line did not include its sub lines.
* Sale order: fixed error when merging sale quotations with new versions.
* Sale order: fixed price list date validity check.
* Sale order: fixed ordered indicator when ordering all with quotation and order split.
* Sale order: fixed an error occurring when opening form view of a sale order line in confirmation wizard.
* Sale order: reverse the button to transform the quotation into order.

#### Stock

* Stock move line: fixed field type for 'counter' on tracking number wizard form.
* Conformity certificate: fixed stock move line qty in BIRT report.
* Stock move: fixed an issue where the 'Split into 2' feature did not work when splitting a line with a tracking number.
* Stock move: fixed the PFP buttons to display only for supplier moves.

#### Supply Chain

* MrpLineType: fixed helper translation.
* Invoice: fixed fiscal position when the interco invoice is generated.
* Sale order: fixed the value of 'Invoiced Amount (excl. tax)' in case of multi-order invoices.


### Developer

#### Purchase

- Changed the PurchaseRequestService.generatePo method to use a default company too.

#### Supply Chain

-- migration script to update amount_invoiced in Sale Order table

UPDATE sale_sale_order SaleOrder
SET amount_invoiced =
(
    CASE
      WHEN SaleOrder.currency <> Company.currency
       AND SaleOrder.company_ex_tax_total <> 0
      THEN SaleOrder.ex_tax_total *
          (
              (
                  COALESCE((
                      SELECT SUM(InvoiceLine.company_ex_tax_total)
                      FROM account_invoice_line InvoiceLine
                      JOIN account_invoice Invoice ON Invoice.id = InvoiceLine.invoice
                      WHERE InvoiceLine.sale_order_line IN (
                          SELECT id FROM sale_sale_order_line WHERE sale_order = SaleOrder.id
                      )
                      AND Invoice.operation_type_select = 3
                      AND Invoice.status_select = 3
                  ), 0)
                  -
                  COALESCE((
                      SELECT SUM(InvoiceLine.company_ex_tax_total)
                      FROM account_invoice_line InvoiceLine
                      JOIN account_invoice Invoice ON Invoice.id = InvoiceLine.invoice
                      WHERE InvoiceLine.sale_order_line IN (
                          SELECT id FROM sale_sale_order_line WHERE sale_order = SaleOrder.id
                      )
                      AND Invoice.operation_type_select = 4
                      AND Invoice.status_select = 3
                  ), 0)
              ) / SaleOrder.company_ex_tax_total
          )
      ELSE
          COALESCE((
              SELECT SUM(InvoiceLine.company_ex_tax_total)
              FROM account_invoice_line InvoiceLine
              JOIN account_invoice Invoice ON Invoice.id = InvoiceLine.invoice
              WHERE InvoiceLine.sale_order_line IN (
                  SELECT id FROM sale_sale_order_line WHERE sale_order = SaleOrder.id
              )
              AND Invoice.operation_type_select = 3
              AND Invoice.status_select = 3
          ), 0)
          -
          COALESCE((
              SELECT SUM(InvoiceLine.company_ex_tax_total)
              FROM account_invoice_line InvoiceLine
              JOIN account_invoice Invoice ON Invoice.id = InvoiceLine.invoice
              WHERE InvoiceLine.sale_order_line IN (
                  SELECT id FROM sale_sale_order_line WHERE sale_order = SaleOrder.id
              )
              AND Invoice.operation_type_select = 4
              AND Invoice.status_select = 3
          ), 0)
    END
)
FROM base_company Company
WHERE SaleOrder.company = Company.id;

## [8.5.6] (2025-11-26)

### Fixes
#### Base

* Product: fixed a potential error due to missing product company when generating variant product.

#### Account

* Invoice line: fixed error when opening an invoice line from a sale order line.
* Reconcile : added an error when trying to pay a holdback invoice terms on an invoice with remaining ones

#### Bank Payment

* Bank order line: added a check for missing receiver bank details when registering an expense payment.
* PAYMENTSCHEDULE : The rejectReason field has been fixed so that it no longer accepts string values.

#### Budget

* Purchase order: fixed an error when displaying list of purchase order lines.

#### Contract

* Contract: fixed trading name on consumption invoice when trading name management is enable.

#### CRM

* Opportunity: display the partner popup when moving an opportunity to 'Closed won' in Kanban view.

#### Production

* ManufOrder: fixed NPE due to missing producible qty when computing the missing components label.

#### Project

* Project: fix activity dates format according to user's localization
* ProjectTask : Move Progress computation process from business project to project module.

#### Sale

* Sale order: hide already processed line when splitting quotation and sale order.

#### Stock

* Stock move: fixed the wrong quantity invoiced in the case of partial invoicing.

#### Supply Chain

* Stock move: fixed requested reserved qty for stock move returns.
* MRP: MRP result grid view is no longer editable.
* Sale order: fixed the domain for sale orders without stock move.


### Developer

#### Budget

A script need to be executed to remove an non necessary action view override. DELETE FROM meta_action WHERE xml_id='budget-purchase-order-see-purchase-order-lines';

#### Stock

- Changed method signature from isStockMoveInvoicingPartiallyActivated(Invoice,StockMoveLine) to isStockMoveInvoicingPartiallyActivated(Invoice) in WorkflowVentilationServiceSupplychainImpl

## [8.5.5] (2025-11-20)

### Fixes
#### Production

* SaleOrderLine: fixed the initialisation of quantity to produce.


### Developer

#### Production

- Added SaleOrderLineComputeQtyService in the SaleOrderLineInitValueService constructor
- Moved the SaleOrderLineInitValueServiceImpl protected method initQty into a new service SaleOrderLineComputeQtyService

## [8.5.4] (2025-11-20)

### Fixes
#### Base

* User: fixed permissions for 'demoerp' user.
* Quick menu: fixed the title for the instance info.
* Product: fixed product variant config by creating a new one when duplicating a Product.

#### Account

* Reconcile: fixed manual reconcile in the specific reconcile view.
* Invoice: fixed the division by zero error and NPE when registering a payment from an invoice with both a fiscal position and a financial discount set.
* Move: added origin in traceback while mass accounting during anomaly.
* Bank reconciliation: fixed the display of accounting moves line(s) to reconcile.
* Accounting report: fixed Aged balance and detailed customer balance report issue.
* Invoice term: fixed the scale of amount when computing the name.
* Payment session: fixed french translation for supplier and bank details in custom dashlet.
* Invoice: fixed attachment behavior when printing/regenerating to avoid duplicates and respect the attachment option, including on ventilation.
* MOVE : fixed inconsistant message when trying to delete a move
* Move line: fixed display condition and validity check on VAT System.

#### Budget

* Sale/Purchase order: fixed the performance issue due to the individual line update.

#### Contract

* Contract: fixed invoicing amounts not translated in french.

#### Human Resource

* Expense: fixed the currency for the advance amount field in form view.
* Expense API: fixed analytic move line not generated when creating expense line from API.
* Allocation line: fixed the value of project field on new.

#### Production

* SaleOrderLine: fixed the initialisation of quantity to produce.
* Product: fixed cost price and avg price when the product is manufactured for the first time.

#### Project

* Project: fixed sequence demo data.
* Project: added an error message when finishing a project if the default completed status was not configured.
* Project: fixed the performance issue in project form with many projects linked to a user.

#### Purchase

* Purchase order: fixed purchase order tax configuration when order were automatically generated.

#### Sale

* Sale order import: fixed an error occurring when importing lines with no tax lines.

#### Stock

* Stock correction: fixed the error message related to the tracking number check.
* Bill of material: fixed decimal digit number for bill of material line.
* Stock move: do not group stock move by status in grid view.
* Inventory: fixed an error occurring when there were more than 2 duplicated inventory lines.
* Partner: fixed the form view title for Freight Carrier.

#### Supply Chain

* Supplychain : fixed an issue where nothing happened when launching the invoicing batch.
* Sale order: fixed sale order invoicing state when invoiced amount is superior to the sale order total w.t.
* Invoice/PurchaseOrder : fixed the link between an advance payment and an invoice from the same purchase order


### Developer

#### Account

- Removed the checkReconcile method from ReconcileCheckService.
- Removed the isEnoughAmountToPay method from InvoiceTermToolService.

Script to remove a deleted action : 
- DELETE FROM meta_action WHERE name = 'action-reconcile-method-check-reconcile';

#### Production

- Changed the ManufOrderWorkflowServiceImpl.updateProductCostPrice parameters to add a BigDecimal costPrice

#### Purchase

Added PurchaseOrderTaxService to PurchaseOrderCreateServiceImpl constructor.

## [8.5.3] (2025-11-06)

### Fixes
#### Base

* Theme: fixed logo on default theme.
* Account Management: fixed duplicate company selection for product families
* Currency conversion: updated old API for currency conversion.
* Indicator: fixed display issue and demo data.
* Bank details: fixed some iban fields when adding bank details from partner view.

#### Account

* Invoice: keep advance payments empty on validate if user cleared them in draft.
* Invoice: correct due dates with multi‑term payment conditions when Free is enabled on payment conditions
* Reconcile: avoid negative amount when duplicating a reconcile.
* Invoice: fixed an issue where Payment mode on invoice term is changed after the ventiation
* Invoice: fixed shipping date in invoice report.
* FixedAsset: fixed lines amount's after split
* Move line query: fixed selected move lines unreconcilation.
* Account management: fixed typo when importing chart of accounts.
* Account: fixed the tax account setting on account charts demo/l10n.

#### Human Resource

* Expense: fixed currency initialization on the lines.

#### Project

* Project: fixed partner informations when generating a project from a sale order.
* Project: fixed project generation when the partner name contains an apostrophe.

#### Stock

* Stock move: fixed title for Customer deliveries invoice button.
* Stock move: fixed display of currency on the form view in the viewer of 'exTaxTotal'.

#### Supply Chain

* Sale order: fixed issue where duplicated lines were not visible in mass invoicing


### Developer

#### Base

Implemented domain filtering to prevent duplicate AccountManagement records for the same company and ProductFamily combination.

-- Add unique constraint to prevent future duplicates
ALTER TABLE account_account_management 
ADD CONSTRAINT uk_product_family_company_unique 
UNIQUE (product_family, company);

---

-- migration script to update bank_code, sort_code, account_nbr and bban_key in bank details table

UPDATE base_bank_details BankDetails
SET bank_code = SUBSTRING(BankDetails.iban FROM 5 FOR 5)
FROM base_bank Bank
WHERE BankDetails.bank = Bank.id
  AND Bank.bank_details_type_select = 1
  AND (BankDetails.bank_code IS NULL OR BankDetails.bank_code = '')
  AND BankDetails.iban IS NOT NULL;

UPDATE base_bank_details BankDetails
SET sort_code = SUBSTRING(BankDetails.iban FROM 10 FOR 5)
FROM base_bank Bank
WHERE BankDetails.bank = Bank.id
  AND Bank.bank_details_type_select = 1
  AND (BankDetails.sort_code IS NULL OR BankDetails.sort_code = '')
  AND BankDetails.iban IS NOT NULL;

UPDATE base_bank_details BankDetails
SET account_nbr = SUBSTRING(BankDetails.iban FROM 15 FOR 11)
FROM base_bank Bank
WHERE BankDetails.bank = Bank.id
  AND Bank.bank_details_type_select = 1
  AND (BankDetails.account_nbr IS NULL OR BankDetails.account_nbr = '')
  AND BankDetails.iban IS NOT NULL;

UPDATE base_bank_details BankDetails
SET bban_key = RIGHT(BankDetails.iban, 2)
FROM base_bank Bank
WHERE BankDetails.bank = Bank.id
  AND Bank.bank_details_type_select = 1
  AND (BankDetails.bban_key IS NULL OR BankDetails.bban_key = '')
  AND BankDetails.iban IS NOT NULL;

## [8.5.2] (2025-10-30)

### Fixes
#### Base

* Import history : the error file field is now hidden if empty and its title has been improved.

#### Account

* InvoiceTerm/PfpValidateStatus : fixed a technical error by changing the Listener.
* Invoice: fixed the invoice generation.

#### Bank Payment

* BankOrder : fixed the bank code on the cfonb160 format
* Bank reconciliation: improved global performance and UX.
* Bank reconciliation: fixed balances compute takes a lot of time to finish.

#### Supply Chain

* Purchase order: correclty clear the origin of a tracking number linked to purchase order when cancelling one.
* Fiscal position: added a consistency control on fiscal position for sale order, move and invoice.


### Developer

#### Account

Changed the checkOtherInvoiceTerms function from InvoiceTermPfpService to InvoiceTermPfpToolService.
Changed the checkOtherInvoiceTerms function from MoveInvoiceTermService to MovePfpToolService.

Added MovePfpToolService in MoveGroupServiceImpl constructor.
Added MovePfpToolService in MoveGroupBudgetServiceImpl constructor.
Added MovePfpToolService in MoveRecordUpdateServiceImpl constructor.

#### Bank Payment

- Removed 'action-bank-reconciliation-line-method-set-selected' action and setSelected() method from controller and service and replaced it with 'action-bank-reconciliation-line-record-set-selected' to select/unselect bank reconciliation lines.

---

- `getMoveLines(), computeMovesReconciledLineBalance() and computeMovesUnreconciledLineBalance()` methods from `BankReconciliationBalanceComputationServiceImpl` have been replaced by `computeBalances(Account)` method to compute moves reconciled/unreconciled balance.

## [8.5.1] (2025-10-23)

### Fixes
#### Base

* Unit conversion: optimized unit conversion retrieval with caching.
* Data backup: fixed field type for 'importId'.
* API SIREN : Fixed error encountered during the data retrieval process for a specific SIRET.
* GlobalAuditInterceptor : Fix null pointer exception on deleting records
* Data Backup: fixed the display of the field 'updateImportId'.
* API SIREN : Change the default sireneUrl.

#### Account

* Invoice/InvoiceTerm: Fixed the change of invoice term's due date after invoice's due date change on a free payment condition.
* Move line: correct number of currency decimal scale in move line grid.
* Accounting report: fixed Aged balance report to use invoice payments instead of invoice terms for accurate balance.
* Move group: Fixed incorrect assignment to pfpValidateStatusSelect.
* Invoice : fixed unpaid filter for advance payment invoices.
* INVOICE / DEBTRECOVERY : Invoice linked to a move having ignoreInDebtRecoveryOk as true are now ignored.
* Invoice: Convert cut off dates to real fields
* MOVE/MOVELINE:fixed advanced filter not displayed unless the whole page is refreshed
* PARTNER/PAYMENTCONDITION : Set accounting config default payment condition on partners.
* ACCOUNT : Correct display condition on vatSystemSelect and isTaxRequireOnMoveLine for tax type accounts.

#### Bank Payment

* BankOrder: fixed the technical error when the bank order is not created with all fields due to wrong import.

#### Business Project

* ProjectTask: add imputable field on project task

#### CRM

* Opportunity : set a default value for team and company and add filter for the team and the assignee user
* Opportunity form: added buttons on 'Sale quotations/orders' dashlet to create a new quotation

#### Human Resource

* Expense: fixed the currency in BIRT report.
* Expense Line: restrict displayed tasks to 'In progress' projects only.

#### Production

* Manuf Order: fixed issue where parent mo was not filled correctly on multi level planning.
* Production/Manuf order: fixed missing mappedBy in M2M relation between manuf order and production order.

#### Project

* Project: removed 'Enable task signature' from App project and 'Signature' from Project task.

#### Purchase

* Mrp: fixed notes to display on the purchase order are not automatically filled.

#### Sale

* Sale order : fixed end of pack line placement in sale order report.
* Sale order : fixed SubTotal cost price doesn't take into account the qty.
* Sale order line: automatically fill the user on delivery and production blocking.

#### Stock

* Stock move: fixed display issue in form view.
* Stock dashboard: deliveries dashboards are now filtered on virtual stock location.

#### Supply Chain

* PurchaseOrderLine : Invoiced must only be enabled if invoice generated InvoicingProject
* Sale order / Advance payment: fixed the advance payment amount at wizard opening.
* Order/AvancePayment: fixed the advance payment amount from sale/purchase order
* INVOICE : Set interco as true when generating invoice from an interco saleOrder / purchaseOrder.


### Developer

#### Base

UPDATE studio_app_base SET sirene_url = 'https://api.insee.fr/api-sirene/3.11';

#### Bank Payment

Added BankOrderCheckService in the BankOrderCreateService constructor. Added BankOrderCheckService in the BankOrderCreateServiceHr constructor. Changed the BankOrderCheckService.checkPreconditions(BankOrder bankOrder) in checkPreconditions(PaymentMode paymentMode, Integer partnerType, LocalDate bankOrderDate, Company senderCompany, BankDetails senderBankDetails)

#### Sale

- SaleOrderLineCostPriceComputeServiceImpl consturctor is updated to introduce SaleOrderLineProductService

#### Supply Chain

Removed CommonInvoiceService.createInvoiceLinesFromOrder Changed the parameter of PurchaseOrderInvoiceService.createInvoiceAndLines from (PurchaseOrder,List<PurchaseOrderLineTax>,Product,BigDecimal,int,Account) to (PurchaseOrder,Product,BigDecimal,int,Account) Changed the parameter of PurchaseOrderInvoiceService.createInvoiceLines from (Invoice,List<PurchaseOrderLineTax>,Product,BigDecimal) to (Invoice,List<PurchaseOrderLine>,Product,BigDecimal) Changed the parameter of InvoiceLineOrderService.getInvoiceLineGeneratorWithComputedTaxPrice from (Invoice,Product,BigDecimal,OrderLineTax,SaleOrderLine,PurchaseOrderLine) to (Invoice,Product,BigDecimal,SaleOrderLine,PurchaseOrderLine,BigDecimal,Set<TaxLine>) Changed the parameter of SaleOrderInvoiceService.createInvoiceAndLines from (SaleOrder,List<SaleOrderLineTax>,Product,BigDecimal,int,Account) to (SaleOrder,Product,BigDecimal,int,Account) Changed the parameter of SaleOrderInvoiceService.createInvoiceLines from (Invoice,List<SaleOrderLineTax>,Product,BigDecimal) to (Invoice,List<SaleOrderLine>,Product,BigDecimal)

## [8.5.0] (2025-10-17)

### Features
#### Base

* Updated Axelor Utils dependency to 3.5.
* Map: added an assistant to create map views.
* Partner: created a new API endpoint to create a partner.

#### CRM

* Opportunity: added a button to create a new quotation from opportunity form.

#### HR

* Expense: added multi-currency support.
* Leave request: created a new batch to generate leave requests.

#### Purchase

* Purchase order: added possibility to modify a validated order.

#### Sale

* Sale order: added shipping cost to sale order.
* Sale order: created a new API endpoint to define payment information on sale order creation.
* Sale order: created a new API endpoint to define partner information on sale order creation.

#### Account

* Invoice: added a new button to remove a payment from an invoice.
* Invoice: added a new button to cancel a payment from an invoice.
* FEC import: manage analytic move lines.
* FEC import: added a new button to visualize the imported lines.
* Account: added a new button to copy accounts to other companies.

#### Bank payment

* Bank statement: added CAMT.053 file support.

#### Stock

* Packaging: created a new API endpoint to manage packaging.
* Packaging: created a new API endpoint to manage packaging line.
* Packaging: created a new API endpoint to create a logistical form.
* Packaging: created a new API endpoint to manage logistical form.
* Product: added a default stock location configuration.
* Stock location: added consignment stock for subcontracting.
* Stock move: created a new API endpoint to update a stock move description.

#### Production

* Packaging: added packaging feature and improved logistical form.

#### Maintenance

* Maintenance: created a new API endpoint to create maintenance requests.

#### Production

* Production order: created a new batch to created production order.
* Operation order: created a new API endpoint to create and manage consumed product on operation order.

#### Project

* Project: added favourites projects and observer members.

#### Contract

* Contract: added a new button to create an intervention from a contract.
* Contract: created a new API endpoint to manage a to-do list.

#### Quality

* Product: added a new product characteristics feature.
* Tracking number: added a new product characteristics feature.
* Partner: added a new quality tab in form.
* Product: added a new quality tab in form.
* Quality: improved quality audit.
* Quality: added required documents feature.
* Quality improvement: created a new API endpoint to manage attached files.

#### Mobile Settings

* Maintenance: added new configuration to manage maintenance requests.
* QI detection: added new configuration to set a default value for QI detection.

### Changes

#### Base

* Added colors for selections.
* Added check messages on init and demo data.
* Data backup: added a reference date to be taken into account.
* Data backup: added a new import origin column.
* Data backup: sorted imported file column.
* Translation: added a tracking workflow with history.
* Product: moved unit fields to the correct panels.
* Data import: technical improvement of CSV data import.
* Avanced export: added an option to select the title of the column to export.

#### HR

* Employee: added storage of the daily salary cost in employee form.
* Job application: improved the view and added new fields.
* Extra hours: removed max limitation on increase field.

#### Purchase

* Call for tenders: added generation of a sale order from offers.
* Call for tenders: added a new default sequence.
* Call for tenders: added generation of call for tenders from MRP.
* Partner price list: added possibility to directly select purchase partner from the form.

#### Sale

* Partner price list: added possibility to directly select sale partner from the form.

#### Account

* Reconcile: added new fields on grid view.
* Accounting report: updated the title of the field 'Display only not completely lettered move lines' for 'Exclude totally reconciled moves'.
* Journal: updated demo data.
* Fixed asset: managed different customer on fixed asset disposal.


#### Bank payment

* Bank statement query: added translatable property to the name field.

#### Stock

* Stock move: grouped grid views by status.

#### Supplychain

* Sale order line: added estimated shipping date.
* Purchase order line: added estimated receipt date.
* Purchase order: automatically compute 'interco' field on purchase order generation.

#### Project

* Project planning time: grouped grid view by employee field.
* Timesheet line: grouped grid view by employee field.

#### Contract

* Contract: grouped grid view by status field.

#### Business project

* Removed business project from community edition.

### Fixes

#### Base

* Moved print email method to axelor-message.
* Sale order/partner: fixed translations.
* App base: fixed translation of product sequence type.
* Init/demo data: fixed some init and demo data.

#### Sale

* Invoice: fixed an issue where an invoice ventilation could throw an exception.

#### Account

* Preparatory process: fixed an issue with multi tax.

#### Stock

* Mass stock move: improved display and process.

#### Supplychain

* Sale order: fixed the display of stock move linked to a sale order.

#### Production

* Bill of material: added default value for calculation quantity.
* Manuf order: fixed relation with production order.

[8.5.16]: https://github.com/axelor/axelor-open-suite/compare/v8.5.15...v8.5.16
[8.5.15]: https://github.com/axelor/axelor-open-suite/compare/v8.5.14...v8.5.15
[8.5.14]: https://github.com/axelor/axelor-open-suite/compare/v8.5.13...v8.5.14
[8.5.13]: https://github.com/axelor/axelor-open-suite/compare/v8.5.12...v8.5.13
[8.5.12]: https://github.com/axelor/axelor-open-suite/compare/v8.5.11...v8.5.12
[8.5.11]: https://github.com/axelor/axelor-open-suite/compare/v8.5.10...v8.5.11
[8.5.10]: https://github.com/axelor/axelor-open-suite/compare/v8.5.9...v8.5.10
[8.5.9]: https://github.com/axelor/axelor-open-suite/compare/v8.5.8...v8.5.9
[8.5.8]: https://github.com/axelor/axelor-open-suite/compare/v8.5.7...v8.5.8
[8.5.7]: https://github.com/axelor/axelor-open-suite/compare/v8.5.6...v8.5.7
[8.5.6]: https://github.com/axelor/axelor-open-suite/compare/v8.5.5...v8.5.6
[8.5.5]: https://github.com/axelor/axelor-open-suite/compare/v8.5.4...v8.5.5
[8.5.4]: https://github.com/axelor/axelor-open-suite/compare/v8.5.3...v8.5.4
[8.5.3]: https://github.com/axelor/axelor-open-suite/compare/v8.5.2...v8.5.3
[8.5.2]: https://github.com/axelor/axelor-open-suite/compare/v8.5.1...v8.5.2
[8.5.1]: https://github.com/axelor/axelor-open-suite/compare/v8.5.0...v8.5.1
[8.5.0]: https://github.com/axelor/axelor-open-suite/compare/v8.4.8...v8.5.0
