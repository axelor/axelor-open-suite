# Changelog
## [Unreleased 6.0]
## Features

- Production : Remove stockLocation from Machine Type object.
- Production : Manage MPS (Master Production Schedule) process.
- MANUFACTURING : New object S&OP (PIC)
- Partner : Added a new partner type 'Subcontractor' and added field related to outsourcing in manufacturing
- PRODUCTION : Creation of a new object MPS Charge
- MACHINE : Tool management
- PRODUCTION : created buttons in product to create new bill of material and production process.
- PURCHASE ORDERLINE : Replaced the min sale price field by a field that indicate the maximum purchase price recommended

## Improvements
- Production : Change in reference of machineWorkCenter from workCenter to machine.
- MANUF ORDER : Print residual products on report and add panel of residual products
- MPS/MRP : title before sequence to change depending on typeSelect
- SALES DASHBOARD : Added multiple charts : turnover per customer, per company and per product per company

## Bug Fixes
- Production : Fix Java Heap Error.
- DEMO DATA : fix issue in BudgetService.
- SEQUENCE : wrong domain on all M2O sequence fields.

[Unreleased 6.0]: https://github.com/axelor/axelor-business-suite/compare/dev...wip
