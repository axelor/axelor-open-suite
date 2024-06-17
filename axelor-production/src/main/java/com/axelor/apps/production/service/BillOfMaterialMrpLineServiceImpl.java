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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.service.MrpLineTool;
import java.util.Optional;

public class BillOfMaterialMrpLineServiceImpl implements BillOfMaterialMrpLineService {

  @Override
  public Optional<BillOfMaterial> getEligibleBillOfMaterialOfProductInMrpLine(
      MrpLine mrpLine, Product product) {

    if (mrpLine == null) {
      return Optional.empty();
    }

    BillOfMaterial billOfMaterial = mrpLine.getBillOfMaterial();
    if (billOfMaterial != null) {
      return Optional.of(billOfMaterial);
    }

    if (product == null) {
      return Optional.empty();
    }

    Optional<SaleOrderLine> saleOrderLineOpt =
        MrpLineTool.getOriginSaleOrderLineInMrpLineOrigin(mrpLine);
    Optional<BillOfMaterial> billOfMaterialOpt =
        saleOrderLineOpt.map(SaleOrderLine::getBillOfMaterial);

    if (billOfMaterialOpt.isPresent()
        && product.equals(billOfMaterialOpt.map(BillOfMaterial::getProduct).orElse(null))) {
      return billOfMaterialOpt;
    }
    return Optional.empty();
  }
}
