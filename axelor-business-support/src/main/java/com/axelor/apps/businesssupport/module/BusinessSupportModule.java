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
package com.axelor.apps.businesssupport.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.businessproject.db.repo.ProjectTaskBusinessProjectRepository;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskBusinessProjectServiceImpl;
import com.axelor.apps.businessproject.service.projecttask.TaskTemplateBusinessProjectServiceImpl;
import com.axelor.apps.businesssupport.db.repo.ProjectAnnouncementBusinessSupportRepository;
import com.axelor.apps.businesssupport.db.repo.ProjectAnnouncementRepository;
import com.axelor.apps.businesssupport.db.repo.ProjectTaskBusinessSupportRepository;
import com.axelor.apps.businesssupport.service.ProjectActivityDashboardBusinessSupportService;
import com.axelor.apps.businesssupport.service.ProjectActivityDashboardServiceBusinessSupportImpl;
import com.axelor.apps.businesssupport.service.ProjectCreateTaskServiceSupportImpl;
import com.axelor.apps.businesssupport.service.ProjectDashboardBusinessSupportServiceImpl;
import com.axelor.apps.businesssupport.service.ProjectTaskBusinessSupportServiceImpl;
import com.axelor.apps.businesssupport.service.TaskTemplateBusinessSupportServiceImpl;
import com.axelor.apps.hr.service.project.ProjectActivityDashboardServiceHRImpl;
import com.axelor.apps.hr.service.project.ProjectDashboardHRServiceImpl;
import com.axelor.apps.project.service.ProjectCreateTaskServiceImpl;

public class BusinessSupportModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(ProjectTaskBusinessProjectServiceImpl.class)
        .to(ProjectTaskBusinessSupportServiceImpl.class);
    bind(ProjectTaskBusinessProjectRepository.class).to(ProjectTaskBusinessSupportRepository.class);
    bind(ProjectCreateTaskServiceImpl.class).to(ProjectCreateTaskServiceSupportImpl.class);
    bind(ProjectDashboardHRServiceImpl.class).to(ProjectDashboardBusinessSupportServiceImpl.class);
    bind(ProjectAnnouncementRepository.class)
        .to(ProjectAnnouncementBusinessSupportRepository.class);
    bind(ProjectActivityDashboardServiceHRImpl.class)
        .to(ProjectActivityDashboardServiceBusinessSupportImpl.class);
    bind(ProjectActivityDashboardBusinessSupportService.class)
        .to(ProjectActivityDashboardServiceBusinessSupportImpl.class);
    bind(TaskTemplateBusinessProjectServiceImpl.class)
        .to(TaskTemplateBusinessSupportServiceImpl.class);
  }
}
