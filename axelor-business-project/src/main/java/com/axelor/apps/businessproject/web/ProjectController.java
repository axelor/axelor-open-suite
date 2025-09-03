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
package com.axelor.apps.businessproject.web;

import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.BusinessProjectClosingControlService;
import com.axelor.apps.businessproject.service.BusinessProjectService;
import com.axelor.apps.businessproject.service.InvoicingProjectService;
import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.businessproject.service.ProjectHistoryService;
import com.axelor.apps.businessproject.service.analytic.ProjectAnalyticTemplateService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.businessproject.translation.ITranslation;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.studio.db.repo.AppBusinessProjectRepository;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ProjectController {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void generateQuotation(ActionRequest request, ActionResponse response) {
    try {
      Project project = request.getContext().asType(Project.class);
      SaleOrder order = Beans.get(ProjectBusinessService.class).generateQuotation(project);
      response.setView(
          ActionView.define(I18n.get("Sale quotation"))
              .model(SaleOrder.class.getName())
              .add("form", "sale-order-form")
              .param("forceTitle", "true")
              .context("_showRecord", String.valueOf(order.getId()))
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generatePurchaseQuotation(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    if (project.getId() != null) {
      response.setView(
          ActionView.define(I18n.get("Purchase Order"))
              .model(PurchaseOrder.class.getName())
              .add("form", "purchase-order-form")
              .add("grid", "purchase-order-quotation-grid")
              .param("search-filters", "purchase-order-filters")
              .context("_project", Beans.get(ProjectRepository.class).find(project.getId()))
              .map());
    }
  }

  public void countToInvoice(ActionRequest request, ActionResponse response) {

    Project project = request.getContext().asType(Project.class);

    int toInvoiceCount = 0;
    if (project.getId() != null) {
      toInvoiceCount = Beans.get(InvoicingProjectService.class).countToInvoice(project);
    }

    response.setValue("$toInvoiceCounter", toInvoiceCount);
  }

  public void showInvoicingProjects(ActionRequest request, ActionResponse response) {

    Project project = request.getContext().asType(Project.class);
    project = Beans.get(ProjectRepository.class).find(project.getId());

    response.setView(
        ActionView.define(I18n.get("Invoice Business Project"))
            .model(InvoicingProject.class.getName())
            .add("form", "invoicing-project-form")
            .param("forceEdit", "true")
            .context("_project", project)
            .map());
  }

  @ErrorException
  public void getPartnerData(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Project project = request.getContext().asType(Project.class);
    Partner partner = project.getClientPartner();

    project = Beans.get(BusinessProjectService.class).computePartnerData(project, partner);

    if (project != null) {
      response.setValue("analyticDistributionTemplate", project.getAnalyticDistributionTemplate());
      response.setValue("currency", project.getCurrency());
      response.setValue("priceList", project.getPriceList());

      response.setValue(
          "contactPartner",
          partner.getContactPartnerSet().size() == 1
              ? partner.getContactPartnerSet().iterator().next()
              : null);
    }
  }

  public void computeProjectTotals(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Project project = request.getContext().asType(Project.class);
    ProjectBusinessService projectBusinessService = Beans.get(ProjectBusinessService.class);
    projectBusinessService.computeProjectTotals(project);

    List<String> projectTaskList = projectBusinessService.checkPercentagesOver1000OnTasks(project);
    if (projectTaskList.isEmpty()) {
      response.setNotify(I18n.get(BusinessProjectExceptionMessage.PROJECT_UPDATE_TOTALS_SUCCESS));
    } else {
      response.setAlert(
          String.format(
                  I18n.get(ITranslation.PROJECT_TASK_FOLLOW_UP_VALUES_TOO_HIGH), projectTaskList)
              + I18n.get(BusinessProjectExceptionMessage.PROJECT_UPDATE_TOTALS_SUCCESS));
    }
    response.setReload(true);
  }

  public void getProjectTimeFollowUpData(ActionRequest request, ActionResponse response)
      throws AxelorException {
    String id = Optional.ofNullable(request.getData().get("id")).map(Object::toString).orElse("");

    if (StringUtils.isBlank(id)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          BusinessProjectExceptionMessage.PROJECT_REPORT_NO_ID_FOUND);
    }
    Map<String, Object> data =
        Beans.get(ProjectBusinessService.class)
            .processRequestToDisplayTimeReporting(Long.valueOf(id));
    response.setData(List.of(data));
  }

  public void getProjectFinancialFollowUpData(ActionRequest request, ActionResponse response)
      throws AxelorException {
    String id = Optional.ofNullable(request.getData().get("id")).map(Object::toString).orElse("");

    if (StringUtils.isBlank(id)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          BusinessProjectExceptionMessage.PROJECT_REPORT_NO_ID_FOUND);
    }

    Map<String, Object> data =
        Beans.get(ProjectBusinessService.class)
            .processRequestToDisplayFinancialReporting(Long.valueOf(id));
    response.setData(List.of(data));
  }

  public void getProjectHistoryData(ActionRequest request, ActionResponse response)
      throws AxelorException {
    String id = Optional.ofNullable(request.getData().get("id")).map(Object::toString).orElse("");

    if (StringUtils.isBlank(id)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          BusinessProjectExceptionMessage.PROJECT_REPORT_NO_ID_FOUND);
    }
    Map<String, Object> data =
        Beans.get(ProjectHistoryService.class)
            .processRequestToDisplayProjectHistory(Long.valueOf(id));
    response.setData(List.of(data));
  }

  public void checkProjectState(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Project project = request.getContext().asType(Project.class);
    project = JPA.find(Project.class, project.getId());

    Integer closingProjectRuleSelect =
        Beans.get(AppBusinessProjectService.class)
            .getAppBusinessProject()
            .getClosingProjectRuleSelect();

    String errorMessage =
        Beans.get(BusinessProjectClosingControlService.class).checkProjectState(project);
    if (errorMessage.isEmpty()) {
      return;
    }
    if (closingProjectRuleSelect == AppBusinessProjectRepository.CLOSING_PROJECT_RULE_BLOCKING) {
      response.setError(errorMessage, null, null, "action-refresh-record");
    } else if (closingProjectRuleSelect
        == AppBusinessProjectRepository.CLOSING_PROJECT_RULE_NON_BLOCKING) {
      response.setAlert(errorMessage, null, null, null, "action-refresh-record");
    }
  }

  public void setAnalyticDistributionTemplateRequired(
      ActionRequest request, ActionResponse response) {
    try {
      Project project = request.getContext().asType(Project.class);

      response.setAttr(
          "analyticDistributionTemplate",
          "required",
          Beans.get(ProjectAnalyticTemplateService.class)
              .isAnalyticDistributionTemplateRequired(project));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void getAnalyticDistributionTemplate(ActionRequest request, ActionResponse response) {
    try {
      Project project = request.getContext().asType(Project.class);
      response.setValue(
          "analyticDistributionTemplate",
          Beans.get(ProjectAnalyticTemplateService.class)
              .getDefaultAnalyticDistributionTemplate(project));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  @ErrorException
  public void convertProject(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Object projectId = request.getContext().get("_projectId");
    if (projectId == null) {
      return;
    }

    Project project = Beans.get(ProjectRepository.class).find(Long.valueOf(projectId.toString()));
    Company company = (Company) request.getContext().get("company");
    Partner clientPartner = (Partner) request.getContext().get("clientPartner");
    if (clientPartner == null || company == null) {
      response.setError(
          I18n.get(
              BusinessProjectExceptionMessage
                  .PROJECT_BUSINESS_PROJECT_MISSING_CLIENT_PARTNER_COMPANY));
    }

    Beans.get(BusinessProjectService.class).setAsBusinessProject(project, company, clientPartner);

    ActionView.ActionViewBuilder builder =
        ActionView.define(I18n.get("Business project"))
            .model(Project.class.getName())
            .add("grid", "project-grid")
            .add("form", "business-project-form")
            .add("kanban", "project-kanban")
            .param("search-filters", "project-project-filters")
            .context("_showRecord", project.getId())
            .domain("self.isBusinessProject = true");
    response.setCanClose(true);
    response.setView(builder.map());
  }

  @ErrorException
  public void setDomainAnalyticDistributionTemplate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    Project project = context.asType(Project.class);

    response.setAttr(
        "analyticDistributionTemplate",
        "domain",
        Beans.get(AnalyticAttrsService.class)
            .getAnalyticDistributionTemplateDomain(
                null, null, project.getCompany(), null, null, false));
  }
}
