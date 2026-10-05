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

import com.axelor.apps.quality.db.repo.QualityImprovementRepository;
import com.axelor.apps.supplychain.service.SupplierScoreTool;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

/** Utility class for the quality indicators of the supplier score. */
public class SupplierScoreQualityTool {

  public static final int TREND_IMPROVING = 1;
  public static final int TREND_STABLE = 2;
  public static final int TREND_DEGRADING = 3;

  private SupplierScoreQualityTool() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * The quality score is the part of the supplier score fed by the Quality app: the open quality
   * improvement rate and the non-conformity rate, weighted as in the global score.
   *
   * @return the score between 0 and 100, or null when neither rate is available.
   */
  public static BigDecimal computeQualityScore(
      BigDecimal openQiRate,
      BigDecimal qiWeight,
      BigDecimal nonConformityRate,
      BigDecimal ncWeight) {
    return SupplierScoreTool.computeWeightedAverage(
        List.of(Pair.of(openQiRate, qiWeight), Pair.of(nonConformityRate, ncWeight)));
  }

  /**
   * Compare the current quality score with a past one.
   *
   * @return {@link #TREND_STABLE} when the variation stays within the tolerance, {@link
   *     #TREND_IMPROVING} or {@link #TREND_DEGRADING} otherwise, null when one of the scores is
   *     missing.
   */
  public static Integer computeTrend(
      BigDecimal currentScore, BigDecimal referenceScore, BigDecimal tolerance) {
    if (currentScore == null || referenceScore == null) {
      return null;
    }
    BigDecimal delta = currentScore.subtract(referenceScore);
    BigDecimal bound = tolerance == null ? BigDecimal.ZERO : tolerance.abs();
    if (delta.abs().compareTo(bound) <= 0) {
      return TREND_STABLE;
    }
    return delta.signum() > 0 ? TREND_IMPROVING : TREND_DEGRADING;
  }

  /**
   * Weight the number of quality improvements of each gravity, so that a critical one counts for
   * more non-conformities than a minor one.
   *
   * @param countByGravity number of quality improvements per {@code gravityTypeSelect}, the null
   *     key holding the ungraded ones.
   * @return the gravity-weighted number of non-conformities, 0 when there is none.
   */
  public static BigDecimal computeWeightedNonConformityCount(
      Map<Integer, Long> countByGravity,
      BigDecimal criticalWeight,
      BigDecimal majorWeight,
      BigDecimal minorWeight) {
    BigDecimal weightedCount = BigDecimal.ZERO;
    for (Map.Entry<Integer, Long> countEntry : countByGravity.entrySet()) {
      Long count = countEntry.getValue();
      if (count == null || count <= 0) {
        continue;
      }
      weightedCount =
          weightedCount.add(
              getGravityWeight(countEntry.getKey(), criticalWeight, majorWeight, minorWeight)
                  .multiply(BigDecimal.valueOf(count)));
    }
    return weightedCount;
  }

  /**
   * A quality improvement without gravity counts as major, so that leaving a file ungraded is never
   * cheaper than grading it.
   *
   * @return the weight of the gravity, never null nor negative.
   */
  public static BigDecimal getGravityWeight(
      Integer gravityTypeSelect,
      BigDecimal criticalWeight,
      BigDecimal majorWeight,
      BigDecimal minorWeight) {
    BigDecimal weight = majorWeight;
    if (gravityTypeSelect != null) {
      if (gravityTypeSelect == QualityImprovementRepository.GRAVITY_TYPE_SELECT_CRITICAL) {
        weight = criticalWeight;
      } else if (gravityTypeSelect == QualityImprovementRepository.GRAVITY_TYPE_SELECT_MINOR) {
        weight = minorWeight;
      }
    }
    return weight == null ? BigDecimal.ZERO : weight.max(BigDecimal.ZERO);
  }
}
