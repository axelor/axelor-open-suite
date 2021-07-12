package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.sale.db.ConfiguratorCreator;
import java.util.List;

public interface ConfiguratorIEService {

  /**
   * Export a list of configurator creator to xml
   *
   * @param ccList
   */
  void exportConfiguratorsToXML(List<ConfiguratorCreator> ccList);
}
