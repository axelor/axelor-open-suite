## [8.1.11] (2024-11-14)

### Fixes
#### Base

* Updated Axelor Open Platform to 7.1.10.
* Updated studio module to version 3.1.6.
* Updated message module to version 3.1.3.
* Updated utils module to version 3.1.3.
* Partner: tax number will be displayed for suppliers too along with customers.
* Pricing: fixed the product category filter in pricing formula.

#### Account

* Payment voucher: fixed disable apply financial on payment voucher.
* Analytic move line query: fixed always readonly button in 'Analytic move lines to reverse' dashlet.
* Accounting export: fixed skipped lines on accounting export when we have more than 10 000 lines.
* Accounting report: replaced move line partner full name with partner sequence and partner full name in reports.
* Invoice: fixed NPE when clicking on print button on grid lines.
* Move/Analytic: record analytic account on moveline on the reverse move
* Partner: fixed an issue where the partner balance was wrong after an unreconcile.
* FEC Import: fixed importing moves with a validation date.
* Bank reconciliation line: fixed an issue where too much memory could be used when filtering move lines.
* Analytic move line query: optimized filter to handle high data volumes efficiently.

#### Bank Payment

* Bank statement: fixed error when importing bank statement with negative final balance.
* Move/Payment session: added bank order origin in the generated move if needed.

#### Contract

* Contract/Invoice: fixed analytic wrong management on contract line.

#### Human Resource

* Expense API: expense line creation with a manual distance is no longer overriden by computed distance.
* Expense: fixed an error occurring when cancelling an expense payment linked to a bank order.

#### Project

* Project: fixed NPE when opening project in project activity dashboard.

#### Purchase

* Purchase order lines: fixed an issue where the warning about default supplier was not displayed.

#### Quality

* Quality: fixed translation of 'Check conformity'.

#### Sale

* Sale order: fixed display of 'Send email' button when record is not saved.

#### Stock

* Stock move line: fixed tracking number domain filter.

#### Supply Chain

* Stock move: fixed issue where partial invoicing was not working.
* Sale order: fixed an issue where the invoicing of sale order lines was blocked.


### Developer

#### Supply Chain

- Added new arguments to SaleOrderInvoiceService.displayErrorMessageIfSaleOrderIsInvoiceable()
- Updated SaleOrderInvoiceService.computeAmountToInvoice visibility to protected and removed it from interface

## [8.1.10] (2024-10-31)

### Fixes
#### Base

* Sequence: fixed draft prefix when checking for the draft sequence number.
* Birt report: fixed number formatting for excel format.
* Partner: fixed NPE when manually adding an accounting situation.

#### Account

* Move: fixed blocked accounting when missing budget alert is required and account type is not one of the charge, income, immobilisation.
* Accounting batch: fixed multiple auto lettering.
* Accounting cut off batch: fixed wrong analytic distribution and axis on generated moves.
* Partner:  fixed automatic account creation when partner is prospect based on 'Automatic partner account creation mode' in account config.
* Invoice: fixed an issue where too much memory could be used when displaying customer invoice lines.
* Invoice/Move: recompute currency rate of movelines after invoice ventilation.

#### Budget

* Purchase order: removed required condition on company department.

#### Contract

* Contract: fixed invoicing contract with revaluation and prorata enabled.
* Contract: fixed a issue when generating a sale order from a contract

#### CRM

* Opportunity: fixed filter on contact domain.
* Lead: fixed an issue preventing lead conversion when having only CRM module.
* Lead: fixed address while converting a lead to a prospect.

#### Human Resource

* Expense: fixed an issue preventing to go to reimbursed status with a payment mode generating a bank order.

#### Production

* Manufacturing order: fixed issue when updating quantity in manufacturing order.

#### Purchase

* Purchase order: fixed value of 'total without tax' in birt report.

#### Sale

* Sale order: fixed an issue preventing from editing a sale order with editable grid and pending order modification enabled

#### Stock

* Stock move: fixed an error occurring when splitting a stock move line.

#### Supply Chain

* Invoice: fixed invoice line price generated from stock move with different unit.


### Developer

#### Budget

SaleOrderDummyBudgetServiceImpl class was removed, as well as following actions:

