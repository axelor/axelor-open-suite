## [9.1.6] (2026-08-20)

### Fixes
#### Base

* Product: fixed product demo data issue which was showing company specific details after saving it.

#### Account

* Invoice: fixed tax discrepancy error during invoice ventilation caused by inconsistent rounding between the per-line and aggregate tax calculations used in the move's tax consistency check.

#### Production

* Production: fixed incorrect component requirements in MRP calculations for bills of materials producing multiple units.
* Production: fixed a manufacturing order generating new tracking numbers instead of reusing the original ones after a produced stock move line was manually deleted.

#### Purchase

* Purchase order: fixed unit price not reset to the product default purchase price when the ordered quantity is outside the supplier catalog quantity range.

#### Sale

* Sale order: fixed sale order blocking control is not being checked on order confirmation.

#### Supply Chain

* Stock move: fixed scheduled outgoing stock move invoicing failing when the linked sale order has no team.

## [9.1.5] (2026-08-13)

### Fixes
#### Base

* Upgrade to AOP 8.2.3
* Partner: fixed Client Situation report balances not displayed when the partner has no assigned user.
* Price list line: fixed the number of decimals not taken into account when creating a price list line.
* Address: remove geocoding call from repository save to prevent latency and failures on bulk/API saves, reuse HTTP client for map geocoding calls, and cache repeated geocoding lookups.
* Partner: fixed clearing the registration code not clearing SIREN, NIC and tax number, and wrongly showing the invalid registration code label.

#### Account

* Chart of accounts (FR): updated the French general chart of accounts (PCG) to the 2026 version in the l10n referential and demo data.
* Invoice: fixed invoice category not being filled for customer refund invoices.
* Invoice: fixed invoice term remaining amount not updated when reconciling a payment voucher's excess payment move line against another invoice from the move line.
* Invoice term: fixed bank details wrongly required with outgoing Direct debit / IPO / Exchanges payment modes.
* UMR: fixed umrNumber auto-fill generating duplicate values for the same company/partner/date.
* Accounting report: fixed account sort order in custom accounting report detail rows.
* Analytic distribution: fixed a rounding drift where the sum of generated analytic move lines could differ from the source amount.
* Fixed asset: fixed economic and IFRS duration in month set from periodicity type instead of the category duration when generating a fixed asset.
* Payment voucher: fixed the payment wizard showing incorrect due dates and origins on opening, before searching for items to pay, and when loading or resetting selected lines.
* Account: fixed automatic partner account creation mode not being applied during chart of accounts and demo data installation.
* Invoice: fixed a zero-amount supplier invoice (created from mixed stock moves) being blocked at ventilation with a misleading 'already been paid' error.

#### Bank Payment

* Payment session: fixed missing file error issue on bank order confirmation.

#### Budget

* Budget: fixed 'Compute budget distribution' failing with an arithmetic error when the budget key covers an amount that does not divide evenly, and fixed a stray unallocated cent left on the last budget line in that case.

#### Cash Management

* Forecast recap: fixed an issue where an invoice entry reconciled with its payment was still shown as an outstanding payable in the cash flow forecast

#### Contract

* Contract batch: fixed 'Display related contracts' preview not matching the contracts actually processed by the invoicing batch.

#### CRM

* Opportunity: fixed status changes from the list view allowing a missing partner or loss reason.

#### Helpdesk

* Helpdesk: fixed deadline date being cleared when saving a ticket with SLA disabled.

#### Human Resource

* Project: fixed filter on project task of waiting and validated timesheet line dashlets on saved timesheet lines.

#### Production

* Production: fixed MO and operation staying "In Progress" after a partial finish already covers the planned quantity.
* Production: replaced a NullPointerException with a clear configuration error when computing cycles without a production process line.
* Production: fixed an issue where fully consumed raw materials were omitted from the first manufacturing order closing cost sheet.
* Manuf order: fixed subcontracted manufacturing order production being blocked when the goods receipt lacks a shipment reference/date, by carrying it over automatically from the goods receipt linked to the subcontracting service purchase order.
* Manufacturing order: fixed inconsistent stock move types generated during partial production declarations of a subcontracted manufacturing order, now generating goods receipts for all declarations.
* Production: fixed NPE when adding a consumed product to an operation with no consumed products (per-operation consumption mode).

#### Quality

* Control plan frequency: fixed missing error message when creating a control plan frequency with an already existing name.

#### Sale

