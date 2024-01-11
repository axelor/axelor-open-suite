/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectStatus;
import com.axelor.apps.project.db.ProjectTemplate;
import com.axelor.auth.db.User;
import com.axelor.meta.CallMethod;
import java.util.Map;
import java.util.Set;

public interface ProjectService {
  Project generateProject(
      Project parentProject,
      String fullName,
      User assignedTo,
      Company company,
      Partner clientPartner);

  /**
   * Generate a project from a partner.
   *
   * @param partner
   * @return
   */
  Project generateProject(Partner partner);

  public Project createProjectFromTemplate(
      ProjectTemplate projectTemplate, String projectCode, Partner clientPartner)
      throws AxelorException;

  public Map<String, Object> createProjectFromTemplateView(ProjectTemplate projectTemplate)
      throws AxelorException;

  public Map<String, Object> getTaskView(
      Project project, String title, String domain, Map<String, Object> context);

  public Project generateProject(
      ProjectTemplate projectTemplate, String projectCode, Partner clientPartner);

  public Map<String, Object> getPerStatusKanban(Project project, Map<String, Object> context);

  public String getTimeZone(Project project);

  public ProjectStatus getDefaultProjectStatus();

  boolean checkIfResourceBooked(Project project);

  public void getChildProjectIds(Set<Long> projectIdsSet, Project project);

  @CallMethod
  public Set<Long> getContextProjectIds();

  @CallMethod
  public String getContextProjectIdsString();
}
