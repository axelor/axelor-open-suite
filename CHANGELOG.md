## [6.4.22] (2023-09-11)

#### Fixed

* API Stock: fixed an issue when creating a stock move line where the quantity was not flagged as modified manually.
* Fixed asset: fixed never ending depreciation lines generation if there is a gap of more than one year.
* Product: on product creation, fills the currency with the currency from the company of the logged user.
* Invoice: fixed wrong translations on ventilate error.
* Manufacturing API: improved behaviour on operation order status change.
* Account: fixed an issue preventing the user from deleting accounts that have compatible accounts
* Invoice line: set analytic accounting panel to readonly.
* Sale/Purchase order and stock move: fixed wrong filters when selecting stock locations, the filters did not correctly followed the stock location configuration.
* Stock move: fixed 'NullPointerException' error when emptying product on stock move line.
* Payment session: fixed generated payment move lines on a partner balance account not having an invoice term.
* Manufacturing order: fixed an issue where some planning processes were not executed.
* Move template: fixed copied move template being valid.
* Invoice term: fixed wrong amount in invoice term generation.
* Journal: balance tag is now correctly computed.
* Manufacturing order: when generating a multi level manufacturing order, correctly fills the partner.

## [6.4.21] (2023-08-24)

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
* Payment voucher: fixed being able to pay PFP refused invoice terms.
* Stock details: in stock details by product, fixed production indicators visibility.
* Business project batch: fixed "id to load is required for loading" error when generating invoicing projects.
* Period: fixed adjusting button not being displayed when adjusting the year.
* Fixed asset: fixed wrong depreciation value for degressive method.
* Payment session: filter invoice terms from accounted or daybook moves.
* Employee: fixed employees having their status set to 'active' while their hire and leave date were empty.
* Bank reconciliation: merge same bank statement lines to fix wrong ongoing balance due to auto reconcile.
* Invoice: fixed anomaly causing payment not being generated

Correct anomaly causing payment not being generated on invoice when a new reconciliation is validated  
and the invoice's move has been reconciled with a shift to another account (LCR excluding Doubtful process).

* Payment voucher / Invoice payment: fixed generated payment move payment condition.
* Payment voucher: fixed excess payment.
* Invoice payment: add missing translation for field "total amount with financial discount".
* Invoice: fixed financial discount deadline date computation.
* Sale order line: fixed a bug where project not emptied on copy.
* Stock move printing: fixed an issue where lines with different prices were wrongly grouped.
* Stock details: fixed "see stock details" button in product and sale order line form views.
* Accounting batch: corrected cut off accounting batch preview record field title cut in half.
* Invoice: fixed invoice term due date not being set before saving.
* Reconcile: fixed inconsistencies when copying reconciles.
* Tax number: translated and added an helper for 'Include in DEB' configuration.
* Leave request: fixed employee filter

A HR manager can now create a leave request for every employee.
Else, if an employee is not a manager, he can only create leave request for himself or for employees he is responsible of.

* Stock API: validating a Stock Correction with a real quantity equal to the database quantity now correctly throws an error.
* Manufacturing order: fixed a bug where sale order set was emptied on change of client partner and any change on saleOrderSet filled the partner.
* Contact: the filter on mainPartner now allows to select a company partner only, not an individual.

## [6.4.20] (2023-08-08)

#### Fixed

* Accounting batch : Improve user feedback on move consistency control when there are no anomalies
* PLANNING: Planning is now correctly filtered on employee and machine form
* SaleOrderLine: Description is now copied only if the configuration allows it
* Move : Fix automatic move line tax generation with reverse charge and multiple vat systems.
* PurchaseOrder and Invoice: Added widget boolean switch for interco field
* Invoice : Fix tax being empty on invoice line when it's required on account
* ManufOrder: Planning a cancelled MO now clears the real dates on operations orders and MO
* Product/ProductFamily : Analytic distribution template is now on readonly if the config analytic type is not by product
* Move : Fix currency amount of automatically generated reverse charge move line not being computed
* Invoice: Fixed french translations
* SOP/MPS: Fixed a bug where real datas were never increased
* Supplychain batch : Fixed bugs that prevented display of processed stock moves
* SOP/MPS: Fixed SOP/MPS Wrong message on forecast generation
* PAYMENT SESSION : corrected accounting trigger from payment mode overriding accounting trigger from payment session on bank order generated from payment session.
* Move : Fix tax computation when we have two financial accounts with the same VAT system
* Debt Recovery: Fix error message on debt recovery batch to display correctly trading name
* Move : Fixed a bug that was opening a move in edit mode instead of read only
* Period : Fixed an issue where a false warning was displayed preventing the user for re-opening a closed period
* Invoice: Fixed a bug where subscription invoice was linked to unrelated advance payment invoice

When creating an invoice auto complete advance payement invoice with no internal reference to an already existing sale order.

* Invoice : Remove payment voucher access on an advance payment invoice
* Move : Fix invoice term amount at percentage change with unsaved move
* Product: When changing costTypeSelect to 'last purchase price', the cost price will now be correctly converted.
* Bank order: Fixed a bug where bank order date was always overridden. Now bank order date is overridden only when it is before the current date and the user is warned.
* BUSINESS PROJECT BATCH: Fixed invoicing project batch

