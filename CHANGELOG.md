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

[9.1.2]: https://github.com/axelor/axelor-open-suite/compare/v9.1.1...v9.1.2
[9.1.1]: https://github.com/axelor/axelor-open-suite/compare/v9.1.0...v9.1.1
[9.1.0]: https://github.com/axelor/axelor-open-suite/compare/v9.0.11...v9.1.0
