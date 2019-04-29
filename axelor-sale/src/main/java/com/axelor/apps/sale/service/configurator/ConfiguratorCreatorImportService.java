package com.axelor.apps.sale.service.configurator;

import java.io.IOException;

public interface ConfiguratorCreatorImportService {

  /**
   * Import configurator creators from given XML config file. Use the default path for XML config
   * file.
   *
   * @param filePath the path to the data file.
   * @return the import log file.
   */
  String importConfiguratorCreators(String filePath) throws IOException;

  /**
   * Import configurator creators from given XML config file.
   *
   * @param filePath the path to the data file.
   * @param configFilePath the path to XML config file.
   * @return the import log file.
   */
  String importConfiguratorCreators(String filePath, String configFilePath) throws IOException;
}