* Sale order: fixed global percentage discount computing a wrong discount on the last order line when its quantity is greater than 1.
* Sale order: fixed the version number field being shown on sale orders when the quotation/order split is enabled, since it has no functional purpose there.

#### Stock

* Stock: fixed invoice ventilation being incorrectly blocked after generating a new stock move from a return.
* Stock correction: allowed selecting or creating a tracking number with no prior stock history.

#### Supply Chain

* Stock move: fixed the invoicing status staying at 'Not invoiced' after ventilating a supplier invoice that was manually linked to the stock move without invoice lines generated from it.
* Invoice/Order line: fixed unit price and discounted price columns sharing the same title.
* Supplychain: fixed MRP purchase proposals generating duplicate purchase order lines for the same product instead of consolidating them.


### Developer

#### Helpdesk

TicketServiceImpl constructor changed: AppHelpdeskRepository/AppBaseService params
replaced by AppHelpdeskService.

#### Production

- ProdProcessLineComputationService: modified getNbCycle method signature to throw AxelorException.

#### Quality

Added `ControlPlanFrequencyService.checkUniqueName(ControlPlanFrequency)` method.

## [9.1.4] (2026-07-31)

### Fixes
#### Base

* Partner: fixed registration number cleaning to also strip dots, slashes and hyphens (not just spaces) before validation and SIREN/NIC extraction.
* Base: fixed Client Situation report showing no sale orders when the partner has no assigned user.

#### Account

* Account: fixed the direction of VAT lines on accounting cutoff moves.
* Invoice: fixed ventilation check on invoice category ignoring displayItemsCategoriesOnPrinting config.
* Accounting batch: fixed the analytic distribution being lost when several move lines are merged into one for the 'Accounting cut-off' batch.
* Invoice: fixed HTML tags in notes causing XML rejection by converting HTML fields to plain text before note creation.
* PaymentReminder: settled invoice terms still printed on the PDF (missing amount_remaining filter)
* Accounting report: fixed wrong values when the acquisition date of fixed asset is equal to the report start date for report type 'Summary of gross values and depreciation'.
* Invoice: fixed the title displayed on the credit notes search popup when merging credit notes.
* Account: fixed missing reported-balance lines for some partners when closing annual accounts with partner allocation enabled.
* MoveLine: fixed VAT system priority to check supplier's VAT on delivery before account's VAT system.
* Account: fixed negative lines not being removed from payment sessions on the first attempt.
* Period: fixed period closure to only process moves from selected journals when closing per journal.
* Invoice: fixed the GED folder keeping the pre-ventilation invoice number after ventilation.

#### Bank Payment

* Bank statement line: fixed BIRT report being empty when bank statement line tables have different physical column orders.

#### Budget

* Global budget: fixed a LazyInitializationException when creating the default budget version.

#### CRM

* CRM: fixed the lead's description not being copied onto the generated opportunity's Customer description field.

#### Human Resource

* Project planning time: fixed unique constraint error when saving multiple planning lines with the same display planned time.
* Partner: fixed default company not being set on the partner created from the employee form.

#### Production

* Product: fixed the access to the parent bill of material from the 'Where-used list' dashlet.
* Production: fixed MRP calculation creating irrelevant manufacturing proposals for unrelated products.
* Sale order line: fixed the domain on production process field to filter by company.

#### Sale

* Sale order: fixed manually edited delivery/invoicing address text being overwritten on every save.
* Sale: fixed Cart sale order generation when a trading name is required.

#### Stock

* Stock rules: fixed alert email not sent when recipients are resolved from the user/team templating formula.
* Stock: fixed an issue in tracking number forms.
* Stock: fixed inventory validation failing when tracked and untracked quantities are mixed.
* Stock: fixed untranslated chart status labels caused by corrupted or unnormalized locale values.
* StockMove: reset fullySpreadOverLogisticalFormsFlag when a LogisticalForm is deleted or set back to draft.

#### Supply Chain

