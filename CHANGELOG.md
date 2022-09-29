## [6.2.9] (2022-09-29)

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
* Debt recovery: removed error message when there is no email address for a mail type message.
* Purchase Order: fixed an issue where "amount available" in budget tab was not correctly computed.
* Bank Payment: added bank statement demo data.
* Invoice: generating an invoice from a sale order or a stock move now correctly sets the project.
* Invoice: fixed an issue where project was not filled on a partial invoice generated from a purchase order.
* Contract: remove duplicate duration values on contract templates and fix french translation in form view.
* MRP: fixed MRP calculation for manufacturing order partially finished, the remaining amount to produce is now used in the computation.
* Stock move line: fixed an issue allowing the user to fill quantity in a title line, causing inconsistencies.
* Tracking number: fixed an issue where the product was missing in a tracking number created from inventory lines.
* BPM: optimization in BPM editor, BPM records are now loaded only if they are needed.
* Tax: fixed an error occurring when choosing a product for a sale or a purchase order if the creation date was not filled.
* Accounting Report: corrected numbers exported as text in general comparative balance report in excel format.

## [6.2.8] (2022-09-15)

#### Fixed

* Invoice: fixed a bug where, when generating an invoice, the default company bank details was used instead of the one selected by the user.
* Optimize stock history batch update to reduce process duration.
* Analytic distribution line: Fix axis with no company being selectable.
* Leave Request: prevent the creation of leave requests for former employees.
* Partner: fixed a regression where the partner displayed name was first name followed by name, it is now back to name followed by first name.
* Accounting report: fixed duplicate report error message raised for custom report types.
* DAS2: fix error message when phone number from DAS2 contact is missing.
* Accounting report config line: fixed bugs where the alternation character '|' could not be used in account code and values of the report could be duplicated.
* Invoice: fixed an issue where financial discount dead line date was not correctly computed on a copied invoice.
* Move: fixed an issue were validating a large amount of moves would lead to a technical error stopping the process.

## [6.2.7] (2022-09-01)

#### Fixed

* Ticket: fix ticket copy by resetting correctly fields that are not configured by the user.
* Invoice: fixed error when ventilating an invoice with analytic lines.
* Invoice: fixed an issue where invoice printing PDF file had a blank page at the end of the report.
* Invoice: fixed discount display issue in the printing.
* Invoice: fixed an error that occured when supplier refund with PFP activated on refunds was created.
* Accounting report: increase account code column size in general balance with comparative periods report.
* Accounting report: fixed a blocking error preventing the generation of a Payment Difference report.
* Move: in move form view, the journal is now readonly if the move has move lines.
* Move: fixed an issue where simulate button was not displayed in move form.
* Move: fixed an issue where tax asset move line was not generated.
* Move: fixed an issue where it was not possible to reverse a move linked to a notified invoice in an accounted subrogation release.
* Fixed Asset: fixed error (JNPE) when we removed first depreciation date or acquisition date.
* Purchase Order: fixed error when adding title line that prevented computation of totals.
* Sale Order: fixed an issue preventing the addition of title lines when 'Do not display the header and end of pack' was checked.
* Cost calculation: fixed a bug where we could not select a product if the company of the default bom's product was different.
* Product: fixed an issue where product per company lines were not created on a new product.
* Stock move: fix stock move generation from demo data.
* Move/Move line: added controls of consistency of the company used in a move and its move lines.
* Sequence: update sequence version list when enabling yearly or monthly reset: the active sequence version will be updated if the end date is not correct.
* Work center group: change "Work center model" title to "Model of work center group" for coherence.

## [6.2.6] (2022-08-11)

#### Fixed

* Print Template: several UI improvements

  - Illustrative sentence added to the top of the line template to explain the different possibilities.
  - The help explanations of the 'content' 'conditions' & 'title' text areas have been modified.
  - A new text area 'Notes' has been added to the line template to help distinguish lines the grid.
  - The column 'conditions' is now displayed in the grid.

