package com.axelor.apps.base.service.filesourceconnector.factory;

import com.axelor.apps.base.db.repo.FileSourceConnectorRepository;
import com.axelor.apps.base.service.filesourceconnector.models.FileTransfertSession;
import com.axelor.apps.base.service.filesourceconnector.models.SFTPFileTransfertSession;
import com.axelor.inject.Beans;

public class FileTransfertSessionFactory {

  public FileTransfertSession getFileTransfertSession(int type) {

    switch (type) {
      case FileSourceConnectorRepository.CONNECTION_TYPE_FTP:
        return Beans.get(SFTPFileTransfertSession.class);
      default:
        throw new IllegalArgumentException("Type is not recognized by the factory");
    }
  }
}
