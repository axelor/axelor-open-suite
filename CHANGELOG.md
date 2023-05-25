## [5.4.42] (2023-05-25)

#### Fixed

* Invoice: fixed an error preventing from merging invoices.

## [5.4.41] (2023-04-27)

#### Fixed

* Stock move: fixed an error occurring when emptying the product in a line.
* Group Menu Assistant: fixed an issue where an empty file was generated.

## [5.4.40] (2023-04-20)

#### Fixed

* Partner: fixed script error when opening partner contact form (issue happening only when axelor-base was installed without axelor-account).
* Customer/Prospect reporting: fixed an error occuring if we only have axelor-base installed when opening the dashboard.
* Stock move: fixed an issue when opening stock move line form from the invoicing wizard.
* Stock move: now prevent splitting action on stock move line that are associated with a invoice line.
* Invoice: fixed filter on company bank details for factorized customer so we are able to select the bank details of the factor.
* Invoice: to avoid inconsistencies, now only canceled invoices can be deleted.
* Bank details: fixed script error when opening bank details form (issue happening only when axelor-base was installed without axelor-account).


## [5.4.39] (2023-04-06)

#### Fixed

* Stock: fixed an issue in some processes where an error would create inconsistencies.
* Contract: fixed an issue in some processes where an error would create inconsistencies.
* Sale: fixed an issue in some processes where an error would create inconsistencies.
* App base config: added missing french translation for "Manage mail account by company".
* Sequence: fixed sequences with too long prefix in demo data.
* Base batch: Removed "Target" action in form view as this process does not exist anymore.
* Company: correctly hide buttons to access config on an unsaved company. 
* Message: fixed a bug that could occur when sending a mail with no content.
* Menu: fixed menu title from 'Template' to 'Templates'.
* Json Field: added missing field 'readonlyIf' used to configure whether a json field is readonly.
* Accounting report journal: fixed report having a blank page.
* Manufacturing order: fixed an issue where emptying planned end date would cause errors. The planned end date is now required for planned manufacturing orders.
* Manufacturing order: in form view, fixed buttons appearing and disappearing during view load.
* Project: fix errors occuring when module business-project was not installed.

## [5.4.38] (2023-03-23)

#### Fixed

* Production: fixed an issue in some processes where an error would create inconsistencies.
* Bank payment: fixed an issue in some processes where an error would create inconsistencies.
* Account: fixed an issue in some processes where an error would create inconsistencies.
* HR: fixed an issue in some processes where an error would create inconsistencies.
* Stock move: fixed an error occurring when opening a stock move line in a different tab.
* Stock move: supplier arrivals now correctly computes the WAP when the unit is different in stock move and stock location.
* Stock move: fixed an issue where "to address" was not correctly filled on a generated reversion stock move.
* Mail message: fixed an issue preventing follower selection after a recipient has already been selected.

## [5.4.37] (2023-03-09)

#### Fixed

* Employee: in the user creation wizard, prevent validation when login is empty or if the activation and expiration dates are inconsistent.
* Base: fixed an issue in some processes where an error would create inconsistencies.
* Accounting report: fixed error preventing analytic balance printing when the currency was not filled.
* Business: fixed an issue in some processes where an error would create inconsistencies.
* Batch form: close the popup to show the invoice list when the user is clicking the "show invoice" button.
* CRM: fixed an issue in some processes where an error would create inconsistencies.
* Stock rules: alert checkbox is no longer displayed when use case is set to 'MRP'.
* Freight carrier mode: fix typo in french translation.
* Sale order: allow to select direct order stock locations from the company as the sale order stock location.
* Project: fixed the display of description in Kanban view.
* Invoice: does not block the advance payment invoice validation if an account is not selected on the invoice.
* Invoice: printing a report will open the correct one even if another report have the same file name.

## [5.4.36] (2023-02-24)

#### Fixed

* Accounting report: fixed advanced search feature on grid view.
* Stock move: fixed error on invoice creation when we try to invoice a stock move generated from a purchase order.
* Stock move: fixed error message when trying to create partial invoice with a quantity equals to 0.
* Leave request: fixed the message informing the user of a negative number of leaves available.
* Product: fixed an error occurring when accessing a product form view.
* Supplychain: improved error management to avoid creating inconsistencies in database.
* Unify the sale orders and deliveries menu entries: now the menu entries at the top are the same as the menu entries at the side.
* Advanced export: fix duplicate lines when exporting a large amount of data.
* Invoice: fixed an error that happened when selecting a Partner.

## [5.4.35] (2023-02-14)

#### Fixed

* Ticket type: fixed error preventing to show chart information on opening a ticket type form.
* Meta select: fixed wrong french translation for "Order".

## [5.4.34] (2023-02-03)

#### Fixed

* User: fixed active user being able to change its user code.
* Sale order: fixed a bug where doing a partial invoicing in percentage could not work.
* Company bank details: fixed company bank details filter in invoice and purchase order form views.
* Stock move: fixed report not showing manually changed address for customer delivery.
* Product: updated condition for default value on product pulled off market date.
* Subrogation release: fixed an issue where supplier invoices were retrieved with customer invoices.
* Purchase order: duplicating a purchase order now correctly resets budget.

## [5.4.33] (2023-01-19)

#### Fixed

* Stock move: fixed a regression were it was impossible to select any product on internal move.
* Contracts: fixed an issue where, when filling a product in a contract line, "missing configuration" errors were not shown to the user.
* Accounting batch: fixed "PersistenceException" error preventing the execution of the batch when the list of opening account was empty.
* Accounting reports: UI improvement for the form view (hide "global" and "global by date" fields for General balance and Partner balance).
* Quality measuring point: set a minimum of 1 for the coefficient.
* Invoice: when generating an interco invoice, the generated supplier invoice now takes the correct company bank details.
* Address: fix UI issue where changing the zip code was emptying the city.

## [5.4.32] (2023-01-05)

#### Fixed

* Bank details: improve message when confirming a bank order and a receiver bank details is inactive.
* Expense: fixed an issue where ground for refusal was hidden even if it was fill.
* MRP: fixed an infinite loop issue

An infinite loop occurred when, for a product, the available stock was less than the minimum quantity of its stock rule
and this product was used in a purchase order.

