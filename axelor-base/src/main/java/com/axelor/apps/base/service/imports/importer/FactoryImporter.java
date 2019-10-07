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

import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.repo.ImportConfigurationRepository;
import com.axelor.inject.Beans;
import java.io.File;

public class FactoryImporter {

  public Importer createImporter(ImportConfiguration importConfiguration) {

    Importer importer;

    if (importConfiguration.getTypeSelect().equals(ImportConfigurationRepository.TYPE_XML)) {
      importer = Beans.get(ImporterXML.class);
    } else {
      importer = Beans.get(ImporterCSV.class);
    }

    return importer.init(importConfiguration);
  }

  public Importer createImporter(ImportConfiguration importConfiguration, File workspace) {

    Importer importer;

    if (importConfiguration.getTypeSelect().equals(ImportConfigurationRepository.TYPE_XML)) {
      importer = Beans.get(ImporterXML.class);
    } else {
      importer = Beans.get(ImporterCSV.class);
    }

    return importer.init(importConfiguration, workspace);
  }
}
