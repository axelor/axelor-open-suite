package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.Configurator;
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

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void regenerateSaleOrderLines(Configurator configurator, Product product)
      throws AxelorException {

    Objects.requireNonNull(configurator);
    Objects.requireNonNull(product);

    configuratorCheckService.checkLinkedSaleOrderLine(configurator, product);

    var saleOrderLines =
        saleOrderLineRepository
            .all()
            .filter(
                "self.product = :product AND self.configurator = :configurator AND self.saleOrder.statusSelect = :draftStatus")
            .bind("product", product)
            .bind("configurator", configurator)
            .bind("draftStatus", SaleOrderRepository.STATUS_DRAFT_QUOTATION)
            .fetch();

    for (var saleOrderLine : saleOrderLines) {
      regenerateSaleOrderLine(configurator, product, saleOrderLine);
    }
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void regenerateSaleOrderLine(
      Configurator configurator, Product product, SaleOrderLine saleOrderLine)
      throws AxelorException {

    createSaleOrderSaleLine(configurator, product, saleOrderLine);
    var saleOrder = saleOrderLine.getSaleOrder();

    // Bye bye old sale order line
    saleOrder.removeSaleOrderLineListItem(saleOrderLine);

    saleOrderComputeService.computeSaleOrder(saleOrder);
    saleOrderRepository.save(saleOrder);
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void generateSaleOrderLine(
      Configurator configurator, Product product, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Objects.requireNonNull(configurator);
    Objects.requireNonNull(product);
    Objects.requireNonNull(saleOrderLine);
    createSaleOrderSaleLine(configurator, product, saleOrderLine);
    var saleOrder = saleOrderLine.getSaleOrder();

    saleOrderComputeService.computeSaleOrder(saleOrder);
    saleOrderRepository.save(saleOrder);
  }

  protected void createSaleOrderSaleLine(
      Configurator configurator, Product product, SaleOrderLine saleOrderLine)
      throws AxelorException {
    var newSaleOrderLine =
        saleOrderLineGeneratorService.createSaleOrderLine(
            saleOrderLine.getSaleOrder(), product, saleOrderLine.getQty());
    newSaleOrderLine.setConfigurator(configurator);
    saleOrderLineRepository.save(newSaleOrderLine);
  }
}
