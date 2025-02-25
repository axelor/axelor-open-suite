package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.i18n.I18n;
import java.util.Objects;

public class ConfiguratorCheckServiceImpl implements ConfiguratorCheckService {

  @Override
  public void checkLinkedSaleOrderLine(Configurator configurator, Product product)
      throws AxelorException {
    Objects.requireNonNull(configurator);
    Objects.requireNonNull(product);

    // Nothing to check in sale module
  }

  @Override
  public void checkLinkedSaleOrderLine(Configurator configurator) throws AxelorException {
    Objects.requireNonNull(configurator);

    // Nothing to check in sale module
  }

  @Override
  public void checkHaveConfigurator(SaleOrder saleOrder) throws AxelorException {
    Objects.requireNonNull(saleOrder);

    if (saleOrder.getSaleOrderLineList() == null || saleOrder.getSaleOrderLineList().isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALE_ORDER_DO_NOT_HAVE_CONFIGURATOR));
    }

    boolean hasConfigurator =
        saleOrder.getSaleOrderLineList().stream().anyMatch(sol -> sol.getConfigurator() != null);
    if (!hasConfigurator) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALE_ORDER_DO_NOT_HAVE_CONFIGURATOR));
    }
  }

  @Override
  public boolean isConfiguratorVersionDifferent(Configurator configurator) {

    Objects.requireNonNull(configurator);

    return !configurator
        .getConfiguratorVersion()
        .equals(configurator.getConfiguratorCreator().getConfiguratorVersion());
  }

  @Override
  public void checkConfiguratorActivated(Configurator configurator) throws AxelorException {
    if (configurator != null && !configurator.getConfiguratorCreator().getIsActive()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SaleExceptionMessage.CONFIGURATOR_IS_NOT_ACTIVATED));
      }
  }
}