- `action-budget-group-purchase-order-on-new-actions`
- `action-budget-purchase-order-record-load-budget-key-config`
- `action-group-budget-saleorder-onload`
- `action-budget-sale-order-record-load-budget-key-config`

## [8.1.9] (2024-10-17)

### Fixes
#### Base

* Webapp: updated Axelor Open Platform dependency to 7.1.7.
* Update axelor-studio dependency to 3.1.4.
* Price list: fixed check on dates on save.
* Partner: when duplicating a partner, do not copy partner addresses and correctly create a copy of the original partner's picture.
* Home action: fixed display issue in user and group form views.

#### Account

* Invoice: fixed error when we duplicate an invoice and change any field on invoice line.
* Account: computed amounts when we change invoice date.
* Tax payment move line: fixed an issue where reverse taxes were not reverted, which was making VAT statement reports wrong.
* Invoice: fixed rounding issue on taxes on ventilated move accounting.
* Accounting report: fixed amounts with currency decimal digits in accounting report 'Revenue and expenditure state'.
* Accounting report: fixed NPE when selecting report type on form opened from year closure.

#### Human Resource

* Timesheet API: fixed an error occurring when creating a timesheet without timer
* Lunch vouchers: fixed an issue where some employees were not included in lunch voucher computation.

#### Project

* Project: fixed code when generating project from sale order.

#### Sale

* Sale Order: fixed an issue were sequence was reset when going back to draft by creating a new version.
* Sale order: added discounted unit price to editable grid.
* Complementary product selected: correctly prevent the user from modfying selected complementary product on a confirmed sale order.

#### Stock

* Stock API: fixed issue on stock correction creation request.
* Inventory: after an import, display filename instead of file path in the confirmation message.
* Stock location: added a filter to not select itself or any sub stock locations as parent stock location.


### Developer

#### Account

Method signature have changed in InvoiceLineService.class :

```java
public void compute(Invoice invoice, InvoiceLine invoiceLine) throws AxelorException;
```

became

```java
public Map<String, Object> compute(Invoice invoice, InvoiceLine invoiceLine) throws AxelorException;
```

---

Please run this SQL script if you have the issue related to reverse taxes in VAT statement report:

```sql
  UPDATE account_tax_payment_move_line tpml
  SET tax_amount = -tax_amount
  WHERE tpml.fiscal_position IS NOT NULL
  AND EXISTS (
  SELECT 1
  FROM account_tax_equiv_reverse_charge_tax_set terc
  LEFT JOIN account_tax_equiv ate ON ate.id = terc.account_tax_equiv
  LEFT JOIN account_tax tax ON tax.id = terc.reverse_charge_tax_set 
  LEFT JOIN account_tax_line tl ON tax.id = tl.tax 
  WHERE ate.fiscal_position = tpml.fiscal_position AND tl.id = tpml.origin_tax_line
  );
```

#### Stock

Created new interface and class `StockLocationAttrsService' and 'StockLocationAttrsServiceImpl`. 
In `stock-location-form` changed parentStockLocation onSelect action from `action-stock-location-attrs-set-parent-stock-location-domain`
to `action-stock-location-method-set-parent-stock-location-domain`, the former action has not been removed.

## [8.1.8] (2024-10-03)

### Fixes
#### Base

* Webapp: updated Axelor Open Platform dependency to 7.1.5.
* ABC analysis: fixed an issue where printing an unsaved analysis was not setting correctly the sequence,and added sequence in demo data.
* Batch: added french translation for '* %s anomaly(ies)'

#### Account

* Accounting report: added filter on company for report type in custom reports if companies are empty.
* Partner blocking: fixed 'blocking partner with late payment' feature which resulted in some processes being stuck.

#### Bank Payment

* Bank order/ Bank statement: fixed broken reports.

#### Business Project

* Invoicing project annex report: fixed empty values in multiple columns.

#### Contract

* Contract template: removed unused condition related to statusSelect field.

#### Helpdesk

* Ticket: fixed the french translation  of 'Assign to me'.

#### Human Resource

* Payroll preparation: improved export to Silae by adding the number of lunch vouchers and employee bonus.
* Payroll preparation: fixed employee bonus amount computation by correctly ignoring lines that were not computed.
* Leave request: fixed the leave reason domain when employee is null.
* Project planning time: fixed error while selecting unit when time unit is null.
* Expense/ Leave request/ Timesheet/ TSTimer: fixed creation with empty employee when connected user doesn't have employee.
* Payroll preparation: improved export to Nibelis by adding the number of working days in the period.

