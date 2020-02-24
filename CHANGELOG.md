# Changelog
## [Unreleased 5.3.0]
## Features
- Add Pack Feature in sale order.
- FLEET : Manage rental cars and minor fixes
- Studio: New features - Label with color,multiline string,grid column sequence,form width,spacer and order by properties. 
- JOURNAL : viewer to display the balance
- SALE ORDER LINE : Display availability status on sale order line grid view if sale order status is 'Confirmed'.
- Studio: sidebar option for panel
- Add DMS Import.
- Map : Filter out the data with empty address.
- Studio : Tab display for panel tab
- JSON-FIELD-FORM : add tracking in form fields
- Studio : group by application on json model grid view.
- Export studio app: email action - email template
- Export Studio app: export actions created with meta-action-from
- FORECAST RECAP LINE TYPE : create new object ForecastRecapLineType
- JSON-MODEL-FORM : add tracking on json fields
- STOCK RULE : New boolean alert when orderAlertSelect is not alert and stockRuleMessageTemplate added.
- Studio: MetaAction and MetaSelect menus with group by on app
- META-MODEL-FORM : add tracking on json fields
- Studio : Added validIf property for custom field.
- Invoice and SaleOrder : Invoice and SaleOrder report updated based on ticket #20927
- CITY : Import automatically from Geonames files
- MRP : Freeze proposals after manually modifying them.
- ExtraHoursLine : Add new field 'Type' referencing new domain ExtraHoursType.
- Added a global configuration to base app to define number of digits for quantity fields.
- Address: Addition of boolean 'isSharedAddress' in base config to check addresses are shared or not.
- BANK STATEMENT LINE : Change orderBy attribute on bank-statement-line-afb-120-grid.
- BANK DETAILS : add search button on bank-details-bank-order-company-grid.

## Improvements
- Remove Pack Feature from Product.
- Account : Remove DirectDebitManagement
- MENU BUILDER : Add selection support for icon and iconBackground
- Custom Model : Hide menu panel and allows to create menu from menubuilder only
- English language : Correction of errors in english words and change gender job word to genderless job word
- Action Builder: Added option to update or use json field from real model
- STUDIO : add 'attrs' for User.
- Studio: Added colSpan,title for the label and  visibleInGrid option for button.
- Studio: Added restriction for model and model field names, allowed only alphanumberic characters
- Studio: Disable 'Visible in grid' option for spacer
- STOCK MOVE LINE : display invoiced status at same place as avalable tag
- DMS Import : Improvement in code
- Company : Replace the M2M bankDetailsSet with O2M
- BANKDETAILS : Add tree and card view for bank details and balance viewer on company bank details
- BANK STATEMENT: update automatically balance and date of bank details concerned by the bank statement when imported.
- ACTIONBUILDER : Update filter on valueJson and metaJsonField fields
- MetaJsonField : show sequence and appBuilder field in json-field-grid
- ACTIONBUILDER : Allow to add a condition at start in generated action-script
- BANK ORDER : replace action record with action method for reject and correct button.
- SEQUENCE : change tracking
- BANK ORDER : Bank order workflow pass from darft to validated when automatic transmission is not activated in payment mode.
- INVOICE : add specific note of company bank details on invoice report.

## Bug Fixes
- Fix injection error during test
- Studio : Fix access to json fields of base model in chart builder form.
- Exception on finalizing a sale order: could not extract ResultSet
- Studio : Fixed display blank when you click on a field which is out of a panel.
- Studio : Fixed selection filter issue and sequence issue.
- StockMoveLine : Fixed empty popup issue while viewing stock move line record in form view
- STOCK MOVE LINE : $invoiced tag display twice.
- LEAVE TEMPLATE : changed field fromDate and toDate name to fromDateT and toDateT.
- MRP : Fix error while generating all proposals.
- UI : Addition of onClick attributes in buttons.
- ImportCityServiceImpl : Use of try-with-resource for ZipFile and FileWriter.
- Sales dashboard : chart not displayed
- PRODUCT : economicManufOrderQty displayed twice

