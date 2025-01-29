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
