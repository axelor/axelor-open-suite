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


[7.0.0]: https://github.com/axelor/axelor-open-suite/compare/v6.5.7...v7.0.0