* Invoice: reject company bank details when default bank details is not active.
* MRP: fixed NPE error that could happen during MRP computation if a operation order had a null planned start date.
* Cost sheet: fixed wrong human cost calculation when computing a cost sheet for a in progress manufacturing order.
* Manufacturing Orders: during manfacturing order generation, added a more explicit message if the bill of materials has 0 quantity.

## [5.4.31] (2022-12-16)

#### Fixed

* Leave request: fix error message when sending leave request when company is missing.
* Partner address: improve UI when adding an address to a partner to avoid inconsistencies.
* Debt recovery history: prevent the user from inserting new rows in grid view.
* Bank Order: Payment mode and file format now are correctly reset when order type select is changed.
* Supplier invoice: fixed an issue where the button to pay the invoice was not displayed.
* Stock move: picking order comments panel are now correctly hidden for supplier arrivals.
* Product details: fixed permission so users do not need write permissions on Product to use this feature.
* Stock move: fixed query exception that happened on the form view when the user had no active company
* Contract / ContractVersion: allow to correctly fill dates when 'isPeriodicInvoicing' is activated on a new contract version.
* Expense: now takes the correct bank details when registering a payment.

## [5.4.30] (2022-12-08)

#### Features

* Invoice: add new French statements in Invoice Report depending of product type.
* Invoice: add partner siret number in French report.

#### Fixed

* User: email field is now correctly displayed

Email field is now displayed in editing mode.
Email field is now displayed in readonly mode only if a value is present.

* Leave request / employee: a HR manager can now access draft requests for other employees from the menu.
* Stock move: changing the real quantity of a line will not reset the discounted price to the original price anymore.

When generating a Stock move from a Purchase order, if a line had a discount,
changing the real quantity of a discounted line would set the price to the original one. This wrong behavior is now fixed.

* Sale order: fixed the automatic filling when partner change for the field 'Hide discount on prints' according to it value in the price list.
* Mass invoicing: fixed a bug where some stock moves could not be invoiced when the invoice was created from a merge.
* Accounting batch: Fix the batch to realize fixed asset line so it correctly update only the fixed asset lines from the selected company.
* Operation order: fix a blocking NullPointerException error when we plan an operation order without a machine.
* Sequence / Forecast recap: Fix demo data sequence for the forecast recap.
* Stock move: details stock locations lines will not longer be generated with zero quantity when the stock move is a delivery.
* Bank Order: now can always sign bank orders even without EBICS module.
* Expense: when paying expense, correctly fill sender company and sender bank details in the generated payment.
* Stock move: if the configuration is active, services (product) are now correctly managed in stock moves.

## [5.4.29] (2022-11-25)

#### Fixed

* Invoice: fixed an error that could happen when creating a invoice with no active company for the user.
* Invoice: fixed an error preventing any payment on a duplicated invoice.
* Purchase order: fixed purchase order printing so it is not showing negative lines as discount.
* Product: procurement method is not required anymore.
* Bank order: created a more explicit error when creating a move for invoice payment if it fails on realization of bank order.
* Invoicing project: fixed blocking error (JNPE) when timesheet line product is not filled.
* Year: fixed demo data for periods generated from fiscal year, now the periods generated from demo data last one month instead of one year.

#### Removed

* Global tracking: remove read feature selection in views.

## [5.4.28] (2022-10-28)

#### Fixed

* Invoice: generated stock move invoices now correctly copy budget distribution on product with tracking number.
* Expense: company and employee now correctly appear as mandatory in form view.
* Payment voucher: company bank details is now required if multi banks is activated.
* Invoice payment: fixed a bug where payments were pending even when without bank order.

## [5.4.27] (2022-10-21)

#### Fixed

* Project: reset sequence on project duplication.
* Move: the date is now displayed even if the move is accounted.

## [5.4.26] (2022-10-17)

#### Changes

* Fiscal year: improve UX to create new period.

#### Fixed

* Interco: fixed an error occurring when generating a sale order from a purchase order.
* Invoice report: invoice lines will now correctly display the currency when the invoice in ati option is activated.
* Logistical form: fixed a translation issue when printing packing list.
* Invoice: fixed company bank details when partner is factorized.
* Accounting batch: fill the selected currency with the default company currency.
* Prod process report: fixed ordering of prod process lines in the report, they are now correctly sorted by priority.
* Bill of materials: added an error message when company is missing during cost price computation.
* Bank order: add verification to avoid sending the same file twice to the bank on user mistake.

## [5.4.25] (2022-09-29)

#### Fixed

* Sale order: fixed an issue allowing users to invoice too much quantities when invoicing partially.
* Sale order: fixed an error preventing users from invoicing partially a sale order after refunding it.
* Move: fixed error message when trying to remove a move.
* Debt recovery: removed error message when there is no email address for a mail type message.
* Invoice: generating an invoice from a sale order or a stock move now correctly sets the project.
* Contract: remove duplicate duration values on contract templates and fix french translation in form view.
* Stock move line: fixed an issue allowing the user to fill quantity in a title line, causing inconsistencies.
* Tracking number: fixed an issue where the product was missing in a tracking number created from inventory lines.
* Tax: fixed an error occurring when choosing a product for a sale or a purchase order if the creation date was not filled.

## [5.4.24] (2022-09-15)

#### Fixed

* Invoice: fixed a bug where, when generating an invoice, the default company bank details was used instead of the one selected by the user.
* Partner: fixed a regression where the partner displayed name was first name followed by name, it is now back to name followed by first name.
* Move: fixed an issue were validating a large amount of moves would lead to a technical error stopping the process.

## [5.4.23] (2022-09-01)

#### Fixed

* Ticket: fix ticket copy by resetting correctly fields that are not configured by the user.
* Invoice: fixed an issue where invoice printing PDF file had a blank page at the end of the report.
* Invoice: fixed discount display issue in the printing.
* Fixed Asset: fixed error (JNPE) when we removed first depreciation date or acquisition date.
* Move: fixed an issue where it was not possible to reverse a move linked to a notified invoice in an accounted subrogation release.

## [5.4.22] (2022-08-11)

#### Fixed

* MRP: reset date fields when copying an existing MRP.
* Lunch voucher: fix lunch voucher computation: half-day leaves now correctly reduce number of lunch vouchers by one.
* Accounting: fixed the "Revenues vs Expenses" chart.
* Manufacturing Order: fixed an issue preventing to finish a manufacturing order with a operation order on standby.
* Expense: fixed an issue where expense ventilation did not work after setting accounting analytic template to expense lines.
* Account clearance: Fixed errors when fetching excess payments preventing the process from working correctly.

