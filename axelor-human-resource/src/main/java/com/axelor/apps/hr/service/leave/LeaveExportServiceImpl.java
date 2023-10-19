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
package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.DateService;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.translation.ITranslation;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.common.csv.CSVFile;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.helpers.MetaSelectHelper;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVPrinter;

public class LeaveExportServiceImpl implements LeaveExportService {

  protected static final String HR_START_ON_SELECT = "hr.start.on.select";

  protected LeaveRequestRepository leaveRequestRepo;
  protected MetaSelectHelper metaSelectHelper;
  protected DateService dateService;
  protected MetaFiles metaFiles;

  @Inject
  public LeaveExportServiceImpl(
      LeaveRequestRepository leaveRequestRepo,
      MetaSelectHelper metaSelectHelper,
      DateService dateService,
      MetaFiles metaFiles) {
    this.leaveRequestRepo = leaveRequestRepo;
    this.metaSelectHelper = metaSelectHelper;
    this.dateService = dateService;
    this.metaFiles = metaFiles;
  }

  @Override
  public MetaFile export(List<Long> ids, User user) throws IOException, AxelorException {

    if (ObjectUtils.isEmpty(ids)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.LEAVE_REQUEST_NO_LINE_PRESENT));
    }

    List<LeaveRequest> leaveRequests = leaveRequestRepo.findByIds(ids);

    File tempFile = MetaFiles.createTempFile(null, ".csv").toFile();
    try (CSVPrinter printer = CSVFile.DEFAULT.withDelimiter(';').withQuoteAll().write(tempFile)) {
      Map<Integer, String> selectionMap =
          Map.of(
              1,
              metaSelectHelper.getSelectTitle(HR_START_ON_SELECT, 1),
              2,
              metaSelectHelper.getSelectTitle(HR_START_ON_SELECT, 2));

      printer.printRecord(createHeader());

      for (LeaveRequest request : leaveRequests) {
        printer.printRecord(addCsvLine(request, selectionMap));
      }
    }

    return metaFiles.upload(new FileInputStream(tempFile), I18n.get("LeaveRequest") + ".csv");
  }

  protected List<String> createHeader() {
    List<String> header = new ArrayList<>();
    header.add(I18n.get(ITranslation.EMPLOYEE_CODE));
    header.add(I18n.get(ITranslation.EMPLOYEE_CODE_NATURE));
    header.add(I18n.get(ITranslation.LEAVE_REQUEST_START_DATE));
    header.add(I18n.get(ITranslation.LEAVE_REQUEST_START_ON));
    header.add(I18n.get(ITranslation.LEAVE_REQUEST_END_DATE));
    header.add(I18n.get(ITranslation.LEAVE_REQUEST_END_ON));
    return header;
  }

  protected List<String> addCsvLine(LeaveRequest request, Map<Integer, String> selectionMap)
      throws AxelorException {
    List<String> line = new ArrayList<>();
    String employeeCode =
        request.getEmployee() == null ? "" : request.getEmployee().getExportCode();
    line.add(employeeCode);
    String codeNature = request.getLeaveReason().getExportCode();
    line.add(codeNature);
    String startDate = request.getFromDateT().format(dateService.getDateFormat());
    line.add(startDate);
    String startOn = I18n.get(selectionMap.get(request.getStartOnSelect()));
    line.add(startOn);
    String endDate = request.getToDateT().format(dateService.getDateFormat());
    line.add(endDate);
    String endOn = I18n.get(selectionMap.get(request.getEndOnSelect()));
    line.add(endOn);
    return line;
  }
}
