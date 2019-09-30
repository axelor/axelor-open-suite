/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.tool;

import com.axelor.apps.tool.date.DateTool;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Outils simplifiant l'utilisation des nombres. */
public final class DecimalTool {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Proratiser une valeur en fonction de date.
   *
   * @param fromDate Date de début de la période de conso.
   * @param toDate Date de fin de la période de conso.
   * @param date Date de proratisation.
   * @param value Valeur initiale.
   * @return La quantité proratisée.
   */
  public static BigDecimal prorata(
      LocalDate fromDate, LocalDate toDate, LocalDate date, BigDecimal value, int scale) {

    BigDecimal prorataValue = BigDecimal.ZERO;

    if (fromDate == null || toDate == null || date == null) {
      return prorataValue;
    }

    BigDecimal totalDays = new BigDecimal(DateTool.daysBetween(fromDate, toDate, false));
    BigDecimal days = new BigDecimal(DateTool.daysBetween(date, toDate, false));

    prorataValue = prorata(totalDays, days, value, scale);

    LOG.debug(
        "Proratisation ({} pour {} à {}) à la date du {} : {}",
        new Object[] {value, fromDate, toDate, date, prorataValue});

    return prorataValue;
  }

  /**
   * Proratiser une valeur en fonction du nombre de jours. (Règle de 3)
   *
   * @param totalDays Le nombre total de jour.
   * @param days Le nombre de jour.
   * @param value La valeur à proratiser.
   * @return La valeur proratisée.
   */
  public static BigDecimal prorata(
      BigDecimal totalDays, BigDecimal days, BigDecimal value, int scale) {

    BigDecimal prorataValue = BigDecimal.ZERO;

    if (totalDays.compareTo(prorataValue) == 0) {
      return prorataValue;
    } else {
      prorataValue =
          (days.multiply(value).divide(totalDays, scale, BigDecimal.ROUND_HALF_EVEN))
              .setScale(scale, RoundingMode.HALF_EVEN);
    }

    LOG.debug(
        "Proratisation d'une valeur sur un total de jour {} pour {} jours et une valeur de {} : {}",
        new Object[] {totalDays, days, value, prorataValue});

    return prorataValue;
  }

  public static BigDecimal prorata(
      LocalDate fromDate, LocalDate toDate, LocalDate date, BigDecimal value) {

    return prorata(fromDate, toDate, date, value, 2);
  }

  public static BigDecimal prorata(BigDecimal totalDays, BigDecimal days, BigDecimal value) {

    return prorata(totalDays, days, value, 2);
  }

  /**
   * Fonction permettant d'obtenir le pourcentage d'une valeur.
   *
   * @param value Valeur initiale.
   * @param percent Pourcentage (format : 10%).
   * @param scale Précision.
   * @return Le pourcentage de la valeur initiale.
   */
  public static BigDecimal percent(BigDecimal value, BigDecimal percent, int scale) {

    return value.multiply(percent).divide(new BigDecimal("100"), scale, RoundingMode.HALF_EVEN);
  }
}
