## [6.4.1] (2022-12-08)

#### Features

* Invoice: add new French statements in Invoice Report depending of product type.
* Invoice: add partner siret number in French report.

#### Fixed

* Move line query: partner field is no more required in database and views.
* Reconcile: optimization were made to reduce execution time on all accounting processes related to reconciliation.
* User: email field is now correctly displayed

Email field is now displayed in editing mode.
Email field is now displayed in readonly mode only if a value is present.

* Followers: fixed a bug where trying to fetch a recipient would result in a exception with somes conditions.
* Sale order: fixed stack overflow error when the partner had a parent partner.
* Cash management: fixed an issue were some data were not displayed correctly on the PDF report.
* Invoice line: choosing a product from a supplier catalog now takes the product name from the supplier catalog and not the translation.
* Payment session: fix confirming a bank order generated from a payment session when the bank order lines had different dates.
* Invoice tax line: set vat system to default value when tax line value is 0.
* Leave request / employee: a HR manager can now access draft requests for other employees from the menu.
* Fix creation of new invoice term from moves and invoices.
* Accounting move: fix NPE when modifying move lines on a duplicated move.
* Accounting move: when we reverse a move, the analytic move lines are now correctly kept on the reversed move.
* Account config: add missing sequences of analytic axis in demo datas.
* Debt recovery history: prevent the user from printing a report when there is no debt recovery.
* Fixed asset: fixed disposal move not generated when proceeding to the disposal of a fixed asset where accounting value is equal to 0.
* Fixed asset: fixed wrong accounting value in degressive computation.
* Fixed asset: fixed tax lines management in cession sale move generation

Check on missing tax account only when the tax line value is positive
Initialize of taxline in movelines for credit move line and tax move line

* Move line: fix typo in french error message.
* Payment voucher: change payment voucher element lists to display the moveline origin instead of the invoice directly.
* Move Template: rework of tax management
  - Tax move lines are now selectable just for preview.
  - Recompute of tax move lines via other move lines with tax on it.
  - Message when a tax move line is set to inform user.

* Stock move: changing the real quantity of a line will not reset the discounted price to the original price anymore.

When generating a Stock move from a Purchase order, if a line had a discount,
changing the real quantity of a discounted line would set the price to the original one. This wrong behavior is now fixed.

* Journal: "Has required origin" boolean french help text have been corrected.
* Sale order : Fixed the automatic filling when partner change for the field 'Hide discount on prints' according to it value in the price list.
* Price List: fixed wrong filter on price list menu entry.
* Stock move mass invoicing: fixed a bug where some stock moves were not be invoiced when the invoice was created from a merge.
* Accounting batch: Fix the batch to realize fixed asset line so it correctly update only the fixed asset lines from the selected company.
* Operation order: fix a blocking NullPointerException error when we plan an operation order without a machine.
* Sequence / Forecast recap: Fix demo data sequence for the forecast recap.
* Accounting dashboard: fix not opening the right grid when clicking on the rejection reason pie chart.
* Invoice: when trying to ventilate an invoice, fixed a case where rounding issue happened preventing the ventilation.
* Accounting report : fix DAS2 printing
* Stock move: details stock locations lines will not longer be generated with zero quantity when the stock move is a delivery. This change will allow to correctly use auto generation of tracking number for sales.
* Bank order: Add the possibility to sign bank orders even without EBICS module.
* Sale/Purchase order: fix the way we fetch default stock location on purchase and sale orders using partner stock settings.
* Expense: when paying expense, correctly fill sender company and sender bank details in the generated payment.
* Accounting demo data: fix demo data for fixed asset category and journals.
* Discounted payment move: corrected discounted payment move been not balanced.
* Invoice payment: corrected discount fields not displayed in invoice payment.
* Payment voucher: fix amount controls on payment given auto imputation and financial discount.
* Accounting move line: fix currency amount/rate readonly condition in the counterpart generation.
* Stock move: if the configuration is active, services (product) are now correctly managed in stock moves.

## [6.4.0] (2022-11-28)

#### Features

* Invoice Terms:

Add a new feature to manage multiple terms for invoicing.
A new “invoice terms” table has been added on invoices and on associated move lines.
By configuring payment conditions and selecting them on an invoice these terms are automatically generated.
This new feature is now used every time we pay any invoice from any process (move lettering, payment session, payment voucher, …).

* Cut off:

  * Manage cut off process on invoices and moves.
  * Add a preview option in the cut off batch.
  * Add new configurations to select cut off accounts.
  * The configurations and the batch are now moved from supplychain to accounting module.

* Payment Session:

Introduce a new UI to allow financial department employees to generate outgoing payments in mass or generate direct debit for incoming payments.
The feature include, depending on whether the options have been activated, a search and selection screen for all invoice terms to be paid depending on search criterias (payment mode, due date, financial discount) and the generation of related accounting moves and the associated bank orders (SEPA or International wire transfer or direct debit).
The payment session only works at the moment in the company currency.

* VAT:

The way VAT is working has been reviewed to better fit with usages. The configuration determining the type of VAT (on Deliveries/Invoicing Vs Payments) has been removed from the tax allowing the use of a single tax record for companies selling goods and services. The new feature includes a set of two fields (on partner and accounts) to determine the regime/system that applies. VAT reports are updated with the new system.

* Deposit slip: cheque deposit slip report complete rework.
* Analytic Account: manage analytic account by company

Allow an analytic account to be linked to a company so the analytic account can be shared between all companies or can be assigned to a specific company.