#### Project

* Project: fixed issue related to contact partner when updating client partner.

#### Stock

* Logistical form line: fixed wrong calculation of net weight.
* Stock move line: the advanced filter saved is not displayed unless we refresh the whole page.

#### Supply Chain

* Sale order invoicing: removed partially invoiced flag when invoice is fully refunded.

#### Intervention

* Equipment: fixed contract domain to have only billing contracts.

## [8.1.7] (2024-09-19)

### Fixes
#### Base

* Update axelor-studio dependency to 3.1.3.
* Partner: fixed convert contact into individual partner error when address is null.
* Advanced export: fix export when multiple fields contain same sub path.
* Request: fixed french translation for 'Request'.
* Address: in form view, do not empty the ZIP code when filling the city, and automatically fill the city when the ZIP code is filled.

#### Account

* CutOff/Analytic: fixed cut off batch when using analytic on credit move line.

#### Budget

* Budget level: fixed scales in the tree view.
* Global budget: fixed duplication without budget level.

#### Business Project

* Invoicing project: attach annex to invoice when 'Attach the Annex to the invoice' is enabled while generating invoice.
* Project: prevented an error during project totals computation when parent task had no time unit.
* Project task: fixed product changing on assignedTo when linked to a framework contract.
* Project: prevented an error during project totals computation when spent time percentages were too low.

#### Contract

* Contract: fixed an error when opening a product in an amendment.
* Contract: fixed error while generating contract from opportunities.

#### CRM

* CRM: added missing action for 'Calls monitoring' dashboard.

#### Helpdesk

* Ticket API: fixed an issue where condition in permission were not evaluated correctly to update tickets.

#### Human Resource

* Timesheet line: fixed issue related to default product value.

#### Mobile Settings

* Mobile Dashboard API: fixed an issue where condition in permission were not evaluated correctly to get mobile chart information.

#### Production

* Sequence per workshop config: improve UI to select workshop stock location and manuf order sequence.

#### Purchase

* Purchase order line tax: fixed reverse charge feature on purchase order.

#### Sale

* Configurator: added field 'attributes' and 'indicators' in grid view.

#### Stock

* Stock: fixed an error occurring when splitting a stock move line with a real quantity greater than the expected quantity.

#### Intervention

* Company: fixed display of intervention config button on company when module is uninstalled.
* Contract: 'On-call planning' field will be required if 'On-call management' is true.

## [8.1.6] (2024-09-05)

### Fixes
#### Base

* Partner: fixed an address not linked to a partner in demo data.

#### Account

* Invoice: fixed an issue where tab title for credit note was wrong.
* Invoice payment: fixed move display in payment details grid view.
* Payment Voucher: fixed wrong amount on generated invoice payment when using financial discount.
* Invoice: fixed an issue preventing invoice ventilation when pack feature is used.
* Mass entry move line: increased width of some columns.

#### Budget

* Purchase order line: fixed an issue where budget panel was editable on a confirmed purchase order.

#### Contract

* Contract: fixed an issue where payment mode and payment condition were not filled by default.
* Contract: fixed display issue of 'isPeriodicInvoicing' field.

#### Human Resource

* Timesheet: opened timesheets are no longer dirty when opening the form view.

#### Maintenance

* Menu: fixed icon for 'Configuration' menu.

#### Production

* Manufacturing order: fixed unit conversion when computing missing components label.
* Manufacturing order API: fixed an issue where condition in permission were not evaluated correctly to see and add products from a manufacturing order.
* Operation order API: fixed an issue where condition in permission were not evaluated correctly to update operation orders.
* Manufacturing order: fixed wrong priority on the sub manuf order.
* Manufacturing order: fixed title for produced quantity in produced products form view.
* Manufacturing order: fixed an issue occurring when adding produced products.

#### Supply Chain

* Invoicing: fixed an issue preventing stock moves/order/contracts invoicing with analytic accounting lines.
* Invoicing: fixed internal reference on invoices generated from delivery stock move missing sale order reference.
* Analytic panel: fixed display issue when product family is empty.

