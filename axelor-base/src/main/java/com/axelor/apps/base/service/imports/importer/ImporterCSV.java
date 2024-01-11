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
package com.axelor.apps.base.service.imports.importer;

import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.service.imports.listener.ImporterListener;
import com.axelor.data.csv.CSVImporter;
import java.io.IOException;
import java.util.Map;

class ImporterCSV extends Importer {

  @Override
  protected ImportHistory process(String bind, String data, Map<String, Object> importContext)
      throws IOException {

    CSVImporter importer = new CSVImporter(bind, data);

    ImporterListener listener = new ImporterListener(getConfiguration().getName());
    importer.addListener(listener);
    importer.setContext(importContext);
    importer.run();

    return addHistory(listener);
  }

  @Override
  protected ImportHistory process(String bind, String data) throws IOException {
    return process(bind, data, null);
  }
}
