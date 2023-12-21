## [6.3.35] (2023-12-21)

#### Fixed

* Stock location: Issue on Edition of Location financial data report
* Wrong quote sequence number on finalize
* [Ticket]: Fix Ticket timer buttons
* INVOICE : currency not updated on first partner onChange
* Move Line : Prevent from updating tax line when the move is accounted
* MAINTENANCE ORDER : fix NPE when we try to plan
* Move lettering : Fill the date of lettering when the status becomes Temporary
* EXPENSE LINE: fix totalTax is not in readonly in form view when expense product blocks the taxes
* Sale / Purchase / Invoice : Fix cases where price decimal config wasn't being used
* Fixed asset : fix depreciation date when failover is complete
* EVENT : some highlights conditions in grids use __datetime__
* Printing template / Printing setting: fix translation issue in position fields.
* Sale Order/Purchase Order: Error when emptying contact
* Business Support: TaskDeadLine field not hidden when the app is installed anymore.
* UNITCOSTCALCULATION : Irrelevant message when no product has been found
* Account clearance : Fix an issue where move lines could be created or edited there

## [6.3.34] (2023-12-07)

#### Fixed

* Sale order: fixed JNPE error when copying a sale order without lines.
* Purchase request: fixed reference to purchase order being copied on duplication.
* Accounting batch: hide unnecessary payment mode information.
* Sale order: fixed wrong price update when generating quotation from template.
* Invoice: fixed reference to subrogation release being copied on duplication.
* Message: fixed encoding errors happening with accented characters when sending an email.
* Fixed asset: accounting report now correctly takes into account fiscal already depreciated amount.
* Configurator: fixed EN demo data for configurator.
* Invoice: fixed reference to "Refusal to pay reason" being copied on invoice duplication.
* Timesheet: fixed timesheet line date check.
* Account: forbid to select the account itself as parent and its child accounts.
* Bank order: highlight orders sent to bank but not realized.
* Move template line: hide and set required tax field when it is configured in financial account.
* Stock move: allow to select external stock location for deliveries.

## [6.3.33] (2023-11-23)

#### Fixed

* MRP Line: fixed an issue preventing to open a partner from a MRP line.
* Invoice: fixed description on move during invoice validation.
* Stock location line: fixed quantity displayed by indicators in form view.
* Invoice: fixed currency being emptied on operation type change.
* Fixed asset: fixed inconsistency in accounting report.
* Bank reconciliation: selectable move line are now based on the currency amount.
* Move Template: hide move template line grid when journal or company is not filled.
* Expense: fixed an issue were employee was not modified in the lines when modifying the employee on the expense.
* City: fixed errors when importing city with geonames.
* Unit cost calculation: fixed error when trying to select a product.
* Forecast: fixed a bug that occured when changing a forecast date from the MRP.
* Fixed asset category: fixed wrong filter on ifrs depreciation and charge accounts not allowing to select special type accounts.
* Appraisal: fixed appraisal generation from template.
* Move: fixed error when we set origin on move without move lines.
* Move: fixed object related errors on delete.
* Move: fixed error when we set description on move without move lines.
* Contract: fixed prorata computation when invoicing a contract and end date contract version

When creating a new version of a contract, the end date of the previous version
is now the activation date of the new version minus one day.
If left empty, it will be today's date minus one day.

* Bank order: do not copy "has been sent to bank" on duplication.
* Product details: fixed a bug occuring on stock details.

## [6.3.32] (2023-11-09)

#### Fixed

* Accounting report: fixed an issue in Journal report (11) where debit and credit were not displayed in the recap by account table.
* FEC Import: now fill correctly the header partner

    * Multi-partner : no partner in header
    * Mono-partner : fill partner in header

* Configurator BOM: fixed a concurrent error when generating line from the same configurator.
* Employee: to fix bank details error, moved the field to main employment contract panel.
* Fixed asset: hide "isEqualToFiscalDepreciation" field when fiscal plan not selected.
* FEC Import: prevent potential errors when using demo data config file.
* Contract: fixed "NullPointerException" error when emptying the product on a contract line.
* Manufacturing order: company is now required in the form view.
* Sale order (Quotation): fixed "End of validity date" computation on copy.
* Lead: prevent the user from editing the postal code when a city is filled to avoid inconsistencies.
* CRM Event: when an event is created from CRM, correctly prefill "related to".
* Sale: Hide 'Timetable templates' entry menu on config.
* Maintenance: reset the status when duplicating a maintenance request.

## [6.3.31] (2023-10-27)

#### Fixed

* Debt Recovery: fixed error message on debt recovery generation to display correctly trading name.
* Manufacturing order: fixed an issue where outsourcing was not activated in operations while it was active on production process lines.
* Payment voucher: removed paid line control at payment voucher confirmation.
* Fixed asset: fixed popup error "Cannot get property 'fixedAssetType' on null object" displayed when clearing fixed asset category field.

## [6.3.30] (2023-10-18)

#### Fixed

* Product: fixed stock indicators computation.
* Project: fixed french translation for 'Create business project from this template'.
* Sale order: fixed duplicated 'Advance Payment' panel.
* Purchase request: fixed 'Group by product' option not working when generating a purchase order.
* Sequence: fixed sequence duplication causing a NPE error.
* Move: fixed an error when we create a move line with a partner without any accounting situation.
* SOP/MPS: fixed a bug where a existing forecast was modified instead of creating a new one when the forecast was on a different date.

## [6.3.29] (2023-10-06)

#### Fixed

