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
import java.math.BigDecimal;
import java.util.Map;

/** Utility class for the quality indicators of the supplier score. */
public class SupplierScoreQualityTool {

  private SupplierScoreQualityTool() {
    throw new IllegalStateException("Utility class");
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
