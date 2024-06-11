## [8.0.8] (2024-06-07)

### Fixes
#### Base

* Updated Axelor Open Platform to 7.0.8.
* Update axelor-studio dependency to 2.0.3.

#### Account

* Move line: set the vat system editable for tax account.
* Account: fixed technical type select for journal type demo data.
* Move line/Reconcile/Tax: fixed error when you forbid tax line on tax account.
* Move line: fixed impossible to create a move line when partner have financial discount
* Move: fixed automatic tax generation on a move with already existing tax lines.
* Move: fixed a technical error when adding a first line without invoice terms in sale or purchase move.
* Account: fixed menu roles data init.
* Invoice payment: fixed reconcile differences process for supplier invoices.
* Analytic distribution template: removed analytic percentage total verification when we add a new analytic distribution line.
* PaymentVoucher: fixed payment on supplier invoices
* Invoice/AutoReconcile: removed tax moveline reconciliation in move excess or move due at ventilation.
* Payment session: fixed issue when validating the payment session causing amount being updated and not corresponding to the value of the amount being validated and equal to the bank order generated.
* Move line: select default account from partner configuration only for first line of the move instead of all lines.
* Move: date of analytic move lines will be updated on change of move's date.
* Accounting report: fixed opening moves are not displayed on aged balance report.
* Accounting report: fixed error linked to field's name change in report 'Summary of gross values and depreciation'.

#### Bank Payment

* Invoice payment: fixed payment remaining 'Pending' while bank order has been realized (while no accounting entry generated).

#### Budget

* Global budget: fixed missing english translation for archiveBtn.

#### CRM

* Partner: fixed display condition for customer recovery button.

#### Human Resource

* Expense: fixed an issue that prevented to ventilate a expense.
* Timesheet API: fixed requests permissions
* Expense API: improved requests permissions.
* Extra hours: fixed an issue where lines were filled with the connected employee instead of the employee filled in the form view.
* Payroll preparation: do not block a period after exporting a payroll preparation.
* Payroll preparation: correctly empty lists on payroll preparation view when employee is changed.
* Timesheet: fixed an issue preventing a manager user to create timesheets for subordinates.

#### Marketing

* Campaign: fixed error preventing from sending reminder emails to leads when the list of partners is empty.

#### Production

* Purchase order: fixed issue on purchase order merge due to old references on ManufOrder

#### Project

* Project and tasks: fixed autocompletion not working for fields linking to tasks and task templates.

#### Sale

* Sale order line: filled project in sale order line while using editable grid of sale order.

#### Stock

* Stock location: fixed NPE while printing report from grid view without selecting any location.


### Developer

#### Account

On MoveLineCurrencyService we update computeFinancialDiscount method signature.

  - From `void computeFinancialDiscount(MoveLine moveLine)`
  - To `void computeFinancialDiscount(MoveLine moveLine, Move move)`

---

The constructor for MoveLineControlServiceImpl has been updated to include the additional parameter 'MoveLineGroupService moveLineGroupService'.

#### Human Resource

Created a new service `MoveLineTaxHRServiceImpl` that extends `MoveLineTaxServiceImpl` and override the method `isMoveLineTaxAccountRequired(MoveLine moveLine, int functionalOriginSelect)`

---

If you use the AOS API for Expense, please check that they still work with your current permission, as we fixed permission check to be more logic.
For example, the check is done on expense line instead of expense for updating expense line. Also the API now correctly checks the permission with the record, meaning that conditional permission will now correctly be applied.

## [8.0.7] (2024-05-24)

### Fixes
#### Base

* Update axelor-studio dependency to 2.0.2.
* ICalendar: fixed synchronization duration widget.
* Sale order/Purchase order/Invoice: fixed wrong column name displayed on discounted amount.
* Price list: fixed a NPE when opening a new price list.
* Birt Template: for developers, added hot reload for .rptdesign files.

#### Account

* Invoice: fixed an issue where it was possible to change the printing format on a ventilated invoice.
* Accounting report: update font size and improve lettering display on partner general ledger.
* Reconciliation: added more traceability when a move line is not reconcilable.
* Accounting Situation: fixed wrong scale on 'Used credit' and 'Accepted credit' fields.
* Payment voucher: fixed an issue where some fields were not displayed in due element list.
* Invoice: fixed a bug where generated invoices from orders had wrong WT/ATI configuration.
* FEC Import: fixed partner not filled when the partner is only on the first line of an entry.
* Move line: fixed move fiscal position not being used for tax equivalence on account change.

