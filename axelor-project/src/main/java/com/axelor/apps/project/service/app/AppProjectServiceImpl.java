/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.service.app;

import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.meta.MetaFiles;
import com.axelor.studio.app.service.AppVersionService;
import com.axelor.studio.db.AppProject;
import com.axelor.studio.db.repo.AppProjectRepository;
import com.axelor.studio.db.repo.AppRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppProjectServiceImpl extends AppBaseServiceImpl implements AppProjectService {

  protected AppProjectRepository appProjectRepo;

  @Inject
  public AppProjectServiceImpl(
      AppRepository appRepo,
      MetaFiles metaFiles,
      AppVersionService appVersionService,
      AppProjectRepository appProjectRepo) {
    super(appRepo, metaFiles, appVersionService);
    this.appProjectRepo = appProjectRepo;
  }

  @Override
  public AppProject getAppProject() {
    return appProjectRepo.all().fetchOne();
  }
}
