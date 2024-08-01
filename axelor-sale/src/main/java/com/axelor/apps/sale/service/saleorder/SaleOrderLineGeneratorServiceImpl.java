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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineInitValueService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineProductService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Map;

public class SaleOrderLineGeneratorServiceImpl implements SaleOrderLineGeneratorService {
  protected SaleOrderLineInitValueService saleOrderLineInitValueService;
  protected SaleOrderLineProductService saleOrderLineProductService;
  protected SaleOrderLineRepository saleOrderLineRepository;
  protected SaleOrderRepository saleOrderRepository;
  protected  SaleOrderLineComputeService saleOrderLineComputeService;
  protected SaleOrderComputeService saleOrderComputeService;
  @Inject
  public SaleOrderLineGeneratorServiceImpl(
      SaleOrderLineInitValueService saleOrderLineInitValueService,
      SaleOrderLineProductService saleOrderLineProductService,
      SaleOrderRepository saleOrderRepository,
      SaleOrderLineRepository saleOrderLineRepository, SaleOrderLineComputeService saleOrderLineComputeService,
 SaleOrderComputeService saleOrderComputeService) {
    this.saleOrderLineInitValueService = saleOrderLineInitValueService;
    this.saleOrderLineProductService = saleOrderLineProductService;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.saleOrderRepository = saleOrderRepository;
    this.saleOrderLineComputeService =saleOrderLineComputeService;
    this.saleOrderComputeService =saleOrderComputeService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public SaleOrderLine createSaleOrderLine(SaleOrder saleOrder, Product product)
      throws AxelorException {
    SaleOrderLine saleOrderLine = new SaleOrderLine();
    saleOrderLineInitValueService.onNewInitValues(saleOrder, saleOrderLine);
    saleOrderLine.setProduct(product);
    saleOrderLineProductService.computeProductInformation(saleOrderLine, saleOrder);
    saleOrderLineComputeService.computeValues(saleOrder, saleOrderLine);
    saleOrderLineRepository.save(saleOrderLine);
    saleOrder.addSaleOrderLineListItem(saleOrderLine);
     saleOrderComputeService.computeSaleOrder(saleOrder);
    saleOrderRepository.save(saleOrder);
    return saleOrderLine;
  }

  private void setSaleOrdervalues(
      Map<String, Object> saleOrderLineMap, SaleOrderLine saleOrderLine) {
    saleOrderLine.setPrice((BigDecimal) saleOrderLineMap.get("price"));
    saleOrderLine.setExTaxTotal((BigDecimal) saleOrderLineMap.get("exTotal"));
  }
}