* Product: fixed a bug where a opened product variant was marked as dirty.
* Sale Order: fixed an error preventing sale order save when the 'Manage partner complementary product' option was activated.
* Configurator creator: change title from Configurator type to Configurator model for uniformity.
* Expense: fixed an issue where expense ventilation did not work after setting accounting analytic template to expense lines.
* Lunch voucher: fix lunch voucher computation: half-day leaves now correctly reduce number of lunch vouchers by one.
* MRP: reset date fields when copying an existing MRP.
* App manufacturing: Use the same title for both app and view.
* Manufacturing Order: fixed an issue preventing to finish a manufacturing order with a operation order on standby.
* Analytic Accounting Template: fixed an issue where default analytic template was not retrieved correctly on move line.
* Analytic Accounting Template: corrected analytic distribution template not resetting after company change in account.
* Move: hide 'Generate counterpart' on validated accounting moves.
* Move Line: corrected wrong description in invoice move lines and added missing origin in move description.
* Move Line: display bank reconciliation information only for line with a cash account.
* Accounting period: Fix variation(%) in general balance report.
* Accounting period: fixed an error impacting the performance of the process to close a period.
* Accounting: fixed the "Revenues vs Expenses" chart.
* Account clearance: Fixed errors when fetching excess payments preventing the process from working correctly.

## [6.2.5] (2022-07-29)

#### Changes

* Invoice / Stock move: Improve PFP status management.
* Move Line: In the move line form view, bank reconciliation panel is now displayed as a panel-tab.

#### Fixed

* MAIL TEMPLATE ASSOCIATION: fix template keys issues when using custom template for mail notifications on comments

In the custom mail template used for mail notifications sent when commenting on a form, following changes were made:

    - Fix variable `$ccRecipients$` so it is filled with all followers.
    - Add variable `$toRecipient$` which is currently the same as `$ccRecipients$`.
    - Add variable `$commentCreator$` to only get the author of the comment.

* Stock correction: it is now possible to apply a correction on any product even if the product is not available in the stock location.
* Move Line: When using "multiple simulate" feature to simulate moves, fix move lines so they are correctly filled with partner and taxes during the status change.
* Bank Statement Line: in the printing wizard, filter bank details so we cannot select bank details without company.
* MRP: status of the mrp is now set to draft on copy.
* MRP demo data: in MRP configuration, changed the statuses to correctly take into account ongoing manufacturing orders for the MRP computation.
* Advanced export: fixed an error preventing the use of advanced export in partner grid view.
* Purchase Order: remove error message when generating a purchase order with a shipment mode missing a shipping cost.
* Manufacturing Order: pre-filling operations does not fill start and end date anymore, allowing them to be filled during the planification.
* Invoice: fixed an issue preventing the user to fill the type of operation when creating a new invoice with customer/supplier info not set by default.
* Product stock details: fixed an error occurring when opening stock history

If a product was used in a stock move, and this stock move was not generated by a sale order,
then an error would happen when opening the stock history of this product.

* Configurator creator: fixed constraint issue error happening during copy or import.
* Configurator creator: fixed a bug during import causing some fields to have an incorrect value.
* Message email: when sending an email, the 'To' field will now be filled with the fullname and email address instead of the name and the email address.
* Production: fixed sequence data-init.
* Geonames: improve error messages when trying to run the import with missing configuration.
* Contacts: checking duplicate and opening a contact form will not open a unusable form anymore.
* Batches: Fixed "created on" value, before it was always set on midnight.
* Stock location : fix field title for stock computation config

Change 'Don't take in consideration for the stock calcul' 
to 'Don't take in consideration for the stock computation'

* Contract: fix typo in french translation in an error message (when closing contract).

## [6.2.4] (2022-07-07)

#### Changes

* Skip expensive computation of available qty of product when possible

The dummy field `$availableQty` appears on `Product` grid views. It's computation done on 
StockMoveRepository.populate will now be triggered if and only if the boolean `_xFillProductAvailableQty`
is true.

Migration steps:
1. Locate usages of `$availableQty` in `Product` views
2. Locate the action-views using these views and set the context variable `_xFillProductAvailableQty` to true
3. Locate usages of the views from point 1 in `grid-view` attributes and set the context variable `_xFillProductAvailableQty` to true thanks to onLoad and onNew attributes of the parent view.


#### Fixed

