package com.axelor.apps.base.service.batch;

import com.axelor.apps.base.db.ImportBatch;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.repo.ImportConfigurationRepository;
import com.axelor.apps.base.db.repo.ImportHistoryRepository;
import com.axelor.apps.base.service.filesourceconnector.FileSourceConnectorService;
import com.axelor.apps.base.service.imports.ImportService;
import com.axelor.apps.base.service.imports.importer.FactoryImporter;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BatchImportData extends AbstractImportBatch {

  private static final String TRACE_ORIGIN = "Import Batch";
  protected ImportService importService;

  protected FactoryImporter factoryImporter;
  protected ImportHistoryRepository importHistoryRepository;
  protected MetaFileRepository metaFileRepository;
  protected ImportConfigurationRepository importConfigurationRepository;
  protected UserRepository userRepository;

  @Inject
  public BatchImportData(
      FileSourceConnectorService fileSourceConnectorService,
      ImportService importService,
      FactoryImporter factoryImporter,
      ImportHistoryRepository importHistoryRepository,
      MetaFileRepository metaFileRepository,
      ImportConfigurationRepository importConfigurationRepository,
      UserRepository userRepository) {
    super(fileSourceConnectorService);
    this.importService = importService;
    this.factoryImporter = factoryImporter;
    this.importHistoryRepository = importHistoryRepository;
    this.metaFileRepository = metaFileRepository;
    this.importConfigurationRepository = importConfigurationRepository;
    this.userRepository = userRepository;
  }

  @Override
  protected void process() {

    ImportBatch importBatch = batch.getImportBatch();

    // In this case it is just using import service on importConfig
    if (!importBatch.getImportFromConnector()) {
      try {
        importService.run(importBatch.getImportConfig());
        incrementDone();
      } catch (AxelorException | IOException e) {
        TraceBackService.trace(e, TRACE_ORIGIN, batch.getId());
        incrementAnomaly();
      }
    } else {
      try {
        List<MetaFile> files = downloadFiles(importBatch.getFileSourceConnectorParameters());
        List<ImportHistory> histories =
            importFiles(files, importBatch.getImportConfig().getBindMetaFile());
        addBatch(histories);
      } catch (Exception e) {
        TraceBackService.trace(e, TRACE_ORIGIN, batch.getId());
        incrementAnomaly();
      }
    }
  }

  @Transactional
  protected void addBatch(List<ImportHistory> histories) {

    for (ImportHistory history : histories) {
      history.setBatch(this.getBatch());
      // If we don't do this we will get a Detached entity exception
      if (history.getDataMetaFile() != null) {
        history.setDataMetaFile(metaFileRepository.find(history.getDataMetaFile().getId()));
      }
      if (history.getLogMetaFile() != null) {
        history.setLogMetaFile(metaFileRepository.find(history.getLogMetaFile().getId()));
      }
      history.setImportConfiguration(
          importConfigurationRepository.find(batch.getImportBatch().getImportConfig().getId()));
      history.setUser(userRepository.find(batch.getCreatedBy().getId()));
      importHistoryRepository.save(history);
    }
  }

  protected List<ImportHistory> importFiles(List<MetaFile> files, MetaFile bindMetaFile) {

    ArrayList<ImportHistory> importHistories = new ArrayList<>();
    ImportConfiguration importConfiguration = batch.getImportBatch().getImportConfig();

    for (MetaFile file : files) {
      try {
        importConfiguration.setBindMetaFile(bindMetaFile);
        importConfiguration.setDataMetaFile(file);
        importHistories.add(factoryImporter.createImporter(importConfiguration).run());
        incrementDone();
      } catch (Exception e) {
        TraceBackService.trace(e, TRACE_ORIGIN, batch.getId());
        incrementAnomaly();
      }
    }

    return importHistories;
  }
}
