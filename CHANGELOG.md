## [9.2.0] (2026-10-30)

### Features

#### Base
* Contact: allowed a contact to be linked to several companies, each with its own email, phones, function and roles, one of them being the main company.
* Partner: allowed fetching company data from the Sirene API by SIREN number in addition to SIRET, using the head office information.

#### Account
* Loans: added loan management:
  * loan configuration and loans with their schedule, amortization engine (3 modes) and bidirectional simulation;
  * installment booking generating the accounting entry, with automatic loan lifecycle transitions and a batch to book installments up to a date;
  * year-end closing batch generating accrued-interest and prepaid-insurance entries, reversed on the first day of the next period;
  * installments editable directly with recomputation of the following ones, and deferral of installments (also negotiable in draft);
  * totals panel and consistency check between the outstanding capital and the borrowing debt account (164).
* Account configuration: added a limit on the number of automatic reconciliations when using excess or advance payments, to avoid slowness on mass reconciliation.
* Supplier invoice: added the original invoice document, required for validation depending on the accounting configuration and digitally signed when a certificate is configured.
* Invoices, orders, opportunities, contracts and global budgets: added a summary bar on grid views showing the totals of the selected records and of all filtered records.
* Move line: added a button to open the related invoice from the move line list and form, shown only when the move comes from an invoice.
* Invoice: added a button next to the accounting move field to open the move directly.
* Payment mode: allowed grouping the bank deposit moves of value for collection payments by payment voucher, by partner or globally to ease bank reconciliation.
* Analytic distribution: filtered the selectable analytic distribution templates according to the analytic rules of the line accounting account.

#### Bank Payment
* Bank reconciliation: allowed reconciling a debit and a credit accounting entry together from the entries to reconcile, and displaying the entries already reconciled on the period.
* Payment voucher: confirming a payment voucher with a payment mode generating bank orders now creates its bank order, cancelled with the voucher.
* Bill of exchange batch: allowed processing customer invoice terms, for the accounting, the billing and the bank order generation steps.
* Bank reconciliation: allowed reconciling several bank statement lines with a single move line, fully or partially, with a reconciliation number.

#### Human Resource
* Expense: added an analytic accounting panel listing the analytic lines of the expense, and recomputed the analytic distribution when the template changes.
* Leave request: added an API endpoint to return a refused or canceled leave request to draft, with the same status check as the form button.
* Expense line: the analytic distribution template now comes from the employee, or from the product when the employee has none, and is displayed on the line.

#### Mobile Settings
* Mobile settings: added the configuration of the Team application for the mobile app.

#### Purchase
* Purchase order: added supplier reminders by email for overdue purchase order lines, sent from the order or its lines, or automatically by a new purchase batch.

#### Sale
* Sale order line: allowed entering the product from the customer catalog reference, which is kept on the generated stock move and invoice lines and printed on their documents.
* Sale order: added the eco-tax on sale order lines, filled from the product, printed on the sale order and copied to the invoice lines.

#### Supply Chain
* Supplier dispute: added the management of supplier disputes, opened from receptions, invoices, contracts or quality improvements, followed through a workflow and measured in the supplier score.
* Purchase order line: added the tracking of supplier acknowledgements of receipt, whose confirmed dates and quantities feed the estimated receipt date and the cash forecast.
* Supplier: added a supplier score computed from on-time delivery, reception conformity and open quality improvements, with a periodic history batch.
* Invoice: added a panel listing the originating sale or purchase orders, each of which can be opened in a new tab.
* Purchase order: timetables now track their invoiced amount, are marked invoiced only once fully invoiced on ventilation, and warn when the remaining amount is negative.

#### Stock
* Supplier arrival: added a supplier delivery date on the reception, used to compute the supplier on-time delivery rate.
* Product: clicking a point of the stock chart now opens the stock move lines of that location and date.

#### Production
* Cost sheet: consumed components can now be valued at their average price on the consumption date in real cost sheets.
* Manufacturing order: added a real cost panel detailing material, labor, machine and subcontracting costs compared with the theoretical cost.
* Manufacturing order: added a configurable final control conformity check when finishing an order and the printing of the conformity declaration; non-compliant receptions can be sent to quarantine.

#### Quality
* Supplier score: added a gravity-weighted non-conformity rate, a quality score and its trend, and required the gravity to close a quality improvement linked to a supplier.
* Control entry: pre-filled the quality improvement created from a control entry, warned when one is already open and listed the linked quality improvements.
* Control entry: added a button to create a control entry from a manufacturing operation and a stock move line, opening the existing draft or in-progress entries instead when there are some.
* Quality API: added an endpoint to save the measured values of a control entry sample line and check its conformity.

#### Contract
* Contract: added 'Start of pack' and 'End of pack' lines to group contract lines, with optional pack sub-total and hidden unit amounts, carried over to invoices and orders.
* Contract: added a Customer amendments menu listing customer contract versions, with a button to set the selected draft amendments to waiting.

#### Budget
* Budget: allowed imputing a budget on the child accounts of its accounts, through the budget key.

#### Project
* Project task: added a batch notifying assignees of their overdue and upcoming tasks, with optional reminders, run daily by default.

### Changes

#### Base
* Product: moved the characteristics to the Quality panel.
* App check: a module no longer changes the behaviour of the modules it extends when its own app is not installed, and a build check enforces it.
* App check: the app checks no longer query the database on each call.
* Duplicate objects: other modules can now add their own processing when merging duplicate records.
* Address: the town name is now the mandatory field of address templates and is printed in the formatted address, the city being optional.
* Base app configuration: rearranged the fields into themed sections (general, partner, addresses, decimals/units, sales, products, emails, technical maintenance).
* Menus: removed the intermediate menus containing a single entry for territories, LCR bank orders and the supplier map.

#### Account
* Invoice ventilation: other modules can now add their own processing after an invoice is ventilated.
* Accounting report: merged the standard and custom accounting report menus into one, the companies field now handling both single and multi-company reports.
* Payment voucher: cancelling a payment voucher now removes it from its non-validated deposit slip and recomputes the slip totals, and is blocked once the slip is validated.
* Cheque deposit slip: added the payment reference and page numbers to the printing, removed the currency column and renamed the payer column to bank name.
* Accounting export: separated export types from report types, the export types menu now only offering the export formats.
* Account: displayed a warning when disabling 'Use for partner balance' on an account whose move lines already have invoice terms.
* Move line: displayed an error pop-up explaining why the reconciliation of the selected move lines failed instead of a generic message.
* Invoice merge: allowed merging draft and validated invoices and credit notes, and rejected ventilated ones with an explicit message listing them.
* Move line: moved the button opening the move next to the move field of the move line form, and removed it when the line is opened from its move.
* Invoice term: renamed the 'Terms list' menu to 'Invoice terms'.

#### Bank Payment
* Bank order: the 'Payment voucher' functional origin is now displayed on bank orders generated from a payment voucher.
* Bank reconciliation: added help texts explaining each balance shown at the bottom of the bank reconciliation form.
* Bank statement: added the totals of debit, credit and amount remaining to reconcile in the bank statement lines list.

#### Human Resource
* Project: improved the performance of saving a project with a large number of planning times.
* Employee: reworked the form layout, replacing the side panel with a General information tab grouping photo, identity, contact and personal details.

#### Purchase
* Purchase request: the supplier is now asked in the generation wizard when missing, and requests become purchased only once their purchase order is validated.

#### Sale
* Sale order: reorganized the delivery tab fields into functional groups.

#### Supply Chain
* Sale configuration: the order blocked message is now managed from the sale configuration instead of the company form.
* Sale order: timetable invoicing now invoices only the remaining amount, blocks exceeding the order total, and allows re-invoicing once the previous invoice is ventilated or canceled.

#### Stock
* Inventory: added the search toolbar on the inventory lines dashlet.

#### Production
* Manufacturing order: improved the form loading performance.
* Manufacturing order: the parent product is now displayed in the multi-level planning for the orders of the first level.
* Manufacturing order: simplified multi-level planning into a single popup listing the sub-orders to plan, and grouped multi-level fields in a dedicated panel.
* Production app configuration: rearranged the fields into tabs, with company configurations shown separately.