* Payment voucher: fix display of trading name on form view.
* Sale/Purchase order: fixed an issue where a popup error was displayed to the user when creating a new order.
* Invoice: fixed an issue where duplicating an invoice without invoice line showed an error.
* Accounting situation: fixed an issue where PFP validator user was displayed when changing to a company with the PFP feature disabled.
* Invoice: fix financial discount not being retrieved from partner.
* Subrogation release: reversing an account move generated by a notification will set the subrogation release back to the status accounted.
* Payment voucher: when searching for element to pay, the list is now correctly cleared of paid elements.
* Journal: company is now correctly imported in demo data
* Accounting Report : fix an exception occurring when printing
* Move line: Add bank reconciliation panel in move line form view when opened from a move.
* Move line: fixed an issue where simulated move lines were missing account code and account name.
* Move line: fixed move lines not updated on move changed to simulated.
* Journal: fixed the display of an error popup when saving a journal without journal type.
* Manuf order: fixed an error popup displayed when opening the user reporting dashboard.
* Accounting reports: Fixed demo data where the created custom accouting report lines were associated to the wrong report type.
* Accounting report 3000: Fixed comparative with previous years.
* Sale order line: hide delivered quantity field if parent is in draft or finalized.
* User: In demo data, fix password reset email template to correctly escape special characters in the generated password.
* User: fixed an error occurring on the form view if account module was not installed.
* Sale order line: fixed an issue where a sale order line could become duplicated.
* Check Duplicate: fixed an error occurring when using the the "check duplicate" function

Objects where you could check the duplicate depending on the chosen fields (e.g. Contacts, Products...)
now correctly opens a window with the duplicated entries instead of an error message.

* Project: Creating a new customer contract from a project now correctly fills the partner.
* Purchase request: remove the filter on selected supplier catalog product.
* Sale order: Fixed an issue where the user was able to select a partner in the sale order which was not coherent with existing currency and price list.
* Sale order: Fixed a bug where clicking on the button 'compute lines with pricing scale' would not update the lines.
* Fixed asset: improve UI by preventing hidden field to be displayed during form load.
* Birt Template: fix wrong type for id parameters in demo data.

## [6.2.3] (2022-06-23)

#### Changes

* webapp: Add data.export.locale property in application.properties file.

#### Fixed

* Reimbursement: fixed bank details and move line filter by filtering by partner.
* Move line: add more control to avoid creation of move line without account.
* Move line: multi currency fields can now be correctly changed from form view
* Move line: removed duplicated analytic panel in move line form.
* Move line: corrected currency amount on move line generated by auto tax move line generation.
* Move: fixed an issue where total currency code was not updated on partner change.
* Move: fixed technical error being displayed when trying to delete a single move.
* Bank reconciliation: fixed starting balance computation.
* Bank reconciliation: fix of multiple reconcile by adding a new domain on move search query and fix total computation in wizard.
* Bank reconciliation: fixed color hilite and selection button on bank reconciliation line list and move line to reconcile list.
* Bank reconciliation: fix duplication of other bank statements during the loading process and fix the condition to display the button to run this process.
* Menu builder: fixed menu builder copy.
* Payment mode: hide the bank order panel on a payment mode when it cannot generate pending payments.
* Purchase order line: fixed NPE when trying to select supplier in supplier request tab.
* Printing settings: fix incomplete demo data for reports configuration by adding address position parameter. Also this parameter is not required anymore to run the reports as the default value "left" will be used.
* Sale order line: prevent user from modifying delivery quantity manually.
* Payment voucher: Manage multi banks for receipt.
* Employee: when a contact partner is linked to an employee, employee's company is now automatically added to the list of companies of the contact.
* Advanced export: fixed export without archived and with ids selected.
* Sale order: In form view, sale order line panel is now hidden when trading name is empty and required.
* Translation: fix typo in "Manual" french translation.
* Expense: corrected wrong bank details on bank order generated from expense.
* Inventory: we now alert the user if the stock location is missing on an inventory line when validating.
* Refund: fixed an error that occured when adding a payment with a financial discount.
* Pricing: In pricing rule, the result computed and putted in field to populate can now be used in the next pricings.
* Invoice line: corrected JNPE error on updating account.
* Partner/Invoice: fix of invoice analytic panel display.
* Product: fixed an issue where an error message was displayed when emptying product pull of date.
* Stock config: when the parameter `displayLineDetailsOnPrinting` is turned off, quantities are now correctly aggregated by product.
* Backup: fixed form view of backup for more consistency.

## [6.2.2] (2022-06-10)

