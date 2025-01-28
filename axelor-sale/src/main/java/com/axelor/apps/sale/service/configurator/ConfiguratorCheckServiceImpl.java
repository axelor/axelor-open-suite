package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.Configurator;
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
}
