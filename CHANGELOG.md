# Changelog
## [Unreleased 5.4.0]
## Features
- Add global tracking log feature.
- Update to Gradle 5.6.4
- Update to Axelor Open Platform 5.3
- Bank details : Add new fields journal and bank account.
- Move template : Add boolean to validate automatically the wizard generated moves
- Move template : Add journal field to wizard
- Move template : Add new field description
- HR : Added a leave line configuration menu in leave management
- Move Template : Add totals for amount type
- Move template : Add details button to grid view to display fields
- Move template : Wizard dataInputList is now an editable grid
- Move template : Add change track on update
- Move template : Add demo data
- EMPLOYEE : set seniorityDate by hireDate
- Studio : Add CSRF protection for every request header.
- Add CSRF protection for timesheet line editor and project planning editor
- Studio : Add support of menu creation for custom model and BPM node.
- DURATION : add new field applicationType;
- MRP: add configuration to ignore end date on incoming mrp line type.
- BPM : Add overview of the result of the wkf.
- SUPPLIER PORTAL : Creation of new Supplier portal
- INVOICE : Added the possibility to add a watermark to the printings
- Reports: Manage locale, date format and timezone following company configuration.
- Product: add products per company to configure different prices depending on the company.
- Product: add product image in grid view.
- HRConfig : Import formula variables.
- PRODUCT: Add json field.
- MRP: hideIf on generateProposalBtn on mrpLine grid.
- Opportunity: Addition of hideIf attribute for 'take charge' button in opportunity grid.
- LEAD : hide Take charge button on grid view.
- TICKET : hide Take charge button on grid view.
- QUALITY TAG : add colorSelect field for tag color
- User : add forcePasswordChange field in user-form.
- Invoice : Specify button css.
- Studio: Selection create/update support for a field.
- Studio: Added a selection builder to update existing selection or to create a new one.

## Improvements
- USER : Default User language is based on application.locale from application.properties
- BASE : Cache memory performace improved by not stocking geographical entities anymore
- Accounting move line : When creating a new line the default debit or credit is set in order to balance the move
- Accounting Move Line : When debit/credit is filled the other field is set to 0 instead of being set to a readonly mode
- Invoice/Orders : The printing filename has been changed to show the id of the printed order/invoice
- Employee : renamed dateOfHire, timeOfHire, endDateContract, dateOfBirth fields to hireDate, hireTime, contractEndDate, birthDate
- Removed block permission from demo data.
- SaleOrder/Invoice/PurchaseOrder Line : Unit is now required
- TeamTask: add new field categorySet.
- Studio: Make app builder optional
- Birt report: Change display of date
- Invoice report : Change in font size in Invoice report.
- Report : Fix unit translation.
- Forecast Recap : default value set to today for from date field
- Product : Added possibility to add a color to the product variant value tag with the field displayColor
- Partner : Deleting partner will not remove linked employee.
- Journal : Improve balance calculation
- Invoice : Addition of new field to display deliveryAddress on form and in report.
- EBICS PARTNER : added tracking to the fields of the ebics partner object.
- Move : It is now possible to change date until the move is validated
- Employee : added a view to the employee's main contract company in the top right in view form
- EbicsUser : Enable searching on requestLog dashlet.

## Bug Fixes
- HR : A leave-line cannot be saved whitout employee or leave-reason.
- Lead: Fix city name and state name issue in report printing.
- EMPLOYEE : add translation of "Employee PhoneBook".
- Studio: Add and fixed attributes of model and fields for import and export app.
- EMPLOYEE : add button for Employee.rptdesign report on form.
- XML : Update all xml files XSD version to AOP 5.3.
- INVOICE : Change Invoicing menu translation.
- INVOICE : Resolve report binding issue.
- MRP LINE : Fix the mrp-line-form dirty view issue.
- Bank reconciliation : add management of case of several account management for account domain, journal domain, auto change of journal and cash account fields and now account and journal from bank details are prioritized.
- MOVE LINE : Fix taxLine required issue.
- AccountManagement :  Fix NPE when product not present in invoiceLine.
- Change eval: _parent to eval: __ parent __ in all actions and views.