* Move template: fixed description not being filled on moves generated from a percentage based template.
* Sale order line: fixed error when emptying product if the 'check stock' feature was enabled for sale orders.
* Sale order line: fixed an issue where updating quantity on a line did not updated the quantity of complementary products.
* Stock move: filled correct default stock locations on creating a new stock move.
* Stock move: fixed wrong french translation on stock move creation and cancelation.
* User: encrypt password when importing user data-init.
* Print template: iText dependency has been replaced by Boxable and PDFBox.
* Cost calculation: fixed the import of cost price calculation.
* HR: fixed an issue preventing people from creating a required new training.
* Task: improved performance of the batch used to update project tasks.
* Sale order: fixed an error occurring when generating analytic move line of complementary product.
* Invoicing dashboard: fixed error on turnover per month charts.
* Account move: fixed default currency not automatically filled on a new accounting move.
* Move line: fixed wrong condition on partner based on partner balance usage account config.
* Debt recovery: fixed missing letter template error message.
* Project: fixed tracebacks not showing on project totals batch anomalies.
* Bank reconciliation: fixed balance not considering move status.

## [6.3.28] (2023-09-21)

#### Fixed

* Follower: fixed an error occuring when sending a message while adding a follower on any form.
* Product: "Economic manufacturing quantity" is now correctly hidden on components.
* Leave line: deleting every leave management now correctly computes remaining and acquired value.
* Invoice: fixed french translation for 'Advance payment invoice'.
* Manufacturing order: fixed 'No calculation' of production indicators on planned Manufacturing Order.
* Advanced export: change PDF generation.
* Stock location: fixed new average price computation in the case of a unit conversion.

## [6.3.27] (2023-09-11)

#### Fixed

* Fixed asset: fixed never ending depreciation lines generation if there is a gap of more than one year.
* Product: on product creation, fills the currency with the currency from the company of the logged user.
* Invoice: fixed wrong translations on ventilate error.
* Invoice line: set analytic accounting panel to readonly.
* Sale/Purchase order and stock move: fixed wrong filters when selecting stock locations, the filters did not correctly followed the stock location configuration.
* Stock move: fixed 'NullPointerException' error when emptying product on stock move line.
* Manufacturing order: fixed an issue where some planning processes were not executed.
* Move template: fixed copied move template being valid.
* Journal: balance tag is now correctly computed.
* Manufacturing order: when generating a multi level manufacturing order, correctly fills the partner.

## [6.3.26] (2023-08-24)

#### Fixed

* Webapp: update Axelor Open Platform dependency to 5.4.22.
* Move: fixed reverse process to fill bank reconciled amount only if 'Hide move lines in bank reconciliation' is ticked and if the account type is 'cash'

To fix existing records, the following script must be executed:

```sql
UPDATE account_move_line moveLine
SET bank_reconciled_amount = 0
FROM account_account account
JOIN account_account_type accountType
ON account.account_type = accountType.id
WHERE moveLine.account = account.id
AND accountType.technical_type_select <> 'cash';
```

* Payment voucher: fixed remaining amount not being recomputed on reset.
* Stock details: in stock details by product, fixed production indicators visibility.
* Business project batch: fixed "id to load is required for loading" error when generating invoicing projects.
* Period: fixed adjusting button not being displayed when adjusting the year.
* Fixed asset: fixed wrong depreciation value for degressive method.
* Employee: value of statusSelect with hire and leave date empty.
* Bank reconciliation: merge same bank statement lines to fix wrong ongoing balance due to auto reconcile.
* Invoice: fixed anomaly causing payment not being generated

Correct anomaly causing payment not being generated on invoice when a new reconciliation is validated  
and the invoice's move has been reconciled with a shift to another account (LCR excluding Doubtful process).

* Sale order line: fixed a bug where project not emptied on copy.
* Stock move printing: fixed an issue where lines with different prices were wrongly grouped.
* Stock details: fixed "see stock details" button in product and sale order line form views.
* Tax number: translated and added an helper for 'Include in DEB' configuration.
* Leave request: fixed employee filter

A HR manager can now create a leave request for every employee.
Else, if an employee is not a manager, he can only create leave request for himself or for employees he is responsible of.

* Manufacturing order: fixed a bug where sale order set was emptied on change of client partner and any change on saleOrderSet filled the partner.
* Contact: the filter on mainPartner now allows to select a company partner only, not an individual.

## [6.3.25] (2023-08-08)

#### Fixed

* PLANNING: Planning is now correctly filtered on employee and machine form
* SaleOrderLine: Description is now copied only if the configuration allows it
* PurchaseOrder and Invoice: Added widget boolean switch for interco field 
* ManufOrder: Planning a cancelled MO now clears the real dates on operations orders and MO
* Product/ProductFamily : Analytic distribution template is now on readonly if the config analytic type is not by product
* Invoice: Fixed french translations
* SOP/MPS: Fixed a bug where real datas were never increased
* SOP/MPS: Fixed SOP/MPS Wrong message on forecast generation
* Debt Recovery: Fixed error message on debt recovery batch to display correctly trading name
* Period : Fixed an issue where a false warning was displayed preventing the user for re-opening a closed period
* Invoice: Fixed a bug where subscription invoice was linked to unrelated advance payment invoice

When creating an invoice auto complete advance payement invoice with no internal reference to an already existing sale order.

* Product: When changing costTypeSelect to 'last purchase price', the cost price will now be correctly converted.
* BUSINESS PROJECT BATCH: Fixed invoicing project batch

## [6.3.24] (2023-07-20)

#### Fixed

* Manufacturing order: fixed JNPE error when merging manufacturing orders missing units.
* Cost sheet: fixed wrong bill of materials used for cost calculation.
* Stock move line: fixed display issues with the button used to generate tracking numbers in stock move lines.
* Operation order: correctly filter work center field on work center group (when the feature is activated).
* Stock move: fixed issue preventing the display of invoicing button.
* Supplychain batch: fixed an error occurring when invoicing outgoing stock moves.
* Product: fixed wrong filter on analytic on product accounting panel.
* Sale order: improved performance when loading card views.
* Interco: fixed generated sale order/purchase order missing a fiscal position.