#### Changes

* Sale order email template: added unit in sale order line details.
* Webapp: add `data.export.locale` property in application.properties file.

#### Fixed

* Fixed first time record loading being slow on grid after server start due to bpm engine initialization.
* Stock location: Allow to print location financial data only for stock locations configured as valued stock location.
* Accounting batch: display fields related to debt recovery only when debt recovery action is selected.
* Contract batch: invoicing a batch of contracts does not take into account closed contract anymore.
* Studio: Fix UI and other changes for studio editor.
* Studio: Fix label translation for editor.
* Studio: Auto convert field name and model name to camelcase for custom fields and custom models.
* Sale order/Invoice printings: fix "Dr." civility that was displayed as "Mlle.".
* Base, HR: add missing french translations.
* Move: fix partner not being filtered according to payment mode compatible types.
* Weighted average price in product company is now correctly computed in company currency instead of purchase currency.
* Expense line: Analytic distribution date is now set to expense line's expense date if it is setted. If not, it is still by default today date.
* MRP: fixed an issue where quantity conversion for mrp lines from manuf orders was not applied.
* Accounting move line: fix the date's validity check, when the date was equal to the period's starting date or the endind date, it was considered being outside of this period.
* Journal: added valid account types to demo data.
* Supplier: allow to create an accounting situation with a company that does not have PFP feature activated.
* Invoice: fixed an issue where printing an invoice from the form view would give an error.
* Cost sheet: fix wrong total when computing cost from bill of materials or manufacturing orders.
* Accounting move: fix account selection when generating automatically the counterpart of a move line.

Improve error messages when the account cannot be determined when generating the counterpart of a move line, also use the default company bank details to select the right account.

* Purchase order: fix wrong total W.T. computation when generated from purchase request.
* Supplier: fixed an issue where realizing a reversed stock move would update supplier quality rating.
* Bank details: fix missing fields when created from company.
* Stock rules: fix user and team fields not displayed with alert activated.
* Expense line / Move line: Analytic distribution template is now correctly copied in move line when it is created from expense.
* Stock move : fix wrong calculation on supplier quality rating

When realizing a stock move, stock move lines with the conformity not filled were counting as non-compliant
and decreasing the supplier's quality rating.

* Invoice: fixed an issue where changing the product in a line with existing analytic would display an error.

## [6.2.1] (2022-05-27)

#### Fixed

* Move: fix total currency computation in move form view.
* Inventory: Fix error popup on creating a new inventory line with a new company.
* Move: fix error on clicking the counterpart generation button when there is nothing to generate.
* Sale order line: fixed an issue where analytic distribution validation failed because of a title line generated by the pack feature.
* Product: now correctly hide the config allowing to define shipping cost per supplier in product form if we disabled the supplier catalog.
* Invoice: fix AxelorException error when emptying the operation type on the invoice form view.
* Geoname: fix geoname import on empty databases (without demodata).
* Journal: reset the list of valid accounts on company change.
* Move: fix NPE error when trying to access the grid view with no active company configured on the user.
* Invoice: fixed an issue where a validated or ventilated invoice without trading name could not be saved.
* MRP: grid view from MRP menu will not also display MPS anymore.
* Bank reconciliation: fix error during reconciliation when a query is not configured in bank statement rule.
* Manufacturing: outsourcing management

Production process lines can now be managed without outsourcing even if the parent prod process is configured as outsourced.
When created, operation orders now are correctly configured as outsourced depending on their related production process lines.

* Payment move line distribution: Use reconciliation date instead of due date as the operation date when generating payment move line distribution.
* Invoice: fix invoice grid view to have only the customer or supplier lines according to the present filter.
* Product: fix typos in fr translations and fr help.
* Invoice: fixed an issue preventing people from ventilating completed sale order.
* ND4S export: fix error during export when existing city is missing zip code.
* Accounting: hide the menu Analytic if analytic accounting management is disabled.
* Invoice payment: correctly disable the application of financial discount on the payment if the payment date is after the financial discount limit date.
* Employee: When creating an employee, the step 'user creation' is now hidden if employee is already associated with a user.
* Accounting Report: fix demo data for accounting report types.
* Expense: move date will now be updated if we only fill kilometric expense lines.
* Move template wizard form: fixed data input wizard popup form view.
* Maintenance: fixed a bug where creating a BoM from maintenance would make it appear in manufacturing but not in maintenance.
* Notification: correctly manage refunds in payment notification process.
* Sale order line: fixed NPE error popup that occured when changing quantity without having selected a product.
* Move line: fix tax panel not displaying in the form view when tax line is empty.
* Payment voucher: fix print button display and fix truncated report.
* Translation: fix translation of "import" in French.
* Accounting Batch: fix error message during move line export when accounting report type is missing.
* Sale order: Duplicating a quotation will not copy the opportunity anymore.

