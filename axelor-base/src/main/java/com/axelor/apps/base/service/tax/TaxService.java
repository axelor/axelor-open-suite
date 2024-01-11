/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.utils.date.DateTool;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

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

          if (DateTool.isBetween(taxLine.getStartDate(), taxLine.getEndDate(), localDate)) {
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

  public BigDecimal convertUnitPrice(
      Boolean priceIsAti, TaxLine taxLine, BigDecimal price, int scale) {

    if (taxLine == null) {
      return price;
    }

    if (priceIsAti) {
      price =
          price.divide(
              taxLine.getValue().divide(new BigDecimal(100)).add(BigDecimal.ONE),
              scale,
              RoundingMode.HALF_UP);
    } else {
      price =
          price
              .add(price.multiply(taxLine.getValue().divide(new BigDecimal(100))))
              .setScale(scale, RoundingMode.HALF_UP);
    }
    return price;
  }
}