* Purchase Order: fixed purchase order amountInvoiced not updated for a credit note generated from a reversion stockMove.
* Supplychain: fixed Cart stock location not defaulted from Partner and cleared when generating the Sale Order.
* Mass stock invoicing: Set fiscal position from partner when invoicing a standalone stock move.
* Purchase order line: fixed line editor being wrongly marked as modified on open when analytic accounts were already set.
* Purchase order: fixed receipt state staying on 'Partially received' when a line's quantity is corrected to 0.
* Sale order: fixed the invoicing state wrongly showing 'Partially invoiced' on a sale order fully reimbursed by a refund invoice generated from a stock move reversion, with no prior sale invoice.
* Supplychain: fixed new delivery note generated after editing a confirmed sale order using the original quantity instead of the newly edited one.
* Purchase order line: fixed the 'Invoiced' flag no longer being set when ventilating a direct invoice, since business-project was removed.
* Sale/Purchase: fixed timetable invoicing status and re-invoicing checks breaking after invoice merging.
* Stock move: fixed incoming partial invoices being marked as fully invoiced after ventilation.
* Invoicing and purchase orders: fixed non-deductible taxes being applied to unrelated deductible tax lines.


### Developer

#### Account

The `InvoiceServiceImpl` constructor now requires a `DMSFileRepository` parameter. Custom
subclasses must inject and forward this dependency.

#### Production

`MrpServiceProductionImpl#createManufOrderMrpLines` gained a new `boolean producedProductInScope`
parameter.

---

Changed SaleOrderLineDomainProductionService.getProdProcessDomain parameters from (saleOrderLine) to (saleOrderLine, saleOrder).

#### Sale

The CartSaleOrderGeneratorServiceImpl and CartSaleOrderGeneratorSupplychainServiceImpl constructors now require an AppBaseService parameter.

#### Stock

```sql
-- Script to update fully_spread_over_logistical_forms_flag field to keep consistent data. Please check data before applying.
UPDATE stock_stock_move sm
SET fully_spread_over_logistical_forms_flag = FALSE
WHERE sm.fully_spread_over_logistical_forms_flag = TRUE
  AND EXISTS (
    SELECT 1 FROM stock_stock_move_line sml
    JOIN sale_sale_order_line sol ON sol.id = sml.sale_order_line
    WHERE sml.stock_move = sm.id AND sol.type_select = 0   -- TYPE_NORMAL
  )
  AND NOT EXISTS (
    SELECT 1 FROM supplychain_packaging_line pl
    JOIN supplychain_packaging pkg ON pkg.id = pl.packaging
    JOIN stock_stock_move_line sml ON sml.id = pl.stock_move_line
    JOIN stock_logistical_form lf ON lf.id = pkg.logistical_form
    WHERE sml.stock_move = sm.id AND lf.status_select = 3
  );
```

#### Supply Chain

The SaleOrderServiceSupplychainImpl constructor now requires a SaleOrderLineQtyToDeliverService parameter.

---

Changed the protected method signature
`isStockMoveInvoicingPartiallyActivated(Invoice)` to
`isStockMoveInvoicingPartiallyActivated(StockMove)`.

---

`PurchaseOrderLineTaxComputeService#computeAndAddTaxToList` now requires a
`Map<PurchaseOrderLineTax, Set<PurchaseOrderLine>>` argument containing the purchase order lines
that contributed to each aggregated tax line. Custom callers and implementations must collect
and pass this contribution map.

## [9.1.3] (2026-07-17)

### Fixes
#### Base

* User: fixed the password pattern config key after the upgrade to AOP 8.2.
* Fiscal position: fixed wrong tax domain on tax equivalence preventing proper reverse charge setup when replacement tax has a different rate than the original tax.
* User: fixed the password change that no longer worked after the upgrade to AOP 8.2.
* Product model: fixed wrong tab title 'Product' to display 'Product models' when opening a product model form.
* Data backup: fixed AutoImportModelMap using natural keys that are not guaranteed unique and required causing silent data loss on restore.
* Import configuration: fixed not null violation of data file on import configuration while using S3 storage.
* Partner: fixed 'Fill from Sirene API' button triggering name validation before the API fills it.

#### Account

* Accounting batch: fixed the wrong currency amount on the merged move line generated by the 'Accounting cut-off' batch when several move lines are merged into one.
* Invoice: fixed an issue where changing the due date on a draft invoice prevented further editing and left invoice term amounts out of sync with the invoice lines.
* Invoice: fixed a blocking invoice category error when validating with the 'Skip ventilation step' option enabled; the category is now computed automatically before validation.
* Payment session: fixed credit note invoiceTerm staying marked as paid after cancellation.
* Reconcile: fixed wrong remaining amounts on invoice terms and invoices when reconciling multicurrency entries.
* Move template: added a warning at validity check when 'Compute tax at creation' is enabled while the template also contains tax lines.
* Accounting batch: fixed wrong Groovy expression error in DebtRecovery printing template file name.
* BankDetails: add field InvoiceNoteType.
* Account: fixed fully allocated advance payment with VAT on payment still being proposed on subsequent customer invoices.
* InvoicingPaymentSituation: allowed selecting the same company on several lines as long as one bank details remains available, enforcing uniqueness on the company/bank details couple.

