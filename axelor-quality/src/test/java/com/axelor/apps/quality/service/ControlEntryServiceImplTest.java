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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.apps.quality.db.ControlEntrySample;
import com.axelor.apps.quality.db.repo.ControlEntryRepository;
import com.axelor.apps.quality.db.repo.ControlEntrySampleRepository;
import com.axelor.apps.quality.db.repo.ControlPlanRepository;
import com.axelor.apps.quality.db.repo.QualityImprovementRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControlEntryServiceImplTest {

  private static final long STOCK_MOVE_LINE_ID = 1L;

  private StockMoveLineRepository stockMoveLineRepository;
  private ControlEntryService service;
  private StockMoveLine stockMoveLine;
  private StockMove stockMove;

  @BeforeEach
  void setUp() {
    stockMoveLineRepository = mock(StockMoveLineRepository.class);
    service =
        new ControlEntryServiceImpl(
            mock(ControlEntrySampleService.class),
            mock(ControlPlanRepository.class),
            mock(ControlEntryRepository.class),
            mock(QualityImprovementRepository.class),
            stockMoveLineRepository);
    stockMove = new StockMove();
    stockMove.setStatusSelect(StockMoveRepository.STATUS_PLANNED);
    stockMoveLine = new StockMoveLine();
    stockMoveLine.setStockMove(stockMove);
    when(stockMoveLineRepository.find(STOCK_MOVE_LINE_ID)).thenReturn(stockMoveLine);
  }

  @Test
  void finishSetsStatusAndNonCompliantLineWhenASampleFails() {
    ControlEntry controlEntry =
        controlEntryOnLine(
            ControlEntrySampleRepository.RESULT_COMPLIANT,
            ControlEntrySampleRepository.RESULT_NOT_COMPLIANT);

    service.finish(controlEntry);

    assertEquals(ControlEntryRepository.FINISHED_STATUS, controlEntry.getStatusSelect());
    assertEquals(
        StockMoveLineRepository.CONFORMITY_NON_COMPLIANT, stockMoveLine.getConformitySelect());
    verify(stockMoveLineRepository).save(stockMoveLine);
  }

  @Test
  void finishSetsCompliantLineWhenAllSamplesPassAndLineUnassessed() {
    ControlEntry controlEntry =
        controlEntryOnLine(
            ControlEntrySampleRepository.RESULT_COMPLIANT,
            ControlEntrySampleRepository.RESULT_COMPLIANT);
    stockMoveLine.setConformitySelect(StockMoveLineRepository.CONFORMITY_NONE);

    service.finish(controlEntry);

    assertEquals(StockMoveLineRepository.CONFORMITY_COMPLIANT, stockMoveLine.getConformitySelect());
  }

  @Test
  void finishKeepsNonCompliantLineWhenAllSamplesPass() {
    ControlEntry controlEntry = controlEntryOnLine(ControlEntrySampleRepository.RESULT_COMPLIANT);
    stockMoveLine.setConformitySelect(StockMoveLineRepository.CONFORMITY_NON_COMPLIANT);

    service.finish(controlEntry);

    assertEquals(
        StockMoveLineRepository.CONFORMITY_NON_COMPLIANT, stockMoveLine.getConformitySelect());
    verify(stockMoveLineRepository, never()).save(any());
  }

  @Test
  void finishLeavesLineUntouchedWhenASampleIsNotControlled() {
    ControlEntry controlEntry =
        controlEntryOnLine(
            ControlEntrySampleRepository.RESULT_COMPLIANT,
            ControlEntrySampleRepository.RESULT_NOT_CONTROLLED);

    service.finish(controlEntry);

    assertEquals(0, stockMoveLine.getConformitySelect());
    verify(stockMoveLineRepository, never()).save(any());
  }

  @Test
  void finishUpdatesLineOfRealizedMove() {
    stockMove.setStatusSelect(StockMoveRepository.STATUS_REALIZED);
    ControlEntry controlEntry =
        controlEntryOnLine(ControlEntrySampleRepository.RESULT_NOT_COMPLIANT);

    service.finish(controlEntry);

    assertEquals(
        StockMoveLineRepository.CONFORMITY_NON_COMPLIANT, stockMoveLine.getConformitySelect());
  }

  @Test
  void finishSkipsLineOfCanceledMove() {
    stockMove.setStatusSelect(StockMoveRepository.STATUS_CANCELED);
    ControlEntry controlEntry =
        controlEntryOnLine(ControlEntrySampleRepository.RESULT_NOT_COMPLIANT);

    service.finish(controlEntry);

    assertEquals(ControlEntryRepository.FINISHED_STATUS, controlEntry.getStatusSelect());
    assertEquals(0, stockMoveLine.getConformitySelect());
    verify(stockMoveLineRepository, never()).save(any());
  }

  @Test
  void finishOnlyChangesStatusWhenNotRelatedToAStockMoveLine() {
    ControlEntry controlEntry =
        controlEntryOnLine(ControlEntrySampleRepository.RESULT_NOT_COMPLIANT);
    controlEntry.setRelatedToSelect("com.axelor.apps.base.db.Product");

    service.finish(controlEntry);

    assertEquals(ControlEntryRepository.FINISHED_STATUS, controlEntry.getStatusSelect());
    assertEquals(0, stockMoveLine.getConformitySelect());
    verify(stockMoveLineRepository, never()).find(any());
  }

  private ControlEntry controlEntryOnLine(int... sampleResults) {
    ControlEntry controlEntry = new ControlEntry();
    controlEntry.setStatusSelect(ControlEntryRepository.IN_PROGRESS_STATUS);
    controlEntry.setRelatedToSelect(StockMoveLine.class.getName());
    controlEntry.setRelatedToSelectId(STOCK_MOVE_LINE_ID);
    for (int result : sampleResults) {
      ControlEntrySample sample = new ControlEntrySample();
      sample.setResultSelect(result);
      controlEntry.addControlEntrySamplesListItem(sample);
    }
    return controlEntry;
  }
}