#### Quality
* Control type: the values controlled on a control plan line are now configured on the control type, with a typed field list, instead of custom fields.
* Control type field: prevented the deletion of a control type field still used by a control type, with an error listing the control types concerned.

#### Project
* Project: the functional blocks of the project save can now be overridden separately.
* Project task: improved the performance of progress updates on large task trees.

### Fixes

#### Base
* Company: removed the 255-character limit on the legal information.
* Batch: fixed the slowness when opening, saving or copying a batch configuration having a large number of executions.

#### Account
* Invoice: fixed ventilation failing on the invoice printing because of a wrong type on the legal information column.
* Analytic move line query: fixed the reverse of all filtered analytic move lines using too much memory or never ending on large volumes.
* Move line: fixed excessive memory use when computing the debit, credit and remaining amount totals of the move line list.

#### Bank Payment
* Bank reconciliation: fixed 'Reconcile to move line' applying the amounts while the bank reconciliation is still in draft.
* Bill of exchange batch: fixed the SQL error of the customer invoice terms preview and generation when the batch has a due date.
* Payment voucher: fixed invoice terms still proposed in a new payment voucher while their payment is pending on a bank order with deferred accounting, which allowed to pay them twice.

#### Human Resource
* Employee: fixed the display of the main employment contract, now shown as a summary of pay company, executive status, employment and department.

#### Supply Chain
* Sale order: fixed the invoicing wizard offering a timetable already invoiced by an invoice not ventilated yet, which led to invoice it twice.
* Timetable: fixed a ventilated refund being linked to the next timetable, which was then shown as invoiced and could no longer be invoiced for its full amount.
* MRP: fixed the forecasts of products excluded from MRP, directly or by their category or family, being taken into account.
* Internal stock move: fixed service products being selectable on internal stock move lines.

#### Budget
* Global budget: fixed archiving a global budget so that it is really archived and no longer listed with active budgets.

### Developer

#### Base

New `checkAppOverrideGuard` gradle task, to be run with `./gradlew checkAppOverrideGuard`.

It reports every Guice binding that overrides a service, a controller or a repository of a
lower module when the overriding implementation contains no `isApp(...)` call. Such an override
applies as soon as the module is deployed, even when its app is not installed.

Guard the overriding methods and delegate to `super` when the app is off:

```java
@Override
public SaleOrder computeSaleOrder(SaleOrder saleOrder) throws AxelorException {
  if (!appSupplychainService.isApp("supplychain")) {
    return super.computeSaleOrder(saleOrder);
  }
  // Supply Chain specific behaviour
  return saleOrder;
}
```

Legitimate overrides must be listed in `config/app-override-guard-allowlist.txt` with a
justification. That file also carries the 160 overrides that predate the check, grouped by
module and removed one module at a time.

The task only looks for a single `isApp(...)` occurrence per class, so it does not catch a new
unguarded override added to an already guarded class. See CONTRIBUTING.md for the full rule.

---

The 16 remaining cross-module overrides of `axelor-sale`, `axelor-crm`, `axelor-account`,
`axelor-stock` and `axelor-talent` now guard their behaviour on their own app and delegate to
`super` when it is off. The impact is wide because the overridden classes are axelor-base
services used by every module downstream
(`PricingGenericServiceImpl`, `PricingMetaServiceImpl`, `ProductBaseRepository`,
`ICalendarService`, `PartnerGenerateServiceImpl`, `DuplicateObjectsService`,
`AccountManagementServiceImpl`, `PeriodServiceImpl`, `MailAccountServiceBaseImpl`) plus two
axelor-crm classes overridden by axelor-sale.

Guards: `isApp("sale")` on the five sale overrides, `isApp("crm")` on the three crm ones,
`isApp("account")` on `AccountManagementServiceAccountImpl` and `PeriodServiceAccountImpl`,
`isApp("stock")` on the two stock repositories, and `isApp("recruitment")` on
`MailAccountServiceTalentImpl` (axelor-talent declares `training`, `appraisal` and
`recruitment`, and that override creates job applications from incoming mails). The existing
`appRecruitment.getApp().getActive()` condition inside that method is left untouched.

Three classes needed no guard and are now listed in
`config/app-override-guard-allowlist.txt` as justified exceptions, each bound in place of a
base class but overriding none of its methods: `ProductCategoryServiceSaleImpl`,
`MessageServiceCrmImpl` (its `createMessage(Event)` is a crm overload, the parent's
`createMessage` has another signature) and `FiscalPositionAccountServiceImpl`.

The check is done with the app service already injected in the class when there is one
(`AppBaseService`, `AppCrmService`), otherwise it comes in through the existing constructor,
or with `Beans.get(AppBaseService.class)` in the three classes that have no constructor.

The following classes get a new constructor parameter, which breaks subclasses calling it:

- `com.axelor.apps.sale.service.PricingGroupSaleServiceImpl`
- `com.axelor.apps.account.service.AccountManagementServiceAccountImpl`
- `com.axelor.apps.account.service.period.PeriodServiceAccountImpl`

Their subclasses in axelor-supplychain and axelor-contract are updated accordingly
(`PricingGroupSupplyChainServiceImpl`, `PricingGroupContractServiceImpl`,
`AccountManagementContractServiceImpl`).

---

`AppBaseServiceImpl.isApp` now serves the app active flag from an in-memory cache
(`appActiveCache`, built with `CacheBuilder`, isolated per tenant when multi tenancy is
enabled) instead of loading the app record. The cache is cleared after commit by the new
`com.axelor.apps.base.db.repo.AppListener` entity listener, declared on `App` in
`axelor-base`.

Two consequences on the semantics of the check:

- The flag is read without flushing the persistence context, so an app installed or
  uninstalled in the current transaction is only seen by `isApp` once that transaction is
  committed. Code that installs an app and then relies on `isApp` in the same transaction
  must commit first.
- Only the `AppBaseService` / `ScriptAppService` path is cached. `AppService.isApp`
  (`axelor-studio`) still loads the app record, so a project overriding `AppService.isApp` no
  longer changes the result of the AOS app checks. Override
  `AppBaseServiceImpl.isApp` instead.

The app checks of the following classes now go through `AppBaseService` instead of
`AppService`. Their constructor signature changes, which breaks subclasses calling it:

- `com.axelor.apps.account.db.repo.PartnerAccountRepository`
- `com.axelor.apps.budget.service.move.MoveReverseServiceBudgetImpl`
- `com.axelor.apps.portal.service.ClientViewServiceImpl`
- `com.axelor.apps.hr.db.repo.PartnerHRRepository`
- `com.axelor.apps.hr.service.expense.ExpenseMoveReverseServiceImpl`
- `com.axelor.apps.supplychain.service.saleorder.SaleOrderCopySupplychainServiceImpl`

Among them, the following also rename their injected field from `appService` to
`appBaseService`, which breaks subclasses using it:

- `com.axelor.apps.account.db.repo.PartnerAccountRepository`
- `com.axelor.apps.portal.service.ClientViewServiceImpl`
- `com.axelor.apps.hr.service.expense.ExpenseMoveReverseServiceImpl`
- `com.axelor.apps.supplychain.service.saleorder.SaleOrderCopySupplychainServiceImpl`

---

The CRM override of the duplicate merge has been replaced by an event.

`DuplicateObjectsService#removeDuplicate` now fires the `com.axelor.apps.base.service.event.DuplicateObjectsBeforeRemove` event
(original object, duplicate objects, model name) before relations are reassigned and duplicates are removed.
Observers run synchronously, in the same transaction.

`DuplicateObjectsCrmService` has been removed and is replaced by `com.axelor.apps.crm.service.observer.DuplicateObjectsCrmObserver` (priority 10).
Modules that extended `DuplicateObjectsCrmService` must now observe `DuplicateObjectsBeforeRemove` instead of overriding the service.

---

