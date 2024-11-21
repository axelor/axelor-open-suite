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
package com.axelor.apps.base.service.filesourceconnector.models;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.FileSourceConnector;
import com.axelor.apps.base.db.FileSourceConnectorParameters;
import com.axelor.meta.db.MetaFile;
import java.util.List;

public interface FileTransfertSession {

  /**
   * Methods that checks if the session is valid. Mostly done by checking if we can connect to the
   * host configured.
   *
   * @return true if valid, else false
   * @throws AxelorException
   */
  boolean isValid() throws AxelorException;

  /**
   * Upload files to host configured in parameters. The session must be configured and connected
   * before calling this method.
   *
   * @param parameters
   * @param files
   * @throws AxelorException
   */
  void upload(FileSourceConnectorParameters parameters, List<MetaFile> files)
      throws AxelorException;

  /**
   * Download files from host configured in parameters. The session must be configured and connected
   * before calling this method.
   *
   * @param parameters
   * @param files
   * @return List of {@link MetaFile}
   * @throws AxelorException
   */
  List<MetaFile> download(FileSourceConnectorParameters parameter) throws AxelorException;

  /**
   * Method to configure a session by using the fileSourceConnector
   *
   * @param fileSourceConnector
   * @return the configured session
   * @throws AxelorException
   */
  FileTransfertSession configureSession(FileSourceConnector fileSourceConnector)
      throws AxelorException;

  /**
   * Disconnect the session
   *
   * @throws AxelorException
   */
  void disconnect() throws AxelorException;

  /**
   * Connect the session
   *
   * @throws AxelorException
   */
  void connect() throws AxelorException;
}
