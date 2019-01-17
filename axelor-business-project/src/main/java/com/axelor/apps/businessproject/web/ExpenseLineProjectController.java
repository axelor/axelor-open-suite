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
package com.axelor.apps.businessproject.web;

import com.axelor.apps.businessproject.exception.IExceptionMessage;
import com.axelor.apps.businessproject.service.ExpenseLineProjectService;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpenseLineProjectController {

  @Inject private ExpenseLineProjectService expenseLineProjectService;

  @Inject private ProjectRepository projectRepository;

  @Inject private ExpenseLineRepository expenseLineRepo;

  /**
   * Set project from context selected lines
   *
   * @param request
   * @param response
   */
  public void setProject(ActionRequest request, ActionResponse response) {

    try {

      Project project = request.getContext().asType(Project.class);
      project = projectRepository.find(project.getId());

      setProject(request, response, project);

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  private void setProject(ActionRequest request, ActionResponse response, Project project) {

    List<Map<String, Object>> expenseLineSet =
        (List<Map<String, Object>>) request.getContext().get("expenseLineSet");

    if (expenseLineSet == null || expenseLineSet.isEmpty()) {
      response.setFlash(IExceptionMessage.LINES_NOT_SELECTED);
    } else {
      List<Long> lineIds =
          expenseLineSet
              .stream()
              .map(it -> Long.parseLong(it.get("id").toString()))
              .collect(Collectors.toList());
      expenseLineProjectService.setProject(lineIds, project);
      response.setAttr("$expenseLineSet", "hidden", true);
      response.setAttr("addSelectedExpenseLinesBtn", "hidden", true);
      response.setAttr("unlinkSelectedExpenseLinesBtn", "hidden", true);
      response.setAttr("cancelManageExpenseLinesBtn", "hidden", true);
      response.setAttr("expenseLinePanel", "refresh", true);
      response.setAttr("expensePanel", "refresh", true);
      response.setAttr("selectNewExpenseLinesBtn", "readonly", false);
      response.setAttr("manageExpenseLinesBtn", "readonly", false);
    }
  }

  /**
   * Remove project from selected lines
   *
   * @param request
   * @param response
   */
  public void unsetProject(ActionRequest request, ActionResponse response) {

    try {
      setProject(request, response, null);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  /**
   * Invert value of 'toInvoice' field and save the record
   *
   * @param request
   * @param response
   */
  @Transactional
  public void updateToInvoice(ActionRequest request, ActionResponse response) {
    try {
      ExpenseLine expenseLine = request.getContext().asType(ExpenseLine.class);
      expenseLine = expenseLineRepo.find(expenseLine.getId());
      expenseLine.setToInvoice(!expenseLine.getToInvoice());
      expenseLineRepo.save(expenseLine);
      response.setValue("toInvoice", expenseLine.getToInvoice());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
