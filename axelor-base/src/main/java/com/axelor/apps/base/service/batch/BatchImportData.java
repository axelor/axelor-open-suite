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
package com.axelor.apps.base.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ImportBatch;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.repo.BatchImportHistoryRepository;
import com.axelor.apps.base.db.repo.ImportConfigurationRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.filesourceconnector.FileSourceConnectorService;
import com.axelor.apps.base.service.imports.ImportService;
import com.axelor.apps.base.service.imports.importer.FactoryImporter;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.common.StringUtils;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class BatchImportData extends AbstractImportBatch {

  private static final String TRACE_ORIGIN = "Import Batch";
  protected ImportService importService;
  protected ImportConfigurationRepository importConfigurationRepository;

  protected FactoryImporter factoryImporter;
  protected MetaFileRepository metaFileRepository;
  protected MetaFiles metaFiles;

  @Inject
  public BatchImportData(
      FileSourceConnectorService fileSourceConnectorService,
      ImportService importService,
      FactoryImporter factoryImporter,
      BatchImportHistoryRepository batchImportHistoryRepository,
      MetaFileRepository metaFileRepository,
      UserRepository userRepository,
      MetaFiles metaFiles,
      ImportConfigurationRepository importConfigurationRepository) {
    super(fileSourceConnectorService, batchImportHistoryRepository, userRepository);
    this.importService = importService;
    this.factoryImporter = factoryImporter;
    this.metaFileRepository = metaFileRepository;
    this.metaFiles = metaFiles;
    this.importConfigurationRepository = importConfigurationRepository;
  }

  @Override
  protected void process() {

    ImportBatch importBatch = batch.getImportBatch();

    // In this case it is just using import service on importConfig
    if (!importBatch.getImportFromConnector()) {
      try {
        ImportHistory importHistory = importService.run(importBatch.getImportConfig());
        createBatchHistory(
            metaFileRepository.find(importHistory.getDataMetaFile().getId()),
            metaFileRepository.find(importHistory.getLogMetaFile().getId()));
        incrementDone();
      } catch (Exception e) {
        TraceBackService.trace(e, TRACE_ORIGIN, batch.getId());
        incrementAnomaly();
      }
    } else {
      try {
        List<MetaFile> files = downloadFiles(importBatch.getFileSourceConnectorParameters());
        importFiles(files);
      } catch (Exception e) {
        TraceBackService.trace(e, TRACE_ORIGIN, batch.getId());
        incrementAnomaly();
      }
    }
  }

  protected void importFiles(List<MetaFile> files) {

    ImportConfiguration importConfiguration = batch.getImportBatch().getImportConfig();
    ImportBatch importBatch = batch.getImportBatch();
    MetaFile originalFile = importConfiguration.getDataMetaFile();

    for (MetaFile file : files) {
      try {
        importFile(importConfiguration, importBatch, file, originalFile);
        incrementDone();
      } catch (Exception e) {
        TraceBackService.trace(e, TRACE_ORIGIN, batch.getId());
        incrementAnomaly();
      }
    }
  }

  protected void importFile(
      ImportConfiguration importConfiguration,
      ImportBatch importBatch,
      MetaFile file,
      MetaFile originalFile)
      throws IOException, AxelorException {

    MetaFile tmpFile = createTemporaryFile(importBatch, file);
    if (tmpFile != null) {
      importConfiguration.setDataMetaFile(tmpFile);
    } else {
      importConfiguration.setDataMetaFile(file);
    }
    ImportHistory importHistory = factoryImporter.createImporter(importConfiguration).run();
    createBatchHistory(
        metaFileRepository.find(file.getId()),
        metaFileRepository.find(importHistory.getLogMetaFile().getId()));
    resetImportConfiguration(importConfiguration, originalFile);
    if (tmpFile != null) {
      metaFiles.delete(tmpFile);
    }
  }

  @Transactional
  protected void resetImportConfiguration(
      ImportConfiguration importConfiguration, MetaFile originalFile) {
    importConfiguration = importConfigurationRepository.find(importConfiguration.getId());
    importConfiguration.setDataMetaFile(metaFileRepository.find(originalFile.getId()));
    importConfigurationRepository.save(importConfiguration);
  }

  /**
   * Methods that created a tmp file if necessary. meaning if the file name imported is different
   * than the desired file name.
   *
   * @param importConfiguration
   * @param importBatch
   * @param file
   * @return MetaFile
   * @throws IOException
   */
  protected MetaFile createTemporaryFile(ImportBatch importBatch, MetaFile file)
      throws IOException {
    if (!StringUtils.isBlank(importBatch.getDataFileName())
        && !file.getFileName().equals(importBatch.getDataFileName())) {
      // If dataFileName is set and is different than the file name, we will create a temporary file
      // with the desired name
      // and use this name for the import
      try (FileInputStream fileInputStream =
          new FileInputStream(MetaFiles.getPath(file).toFile())) {
        return metaFiles.upload(fileInputStream, importBatch.getDataFileName());
      }
    }
    return null;
  }
}
