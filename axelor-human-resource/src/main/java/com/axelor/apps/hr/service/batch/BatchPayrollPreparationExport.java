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
package com.axelor.apps.hr.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.HrBatch;
import com.axelor.apps.hr.db.PayrollPreparation;
import com.axelor.apps.hr.db.repo.HrBatchRepository;
import com.axelor.apps.hr.db.repo.PayrollPreparationRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.PayrollPreparationService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.file.CsvTool;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BatchPayrollPreparationExport extends BatchStrategy {

  protected int total;
  protected HrBatch hrBatch;

  protected PayrollPreparationService payrollPreparationService;

  @Inject protected PayrollPreparationRepository payrollPreparationRepository;

  @Inject CompanyRepository companyRepository;

  @Inject PeriodRepository periodRepository;

  @Inject HRConfigService hrConfigService;

  @Inject
  public BatchPayrollPreparationExport(PayrollPreparationService payrollPreparationService) {
    super();
    this.payrollPreparationService = payrollPreparationService;
  }

  @Override
  protected void start() throws IllegalAccessException {

    super.start();

    total = 0;
    hrBatch = Beans.get(HrBatchRepository.class).find(batch.getHrBatch().getId());

    checkPoint();
  }

  @Override
  protected void process() {

    String exportAll = "";
    if (!hrBatch.getExportAlreadyExported()) {
      exportAll = " AND self.exported = false ";
    }

    List<PayrollPreparation> payrollPreparationList =
        payrollPreparationRepository
            .all()
            .filter(
                "self.company = ?1 AND self.period = ?2 " + exportAll,
                hrBatch.getCompany(),
                hrBatch.getPeriod())
            .fetch();

    switch (hrBatch.getPayrollPreparationExportTypeSelect()) {
      case HrBatchRepository.EXPORT_TYPE_STANDARD:
        try {
          batch.setMetaFile(standardExport(payrollPreparationList));
        } catch (IOException e) {
          incrementAnomaly();
          TraceBackService.trace(e, ExceptionOriginRepository.LEAVE_MANAGEMENT, batch.getId());
        }
        break;
      case HrBatchRepository.EXPORT_TYPE_NIBELIS:
        try {
          batch.setMetaFile(nibelisExport(payrollPreparationList));
        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(e, ExceptionOriginRepository.LEAVE_MANAGEMENT, batch.getId());
        }
        break;
      case HrBatchRepository.EXPORT_TYPE_SILAE:
        try {
          batch.setMetaFile(silaeExport(payrollPreparationList));
        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(e, ExceptionOriginRepository.LEAVE_MANAGEMENT, batch.getId());
        }
        break;
      default:
        break;
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public MetaFile standardExport(List<PayrollPreparation> payrollPreparationList)
      throws IOException {

    List<String[]> list = new ArrayList<>();
    LocalDateTime today =
        Beans.get(AppBaseService.class).getTodayDateTime(hrBatch.getCompany()).toLocalDateTime();

    for (PayrollPreparation payrollPreparation : payrollPreparationList) {
      String[] item = new String[5];
      item[0] = payrollPreparation.getEmployee().getName();
      item[1] = payrollPreparation.getDuration().toString();
      item[2] = payrollPreparation.getLunchVoucherNumber().toString();
      item[3] = payrollPreparation.getEmployeeBonusAmount().toString();
      item[4] = payrollPreparation.getExtraHoursNumber().toString();
      list.add(item);

      payrollPreparation.setExported(true);
      payrollPreparation.setExportDateTime(today);
      payrollPreparation.setExportTypeSelect(HrBatchRepository.EXPORT_TYPE_STANDARD);
      payrollPreparation.addBatchListItem(batch);
      payrollPreparationRepository.save(payrollPreparation);
      total++;
      incrementDone();
    }

    String fileName = payrollPreparationService.getPayrollPreparationExportName();
    File file = MetaFiles.createTempFile(fileName, ".csv").toFile();

    CsvTool.csvWriter(
        file.getParent(),
        file.getName(),
        ';',
        payrollPreparationService.getPayrollPreparationExportHeader(),
        list);

    FileInputStream inStream = new FileInputStream(file);
    MetaFile metaFile = Beans.get(MetaFiles.class).upload(inStream, file.getName());

    return metaFile;
  }

  @Transactional(rollbackOn = {Exception.class})
  public MetaFile nibelisExport(List<PayrollPreparation> payrollPreparationList)
      throws IOException, AxelorException {

    List<String[]> list = new ArrayList<>();

    for (PayrollPreparation payrollPreparation : payrollPreparationList) {

      payrollPreparation.addBatchListItem(batch);
      payrollPreparationService.exportNibelis(payrollPreparation, list);
      total++;
    }

    String fileName = payrollPreparationService.getPayrollPreparationExportName();
    File file = MetaFiles.createTempFile(fileName, ".csv").toFile();

    CsvTool.csvWriter(
        file.getParent(),
        file.getName(),
        ';',
        payrollPreparationService.getPayrollPreparationMeilleurGestionExportHeader(),
        list);

    FileInputStream inStream = new FileInputStream(file);
    MetaFile metaFile = Beans.get(MetaFiles.class).upload(inStream, file.getName());

    return metaFile;
  }

  @Override
  protected void stop() {

    String comment =
        String.format(
            I18n.get(HumanResourceExceptionMessage.BATCH_PAYROLL_PREPARATION_EXPORT_RECAP) + '\n',
            total);

    addComment(comment);
    super.stop();
  }

  @Transactional(rollbackOn = {Exception.class})
  public MetaFile silaeExport(List<PayrollPreparation> payrollPreparationList)
      throws IOException, AxelorException {

    List<String[]> list = new ArrayList<>();

    for (PayrollPreparation payrollPreparation : payrollPreparationList) {
      payrollPreparation.addBatchListItem(batch);
      payrollPreparationService.exportSilae(payrollPreparation, list);
      total++;
      incrementDone();
    }

    String fileName = payrollPreparationService.getPayrollPreparationExportName();
    File file = MetaFiles.createTempFile(fileName, ".csv").toFile();

    CsvTool.csvWriter(
        file.getParent(),
        file.getName(),
        ';',
        payrollPreparationService.getPayrollPreparationSilaeExportHeader(),
        list);

    FileInputStream inStream = new FileInputStream(file);
    MetaFile metaFile = Beans.get(MetaFiles.class).upload(inStream, file.getName());
    return metaFile;
  }
}