#### Bank Payment

* Bank reconciliation: fixed wrong computation of balances when having more than 10 records.
* Bank reconciliation: fixed move line filter and controls when reconciliating.
* Accounting report type: added bank reconciliation statement report record in accounting report type list.
* Accounting report: fixed bank reconciliation accounting report displaying already reconciled move lines.
* Bank statement line: fixed wrong balance on the demo data sample statement.

#### Business Project

* Timesheet line: fixed an issue preventing to invoice timesheet when the task has an activity.
* Project: fixed validation error missing timeUnit when creating a planned time planning while business-project module is not installed.
* Timesheet line: fixed NPE when generating a project invoice with a timesheet line that has a non null duration for customer.

#### Contract

* Contract: fixed display condition of the Revaluation fields.
* Contract: fixed 'nouvelle version' used as key instead of 'new version'.
* Contract: fixed 'ID to load' error when we modifying supposed activation date without saving.
* Contract: deleted version history on duplication.

#### Helpdesk

* Ticket: fixed demo data status.

#### Human Resource

* Expense line: fixed an error when computing distance on orphan kilometric expense line.
* TSTimer: fixed french translation of errors message when creating timer from API.
* Timesheet API: added more checks on date to prevent inconsistency issues.
* Leave request: fixed issue where a leave request was not updated after sending it.

#### Maintenance

* Maintenance request: fixed impossible to create a maintenance request from the quick adding field.

#### Mobile Settings

* Mobile dashboard: set name required in view for dashboard configuration lines.

#### Production

* Bill of materials: fixed blank printing.
* Manufacturing order: fixed error preventing quantity change on consumed products.
* Configurator: improved demo data for configurator 'Ordinateur personnalisé'.
* Manufacturing Order: fixed NPE error on selecting a product in Consumed products.

#### Project

* Project: fixed an issue where a task was not displayed when using the sub-task button.

#### Purchase

* Purchase order line: fixed an issue causing an error when the line was marked as `toInvoice`.
* Purchase request: added sequence for purchase request in demo data.

#### Sale

* Sale order merge: fixed an issue where it was not possible to select a price list in case of conflicts.
* Sale order: fixed sale order line sequence issue.
* Sale order: fixed scales on amount in sale order line grid.

#### Stock

* Stock move line: fixed issue when changing the line type to title.

#### Supply Chain

* Customer invoice line: fixed default product unit on product change.
* Invoice: fixed 'FixedAsset' boolean never propagated to invoice line from purchase order line.
* Purchase order: when a purchase order is generated from a sale order, when the catalog does not have a code or name, it will use the product.

## [8.0.6] (2024-05-03)

### Fixes
#### Base

* Pricing: the formula panel is now collapsed by default.
* Sequence: fixed NPE while checking yearly reset or monthly reset on unsaved sequence.
* Advanced export: Fixed export which wasn't working when maximum export limit exceeded.

#### Account

* Invoice: fixed default financial discount from partner not being computed after invoice copy.
* Accounting export: Fixed error when replaying the accounting export.
* Invoice payment: fixed financial discount management when there is no payment date.

#### Bank Payment

* Bank reconciliation: fixed tax move line generation when origin is payment.

#### Contract

* Contract: fixed prorata invoicing when invoicing a full period.
* Contract: correctly disable related configuration when disabling invoice management.
* Contract: Fixed error when using contract template with contract line to generate a contract.

#### Production

* Manufacturing order: fixed conversion of missing quantity of consumed products.
* Machine: fixed machine planning type when creating it from machine.

#### Purchase

* Purchase order line: Fixed maximum and minimum order quantity error when partner doesn't have supplier catalog lines.
* Purchase order report: fixed sequence order issue when title lines are present.

#### Quality

* ControlEntry/ControlPlan: fixed issues when dupplicating a control entry or control plan.

#### Sale

* Sale order: fixed refresh issue when saving a sale order.
* Configurator : Fixed issues with the new configurator model in demo data.

#### Stock

* Tracking number: fixed available qty not displayed on grid view.
* Stock move: fixed printing settings when creating stock move from sale order.
* Stock configuration: fixed typo in french translation, 'incoterme' => 'incoterm'


