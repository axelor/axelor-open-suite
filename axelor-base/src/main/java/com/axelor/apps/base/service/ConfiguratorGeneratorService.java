package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ConfiguratorGenerator;

public interface ConfiguratorGeneratorService {

  void syncDependencies(ConfiguratorGenerator generator);
}
