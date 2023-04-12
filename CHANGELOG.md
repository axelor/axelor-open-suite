## [6.5.4] (2023-04-06)

#### Database change

* App account: fixed the configuration to activate invoice term feature.

For this fix to work, database changes have been made as a new boolean configuration `allowMultiInvoiceTerms` has been added in `AppAccount` and `hasInvoiceTerm` has been removed in `Account`.
If you do nothing, it will work but you will need to activate the new configuration in App account if you want to use invoice term feature. Else it is recommended to run the SQL script below before starting the server on the new version.

```sql
  ALTER TABLE base_app_account
  ADD COLUMN allow_multi_invoice_terms BOOLEAN;

  UPDATE base_app_account
  SET allow_multi_invoice_terms = EXISTS(
    SELECT 1 FROM account_account
    WHERE has_invoice_term IS TRUE
  );

  ALTER TABLE account_account
  DROP COLUMN has_invoice_term;
```

#### Deprecated

* Stock API: Deprecate API call to stock-move/internal/{id}

#### Changes

* Webapp: update AOP version to 5.4.19
* GDPR: added help on AppGdpr configuration fields.

#### Fixed

* Tracking number: fix inconsistent french translation.
* Stock: fixed an issue in some processes where an error would create inconsistencies.
* Contract: fixed an issue in some processes where an error would create inconsistencies.
* Sale: fixed an issue in some processes where an error would create inconsistencies.
* Studio: fixed an issue in some processes where an error would create inconsistencies.
* Supplier management: fixed an issue in some processes where an error would create inconsistencies.
* App base config: added missing french translation for "Manage mail account by company".
* Sequence: fixed sequences with too long prefix in demo data.
* Supplychain: fixed error while importing purchase order from demo data.
* Accounting report DGI 2055: fixed issues on both tables.
* Stock move line: modifying the expected quantity does not modify a field used by the mobile API anymore.
* Move: fixed an issue so the form is not automatically saved when updating the origin date and the due date.
* Invoice: fixed payment when bank order confirmation is automatic.
* Bank details: fixed error occurring when base module was installed without bank-payment module.
* Sale order: fixed the currency not updating when changing the customer partner.
* Payment session: fixed an issue where the field "partner for email" was not emptied on copy and after sending the mail.
* Account management: fixed typo in the title of the field "notification template" and filter this field on payment session template.
* Base batch: Removed "Target" action in form view as this process does not exist anymore.
* Move line: fixed retrieval of the conversion rate at the date of the movement.
* Company: correctly hide buttons to access config on an unsaved company.
* Message: fixed a bug that could occur when sending a mail with no content.
* Inventory: fixed a bug where inventory lines were not updated on import.
* Menu: fixed menu title from 'Template' to 'Templates'.
* Json field: added missing field 'readonlyIf' used to configure whether a json field is readonly.
* BPM: fixed timer event execution and optimised cache for custom model.
* Payment session: fixed buttons displaying wrongly if session payment sum total is inferior or equal to 0.
* Accounting report journal: fixed report having a blank page.
* Stock move: when updating a stock move line, can now set an unit with the stock API.
* Manufacturing order: fixed an issue where emptying planned end date would cause errors. The planned end date is now required for planned manufacturing orders.
* Sequence: fixed an issue where we could create sequences with over 14 characters by adding '%'.
* Reconcile: improve reconciliations performances with large move lines lists.
* Bank statement: fixed issue with balance check on files containing multiple bank details and multiple daily balances.
* Studio editor: fixed theme issue.
* SaleOrder: reintroduced send email button in the toolbar.
* Accounting report payment vat: fixed no lines in payment vat report sum by tax part and not lettered part.
* Account, Invoice and Move: Remove error message at analytic distribution template on change when no analytic rules is configured.
* Timesheet: fixed an issue preventing to select a project in the lines generation wizard.
* Purchase order supplier: fixed desired receipt date field on form view.
* Payment voucher: fixed status initialization on creation.
* Manufacturing order: in form view, fixed buttons appearing and disappearing during view load.
* Project: fixed errors occuring when business-project was not installed.
* GDPR: fixed issues when copying GDPRRequest and GDPRRegisterProcessing.
* City: fixed an error occurring when importing city with manual type.

## [6.5.3] (2023-03-23)

#### Changes

* Webapp: update AOP version to 5.4.19

