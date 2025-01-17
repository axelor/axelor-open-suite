package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.Objects;

public class ConfiguratorCheckServiceImpl implements ConfiguratorCheckService {

  protected final SaleOrderLineRepository saleOrderLineRepository;

  @Inject
  public ConfiguratorCheckServiceImpl(SaleOrderLineRepository saleOrderLineRepository) {
    this.saleOrderLineRepository = saleOrderLineRepository;
  }

  @Override
  public void checkLinkedSaleOrderLine(Configurator configurator, Product product)
      throws AxelorException {
    Objects.requireNonNull(configurator);
    Objects.requireNonNull(product);

    var terminatedSOL =
        saleOrderLineRepository
            .all()
            .filter(
                "self.product = :product AND self.configurator = :configurator AND self.saleOrder.statusSelect IN (:finalizedStatus, :confirmedStatus, :completedStatus)")
            .bind("product", product)
            .bind("configurator", configurator)
            .bind("finalizedStatus", SaleOrderRepository.STATUS_FINALIZED_QUOTATION)
            .bind("confirmedStatus", SaleOrderRepository.STATUS_ORDER_CONFIRMED)
            .bind("completedStatus", SaleOrderRepository.STATUS_ORDER_COMPLETED)
            .fetchOne();

    if (terminatedSOL != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.CONFIGURATOR_CAN_NOT_REGENERATE_PRODUCT));
    }
  }

  @Override
  public void checkLinkedSaleOrderLine(Configurator configurator) throws AxelorException {
    Objects.requireNonNull(configurator);
    var terminatedSOL =
        saleOrderLineRepository
            .all()
            .filter(
                "self.configurator = :configurator AND self.saleOrder.statusSelect IN (:finalizedStatus, :confirmedStatus, :completedStatus)")
            .bind("configurator", configurator)
            .bind("finalizedStatus", SaleOrderRepository.STATUS_FINALIZED_QUOTATION)
            .bind("confirmedStatus", SaleOrderRepository.STATUS_ORDER_CONFIRMED)
            .bind("completedStatus", SaleOrderRepository.STATUS_ORDER_COMPLETED)
            .fetchOne();

    if (terminatedSOL != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.CONFIGURATOR_CAN_NOT_REGENERATE_PRODUCT));
    }
  }
}
