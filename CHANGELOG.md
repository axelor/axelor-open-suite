## [9.0.8] (2026-04-30)

### Fixes
#### Base

* Company: new legal information fields
* Update studio dependency to 4.0.6.
* App Base: fixed today date field appearing twice on the form view.
* Advanced export: fixed duplicated rows when a selection field is overridden.
* Partner: fixed wrong field name in setContactPartnerDomain causing contactPartnerSet to allow selecting any partner.
* File source connector: fixed SFTP private key path not resolved error.
* Base: fixed batch history panel refresh after first save on a new record.
* ProjectPlanningTime: fixed projectPlanningTime's endDateTime computation issue
* Company: replaced the display of workshopList with a panel dashlet.
* Base: fixed intermittent 'file could not be generated' error during sale order finalization by closing the FileInputStream after attach.
* Message: fixed temporary email sending not being blocked when email sending is deactivated in the base app.
* Base: fixed per-company configuration grids rendering empty when multi-company mode is disabled.
* Partner: fixed missing automatic completion of type, company and currency when creating a third party using Sirene API.
* PriceList: fix typeSelect field not editable when creating from Referential

#### Account

* Accounting reports: fixed empty custom report generated on a newly created company.
* Invoice line: new field eco-tax amount and mention, with default defined on product.
* Invoice: fixed 'Suppl. Invoices to pay' to include validated advance payment invoices too.
* Invoice: add vatSystemSelect field
* InvoiceNote: updated modelisation to extract invoice type in a new separate object.
* Invoice: fixed BIRT report failing with 'column does not exist' and duplicate column metadata after the vatLiabilitySelect/vatSystemSelect rename.
* Accounting batch: fixed opening-only close/open accounts batch failing when generate result move is enabled.
* Move: fixed MappingException when changing payment condition on an unsaved duplicated move.
* BankDetails: add field InvoiceNoteType.
* Account: add dedicated merge action for customer and supplier credit notes.
* AccountConfig: New field penaltyRateNote to separate penalty rate note from legal note
* Vat exemption reason: added VAT exemption reason in VAT summary table in invoice and sale order reports.
* Accounting report: fixed 'DADS report declaration preparatory process' report to work when company's customer account is not configured.
* Vat exemption reason: added VAT exemption reason management on invoices, sale orders, and purchase orders.
* Payment Session: fixed pagination skipping invoice terms beyond the first page during session validation.
* Account: hide print button in mass entry grid and form views.
* InvoiceNote: updated management of invoice category.

#### Bank Payment

* Bank reconciliation: fix isPosted flag and ending balance calculation
* Bank statement: fix column width and font in Birt reports.
* Move: fixed HibernateException on onLoad after generating counterpart when invoice terms are created.

#### Budget

* Sale order: fixed same name panel in sale order form.

#### Cash Management

* Opportunity : fixed company bank details not auto-filled on opportunity and not carried over to sale order.
* Forecast: exclude archived reports and summaries from dashboard.

#### Contract

* Contract: fixed an error raised when validating a revaluation formula.
* Contract: filled ref/refId on traceback so the failing contract is identified when a batch raises an error.

#### Human Resource

* HR: added button to display accounting moves linked to an expense.
* Timesheet: fixed timesheet line unit display
* HR batch: fixed french translation for leave reasons field in leave management batch.

#### Production

* Manufacturing order: fixed double WAP computation and duplicate stock location line history entry when finishing a manufacturing order.
* Production: fixed NPE and incorrect form displayed when clicking a component node in the BOM tree view.
* Sale order: use AppSaleService to fetch AppSale configuration.
* MRP: included manufacturing orders consuming a product when MRP is filtered on raw materials, so projected stock reflects expected consumption.
* Production: fixed tracking number continuity during partial production.

#### Purchase

* Call tender: fixed the currency error when generating a purchase order due to a contact being selected as supplier partner.
* Purchase: allow editing trading name on draft purchase orders.
* Call tender: fixed error when generating purchase order without a company.

#### Quality

* Company: fixed french translation of Quality config button.

#### Sale

