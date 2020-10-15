/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.sale.service.configurator;

import java.io.IOException;
import java.io.InputStream;

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

  /**
   * Import configurator creators from given XML config file input stream. This method allows to use
   * a file object to replace the filePath.
   *
   * @param xmlInputStream input stream to the data file.
   * @return the import log file.
   */
  String importConfiguratorCreators(InputStream xmlInputStream) throws IOException;

  /**
   * Import configurator creators from given XML config file. This method allows to use a file
   * object to replace the filePath.
   *
   * @param xmlInputStream input stream to the data file.
   * @param configFilePath the path to XML config file.
   * @return the import log file.
   */
  String importConfiguratorCreators(InputStream xmlInputStream, String configFilePath)
      throws IOException;
}
