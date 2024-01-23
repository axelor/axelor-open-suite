/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.WorkshopSequenceConfigLine;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class ProductionConfigService {

  public ProductionConfig getProductionConfig(Company company) throws AxelorException {

    ProductionConfig productionConfig = company.getProductionConfig();

    if (productionConfig == null) {
      throw new AxelorException(
          company,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.PRODUCTION_CONFIG_1),
          company.getName());
    }

    return productionConfig;
  }

  /**
   * Find the configured sequence for a manufacturing order.
   *
   * <p>A company can have a different sequence per workshop. If no sequence is found per workshop,
   * then return {@link ProductionConfig#manufOrderSequence}.
   *
   * @param productionConfig the config corresponding to the company.
   * @param workshop the workshop of the manufacturing order.
   * @return the found sequence.
   * @throws AxelorException if no sequence is found for the given workshop, and if no default
   *     sequence is filled.
   */
  public Sequence getManufOrderSequence(ProductionConfig productionConfig, StockLocation workshop)
      throws AxelorException {
    Sequence sequence = null;
    AppProductionService appProductionService = Beans.get(AppProductionService.class);

    if (appProductionService.isApp("production")) {

      if (productionConfig.getWorkshopSequenceConfigLineList() != null
          && appProductionService.getAppProduction().getManageWorkshop()) {
        sequence =
            productionConfig.getWorkshopSequenceConfigLineList().stream()
                .filter(
                    workshopSequenceConfigLine ->
                        workshopSequenceConfigLine.getWorkshopStockLocation().equals(workshop))
                .map(WorkshopSequenceConfigLine::getSequence)
                .findFirst()
                .orElseGet(productionConfig::getManufOrderSequence);
      } else {
        sequence = productionConfig.getManufOrderSequence();
      }
    }
    if (sequence == null) {
      throw new AxelorException(
          productionConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.PRODUCTION_CONFIG_MISSING_MANUF_ORDER_SEQ),
          productionConfig.getCompany().getName());
    }

    return sequence;
  }
}