## [6.4.19] (2023-07-20)

#### Fixed

* Manufacturing order: fixed JNPE error when merging manufacturing orders missing units.
* Cost sheet: fixed wrong bill of materials used for cost calculation.
* Stock move line: fixed display issues with the button used to generate tracking numbers in stock move lines.
* Operation order: correctly filter work center field on work center group (when the feature is activated).
* Payment condition: improved warning message when modifying an existing payment condition.
* Stock move: fixed issue preventing the display of invoicing button.
* Supplychain batch: fixed an error occurring when invoicing outgoing stock moves.
* Invoice: fixed unwanted financial discount on advance payment invoice.
* Bank payment printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Product: fixed wrong filter on analytic on product accounting panel.
* Sale order: improved performance when loading card views.
* Interco: fixed generated sale order/purchase order missing a fiscal position.

## [6.4.18] (2023-07-11)

#### Fixed

* Business project: Automatic project can be enabled only if project generation/selection for order is enabled

The helper for the project generation/selection for order has been changed. To update it, a script must be executed:

```sql
DELETE FROM meta_help WHERE field_name = 'generateProjectOrder';
```

* App configuration: remove YOURS API from Routing Services

If using the distance computation with web services in expenses, users should select the OSRM API in App base config.

* Move: fixed automatic fill of VAT system when financial account is empty.
* Move: fixed duplicate origin verification when move is not saved.
* Invoice: fixed error when cancelling an invoice payment.
* Product/Account Management: hide financial account when it is inactive on product account management.
* Reconcile group: added back "calculate" and "accounting reconcile" buttons on move line grid view.
* Forecast generator: fixed endless loading when no periodicity selected.
* Invoice: fixed an error preventing the invoice printing generation.
* Stock move line: fixed a issue when emptying product from stock move line would create a new stock move line.
* Prod process line: fixed an issue where capacity settings panel was hidden with work center group feature activated.
* Inventory: fixed wrong gap value on inventory.
* Accounting report: fixed impossibility to select a payment move line in DAS2 grid even if code N4DS is not empty.
* Fixed asset: fixed move line amount 0 error on sale move generation.
* Stock rules demo data: fixed wrong repicient in message template.
* Accounting batch: fixed error when we try to run credit transfer batch without bank details.
* Sale order template: fixed error when selecting a project.
* Payment session: change titles related to emails on form view.
* Payment voucher: fixed invoice terms display when trading name is not managed or filled.
* Move: on change of company, currency is now updated to company currency even when partner filled.
* Contract: fixed an error occurring when invoicing a contract

An error occurred when invoicing a contract if time prorated Invoice was enabled and then periodic invoicing was disabled.

* Fixed asset: fixed being able to dispose a fixed asset while generating a sale move but with no tax line set.
* Move: removed verification on tax in move with cut off functional origin.
* Invoice term: Fixed company amount remaining on pfp partial validation to pay the right amount.
* Details stock location line: removed reserved quantity from the form view.
* Sale order: fixed totals in sales order printouts.
* Invoice: fixed view marked as dirty after invoice validation.
* Account printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Accounting report config line: filter analytic accounts with report type company.
* Invoice/Move: filled analytic axis on move when we ventilate an invoice.

## [6.4.17] (2023-06-22)

#### Fixed

* Sale order line: fixed the view, move the hidden fields to a separate panel which avoids unnecessary blank space and the product field appears in its proper position.
* Accounting batch: removed period check on consistency accounting batch.
* Stock move: date of realisation of the stock move will be emptied when planning a stock move.
* Move template: fixed invoice terms not being created when generating a move from a template.
* Move: added missing translation when a move is deleted.
* Bank reconciliation line: prevent new line creation outside of a bank reconciliation.
* Job position: fixed english title "Responsible" instead of "Hiring manager".
* Account: fixed misleading error message when company has no partner.
* Base printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Invoice: fixed an issue where invoice terms information were displayed on the invoice printing even when the invoice term feature was disabled.
* Base printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Invoice: do not set financial discount on refunds.
* Reconcile: fixed an issue where letter button was shown if the group was unlettered.
* Invoice: added missing translation on an error message that can be shown during invoice ventilation.
* Sale order: fixed discount information missing on reports.
* Invoice: fixed an issue happening when we try to save an invoice with an analytic move line on invoice line.
* Stock Move: fixed a bug where future quantity was not correctly updated.
* Partner: fixed an issue where blocking date was not displayed
* Move: fixed currency exchange rate wrongly set on counterpart generation.
* Accounting Batch: accounting cut-off batch now takes into account 'include not stock managed product' boolean for the preview.
* Sale order: fixed an issue when computing invoicing state where the invoiced was marked as not invoiced instead of partially invoiced.
* Trading name: fixed wrong french translation for trading name ('Nom commercial' -> 'Enseigne').

## [6.4.16] (2023-06-08)

#### Fixed

