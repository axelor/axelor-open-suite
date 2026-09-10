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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.quality.db.QualityConfig;
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
  private NonCompliantReceptionService service;
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
    service =
        new NonCompliantReceptionServiceImpl(
            appQualityService,
            new QuarantineStockLocationServiceImpl(new QualityConfigService()),
            stockMoveLineService,
            stockMoveLineRepository);

    company = new Company();
    company.setName("Company");
    destination = new StockLocation();
    destination.setName("Main warehouse");
    companyQuarantine = new StockLocation();
    companyQuarantine.setName("Quarantine");
    QualityConfig qualityConfig = new QualityConfig();
    qualityConfig.setCompany(company);
    qualityConfig.setQuarantineStockLocation(companyQuarantine);
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