### Developer

#### Production

Changed signature of `ProdProductProductionRepository.computeMissingQty(Long productId, BigDecimal qty, Long toProduceManufOrderId)`
to `ProdProductProductionRepository.computeMissingQty(Long productId, BigDecimal qty, Long toProduceManufOrderId, Unit targetUnit)`

#### Quality

Created `ControlEntryManagementRepository` extending `ControlEntryRepository`, `ControlPlanManagementRepository` extending `ControlPlanRepository`
and `ControlEntryPlanLineManagementRepository` extending `ControlEntryPlanLineManagementRepository`

## [8.0.5] (2024-04-19)

### Fixes
#### Base

* (Webapp) Updated Axelor Open Platform dependency to 7.0.7.
* Birt template: fixed duplicated printouts generated when attach boolean is set to true.

#### Account

* Payment voucher: fixed required message at on new and fixed invoice refresh at confirm.
* Accounting report: fixed 'Fees declaration supporting file' report displaying records that should not appear.
* Invoice: improved UX to prevent the use of financial discount on advance payment invoice.
* Financial Discount: fixed french translations for 'discount'.
* Invoice payment: fixed anomalies when doing multi currency payments.
* Invoice: fixed invoice term amount remaining on invoice payment move.

#### Budget

* Purchase order/Sale order/Stock move/Invoice: link order budget to invoice after stock move generation.
* Global budget: fixed structure validation when the global budget have no budget.
* Budget: fixed technical error when creating budget on a saved budget level.

#### Human Resource

* Expense: fixed expense accounting moves generation when expense line dates are different and tax amount is zero.

#### Sale

* Sale order: removed french translation from english file.

#### Supply Chain

* Mass stock move invoicing: fixed an issue where invoiced partners were not used to invoice stock moves, the other partner was used instead.
* Mass stock move invoicing: fixed an issue preventing to invoice customer returns.

## [8.0.4] (2024-04-04)

### Fixes
#### Base

* Pricing: fixed the parent product filter in computed pricing formula.
* Company: fixed missing info in the demo data import.

#### Account

* Invoice: fixed printing of advance payment invoice in invoice report.
* Analytic axis by company: fixed analytic axis display error.
* Invoice: fixed report when invoice is linked to more than one stock move.
* Move: fixed error when selecting a third party payer partner.
* Move template line: added missing tax in demo data.
* Payment Session: fixed error when selecting bank details where payment mode have empty accounting setting.
* Accounting Situation: fixed used credit calculation not taking in account completed sale orders.
* Analytic: fixed amount computation when we change the analytic line percentage.
* Accounting Batch: in closing account batch, prevented result move generation when no accounts are treated.
* Accounting batch: fixed move generation on account closing/opening batch.

#### Bank Payment

* Bank reconciliation: move lines to reconcile in other currencies are now displayed.
* AccountingBatch: fixed direct debit batch.

#### Business Project

* Invoicing project: fixed batch to prevent the generation of invoicing business project when there is nothing to invoice.

#### Contract

* Contract template/Contract version: fixed 'Additional Benefit Management' field display.
* Contract template: fixed an error occuring in the server logs on opening a contract template.
* Contract: fixed issue on change of contract line fields on a new version.

#### Human Resource

* Expense: fixed expenses creation for subordinates.
* Expense line: fixed project filter to use user related to employee instead of current user.

#### Production

* Manufacturing order: fixed unable to select a tracking number on a consumed product.
* Bill of materials import: fixed creation of a new product from bill of materials import form view.
* Outsourcing: fixed setting outsourced in a new prod process line.

#### Quality

* Quality: fixed formula of dimension in control type demo data.

#### Sale

* Sale order line: fixed an error that occured when changing project of a line.
* Sale order: fixed scale for quantity and price fields in editable grid.

#### Talent

* App config: fixed demo data in the recruitment app config.


### Developer

#### Account

- Updated Invoice.rptdesign file with some fixes.
- On an advance invoice payment, 'Imputed by' field is now correctly filled with the reference to the payment on the invoice.

---

In accounting configuration, the field `analyticAxisByCompanyList` had previously a sequence starting with 0. However, since the upgrade of the framework to v7, the sequence of this list now starts with 1. 
The processes that previously assumed a starting sequence of 0 will now be adjusted to start from 1 in order to align with the behavior of AOP.

