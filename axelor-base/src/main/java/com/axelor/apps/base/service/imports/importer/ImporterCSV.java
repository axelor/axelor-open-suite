/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.imports.importer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.imports.listener.ImporterListener;
import com.axelor.data.csv.CSVImporter;
import com.axelor.i18n.I18n;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Map;

class ImporterCSV extends Importer {

  @Override
  protected ImportHistory process(String bind, String data, Map<String, Object> importContext)
      throws IOException, AxelorException {

    CSVImporter importer = new CSVImporter(bind, data);

    ImporterListener listener = new ImporterListener(getConfiguration().getName());
    importer.addListener(listener);
    importer.setContext(importContext);
    importer.run();

    return addHistory(listener);
  }

  @Override
  protected ImportHistory process(String bind, String data) throws IOException, AxelorException {
    return process(bind, data, null);
  }

  @Override
  public void checkEntryFilesType(File bind, File data) throws AxelorException {
    super.checkEntryFilesType(bind, data);
    if (!Files.getFileExtension(data.getAbsolutePath()).equals("csv")
        && !Files.getFileExtension(data.getAbsolutePath()).equals("zip")) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.IMPORT_CONFIGURATION_WRONG_DATA_FILE_TYPE_CSV_MESSAGE));
    }
  }
}
