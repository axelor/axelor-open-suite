/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.importer;

import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.axelor.studio.service.ConfigurationService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ModuleImporterService {

  @Inject private ConfigurationService configService;

  @Inject private MetaModuleRepository metaModuleRepo;

  @Transactional
  public void createModules(DataReaderService reader, String key) {

    if (key == null || reader == null) {
      return;
    }

    int totalLines = reader.getTotalLines(key);

    for (int rowNum = 0; rowNum < totalLines; rowNum++) {

      if (rowNum == 0) {
        continue;
      }

      String[] row = reader.read(key, rowNum);
      if (row == null) {
        continue;
      }

      String name = row[0];
      if (Strings.isNullOrEmpty(name)) {
        continue;
      }
      //      if (configService.getNonCustomizedModules().contains(name)) {
      //        continue;
      //      }
      MetaModule module = configService.getModule(name);
      if (module == null) {
        module = new MetaModule(name);
      }
      if (row[1] != null) {
        for (String moduleName : row[1].split(",")) {
          module.addDepend(metaModuleRepo.findByName(moduleName));
        }
      }

      module.setTitle(row[2]);
      module.setModuleVersion(row[3]);
      module.setDescription(row[4]);

      metaModuleRepo.save(module);
    }
  }
}
