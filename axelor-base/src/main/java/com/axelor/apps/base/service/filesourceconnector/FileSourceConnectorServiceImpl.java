package com.axelor.apps.base.service.filesourceconnector;

import com.axelor.apps.base.db.FileSourceConnector;
import com.axelor.apps.base.db.FileSourceConnectorParameters;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.filesourceconnector.factory.FileTransfertSessionFactory;
import com.axelor.apps.base.service.filesourceconnector.models.FileTransfertSession;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;

public class FileSourceConnectorServiceImpl implements FileSourceConnectorService {

  protected FileTransfertSessionFactory fileTransfertSessionFactory;

  @Inject
  public FileSourceConnectorServiceImpl(FileTransfertSessionFactory fileTransfertSessionFactory) {

    this.fileTransfertSessionFactory = fileTransfertSessionFactory;
  }

  @Override
  public boolean isValid(FileSourceConnector fileSourceConnector) throws AxelorException {
    Objects.requireNonNull(fileSourceConnector);

    FileTransfertSession session = createSession(fileSourceConnector);

    return session.isValid();
  }

  @Override
  public FileTransfertSession createSession(FileSourceConnector fileSourceConnector)
      throws AxelorException {
    Objects.requireNonNull(fileSourceConnector);

    if (fileSourceConnector.getConnectionType() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(BaseExceptionMessage.FILE_SOURCE_CONNECTOR_CONNECTION_TYPE_NULL));
    }

    FileTransfertSession session =
        fileTransfertSessionFactory
            .getFileTransfertSession(fileSourceConnector.getConnectionType())
            .configureSession(fileSourceConnector);

    return session;
  }

  @Override
  public void upload(
      FileTransfertSession fileTransfertSession,
      FileSourceConnectorParameters parameter,
      List<MetaFile> files)
      throws AxelorException {
    Objects.requireNonNull(fileTransfertSession);
    Objects.requireNonNull(parameter);
    Objects.requireNonNull(files);
    try {
      fileTransfertSession.connect();
      fileTransfertSession.upload(parameter, files);
    } finally {
      fileTransfertSession.disconnect();
    }
  }

  @Override
  public List<MetaFile> download(
      FileTransfertSession fileTransfertSession, FileSourceConnectorParameters parameter)
      throws AxelorException {
    Objects.requireNonNull(fileTransfertSession);
    Objects.requireNonNull(parameter);

    try {
      fileTransfertSession.connect();
      List<MetaFile> result = fileTransfertSession.download(parameter);
      return result;
    } finally {
      fileTransfertSession.disconnect();
    }
  }
}
