/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.db.repo;

import com.axelor.apps.production.db.BillOfMaterial;
import java.math.BigDecimal;
import java.util.Set;

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
    copy.clearBillOfMaterialSet();
    Set<BillOfMaterial> billOfMaterials = entity.getBillOfMaterialSet();

    if (billOfMaterials != null && !billOfMaterials.isEmpty()) {
      billOfMaterials.forEach(bom -> copy.addBillOfMaterialSetItem(copy(bom, deep)));
    }

    return copy;
  }
}
