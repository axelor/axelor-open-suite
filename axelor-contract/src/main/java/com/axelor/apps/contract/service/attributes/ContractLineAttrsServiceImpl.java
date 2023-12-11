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

  protected String computeField(String field, String prefix) {
    return String.format("%s%s", prefix, field);
  }

  protected void addQtyScale(Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("qty", prefix),
        "scale",
        appAccountService.getNbDecimalDigitForQty(),
        attrsMap);
  }

  protected void addPriceScale(Map<String, Map<String, Object>> attrsMap, String prefix) {
    this.addAttr(
        this.computeField("price", prefix),
        "scale",
        appAccountService.getNbDecimalDigitForUnitPrice(),
        attrsMap);
  }

  @Override
  public Map<String, Map<String, Object>> setScaleAndPrecision(Contract contract, String prefix) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    if (contract != null) {
      int currencyScale = currencyScaleServiceContract.getScale(contract);

      this.addAttr(this.computeField("exTaxTotal", prefix), "scale", currencyScale, attrsMap);
      this.addAttr(this.computeField("inTaxTotal", prefix), "scale", currencyScale, attrsMap);

      this.addAttr(
          this.computeField("initialPricePerYear", prefix), "scale", currencyScale, attrsMap);
      this.addAttr(
          this.computeField("yearlyPriceRevalued", prefix), "scale", currencyScale, attrsMap);
    }

    this.addQtyScale(attrsMap, prefix);
    this.addPriceScale(attrsMap, prefix);

    return attrsMap;
  }
}
