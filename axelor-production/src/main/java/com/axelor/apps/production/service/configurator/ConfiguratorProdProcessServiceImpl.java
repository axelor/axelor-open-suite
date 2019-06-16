/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.ConfiguratorProdProcess;
import com.axelor.apps.production.db.ConfiguratorProdProcessLine;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.sale.service.configurator.ConfiguratorService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.JsonContext;
import com.google.inject.Inject;
import java.util.List;

public class ConfiguratorProdProcessServiceImpl implements ConfiguratorProdProcessService {

  protected ConfiguratorProdProcessLineService confProdProcessLineService;
  protected ConfiguratorService configuratorService;
  protected ProdProcessRepository prodProcessRepository;

  @Inject
  ConfiguratorProdProcessServiceImpl(
      ConfiguratorProdProcessLineService confProdProcessLineService,
      ConfiguratorService configuratorService,
      ProdProcessRepository prodProcessRepository) {
    this.confProdProcessLineService = confProdProcessLineService;
    this.configuratorService = configuratorService;
    this.prodProcessRepository = prodProcessRepository;
  }

  @Override
  public ProdProcess generateProdProcessService(
      ConfiguratorProdProcess confProdProcess, JsonContext attributes, Product product)
      throws AxelorException {
    if (confProdProcess == null) {
      return null;
    }
    String code;
    StockLocation stockLocation;
    StockLocation producedProductStockLocation;
    StockLocation workshopStockLocation;

    if (confProdProcess.getDefCodeAsFormula()) {
      code =
          String.valueOf(
              configuratorService.computeFormula(confProdProcess.getCodeFormula(), attributes));
    } else {
      code = confProdProcess.getCode();
    }
    if (confProdProcess.getDefStockLocationAsFormula()) {
      stockLocation =
          (StockLocation)
              configuratorService.computeFormula(
                  confProdProcess.getStockLocationFormula(), attributes);
    } else {
      stockLocation = confProdProcess.getStockLocation();
    }
    if (confProdProcess.getDefProducedProductStockLocationAsFormula()) {
      producedProductStockLocation =
          (StockLocation)
              configuratorService.computeFormula(
                  confProdProcess.getProducedProductStockLocationFormula(), attributes);
    } else {
      producedProductStockLocation = confProdProcess.getProducedProductStockLocation();
    }
    if (confProdProcess.getDefWorkshopStockLocationAsFormula()) {
      workshopStockLocation =
          (StockLocation)
              configuratorService.computeFormula(
                  confProdProcess.getWorkshopStockLocationFormula(), attributes);
    } else {
      workshopStockLocation = confProdProcess.getWorkshopStockLocation();
    }

    ProdProcess prodProcess =
        createProdProcessHeader(
            confProdProcess,
            code,
            stockLocation,
            producedProductStockLocation,
            workshopStockLocation,
            product);

    List<ConfiguratorProdProcessLine> confLines =
        confProdProcess.getConfiguratorProdProcessLineList();
    if (confLines != null) {
      for (ConfiguratorProdProcessLine confLine : confLines) {
        prodProcess.addProdProcessLineListItem(
            confProdProcessLineService.generateProdProcessLine(confLine, attributes));
      }
    }
    return prodProcess;
  }

  /** Instantiate a new prod process and set the right attributes. */
  protected ProdProcess createProdProcessHeader(
      ConfiguratorProdProcess confProdProcess,
      String code,
      StockLocation stockLocation,
      StockLocation producedProductStockLocation,
      StockLocation workshopStockLocation,
      Product product) {
    ProdProcess prodProcess = new ProdProcess();
    prodProcess.setName(confProdProcess.getName());
    prodProcess.setCompany(confProdProcess.getCompany());
    prodProcess.setStatusSelect(confProdProcess.getStatusSelect());
    prodProcess.setCode(code);
    prodProcess.setStockLocation(stockLocation);
    prodProcess.setProducedProductStockLocation(producedProductStockLocation);
    prodProcess.setWorkshopStockLocation(workshopStockLocation);
    prodProcess.setProduct(product);
    return prodProcessRepository.save(prodProcess);
  }
}
