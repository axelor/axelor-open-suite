package com.axelor.apps.production.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.configurator.ConfiguratorCheckServiceImpl;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfiguratorCheckServiceProductionImpl extends ConfiguratorCheckServiceImpl
    implements ConfiguratorCheckServiceProduction {

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
    var anyProductionOrder =
        productionOrderRepository
            .all()
            .filter("self.saleOrder.id IN (:saleOrders)")
            .bind(
                "saleOrders",
                StringHelper.getIdListString(
                    saleOrderLines.stream()
                        .map(SaleOrderLine::getSaleOrder)
                        .collect(Collectors.toList())))
            .fetchOne();

    if (anyProductionOrder != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.CAN_NOT_RENGENERATE_SALE_ORDER_LINE_LINKED_TO_MO));
    }
  }

  @Override
  public void checkUsedBom(BillOfMaterial billOfMaterial) throws AxelorException {
    Objects.requireNonNull(billOfMaterial);

    // Check usage in ManufOrder
    var anyManufOrder =
        manufOrderRepository
            .all()
            .filter("self.billOfMaterial = :billOfMaterial")
            .bind("billOfMaterial", billOfMaterial)
            .fetchOne();

    if (anyManufOrder != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.CAN_NOT_RENGENERATE_SALE_ORDER_LINE_LINKED_TO_MO));
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
    var anyProductionOrder =
        productionOrderRepository
            .all()
            .filter("self.saleOrder IN (:saleOrders)")
            .bind(
                "saleOrders",
                StringHelper.getIdListString(
                    saleOrderLines.stream()
                        .map(SaleOrderLine::getSaleOrder)
                        .collect(Collectors.toList())))
            .fetchOne();

    if (anyProductionOrder != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.CAN_NOT_RENGENERATE_SALE_ORDER_LINE_LINKED_TO_MO));
    }
  }
}
