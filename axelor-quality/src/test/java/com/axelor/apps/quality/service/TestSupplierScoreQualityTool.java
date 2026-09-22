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
package com.axelor.apps.quality.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestSupplierScoreQualityTool {

  private static final BigDecimal CRITICAL = new BigDecimal("10");
  private static final BigDecimal MAJOR = new BigDecimal("3");
  private static final BigDecimal MINOR = new BigDecimal("1");

  @Test
  void testGetGravityWeightCritical() {
    Assertions.assertEquals(
        CRITICAL, SupplierScoreQualityTool.getGravityWeight(1, CRITICAL, MAJOR, MINOR));
  }

  @Test
  void testGetGravityWeightMajor() {
    Assertions.assertEquals(
        MAJOR, SupplierScoreQualityTool.getGravityWeight(2, CRITICAL, MAJOR, MINOR));
  }

  @Test
  void testGetGravityWeightMinor() {
    Assertions.assertEquals(
        MINOR, SupplierScoreQualityTool.getGravityWeight(3, CRITICAL, MAJOR, MINOR));
  }

  @Test
  void testGetGravityWeightUngradedCountsAsMajor() {
    Assertions.assertEquals(
        MAJOR, SupplierScoreQualityTool.getGravityWeight(null, CRITICAL, MAJOR, MINOR));
  }

  @Test
  void testGetGravityWeightZeroCountsAsMajor() {
    Assertions.assertEquals(
        MAJOR, SupplierScoreQualityTool.getGravityWeight(0, CRITICAL, MAJOR, MINOR));
  }

  @Test
  void testGetGravityWeightUnknownGravityCountsAsMajor() {
    Assertions.assertEquals(
        MAJOR, SupplierScoreQualityTool.getGravityWeight(7, CRITICAL, MAJOR, MINOR));
  }

  @Test
  void testGetGravityWeightNullOrNegativeWeightIsZero() {
    Assertions.assertEquals(
        BigDecimal.ZERO, SupplierScoreQualityTool.getGravityWeight(1, null, MAJOR, MINOR));
    Assertions.assertEquals(
        BigDecimal.ZERO,
        SupplierScoreQualityTool.getGravityWeight(3, CRITICAL, MAJOR, new BigDecimal("-2")));
  }

  @Test
  void testComputeWeightedNonConformityCountEmpty() {
    Assertions.assertEquals(
        BigDecimal.ZERO,
        SupplierScoreQualityTool.computeWeightedNonConformityCount(
            new HashMap<>(), CRITICAL, MAJOR, MINOR));
  }

  @Test
  void testComputeWeightedNonConformityCountAllGravities() {
    Map<Integer, Long> countByGravity = new HashMap<>();
    countByGravity.put(1, 1L);
    countByGravity.put(3, 2L);
    countByGravity.put(null, 1L);
    Assertions.assertEquals(
        new BigDecimal("15"),
        SupplierScoreQualityTool.computeWeightedNonConformityCount(
            countByGravity, CRITICAL, MAJOR, MINOR));
  }

  @Test
  void testComputeWeightedNonConformityCountIgnoresEmptyCounts() {
    Map<Integer, Long> countByGravity = new HashMap<>();
    countByGravity.put(1, 0L);
    countByGravity.put(2, null);
    countByGravity.put(3, 4L);
    Assertions.assertEquals(
        new BigDecimal("4"),
        SupplierScoreQualityTool.computeWeightedNonConformityCount(
            countByGravity, CRITICAL, MAJOR, MINOR));
  }

  @Test
  void testComputeQualityScoreBothRates() {
    Assertions.assertEquals(
        new BigDecimal("68.33"),
        SupplierScoreQualityTool.computeQualityScore(
            new BigDecimal("90"), BigDecimal.ONE, new BigDecimal("25"), new BigDecimal("0.5")));
  }

  @Test
  void testComputeQualityScoreOneRateMissing() {
    Assertions.assertEquals(
        new BigDecimal("25.00"),
        SupplierScoreQualityTool.computeQualityScore(
            null, BigDecimal.ONE, new BigDecimal("25"), new BigDecimal("0.5")));
  }

  @Test
  void testComputeQualityScoreNothingAvailable() {
    Assertions.assertNull(
        SupplierScoreQualityTool.computeQualityScore(
            null, BigDecimal.ONE, new BigDecimal("25"), BigDecimal.ZERO));
  }

  @Test
  void testComputeTrendMissingScore() {
    Assertions.assertNull(
        SupplierScoreQualityTool.computeTrend(null, new BigDecimal("70"), new BigDecimal("5")));
    Assertions.assertNull(
        SupplierScoreQualityTool.computeTrend(new BigDecimal("70"), null, new BigDecimal("5")));
  }

  @Test
  void testComputeTrendStableWithinTolerance() {
    Assertions.assertEquals(
        SupplierScoreQualityTool.TREND_STABLE,
        SupplierScoreQualityTool.computeTrend(
            new BigDecimal("75"), new BigDecimal("70"), new BigDecimal("5")));
    Assertions.assertEquals(
        SupplierScoreQualityTool.TREND_STABLE,
        SupplierScoreQualityTool.computeTrend(
            new BigDecimal("65"), new BigDecimal("70"), new BigDecimal("5")));
  }

  @Test
  void testComputeTrendImproving() {
    Assertions.assertEquals(
        SupplierScoreQualityTool.TREND_IMPROVING,
        SupplierScoreQualityTool.computeTrend(
            new BigDecimal("75.01"), new BigDecimal("70"), new BigDecimal("5")));
  }

  @Test
  void testComputeTrendDegrading() {
    Assertions.assertEquals(
        SupplierScoreQualityTool.TREND_DEGRADING,
        SupplierScoreQualityTool.computeTrend(
            new BigDecimal("68.33"), new BigDecimal("75"), new BigDecimal("5")));
  }

  @Test
  void testComputeTrendNullToleranceMeansExact() {
    Assertions.assertEquals(
        SupplierScoreQualityTool.TREND_STABLE,
        SupplierScoreQualityTool.computeTrend(new BigDecimal("70"), new BigDecimal("70"), null));
    Assertions.assertEquals(
        SupplierScoreQualityTool.TREND_DEGRADING,
        SupplierScoreQualityTool.computeTrend(new BigDecimal("69.99"), new BigDecimal("70"), null));
  }

  @Test
  void testComputeWeightedNonConformityCountDecimalWeights() {
    Map<Integer, Long> countByGravity = new HashMap<>();
    countByGravity.put(2, 3L);
    Assertions.assertEquals(
        new BigDecimal("4.5"),
        SupplierScoreQualityTool.computeWeightedNonConformityCount(
            countByGravity, CRITICAL, new BigDecimal("1.5"), MINOR));
  }
}
