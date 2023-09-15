## [7.1.4] (2023-09-11)

#### Features

* API: improve response when creating object

#### Fixed

* App builder: update axelor-studio dependency from 1.2.1 to 1.2.3.
* API Stock: fixed an issue when creating a stock move line where the quantity was not flagged as modified manually.
* Account: added check for depreciation account in fixed asset category on fixed asset disposal.
* Purchase order: fixed 'NullPointerException' error happening when a line without a product was created.
* Fixed asset: fixed never ending depreciation lines generation if there is a gap of more than one year.
* Reconcile: fixed payment adjustment when the last invoice term paid is not the one with the latest date.
* Product: on product creation, fills the currency with the currency from the company of the logged user.
* Invoice: fixed wrong translations on ventilate error.
* Manufacturing API: improved behaviour on operation order status change.
* HR Dashboard: fixed expense dashboards.
* Account: fixed an issue preventing the user from deleting accounts that have compatible accounts.
* Move template: fixed filters on accounting accounts in move template lines.
* Contract line: added a warning when the user selects a product with a time unit.
* Operation order: fixed workflow issues

Resuming an operation order in a grid or a form has now the same process and will not affect other orders.
When pausing an operation order, if every other order is also in stand by, the manuf order will also be paused.

* Invoice line: set analytic accounting panel to readonly.
* Account chart: updated accounting config files to improve accounting import.
* Payment session: fixed the account at payment on payment session using compensation invoice/moves.
* Project: fixed error when opening the default dashboard view.
* Contract: hide revaluation related panel and fields when periodic invoicing is disabled.
* Sale/Purchase order and stock move: fixed wrong filters when selecting stock locations, the filters did not correctly followed the stock location configuration.
* Stock move: fixed 'NullPointerException' error when emptying product on stock move line.
* Payment session: fixed generated payment move lines on a partner balance account not having an invoice term.
* Manufacturing order: fixed an issue where some planning processes were not executed.
* Move template: fixed copied move template being valid.
* Invoice term: fixed wrong amount in invoice term generation.
* Journal: balance tag is now correctly computed.
* Contract: fixed 'NullPointerException' error when invoicing a contract.
* Manufacturing order: when generating a multi level manufacturing order, correctly fills the partner.

## [7.1.3] (2023-08-24)

#### Fixed

* Webapp: update Axelor Open Platform version to 6.1.5.
* Move: fixed reverse process to fill bank reconciled amount only if 'Hide move lines in bank reconciliation' is ticked and if the account type is 'cash'

To fix existing records, the following script must be executed:

```sql
UPDATE account_move_line moveLine
SET bank_reconciled_amount = 0
FROM account_account account
JOIN account_account_type accountType
ON account.account_type = accountType.id
WHERE moveLine.account = account.id
AND accountType.technical_type_select <> 'cash';
```

* SOP/PMS: Gap is now really a percentage

Changed computation of "Gap" fields so the result will be percentage (e.g. 13% instead of 0.13). The following script can be used to fix existing SOP/PMS:

```sql
UPDATE production_sop_line SET sop_manuf_gap = sop_manuf_gap*100;
UPDATE production_sop_line SET sop_sales_gap = sop_sales_gap*100;
UPDATE production_sop_line SET sop_stock_gap = sop_stock_gap*100; 
```

* Payment voucher: fixed remaining amount not being recomputed on reset.
* Payment voucher: fixed being able to pay PFP refused invoice terms.
* Move: fixed tax check error on daybook when taxes are compensated.
* Contract: fixed NullPointerException when invoicing a contract.
* Sale order: fixed a bug where the project was not filled in lines in sale order generated from a project.
* Stock details: in stock details by product, fixed production indicators visibility.
* Business project batch: fixed "id to load is required for loading" error when generating invoicing projects.
* Move/Invoice: added a verification of the sum of the percentages by analytic axis when we account a move.
* Period: fixed adjusting button not being displayed when adjusting the year.
* Fixed asset: fixed wrong depreciation value for degressive method.
* Move / Mass entry: fixed error when emptying the date on a move generated from mass entry.
* Payment session: filter invoice terms from accounted or daybook moves.
* Employee: fixed employees having their status set to 'active' while their hire and leave date were empty.
* Bank reconciliation: merge same bank statement lines to fix wrong ongoing balance due to auto reconcile.
* Invoice / Payment voucher: fixed generated adjusting moves with multi currency.
* Accounting report: dates on invoice reports are now required to prevent an error generating an empty report.
* Invoice: fixed anomaly causing payment not being generated

