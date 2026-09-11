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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.quality.db.QIDetection;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.QIStatus;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.db.repo.QualityImprovementRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.meta.MetaFiles;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QualityImprovementCreateServiceImplTest {

  private QualityImprovementPrefillService prefillService;
  private QIIdentificationService qiIdentificationService;
  private QualityImprovementCreateService service;
  private QIStatus defaultStatus;
  private QIDetection qiDetection;
  private Company company;
  private StockMoveLine stockMoveLine;

  @BeforeEach
  void setUp() throws AxelorException {
    QualityImprovementRepository repository = mock(QualityImprovementRepository.class);
    when(repository.save(any()))
        .thenAnswer(
            invocation -> {
              QualityImprovement qi = invocation.getArgument(0);
              qi.setQiIdentification(new QIIdentification());
              return qi;
            });
    QualityImprovementService qualityImprovementService = mock(QualityImprovementService.class);
    defaultStatus = new QIStatus();
    when(qualityImprovementService.getDefaultQIStatus()).thenReturn(defaultStatus);
    prefillService = mock(QualityImprovementPrefillService.class);
    qiIdentificationService = mock(QIIdentificationService.class);
    service =
        new QualityImprovementCreateServiceImpl(
            repository,
            qualityImprovementService,
            mock(QualityImprovementCheckValuesService.class),
            prefillService,
            qiIdentificationService,
            mock(MetaFiles.class));

    qiDetection = new QIDetection();
    company = new Company();
    StockMove stockMove = new StockMove();
    stockMove.setCompany(company);
    stockMoveLine = new StockMoveLine();
    stockMoveLine.setStockMove(stockMove);
    stockMoveLine.setRealQty(new BigDecimal("7"));
  }

  @Test
  void productFileFromLineIsPrefilledAndFullyNonConforming() throws AxelorException {
    QualityImprovement qi =
        service.createQualityImprovementFromStockMoveLine(
            stockMoveLine, qiDetection, QualityImprovementRepository.TYPE_PRODUCT, true);

    assertSame(company, qi.getCompany());
    assertSame(defaultStatus, qi.getQiStatus());
    assertSame(qiDetection, qi.getQiDetection());
    assertEquals(QualityImprovementRepository.TYPE_PRODUCT, qi.getType());
    assertTrue(qi.getIsAutomaticallyCreated());
    assertEquals(new BigDecimal("7"), qi.getQiIdentification().getNonConformingQuantity());
    verify(prefillService)
        .fillFromStockMoveLine(qi.getQiIdentification(), stockMoveLine, qiDetection);
    verify(qiIdentificationService).updateQIIdentification(qi.getQiIdentification());
  }

  @Test
  void manualSystemFileFromLineKeepsLinksWithoutNonConformingQuantity() throws AxelorException {
    QualityImprovement qi =
        service.createQualityImprovementFromStockMoveLine(
            stockMoveLine, qiDetection, QualityImprovementRepository.TYPE_SYSTEM, false);

    assertFalse(qi.getIsAutomaticallyCreated());
    assertEquals(0, qi.getQiIdentification().getNonConformingQuantity().signum());
    verify(prefillService)
        .fillFromStockMoveLine(qi.getQiIdentification(), stockMoveLine, qiDetection);
  }
}
