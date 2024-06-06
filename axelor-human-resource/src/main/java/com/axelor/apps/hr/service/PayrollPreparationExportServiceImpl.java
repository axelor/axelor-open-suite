package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.EmployeeBonusMgtLine;
import com.axelor.apps.hr.db.ExtraHoursLine;
import com.axelor.apps.hr.db.ExtraHoursType;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.PayrollLeave;
import com.axelor.apps.hr.db.PayrollPreparation;
import com.axelor.apps.hr.db.repo.ExtraHoursLineRepository;
import com.axelor.apps.hr.db.repo.HrBatchRepository;
import com.axelor.apps.hr.db.repo.PayrollPreparationRepository;
import com.axelor.apps.hr.service.config.HRConfigService;
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

  /**
   * If each employee's payroll preparation has been exported, close the pay period.
   *
   * @param payrollPreparation
   */
  @Override
  @Transactional
  public void closePayPeriodIfExported(PayrollPreparation payrollPreparation) {

    Company company = payrollPreparation.getCompany();
    Period payPeriod = payrollPreparation.getPeriod();

    long nbNotExportedPayroll =
        payrollPreparationRepository
            .all()
            .filter(
                "self.company = :_company AND self.exported = false "
                    + "AND self.period = :_period")
            .bind("_company", company)
            .bind("_period", payPeriod)
            .count();

    if (nbNotExportedPayroll == 0) {
      payPeriod.setStatusSelect(PeriodRepository.STATUS_CLOSED);
      payPeriod.setClosureDateTime(appBaseService.getTodayDateTime().toLocalDateTime());
    }
    periodRepository.save(payPeriod);
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

    // LUNCH VOUCHER MANAGEMENT
    if (payrollPreparation.getLunchVoucherNumber().compareTo(BigDecimal.ZERO) > 0) {
      String[] lunchVoucherLine = createExportFileLine(payrollPreparation);
      lunchVoucherLine[3] = hrConfig.getExportCodeForLunchVoucherManagement();
      lunchVoucherLine[6] = payrollPreparation.getLunchVoucherNumber().toString();
      list.add(lunchVoucherLine);
    }

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
    // Payroll duration
    String[] durationLine = createSilaeExportFileLine(payrollPrep);
    durationLine[1] = hrConfig.getExportCodeForDuration();
    durationLine[2] = String.valueOf(payrollPrep.getDuration());
    exportLineList.add(durationLine);

    // Payroll extraHoursNumber
    String[] extraHoursLine = createSilaeExportFileLine(payrollPrep);
    extraHoursLine[1] = hrConfig.getExportCodeForExtraHours();
    extraHoursLine[2] = String.valueOf(payrollPrep.getExtraHoursNumber());
    exportLineList.add(extraHoursLine);

    // Payroll employee bonus amount
    if (payrollPrep.getEmployeeBonusAmount().compareTo(BigDecimal.ZERO) > 0) {
      Map<String, BigDecimal> map = new HashMap<>();
      for (EmployeeBonusMgtLine bonus : payrollPrep.getEmployeeBonusMgtLineList()) {
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
        String[] employeeBonusLine = createSilaeExportFileLine(payrollPrep);
        employeeBonusLine[1] = entry.getKey();
        employeeBonusLine[2] = entry.getValue().toString();
        exportLineList.add(employeeBonusLine);
      }
    }

    // LUNCH VOUCHER MANAGEMENT
    if (payrollPrep.getLunchVoucherNumber().compareTo(BigDecimal.ZERO) > 0) {
      String[] lunchVoucherLine = createSilaeExportFileLine(payrollPrep);
      lunchVoucherLine[1] = hrConfig.getExportCodeForLunchVoucherManagement();
      lunchVoucherLine[2] = payrollPrep.getLunchVoucherNumber().toString();
      exportLineList.add(lunchVoucherLine);
    }
    return exportLineList;
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