## [6.3.23] (2023-07-11)

#### Fixed

* Business project: Automatic project can be enabled only if project generation/selection for order is enabled

The helper for the project generation/selection for order has been changed. To update it, a script must be executed:

```sql
DELETE FROM meta_help WHERE field_name = 'generateProjectOrder';
```

* App configuration: remove YOURS API from Routing Services

If using the distance computation with web services in expenses, users should select the OSRM API in App base config.

* Product/Account Management: hide financial account when it is inactive on product account management.
* Forecast generator: fixed endless loading when no periodicity selected.
* Stock move line: fixed a issue when emptying product from stock move line would create a new stock move line.
* Prod process line: fixed an issue where capacity settings panel was hidden with work center group feature activated.
* Inventory: fixed wrong gap value on inventory.
* Accounting report: fixed impossibility to select a payment move line in DAS2 grid even if code N4DS is not empty.
* Fixed asset: fixed move line amount 0 error on sale move generation.
* Sale order template: fixed error when selecting a project.
* Accounting batch: fixed error when we try to run credit transfer batch without bank details.
* Sale order template: fixed error when selecting a project.
* Move: on change of company, currency is now updated to company currency even when partner filled.
* Contract: fixed an error occurring when invoicing a contract

An error occurred when invoicing a contract if time prorated Invoice was enabled and then periodic invoicing was disabled.

* Move: removed verification on tax in move with cut off functional origin.
* Details stock location line: removed reserved quantity from the form view.
* Sale order: fixed totals in sales order printouts.

## [6.3.22] (2023-06-22)

#### Fixed

* Base printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Sale order line: fixed the view, move the hidden fields to a separate panel which avoids unnecessary blank space and the product field appears in its proper position.
* Stock move: date of realisation of the stock move will be emptied when planning a stock move.
* Move: added missing translation when a move is deleted.
* Bank reconciliation line: prevent new line creation outside of a bank reconciliation.
* Job position: fixed english title "Responsible" instead of "Hiring manager".
* Invoice: do not set financial discount on refunds.
* Reconcile: fixed an issue where letter button was shown if the group was unlettered.
* Invoice: added missing translation on an error message that can be shown during invoice ventilation.
* Sale order: fixed discount information missing on reports.
* Stock Move: fixed a bug where future quantity was not correctly updated.
* Partner: fixed an issue where blocking date was not displayed
* Move: fixed currency exchange rate wrongly set on counterpart generation.
* Sale order: added missing case when computing invoicing state.

## [6.3.21] (2023-06-08)

#### Fixed

* Business project, HR printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Configurator: fixed issue where sale order line generated from the configurator did not have a bill of materials.
* Invoice: allow supplier references (supplier invoice number and origin date) to be filled on a ventilated invoice.
* Invoice: fixed an issue where the button to print the annex was not displayed.
* Account config: hide 'Generate move for advance payment' field when 'Manage advance payment invoice' is enabled.
* Leave request: fixed an issue on hilite color in leave request validate grid.
* Birt template parameter: fixed french translation issue where two distinct technical terms were both translated as 'Décimal'.
* Budget distribution: fixed an issue where the budget were not negated on refund.
* Sale order line form: fixed an UI issue on form view where the product field was not displayed.
* Supplier portal and customer portal: add missing permissions on demo data.
* Project: when creating a new resource booking from a project form, the booking is now correctly filled with information from the project.
* MRP: UI improvements on form view by hiding unnecessary fields.
* Stock: fixed an error occurring when updating stock location on a product with tracking number.
* Cost calculation: fixed calculation issue when computing cost from a bill of materials.
* Tracking number: fixed an issue preventing to select a product on a manually created tracking number.
* Fixed asset: fixed JNPE error on disposal if account config customer sales journal is empty.
* Accouting report view: fixed an issue where the filter on payment mode was displayed on an analytic report type.

## [6.3.20] (2023-05-25)

#### Fixed

* Production, Sale, Quality printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Configurator: fixed an issue where removing an attribute did not update the configurator form.
* Sale order: fixed an issue during sale order validation when checking price list date validity.
* Invoice payment: update cheque and deposit info on the invoice payment record when generated from Payment Voucher and Deposit slip.
* Purchase order: fixed an error occurring when generating an invoice from a purchase order with a title line.
* Bank reconciliation: fixed an issue in bank reconciliation printing where reconciled lines still appeared.
* Bill of materials: fixed creation of personalized bill of materials.
* Invoice: added an error message when generating moves with no description when a description is required.
* Project: fixed an issue when creating a task in a project marked as "to invoice" where the task was not marked as "to invoice" by default.
* Manufacturing order: fixed filter on sale order.
* Move: fixed an error that occured when selecting a partner with an empty company.
* Summary of gross values and depreciation accounting report: fixed wrong values for depreciation columns.
* Manufacturing order: when planning a manufacturing order, fixed the error message when the field production process is empty.
* Timesheet: when generating lines, get all lines from project instead of only getting lines from task.
* Accounting report DAS 2: fixed export not working if N4DS code is missing.
* Bank order: fixed an issue where moves generated from a bank order were not accounted/set to daybook.
* Project task: when creating a new project task, the status will now be correctly initialized.
* Product: fixed an issue where activating the configuration "auto update sale price" did not update the sale price.
* Stock move: prevent cancellation of an invoiced stock move.
* Sale order: fixed popup error "Id to load is required for loading" when opening a new sale order line.
* Invoice: fixed an error preventing from merging invoices.
* Expense: prevent deletion of ventilated expense.

## [6.3.19] (2023-05-11)

#### Fixed