#### Cash Management

* Forecast recap: fixed double counting on partially invoiced orders.

#### Contract

* Contract: fixed invoice origin field always shows version 0 contract ID for versioned contracts.
* Partner: fixed the display of supplier/customer contract panels when partner is not a supplier/customer.

#### CRM

* Lead: fixed company department in generated contact from lead.

#### Human Resource

* EXPENSE: fixed NPE when refreshing an expense after adding an kilometric expense line.

#### Production

* Prod process: fixed filter on stock location of residual product and produced product fields.

#### Stock

* Stock: restore automatic tracking number assignment during Mass Transfer Pick step.
* Stock move: fixed billing address not displayed on delivery BIRT report when address position is set to right.

#### Supply Chain

* Stock move: fixed error when invoicing stock moves for service products without a stock unit.
* Sale/Purchase order: fixed merge quotations action allowing to merge a single quotation.
* Stock move: fixed an error when reverting a merged stock move linked to multiple sale orders.
* Purchase request: fixed supplier details not populated on generated purchase order.


### Developer

#### Base

`TaxEquivService#getTaxDomain(TaxEquiv, boolean, boolean)` signature changed to `getTaxDomain(TaxEquiv)`.

---

Password management realigned with the AOP 8.2 flow (AuthService.changePassword): the inline
password fields no longer worked (the platform stopped encrypting transientPassword on save) and
were removed; the platform "Change password" button is used instead.

Removed public API (adapt custom modules): UserService.changeUserPassword /
getPasswordPatternDescription, UserController.validate / validatePassword / generateRandomPassword.

The "password changed" email is now sent from AuthServiceBaseImpl (bound in BaseModule) on any
AuthService.changePassword when User.sendEmailUponPasswordChange is true (admin change + batch,
never self-service); the flag is one-shot. Password complexity is now enforced by the AOP pattern
policy instead of Java.

Configuration migration in axelor-config.properties (rename + enable):
```
# before
user.password.pattern = <regex>
# after
user.password.pattern.enabled = true
user.password.pattern.value = <regex>
```

To keep a clean meta_action table, run:
```sql
DELETE FROM meta_action WHERE name IN (
  'action-user-validate-password',
  'action-user-generate-random-password',
  'action-attrs-user-generate-random-password',
  'action-method-user-generate-random-password'
);
```

#### Account

- Added InvoiceTermRecordService as a constructor parameter to InvoiceTermServiceImpl.
- Removed the unused InvoiceTermService constructor parameter from InvoiceTermRecordServiceImpl.
- Added new method boolean checkIfAmountCustomizedInvoiceTerms(List<InvoiceTerm>) to the InvoiceTermService.

---

Invoice form view: added the action-method 'action-invoice-method-compute-invoice-category' into the 'action-invoice-group-before-validate' action-group

---

- Invoice: removed specificNoteOnInvoiceToDisplayPanel and added bank detail note computation in note lists on ventilation.

#### Stock

Added the TrackingNumberService to the MassStockMoveNeedToPickedProductServiceImpl constructor

#### Supply Chain

- PurchaseRequestToPoCreateServiceImpl: added protected `setPurchaseOrderSupplierDetails(PurchaseOrder)` — override and call `super` to populate additional supplier fields on the generated PO.
- PurchaseRequestToPoCreateServiceSupplychainImpl: constructor updated with a new `AccountConfigService` parameter.

## [9.1.2] (2026-07-02)

### Fixes
#### Base

* Upgrade to AOP 8.2.2
* Partner Sirene API: fixed null value in street name computation.
* Data backup: fixed 'Update Import Id' failing for entities with joined table inheritance, and fixed long type fields incorrectly taking the import id value.
* Product: reset revaluation section fields on product copy.
* Partner Sirene API: added missing siren api buttons on grid view of customer and supplier.
* Base: fixed address city selection to show only cities matching the entered zip code.
* Data Backup: fixed backup creation failing when 'Update Import Id' option is enabled for entities using joined inheritance.
* Barcode: fixed EAN_13 barcode type to accept 13-digit serial numbers.
* Updated xsd schema URL to fix a build issue.
* Product: improved performance when editing a product referenced in a large number of price lists.
* Product category: fixed drag and drop in tree view.
* Discount: fixed discount amount not being emptied after changing discount type select
* added missing french translation for 'Template Rules' and 'Routings'.
* Update studio dependency to 4.0.7
* Unit conversion: display a clear error message when the formula evaluation fails.
* Base: fixed duplicate default address on partner after merge.

