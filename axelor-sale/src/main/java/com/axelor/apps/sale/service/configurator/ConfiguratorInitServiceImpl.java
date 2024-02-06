package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.repo.ConfiguratorRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ConfiguratorInitServiceImpl implements ConfiguratorInitService {

  protected ConfiguratorRepository configuratorRepository;

  @Inject
  public ConfiguratorInitServiceImpl(ConfiguratorRepository configuratorRepository) {
    this.configuratorRepository = configuratorRepository;
  }

  @Override
  @Transactional
  public Configurator create(ConfiguratorCreator configuratorCreator) {
    Configurator configurator = new Configurator();
    configurator.setConfiguratorCreator(configuratorCreator);
    configurator.setConfiguratorCreatorName(configuratorCreator.getName());
    return configuratorRepository.save(configurator);
  }
}
