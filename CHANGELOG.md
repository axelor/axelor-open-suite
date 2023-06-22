## [6.2.30] (2023-06-22)

#### Fixed

* Base printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Sale order line: fixed the view, move the hidden fields to a separate panel which avoids unnecessary blank space and the product field appears in its proper position.
* Stock move: date of realisation of the stock move will be emptied when planning a stock move.
* Bank reconciliation line: prevent new line creation outside of a bank reconciliation.
* Job position: fixed english title "Responsible" instead of "Hiring manager".
* Invoice: do not set financial discount on refunds.
* Sale order: fixed discount information missing on reports.
* Stock Move: fixed a bug where future quantity was not correctly updated.
* Partner: fixed an issue where blocking date was not displayed
* Move: fixed currency exchange rate wrongly set on counterpart generation.
* Sale order: added missing case when computing invoicing state.

## [6.2.29] (2023-06-08)

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
* Accouting report view: fixed an issue where the filter on payment mode was displayed on an analytic report type.

## [6.2.28] (2023-05-25)

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

## [6.2.27] (2023-05-11)

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
* Project: Display "Ticket" instead of "Project Task" in Activities tab when the activity is from a ticket.
* Bank reconciliation: fixed initial and final balance when importing multiple statements.
* Inventory: fixed UI issue by preventing unit from being changed in inventory lines.
* Stock rules: now correctly apply stock rules when minimum quantity is zero.

## [6.2.26] (2023-04-27)

#### Fixed

* Purchase order: fixed fiscal position on a purchase order generated from a sale order.
* Stock move: fixed an error occurring when emptying the product in a line.
* Analytic Rules: added a company filter on analytic account verification.
* Group Menu Assistant: fixed an issue where an empty file was generated.

## [6.2.25] (2023-04-21)

#### Fixed

* Sale order: fixed an issue where opening or saving a sale order without lines was impossible due to an SQL error.

## [6.2.24] (2023-04-20)

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
* Bank details: fixed script error when opening bank details form (issue happening only when axelor-base was installed without axelor-account).

## [6.2.23] (2023-04-06)

#### Changes

* Webapp: update AOP version to 5.4.19

#### Fixed

* Tracking number: fix inconsistent french translation.
* Stock: fixed an issue in some processes where an error would create inconsistencies.
* Contract: fixed an issue in some processes where an error would create inconsistencies.
* Sale: fixed an issue in some processes where an error would create inconsistencies.
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
* Sequence: fix an issue where we could create sequences with over 14 characters by adding '%'
* Bank statement: fixed issue with balance check on files containing multiple bank details and multiple daily balances.
* Studio editor: fixed theme issue.
* Accounting report payment vat: fixed no lines in payment vat report sum by tax part and not lettered part.
* Payment voucher: fixed status initialization on creation.
* Manufacturing order: in form view, fixed buttons appearing and disappearing during view load.
* Project: fixed errors occuring when business-project was not installed.
* City: fixed an error occurring when importing city with manual type.

## [6.2.22] (2023-03-23)

#### Changes

* Webapp: update AOP version to 5.4.19

#### Fixed

* Tracking number configuration : 'Auto select sale tracking Nbr.' is now correctly taken into account when creating a stock move from a sale order.
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
* Move line: fixed an issue where duplicated analytic lines were generated.
* Financial discount: fixed french help translation.
* Mail message: fixed an issue preventing follower selection after a recipient has already been selected.

## [6.2.21] (2023-03-09)

#### Fixed

* Invoice: when the PFP feature was disabled, fixed an issue where the menu "supplier invoices to pay" was not displaying any invoices.
* Employee: in the user creation wizard, prevent validation when login is empty or if the activation and expiration dates are inconsistent.
* Base: fixed an issue in some processes where an error would create inconsistencies.
* Project task: fixed an issue where setting project task category would not update invoicing type.
* Mail message: use tracking subject instead of template subject when adding followers or posting comments.
* Accounting report: fixed error preventing analytic balance printing when the currency was not filled.
* Business: fixed an issue in some processes where an error would create inconsistencies.
* Batch form: close the popup to show the invoice list when the user is clicking the "show invoice" button.
* Logistical Form: filter stock moves on company on logistical forms.
* CRM: fixed an issue in some processes where an error would create inconsistencies.
* Stock rules: alert checkbox is no longer displayed when use case is set to 'MRP'.
* Freight carrier mode: fix typo in french translation.
* Sale order: allow to select direct order stock locations from the company as the sale order stock location.
* Project: fixed the display of description in Kanban view.
* HR Batch: fixed error making the batch process crash when using batch with a scheduler.
* Configurator: in the help panel for writing groovy scripts, fix external link so it is opened on a new tab by default.
* Invoice: does not block the advance payment invoice validation if an account is not selected on the invoice.
* Accounting report: it is no longer required to fill the year to generate DAS 2 reports.
* Invoice: printing a report will open the correct one even if another report have the same file name.

