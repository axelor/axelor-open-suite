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
package com.axelor.csv.script;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.costsheet.CostSheetService;
import com.axelor.exception.AxelorException;
import java.util.Map;
import javax.inject.Inject;
import javax.transaction.Transactional;

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
