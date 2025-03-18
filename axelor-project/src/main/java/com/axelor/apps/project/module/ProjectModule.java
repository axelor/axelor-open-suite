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
package com.axelor.apps.project.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.project.db.repo.ProjectManagementRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskLinkTypeManagementRepository;
import com.axelor.apps.project.db.repo.ProjectTaskLinkTypeRepository;
import com.axelor.apps.project.db.repo.ProjectTaskProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.db.repo.ProjectTemplateManagementRepository;
import com.axelor.apps.project.db.repo.ProjectTemplateRepository;
import com.axelor.apps.project.db.repo.TaskTemplateManagementRepository;
import com.axelor.apps.project.db.repo.TaskTemplateRepository;
import com.axelor.apps.project.db.repo.TeamProjectRepository;
import com.axelor.apps.project.db.repo.WikiProjectRepository;
import com.axelor.apps.project.db.repo.WikiRepository;
import com.axelor.apps.project.quickmenu.ActiveProjectQuickMenuCreator;
import com.axelor.apps.project.service.MetaJsonFieldProjectService;
import com.axelor.apps.project.service.MetaJsonFieldProjectServiceImpl;
import com.axelor.apps.project.service.ProjectActivityDashboardService;
import com.axelor.apps.project.service.ProjectActivityDashboardServiceImpl;
import com.axelor.apps.project.service.ProjectCheckListTemplateService;
import com.axelor.apps.project.service.ProjectCheckListTemplateServiceImpl;
import com.axelor.apps.project.service.ProjectCreateTaskService;
import com.axelor.apps.project.service.ProjectCreateTaskServiceImpl;
import com.axelor.apps.project.service.ProjectDashboardService;
import com.axelor.apps.project.service.ProjectDashboardServiceImpl;
import com.axelor.apps.project.service.ProjectMenuService;
import com.axelor.apps.project.service.ProjectMenuServiceImpl;
import com.axelor.apps.project.service.ProjectService;
import com.axelor.apps.project.service.ProjectServiceImpl;
import com.axelor.apps.project.service.ProjectTaskAttrsService;
import com.axelor.apps.project.service.ProjectTaskAttrsServiceImpl;
import com.axelor.apps.project.service.ProjectTaskCategoryService;
import com.axelor.apps.project.service.ProjectTaskCategoryServiceImpl;
import com.axelor.apps.project.service.ProjectTaskComputeService;
import com.axelor.apps.project.service.ProjectTaskComputeServiceImpl;
import com.axelor.apps.project.service.ProjectTaskGroupService;
import com.axelor.apps.project.service.ProjectTaskGroupServiceImpl;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.apps.project.service.ProjectTaskServiceImpl;
import com.axelor.apps.project.service.ProjectTaskToolService;
import com.axelor.apps.project.service.ProjectTaskToolServiceImpl;
import com.axelor.apps.project.service.ProjectTemplateService;
import com.axelor.apps.project.service.ProjectTemplateServiceImpl;
import com.axelor.apps.project.service.ProjectTimeUnitService;
import com.axelor.apps.project.service.ProjectTimeUnitServiceImpl;
import com.axelor.apps.project.service.ProjectToolService;
import com.axelor.apps.project.service.ProjectToolServiceImpl;
import com.axelor.apps.project.service.ResourceBookingService;
import com.axelor.apps.project.service.ResourceBookingServiceImpl;
import com.axelor.apps.project.service.TaskStatusProgressByCategoryService;
import com.axelor.apps.project.service.TaskStatusProgressByCategoryServiceImpl;
import com.axelor.apps.project.service.TaskStatusToolService;
import com.axelor.apps.project.service.TaskStatusToolServiceImpl;
import com.axelor.apps.project.service.TaskTemplateService;
import com.axelor.apps.project.service.TaskTemplateServiceImpl;
import com.axelor.apps.project.service.TimerProjectTaskService;
import com.axelor.apps.project.service.TimerProjectTaskServiceImpl;
import com.axelor.apps.project.service.UnitConversionForProjectService;
import com.axelor.apps.project.service.UnitConversionForProjectServiceImpl;
import com.axelor.apps.project.service.UserProjectService;
import com.axelor.apps.project.service.UserProjectServiceImpl;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.project.service.app.AppProjectServiceImpl;
import com.axelor.apps.project.service.batch.ProjectBatchInitService;
import com.axelor.apps.project.service.batch.ProjectBatchInitServiceImpl;
import com.axelor.apps.project.service.config.ProjectConfigService;
import com.axelor.apps.project.service.config.ProjectConfigServiceImpl;
import com.axelor.apps.project.service.dashboard.ProjectManagementDashboardService;
import com.axelor.apps.project.service.dashboard.ProjectManagementDashboardServiceImpl;
import com.axelor.apps.project.service.roadmap.ProjectVersionRemoveService;
import com.axelor.apps.project.service.roadmap.ProjectVersionRemoveServiceImpl;
import com.axelor.apps.project.service.roadmap.ProjectVersionService;
import com.axelor.apps.project.service.roadmap.ProjectVersionServiceImpl;
import com.axelor.apps.project.service.roadmap.SprintGeneratorService;
import com.axelor.apps.project.service.roadmap.SprintGeneratorServiceImpl;
import com.axelor.apps.project.service.roadmap.SprintGetService;
import com.axelor.apps.project.service.roadmap.SprintGetServiceImpl;
import com.axelor.apps.project.service.roadmap.SprintService;
import com.axelor.apps.project.service.roadmap.SprintServiceImpl;
import com.axelor.apps.project.service.taskLink.ProjectTaskLinkService;
import com.axelor.apps.project.service.taskLink.ProjectTaskLinkServiceImpl;
import com.axelor.apps.project.service.taskLink.ProjectTaskLinkTypeService;
import com.axelor.apps.project.service.taskLink.ProjectTaskLinkTypeServiceImpl;
import com.axelor.team.db.repo.TeamRepository;

