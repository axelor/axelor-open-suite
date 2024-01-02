/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service.bomimport;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.BillOfMaterialImportLine;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;

public class BillOfMaterialImportLineServiceImpl implements BillOfMaterialImportLineService {

  @Override
  public Integer computeBoMLevel(BillOfMaterialImportLine billOfMaterialImportLine)
      throws AxelorException {
    if (billOfMaterialImportLine == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          ProductionExceptionMessage.BOM_IMPORT_PARENTS_NOT_DONE_PROPERLY);
    }
    if (billOfMaterialImportLine.getParent() == null) {
      billOfMaterialImportLine.setBomLevel(0);
      return 0;
    } else if (billOfMaterialImportLine.getParent().getBomLevel() != null) {
      billOfMaterialImportLine.setBomLevel(billOfMaterialImportLine.getParent().getBomLevel() + 1);
      return billOfMaterialImportLine.getBomLevel();
    }
    billOfMaterialImportLine.setBomLevel(computeBoMLevel(billOfMaterialImportLine.getParent()) + 1);
    return billOfMaterialImportLine.getBomLevel();
  }
}
