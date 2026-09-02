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
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestSupplierScoreTool {

  @Test
  void testComputeRateZeroDenominator() {
    Assertions.assertNull(SupplierScoreTool.computeRate(3, 0));
  }

  @Test
  void testComputeRateNoneOnTime() {
    Assertions.assertEquals(new BigDecimal("0.00"), SupplierScoreTool.computeRate(0, 4));
  }

  @Test
  void testComputeRateAllOnTime() {
    Assertions.assertEquals(new BigDecimal("100.00"), SupplierScoreTool.computeRate(4, 4));
  }

  @Test
  void testComputeRateRounding() {
    Assertions.assertEquals(new BigDecimal("66.67"), SupplierScoreTool.computeRate(2, 3));
  }

  @Test
  void testIsOnTimeSameDay() {
    LocalDate date = LocalDate.of(2026, 3, 10);
    Assertions.assertTrue(SupplierScoreTool.isOnTime(date, date));
  }

  @Test
  void testIsOnTimeBefore() {
    Assertions.assertTrue(
        SupplierScoreTool.isOnTime(LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 9)));
  }

  @Test
  void testIsOnTimeLate() {
    Assertions.assertFalse(
        SupplierScoreTool.isOnTime(LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 11)));
  }

  @Test
  void testIsOnTimeNoReceipt() {
    Assertions.assertFalse(SupplierScoreTool.isOnTime(LocalDate.of(2026, 3, 10), null));
  }

  @Test
  void testIsOnTimeNoEstimatedDate() {
    Assertions.assertFalse(SupplierScoreTool.isOnTime(null, LocalDate.of(2026, 3, 10)));
  }

  @Test
  void testComputeWeightedAverageAllPresent() {
    List<Pair<BigDecimal, BigDecimal>> weightedValues =
        List.of(
            Pair.of(new BigDecimal("80"), new BigDecimal("3")),
            Pair.of(new BigDecimal("40"), new BigDecimal("1")));
    Assertions.assertEquals(
        new BigDecimal("70.00"), SupplierScoreTool.computeWeightedAverage(weightedValues));
  }

  @Test
  void testComputeWeightedAverageNullValueRenormalisesWeights() {
    List<Pair<BigDecimal, BigDecimal>> weightedValues =
        List.of(
            Pair.of(new BigDecimal("80"), new BigDecimal("1")),
            Pair.of(null, new BigDecimal("1")),
            Pair.of(new BigDecimal("60"), new BigDecimal("1")));
    Assertions.assertEquals(
        new BigDecimal("70.00"), SupplierScoreTool.computeWeightedAverage(weightedValues));
  }

  @Test
  void testComputeWeightedAverageZeroWeightExcludesValue() {
    List<Pair<BigDecimal, BigDecimal>> weightedValues =
        List.of(
            Pair.of(new BigDecimal("80"), new BigDecimal("1")),
            Pair.of(new BigDecimal("0"), BigDecimal.ZERO));
    Assertions.assertEquals(
        new BigDecimal("80.00"), SupplierScoreTool.computeWeightedAverage(weightedValues));
  }

  @Test
  void testComputeWeightedAverageNullWeightExcludesValue() {
    List<Pair<BigDecimal, BigDecimal>> weightedValues =
        List.of(
            Pair.of(new BigDecimal("80"), new BigDecimal("1")), Pair.of(new BigDecimal("0"), null));
    Assertions.assertEquals(
        new BigDecimal("80.00"), SupplierScoreTool.computeWeightedAverage(weightedValues));
  }

  @Test
  void testComputeWeightedAverageNothingAvailable() {
    List<Pair<BigDecimal, BigDecimal>> weightedValues =
        List.of(Pair.of(null, new BigDecimal("1")), Pair.of(new BigDecimal("50"), BigDecimal.ZERO));
    Assertions.assertNull(SupplierScoreTool.computeWeightedAverage(weightedValues));
  }

  @Test
  void testComputeWeightedAverageRounding() {
    List<Pair<BigDecimal, BigDecimal>> weightedValues =
        List.of(
            Pair.of(new BigDecimal("100"), new BigDecimal("1")),
            Pair.of(new BigDecimal("50"), new BigDecimal("1")),
            Pair.of(new BigDecimal("50"), new BigDecimal("1")));
    Assertions.assertEquals(
        new BigDecimal("66.67"), SupplierScoreTool.computeWeightedAverage(weightedValues));
  }
}