## [6.2.20] (2023-02-24)

#### Fixed

* Accounting report: fixed advanced search feature on grid view.
* Stock move: fixed error on invoice creation when we try to invoice a stock move generated from a purchase order.
* Stock move: fixed an error blocking stock move planification when app supplychain is not initialized.
* Stock move: fixed error message when trying to create partial invoice with a quantity equals to 0.
* Leave request: fixed the message informing the user of a negative number of leaves available.
* Followers: fixed a bug where a NPE could occur if default mail message template was null.
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
* Accounting Batch: fix trading name field display.

## [6.2.19] (2023-02-14)

#### Fixed

* Studio : Fix editor issues

Fix difficulty to insert fields. Fix crash on click on '+' icon of panel tab.

* Partner: improve email panel

Fixed an issue where permissions were not applied for emails displayed in this panel, also improve display by using a card view.

* Opportunity: lost reason is now correctly cleared when reopening a lost opportunity.
* Opportunity: fixed description on kanban view.
* Ticket type: fixed error preventing to show chart information on opening a ticket type form.
* Meta select: fixed wrong french translation for "Order".

## [6.2.18] (2023-02-03)

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
* Manufacturing order: fixed small UI issue in form view.
* Sale order printing: correctly hide discount related rows when the sale order is configured to hide discounts.
* Move consolidation: fixed errors during move consolidation happening when analytic distribution line were missing information.
* Subrogation release: fixed an issue where supplier invoices were retrieved with customer invoices.
* Account management: fixed an issue preventing any user from adding an account management of type tax in the financial configuration.
* Purchase order: duplicating a purchase order now correctly resets budget.
* Bill of materials: fixed an issue where the label 'BOM by default' was displayed on a new bill of materials.

## [6.2.17] (2023-01-19)

#### Fixed

* Inventory: Reversed Gap computation on inventory lines. Previously, the gap value was negative when the quantity found in the inventory was greater than the expected quantity. It is now positive in this case and negative if we have less quantity than expected.
* Stock move: fixed a regression were it was impossible to select any product on internal move.
* Contracts: fixed an issue where, when filling a product in a contract line, "missing configuration" errors were not shown to the user.
* Manufacturing order: fixed a bug where outsourced manufacturing orders could not be planned because 'outsourcing receipt stock location' was missing in the stock config.
* Accounting batch : fix PersistenceException when openingAccountSet is empty
* Accounting reports: UI improvement for the form view (hide "global" and "global by date" fields for General balance and Partner balance).
* Bank reconciliation: fixed a UI issue where clicking "Unselected" with a selected move line breaks the alignement of the blue buttons in the move line grid.
* Quality measuring point: set a minimum of 1 for the coefficient.
* Fixed asset: fix typos in french translation that appeared in the popup when using "Tools" in grid view.
* Complementary products: fixed an issue where the quantity of complementary products were multiplied by the quantity of the main product (when using the configuration "manage multiple sale quantity").
* Invoice: fixed an issue which removed the product name when changing the quantity in a line.
* Invoice: fixed a bug where the lines of a advance payment could be duplicated from a purchase order with reversed charge tax line.
* Invoice: when generating an interco invoice, the generated supplier invoice now takes the correct company bank details.
* Address: fixed an UI issue where changing the zip code was emptying the city.

## [6.2.16] (2023-01-05)

#### Fixed

