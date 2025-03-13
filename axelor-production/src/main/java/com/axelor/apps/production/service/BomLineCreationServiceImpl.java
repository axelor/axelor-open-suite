/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;

public class BomLineCreationServiceImpl implements BomLineCreationService {

  protected final BillOfMaterialLineService billOfMaterialLineService;

  @Inject
  public BomLineCreationServiceImpl(BillOfMaterialLineService billOfMaterialLineService) {
    this.billOfMaterialLineService = billOfMaterialLineService;
  }

  @Override
  public BillOfMaterialLine createBomLineFromSol(SaleOrderLine subSaleOrderLine) {
    BillOfMaterialLine billOfMaterialLine =
        billOfMaterialLineService.createBillOfMaterialLine(
            subSaleOrderLine.getProduct(),
            subSaleOrderLine.getBillOfMaterial(),
            subSaleOrderLine.getQty(),
            subSaleOrderLine.getUnit(),
            Optional.ofNullable(subSaleOrderLine.getSequence())
                .map(seq -> seq * 10)
                .or(
                    () ->
                        Optional.ofNullable(subSaleOrderLine.getBillOfMaterialLine())
                            .map(BillOfMaterialLine::getPriority))
                .orElse(0),
            subSaleOrderLine.getProduct().getStockManaged(),
            Optional.ofNullable(subSaleOrderLine.getBillOfMaterialLine())
                .map(BillOfMaterialLine::getWasteRate)
                .orElse(BigDecimal.ZERO));

    int saleSupplySelect = subSaleOrderLine.getSaleSupplySelect();
    if (saleSupplySelect != SaleOrderLineRepository.SALE_SUPPLY_PRODUCE) {
      billOfMaterialLine.setBillOfMaterial(null);
    }
    return billOfMaterialLine;
  }

  @Override
  public BillOfMaterialLine createBomLineFromSolDetails(SaleOrderLineDetails saleOrderLineDetails) {
    return billOfMaterialLineService.createBillOfMaterialLine(
        saleOrderLineDetails.getProduct(),
        null,
        saleOrderLineDetails.getQty(),
        saleOrderLineDetails.getUnit(),
        Optional.ofNullable(saleOrderLineDetails.getBillOfMaterialLine())
            .map(BillOfMaterialLine::getPriority)
            .orElse(0),
        saleOrderLineDetails.getProduct().getStockManaged(),
        BigDecimal.ZERO);
  }
}
