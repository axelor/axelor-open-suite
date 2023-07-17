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


[7.1.0]: https://github.com/axelor/axelor-open-suite/compare/v7.0.5...v7.1.0