#### Account

* Reconciliation: fixed NPE when unlettering an advance payment move with tax payment move lines having no reconcile.
* Invoice line: fixed duplicate product, filter on supplier and type fields briefly shown on new line popup.
* Account: fixed ArithmeticException when saving a tax line with rate exceeding 2 decimal digits.
* Invoice: fixed invalid PDF signature on printed invoice; single copy is served unchanged and multiple copies are re-signed after merging.
* Move line/Move line query/Reconciliation: display an error message when a selected moveLine belongs to an invoice with a pending payment.
* Invoice: fixed ventilation failing with tax amounts not equal when several invoice lines share the same product account with analytic distribution template.
* Invoice: fixed regenerated invoice PDF copies being returned without the certificate signature.
* Invoice: removed 'Updated copy' option from the Reports print wizard for supplier invoices and supplier credit notes.
* Move line: fixed lettering without partner ignoring payment difference threshold, causing always-partial reconciliation.
* Account: fixed SemanticException when using AND operator in Analytic Move Line Query filter.
* Invoice: pre-fill the default account and taxes on invoice lines without a product using the partner's accounting situation.
* Account: fixed reverse-charge VAT lines missing on auto counterpart when supplier has a default expense account.
* Invoice term : fixed readonly condition of due date in form view opening from menu.
* Payment Voucher: display proper error message when cheque deposit journal is not configured.
* Accounting dashboard: fixed error when opening the dashboard caused by invalid date arithmetic in chart queries.
* Move line: fixed the display of the company currency in grid views and form.
* Move: fixed financial discount miscalculated on manual journal entries with reverse-charge (intra-EU / import) VAT.
* Invoice: fixed cannot set back to draft a canceled invoice due to required condition.
* Account: fixed Mass Entry validation failing when move lines have different partners.
* Accounting batch: fixed the date of analytic distribution lines on generated moves to use the move accounting date instead of the origin date for the 'Accounting cut-off' batch.
* Invoice: fixed anomaly traceback not saved when mass ventilation fails.
* Invoice: fixed internal server error when recording a payment on a foreign currency invoice where the company-currency micro-residual converts to zero in the invoice currency.

#### Bank Payment

* Bank payment: fixed CFONB120 multi-period import rejected on non-chronological period order.

#### Contract

* Contract: fixed 'Initial price per year' and 'Yearly price revalued' showing 0.00 in contract line grid on draft amendments.
* Contract: fixed fiscal position not being set on generated invoice.
* Contract: fixed close button appears after a termination that failed due to the notice period.

#### CRM

* Event: fixed opportunity, event lead and partner linked via relatedToSelect not displayed on event-grid.

#### Human Resource

* Expense: fixed ventilation failing when the VAT system could not be resolved from the partner accounting situation.
* HR: fixed Employee grid not refreshing the contact partner full name inline after editing the form.

#### Production

* Production: fixed stock reservation not requested when planning a Manufacturing Order with 'Auto request qty for manuf orders' enabled.
* Production: fixed sub manufacturing orders being created with zero or negative quantity when sub-component stock covers the requirement.
* Sale order: fixed the error on confirmation when both customerStockMoveGenerationAuto and autoPlanManufOrderFromSO are activated.
* Manuf order: fixed duplicate in and out stock moves while planning manuf orders from diff tabs.
* Manuf order : fixed no session error when partially completing a manufacturing order with tracking number

#### Sale

* Sale order: fixed global discount changes not being applied to the order lines, so the per-line discounts (and the discounts carried to the invoice) are now recomputed when the global discount type or amount changes.
* Sale order: on the editable sale order line grid, the discount type and discount amount fields are now correctly set as read-only according to the global discount management.
* Sale order: fixed subtotal cost price that could not be modified.
* Sale order: fixed add line from configurator button not available on confirmed sale order being edited.
* Sale order: made the per-line discount fields read-only in the editable grid when a global discount is set on the order, consistently with the form view.
* Sale dashboard: fixed inconsistencies between charts due to incorrect date bounds, missing company filter, and non-YTD comparison in turnover charts.

