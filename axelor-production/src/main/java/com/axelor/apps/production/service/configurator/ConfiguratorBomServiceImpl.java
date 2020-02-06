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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.UnitRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ConfiguratorBOM;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.ConfiguratorBOMRepository;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.sale.service.configurator.ConfiguratorService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.JsonContext;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class ConfiguratorBomServiceImpl implements ConfiguratorBomService {

  private static final int MAX_LEVEL = 10;

  protected ConfiguratorBOMRepository configuratorBOMRepo;
  protected ConfiguratorService configuratorService;
  protected BillOfMaterialRepository billOfMaterialRepository;
  protected ConfiguratorProdProcessService confProdProcessService;

  @Inject
  ConfiguratorBomServiceImpl(
      ConfiguratorBOMRepository configuratorBOMRepo,
      ConfiguratorService configuratorService,
      BillOfMaterialRepository billOfMaterialRepository,
      ConfiguratorProdProcessService confProdProcessService) {
    this.configuratorBOMRepo = configuratorBOMRepo;
    this.configuratorService = configuratorService;
    this.billOfMaterialRepository = billOfMaterialRepository;
    this.confProdProcessService = confProdProcessService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public BillOfMaterial generateBillOfMaterial(
      ConfiguratorBOM configuratorBOM, JsonContext attributes, int level, Product generatedProduct)
      throws AxelorException {
    level++;
    if (level > MAX_LEVEL) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.CONFIGURATOR_BOM_TOO_MANY_CALLS));
    }
    String name;
    Product product;
    BigDecimal qty;
    Unit unit;
    ProdProcess prodProcess;

    if (configuratorBOM.getDefNameAsFormula()) {
      name =
          (String) configuratorService.computeFormula(configuratorBOM.getNameFormula(), attributes);
    } else {
      name = configuratorBOM.getName();
    }
    if (configuratorBOM.getDefProductFromConfigurator()) {
      if (generatedProduct == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.CONFIGURATOR_BOM_IMPORT_GENERATED_PRODUCT_NULL));
      }
      product = generatedProduct;
    } else if (configuratorBOM.getDefProductAsFormula()) {
      product =
          (Product)
              configuratorService.computeFormula(configuratorBOM.getProductFormula(), attributes);
      if (product == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.CONFIGURATOR_BOM_IMPORT_FORMULA_PRODUCT_NULL));
      }
      product = Beans.get(ProductRepository.class).find(product.getId());
    } else {
      if (configuratorBOM.getProduct() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.CONFIGURATOR_BOM_IMPORT_FILLED_PRODUCT_NULL));
      }
      product = configuratorBOM.getProduct();
    }
    if (configuratorBOM.getDefQtyAsFormula()) {
      qty =
          new BigDecimal(
              configuratorService
                  .computeFormula(configuratorBOM.getQtyFormula(), attributes)
                  .toString());
    } else {
      qty = configuratorBOM.getQty();
    }
    if (configuratorBOM.getDefUnitAsFormula()) {
      unit =
          (Unit) configuratorService.computeFormula(configuratorBOM.getUnitFormula(), attributes);
      if (unit != null) {
        unit = Beans.get(UnitRepository.class).find(unit.getId());
      }
    } else {
      unit = configuratorBOM.getUnit();
    }
    if (configuratorBOM.getDefProdProcessAsFormula()) {
      prodProcess =
          (ProdProcess)
              configuratorService.computeFormula(
                  configuratorBOM.getProdProcessFormula(), attributes);
      if (prodProcess != null) {
        prodProcess = Beans.get(ProdProcessRepository.class).find(prodProcess.getId());
      }
    } else if (configuratorBOM.getDefProdProcessAsConfigurator()) {
      prodProcess =
          confProdProcessService.generateProdProcessService(
              configuratorBOM.getConfiguratorProdProcess(), attributes, product);
    } else {
      prodProcess = configuratorBOM.getProdProcess();
    }

    BillOfMaterial billOfMaterial = new BillOfMaterial();
    billOfMaterial.setCompany(configuratorBOM.getCompany());
    billOfMaterial.setName(name);
    billOfMaterial.setProduct(product);
    billOfMaterial.setQty(qty);
    billOfMaterial.setUnit(unit);
    billOfMaterial.setProdProcess(prodProcess);
    billOfMaterial.setStatusSelect(configuratorBOM.getStatusSelect());
    billOfMaterial.setDefineSubBillOfMaterial(configuratorBOM.getDefineSubBillOfMaterial());

    if (configuratorBOM.getConfiguratorBomList() != null) {
      for (ConfiguratorBOM confBomChild : configuratorBOM.getConfiguratorBomList()) {
        BillOfMaterial childBom =
            generateBillOfMaterial(confBomChild, attributes, level, generatedProduct);
        billOfMaterial.addBillOfMaterialSetItem(childBom);
      }
    }

    billOfMaterial = billOfMaterialRepository.save(billOfMaterial);
    configuratorBOM.setBillOfMaterialId(billOfMaterial.getId());
    configuratorBOMRepo.save(configuratorBOM);
    return billOfMaterial;
  }
}
