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
import com.axelor.apps.base.db.AdvancedImport;
import com.axelor.apps.base.db.ImportBatch;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.repo.AdvancedImportRepository;
import com.axelor.apps.base.db.repo.BatchImportHistoryRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.advanced.imports.DataImportService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.filesourceconnector.FileSourceConnectorService;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class BatchAdvancedImportData extends AbstractImportBatch {

  private static final String TRACE_ORIGIN = "Advanced import Batch";

  protected DataImportService dataImportService;
  protected MetaFileRepository metaFileRepository;

  @Inject
  protected BatchAdvancedImportData(
      FileSourceConnectorService fileSourceConnectorService,
      BatchImportHistoryRepository batchImportHistoryRepository,
      DataImportService dataImportService,
      MetaFileRepository metaFileRepository,
      UserRepository userRepository) {
    super(fileSourceConnectorService, batchImportHistoryRepository, userRepository);
    this.dataImportService = dataImportService;
    this.metaFileRepository = metaFileRepository;
  }

  @Override
  protected void process() {

    ImportBatch importBatch = batch.getImportBatch();

    // In this case it is just using import service on importConfig
    try {
      importData(importBatch.getAdvancedImport());
      incrementDone();
    } catch (Exception e) {
      TraceBackService.trace(e, TRACE_ORIGIN, batch.getId());
      incrementAnomaly();
    }
  }

  protected void importData(AdvancedImport advancedImport)
      throws AxelorException, ClassNotFoundException, IOException {
    Objects.requireNonNull(advancedImport);
    if (advancedImport.getStatusSelect() != AdvancedImportRepository.STATUS_SELECT_VALIDATED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.ADVANCED_IMPORT_NOT_VALIDATED));
    }
    ImportHistory importHistory = dataImportService.importData(advancedImport);
    createBatchHistory(
        metaFileRepository.find(importHistory.getDataMetaFile().getId()),
        importHistory.getLogMetaFile() != null
            ? metaFileRepository.find(importHistory.getLogMetaFile().getId())
            : null);
  }

  protected void importFiles(List<MetaFile> files) {

    AdvancedImport advancedImport = batch.getImportBatch().getAdvancedImport();

    for (MetaFile file : files) {
      try {
        advancedImport.setImportFile(file);
        importData(advancedImport);
        incrementDone();
      } catch (Exception e) {
        TraceBackService.trace(e, TRACE_ORIGIN, batch.getId());
        incrementAnomaly();
      }
    }
  }
}
