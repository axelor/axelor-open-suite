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
package com.axelor.apps.base.service.imports;

import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.repo.ImportConfigurationRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ImportConfigurationServiceImpl implements ImportConfigurationService {

  protected ImportConfigurationRepository importConfigurationRepo;
  protected AppBaseService appBaseService;

  @Inject
  public ImportConfigurationServiceImpl(
      ImportConfigurationRepository importConfigurationRepo, AppBaseService appBaseService) {
    this.importConfigurationRepo = importConfigurationRepo;
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void updateStatusCompleted(
      ImportConfiguration configuration, ImportHistory importHistory) {
    configuration = importConfigurationRepo.find(configuration.getId());
    configuration.addImportHistoryListItem(importHistory);
    configuration.setEndDateTime(appBaseService.getTodayDateTime().toLocalDateTime());
    configuration.setStatusSelect(ImportConfigurationRepository.STATUS_COMPLETED);
    importConfigurationRepo.save(configuration);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void updateStatusError(ImportConfiguration configuration) {
    configuration.setStatusSelect(ImportConfigurationRepository.STATUS_ERROR);
    importConfigurationRepo.save(configuration);
  }
}
