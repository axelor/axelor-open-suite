package com.axelor.apps.base.service.discount;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class GlobalDiscountServiceImpl implements GlobalDiscountService {

  @Override
  public BigDecimal computeDiscountFixedEquivalence(
      BigDecimal exTaxTotal, BigDecimal priceBeforeGlobalDiscount) {
    if (exTaxTotal == null || priceBeforeGlobalDiscount == null) {
      return BigDecimal.ZERO;
    }
    return priceBeforeGlobalDiscount.subtract(exTaxTotal);
  }

  @Override
  public BigDecimal computeDiscountPercentageEquivalence(
      BigDecimal exTaxTotal, BigDecimal priceBeforeGlobalDiscount) {
    if (exTaxTotal == null || priceBeforeGlobalDiscount == null) {
      return BigDecimal.ZERO;
    }
    return priceBeforeGlobalDiscount
        .subtract(exTaxTotal)
        .multiply(BigDecimal.valueOf(100))
        .divide(priceBeforeGlobalDiscount, RoundingMode.HALF_UP);
  }

  @Override
  public Map<String, Map<String, Object>> setDiscountDummies(
      Integer discountTypeSelect,
      Currency currency,
      BigDecimal exTaxTotal,
      BigDecimal priceBeforeGlobalDiscount) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();
    switch (discountTypeSelect) {
      case PriceListLineRepository.AMOUNT_TYPE_PERCENT:
        setPercentageGlobalDiscountDummies(
            currency, exTaxTotal, priceBeforeGlobalDiscount, attrsMap);
        break;
      case PriceListLineRepository.AMOUNT_TYPE_FIXED:
        setFixedGlobalDiscountDummies(currency, exTaxTotal, priceBeforeGlobalDiscount, attrsMap);
        break;
    }
    return attrsMap;
  }

  protected void setPercentageGlobalDiscountDummies(
      Currency currency,
      BigDecimal exTaxTotal,
      BigDecimal priceBeforeGlobalDiscount,
      Map<String, Map<String, Object>> attrsMap) {
    if (currency == null) {
      return;
    }
    BigDecimal equivalence = computeDiscountFixedEquivalence(exTaxTotal, priceBeforeGlobalDiscount);
    this.addAttr("discountCurrency", "value", "%", attrsMap);
    this.addAttr("discountScale", "value", 2, attrsMap);
    this.addAttr("$discountEquivalence", "value", equivalence, attrsMap);
    this.addAttr(
        "$swapDiscountTypeBtn",
        "value",
        formatEquivalence(equivalence, currency.getSymbol(), currency.getNumberOfDecimals()),
        attrsMap);
  }

  protected void setFixedGlobalDiscountDummies(
      Currency currency,
      BigDecimal exTaxTotal,
      BigDecimal priceBeforeGlobalDiscount,
      Map<String, Map<String, Object>> attrsMap) {
    if (currency == null) {
      return;
    }
    BigDecimal equivalence =
        computeDiscountPercentageEquivalence(exTaxTotal, priceBeforeGlobalDiscount);
    this.addAttr("discountCurrency", "value", currency.getSymbol(), attrsMap);
    this.addAttr("discountScale", "value", currency.getNumberOfDecimals(), attrsMap);
    this.addAttr("$discountEquivalence", "value", equivalence, attrsMap);
    this.addAttr("$swapDiscountTypeBtn", "value", formatEquivalence(equivalence, "%", 2), attrsMap);
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  protected String formatEquivalence(BigDecimal value, String symbol, int scale) {
    return String.format("%s %s", value.setScale(scale, RoundingMode.HALF_UP), symbol);
  }
}
