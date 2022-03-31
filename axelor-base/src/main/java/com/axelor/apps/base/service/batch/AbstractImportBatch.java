package com.axelor.apps.base.service.batch;

import com.axelor.apps.base.db.FileSourceConnectorParameters;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.filesourceconnector.FileSourceConnectorService;
import com.axelor.apps.base.service.filesourceconnector.models.FileTransfertSession;
import com.axelor.apps.base.translation.ITranslation;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.util.List;

public abstract class AbstractImportBatch extends AbstractBatch {

  protected FileSourceConnectorService fileSourceConnectorService;

  @Inject
  protected AbstractImportBatch(FileSourceConnectorService fileSourceConnectorService) {
    this.fileSourceConnectorService = fileSourceConnectorService;
  }

  protected List<MetaFile> downloadFiles(
      FileSourceConnectorParameters fileSourceConnectorParameters) throws AxelorException {

    FileTransfertSession session =
        fileSourceConnectorService.createSession(
            fileSourceConnectorParameters.getFileSourceConnector());
    return fileSourceConnectorService.download(session, fileSourceConnectorParameters);
  }

  @Override
  protected void stop() {

    StringBuilder comment = new StringBuilder();
    comment.append(
        "\t"
            + String.format(
                I18n.get(ITranslation.BASE_IMPORT_BATCH_FILES_IMPORTED), batch.getDone()));
    comment.append(
        "\t"
            + String.format(
                I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4),
                batch.getAnomaly()));
    addComment(comment.toString());
    super.stop();
  }
}
