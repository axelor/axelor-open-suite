package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PrintingSettings;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.mapper.Mapper;
import com.axelor.studio.db.AppBase;
import com.axelor.studio.db.AppSale;
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

  @Inject
  public SaleOrderInitValueServiceImpl(
      AppBaseService appBaseService,
      CompanyRepository companyRepository,
      AppSaleService appSaleService,
      UserService userService,
      BankDetailsService bankDetailsService) {
    this.appBaseService = appBaseService;
    this.companyRepository = companyRepository;
    this.appSaleService = appSaleService;
    this.userService = userService;
    this.bankDetailsService = bankDetailsService;
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

  protected Map<String, Object> saleOrderDefaultValues(SaleOrder saleOrder) {
    Map<String, Object> saleOrderMap = new HashMap<>();
    AppSale appSale = appSaleService.getAppSale();
    Company company = getCompany();
    int salesPersonSelect = appSale.getSalespersonSelect();
    User user = AuthUtils.getUser();
    PrintingSettings printingSettings =
        Optional.ofNullable(user)
            .map(User::getActiveCompany)
            .map(Company::getPrintingSettings)
            .orElse(null);

    if (company != null) {
      saleOrder.setDuration(company.getSaleConfig().getDefaultValidityDuration());
      saleOrder.setCurrency(company.getCurrency());
    }

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
      saleOrderMap.put("duration", company.getSaleConfig().getDefaultValidityDuration());
      saleOrderMap.put("currency", company.getCurrency());
    }

    for (Map.Entry<String, Object> entry : saleOrderMap.entrySet()) {
      Mapper.of(SaleOrder.class).set(saleOrder, entry.getKey(), entry.getValue());
    }

    return saleOrderMap;
  }

  protected Map<String, Object> getInAti(SaleOrder saleOrder) {
    Map<String, Object> saleOrderMap = new HashMap<>();
    Company company = getCompany();
    if (company != null) {
      int saleOrderInAtiSelect = company.getSaleConfig().getSaleOrderInAtiSelect();
      boolean inAti = saleOrderInAtiSelect == 2 || saleOrderInAtiSelect == 4;
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

  protected Map<String, Object> getGroupProductsOnPrintings(SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    AppBase appBase = appBaseService.getAppBase();
    saleOrder.setGroupProductsOnPrintings(appBase.getIsRegroupProductsOnPrintings());
    saleOrderMap.put("companyBankDetails", saleOrder.getGroupProductsOnPrintings());
    return saleOrderMap;
  }

  protected User getUser(SaleOrder saleOrder, int salesPersonSelect) {
    User user = null;
    Partner clientPartner = saleOrder.getClientPartner();
    if (salesPersonSelect == 1) {
      user = AuthUtils.getUser();
    }
    if (salesPersonSelect == 2 && clientPartner != null) {
      user = clientPartner.getUser();
    }
    return user;
  }

  protected Company getCompany() {
    Company activeCompany = AuthUtils.getUser().getActiveCompany();
    if (activeCompany == null && companyRepository.all().count() == 1) {
      activeCompany = companyRepository.all().fetchOne();
    }
    return activeCompany;
  }
}
