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
package com.axelor.apps.sale.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.CompanyService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.rest.dto.SaleOrderLinePostRequest;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderDomainService;
import com.axelor.apps.sale.service.saleorder.SaleOrderInitValueService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineGeneratorService;
import com.axelor.apps.sale.service.saleorder.SaleOrderOnChangeService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderRestServiceImpl implements SaleOrderRestService {
  protected SaleOrderRepository saleOrderRepository;
  protected AppSaleService appSaleService;
  protected CompanyService companyService;
  protected SaleOrderInitValueService saleOrderInitValueService;
  protected SaleOrderOnChangeService saleOrderOnChangeService;
  protected SaleOrderDomainService saleOrderDomainService;
  protected PartnerRepository partnerRepository;
  protected SaleOrderLineGeneratorService saleOrderLineCreateService;

  @Inject
  public SaleOrderRestServiceImpl(
      SaleOrderRepository saleOrderRepository,
      AppSaleService appSaleService,
      CompanyService companyService,
      SaleOrderInitValueService saleOrderInitValueService,
      SaleOrderOnChangeService saleOrderOnChangeService,
      SaleOrderDomainService saleOrderDomainService,
      PartnerRepository partnerRepository,
      SaleOrderLineGeneratorService saleOrderLineCreateService) {
    this.saleOrderRepository = saleOrderRepository;
    this.appSaleService = appSaleService;
    this.companyService = companyService;
    this.saleOrderInitValueService = saleOrderInitValueService;
    this.saleOrderOnChangeService = saleOrderOnChangeService;
    this.saleOrderDomainService = saleOrderDomainService;
    this.partnerRepository = partnerRepository;
    this.saleOrderLineCreateService = saleOrderLineCreateService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public SaleOrder fetchAndAddSaleOrderLines(
      List<SaleOrderLinePostRequest> saleOrderLinePostRequestList, SaleOrder saleOrder)
      throws AxelorException {
    if (CollectionUtils.isEmpty(saleOrderLinePostRequestList)) {
      return saleOrder;
    }
    List<SaleOrderLine> saleOrderLineList =
        createSaleOrderLine(saleOrderLinePostRequestList, saleOrder);
    if (CollectionUtils.isNotEmpty(saleOrderLineList)) {
      for (SaleOrderLine saleOrderLine : saleOrderLineList)
        saleOrder.addSaleOrderLineListItem(saleOrderLine);
    }
    saleOrderRepository.save(saleOrder);
    return saleOrder;
  }

  protected List<SaleOrderLine> createSaleOrderLine(
      List<SaleOrderLinePostRequest> saleOrderLinePostRequestList, SaleOrder saleOrder)
      throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = new ArrayList<>();
    for (SaleOrderLinePostRequest saleOrderLinePostRequest : saleOrderLinePostRequestList) {
      Product product = saleOrderLinePostRequest.fetchProduct();
      BigDecimal quantity = saleOrderLinePostRequest.getQuantity();
      SaleOrderLine saleOrderLine =
          saleOrderLineCreateService.createSaleOrderLine(saleOrder, product, quantity);
      saleOrderLineList.add(saleOrderLine);
    }
    return saleOrderLineList;
  }
}
