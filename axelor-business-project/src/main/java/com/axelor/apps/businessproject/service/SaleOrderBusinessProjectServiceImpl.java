package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.businessproject.exception.IExceptionMessage;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.businessproject.service.projectgenerator.ProjectGeneratorFactory;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectGeneratorType;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;

public class SaleOrderBusinessProjectServiceImpl implements SaleOrderBusinessProjectService {

  protected AppBaseService appBaseService;
  protected AppBusinessProjectService appBusinessProjectService;

  @Inject
  public SaleOrderBusinessProjectServiceImpl(
      AppBaseService appBaseService, AppBusinessProjectService appBusinessProjectService) {
    this.appBaseService = appBaseService;
    this.appBusinessProjectService = appBusinessProjectService;
  }

  @Override
  public Project generateProject(SaleOrder saleOrder) throws AxelorException {

    if (saleOrder.getSaleOrderLineList() == null || saleOrder.getSaleOrderLineList().isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.SALE_ORDER_GENERATE_FILL_PROJECT_ERROR_2));
    }
    LocalDateTime startDate = getElementStartDate(saleOrder);

    ProjectGeneratorType projectGeneratorType = saleOrder.getProjectGeneratorType();

    ProjectGeneratorFactory factory = ProjectGeneratorFactory.getFactory(projectGeneratorType);

    Project project;
    if (projectGeneratorType.equals(ProjectGeneratorType.PROJECT_ALONE)) {
      project = factory.create(saleOrder);
    } else {
      project = factory.generate(saleOrder, startDate);
    }

    return project;
  }

  @Override
  public LocalDateTime getElementStartDate(SaleOrder saleOrder) {
    LocalDateTime startDate = saleOrder.getElementStartDate();

    if (startDate == null) {
      return appBaseService
          .getTodayDate(
              Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
          .atStartOfDay();
    }
    return startDate;
  }
}
