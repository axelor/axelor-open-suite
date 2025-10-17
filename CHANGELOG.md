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

[8.5.0]: https://github.com/axelor/axelor-open-suite/compare/v8.4.8...v8.5.0