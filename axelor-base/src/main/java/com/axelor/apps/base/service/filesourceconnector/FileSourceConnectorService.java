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
package com.axelor.apps.base.service.filesourceconnector;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.FileSourceConnector;
import com.axelor.apps.base.db.FileSourceConnectorParameters;
import com.axelor.apps.base.service.filesourceconnector.models.FileTransfertSession;
import com.axelor.meta.db.MetaFile;
import java.util.List;

public interface FileSourceConnectorService {

  /**
   * Method that tries to connect to host in fileSourceConnector
   *
   * @param fileSourceConnector : {@link FileSourceConnector}
   * @return true if connection is valid, else false.
   */
  boolean isValid(FileSourceConnector fileSourceConnector) throws AxelorException;

  /**
   * Method to create a FileTransfertSession with fileSourceConnector
   *
   * @param fileSourceConnector
   * @return
   * @throws AxelorException
   */
  FileTransfertSession createSession(FileSourceConnector fileSourceConnector)
      throws AxelorException;

  /**
   * Upload files using the fileTransfertSession and parameter. This method will automatically
   * connect and disconnect the session for the operation.
   *
   * <p>So if you want to do multiple operations it might be better to directly use the methods of
   * fileTransfertSession to manage the connection and disconnection manually.
   *
   * @param fileTransfertSession
   * @param parameter
   * @param files
   * @throws AxelorException
   */
  void upload(
      FileTransfertSession fileTransfertSession,
      FileSourceConnectorParameters parameter,
      List<MetaFile> files)
      throws AxelorException;

  /**
   * Download files using the fileTransfertSession and parameter. This method will automatically
   * connect and disconnect the session for the operation.
   *
   * <p>So if you want to do multiple operations it might be better to directly use the methods of
   * fileTransfertSession to manage the connection and disconnection manually.
   *
   * @param fileTransfertSession
   * @param parameter
   * @param files
   * @return download files: List of {@link MetaFile}
   * @throws AxelorException
   */
  List<MetaFile> download(
      FileTransfertSession fileTransfertSession, FileSourceConnectorParameters parameter)
      throws AxelorException;
}