#### Fixed

* Bank reconciliation: fixed incorrect behaviour while correcting a validated bank reconciliation.
* Tracking number configuration : 'Auto select sale tracking Nbr.' is now correctly taken into account when creating a stock move from a sale order.
* Accounting report: For all reports, remove the 10000 and 40 lines limit before page break.
* Accounting batch: hide "bank details" filter for batch Moves consistency control.
* Production: fixed an issue in some processes where an error would create inconsistencies.
* Bank payment: fixed an issue in some processes where an error would create inconsistencies.
* Account: fixed an issue in some processes where an error would create inconsistencies.
* HR: fixed an issue in some processes where an error would create inconsistencies.
* Account: hide analytic settings panel when analytic management is not activated on the company.
* Analytic distribution line: corrected error '0' when analytic account is selected.
* Payment session: accounting method and move accounting date are now correctly readonly on a canceled payment session.
* Invoice: fixed PFP check when paying multiple supplier invoices.
* Helpdesk: fixed error when saving tickets on an instance using demo data.
* Accounting batch: reset cut off move status when on journal change.
* Payment session: fixed an issue where a payment session retrieved day book moves with "retrieve daybook moves in payment session" configuration deactivated.
* Payment session: fixed filter on payment session for invoice terms to retrieve invoice terms linked to refunds.
* Template: fix html widget for SMS templates.
* Template: fix "Emailing" french translation.
* Stock move: fixed an error occurring when opening a stock move line in a different tab.
* Stock move: fixed an issue where "to address" was not correctly filled on a generated reversion stock move.
* Stock move: supplier arrivals now correctly computes the WAP when the unit is different in stock move and stock location.
* Invoice: fixed an issue preventing from paying invoices and refunds.
* Product: fixed demo data of service so they are not managed in stock.
* Doubtful customer batch: fix success count on batch completion.
* HR: fixed typo "Managment" => "Managment".
* MRP: generating proposals now correctly generates every purchase order lines.
* Partner: prevent isCustomer from being unticked automatically if there are existing customer records in database.
* Move line: fixed an issue where duplicated analytic lines were generated.
* Financial discount: fixed french help translation.
* Mail message: fixed an issue preventing follower selection after a recipient has already been selected.

## [6.5.2] (2023-03-09)

#### Changes

* Debt recovery method line: add demo data email messages for B2C and B2B reminder recovery methods.

#### Fixed

* Analytic account: fixed demo data so analytic account imported are link to the company.
* GDPR: fixed demo data.
* Move: fixed error on move company change that happened if the journal was not filled in company configuration.
* Analytic/Move line: forbid move line validation if all the axis are not filled.
* Accounting Batch: prevent general ledger generation when an anomaly is thrown during the batch execution.
* BPM Editor: fix impossibility to save bpm process with subprocesses.
* Accounting Batch: fix technical error when we launch doubtful customer accounting batch that prevented the batch execution.
* Sale order: incoterm is not required anymore if it contains only services
* Account Config: fixed account chart data so imported accounts are now active.
* Move line: enabled VAT System modification when its a simulated move.
* Invoice: when the PFP feature was disabled, fixed an issue where the menu "supplier invoices to pay" was not displaying any invoices.
* Employee: in the user creation wizard, prevent validation when login is empty or if the activation and expiration dates are inconsistent.
* Invoice: fixed an error where invoice term percentage computation was blocking ventilation.
* Move/InvoiceTerm: removed possibility to add new invoiceTerm from grid.
* Base: fixed an issue in some processes where an error would create inconsistencies.
* Purchase order: fixed an error occurring when selecting a supplier partner.
* Account, Invoice and Move: add more consistency checks on analytic distribution template, to prevent unauthorized analytic distribution from being set.
* Accounting report 2054: Gross value amount of a fixed asset bought and disposed in the same year must appear in columns B and C.
* Demo data: update year and period date to have the next year present in demo data.
* Project task: fixed an issue where setting project task category would not update invoicing type.
* Mail message: use tracking subject instead of template subject when adding followers or posting comments.
* Accounting report: fixed error preventing analytic balance printing when the currency was not filled.
* Lead: fixed Lead report printings.
* Account config: fixed an issue where clicking "import chart button" was not possible until the field "Account code nbr. char" was filled.
* Move/Invoice/PurchaseOrder/SaleOrder: hide analytic panel when it is not managed on the selected company.
* Business: fixed an issue in some processes where an error would create inconsistencies.
* Invoice: ventilating an invoice refund correctly now correctly creates an invoice payment.
* Logistical Form: filter stock moves on company on logistical forms.
* CRM: fixed an issue in some processes where an error would create inconsistencies.
* Stock rules: alert checkbox is no longer displayed when use case is set to 'MRP'.
* Fixed asset: warning message translated in FR when trying to realize a line with IFRS depreciation plan.
* Fixed asset: fix typos in french translation.
* Fixed asset: fixed an issue where 'Generate a fixed asset from this line' box disappeared after selecting a fixed asset category.
* Freight carrier mode: fixed typo in french translation.
* Invoice: fixed an issue preventing to change the partner with existing invoice lines.
* Sale order: allow to select direct order stock locations from the company as the sale order stock location.
* Accounting/Invoicing: fixed typos in the configuration of "statements for item category".
* Project: fixed the display of description in Kanban view.
* HR Batch: fixed error making the batch process crash when using batch with a scheduler.
* Configurator: in the help panel for writing groovy scripts, fix external link so it is opened on a new tab by default.
* Invoice: remove the possibility to cancel a ventilated invoice.