* Bank details: improve message when confirming a bank order and a receiver bank details is inactive.
* Move: fixed an issue where description was not filled during move origin change.
* Expense: fixed an issue where ground for refusal was hidden even if it was fill.
* Manufacturing: fix form views for subcontractor deliveries and arrivals.
* Analytic distribution: add filter for analytic axis, using the account config of the company.
* Followers: Fixed a residual bug where trying to fetch a recipient would result in a exception with somes conditions
* Followers: Fixed a residual bug where trying to fetch a recipient would result in a exception with somes conditions.
* MRP: fixed an infinite loop issue

An infinite loop occurred when, for a product, the available stock was less than the minimum quantity of its stock rule
and this product was used in a purchase order.

* MRP: fix generation proposal menu for more clarity.

Generate proposal menu item now opens a wizard with 2 buttons:
One for generating all possible proposals, one for generating proposals of only selected lines.

* MRP: fixed NPE error that could happen during MRP computation if a operation order had a null planned start date.
* Move line/analytic: fixed an issue where analytic was not generated during move line generation.
* Invoice: reject company bank details when default bank details is not active.
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

## [6.2.15] (2022-12-16)

#### Fixed

* Bill of materials: fix error when accessing general bill of materials menu.
* Leave request: fix error message when sending leave request when company is missing.
* Accounting reports: add origin informations (ref and date) on general ledger and partner general ledger.
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
* Accounting report: fixed wrongly defined display condition on analytic axis, analytic account, account type fields for analytic type reports form view.
* Translation: fix wrong french translation for Ongoing.
* Purchase order: now correctly takes the default virtual supplier stock location when creating a purchase order.
* Bank statement rule: Add filter on counter part account to avoid selecting view accounts.
* Product details: fixed permission so users do not need write permissions on Product to use this feature.
* Stock move: fixed query exception that happened on the form view when the user had no active company.
* Contract / ContractVersion: allow to correctly fill dates when 'isPeriodicInvoicing' is activated on a new contract version.
* Account Management : Add filter by company on journal field.
* Expense: now takes the correct bank details when registering a payment.

## [6.2.14] (2022-12-08)

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
* Leave request / employee: a HR manager can now access draft requests for other employees from the menu.
* Debt recovery history: prevent the user from printing a report when there is no debt recovery.
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
* Bank order: Ad the possibility to sign bank orders even without EBICS module.
* Sale/Purchase order: fix the way we fetch default stock location on purchase and sale orders using partner stock settings.
* Expense: when paying expense, correctly fill sender company and sender bank details in the generated payment.
* Accounting move line: fix currency amount/rate readonly condition in the counterpart generation.
* Stock move: if the configuration is active, services (product) are now correctly managed in stock moves.

## [6.2.13] (2022-11-25)

#### Fixed

* Move line: fixed an issue preventing the currency amount computation when setting a debit or a credit.
* Bank reconciliation: show move records with status daybook or accounted only.
* Bank reconciliation: fix reconciliation of manually added bank reconciliation line.
* Invoice Line: choosing a product from a supplier catalog in an invoice line now has the same behavior as a purchase order line:

Choosing a product from a supplier catalog takes the product name from the supplier catalog.
If there is a minimum quantity, the user is alerted that the quantity is inferior than the minimum quantity.
Choosing a quantity inferior than the minimum quantity does not remove the product name anymore.

* Partner: fixed error preventing "group products on printings" configuration from being displayed correctly.
* Inventory: fixed an issue where base quantities were not updated when starting the inventory, causing inconsistencies.
* Invoice: fixed an error that could happen when creating a invoice with no active company for the user.
* Invoice: fixed an error preventing any payment on a duplicated invoice.
* Purchase order: fixed purchase order printing so it is not showing negative lines as discount.
* Purchase order: added a check on budget distributions on validation of the purchase order to warn the user if the price is not fully imputed on existing budget lines.
* Product: procurement method is not required anymore.
* Bank order: created a more explicit error when creating a move for invoice payment if it fails on realization of bank order.
* Debt recovery: The debt recovery process will now be reset to the first step if all the concerned invoices are considered as new for the recovery.
* Invoicing project: fixed blocking error (JNPE) when timesheet line product is not filled.
* Payment move line distribution: these elements are now only generated when the purchase move has a partner.
* Accounting report config line: fix in demo data to correctly import result in configuration lines for accounting report.
* Year: fixed demo data for periods generated from fiscal year, now the periods generated from demo data last one month instead of one year.

