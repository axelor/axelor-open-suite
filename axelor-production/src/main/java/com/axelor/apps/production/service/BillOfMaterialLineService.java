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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import java.math.BigDecimal;

public interface BillOfMaterialLineService {

  BillOfMaterialLine createBillOfMaterialLine(
      Product product,
      BillOfMaterial billOfMaterial,
      BigDecimal qty,
      Unit unit,
      Integer priority,
      boolean hasNoManageStock);

  BillOfMaterialLine createFromRawMaterial(
      long productId, int priority, BillOfMaterial billOfMaterial) throws AxelorException;

  BillOfMaterialLine createFromBillOfMaterial(BillOfMaterial billOfMaterial);

  void fillBom(BillOfMaterialLine billOfMaterialLine, Company company) throws AxelorException;

  void fillHasNoManageStock(BillOfMaterialLine billOfMaterialLine);

  void fillUnit(BillOfMaterialLine billOfMaterialLine);
}
