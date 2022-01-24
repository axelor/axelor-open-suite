/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.project.db.Project;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.meta.CallMethod;
import com.axelor.meta.schema.actions.ActionView;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.MessagingException;
import wslite.json.JSONException;

public interface TimesheetService {

  @Transactional(rollbackOn = {Exception.class})
  public void confirm(Timesheet timesheet) throws AxelorException;

  public Message sendConfirmationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException;

  public Message confirmAndSendConfirmationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException;

  /**
   * Checks that there is a line for each working day of the timesheet.
   *
   * @param timesheet
   * @throws AxelorException
   */
  public void checkEmptyPeriod(Timesheet timesheet) throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  public void validate(Timesheet timesheet) throws AxelorException;

  public Message sendValidationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException;

  public Message validateAndSendValidationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException;

  @Transactional(rollbackOn = {Exception.class})
  public void refuse(Timesheet timesheet) throws AxelorException;

  public Message sendRefusalEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException;

  public Message refuseAndSendRefusalEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException;

  public void cancel(Timesheet timesheet) throws AxelorException;

  /**
   * Set the timesheet to draft status.
   *
   * @param timesheet a timesheet
   */
  void draft(Timesheet timesheet);

  public Message sendCancellationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException;

  public Message cancelAndSendCancellationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException;

  public Timesheet generateLines(
      Timesheet timesheet,
      LocalDate fromGenerationDate,
      LocalDate toGenerationDate,
      BigDecimal logTime,
      Project project,
      Product product)
      throws AxelorException;

  public LocalDate getFromPeriodDate();

  public Timesheet getCurrentTimesheet();

  public Timesheet getCurrentOrCreateTimesheet();

  public Timesheet createTimesheet(User user, LocalDate fromDate, LocalDate toDate);

  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<TimesheetLine> timesheetLineList, int priority) throws AxelorException;

  public List<InvoiceLine> createInvoiceLine(
      Invoice invoice,
      Product product,
      User user,
      String date,
      BigDecimal hoursDuration,
      int priority,
      PriceList priceList)
      throws AxelorException;

  @Transactional
  public void computeTimeSpent(Timesheet timesheet);

  public BigDecimal computeSubTimeSpent(Project project);

  public void computeParentTimeSpent(Project project);

  public BigDecimal computeTimeSpent(Project project);

  public String computeFullName(Timesheet timesheet);

  public List<Map<String, Object>> createDefaultLines(Timesheet timesheet);

  public BigDecimal computePeriodTotal(Timesheet timesheet);

  public String getPeriodTotalConvertTitle(Timesheet timesheet);

  public void createDomainAllTimesheetLine(
      User user, Employee employee, ActionView.ActionViewBuilder actionView);

  public void createValidateDomainTimesheetLine(
      User user, Employee employee, ActionView.ActionViewBuilder actionView);

  /**
   * Update {@link Timesheet#timeLoggingPreferenceSelect} and recompute all durations.
   *
   * @param timesheet a context timesheet
   * @return the updated timesheet
   */
  void updateTimeLoggingPreference(Timesheet timesheet) throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  public void generateLinesFromExpectedProjectPlanning(Timesheet timesheet) throws AxelorException;

  public void prefillLines(Timesheet timesheet) throws AxelorException;

  public void setProjectTaskTotalRealHrs(List<TimesheetLine> timesheetLines, boolean isAdd);

  public void removeAfterToDateTimesheetLines(Timesheet timesheet);

  @CallMethod
  public Set<Long> getContextProjectIds();
}
