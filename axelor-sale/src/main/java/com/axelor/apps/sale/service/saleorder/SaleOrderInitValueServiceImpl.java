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
    SaleConfig saleConfig = saleConfigService.getSaleConfig(company);

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
      saleOrderMap.put("duration", saleConfig.getDefaultValidityDuration());
      saleOrderMap.put("currency", company.getCurrency());
    }

    return saleOrderMap;
  }

  protected Map<String, Object> getInAti(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    SaleConfig saleConfig = saleConfigService.getSaleConfig(saleOrder.getCompany());
    Company company = companyService.getDefaultCompany(null);
    if (company != null) {
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
