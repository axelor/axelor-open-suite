## [7.0.3] (2023-06-08)

#### Fixed

* Business project, HR printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Configurator: fixed issue where sale order line generated from the configurator did not have a bill of materials.
* Deposit slip: fixed errors when loading selected lines.
* Move: fixed error when selecting a journal with no authorized functional origin.
* Invoice: allow supplier references (supplier invoice number and origin date) to be filled on a ventilated invoice.
* Move: fixed move lines tax computation on fiscal position change.
* Invoice/Stock move: fixed an issue where invoice terms were not present on an invoice generated from a stock move.
* Invoice: fixed an issue where the button to print the annex was not displayed.
* Account config: hide 'Generate move for advance payment' field when 'Manage advance payment invoice' is enabled.
* Invoice: fixed JNPE on invoice term form when the form is openened from the invoice.
* Lead: fixed event tab display on lead form view.
* Move line: fixed duplicate invoice terms when move has no payment condition.
* Move: fill automatically vat system when we change account.
* Move template: fixed creation of unbalanced move with a move template of type percent.
* GDPR: fixed demo data error for anonymizer configuration.
* Leave request: fixed an issue on hilite color in leave request validate grid.
* Birt template parameter: fixed french translation issue where two distinct technical terms were both translated as 'Décimal'.
* Budget distribution: fixed an issue where the budget were not negated on refund.
* Move: fixed auto tax generation via fiscal position when no reverse charge tax is configured.
* Sale order line form: fixed an UI issue on form view where the product field was not displayed.
* Move line query: fixed balance computation.
* Supplier portal and customer portal: add missing permissions on demo data.
* Move line: fixed an issue where analytic distribution template were not filtered per company.
* Project: when creating a new resource booking from a project form, the booking is now correctly filled with information from the project.
* Partner: correctly select a default value for tax system on a generated accounting situation.
* Move line: prevent from changing analytic account when a template is set.
* Move/Holdback: fixed invoice term generation at counterpart generation with holdback payment condition.
* MRP: UI improvements on form view by hiding unnecessary fields.
* Stock: fixed an error occurring when updating stock location on a product with tracking number.
* Move: fixed reverse process.
* Move: fixed multiple errors when opening a move line.
* Move: fixed due date not filled when generating moves from an invoice.
* Move: fixed not being able to select a company when it is not automatically set.
* Cost calculation: fixed calculation issue when computing cost from a bill of materials.
* Tracking number: fixed an issue preventing to select a product on a manually created tracking number.
* Reconcile: fixed an issue were it was possible to unreconcile already unreconciled move lines.
* Fixed asset: fixed JNPE error on disposal if account config customer sales journal is empty.
* Move/move line: fixed filter when we select analytic distribution template in move line or payment mode/partner bank details/trading name in move.
* Move: fixed exception when selecting an account on a move line where cutoff dates are filled.
* Accouting report view: fixed an issue where the filter on payment mode was displayed on an analytic report type.

## [7.0.2] (2023-05-25)

#### Fixed