#### Stock

* Product: fixed stock indicators to show variants stocks for product model.
* Mass stock move: fixed an error occurring when using 'Pick all' or 'The rest'.
* Tracking number: fixed error when creating new tracking number from menu.
* Stock rules: fixed wrong refill type filter when product by companies feature is enabled.

#### Supply Chain

* Sale order: fixed deletion of a sale order line failing when a canceled stock move line still referenced it.
* Stock history update batch: fix the blocking errors raised on a second run over the same period and ensure the stock rotation category is correctly assigned
* Stock depreciation: restrict product selection to stock-managed products.
* Sale: fixed mass invoicing to process only selected customer deliveries.
* Stock: fixed logistical form dashlet on customer deliveries incorrectly showing all existing logistical forms instead of only those linked to the stock move, and added automatic logistical form creation on outgoing stock move save.
* Stock move: fixed logistical form not cleared when duplicating a stock move.
* Purchase: fixed mass invoicing to process only selected delivery receipts.
* MRP: fixed issue with deleted record reappearing in draft status after refresh.
* Purchase order: fixed invoice generation allowed on a totally invoiced purchase order.
* Supplychain: fixed MRP Family not grouping purchase proposals when demands fall exactly on the day window limit.
* Purchase: fixed invoice qty set to 0 when regenerating after cancellation.
* MRP: fixed duplicate sales forecast lines in MRP result.


### Developer

#### Base

Add a new ProductSupplychainServiceImpl that extends ProductServicePurchaseImpl

#### Account

- MoveLineConsolidateService: modified findConsolidateMoveLine method signature, changed first parameter from Map<List<Object>, MoveLine> map to Map<List<Object>, List<MoveLine>> map.

---

Added `AccountingSituationService` and `FiscalPositionAccountService` as constructor parameters to `InvoiceLineGenerator`.
Added `AccountingSituationService` and `FiscalPositionAccountService` as constructor parameters to `InvoiceLineServiceImpl` and its subclasses.

#### Production

- SaleOrderLineMOGenerationSingleLineService: `generateManufOrders()` signature updated — added `BigDecimal grossQtyRequested` parameter.
- SaleOrderLineMOGenerationService: `generateManufOrders()` signature updated — added `BigDecimal grossQtyRequested` parameter.
- ProductionOrderSaleOrderMOGenerationService: `generateManufOrders()` signature updated — added `BigDecimal grossQtyRequested` parameter.

#### Sale

-- Script
ALTER TABLE sale_sale_order_line
  ADD COLUMN IF NOT EXISTS manual_sub_total_cost_price numeric(20, 3),
  ADD COLUMN IF NOT EXISTS is_sub_total_cost_price_manually_edited boolean;

#### Supply Chain

The `onDelete` actions `action-mrp-validate-delete-mrp-reset` and
`action-mrp-validate-delete-multi-mrp-reset` have been removed from the MRP form and
grid views. The `MrpManagementRepository.remove()` override that was incorrectly
resetting and saving the record instead of deleting it has been replaced with a correct
implementation that deletes associated `MrpLine` records before removing the MRP itself.

```sql
DELETE FROM meta_action WHERE name = 'action-mrp-validate-delete-mrp-reset';
DELETE FROM meta_action WHERE name = 'action-mrp-validate-delete-multi-mrp-reset';
```

## [9.1.1] (2026-06-18)

### Fixes
#### Base

* Birt report: fixed file not found error and concurrent generation conflicts when printing birt reports.
* Expense: fixed electronic signature failure when the uploaded file has a short filename.
* Product: fixed missing fields when company-specific product configuration is disabled.
* Advance export: fixed language in batch when user is null.
* Account management: prevent duplicate company entries per tax.
* App base: added a separate configuration to activate email sending on stream messages.
* Address: removed the department field.

#### Bank Payment

* Bank reconciliation: fixed wrong translation of warning message when selecting lines.
* Bank order: included the invoice's printedPDF file in the 'Display invoice' document list.
* BANKORDER : allowed multidate for pain.xxx.cfonb160.dco type of BankOrderFileFormat
* Bank order: removed DatatypeConfigurationException, JAXBException and IOException from public method signatures.

#### Production

