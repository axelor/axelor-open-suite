/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service.config;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.WorkshopSequenceConfigLine;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;

public class ProductionConfigService {

  public ProductionConfig getProductionConfig(Company company) throws AxelorException {

    ProductionConfig productionConfig = company.getProductionConfig();

    if (productionConfig == null) {
      throw new AxelorException(
          company,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PRODUCTION_CONFIG_1),
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
    if (productionConfig.getWorkshopSequenceConfigLineList() != null) {
      sequence =
          productionConfig.getWorkshopSequenceConfigLineList().stream()
              .filter(
                  workshopSequenceConfigLine ->
                      workshopSequenceConfigLine.getWorkshopStockLocation().equals(workshop))
              .map(WorkshopSequenceConfigLine::getSequence)
              .findFirst()
              .orElseGet(productionConfig::getManufOrderSequence);
    }

    if (sequence == null) {
      throw new AxelorException(
          productionConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PRODUCTION_CONFIG_MISSING_MANUF_ORDER_SEQ),
          productionConfig.getCompany().getName());
    }

    return sequence;
  }
}