- Init data address templates updated (only on new databases): `townName` required, `city` optional with title "City/Town". To fill the town name of existing addresses:
  `UPDATE base_address a SET town_name = c.name FROM base_city c WHERE a.city = c.id AND (a.town_name IS NULL OR a.town_name = '');`

---

- `Company.legalInformation` is now a large text field: `ALTER TABLE base_company ALTER COLUMN legal_information TYPE text;`

---

- New entity `PartnerContactLink` (table `base_partner_contact_link`) and new field `Partner.partnerContactLinkList` (mappedBy `contact`).
- Saving a contact now aligns the companies' contact lists with its active links: existing contacts must get their links, otherwise they are removed from the contact list of companies other than their main company on next save:
  ```sql
  INSERT INTO base_partner_contact_link (id, version, partner, contact, is_main_company, is_active, email_address, fixed_phone, job_title_function, function_business_card)
  SELECT nextval('base_partner_contact_link_seq'), 0, l.company, c.id, l.company = c.main_partner, true,
    CASE WHEN l.company = c.main_partner THEN c.email_address END, CASE WHEN l.company = c.main_partner THEN c.fixed_phone END,
    CASE WHEN l.company = c.main_partner THEN c.job_title_function END, CASE WHEN l.company = c.main_partner THEN c.function_business_card END
  FROM (SELECT base_partner AS company, contact_partner_set AS contact FROM base_partner_contact_partner_set
        UNION SELECT main_partner, id FROM base_partner WHERE is_contact AND main_partner IS NOT NULL) l
  JOIN base_partner c ON c.id = l.contact;
  ```
- Contact save now rejects a contact whose full name exactly matches another contact.
- New service `PartnerContactLinkService` / `PartnerContactLinkServiceImpl`.
- Constructors changed (new `PartnerContactLinkService` param): `PartnerServiceImpl`, `PartnerSaleServiceImpl`, `PartnerSupplychainServiceImpl`, `PartnerConvertServiceImpl`, `PartnerConvertHRServiceImpl`, `PartnerCreationServiceImpl`, `PartnerCreationServiceSupplychainImpl`.
- `PartnerConvertService.convertToIndividualPartner` now throws `AxelorException`.
- `partner-contact-form`: `mainPartner` is now read-only; action `action-partner-method-add-contact-partner` removed from its `onSave`; `mainPartner` no longer set by `action-partner-contact-record-default` / `action-partner-attrs-partner-seq-editable`.

---

- Removed field `batchList` from `AccountingBatch`, `InvoiceBatch`, `BaseBatch`, `ImportBatch`, `MailBatch`, `ContractBatch`, `CrmBatch`, `HrBatch`, `InterventionBatch`, `ProductionBatch`, `ProjectBatch`, `SaleBatch`, `StockBatch`, `SupplychainBatch` (mappedBy, no DB change): query `Batch` by its batch config instead.
- Removed classes: `AccountingBatchAccountRepository`, `InvoiceBatchAccountRepository`, `BaseBatchBaseRepository`, `MailBatchBaseRepository`, `ContractBatchContractRepository`, `CrmBatchCrmRepository`, `HrBatchHRRepository`, `ProductionBatchManagementRepository`, `SaleBatchSaleRepository`, `SupplychainBatchSupplychainRepository`.

---

- `PartnerApiFetchService.fetch(String)` -> `fetch(String identifier, boolean isSirenSearch)`.
- `PartnerGenerateService.configurePartner(Partner, String, Map)` -> `configurePartner(Partner, String identifier, boolean isSirenSearch, Map)`.
- `GenericApiFetchService`: `fetch`, `getUrl`, `treatResponse`, `getData`, `getApiSireneData` take an extra `boolean isSirenSearch` parameter.
- Action `action-api-sirene-configuration-record-set-parent-siret` renamed to `action-api-sirene-configuration-record-set-parent-siret-siren-numbers`.
- New selection `partner.sirene.api.search.type.select`.

---

- Removed menus `admin-root-organisation` (base), `account-root-bank-order-lcr` (bank-payment) and `purchase-report-maps` (purchase).
- `admin-root-organisation-territory` now has parent `admin-root`, `account-root-bank-order-lcr-bank-orders` now has parent `account-root-bank-orders`, `purchase-maps-partner-suppliers` now has parent `sc-root-purchase-report`.

#### Account

The supplychain and contract overrides of `afterVentilation` have been replaced by an event.

`WorkflowVentilationServiceImpl#afterVentilation` now fires the `com.axelor.apps.account.service.event.InvoiceAfterVentilation` event
at the end of its processing. Observers run synchronously, in the same transaction; an `AxelorException` thrown by an observer
is propagated as is and rolls back the ventilation.
The constructor of `WorkflowVentilationServiceImpl` takes a new `Event<InvoiceAfterVentilation>` parameter.

- `WorkflowVentilationServiceSupplychainImpl` has been removed. Its processing has been moved to the new
  `InvoiceVentilationSupplychainService` / `InvoiceVentilationSupplychainServiceImpl`, called by
  `InvoiceVentilationSupplychainObserver` (priority 10). The helper methods (`saleOrderProcess`, `purchaseOrderProcess`,
  `stockMoveProcess`...) are kept with the same signatures in `InvoiceVentilationSupplychainServiceImpl`.
- `WorkflowVentilationContractServiceImpl` has been removed and is replaced by `InvoiceVentilationContractObserver` (priority 20).

Modules that extended `WorkflowVentilationServiceSupplychainImpl` or `WorkflowVentilationContractServiceImpl` to override
`afterVentilation` must now observe `InvoiceAfterVentilation` instead.

---

Added `AccountConfig.excessPaymentReconcileNumberLimit` (integer, default 50). When set,
`PaymentServiceImpl.useExcessPaymentOnMoveLines` stops after that many reconciliations and
records a message traceback. `PaymentServiceImpl` constructor gained an `AccountConfigService`
parameter.

```sql
UPDATE account_account_config SET excess_payment_reconcile_number_limit = 50 WHERE excess_payment_reconcile_number_limit IS NULL;
```

---

- Removed menu `account-root-accounting-custom-accounting-reports` and action-view `account.root.accounting.custom.accounting.reports`; the `_isCustom` context is no longer used by `accounting-report-form`.
- `AccountingReport.company` is now hidden and synchronized from `companySet` (set when exactly one company is selected).
- `AccountingReportService.resolveReportTypeForCompany(AccountingReport, boolean)` -> `resolveReportTypeForCompany(AccountingReport)`.
- `AccountingReportToolService.getAccountingReportTypeIds(AccountingReport, boolean)` -> `getAccountingReportTypeIds(AccountingReport)`.

---

- New interface methods: `DepositSlipService.removePaymentVoucher(DepositSlip, PaymentVoucher)`, `DepositSlipService.computeTotals(DepositSlip)`, `MoveReverseService.checkBeforeReverse(Move)`, `PaymentVoucherCancelService.checkDepositSlip(PaymentVoucher)`.
- `InvoicePaymentAlertService.validateBeforeReverse(InvoicePayment)` now throws `AxelorException`.
- Constructors: `PaymentVoucherCancelServiceImpl` (new, `DepositSlipService`), `PaymentVoucherCancelServiceBankPayImpl` (added `DepositSlipService`), `InvoicePaymentAlertServiceImpl` and `InvoicePaymentAlertServiceBankPayImpl` (new, `MoveReverseService`).
- Action `action-deposit-slip-record-total-amount-cheque-count` removed, replaced by `action-deposit-slip-method-compute-totals`.

---

- Options 1000, 1001, 1010 and 1011 removed from selection `accounting.report.type.select` (kept in `accounting.report.type.select.export`).
- New views `accounting-report-export-type-grid` and `accounting-report-export-type-form`, used by action `account.conf.reporting.accounting.report.types.export`.
- `AccountingReportType` imports now check `typeSelect` with `com.axelor.csv.script.ImportAccountingReportType.checkTypeSelect(reportExportTypeSelect, typeSelect)`.

---

