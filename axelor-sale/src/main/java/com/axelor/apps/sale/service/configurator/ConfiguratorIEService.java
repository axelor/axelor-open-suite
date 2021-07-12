package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;
import java.util.List;

public interface ConfiguratorIEService {

  /**
   * Export list of configurator creator to xml
   *
   * @param ccList
   */
  MetaFile exportConfiguratorsToXML(List<ConfiguratorCreator> ccList) throws AxelorException;

  String importXMLToConfigurators(String pathDiff) throws AxelorException;
}