## [8.1.5] (2024-08-22)

### Fixes
#### Base

* Address: removed unused fields related to invoicing/delivery/default address in base address form.

#### Account

* Mass entry move line: increased width of the date columns.
* MoveLine: fixed tax management in form view
* Fixed asset: removed disposal info during copy.
* Accounting report: fixed wrong amounts when selecting accounts with parent accounts.
* Invoice: fixed unable to display invoice lines (grid view) when one or more invoices are selected.

#### Bank Payment

* Bank statement: fixed name when loading bank statement lines.

#### Budget

* StockMove/Invoice: fixed technical error when invoicing a stock move line with a value of zero.

#### Business Project

* Project: prevented an error during project totals computation when spent time percentages were too high.
* Project/Kanban: use the same controls as when you change the project status in kanban view.

#### Contract

* Contract: removed unused action.

#### Helpdesk

* Ticket type: fixed 'Ticket per status' chart.

#### Human Resource

* App Project: added missing french translation.
* Employee bonus management: fixed computation process when there is no user linked with employee.
* Project: added missing french translation in the planning tab.
* Leave reason: fixed typo in french translation.

#### Mobile Settings

* Added missing english translation 'Order'.

#### Sale

* Sale order line: take product application start date into account when selecting product.
* Sale config: fixed 'default holdback percentage' display.
* Complementary product: removed field 'optional' from partner views.

#### Stock

* Stock config: fixed translation for 'Send email on supplier arrival cancellation'
* Stock API: fixed an issue where condition in permission were not evaluated correctly to read stocks and modify/create stock moves.
* Inventory API: fixed an issue where condition in permission were not evaluated correctly to update an inventory.

#### Supply Chain

* Supplychain config: fixed 'Update customer accepted credit' process updating the credit for all companies instead of the current one.

## [8.1.4] (2024-08-08)

### Fixes
#### Base

* Partner : fixed error on change of partner type.

#### Account

* AccountingBatch : fixed auto move lettering batch proposals
* AccountingBatch : fixed reconcile by balanced move mode in auto move lettering batch
* AccountingBatch : fixed auto move lettering batch due to negative credit move line amount remaining

#### Bank Payment

* BankOrder : fixed manual multi currency bank order's move generation

## [8.1.3] (2024-07-25)

### Fixes
#### Base

* Webapp: updated Axelor Open Platform dependency to 7.1.4.
* Update axelor-studio dependency to 3.1.2
* Address Template: fixed defaut address template so zip code should come before the city.
* Partner: fixed error popup when opening partner without accounting situations.
* Partner: in demo data, set 'Is internal' on partner Axelor.
* Translation: fixed an issue where 'Canceled', 'Confirmed', 'Received' french translations were wrong.
* Product: reset the serial number on product duplication.

#### Account

* Invoice/Advance Payment: fixed an issue preventing invoice ventilation if multiple advance payments existed.
* Move: fixed debit/credit scale when we change move line currency amount.
* Payment voucher: fixed an issue preventing the payment of invoices with financial discount.
* Move: fixed issue preventing partner selection if the move has a journal with multiple compatible partners.
* Invoice: fixed an issue where an exonerated tax had a VAT system filled.
* Move line mass entry: set description required following account configuration.
* Mass entry: fixed analytic axis empty on partner selection.
* Fixed asset: fixed the depreciation values panel readonly if 'Is equal to fiscal depreciation' is enabled.
* Analytic: fixed required analytic distribution template when the analytic distribution type is per Product/Family/Account.

#### Bank Payment

* Bank reconciliation: fixed total of selected move lines in multiple reconciles when currency is different from company currency.

#### Business Project

* Invoicing project: fixed timesheet line invoicing filter.

#### Contract

* Contract: fixed batch contract revaluation process order.

#### CRM

* Opportunity status: fixed data init to use french status name instead of english.
* Opportunity type: remove unused reference from data init config, this will remove a warning when loading the application on a new database.

#### Human Resource

* Expense line: fixed error when computing kilometric distance without choosing a type.
* Employee: fixed card view display when using dark theme.

#### Production

* Translations: fixed french translation for prod process and bom, form view title was plural instead of singular.
* Product: fixed action from production referenced in base.

#### Project

