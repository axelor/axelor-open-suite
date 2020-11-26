package com.axelor.apps.sale.db.repo;

import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorService;
import com.axelor.inject.Beans;
import javax.persistence.PostPersist;

public class ConfiguratorCreatorListener {

  @PostPersist
  private void onPostPersist(ConfiguratorCreator creator) {
    Beans.get(ConfiguratorCreatorService.class).init(creator);
  }
}
