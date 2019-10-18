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
- JSON-MODEL-FORM : add tracking on json fields
- STOCK RULE : New boolean alert when orderAlertSelect is not alert and stockRuleMessageTemplate added.

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

## Bug Fixes
- Fix injection error during test
- Studio : Fix access to json fields of base model in chart builder form.
- Exception on finalizing a sale order: could not extract ResultSet

