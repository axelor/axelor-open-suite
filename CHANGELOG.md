## [8.4.2] (2025-07-18)

### Fixes
#### Base

* Price list line: fixed the display issue with the 'Amount' title.
* Added new default theme.
* App Base: fixed French translation for some configuration settings.
* Company: Added support for dark logo
* Siren API: fixed message when having wrong credentials.
* Theme: fixed modern theme menu color.

#### Account

* INVOICE : Wrong title displayed on PDF for a credit note issued on an advance payment.
* Partner/AccountingSituation : Correct anomaly causing account fields to be empty when generating automatically a new accounting situation on the partner (when adding a new company)
* AccountingBatch/BillOfExchange : Fixed the bill of exchange data type select.
* Invoice: fixed an issue where the payment date was not emptied when duplicate or generate from a paid invoice.
* Accounting report: fixed moveStatusSelect filter display issue after a status is being selected.

#### Bank Payment

* Invoice/BillOfExchange: fixed the fact the placement move was a Sale move and not a Payment move
* Move/MassEntry : fixed error when changing partner bank details on a move line mass entry.

#### Business Project

* Sale order: removed a duplicated extension in business project module.

#### Contract

* Contract/Invoice/Credit Note : fixed the issue where the invoiced amount of the contract was positive even for credit note.

#### Human Resource

* Expense: fixed an error when emptying employee

#### Production

* Cost Calculation : Wrong calculation when Calculation quantity from the BOM is > 1
* Prod Process: fixed NPE during creation of a phases
* Sales and Operations Planning: added missing title on 'Generate MPS forecasts' wizard
* Sale order: sublines are now correctly personalized.
* Sale order: changing the bill of material and production process in a sale order line now correctly updates the sale order line details.
* MRP: fixed an issue where bill of material line marked as not stock managed where taken into account for computation.
* Prod process: Update stock locations on company change
* Bill of material: fixed the error message when updating cost price and the price was 0.
* Sale order: fixed cost price computation of operation sale order line details with a human and machine work center
* Cost calculation: prevent product creation.

#### Purchase

* Call for tender: now sorted by desc creation date
* Call for tender: fixed translations and scaling for prices and units.

#### Sale

* Sale: fixed error message when currency conversion is not found.
* Sale order: fixed read only condition for add pack button when there are no sale order lines and corrected action.
* Sale order: fixed read only condition for add line from configurator button when there are no sale order lines.

#### Stock

* Stock: fixed stock location lines by product panel in stock details by product view.
* Incoterm: set incoterm required in sale order only for company client partner.
* STOCK/LOGISTICALFORM : Fix html column headers titles on line grid

#### Supply Chain

* Sale / Purchase / Stock: fixed some views where quantity and price decimal config wasn't being used.
* Sale order: timetable amount take into account taxes or not depending on the configuration.
* SaleOrderLine : fixed technical error at the line creation
* Sale order: fixed interco configuration when merging sale orders.


### Developer

#### Base

Migration script to add theme logo mode and logo fields-

```
  ALTER TABLE base_company ADD COLUMN dark_logo BIGINT;
  ALTER TABLE base_company ADD COLUMN light_logo BIGINT;

  ALTER TABLE meta_theme ADD COLUMN logo_mode VARCHAR(255);

  CREATE INDEX base_company_dark_logo_idx ON base_company USING btree (dark_logo);
  CREATE INDEX base_company_light_logo_idx ON base_company USING btree (light_logo);

  ALTER TABLE base_company ADD CONSTRAINT fk_5mjhnp9wy6tb96qgfjlfvfp5m 
    FOREIGN KEY (dark_logo) REFERENCES meta_file(id) NOT VALID;
  ALTER TABLE base_company VALIDATE CONSTRAINT fk_5mjhnp9wy6tb96qgfjlfvfp5m;

  ALTER TABLE base_company ADD CONSTRAINT fk_fxn0yfj1un0siwp3yqo3kymbo 
    FOREIGN KEY (light_logo) REFERENCES meta_file(id) NOT VALID;
  ALTER TABLE base_company VALIDATE CONSTRAINT fk_fxn0yfj1un0siwp3yqo3kymbo;

```

