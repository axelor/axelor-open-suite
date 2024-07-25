package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.Localization;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderLineDummyServiceImpl implements SaleOrderLineDummyService {

  protected AppBaseService appBaseService;
  protected SaleOrderLineDiscountService saleOrderLineDiscountService;

  @Inject
  public SaleOrderLineDummyServiceImpl(
      AppBaseService appBaseService, SaleOrderLineDiscountService saleOrderLineDiscountService) {
    this.appBaseService = appBaseService;
    this.saleOrderLineDiscountService = saleOrderLineDiscountService;
  }

  public Map<String, Object> getOnNewDummies(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Object> dummyFields = new HashMap<>();
    dummyFields.putAll(initPartnerLanguage(saleOrder));
    dummyFields.putAll(initCurrency(saleOrder, saleOrderLine));
    dummyFields.putAll(initNonNegotiable(saleOrder));
    dummyFields.putAll(initDecimals(saleOrder));
    dummyFields.putAll(initCompanyCurrency(saleOrder, saleOrderLine));
    dummyFields.putAll(initReadonlyDummy(saleOrder, saleOrderLine));
    return dummyFields;
  }

  @Override
  public Map<String, Object> getOnLoadDummies(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> dummyFields = new HashMap<>();
    dummyFields.putAll(initPartnerLanguage(saleOrder));
    dummyFields.putAll(initCurrency(saleOrder, saleOrderLine));
    dummyFields.putAll(initNonNegotiable(saleOrder));
    dummyFields.putAll(initCompanyCurrency(saleOrder, saleOrderLine));
    dummyFields.putAll(fillMaxDiscount(saleOrder, saleOrderLine));
    dummyFields.putAll(initReadonlyDummy(saleOrder, saleOrderLine));
    return dummyFields;
  }

  @Override
  public Map<String, Object> getOnNewEditableDummies(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Object> dummyFields = new HashMap<>();
    dummyFields.putAll(initNonNegotiable(saleOrder));
    dummyFields.putAll(initDecimals(saleOrder));
    return dummyFields;
  }

  @Override
  public Map<String, Object> getOnProductChangeDummies(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> dummyFields = new HashMap<>();
    dummyFields.putAll(fillMaxDiscount(saleOrder, saleOrderLine));
    return dummyFields;
  }

  @Override
  public Map<String, Object> getOnDiscountTypeChangeDummies(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> dummyFields = new HashMap<>();
    dummyFields.putAll(fillMaxDiscount(saleOrder, saleOrderLine));
    return dummyFields;
  }

  protected Map<String, Object> initPartnerLanguage(SaleOrder saleOrder) {
    Map<String, Object> dummyFields = new HashMap<>();
    String languageCode =
        Optional.ofNullable(saleOrder.getClientPartner())
            .map(Partner::getLocalization)
            .map(Localization::getLanguage)
            .map(Language::getCode)
            .orElse("");
    dummyFields.put("$partnerLanguage", languageCode.toUpperCase());
    return dummyFields;
  }

  protected Map<String, Object> initCurrency(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> dummyFields = new HashMap<>();
    dummyFields.put("$currency", saleOrder.getCurrency());
    SaleOrder oldVersionSaleOrder = saleOrderLine.getOldVersionSaleOrder();
    if (oldVersionSaleOrder != null) {
      dummyFields.put("$currency", oldVersionSaleOrder.getCurrency());
    }
    return dummyFields;
  }

  protected Map<String, Object> initNonNegotiable(SaleOrder saleOrder) {
    Map<String, Object> dummyFields = new HashMap<>();
    PriceList priceList = saleOrder.getPriceList();

    if (priceList != null) {
      dummyFields.put("$nonNegotiable", priceList.getNonNegotiable());
    }

    return dummyFields;
  }

  protected Map<String, Object> initDecimals(SaleOrder saleOrder) {
    Map<String, Object> dummyFields = new HashMap<>();
    AppBase appBase = appBaseService.getAppBase();
    Currency currency = saleOrder.getCurrency();

    dummyFields.put("$nbDecimalDigitForUnitPrice", appBase.getNbDecimalDigitForUnitPrice());
    dummyFields.put("$nbDecimalDigitForQty", appBase.getNbDecimalDigitForQty());
    if (currency != null) {
      dummyFields.put("$currencyNumberOfDecimals", currency.getNumberOfDecimals());
    }
    return dummyFields;
  }

  protected Map<String, Object> initCompanyCurrency(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> dummyFields = new HashMap<>();
    Company company = saleOrder.getCompany();
    Currency currency = null;
    if (company != null) {
      currency = company.getCurrency();
    }
    dummyFields.put("$companyCurrency", currency);

    SaleOrder oldVersionSaleOrder = saleOrderLine.getOldVersionSaleOrder();
    if (oldVersionSaleOrder != null) {
      Company oldCompany = oldVersionSaleOrder.getCompany();
      if (oldCompany != null && oldCompany.getCurrency() != null) {
        dummyFields.put("$currency", oldCompany.getCurrency());
      }
    }

    return dummyFields;
  }

  protected Map<String, Object> fillMaxDiscount(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Map<String, Object> dummyFields = new HashMap<>();
    BigDecimal maxDiscount =
        saleOrderLineDiscountService.computeMaxDiscount(saleOrder, saleOrderLine);
    dummyFields.put("$maxDiscount", maxDiscount);
    return dummyFields;
  }

  protected Map<String, Object> initReadonlyDummy(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> dummyFields = new HashMap<>();
    int statusSelect = saleOrder.getStatusSelect();
    dummyFields.put(
        "$isReadOnly",
        statusSelect != SaleOrderRepository.STATUS_DRAFT_QUOTATION
            && !saleOrder.getOrderBeingEdited());
    return dummyFields;
  }
}