* Annual Closure : Improve annual closure and new closure assistant.

Now specific journals can be open or closed on the period. This feature is also taken into account in the annual closure accounting batch.
In the annual closure accounting batch possibility to generate the result move if the year accounts are opened.
New accounting batch “Control moves consistency” (“Contrôle des écritures”) to check daybook moves before the annual closure in order to check if daybook moves can be automatically accounted.
A new closure assistant allows you to follow the fiscal year closure step by step (possibility to see the step, open the action, validate or cancel it).
At the close of a year, all linked periods are automatically closed.

* Studio : UI improvement and changes

  * Support of icon and color for selection builder.
  * Set required field in red Ergonomy view.
  * Add pattern on name in custom objects.
  * Fix panel bugs.
  * Use color for fields icon.
  * Deletion popup next to the field.
  * Graphical offset when selecting a spacer.

* Mapper : UI improvement and changes

  * Remove search fields column and replace 'Search' value from option 'Query'
  * Add query builder for value column when 'Value from' is 'Query'
  * Add 'Built-Ins Variables' option allow to select built in variables when 'Value from' is 'Context'
  * Add 'Condition' column with expression builder to create condition for selected field
  * Add 'Save' new boolean option for save the new or existing record by generated script
  * Display required fields in red

* BPM : UI improvement and changes

  * Migrate BPM query builder with generic builder
  * Migrate mapper changes with bpm mapper
  * Support item as buttons on user task
  * Support item as buttons in View Attributes of task


#### Changes

* MRP: add new scheduler to run MRP at predefined times.
* Please note following database changes for MRP:
    * Added new fields name and fullName (mrpSeq + name)
    * Added a unicity constraint on mrpSeq

* MRP: Manufacturing order proposals (and their needs) are now set with an appropriate maturity date by taking into account the duration of the manufacturing order.

* Bank reconciliation: Added a new process allowing to correct a reconciliation even after its validation.
* PurchaseOrder : do not manage budget exceed in supplychain module

The budget exceed check called when requesting a purchase order can no longer be managed from supplychain module, as there is now different types of budget exceed checks that are managed directly in budget module.
Also, the check is now more of an informative message than a blocking error.

* Tax rate: Allow to define the tax rate in % instead of a coefficient.
* Bank details : change how full name is computed

Bank details full name is now computed as :
`<code> - <label> - <iban> - <bank.fullname>`

* Sequence: display an understandable message when the generated sequence value already exists
* Move/Journal: add a prefix on the origin when we reverse or copy a move.
* Accounting Situation: add a search bar on the move line dashlet.
* Account Configuration: in configuration by company, split the configuration to activate payment vouchers on invoice to allow to configure independently payment vouchers on supplier and on customer invoices.
* Invoice report: add text line option on deliveries under tax table in invoice report.
* Bank order line origin: add new button to open the invoice pdf files.
* App: add a new option allowing to install apps automatically on startup with demo data

`aos.apps.install-apps` property can now be used to install AOS modules by default when starting Axelor Open Suite on a new database. If `data.import.demo-data` is true, then demo data will also be imported.

* Move: add a coherence control with tax lines

When you want to validate a move, a coherence control now checks if tax lines are duplicated with the same taxline and the same vat system.

* Employee: change employee card view.
* Move: add reverse charge move line generation on auto tax generation.
* Partner: add validity check on registration code.
* Fixed asset: improve the split feature by adding the possibility to split by gross value.
* Global tracking log: Add reference display button.
* FTP Management: Added a generic connector for file transfer (only support SFTP for now).
* Contract batch: Add invoicing date for invoicing contract batch.
* Move: add a new field company bank details used in forecast recap.
* Move: add button to mass reverse move from grid view.
* Reconcile manager: add new functionality reconcile manager in accounting periodic menu

This feature allows to filter with parameters on move lines with mass reconcile or unreconcile function

* Move: Add more coherence control to check if tax fields should be filled.
* Contract batch: Add type for invoicing contract batch

Added a selection field to be able to run the contract invoicing batch for supplier or customer contracts only

* Payment mode: Add a triggering parameter for the generation of accounting moves in the payment mode.

Moves may be generated immediately (current behavior) or cannot be generated automatically. Moreover, if the bank payment application is activated, it is also possible to generate moves at the bank order confirmation, bank order validation or bank order realization.

* Stock Batch: add a batch to recompute all quantities and WAP history in stock location lines. In WAP history, removed link to stock move line.
* Contact: Improve contact card view presentation.

#### Fixed

* Accounting move: fix how partner information is managed on accounting moves

In the accounting move header, if the partner is filled, then the partner column in lines is hidden. If it is empty, then we allow different partners on move lines.
On FEC import, the partner header on created moves is not filled anymore.
An error on move validation is now raised if the header partner is different from the one in its lines.

#### Removed

* Remove stock and production activation for the mobile application.

A new mobile application for stock and production modules are now available, these modules on the old application are no longer maintained.

* Account budget: Remove checkAvailableBudget in budget, which was unused.
* Accounting report: removed old specific export format for Sale, Purchase, Treasury, Refund (1006 to 1009 accounting report type). Already replaced per the generic Journal entry export with a filter on the journal.

[6.4.1]: https://github.com/axelor/axelor-open-suite/compare/v6.4.0...v6.4.1
[6.4.0]: https://github.com/axelor/axelor-open-suite/compare/v6.3.5...v6.4.0
