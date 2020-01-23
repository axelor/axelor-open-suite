/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.db.repo;

import com.axelor.apps.production.db.BillOfMaterial;
import java.math.BigDecimal;

public class BillOfMaterialManagementRepository extends BillOfMaterialRepository {

  @Override
  public BillOfMaterial save(BillOfMaterial billOfMaterial) {

    if (billOfMaterial.getVersionNumber() != null && billOfMaterial.getVersionNumber() > 1) {
      billOfMaterial.setFullName(
          billOfMaterial.getName() + " - v" + billOfMaterial.getVersionNumber());
    } else {
      billOfMaterial.setFullName(billOfMaterial.getName());
    }

    return super.save(billOfMaterial);
  }

  @Override
  public BillOfMaterial copy(BillOfMaterial entity, boolean deep) {

    BillOfMaterial copy = super.copy(entity, deep);

    copy.setStatusSelect(STATUS_DRAFT);
    copy.setVersionNumber(1);
    copy.setOriginalBillOfMaterial(null);
    copy.setCostPrice(BigDecimal.ZERO);
    copy.clearCostSheetList();

    return copy;
  }
}