#### Business Project

Removed the onLoad extension from the SaleOrder form with id `business-project-sale-order-form` as it was a duplicate.

#### Production

UnitCostCalcLineService.createUnitCostCalcLine takes qtyRatio as argument

## [8.4.1] (2025-07-03)

### Fixes
#### Base

* Data Backup: fixed translations and added help for some fields.

#### Account

* MoveLine/TaxSet: fixed technical error generating movelines without taxes in some process
* AnalyticMoveLine/MoveLine : fixed the reset of analytic accounts on moveline when changing it on analytic move line
* MassEntryMove: fixed the error message list.
* Move: improve move validation time fixing global audit tracker
* MassEntryMove: fixed the counterpart process without saving the move.

#### Human Resource

* Issue on Windows when we try to build the AOS project.

#### Production

* Sale order: display quantity and total duration for operation sale order line details.

#### Purchase

* Purchase order line: fixed an issue where quantity was not reset to valid when managing multiple quantity.
* Call for tender: added an option to activate the feature.

#### Stock

* Inventory: fixed missing parameter for inventory birt template.


### Developer

#### Base

Migration script -

```
UPDATE meta_field
SET label = 'Relative dates',
description = 'Allows exporting dates by calculating the difference with the export date. During import, the data will be updated based on the import date and the previously saved offset.'
WHERE name = 'isRelativeDate' AND meta_model IN (SELECT id FROM meta_model WHERE name = 'DataBackup');

UPDATE meta_field
SET description = 'Batch size used when reading data. Allows you to optimize performance based on database volume.'
WHERE name = 'fetchLimit' AND meta_model IN (SELECT id FROM meta_model WHERE name = 'DataBackup');

UPDATE meta_field
SET description = 'Can be used in order to keep a fixed reference to update the current existing database. Not required for loading into another database.'
WHERE name = 'updateImportId' AND meta_model IN (SELECT id FROM meta_model WHERE name = 'DataBackup');
```

#### Account

Migration script -

```
ALTER TABLE account_move ALTER COLUMN mass_entry_errors TYPE text;
```

---

MassEntryMoveCreateService.createMoveFromMassEntryList now take a move instead of the move id

#### Purchase

The following script must be played if the database is not auto updated:
```
ALTER TABLE studio_app_purchase ADD COLUMN manage_call_for_tender boolean;
```

## [8.4.0] (2025-06-26)

### Features
#### Base

* Updated Axelor Open Platform to 7.4. You can find all information on this release [here](https://github.com/axelor/axelor-open-platform/blob/7.4/CHANGELOG.md).
* Updated Axelor Studio dependency to 3.5. You can find all information on this release [here](https://github.com/axelor/axelor-studio/blob/release/3.5/CHANGELOG.md).


#### Purchase

* Added a new call for tenders feature.

#### Sale

* Sale order: allowed for partial validation of quotation line. Quotation and orders are separated if the configuration is activated.
* Sale order: managed carrier prices by freight carrier mode.

#### Account

* Partner: added different UMRs for different bank details on the same partner.
* Analytic Move line: in the move line reverse values, added a percentage on each line to be able to create multiple lines per axis.
* Fixed Asset: implemented mass disposal process.
* Invoice: managed company tax number on the invoice.

#### Production

* Sale order: added automatic creation of production order from sale order.

#### Budget

* Budget: allowed to split the amount on multiple periods.

 
[8.4.2]: https://github.com/axelor/axelor-open-suite/compare/v8.4.1...v8.4.2
[8.4.1]: https://github.com/axelor/axelor-open-suite/compare/v8.4.0...v8.4.1
[8.4.0]: https://github.com/axelor/axelor-open-suite/compare/v8.3.9...v8.4.0