Cancelling a ventilated invoice is not possible anymore. 
Reversing a move linked to an invoice doesn't cancel this invoice anymore.
Remove the config allowing to cancel ventilated invoice.

* Invoice: does not block the advance payment invoice validation if an account is not selected on the invoice.
* Accounting report: it is no longer required to fill the year to generate DAS 2 reports.
* Invoice: printing a report will open the correct one even if another report have the same file name.

## [6.5.1] (2023-02-24)

#### Fixed

* Accounting report: fixed advanced search feature on grid view.
* Payment session: Fix compute financial discount when the accounting date is linked to the original document
* French translation: corrected several spelling mistakes.
* Stock move: fixed error on invoice creation when we try to invoice a stock move generated from a purchase order.
* Stock move: fixed an error blocking stock move planification when app supplychain is not initialized.
* Stock move: fixed error message when trying to create partial invoice with a quantity equals to 0.
* App base config: added missing translation for Nb of digits for tax rate.
* GDPR: 'label' translation changed.
* Leave request: fixed the message informing the user of a negative number of leaves available.
* Followers: fixed a bug where a NPE could occur if default mail message template was null.
* Invoice: fixed the duplicate supplier invoice warning so it does not trigger for an invoice and its own refund.
* Invoice: fixed an issue occurring when computing financial discount deadline date.
* Invoice: fixed an error that happened when selecting a Partner.
* Payment session: fixed move generation on payment session when we use global accounting method.
* MRP: Improve proposals generation process performance.
* Supplychain: improved error management to avoid creating inconsistencies in database.
* Move template line: selecting a partner is now correctly filtered on non-contact partners.
* Price lists: in a sale order, correctly check if the price list is active before allowing it to be selected.
* Move: improve error messages at tax generation.
* Stock location line: updating the WAP from the stock location line will now correctly update the WAP on the product.
* Unify the sale orders and deliveries menu entries: now the menu entries at the top are the same as the menu entries at the side.
* BPM | DMN: Make it able to get model if modified and fix model change issue in DMN editor.
* Move: correctly retrieves the analytic distribution template when reversing a move.
* Advanced export: fix duplicate lines when exporting a large amount of data.
* Production batch: fixed running 'Work in progress valuation' batch process from the form view.
* Accounting Batch: fixed trading name field display.
* Account: fix invalid xml file in the source code.

## [6.5.0] (2022-02-14)

#### Features

* GDPR: add new module Axelor GDPR.
* Anonymizer : Add a new configurable setting to pseudonymize the data backup
* Faker API: Add link to a fake api in order to pseudonymize data

See faker api documentation : http://dius.github.io/java-faker/apidocs/index.html

* Fixed Asset : Manage negative values of asset values
* Period: New status 'closure in progress'
* Invoice: New configuration to display statements about the products in reports.

A new configuration is available in the accounting configuration by company, in the invoice tab. This configuration is available to display statements depending on the products category in the invoice reports.

* Invoice : New configuration to display the partner’s siren number on the invoice reports.

A new configuration is available in the accounting configuration by company, in the invoice tab.