## [5.4.21] (2022-08-01)

#### Fixed

* Advanced export: fixed an error preventing partners export when trying to use the feature from partner grid view.

## [5.4.20] (2022-07-29)

#### Fixed

* Stock correction: it is now possible to apply a correction on any product even if the product is not available in the stock location.
* MRP: status of the mrp is now set to draft on copy.
* Manufacturing Order: pre-filling operations does not fill start and end date anymore, allowing them to be filled during the planification.
* Invoice: fixed an issue preventing the user to fill the type of operation when creating a new invoice with customer/supplier info not set by default.
* Message email: when sending an email, the 'To' field will now be filled with the fullname and email address instead of the name and the email address.
* Production: fixed sequence data-init.
* Contacts: checking duplicate and opening a contact form will not open a unusable form anymore.
* Batches: Fixed "created on" value, before it was always set on midnight.
* Contract: fix typo in french translation in an error message (when closing contract).
* Stock location: fix field title for stock computation config

  <details>
  Change 'Don't take in consideration for the stock calcul' 
  to 'Don't take in consideration for the stock computation'
  </details>


## [5.4.19] (2022-07-07)

#### Fixed

* Sale/Purchase order: fixed an issue where a popup error was displayed to the user when creating a new order.
* Invoice: fixed an issue where duplicating an invoice without invoice line showed an error.
* Accounting situation: fixed an issue where PFP validator user was displayed when changing to a company with the PFP feature disabled.
* Journal: fixed the display of an error popup when saving a journal without journal type.
* Manuf order: fixed an error popup displayed when opening the user reporting dashboard.
* Sale order line: hide delivered quantity field if parent is in draft or finalized.
* Check Duplicate: fixed an error occurring when using the the "check duplicate" function

Objects where you could check the duplicate depending on the chosen fields (e.g. Contacts, Products...)
now correctly opens a window with the duplicated entries instead of an error message.

* Project: Creating a new customer contract from a project now correctly fills the partner.
* Purchase request: remove the filter on selected supplier catalog product.
* Fixed asset: improve UI by preventing hidden field to be displayed during form load.
* Subrogation release: reversing an account move generated by a notification will set the subrogation release back to the status accounted.
* Birt Template: fix wrong type for id parameters in demo data.

## [5.4.18] (2022-06-23)

#### Fixed

* Reimbursement: fixed bank details and move line filter by filtering by partner.
* Menu builder: fixed menu builder copy.
* Payment mode: hide the bank order panel on a payment mode when it cannot generate pending payments.
* Bank Reconciliation: fixed duplicated other bank statements during the loading process.
* Purchase order line: fixed NPE when trying to select supplier in supplier request tab.
* Sale order line: prevent user from modifying delivery quantity manually.
* Payment voucher: Manage multi banks for receipt.
* Advanced export: fixed export without archived and with ids selected.
* Expense: corrected wrong bank details on bank order generated from expense.
* Move: fixed technical error being displayed when trying to delete a single move.
* Invoice line: corrected JNPE error on updating account.
* Product: fixed an issue where an error message was displayed when emptying product pull of date.
* Stock config: when the parameter `displayLineDetailsOnPrinting` is turned off, quantities are now correctly aggregated by product.

## [5.4.17] (2022-06-10)

#### Changes

* Sale order email template: added unit in sale order line details.

#### Fixed

* Contract batch: invoicing a batch of contracts does not take into account closed contract anymore.
* Sale order/Invoice printings: fix "Dr." civility that was displayed as "Mlle.".
* Product: Weighted average price in product company is now correctly computed in company currency instead of purchase currency.
* Purchase order: fix wrong total W.T. computation when generated from purchase request.
* Purchase order line: fill supplier code and name on purchase order lines generated from purchase request.
* Expense line: Analytic distribution date is now set to expense line's expense date if it is setted. If not, it is still by default today date.
* MRP: fixed an issue where quantity conversion for mrp lines from manuf orders was not applied.
* Supplier: allow to create an accounting situation with a company that does not have PFP feature activated.
* Invoice: fixed an issue where printing an invoice from the form view would give an error.
* Cost sheet: fix wrong total when computing cost from bill of materials or manufacturing orders.
* Supplier: fixed an issue where realizing a reversed stock move would update supplier quality rating.
* Bank details: fix missing fields when created from company.
* Stock rules: fix user and team fields not displayed with alert activated.
* Expense line / Move line: Analytic distribution template is now correctly copied in move line when it is created from expense.
* Stock move: fix wrong computation on supplier quality rating

When realizing a stock move, stock move lines with the conformity not filled were counting as non-compliant
and decreasing the supplier's quality rating.


## [5.4.16] (2022-05-27)

#### Fixed

* Sale order line: fixed an issue where analytic distribution validation failed because of a title line generated by the pack feature.
* Product: now correctly hide the config allowing to define shipping cost per supplier in product form if we disabled the supplier catalog.
* Invoice: fix AxelorException error when emptying the operation type on the invoice form view.
* Journal: reset the list of valid accounts on company change.
* Invoice: fix invoice grid view to have only the customer or supplier lines according to the present filter.
* Product: fix typos in fr translations and fr help.
* Invoice: fixed an issue preventing people from ventilating completed sale order.
* Accounting: hide the menu Analytic if analytic accounting management is disabled.
* Employee: When creating an employee, the step 'user creation' is now hidden if employee is already associated with a user.
* Expense: move date will now be updated if we only fill kilometric expense lines.
* Move template wizard form: fixed data input wizard popup form view.
* Notification: correctly manage refunds in payment notification process.
* Add checking server side for contracts, sale order, purchase request and purchase order.
* Payment voucher: fix print button display and fix truncated report.
* Sale order: Duplicating a quotation will not copy the opportunity anymore.

## [5.4.15] (2022-05-11)

#### Fixed

