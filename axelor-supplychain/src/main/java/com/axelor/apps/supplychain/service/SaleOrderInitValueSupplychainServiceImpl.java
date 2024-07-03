package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.TaxNumber;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.CompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.SaleOrderInitValueServiceImpl;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.studio.db.AppSupplychain;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SaleOrderInitValueSupplychainServiceImpl extends SaleOrderInitValueServiceImpl {

  protected SaleOrderSupplychainService saleOrderSupplychainService;
  protected AppSupplychainService appSupplychainService;
  protected SaleOrderShipmentService saleOrderShipmentService;

  @Inject
  public SaleOrderInitValueSupplychainServiceImpl(
      AppBaseService appBaseService,
      CompanyRepository companyRepository,
      AppSaleService appSaleService,
      UserService userService,
      BankDetailsService bankDetailsService,
      SaleConfigService saleConfigService,
      CompanyService companyService,
      SaleOrderSupplychainService saleOrderSupplychainService,
      AppSupplychainService appSupplychainService,
      SaleOrderShipmentService saleOrderShipmentService) {
    super(
        appBaseService,
        companyRepository,
        appSaleService,
        userService,
        bankDetailsService,
        saleConfigService,
        companyService);
    this.saleOrderSupplychainService = saleOrderSupplychainService;
    this.appSupplychainService = appSupplychainService;
    this.saleOrderShipmentService = saleOrderShipmentService;
  }

  @Override
  public Map<String, Object> getOnNewInitValues(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> initValues = super.getOnNewInitValues(saleOrder);
    initValues.putAll(getPaymentMode(saleOrder));
    initValues.putAll(getBankDetails(saleOrder));
    initValues.putAll(getStockLocation(saleOrder));
    initValues.putAll(getInterco(saleOrder));
    initValues.putAll(getShipmentCostLine(saleOrder));
    initValues.putAll(getTaxNumber(saleOrder));
    return initValues;
  }

  @Override
  protected Map<String, Object> saleOrderDefaultValuesMap(SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderMap = super.saleOrderDefaultValuesMap(saleOrder);

    TradingName tradingName = saleOrder.getTradingName();
    saleOrderMap.put("amountInvoiced", BigDecimal.ZERO);
    if (tradingName != null) {
      saleOrderMap.put("stockLocation", tradingName.getShippingDefaultStockLocation());
    }

    return saleOrderMap;
  }

  @Override
  protected Map<String, Object> getBankDetails(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    saleOrder.setCompanyBankDetails(
        bankDetailsService.getDefaultCompanyBankDetails(
            saleOrder.getCompany(),
            saleOrder.getPaymentMode(),
            saleOrder.getClientPartner(),
            null));
    saleOrderMap.put("companyBankDetails", saleOrder.getCompanyBankDetails());
    return saleOrderMap;
  }

  protected Map<String, Object> getPaymentMode(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    Partner clientPartner = saleOrder.getClientPartner();
    if (clientPartner != null) {
      PaymentMode paymentMode = clientPartner.getInPaymentMode();
      if (paymentMode != null) {
        saleOrder.setPaymentMode(paymentMode);
      } else {
        paymentMode =
            Optional.ofNullable(AuthUtils.getUser())
                .map(User::getActiveCompany)
                .map(Company::getAccountConfig)
                .map(AccountConfig::getInPaymentMode)
                .orElse(null);
        saleOrder.setPaymentMode(paymentMode);
      }
    }
    saleOrderMap.put("paymentMode", saleOrder.getPaymentMode());
    return saleOrderMap;
  }

  protected Map<String, Object> getStockLocation(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    StockLocation shippingDefaultStockLocation =
        Optional.ofNullable(saleOrder.getTradingName())
            .map(TradingName::getShippingDefaultStockLocation)
            .orElse(null);
    if (shippingDefaultStockLocation == null || saleOrder.getStockLocation() == null) {
      return saleOrderMap;
    }
    Partner clientPartner = saleOrder.getClientPartner();
    Company company = saleOrder.getCompany();
    saleOrder.setStockLocation(
        saleOrderSupplychainService.getStockLocation(clientPartner, company));
    saleOrderMap.put("stockLocation", saleOrder.getStockLocation());
    return saleOrderMap;
  }

  protected Map<String, Object> getInterco(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    AppSupplychain appSupplychain = appSupplychainService.getAppSupplychain();
    boolean isIntercoFromSale = appSupplychain.getIntercoFromSale();
    boolean createdByInterco = saleOrder.getCreatedByInterco();
    Partner clientPartner = saleOrder.getClientPartner();
    Company company =
        companyRepository
            .all()
            .filter("self.partner = :clientPartner")
            .bind("clientPartner", clientPartner)
            .fetchOne();
    boolean isInterco =
        isIntercoFromSale && !createdByInterco && clientPartner != null && company != null;
    saleOrder.setInterco(isInterco);
    saleOrderMap.put("interco", saleOrder.getInterco());
    return saleOrderMap;
  }

  protected Map<String, Object> getShipmentCostLine(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    saleOrderShipmentService.createShipmentCostLine(saleOrder);
    saleOrderMap.put("saleOrderLineList", saleOrder.getSaleOrderLineList());
    return saleOrderMap;
  }

  protected Map<String, Object> getTaxNumber(SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderMap = new HashMap<>();
    Company company = saleOrder.getCompany();
    List<TaxNumber> taxNumberList = company.getTaxNumberList();
    if (taxNumberList.size() == 1) {
      saleOrder.setTaxNumber(taxNumberList.stream().findFirst().orElse(null));
    }
    saleOrderMap.put("taxNumber", saleOrder.getTaxNumber());
    return saleOrderMap;
  }
}