- New fields: `AccountConfig.originalInvoiceRequiredOnSupplierInvoice`, `Invoice.isPrintedPDFDigitallySigned`.
- Existing account configs keep the requirement disabled; to apply the default (required):
  `UPDATE account_account_config SET original_invoice_required_on_supplier_invoice = true;`
- New service `InvoiceSupplierDocumentService` / `InvoiceSupplierDocumentServiceImpl`, called from `InvoiceManagementRepository.save`.

---

- `MoveLineService.reconcileMoveLinesWithCacheManagement` and `PaymentService.useExcessPaymentOnMoveLinesDontThrow` now return `List<String>` (error messages) instead of `int`; same for protected `PaymentServiceImpl.useExcessPaymentOnMoveLines`.

---

- `InvoiceMergingController.convertSelectedLinesToMergeLines` removed; new `openCustInvoiceMergeWizard` / `openSupplInvoiceMergeWizard`.
- `InvoiceMergingServiceImpl.checkErrors(StringJoiner, InvoiceMergingResult)` -> `checkErrors(StringJoiner, List<Invoice>, InvoiceMergingResult)`.
- New interface method `InvoiceMergingViewService.buildMergeWizardView`.
- Removed actions `action-invoice-method-convert-selected-lines-to-merge-lines`, `action-invoice-mergecustinvoices`, `action-invoice-mergesupplinvoices`; replaced by `action-invoice-method-open-cust-invoice-merge-wizard` / `action-invoice-method-open-suppl-invoice-merge-wizard`.

---

- New method `AnalyticMoveLineQueryService.reverseAll(AnalyticMoveLineQuery)` returning the new record `AnalyticMoveLineQueryService.AnalyticMoveLineReverseResult`.
- `AnalyticMoveLineQueryServiceImpl.getAnalyticMoveLineQuery` now returns a filter with `EXISTS` sub-queries on `self.moveLine` instead of a fixed list of move line ids.
- New protected method `AnalyticMoveLineQueryController.setReverseResponse`.

---

- New service `MoveLineSummaryService` (`MoveLineSummaryServiceImpl`) used by `MoveLineController.fetchSummary`.

---

- Toolbar button `viewMoveBtn` removed from `move-line-form`, replaced by the button `showMoveButton` in its `movePanel` side panel.

---

- New field `PaymentMode.valueForCollectionGroupingSelect` (selection `payment.mode.value.for.collection.grouping.select`), required on value for collection payment modes:
  ```sql
  UPDATE account_payment_mode SET value_for_collection_grouping_select = 1 WHERE value_for_collection_grouping_select IS NULL OR value_for_collection_grouping_select = 0;
  ```
- New method `PaymentVoucherConfirmService.valueForCollectionMoveToGeneratedMove(List<PaymentVoucher>, LocalDate)`, now called by `DepositSlipServiceImpl` instead of the per-voucher method.

---

- Constructor of `AnalyticAttrsServiceImpl` takes the new parameters `AccountService` and `AccountManagementAccountService`.
- New interface method `AnalyticAttrsService.getAnalyticDistributionTemplateDomainFromProduct`.
- Removed action `action-contract-line-attrs-set-analytic-distribution-template-domain`, replaced by `action-contract-line-method-set-domain-analytic-distribution-template`.

#### Bank Payment

The 25 remaining overrides of `axelor-account` classes now guard their bank payment specific
behaviour on `isApp("bank-payment")` and delegate to `super` when the app is off. These
overrides included classes of the accounting core: `MoveCreateServiceImpl`,
`MoveReverseServiceImpl`, `InvoiceTermServiceImpl`, `PaymentSessionValidateServiceImpl`,
`PaymentSessionBillOfExchangeValidateServiceImpl`, the credit transfer batches and the
`InvoicePaymentController`, so a company doing accounting without the Bank Payment app was
still running bank payment code.

The check is done with the app service already injected in the class when there is one
(`AppBankPaymentService`, `AppAccountService`, `AppBaseService`), otherwise `AppBaseService`
comes in through the existing constructor. The classes with no constructor read it with
`Beans.get(AppBaseService.class)`.

`PaymentScheduleLineBankPaymentServiceImpl` needed no guard and is now listed in
`config/app-override-guard-allowlist.txt` as a justified exception: it is bound on
`PaymentScheduleLineServiceImpl` (axelor-account) but overrides none of its methods, it only
adds the rejection methods of `PaymentScheduleLineBankPaymentService`.

The following classes get a new constructor parameter, which breaks subclasses calling it:

- `com.axelor.apps.bankpayment.service.invoice.payment.InvoicePaymentValidateServiceBankPayImpl`
- `com.axelor.apps.bankpayment.service.InvoiceTermFilterBankPaymentServiceImpl`
- `com.axelor.apps.bankpayment.service.moveline.MoveLineGroupBankPaymentServiceImpl`
- `com.axelor.apps.bankpayment.service.move.MoveGroupOnChangeServiceBankPaymentImpl`
- `com.axelor.apps.bankpayment.service.moveline.MoveLineMassEntryRecordBankPaymentServiceImpl`
- `com.axelor.apps.bankpayment.service.move.MoveReverseServiceBankPaymentImpl`
- `com.axelor.apps.bankpayment.service.PaymentSessionBillOfExchangeValidateBankPaymentServiceImpl`

`MoveReverseServiceBankPaymentImpl` now holds the `appBaseService` field, so
`com.axelor.apps.hr.service.expense.ExpenseMoveReverseServiceImpl` no longer declares its own
and passes the service to `super` instead.

---

Added `InvoiceTermPaymentService` as parameter for `PaymentVoucherConfirmServiceBankPayImpl`'s constructor.

---

- New fields `MoveLine.moveLineBankReconcileNbr` and `MoveLine.bankReconcileNbr` (stored, shown instead of `postedNbr` in the bank reconciliation move line grids). To fill it for existing lines:
  `UPDATE account_move_line SET bank_reconcile_nbr = posted_nbr WHERE posted_nbr IS NOT NULL AND bank_reconcile_nbr IS NULL;`
- New transient field `BankReconciliation.showAlreadyBankReconciledOnPeriodMoveLines`.
- `BankReconciliationReconciliationServiceImpl` constructor: added `SequenceService`, `BankReconciliationRepository`, `MoveLinePostedNbrService`.
- New interface methods: `BankReconciliationQueryService.getRequestMoveLines(boolean)`, `BankReconciliationReconciliationService.unreconcileMoveLines(List<MoveLine>)`, `MoveLinePostedNbrService.updateBankReconcileNbr(MoveLine)`.
- Removed button `showReconciledMoveLineBtn` and action `action-bank-reconciliation-view-show-reconciled-move-lines` from `bank-reconciliation-form`.

---

- New field `PaymentVoucher.bankOrder`; new bank order functional origin 60 (payment voucher).
- `PaymentVoucherCancelService.cancelPaymentVoucher(PaymentVoucher)` now throws `AxelorException`.
- `PaymentVoucherConfirmService` now bound to `PaymentVoucherConfirmServiceBankPayImpl` and `PaymentVoucherCancelServiceImpl` to `PaymentVoucherCancelServiceBankPayImpl` in bank-payment.

---

- In `bank-reconciliation-form`, the labels `alreadyPointedLabel`, `notPointedLabel`, `balanceLabel`, `selectedLabel` and `newBankBalanceLabel` are replaced by the buttons `alreadyPointedBtn`, `notPointedBtn`, `balanceBtn`, `selectedBtn` and `newBankBalanceBtn`.

---

- New fields `InvoiceTerm.lcrAccounted` and `InvoiceTerm.batchSet`.
- New option `4` (Customer invoice terms) in the bill of exchange data type selection of `AccountingBatch` (`AccountingBatchRepository.BILL_OF_EXCHANGE_DATA_CUSTOMER_INVOICE_TERM`).
- New interface `InvoicePaymentCreateServiceBankPay`, now implemented by `InvoicePaymentCreateServiceBankPayImpl`.
- New service `BillOfExchangeInvoiceTermQueryService` and batch classes `BatchBillOfExchangeInvoiceTerm`, `BatchBillOfExchangeSendBillingInvoiceTerm`, `BatchBankOrderGenerationBillOfExchangeInvoiceTerm`.

