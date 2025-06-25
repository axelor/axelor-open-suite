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
package com.axelor.apps.talent.service;

import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.axelor.meta.loader.AppVersionService;
import com.axelor.studio.db.AppRecruitment;
import com.axelor.studio.db.repo.AppRecruitmentRepository;
import com.axelor.studio.db.repo.AppRepository;
import com.axelor.studio.service.AppSettingsStudioService;
import com.google.inject.Inject;

public class AppTalentServiceImpl extends AppBaseServiceImpl implements AppTalentService {
  protected AppRecruitmentRepository appRecruitmentRepository;

  @Inject
  public AppTalentServiceImpl(
      AppRepository appRepo,
      MetaFiles metaFiles,
      AppVersionService appVersionService,
      AppSettingsStudioService appSettingsService,
      MetaModuleRepository metaModuleRepo,
      MetaFileRepository metaFileRepo,
      AppRecruitmentRepository appRecruitmentRepository) {
    super(appRepo, metaFiles, appVersionService, appSettingsService, metaModuleRepo, metaFileRepo);
    this.appRecruitmentRepository = appRecruitmentRepository;
  }

  @Override
  public AppRecruitment getAppRecruitment() {
    return appRecruitmentRepository.all().fetchOne();
  }
}
