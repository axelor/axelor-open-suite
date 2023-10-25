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

import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.LunchVoucherMgt;
import com.axelor.apps.hr.db.LunchVoucherMgtLine;
import com.axelor.apps.hr.db.repo.LunchVoucherMgtLineRepository;
import com.axelor.apps.hr.db.repo.LunchVoucherMgtRepository;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherExportService;
import com.axelor.apps.hr.service.lunch.voucher.LunchVoucherMgtService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.List;

@Singleton
public class LunchVoucherMgtController {

  public void calculate(ActionRequest request, ActionResponse response) {

    try {
      LunchVoucherMgt lunchVoucherMgt =
          Beans.get(LunchVoucherMgtRepository.class)
              .find(request.getContext().asType(LunchVoucherMgt.class).getId());
      Beans.get(LunchVoucherMgtService.class).calculate(lunchVoucherMgt);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validate(ActionRequest request, ActionResponse response) {
    LunchVoucherMgt lunchVoucherMgt =
        Beans.get(LunchVoucherMgtRepository.class)
            .find(request.getContext().asType(LunchVoucherMgt.class).getId());
    try {
      Beans.get(LunchVoucherMgtService.class).validate(lunchVoucherMgt);

      response.setReload(true);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
      return;
    }

    try {
      Beans.get(PeriodService.class).checkPeriod(lunchVoucherMgt.getPayPeriod());
      Beans.get(PeriodService.class).checkPeriod(lunchVoucherMgt.getLeavePeriod());
    } catch (Exception e) {
      response.setInfo(e.getMessage());
    }
  }

  public void updateTotal(ActionRequest request, ActionResponse response) {
    LunchVoucherMgt lunchVoucherMgt = request.getContext().asType(LunchVoucherMgt.class);
    Beans.get(LunchVoucherMgtService.class).calculateTotal(lunchVoucherMgt);

    response.setValue("totalLunchVouchers", lunchVoucherMgt.getTotalLunchVouchers());
    response.setValue("requestedLunchVouchers", lunchVoucherMgt.getRequestedLunchVouchers());
    response.setValue("givenLunchVouchers", lunchVoucherMgt.getGivenLunchVouchers());
  }

  public void export(ActionRequest request, ActionResponse response) throws IOException {
    LunchVoucherMgt lunchVoucherMgt =
        Beans.get(LunchVoucherMgtRepository.class)
            .find(request.getContext().asType(LunchVoucherMgt.class).getId());

    try {
      Beans.get(LunchVoucherExportService.class).export(lunchVoucherMgt);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateStock(ActionRequest request, ActionResponse response) {
    try {
      LunchVoucherMgt lunchVoucherMgt = request.getContext().asType(LunchVoucherMgt.class);
      if (lunchVoucherMgt.getId() == null) {
        return;
      }
      List<LunchVoucherMgtLine> oldLunchVoucherLines =
          Beans.get(LunchVoucherMgtLineRepository.class)
              .all()
              .filter("self.lunchVoucherMgt.id = ?", lunchVoucherMgt.getId())
              .fetch();
      int stockQuantityStatus =
          Beans.get(LunchVoucherMgtService.class)
              .updateStock(
                  lunchVoucherMgt.getLunchVoucherMgtLineList(),
                  oldLunchVoucherLines,
                  lunchVoucherMgt.getCompany());
      response.setValue("stockQuantityStatus", stockQuantityStatus);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkPayPeriod(ActionRequest request, ActionResponse response) {
    LunchVoucherMgt lunchVoucherMgt = request.getContext().asType(LunchVoucherMgt.class);
    try {
      Beans.get(PeriodService.class).checkPeriod(lunchVoucherMgt.getPayPeriod());
    } catch (Exception e) {
      response.setInfo(e.getMessage());
    }
  }

  public void checkLeavePeriod(ActionRequest request, ActionResponse response) {
    LunchVoucherMgt lunchVoucherMgt = request.getContext().asType(LunchVoucherMgt.class);
    try {
      Beans.get(PeriodService.class).checkPeriod(lunchVoucherMgt.getLeavePeriod());
    } catch (Exception e) {
      response.setInfo(e.getMessage());
    }
  }
}