---

- New field `BankReconciliationLine.reconcileNumber`.
- New sequence type `bankReconciliation` (`SequenceRepository.BANK_RECONCILIATION`): only created for new databases, a "Bank reconciliation" sequence must be created for each company of an existing database.
- Constructor of `BankReconciliationValidateService` takes the new parameters `BankReconciliationSelectedLineComputationService` and `SequenceService`.
- Constructor of `BankReconciliationSelectedLineComputationServiceImpl` takes the new parameter `BankReconciliationLineRepository`.
- New interface methods `BankReconciliationLineService.checkReconcileToMoveLine`, `reconcileBRLToMoveLine`, and `BankReconciliationSelectedLineComputationService.getMoveLineRemainingAmount`, `computeSelectedBankReconciliationLinesTotal`, `computeBankReconciliationLinesTotal`.

#### Human Resource

The 14 remaining cross-module overrides of `axelor-human-resource` now guard their HR specific
behaviour on `isApp(...)` and delegate to `super` when the app is off. The module declares
four apps (`employee`, `expense`, `timesheet`, `leave`) where `expense`, `timesheet` and
`leave` all depend on `employee`, so the guard uses the app that owns the added behaviour:

- `isApp("expense")` for the overrides of `axelor-account` and `axelor-bank-payment` that
  branch on `Move.expense` / `Expense`: `BatchCreditTransferExpensePaymentHR`,
  `BankOrderLineOriginServiceHRImpl`, `MoveValidateHRServiceImpl`, `MoveLineTaxHRServiceImpl`,
  `ReconcileCheckServiceHRImpl`, `AnalyticMoveLineGenerateRealServiceHrImpl`.
- `isApp("employee")` for the overrides driven by the employee link: `UserHRRepository`,
  `PartnerHRRepository`, `PartnerConvertHRServiceImpl`, and the three overrides of
  `axelor-project` (`ProjectDashboardHRServiceImpl`, `ProjectActivityDashboardServiceHRImpl`,
  `TaskTemplateHrServiceImpl`), consistent with `ProjectHRRepository` and
  `ProjectTaskHRRepository` which already guard on that app.

The check is done with the app service already injected in the class when there is one
(`AppBaseService`, own or inherited), otherwise it comes in through the existing constructor.
The classes with no constructor read it with `Beans.get(AppBaseService.class)`.

`AccountConfigHRService` and `PublicHolidayHrServiceImpl` needed no guard and are listed in
`config/app-override-guard-allowlist.txt` as justified exceptions: both are bound in place of
a class of a lower module but override none of its methods. `AccountConfigHRService` only adds
the expense account getters, and `PublicHolidayHrServiceImpl.checkPublicHolidayDay(LocalDate,
Employee)` is an overload declared by `PublicHolidayHrService`, not an override of
`PublicHolidayServiceImpl.checkPublicHolidayDay(LocalDate, EventsPlanning)`.

The following classes get a new constructor parameter, which breaks subclasses calling it:

- `com.axelor.apps.hr.service.PartnerConvertHRServiceImpl`
- `com.axelor.apps.hr.service.bankorder.BankOrderLineOriginServiceHRImpl`
- `com.axelor.apps.hr.service.ReconcileCheckServiceHRImpl`

---

- New interface method `ProjectPlanningTimeService.getPlannedHrsByTask(Project)`.

---

- New interface method `ExpenseAnalyticService.refreshAnalyticDistribution(ExpenseLine)`.

---

- New service `LeaveRequestDraftService.draft(LeaveRequest)`, new endpoint `PUT /leave-request/draft/{leaveRequestId}`.
- Action `action-leave-request-record-draft` removed, replaced by `action-leave-request-group-draft`.

---

- New interface methods `ExpenseAnalyticService.computeAnalyticDistributionTemplate(ExpenseLine)` and `applyAnalyticDistribution(ExpenseLine)`.
- `ExpenseLineCreateServiceImpl` constructor: `AppAccountService` removed.
- `ExpenseLineToolServiceImpl` and `ExpenseLineUpdateServiceImpl` constructors: new `ExpenseAnalyticService` parameter.

---

- `employee-form`: panels `contactPanel`, `hrPanel`, `contactInformationPanel`, `medicalVisitDatePanel`, `editContactPanel`, `booleanPanel` and the read-only `imagePanel` removed; new `generalInfoPanel`, `photoPanel`, `identityPanel`, `contactInfoPanel`, `hrInfoPanel`, `personalInfoEditPanel`, `contactsPanel`. Field `webSite` no longer shown in the form.

#### Mobile Settings

- New field `AppMobileSettings.isTeamAppEnabled`.
- New `MobileConfig` sequence `app-team` (selection `mobilesettings.mobile.config.sequence.select`), created by init-data only; existing databases need the record:
  `INSERT INTO mobilesettings_mobile_config (id, version, import_id, sequence, is_app_enabled, is_customize_menu_enabled) VALUES (nextval('mobilesettings_mobile_config_seq'), 0, '13', 'app-team', false, false);`

#### Purchase

- New entity `PurchaseBatch` and field `Batch.purchaseBatch`; new menu `admin-root-batch-purchase`.
- New field `PurchaseConfig.supplierReminderTemplateSet`: email templates to set per company to send reminders.
- New schedule `SupplierReminderJob` (inactive) imported by data-init.
- New method `TemplateMessageServiceBaseImpl.generateMessage(Model, Template, Map<String, Object>)`.

---

- New field `PurchaseRequestLine.purchaseOrder`.
- `PurchaseRequestService.generatePo(...)` and `PurchaseRequestToPoCreateService.createFromRequests(...)`: added a `Partner defaultSupplier` parameter.
- New interface methods: `PurchaseRequestWorkflowService.purchasePurchaseRequestsByPo`, `unlinkPurchaseRequestsFromPo`, `generatePurchaseOrderFromRequest`; `PurchaseOrderLineService.fillPrice(PurchaseOrderLine, PurchaseOrder)`.
- Constructors: `PurchaseOrderWorkflowServiceImpl`, `PurchaseOrderWorkflowServiceSupplychainImpl`, `PurchaseOrderWorkflowBudgetServiceImpl` (added `PurchaseRequestWorkflowService`); `PurchaseRequestWorkflowServiceImpl` (added `PurchaseRequestRepository`).
- `PurchaseRequestToPoCreateServiceImpl`: `createPurchaseOrder(PurchaseRequest, Company)` -> `createPurchaseOrder(PurchaseRequest, Partner, Company)`, `getGroupBySupplierKey(PurchaseRequest)` -> `getGroupBySupplierKey(PurchaseRequest, Partner)`, `generatePoLinesPurchaseRequest` lost its `boolean` parameter, `findLineByProductAndUnit` removed.
- Action `action-purchase-request-set-group-by-supplier` removed (replaced by `action-purchase-request-attrs-supplier`).

#### Sale

- New fields `SaleOrderLine.customerCatalog` (+ transient `customerProductCode`), `InvoiceLine.customerCatalog`, `StockMoveLine.customerCatalog`.
- Sale order, invoice and stock move printings now read the customer reference from these fields; to keep it on existing documents:
  `UPDATE sale_sale_order_line sol SET customer_catalog = (SELECT cc.id FROM sale_customer_catalog cc JOIN sale_sale_order so ON so.id = sol.sale_order WHERE cc.product = sol.product AND cc.customer_partner = so.client_partner ORDER BY cc.id LIMIT 1) WHERE sol.customer_catalog IS NULL;`
  `UPDATE stock_stock_move_line sml SET customer_catalog = sol.customer_catalog FROM sale_sale_order_line sol WHERE sml.sale_order_line = sol.id AND sml.customer_catalog IS NULL;`
  `UPDATE account_invoice_line il SET customer_catalog = sol.customer_catalog FROM sale_sale_order_line sol WHERE il.sale_order_line = sol.id AND il.customer_catalog IS NULL;`
