package com.axelor.apps.sale.service;

import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.ConfiguratorCreator;

public interface ConfiguratorCreatorService {

		public Configurator generateConfigurator(ConfiguratorCreator creator);
}
