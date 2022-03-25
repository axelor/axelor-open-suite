package com.axelor.apps.base.service.filesourceconnector.models;

import java.util.List;

import com.axelor.apps.base.db.FileSourceConnector;
import com.axelor.apps.base.db.FileSourceConnectorParameters;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;

public interface FileTransfertSession {

  boolean isValid() throws AxelorException;

  void upload(FileSourceConnectorParameters parameters, List<MetaFile> files)
      throws AxelorException;

  List<MetaFile> download(FileSourceConnectorParameters parameter) throws AxelorException;

  FileTransfertSession configureSession(FileSourceConnector fileSourceConnector)
      throws AxelorException;

  void disconnect() throws AxelorException;

  void connect() throws AxelorException;
}
