package com.axelor.apps.base.service.filesourceconnector;

import java.util.List;

import com.axelor.apps.base.db.FileSourceConnector;
import com.axelor.apps.base.db.FileSourceConnectorParameters;
import com.axelor.apps.base.service.filesourceconnector.models.FileTransfertSession;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;

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

  void upload(
      FileTransfertSession fileTransfertSession,
      FileSourceConnectorParameters parameter,
      List<MetaFile> files)
      throws AxelorException;

  List<MetaFile> download(
      FileTransfertSession fileTransfertSession, FileSourceConnectorParameters parameter)
      throws AxelorException;
}
