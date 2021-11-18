/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businesssupport.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.businessproject.db.repo.ProjectTaskBusinessProjectRepository;
import com.axelor.apps.businessproject.service.ProjectBusinessServiceImpl;
import com.axelor.apps.businessproject.service.ProjectTaskBusinessProjectServiceImpl;
import com.axelor.apps.businesssupport.db.repo.ProjectTaskBusinessSupportRepository;
import com.axelor.apps.businesssupport.service.ProjectBusinessSupportServiceImpl;
import com.axelor.apps.businesssupport.service.ProjectTaskBusinessSupportServiceImpl;

public class BusinessSupportModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(ProjectTaskBusinessProjectServiceImpl.class)
        .to(ProjectTaskBusinessSupportServiceImpl.class);
    bind(ProjectTaskBusinessProjectRepository.class).to(ProjectTaskBusinessSupportRepository.class);
    bind(ProjectBusinessServiceImpl.class).to(ProjectBusinessSupportServiceImpl.class);
  }
}