- New service `SaleOrderLineCustomerCatalogService`.
- `SaleOrderOnChangeServiceImpl` and `SaleOrderOnChangeSupplychainServiceImpl` constructors: added `SaleOrderLineCustomerCatalogService`.
- The supplychain `invoice-line-form` is now a real extension (id `supplychain-invoice-line-form`).

---

- New field `AppBase.enableEcoTax`: eco-tax fields on products, sale order lines and invoice lines are now only displayed when it is enabled; set it to true to keep the previous behaviour.
- `Product.defaultEcoTaxAmount` and `Product.defaultEcoTaxMention` moved from axelor-account to axelor-base (same table `base_product`, no SQL needed).
- New fields `SaleOrderLine.ecoTaxAmount` and `SaleOrderLine.ecoTaxMention`.
- Removed view extension `account-product-form` (axelor-account `Product.xml` deleted).
- New interface method `SaleOrderLineProductService.fillEcoTaxInformation(SaleOrderLine)`.

#### Supply Chain

The `orderBloquedMessage` field has been moved from `Company` (owned by `axelor-sale`) to
`SaleConfig` (now owned by `axelor-supplychain`) and renamed to `orderBlockedMessage`. It is
no longer displayed on the company form and is now available on the sale configuration form,
in the customer credit panel.

`AccountingSituationSupplychainServiceImpl.checkExceededUsedCredit` now reads the message
from `company.getSaleConfig().getOrderBlockedMessage()` instead of
`company.getOrderBloquedMessage()`.

This is a destructive database change: the `base_company.order_bloqued_message` column is
dropped and its data is moved to `sale_sale_config.order_blocked_message`. Run the following
migration before dropping the column, and validate it against your data before applying to
production:

```sql
ALTER TABLE sale_sale_config ADD COLUMN IF NOT EXISTS order_blocked_message text;

UPDATE sale_sale_config sc
SET order_blocked_message = c.order_bloqued_message
FROM base_company c
WHERE c.id = sc.company
  AND c.order_bloqued_message IS NOT NULL;

ALTER TABLE base_company DROP COLUMN IF EXISTS order_bloqued_message;
```

---

The 53 remaining cross-module overrides of `axelor-supplychain` now guard their
supplychain specific behaviour on `isApp("supplychain")` and delegate to `super` when the
app is off. The check is done with the app service already injected in the class when
there is one (`AppSupplychainService`, `AppBaseService`, `AppAccountService`,
`AppSaleService`, `AppPurchaseService`), otherwise `AppBaseService` is injected.

Five classes needed no guard and are now listed in
`config/app-override-guard-allowlist.txt` as justified exceptions: they are bound in place
of a class of a lower module but override none of its methods, they only add the methods of
their own supplychain interface (`AdvancePaymentServiceSupplychainImpl`,
`StockLocationUtilsServiceSupplychainImpl`, `AccountingCutOffSupplyChainServiceImpl`,
`PartnerSupplychainServiceImpl`, `PartnerLinkSupplychainServiceImpl`).

The following classes get a new constructor parameter, which breaks subclasses calling
their constructor:

- `com.axelor.apps.supplychain.service.workflow.WorkflowCancelServiceSupplychainImpl`
- `com.axelor.apps.supplychain.service.StockHistoryServiceSupplyChainImpl`
- `com.axelor.apps.supplychain.service.invoice.InvoiceMergingServiceSupplychainImpl`
- `com.axelor.apps.supplychain.service.batch.SaleBatchSupplyChainService`
- `com.axelor.apps.supplychain.service.cartline.CartLineProductSupplychainServiceImpl`
- `com.axelor.apps.supplychain.service.cart.CartResetSupplychainServiceImpl`
- `com.axelor.apps.supplychain.service.ConfiguratorCheckServiceSupplychainImpl`
- `com.axelor.apps.supplychain.service.TrackingNumberCompanySupplychainServiceImpl`
- `com.axelor.apps.supplychain.service.pricing.PricingGroupSupplyChainServiceImpl`
- `com.axelor.apps.supplychain.service.analytic.AnalyticMoveLineParentSupplychainServiceImpl`
- `com.axelor.apps.supplychain.service.PurchaseOrderLineTaxComputeSupplychainServiceImp`
- `com.axelor.apps.supplychain.db.repo.SaleOrderSupplychainRepository`
- `com.axelor.apps.supplychain.db.repo.LogisticalFormSupplychainRepository`

These ones had no constructor and now have one, which breaks subclasses too:

- `com.axelor.apps.supplychain.service.purchaseorderline.view.PurchaseOrderLineViewServiceSupplychainImpl`
- `com.axelor.apps.supplychain.rest.StockProductRestServiceSupplychainImpl`
- `com.axelor.apps.supplychain.service.StockMoveCurrencyServiceSupplychainImpl`

`SaleOrderSupplychainRepository` and `LogisticalFormSupplychainRepository` take the service
through their existing constructor, which breaks their subclasses too. The four
repositories that have no constructor read the check through
`Beans.get(AppBaseService.class)`, the way they already fetch their other collaborators, so
they gain neither a field nor a constructor.

---

- New entity `SupplierDispute` (+ `SupplierDispute.contract` in contract, `SupplierDispute.qualityImprovement` in quality).
- New fields `Partner.supplierDisputeAvgResolutionDays`, `Partner.supplierDisputeFavourableRate`, `Partner.supplierClosedDisputeCount`, same on `SupplierScoreHistory`.
- New fields `PurchaseConfig.supplierDisputeOpeningMessageTemplate` and `PurchaseConfig.supplierDisputeAwaitingResponseMessageTemplate`.
- New sequence type `supplierDispute`: create a sequence per company on existing databases.
- `SupplierScoreServiceImpl` and `SupplierScoreQualityServiceImpl` constructors: added `SupplierDisputeIndicatorService`.

---

- New entity `PurchaseOrderAcknowledgment` and field `PurchaseOrderLine.purchaseOrderAcknowledgmentList`.
- `ForecastRecapServiceImpl` constructor: added `PurchaseOrderAcknowledgmentService`.

---

- New entity `SupplierScoreHistory` (table `supplychain_supplier_score_history`).
- New fields: `Partner.supplierScore`, `Partner.supplierOtdRate`, `Partner.supplierConformityRate`, `Partner.supplierAssessedReceptionCount`, `Partner.supplierScoreComputedOn`, `Partner.supplierScoreHistoryList`, `Partner.supplierOpenQiRate` (quality), `SupplychainBatch.supplierPartnerSet`.
- New app fields: `AppSupplychain.manageSupplierScore`, `supplierScoreRollingMonths`, `supplierScoreOtdWeight`, `supplierScoreConformityWeight`, `autoComputeSupplierOtdRate`, `autoComputeSupplierConformityRate`, and (quality) `supplierScoreQiWeight`, `autoComputeSupplierOpenQiRate`. Existing app rows get no value for the defaulted ones:
  `UPDATE studio_app_supplychain SET supplier_score_rolling_months = 12, supplier_score_otd_weight = 1, supplier_score_conformity_weight = 1, supplier_score_qi_weight = 1, auto_compute_supplier_otd_rate = true, auto_compute_supplier_conformity_rate = true, auto_compute_supplier_open_qi_rate = true;`
- Constructors changed (+ `SupplierScoreService`): `StockMoveServiceSupplychainImpl` and its subclasses, `QualityImprovementManagementRepository`.

---

- New interface methods `InvoiceServiceSupplychain.getSaleOrderIds(Invoice)` and `getPurchaseOrderIds(Invoice)`.

---

- `SaleOrderInvoiceServiceImpl`, `SaleOrderInvoiceContractServiceImpl`, `SaleOrderBudgetServiceImpl` constructors: new `TimetableRepository` parameter.

---

- New field `Timetable.invoicedAmount`. Existing invoiced timetables:
  `UPDATE supplychain_timetable SET invoiced_amount = amount WHERE invoiced = true;`