* Business project, HR printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Configurator: fixed issue where sale order line generated from the configurator did not have a bill of materials.
* Deposit slip: fixed errors when loading selected lines.
* Invoice: allow supplier references (supplier invoice number and origin date) to be filled on a ventilated invoice.
* Invoice/Stock move: fixed an issue where invoice terms were not present on an invoice generated from a stock move.
* Invoice: fixed an issue where the button to print the annex was not displayed.
* Account config: hide 'Generate move for advance payment' field when 'Manage advance payment invoice' is enabled.
* Leave request: fixed an issue on hilite color in leave request validate grid.
* Birt template parameter: fixed french translation issue where two distinct technical terms were both translated as 'Décimal'.
* Budget distribution: fixed an issue where the budget were not negated on refund.
* Move: fixed auto tax generation via fiscal position when no reverse charge tax is configured.
* Sale order line form: fixed an UI issue on form view where the product field was not displayed.
* Move line query: fixed balance computation.
* Supplier portal and customer portal: add missing permissions on demo data.
* Project: when creating a new resource booking from a project form, the booking is now correctly filled with information from the project.
* Partner: correctly select a default value for tax system on a generated accounting situation.
* Move line: prevent from changing analytic account when a template is set.
* Move/Holdback: fixed invoice term generation at counterpart generation with holdback payment condition.
* MRP: UI improvements on form view by hiding unnecessary fields.
* Stock: fixed an error occurring when updating stock location on a product with tracking number.
* Cost calculation: fixed calculation issue when computing cost from a bill of materials.
* Tracking number: fixed an issue preventing to select a product on a manually created tracking number.
* Reconcile: fixed an issue were it was possible to unreconcile already unreconciled move lines.
* Fixed asset: fixed JNPE error on disposal if account config customer sales journal is empty.
* Accouting report view: fixed an issue where the filter on payment mode was displayed on an analytic report type.

## [6.4.15] (2023-05-25)

#### Fixed

* Production, Purchase, Quality printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Invoice payment: disable financial discount process when the invoice is paid by a refund.
* Accounting batch: fixed close annual accounts batch when no moves are selectable and simulate generate move if needed.
* Configurator: fixed an issue where removing an attribute did not update the configurator form.
* Tax: fixed tax demo data missing accounting configuration and having wrong values.
* Sale order: fixed an issue during sale order validation when checking price list date validity.
* Invoice payment: update cheque and deposit info on the invoice payment record when generated from Payment Voucher and Deposit slip.
* Purchase order: fixed an error occurring when generating an invoice from a purchase order with a title line.
* Accounting batch: fix duplicated moves in closure/opening batch.
* Bank reconciliation: fixed an issue in bank reconciliation printing where reconciled lines still appeared.
* Bill of materials: fixed creation of personalized bill of materials.
* Invoice: added an error message when generating moves with no description when a description is required.
* Project: fixed an issue when creating a task in a project marked as "to invoice" where the task was not marked as "to invoice" by default.
* Manufacturing order: fixed filter on sale order.
* Bank order: fixed payment status update when we cancel a bank order and there are still pending payments on the invoice.
* Move: fixed an error that occured when selecting a partner with an empty company.
* Summary of gross values and depreciation accounting report: fixed wrong values for depreciation columns.
* Manufacturing order: when planning a manufacturing order, fixed the error message when the field production process is empty.
* Timesheet: when generating lines, get all lines from project instead of only getting lines from task.
* Accounting report DAS 2: fixed export not working if N4DS code is missing.
* Accounting report DAS 2: fixed balance.
* Bank order: fixed an issue where moves generated from a bank order were not accounted/set to daybook.
* Project task: when creating a new project task, the status will now be correctly initialized.
* Product: fixed an issue where activating the configuration "auto update sale price" did not update the sale price.
* Stock move: prevent cancellation of an invoiced stock move.
* Stock move: modifying a real quantity or creating an internal stock move from the mobile application will correctly indicate that the real quantity has been modified by an user.
* Bank order: fixed an issue where the process never ended when cancelling a bank order.
* Sale order: fixed popup error "Id to load is required for loading" when opening a new sale order line.
* Journal: fixed error message when the "type select" was not filled in the journal type.
* Account config: fixed UI and UX for payment session configuration.
* Invoice: fixed an error preventing from merging invoices.
* Expense: prevent deletion of ventilated expense.

## [6.4.14] (2023-05-11)

#### Fixed