* Lead: Fixed a bug where converting a lead without picture resulted in `NullPointerException` error and would not convert the lead.
* Inventory: Fix an error when importing inventory demo data.
* Partner: Fix an issue where partner full name was not correctly computed.
* Stock move: Fix a bug where switching to the next stock move from unsaved record could result in a unsaved record exception.
* Manufacturing Order: Fix an issue where updating consumed/produced products panel shows an error and does not update the stock.
* Operation order: Fix stock move and consumed product panels display.
* Project: Fix an issue were it was not possible to generate multiple planning lines.
* Project: Fix an issue were timesheet lines generated from planning lines were not correctly marked as "to invoice".
* Stock correction: Fix an issue when modifying a quantity in stock would change the average price of the product in stock location.
* BANK RECONCILIATION: Translate bank reconciliation report filename.
* STOCK LOCATION: Fix `NullPointerException` error on birt report run from an external stock location (excel).
* Accounting Reports: Disable page break interval to get only one tab on excel files.
* Configurator: Fixed import popup title.
* Configurator Prod Process: Fix an issue preventing the user to configure a stock location from a formula.

## [5.4.14] (2022-04-29)

#### Changes

* Databackup: Add option to use fake data when making the backup with anonymization enabled.

#### Fixed

* Purchase Order: generating purchase orders now correctly applies the company purchase config for purchase printing.
* Lunch voucher: fixed issue where half days were not accounted in lunch voucher computation.
* Partner: fixed issue where missing sales config is blocking supplier save.
* Lead: add a server side check on every status change, preventing user mistake in case of malfunctioning views.
* Product: average price (WAP) is now correctly computed in product purchase currency.

## [5.4.13] (2022-04-15)

#### Changes

* Add an anonymization feature to the data backup feature.

Add an anonymization option to allow the person making the data backup to get a backup with the selected fields anonymized.
For the moment fields are anonymized using an hash function, a call to an API to get fake data is not implemented yet.

#### Fixed

* BankReconciliation: On company change, empty fields related to company in bank reconciliation form view.
* Account Config: Remove account clearance editor view and instead use a standard panel.
* Leave: filter out leave reasons that do not manage accumulation.
* PaymentAssistantReport: fix empty columns when ticket is printed.
* Stock and MRP: Add a server side check on every status change, preventing user mistake in case of malfunctioning views.
* StockLocation: fix print button on stock location grid view.

## [5.4.12] (2022-04-01)

#### Fixed

* Product Disponibility: add filter to remove products that are not stock managed from product set in product disponibility form.
* Production process: Fix french help message in data-init.
* BUDGET: Clear budget when purchase order is cancelled
* CONVERT WIZARD : Fix no check for potential duplicate string field
* Stock move: add server side check on status before realizing a stock move.
* Bank Reconciliation Line: Fix of errors in domains for partner and account
* Manuf Order : Fix issue on consumed products on generated manuf order from SaleOrder
* Inventory: Filling inventory line list will now correctly exclude the lines without stock if exclude out of stock option is true
* Lead: fix loading lead logo when converting a lead to a partner.
* Partner category: fix french translation.
* Move, Accounting Period: improve format of displayed dates in error message.
* Supplier stock move invoicing wizard now correctly opens invoice supplier grid.
* Invoice: Remove duplicated specific notes copied from tax when we have multiple lines.
* MetaSchedule: Fix the title for 'Service' field.
* Contact: fixed duplicate contact name warning not displayed when the contact was not saved.
* Analytic distribution required on sale order line: Fix french translation.
* Analytic distribution required on purchase order line: fix french translation.
* User: Fix missing "create partner" and "create employee" buttons when creating a new user.
* Custom state accounting report: Fix typo in french demo data (explotation -> exploitation).
* YEAR: changed demo data for period on civil year.
* Object data config: Fix the title of the popup appearing when anonymizing data.

## [5.4.11] (2022-01-26)

#### Fixed

* STOCKMOVE/INVOICING: Correctly apply unit conversion when updating invoiced quantity in stock move lines.
* INTERCO: Fix analytical distribution model of generated interco invoice.
* Move: fix wrong currency amount and rate computation in VAT move line.
* SALE ORDER: Blocking invoice generation when the full amount of the order has already been invoiced.
* STOCK: Fix filters of stock forecast menu entries.
* SaleOrderLine: Fix invoiced quantities computation when having refunds.
* Stock reservation: fix german translation of error message.
* PERIODS: Removed overlapping periods in demo data.
* LeaveLine: Fixed demo data where leave lines did not have a name.
* Sequence: Fix full name and prefix or suffix issue on data init.
* Invoice: on ventilation, now refund automatically pays the supplier invoice. This was previously only working with customer invoice.
* PrintingSettings : remove company field from PrintingSettings and remove company condition from all permissions on PrintingSettings.
* Event: fix NPE happening on start date update.
* Product: fix stock details never showing any stock location line.
* STOCK DETAILS: Fixed issue where stock locations were sometimes not properly filtered.
* Human resources: restored chart view for leave per employee.
* Expense Line: Prevent negative and inconsistent amounts in expense lines.
* Budget: Fix budget lines update on cancelling an invoice.
* Budget: Fix budget lines update on cancelling a purchase order.


## [5.4.10] (2021-11-02)

#### Changes

* STOCKLOCATION: Add new configuration to compute stock valuation by purchase value.

#### Fixed

* ACCOUNT MOVE: fix copy feature by resetting more fields during copy.
* Stock Move Line: unit price become readonly when generated from orders.
* Advanced Export: add includeArchivedRecords boolean to manage archived records.
* MRP: Use default procurement method configured in product per company.
* Lead: remove useless action called on lead creation causing issues with permission.
* FixedAsset: Fixed filter on company on Fixed asset category.
* INVOICE LINE: fixed an issue where the 'filter on supplier' field deactivated after a different modification in the form view.
* Account management: Add missing form and grid view to the field analytic distribution template.
* Product: Add unicity constraint on serial number.
* PURCHASEORDER: fixed an issue where purchase orders were wrongfully labeled as delivered.
* Forecast Recap: fix display of bank details last update balance date in form view.
* PRODUCT: Fix barcode image generation for Code\_39 and code\_128.
* Product Category: fix wrong grid view used for parent product category.
* Invoice: fix error happening during the creation of a new invoice after generating an invoice from a purchase order.
* Invoice Payment: fix NPE on payment cancel.
* Stocks: Fixed an issue where dashboards 'Upcoming supplier arrivals' and 'Late supplier arrivals' would either be empty or displaying unrelevant data.
* BANK PAYMENT BATCH: fix java.lang.NoSuchMethodException error when trying to run the batch manually.
* BatchLeaveManagement: Fix an issue where created leave lines had an empty name.
* Batch: Fixed duration which was computed in minutes instead of seconds.
* TimesheetLine: wrong rounded value fixed in working hours computation.
* Sale order line: When adding a pack on a quotation, correctly set analytic, supply and production information.
* Printings: fix printings issues when using an external birt runtime.
* PRODUCT and PURCHASEORDERLINE: fix currency conversion when updating and using last purchase price.
* Extra hours: fix typo in french translation.
* Invoice line: in advance search, fix an error where it was not possible to select the field 'budget'.
* Move: fix rounding issue display on totals during manual creation of a move line.
* Advance payment invoice: prevent refund creation.
* Move: in move grid view, fix NPE when we click on the button 'delete' without any move selected.
* Fix french translation 'Personnaliser' to 'Personnalisé'.