## [6.2.0] (2022-05-11)

#### Features

* Account: Add financial discount feature.

The objective of this feature is to be able to manage financial discount. Financial discount has an impact on accounting (contrary to already existing commercial discount).
A financial discount can be created in Accounting/Configuration/Financial Discount and then set on the partner. On the invoice, the financial discount is selected and applied during the payment.

* Account: Add fiscal Position on purchase orders, sale orders and invoices.

* Account: Improve company tax number management.

Tax numbers can be configured for a company with a fiscal position. The correct tax number will be selected for sale and purchase orders according to fiscal position, and can be used for exports.

* Account: Add new status 'simulated' for move.

This is a new option that can be activated in accounting config. Simulated move will not be accounted, but can be used in accounting reports.

* Accounting: FEC import, fix the existing and add import FEC Type.

Fec import types can be configured form a csv file to choose separator, special fields and check moves.
Can now set a move description, company in fec import and a option to validate a move on import.
Also two panels have been added to see the generated move and generated anomalies.

* Analytic Accounting: Revamping analytic accounting for higher flexibility.

New panel to specify a default configuration that can be used in the movelines and billing lines. 
In the analytical axis, you can now select up to 10 groupings, and configure the values of the groupings for a more precise accounting management.

* Bank statement: automatic accounting of statements and automatic generation of moves related to these statements.

Moves based on the statement rules and query can be generated automatically. This feature must be activated from the bank payment application. 

* Bank statement line: Add ODS and xlsx export options for bank statement line.

New export option on bank statement line. ODS and xlsx export are available for bank statement line export.

* Purchase Order: Add advance payment feature.

The objective of this feature is to be able to manage advance payment. As it has already been done for sale order, it is now possible to make an advance payment for a purchase order. An advance payment can be invoiced on a purchase order.

* Sale Quotation: Selected lines in a sale quotation can be split into a new quotation.

* Complementary Product: Add generation type for partner complementary products.

Now complementary products can be configured on partner. Then it will be generated for every sales orders or sale order lines.

* Pricing: new engine on Sale.

Pricing is a new tool that allows you to define sales order lines' fields according to classification rules and result rules defined by groovy formulas.

* Project and Task: Main new features and changes in the project module.

The project module has been modified to provide more flexibility in the configuration of projects and tasks. It is now possible to customize the characteristics of a project and its tasks to display only what is necessary.
Project phases have been replaced by sub-projects.
An activity feed has also been added to track all the activities of a project.
A new "Current project" menu has been added to display only the data and activities of the user's current projects.

* Email Template: New button to test template.

* Partner: Add automatic blocking on customers.

Partners with late payments can now be blocked automatically by a batch.
In the configuration by company of the accounting application, in the tab "Receivables recovery", it is now possible to activate the blocking of customers with late payments.
It is possible to configure the number of days before the blocking. A respite can be configured on the debt recovery. 
When a partner is up to date on this payment, it is automatically unblocked. 
It the user is manually block, it will not be unblocked by the script.

* Processes that take too long now run in the background.

A process that exceeds the timeout configured in the base application continues in background. The user is notified when the process goes into background and when it is finished.
Implemented for MRP, closing period process, and all batches.

#### Changes

* Invoicing Project: update project invoicing batch with automatic invoicing.

Timesheet lines and tasks can be selected automatically to invoice following the configuration in app business project. For example, only tasks with configured status will be invoiced.

* Production: Timing of implementation on Work center.

Implementation time can be determined in absolute value or by script for the work center, in prod process line and prod process line configurators.

* Configurator: Add studio customs fields support.

* Configurator: Add workshop.

A workshop can be assigned to the elements (routing and BOM) generated by the configurator. The workshop can be defined by a script.

