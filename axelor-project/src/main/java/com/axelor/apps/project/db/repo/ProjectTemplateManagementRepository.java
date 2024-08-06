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
package com.axelor.apps.project.db.repo;

import com.axelor.apps.project.db.ProjectTemplate;
import com.google.common.base.Strings;

public class ProjectTemplateManagementRepository extends ProjectTemplateRepository {

  protected void setProjectTemplateFullName(ProjectTemplate entity) {
    String projectCode = (Strings.isNullOrEmpty(entity.getCode())) ? "" : entity.getCode() + " - ";
    entity.setFullName(projectCode + entity.getName());
  }

  @Override
  public ProjectTemplate save(ProjectTemplate entity) {
    this.setProjectTemplateFullName(entity);

    return super.save(entity);
  }
}
