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

[9.1.1]: https://github.com/axelor/axelor-open-suite/compare/v9.1.0...v9.1.1
[9.1.0]: https://github.com/axelor/axelor-open-suite/compare/v9.0.11...v9.1.0
