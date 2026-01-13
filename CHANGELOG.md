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
