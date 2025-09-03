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
package com.axelor.apps.project.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.common.StringUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.google.inject.Inject;

public class ProjectNameComputeServiceImpl implements ProjectNameComputeService {

  protected AppProjectService appProjectService;

  @Inject
  public ProjectNameComputeServiceImpl(AppProjectService appProjectService) {
    this.appProjectService = appProjectService;
  }

  @Override
  public String setProjectFullName(Project project) throws AxelorException {
    Context scriptContext = new Context(Mapper.toMap(project), Project.class);
    GroovyScriptHelper groovyScriptHelper = new GroovyScriptHelper(scriptContext);

    String fullNameGroovyFormula = appProjectService.getAppProject().getFullNameGroovyFormula();
    if (StringUtils.isBlank(fullNameGroovyFormula)) {
      fullNameGroovyFormula = "code +\"-\"+ name";
    }
    try {
      Object result = groovyScriptHelper.eval(fullNameGroovyFormula);
      if (result == null) {
        throw new AxelorException(
            project,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(ProjectExceptionMessage.PROJECT_GROOVY_FORMULA_ERROR));
      }
      return result.toString();
    } catch (Exception e) {
      throw new AxelorException(
          e,
          project,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProjectExceptionMessage.PROJECT_GROOVY_FORMULA_SYNTAX_ERROR));
    }
  }
}
