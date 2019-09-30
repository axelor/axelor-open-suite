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
package com.axelor.apps.base.service.imports;

import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.repo.ImportConfigurationRepository;
import com.axelor.apps.base.service.imports.importer.FactoryImporter;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.Map;

public class ImportService {

  @Inject private FactoryImporter factoryImporter;

  @Inject private ImportConfigurationRepository importConfigRepo;

  public ImportHistory run(ImportConfiguration configuration) throws AxelorException, IOException {

    return factoryImporter.createImporter(importConfigRepo.find(configuration.getId())).run();
  }

  public ImportHistory run(ImportConfiguration configuration, Map<String, Object> config)
      throws AxelorException, IOException {

    return factoryImporter.createImporter(importConfigRepo.find(configuration.getId())).run(config);
  }
}