Correct anomaly causing payment not being generated on invoice when a new reconciliation is validated  
and the invoice's move has been reconciled with a shift to another account (LCR excluding Doubtful process).

* Import Batch: added missing translation.
* Move: The date is now correctly required in the form view.
* Payment voucher / Invoice payment: fixed generated payment move payment condition.
* Payment voucher: fixed excess payment.
* Accounting report: fixed payment differences report generation.
* Invoice payment: add missing translation for field "total amount with financial discount".
* Invoice: fixed financial discount deadline date computation.
* Sale order line: fixed a bug where the link to the project was not emptied on copy.
* Mass entry: fixed date update on move lines with the same move identification.
* Stock move printing: fixed an issue where lines with different prices were wrongly grouped.
* Stock details: fixed "see stock details" button in product and sale order line form views.
* Accounting batch: corrected cut off accounting batch preview record field title cut in half.
* Invoice line: fixed error when emptying the tax line.
* Invoice: fixed invoice term due date not being set before saving.
* Reconcile: fixed inconsistencies when copying reconciles.
* Tax number: translated and added an helper for 'Include in DEB' configuration.
* Leave request: fixed employee filter

A HR manager can now create a leave request for every employee.
Else, if an employee is not a manager, he can only create leave request for himself or for employees he is responsible of.

* Accounting config: fixed default bank details errors message.
* Invoice term: fixed negative amount on manual creation.
* CRM search: hide some panels to avoid NPE error.
* Mass entry: fixed error when we control duplicate origins in a journal where the origin is not required.
* Stock API: validating a Stock Correction with a real quantity equal to the database quantity now correctly throws an error.
* Manufacturing order: fixed a bug where sale order set was emptied on change of client partner and any change on saleOrderSet filled the partner.
* Forecast line: fixed company bank details filter.
* Contact: the filter on mainPartner now allows to select a company partner only, not an individual.
* Move: fixed due date issue when changing from single to multi invoice term payment condition.
* Stock move line: improve performance when selecting a tracking number if there are a large number of stock location lines.

## [7.1.2] (2023-08-08)

#### Fixed

* Invoice : fix the way we check the awaiting payment
* Accounting batch : Improve user feedback on move consistency control when there are no anomalies
* PLANNING: Planning is now correctly filtered on employee and machine form
* SaleOrderLine: Description is now copied only if the configuration allows it
* PARTNER: Fixed a bug where button 'create sale quotation' was always displayed
* Custom accounting report : Excel sheets are now named after the analytic account
* Move : Fix automatic move line tax generation with reverse charge and multiple vat systems.
* Stock move: reversed stock move now correctly reverses stock locations
* Mass entry : Fix fill of payment condition when journal isn't type 'other'
* Move : Fix invoice terms amount at percentage change with negative currency amount
* PurchaseOrder and Invoice: Added widget boolean switch for interco field 
* Employee dashboard: added condition on app
* Invoice : Fix tax being empty on invoice line when it's required on account
* ManufOrder: Planning a cancelled MO now clears the real dates on operations orders and MO
* Move : Fix display of analytic axis accounts when we change it on analytic move lines
* Product/ProductFamily : set analytic distribution template readonly if the config analytic type is not by product
* Move : Fix currency amount of automatically generated reverse charge move line not being computed
* Invoice: Fixed french translations
* INVOICE : fix form view - added blank spaces before the company field and move the originDate field
* SOP/MPS: Fixed a bug where real datas were never increased
* Supplychain batch : Fixed bugs that prevented display of processed stock moves
* SOP/MPS: Fixed SOP/MPS Wrong message on forecast generation
* MOVE/INVOICE/ORDERS : Compute budget distribution when object are not saved
* PAYMENT SESSION : corrected accounting trigger from payment mode overriding accounting trigger from payment session on bank order generated from payment session.
* Move : Fix tax computation when we have two financial accounts with the same VAT system
* Debt Recovery: Fix error message on debt recovery batch to display correctly trading name
* Move : Fixed a bug that was opening a move in edit mode instead of read only
* Period : Fixed an issue where a false warning was displayed preventing the user for re-opening a closed period
* Invoice: Fixed a bug where subscription invoice was linked to unrelated advance payment invoice

