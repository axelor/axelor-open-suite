/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.web;

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.ProjectAnalyticMoveLineService;
import com.axelor.apps.businessproject.service.projectgenerator.ProjectGeneratorFactory;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectGeneratorType;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Singleton
public class SaleOrderProjectController {

  private static final String CONTEXT_SHOW_RECORD = "_showRecord";

  public void generateProject(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
      if (saleOrder.getSaleOrderLineList() == null || saleOrder.getSaleOrderLineList().isEmpty()) {
        response.setAlert(
            I18n.get(BusinessProjectExceptionMessage.SALE_ORDER_GENERATE_FILL_PROJECT_ERROR_2));
        return;
      }
      String generatorType = (String) request.getContext().get("_projectGeneratorType");
      LocalDateTime startDate = getElementStartDate(request.getContext());

      ProjectGeneratorType projectGeneratorType = ProjectGeneratorType.valueOf(generatorType);

      ProjectGeneratorFactory factory = ProjectGeneratorFactory.getFactory(projectGeneratorType);

      Project project;
      if (projectGeneratorType.equals(ProjectGeneratorType.PROJECT_ALONE)) {
        project = factory.create(saleOrder);
      } else {
        project = factory.generate(saleOrder, startDate);
      }

      response.setReload(true);
      response.setView(
          ActionView.define(I18n.get("Project"))
              .model(Project.class.getName())
              .add("form", "project-form")
              .param("forceEdit", "true")
              .context(CONTEXT_SHOW_RECORD, String.valueOf(project.getId()))
              .map());

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
      response.setReload(true);
    }
  }

  public void fillProject(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
      if (saleOrder.getSaleOrderLineList() == null
          || (saleOrder.getSaleOrderLineList() != null
              && saleOrder.getSaleOrderLineList().isEmpty())) {
        response.setAlert(
            I18n.get(BusinessProjectExceptionMessage.SALE_ORDER_GENERATE_FILL_PROJECT_ERROR_2));
        return;
      }
      String generatorType = (String) request.getContext().get("_projectGeneratorType");
      LocalDateTime startDate = getElementStartDate(request.getContext());

      ProjectGeneratorFactory factory =
          ProjectGeneratorFactory.getFactory(ProjectGeneratorType.valueOf(generatorType));
      ActionViewBuilder view = factory.fill(saleOrder.getProject(), saleOrder, startDate);

      response.setReload(true);
      response.setView(view.map());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
      response.setReload(true);
    }
  }

  public void updateLines(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
      saleOrder = Beans.get(ProjectAnalyticMoveLineService.class).updateLines(saleOrder);
      response.setValue("saleOrderLineList", saleOrder.getSaleOrderLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected LocalDateTime getElementStartDate(Context context) {
    LocalDateTime date;
    String stringStartDate = (String) context.get("_elementStartDate");
    if (!Strings.isNullOrEmpty(stringStartDate)) {
      date = LocalDateTime.ofInstant(Instant.parse(stringStartDate), ZoneId.systemDefault());
    } else {
      date =
          Beans.get(AppBaseService.class)
              .getTodayDate(
                  Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
              .atStartOfDay();
    }
    return date;
  }
}
