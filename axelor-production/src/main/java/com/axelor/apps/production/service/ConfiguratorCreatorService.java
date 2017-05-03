package com.axelor.apps.production.service;

import com.axelor.apps.production.db.Configurator;
import com.axelor.apps.production.db.ConfiguratorCreator;

public interface ConfiguratorCreatorService {

		public Configurator generateConfigurator(ConfiguratorCreator creator);
}