When creating an invoice auto complete advance payement invoice with no internal reference to an already existing sale order.

* Move/MoveLine : empty taxLine when changing the account of a moveLine to an account without tax authorized
* Invoice : Remove payment voucher access on an advance payment invoice
* Payment session : Fix session total amount computation

The following script can be executed to clean old actions
```sql
DELETE FROM meta_action
WHERE name IN (
  'action-invoice-term-method-compute-total-payment-session',
  'action-payment-session-record-set-session-total-amount-btn',
  'action-method-payment-session-compute-total'
);
```

* Move : Fix invoice term amount at percentage change with unsaved move
* Project: fix real costs computing in tasks and subtasks
* Accounting report: Fixed a bug where unreconcilied move lines were not displayed
* Product: When changing costTypeSelect to 'last purchase price', the cost price will now be correctly converted.
* Move : Set readonly move form when period is closed or doesn't exist
* Bank order: Fixed a bug where bank order date was always overridden. Now bank order date is overridden only when it is before the current date and the user is warned.
* PaymentSession : Fix compensation invoice terms amount when select using buttons
* BUSINESS PROJECT BATCH: Fixed invoicing project batch

## [7.1.1] (2023-07-20)

#### Fixed

* App builder: updated axelor-studio dependency from 1.2.0 to 1.2.1.
* Account/Move/Invoice: fixed analytic check when required on move line, invoice line.
* Manufacturing order: fixed JNPE error when merging manufacturing orders missing units.
* Cost sheet: fixed wrong bill of materials used for cost calculation.
* Move: set description required on move line when it is enabled in company account configuration.
* Stock move line: fixed display issues with the button used to generate tracking numbers in stock move lines.
* Custom accounting report: added a tab listing anomalies that are preventing generation.
* Operation order: correctly filter work center field on work center group (when the feature is activated).
* Payment condition: improved warning message when modifying an existing payment condition.
* Stock move: fixed issue preventing the display of invoicing button.
* Supplychain batch: fixed an error occurring when invoicing outgoing stock moves.
* Invoice: fixed unwanted financial discount on advance payment invoice.
* Bank payment printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Accounting report: fixed error on N4DS export when the partner address has no city filled.
* Payment session/Bank order: fix of compensation in payment session process with bank order.
* Sale order: fixed missing actions on loading sale order form views.
* Fixed asset: improved the error message shown when an exception occurs during a mass validation.
* Analytic distribution template: fixed error when creating a new analytic distribution template.
* Product: fixed wrong filter on analytic on product accounting panel.
* Supplychain batch: fixed an error occurring when invoicing outgoing stock moves without sale order.
* Faker API: update documentation in help message.
* Interco: fixed generated sale order/purchase order missing a fiscal position.

## [7.1.0] (2023-07-12)

### Features/Changes

#### Base

* Faker API Field: 
- Add the possibility to use the api faker method parameters
- Add data type to filter API fields in the anonymizer lines

* Base Batch: New batch to force users password change
* BirtTemplate: It is now possible to choose the .rptdesign file used to generate the attached file when sending a generated email. Parameters sent to the BIRT engine can also now be computed using groovy scripting.

#### Accounting

* Move: 
    - New mass entry feature
    - Add management of currency amount negative value for credit
    - Add PFP process like on Invoices
    - Extend automatic cut off dates process to move confirmation
    - On move creation or generation, originDate is filled with onChange of moveDate.
    - Add an allowed tax gap on company account config and check consistency of tax values on daybooking, simulating or accounting.
    - Add new technical origin for move generated from mass entry process