* Project: fixed planning panel display for unsaved record.
* Project: fixed the typo in french translation for unit help.
* Project API: fixed an issue where condition in permission were not evaluated correctly to access unit and planning time of a project.

#### Purchase

* Purchase order: fixed french typo for 'nouvelles version'.

#### Quality

* Control entry API: fixed an issue where condition in permission were not evaluated correctly to access a control entry.

#### Sale

* Configurator creator: fixed issue related to meta json field simple name.
* Partner: added missing french translation for 'generation type' in complementary product tab.
* Sale order: fixed sale order sequence when creating new version.
* Sale order: fixed an issue preventing from invoicing X% of a sale order as an advance payment where X was greater than the sale order total.

#### Supply Chain

* Timetable: fixed the scale issue to compute amount on change of percentage.
* Stock move/Invoice: fixed unique invoice generation from stock move reversion.
* Sale order: fixed partial invoice generation with title line.


### Developer

#### Production

Created a new action group `action-product-group-production-onload` which will be run onLoad of the product form view,
replacing `action-group-product-onload`: if you override `onLoad` of product form in your module, please check related change.

## [8.1.2] (2024-07-11)

### Fixes
#### Base

* Webapp: updated Axelor Open Platform dependency to 7.1.3.
* Update axelor-studio, axelor-message and axelor-utils dependency to 3.1.1. This is fixing an issue where, with a default gradle configuration, the latest AOP snapshot was used instead of the version set in the webapp.
* Partner: fixed issue related to view extension in CRM, causing an error when reloading views.
* Currency conversion line: fixed an error preventing exchange rate update.
* Fixed an issue where the logo defined in properties was not used in the login page.
* City: fix city demo data import
* Product: Fixed display of tracking number panel.
* Product: fixed NPE when duplicating and saving a product.
* Printing Template: technical refactor to be able to extend in other modules.
* BIRT template: fixed error message when required BIRT parameter is missing.

#### Account

* Unreconcile: fixed invoice terms amount on both move lines.
* Accounting report: fixed missing assets and disposal column value on 'Gross value and depreciations' report.
* Accounting report: fixed Summary table of VAT Statement on invoices report which was not displaying all data.
* Block customers with late payment batch: fixed an issue where the batch did not block some partners.
* Analytic/InvoiceLine: removed analytic when account does not allow analytic in all configuration.
* Accounting situation: fixed VAT system display when partner is internal.
* MoveReverse: fixed imputation on reverse move invoice terms.
* Accounting report: fixed summary table on the first page of the VAT Statement on payments displaying wrong values.

#### Bank Payment

* BankStatementLine: Set demo statement with operation dates/value date in 2024

#### Budget

* Purchase order line: fixed an issue where changing the account in tab budget changed the analytic configuration on the line.

#### Contract

* Contract line: fixed an issue where analytic distribution panel was never hidden.

#### CRM

* Catalog: fixed an issue where the user could upload files other than PDF.

#### Human Resource

* Expense line: orphan expense line are now also digitally signed if there is a justification file
* Employee: Added demo Data for HR employee
* Timesheet line: Fixed timesheet line deletion when it is linked to timer.

#### Production

* Production order: fixed production order sequence generated from product form not using the correct sequence in a multi company configuration.

#### Project

* Sale order: Fixed project generated with empty code which could trigger a exception

#### Sale

* Sale order template: fixed NPE when company is empty.
* Sale order template: fixed NPE when currency or partner is empty.

#### Stock

* Sales dashboard: fixed stock location for customer deliveries.

#### Supply Chain

* Invoice: removed time table link when we merge or delete invoices, fixing an issue preventing invoice merge.
* Translation: fixed alert message related to partner language always showing due to localization.


### Developer

#### Base

Added the possibility to extend `base.printing.template.type.select` easily from other modules so any module can add new classes used to generate PDF from AOS models.

#### Account

To fix existing data if you reversed a move related to an invoice, you can run the following script:

```sql
UPDATE account_invoice_term AS it 
SET amount_remaining = 0, company_amount_remaining = 0, is_paid = true
FROM account_move_line ml JOIN account_move m ON m.id = ml.move
WHERE ml.id = it.move_line AND ml.amount_remaining = 0 AND m.invoice IS NULL;
```

