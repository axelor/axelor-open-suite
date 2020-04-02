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
package com.axelor.apps.production.service;

import com.axelor.apps.production.db.ConfiguratorProdProcess;
import com.axelor.apps.production.db.ConfiguratorProdProcessLine;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.JsonContext;
import com.google.inject.Inject;
import java.util.List;

public class ConfiguratorProdProcessServiceImpl implements ConfiguratorProdProcessService {

  protected ConfiguratorProdProcessLineService confProdProcessLineService;

  @Inject
  ConfiguratorProdProcessServiceImpl(
      ConfiguratorProdProcessLineService confProdProcessLineService) {
    this.confProdProcessLineService = confProdProcessLineService;
  }

  @Override
  public ProdProcess generateProdProcessService(
      ConfiguratorProdProcess confProdProcess, JsonContext attributes) throws AxelorException {
    if (confProdProcess == null) {
      return null;
    }
    List<ConfiguratorProdProcessLine> confLines =
        confProdProcess.getConfiguratorProdProcessLineList();
    ProdProcess prodProcess = new ProdProcess();
    prodProcess.setName(confProdProcess.getName());
    prodProcess.setCompany(confProdProcess.getCompany());
    prodProcess.setStockLocation(confProdProcess.getStockLocation());
    if (confLines != null) {
      for (ConfiguratorProdProcessLine confLine : confLines) {
        prodProcess.addProdProcessLineListItem(
            confProdProcessLineService.generateProdProcessLine(confLine, attributes));
      }
    }
    return prodProcess;
  }
}
