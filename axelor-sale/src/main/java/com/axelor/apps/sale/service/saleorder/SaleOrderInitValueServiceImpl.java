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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PrintingSettings;
import com.axelor.apps.base.service.CompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleConfigRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderProductPrintingService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.mapper.Mapper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderInitValueServiceImpl implements SaleOrderInitValueService {

  protected AppBaseService appBaseService;
  protected UserService userService;
  protected SaleOrderBankDetailsService saleOrderBankDetailsService;
  protected SaleConfigService saleConfigService;
  protected CompanyService companyService;
  protected SaleOrderUserService saleOrderUserService;
  protected SaleOrderProductPrintingService saleOrderProductPrintingService;

  @Inject
  public SaleOrderInitValueServiceImpl(
      AppBaseService appBaseService,
      UserService userService,
      SaleOrderBankDetailsService saleOrderBankDetailsService,
      SaleConfigService saleConfigService,
      CompanyService companyService,
      SaleOrderUserService saleOrderUserService,
      SaleOrderProductPrintingService saleOrderProductPrintingService) {
    this.appBaseService = appBaseService;
    this.userService = userService;
    this.saleOrderBankDetailsService = saleOrderBankDetailsService;
    this.saleConfigService = saleConfigService;
    this.companyService = companyService;
    this.saleOrderUserService = saleOrderUserService;
    this.saleOrderProductPrintingService = saleOrderProductPrintingService;
  }

  @Override
  public Map<String, Object> getOnNewInitValues(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> initValues = new HashMap<>();
    initValues.putAll(saleOrderDefaultValues(saleOrder));
    initValues.putAll(getInAti(saleOrder));
    initValues.putAll(saleOrderBankDetailsService.getBankDetails(saleOrder));
    initValues.putAll(saleOrderProductPrintingService.getGroupProductsOnPrintings(saleOrder));
    return initValues;
  }

  @Override
  public Map<String, Object> setIsTemplate(SaleOrder saleOrder, boolean isTemplate) {
    Map<String, Object> initValues = new HashMap<>();
    saleOrder.setTemplate(isTemplate);
    initValues.put("template", saleOrder.getTemplate());
    return initValues;
  }

  protected Map<String, Object> saleOrderDefaultValues(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = saleOrderDefaultValuesMap(saleOrder);
    for (Map.Entry<String, Object> entry : saleOrderMap.entrySet()) {
      Mapper.of(SaleOrder.class).set(saleOrder, entry.getKey(), entry.getValue());
    }

    return saleOrderMap;
  }

  protected Map<String, Object> saleOrderDefaultValuesMap(SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    Company company = companyService.getDefaultCompany(null);
    User user = AuthUtils.getUser();
    PrintingSettings printingSettings =
        Optional.ofNullable(user)
            .map(User::getActiveCompany)
            .map(Company::getPrintingSettings)
            .orElse(null);

    saleOrderMap.put("exTaxTotal", BigDecimal.ZERO);
    saleOrderMap.put("taxTotal", BigDecimal.ZERO);
    saleOrderMap.put("inTaxTotal", BigDecimal.ZERO);
    saleOrderMap.put("advanceTotal", BigDecimal.ZERO);

    saleOrderMap.put("creationDate", appBaseService.getTodayDate(saleOrder.getCompany()));
    saleOrderMap.put("statusSelect", SaleOrderRepository.STATUS_DRAFT_QUOTATION);
    saleOrderMap.put("company", company);

    saleOrderMap.put("salespersonUser", saleOrderUserService.getUser(saleOrder));
    saleOrderMap.put("team", userService.getUserActiveTeam());
    saleOrderMap.put("printingSettings", printingSettings);
    if (user != null) {
      saleOrderMap.put("tradingName", user.getTradingName());
    }
    saleOrderMap.put("template", saleOrder.getTemplate());

    if (company != null) {
      SaleConfig saleConfig = saleConfigService.getSaleConfig(company);
      saleOrderMap.put("duration", saleConfig.getDefaultValidityDuration());
      saleOrderMap.put("currency", company.getCurrency());
    }

    return saleOrderMap;
  }

  protected Map<String, Object> getInAti(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    Company company = companyService.getDefaultCompany(null);
    if (company != null) {
      SaleConfig saleConfig = saleConfigService.getSaleConfig(company);
      int saleOrderInAtiSelect = saleConfig.getSaleOrderInAtiSelect();
      boolean inAti =
          saleOrderInAtiSelect == SaleConfigRepository.SALE_ATI_ALWAYS
              || saleOrderInAtiSelect == SaleConfigRepository.SALE_ATI_DEFAULT;
      saleOrder.setInAti(inAti);
    }
    saleOrderMap.put("inAti", saleOrder.getInAti());
    return saleOrderMap;
  }
}
