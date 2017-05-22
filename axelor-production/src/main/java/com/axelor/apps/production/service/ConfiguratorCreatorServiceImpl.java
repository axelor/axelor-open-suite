package com.axelor.apps.production.service;

import com.axelor.apps.production.db.Configurator;
import com.axelor.apps.production.db.ConfiguratorCreator;
import com.axelor.apps.production.db.repo.ConfiguratorRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ConfiguratorCreatorServiceImpl implements ConfiguratorCreatorService {
	
	@Inject
	private ConfiguratorRepository configuratorRepo;
	
	
	@Override
	@Transactional
	public Configurator generateConfigurator(ConfiguratorCreator creator) {
		
		if (creator == null) {
			return null;
		}
		
		Configurator configurator =  configuratorRepo.all().filter("self.configuratorCreator = ?1", creator).fetchOne();
		
		if (configurator == null) {
			configurator = new Configurator();
			configurator.setConfiguratorCreator(creator);
			configuratorRepo.save(configurator);
		}
		
		
		return configurator;
	}

}