* Updated Axelor Open Plateform dependency to 6.1.3. See the [Changelog](https://github.com/axelor/axelor-open-platform/blob/v6.1.3/CHANGELOG.md#613-2023-05-15) for more details.
* Updated axelor-studio version to 1.0.2, this fixes following issues:
  - fixed an error causing DMN to crash.
  - fixed an issue preventing access to the application in the case where a parameter was missing in `axelor-config.properties`.

* Invoice payment: disable financial discount process when the invoice is paid by a refund.
* Accounting batch: fixed close annual accounts batch when no moves are selectable and simulate generate move if needed.
* Configurator: fixed an issue where removing an attribute did not update the configurator form.
* Tax: fixed tax demo data missing accounting configuration and having wrong values.
* Sale order: fixed an issue during sale order validation when checking price list date validity.
* Printing settings: on orders and invoices, removed the filter on printing settings.
* Accounting move: fixed an issue where we were not able to change currency on a move.
* Invoice payment: update cheque and deposit info on the invoice payment record when generated from Payment Voucher and Deposit slip.
* Purchase order: fixed an error occurring when generating an invoice from a purchase order with a title line.
* Accounting batch: fix duplicated moves in closure/opening batch.
* Bank reconciliation: fixed an issue in bank reconciliation printing where reconciled lines still appeared.
* GDPR search: fixed an issue where some filters in the search were not correctly taken into account.
* GDPR: add UI improvement and data-init to make the module configuration easier.
* Bill of materials: fixed creation of personalized bill of materials.
* Invoice: added an error message when generating moves with no description when a description is required.
* Project: fixed an issue when creating a task in a project marked as "to invoice" where the task was not marked as "to invoice" by default.
* Manufacturing order: fixed filter on sale order.
* Bank order: fixed payment status update when we cancel a bank order and there are still pending payments on the invoice.
* Move: fixed an error that occured when selecting a partner with an empty company.
* Summary of gross values and depreciation accounting report: fixed wrong values for depreciation columns.
* Manufacturing order: when planning a manufacturing order, fixed the error message when the field production process is empty.
* Accounting move line: fixed filter on partner.
* Timesheet: when generating lines, get all lines from project instead of only getting lines from task.
* Accounting report DAS 2: fixed export not working if N4DS code is missing.
* Accounting report DAS 2: fixed balance.
* Bank order: fixed an issue where moves generated from a bank order were not accounted/set to daybook.
* Project task: when creating a new project task, the status will now be correctly initialized.
* Product: fixed an issue where activating the configuration "auto update sale price" did not update the sale price.
* Stock move: prevent cancellation of an invoiced stock move.
* Payment condition: add controls on payment condition when moves are created.
* Stock move: modifying a real quantity or creating an internal stock move from the mobile application will correctly indicate that the real quantity has been modified by an user.
* Bank order: fixed an issue where the process never ended when cancelling a bank order.
* Sale order: fixed popup error "Id to load is required for loading" when opening a new sale order line.
* Journal: fixed error message when the "type select" was not filled in the journal type.
* Account config: fixed UI and UX for payment session configuration.
* Account/Analytic: fixed analytic account filter in analytic lines.
* Account/Analytic: fix analytic account domain in analytic lines
* Move line: fixed error when emptying account on move line.
* Invoice: fixed an error preventing from merging invoices.
* Expense: prevent deletion of ventilated expense.

## [7.0.1] (2023-05-11)

#### Fixed

* Update axelor-studio to version 1.0.1 with multiples fixes made to the app builder.
* Invoice: fixed bank details being required for wrong payment modes.
* Invoice: fixed an issue blocking advance payment invoice creation when the lines were missing an account.
* Job application: fixed an error occuring when creating a job application without setting a manager.
* Bank reconciliation: added missing translation for "Bank reconciliation lines" in french.
* Product: fixed an issue preventing product copy when using sequence by product category.
* Bank reconciliation/Bank statement rule: added a control in auto accounting process to check if bank detail bank account and bank statement rule cash account are the same.
* Tracking number search: fixed an error occurring when using the tracking number search.
* Stock move: fixed an issue when creating tracking number from an unsaved stock move. If we do not save the stock move, tracking number are now correctly deleted.
* Sale order: fixed an issue where sale order templates were displayed from the 'Historical' menu entry.
* Bank reconciliation: fixed issue preventing to select move lines to reconcile them. 
* Accounting payment vat report: fixed wrong french translations.
* MRP: fixed an JNPE error when deleting a purchase order generated by a MRP.
* Partner: added missing translation on partner size selection.
* Public holiday events planning: set the holidays calendar in a dynamic way to avoid it become outdated in the demo data.
* VAT amount received accounting report: fixed height limit and 40 page interval break limit.
* Invoice payment: fixed payment with different currencies.
* Accounting report das 2: fixed currency required in process.
* Payment Voucher: fixed excess on payment amount, generate an unreconciled move line with the difference.
* Bank reconciliation: fixed tax computation with auto accounting.
* Sale, Stock, CRM, Supplychain printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Accounting batch: added missing filter on year.
* Move line: fixed analytic account domain when no analytic rules are based on this account.
* Purchase order: stock location is not required anymore if there are no purchase order lines with stock managed product.
* Custom accounting reports: fixed accounting reports "Display Details" feature.
* Accounting situation: display VAT system select when the partner is internal.
* Invoice: fixed wrong alert message when invoiced quantity was superior to delivered or ordered qty.
* Project report: fixed error preventing the generation of the PDF report for projects.
* Project: Display "Ticket" instead of "Project Task" in Activities tab when the activity is from a ticket.
* Opportunity: added missing sequence on the Kanban and Card view.
* Payment session: select/unselect buttons are now hidden when status is not in progress.
* Analytic move line query: fixed filter on analytic account.
* Move: fixed default currency selection, now the currency from the partner is automatically selected, and if the partner is missing the currency from the company is used.
* Bank reconciliation: fixed initial and final balance when importing multiple statements.
* Accounting report: fixed translation of currency title.
* Bank order: fixed an error preventing the validation of a bank order.
* Inventory: fixed UI issue by preventing unit from being changed in inventory lines.
* Stock rules: now correctly apply stock rules when minimum quantity is zero.

#### Removed

* Because of a refactor, action-record-initialize-permission-validation-move is not used anymore and is now deleted.

* Delete action 'action-record-analytic-distribution-type-select' since its not used anymore.

You can run following SQL script to update your database:

```sql
DELETE FROM meta_action WHERE name='action-record-initialize-permission-validation-move';

DELETE FROM meta_action WHERE name='action-record-analytic-distribution-type-select';
```


## [7.0.0] (2023-04-28)


#### Upgrade to AOP 6.1

* See [AOP migration guides](https://docs.axelor.com/adk/6.1/migrations.html) for AOP migration details
* Upgraded most libraries dependencies.
* Group: new option to enable collaboration
* Studio, BPM, Message and Tools leave Axelor Open Suite to become AOP addons
    * axelor-tools became axelor-utils
* Studio and BPM upgraded
    * Merge Studio and BPM module to a single module and create two different apps
* Apps logic is integrated into the studio
    * apps definition using YAML
    * auto-installer moved from Base to Studio
    * Add new types for apps (Standard, Addons, Enterprise, Custom and Others)
* Web app: application.properties renamed to axelor-config.properties and completed.
    * See [AOP documentation](https://docs.axelor.com/adk/latest/migrations/migration-6.0.html#configurations-naming) for parameter changes related to AOP.
    *  See details for parameter changes related to AOS
        <details>
        `aos.api.enable` is renamed `utils.api.enable` and is now true by default.
        `aos.apps.install-apps` is renamed `studio.apps.install`
        `axelor.report.use.embedded.engine` is renamed `reports.aos.use-embedded-engine`
        `axelor.report.engine` is renamed `reports.aos.external-engine`
        `axelor.report.resource.path` is renamed `reports.aos.resource-path`
        </details>

#### Features

* Swagger
    * API: implement OpenAPI with Swagger UI.
        <details>
            Complete the properties `aos.swagger.enable` and `aos.swagger.resource-packages` in the axelor-config.properties to enable the API documentation menu in Technical maintenance.
        </details>
* Mobile settings
New module to configure the new [Axelor Open Mobile](https://github.com/axelor/axelor-mobile)
* TracebackService: automatically use tracebackservice on controller exceptions
Now, for every controller methods in AOS packages ending with `web`, any
exception will create a traceback.

#### Changes

* Supplychain module: remove bank-payment dependency
* AxelorMessageException: Moved from Message module to Base
* Add order to all menus
    * Add a gap of 100 between menus
    * Negative value for root menus and positive for others
* Stock: reworked all menus in stock module menus
* Account: rework accounting move form view to optimize responsiveness.
* CRM: App configurations are not required anymore.
closedWinOpportunityStatus, closedLostOpportunityStatus, salesPropositionStatus can now be left empty in the configuration. If the related features are used, a message will be shown to inform the user that the configuration must be made.
* Change several dates to dateTime
    * axelor-base: Period
    * axelor-purchase: PurchaseOrder
    * axelor-account: Invoice, InvoiceTerm, Reconcile, ReconcileGroup, ClosureAssistantLine
    * axelor-bank-payment: BankReconciliation
    * axelor-stock: Inventory
    * axelor-production: UnitCostCalculation
    * axelor-human-resources: Timesheet, PayrollPreparation, LeaveRequest, Expense, LunchVoucherMgt
    * axelor-contract: ContractVersion
* Date format:
    * Add a new locale per company
    * Use company locale and/or user locale instead of hard coded date format
* Business project: exculdeTaskInvoicing renamed to excludeTaskInvoicing.
* Template: Add 'help' tab for mail templates.
* New french admin-fr user in demo data
* Add tracking in different forms (app configurations, ebics, etc...)

#### Removed

* Removed deprecated IException interfaces (replaced by new ExceptionMessage java classes)
* Removed all translations present in source code except english and french.
* Removed axelor-project-dms module.
* Removed axelor-mobile module (replaced by axelor-mobile-settings).
* Removed Querie model.
* SaleOrder: removed following unused fields:
    * `invoicedFirstDate`
    * `nextInvPeriodStartDate`
* PaymentSession: removed cancellationDate field.
* Account: removed unused configuration for ventilated invoices cancelation.

#### Fixed

* Password : passwords fields are now encrypted
    <details>
        Concerned models : Ebics User, Calendar and Partner.
        You can now encrypt old fields by using this task :
        `gradlew database --encrypt`
    </details>


[7.0.3]: https://github.com/axelor/axelor-open-suite/compare/v7.0.2...v7.0.3
[7.0.2]: https://github.com/axelor/axelor-open-suite/compare/v7.0.1...v7.0.2
[7.0.1]: https://github.com/axelor/axelor-open-suite/compare/v7.0.0...v7.0.1
[7.0.0]: https://github.com/axelor/axelor-open-suite/compare/v6.5.7...v7.0.0
