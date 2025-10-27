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

[8.5.1]: https://github.com/axelor/axelor-open-suite/compare/v8.5.0...v8.5.1
[8.5.0]: https://github.com/axelor/axelor-open-suite/compare/v8.4.8...v8.5.0