## [5.4.9] (2021-06-08)

#### Changes

* Email: do not block process when an error occurs on sending mail.

Automatic mail notification can be enabled on stock moves, invoices, and
manufacturing order. If we have an error when sending the message, the
process will now not be blocking but will still show the error to the user.

#### Fixed

* ManufOrder: make linked sale order sequence appears a origin in printing.
* Move reversion: Fill reversal date in analytical moveline on reversed move.
* SaleOrder: fix NPE on product selection when the current user does not have an active company.
* Invoice: fix rounding error on advance payment imputation.
* Purchase order: fix display of purchase order lines from menu entry Historical.
* StockLocation: content lines and detail lines are no longer duplicated on copy.
* Manufacturing order: fix an issue where the button to generate waste stock move was not clickable.
* TrackingNumberConfig: Fix sequence being required even if we do not generate automatically a tracking number for purchase.
* Email sending: fix NPE when sending email from scheduler.
* Sale order: fix button to print invoices from invoicing dashlet.
* MRP: fix MRP process being stuck in a loop with wrong mrp line type configuration.
* Invoice Line: Fix filter on supplier in supplier invoice.
* Product: Empty last purchase price on copy.

## [5.4.8] (2021-04-21)

#### Fixed

* Stock move multi invoicing: fix IndexOutOfBoundsException when trying to invoice a stock move with no lines.
* Sale Order: archive stock moves that are automatically cancelled when editing a sale order.
* Purchase order: fix default payment mode when generated from sale order.
* Product company: add database constraint to prevent having multiple lines in a product with the same company.
* Move line export: fix issue when exporting lines with special char in description.
* PRODUCT: fix generation of product variant sequence.
* ADVANCED IMPORT: header visibility fix.
* Invoice: fix printing when cancelling advance.
* Fix unescaped quotes in translation csv causing issue when reloading views.
* MailMessage: fix sender user always being the same for all sent messages.
* Configurator creator attributes: fix issue where `onChange` field could not be emptied.
* Purchase order: fix error due to missing parameter when generating a purchase order printing for an email.

## [5.4.7] (2021-03-17)

#### Changes

* App Mobile: add production related fields.
* CRM: Dashboards improvements:
  - Improve titles and translation.
  - Modify 'Average duration between lead and first opportunity' Chart so the data is shown by months.
* Prod process: description list can now be sorted.
* Timesheet: Auto-fill activity when using generation assistant.
* Account chart: Add 'tax authorized on move line' and 'tax required on move line' values in account demo data.
* Invoice: a product must be sellable/purchasable in order to be selected in a customer/supplier invoice line.
* Invoice printing: Move partner tax number under external reference.
* Sale and purchase orders: Fix number formatting in printings.
* Sale order: Remove autofill of 'Order date' during auto-generation of order.

#### Fixed

* Job position : add missing fr translations in form.
* Accounting move printing: fix issue where lines were duplicated.
* Invoicing project: add missing french translations in form.
* AppBase config: Add missing french translation.
* AppBase config: Add timesheet reminder batch in demo data.
* Configurator creator: fix issue where attributes from a non active configurator model are displayed in others configurators.
* Forecast recap type: fix sale order french translation to 'Commande client' instead of 'Commande'.
* Employment contract: solve export employment contract NullPointerException issue.
* Opportunity: Use next sequence code on copy.
* Move line: fix exchange rate not being computed.
* Employee: add missing french translations of form and report.
* Sale order report: qty column is displayed regardless of the line type.
* Configurator creator: fix on copy issues.
* Job application: add missing french translations in form.
* Leave request: Block the approval when a leave request is already validated.
* Product company: fix weighted average price value when lines are auto generated.
* Vehicle: fix auto fill vehicle in vehicle service log, cost and contract popup and change french translation.
* Training: add missing french translations in form.
* Expense: fix ConstraintViolationException when validating an expense.

## [5.4.6] (2021-02-08)

#### Changes

* TeamTask: Add parent task template and team task category field on task template. Improve task tree creation for project generated from project template.
* ResourceBooking: form view change.
  * Removed 'Computed automatically if left empty' tag.
  * Name is now required.
  * Added missing translation.
* Helpdesk SLA dashboards: add translation and improve filters.
* Helpdesk Ticket dashboards: improve menu, tab and dashboard titles.
* Stock Deliveries dashboard changes:
  * Display country code instead of country alpha code.
  * Change legend name and series name in 'Customer average delivery delay'.
  * Add missing translations.
  * Add Date Range feature in some dashboards.
* Company: add missing translations in company form.
* Sync Contact: Change title to Contact synchronisation.
* MANUF ORDER: add qty and unit field on grid view.
* Stock Location Reporting: change name from 'Bad stock locations line' dashlet to 'Products whose future quantity is less than the minimum quantity allowed'.
* Citizenship: change Fr translation of 'citizenship' menu
* CRM: CRM Dashboards changes.
* PURCHASE MENU: Moving suppliers map under the new Maps menu.
  * Making Suppliers Map a sub menu, and moving under new Menu Maps.
  * Changing Suppliers Map name to Suppliers.
* Human Resource: Change dashboard name to Reportings
* Quality Dashboard: improve dashlets titles.
* Departments: change departments entry menu fr translation.
* HR Dashboards: Improve french translations and hide leave per employee dashlet.

#### Fixed

* OPPORTUNITY: filter out lost opportunities in best open deals dashlet.
* Message: update french translations
* FEC Import: fetch account and journal from the company and code instead of only from the code.
* Partner: Add missing french translation for 'Customer catalog lines'
* Quality: Fix control point dashboard sql error issue.
* ACCOUNT REVERSE MOVE: When generating a reverse move, keep references to analytic move lines.
* Project: add missing translations in project planning user select wizard form
* Menu: Add french translation of 'Partner price lists'
* MRP: Filter out canceled or archived sale order in sale order lines selection.
* User: Change the french translation of 'All permissions'
* Stock move: fix split into 2.

 A stock move generated from split feature now correctly keeps the link to the order that generated it.

