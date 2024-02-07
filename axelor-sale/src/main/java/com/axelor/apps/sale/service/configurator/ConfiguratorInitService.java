package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.ConfiguratorCreator;

public interface ConfiguratorInitService {

  /**
   * Create and persist a configurator with initialized fields.
   *
   * @param configuratorCreator a configurator creator
   * @return the created configurator
   */
  Configurator create(ConfiguratorCreator configuratorCreator);
}