* Move, Move line: 

Cut off dates are no longer required for functional origin: closure, opening, cut off

* Cut off batches: Add three new columns displaying the number of days considered in the calculation for Cut-Off processing of Advance Accrued Expenses (CCA) and Advance Accrued Revenues (PCA), allowing users to verify the consistency of the days used in the calculation.
* Move, Move Template: Set partner as required when journal is sale or expense
* Reconcile: Add an effective date to the reconcile which will be used instead of reconciliation date for generated payment and moves. This effective date is computed from information in reconciled moves.
* Reconcile: reconciliation of different accounts and payment differences management
    - Added new fields in account config, one account for debit difference, one account for credit difference.
    - When you pay an invoice term and the remaining amount is less than the threshold distance, a new payment will be made of the remaining amount.
    - Reconciliation of different accounts are now managed, using a new payment move which reconciles both invoice terms.

* Custom accounting report: 
    - Fetch parent account when higher level analytic accounts are detailed
    - Add previous start balance
    - Can now filter result with accounts and analytic accounts from the report
    - Allow to display debit or credit values instead of only the difference between them

* Debt recovery: improve debt recovery and debt recovery history attachments management

* Deposit slip: handle bank details and deposit date in deposit slip.

* Payment session: 
    - Show buttons to select all / unselect all.
    - add compensation field on partner to compensate customer invoices on supplier payment or supplier invoices on customer payment.
    - add partner set field to filter partners linked to these invoice terms.

* Analytic distribution template: trading name now has an analytic distribution template field:

In Account configuration, if the Analytic distribution type is 'Per trading name', 
Analytic distribution template will be retrieved from the trading name when creating Purchase order, Sale order and invoice lines.

#### Budget

* Budget: integrate new budget module

#### Stock

* Stock location: Stock location can be now configured from the stock move line
* Stock Move: Canceling a stock move will now set the wap price to its former value if nothing happened since the realization.
* Stock moves: Change origin architecture from refSelect to M2O

#### Supply Chain
* Sale order: Sale orders and sale order lines can now be filtered with invoicing state.


#### Production

* Manufacturing order: 

When planning a manufacturing order, operation orders dates now take into account available dates of the machine. 

* Operation order: 

New fields to distinguish human and machine planned times on operations.
In the calendar view, the operation orders will now be updated (if needed) on change of another operation order.

* Work Center & Prod process Line : 

Moved starting, setup and ending duration from Machine to Work Center.
Human resources panel refactoring

* Machine : 

Machine form view refactoring

#### CRM

* CRM: Redesign of the CRM menus architecture and multiple menus domains.
* CRM Search: Added a powerful searching tool in multiple object type (opportunities, leads, event, etc …). Using keywords and configurable mapping. 
* Opportunities: 
Added a control on opportunities winning process that displays on-going opportunities for selected clients. With multiple closing functions. 
Add controls on decimal and integer values in opportunities for process maintainability.
* Partner, Leads, Opportunities: 
Refactor on last event and next event on partner, leads, opportunities (to make it work properly). 
Added back a drag and drop function on lead/opportunities/prospect status grid view.
* Lead conversion: Added a new conversion process for leads.
* Lead, Prospect: Added a Kanban view.
* Improve emails display in all CRM views.
* Added an automatic conversion function for similar leads (name, email, etc…) when converting one. 
* Added a catalog email sending function in catalog grid view. 
* Opportunity: added a panel in opportunity view displaying events related to the opportunity.
* Partner: added partner status to the app. This feature permits the user to follow a prospect using a custom process. It follows the leads process before converting the prospect to customer. 
* Partner: added related contacts in contact view (contacts with the same email address domain).


#### Project