* Global tracking log: add missing translations in wizard form.
* Stock Move: fix server error in grid view when sorting by date.
* Cost Sheet Line: Fix rounding issue happening during computation.
* Configurator Creator: prevent the creation of duplicate attribute name.
* StockMove: add french translation of 'Please select the stock move(s) to print'
* EVENT: Hidding past date warning after record is saved.
* TICKET: Fix SLA policy viewer.
* DATA CONFIG LINE: add missing translations.
* Stock Move Line: fix duplicate stock move lines appearing in sale order line delivery dashlet.
* StockConfig: all stock locations are now filtered per their company in the form view.
* COST SHEET REPORT: Hide cost sheet group column in printings when it is disabled in configuration.
* ProjectTemplate: Fix error happening when generating project with users.
* AccountingReport: fix detailed customer balance printing being empty.
* Fix Event calendar tab name's translation.
* ADVANCE IMPORT: add missing translations in advanced import form.
* Move: fix wrong form view opened on reversing a move.
* Team task: Hide 'book resource' button if resource management is not activated.
* StockRules: Filter message template configuration so we can only select stock rules template.
* Stock Move: fix split by unit duplicating stock move lines.
* Convert demo file: add missing translation in wizard form.
* PRODUCT CATEGORY: Add translation for tree view.
* Production batch: add production batch tab french translation.
* Stock move mass invoicing: correctly generate a refund when the price is 0.
* Purchase Manager Dashboard: fix accounting family not displayed in 'Pos volume by buyer by accounting family'
* PROJECT PLANNING TIME: Add translations for editor view.
* Cancel Reason: Add missing french translation for 'free text'.
* Printing settings: add missing translations in form view.
* TEAM TASK CATEGORY: Fix wrong french translation for form view tab.
* Add 'Project/Business (Project)', 'Job Application' and 'Job applications' french translations.
* Data Backup: update missing translations.

#### Removed

* Menu: Remove purchase orders entry menu under reportings.

## [5.4.5] (2021-01-11)

#### Changes

* Project: Add a button to delete team task from task tree.
* Configurator: check the condition before generating sub bill of materials
* ACCOUNTING REPORT: add new filters for analytic reports.
* ACCOUNTING REPORT: add in analytic general ledger origin and description to analytic lines.
* Update spanish translation.

#### Fixed

* Configurator: allow groovy string interpolation in scripts.
* Global tracking: fix script in demo data to avoid NPE.
* Invoice: Set due date readonly when selected payment condition is not free.
* TEAMTASK: Fix type default value.
* FORECAST RECAP: In the forecast recap view, the type of forecast displayed is correct now (before it was always ingoing transaction).
* Move Template: fix french demo data.
* MRP: Fix english typo and add missing french translation.
* MRP PRINTING: fix empty unit column.
* MRP: fix null pointer exception when running calculation on missing procurement method.
* Manuf Order: fix operation order name.

Fix issue where the operation order name starts with null when generated from a production order.
Update operation order name with manufacturing order sequence when the manufacturing order is planned.


## [5.4.4] (2020-12-04)

#### Changes

* Reconcile: change amount field title.
* EMPLOYEE: add french translations for employee resume printing.
* DebtRecovery: Sorting grid based on date, company and partner.

#### Fixed

* Timesheet, Expense: Fix filter after clicking "Show timesheets/expenses to be validated".
* Project grid view has been improved and displays now project progress and status.
* Company: Prevent having twice the same active bank details.
* Reconcile Group: add missing translation.
* Fix opportunity in demo data missing sequence value.
* INVOICE: Add product name translation in printing.
* App Production: add missing translations.
* PRODUCT: fix wrong quantity display in production information "Where-used list", in product form.
* Campaign Reminder: Add missing translation.
* Team Task: Cannot chose a closed project from task project view anymore.
* Stock Move: Generate line for each 'Purchase Qty by tracking' without considering boolean 'Generate new purchase auto tracking Nbr'.
* Configurator: link the generated bill of materials to the generated sale order line even if we do not generate a product.
- Frequency: fix the years of a frequency.
* Configurator Creator: correctly show only the name and the button on a new configurator creator form.
* Demo data: do not set products cost price to 0 when importing bill of materials components.
* INVOICE REPORT: Fixing period format.
* Campaign: Add missing translation.
* Configurator: add missing special variables in script.
* Cost Sheet Line: fix error if the product has no prices configured and is not purchasable.
* Product Company: fix missing values in demo data.
* Campaign: Hide tool button if all child items are hidden.
* Leave request report: Manage the case where there are multiple leave requests for a single day.

## [5.4.3] (2020-11-17)

#### Changes

* MRP: add error log panel.
* USER: Add boolean to display the electronic signature on quotations.

#### Fixed

* Invoice and Purchase Order: Set project to invoice and purchase order lines when generated from sale order.
* PurchaseOrder: Fix error on requesting due to missing production module field in report.
* App: prevent user from deleting or adding an app from interface.
* Invoice: changed error message when ventilating an invoice anterior to the last ventilated invoice.
* FORECAST RECAP LINE TYPE: when the type is changed, the operation type field become empty.
* FORECAST RECAP LINE TYPE: the title Operaton Type is replaced by Operation type and its french translation has been added.
* CONVERT LEAD WIZARD FORM: Add partner information translation.
* ADVANCED EXPORT: Extended selections are not exported.
* Bank Statement Lines: line color is now accurate when importing a bank statement.
* Inventory: Add missing fields in demo data.
* PAYMENT: Fix error message when multi bank details is enabled.
* FORECAST GENERATOR: Copying a forecast generator keeps only the values of fields filled at its creation and resets the other fields.
* Opportunity : Fix sequence on demo data
* Partner: Hide the generate project button in partner contact form view.
* Inventory: Add missing translations, fix header display and add inventory sequence on each report page.
* Stock Move Line: fixed conversion issue when changing values in editable-grid and form view.
* Stock Move: fix location planned quantity not updating on some cases on real quantity change in planned stock moves.
* Prevent NPE on current user when some services were called from a scheduler.
* Analytic Move Line: Change project depending on the state of the parent order or invoice.
* EMPLOYMENT CONTRACT: fixed EmploymentContractTemplate doesn't exist error when printing
* AppCrm: Change french translation of the configuration to display customer description in opportunity.

