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
package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorderline.creation.SaleOrderLineGeneratorService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;

public class ConfiguratorSaleOrderLineServiceImpl implements ConfiguratorSaleOrderLineService {

  protected final ConfiguratorCheckService configuratorCheckService;
  protected final SaleOrderLineRepository saleOrderLineRepository;
  protected final SaleOrderLineGeneratorService saleOrderLineGeneratorService;
  protected final SaleOrderComputeService saleOrderComputeService;
  protected final SaleOrderRepository saleOrderRepository;

  @Inject
  public ConfiguratorSaleOrderLineServiceImpl(
      ConfiguratorCheckService configuratorCheckService,
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderLineGeneratorService saleOrderLineGeneratorService,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderRepository saleOrderRepository) {
    this.configuratorCheckService = configuratorCheckService;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.saleOrderLineGeneratorService = saleOrderLineGeneratorService;
    this.saleOrderComputeService = saleOrderComputeService;
    this.saleOrderRepository = saleOrderRepository;
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void regenerateSaleOrderLine(
      Configurator configurator, Product product, SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {

    generateSaleOrderLine(configurator, product, saleOrderLine);

    // Bye bye old sale order line
    saleOrder.removeSaleOrderLineListItem(saleOrderLine);
    saleOrderRepository.save(saleOrder);
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public SaleOrderLine generateSaleOrderLine(
      Configurator configurator, Product product, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Objects.requireNonNull(configurator);
    Objects.requireNonNull(product);
    Objects.requireNonNull(saleOrderLine);
    var newSaleOrderLine =
        saleOrderLineGeneratorService.createSaleOrderLine(
            saleOrderLine.getSaleOrder(), product, saleOrderLine.getQty());
    newSaleOrderLine.setConfigurator(configurator);
    return saleOrderLineRepository.save(newSaleOrderLine);
  }
}
