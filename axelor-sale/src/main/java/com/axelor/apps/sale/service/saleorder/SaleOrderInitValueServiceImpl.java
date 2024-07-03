package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PrintingSettings;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.CompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleConfigRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.mapper.Mapper;
import com.axelor.studio.db.AppBase;
import com.axelor.studio.db.AppSale;
import com.axelor.studio.db.repo.AppSaleRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderInitValueServiceImpl implements SaleOrderInitValueService {

  protected AppBaseService appBaseService;
  protected CompanyRepository companyRepository;
  protected AppSaleService appSaleService;
  protected UserService userService;
  protected BankDetailsService bankDetailsService;
  protected SaleConfigService saleConfigService;
  protected CompanyService companyService;

  @Inject
  public SaleOrderInitValueServiceImpl(
      AppBaseService appBaseService,
      CompanyRepository companyRepository,
      AppSaleService appSaleService,
      UserService userService,
      BankDetailsService bankDetailsService,
      SaleConfigService saleConfigService,
      CompanyService companyService) {
    this.appBaseService = appBaseService;
    this.companyRepository = companyRepository;
    this.appSaleService = appSaleService;
    this.userService = userService;
    this.bankDetailsService = bankDetailsService;
    this.saleConfigService = saleConfigService;
    this.companyService = companyService;
  }

  @Override
  public Map<String, Object> getOnNewInitValues(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> initValues = new HashMap<>();
    initValues.putAll(saleOrderDefaultValues(saleOrder));
    initValues.putAll(getInAti(saleOrder));
    initValues.putAll(getBankDetails(saleOrder));
    initValues.putAll(getGroupProductsOnPrintings(saleOrder));
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
    AppSale appSale = appSaleService.getAppSale();
    Company company = companyService.getDefaultCompany(null);
    int salesPersonSelect = appSale.getSalespersonSelect();
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

    saleOrderMap.put("salespersonUser", getUser(saleOrder, salesPersonSelect));
    saleOrderMap.put("team", userService.getUserActiveTeam());
    saleOrderMap.put("printingSettings", printingSettings);
    if (user != null) {
      saleOrderMap.put("tradingName", user.getTradingName());
    }
    saleOrderMap.put("template", false);

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

  protected Map<String, Object> getBankDetails(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    saleOrder.setCompanyBankDetails(
        bankDetailsService.getDefaultCompanyBankDetails(
            saleOrder.getCompany(), null, saleOrder.getClientPartner(), null));
    saleOrderMap.put("companyBankDetails", saleOrder.getCompanyBankDetails());
    return saleOrderMap;
  }

  protected Map<String, Object> getGroupProductsOnPrintings(SaleOrder saleOrder) {
    Map<String, Object> saleOrderMap = new HashMap<>();
    AppBase appBase = appBaseService.getAppBase();
    saleOrder.setGroupProductsOnPrintings(appBase.getIsRegroupProductsOnPrintings());
    saleOrderMap.put("companyBankDetails", saleOrder.getGroupProductsOnPrintings());
    return saleOrderMap;
  }

  protected User getUser(SaleOrder saleOrder, int salesPersonSelect) {
    User user = null;
    Partner clientPartner = saleOrder.getClientPartner();
    if (salesPersonSelect == AppSaleRepository.APP_SALE_CURRENT_LOGIN_USER) {
      user = AuthUtils.getUser();
    }
    if (salesPersonSelect == AppSaleRepository.APP_SALE_USER_ASSIGNED_TO_CUSTOMER
        && clientPartner != null) {
      user = clientPartner.getUser();
    }
    return user;
  }
}