* Invoice: fixed bank details being required for wrong payment modes.
* Invoice: fixed an issue blocking advance payment invoice creation when the lines were missing an account.
* Job application: fixed an error occuring when creating a job application without setting a manager.
* Bank reconciliation: added missing translation for "Bank reconciliation lines" in french.
* Product: fixed an issue preventing product copy when using sequence by product category.
* Bank reconciliation/Bank statement rule: added a control in auto accounting process to check if bank detail bank account and bank statement rule cash account are the same.
* Stock move: fixed an issue when creating tracking number from an unsaved stock move. If we do not save the stock move, tracking number are now correctly deleted.
* Sale order: fixed an issue where sale order templates were displayed from the 'Historical' menu entry.
* Accounting payment vat report: fixed wrong french translations.
* MRP: fixed an JNPE error when deleting a purchase order generated by a MRP.
* VAT amount received accounting report: fixed height limit and 40 page interval break limit.
* Invoice payment: fixed payment with different currencies.
* Accounting report das 2: fixed currency required in process.
* Payment Voucher: fixed excess on payment amount, generate an unreconciled move line with the difference.
* Sale, Stock, CRM, Supplychain printings: fixed an issue were pictures with specific filenames were not displayed in the printings.
* Accounting batch: added missing filter on year.
* Move line: fixed analytic account domain when no analytic rules are based on this account.
* Purchase order: stock location is not required anymore if there are no purchase order lines with stock managed product.
* Accounting situation: display VAT system select when the partner is internal.
* Invoice: fixed wrong alert message when invoiced quantity was superior to delivered or ordered qty.
* Project: Display "Ticket" instead of "Project Task" in Activities tab when the activity is from a ticket.
* Payment session: select/unselect buttons are now hidden when status is not in progress.
* Bank reconciliation: fixed initial and final balance when importing multiple statements.
* Inventory: fixed UI issue by preventing unit from being changed in inventory lines.
* Stock rules: now correctly apply stock rules when minimum quantity is zero.

## [6.4.13] (2023-04-27)

#### Fixed

* Move: removed save action when we change the move date.
* Purchase order: fixed fiscal position on a purchase order generated from a sale order.
* FEC import: fixed an error occuring when importing FEC using the format without taxes.
* Debt recovery: fixed a a regression on demo data, the demo data should now have existing email templates.
* Batch bill of exchange: raise an anomaly when invoices are ready to be processed but bank details is inactive.
* Invoice: added a verification for analytics account on validate and ventilate button.
* Stock move: fixed an error occurring when emptying the product in a line.
* Move: fixed an error happening when regenerating invoice terms while the move is not saved.
* Analytic Rules: added a company filter on analytic account verification.
* Payment session: optimization done to improve performance of invoice term search process.
* Payment session: improved form view by removing blank spaces by adding a smaller dashlet.
* Group Menu Assistant: fixed an issue where an empty file was generated.
* Move line/Fixed asset: corrected wrong journal on fixed asset generated from move line.

## [6.4.12] (2023-04-21)

#### Fixed

* Sale order: fixed an issue where opening or saving a sale order without lines was impossible due to an SQL error.

## [6.4.11] (2023-04-20)

#### Changes

* Payment session: highlight in orange invoice terms with a financial discount.

#### Fixed

* Sale order: sale orders with a 0 total amount are now correctly displayed as invoiced if they have an ventilated invoice.
* Account management: fixed an issue where global accounting cash account was not displayed.
* Partner: fixed script error when opening partner contact form (issue happening only when axelor-base was installed without axelor-account).
* Operation order calendar: display operation orders with all status except operations in draft, cancelled or merged manufacturing orders.
* Payment session: removed sidebar in form view.
* Move/Simplified Move: hide counterpart generate button when functional origin is not sale, expense, fixed asset or empty.
* Customer/Prospect reporting: fixed an error occuring if we only have axelor-base installed when opening the dashboard.
* Move: fixed analytic move lines copy when reversing a move.
* Partner: fixed an error preventing the display of partner sale history when a product was sold with a quantity equals to 0.
* Operation order: fix UI issues when the user was modifying date time fields used for the planification.
* Invoice: fix unbalanced move generation when we create an invoice with holdback.
* Payment session: keep linked invoice terms when invoice terms needs to be released from payment session, when refund or bank order process for example.
* Payment session: allow to update parameters and refresh invoice terms.
* Base batch: fixed an issue when clicking the button to run manually the "synchronize calendar" batch.
* Move/Invoice term: Skip invoice term with holdback computation, if the functional origin select of the move is not fixed asset, sale, or purchase.
* Payment session/bank order: fixed issue where payments on invoices remains in pending state when autoConfirmBankOrder on payment mode is true.
* Partner: fixed an error preventing the display of partner sale history when a product was sold with a quantity equals to 0.
* Move: fixed an issue where the form view is marked as dirty even when opening a form view in readonly.
* BPM: fixed view attribute issue for a sub-process.
* Stock move: fixed an issue when opening stock move line form from the invoicing wizard.
* Payment session: remove reload on invoice term dashlet for select/unselect buttons.
* Payment condition: added missing english demo data for payment condition line.
* Message: fixed an issue where emails automatically sent were not updated.
* Invoice: fixed filter on company bank details for factorized customer so we are able to select the bank details of the factor.
* Sale order: generating a purchase order from a sale order now correctly takes into account supplier catalog product code and name.
* Accounting report DAS2: fixed balance panel computation.
* Stock move: now prevent splitting action on stock move line that are associated with a invoice line.
* Invoice: to avoid inconsistencies, now only canceled invoices can be deleted.
* Accounting period: fixed an issue where the user was able to reopen a closed period on a closed year.
* Bank details: fixed script error when opening bank details form (issue happening only when axelor-base was installed without axelor-account).
* Payment session: fill partner bank details on move generation accounted by invoice terms.