- `TimetableService.deleteInvoiceTimeTable(Invoice)` renamed to `cancelTimetable(Invoice)`; new interface method `updateTimetables(Invoice)`.
- `TimetableServiceImpl` constructor: new `PurchaseOrderInvoiceService` parameter.
- `InvoiceServiceSupplychainImpl` constructor: new `TimetableService` parameter.
- `WorkflowCancelServiceSupplychainImpl` and `WorkflowCancelServiceContractImpl` constructors: new `TimetableService` parameter.

#### Stock

Added the `supplierDeliveryDate` date field to `StockMove`.

```sql
ALTER TABLE stock_stock_move ADD COLUMN supplier_delivery_date date;
```

#### Production

The 29 remaining cross-module overrides of `axelor-production` now guard their production
specific behaviour on `isApp("production")` and delegate to `super` when the app is off.
Among them the five overrides of `axelor-quality` services, which were applying production
behaviour (manufacturing order, operation order, production process line) to the quality
module on a base where Production is not installed.

The check is done with the app service already injected in the class when there is one
(`AppProductionService`, `AppBaseService`, `AppSaleService`, `AppPurchaseService`,
`AppSupplychainService`), otherwise `AppBaseService` comes in through the existing
constructor. The five repositories, which have no constructor, read it with
`Beans.get(AppBaseService.class)` the way they already fetch their other collaborators.

`StockConfigProductionService` needed no guard and is now listed in
`config/app-override-guard-allowlist.txt` as a justified exception: it is bound on
`StockConfigService` (axelor-stock) but overrides none of its methods, it only adds the
production stock location getters.

The following classes get a new constructor parameter, which breaks subclasses calling it:

- `com.axelor.apps.production.service.SaleOrderLineOnSaleSupplyChangeServiceProductionImpl`
- `com.axelor.apps.production.service.TrackingNumberCompanyProductionServiceImpl`
- `com.axelor.apps.production.service.configurator.ConfiguratorCheckServiceProductionImpl`
- `com.axelor.apps.production.service.SaleOrderLineComputeQtyProductionServiceImpl`
- `com.axelor.apps.production.service.QualityImprovementUpdateProductionServiceImpl`
- `com.axelor.apps.production.rest.QualityImprovementParseProductionServiceImpl`
- `com.axelor.apps.production.service.ControlEntryProductionServiceImpl`

---

A new `ConsumedStockMoveLineValuationService` values consumed stock move lines at the WAP
price recorded when they were realized, or at their company unit price if none was recorded.
It is used by both the real cost sheet and the manufacturing order material cost.

The constructors of the following classes have a new `ConsumedStockMoveLineValuationService`
parameter:
- `CostSheetServiceImpl`
- `ManufOrderCostServiceImpl`

---

- New service `ManufOrderCostService`.

---

Final control check, quarantine and automatic quality improvements on non-compliant receptions:

- New fields: `QualityConfig.manufOrderFinalControlCheckSelect`, `QualityConfig.manufOrderConformityDeclarationPrintTemplate`, `QualityConfig.quarantineStockLocation`, `QualityConfig.receptionQiDetection`, `StockLocation.quarantineStockLocation`, `AppQuality.redirectNonCompliantReceptionToQuarantine`, `AppQuality.createQiOnNonCompliantReception`, `QualityImprovement.isAutomaticallyCreated`.
- `StockMoveServiceProductionImpl` now extends `StockMoveServiceQualityImpl` (was `StockMoveServiceSupplychainImpl`); new constructor params `AppQualityService`, `NonCompliantReceptionService`, `ReceptionQualityImprovementCancelService`.
- Constructors changed: `ManufOrderWorkflowServiceImpl` and `ManufOrderWorkflowMaintenanceServiceImpl` (+ `ManufOrderFinalControlCheckService`), `ControlEntryServiceImpl` and `ControlEntryProductionServiceImpl` (+ `StockMoveLineRepository`).
- New interface methods: `ControlEntryService.finish`, `ControlEntryService.getSamplesResult`, `QualityImprovementCreateService.createQualityImprovementFromStockMoveLine`, `QualityImprovementPrefillService.getDefaultDetection(StockMoveLine)`, `QualityImprovementService.getCancelledQIStatus`.

---

- New service `ManufOrderMultiLevelPlanningService`: `generateAllSubManufOrder`, `getToConsumeSubBomList`, `planSelectedOrdersAndDiscardOthers` and `getChildrenManufOrder` moved there from `ManufOrderService`/`ManufOrderServiceImpl` (with protected helpers `generateChildMOs`, `createDraftManufOrder`, `setParentMo`, `getDefaultBOMCached`, `getManualSequence`).
- `ManufOrderServiceImpl` constructor: `PartnerRepository` removed.
- `ManufOrderQueryServiceImpl` constructor: `ManufOrderService` replaced by `ManufOrderMultiLevelPlanningService`.
- `ManufOrderProductRestServiceImpl` constructor: new `ManufOrderMultiLevelPlanningService` parameter.
- `ManufOrderController.generateMultiLevelManufOrder` removed.
- Removed view `multi-level-generated-draft-manuf-order-wizard-form` and actions `action-manuf-order-method-generate-multi-level`, `action-manuf-order-attrs-multi-level-wizard-onnew`, `action-manuf-order-view-open-form-view-onclick`; new grid `multi-level-draft-manuf-order-grid` and panel `multiLevelPanel` in `manuf-order-form`.

---

- `app-production-config-form`: panels `productionConfigurationPanel`, `firstConfigurationPanel`, `secondConfigurationPanel` and `actionsPanel` removed; new `mainPanelTab` and `companyConfigurationPanel`.

#### Maintenance

The 9 overrides of `axelor-production` now guard their maintenance specific behaviour on
`isApp("maintenance")` and delegate to `super` when the app is off. Until now none of them
carried a guard, so maintenance behaviour was injected into every manufacturing order, bill of
material, cost sheet and MRP process as soon as the module was deployed.

Most of these overrides already branched on `ManufOrder.typeSelect == TYPE_MAINTENANCE` before
falling back to `super`; the guard now short circuits them one level earlier, so a manufacturing
order of that type left over from a deactivated app no longer takes the maintenance path.

`CostSheetServiceMaintenanceImpl` carries the check in its two `isMaintenance(...)` helpers,
which every one of its eight overrides already goes through.

The check is done with the `appBaseService` already inherited from the overridden production
service when there is one, otherwise it comes in through the existing constructor
(`OperationOrderStockMoveServiceMaintenanceImpl`) or is read with
`Beans.get(AppBaseService.class)` in the two classes that have no constructor
(`BillOfMaterialComputeNameServiceMaintenanceImpl`, `ProductionBatchMaintenanceService`).

`BillOfMaterialServiceMaintenanceImpl` needed no guard and is now listed in
`config/app-override-guard-allowlist.txt` as a justified exception: it is bound on
`BillOfMaterialService` (axelor-production) but overrides none of its methods, `getFileName` and
`getReportLink` being declared by `BillOfMaterialMaintenanceService` only.

`com.axelor.apps.maintenance.service.OperationOrderStockMoveServiceMaintenanceImpl` gets a new
constructor parameter, which breaks subclasses calling it.

#### Quality

`ControlEntryPlanLine.planAttrs` and `ControlEntryPlanLine.entryAttrs` (json custom field
containers) are removed. A field controlled by a control type is defined by the new entity
`ControlTypeField` (`name`, `code`, `typeSelect`, `valueSet`, `isRequired`) and attached to its
control type by the new entity `ControlTypeFieldLine`, which carries the order of the field and
the side it is used on: a reference field through `planControlType`, a measured one through
`entryControlType`. A control type therefore exposes `ControlType.planFieldLineList` and
`ControlType.entryFieldLineList`, and the same field can be reused by several control types with
a different rank in each. Values are stored in the new entity `ControlTypeFieldValue`, reachable
through `ControlEntryPlanLine.planValueList` (reference values of a control plan line) and
`ControlEntryPlanLine.entryValueList` (values measured on a control entry sample line).

