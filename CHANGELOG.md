## [6.3.4] (2022-10-28)

#### Fixed

* Invoice: generated stock move invoices now correctly copy budget distribution on product with tracking number.
* Invoice: fixed an issue where a copied invoice could not be paid.
* Invoice payment: fixed a bug where payments were pending even when without bank order.
* Stock move: fixed an error preventing "Generate invoice" button from appearing.
* Advanced export: fixed JNPE displaying when selecting "target" field.
* Expense: company and employee now correctly appear as mandatory in form view.
* FEC Import: fix imported move daybooking and accounting.
* Payment voucher: company bank details is now required if multi banks is activated.
* Leave request: fix an issue occurring when validating a leave request and with the employee not linked to an user.
* Business project: fixed an issue preventing the creation of a business project.
* Accounting report configuration line: fix import '-' sign issues.
* Operation order: prevent creation of operation order without manufacturing order and prevent machine change.
* Analytic distribution line: use the company analytic axis configuration to filter the analytic axis on an analytic distribution line.
* Bank reconciliation: prevent dates edition when including other bank statements.

## [6.3.3] (2022-10-21)

#### Changes

* Add missing config for extraordinary depreciation account on fixed assets category and modify disposal move generation process to include this new config account.

#### Fixed

* Studio Editor: Fix UI issues
  * Save with the shortcut Ctrl+S.
  * Automatically switch to the properties panel when inserting a new field.
  * Hide delete button on classic view.
  * Scroll to attrs field on select of model, view and custom field.
  * Add title like "Overview" in white color.
  * Add same typeface in studio and in the rest of the application.
  * Add same save and delete icon.
  * Panel title change to attribute.
  * Fix issue to remove actions from the studio.
  * Fix no value of the showTitle in the widget attrs for the panel.
* Menu builder: add help attribute in domainCondition field of MenuBuilder form.
* Bank reconciliation report: Made adjustements (fonts, size, etc..) in the report.
* Analytic axis: improve groupings management.
* Move line: fixed an issue when copying a move line used in a bank reconciliation session.
* Sale Order and Invoice: now correctly filter selectable partner if a line is already present.
* Bank reconciliation printing: fixed an issue where the printing was showing canceled and already reconciled moves.
* Fixed asset: fixed an issue in UI where the button to update depreciation settings was shown while it shoulde have been hidden.
* Bank Reconciliation: fix ongoing reconciled balances computation.
* Move: fixed an error preventing to select a parte with an empty journal while creating new move.
* Sequence version: fixed an issue when generating a new sequence version by year/month.
* Stock move merge: When merging stock moves to a single invoice, the fiscal position is now correctly filled and must be the same for all orders.
* Project: reset sequence on project duplication.
* Move: the date is now displayed even if the move is accounted.
* Bank Reconciliation: help move line selection by filtering on account type.
* Accounting report: fix missing french translation in general balance printing.

## [6.3.2] (2022-10-17)

#### Changes

* Fiscal year: improve UX to create new period.

#### Fixed

* Outcoming stock move: when realized, outcoming stock move will now generate a line on the wap history.
* Interco: fixed an error occurring when generating a sale order from a purchase order.
* Invoice report: invoice lines will now correctly display the currency when the invoice in ati option is activated.
* Helpdesk: fixed typo in french translation for cancel button.
* Supplychain: fixed error when importing purchase order from supplychain.
* Logistical form: fixed a translation issue when printing packing list.
* Inventory: trying to import a inventory line with a product that does not exist will now result in a explicit error.
* Invoice: fixed company bank details when partner is factorized.
* Bank reconciliation: move line is now saved in bank statement line to avoid unlimited auto accounting on a same bank statement line.
* Accounting batch: fill the selected currency with the default company currency.
* Prod process report: fixed ordering of prod process lines in the report, they are now correctly sorted by priority.
* Bill of materials: added an error message when company is missing during cost price computation.
* Bank reconciliation: fixed an issue where balance was not recomputed when lines are selected or unselected.
* Stock history: fixed an error occurring when updating stock history in batch if product does not have a stock location.
* Sale order report: now correctly display the title of shipment and end of validity dates.
* Analytic rules: prevent account from being saved if there are unauthorized analytic accounts.
* Supplychain: fixed error occurring while importing demo data.
* Bank order: add verification to avoid sending twice the same file to the bank on user mistake.
* DAS2 preview: fixed year filter on move in DAS2 preview process.
* Add missing french translation on error message in PFP process.

