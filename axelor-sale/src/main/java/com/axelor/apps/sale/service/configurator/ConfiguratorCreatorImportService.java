/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.ConfiguratorCreator;
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

  /**
   * When exported, attribute name finish with '_XX' where XX is the id of the creator. After
   * importing or copying, we need to fix these values.
   *
   * @param creator a saved configurator creator
   * @throws AxelorException
   */
  void fixAttributesName(ConfiguratorCreator creator) throws AxelorException;
}
