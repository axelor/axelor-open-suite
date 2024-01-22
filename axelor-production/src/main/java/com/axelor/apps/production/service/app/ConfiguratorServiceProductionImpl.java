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
package com.axelor.apps.production.service.app;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ConfiguratorBOM;
import com.axelor.apps.production.service.configurator.ConfiguratorBomService;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.configurator.ConfiguratorFormulaService;
import com.axelor.apps.sale.service.configurator.ConfiguratorMetaJsonFieldService;
import com.axelor.apps.sale.service.configurator.ConfiguratorServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.rpc.JsonContext;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ConfiguratorServiceProductionImpl extends ConfiguratorServiceImpl {

  @Inject
  public ConfiguratorServiceProductionImpl(
      AppBaseService appBaseService,
      ConfiguratorFormulaService configuratorFormulaService,
      ProductRepository productRepository,
      SaleOrderLineService saleOrderLineService,
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderComputeService saleOrderComputeService,
      MetaFieldRepository metaFieldRepository,
      ConfiguratorMetaJsonFieldService configuratorMetaJsonFieldService) {
    super(
        appBaseService,
        configuratorFormulaService,
        productRepository,
        saleOrderLineService,
        saleOrderLineRepository,
        saleOrderComputeService,
        metaFieldRepository,
        configuratorMetaJsonFieldService);
  }

  /**
   * In this implementation, we also create a bill of materials.
   *
   * @param configurator
   * @param jsonAttributes
   * @param jsonIndicators
   * @param saleOrderId
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void generateProduct(
      Configurator configurator,
      JsonContext jsonAttributes,
      JsonContext jsonIndicators,
      Long saleOrderId)
      throws AxelorException {
    super.generateProduct(configurator, jsonAttributes, jsonIndicators, saleOrderId);
    ConfiguratorBOM configuratorBOM = configurator.getConfiguratorCreator().getConfiguratorBom();
    if (configuratorBOM != null) {
      Product generatedProduct = configurator.getProduct();
      Beans.get(ConfiguratorBomService.class)
          .generateBillOfMaterial(configuratorBOM, jsonAttributes, 0, generatedProduct)
          .ifPresent(generatedProduct::setDefaultBillOfMaterial);
    }
  }

  /** In this implementation, we also create a bill of material. */
  @Override
  protected SaleOrderLine generateSaleOrderLine(
      Configurator configurator,
      JsonContext jsonAttributes,
      JsonContext jsonIndicators,
      SaleOrder saleOrder)
      throws AxelorException {

    SaleOrderLine saleOrderLine =
        super.generateSaleOrderLine(configurator, jsonAttributes, jsonIndicators, saleOrder);
    ConfiguratorBOM configuratorBOM = configurator.getConfiguratorCreator().getConfiguratorBom();
    if (configuratorBOM != null) {
      Beans.get(ConfiguratorBomService.class)
          .generateBillOfMaterial(configuratorBOM, jsonAttributes, 0, null)
          .ifPresent(saleOrderLine::setBillOfMaterial);
    }
    return saleOrderLine;
  }

  @Override
  protected void fillSaleOrderWithProduct(SaleOrderLine saleOrderLine) throws AxelorException {
    if (saleOrderLine.getProduct() == null) {
      return;
    }
    super.fillSaleOrderWithProduct(saleOrderLine);
    setProductionInformation(saleOrderLine);
  }

  protected void setProductionInformation(SaleOrderLine saleOrderLine) {
    saleOrderLine.setBillOfMaterial(getDefaultBOM(saleOrderLine));

    if (saleOrderLine.getSaleSupplySelect() == ProductRepository.SALE_SUPPLY_PURCHASE
        || saleOrderLine.getSaleSupplySelect() == ProductRepository.SALE_SUPPLY_PRODUCE) {
      saleOrderLine.setStandardDelay(saleOrderLine.getProduct().getStandardDelay());
    }
  }

  protected BillOfMaterial getDefaultBOM(SaleOrderLine saleOrderLine) {
    Product product = saleOrderLine.getProduct();
    BillOfMaterial defaultBillOfMaterial = null;

    if (saleOrderLine.getSaleSupplySelect() != ProductRepository.SALE_SUPPLY_PRODUCE) {
      return defaultBillOfMaterial;
    }

    if (product.getDefaultBillOfMaterial() != null) {
      defaultBillOfMaterial = product.getDefaultBillOfMaterial();
    } else if (product.getParentProduct() != null) {
      defaultBillOfMaterial = product.getParentProduct().getDefaultBillOfMaterial();
    }

    return defaultBillOfMaterial;
  }
}
