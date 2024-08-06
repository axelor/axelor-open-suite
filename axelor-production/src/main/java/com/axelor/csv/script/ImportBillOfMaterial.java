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
package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.costsheet.CostSheetService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Map;

public class ImportBillOfMaterial {

  @Inject BillOfMaterialService billOfMaterialService;

  @Inject CostSheetService costSheetService;

  @Inject BillOfMaterialRepository bomRepo;

  @Transactional(rollbackOn = {Exception.class})
  public Object computeCostPrice(Object bean, Map values) throws AxelorException {
    if (bean == null) {
      return bean;
    }
    assert bean instanceof BillOfMaterial;
    BillOfMaterial bom = (BillOfMaterial) bean;
    bom = bomRepo.save(bom);
    if (bom.getDefineSubBillOfMaterial()) {
      costSheetService.computeCostPrice(bom, CostSheetService.ORIGIN_BILL_OF_MATERIAL, null);
      billOfMaterialService.updateProductCostPrice(bom);
    }
    return bom;
  }
}