* Invoice: fixed an issue blocking advance payment invoice creation when the lines were missing an account.
* Job application: fixed an error occuring when creating a job application without setting a manager.
* Bank reconciliation/Bank statement rule: added a control in auto accounting process to check if bank detail bank account and bank statement rule cash account are the same.
* Stock move: fixed an issue when creating tracking number from an unsaved stock move. If we do not save the stock move, tracking number are now correctly deleted.
* Sale order: fixed an issue where sale order templates were displayed from the 'Historical' menu entry.
* Accounting payment vat report: fixed wrong french translations.
* MRP: fixed an JNPE error when deleting a purchase order generated by a MRP.
* Sale, Stock, CRM, Supplychain printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Purchase order: stock location is not required anymore if there are no purchase order lines with stock managed product.
* Invoice: fixed wrong alert message when invoiced quantity was superior to delivered or ordered qty.
* Invoice: fixed wrong alert message when invoiced quantity was superior to delivered or ordered qty.
* Bank reconciliation: fixed initial and final balance when importing multiple statements.
* Inventory: fixed UI issue by preventing unit from being changed in inventory lines.
* Stock rules: now correctly apply stock rules when minimum quantity is zero.

## [6.3.18] (2023-04-27)

#### Fixed

* Purchase order: fixed fiscal position on a purchase order generated from a sale order.
* FEC import: fixed an error occuring when importing FEC using the format without taxes.
* Debt recovery: fixed a a regression on demo data, the demo data should now have existing email templates.
* Batch bill of exchange: raise anomaly when invoices are ready to be processed but bank details is inactive.
* Stock move: fixed an error occurring when emptying the product in a line.
* Analytic Rules: added a company filter on analytic account verification.
* Group Menu Assistant: fixed an issue where an empty file was generated.
* Move line/Fixed asset: corrected wrong journal on fixed asset generated from move line.

## [6.3.17] (2023-04-21)

#### Fixed

* Sale order: fixed an issue where opening or saving a sale order without lines was impossible due to an SQL error.

## [6.3.16] (2023-04-20)

#### Fixed

* Sale order: sale orders with a 0 total amount are now correctly displayed as invoiced if they have an ventilated invoice.
* Partner: fixed script error when opening partner contact form (issue happening only when axelor-base was installed without axelor-account).
* Operation order calendar: display operation orders with all status except operations in draft, cancelled or merged manufacturing orders.
* Customer/Prospect reporting: fixed an error occuring if we only have axelor-base installed when opening the dashboard.
* Move: fixed analytic move lines copy when reversing a move.
* Partner: fixed an error preventing the display of partner sale history when a product was sold with a quantity equals to 0.
* Operation order: fix UI issues when the user was modifying date time fields used for the planification.
* Base batch: fixed an issue when clicking the button to run manually the "synchronize calendar" batch.
* Partner: fixed an error preventing the display of partner sale history when a product was sold with a quantity equals to 0.
* BPM: fixed view attribute issue for a sub-process.
* Stock move: fixed an issue when opening stock move line form from the invoicing wizard.
* Message: fixed an issue where emails automatically sent were not updated.
* Invoice: fixed filter on company bank details for factorized customer so we are able to select the bank details of the factor.
* Sale order: generating a purchase order from a sale order now correctly takes into account supplier catalog product code and name.
* Stock move: now prevent splitting action on stock move line that are associated with a invoice line.
* Invoice: to avoid inconsistencies, now only canceled invoices can be deleted.
* Accounting period: fixed an issue where the user was able to reopen a closed period on a closed year.
* Bank details: fixed script error when opening bank details form (issue happening only when axelor-base was installed without axelor-account).

## [6.3.15] (2023-04-06)

#### Changes

* Webapp: update AOP version to 5.4.19

#### Fixed

* Tracking number: fix inconsistent french translation.
* Stock: fixed an issue in some processes where an error would create inconsistencies.
* Contract: fixed an issue in some processes where an error would create inconsistencies.
* Sale: fixed an issue in some processes where an error would create inconsistencies.
* Studio: fixed an issue in some processes where an error would create inconsistencies.
* App base config: added missing french translation for "Manage mail account by company".
* Sequence: fixed sequences with too long prefix in demo data.
* Bank details: fixed error occurring when base module was installed without bank-payment module.
* Sale order: fixed the currency not updating when changing the customer partner.
* Base batch: Removed "Target" action in form view as this process does not exist anymore.
* Company: correctly hide buttons to access config on an unsaved company. 
* Message: fixed a bug that could occur when sending a mail with no content.
* Inventory: fixed a bug where inventory lines were not updated on import.
* Menu: fixed menu title from 'Template' to 'Templates'.
* Json field: added missing field 'readonlyIf' used to configure whether a json field is readonly.
* BPM: fixed timer event execution and optimised cache for custom model.
* Accounting report journal: fixed report having a blank page.
* Manufacturing order: fixed an issue where emptying planned end date would cause errors. The planned end date is now required for planned manufacturing orders.
* Sequence: fixed an issue where we could create sequences with over 14 characters by adding '%'
* Bank statement: fixed issue with balance check on files containing multiple bank details and multiple daily balances.
* Studio editor: fixed theme issue.
* Accounting report payment vat: fixed no lines in payment vat report sum by tax part and not lettered part.
* Timesheet: fixed an issue preventing to select a project in the lines generation wizard.
* Payment voucher: fixed status initialization on creation.
* Manufacturing order: in form view, fixed buttons appearing and disappearing during view load.
* Project: fixed errors occuring when business-project was not installed.
* City: fixed an error occurring when importing city with manual type.

## [6.3.14] (2023-03-23)

#### Changes

* Webapp: update AOP version to 5.4.19

#### Fixed