* Forecast Recap Line Type: Two fields are added to filter forecast recap.

Forecast Recap could be filtered by functional origin and/or journals using the new fields journalSet and functionalOriginSelect from the Forecast Recap Line Type.

* Accounting report type : improve custom report types

  - Revamp report values to be computed in Java.
  - Add data cube in report and allow to create dynamic columns
  - Add percentage type column
  - Add group type column
  - Add custom period comparison
  - Add previous year computation

* Fixed asset line and move: Align depreciation dates for generated account moves with the end of period ( fiscal year or month) dates based on the depreciation plan instead of the depreciation date.
* Fixed asset: specific behavior for Depreciated/Transferred imported records
* Accounting batch and report: The general ledger may be filtered by journal and the report date is fixed.
* Analytic move lines: Analytic move lines are automatically linked to the order lines or contract lines on invoice generation.
* Bank reconciliation: The analytic distribution of the account is filled on the moves lines during the generation of the moves, if the account has analytic distribution required on move lines
* Bank reconciliation: Add dynamic display of selection balance for bank reconciliation lines and unreconciled move lines.
* Bank reconciliation: Add a boolean in account settings to fill move line cut off periods at move generation
* Bank reconciliation: Bank statement rule new specific tax field, or account default tax is applied on move lines during the move generation.
* PFP: New view for awaiting PFP in accounting menu entry
A new configuration is also added in order to take into account whether or not the daybook moves invoice terms in the new view.
* Accounting batch: The "Close/Open the annual accounts" batch may exclude specials and commitment accounts from the general ledger if "Include special accounts" is unchecked.
* Fiscal position : Prevent the creation of multiple account equivalences with the same account to replace
* Pricing scale: Versioning

Added a pricing scale history, it is now possible to historise the current pricing scale to change the values taken into account in the pricing scale lines and keep the current values in memory. It is also possible to revert to a previous version of a scale. A tracker has also been added in the scale rules to follow the evolution of the calculation rule which is not historised with the scale.

* Sale Order: Lines with a zero cost or sale price may be taken into account for the margin calculation.

New configuration is added to the sale’s app to activate this feature.

* WAP: New configuration to prevent significant value changes.

A new configuration “Tolerance on WAP changes (%).” is available in the stock configuration by company. This configuration allows you to define a tolerance limit when calculating the WAP. If the change in the WAP exceeds the defined percentage, the user will be alerted.

* Manufacturing order: Display producible quantity

The producible quantity is displayed on manufacturing order view in order to inform the user how much quantity he can produce with his current stock and his selected bill of materials.

 * CRM: added customization of lead and opportunity status, lead, prospect and opportunity scoring, and modified third party and lead forms.

* CRM: New features

  - Opportunity: The opportunity type became a string field
  - Opportunity: Allow to create opportunity from convert lead
  - Opportunity: Manage recurring
  - Opportunity: New gesture of status

* Removed static selection field salesStageSelect and replaced it with M2O field opportunityStatus
* Lead: New object LeadStatus to create your own status
* CRM Reporting : add new object 'CRM reporting'
* Create new object 'Agency'.
* Add agency field in User, Partner and Lead.
* Create CrmReporting object for filter lead/partner.
* Lead and Partner: Remove partner type from partner form and change process for next scheduled event on lead and partner form.

#### Changes

* WAP History: WAP history is now deprecated and has been replaced with Stock location line history
* Fixed Asset Category: Rename fields in fixed assets category to fit with display labels

provisionTangibleFixedAssetAccount -> provisionFixedAssetAccount

appProvisionTangibleFixedAssetAccount -> appProvisionFixedAssetAccount

wbProvisionTangibleFixedAssetAccount -> wbProvisionFixedAssetAccount


* Journal: Make journal type mandatory on journal
* Role: Add two new panel tabs to view the linked users and groups
* Sale and Purchase Order: The default email template and language are now selected according to the partner language.
* Invoice: displaying delivery address on invoice report is now enable by default
* Accounting Report: The report reference is now displayed on the report print.
* Accounting report: Add global totals in the general and partner general ledger.
* Accounting report type: Split the accounting report type menu entry in two (export and report)
* Supplier catalog: Improve the creation of supplier catalog.

The supplier catalog price is set by default with the purchase price of the product and its minimum quantity is set by default to 1.
The supplier catalog price fills the purchase price of the product if “Take product purchase price” is checked.