* Configurator: Manage consumed products on phase.

Add a new configurator "Configurator Prod Product" to manage consumed products on phases.
Configurators Prod Product can be added to Configurators Prod Process Line if Configurator Prod Process manage consumed products on phases.

* Configurator: Add access to parent sale order in configurator formula.

In a configurator script to fill a product (or sale order line) field, the variable `parentSaleOrderLine` can now be used to get the id of the sale order being updated.

* Configurator:
    - Prod Process Line: Add work center group
    - Prod Process Line: Add a use condition field
    - Prod Process Line: Allow to manage outsourcing depending on App Production config
    - Prod Process Line: Allow to define name, priority, stock location, description and work center with a formula
    - Prod Process: Allow to define name with a formula

* Sale Order Line and Manufacturing Order: New comment for the manufacturing order on the sale order line.

New comment can be added on the command line to be displayed when the manufacturing order is planned or started.

* Stock location: Add barcode.

Add barcode generation process to stock location:
Add related settings field for barcode generation on App Stock. Rename tracking number barcode fields in order to have a clear difference.

* Tracking number: Add barcode.

Barcodes are available for tracking numbers. Barcodes can be activated on tracking numbers from the Stock application.
It is possible to specify a barcode type for all tracking numbers or per tracking number.

* Manufacturing order: Add barcode.

Barcode is displayed in manufacturing order's reports.

* Purchase order: Reports.

Reports generated from a purchase order now display specific notes based on the purchase's lines' tax equivalences. The tax equivalences are determined from the fiscal position of the order. This behaviour was already present on sale orders and invoices and is now also available for purchase orders.

* Production: Add stock location on S&OP (PIC in french).

* Account: Employees can now be selected as compatible partner in the journal configuration.

* Traceback: Add printing option on traceback.

* MRP: Take in account subcategories.

MRP can be configured to take into account subcategories.

* DataBackup: Allow to create backup file even if no record exists.

Data backup can create a backup file even if no record exists by selecting "Generate csv file for empty tables".

* APP mobile - Stock: Add 'Inventory status select'.

Inventory status can be selected to filter which inventories must take into account in the stock mobile app.

* Inventory: Add option to include sub stock location.

* Product: Check expiration date on stock move realization.

* Mass update: Enable mass update on some fields of MetaMenu,MetaView and MetaAction.

* Sale order: Add new field to mark order as a one-off sale.

One-off sales can be excluded from MRP computation.

* Move: add a control to check if the currency is filled on the accounting move.

* Sale Order: add last reminder date and comments to sale order.

* Accounting Report: Manage subsidiary account by type for export journal entry.

* Declaration Of Exchanges: Adjustment to follow the changes in legislation (2022).

Add country origin code in the outgoing move filled with the country alpha2 code from adress linked to the original stock location of the stock move in the stock move lines.

* Invoice: Add payment delay reason.

Add payment delay reason in the accounting panel of the Invoice. This payment delay reason can be configured in Accounting > Debt recovery > Configuration > Payment delay reason.

* Printing Setting: The position of the address can be changed in the printing (right or left).

* Manufacturing Order: New tree view for multi-level planning.

* Bank Details: Add color indication on grid view.

If the balance updated date has a delai of 3 to 7 days, the field is in orange. If it's more than 7, it's in red.
If the balance is equal to 0, the field is in orange. If it's lower than 0, it's in red.

* Employee: Add Training Skill.

Training skills can be add to an employee to manage his skills.

* Move: New date of reversion entry for reverse move.

The reversion date can now be set for the next day during generating a reverse move.

* Studio: Add canSearch and height attribute to dashlet builder.

* Tracking Number Configuration: add a dashlet to show the concerned products.

* Project task: new editor.

* Purchase Order Line: Add tracking on desired and estimated delivery date.

* Purchase Order: Add tracking on delivery date.

* Partner: Add tags of Customer, Supplier, Subcontractor and others on card view.

* Account: Manage Partners DAS2 Activity and Accounts Service Types.

* Account: Add two new reports which help for DAS2 Fees declaration.

* User Preference: Display the today date in recipe mode.

* Partner: Add Tracking for some fields.

Tack partner category, industry sector, registration code, siren number, main activity, partner address list, fixed phone, email address, in payment mode, out payment mode, payment condition, financial discount, das2 activity, currency, umr activation.

