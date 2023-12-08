package com.axelor.apps.contract.service.attributes;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.service.CurrencyScaleServiceContract;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class ContractLineAttrsServiceImpl implements ContractLineAttrsService {

  protected CurrencyScaleServiceContract currencyScaleServiceContract;
  protected AppAccountService appAccountService;

  @Inject
  public ContractLineAttrsServiceImpl(
      CurrencyScaleServiceContract currencyScaleServiceContract,
      AppAccountService appAccountService) {
    this.currencyScaleServiceContract = currencyScaleServiceContract;
    this.appAccountService = appAccountService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  protected void addQtyScale(Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("qty", "scale", appAccountService.getNbDecimalDigitForQty(), attrsMap);
  }

  protected void addPriceScale(Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("price", "scale", appAccountService.getNbDecimalDigitForUnitPrice(), attrsMap);
  }

  @Override
  public Map<String, Map<String, Object>> setScaleAndPrecision(Contract contract) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    if (contract != null) {
      int currencyScale = currencyScaleServiceContract.getScale(contract);

      this.addAttr("exTaxTotal", "scale", currencyScale, attrsMap);
      this.addAttr("inTaxTotal", "scale", currencyScale, attrsMap);

      this.addAttr("initialPricePerYear", "scale", currencyScale, attrsMap);
      this.addAttr("yearlyPriceRevalued", "scale", currencyScale, attrsMap);
    }

    this.addQtyScale(attrsMap);
    this.addPriceScale(attrsMap);

    return attrsMap;
  }
}