* Tracking number configuration : 'Auto select sale tracking Nbr.' is now correctly taken into account when creating a stock move from a sale order.
* Accounting report: For all reports, remove the 10000 and 40 lines limit before page break.
* Production: fixed an issue in some processes where an error would create inconsistencies.
* Bank payment: fixed an issue in some processes where an error would create inconsistencies.
* Account: fixed an issue in some processes where an error would create inconsistencies.
* HR: fixed an issue in some processes where an error would create inconsistencies.
* Template: fix html widget for SMS templates.
* Template: fix "Emailing" french translation.
* Stock move: fixed an error occurring when opening a stock move line in a different tab.
* Stock move: fixed an issue where "to address" was not correctly filled on a generated reversion stock move.
* Stock move: supplier arrivals now correctly computes the WAP when the unit is different in stock move and stock location.
* HR: fixed typo "Managment" => "Managment".
* MRP: generating proposals now correctly generates every purchase order lines.
* Partner: prevent isCustomer from being unticked automatically if there are existing customer records in database.
* Move line: fixed an issue where duplicated analytic lines were generated.
* Financial discount: fixed french help translation.
* Mail message: fixed an issue preventing follower selection after a recipient has already been selected.

## [6.3.13] (2023-03-09)

#### Changes

* Debt recovery method line: add demo data email messages for B2C and B2B reminder recovery methods.

#### Fixed

* Sale order: incoterm is not required anymore if it contains only services
* Account Config: fixed account chart data so imported accounts are now active.
* Invoice: when the PFP feature was disabled, fixed an issue where the menu "supplier invoices to pay" was not displaying any invoices.
* Employee: in the user creation wizard, prevent validation when login is empty or if the activation and expiration dates are inconsistent.
* Base: fixed an issue in some processes where an error would create inconsistencies.
* Accounting report 2054: Gross value amount of a fixed asset bought and disposed in the same year must appear in columns B and C.
* Project task: fixed an issue where setting project task category would not update invoicing type.
* Mail message: use tracking subject instead of template subject when adding followers or posting comments.
* Accounting report: fixed error preventing analytic balance printing when the currency was not filled.
* Business: fixed an issue in some processes where an error would create inconsistencies.
* Batch form: close the popup to show the invoice list when the user is clicking the "show invoice" button.
* Logistical Form: filter stock moves on company on logistical forms.
* CRM: fixed an issue in some processes where an error would create inconsistencies.
* Stock rules: alert checkbox is no longer displayed when use case is set to 'MRP'.
* Fixed asset: warning message translated in FR when trying to realize a line with IFRS depreciation plan.
* Fixed asset: fix typos in french translation.
* Freight carrier mode: fix typo in french translation.
* Invoice: fixed an issue preventing to change the partner with existing invoice lines.
* Sale order: allow to select direct order stock locations from the company as the sale order stock location.
* Project: fixed the display of description in Kanban view.
* HR Batch: fixed error making the batch process crash when using batch with a scheduler.
* Configurator: in the help panel for writing groovy scripts, fix external link so it is opened on a new tab by default.
* Invoice: does not block the advance payment invoice validation if an account is not selected on the invoice.
* Accounting report: it is no longer required to fill the year to generate DAS 2 reports.
* Invoice: printing a report will open the correct one even if another report have the same file name.

## [6.3.12] (2023-02-24)

#### Fixed

* Accounting report: fixed advanced search feature on grid view.
* Stock move: fixed error on invoice creation when we try to invoice a stock move generated from a purchase order.
* Stock move: fixed an error blocking stock move planification when app supplychain is not initialized.
* Stock move: fixed error message when trying to create partial invoice with a quantity equals to 0.
* Leave request: fixed the message informing the user of a negative number of leaves available.
* Followers: fixed a bug where a NPE could occur if default mail message template was null.
* Invoice: fixed the duplicate supplier invoice warning so it does not trigger for an invoice and its own refund.
* MRP: Improve proposals generation process performance.
* Supplychain: improved error management to avoid creating inconsistencies in database.
* Move template line: selecting a partner is now correctly filtered on non-contact partners.
* Price lists: in a sale order, correctly check if the price list is active before allowing it to be selected.
* DMN: fixed model change issue.
* Stock location line: updating the WAP from the stock location line will now correctly update the WAP on the product.
* Unify the sale orders and deliveries menu entries: now the menu entries at the top are the same as the menu entries at the side.
* Move: correctly retrieves the analytic distribution template when reversing a move.
* Advanced export: fix duplicate lines when exporting a large amount of data.
* Invoice: fixed an error that happened when selecting a Partner.
* Production batch: fixed running 'Work in progress valuation' batch process from the form view.
* Accounting Batch: fixed trading name field display.

## [6.3.11] (2023-02-14)

#### Fixed

* Studio : Fix editor issues

Fix difficulty to insert fields. Fix crash on click on '+' icon of panel tab.

* Partner: improve email panel

Fixed an issue where permissions were not applied for emails displayed in this panel, also improve display by using a card view.

* Opportunity: lost reason is now correctly cleared when reopening a lost opportunity.
* Opportunity: fixed description on kanban view.
* Ticket type: fixed error preventing to show chart information on opening a ticket type form.
* Meta select: fixed wrong french translation for "Order".

## [6.3.10] (2023-02-03)

#### Fixed

* Accounting report: fixed partner grouping issue on preparatory process for fees declaration.
* User: fixed active user being able to change its user code.
* Sale order: fixed a bug where doing a partial invoicing in percentage could not work.
* Company bank details: fixed company bank details filter in invoice and purchase order form views.
* Stock move: fixed rounding issues when computing the total to avoid a gap between order/invoice totals and related stock moves totals.
* Stock move: fixed report not showing manually changed address for customer delivery.
* Cost calculation: fixed the priority on the bill of materials chosen during the calculation process. This fixes an issue where a bill of materials from another company was used to compute the cost.
* Fiscal position: changed the behavior of fiscal position on purchase and sale order:

Now the partner is filtered depending on the currency and price list which are readonly if there is an order line.
The fiscal position is now editable even if there is an order line. Changing the partner updates the fiscal position and the taxes on lines.