* Production: fixed configurator-generated ProdProcessLine not inheriting startingDuration, endingDuration, and setupDuration from the WorkCenter, leading to underestimated BOM cost prices.
* Production: fixed tracking number continuity during partial production.

#### Project

* Project: moved 'Dashboard' and 'Activities' into a new panel tab.

#### Purchase

* Call tender: Add checkbox 'Attach file in email'
* Provide feature for Product attribute configuration for call tender
* Call tender: Add a new integer field Delivery time (days)
* Blocking: improved code organization for purchase blocking
* Purchase Requests: fixed an issue where purchase order was not linked with PurchaseRequest
* Call tender : Provided the way to compare the offers (Product attributes by supplier & Suppliers' response by product)
* Call tender need: Added a new html text field description
* Call tender: Added report configuration
* Call tender: Added Button 'Import offer' to import the excel file per supplier.
* Call tender: Split button 'Send call for tenders' into 'Generate emails' & 'Send emails'

#### Stock

* Stock move: fixed logistical forms dashlet showing all logistical forms instead of only those linked to the current stock move.


### Developer

#### Bank Payment

Added MetaFiles as parameter in BankOrderLineOriginServiceImpl constructor
Added MetaFiles as parameter in BankOrderLineOriginServiceHRImpl constructor

---

The following public method signatures were changed to remove checked exceptions JAXBException, IOException, and DatatypeConfigurationException:

- InvoicePaymentMoveCreateService.createInvoicePaymentMove(InvoicePayment)
- InvoicePaymentValidateService.validate(InvoicePayment, boolean)
- InvoicePaymentValidateService.validate(InvoicePayment)
- BankOrderService.validate(BankOrder)
- BankOrderService.generateFile(BankOrder)
- BankOrderValidationService.validateFromBankOrder(InvoicePayment, boolean)
- BankOrderValidationService.realize(BankOrder)
- BankOrderValidationService.validatePayment(BankOrder)
- BankOrderValidationService.confirm(BankOrder)
- BankOrderFileService.generateFile()
- BankOrderFile00800101Service.generateFile()
- BankOrderFile00800102Service.generateFile()
- BankOrderFile00100102Service.generateFile()
- BankOrderFile00100103Service.generateFile()
- BankOrderFileAFB160DCOService.generateFile()
- BankOrderFileAFB160Service.generateFile()
- BankOrderFileAFB320XCTService.generateFile()
- BatchBankPaymentService.createBankOrder(Batch)
- BatchBankPaymentService.createBankOrderFromPaymentScheduleLines(Batch)
- BatchBankPaymentService.createBankOrderFromMonthlyPaymentScheduleLines(Batch)

#### Purchase

The `PURCHASE_BLOCKING` constant has been moved from `axelor-base` to `axelor-purchase` module
for better modularity.

**Migration:** No action required. The entity package remains `com.axelor.apps.base.db.Blocking`,
so existing imports continue to work.

---

- PurchaseRequestServiceImpl consturctor is updated to introduce PurchaseRequestToPoCreateService

## [9.1.0] (2026-06-15)

### Features

#### Base
* App base: added a dedicated configuration to enable email sending on stream messages.
* Product: added an option to conditionally display the Safety panel, and kept the company-specific panel visible even when no field is configured.
* Partner: made the trading name available on the partner view (shared with the Intervention module).

#### Account
* Payment voucher: added a button to compute the total amount.
* Move lines: added a summary bar on the grid and allowed editable selection of analytic move lines.
* Closure assistant: disclosed additional information on the compute result screen.

#### Bank Payment
* Bank order: allowed multidate for the `pain.xxx.cfonb160.dco` file format.

#### Human Resource
* Leave request: added new leave units (business day, calendar day).

#### Mobile Settings
* Added configurations to control stock move display and tracking number validation on the mobile app.

#### Maintenance
* Added preventive maintenance management: an automatic generation batch (with anticipation days) and maintenance bills of materials with component management.
* Integrated maintenance manufacturing orders into MRP, including in-progress orders.

#### Production
* Production process: added hazard phrase management on the process and its lines.

#### Project
* Project: moved 'Dashboard' and 'Activities' into a dedicated panel tab to speed up form loading.

#### Purchase
* Call for tenders: reworked offer sending — split 'Send call for tenders' into 'Generate emails' and 'Send emails', with an option to attach a file to the email.
* Call for tenders: added offer import from an Excel file per supplier, and offer comparison views with charts (product attributes by supplier, suppliers' response by product).
* Call for tenders: added report configuration and product attribute configuration.
* Call for tenders need: added an HTML description field and a 'Delivery time (days)' field.
* Purchase order line: added a pricing process triggered on product change.

#### Sale
* Sale order line: added a reference to the main sale order on its sub-lines.
* Sale / purchase order: made the 'Show Lines' action respect the grid selection on quotation grids.

#### Stock
* Added a stock depreciation feature.
* Stock move: added barcode management.
* Inventory: added an option to block stock moves during an ongoing inventory.
* Stock move report: take into account the title lines coming from the sale/purchase order.

#### Supply Chain
* MRP: added options to exclude a product family or category from MRP, and take planned manufacturing-order stock moves into account.

### Changes

#### Base
* Address: removed the department field.
* Company / workshops: replaced the stock location list with a dashlet.
* Removed the tag automatically set from the company/trading name configuration.

#### Account
* Account management: changed the unique constraint to be company-specific.

#### Sale
* Sale order: removed the obsolete global-discount field.

#### Stock
* Stock move line: automatically fill the perishable and warranty settings.
* Stock correction: added a control when validating during an ongoing inventory on the same stock location.

#### Production
* Manufacturing order: optimized the process and added a warning when starting an operation or a manufacturing order.

#### Supply Chain
* Purchase order: set the intercompany flag on generated purchase orders.
* Mass invoicing: grouped the menu entries.
* Sale order: convert sale orders with sub-lines into a stock move.

### Fixes

#### Base
* Product: fixed product creation when the serial number already exists.
* Partner: fixed the address retrieved from the SIRENE API.

#### Account
* Move / move line: fixed several display and computation issues (consistent partner display, amount-remaining recalculation, VAT system on tax-account change, hiding 'Generate tax lines' for incompatible origins).
* Reconciliation: fixed reconciliation of tax move lines when no partner is set on OD moves.
* Journal: fixed the sequence not being imported on newly generated company journals.
* Period: fixed anomaly handling during the close process.
* Payment voucher: fixed moves flagged as ignored in debt recovery being wrongly excluded.

#### Bank Payment
* Bank order: fixed SEPA file generation to use the company currency amount.
* Bank reconciliation: prevent editing a move line linked to a validated bank reconciliation.
* Payment session: fixed payment moves not being generated in some bank-order auto-confirmation cases.

#### CRM
* Catalog: fixed the email form not opening after sending an email.

#### Human Resource
* Timesheet line: fixed several issues (activity when the product is missing, computation, product check, missing editor buttons).
* Expense: fixed product creation with category-based sequencing.

#### Production
* Manufacturing order: fixed stock move price and WAP computation when finishing, outsourcing cost, and order merging.
* Cost sheet: fixed human cost valuation and now include same-day realized moves in the ratio.
* Production process: fixed BOM decimals on phases and work center duration/changes (including configurator-generated lines).
* CBN process: fixed NPEs occurring during the net requirements calculation.
* Sale order line: fixed sub-line generation for deep BOM levels and NPEs on BOM change.

#### Project
* Project / task templates: fixed custom field behavior and task template time fields.

#### Purchase
* Purchase request: fixed purchase order generation/linking and added a company check to prevent an NPE.

#### Sale
* Sale order: fixed the 'Recalculate Prices' button resetting prices to zero, the product code being overwritten on regeneration, the configurator copy, and an NPE on customer product display.

#### Stock
* Stock move: fixed the available status and future-quantity computations (including the split tracking number configuration) and an NPE on creation.
* Inventory: fixed an NPE when exporting to CSV.

#### Talent
* Fixed the job application form.

#### Intervention
* Fixed intervention generation from a contract.

[9.1.6]: https://github.com/axelor/axelor-open-suite/compare/v9.1.5...v9.1.6
[9.1.5]: https://github.com/axelor/axelor-open-suite/compare/v9.1.4...v9.1.5
[9.1.4]: https://github.com/axelor/axelor-open-suite/compare/v9.1.3...v9.1.4
[9.1.3]: https://github.com/axelor/axelor-open-suite/compare/v9.1.2...v9.1.3
[9.1.2]: https://github.com/axelor/axelor-open-suite/compare/v9.1.1...v9.1.2
[9.1.1]: https://github.com/axelor/axelor-open-suite/compare/v9.1.0...v9.1.1
[9.1.0]: https://github.com/axelor/axelor-open-suite/compare/v9.0.11...v9.1.0