## [6.3.1] (2022-09-29)

#### Fixed

* Sale order: fixed an issue allowing users to invoice too much quantities when invoicing partially.
* Sale order: fixed an error preventing users from invoicing partially a sale order after refunding it.
* Sale quotation template: Fix NPE error when saving a new sale quotation template.
* Bank Statement: corrected wrong behaviors of check bank statement on import.
* Account type: Update demo data by checking, on the designated technical account types, the checkbox that exports the partner associated to the move line.
* Move: fixed an issue where simulate button was not displayed on grid view after deleting move from form view.
* Move: fixed error message when trying to remove a move.
* Move: fixed an issue where the period field was not emptied on company change, causing inconsistencies.
* Move: duplicating a move with status simulated will now correctly reset the status to new instead of simulated.
* Move: prevent the accounting of a move that contains move lines out of the move period.
* Move: optimize performance when reconciling multiple move lines.
* Journal: complete "allow accounting daybook" in journal demo data.
* Debt recovery: removed error message when there is no email address for a mail type message.
* Purchase Order: fixed an issue where "amount available" in budget tab was not correctly computed.
* DAS2: fix mandatory infos null checks for DAS2 export
* Invoice: generating an invoice from a sale order or a stock move now correctly sets the project.
* Invoice: fixed an issue where project was not filled on a partial invoice generated from a purchase order.
* Invoice line: fix error message when opening an invoice line not from an invoice.
* Bank Payment: added bank statement demo data.
* Contract: remove duplicate duration values on contract templates and fix french translation in form view.
* MRP: fixed MRP calculation for manufacturing order partially finished, the remaining amount to produce is now used in the computation.
* Stock move line: fixed an issue allowing the user to fill quantity in a title line, causing inconsistencies.
* Tracking number: fixed an issue where the product was missing in a tracking number created from inventory lines.
* Tax: fixed an error occurring when choosing a product for a sale or a purchase order if the creation date was not filled.
* Accounting Report: corrected numbers exported as text in general comparative balance report in excel format.
* Accounting Batch: fixed NPE error on bank order creation when expense payment date is null.

## [6.3.0] (2022-09-15)

#### Features

* Studio: API connector

The 'API Connector' allows to call REST api dynamically.
A new type of script node on bpm is added to call api connector from bpm.
Export and import api connector with app loader.

* BPM: Camunda upgrade to 7.17 and improvements.
* Account, Analytic Journal, Analytic Account: manage active/inactive status to allow an accounting manager to disable an existing account/analytic journal/analytic account.
* Accounting period: new temporarily closed status

The objective of this feature is to be able to manage temporarily closure in period. You can now select roles in account configuration for temporarily closure or permanently closure.
Thanks to these configuration, it will be possible to allow specific users to modify moves inside a temporarily closed period.

* Analytic Journal: add a code for analytic journal and a unique constraint on code and company.
* MRP: added a new mrp line type in order to use stock history
* MRP: purchase proposal changes - introduce a new mrp line type for delivery

Add a new mrp line type 'purchase proposal / estimated delivery in order' to increase the cumulated quantity once we will have received the product instead of increasing it at the order date.
The previous behavior is kept if we do not define this new mrp line type.
A new column is also added to know the theorical purchase date and the theorical delivery date to avoid being out of stock. In case of empty stock, the dates are displayed in red color.

* Bank Reconciliation: In bank statement rule, we can now choose to get the partner with a groovy formula and to letter the generated move to a invoice.
* Currency: Add a new field ISO Code