* Product: updated condition for default value on product pulled off market date.
* Accounting report: corrected wrong values in 2054 accounting report.
* Manufacturing order: fixed small UI issue in form view.
* Sale order/Purchase order/Invoice line: fixed JNPE error occured when fetching the correct translation for code and description.
* Sale order printing: correctly hide discount related rows when the sale order is configured to hide discounts.
* Move consolidation: fixed errors during move consolidation happening when analytic distribution line were missing information.
* Subrogation release: fixed an issue where supplier invoices were retrieved with customer invoices.
* Account management: fixed an issue preventing any user from adding an account management of type tax in the financial configuration.
* Fixed asset category: clear company related fields when changing or clearing the company field to avoid inconsistencies.
* Fixed asset: clear company related fields when changing or clearing the company field to avoid inconsistencies.
* Fixed asset: set default value to 0 to avoid saving a wrong default value in the fields Disposal Type and Quantity type on new and ongoing fixed asset.
* Fixed asset: improved UI for the 'Update depreciating settings' feature.
* Fixed asset: fixed filter on partner field in form view.
* Account config: fixed an issue were financial discount related configuration were required.
* Purchase order: duplicating a purchase order now correctly resets budget.
* Bill of materials: fixed an issue where the label 'BOM by default' was displayed on a new bill of materials.

## [6.3.9] (2023-01-19)

#### Fixed

* Inventory: Reversed Gap computation on inventory lines. Previously, the gap value was negative when the quantity found in the inventory was greater than the expected quantity. It is now positive in this case and negative if we have less quantity than expected.
* Stock move: fixed a regression were it was impossible to select any product on internal move.
* Contracts: fixed an issue where, when filling a product in a contract line, "missing configuration" errors were not shown to the user.
* Manufacturing order: fixed a bug where outsourced manufacturing orders could not be planned because 'outsourcing receipt stock location' was missing in the stock config.
* TranslationService: fixed an issue happening when using a translatable field, when the translation key is equal to the value, the wrong translation was displayed.
* Company: add explicit error message if the current user active company is not set and needed.
* Accounting batch: fixed "PersistenceException" error preventing the execution of the batch when the list of opening account was empty.
* Accounting reports: UI improvement for the form view (hide "global" and "global by date" fields for General balance and Partner balance).
* Bank reconciliation: fixed a UI issue where clicking "Unselected" with a selected move line breaks the alignement of the blue buttons in the move line grid.
* Quality measuring point: set a minimum of 1 for the coefficient.
* Fixed asset: fix typos in french translation that appeared in the popup when using "Tools" in grid view.
* Complementary products: fixed an issue where the quantity of complementary products were multiplied by the quantity of the main product (when using the configuration "manage multiple sale quantity").
* Invoice: fixed an issue which removed the product name when changing the quantity in a line.
* Invoice: fixed a bug where the lines of a advance payment could be duplicated from a purchase order with reversed charge tax line.
* Invoice: when generating an interco invoice, the generated supplier invoice now takes the correct company bank details.
* Address: fixed an UI issue where changing the zip code was emptying the city.

## [6.3.8] (2023-01-05)

#### Fixed

* Fixed asset: fix error message when realizing a derogatory line with a category that lacks one of the derogatory depreciation accounts.
* Sale order, stock move, purchase order: fixed an issue where a NPE error was displayed when adding a new line in sale order, sale order template, stock move, purchase order.
* Bank details: improve message when confirming a bank order and a receiver bank details is inactive.
* Fixed asset: Fix wrong depreciation value computation in fixed asset line till the depreciation date for both linear and degressive depreciation plan.
* Move: fixed an issue where description was not filled during move origin change.
* Expense: fixed an issue where ground for refusal was hidden even if it was fill.
* Manufacturing: fix form views for subcontractor deliveries and arrivals.
* Analytic distribution: add filter for analytic axis, using the account config of the company.
* Followers: Fixed a residual bug where trying to fetch a recipient would result in a exception with somes conditions
* Followers: Fixed a residual bug where trying to fetch a recipient would result in a exception with somes conditions.
* Move line: fixed an issue where base analytic distribution was readonly while the configuration allowed the modification.
* MRP: fixed an infinite loop issue

An infinite loop occurred when, for a product, the available stock was less than the minimum quantity of its stock rule
and this product was used in a purchase order.

* MRP: fix generation proposal menu for more clarity.

Generate proposal menu item now opens a wizard with 2 buttons:
One for generating all possible proposals, one for generating proposals of only selected lines.

* MRP: fixed NPE error that could happen during MRP computation if a operation order had a null planned start date.
* MRP: When generating purchase order from proposals, correctly fill desired delivery date.
* Fixed asset category: fixed some french translation in accounting information panel.
* Move line/analytic: fixed an issue where analytic was not generated during move line generation.
* Invoice: reject company bank details when default bank details is not active.
* Fixed asset: fixed disposalValue when disposing by scrapping, hide disposalValue when asset is disposed by cession.
* Fixed asset: fixed issue with prorata temporis when computation date starts on the 31st of months.
* Cost sheet: fixed wrong human cost calculation when computing a cost sheet for a in progress manufacturing order.
* Bank reconciliation: added default account type in search query.
* Demo data: complete demo data for permissions, roles and menus.
* Move: currency related fields are now changed on change of company.
* Inventory: when changing real quantity in a line, now takes the correct wap price into account when computing gap and real value.
* Contracts: correctly apply the configuration "Nb of digits for unit prices" to price in contract lines.
* Contracts: correctly apply the configuration "Nb of digits for quantity" to price in contract lines.
* Accounting report type: UI form view improvements.
* Move: fix creation of an move line when an partner has a default charge account.
* Manufacturing Orders: during manfacturing order generation, added a more explicit message if the bill of materials has 0 quantity.
* Timesheet line: fixed task display in grid view.
* Sale order separation: Fix computation of miscellaneous sale order fields.
* Accounting report: fixed an issue where 0.0 figures were wrongly displayed in title lines for PDF personnalized reports.
* Move/Move line: Fix deletion of analytic axis and remove save popup when nothing is edited on move line.
* Accounting report: fixed different issues on ETAT PREPARATOIRE DGI 2054.
* Fixed asset: fixed issues with prorata-temporis on depreciation plan computation of imported fixed assets.

