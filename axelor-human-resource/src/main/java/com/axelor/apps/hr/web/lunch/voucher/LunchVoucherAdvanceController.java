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
package com.axelor.apps.hr.web.lunch.voucher;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LunchVoucherAdvance;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.report.IReport;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherAdvanceService;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherMgtService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Singleton
public class LunchVoucherAdvanceController {

  public void checkOnNewAdvance(ActionRequest request, ActionResponse response)
      throws AxelorException {
    LunchVoucherAdvance lunchVoucherAdvance =
        EntityHelper.getEntity(request.getContext().asType(LunchVoucherAdvance.class));

    if (lunchVoucherAdvance.getEmployee().getMainEmploymentContract() == null) {
      response.setError(
          String.format(
              I18n.get(HumanResourceExceptionMessage.EMPLOYEE_CONTRACT_OF_EMPLOYMENT),
              lunchVoucherAdvance.getEmployee().getName()));
      return;
    }
    Company company = lunchVoucherAdvance.getEmployee().getMainEmploymentContract().getPayCompany();
    HRConfig hrConfig = Beans.get(HRConfigService.class).getHRConfig(company);
    int stock =
        Beans.get(LunchVoucherMgtService.class)
            .checkStock(company, lunchVoucherAdvance.getNbrLunchVouchers());

    if (stock <= 0) {
      response.setAlert(
          String.format(
              I18n.get(HumanResourceExceptionMessage.LUNCH_VOUCHER_MIN_STOCK),
              company.getName(),
              hrConfig.getMinStockLunchVoucher(),
              hrConfig.getAvailableStockLunchVoucher(),
              TraceBackRepository.CATEGORY_INCONSISTENCY));
    }
  }

  public void onNewAdvance(ActionRequest request, ActionResponse response) {
    LunchVoucherAdvance lunchVoucherAdvance =
        EntityHelper.getEntity(request.getContext().asType(LunchVoucherAdvance.class));

    try {
      Beans.get(LunchVoucherAdvanceService.class).onNewAdvance(lunchVoucherAdvance);
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void print(ActionRequest request, ActionResponse response) {
    LunchVoucherAdvance lunchVoucherAdvance =
        request.getContext().asType(LunchVoucherAdvance.class);
    String name =
        lunchVoucherAdvance.getEmployee().getName()
            + "-"
            + Beans.get(AppBaseService.class)
                .getTodayDate(
                    Optional.ofNullable(lunchVoucherAdvance.getEmployee())
                        .map(Employee::getUser)
                        .map(User::getActiveCompany)
                        .orElse(
                            Optional.ofNullable(AuthUtils.getUser())
                                .map(User::getActiveCompany)
                                .orElse(null)))
                .format(DateTimeFormatter.ISO_DATE);
    try {
      String fileLink =
          ReportFactory.createReport(IReport.LUNCH_VOUCHER_ADVANCE, name)
              .addParam("lunchVoucherAdvId", lunchVoucherAdvance.getId())
              .addParam("Timezone", getTimezone(lunchVoucherAdvance))
              .addFormat(ReportSettings.FORMAT_PDF)
              .generate()
              .getFileLink();
      response.setView(ActionView.define(name).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected String getTimezone(LunchVoucherAdvance lunchVoucherAdvance) {
    if (lunchVoucherAdvance.getEmployee() == null
        || lunchVoucherAdvance.getEmployee().getUser() == null
        || lunchVoucherAdvance.getEmployee().getUser().getActiveCompany() == null) {
      return null;
    }
    return lunchVoucherAdvance.getEmployee().getUser().getActiveCompany().getTimezone();
  }
}