#### Removed

* Global tracking: remove read feature selection in views.

## [6.2.12] (2022-10-28)

#### Fixed

* Invoice: generated stock move invoices now correctly copy budget distribution on product with tracking number.
* Invoice: fixed an issue where a copied invoice could not be paid.
* Invoice payment: fixed a bug where payments were pending even when without bank order.
* Stock move: fixed an error preventing "generate invoice button" from appearing.
* Advanced export: fixed JNPE displaying when selecting "target" field.
* Expense: company and employee now correctly appear as mandatory in form view.
* FEC Import: fix imported move daybooking and accounting.
* Payment voucher: company bank details is now required if multi banks is activated.
* Business project: fixed an issue preventing the creation of a business project.
* Operation order: prevent creation of operation order without manufacturing order and prevent machine change.
* Analytic distribution line: use the company analytic axis configuration to filter the analytic axis on an analytic distribution line.
* Bank reconciliation: prevent dates edition when including other bank statements.

## [6.2.11] (2022-10-21)

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
* Analytic axis: improve groupings management.
* Move line: fixed an issue when copying a move line used in a bank reconciliation session.
* Sale Order and Invoice: now correctly filter selectable partner if a line is already present.
* Bank Reconciliation: fix ongoing reconciled balances computation.
* Move: fixed an error preventing to select a parte with an empty journal while creating new move.
* Stock move merge: When merging stock moves to a single invoice, the fiscal position is now correctly filled and must be the same for all orders.
* Project: reset sequence on project duplication.
* Move: the date is now displayed even if the move is accounted.
* Bank Reconciliation: help move line selection by filtering on account type.
* Accounting report: fix missing french translation in general balance printing.

## [6.2.10] (2022-10-17)

#### Changes

* Fiscal year: improve UX to create new period.

#### Fixed

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
* Stock history: fixed a bug where stock history lines were not persisted when using the batch to update the stock history.

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


[6.2.30]: https://github.com/axelor/axelor-open-suite/compare/v6.2.29...v6.2.30
[6.2.29]: https://github.com/axelor/axelor-open-suite/compare/v6.2.28...v6.2.29
[6.2.28]: https://github.com/axelor/axelor-open-suite/compare/v6.2.27...v6.2.28
[6.2.27]: https://github.com/axelor/axelor-open-suite/compare/v6.2.26...v6.2.27
[6.2.26]: https://github.com/axelor/axelor-open-suite/compare/v6.2.25...v6.2.26
[6.2.25]: https://github.com/axelor/axelor-open-suite/compare/v6.2.24...v6.2.25
[6.2.24]: https://github.com/axelor/axelor-open-suite/compare/v6.2.23...v6.2.24
[6.2.23]: https://github.com/axelor/axelor-open-suite/compare/v6.2.22...v6.2.23
[6.2.22]: https://github.com/axelor/axelor-open-suite/compare/v6.2.21...v6.2.22
[6.2.21]: https://github.com/axelor/axelor-open-suite/compare/v6.2.20...v6.2.21
[6.2.20]: https://github.com/axelor/axelor-open-suite/compare/v6.2.19...v6.2.20
[6.2.19]: https://github.com/axelor/axelor-open-suite/compare/v6.2.18...v6.2.19
[6.2.18]: https://github.com/axelor/axelor-open-suite/compare/v6.2.17...v6.2.18
[6.2.17]: https://github.com/axelor/axelor-open-suite/compare/v6.2.16...v6.2.17
[6.2.16]: https://github.com/axelor/axelor-open-suite/compare/v6.2.15...v6.2.16
[6.2.15]: https://github.com/axelor/axelor-open-suite/compare/v6.2.14...v6.2.15
[6.2.14]: https://github.com/axelor/axelor-open-suite/compare/v6.2.13...v6.2.14
[6.2.13]: https://github.com/axelor/axelor-open-suite/compare/v6.2.12...v6.2.13
[6.2.12]: https://github.com/axelor/axelor-open-suite/compare/v6.2.11...v6.2.12
[6.2.11]: https://github.com/axelor/axelor-open-suite/compare/v6.2.10...v6.2.11
[6.2.10]: https://github.com/axelor/axelor-open-suite/compare/v6.2.9...v6.2.10
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
