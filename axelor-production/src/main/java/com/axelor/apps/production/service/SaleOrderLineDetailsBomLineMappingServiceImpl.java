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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;
import java.util.Objects;

public class SaleOrderLineDetailsBomLineMappingServiceImpl
    implements SaleOrderLineDetailsBomLineMappingService {

  protected final SaleOrderLineDetailsService saleOrderLineDetailsService;

  @Inject
  public SaleOrderLineDetailsBomLineMappingServiceImpl(
      SaleOrderLineDetailsService saleOrderLineDetailsService) {
    this.saleOrderLineDetailsService = saleOrderLineDetailsService;
  }

  @Override
  public SaleOrderLineDetails mapToSaleOrderLineDetails(
      BillOfMaterialLine billOfMaterialLine, SaleOrder saleOrder) throws AxelorException {
    Objects.requireNonNull(billOfMaterialLine);

    if (billOfMaterialLine.getProduct().getProductSubTypeSelect()
        == ProductRepository.PRODUCT_SUB_TYPE_COMPONENT) {
      SaleOrderLineDetails saleOrderLineDetails = new SaleOrderLineDetails();
      saleOrderLineDetails.setTypeSelect(SaleOrderLineDetailsRepository.TYPE_COMPONENT);
      saleOrderLineDetails.setProduct(billOfMaterialLine.getProduct());
      saleOrderLineDetails.setQty(billOfMaterialLine.getQty());
      saleOrderLineDetails.setBillOfMaterialLine(billOfMaterialLine);
      saleOrderLineDetailsService.productOnChange(saleOrderLineDetails, saleOrder);

      return saleOrderLineDetails;
    }
    return null;
  }
}