#### Project

If you have the issue on project generation from sale order, the fix requires to run the following sql request in order to fully work: `UPDATE project_project SET code = id where code IS NULL;`

## [8.1.1] (2024-06-27)

### Fixes
#### Base

* App base: removed admin module reference from modules field.
* Partner: fixed number of decimals displayed on debit balance.
* Template: Fixed translation for content panel title.
* BIRT Template: fixed default BIRT parameter templates so they can be used in mail templates.
* Partner: fixed merging duplicates not working correctly.
* BIRT Template: fixed wrong report used when getting report from source.

#### Account

* Move: fixed error popup changing date on unsaved move.
* Move: fixed blocking message due to a wrong tax check preventing some accounting moves validation.
* Invoice payment: fixed wrong scale for prices in form view.
* Mass entry: fixed error when a user without an active company is selecting a company.
* Payment voucher: fixed technical error when the user's active company is null and there are more than one company in the app.
* Invoice/MoveLine: fixed financial discount amount and currency amount scaling.
* Mass entry: fixed today date settings in dev mode not working with created lines from mass entry form.
* Tax: fixed display of non deductible tax fields on account management form.
* Account config: improve the interface to prevent customer blocking if 'Late payment account blocking' configuration is disabled.
* Move/Analytic: fixed negative analytic amounts when generating cut off moves.
* Invoice: fixed foreign currency invoice duplication when current currency rate is different from the currency rate in the original invoice.
* Invoice: fixed an issue where due date was not updated correctly when saving an invoice.

#### Bank Payment

* Bank statement line: prevent user from creating a new bank statement line manually.
* Bank reconciliation: fixed move line filter when using multiple reconciliations.

#### Budget

* Budget: fixed an issue where the default budget structure was not fetched correctly on budget creation.

#### Contract

* Contract: fixed an issue where the value of yearly ex tax total was not revalued.
* Contract: fixed missing Contract type select in Contract template english demo data.
* Contract: fixed a bug where it was impossible to activate a contract when automatic invoicing is enabled.
* Contract: fixed tax error message related to supplier contracts.

#### Human Resource

* Payroll Preparation: fixed an issue were leaves were not always displayed.
* Increment leave batch: fixed an issue where the batch did not update some employees.

#### Purchase

* Purchase order: Fixed purchase order lines not displayed on 'Historical' menu.

#### Supply Chain

* Fixed forwarder partner domain filter.

#### Intervention

* Sequence: fixed sequence import in intervention demo data.


### Developer

#### Base

Replaced `action-partner-record-set-positive-balance` by `action-partner-method-set-positive-balance` in Partner form.

## [8.1.0] (2024-06-14)

### Features
#### Base