If you use this configuration, after this patch please make sure that this sequence is starting with 1 in your database.

---

- Removed getAnalyticAmountFromParent method from AnalyticLineService.
- Removed calculateAmountWithPercentage method from AnalyticDistributionController.
- Removed action-method : `action-method-calculate-amount-with-percentage-analytic-move-line` and replaced by existing action-method : `action-analytic-move-line-method-compute-amount`.
- Added computeAmount method in AnalyticMoveLineService.
- Updated parameters on getParentWithContext method from AnalyticControllerUtils.

#### Sale

Removed `action action-sale-order-line-record-progress`

## [8.0.3] (2024-03-21)

### Fixes
#### Base

* Webapp: Updated Axelor Open Platform dependency to 7.0.4.
* App: removed warning during app installation.
* Sale order/Purchase order: fixed number of decimals display in price and totals.
* Fixed wrong french translation of 'Application' (was 'Domaine d'applicabilité').
* Language: fixed an issue where getting default language did not use the configuration 'application.locale'.
* App Base: fixed wrong currency conversion line in demo data.
* Price list: check that the product is purchasable on purchase price lists.

#### Account

* Accounting batch : fix result move functional origin in closure/open batch.
* Move: fixed mass entry technical origin missing in Move printing.
* Payment voucher: fixed paid amount selecting overdue move line.
* Accounting batch: fixed the block customer message when no result.
* Partner/AccountingSituation: added error label when multiple accounting situation for a company and a partner.
* Reconcile manager: fixed move lines selection.
* Accounting batch: fixed currency amounts on result moves in opening/closure.
* Invoice payment: fixed technical error when setting currency to null.
* Financial discount: fixed technical error linked to invoice terms.
* FEC Export: fixed technical error when journal is missing.

#### Budget

* Budget template: fixed database error related to scaling.
* Purchase order line: fixed wrong budget distribution when invoicing multiple purchase order lines.

#### Contract

* Contract template: fixed error when opening an existing contract template or saving a new one.
* Contract: fixed technical error when creating lines without saving.
* Contract line: fixed analytic lines creation using amount in company currency.
* Contract: fixed prorata invoicing when invoicing period is smaller than the invoicing frequency.

#### CRM

* Event: fixed an error preventing the computation of the event duration.
* Tour: hide validate button when everything is validated.

#### Helpdesk

* SLA: added missing translations inside 'reach in' in readonly.
* Ticket dashboard: fixed an issue preventing ticket dashboard loading.

#### Human Resource

* Expense: fixed internal error preventing employee selection.

#### Production

* Manufacturing order: fixed a bug where missing quantities were not displayed.
* Prod process line: added missing filter on type for work centers.
* Manufacturing order: fixed error on change of client partner for manuf orders without related sale orders.
* MPS: fixed quantity not editable on MPS proposal.
* Product: fixed an issue where 'economic manuf order qty' field was displayed twice.
* Product: fixed cost sheet group display on product form on semi-finished products.

#### Stock

* Inventory: fixed type in inventory demo data.

#### Supplier Management

* Supplier request: fixed 'JNPE' error on partner selection in Supplier request form.


### Developer

#### Contract

A new transient field `companyCurrency` was added in `ContractTemplate` so the model can implement the `Currenciable` interface.

#### Production

Moved `com.axelor.apps.production.service.ProdProductProductionRepository` to `com.axelor.apps.production.db.repo.ProdProductProductionRepository`

## [8.0.2] (2024-03-07)

### Fixes

* The format of this file has been updated: the fixes are now separated by modules to improve readability, and a new `developer` section was added with technical information that could be useful for developers working on modules on top of Axelor Open Suite.

#### Base

* Import configuration: fixed issue preventing from using zip file to import.
* Studio Apps: fixed regression where some apps name were not translated.
* Translation import/export: fixed display issue when an error occurs during import.
* ABC class: fixed a bug where everything was classed in the first class.

#### Account

* Account clearance: fixed issue when opening a generated move line from account clearance.
* Doubtful batch: fixed NPE errors on doubtful customer batch.
* Invoice payment: added missing french translation for error message.
* Invoice payment: fixed financial discount management when VAT is exempted.
* Move: fixed technical error in move reverse on original move unreconcile.
* Period: fixed an issue when checking user permission where the roles in user's group were not checked.
* Account: manage dynamically decimals depending on the currency in Accounting reports.
* Reconcile: fixed passed for payment check at reconcile confirmation.
* Reconcile: fixed selected amount with negative values.
* Fixed asset: fixed disposal wizard view and disposal sale move.
* Move: fixed missing label in accounting move printing.
* Reconcile generator: fixed scale management.

#### CRM

* CRM App: fixed small typos in english titles.
* Tour: reset 'validated' status on lines after copying a tour.
* Lead: fixed anomaly which was forcing a lead to have a company.

#### Human Resource

* Timesheet line: improved timesheet line creation process.
* Leave request: fixed Payroll input display so it is visible for HR manager only.
* Leave reason: fixed demo data.
* Payroll preparation: fixed 'the exported file is not found' error on payroll generation when the export dir is different from upload dir.

#### Mobile Settings

* Mobile dashboard: deleting a mobile dashboard will now correctly delete related mobile dashboard lines.

#### Production

* Sale order: fixed a NPE that occured at the manuf order generation.
* Bill of materials: fixed a bug where imported BOMs were linked to components.
* Manufacturing order: fixed an issue when the products to consume are by operation where the product list to consume was not filled when planning.
* Manufacturing order: real process are now correctly computed in cost sheet.
* Manufacturing order: fixed a bug a producible quantity was not correctly computed when a component was not available.

#### Sale

* Sale order: improve performance on sale order save.

#### Stock

* Stock move: fixed 'concurrent modification' error preventing from planning a stock move.

#### Supply Chain

* Stock move: fixed a bug that prevented to totally invoice a stock move when partial invoicing for out stock move was activated.
* Supplychain configuration: fixed default value for "Generation of out stock move for products".
* Sale order: fixed a bug where sale orders waiting for stock move were not displayed.


### Developer

#### Base

* CurrencyScale : Technical rework for CurrencyScale services

Created an interface called "Currenciable" with the following public methods who take as parameter in most of cases a Currenciable object :

- getScaledValue
- getCompanyScaledValue
- getScale
- getCompanyScale
- getCurrencyScale
- getCompanyCurrencyScale
- isGreaterThan
- equals

Removed CurrencyScaleServiceAccount, CurrencyScaleServiceBankPayment, CurrencyScaleServiceBudget, CurrencyScaleServiceContract, CurrencyScaleServicePurchase, CurrencyScaleServiceSale.
And replace those services by CurrencyService call.

Implement Currenciable interface into those models:

- axelor-account -> AnalyticMoveLine, FixedAsset, FixedAssetLine, Invoice, InvoiceLine, InvoicePayment, InvoiceTerm, Move, MoveLine, PaymentVoucher
- axelor-bank-payment -> BankReconciliation, BankReconciliationLine, BankStatementLine
- axelor-budget -> Budget, BudgetDistribution, BudgetLevel, BudgetScenario, GlobalBudget
- axelor-contract -> Contract, ContractLine
- axelor-purchase -> PurchaseOrder, PurchaseOrderLine
- axelor-sale -> SaleOrder, SaleOrderLine

#### Account

- Removal of `action-method-account-clearance-show-move-lines` and creation of `action-account-clearance-view-move-lines` for its replacement
- `showAccountClearanceMoveLines` has been removed from `AccountClearanceController`

---

- Changed FixedAssetGroupService.getDisposalWizardValuesMap parameters: fixedAsset,disposalTypeSelect -> disposal, fixedAsset, disposalTypeSelect

#### CRM

- In `lead-form-view`, removed `required` from `enterpriseName`

#### Human Resource

- Moved every timesheet line creation methods into TimesheetLineCreationService. This service was added in the constructor of several other services.
- Create TimesheetLineCreationProjectServiceImpl to override a creation method.

## [8.0.1] (2024-02-22)

#### Fixed

* Updated Axelor Open Platform dependency to 7.0.3.
* App builder: update axelor-studio dependency to 2.0.1

Please see the corresponding [CHANGELOG](https://github.com/axelor/axelor-studio/blob/2.0.1/CHANGELOG.md).

* Stock location: fixed wrong QR Code on copied stock location.
* Move: fixed currency rate errors in move line view.
* Invoice: fixed an issue when returning to the refund list after creating a refund from an invoice.
* Bank order: fixed multi currency management.
* Purchase order: fixed JNPE error on purchase order historic opening.
* Cost calculation: fixed JNPE error on select of product.
* Product: replaced stock history panel which is showing empty records by a panel to stock location line history.
* Employee: fixed error happening while deleting employee.
* Invoice: fixed an error on invoice ventilation when the invoice had an advance payment in previous period.
* Sale order: removed the possibility to mass update fields on sale order, as it caused inconsistencies.
* Fixed asset: fixed purchase account move domain in fixed asset form view.
* Expense: added new API request to update expense line.
* Import: display an error when mapping or data file do not have the expected extension.
* Operation order: fixed duplication of operation order that could happen on some cases during planification.
* PurchaseOrder/SaleOrder: fixed analytic distribution lines creation with company amount.
* Invoice: fixed display of delivery address on advance payment invoices generated from a sale order.
* Budget: remove all dependencies with other modules when the app budget is disabled.
* Accounting batch: take in account accounted moves in accounting report setting move status field.
* Invoice: fixed wrong scale at ventilation in the move.
* Computing amounts in employee bonus management now alert user if employee does not have a birth date or seniority date.
* Expense: added an helper and improved the title on orphan expense line config.
* Project: fixed opening gantt view per user.
* Accounting report: set readonly export button and add report type check to provide critical error.
* Leave type: hide allowInjection if exceptionnal leave.
* Invoice: fixed an issue with a partial multi currency payment computing the wrong amount remaining to pay.
* Operation order: finishing a manuf order from the operations correctly computes the cost sheet quantities.
* Sale order: fixed technical error preventing pack creation.
* Contract: reset sequence when duplicating contracts.
* Reconciliation: fixed invoice term imputation when PFP not validated.
* Manufacturing order: finishing a manufacturing order now correctly updates the cost price of a product.
* Stock move: fixed error when spliting in two a stock move.
* Inventory line: forbid negative values in inventories.
* Accounting export: fixed FEC export not taking journal into account.
* Project: fixed critical error when we change purchase order line quantity on purchase order generated by project task.
* Medical visit: fixed JNPE error when saving a new medical visit.

## [8.0.0] (2024-02-07)

### Features

#### Upgrade to Axelor Open Platform version 7.0

* Axelor Open Platform version 7 is a full rewrite of our front-end. The previous version was written on top of AngularJs, it is now on top of React.
* See corresponding [Migration guide](https://docs.axelor.com/adk/7.0/migrations/migration-7.0.html) for information on breaking changes.

#### Base

* Management of prices scale per currency: now currency with no scale or with a scale of 3 are managed.
* Pricings: the "pricing" feature which was available for sale order line, can now be used for any model.
* Address: a new configuration is now available for addresses, and is used to have different address format per country.
* Localization: a new configuration "Localization" was added to manage language and country information for the user and the company.
The resulting locale will be used for translation, date and currency formats.
* Translation: add a new import/export feature to update translations more easily on a running instance.

#### CRM

* Tour: add a new model to plan tour for salespersons.

#### Purchase

* Purchase order: improve purchase order merge process.

#### Sale

* Sale order line: add a new configuration to enable editable grid on sale order line.
* Sale order: improve sale order merge process.

#### Account

* Invoice: improve invoice merge process.
* Invoice: it is now possible to invoice multiple sale orders with a single invoice.
* Financial discount: reworked financial discount feature.

#### Budget

* Improve budget structure to allow as many as budget level as wanted.
* Improve budget validation so each section can be validated.
* Add a budget generator that will help to build budgets, preview the result and generate a budget with all sub levels.
* Add a new reporting for a budget scenario.
* Add the possibility to link projects with budgets.

#### Stock

* Stock move: a new stock move can be generated from a return.
* Stock move: a new button has been added to split lines into a fulfilled line and a line with the remaining quantity.
* Stock location: stock location valuation can now be retrieved at a given date in the report.

#### Supply Chain

* MRP: add a new delay on purchase and manufacturing order proposal.
* Stock move: merging stock moves is now possible.

#### Production

* Bill of materials: add a new menu allowing to import bill of materials.
* Subcontracting: the subconctracting feature was reworked.
* Manufacturing order: add a new configuration to complete stock moves from manufacturing order. Produced products quantities are retrieved on the stock move when the manufacturing order is done.

#### Quality

* Quality control: this new feature allows to define controls, make them periodically, and visualize the result.

#### HR

* Expense: manage expense lines without parent expense to see lines created from the mobile app.

#### Mobile Settings

* Dashboard: a new menu is added to allow to create dashboards available on the mobile application.

### Change

#### Base

* Sequence: add alphanumeric pattern for sequence, allowing to create sequence like this: `320BMF -> 320BMG -> ... -> 320BMZ -> 320BNA -> ...`
* Sequence: allow to configure prefix/suffix with a groovy script.
* Tax number: add a configuration allowing to enable or disable this feature.

#### CRM

* Opportunity: the type of opportunity is now a selection instead of a simple string.

#### Purchase

* Supplier catalog: manage maximum quantity.

#### Sale

* Configurator: add demo data.

#### Account

* Accounting move: improve form view.
* FEC Import: add a new button to retrieve a default FEC import configuration.
* Accounting configuration: improve import for company accounting configuration.
* Journal: display a debit/credit indicator.
* Doubtful customer: add a preview to see accounting moves to shift to doubtful.
* Accounting custom report: add a new config to to hide empty lines.
* Accounting custom report: line detail now works with accounting account codes.
* Accounting custom report: remove characters limitation for accounting account codes.
* Accounting custom report: removed unused fields.
* Fixed assets: technical change to prefix fields related to import with `import`.

#### Budget

* Improve budget distribution.
* Add a new option to update the budget automatically on order/invoices/moves validation.
* Global budget: do not show indicators while budget is not saved.

#### Bank Payment

* Bank order: add a new configuration to switch the lines display, editable or list.
* EBICS: removed fields related to EBICS T.

#### Business project

* Rework menus order.

#### Stock

* Stock location valuation: add an option to see the valuation details per sub stock location.
* Stock rules: stock rules are now computed using a batch to avoid performance issues when realizing large stock moves.
* Tracking number: improve wizard to split lines per tracking number on a manual stock move.
* Tracking number: on a product, it is now possible to manage tracking number per company.
* Tracking number: add supplier on tracking number.
* Tracking number: improve the tracking number wizard on stock move to allow tracking number selection.
* Incoterm: add a configuration to activate or deactivate.

#### Supply Chain

* Add a new "Timetables" menu entry to see timetable invoicing for sale orders.

#### Production

* ProductionConfig: the configurations per company form view was reworked.
* Work center: if configured, the work center group is now used for the planification.
* Work center: add a chart and a calendar view on work center group to visualize related work centers planning.
* Work center: improve employee management on work centers.
* Work center: improve production process line configuration.
* Employee: add a new calendar view for employee.
* Machine: add a chart to visualize machine planning.

#### Helpdesk

* Ticket: make ticket status configurable.
* Ticket: remove unused fields `mailSubject` and `ticketLead`.
* SLA: improve SLA feature.

#### HR

* Leave request: improve leave configuration.
* Timesheet: rework timer feature and add a new configuration to manage multiple timers in parallel.
* Add a new API to create an expense.

#### Mobile Settings

* Stock: add new configurations to select which roles have access to which feature on the mobile application.
* Stock: add a new configuration to disable stock location management on the mobile application.
* Authentication: add a new API to fetch user permissions.
* HR: add new configuration to manage timesheets from the mobile application.

[8.0.8]: https://github.com/axelor/axelor-open-suite/compare/v8.0.7...v8.0.8
[8.0.7]: https://github.com/axelor/axelor-open-suite/compare/v8.0.6...v8.0.7
[8.0.6]: https://github.com/axelor/axelor-open-suite/compare/v8.0.5...v8.0.6
[8.0.5]: https://github.com/axelor/axelor-open-suite/compare/v8.0.4...v8.0.5
[8.0.4]: https://github.com/axelor/axelor-open-suite/compare/v8.0.3...v8.0.4
[8.0.3]: https://github.com/axelor/axelor-open-suite/compare/v8.0.2...v8.0.3
[8.0.2]: https://github.com/axelor/axelor-open-suite/compare/v8.0.1...v8.0.2
[8.0.1]: https://github.com/axelor/axelor-open-suite/compare/v8.0.0...v8.0.1
[8.0.0]: https://github.com/axelor/axelor-open-suite/compare/v7.2.7...v8.0.0