public class ProjectModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(ProjectRepository.class).to(ProjectManagementRepository.class);
    bind(ProjectTemplateRepository.class).to(ProjectTemplateManagementRepository.class);
    bind(AppProjectService.class).to(AppProjectServiceImpl.class);
    bind(ProjectTaskRepository.class).to(ProjectTaskProjectRepository.class);
    bind(ProjectService.class).to(ProjectServiceImpl.class);
    bind(ProjectTaskService.class).to(ProjectTaskServiceImpl.class);
    bind(TeamRepository.class).to(TeamProjectRepository.class);
    bind(TimerProjectTaskService.class).to(TimerProjectTaskServiceImpl.class);
    bind(MetaJsonFieldProjectService.class).to(MetaJsonFieldProjectServiceImpl.class);
    bind(ProjectMenuService.class).to(ProjectMenuServiceImpl.class);
    bind(TaskTemplateService.class).to(TaskTemplateServiceImpl.class);
    bind(ProjectTemplateService.class).to(ProjectTemplateServiceImpl.class);
    bind(TaskTemplateRepository.class).to(TaskTemplateManagementRepository.class);
    bind(ResourceBookingService.class).to(ResourceBookingServiceImpl.class);
    bind(ProjectDashboardService.class).to(ProjectDashboardServiceImpl.class);
    bind(ProjectActivityDashboardService.class).to(ProjectActivityDashboardServiceImpl.class);
    bind(WikiRepository.class).to(WikiProjectRepository.class);
    bind(ProjectCreateTaskService.class).to(ProjectCreateTaskServiceImpl.class);
    bind(ProjectConfigService.class).to(ProjectConfigServiceImpl.class);
    bind(ProjectTaskLinkService.class).to(ProjectTaskLinkServiceImpl.class);
    bind(ProjectTaskLinkTypeService.class).to(ProjectTaskLinkTypeServiceImpl.class);
    bind(ProjectTaskLinkTypeRepository.class).to(ProjectTaskLinkTypeManagementRepository.class);
    bind(TaskStatusToolService.class).to(TaskStatusToolServiceImpl.class);
    bind(ProjectTaskToolService.class).to(ProjectTaskToolServiceImpl.class);
    bind(ProjectTaskCategoryService.class).to(ProjectTaskCategoryServiceImpl.class);
    bind(TaskStatusProgressByCategoryService.class)
        .to(TaskStatusProgressByCategoryServiceImpl.class);
    bind(UserProjectService.class).to(UserProjectServiceImpl.class);
    addQuickMenu(ActiveProjectQuickMenuCreator.class);
    bind(ProjectToolService.class).to(ProjectToolServiceImpl.class);
    bind(ProjectTaskAttrsService.class).to(ProjectTaskAttrsServiceImpl.class);
    bind(ProjectCheckListTemplateService.class).to(ProjectCheckListTemplateServiceImpl.class);
    bind(ProjectTimeUnitService.class).to(ProjectTimeUnitServiceImpl.class);
    bind(ProjectVersionRemoveService.class).to(ProjectVersionRemoveServiceImpl.class);
    bind(SprintService.class).to(SprintServiceImpl.class);
    bind(SprintGeneratorService.class).to(SprintGeneratorServiceImpl.class);
    bind(SprintGetService.class).to(SprintGetServiceImpl.class);
    bind(ProjectVersionService.class).to(ProjectVersionServiceImpl.class);
    bind(ProjectBatchInitService.class).to(ProjectBatchInitServiceImpl.class);
    bind(ProjectManagementDashboardService.class).to(ProjectManagementDashboardServiceImpl.class);
    bind(ProjectTaskGroupService.class).to(ProjectTaskGroupServiceImpl.class);
    bind(ProjectTaskComputeService.class).to(ProjectTaskComputeServiceImpl.class);
    bind(UnitConversionForProjectService.class).to(UnitConversionForProjectServiceImpl.class);
  }
}
