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

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.apps.hr.db.HrBatch;
import com.axelor.apps.hr.db.repo.EmploymentContractRepository;
import com.axelor.apps.hr.db.repo.HrBatchRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.EmploymentContractService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.file.CsvTool;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BatchEmploymentContractExport extends BatchStrategy {

  protected int total;
  protected HrBatch hrBatch;

  @Override
  protected void start() throws IllegalAccessException {

    super.start();

    total = 0;
    hrBatch = Beans.get(HrBatchRepository.class).find(batch.getHrBatch().getId());

    checkPoint();
  }

  @Override
  protected void process() {

    List<EmploymentContract> employmentContractList =
        Beans.get(EmploymentContractRepository.class)
            .all()
            .filter(
                "self.payCompany = ?1 AND self.status = ?2",
                hrBatch.getCompany(),
                EmploymentContractRepository.STATUS_ACTIVE)
            .fetch();

    switch (hrBatch.getEmploymentContractExportTypeSelect()) {
      case HrBatchRepository.EMPLOYMENT_CONTRACT_EXPORT_TYPE_SILAE:
        try {
          batch.setMetaFile(employmentContractExportSilae(employmentContractList));
        } catch (IOException e) {
          incrementAnomaly();
          TraceBackService.trace(e);
        }
        break;
      default:
        break;
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public MetaFile employmentContractExportSilae(List<EmploymentContract> employmentContractList)
      throws IOException {

    List<String[]> list = new ArrayList<>();

    for (EmploymentContract employmentContract : employmentContractList) {
      Beans.get(EmploymentContractService.class)
          .employmentContractExportSilae(employmentContract, list);

      total++;
      incrementDone();
    }

    File tempFile =
        MetaFiles.createTempFile(
                Beans.get(EmploymentContractService.class).employmentContractExportName(), ".csv")
            .toFile();

    String[] headers = Beans.get(EmploymentContractService.class).employmentContractExportHeaders();

    CsvTool.csvWriter(tempFile.getParent(), tempFile.getName(), ';', headers, list);

    MetaFiles metaFiles = Beans.get(MetaFiles.class);
    MetaFile metaFile = metaFiles.upload(tempFile);
    tempFile.delete();

    return metaFile;
  }

  @Override
  protected void stop() {

    String comment =
        String.format(
            I18n.get(HumanResourceExceptionMessage.BATCH_EMPLOYMENT_CONTRACT_EXPORT_RECAP) + '\n',
            total);

    addComment(comment);
    super.stop();
  }
}