* Sale order: fixed multiple quantities check skipped on a new sale order line.
* Sale: fixed performance issue in sale order line loading caused by repeated DI resolution.
* Sale order line: fixed type selection in sale order line when pack management is disabled.
* Sale order line: fixed delivery state hilite regardless of sale order status.
* Sale order : fixed error when adding a pack without any products while editable tree view is activated.

#### Stock

* Inventory: fixed duplicate inventory lines for tracked products when filling lines.
* Stock location: fixed drag and drop in tree view.
* Stock location: fixed an error when saving a valued stock location.
* Stock location: fixed value indicator not displayed in edit mode.
* StockMove: fixed NPE when we mass invoice and the stock move has an address null.
* Stock: fixed stock location form marked dirty when opened.
* Stock: fixed demo data import failure caused by a missing carrier partner reference in stock_logisticalForm.

#### Supply Chain

* Sale order: fixed customer credit overrun not being detected at quotation finalization when the current quotation pushes the total above the accepted credit.
* Supplychain: fixed performance issue in sale order to stock move generation for orders with many lines.
* MRP: fixed NullPointerException in MRP processing when a product had a null productTypeSelect or excludeFromMrp.
* Sale Order: fixed LazyInitializationException when generating stock moves for lines with different estimated shipping dates.


### Developer

#### Base

- Company: New fields legalInformation and legalFormAndCapital.

Script to run : 
ALTER TABLE base_company                                                                                                                                    
    ADD COLUMN IF NOT EXISTS legal_information VARCHAR(255),                                                                                                
    ADD COLUMN IF NOT EXISTS legal_form_and_capital VARCHAR(255);

---

- PartnerGenerateService: modified configurePartner method signature to include a new parameter Map<String, Boolean> partnerTypeData.

#### Account

- Product: New fields defaultEcoTaxAmount and defaultEcoTaxMention.
- InvoiceLine: New fields ecoTaxAmount and ecoTaxMention.

Script to run : 
ALTER TABLE base_product
  ADD COLUMN IF NOT EXISTS default_eco_tax_amount DECIMAL(20,10),
  ADD COLUMN IF NOT EXISTS default_eco_tax_mention VARCHAR(255);

ALTER TABLE account_invoice_line
  ADD COLUMN IF NOT EXISTS eco_tax_amount DECIMAL(20,10),
  ADD COLUMN IF NOT EXISTS eco_tax_mention VARCHAR(255);

---

add vatSystemSelect field on invoices and simplified computation process using previous partner.accountingSituation.vatSystemSelect

- Invoice: New field vatSystemSelect.

Script to run: 
-- 1. Add vat_system_select column on account_invoice
ALTER TABLE account_invoice
    ADD COLUMN IF NOT EXISTS vat_system_select INTEGER;

-- 2. Backfill supplier invoices (operation_type_select IN (1, 2))
-- from AccountingSituation of the invoice's partner
UPDATE account_invoice inv
SET vat_system_select = sit.vat_system_select
FROM account_accounting_situation sit
WHERE sit.partner = inv.partner
  AND sit.company = inv.company
  AND sit.vat_system_select IN (1, 2)
  AND inv.operation_type_select IN (1, 2)
  AND inv.status_select IN (1, 2, 3)
  AND inv.vat_system_select IS NULL;

-- 3. Backfill customer invoices (operation_type_select IN (3, 4))
-- from AccountingSituation of the company's partner
UPDATE account_invoice inv
SET vat_system_select = sit.vat_system_select
FROM account_accounting_situation sit, base_company c
WHERE c.id = inv.company
  AND sit.partner = c.partner
  AND sit.company = inv.company
  AND sit.vat_system_select IN (1, 2)
  AND inv.operation_type_select IN (3, 4)
  AND inv.status_select IN (1, 2, 3)
  AND inv.vat_system_select IS NULL;

-- 4. Fallback: default to 1 (Standard regime) for unresolvable invoices
UPDATE account_invoice
SET vat_system_select = 1
WHERE vat_system_select IS NULL OR vat_system_select = 0;

---

