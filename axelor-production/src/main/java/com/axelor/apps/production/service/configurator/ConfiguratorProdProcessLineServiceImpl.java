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
package com.axelor.apps.production.service.configurator;

import com.axelor.apps.production.db.ConfiguratorProdProcessLine;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.sale.service.configurator.ConfiguratorService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.JsonContext;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class ConfiguratorProdProcessLineServiceImpl implements ConfiguratorProdProcessLineService {

  protected ConfiguratorService configuratorService;

  @Inject
  ConfiguratorProdProcessLineServiceImpl(ConfiguratorService configuratorService) {
    this.configuratorService = configuratorService;
  }

  @Override
  public ProdProcessLine generateProdProcessLine(
      ConfiguratorProdProcessLine confProdProcessLine, JsonContext attributes)
      throws AxelorException {
    if (confProdProcessLine == null) {
      return null;
    }
    ProdProcessLine prodProcessLine = new ProdProcessLine();
    BigDecimal minCapacityPerCycle;
    BigDecimal maxCapacityPerCycle;
    long durationPerCycle;

    if (confProdProcessLine.getDefMinCapacityFormula()) {
      minCapacityPerCycle =
          new BigDecimal(
              configuratorService
                  .computeFormula(confProdProcessLine.getMinCapacityPerCycleFormula(), attributes)
                  .toString());
    } else {
      minCapacityPerCycle = confProdProcessLine.getMinCapacityPerCycle();
    }
    if (confProdProcessLine.getDefMaxCapacityFormula()) {
      maxCapacityPerCycle =
          new BigDecimal(
              configuratorService
                  .computeFormula(confProdProcessLine.getMaxCapacityPerCycleFormula(), attributes)
                  .toString());
    } else {
      maxCapacityPerCycle = confProdProcessLine.getMaxCapacityPerCycle();
    }
    if (confProdProcessLine.getDefDurationFormula()) {
      durationPerCycle =
          Long.decode(
              configuratorService
                  .computeFormula(confProdProcessLine.getDurationPerCycleFormula(), attributes)
                  .toString());
    } else {
      durationPerCycle = confProdProcessLine.getDurationPerCycle();
    }

    prodProcessLine.setName(confProdProcessLine.getName());
    prodProcessLine.setPriority(confProdProcessLine.getPriority());
    prodProcessLine.setWorkCenter(confProdProcessLine.getWorkCenter());
    prodProcessLine.setOutsourcing(confProdProcessLine.getOutsourcing());
    prodProcessLine.setStockLocation(confProdProcessLine.getStockLocation());
    prodProcessLine.setDescription(confProdProcessLine.getDescription());

    prodProcessLine.setMinCapacityPerCycle(minCapacityPerCycle);
    prodProcessLine.setMaxCapacityPerCycle(maxCapacityPerCycle);
    prodProcessLine.setDurationPerCycle(durationPerCycle);

    return prodProcessLine;
  }
}