## [6.4.10] (2023-04-06)

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

#### Fixed

* Tracking number: fix inconsistent french translation.
* Stock: fixed an issue in some processes where an error would create inconsistencies.
* Contract: fixed an issue in some processes where an error would create inconsistencies.
* Sale: fixed an issue in some processes where an error would create inconsistencies.
* Studio: fixed an issue in some processes where an error would create inconsistencies.
* App base config: added missing french translation for "Manage mail account by company".
* Sequence: fixed sequences with too long prefix in demo data.
* Accounting report DGI 2055: fixed issues on both tables.
* Stock move line: modifying the expected quantity does not modify a field used by the mobile API anymore.
* Move: fixed an issue so the form is not automatically saved when updating the origin date and the due date.
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
* Accounting report payment vat: fixed no lines in payment vat report sum by tax part and not lettered part.
* Timesheet: fixed an issue preventing to select a project in the lines generation wizard.
* Payment voucher: fixed status initialization on creation.
* Manufacturing order: in form view, fixed buttons appearing and disappearing during view load.
* Project: fixed errors occuring when business-project was not installed.
* City: fixed an error occurring when importing city with manual type.

## [6.4.9] (2023-03-23)

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
* Payment session: accounting method and move accounting date are now correctly readonly on a canceled payment session.
* Invoice: fixed PFP check when paying multiple supplier invoices.
* Accounting batch: reset cut off move status when on journal change.
* Payment session: fixed an issue where a payment session retrieved day book moves with "retrieve daybook moves in payment session" configuration deactivated.
* Payment session: fixed filter on payment session for invoice terms to retrieve invoice terms linked to refunds.
* Template: fix html widget for SMS templates.
* Template: fix "Emailing" french translation.
* Stock move: fixed an error occurring when opening a stock move line in a different tab.
* Stock move: fixed an issue where "to address" was not correctly filled on a generated reversion stock move.
* Stock move: supplier arrivals now correctly computes the WAP when the unit is different in stock move and stock location.
* Invoice: fixed an issue preventing from paying invoices and refunds.
* Doubtful customer batch: fix success count on batch completion.
* HR: fixed typo "Managment" => "Managment".
* MRP: generating proposals now correctly generates every purchase order lines.
* Partner: prevent isCustomer from being unticked automatically if there are existing customer records in database.
* Move line: fixed an issue where duplicated analytic lines were generated.
* Financial discount: fixed french help translation.
* Mail message: fixed an issue preventing follower selection after a recipient has already been selected.

## [6.4.8] (2023-03-09)

#### Changes

* Debt recovery method line: add demo data email messages for B2C and B2B reminder recovery methods.

#### Fixed

* Analytic account: fixed demo data so analytic account imported are link to the company.
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
* Accounting report 2054: Gross value amount of a fixed asset bought and disposed in the same year must appear in columns B and C.
* Demo data: update year and period date to have the next year present in demo data.
* Project task: fixed an issue where setting project task category would not update invoicing type.
* Mail message: use tracking subject instead of template subject when adding followers or posting comments.
* Accounting report: fixed error preventing analytic balance printing when the currency was not filled.
* Account config: fixed an issue where clicking "import chart button" was not possible until the field "Account code nbr. char" was filled.
* Business: fixed an issue in some processes where an error would create inconsistencies.
* Batch form: close the popup to show the invoice list when the user is clicking the "show invoice" button.
* Invoice: ventilating an invoice refund correctly now correctly creates an invoice payment.
* Logistical Form: filter stock moves on company on logistical forms.
* CRM: fixed an issue in some processes where an error would create inconsistencies.
* Stock rules: alert checkbox is no longer displayed when use case is set to 'MRP'.
* Fixed asset: warning message translated in FR when trying to realize a line with IFRS depreciation plan.
* Fixed asset: fix typos in french translation.
* Fixed asset: fixed an issue where 'Generate a fixed asset from this line' box disappeared after selecting a fixed asset category.
* Freight carrier mode: fix typo in french translation.
* Invoice: fixed an issue preventing to change the partner with existing invoice lines.
* Sale order: allow to select direct order stock locations from the company as the sale order stock location.
* Project: fixed the display of description in Kanban view.
* HR Batch: fixed error making the batch process crash when using batch with a scheduler.
* Configurator: in the help panel for writing groovy scripts, fix external link so it is opened on a new tab by default.
* Invoice: remove the possibility to cancel a ventilated invoice.

Cancelling a ventilated invoice is not possible anymore. 
Reversing a move linked to an invoice does not cancel this invoice anymore.
Remove the config allowing to cancel ventilated invoice.

* Invoice: does not block the advance payment invoice validation if an account is not selected on the invoice.
* Accounting report: it is no longer required to fill the year to generate DAS 2 reports.
* Invoice: printing a report will open the correct one even if another report have the same file name.

## [6.4.7] (2023-02-24)

#### Fixed

