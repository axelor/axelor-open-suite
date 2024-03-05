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
