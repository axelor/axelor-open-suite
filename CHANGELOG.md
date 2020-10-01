## [6.0.0] (2020-10-05)

#### Features

* PRINT TEMPLATE: create test button to check print template line expression.
* HR: add employment contract sub type.
* PRODUCTION: created buttons in product to create new bill of material and production process.
* Axelor DocuSign: add new module axelor-docusign.
* Axelor Project DMS: add new module axelor-project-dms.
* PRINT TEMPLATE: Rework print template feature.

Add new configurations for print template: print format, sequence, columns
    number, conditions, signature

* TEMPLATE: update template engine: the user can now choose between StringTemplate or groovy.
* MANUFACTURING: Sales & Operation Planning (PIC).
* MACHINE: Implement tool management on machines.
* MAIL MESSAGE: use template object for email generated from a notification message.
* Partner: Add a new partner type 'Subcontractor' and add field related to outsourcing in manufacturing.
* PRINT TEMPLATE: Add XML export and import.
* Production: Manage MPS (Master Production Schedule) process.
* PRINT TEMPLATE: Use Itext instead of birt to generate templates.
* PRODUCTION: Add Master production scheduling charge.
* New changelog management.

#### Changes

* PRINT TEMPLATE: Add header and footer settings in print template.
* Print Template: use locale based on selected language in Template.
* PRINT TEMPLATE LINE: add new field 'ignore the line'.
* Production: machine work center is now a machine instead of a work center.
* MPS/MRP: title before sequence to change depending on the type.
* Use relative path instead of absolute path in configuration file path fields.
* Production: Remove stock location in machine type.
* Project DMS: Move 'Project DMS' menu inside projects main menu.
* MANUF ORDER: Print residual products on report and add panel of residual products.
* PURCHASE ORDER LINE: Replace the min sale price field by a field that indicates the maximum purchase price recommended.
* USER: the admin can now force the user to change password on the next connection.
* Invoice: Add tracking for most fields.
* ANALYTIC MOVE LINE : hide fields when open from a invoiceLine.
* ANALYTIC MOVE LINE : hide fields when open from a saleOrder or a purchaseOrder

#### Fixed

* Production configuration: fix stock location filter in workshop sequence config line and make the grid editable.
* Quality Alert: Show title of fields description, corrective actions and preventive actions.
* Email message template: remove from address in template.

Setting a custom `from` address per email template is now disabled, as the from
address should depend only on the SMTP account. The `from` should now always
be set in SMTP account configuration.

* LeaveReason: rename `leaveReason` field into `name`.
* JobPosition: Remove character limit on Profile Wanted field.

[6.0.0]: https://github.com/axelor/axelor-open-suite/compare/v5.4.1...v6.0.0