* Accounting report: fixed advanced search feature on grid view.
* Payment session: Fix compute financial discount when the accounting date is linked to the original document
* French translation: corrected several spelling mistakes.
* App base config: added missing translation for Nb of digits for tax rate.
* Stock move: fixed error on invoice creation when we try to invoice a stock move generated from a purchase order.
* Stock move: fixed an error blocking stock move planification when app supplychain is not initialized.
* Stock move: fixed error message when trying to create partial invoice with a quantity equals to 0.
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

## [6.4.6] (2023-02-14)

#### Fixed

* Partner: improve email panel

Fixed an issue where permissions were not applied for emails displayed in this panel, also improve display by using a card view.

* Studio: fixed crash on click on '+' icon of panel tab and add support of 'only if'.
* Invoice: fixed error when ventilating a new invoice when the configuration 'has invoice term' field on account was disabled.
* Move: correctly fill origin and origin date on move line when we duplicate a move.
* Move template: correctly set payment condition on move generation and set description of move template on all move lines where move template description is filled.
* Opportunity: lost reason is now correctly cleared when reopening a lost opportunity.
* Opportunity: fixed description on kanban view.
* Ticket type: fixed error preventing to show chart information on opening a ticket type form.
* Meta select: fixed wrong french translation for "Order".

## [6.4.5] (2023-02-03)

#### Fixed

* Payment: cancelling a payment from an Invoice or an Expense must go through the reverse of the move:

When registering a payment in an invoice or an expense, the move generated will often be in Daybook mode.
So the cancel button of the payment will indicate to the user that cancelling this Invoice/Expense payment must 
be done by reversing the corresponding move.
If the move generated from the payment is not accounted, it will be deleted, and the Invoice/Expense payment will be cancelled.

* Payment voucher: fixed amount remaining to pay when we duplicate a move.
* Accounting report: fixed partner grouping issue on preparatory process for fees declaration.
* Cheque rejection: fixed an issue where we were unable to select a payment voucher.
* User: fixed active user being able to change its user code.
* Sale order: fixed a bug where doing a partial invoicing in percentage could not work.
* Company bank details: fixed company bank details filter in invoice and purchase order form views.
* Accounting batch: fixed possible inconsistencies by blocking move generation if the status between the operation journal and the selected cut off move are not compatible.
* BPM: Fix view attrs and dmn expression builders

  - Add meta attrs support in view attrs
  - Display all models from all pools in view attrs
  - Fix blank screen when editing a column properties in DMN editor
  - Fix incomplete selection when using models for output in DMN editor

* Stock move: fixed rounding issues when computing the total to avoid a gap between order/invoice totals and related stock moves totals.
* Stock move: fixed report not showing manually changed address for customer delivery.
* Cost calculation: fixed the priority on the bill of materials chosen during the calculation process. This fixes an issue where a bill of materials from another company was used to compute the cost.
* Fiscal position: changed the behavior of fiscal position on purchase and sale order:

Now the partner is filtered depending on the currency and price list which are readonly if there is an order line.
The fiscal position is now editable even if there is an order line. Changing the partner updates the fiscal position and the taxes on lines.

* Accounting report: fixed an issue were move lines with no partner were not displayed in the report.
* Accounting report: corrected wrong values in 2054 accounting report.
* Payment session: fixed payment session cash move line generation by correcting the amount in move lines.
* Bank reconciliation: fixed unselect button disappearing when we customize the grid.
* Payment condition: add a warning message on payment condition modification if the payment condition is used on existing records.
* Product: updated condition for default value on product pulled off market date.
* Manufacturing order: fixed small UI issue in form view.
* Analytic move line query: fixed an issue were analytic axes not linked to a company were not displayed.
* Partner: fixed an issue where multiple error messages were displayed if the partner bank details was invalid.
* Account management: fixed demo data import.
* Sale order/Purchase order/Invoice line: fixed JNPE error occured when fetching the correct translation for code and description.
* Studio: inserting fields is now easier.
* Invoice term: fixed error on creation of new invoice term on a new invoice.
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

## [6.4.4] (2023-01-19)

#### Fixed

* Inventory: Reversed Gap computation on inventory lines. Previously, the gap value was negative when the quantity found in the inventory was greater than the expected quantity. It is now positive in this case and negative if we have less quantity than expected.
* Stock move: fixed a regression were it was impossible to select any product on internal move.
* Contracts: fixed an issue where, when filling a product in a contract line, "missing configuration" errors were not shown to the user.
* Manufacturing order: fixed a bug where outsourced manufacturing orders could not be planned because 'outsourcing receipt stock location' was missing in the stock config.
* TranslationService: fixed an issue happening when using a translatable field, when the translation key is equal to the value, the wrong translation was displayed.
* Company: add explicit error message if the current user active company is not set and needed.
* Accounting batch: fix annual closing move generation.
* Accounting batch: fixed "PersistenceException" error preventing the execution of the batch when the list of opening account was empty.
* Accounting batch: fixed an issue where Payment reminder report was not generated.
* Accounting batch: fixed status of moves generated by annual closure batch by accounting them during the process.
* Accounting batch Close/open annual accounts: added error gestion for persistence exception.
* Accounting batch: the batch controlling move consistency was only checking daybook moves, fixed this behavior so this batch is now also checking accounted moves.
* Accounting reports: UI improvement for the form view (hide "global" and "global by date" fields for General balance and Partner balance).
* Accounting report: restored both DAS2 reports according to the initial specifications.
* Accounting report: fixed column C of impairment value table of the DGI 2055 accounting report.
* Accounting report: fix accounting report 2055 wrong column A.
* Bank reconciliation: fixed a UI issue where clicking "Unselected" with a selected move line breaks the alignement of the blue buttons in the move line grid.
* Period: fixed an issue preventing to reopen a period that was closed permanently or temporarily.
* Quality measuring point: set a minimum of 1 for the coefficient.
* Fixed asset: fix typos in french translation that appeared in the popup when using "Tools" in grid view.
* Fixed asset: fix wrong disposal depreciation value when disposing just before the last depreciation line, with prorata temporis.
* Accounting: correct demo data to correctly manage reverse vat on intra-eu purchases.
* Bank order: fix payment mode selection and set accounting move trigger select required:

