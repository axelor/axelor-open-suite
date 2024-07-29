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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public interface TaxService {

  /**
   * Fonction permettant de récupérer le taux de TVA d'une TVA
   *
   * @param tax Une TVA
   * @return Le taux de TVA
   * @throws AxelorException
   */
  BigDecimal getTaxRate(Tax tax, LocalDate localDate) throws AxelorException;

  /**
   * Fonction permettant de récupérer le taux de TVA d'une TVA
   *
   * @param tax Une TVA
   * @return Le taux de TVA
   * @throws AxelorException
   */
  TaxLine getTaxLine(Tax tax, LocalDate localDate) throws AxelorException;

  /**
   * Fonction permettant de récupérer le taux de TVA d'une TVA
   *
   * @param taxSet Une TVA
   * @return Le taux de TVA
   * @throws AxelorException
   */
  Set<TaxLine> getTaxLineSet(Set<Tax> taxSet, LocalDate localDate) throws AxelorException;

  BigDecimal convertUnitPrice(
      Boolean priceIsAti, Set<TaxLine> taxLineSet, BigDecimal price, int scale);

  BigDecimal getTotalTaxRate(Set<TaxLine> taxLineSet);

  BigDecimal getTotalTaxRateInPercentage(Set<TaxLine> taxLineSet);

  String computeTaxCode(Set<TaxLine> taxLineSet);

  Set<Tax> getTaxSet(Set<TaxLine> taxLineSet);
}