* Move template: Moves generated from move template are still created even if their accounting is in error

When we try to create moves with some move templates and we want to account for them in the same process, if the created moves can't be accounted for, they should still be created.
All the exceptions will be displayed in a single info message at the end of the process.

* Leave request: Company is now required in leave request.
* Move Line: Add new fields to in order to fix some information

New fields : companyCode, companyName, journalCode, journalName, currencyCode, companyCurrencyCode, fiscalYearCode, accountingDate, adjustingMove

* Manufacturing order: Links to BoM, production process and product are now open in a new tab
* Production Process: renamed subcontractors to subcontractor
* Account: Alert the user when some configurations are done on the account.

The user is alerted if he unchecks analytic and tax settings and some already existing moves use these configurations.

The user is alerted if he checks the reconcile option. He also has the possibility to reconcile move lines from a specific date when he activates this option.

* Contract batch : Display generated invoices and treated contracts for invoicing contract batch
* Contract batch : Display contracts that can be managed for invoicing contract batch mandatory field not underlined in red on contracts
* Contract: Re-invoicing contract after invoice cancellation
* Project module: improved usability and bug fixes.
* Project: Minor changes on project task, project phase, parent, child, project and timesheet
* Project: Add multiple lines of planification time
* Project: Task editor in project-form
* Project: improve planning panel in a project-form
* Project: Remove unused fields

Remove fields: totalSaleOrdersFinalized, totalSaleOrdersConfirmed, totalSaleOrdersInvoiced

* HR: New relational field with project on the HR timesheet editor

#### Fixed

* Maintenance order: Create new maintenance-manuf-order-form, maintenance-bill-of-material-form, maintenance-prod-process-form, maintenance-machine-form views and menus in order to dissociate views between manufacturing and maintenance modules.
* Manufacturing order: Remove actions and views linked to the maintenance process.
* Partner address: address is now required in the database.

This change does not mean that an address is required for a partner, but that we cannot add "empty" address lines in the partner anymore.

* Day planning: name field changed to nameSelect
* Interco/PurchaseOrder/SaleOrder: Changed some fields name and linked them in the interco process:

In Sale order, rename wrongly named column deliveryDate to estimatedShippingDate to match its title. We then added a new field estimatedDeliveryDate.

In Purchase Order, we changed deliveryDate name to estimatedReceiptDate.

We now correctly link in the interco process estimatedReceiptDate and estimatedDeliveryDate.

In Sale order line, rename wrongly named column estimatedDelivDate to estimatedShippingDate and add new field estimatedDeliveryDate. We also renamed desiredDelivDate to desiredDeliveryDate.

In Purchase Order line, change desiredDelivDate to desiredReceiptDate and change estimatedDelivDate to estimatedReceiptDate.

We now correctly link in the interco process estimatedReceiptDate and estimatedDeliveryDate for sale/purchase orders and their lines.

* Accounting Report: All reports will now filter the moves with the original currency and not the company currency

Also the currency field is moved to the filter tab and its title is renamed to "Original currency"

* Sale order and Purchase order: Rename dummy fields of invoicing wizard forms in order to follow standard guidelines

* CRM : Improvements

CRM: Review of some views: partner-grid, contact-form, lead-form-view, lead-form, lead-grid-view, lead-grid, partner-form

Opportunity Status: add label-help on some opportunities status in form

* Catalog: Add a configuration in CRM app configuration to display catalogs menu.
* Opportunity : corrected missing sales proposition status update in opportunity by adding the status in the crm app

#### Removes

* Sale Order: Remove event and email tabs from the sale order view
* Reported balance line: delete object, views and process
* Opportunity : Remove payingDate field
* Opportunity : Remove lead field
* CRM : remove Target and TargetConfiguration from CRM

[6.5.4]: https://github.com/axelor/axelor-open-suite/compare/v6.5.3...v6.5.4
[6.5.3]: https://github.com/axelor/axelor-open-suite/compare/v6.5.2...v6.5.3
[6.5.2]: https://github.com/axelor/axelor-open-suite/compare/v6.5.1...v6.5.2
[6.5.1]: https://github.com/axelor/axelor-open-suite/compare/v6.5.0...v6.5.1
[6.5.0]: https://github.com/axelor/axelor-open-suite/compare/v6.4.6...v6.5.0
