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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.repo.ImportConfigurationRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.io.File;

public class FactoryImporter {

  public Importer createImporter(ImportConfiguration importConfiguration) throws AxelorException {
    Importer importer = getImporter(importConfiguration);
    return importer.init(importConfiguration);
  }

  public Importer createImporter(ImportConfiguration importConfiguration, File workspace)
      throws AxelorException {
    Importer importer = getImporter(importConfiguration);
    return importer.init(importConfiguration, workspace);
  }

  protected Importer getImporter(ImportConfiguration importConfiguration) throws AxelorException {
    switch (importConfiguration.getTypeSelect()) {
      case ImportConfigurationRepository.TYPE_XML:
        return Beans.get(ImporterXML.class);
      case ImportConfigurationRepository.TYPE_CSV:
        return Beans.get(ImporterCSV.class);
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(BaseExceptionMessage.IMPORT_CONFIGURATION_TYPE_MISSING));
    }
  }
}
