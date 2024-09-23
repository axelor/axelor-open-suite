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
package com.axelor.apps.maintenance.service;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.service.BillOfMaterialComputeNameServiceImpl;
import com.axelor.common.StringUtils;

public class BillOfMaterialComputeNameServiceMaintenanceImpl
    extends BillOfMaterialComputeNameServiceImpl {

  @Override
  public String computeFullName(BillOfMaterial bom) {
    StringBuilder fullName = new StringBuilder();
    if (bom.getProduct() != null) {
      fullName.append(super.computeFullName(bom));
    } else {
      if (bom.getMachineType() != null && StringUtils.notEmpty(bom.getMachineType().getCode())) {
        fullName.append(bom.getMachineType().getCode());
      }
      if (StringUtils.notEmpty(bom.getName())) {
        if (StringUtils.notEmpty(fullName.toString())) {
          fullName.append(" | ");
        }
        fullName.append(bom.getName());
      }
      if ((bom.getVersionNumber() > 1)) {
        fullName.append(" - v");
        fullName.append(bom.getVersionNumber());
      }
    }
    return fullName.toString().trim();
  }
}
