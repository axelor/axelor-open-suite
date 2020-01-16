/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.quality.db.repo.QualityAlertManagementRepository;
import com.axelor.apps.quality.db.repo.QualityAlertRepository;
import com.axelor.apps.quality.db.repo.QualityControlManagementRepository;
import com.axelor.apps.quality.db.repo.QualityControlRepository;
import com.axelor.apps.quality.service.QualityControlService;
import com.axelor.apps.quality.service.QualityControlServiceImpl;
import com.axelor.apps.quality.service.app.AppQualityService;
import com.axelor.apps.quality.service.app.AppQualityServiceImpl;

public class QualityModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(QualityControlService.class).to(QualityControlServiceImpl.class);
    bind(QualityAlertRepository.class).to(QualityAlertManagementRepository.class);
    bind(QualityControlRepository.class).to(QualityControlManagementRepository.class);
    bind(AppQualityService.class).to(AppQualityServiceImpl.class);
  }
}
