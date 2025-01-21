/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.repo.ProductCompanyRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ConfiguratorBOM;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.service.configurator.ConfiguratorBomService;
import com.axelor.apps.production.service.configurator.ConfiguratorCheckServiceProduction;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.ConfiguratorRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.configurator.ConfiguratorCheckService;
import com.axelor.apps.sale.service.configurator.ConfiguratorFormulaService;
import com.axelor.apps.sale.service.configurator.ConfiguratorMetaJsonFieldService;
import com.axelor.apps.sale.service.configurator.ConfiguratorSaleOrderLineService;
import com.axelor.apps.sale.service.configurator.ConfiguratorServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.creation.SaleOrderLineGeneratorService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineOnProductChangeService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.rpc.JsonContext;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Optional;

public class ConfiguratorServiceProductionImpl extends ConfiguratorServiceImpl {

  protected final ConfiguratorBomService configuratorBomService;
  protected final BillOfMaterialRepository billOfMaterialRepository;
  protected final ConfiguratorCheckServiceProduction configuratorCheckServiceProduction;

  @Inject
  public ConfiguratorServiceProductionImpl(
      AppBaseService appBaseService,
      ConfiguratorFormulaService configuratorFormulaService,
      ProductRepository productRepository,
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderComputeService saleOrderComputeService,
      MetaFieldRepository metaFieldRepository,
      ConfiguratorMetaJsonFieldService configuratorMetaJsonFieldService,
      SaleOrderLineOnProductChangeService saleOrderLineOnProductChangeService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLineGeneratorService saleOrderLineGeneratorService,
      SaleOrderRepository saleOrderRepository,
      ConfiguratorCheckService configuratorCheckService,
      ConfiguratorSaleOrderLineService configuratorSaleOrderLineService,
      ProductCompanyRepository productCompanyRepository,
      ConfiguratorBomService configuratorBomService,
      BillOfMaterialRepository billOfMaterialRepository,
      ConfiguratorCheckServiceProduction configuratorCheckServiceProduction,
      ConfiguratorRepository configuratorRepository) {
    super(
        appBaseService,
        configuratorFormulaService,
        productRepository,
        saleOrderLineRepository,
        saleOrderComputeService,
        metaFieldRepository,
        configuratorMetaJsonFieldService,
        saleOrderLineOnProductChangeService,
        saleOrderLineComputeService,
        saleOrderLineGeneratorService,
        saleOrderRepository,
        configuratorCheckService,
        configuratorSaleOrderLineService,
        productCompanyRepository,
            configuratorRepository);
    this.configuratorBomService = configuratorBomService;
    this.billOfMaterialRepository = billOfMaterialRepository;
    this.configuratorCheckServiceProduction = configuratorCheckServiceProduction;
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
      configuratorBomService
          .generateBillOfMaterial(
              configuratorBOM, jsonAttributes, 0, generatedProduct, configurator)
          .ifPresent(generatedProduct::setDefaultBillOfMaterial);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void regenerateProduct(
      Configurator configurator,
      Product product,
      JsonContext jsonAttributes,
      JsonContext jsonIndicators,
      Long saleOrderId)
      throws AxelorException {
    super.regenerateProduct(configurator, product, jsonAttributes, jsonIndicators, saleOrderId);
    ConfiguratorBOM configuratorBOM = configurator.getConfiguratorCreator().getConfiguratorBom();
    BillOfMaterial oldBillOfMaterial = null;
    ProdProcess oldProdProcess = null;
    if (configuratorBOM != null) {
      oldBillOfMaterial = product.getDefaultBillOfMaterial();
      configuratorBomService
          .generateBillOfMaterial(configuratorBOM, jsonAttributes, 0, product, configurator)
          .ifPresent(product::setDefaultBillOfMaterial);
    }

    // Removing
    if (oldBillOfMaterial != null) {
      try {
        configuratorCheckServiceProduction.checkUsedBom(oldBillOfMaterial);
        billOfMaterialRepository.remove(oldBillOfMaterial);
      } catch (AxelorException e) {
        // Only tracing, we will not remove the bom.
        TraceBackService.trace(e);
      }
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
          .generateBillOfMaterial(configuratorBOM, jsonAttributes, 0, null, configurator)
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
    BillOfMaterial defaultBOM = getDefaultBOM(saleOrderLine);
    saleOrderLine.setBillOfMaterial(defaultBOM);
    saleOrderLine.setProdProcess(
        Optional.ofNullable(defaultBOM).map(BillOfMaterial::getProdProcess).orElse(null));
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
