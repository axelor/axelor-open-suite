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

[8.1.4]: https://github.com/axelor/axelor-open-suite/compare/v8.1.3...v8.1.4
[8.1.3]: https://github.com/axelor/axelor-open-suite/compare/v8.1.2...v8.1.3
[8.1.2]: https://github.com/axelor/axelor-open-suite/compare/v8.1.1...v8.1.2
[8.1.1]: https://github.com/axelor/axelor-open-suite/compare/v8.1.0...v8.1.1
[8.1.0]: https://github.com/axelor/axelor-open-suite/compare/v8.0.8...v8.1.0
