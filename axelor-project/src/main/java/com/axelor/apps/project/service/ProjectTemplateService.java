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
import com.axelor.apps.project.db.ProjectTemplate;
import java.util.Map;

public interface ProjectTemplateService {

  ProjectTemplate addParentTaskTemplate(ProjectTemplate projectTemplate);

  public Map<String, Object> createProjectFromTemplateView(ProjectTemplate projectTemplate)
      throws AxelorException;

  boolean isWizardNeeded(ProjectTemplate projectTemplate);

  Map<String, Object> computeWizardContext(ProjectTemplate projectTemplate);
}
