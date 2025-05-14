/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.discount;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.interfaces.GlobalDiscounter;
import com.axelor.apps.base.interfaces.GlobalDiscounterLine;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public abstract class GlobalDiscountAbstractService {

  protected abstract void compute(GlobalDiscounter globalDiscounter) throws AxelorException;

  protected abstract List<? extends GlobalDiscounterLine> getGlobalDiscounterLines(
      GlobalDiscounter globalDiscounter);

  public void applyGlobalDiscountOnLines(GlobalDiscounter globalDiscounter) throws AxelorException {
    if (globalDiscounter == null
        || CollectionUtils.isEmpty(getGlobalDiscounterLines(globalDiscounter))) {
      return;
    }
    computePriceBeforeGlobalDiscount(globalDiscounter);
    switch (globalDiscounter.getDiscountTypeSelect()) {
      case PriceListLineRepository.AMOUNT_TYPE_PERCENT:
        applyPercentageGlobalDiscountOnLines(globalDiscounter);
        compute(globalDiscounter);
        adjustPercentageDiscountOnLastLine(globalDiscounter);
        break;
      case PriceListLineRepository.AMOUNT_TYPE_FIXED:
        applyFixedGlobalDiscountOnLines(globalDiscounter);
        compute(globalDiscounter);
        adjustFixedDiscountOnLastLine(globalDiscounter);
        break;
    }
  }

  protected void computePriceBeforeGlobalDiscount(GlobalDiscounter globalDiscounter) {
    globalDiscounter.setPriceBeforeGlobalDiscount(
        getGlobalDiscounterLines(globalDiscounter).stream()
            .map(
                globalDiscounterLine ->
                    globalDiscounterLine.getPrice().multiply(globalDiscounterLine.getQty()))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO));
  }

  protected void applyPercentageGlobalDiscountOnLines(GlobalDiscounter globalDiscounter) {
    getGlobalDiscounterLines(globalDiscounter).stream()
        .filter(
            globalDiscounterLine ->
                globalDiscounterLine
                    .getTypeSelect()
                    .equals(globalDiscounterLine.getTypeSelectNormal()))
        .forEach(
            globalDiscounterLine -> {
              globalDiscounterLine.setDiscountTypeSelect(
                  PriceListLineRepository.AMOUNT_TYPE_PERCENT);
              globalDiscounterLine.setDiscountAmount(globalDiscounter.getDiscountAmount());
            });
  }

  protected void applyFixedGlobalDiscountOnLines(GlobalDiscounter globalDiscounter) {
    getGlobalDiscounterLines(globalDiscounter).stream()
        .filter(
            globalDiscounterLine ->
                globalDiscounterLine
                    .getTypeSelect()
                    .equals(globalDiscounterLine.getTypeSelectNormal()))
        .forEach(
            globalDiscounterLine -> {
              globalDiscounterLine.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_FIXED);
              globalDiscounterLine.setDiscountAmount(
                  globalDiscounterLine
                      .getPrice()
                      .divide(globalDiscounter.getPriceBeforeGlobalDiscount(), RoundingMode.HALF_UP)
                      .multiply(globalDiscounter.getDiscountAmount()));
            });
  }

  protected void adjustPercentageDiscountOnLastLine(GlobalDiscounter globalDiscounter) {
    BigDecimal priceDiscountedByLine = globalDiscounter.getExTaxTotal();
    BigDecimal priceDiscountedOnTotal =
        globalDiscounter
            .getPriceBeforeGlobalDiscount()
            .multiply(BigDecimal.valueOf(100).subtract(globalDiscounter.getDiscountAmount()))
            .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
    if (priceDiscountedByLine.compareTo(priceDiscountedOnTotal) == 0) {
      return;
    }
    BigDecimal differenceInDiscount = priceDiscountedOnTotal.subtract(priceDiscountedByLine);

    GlobalDiscounterLine lastLine =
        getGlobalDiscounterLines(globalDiscounter)
            .get(getGlobalDiscounterLines(globalDiscounter).size() - 1);

    lastLine.setDiscountAmount(
        BigDecimal.ONE
            .subtract(
                lastLine
                    .getPriceDiscounted()
                    .add(differenceInDiscount)
                    .divide(lastLine.getPrice(), RoundingMode.HALF_UP))
            .multiply(BigDecimal.valueOf(100)));
  }

  protected void adjustFixedDiscountOnLastLine(GlobalDiscounter globalDiscounter) {
    BigDecimal priceDiscountedByLine = globalDiscounter.getExTaxTotal();

    BigDecimal priceDiscountedOnTotal =
        globalDiscounter
            .getPriceBeforeGlobalDiscount()
            .subtract(globalDiscounter.getDiscountAmount());
    if (priceDiscountedByLine.compareTo(priceDiscountedOnTotal) == 0) {
      return;
    }

    BigDecimal differenceInDiscount = priceDiscountedOnTotal.subtract(priceDiscountedByLine);
    GlobalDiscounterLine lastLine =
        getGlobalDiscounterLines(globalDiscounter)
            .get(getGlobalDiscounterLines(globalDiscounter).size() - 1);
    lastLine.setDiscountAmount(
        lastLine
            .getDiscountAmount()
            .subtract(differenceInDiscount.divide(lastLine.getQty(), RoundingMode.HALF_UP)));
  }

  public BigDecimal computeDiscountFixedEquivalence(GlobalDiscounter globalDiscounter) {
    if (globalDiscounter == null) {
      return BigDecimal.ZERO;
    }
    BigDecimal exTaxTotal = globalDiscounter.getExTaxTotal();
    BigDecimal priceBeforeGlobalDiscount = globalDiscounter.getPriceBeforeGlobalDiscount();
    if (exTaxTotal == null || priceBeforeGlobalDiscount == null) {
      return BigDecimal.ZERO;
    }
    return priceBeforeGlobalDiscount.subtract(exTaxTotal);
  }

  public BigDecimal computeDiscountPercentageEquivalence(GlobalDiscounter globalDiscounter) {
    if (globalDiscounter == null) {
      return BigDecimal.ZERO;
    }
    BigDecimal exTaxTotal = globalDiscounter.getExTaxTotal();
    BigDecimal priceBeforeGlobalDiscount = globalDiscounter.getPriceBeforeGlobalDiscount();
    if (exTaxTotal == null || priceBeforeGlobalDiscount == null) {
      return BigDecimal.ZERO;
    }
    return priceBeforeGlobalDiscount
        .subtract(exTaxTotal)
        .multiply(BigDecimal.valueOf(100))
        .divide(priceBeforeGlobalDiscount, RoundingMode.HALF_UP);
  }

  public Map<String, Map<String, Object>> setDiscountDummies(GlobalDiscounter globalDiscounter) {
    if (globalDiscounter == null) {
      return null;
    }
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();
    switch (globalDiscounter.getDiscountTypeSelect()) {
      case PriceListLineRepository.AMOUNT_TYPE_PERCENT:
        setPercentageGlobalDiscountDummies(globalDiscounter, attrsMap);
        break;
      case PriceListLineRepository.AMOUNT_TYPE_FIXED:
        setFixedGlobalDiscountDummies(globalDiscounter, attrsMap);
        break;
    }
    return attrsMap;
  }

  protected void setPercentageGlobalDiscountDummies(
      GlobalDiscounter globalDiscounter, Map<String, Map<String, Object>> attrsMap) {
    Currency currency = globalDiscounter.getCurrency();
    if (currency == null) {
      return;
    }
    BigDecimal equivalence = computeDiscountFixedEquivalence(globalDiscounter);
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
      GlobalDiscounter globalDiscounter, Map<String, Map<String, Object>> attrsMap) {
    Currency currency = globalDiscounter.getCurrency();
    if (currency == null) {
      return;
    }
    BigDecimal equivalence = computeDiscountPercentageEquivalence(globalDiscounter);
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
