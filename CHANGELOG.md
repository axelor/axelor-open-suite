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

[6.3.0]: https://github.com/axelor/axelor-open-suite/compare/v6.2.8...v6.3.0