Reference values are no longer duplicated on the control entry sample lines: an entry line reads
them through `controlPlanLine.planValueList`, which is what the "Targets" dashlet of the control
entry sample line form displays.

Conformity formulas must be rewritten: `$planAttrs.<field>` becomes `plan.<code>` and
`$entryAttrs.<field>` becomes `entry.<code>`, where `<code>` is `ControlTypeField.code`. A decimal
field yields a `BigDecimal`, a text field a `String`, a boolean field a `Boolean` and a selection
field a `CharacteristicProperty` record.

`ControlEntryPlanLineServiceImpl` and `ControlEntryPlanLineManagementRepository` have new
constructor arguments. `QualityExceptionMessage.EVAL_FORMULA_NULL_FIELDS` is replaced by
`QualityExceptionMessage.EVAL_FORMULA_ERROR` and `CONTROL_TYPE_FIELD_VALUE_REQUIRED`.

The `MetaJsonField` init data of the quality module (`quality_controlEntryPlanLineMetaJsonField.csv`)
is removed and replaced by `quality_controlTypeField.csv`, `quality_controlTypeFieldLine.csv` and
`quality_characteristicProperty.csv`.

Creating the control entry samples writes one row per measured value, that is samples x control
plan lines x measure fields of the control type. Measured on PostgreSQL at about 1.3 ms per
value: under a second for a usual control plan, around 30 s for an extreme one (20 samples x 30
lines x 40 measure fields = 24 000 values). The cost is the per row ORM insert, so enabling
`hibernate.jdbc.batch_size` only saves about 10%.

---

- New fields `Partner.supplierNonConformityRate`, `Partner.supplierQualityScore`, `Partner.supplierQualityTrendSelect`, `SupplierScoreHistory.supplierNonConformityRate`, `SupplierScoreHistory.supplierQualityScore`.
- New app configuration fields on `AppSupplychain`, to set on existing databases:
  `UPDATE studio_app_supplychain SET supplier_score_non_conformity_weight = 0.5, auto_compute_supplier_non_conformity_rate = true, supplier_score_gravity_critical_weight = 10, supplier_score_gravity_major_weight = 3, supplier_score_gravity_minor_weight = 1, supplier_quality_trend_months = 3, supplier_quality_trend_tolerance = 5;`
- New selection `quality.supplier.quality.trend.select`.
- `QualityImprovementManagementRepository.findPersistedSupplierPartnerId` replaced by `findPersistedState`.

---

- New `ControlTypeFieldManagementRepository` bound to `ControlTypeFieldRepository` (checks usage in `remove`).
- New service `ControlTypeFieldService` / `ControlTypeFieldServiceImpl` (`checkControlTypeUsage`).

---

- `QualityImprovementCreateService.createQualityImprovementFromControlEntry` takes a new `int type` parameter.
- Constructors changed: `ControlEntryServiceImpl` and `ControlEntryProductionServiceImpl` (+ `QualityImprovementRepository`), `QualityImprovementCreateServiceImpl` (+ `QualityImprovementPrefillService`, `QIIdentificationService`).
- New interface method `ControlEntryService.getOpenQualityImprovementSequences`; new service `QualityImprovementPrefillService` / `QualityImprovementPrefillServiceImpl` (overridden by `QualityImprovementPrefillProductionServiceImpl` in production).

#### Contract

The 10 remaining cross-module overrides of these three modules now guard their behaviour on
their own app and delegate to `super` when it is off:

- `isApp("contract")` on the eight overrides of `axelor-supplychain` and `axelor-account`
  (`AnalyticMoveLineContractServiceImpl`, `InvoiceLineAnalyticContractServiceImpl`,
  `WorkflowCancelServiceContractImpl`, `PricingGroupContractServiceImpl`,
  `AccountManagementContractServiceImpl`, `SaleOrderInvoiceContractServiceImpl`,
  `PurchaseOrderInvoiceContractServiceImpl`, `AnalyticMoveLineParentContractServiceImpl`);
- `isApp("intervention")` on `ContractInterventionServiceImpl`, which overrides
  `ContractServiceImpl` of axelor-contract;
- `isApp("cash-management")` on `OpportunitySaleOrderCashManagementServiceImpl`.

Two of them were changing accounting behaviour outside their own perimeter:
`AccountManagementContractServiceImpl.getProductAccount` returned no account at all for the
year end bonus product, and the two `mergeInvoice` overrides were writing the contract set on
every merged invoice.

The check is done with the app service already injected in the class when there is one
(`AppBaseService`, `AppAccountService`, `AppSupplychainService`), otherwise `AppBaseService`
comes in through the existing constructor.

Nothing is allowlisted for these three modules: their `KNOWN DEBT` sections disappear from
`config/app-override-guard-allowlist.txt`.

The following classes get a new constructor parameter, which breaks subclasses calling it:

- `com.axelor.apps.contract.service.AccountManagementContractServiceImpl`
- `com.axelor.apps.contract.service.WorkflowCancelServiceContractImpl`
- `com.axelor.apps.contract.service.pricing.PricingGroupContractServiceImpl`
- `com.axelor.apps.contract.service.AnalyticMoveLineParentContractServiceImpl`

---

- New fields: `AppContract.isPackManagement`, `ContractLine.isShowTotal`, `ContractLine.isHideUnitAmounts`.
- New repositories `InvoiceContractRepository` (bound to `InvoiceSupplychainRepository`) and `SaleOrderContractRepository` (bound to `SaleOrderSupplychainRepository`); `BudgetInvoiceRepository` and `SaleOrderBudgetRepository` now extend them.
- New services `ContractLinePackService` and `AppContractService`.

#### Budget

`axelor-budget` already had 19 of its 23 cross-module overrides guarded on
`isApp("budget")`. The four that were left now carry the same guard and delegate to `super`
when the app is off:

- `MoveValidateBudgetServiceImpl.accounting` was computing the budget distribution of every
  move before accounting it;
- `MoveLineCreateBudgetServiceImpl.fillMoveLineWithInvoiceLine` was copying the budget fields
  and distributions of the invoice line onto every move line;
- `MoveLineConsolidateBudgetServiceImpl.consolidateMoveLine` was merging the budget
  distributions of consolidated move lines;
- `MoveGroupBudgetServiceImpl.checkBeforeSave` was validating the budget dates of every move
  on save, and could block a save with a budget error on a base without the Budget app.

The check uses the `appBaseService` already inherited from the overridden service where there
is one, and the module's own `AppBudgetService` — the service the other 19 guards use — where
nothing was available.

The following classes get a new constructor parameter, which breaks subclasses calling it:

- `com.axelor.apps.budget.service.move.MoveLineConsolidateBudgetServiceImpl`
- `com.axelor.apps.budget.service.move.MoveGroupBudgetServiceImpl`

The `KNOWN DEBT - axelor-budget` section disappears from
`config/app-override-guard-allowlist.txt`.

---

- New field `Budget.allowBudgetImputationOnChildAccounts`.
- New interface methods `BudgetService.findBudgetByAccountWithKey` and `BudgetService.getAccountDomainWithParents`.

#### Project

- New fields: `ProjectBatch.fetchLimit`, `ProjectBatch.toDoAnticipationDays`, `ProjectBatch.overdueLookbackDays`, `ProjectBatch.overdueReminderFrequencyDays`.
- New interface method `ProjectTaskToolService.isCompleted(ProjectTask)`.
- `BatchRemoveTaskStatusService` now extends the new `com.axelor.apps.project.service.batch.BatchStrategy` (instead of the base one).
- `project-batch-form`: button `removeTaskStatusBtn` renamed `runBatchBtn`.

---

- `ProjectTaskProgressUpdateServiceImpl`: new constructor with `ProjectTaskRepository`; protected `updateChildrenProgress(ProjectTask, BigDecimal, int)` and `updateParentsProgress(ProjectTask, int)` take an extra `Map<Long, List<ProjectTask>>` parameter.

[9.2.0]: https://github.com/axelor/axelor-open-suite/compare/v9.1.10...v9.2.0