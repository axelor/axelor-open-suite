package com.axelor.apps.base.service.batch;

import com.axelor.apps.base.db.AdvancedImport;
import com.axelor.apps.base.db.ImportBatch;
import com.axelor.apps.base.service.advanced.imports.AdvancedImportService;
import com.axelor.apps.base.service.filesourceconnector.FileSourceConnectorService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.util.List;

public class BatchAdvancedImportData extends AbstractImportBatch {

  private static final String TRACE_ORIGIN = "Advanced import Batch";

  protected AdvancedImportService advancedImportService;

  @Inject
  protected BatchAdvancedImportData(FileSourceConnectorService fileSourceConnectorService) {
    super(fileSourceConnectorService);
  }

  @Override
  protected void process() {

    ImportBatch importBatch = batch.getImportBatch();

    // In this case it is just using import service on importConfig
    if (!importBatch.getImportFromConnector()) {
      try {
        advancedImportService.apply(importBatch.getAdvancedImport());
        incrementDone();
      } catch (Exception e) {
        TraceBackService.trace(e, TRACE_ORIGIN, batch.getId());
        incrementAnomaly();
      }
    } else {
      try {
        List<MetaFile> files = downloadFiles(importBatch.getFileSourceConnectorParameters());
        importFiles(files, importBatch.getImportConfig().getBindMetaFile());
      } catch (Exception e) {
        TraceBackService.trace(e, TRACE_ORIGIN, batch.getId());
        incrementAnomaly();
      }
    }
  }

  protected void importFiles(List<MetaFile> files, MetaFile bindMetaFile) {

    AdvancedImport advancedImport = batch.getImportBatch().getAdvancedImport();

    for (MetaFile file : files) {
      try {
        advancedImport.setImportFile(file);
        advancedImportService.apply(advancedImport);
        incrementDone();
      } catch (Exception e) {
        TraceBackService.trace(e, TRACE_ORIGIN, batch.getId());
        incrementAnomaly();
      }
    }
  }
}
