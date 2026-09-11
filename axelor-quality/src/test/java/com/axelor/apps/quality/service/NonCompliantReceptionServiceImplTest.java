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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.quality.db.QIDetection;
import com.axelor.apps.quality.db.QualityConfig;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.db.repo.QualityImprovementRepository;
import com.axelor.apps.quality.service.app.AppQualityService;
import com.axelor.apps.quality.service.config.QualityConfigService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.studio.db.AppQuality;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NonCompliantReceptionServiceImplTest {

  private AppQuality appQuality;
  private StockMoveLineService stockMoveLineService;
  private StockMoveLineRepository stockMoveLineRepository;
  private QualityImprovementCreateService qualityImprovementCreateService;
  private StockMoveLineQualityService stockMoveLineQualityService;
  private NonCompliantReceptionService service;
  private QIDetection receptionQiDetection;
  private Company company;
  private StockLocation destination;
  private StockLocation companyQuarantine;
  private StockMove stockMove;

  @BeforeEach
  void setUp() {
    appQuality = new AppQuality();
    appQuality.setRedirectNonCompliantReceptionToQuarantine(true);
    AppQualityService appQualityService = mock(AppQualityService.class);
    when(appQualityService.getAppQuality()).thenReturn(appQuality);
    stockMoveLineService = mock(StockMoveLineService.class);
    stockMoveLineRepository = mock(StockMoveLineRepository.class);
    qualityImprovementCreateService = mock(QualityImprovementCreateService.class);
    stockMoveLineQualityService = mock(StockMoveLineQualityService.class);
    when(stockMoveLineQualityService.getOpenQualityImprovementSequences(any()))
        .thenReturn(List.of());
    QualityConfigService qualityConfigService = new QualityConfigService();
    service =
        new NonCompliantReceptionServiceImpl(
            appQualityService,
            new QuarantineStockLocationServiceImpl(qualityConfigService),
            stockMoveLineService,
            stockMoveLineRepository,
            qualityConfigService,
            qualityImprovementCreateService,
            stockMoveLineQualityService);

    company = new Company();
    company.setName("Company");
    destination = new StockLocation();
    destination.setName("Main warehouse");
    companyQuarantine = new StockLocation();
    companyQuarantine.setName("Quarantine");
    QualityConfig qualityConfig = new QualityConfig();
    qualityConfig.setCompany(company);
    qualityConfig.setQuarantineStockLocation(companyQuarantine);
    receptionQiDetection = new QIDetection();
    qualityConfig.setReceptionQiDetection(receptionQiDetection);
    company.setQualityConfig(qualityConfig);

    stockMove = new StockMove();
    stockMove.setCompany(company);
    stockMove.setTypeSelect(StockMoveRepository.TYPE_INCOMING);
    stockMove.setPartner(new Partner());
  }

  @Test
  void fullyReceivedNonCompliantLineGoesToCompanyQuarantine() throws AxelorException {
    StockMoveLine line = line(StockMoveLineRepository.CONFORMITY_NON_COMPLIANT, "10", "10");

    List<StockMoveLine> redirected = service.redirectToQuarantine(stockMove);

    assertEquals(List.of(line), redirected);
    assertSame(companyQuarantine, line.getToStockLocation());
    verify(stockMoveLineService, never()).splitIntoFulfilledMoveLineAndUnfulfilledOne(any());
    verify(stockMoveLineRepository).save(line);
  }

  @Test
  void partiallyReceivedNonCompliantLineIsSplitBeforeRedirect() throws AxelorException {
    StockMoveLine line = line(StockMoveLineRepository.CONFORMITY_NON_COMPLIANT, "10", "4");

    service.redirectToQuarantine(stockMove);

    verify(stockMoveLineService).splitIntoFulfilledMoveLineAndUnfulfilledOne(line);
    assertSame(companyQuarantine, line.getToStockLocation());
  }

  @Test
  void destinationQuarantineOverridesCompanyQuarantine() throws AxelorException {
    StockLocation destinationQuarantine = new StockLocation();
    destination.setQuarantineStockLocation(destinationQuarantine);
    StockMoveLine line = line(StockMoveLineRepository.CONFORMITY_NON_COMPLIANT, "10", "10");

    service.redirectToQuarantine(stockMove);

    assertSame(destinationQuarantine, line.getToStockLocation());
  }

  @Test
  void compliantAndUnassessedLinesAreLeftAlone() throws AxelorException {
    StockMoveLine compliant = line(StockMoveLineRepository.CONFORMITY_COMPLIANT, "10", "10");
    StockMoveLine unassessed = line(StockMoveLineRepository.CONFORMITY_NONE, "10", "10");

    assertTrue(service.redirectToQuarantine(stockMove).isEmpty());
    assertSame(destination, compliant.getToStockLocation());
    assertSame(destination, unassessed.getToStockLocation());
    verify(stockMoveLineRepository, never()).save(any());
  }

  @Test
  void optionOffLeavesNonCompliantLinesAlone() throws AxelorException {
    appQuality.setRedirectNonCompliantReceptionToQuarantine(false);
    StockMoveLine line = line(StockMoveLineRepository.CONFORMITY_NON_COMPLIANT, "10", "10");

    assertTrue(service.redirectToQuarantine(stockMove).isEmpty());
    assertSame(destination, line.getToStockLocation());
    assertFalse(service.isRedirectedToQuarantine(line));
  }

  @Test
  void customerReturnIsNotARedirectCandidate() throws AxelorException {
    stockMove.setIsReversion(true);
    StockMoveLine line = line(StockMoveLineRepository.CONFORMITY_NON_COMPLIANT, "10", "10");

    assertFalse(service.isSupplierReception(stockMove));
    assertTrue(service.redirectToQuarantine(stockMove).isEmpty());
    assertSame(destination, line.getToStockLocation());
  }

  @Test
  void missingQuarantineBlocksBeforeAnyLineIsModified() throws AxelorException {
    company.getQualityConfig().setQuarantineStockLocation(null);
    StockMoveLine first = line(StockMoveLineRepository.CONFORMITY_NON_COMPLIANT, "10", "4");
    StockMoveLine second = line(StockMoveLineRepository.CONFORMITY_NON_COMPLIANT, "5", "5");

    assertThrows(AxelorException.class, () -> service.redirectToQuarantine(stockMove));

    assertSame(destination, first.getToStockLocation());
    assertSame(destination, second.getToStockLocation());
    verify(stockMoveLineService, never()).splitIntoFulfilledMoveLineAndUnfulfilledOne(any());
    verify(stockMoveLineRepository, never()).save(any());
  }

  @Test
  void nonCompliantLineOfReceptionIsRedirectedToQuarantineForReservation() {
    StockMoveLine line = line(StockMoveLineRepository.CONFORMITY_NON_COMPLIANT, "10", "10");
    StockMoveLine compliant = line(StockMoveLineRepository.CONFORMITY_COMPLIANT, "10", "10");

    assertTrue(service.isRedirectedToQuarantine(line));
    assertFalse(service.isRedirectedToQuarantine(compliant));
  }

  @Test
  void automaticFileIsCreatedForEachNonCompliantLine() throws AxelorException {
    appQuality.setCreateQiOnNonCompliantReception(true);
    StockMoveLine first = line(StockMoveLineRepository.CONFORMITY_NON_COMPLIANT, "10", "10");
    StockMoveLine second = line(StockMoveLineRepository.CONFORMITY_NON_COMPLIANT, "5", "5");
    line(StockMoveLineRepository.CONFORMITY_COMPLIANT, "5", "5");
    when(qualityImprovementCreateService.createQualityImprovementFromStockMoveLine(
            any(), any(), anyInt(), anyBoolean()))
        .thenReturn(new QualityImprovement());

    service.checkAutomaticQualityImprovementPrerequisites(stockMove);
    List<QualityImprovement> created = service.createAutomaticQualityImprovements(stockMove);

    assertEquals(2, created.size());
    verify(qualityImprovementCreateService)
        .createQualityImprovementFromStockMoveLine(
            first, receptionQiDetection, QualityImprovementRepository.TYPE_PRODUCT, true);
    verify(qualityImprovementCreateService)
        .createQualityImprovementFromStockMoveLine(
            second, receptionQiDetection, QualityImprovementRepository.TYPE_PRODUCT, true);
  }

  @Test
  void lineWithAnOpenFileNeverGetsASecondAutomaticOne() throws AxelorException {
    appQuality.setCreateQiOnNonCompliantReception(true);
    StockMoveLine line = line(StockMoveLineRepository.CONFORMITY_NON_COMPLIANT, "10", "10");
    when(stockMoveLineQualityService.getOpenQualityImprovementSequences(line))
        .thenReturn(List.of("QI26007"));

    assertTrue(service.createAutomaticQualityImprovements(stockMove).isEmpty());
    verify(qualityImprovementCreateService, never())
        .createQualityImprovementFromStockMoveLine(any(), any(), anyInt(), anyBoolean());
  }

  @Test
  void automaticFileOptionOffCreatesNothing() throws AxelorException {
    line(StockMoveLineRepository.CONFORMITY_NON_COMPLIANT, "10", "10");
    company.getQualityConfig().setReceptionQiDetection(null);

    service.checkAutomaticQualityImprovementPrerequisites(stockMove);
    assertTrue(service.createAutomaticQualityImprovements(stockMove).isEmpty());
    verify(qualityImprovementCreateService, never())
        .createQualityImprovementFromStockMoveLine(any(), any(), anyInt(), anyBoolean());
  }

  @Test
  void missingReceptionDetectionBlocksOnlyWhenANonCompliantLineExists() throws AxelorException {
    appQuality.setCreateQiOnNonCompliantReception(true);
    company.getQualityConfig().setReceptionQiDetection(null);
    line(StockMoveLineRepository.CONFORMITY_COMPLIANT, "10", "10");

    service.checkAutomaticQualityImprovementPrerequisites(stockMove);

    line(StockMoveLineRepository.CONFORMITY_NON_COMPLIANT, "10", "10");
    assertThrows(
        AxelorException.class,
        () -> service.checkAutomaticQualityImprovementPrerequisites(stockMove));
    verify(qualityImprovementCreateService, never())
        .createQualityImprovementFromStockMoveLine(any(), eq(null), anyInt(), anyBoolean());
  }

  private StockMoveLine line(int conformitySelect, String qty, String realQty) {
    Product product = new Product();
    product.setProductTypeSelect(ProductRepository.PRODUCT_TYPE_STORABLE);
    StockMoveLine line = new StockMoveLine();
    line.setProduct(product);
    line.setConformitySelect(conformitySelect);
    line.setQty(new BigDecimal(qty));
    line.setRealQty(new BigDecimal(realQty));
    line.setToStockLocation(destination);
    stockMove.addStockMoveLineListItem(line);
    return line;
  }
}
