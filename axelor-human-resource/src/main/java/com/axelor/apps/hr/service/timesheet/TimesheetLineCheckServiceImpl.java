package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.project.db.Project;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class TimesheetLineCheckServiceImpl implements TimesheetLineCheckService {
  protected AppHumanResourceService appHumanResourceService;

  @Inject
  public TimesheetLineCheckServiceImpl(AppHumanResourceService appHumanResourceService) {
    this.appHumanResourceService = appHumanResourceService;
  }

  @Override
  public void checkActivity(Project project, Product product) throws AxelorException {
    if (product == null) {
      return;
    }
    if (!appHumanResourceService.getAppTimesheet().getEnableActivity()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_ACTIVITY_NOT_ENABLED));
    }
    if (!product.getIsActivity()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_PRODUCT_NOT_ACTIVITY));
    }
    if (project != null) {
      Set<Product> productSet = project.getProductSet();
      if (CollectionUtils.isNotEmpty(productSet) && !productSet.contains(product)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(HumanResourceExceptionMessage.TIMESHEET_ACTIVITY_NOT_ALLOWED));
      }
    }
  }
}
