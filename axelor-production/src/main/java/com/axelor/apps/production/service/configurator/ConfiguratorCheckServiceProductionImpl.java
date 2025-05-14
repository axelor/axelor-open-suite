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
package com.axelor.apps.production.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.supplychain.service.ConfiguratorCheckServiceSupplychainImpl;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class ConfiguratorCheckServiceProductionImpl
    extends ConfiguratorCheckServiceSupplychainImpl {

  protected final ProductionOrderRepository productionOrderRepository;
  protected final ManufOrderRepository manufOrderRepository;
  protected final BillOfMaterialRepository billOfMaterialRepository;
  protected final ProdProcessRepository prodProcessRepository;

  @Inject
  public ConfiguratorCheckServiceProductionImpl(
      SaleOrderLineRepository saleOrderLineRepository,
      ProductionOrderRepository productionOrderRepository,
      ManufOrderRepository manufOrderRepository,
      BillOfMaterialRepository billOfMaterialRepository,
      ProdProcessRepository prodProcessRepository) {
    super(saleOrderLineRepository);
    this.productionOrderRepository = productionOrderRepository;
    this.manufOrderRepository = manufOrderRepository;
    this.billOfMaterialRepository = billOfMaterialRepository;
    this.prodProcessRepository = prodProcessRepository;
  }

  @Override
  public void checkLinkedSaleOrderLine(Configurator configurator) throws AxelorException {
    super.checkLinkedSaleOrderLine(configurator);
    var saleOrderLines =
        saleOrderLineRepository
            .all()
            .filter("self.configurator = :configurator")
            .bind("configurator", configurator)
            .fetch();

    // Will check if any production orders have been generated
    var inProduction =
        saleOrderLines.stream()
            .anyMatch(sol -> sol.getManufOrderList() != null && !sol.getManufOrderList().isEmpty());

    if (inProduction) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.CAN_NOT_RENGENERATE_PRODUCT_LINKED_TO_MO));
    }
  }

  @Override
  public void checkLinkedSaleOrderLine(Configurator configurator, Product product)
      throws AxelorException {
    super.checkLinkedSaleOrderLine(configurator, product);

    var saleOrderLines =
        saleOrderLineRepository
            .all()
            .filter("self.configurator = :configurator AND self.product = :product")
            .bind("configurator", configurator)
            .bind("product", product)
            .fetch();

    // Will check if any production orders have been generated
    var inProduction =
        saleOrderLines.stream()
            .anyMatch(sol -> sol.getManufOrderList() != null && !sol.getManufOrderList().isEmpty());

    if (inProduction) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.CAN_NOT_RENGENERATE_PRODUCT_LINKED_TO_MO));
    }
  }
}
