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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.QIAnalysis;
import com.axelor.apps.quality.db.QIAnalysisCause;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QIResolutionDefault;
import com.axelor.apps.quality.db.QIStatus;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.db.repo.QualityImprovementRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.db.Query;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReceptionQualityImprovementCancelServiceImplTest {

  private static final long STOCK_MOVE_ID = 9L;

  private QualityImprovementRepository repository;
  private ReceptionQualityImprovementCancelService service;
  private QIStatus newStatus;
  private QIStatus inProgressStatus;
  private QIStatus cancelledStatus;
  private StockMove stockMove;

  @BeforeEach
  void setUp() throws AxelorException {
    repository = mock(QualityImprovementRepository.class);
    QualityImprovementService qualityImprovementService = mock(QualityImprovementService.class);
    newStatus = status("New");
    inProgressStatus = status("In progress");
    cancelledStatus = status("Cancelled");
    when(qualityImprovementService.getDefaultQIStatus()).thenReturn(newStatus);
    when(qualityImprovementService.getCancelledQIStatus()).thenReturn(cancelledStatus);
    service =
        new ReceptionQualityImprovementCancelServiceImpl(repository, qualityImprovementService);
    stockMove = new StockMove();
    stockMove.setId(STOCK_MOVE_ID);
  }

  @Test
  void untouchedAutomaticFileIsCancelled() throws AxelorException {
    QualityImprovement untouched = automaticFile("QI1", newStatus);
    openAutomaticFiles(untouched);

    service.cancelUntouchedQualityImprovements(stockMove);

    assertSame(cancelledStatus, untouched.getQiStatus());
    verify(repository).save(untouched);
  }

  @Test
  void workedOnFilesStayOpen() throws AxelorException {
    QualityImprovement statusChanged = automaticFile("QI1", inProgressStatus);
    QualityImprovement causeAdded = automaticFile("QI2", newStatus);
    causeAdded.getQiAnalysis().addQiAnalysisCausesListItem(new QIAnalysisCause());
    QualityImprovement defectAdded = automaticFile("QI3", newStatus);
    defectAdded.getQiResolution().addQiResolutionDefaultsListItem(new QIResolutionDefault());
    QualityImprovement objectiveWritten = automaticFile("QI4", newStatus);
    objectiveWritten.getQiAnalysis().setObjective("Check the supplier lot");
    openAutomaticFiles(statusChanged, causeAdded, defectAdded, objectiveWritten);

    service.cancelUntouchedQualityImprovements(stockMove);

    assertSame(inProgressStatus, statusChanged.getQiStatus());
    assertSame(newStatus, causeAdded.getQiStatus());
    assertSame(newStatus, defectAdded.getQiStatus());
    assertSame(newStatus, objectiveWritten.getQiStatus());
    verify(repository, never()).save(any());
  }

  @Test
  void warningNamesCancelledAndKeptFiles() throws AxelorException {
    QualityImprovement untouched = automaticFile("QI1", newStatus);
    QualityImprovement workedOn = automaticFile("QI2", inProgressStatus);
    openAutomaticFiles(untouched, workedOn);

    String warning = service.getCancellationWarning(stockMove);

    assertTrue(warning.contains("QI1"));
    assertTrue(warning.contains("QI2"));
  }

  @Test
  void warningIsNullWithoutAutomaticFiles() throws AxelorException {
    openAutomaticFiles();

    assertNull(service.getCancellationWarning(stockMove));
    assertNull(service.getCancellationWarning(new StockMove()));
  }

  @Test
  void untouchedRuleIgnoresManualFilesBecauseTheyAreNeverFetched() {
    QualityImprovement manual = automaticFile("QI1", newStatus);
    manual.setIsAutomaticallyCreated(false);

    assertTrue(service.isUntouched(manual, newStatus));
    assertFalse(service.isUntouched(manual, inProgressStatus));
    assertEquals(List.of(), service.findOpenAutomaticQualityImprovements(new StockMove()));
  }

  private void openAutomaticFiles(QualityImprovement... files) {
    Query<QualityImprovement> query = mock(Query.class);
    when(repository.findOpenAutomaticByStockMoveId(STOCK_MOVE_ID)).thenReturn(query);
    when(query.fetch()).thenReturn(List.of(files));
  }

  private QualityImprovement automaticFile(String sequence, QIStatus qiStatus) {
    QualityImprovement qi = new QualityImprovement();
    qi.setSequence(sequence);
    qi.setQiStatus(qiStatus);
    qi.setIsAutomaticallyCreated(true);
    qi.setQiAnalysis(new QIAnalysis());
    qi.setQiResolution(new QIResolution());
    return qi;
  }

  private QIStatus status(String name) {
    QIStatus status = new QIStatus();
    status.setName(name);
    return status;
  }
}
