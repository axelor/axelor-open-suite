package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineDiscountService;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.supplychain.service.SaleOrderLineDummySupplychainServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChain;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderLineDummyBudgetServiceImpl extends SaleOrderLineDummySupplychainServiceImpl {

  @Inject
  public SaleOrderLineDummyBudgetServiceImpl(
      AppBaseService appBaseService,
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain,
      StockMoveLineRepository stockMoveLineRepository) {
    super(
        appBaseService,
        saleOrderLineDiscountService,
        saleOrderLineServiceSupplyChain,
        stockMoveLineRepository);
  }

  @Override
  public Map<String, Object> getOnNewDummies(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Object> dummyFields = super.getOnNewDummies(saleOrderLine, saleOrder);

    if (appBaseService.isApp("budget")) {
      dummyFields.putAll(fillCurrencyFields(saleOrder));
    }

    return dummyFields;
  }

  @Override
  public Map<String, Object> getOnLoadDummies(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> dummyFields = super.getOnLoadDummies(saleOrderLine, saleOrder);

    if (appBaseService.isApp("budget")) {
      dummyFields.putAll(fillCurrencyFields(saleOrder));
    }

    return dummyFields;
  }

  protected Map<String, Object> fillCurrencyFields(SaleOrder saleOrder) {
    Map<String, Object> dummyFields = new HashMap<>();
    Currency currency =
        Optional.of(saleOrder).map(SaleOrder::getCompany).map(Company::getCurrency).orElse(null);
    if (currency != null) {
      dummyFields.put("$currencySymbol", currency.getSymbol());
      dummyFields.put("$companyCurrencyScale", currency.getNumberOfDecimals());
    }

    return dummyFields;
  }
}
