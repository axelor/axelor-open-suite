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
package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.EmployeeBonusMgtLine;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.ExportCode;
import com.axelor.apps.hr.db.ExtraHoursLine;
import com.axelor.apps.hr.db.ExtraHoursType;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.OtherCostsEmployee;
import com.axelor.apps.hr.db.PayrollLeave;
import com.axelor.apps.hr.db.PayrollPreparation;
import com.axelor.apps.hr.db.repo.ExtraHoursLineRepository;
import com.axelor.apps.hr.db.repo.HrBatchRepository;
import com.axelor.apps.hr.db.repo.PayrollPreparationRepository;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.utils.helpers.file.CsvHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PayrollPreparationExportServiceImpl implements PayrollPreparationExportService {
  private static final DateTimeFormatter NIBELIS_EXPORT_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter SILAE_EXPORT_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy");

  protected PayrollPreparationRepository payrollPreparationRepository;
  protected AppBaseService appBaseService;
  protected PeriodRepository periodRepository;
  protected PayrollPreparationService payrollPreparationService;
  protected HRConfigService hrConfigService;

  @Inject
  public PayrollPreparationExportServiceImpl(
      PayrollPreparationRepository payrollPreparationRepository,
      AppBaseService appBaseService,
      PeriodRepository periodRepository,
      PayrollPreparationService payrollPreparationService,
      HRConfigService hrConfigService) {
    this.payrollPreparationRepository = payrollPreparationRepository;
    this.appBaseService = appBaseService;
    this.periodRepository = periodRepository;
    this.payrollPreparationService = payrollPreparationService;
    this.hrConfigService = hrConfigService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public String exportPayrollPreparation(PayrollPreparation payrollPreparation)
      throws AxelorException, IOException {

    List<String[]> list = new ArrayList<>();
    String[] headerLine = {};
    if (payrollPreparation.getExportTypeSelect() == HrBatchRepository.EXPORT_TYPE_STANDARD) {
      String[] item = new String[5];
      item[0] = payrollPreparation.getEmployee().getName();
      item[1] = payrollPreparation.getDuration().toString();
      item[2] = payrollPreparation.getLunchVoucherNumber().toString();
      item[3] = payrollPreparation.getEmployeeBonusAmount().toString();
      item[4] = payrollPreparation.getExtraHoursNumber().toString();
      list.add(item);
      headerLine = this.getPayrollPreparationExportHeader();
    } else if (payrollPreparation.getExportTypeSelect() == HrBatchRepository.EXPORT_TYPE_NIBELIS) {
      this.exportNibelis(payrollPreparation, list);
      headerLine = this.getPayrollPreparationMeilleurGestionExportHeader();
    } else if (payrollPreparation.getExportTypeSelect() == HrBatchRepository.EXPORT_TYPE_SILAE) {
      this.exportSilae(payrollPreparation, list);
      headerLine = this.getPayrollPreparationSilaeExportHeader();
    }

    String fileName = this.getPayrollPreparationExportName();
    File file = MetaFiles.createTempFile(fileName, ".csv").toFile();

    CsvHelper.csvWriter(file.getParent(), file.getName(), ';', headerLine, list);

    try (InputStream is = new FileInputStream(file)) {
      Beans.get(MetaFiles.class).attach(is, file.getName(), payrollPreparation);
    }

    payrollPreparation.setExported(true);
    payrollPreparation.setExportDateTime(
        appBaseService.getTodayDateTime(payrollPreparation.getCompany()).toLocalDateTime());
    payrollPreparationRepository.save(payrollPreparation);

    return file.getPath();
  }

  public String[] createExportFileLine(PayrollPreparation payrollPreparation) {

    String[] item = new String[7];
    item[0] = payrollPreparation.getEmployee().getExportCode();
    item[1] = payrollPreparation.getEmployee().getContactPartner().getName();
    item[2] = payrollPreparation.getEmployee().getContactPartner().getFirstName();
    return item;
  }

  @Override
  public void exportNibelis(PayrollPreparation payrollPreparation, List<String[]> list)
      throws AxelorException {

    HRConfig hrConfig = hrConfigService.getHRConfig(payrollPreparation.getCompany());

    exportNibelisLeaveList(payrollPreparation, list);
    exportNibelisLunchVoucher(payrollPreparation, list, hrConfig);
    exportNibelisEmployeeBonus(payrollPreparation, list);
    exportNibelisExtraHours(payrollPreparation, list);
    exportNibelisExpense(payrollPreparation, list);
    exportNibelisOtherCosts(payrollPreparation, list);
  }

  protected void exportNibelisExpense(PayrollPreparation payrollPreparation, List<String[]> list) {
    if (payrollPreparation.getExpenseAmount().signum() == 0) {
      return;
    }

    for (Expense expense : payrollPreparation.getExpenseList()) {
      for (ExpenseLine expenseLine : expense.getGeneralExpenseLineList()) {
        String exportCode = expenseLine.getExpenseProduct().getExportCode();
        if (StringUtils.notEmpty(exportCode)) {
          String[] exportExpenseLine = createExportFileLine(payrollPreparation);
          exportExpenseLine[3] = exportCode;
          exportExpenseLine[6] = String.valueOf(expenseLine.getTotalAmount());
          list.add(exportExpenseLine);
        }
      }
    }
  }

  protected void exportNibelisExtraHours(
      PayrollPreparation payrollPreparation, List<String[]> list) {
    // EXTRA HOURS
    if (payrollPreparation.getExtraHoursNumber().compareTo(BigDecimal.ZERO) > 0) {
      List<ExtraHoursLine> extraHourLineList =
          Beans.get(ExtraHoursLineRepository.class)
              .all()
              .filter(
                  "self.payrollPreparation.id = ?1"
                      + " AND self.extraHoursType.payrollPreprationExport = ?2",
                  payrollPreparation.getId(),
                  true)
              .fetch();
      Map<ExtraHoursType, BigDecimal> extraHourLineExportMap =
          extraHourLineList.stream()
              .collect(
                  Collectors.groupingBy(
                      ExtraHoursLine::getExtraHoursType,
                      Collectors.reducing(
                          BigDecimal.ZERO, ExtraHoursLine::getQty, BigDecimal::add)));
      extraHourLineExportMap.forEach(
          (extraHoursTypeGroup, totalHours) -> {
            String[] extraHourLine = createExportFileLine(payrollPreparation);
            extraHourLine[3] = extraHoursTypeGroup.getExportCode();
            extraHourLine[6] = totalHours.toString();
            list.add(extraHourLine);
          });
    }
  }

  protected void exportNibelisEmployeeBonus(
      PayrollPreparation payrollPreparation, List<String[]> list) {
    // EMPLOYEE BONUS MANAGEMENT
    if (payrollPreparation.getEmployeeBonusAmount().compareTo(BigDecimal.ZERO) > 0) {
      Map<String, BigDecimal> map = new HashMap<>();
      for (EmployeeBonusMgtLine bonus : payrollPreparation.getEmployeeBonusMgtLineList()) {
        if (bonus.getEmployeeBonusMgt().getEmployeeBonusType().getPayrollPreparationExport()) {
          if (map.containsKey(bonus.getEmployeeBonusMgt().getEmployeeBonusType().getExportCode())) {
            map.put(
                bonus.getEmployeeBonusMgt().getEmployeeBonusType().getExportCode(),
                bonus
                    .getAmount()
                    .add(
                        map.get(
                            bonus.getEmployeeBonusMgt().getEmployeeBonusType().getExportCode())));
          } else {
            map.put(
                bonus.getEmployeeBonusMgt().getEmployeeBonusType().getExportCode(),
                bonus.getAmount());
          }
        }
      }
      for (Map.Entry<String, BigDecimal> entry : map.entrySet()) {
        String[] employeeBonusLine = createExportFileLine(payrollPreparation);
        employeeBonusLine[3] = entry.getKey();
        employeeBonusLine[6] = entry.getValue().toString();
        list.add(employeeBonusLine);
      }
    }
  }

  protected void exportNibelisLunchVoucher(
      PayrollPreparation payrollPreparation, List<String[]> list, HRConfig hrConfig) {
    // LUNCH VOUCHER MANAGEMENT
    if (payrollPreparation.getLunchVoucherNumber().compareTo(BigDecimal.ZERO) > 0) {
      String[] lunchVoucherLine = createExportFileLine(payrollPreparation);
      lunchVoucherLine[3] = hrConfig.getExportCodeForLunchVoucherManagement();
      lunchVoucherLine[6] = payrollPreparation.getLunchVoucherNumber().toString();
      list.add(lunchVoucherLine);
    }
  }

  protected void exportNibelisLeaveList(PayrollPreparation payrollPreparation, List<String[]> list)
      throws AxelorException {
    // LEAVES
    if (payrollPreparation.getLeaveDuration().compareTo(BigDecimal.ZERO) > 0) {
      List<PayrollLeave> payrollLeaveList =
          payrollPreparationService.fillInLeaves(payrollPreparation);
      for (PayrollLeave payrollLeave : payrollLeaveList) {
        if (payrollLeave.getLeaveReason().getPayrollPreprationExport()) {
          String[] leaveLine = createExportFileLine(payrollPreparation);
          leaveLine[3] = payrollLeave.getLeaveReason().getExportCode();
          leaveLine[4] = payrollLeave.getFromDate().format(NIBELIS_EXPORT_DATE_FORMATTER);
          leaveLine[5] = payrollLeave.getToDate().format(NIBELIS_EXPORT_DATE_FORMATTER);
          leaveLine[6] = payrollLeave.getDuration().toString();
          list.add(leaveLine);
        }
      }
    }
  }

  protected void exportNibelisOtherCosts(
      PayrollPreparation payrollPreparation, List<String[]> list) {
    for (OtherCostsEmployee otherCostsEmployee : payrollPreparation.getOtherCostsEmployeeSet()) {
      String[] otherCostLine = createExportFileLine(payrollPreparation);
      otherCostLine[3] = getExportCode(otherCostsEmployee);
      otherCostLine[6] = String.valueOf(otherCostsEmployee.getAmount());
      list.add(otherCostLine);
    }
  }

  @Override
  public String getPayrollPreparationExportName() {
    return I18n.get("Payroll preparation")
        + " - "
        + appBaseService.getTodayDateTime().toLocalDateTime().toString();
  }

  @Override
  public String[] getPayrollPreparationExportHeader() {

    String[] headers = new String[5];
    headers[0] = I18n.get("Employee");
    headers[1] = I18n.get("Working days' number");
    headers[2] = I18n.get("Lunch vouchers' number");
    headers[3] = I18n.get("Employee bonus amount");
    headers[4] = I18n.get("Extra hours' number");
    return headers;
  }

  @Override
  public String[] getPayrollPreparationMeilleurGestionExportHeader() {
    String[] headers = new String[7];
    headers[0] = I18n.get("PayrollPreparation.registrationNumber");
    headers[1] = I18n.get("Employee lastname");
    headers[2] = I18n.get("Employee firstname");
    headers[3] = I18n.get("Code");
    headers[4] = I18n.get("Start date");
    headers[5] = I18n.get("End date");
    headers[6] = I18n.get("Value");
    return headers;
  }

  @Override
  public List<String[]> exportSilae(PayrollPreparation payrollPrep, List<String[]> exportLineList)
      throws AxelorException {
    HRConfig hrConfig = hrConfigService.getHRConfig(payrollPrep.getCompany());

    exportSilaeLeaves(payrollPrep, exportLineList);
    exportSilaeDuration(payrollPrep, exportLineList, hrConfig);
    exportSilaeExtraHour(payrollPrep, exportLineList, hrConfig);
    exportSilaeExpense(payrollPrep, exportLineList);
    exportSilaeOtherCosts(payrollPrep, exportLineList);

    return exportLineList;
  }

  protected void exportSilaeExpense(PayrollPreparation payrollPreparation, List<String[]> list) {
    if (payrollPreparation.getExpenseAmount().signum() == 0) {
      return;
    }

    for (Expense expense : payrollPreparation.getExpenseList()) {
      for (ExpenseLine expenseLine : expense.getGeneralExpenseLineList()) {
        String exportCode = expenseLine.getExpenseProduct().getExportCode();
        if (StringUtils.notEmpty(exportCode)) {
          String[] exportExpenseLine = createSilaeExportFileLine(payrollPreparation);
          exportExpenseLine[1] = exportCode;
          exportExpenseLine[2] = String.valueOf(expenseLine.getTotalAmount());
          list.add(exportExpenseLine);
        }
      }
    }
  }

  protected void exportSilaeExtraHour(
      PayrollPreparation payrollPrep, List<String[]> exportLineList, HRConfig hrConfig) {
    // Payroll extraHoursNumber
    String[] extraHoursLine = createSilaeExportFileLine(payrollPrep);
    extraHoursLine[1] = hrConfig.getExportCodeForExtraHours();
    extraHoursLine[2] = String.valueOf(payrollPrep.getExtraHoursNumber());
    exportLineList.add(extraHoursLine);
  }

  protected void exportSilaeDuration(
      PayrollPreparation payrollPrep, List<String[]> exportLineList, HRConfig hrConfig) {
    // Payroll duration
    String[] durationLine = createSilaeExportFileLine(payrollPrep);
    durationLine[1] = hrConfig.getExportCodeForDuration();
    durationLine[2] = String.valueOf(payrollPrep.getDuration());
    exportLineList.add(durationLine);
  }

  protected void exportSilaeLeaves(PayrollPreparation payrollPrep, List<String[]> exportLineList)
      throws AxelorException {
    // Payroll leaves
    if (payrollPrep.getLeaveDuration().compareTo(BigDecimal.ZERO) > 0) {
      List<PayrollLeave> payrollLeaveList = payrollPreparationService.fillInLeaves(payrollPrep);
      for (PayrollLeave payrollLeave : payrollLeaveList) {
        if (payrollLeave.getLeaveReason().getPayrollPreprationExport()) {
          String[] leaveLine = createSilaeExportFileLine(payrollPrep);
          leaveLine[1] = payrollLeave.getLeaveReason().getExportCode();
          leaveLine[2] = String.valueOf(payrollLeave.getDuration());
          leaveLine[3] = payrollLeave.getFromDate().format(SILAE_EXPORT_DATE_FORMATTER);
          leaveLine[4] = payrollLeave.getToDate().format(SILAE_EXPORT_DATE_FORMATTER);
          exportLineList.add(leaveLine);
        }
      }
    }
  }

  protected void exportSilaeOtherCosts(PayrollPreparation payrollPreparation, List<String[]> list) {
    for (OtherCostsEmployee otherCostsEmployee : payrollPreparation.getOtherCostsEmployeeSet()) {
      String[] otherCostLine = createSilaeExportFileLine(payrollPreparation);
      otherCostLine[1] = getExportCode(otherCostsEmployee);
      otherCostLine[2] = String.valueOf(otherCostsEmployee.getAmount());
      list.add(otherCostLine);
    }
  }

  protected String getExportCode(OtherCostsEmployee otherCostsEmployee) {
    return Optional.ofNullable(otherCostsEmployee.getExportCode())
        .map(ExportCode::getCode)
        .orElse(null);
  }

  @Override
  public String[] getPayrollPreparationSilaeExportHeader() {
    String[] headers = new String[5];
    headers[0] = I18n.get("PayrollPreparation.registrationNumber");
    headers[1] = I18n.get("Code");
    headers[2] = I18n.get("Value");
    headers[3] = I18n.get("Start date");
    headers[4] = I18n.get("End date");
    return headers;
  }

  public String[] createSilaeExportFileLine(PayrollPreparation payroll) {
    String[] item = new String[5];
    item[0] = payroll.getEmployee().getExportCode();
    item[3] = payroll.getPeriod().getFromDate().format(SILAE_EXPORT_DATE_FORMATTER);
    item[4] = payroll.getPeriod().getToDate().format(SILAE_EXPORT_DATE_FORMATTER);
    return item;
  }
}