The old field code is now only used for printings.
The new field ISO code is used for retrieving currency values.

* Accounting dashboard: Add new dashboards.
* Accounting reports: Deductible and payable vat now appear as negative amounts for supplier and client refunds amounts.
* Accounting reports: Allow the creation of templates to simplify accounting report configuration.
* Bank Order: Manage bill of exchange (LCR).
* HR: expenses can now be copied.
* Pricing scale: Meta json field can now be used in pricing scale computation.
* Product: Manage partner language for product name and description translation.

When adding line to an invoice, sale order or purchase order, the name and description are now translated in partner language (if a translation exists).
In this case, a message now appears to alert the user.

#### Changes

* ACCOUNT MOVE: update sequence when we validate it only if previous is a draft sequence

The main goal is to be able to import some account moves from the FEC import feature and keep the original move reference.
Second is to be sure that we never have two definitive reference for the same move.

* Accounting report: Custom accounting report will now uses a sequence for 'accounting report' and not 'custom accounting report'
* HR: Most HR process (expenses, leave requests, timesheets, ...) were linked to the user and not to the employee. This behavior is now changed, and they are now linked to the employee.
* USER: rename today field into todayDateT
* PARTNER: Forbid to untick partner categories (factor, customer and supplier) if records already exist.
* Rename move status

Status accounted becomes daybook
Status validated becomes accounted
Track messages updated
Viewer tags updated
Validate buttons updated
Validation date becomes accounting date
Related prompt message updated

* Journal: Changed unicity constraint to company,code and set readonly if linked to move


#### Fixed

* Optimize update stock history batch to reduce process duration
* Sequence: fix an inconsistency for yearly reset sequence

Sequence version with yearly reset are automatically generated with the correct end date

* AccountingReportConfigLine: Fixed bugs where the alternation character '|' could not be used in account code and values of the report could be multiplied
* Sequence: fix sequence generation

New sequence version with monthly reset will now create a sequence version 
with start date at the beginning of the month and the end date at the end of the month.
Same change with sequence with yearly reset but with beginning and ending of the year

#### Deprecated

* Java service: rename IExceptionMessage

All IExceptionMessage service are now moved to `[Module name]ExceptionMessage`. All IExceptionMessage services are now deprecated. On any module using IExceptionMessage classes, the new classes must be used instead.

* Deprecate old API calls in contact, base, crm and sale modules. There use is currently discouraged, they will be replaced in a future version implementing a new API for AOS modules.
* Deprecate stock and production configuration for the mobile application.

These old configs will be removed in 6.4 because a new mobile application for
stock and production modules will be available.


#### Removed

* Remove configuration which allows to remove the validated move
* Removed deprecated java methods

The following java methods were deprecated and are now removed:

  * AppBaseService#getTodayDate() (replaced by getTodayDate(Company))
  * In AxelorException.java, the constructors AxelorException() and AxelorException(String message, int category, Object... messageArgs)
  * QueryBuilder#create() (need to call build() instead)
  * YearServiceAccountImpl#computeReportedBalance2
  * RefundInvoice#refundInvoiceLines
  * SaleOrderService#getReportLink

If you had modules calling these methods, you will need to update them so they can be compatible with Axelor Open Suite v6.3.

* Remove unused purchase request grid views
* Remove unused partner views
* Sale order line: unused invoicing date field was removed.
* Account Config: Remove Invoices button and associate action from account config
* Stock correction: Removed unused future and reserved quantity from database.

[6.3.4]: https://github.com/axelor/axelor-open-suite/compare/v6.3.3...v6.3.4
[6.3.3]: https://github.com/axelor/axelor-open-suite/compare/v6.3.2...v6.3.3
[6.3.2]: https://github.com/axelor/axelor-open-suite/compare/v6.3.1...v6.3.2
[6.3.1]: https://github.com/axelor/axelor-open-suite/compare/v6.3.0...v6.3.1
[6.3.0]: https://github.com/axelor/axelor-open-suite/compare/v6.2.8...v6.3.0
