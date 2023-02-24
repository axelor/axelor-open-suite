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

[6.5.0]: https://github.com/axelor/axelor-open-suite/compare/v6.4.6...v6.5.0
