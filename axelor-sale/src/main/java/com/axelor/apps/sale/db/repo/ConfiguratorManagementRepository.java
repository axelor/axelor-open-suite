package com.axelor.apps.sale.db.repo;

import com.axelor.apps.sale.db.Configurator;

public class ConfiguratorManagementRepository extends ConfiguratorRepository {

  @Override
  public Configurator copy(Configurator entity, boolean deep) {
    var copy = super.copy(entity, deep);
    copy.setProduct(null);
    return copy;
  }

  @Override
  public Configurator save(Configurator entity) {
    entity = super.save(entity);
    if (entity.getConfiguratorCreator() != null && entity.getConfiguratorVersion() == null) {
      entity.setConfiguratorVersion(entity.getConfiguratorCreator().getConfiguratorVersion());
    }
    return entity;
  }
}