## [6.3.7] (2022-12-16)

#### Fixed

* Bill of materials: fix error when accessing general bill of materials menu.
* Leave request: fix error message when sending leave request when company is missing.
* Accounting reports: add origin informations (ref and date) on general ledger and partner general ledger.
* Move line: clear fields related to partner when partner is emptied.
* Invoice: correctly hide refund list when there is no refund.
* Move: on move generation from template view, correctly set "generate move" button as readonly when there is no input.
* Task editor: fix error while dragging task.
* Partner address: improve UI when adding an address to a partner to avoid inconsistencies.
* Menu builder: eval is automatically added for context value fixed.
* Menu builder: set context by default when overriding an existing menu.
* Account management: set default values when we create a new record in account management grid.
* Purchase Order: add missing translation when generating advance payment.
* Debt recovery history: prevent the user from inserting new rows in grid view.
* Bank Order: Payment mode and file format now are correctly reset when order type select is changed.
* Stock move: picking order comments panel are now correctly hidden for supplier arrivals.
* Purchase order report: fixed an issue where product name was displayed instead of specific supplier product name.
* Fixed asset category: filter IFRS account fields by charge account type.
* Accounting report: fixed wrongly defined display condition on analytic axis, analytic account, account type fields for analytic type reports form view.
* Translation: fix wrong french translation for Ongoing.
* Purchase order: now correctly takes the default virtual supplier stock location when creating a purchase order.
* Bank statement rule: Add filter on counter part account to avoid selecting view accounts.
* Product details: fixed permission so users do not need write permissions on Product to use this feature.
* Stock move: fixed query exception that happened on the form view when the user had no active company.
* Contract / ContractVersion: allow to correctly fill dates when 'isPeriodicInvoicing' is activated on a new contract version.
* Account Management : Add filter by company on journal field.
* Expense: now takes the correct bank details when registering a payment.
* Data Backup: fixed an error occurring when creating a data backup with the anonymization enabled.

## [6.3.6] (2022-12-08)

#### Features

* Invoice: add new French statements in Invoice Report depending of product type.
* Invoice: add partner siret number in French report.

#### Fixed

* User: email field is now correctly displayed

Email field is now displayed in editing mode.
Email field is now displayed in readonly mode only if a value is present.

* Followers: fixed a bug where trying to fetch a recipient would result in a exception with somes conditions.
* Sale order: fixed stack overflow error when the partner had a parent partner.
* Cash management: fixed an issue were some data were not displayed correctly on the PDF report.
* Invoice line: choosing a product from a supplier catalog now takes the product name from the supplier catalog and not the translation.
* Leave request / employee: a HR manager can now access draft requests for other employees from the menu.
* Debt recovery history: prevent the user from printing a report when there is no debt recovery.
* Fixed asset: fixed disposal move not generated when proceeding to the disposal of a fixed asset where accounting value is equal to 0.
* Fixed asset: fixed wrong accounting value in degressive computation.
* Fixed asset: fixed tax lines management in cession sale move generation

Check on missing tax account only when the tax line value is positive
Initialize of taxline in movelines for credit move line and tax move line

* Accounting demo data: fix demo data for fixed asset category and journals.
* Stock move: changing the real quantity of a line will not reset the discounted price to the original price anymore.

When generating a Stock move from a Purchase order, if a line had a discount,
changing the real quantity of a discounted line would set the price to the original one. This wrong behavior is now fixed.

* Sale order: fixed the automatic filling when partner change for the field 'Hide discount on prints' according to it value in the price list.
* Price List: fixed wrong filter on price list menu entry which prevented most price lists to be displayed.
* Mass invoicing: fixed a bug where some stock moves could not be invoiced when the invoice was created from a merge.
* Accounting batch: Fix the batch to realize fixed asset line so it correctly update only the fixed asset lines from the selected company.
* Accounting move: when we reverse a move, the analytic move lines are now correctly kept on the reversed move.
* Operation order: fix a blocking NullPointerException error when we plan an operation order without a machine.
* Sequence / Forecast recap: Fix demo data sequence for the forecast recap.
* Stock move: details stock locations lines will not longer be generated with zero quantity when the stock move is a delivery.
* Bank order: Add the possibility to sign bank orders even without EBICS module.
* Sale/Purchase order: fix the way we fetch default stock location on purchase and sale orders using partner stock settings.
* Expense: when paying expense, correctly fill sender company and sender bank details in the generated payment.
* Accounting move line: fix currency amount/rate readonly condition in the counterpart generation.
* Stock move: if the configuration is active, services (product) are now correctly managed in stock moves.

## [6.3.5] (2022-11-25)

#### Fixed

* Move line: fixed an issue preventing the currency amount computation when setting a debit or a credit.
* Bank reconciliation: show move records with status daybook or accounted only.
* Bank reconciliation: fix reconciliation of manually added bank reconciliation line.
* Fixed asset cession: fixed accounting account and amounts on generated sale move.
* Invoice Line: choosing a product from a supplier catalog in an invoice line now has the same behavior as a purchase order line:

Choosing a product from a supplier catalog takes the product name from the supplier catalog.
If there is a minimum quantity, the user is alerted that the quantity is inferior than the minimum quantity.
Choosing a quantity inferior than the minimum quantity does not remove the product name anymore.

