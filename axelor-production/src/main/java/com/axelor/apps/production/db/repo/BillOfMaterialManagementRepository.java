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
package com.axelor.apps.production.db.repo;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class BillOfMaterialManagementRepository extends BillOfMaterialRepository {

  protected BillOfMaterialLineRepository billOfMaterialLineRepository;

  @Inject
  public BillOfMaterialManagementRepository(
      BillOfMaterialLineRepository billOfMaterialLineRepository) {

    this.billOfMaterialLineRepository = billOfMaterialLineRepository;
  }

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
    copy.clearBillOfMaterialLineList();

    List<BillOfMaterialLine> billOfMaterialLineList = entity.getBillOfMaterialLineList();
    if (billOfMaterialLineList != null && !billOfMaterialLineList.isEmpty()) {
      billOfMaterialLineList.forEach(
          boml ->
              copy.addBillOfMaterialLineListItem(billOfMaterialLineRepository.copy(boml, deep)));
    }

    return copy;
  }
}
