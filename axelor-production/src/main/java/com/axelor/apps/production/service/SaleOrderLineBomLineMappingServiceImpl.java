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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineOnProductChangeService;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public class SaleOrderLineBomLineMappingServiceImpl implements SaleOrderLineBomLineMappingService {

  protected final SaleOrderLineBomService saleOrderLineBomService;
  protected final SaleOrderLineOnProductChangeService saleOrderLineOnProductChangeService;

  @Inject
  public SaleOrderLineBomLineMappingServiceImpl(
      SaleOrderLineBomService saleOrderLineBomService,
      SaleOrderLineOnProductChangeService saleOrderLineOnProductChangeService) {
    this.saleOrderLineBomService = saleOrderLineBomService;
    this.saleOrderLineOnProductChangeService = saleOrderLineOnProductChangeService;
  }

  @Override
  public SaleOrderLine mapToSaleOrderLine(
      BillOfMaterialLine billOfMaterialLine, SaleOrder saleOrder) throws AxelorException {
    Objects.requireNonNull(billOfMaterialLine);

    if (billOfMaterialLine.getProduct().getProductSubTypeSelect()
        == ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT) {
      SaleOrderLine saleOrderLine = new SaleOrderLine();
      saleOrderLine.setProduct(billOfMaterialLine.getProduct());
      saleOrderLine.setQty(billOfMaterialLine.getQty());
      saleOrderLine.setBillOfMaterialLine(billOfMaterialLine);
      saleOrderLine.setSequence(
          Optional.ofNullable(billOfMaterialLine.getPriority())
              .map(priority -> priority / 10)
              .orElse(0));
      // computing the line will generate sub lines.
      saleOrderLineOnProductChangeService.computeLineFromProduct(saleOrder, saleOrderLine);

      return saleOrderLine;
    }
    return null;
  }

  @Override
  public boolean isBomLineEqualsSol(
      BillOfMaterialLine billOfMaterialLine, SaleOrderLine saleOrderLine) {

    return billOfMaterialLine.getQty().equals(saleOrderLine.getQty())
        && billOfMaterialLine.getProduct().equals(saleOrderLine.getProduct())
        && billOfMaterialLine.getUnit().equals(saleOrderLine.getUnit())
        && billOfMaterialLine.getPriority().equals(saleOrderLine.getSequence() * 10)
        && Optional.ofNullable(saleOrderLine.getBillOfMaterial())
            .map(solBom -> solBom.equals(billOfMaterialLine.getBillOfMaterial()))
            .or(() -> Optional.of(billOfMaterialLine.getBillOfMaterial() == null))
            .orElse(false);
  }

  @Override
  public boolean isSyncWithBomLine(SaleOrderLine saleOrderLine) {
    Objects.requireNonNull(saleOrderLine);

    // No BOM => Not synchronize
    if (saleOrderLine.getBillOfMaterialLine() == null) {
      return false;
    }

    return this.isBomLineEqualsSol(saleOrderLine.getBillOfMaterialLine(), saleOrderLine);
  }
}
