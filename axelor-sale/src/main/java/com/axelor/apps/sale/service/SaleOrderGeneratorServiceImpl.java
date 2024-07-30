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
import com.axelor.apps.base.db.*;
import com.axelor.apps.base.service.CompanyService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;

public class SaleOrderGeneratorServiceImpl implements SaleOrderGeneratorService {
  SaleOrderRepository saleOrderRepository;
  protected AppSaleService appSaleService;
  protected CompanyService companyService;

  @Inject
  public SaleOrderGeneratorServiceImpl(
      SaleOrderRepository saleOrderRepository,
      AppSaleService appSaleService,
      CompanyService companyService) {
    this.saleOrderRepository = saleOrderRepository;
    this.appSaleService = appSaleService;
    this.companyService = companyService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public SaleOrder createSaleOrder(Partner clientPartner) throws AxelorException {
    SaleOrder saleOrder = new SaleOrder();
    Company company = companyService.getDefaultCompany(null);
    saleOrder.setClientPartner(clientPartner);
    saleOrder.setCreationDate(LocalDate.now());
    saleOrder.setCompany(company);
    saleOrder.setCreationDate(appSaleService.getTodayDate(company));
    saleOrderRepository.save(saleOrder);
    return saleOrder;
  }
}