* Partner: fixed error preventing "group products on printings" configuration from being displayed correctly.
* Leave Request: fixed issue preventing the user to submit a leave request.
* Inventory: fixed an issue where base quantities were not updated when starting the inventory, causing inconsistencies.
* Move: partner and currency fields are now in readonly when there is a move line to prevent inconsistencies.
* Invoice: fixed an error that could happen when creating a invoice with no active company for the user.
* Invoice: fixed an error preventing any payment on a duplicated invoice.
* Timesheet line: fixed an issue were employee was not correctly set on timesheet line creation.
* Accounting batch annual closure: set the closure/opening moves' status to Daybook when daybook mode is activated and allow to create these moves on a closed period.
* Stock correction: changing stock location correctly update base quantity in form view.
* Purchase order: fixed purchase order printing so it is not showing negative lines as discount.
* Purchase order: added a check on budget distributions on validation of the purchase order to warn the user if the price is not fully imputed on existing budget lines.
* Product: procurement method is not required anymore.
* Bank order: created a more explicit error when creating a move for invoice payment if it fails on realization of bank order.
* Debt recovery: The debt recovery process will now be reset to the first step if all the concerned invoices are considered as new for the recovery.
* Invoicing project: fixed blocking error (JNPE) when timesheet line product is not filled.
* Payment move line distribution: these elements are now only generated when the purchase move has a partner.
* WS Connector: fix numeric value handling on ws connector.
* Fixed Asset: remove useless checks on failover date that were triggering by mistake.
* Partner: fixed a regression were some default fields were not filled on creation.
* Accounting report config line: fix in demo data to correctly import result in configuration lines for accounting report.
* Year: fixed demo data for periods generated from fiscal year, now the periods generated from demo data last one month instead of one year.
* Webapp: updated AOP correction version in open-suite-webapp.

#### Removed

* Global Tracking : remove read feature

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

[6.3.35]: https://github.com/axelor/axelor-open-suite/compare/v6.3.34...v6.3.35
[6.3.34]: https://github.com/axelor/axelor-open-suite/compare/v6.3.33...v6.3.34
[6.3.33]: https://github.com/axelor/axelor-open-suite/compare/v6.3.32...v6.3.33
[6.3.32]: https://github.com/axelor/axelor-open-suite/compare/v6.3.31...v6.3.32
[6.3.31]: https://github.com/axelor/axelor-open-suite/compare/v6.3.30...v6.3.31
[6.3.30]: https://github.com/axelor/axelor-open-suite/compare/v6.3.29...v6.3.30
[6.3.29]: https://github.com/axelor/axelor-open-suite/compare/v6.3.28...v6.3.29
[6.3.28]: https://github.com/axelor/axelor-open-suite/compare/v6.3.27...v6.3.28
[6.3.27]: https://github.com/axelor/axelor-open-suite/compare/v6.3.26...v6.3.27
[6.3.26]: https://github.com/axelor/axelor-open-suite/compare/v6.3.25...v6.3.26
[6.3.25]: https://github.com/axelor/axelor-open-suite/compare/v6.3.24...v6.3.25
[6.3.24]: https://github.com/axelor/axelor-open-suite/compare/v6.3.23...v6.3.24
[6.3.23]: https://github.com/axelor/axelor-open-suite/compare/v6.3.22...v6.3.23
[6.3.22]: https://github.com/axelor/axelor-open-suite/compare/v6.3.21...v6.3.22
[6.3.21]: https://github.com/axelor/axelor-open-suite/compare/v6.3.20...v6.3.21
[6.3.20]: https://github.com/axelor/axelor-open-suite/compare/v6.3.19...v6.3.20
[6.3.19]: https://github.com/axelor/axelor-open-suite/compare/v6.3.18...v6.3.19
[6.3.18]: https://github.com/axelor/axelor-open-suite/compare/v6.3.17...v6.3.18
[6.3.17]: https://github.com/axelor/axelor-open-suite/compare/v6.3.16...v6.3.17
[6.3.16]: https://github.com/axelor/axelor-open-suite/compare/v6.3.15...v6.3.16
[6.3.15]: https://github.com/axelor/axelor-open-suite/compare/v6.3.14...v6.3.15
[6.3.14]: https://github.com/axelor/axelor-open-suite/compare/v6.3.13...v6.3.14
[6.3.13]: https://github.com/axelor/axelor-open-suite/compare/v6.3.12...v6.3.13
[6.3.12]: https://github.com/axelor/axelor-open-suite/compare/v6.3.11...v6.3.12
[6.3.11]: https://github.com/axelor/axelor-open-suite/compare/v6.3.10...v6.3.11
[6.3.10]: https://github.com/axelor/axelor-open-suite/compare/v6.3.9...v6.3.10
[6.3.9]: https://github.com/axelor/axelor-open-suite/compare/v6.3.8...v6.3.9
[6.3.8]: https://github.com/axelor/axelor-open-suite/compare/v6.3.7...v6.3.8
[6.3.7]: https://github.com/axelor/axelor-open-suite/compare/v6.3.6...v6.3.7
[6.3.6]: https://github.com/axelor/axelor-open-suite/compare/v6.3.5...v6.3.6
[6.3.5]: https://github.com/axelor/axelor-open-suite/compare/v6.3.4...v6.3.5
[6.3.4]: https://github.com/axelor/axelor-open-suite/compare/v6.3.3...v6.3.4
[6.3.3]: https://github.com/axelor/axelor-open-suite/compare/v6.3.2...v6.3.3
[6.3.2]: https://github.com/axelor/axelor-open-suite/compare/v6.3.1...v6.3.2
[6.3.1]: https://github.com/axelor/axelor-open-suite/compare/v6.3.0...v6.3.1
[6.3.0]: https://github.com/axelor/axelor-open-suite/compare/v6.2.8...v6.3.0
