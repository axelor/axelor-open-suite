/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.sale.service.configurator.ConfiguratorService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.JsonContext;
import com.google.inject.Inject;
import java.util.List;

public class ConfiguratorProdProcessServiceImpl implements ConfiguratorProdProcessService {

  protected ConfiguratorProdProcessLineService confProdProcessLineService;
  protected ConfiguratorService configuratorService;
  protected ProdProcessRepository prodProcessRepository;
  protected StockLocationRepository stockLocationRepository;

  @Inject
  public ConfiguratorProdProcessServiceImpl(
      ConfiguratorProdProcessLineService confProdProcessLineService,
      ConfiguratorService configuratorService,
      ProdProcessRepository prodProcessRepository,
      StockLocationRepository stockLocationRepository) {
    this.confProdProcessLineService = confProdProcessLineService;
    this.configuratorService = configuratorService;
    this.prodProcessRepository = prodProcessRepository;
    this.stockLocationRepository = stockLocationRepository;
  }

  @Override
  public ProdProcess generateProdProcessService(
      ConfiguratorProdProcess confProdProcess, JsonContext attributes, Product product)
      throws AxelorException {
    if (confProdProcess == null) {
      return null;
    }
    String name;
    String code;
    StockLocation stockLocation;
    StockLocation producedProductStockLocation;
    StockLocation workshopStockLocation;
    Boolean isConsProOnOperation;

    if (confProdProcess.getDefNameAsFormula()) {
      Object computedName =
          configuratorService.computeFormula(confProdProcess.getNameFormula(), attributes);
      if (computedName == null) {
        throw new AxelorException(
            confProdProcess,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(IExceptionMessage.CONFIGURATOR_PROD_PROCESS_INCONSISTENT_NAME_FORMULA),
                confProdProcess.getId()));
      } else {
        name = String.valueOf(computedName);
      }
    } else {
      name = confProdProcess.getName();
      if (name == null) {
        throw new AxelorException(
            confProdProcess,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(IExceptionMessage.CONFIGURATOR_PROD_PROCESS_INCONSISTENT_NULL_NAME),
                confProdProcess.getId()));
      }
    }
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
      if (stockLocation != null) {
        // M2O field define by script
        // Explicit repo call needed in order to prevent case where formula is referring to
        // context attribute defined on configurator creator
        // In this case object is not managed and it causes hibernate issues
        stockLocation = stockLocationRepository.find(stockLocation.getId());
      }
    } else {
      stockLocation = confProdProcess.getStockLocation();
    }
    if (confProdProcess.getDefProducedProductStockLocationAsFormula()) {
      producedProductStockLocation =
          (StockLocation)
              configuratorService.computeFormula(
                  confProdProcess.getProducedProductStockLocationFormula(), attributes);
      if (producedProductStockLocation != null) {
        // M2O field define by script
        // Explicit repo call needed in order to prevent case where formula is referring to
        // context attribute defined on configurator creator
        // In this case object is not managed and it causes hibernate issues
        producedProductStockLocation =
            stockLocationRepository.find(producedProductStockLocation.getId());
      }
    } else {
      producedProductStockLocation = confProdProcess.getProducedProductStockLocation();
    }
    if (confProdProcess.getDefWorkshopStockLocationAsFormula()) {
      workshopStockLocation =
          (StockLocation)
              configuratorService.computeFormula(
                  confProdProcess.getWorkshopStockLocationFormula(), attributes);
      if (workshopStockLocation != null) {
        // M2O field define by script
        // Explicit repo call needed in order to prevent case where formula is referring to
        // context attribute defined on configurator creator
        // In this case object is not managed and it causes hibernate issues
        workshopStockLocation = stockLocationRepository.find(workshopStockLocation.getId());
      }
    } else {
      workshopStockLocation = confProdProcess.getWorkshopStockLocation();
    }
    if (confProdProcess.getDefIsConsProOnOperationAsFormula()) {
      Object computedIsConsProOnOperation =
          configuratorService.computeFormula(
              confProdProcess.getIsConsProOnOperationFormula(), attributes);
      if (computedIsConsProOnOperation == null) {
        throw new AxelorException(
            confProdProcess,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(
                    IExceptionMessage
                        .CONFIGURATOR_PROD_PROCESS_INCONSISTENT_IS_CONS_PRO_ON_OPERATION_FORMULA),
                confProdProcess.getId()));
      } else {
        isConsProOnOperation = (Boolean) computedIsConsProOnOperation;
      }
    } else {
      isConsProOnOperation = confProdProcess.getIsConsProOnOperation();
    }

    ProdProcess prodProcess =
        createProdProcessHeader(
            confProdProcess,
            name,
            code,
            stockLocation,
            producedProductStockLocation,
            workshopStockLocation,
            isConsProOnOperation,
            product);

    List<ConfiguratorProdProcessLine> confLines =
        confProdProcess.getConfiguratorProdProcessLineList();
    if (confLines != null) {
      for (ConfiguratorProdProcessLine confLine : confLines) {
        ProdProcessLine generatedProdProcessLine =
            confProdProcessLineService.generateProdProcessLine(
                confLine,
                (isConsProOnOperation != null ? isConsProOnOperation : false),
                attributes);
        if (generatedProdProcessLine != null) {
          prodProcess.addProdProcessLineListItem(generatedProdProcessLine);
        }
      }
    }
    return prodProcess;
  }

  /** Instantiate a new prod process and set the right attributes. */
  protected ProdProcess createProdProcessHeader(
      ConfiguratorProdProcess confProdProcess,
      String name,
      String code,
      StockLocation stockLocation,
      StockLocation producedProductStockLocation,
      StockLocation workshopStockLocation,
      Boolean isConsProOnOperation,
      Product product) {
    ProdProcess prodProcess = new ProdProcess();
    prodProcess.setName(name);
    prodProcess.setCompany(confProdProcess.getCompany());
    prodProcess.setStatusSelect(confProdProcess.getStatusSelect());
    prodProcess.setCode(code);
    prodProcess.setStockLocation(stockLocation);
    prodProcess.setProducedProductStockLocation(producedProductStockLocation);
    prodProcess.setWorkshopStockLocation(workshopStockLocation);
    prodProcess.setIsConsProOnOperation(isConsProOnOperation);
    prodProcess.setProduct(product);
    return prodProcessRepository.save(prodProcess);
  }
}