Replace free-text name/type fields on InvoiceNote with a M2O reference
to a new InvoiceNoteType entity (code + name).
This standardizes note classification using UNTDID 4451 codes
(AAB, AAI, ABL, ACC, ADN, BAR, BAQ, BLU, PMT, PMD, REG, SUR)
for electronic invoicing compliance.

Steps:
1. Create account_invoice_note_type table with standard reference data
2. Add invoice_note_type FK column on account_invoice_note
3. Migrate existing records: match type → code, create new types for unmatched
4. Add invoice_note_type FK column on base_bank_details (#110332)
5. Drop old columns (name, type) from account_invoice_note

Script to run : 
-- 1. Create InvoiceNoteType table
CREATE TABLE IF NOT EXISTS account_invoice_note_type (
  id BIGINT NOT NULL,
  version INTEGER DEFAULT 0,
  archived BOOLEAN,
  created_on TIMESTAMP,
  updated_on TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  import_id VARCHAR(255),
  import_origin VARCHAR(255),
  attrs JSONB,
  code VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  CONSTRAINT account_invoice_note_type_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS account_invoice_note_type_seq START WITH 1 INCREMENT BY 1;

-- Reset sequence to max existing id if table already has data
SELECT setval('account_invoice_note_type_seq',
  GREATEST((SELECT COALESCE(MAX(id), 0) FROM account_invoice_note_type) + 1, 1), false);

-- 2. Insert standard UNTDID 4451 reference data
INSERT INTO account_invoice_note_type (id, version, code, name, created_on)
SELECT nextval('account_invoice_note_type_seq'), 0, v.code, v.name, NOW()
FROM (VALUES
  ('AAB', 'Payment term'),
  ('AAI', 'General information'),
  ('ABL', 'Government information'),
  ('ACC', 'Factor assignment clause'),
  ('ADN', 'Type of transaction reason'),
  ('BAR', 'Processing Instructions'),
  ('BAQ', 'VAT exemption reason'),
  ('BLU', 'Waste information'),
  ('PMT', 'Payment information'),
  ('PMD', 'Payment detail/remittance information'),
  ('REG', 'Regulatory information'),
  ('SUR', 'Supplier remarks')
) AS v(code, name)
WHERE NOT EXISTS (
  SELECT 1 FROM account_invoice_note_type t WHERE t.code = v.code
);

SELECT setval('account_invoice_note_type_seq',
  GREATEST((SELECT COALESCE(MAX(id), 0) FROM account_invoice_note_type) + 1, 1), false);

-- 3. Add invoice_note_type FK column on account_invoice_note
ALTER TABLE account_invoice_note
  ADD COLUMN IF NOT EXISTS invoice_note_type BIGINT;

-- 4. Migrate existing InvoiceNote records

-- 4a. Create InvoiceNoteType for unmatched non-null types
INSERT INTO account_invoice_note_type (id, version, code, name, created_on)
SELECT
  nextval('account_invoice_note_type_seq'),
  0,
  sub.type,
  COALESCE(sub.first_name, sub.type),
  NOW()
FROM (
  SELECT
      n.type,
      (SELECT n2.name
       FROM account_invoice_note n2
       WHERE n2.type = n.type
         AND n2.name IS NOT NULL
         AND n2.name <> ''
       LIMIT 1) AS first_name
  FROM account_invoice_note n
  WHERE n.type IS NOT NULL
    AND n.type <> ''
    AND n.type NOT IN (SELECT code FROM account_invoice_note_type)
  GROUP BY n.type
) sub;

-- 4b. Link InvoiceNote records to their InvoiceNoteType
UPDATE account_invoice_note n
SET invoice_note_type = t.id
FROM account_invoice_note_type t
WHERE n.type IS NOT NULL
AND n.type != ''
AND n.type = t.code;

-- 4c. Log count of notes left with NULL invoice_note_type
-- (notes whose original `type` was NULL or empty).
DO $$
DECLARE
  null_count BIGINT;
BEGIN
  SELECT COUNT(*) INTO null_count
  FROM account_invoice_note
  WHERE invoice_note_type IS NULL;
  IF null_count > 0 THEN
      RAISE NOTICE
          '[#109676] % InvoiceNote rows left without invoice_note_type (original type was NULL or empty)',
          null_count;
  END IF;
END $$;

-- 5. Add FK constraint
ALTER TABLE account_invoice_note
  DROP CONSTRAINT IF EXISTS fk_account_invoice_note_invoice_note_type;

ALTER TABLE account_invoice_note
  ADD CONSTRAINT fk_account_invoice_note_invoice_note_type
  FOREIGN KEY (invoice_note_type)
  REFERENCES account_invoice_note_type(id);

-- 6. Add invoice_note_type FK column on base_bank_details (#110332)
ALTER TABLE base_bank_details
  ADD COLUMN IF NOT EXISTS invoice_note_type BIGINT;

ALTER TABLE base_bank_details
  DROP CONSTRAINT IF EXISTS fk_base_bank_details_invoice_note_type;

ALTER TABLE base_bank_details
  ADD CONSTRAINT fk_base_bank_details_invoice_note_type
  FOREIGN KEY (invoice_note_type)
  REFERENCES account_invoice_note_type(id);

-- 7. Drop old columns
ALTER TABLE account_invoice_note
  DROP COLUMN IF EXISTS name,
  DROP COLUMN IF EXISTS type;

---

- Invoice: removed specificNoteOnInvoiceToDisplayPanel and added bank detail note computation in note lists on ventilation.

Script to run: 
ALTER TABLE base_bank_details
  ADD COLUMN IF NOT EXISTS invoice_note_type BIGINT;

ALTER TABLE base_bank_details
  DROP CONSTRAINT IF EXISTS fk_base_bank_details_invoice_note_type;

ALTER TABLE base_bank_details
  ADD CONSTRAINT fk_base_bank_details_invoice_note_type
  FOREIGN KEY (invoice_note_type)
  REFERENCES account_invoice_note_type(id);

---

- AccountConfig: New field penaltyRateNote.

Script to run : 
ALTER TABLE account_account_config
  ADD COLUMN IF NOT EXISTS penalty_rate_note TEXT;

---

A new entity `VatExemptionReason` (table `account_vat_exemption_reason`) has been introduced in
axelor-base to store standard EU/FR VAT exemption codes (e.g. VATEX-EU-AE, VATEX-EU-IC, VATEX-EU-G).

The `vatExemptionReason` field (many-to-one to `VatExemptionReason`) has been added to the following entities:
- `TaxEquiv` (axelor-base) — statically assigned per tax equivalence rule
- `FiscalPosition` (axelor-base) — fallback reason when customerSpecificNote is true
- `Partner` (axelor-base) — partner-level override when customerSpecificNote is true
- `InvoiceLine` (axelor-account)
- `InvoiceLineTax` (axelor-account)
- `SaleOrderLine` (axelor-sale)
- `SaleOrderLineTax` (axelor-sale)
- `PurchaseOrderLine` (axelor-purchase)
- `PurchaseOrderLineTax` (axelor-purchase)

Resolution logic (implemented in `OrderLineTaxService.resolveVatExemptionReason`):
- If `fiscalPosition.customerSpecificNote` is true: use `partner.vatExemptionReason`, falling back to `fiscalPosition.vatExemptionReason` if the partner reason is null.
- Otherwise: use `taxEquiv.vatExemptionReason`.

The specific note displayed on the document is now sourced from `vatExemptionReason.note` when present,
falling back to `taxEquiv.specificNote`.

--- Constructor changes ---

`InvoiceLineServiceImpl` — new parameter `OrderLineTaxService orderLineTaxService` added at the end.

`InvoiceLineSupplychainService` (axelor-supplychain) — new parameter `OrderLineTaxService orderLineTaxService` added.

`SaleOrderLineTaxServiceImpl` — new parameter `OrderLineTaxService orderLineTaxService` added at the end.

`SaleOrderLineFiscalPositionServiceImpl` — new parameter `OrderLineTaxService orderLineTaxService` added at the end.

`SaleOrderLineProductServiceImpl` — new parameter `OrderLineTaxService orderLineTaxService` added at the end.

`SaleOrderLineProductSupplychainServiceImpl` (axelor-supplychain) — new parameter `OrderLineTaxService orderLineTaxService` added; forwarded to super().


--- New API ---

`OrderLineTaxService` (axelor-base):
  VatExemptionReason resolveVatExemptionReason(FiscalPosition fiscalPosition, TaxEquiv taxEquiv, Partner partner);

--- Demo data ---

New file `account_taxExemptionReason.csv` (EN and FR) providing 11 standard EU/FR exemption reason records.
Existing file `account_taxEquiv.csv` (EN and FR) updated with a new `vatExemptionReason_code` column
linking each tax equivalence rule to the appropriate exemption code.

--- Migration script ---

-- Create VatExemptionReason table
CREATE TABLE account_vat_exemption_reason (
  id                  BIGINT NOT NULL,
  archived            BOOLEAN,
  import_id           CHARACTER VARYING(255),
  import_origin       CHARACTER VARYING(255),
  process_instance_id CHARACTER VARYING(255),
  version             INTEGER,
  created_on          TIMESTAMP(6) WITHOUT TIME ZONE,
  updated_on          TIMESTAMP(6) WITHOUT TIME ZONE,
  attrs               JSONB,
  code                CHARACTER VARYING(255) NOT NULL,
  name                CHARACTER VARYING(255) NOT NULL,
  note                TEXT NOT NULL,
  created_by          BIGINT,
  updated_by          BIGINT,
  CONSTRAINT account_vat_exemption_reason_pkey PRIMARY KEY (id),
  CONSTRAINT uk_45cls6n7anyc3uvtlrk30qwsg UNIQUE (import_id),
  CONSTRAINT uk_mmety0ylwddkkoouqdjj2bweg UNIQUE (code),
  CONSTRAINT fk_rj551obg6gydoq5rqbotlspqj FOREIGN KEY (created_by) REFERENCES auth_user(id),
  CONSTRAINT fk_hntwiq8h6xox2gwgj1p5hcw8b FOREIGN KEY (updated_by) REFERENCES auth_user(id)
);
CREATE INDEX account_vat_exemption_reason_name_idx ON account_vat_exemption_reason (name);

-- Add vatExemptionReason column to TaxEquiv
ALTER TABLE account_tax_equiv
  ADD COLUMN IF NOT EXISTS vat_exemption_reason BIGINT,
  ADD CONSTRAINT fk_9oarhmr8lmiuk22aaum7mv1cr
    FOREIGN KEY (vat_exemption_reason) REFERENCES account_vat_exemption_reason(id);

-- Add vatExemptionReason column to FiscalPosition
ALTER TABLE account_fiscal_position
  ADD COLUMN IF NOT EXISTS vat_exemption_reason BIGINT,
  ADD CONSTRAINT fk_9gk5x59ubme5j4ah5767nxq04
    FOREIGN KEY (vat_exemption_reason) REFERENCES account_vat_exemption_reason(id);

-- Add vatExemptionReason column to Partner
ALTER TABLE base_partner
  ADD COLUMN IF NOT EXISTS vat_exemption_reason BIGINT,
  ADD CONSTRAINT fk_kdtmpwgucpdl237qmd7qmbsud
    FOREIGN KEY (vat_exemption_reason) REFERENCES account_vat_exemption_reason(id);

-- Add vatExemptionReason column to InvoiceLine
ALTER TABLE account_invoice_line
  ADD COLUMN IF NOT EXISTS vat_exemption_reason BIGINT,
  ADD CONSTRAINT fk_8iumyh08vva5e12ghetgvrbtp
    FOREIGN KEY (vat_exemption_reason) REFERENCES account_vat_exemption_reason(id);

-- Add vatExemptionReason column to InvoiceLineTax
ALTER TABLE account_invoice_line_tax
  ADD COLUMN IF NOT EXISTS vat_exemption_reason BIGINT,
  ADD CONSTRAINT fk_2qn28jl8wurud5ab05b11na99
    FOREIGN KEY (vat_exemption_reason) REFERENCES account_vat_exemption_reason(id);

-- Add vatExemptionReason column to SaleOrderLine
ALTER TABLE sale_sale_order_line
  ADD COLUMN IF NOT EXISTS vat_exemption_reason BIGINT,
  ADD CONSTRAINT fk_pi3ws18w8qw11iuc9worbw7vp
    FOREIGN KEY (vat_exemption_reason) REFERENCES account_vat_exemption_reason(id);

-- Add vatExemptionReason column to SaleOrderLineTax
ALTER TABLE sale_sale_order_line_tax
  ADD COLUMN IF NOT EXISTS vat_exemption_reason BIGINT,
  ADD CONSTRAINT fk_5sa3d2cd74unv4tpu9ga77nei
    FOREIGN KEY (vat_exemption_reason) REFERENCES account_vat_exemption_reason(id);

-- Add vatExemptionReason column to PurchaseOrderLine
ALTER TABLE purchase_purchase_order_line
  ADD COLUMN IF NOT EXISTS vat_exemption_reason BIGINT,
  ADD CONSTRAINT fk_ssmlm5sg6syiqma2q9el7usao
    FOREIGN KEY (vat_exemption_reason) REFERENCES account_vat_exemption_reason(id);

-- Add vatExemptionReason column to PurchaseOrderLineTax
ALTER TABLE purchase_purchase_order_line_tax
  ADD COLUMN IF NOT EXISTS vat_exemption_reason BIGINT,
  ADD CONSTRAINT fk_98soi4hx4knmlr57865ea152y
    FOREIGN KEY (vat_exemption_reason) REFERENCES account_vat_exemption_reason(id);

---

- AccountConfig: New field defaultInvoiceCategory.
- Invoice: new field invoiceCategory, computed according to invoice lines product type.
- AccountConfig: modified typeList mechanism (in InvoiceProductStatement) to use new selection.
- Invoice: deleted invoiceProductStatement and replaced it by a new mention in invoiceNoteList with code 'REG'.
Script to run : 
ALTER TABLE account_account_config
  ADD COLUMN IF NOT EXISTS penalty_rate_note TEXT,
  ADD COLUMN IF NOT EXISTS default_invoice_category_select VARCHAR(255);

ALTER TABLE account_invoice
  ADD COLUMN IF NOT EXISTS invoice_category_select VARCHAR(255);

ALTER TABLE base_company
  ADD COLUMN IF NOT EXISTS legal_information VARCHAR(255),
  ADD COLUMN IF NOT EXISTS legal_form_and_capital VARCHAR(255);

UPDATE account_invoice_product_statement SET type_list = 'goods' WHERE type_list = 'storable';
UPDATE account_invoice_product_statement SET type_list = 'services' WHERE type_list = 'service';
UPDATE account_invoice_product_statement SET type_list = 'mixed' WHERE type_list = 'storable,service';
UPDATE account_invoice_product_statement SET type_list = 'mixed'
WHERE type_list LIKE '%storable%' AND type_list LIKE '%service%' AND type_list != 'mixed';

UPDATE account_invoice i
SET invoice_category_select = 'goods'
WHERE i.operation_type_select IN (1, 3)
  AND i.operation_sub_type_select = 1
  AND i.status_select IN (2, 3)
  AND i.invoice_category_select IS NULL
  AND EXISTS (
    SELECT 1 FROM account_invoice_line il WHERE il.invoice = i.id
  )
  AND NOT EXISTS (
    SELECT 1 FROM account_invoice_line il
    LEFT JOIN base_product p ON il.product = p.id
    WHERE il.invoice = i.id
      AND (p.product_type_select != 'storable' OR il.product IS NULL)
);

UPDATE account_invoice i
SET invoice_category_select = 'services'
WHERE i.operation_type_select IN (1, 3)
  AND i.operation_sub_type_select = 1
  AND i.status_select IN (2, 3)
  AND i.invoice_category_select IS NULL
  AND EXISTS (
    SELECT 1 FROM account_invoice_line il WHERE il.invoice = i.id
  )
  AND NOT EXISTS (
    SELECT 1 FROM account_invoice_line il
    LEFT JOIN base_product p ON il.product = p.id
    WHERE il.invoice = i.id
      AND (p.product_type_select != 'service' OR il.product IS NULL)
  );

UPDATE account_invoice i
SET invoice_category_select = ac.default_invoice_category_select
FROM account_account_config ac
WHERE ac.company = i.company
  AND i.operation_type_select IN (1, 3)
  AND i.operation_sub_type_select = 1
  AND i.status_select IN (2, 3)
  AND i.invoice_category_select IS NULL
  AND ac.default_invoice_category_select IS NOT NULL
  AND EXISTS (
    SELECT 1 FROM account_invoice_line il WHERE il.invoice = i.id
);

CREATE SEQUENCE IF NOT EXISTS account_invoice_note_seq START WITH 1 INCREMENT BY 1;
SELECT setval('account_invoice_note_seq',
  GREATEST((SELECT COALESCE(MAX(id), 0) FROM account_invoice_note) + 1, 1), false);

INSERT INTO account_invoice_note (id, version, invoice, invoice_note_type, note, created_on)
SELECT
  nextval('account_invoice_note_seq'),
  0,
  i.id,
  nt.id,
  i.invoice_product_statement,
  NOW()
FROM account_invoice i
CROSS JOIN account_invoice_note_type nt
WHERE nt.code = 'REG'
  AND i.status_select = 3
  AND i.invoice_product_statement IS NOT NULL
  AND i.invoice_product_statement != '';

ALTER TABLE account_invoice
  DROP COLUMN IF EXISTS invoice_product_statement;

#### Bank Payment

During bank reconciliation validation, lines with a posted number (postedNbr)
but no associated move line were not marked as posted (isPosted) and were
incorrectly blocked by the incomplete line check.

The ending balance was also miscalculated for these lines, as their
credit/debit amounts were ignored when no move line was linked.

Additionally, when a move line is reconciled without a bank statement line,
the reconciled amount on the move line is now correctly set.

#### Cash Management

- CashManagementModule: Added binding of OpportunitySaleOrderSupplychainServiceImpl to OpportunitySaleOrderCashManagementServiceImpl.

#### Production

ManufOrderStockMoveService: added finishInStockMoves, finishOutStockMoves, partialFinishIn, partialFinishOut methods.
CostSheetService: added two computeCostPrice overloads accepting an overrideProducedQty (used by ManufOrder finish when OUT moves are still planned) and an additional pair of excludedConsumedLineIds / excludedProducedLineIds sets. The exclusion sets complement the date-based filter on previousCostSheetDate (which has off-by-one semantics) so the cost sheet stays strictly batch-specific even when partial and final finishes happen on the same day.
ManufOrderWorkflowServiceImpl: constructor now requires UnitConversionService.

---

In `SaleOrderLineBomSyncServiceImpl`, replaced `AppSaleRepository` with `AppSaleService` in the constructor.

#### Stock

Constructor of `InventoryService` now requires a `ProductCompanyService`.

---

- StockLocationUtilsService: removed methods `getStockLocationValue(StockLocation)` and `getStockLocationValue(Long, Long)`.

Script to run:
DELETE FROM meta_action
WHERE name = 'action-stock-location-method-set-stock-location-value';

## [9.0.7] (2026-04-16)

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
* Update Axelor Open Platform to 8.1.2

#### Account

* Accounting report type: fixed missing company records in demo data.
* Account: fixed payment session eligible terms including incompatible payment mode types.
* Mass entry: fixed error when trying to delete a move of type mass entry.
* FEC Import: fixed missing currency on imported moves when Idevise column is empty.
* Accounting report: fixed Aged balance report to use only validated invoice payments.
* Invoice: fix VAT system forced to payment on advance payment invoice tax lines.
* Accounting chart: fixed FRA_PCG tax import leaving activeTaxLine null due to incomplete importIds in account_tax.csv.
* Accounting batch: fixed SemanticException when running close/open accounts batch.
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

* Purchase order: fixed an error occurring when installing purchase order demo data.
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

## [9.0.6] (2026-04-02)

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

* Production: fixed an issue where using 'Consume in stock moves' on a Manufacturing Order could also realize finished-product stock moves.
* Production: fixed machine charge dashboard percentage precision by using seconds instead of truncated minutes.
* Sale order: fixed an error occurring when personalizing bill of material from sale order.
* Sale order: fixed operation quantity returning 0 for semi-finished product when exploding multi-level BOM.
* Manufacturing order: fixed an issue where cost sheet generated had a wrong status.
* Production: fixed circular self-injection of BillOfMaterialService in BillOfMaterialServiceImpl.
* Stock details by product: fixed projected stock not showing manufacturing order component consumptions.
* Cost sheet: fixed human resource cost not scaled by number of cycles when using per-hour cost type.
* Production: fixed wrong stock move line generation in operation order when updating planned quantity.

#### Project

* Project template : fixed error while generating a project from a project template with several tasks.

#### Purchase

* Purchase order: fixed trading name field being read-only when the purchase order was generated from a call for tenders.
* Purchase order lines: fixed the slowdown when selecting a product.

#### Quality

* Quality control / Quality Alert: fixed inconsistent date issue.

#### Sale

* Configurator: fixed an error message occurring when opening an attribute line.
* Configurator: fixed unique constraint violation when importing a configurator with numeric attribute names.
* Sale: fixed circular self-injection of ProductRestService in ProductRestServiceImpl.
* Sale: optimized margin computation by caching considerZeroCost flag outside loops.
* Configurator: fixed product code being overwritten on sale order line regeneration.
* Sale order: improved performance when generating report with large database.
* Configurator: fixed an issue occurring when trying to export a configurator.

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

## [9.0.5] (2026-03-19)

### Fixes
#### Base

* Update Axelor Open Platform to 8.1.1
* Product: fixed an issue on product creation if serial number already existed.
* Demo data: added missing weight unit conversions (kg to g, kg to mg) in English CSV.
* Partner: fixed address created via API SIRENE not appearing on reports.
* Fixed internal errors caused by invalid relation-id query comparisons in multiple references.
* Update studio dependency to 4.0.4

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
* Production: fixed wrong negative WAP calculation on manufacturing order finish after partial finish.

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

#### Talent

* Job application: fixed the visibility of actions panel.

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

---

`ManufOrderStockMoveService.updatePrices(ManufOrder, BigDecimal)` has been removed.
It is replaced by `updatePrices(ManufOrder, BigDecimal, Set<Long> stockMoveIds)`, which only
processes the outgoing stock moves whose IDs are provided. This prevents re-processing stock
moves already realized in previous partial finishes, which could cause a negative WAP.
Client overrides of the old method must be migrated to the new signature.

#### Project

Script to remove the unused action : 
"DELETE FROM meta_action WHERE name = 'action-project-record-manage-timespent-reset-values';

#### Sale

- Replaced AppBaseService and AppSaleService with ProductSaleDomainService in SaleOrderLineDomainServiceImpl constructor.

#### Stock

- Added StockMoveService to the StockMoveLineServiceImpl constructor.
- Added StockMoveService to the StockMoveLineServiceSupplychainImpl constructor.
- Added StockMoveService to the StockMoveLineProductionServiceImpl constructor.

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

[9.0.8]: https://github.com/axelor/axelor-open-suite/compare/v9.0.7...v9.0.8
[9.0.7]: https://github.com/axelor/axelor-open-suite/compare/v9.0.6...v9.0.7
[9.0.6]: https://github.com/axelor/axelor-open-suite/compare/v9.0.5...v9.0.6
[9.0.5]: https://github.com/axelor/axelor-open-suite/compare/v9.0.4...v9.0.5
[9.0.4]: https://github.com/axelor/axelor-open-suite/compare/v9.0.3...v9.0.4
[9.0.3]: https://github.com/axelor/axelor-open-suite/compare/v9.0.2...v9.0.3
[9.0.2]: https://github.com/axelor/axelor-open-suite/compare/v9.0.1...v9.0.2
[9.0.1]: https://github.com/axelor/axelor-open-suite/compare/v9.0.0...v9.0.1
[9.0.0]: https://github.com/axelor/axelor-open-suite/compare/v8.5.9...v9.0.0