Payment mode with Immediate move generation trigger settings can now be chosen in a bank order.
Accounting trigger move select is now required and not automatically filled when creating a new bank order.

* Complementary products: fixed an issue where the quantity of complementary products were multiplied by the quantity of the main product (when using the configuration "manage multiple sale quantity").
* Move/Analytic distribution: fixed an issue were saving a modification on analytic distribution in move line was not working correctly.
* Move template: fixed issues on move generation from a template causing the generated move to not display some fields and to have duplicated analytic lines on move lines.
* Period: fixed an error preventing a period from being closed.
* Invoice: fixed an issue which removed the product name when changing the quantity in a line.
* Invoice: fixed a bug where the lines of a advance payment could be duplicated from a purchase order with reversed charge tax line.
* Invoice: when generating an interco invoice, the generated supplier invoice now takes the correct company bank details.
* Invoice: fixed error on ventilation when we have an payment condition with a line with hold back option.
* Address: fixed an UI issue where changing the zip code was emptying the city.
* EBICS: restore proxy support for EBICS communications.

## [6.4.3] (2023-01-05)

#### Fixed

* Accounting report: fixed presentation in excel output for AccountingReportType15.
* Fixed asset: fix error message when realizing a derogatory line with a category that lacks one of the derogatory depreciation accounts.
* Sale order, stock move, purchase order: fixed an issue where a NPE error was displayed when adding a new line in sale order, sale order template, stock move, purchase order.
* Accounting report: fixed move lines retrieval on DAS2 Proof, given das2 activity and service type.
* Move line query: fixed filter when partner field is not filled.
* Fixed asset category: Rename some accounts in accounting tab.
* Bank details: improve message when confirming a bank order and a receiver bank details is inactive.
* Invoice term: delete reason of refusal to pay when accepting PFP.
* Payment voucher: fix generated payment move given financial discount tax amount.
* Fixed asset: Fix wrong depreciation value computation in fixed asset line till the depreciation date for both linear and degressive depreciation plan.
* Substitute pfp validator: added a control on dates so start date cannot be after end date.
* Move line: take into consideration isTaxAuthorizedOnMoveLine from account for the auto completion.
* Move: fixed an issue where description was not filled during move origin change.
* Expense: fixed an issue where ground for refusal was hidden even if it was filled.
* Manufacturing: fix form views for subcontractor deliveries and arrivals.
* Move template line: Hide distribution template field if selected account does not have analytic distribution authorized.
* Analytic distribution: add filter for analytic axis, using the account config of the company.
* Invoice: add missing translations on tab titles.
* Followers: Fixed a residual bug where trying to fetch a recipient would result in a exception with somes conditions
* Followers: Fixed a residual bug where trying to fetch a recipient would result in a exception with somes conditions.
* Fixed asset: sale move generation is now correctly based on sale account.
* Move line: fixed an issue where base analytic distribution was readonly while the configuration allowed the modification.
* MRP: fixed an infinite loop issue

An infinite loop occurred when, for a product, the available stock was less than the minimum quantity of its stock rule
and this product was used in a purchase order.

* MRP: fix generation proposal menu for more clarity.

Generate proposal menu item now opens a wizard with 2 buttons:
One for generating all possible proposals, one for generating proposals of only selected lines.