## [5.4.2] (2020-10-23)

#### Features

* Account: Add import of FEC file exported from accounting export.

#### Changes

* Partner stock settings: add default external stock location.

Add default external stock location in partner configuration, that will be
used as a destination for sales and a from location for purchases.

* EbicsUser: Manage fields visibilty.

In EbicsUser form view display serial number (CORP) and show required
password only if user type is signatory and ebics partner mode is ebics TS,

* TICKET: Addition of color on selection in ticket grid and form view.
* QUALITY ALERT: Addition of color on selection in quality alert grid and form view.
* ANALYTIC MOVE LINE: date and analytic account are now mandatory.
* ManufOrder: Add color to priority field.
* ANALYTIC MOVE LINE: add tracking on all fields.
* DEBT RECOVERY METHOD LINE: make debt recovery level required.
* In Sale order and Stock move, hide/readonly allocation related entities for product type service.
* Project: When generating sale order from project changed name of the generated tab from 'Sale order' to 'Sale quotation'.
* ACCOUNTING MOVE: change the debit and credit field positions in total calculation form view.
* Invoice: set unit price value according to hide discount value for invoice report.
* LEAVELINE: change menu name 'All employees's leave lines' to 'Leave accounts'.
* SALE ORDER: Make visible some fields on sale order finished in date panel.

#### Fixed

* Fix Employees and expenses issues.
  - On kilometric log, the total distance travelled is now updated only if the expense line is added.
  - The kilometric log now has an unique constraint on employee and year.
  - Error message when missing year has been improved to display the year's type.
* ACCOUNTING BATCH: corrected conflict between boolean isTaxRequiredOnMoveLine and closure/opening accounting batch.
* Demo data: fix ICalendar permission that were not working.
* MRP: Stop the MRP computation if a loop in bill of materials component is happening.
* PARTNER: corrected "employee field doesn't exist" after loading a partner if human-resource module is not installed.
* Sale Order Report: fix warning appearing when launching the report.
* Fix `cannot be cast` exception on deleting some objects.
* YEAR: corrected sql error and hibernate error when closing a fiscal year.
* Copy analytic move lines when generate invoice from saleorder and purchaseorder.
* LogisticalFormLine: Fix stock move line domain.

## [5.4.1] - 2020-10-05
## Improvements
- USER FORM: Add search feature on user permission panel.
- Sale Order: Set team according to sale config.
- Stock move: "Refresh the products net mass" button placed in the "Tools" menu.
- Sale Order / Stock Move: remove available status for service type products.
- Declaration Of Exchanges: corrected wrong translation for product type select.
- ACCOUNTING REPORT: Rework fixed asset summary report.
- Stock config: set all the booleans of the stock move printing settings section by default to true.
- Base App Service: Manage correctly timezone in date fields without time using company configuration.
- DEBT RECOVERY CONFIGLINE: Partner category is now mandatory in debt recovery configuration.
- EMPLOYEE: add tracking on most fields.

## Bug Fixes
- Move: Fix NPE on date change.
- BATCH RH: corrected payroll preparation batch, now the batch is runnable.
- Studio: Manage Custom model menu in export / import operation.
- Fix concurrent modification error when adding contact to customer.
- Account management: Fix visibility issue on product and product family fields.
- Stock Move status change: improve performance on cancelling and planning stock move.
- Opportunity: Add sequence on demo data.
- BANK ORDER: Fix NPE when validating a bank order.
- Partner: fix supplier quality rating not being synchronized with supplier quality rating widget in partner form.
- LOGISTICAL FORM: Fix exception translation.
- Invoice Refund: fix refund not updating invoiced state of stock move and orders.
- Logistical form: Remove duplicate status select.
- SaleOrderLine: Not showing picking order info for services.
- FORECAST RECAP: Fix filter on invoices which was using bank details instead of company bank details.
- Tracking number: Fix wrong form view on search.
- Invoice: correctly hide discounts on the printing if the option is active.
- Account Equiv: fix NPE and make accounts fields required.
- Databackup: remove transient fields from backup.
- SMTP Account: the user can now configure the sending email address instead of using the login.
- App Supplychain: Hide configuration 'Block deallocation on availability request' if 'Manage stock reservation' is disabled.
- Stock location line: Fix display issue of button text on popup.

## [5.4.0] - 2020-09-16
## Features
- Add global tracking log feature.
- Update to Axelor Open Platform 5.3.
- Update to Gradle 5.6.4.
- HR: Added a leave line configuration menu in leave management.
- Move template: Add boolean to validate automatically the wizard generated moves.
- Move template: Add journal field to wizard.
- Move template: Add new field description.
- Move Template: Add totals for amount type.
- Move template: Add details button to grid view to display fields.
- Move template: Wizard dataInputList is now an editable grid.
- Move template: Add change track on update.
- Move template: Add demo data.
- Add CSRF protection for timesheet line editor and project planning editor.
- SUPPLIER PORTAL: Creation of supplier portal.
- Reports: Manage locale, date format and timezone following company configuration.
- Product: add products per company to configure different prices depending on the company.
- Studio: Add CSRF protection for every request header.
- Studio: Add support of menu creation for custom model and BPM node.
- Studio: Selection creation/update support.
- Studio: Added a selection builder to update existing selection or to create a new one.

