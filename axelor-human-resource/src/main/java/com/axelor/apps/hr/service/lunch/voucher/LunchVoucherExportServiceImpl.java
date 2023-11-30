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
package com.axelor.apps.hr.service.lunch.voucher;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.LunchVoucherMgt;
import com.axelor.apps.hr.db.LunchVoucherMgtLine;
import com.axelor.apps.hr.db.repo.LunchVoucherMgtRepository;
import com.axelor.apps.hr.translation.ITranslation;
import com.axelor.common.csv.CSVFile;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVPrinter;

public class LunchVoucherExportServiceImpl implements LunchVoucherExportService {

  protected AppBaseService appBaseService;
  protected LunchVoucherMgtRepository lunchVoucherMgtRepository;
  protected MetaFiles metaFiles;

  @Inject
  public LunchVoucherExportServiceImpl(
      AppBaseService appBaseService,
      LunchVoucherMgtRepository lunchVoucherMgtRepository,
      MetaFiles metaFiles) {
    this.appBaseService = appBaseService;
    this.lunchVoucherMgtRepository = lunchVoucherMgtRepository;
    this.metaFiles = metaFiles;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void export(LunchVoucherMgt lunchVoucherMgt) throws IOException {

    File tempFile = MetaFiles.createTempFile(null, ".csv").toFile();
    try (CSVPrinter printer = CSVFile.DEFAULT.withDelimiter(';').withQuoteAll().write(tempFile)) {

      printer.printRecord(createHeader());

      for (LunchVoucherMgtLine lunchVoucherMgtLine : lunchVoucherMgt.getLunchVoucherMgtLineList()) {

        printer.printRecord(addCsvLine(lunchVoucherMgtLine));
      }
    }

    lunchVoucherMgt.setCsvFile(computeExportFileName(lunchVoucherMgt, tempFile));

    lunchVoucherMgt.setExportDateTime(
        appBaseService.getTodayDateTime(lunchVoucherMgt.getCompany()).toLocalDateTime());
    lunchVoucherMgtRepository.save(lunchVoucherMgt);
  }

  protected List<String> createHeader() {
    List<String> header = new ArrayList<>();
    header.add(I18n.get(ITranslation.EMPLOYEE_CODE));
    header.add(I18n.get(ITranslation.EMPLOYEE_NAME_AND_SURNAME));
    header.add(I18n.get(ITranslation.EMPLOYEE_LUNCH_VOUCHER_NUMBER));
    return header;
  }

  protected List<String> addCsvLine(LunchVoucherMgtLine lunchVoucherMgtLine) {
    List<String> line = new ArrayList<>();
    line.add(lunchVoucherMgtLine.getEmployee().getExportCode());
    line.add(lunchVoucherMgtLine.getEmployee().getContactPartner().getSimpleFullName());
    line.add(lunchVoucherMgtLine.getLunchVoucherNumber().toString());
    return line;
  }

  protected MetaFile computeExportFileName(LunchVoucherMgt lunchVoucherMgt, File tempFile)
      throws IOException {
    return metaFiles.upload(
        new FileInputStream(tempFile),
        I18n.get("LunchVoucherCommand")
            + " - "
            + appBaseService
                .getTodayDate(lunchVoucherMgt.getCompany())
                .format(DateTimeFormatter.ISO_DATE)
            + ".csv");
  }
}