* MRP: fixed NPE error that could happen during MRP computation if a operation order had a null planned start date.
* MRP: When generating purchase order from proposals, correctly fill desired delivery date.
* Fixed Asset: add filter by purchase journal in purchase account move field.
* Fixed asset category: fixed some french translation in accounting information panel.
* Move line/analytic: fixed an issue where analytic was not generated during move line generation.
* Invoice term: fixed the issue making the partner form blank.
* Accounting Report: add filter by company on AccountingReportType22.
* Invoice: reject company bank details when default bank details is not active.
* Fixed asset: fixed disposalValue when disposing by scrapping, hide disposalValue when asset is disposed by cession.
* Fixed asset: fixed issue with prorata temporis when computation date starts on the 31st of months.
* MoveLineQuery: Change design of isSelected field.
* Cost sheet: fixed wrong human cost calculation when computing a cost sheet for a in progress manufacturing order.
* Bank reconciliation: added default account type in search query.
* Analytic distribution: make analytic distribution required on invoice lines given the account configuration, check added on analytic move line to ensure a total of 100% by axis.
* Demo data: complete demo data for permissions, roles and menus.
* Move: currency related fields are now changed on change of company
* Inventory: when changing real quantity in a line, now takes the correct wap price into account when computing gap and real value.
* Contracts: correctly apply the configuration "Nb of digits for unit prices" to price in contract lines.
* Contracts: correctly apply the configuration "Nb of digits for quantity" to price in contract lines.
* Accounting report type: UI form view improvements.
* Move: fix creation of an move line when an partner has a default charge account.
* BPM: Add meta attrs support in view attrs and fix built in variable support.
* Manufacturing Orders: during manfacturing order generation, added a more explicit message if the bill of materials has 0 quantity.
* Accounting report: Fix filter by account on "vat statement received" report.
* Timesheet line: fixed task display in grid view.
* Aged balance report: corrected wrong currency amount displayed and corrected non due amount displayed in wrong column.
* Sale order separation: fixed computation of miscellaneous sale order fields.
* Accounting report: fixed an issue where 0.0 figures were wrongly displayed in title lines for PDF personnalized reports.
* Move/Move line: fixed deletion of analytic axis and remove save popup when nothing is edited on move line.
* Move template: add partner on the generated move when there is only one.
* Bank reconciliation: fixed display of move description in BankReconciliation reports.
* Accounting report: fixed different issues on ETAT PREPARATOIRE DGI 2054.
* Fixed asset: fixed issues with prorata-temporis on depreciation plan computation of imported fixed assets.
* Accounting report: fixed DAS2 proof declaration report to only display reconciled move lines and purchase moves on printing.

## [6.4.2] (2022-12-16)

#### Features

* Year: Demo data for civil, fiscal and payroll years are now based on the current year.

Add the possibility to fix a date element when using TODAY in demo data with equal sign.
Example: TODAY[-4y=1M=1d] will give us 2018-01-01 if we are in 2022.


#### Fixed

* Bill of materials: fix error when accessing general bill of materials menu.
* Invoice: fixed an error occurring when total A.T.I was equal to zero.
* Leave request: fix error message when sending leave request when company is missing.
* Accounting reports: add origin informations (ref and date) on general ledger and partner general ledger.
* Move line: clear fields related to partner when partner is emptied.
* Invoice: correctly hide refund list when there is no refund.
* Invoice: fixed an error occurring when creating a line without product.
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

[6.4.22]: https://github.com/axelor/axelor-open-suite/compare/v6.4.21...v6.4.22
[6.4.21]: https://github.com/axelor/axelor-open-suite/compare/v6.4.20...v6.4.21
[6.4.20]: https://github.com/axelor/axelor-open-suite/compare/v6.4.19...v6.4.20
[6.4.19]: https://github.com/axelor/axelor-open-suite/compare/v6.4.18...v6.4.19
[6.4.18]: https://github.com/axelor/axelor-open-suite/compare/v6.4.17...v6.4.18
[6.4.17]: https://github.com/axelor/axelor-open-suite/compare/v6.4.16...v6.4.17
[6.4.16]: https://github.com/axelor/axelor-open-suite/compare/v6.4.15...v6.4.16
[6.4.15]: https://github.com/axelor/axelor-open-suite/compare/v6.4.14...v6.4.15
[6.4.14]: https://github.com/axelor/axelor-open-suite/compare/v6.4.13...v6.4.14
[6.4.13]: https://github.com/axelor/axelor-open-suite/compare/v6.4.12...v6.4.13
[6.4.12]: https://github.com/axelor/axelor-open-suite/compare/v6.4.11...v6.4.12
[6.4.11]: https://github.com/axelor/axelor-open-suite/compare/v6.4.10...v6.4.11
[6.4.10]: https://github.com/axelor/axelor-open-suite/compare/v6.4.9...v6.4.10
[6.4.9]: https://github.com/axelor/axelor-open-suite/compare/v6.4.8...v6.4.9
[6.4.8]: https://github.com/axelor/axelor-open-suite/compare/v6.4.7...v6.4.8
[6.4.7]: https://github.com/axelor/axelor-open-suite/compare/v6.4.6...v6.4.7
[6.4.6]: https://github.com/axelor/axelor-open-suite/compare/v6.4.5...v6.4.6
[6.4.5]: https://github.com/axelor/axelor-open-suite/compare/v6.4.4...v6.4.5
[6.4.4]: https://github.com/axelor/axelor-open-suite/compare/v6.4.3...v6.4.4
[6.4.3]: https://github.com/axelor/axelor-open-suite/compare/v6.4.2...v6.4.3
[6.4.2]: https://github.com/axelor/axelor-open-suite/compare/v6.4.1...v6.4.2
[6.4.1]: https://github.com/axelor/axelor-open-suite/compare/v6.4.0...v6.4.1
[6.4.0]: https://github.com/axelor/axelor-open-suite/compare/v6.3.5...v6.4.0
