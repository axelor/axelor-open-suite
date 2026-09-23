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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestSupplierDisputeIndicatorTool {

  private static final LocalDateTime OPENING = LocalDateTime.of(2026, 3, 10, 9, 0);

  @Test
  void testComputeAverageResolutionDaysNoDispute() {
    Assertions.assertNull(SupplierDisputeIndicatorTool.computeAverageResolutionDays(List.of()));
  }

  @Test
  void testComputeAverageResolutionDaysSingle() {
    Assertions.assertEquals(
        new BigDecimal("2.00"),
        SupplierDisputeIndicatorTool.computeAverageResolutionDays(
            List.of(Pair.of(OPENING, OPENING.plusDays(2)))));
  }

  @Test
  void testComputeAverageResolutionDaysFractional() {
    Assertions.assertEquals(
        new BigDecimal("1.75"),
        SupplierDisputeIndicatorTool.computeAverageResolutionDays(
            List.of(
                Pair.of(OPENING, OPENING.plusDays(1)),
                Pair.of(OPENING, OPENING.plusDays(2).plusHours(12)))));
  }

  @Test
  void testComputeAverageResolutionDaysRounding() {
    Assertions.assertEquals(
        new BigDecimal("0.33"),
        SupplierDisputeIndicatorTool.computeAverageResolutionDays(
            List.of(Pair.of(OPENING, OPENING.plusHours(8)))));
  }

  @Test
  void testComputeAverageResolutionDaysIgnoresIncompletePairs() {
    Assertions.assertEquals(
        new BigDecimal("3.00"),
        SupplierDisputeIndicatorTool.computeAverageResolutionDays(
            List.of(
                Pair.of(OPENING, OPENING.plusDays(3)),
                Pair.of(null, OPENING.plusDays(10)),
                Pair.of(OPENING, null))));
  }

  @Test
  void testIsFavourable() {
    Assertions.assertTrue(
        SupplierDisputeIndicatorTool.isFavourable(SupplierDisputeRepository.OUTCOME_CREDIT_NOTE));
    Assertions.assertTrue(
        SupplierDisputeIndicatorTool.isFavourable(SupplierDisputeRepository.OUTCOME_REPLACEMENT));
  }

  @Test
  void testIsNotFavourable() {
    Assertions.assertFalse(
        SupplierDisputeIndicatorTool.isFavourable(SupplierDisputeRepository.OUTCOME_ACCEPTED));
    Assertions.assertFalse(
        SupplierDisputeIndicatorTool.isFavourable(SupplierDisputeRepository.OUTCOME_REFUSED));
    Assertions.assertFalse(
        SupplierDisputeIndicatorTool.isFavourable(
            SupplierDisputeRepository.OUTCOME_PARTIALLY_ACCEPTED));
    Assertions.assertFalse(SupplierDisputeIndicatorTool.isFavourable(null));
  }
}
