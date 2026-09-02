/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

/** Utility class for computing supplier score indicators. */
public class SupplierScoreTool {

  public static final int RATE_SCALE = 2;
  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  private SupplierScoreTool() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Compute a percentage from a numerator and a denominator.
   *
   * @return the rate between 0 and 100 rounded to {@link #RATE_SCALE}, or null when the denominator
   *     is not positive (no data to rate).
   */
  public static BigDecimal computeRate(long numerator, long denominator) {
    if (denominator <= 0) {
      return null;
    }
    return BigDecimal.valueOf(numerator)
        .multiply(HUNDRED)
        .divide(BigDecimal.valueOf(denominator), RATE_SCALE, RoundingMode.HALF_UP);
  }

  /**
   * A purchase order line is on time when its last receipt happened on or before its estimated
   * receipt date. A line without a receipt date is never on time.
   */
  public static boolean isOnTime(LocalDate estimatedReceiptDate, LocalDate lastReceiptDate) {
    return estimatedReceiptDate != null
        && lastReceiptDate != null
        && !lastReceiptDate.isAfter(estimatedReceiptDate);
  }

  /** Label identifying the month of a snapshot, for instance {@code 2026-09}. */
  public static String computePeriodLabel(LocalDate snapshotDate) {
    return YearMonth.from(snapshotDate).toString();
  }

  /**
   * Compute the weighted average of a list of (value, weight) pairs. Pairs whose value is null or
   * whose weight is null or not positive are ignored, and the remaining weights are renormalised.
   *
   * @return the weighted average rounded to {@link #RATE_SCALE}, or null when no pair is taken into
   *     account.
   */
  public static BigDecimal computeWeightedAverage(
      List<Pair<BigDecimal, BigDecimal>> weightedValues) {
    BigDecimal weightedSum = BigDecimal.ZERO;
    BigDecimal weightSum = BigDecimal.ZERO;
    for (Pair<BigDecimal, BigDecimal> weightedValue : weightedValues) {
      BigDecimal value = weightedValue.getLeft();
      BigDecimal weight = weightedValue.getRight();
      if (value == null || weight == null || weight.signum() <= 0) {
        continue;
      }
      weightedSum = weightedSum.add(value.multiply(weight));
      weightSum = weightSum.add(weight);
    }
    if (weightSum.signum() == 0) {
      return null;
    }
    return weightedSum.divide(weightSum, RATE_SCALE, RoundingMode.HALF_UP);
  }
}
