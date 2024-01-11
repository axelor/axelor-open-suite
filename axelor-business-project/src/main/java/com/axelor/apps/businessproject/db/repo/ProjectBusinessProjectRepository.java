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
package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.hr.db.repo.ProjectHRRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppBusinessProject;
import javax.persistence.PersistenceException;

public class ProjectBusinessProjectRepository extends ProjectHRRepository {

  @Override
  public Project save(Project project) {
    try {
      AppBusinessProject appBusinessProject =
          Beans.get(AppBusinessProjectService.class).getAppBusinessProject();

      if (StringUtils.isBlank(project.getCode())
          && appBusinessProject.getGenerateProjectSequence()) {
        Company company = project.getCompany();
        String seq =
            Beans.get(SequenceService.class)
                .getSequenceNumber(
                    SequenceRepository.PROJECT_SEQUENCE, company, Project.class, "code");

        if (seq == null) {
          throw new AxelorException(
              company,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BusinessProjectExceptionMessage.PROJECT_SEQUENCE_ERROR),
              company.getName());
        }

        project.setCode(seq);
      }
    } catch (AxelorException e) {
      throw new PersistenceException(e.getMessage(), e);
    }
    return super.save(project);
  }
}
