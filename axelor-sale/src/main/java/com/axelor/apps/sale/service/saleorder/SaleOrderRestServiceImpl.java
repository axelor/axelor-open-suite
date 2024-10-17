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
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.rest.dto.SaleOrderLinePostRequest;
import com.axelor.apps.sale.service.saleorderline.creation.SaleOrderLineGeneratorService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderRestServiceImpl implements SaleOrderRestService {
  protected SaleOrderRepository saleOrderRepository;
  protected SaleOrderLineGeneratorService saleOrderLineGeneratorService;

  @Inject
  public SaleOrderRestServiceImpl(
      SaleOrderRepository saleOrderRepository,
      SaleOrderLineGeneratorService saleOrderLineGeneratorService) {
    this.saleOrderRepository = saleOrderRepository;
    this.saleOrderLineGeneratorService = saleOrderLineGeneratorService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public SaleOrder fetchAndAddSaleOrderLines(
      List<SaleOrderLinePostRequest> saleOrderLinePostRequestList, SaleOrder saleOrder)
      throws AxelorException {
    if (CollectionUtils.isEmpty(saleOrderLinePostRequestList)) {
      return saleOrder;
    }
    for (SaleOrderLinePostRequest saleOrderLinePostRequest : saleOrderLinePostRequestList) {
      Product product = saleOrderLinePostRequest.fetchProduct();
      BigDecimal quantity = saleOrderLinePostRequest.getQuantity();
      saleOrderLineGeneratorService.createSaleOrderLine(saleOrder, product, quantity);
    }
    saleOrderRepository.save(saleOrder);
    return saleOrder;
  }
}
