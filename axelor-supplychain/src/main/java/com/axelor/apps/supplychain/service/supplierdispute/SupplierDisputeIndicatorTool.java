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
package com.axelor.apps.supplychain.service.supplierdispute;

import com.axelor.apps.supplychain.db.repo.SupplierDisputeRepository;
import com.axelor.apps.supplychain.service.SupplierScoreTool;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

/** Utility class for computing supplier dispute indicators. */
public class SupplierDisputeIndicatorTool {

  private static final BigDecimal SECONDS_PER_DAY =
      BigDecimal.valueOf(Duration.ofDays(1).getSeconds());

  private SupplierDisputeIndicatorTool() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Compute the average number of days between the opening and the closing of disputes. Pairs
   * missing one of the two dates are ignored.
   *
   * @return the average rounded to {@link SupplierScoreTool#RATE_SCALE}, or null when no pair is
   *     taken into account.
   */
  public static BigDecimal computeAverageResolutionDays(
      List<Pair<LocalDateTime, LocalDateTime>> openingClosingPairs) {
    long totalSeconds = 0;
    long count = 0;
    for (Pair<LocalDateTime, LocalDateTime> pair : openingClosingPairs) {
      if (pair.getLeft() == null || pair.getRight() == null) {
        continue;
      }
      totalSeconds += Duration.between(pair.getLeft(), pair.getRight()).getSeconds();
      count++;
    }
    if (count == 0) {
      return null;
    }
    return BigDecimal.valueOf(totalSeconds)
        .divide(
            SECONDS_PER_DAY.multiply(BigDecimal.valueOf(count)),
            SupplierScoreTool.RATE_SCALE,
            RoundingMode.HALF_UP);
  }

  /** A dispute is settled in our favour when it ends with a credit note or a replacement. */
  public static boolean isFavourable(Integer resolutionOutcomeSelect) {
    return resolutionOutcomeSelect != null
        && (resolutionOutcomeSelect == SupplierDisputeRepository.OUTCOME_CREDIT_NOTE
            || resolutionOutcomeSelect == SupplierDisputeRepository.OUTCOME_REPLACEMENT);
  }
}
