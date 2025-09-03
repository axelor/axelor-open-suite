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
package com.axelor.apps.base.service.tax;

import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class TaxService {

  /**
   * Fonction permettant de récupérer le taux de TVA d'une TVA
   *
   * @param tax Une TVA
   * @return Le taux de TVA
   * @throws AxelorException
   */
  public BigDecimal getTaxRate(Tax tax, LocalDate localDate) throws AxelorException {

    return this.getTaxLine(tax, localDate).getValue();
  }

  /**
   * Fonction permettant de récupérer le taux de TVA d'une TVA
   *
   * @param tax Une TVA
   * @return Le taux de TVA
   * @throws AxelorException
   */
  public TaxLine getTaxLine(Tax tax, LocalDate localDate) throws AxelorException {

    if (tax == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(BaseExceptionMessage.TAX_2));
    }

    if (tax.getActiveTaxLine() != null) {
      return tax.getActiveTaxLine();
    }
    if (localDate != null) {
      if (tax.getTaxLineList() != null && !tax.getTaxLineList().isEmpty()) {

        for (TaxLine taxLine : tax.getTaxLineList()) {

          if (LocalDateHelper.isBetween(taxLine.getStartDate(), taxLine.getEndDate(), localDate)) {
            return taxLine;
          }
        }
      }
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(BaseExceptionMessage.TAX_DATE_MISSING),
          tax.getName());
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(BaseExceptionMessage.TAX_1),
        tax.getName());
  }

  /**
   * Fonction permettant de récupérer le taux de TVA d'une TVA
   *
   * @param taxSet Une TVA
   * @return Le taux de TVA
   * @throws AxelorException
   */
  public Set<TaxLine> getTaxLineSet(Set<Tax> taxSet, LocalDate localDate) throws AxelorException {

    if (CollectionUtils.isEmpty(taxSet)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(BaseExceptionMessage.TAX_2));
    }
    Set<TaxLine> taxLineSet = Sets.newHashSet();

    for (Tax tax : taxSet) {
      if (tax.getActiveTaxLine() != null) {
        taxLineSet.add(tax.getActiveTaxLine());
      } else if (CollectionUtils.isNotEmpty(tax.getTaxLineList()) && localDate != null) {
        taxLineSet.add(
            tax.getTaxLineList().stream()
                .filter(
                    taxLine ->
                        LocalDateHelper.isBetween(
                            taxLine.getStartDate(), taxLine.getEndDate(), localDate))
                .findFirst()
                .orElse(null));
      }
    }
    if (CollectionUtils.isNotEmpty(taxLineSet)) {
      return taxLineSet;
    }
    if (localDate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(BaseExceptionMessage.TAX_DATE_MISSING),
          taxSet.stream().map(Tax::getName).collect(Collectors.joining(",")));
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(BaseExceptionMessage.TAX_1),
        taxSet.stream()
            .map(Tax::getName)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(",")));
  }

  public BigDecimal convertUnitPrice(
      Boolean priceIsAti, Set<TaxLine> taxLineSet, BigDecimal price, int scale) {

    if (CollectionUtils.isEmpty(taxLineSet)) {
      return price;
    }

    if (priceIsAti) {
      price =
          price.divide(
              getTotalTaxRate(taxLineSet).add(BigDecimal.ONE), scale, RoundingMode.HALF_UP);
    } else {
      price =
          price
              .add(price.multiply(getTotalTaxRate(taxLineSet)))
              .setScale(scale, RoundingMode.HALF_UP);
    }
    return price;
  }

  public BigDecimal getTotalTaxRate(Set<TaxLine> taxLineSet) {
    return getTotalTaxRateInPercentage(taxLineSet).divide(BigDecimal.valueOf(100));
  }

  public BigDecimal getTotalTaxRateInPercentage(Set<TaxLine> taxLineSet) {
    if (CollectionUtils.isEmpty(taxLineSet)) {
      return BigDecimal.ZERO;
    }
    return taxLineSet.stream()
        .filter(Objects::nonNull)
        .map(TaxLine::getValue)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public String computeTaxCode(Set<TaxLine> taxLineSet) {
    if (CollectionUtils.isEmpty(taxLineSet)) {
      return "";
    }
    return taxLineSet.stream()
        .filter(Objects::nonNull)
        .map(TaxLine::getTax)
        .filter(Objects::nonNull)
        .map(Tax::getCode)
        .sorted()
        .collect(Collectors.joining("/"));
  }

  public Set<Tax> getTaxSet(Set<TaxLine> taxLineSet) {
    if (CollectionUtils.isEmpty(taxLineSet)) {
      return Sets.newHashSet();
    }
    return taxLineSet.stream().map(TaxLine::getTax).collect(Collectors.toSet());
  }
}