* BankDetails: Add Tracking for some fields.

Tack partner, owner name, label, currency, bank account, journal, iban, bank, bank address, active.

Invoice : Add Tracking for some fields.

Tack pfp validator, pfp validate status, reason of refusal to pay, reason of refusal to pay (str).

* Stock Location: Valued.

The valuation of the stock location is displayed if the stock location is configured as valued instead of depending on the stock location type.

* Indicator generator: Query will now return result as a list of text instead of number.

* Move: fill the origin date from the invoice.

* User: Add update date of password in user-form.

* Stock detail by product: Allow to see stock history when no stock location is selected.

* Stock: New batch to update stock history.

* Stock detail by product : Allow to export result as a csv file.

* App base: Use open street map as the default map API.

* Invoice Printing: Display delivery dates on customer invoices/refunds reports.

* Product: Allow the configuration of a BOM by default per company.

* Product: Add column available quantity on variants panel on product form view.

* ManufOrder: Allowed user to select and discard multi-level planning orders.

* Partner: Add the create and the update date in blocking editor.

* Wap history: Add a new field to show the origin of a wap variation in a stock location line form view.

* Global tracking log line : Add new button that displays the record reference.

* Invoice: interco invoice is now generated after ventilation. Before this change, it was generated after validation.

* Manufacturing order: add buttons to use the reservation feature for consumed products.

* Payment delay reason: New demo data.

* User: change visibility condition of the Passed For Payment panel in user form view.

The panel is now displayed if at least one company in the set of companies is activated for the "Past due for payment" function, instead of using the active company.

* MRP: Add a configuration to choose whether to run the process only on given stock location or on every sub stock locations as well.

* Accounting report: Add order by in general balance with comparative periods report.

* Move line: Avoiding saving or validating a move line if account not allowed

When saving a move line, added a control on accounting account that checks if the account is allowed in the move's journal.

* Accounting Batch: merge existing batches annual accounting into one.

The batch to open annual accounts of the next year and the batch to close annual accounts of the last year are now replaced by a single batch that will do both.

* Invoice: fill more fields on supplier invoice generated from interco feature

When generating a supplier invoice from a customer invoice with interco feature, we now automatically fill origin date and supplier invoice number, and we copy the customer invoice printed PDF into supplier invoice files.

* Account: Improve error message when adding a payment mode that already exists.

* Fiscal position: Handle fiscal position and tax number when merging quotations

* Currency conversion line: Error handling if rates or currency cannot be retrieved.

* Payment voucher: Allow to choose the imputation order

Add sequence in pay voucher due element in order to choose the imputation order when loading lines. 
User can now change imputation order by changing sequence before loading lines.
Sequence can be change by moving line or by editing it.

* Forecast Recap: Display manual forecast reason on recap lines.

Forecast reason was replaced by Forecast recap line type: The name of the
forecast line type is now the reason. This way, the type displayed in recap
will be equal to the reason for manual forecasts.

#### Fixed

* HR: Day Planning - Rename weeklyPlann to weeklyPlanning.

`WRN :` Script need to be launch to upgrade version.

#### Removed

* Territory: Remove object


[6.2.9]: https://github.com/axelor/axelor-open-suite/compare/v6.2.8...v6.2.9
[6.2.8]: https://github.com/axelor/axelor-open-suite/compare/v6.2.7...v6.2.8
[6.2.7]: https://github.com/axelor/axelor-open-suite/compare/v6.2.6...v6.2.7
[6.2.6]: https://github.com/axelor/axelor-open-suite/compare/v6.2.5...v6.2.6
[6.2.5]: https://github.com/axelor/axelor-open-suite/compare/v6.2.4...v6.2.5
[6.2.4]: https://github.com/axelor/axelor-open-suite/compare/v6.2.3...v6.2.4
[6.2.3]: https://github.com/axelor/axelor-open-suite/compare/v6.2.2...v6.2.3
[6.2.2]: https://github.com/axelor/axelor-open-suite/compare/v6.2.1...v6.2.2
[6.2.1]: https://github.com/axelor/axelor-open-suite/compare/v6.2.0...v6.2.1
[6.2.0]: https://github.com/axelor/axelor-open-suite/compare/v6.1.11...v6.2.0