* Project: Allowed deletion of planned times from the dashlet in Project.
* Project: Added Time follow-up and Financial follow-up on Tasks, aggregated on Project level.
* ProjectTask: Changed planning UX and constraints to fit with the new flow of project management.
* Project: Added a batch to compute project and task’s time and financial reporting. Action can also be triggered from Project’s Tools menu. Invoicing Project Batches were renamed to Business Project Batches
* Project: Removed obsolete time fields on Project and Tasks.
* Project: Added demo data for Business Project (Task by line & Task by line using subtasks models) to allow demonstration of the new reporting.
* Project: Added a batch to historize project reporting, as well as visual indicators to compare current values to last historized values.
* Business Project: Improved App configuration UX
* Project: Manage project time units (Hours, Days) and conversions to allow to manage a project in Hours or in Days, as well as to manage individual tasks in both units. Planning has also been changed to reflect these features.
* Contract: invoicing pro rata per line
* Contract: reevaluating contract yearly prices based on index values
* Contract: added discount on contract lines.

#### Sales

* Sale Order: Improve UX and help for project generation.
* Sale Order: Generating task with subtask models has been fixed and improved

#### Helpdesk

* Helpdesk: 

Added a new field fullName to the ticket which will be used as namecolumn
Add API to update ticket status

#### GDPR

* Processing register: option to not archive

In GDPR Processing Register, add a boolean to enable/disable archiving data during the data processing.

* App GDPR: New config to exclude fields from the access request.
* Erasure Request: Add the possibility to break links in an anonymised object.

#### HR

* New employee and manager dashboard with custom views
* Reorganize HR menu items and add new HR menu with a new dashboard
* Files :

Panel review
Linked to skills

* Lunch voucher: Process improvement (links with expenses, refacto, form review)
* Employee: 

New titles
Dependent children

* Recruitment: Application form review
* Expense:

Multi-currency expenses
Analytic distribution template on employee form
Add justication to expense line grid view

#### Cash management

* Forecast generator: add fortnightly and weekly periodicity in forecast generator.

#### Mobile

* Mobile Settings: add Helpdesk config for the mobile app

### Fixed

#### Sale/Purchase

* Sale and Purchase order: Change default view for historic menu, from card view to grid view

#### Stock

* Stock move: the field “filter on available products” is now stored in database.

#### Project

* Project and Task: Split ProjectStatus between ProjectStatus for project and TaskStatus for task.

#### Base/HR

* User, Employee: Improved behavior to avoid links errors and inconsistencies and for more coherence.
* Partner: change companyStr length
* Company: Add all java timezone in timezone selection in Company.
* Training skill: graduation date and end of validity date are not required anymore.

#### Helpdesk

* Ticket: Renamed the customer field into customerPartner

#### Accounting

* Accounting report analytic config line : Rule level maximum value is now the highest existing analytic level.
* Custom accounting report: fixed comparison period order.
* Move: origin date is now always filled in automatic creation.
* Invoice term: rework partial pfp validation in invoice terms to be more user friendly and fix financial discount in second invoice term generation.
* Journal type: field technicalTypeSelect is now compulsory.

### Removed

* App base configuration: remove OSM Routing API selection

Now, when computing distance for Kilometric Allowances, if using the Open Street Map mapping service provider, 
it will use the OSRM API by default.

* Leave management: remove unused 'user' column in data model.
* Alarms: removed everything related to alarms.
* CRM : Leads : Remove 'Import Leads' feature.
* [EXPENSE] Remove multi user expense.
* Object data config: removed unused domain object data config.
* WAP History: removed WAP history  (replaced by a new table “StockLocationLineHistory” with more information.
* Skills : experienceSkillSet and skillSet are now removed from employee form.
* Simplified moves: removed in favor of mass entry.


[7.1.4]: https://github.com/axelor/axelor-open-suite/compare/v7.1.3...v7.1.4
[7.1.3]: https://github.com/axelor/axelor-open-suite/compare/v7.1.2...v7.1.3
[7.1.2]: https://github.com/axelor/axelor-open-suite/compare/v7.1.1...v7.1.2
[7.1.1]: https://github.com/axelor/axelor-open-suite/compare/v7.1.0...v7.1.1
[7.1.0]: https://github.com/axelor/axelor-open-suite/compare/v7.0.5...v7.1.0
