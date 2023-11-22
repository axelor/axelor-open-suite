/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.bomimport;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.repo.ImportConfigurationRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.imports.ImportService;
import com.axelor.apps.production.db.BillOfMaterialImport;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;

public class BillOfMaterialImportServiceImpl implements BillOfMaterialImportService {

  protected final AppBaseService appBaseService;
  protected final ImportService importService;
  protected final ImportConfigurationRepository importConfigurationRepository;
  protected final BillOfMaterialImporter billOfMaterialImporter;

  @Inject
  public BillOfMaterialImportServiceImpl(
      AppBaseService appBaseService,
      ImportService importService,
      ImportConfigurationRepository importConfigurationRepository,
      BillOfMaterialImporter billOfMaterialImporter) {
    this.appBaseService = appBaseService;
    this.importService = importService;
    this.importConfigurationRepository = importConfigurationRepository;
    this.billOfMaterialImporter = billOfMaterialImporter;
  }

  @Override
  public ImportHistory processImport(BillOfMaterialImport billOfMaterialImport)
      throws AxelorException, IOException {
    billOfMaterialImporter.addBillOfMaterialImport(billOfMaterialImport);
    return billOfMaterialImporter.init(createImportConfiguration(billOfMaterialImport)).run();
  }

  @Transactional
  protected ImportConfiguration createImportConfiguration(
      BillOfMaterialImport billOfMaterialImport) {
    ImportConfiguration importConfiguration = new ImportConfiguration();

    importConfiguration.setUser(AuthUtils.getUser());
    importConfiguration.setName(billOfMaterialImport.getName());
    importConfiguration.setBindMetaFile(billOfMaterialImport.getImportSource().getBindingFile());
    importConfiguration.setDataMetaFile(billOfMaterialImport.getImportMetaFile());
    importConfiguration.setStartDateTime(appBaseService.getTodayDateTime().toLocalDateTime());

    return importConfigurationRepository.save(importConfiguration);
  }
}