## Improvements
- Invoice: Change buttons color.
- User: add field in user-form to force a password change for the user.
- QUALITY TAG: add field to configure the color of a quality tag.
- LEAD, TICKET, OPPORTUNITY: hide 'Take charge' button in grid view if the assigned user is the current user.
- MRP: hide generate proposal button on mrp line grid when the line is not a proposal.
- PRODUCT: Add json field "productAttrs" displayed in main product form view.
- HRConfig: Import formula variables in demo data.
- Product: add product image in grid view.
- INVOICE : Added the possibility to add a watermark to the printings.
- BPM: Add overview of the result of the wkf.
- MRP: add configuration to ignore end date on incoming mrp line type.
- Bank details: Add new fields journal and bank account.
- EMPLOYEE: set seniority date to hire date by default when hire date is filled.
- DURATION: add new field applicationType to know on what record the duration is used.
- USER: Default User language is based on application.locale from application.properties.
- BASE: Cache memory performance improved by not stocking geographical entities anymore.
- Accounting move line: When creating a new line the default debit or credit is set in order to balance the move.
- Accounting Move Line: When debit/credit is filled the other field is set to 0 instead of being set to a readonly mode.
- Invoice/Orders: The printing filename has been changed to show the id of the printed order/invoice.
- Employee: renamed dateOfHire, timeOfHire, endDateContract, dateOfBirth fields to hireDate, hireTime, contractEndDate, birthDate.
- Removed block permission from demo data.
- SaleOrder/Invoice/PurchaseOrder Line: Unit is now required.
- TeamTask: add new field categorySet to link multiple categories to a team task.
- Studio: Make app builder optional.
- Invoice/Sale OrderReport : Fix unit translation.
- Forecast Recap: default value set to today for from date field.
- Product: Added possibility to add a color to the product variant value tag with the field displayColor.
- Partner: Deleting partner will not remove linked employee.
- Journal: Improve balance calculation.
- Invoice: Addition of new field to display deliveryAddress on form and in report.
- EBICS PARTNER: added tracking to the fields of the ebics partner object.
- Move: It is now possible to change date until the move is validated.
- Employee: added a view to the employee's main contract company in the top right in view form.
- EbicsUser: Enable searching on requestLog dashlet.

## Bug Fixes
- HR: A leave-line cannot be saved whitout employee or leave-reason.
- Lead: Fix city name and state name issue in report printing.
- Studio: Add and fixed attributes of model and fields for import and export app.
- Bank reconciliation: add management of case of several account management for account domain, journal domain, auto change of journal and cash account fields and now account and journal from bank details are prioritized.
- Invoice: Fix NullPointerException when the product is not filled in invoice line.

[5.4.42]: https://github.com/axelor/axelor-open-suite/compare/v5.4.41...v5.4.42
[5.4.41]: https://github.com/axelor/axelor-open-suite/compare/v5.4.40...v5.4.41
[5.4.40]: https://github.com/axelor/axelor-open-suite/compare/v5.4.39...v5.4.40
[5.4.39]: https://github.com/axelor/axelor-open-suite/compare/v5.4.38...v5.4.39
[5.4.38]: https://github.com/axelor/axelor-open-suite/compare/v5.4.37...v5.4.38
[5.4.37]: https://github.com/axelor/axelor-open-suite/compare/v5.4.36...v5.4.37
[5.4.36]: https://github.com/axelor/axelor-open-suite/compare/v5.4.35...v5.4.36
[5.4.35]: https://github.com/axelor/axelor-open-suite/compare/v5.4.34...v5.4.35
[5.4.34]: https://github.com/axelor/axelor-open-suite/compare/v5.4.33...v5.4.34
[5.4.33]: https://github.com/axelor/axelor-open-suite/compare/v5.4.32...v5.4.33
[5.4.32]: https://github.com/axelor/axelor-open-suite/compare/v5.4.31...v5.4.32
[5.4.31]: https://github.com/axelor/axelor-open-suite/compare/v5.4.30...v5.4.31
[5.4.30]: https://github.com/axelor/axelor-open-suite/compare/v5.4.29...v5.4.30
[5.4.29]: https://github.com/axelor/axelor-open-suite/compare/v5.4.28...v5.4.29
[5.4.28]: https://github.com/axelor/axelor-open-suite/compare/v5.4.27...v5.4.28
[5.4.27]: https://github.com/axelor/axelor-open-suite/compare/v5.4.26...v5.4.27
[5.4.26]: https://github.com/axelor/axelor-open-suite/compare/v5.4.25...v5.4.26
[5.4.25]: https://github.com/axelor/axelor-open-suite/compare/v5.4.24...v5.4.25
[5.4.24]: https://github.com/axelor/axelor-open-suite/compare/v5.4.23...v5.4.24
[5.4.23]: https://github.com/axelor/axelor-open-suite/compare/v5.4.22...v5.4.23
[5.4.22]: https://github.com/axelor/axelor-open-suite/compare/v5.4.21...v5.4.22
[5.4.21]: https://github.com/axelor/axelor-open-suite/compare/v5.4.20...v5.4.21
[5.4.20]: https://github.com/axelor/axelor-open-suite/compare/v5.4.19...v5.4.20
[5.4.19]: https://github.com/axelor/axelor-open-suite/compare/v5.4.18...v5.4.19
[5.4.18]: https://github.com/axelor/axelor-open-suite/compare/v5.4.17...v5.4.18
[5.4.17]: https://github.com/axelor/axelor-open-suite/compare/v5.4.16...v5.4.17
[5.4.16]: https://github.com/axelor/axelor-open-suite/compare/v5.4.15...v5.4.16
[5.4.15]: https://github.com/axelor/axelor-open-suite/compare/v5.4.14...v5.4.15
[5.4.14]: https://github.com/axelor/axelor-open-suite/compare/v5.4.13...v5.4.14
[5.4.13]: https://github.com/axelor/axelor-open-suite/compare/v5.4.12...v5.4.13
[5.4.12]: https://github.com/axelor/axelor-open-suite/compare/v5.4.11...v5.4.12
[5.4.11]: https://github.com/axelor/axelor-open-suite/compare/v5.4.10...v5.4.11
[5.4.10]: https://github.com/axelor/axelor-open-suite/compare/v5.4.9...v5.4.10
[5.4.9]: https://github.com/axelor/axelor-open-suite/compare/v5.4.8...v5.4.9
[5.4.8]: https://github.com/axelor/axelor-open-suite/compare/v5.4.7...v5.4.8
[5.4.7]: https://github.com/axelor/axelor-open-suite/compare/v5.4.6...v5.4.7
[5.4.6]: https://github.com/axelor/axelor-open-suite/compare/v5.4.5...v5.4.6
[5.4.5]: https://github.com/axelor/axelor-open-suite/compare/v5.4.4...v5.4.5
[5.4.4]: https://github.com/axelor/axelor-open-suite/compare/v5.4.3...v5.4.4
[5.4.3]: https://github.com/axelor/axelor-open-suite/compare/v5.4.2...v5.4.3
[5.4.2]: https://github.com/axelor/axelor-open-suite/compare/v5.4.1...v5.4.2
[5.4.1]: https://github.com/axelor/axelor-open-suite/compare/v5.4.0...v5.4.1
[5.4.0]: https://github.com/axelor/axelor-open-suite/compare/v5.3.12...v5.4.0
