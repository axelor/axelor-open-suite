/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTemplate;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;

public interface ProjectService {
  Project generateProject(
      Project parentProject,
      String fullName,
      User assignedTo,
      Company company,
      Partner customerPartner);

  Partner getCustomerPartnerFromProject(Project project) throws AxelorException;

  BigDecimal computeDurationFromChildren(Long projectId);

  /**
   * Generate a project from a partner.
   *
   * @param partner
   * @return
   */
  Project generateProject(Partner partner);

  public Project createProjectFromTemplate(
      ProjectTemplate projectTemplate, String projectCode, Partner customerPartner)
      throws AxelorException;
}
