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

 
[8.4.1]: https://github.com/axelor/axelor-open-suite/compare/v8.4.0...v8.4.1
[8.4.0]: https://github.com/axelor/axelor-open-suite/compare/v8.3.9...v8.4.0
