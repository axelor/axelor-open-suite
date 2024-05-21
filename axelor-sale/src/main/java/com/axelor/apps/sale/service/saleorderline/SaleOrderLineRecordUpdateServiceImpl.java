package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.helper.SaleOrderLineHelper;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.google.inject.Inject;

import java.math.BigDecimal;
import java.util.Map;

public class SaleOrderLineRecordUpdateServiceImpl implements SaleOrderLineRecordUpdateService {

  protected final AppSaleService appSaleService;

  @Inject
  public SaleOrderLineRecordUpdateServiceImpl(AppSaleService appSaleService) {
    this.appSaleService = appSaleService;
  }

  @Override
  public void setCompanyCurrencyValue(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrder != null && saleOrder.getCompany() != null) {
      SaleOrderLineHelper.addAttr(
          "$companyCurrency", "value", saleOrder.getCompany().getCurrency(), attrsMap);
      return;
    }
    SaleOrder oldVersionSaleOrder = saleOrderLine.getOldVersionSaleOrder();
    if (oldVersionSaleOrder != null && oldVersionSaleOrder.getCompany() != null) {
      SaleOrderLineHelper.addAttr(
          "$companyCurrency", "value", oldVersionSaleOrder.getCompany().getCurrency(), attrsMap);
    }
  }

  @Override
  public void setCurrencyValue(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrder != null) {
      SaleOrderLineHelper.addAttr("$currency", "value", saleOrder.getCurrency(), attrsMap);
      return;
    }
    SaleOrder oldVersionSaleOrder = saleOrderLine.getOldVersionSaleOrder();
    if (oldVersionSaleOrder != null) {
      SaleOrderLineHelper.addAttr(
          "$currency", "value", oldVersionSaleOrder.getCurrency(), attrsMap);
    }
  }

  @Override
  public void initDummyFields(Map<String, Map<String, Object>> attrsMap) {
    SaleOrderLineHelper.addAttr(
        "$nbDecimalDigitForQty", "value", appSaleService.getNbDecimalDigitForQty(), attrsMap);
    SaleOrderLineHelper.addAttr(
        "$nbDecimalDigitForUnitPrice",
        "value",
        appSaleService.getNbDecimalDigitForUnitPrice(),
        attrsMap);
  }

  @Override
  public void setNonNegotiableValue(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrder != null && saleOrder.getPriceList() != null) {
      SaleOrderLineHelper.addAttr(
          "$nonNegotiable", "value", saleOrder.getPriceList().getNonNegotiable(), attrsMap);
    }
  }

  @Override
  public void setInitialQty(Map<String, Map<String, Object>> attrsMap, BigDecimal qty) {
    SaleOrderLineHelper.addAttr("qty", "value", qty, attrsMap);
  }
}
