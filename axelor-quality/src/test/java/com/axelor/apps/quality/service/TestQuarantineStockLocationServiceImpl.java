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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.quality.db.QualityConfig;
import com.axelor.apps.quality.service.config.QualityConfigService;
import com.axelor.apps.stock.db.StockLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestQuarantineStockLocationServiceImpl {

  private QuarantineStockLocationService service;
  private Company company;
  private StockLocation destination;
  private StockLocation companyQuarantine;
  private StockLocation destinationQuarantine;

  @BeforeEach
  void setUp() {
    service = new QuarantineStockLocationServiceImpl(new QualityConfigService());
    company = new Company();
    company.setName("Company");
    destination = new StockLocation();
    destination.setName("Main warehouse");
    companyQuarantine = new StockLocation();
    companyQuarantine.setName("Company quarantine");
    destinationQuarantine = new StockLocation();
    destinationQuarantine.setName("Warehouse quarantine");
  }

  @Test
  void destinationOverrideWins() throws AxelorException {
    destination.setQuarantineStockLocation(destinationQuarantine);
    company.setQualityConfig(qualityConfigWith(companyQuarantine));

    assertSame(destinationQuarantine, service.getQuarantineStockLocation(destination, company));
  }

  @Test
  void fallsBackToCompanyQualityConfig() throws AxelorException {
    company.setQualityConfig(qualityConfigWith(companyQuarantine));

    assertSame(companyQuarantine, service.getQuarantineStockLocation(destination, company));
    assertSame(companyQuarantine, service.getQuarantineStockLocation(null, company));
  }

  @Test
  void missingCompanyQuarantineIsConfigurationError() {
    company.setQualityConfig(qualityConfigWith(null));

    AxelorException e =
        assertThrows(
            AxelorException.class, () -> service.getQuarantineStockLocation(destination, company));
    assertEquals(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getCategory());
  }

  @Test
  void missingQualityConfigIsConfigurationError() {
    AxelorException e =
        assertThrows(
            AxelorException.class, () -> service.getQuarantineStockLocation(destination, company));
    assertEquals(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getCategory());
  }

  private QualityConfig qualityConfigWith(StockLocation quarantine) {
    QualityConfig qualityConfig = new QualityConfig();
    qualityConfig.setCompany(company);
    qualityConfig.setQuarantineStockLocation(quarantine);
    return qualityConfig;
  }
}
