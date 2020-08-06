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
package com.axelor.apps.production.web;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.production.db.MpsCharge;
import com.axelor.apps.production.db.MpsWeeklySchedule;
import com.axelor.apps.production.db.repo.MpsChargeRepository;
import com.axelor.apps.production.db.repo.MpsWeeklyScheduleRepository;
import com.axelor.apps.production.report.IReport;
import com.axelor.apps.production.service.MpsChargeService;
import com.axelor.apps.production.service.MpsChargeServiceImpl;
import com.axelor.apps.production.translation.ITranslation;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class MpsChargeController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  public void fillDummy(ActionRequest request, ActionResponse response) {
	  MpsCharge mpsCharge = request.getContext().asType(MpsCharge.class);
	  mpsCharge = Beans.get(MpsChargeRepository.class).find(mpsCharge.getId());
	
    int startWeek = mpsCharge.getStartMonthDate().getDayOfYear() / 7;
    int endWeek = mpsCharge.getEndMonthDate().getDayOfYear() / 7;

	for(int i = startWeek; i <= endWeek; i++) {
		Beans.get(MpsChargeServiceImpl.class).createDummy(i,mpsCharge);
	}
  }
  
  public void getMpsWeeklyScheduleCustom(ActionRequest request, ActionResponse response) {

    LocalDate startMonthDate =
        LocalDate.parse(
            request.getData().get("startMonthDate").toString(), DateTimeFormatter.ISO_DATE);
    LocalDate endMonthDate =
        LocalDate.parse(
            request.getData().get("endMonthDate").toString(), DateTimeFormatter.ISO_DATE);

    MpsChargeService mpsChargeService = Beans.get(MpsChargeService.class);

    List<MpsWeeklySchedule> mpsWeeklyScheduleList =
        Beans.get(MpsWeeklyScheduleRepository.class).all().order("totalHours").fetch();

    Map<MpsWeeklySchedule, Map<Integer, BigDecimal>> totalHoursWeekCountMap = new LinkedHashMap<>();
    for (MpsWeeklySchedule mpsWeeklySchedule : mpsWeeklyScheduleList) {
      Map<Integer, BigDecimal> totalHoursWeekMap = new HashMap<>();
      for (int i = 1; i <= 53; i++) {
        totalHoursWeekMap.put(i, mpsWeeklySchedule.getTotalHours());
      }
      totalHoursWeekCountMap.put(mpsWeeklySchedule, totalHoursWeekMap);
    }
    List<Map<String, Object>> dataMapList =
        mpsChargeService.getTableDataWeekMapList(totalHoursWeekCountMap);

    response.setData(dataMapList);
  }

  public void getMpsWeeklyScheduleChartFirstYear(ActionRequest request, ActionResponse response) {

    LocalDate startMonthDate = (LocalDate) request.getContext().getParent().get("startMonthDate");
    LocalDate endMonthDate = (LocalDate) request.getContext().getParent().get("endMonthDate");
    MpsCharge mpsCharge = request.getContext().getParent().asType(MpsCharge.class);

    if (startMonthDate.getYear() != endMonthDate.getYear()) {
      endMonthDate = startMonthDate.with(TemporalAdjusters.lastDayOfYear());
    }

    MpsChargeService mpsChargeService = Beans.get(MpsChargeService.class);

    Map<MpsWeeklySchedule, Map<Integer, BigDecimal>> totalHoursCountMap =
        mpsChargeService.countTotalWeekHours(startMonthDate, endMonthDate);
    List<Map<String, Object>> dataMapList =
        mpsChargeService.getChartDataMapWeekList(totalHoursCountMap, mpsCharge);

    response.setData(dataMapList);
  }

  public void getMpsWeeklyScheduleChartSecondYear(ActionRequest request, ActionResponse response) {

    LocalDate startMonthDate = (LocalDate) request.getContext().getParent().get("startMonthDate");
    LocalDate endMonthDate = (LocalDate) request.getContext().getParent().get("endMonthDate");
    MpsCharge mpsCharge = request.getContext().getParent().asType(MpsCharge.class);

    if (startMonthDate.getYear() == endMonthDate.getYear()) {
      return;
    }
    startMonthDate = endMonthDate.with(TemporalAdjusters.firstDayOfYear());
    MpsChargeService mpsChargeService = Beans.get(MpsChargeService.class);
    Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCountMap =
        mpsChargeService.countTotalHours(startMonthDate, endMonthDate);
    List<Map<String, Object>> dataMapList =
        mpsChargeService.getChartDataMapList(totalHoursCountMap, mpsCharge);

    response.setData(dataMapList);
  }

  public void print(ActionRequest request, ActionResponse response) throws AxelorException {

    String name = I18n.get(ITranslation.MPS_CHARGE);
    LocalDate startMonthDate = (LocalDate) request.getContext().get("startMonthDate");
    LocalDate endMonthDate = (LocalDate) request.getContext().get("endMonthDate");
    if (startMonthDate == null || endMonthDate == null) {
      return;
    }

    String fileLink =
        ReportFactory.createReport(IReport.MPS_CHARGE, name + "-${date}")
            .addParam("mpsId", request.getContext().get("id"))
            .addParam("logoPath", AuthUtils.getUser().getActiveCompany().getLogo().getFilePath())
            .addParam(
                "startMonthDate", startMonthDate.format(DateTimeFormatter.ofPattern("dd/MM/YYYY")))
            .addParam(
                "endMonthDate", endMonthDate.format(DateTimeFormatter.ofPattern("dd/MM/YYYY")))
            .generate()
            .getFileLink();

    LOG.debug("Printing {}", name);
    response.setView(ActionView.define(name).add("html", fileLink).map());
  }
}
