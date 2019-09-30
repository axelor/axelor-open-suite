/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.imports.importer;

import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.service.imports.listener.ImporterListener;
import com.axelor.data.xml.XMLImporter;
import java.io.IOException;
import java.util.Map;

class ImporterXML extends Importer {

  @Override
  protected ImportHistory process(String bind, String data, Map<String, Object> importContext)
      throws IOException {

    XMLImporter importer = new XMLImporter(bind, data);

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
