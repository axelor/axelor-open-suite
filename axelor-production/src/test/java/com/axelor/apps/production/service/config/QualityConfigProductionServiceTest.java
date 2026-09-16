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
package com.axelor.apps.production.service.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.quality.db.QualityConfig;
import com.axelor.apps.quality.db.repo.QualityConfigRepository;
import com.axelor.apps.quality.service.config.QualityConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QualityConfigProductionServiceTest {

  private QualityConfigProductionService service;
  private Company company;
  private QualityConfig qualityConfig;

  @BeforeEach
  void setUp() {
    service = new QualityConfigProductionService(new QualityConfigService());
    company = new Company();
    company.setName("Company");
    qualityConfig = new QualityConfig();
    qualityConfig.setCompany(company);
  }

  @Test
  void checkSelectIsNoneWithoutCompany() {
    assertEquals(
        QualityConfigRepository.MANUF_ORDER_FINAL_CONTROL_CHECK_NONE,
        service.getManufOrderFinalControlCheckSelect(null));
  }

  @Test
  void checkSelectIsNoneWithoutQualityConfig() {
    assertEquals(
        QualityConfigRepository.MANUF_ORDER_FINAL_CONTROL_CHECK_NONE,
        service.getManufOrderFinalControlCheckSelect(company));
  }

  @Test
  void checkSelectIsNoneWhenTheColumnIsStillEmpty() {
    qualityConfig.setManufOrderFinalControlCheckSelect(null);
    company.setQualityConfig(qualityConfig);

    assertEquals(
        QualityConfigRepository.MANUF_ORDER_FINAL_CONTROL_CHECK_NONE,
        service.getManufOrderFinalControlCheckSelect(company));
  }

  @Test
  void checkSelectComesFromQualityConfig() {
    qualityConfig.setManufOrderFinalControlCheckSelect(
        QualityConfigRepository.MANUF_ORDER_FINAL_CONTROL_CHECK_BLOCKING);
    company.setQualityConfig(qualityConfig);

    assertEquals(
        QualityConfigRepository.MANUF_ORDER_FINAL_CONTROL_CHECK_BLOCKING,
        service.getManufOrderFinalControlCheckSelect(company));
  }

  @Test
  void printTemplateFailsWithoutQualityConfig() {
    AxelorException exception =
        assertThrows(
            AxelorException.class,
            () -> service.getManufOrderConformityDeclarationPrintTemplate(company));

    assertEquals(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, exception.getCategory());
  }

  @Test
  void printTemplateFailsWhenNotConfigured() {
    company.setQualityConfig(qualityConfig);

    AxelorException exception =
        assertThrows(
            AxelorException.class,
            () -> service.getManufOrderConformityDeclarationPrintTemplate(company));

    assertEquals(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, exception.getCategory());
  }

  @Test
  void printTemplateComesFromQualityConfig() throws AxelorException {
    PrintingTemplate printTemplate = new PrintingTemplate();
    qualityConfig.setManufOrderConformityDeclarationPrintTemplate(printTemplate);
    company.setQualityConfig(qualityConfig);

    assertSame(printTemplate, service.getManufOrderConformityDeclarationPrintTemplate(company));
  }
}