* Updated Axelor Open Platform to 7.1. You can find all information on this release [here](https://github.com/axelor/axelor-open-platform/blob/7.1/CHANGELOG.md).
* Partner: add a generic system to manage other type of company registration than SIRET.
* Product: configuration about perishability and warranty are moved to the tracking number.
* Print template: a new table "Print template" was added. This adds possibilities when printing any model from processes or from the interface, like adding a PDF page at the end of the PDF generated by BIRT, or adding a "Print" button on the interface simply by activating a template.
* Address template: for address specific to a country, it is now possible to configure exactly how the address should be written.
* Email template: manage multiple localizations in email template.
* Migration: add new tool to check if meta fields and meta models are still present after a migration.
* Migration: add new tool to find permissions that are on removed/renamed models.
* Quick menus: implement quick menus to manage active company change, and get technical information for dev instances.

#### Intervention

* Equipement management.

Add support to generate equipment manually or generated with templates. Each equipment can be one or more products. Add equipment family that is used to manage intervention and for a tree visualization.

* Interventions management.

Add a generic intervention logic, that can be used for multiple kind of services: technical maintenance, after-sales services, hardware repair, ...

They can be generated from a customer request or contracts. Each intervention can generate a configurable questionnaire: for example technical check on a machine, measurements, storing a signature, ...

#### Human Resource

* Bank card: add a new table for managing employee bank card.
* Expense: Manage expense for external employees (freelance).

#### Supply Chain

* Sale order: allow to mass invoice sale order lines from different sale orders.

#### Account

* Manage multi-taxes: link to tax are replaced by a link to multiple taxes. Invoice, orders as well as fiscal position and other configurations are modified.
* Manage non deductible taxes.
* Invoice: manage refund on advance payment invoices.
* Invoice: manage vat on advance payment invoices payment.
* Bill of exchange: add support of bill of exchange on payment session.
* Bill of exchange: allow to cancel a bill of exchange from the invoice.
* Custom accounting report: manage multi company.

#### Budget

* Add export to budget. An exported budget can be imported using advanced import feature.

#### Production

* SOP: new batch to compute real values.

#### Contract

* Addition of framework contracts and allows sale order from them.
* Addition of a new type of contract to manage year end bonus.
* File management on contracts has been reworked.

#### Project

* Management of progress between parent and children tasks.
* Automatic creation of events in calendar for planned times.

#### Business project

* Site: manage sites for business projects.
* Added holdbacks management on business projects.
* Implemented progress billing based on tasks from a project.
* Manage analytic accounting on projects.
* Manage multiple expenses invoicing from a business project, to invoice to the customer expenses made by employees.

### Changes

#### Base

* Pricing scale: add a type to manage generic pricing scale with specific pricing scale.
* Events planning: add a new tool to generate planning lines on a given period.
* Exchange rate: improve exchange rate computation to minimize rounding errors.

#### Account

* Partner: manage RUM per company
* Accounting move template: add functional origin.
* Fixed assets: allow to "import" manually a fixed asset through a new wizard.
* Accounting batch: when opening/closing accounts, add the possibility to choose the status of generated moves.
* Accounting batch: when closing a period, add a config to delete simulated moves.

#### Bank Payment

* Improve bank reconciliation view.
* Add a new bank statement format import (csv).

#### Human Resource

* Timesheet: get the timesheet product from the project task.
* Expense: improve expense declaration.

#### Stock

* Inventory: add the possibility to init product WAP for a product not already available in stock.
* Tracking number: manage origin of tracking numbers.
* Tracking number: add dimensions on tracking numbers.

#### Production

* MRP: rework manufacturing order proposal date computation to correctly manage prod process line with the same priority.
* Add a new configuration per company "Residual stock location".
* Prod process import: manage files.
* Outsourcing: manage outsourcing with planification.

#### Contract

* Manage title lines on contracts.
* Manage price list on contracts.
* Improve UI/UX on contract form view.

#### Project

* Manage default status from project configuration.

#### Business project

* Reworked sale order invoicing from a business project.

#### Support

Partner: add a panel in the form view to show tickets related to the partner.


### Fixes

#### Base

* Product: modified volume computation.
* Make sure that `__locale` parameter is used instead of `Locale` in birt report so the localization is always correctly applied in printings.

#### Human Resource

* Timesheet: Reworked automatic timesheet line addition when the employee is on leave.

#### Production

* Bill of materials: fixed namecolumn management in bill of materials so the user can write a name instead of having only a generated one.

[8.1.11]: https://github.com/axelor/axelor-open-suite/compare/v8.1.10...v8.1.11
[8.1.10]: https://github.com/axelor/axelor-open-suite/compare/v8.1.9...v8.1.10
[8.1.9]: https://github.com/axelor/axelor-open-suite/compare/v8.1.8...v8.1.9
[8.1.8]: https://github.com/axelor/axelor-open-suite/compare/v8.1.7...v8.1.8
[8.1.7]: https://github.com/axelor/axelor-open-suite/compare/v8.1.6...v8.1.7
[8.1.6]: https://github.com/axelor/axelor-open-suite/compare/v8.1.5...v8.1.6
[8.1.5]: https://github.com/axelor/axelor-open-suite/compare/v8.1.4...v8.1.5
[8.1.4]: https://github.com/axelor/axelor-open-suite/compare/v8.1.3...v8.1.4
[8.1.3]: https://github.com/axelor/axelor-open-suite/compare/v8.1.2...v8.1.3
[8.1.2]: https://github.com/axelor/axelor-open-suite/compare/v8.1.1...v8.1.2
[8.1.1]: https://github.com/axelor/axelor-open-suite/compare/v8.1.0...v8.1.1
[8.1.0]: https://github.com/axelor/axelor-open-suite/compare/v8.0.8...v8.1.0
