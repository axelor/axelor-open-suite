/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.batch;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.leave.management.LeaveManagementService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaModel;
import com.google.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import javax.mail.MessagingException;

public class BatchTimesheetReminder extends BatchStrategy {

  protected TimesheetRepository timesheetRepo;
  protected MessageService messageService;
  protected MessageRepository messageRepo;
  protected TemplateMessageService templateMessageService;

  @Inject
  public BatchTimesheetReminder(
      LeaveManagementService leaveManagementService,
      TimesheetRepository timesheetRepo,
      MessageService messageService,
      MessageRepository messageRepo,
      TemplateMessageService templateMessageService) {
    super(leaveManagementService);
    this.timesheetRepo = timesheetRepo;
    this.messageService = messageService;
    this.messageRepo = messageRepo;
    this.templateMessageService = templateMessageService;
  }

  @Override
  protected void process() {
    Template template = batch.getHrBatch().getTemplate();
    MetaModel metaModel = template.getMetaModel();
    try {
      if (metaModel != null) {
        if (metaModel.getName().equals(Employee.class.getSimpleName())) {
          sendReminderUsingEmployees(template);

        } else if (metaModel.getName().equals(Timesheet.class.getSimpleName())) {
          sendReminderUsingTimesheets(template);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(e, metaModel.getName(), batch.getId());
      incrementAnomaly();
    }
  }

  @Override
  protected void stop() {
    String comment =
        String.format(I18n.get(IExceptionMessage.BATCH_TIMESHEET_REMINDER_DONE), batch.getDone())
            + "<br/>"
            + String.format(
                I18n.get(IExceptionMessage.BATCH_TIMESHEET_REMINDER_ANOMALY), batch.getAnomaly());

    addComment(comment);
    super.stop();
  }

  private List<Employee> getEmployeesWithoutRecentTimesheet(Company company) {
    LocalDate now = appBaseService.getTodayDate(company);
    long daysBeforeReminder = batch.getHrBatch().getDaysBeforeReminder().longValue();

    List<Employee> employees =
        employeeRepository
            .all()
            .filter(
                "self.timesheetReminder = 't' AND self.mainEmploymentContract.payCompany = :companyId")
            .bind("companyId", batch.getHrBatch().getCompany().getId())
            .fetch();

    employees.removeIf(employee -> hasRecentTimesheet(now, daysBeforeReminder, employee));
    return employees;
  }

  private boolean hasRecentTimesheet(LocalDate now, long daysBeforeReminder, Employee employee) {
    Timesheet timesheet = getRecentEmployeeTimesheet(employee);
    return timesheet != null && timesheet.getToDate().plusDays(daysBeforeReminder).isAfter(now);
  }

  protected Timesheet getRecentEmployeeTimesheet(Employee employee) {
    Timesheet timesheet =
        timesheetRepo
            .all()
            .filter(
                "self.user.id = :userId AND self.statusSelect IN (:confirmed, :validated) AND self.company = :companyId")
            .bind("userId", employee.getUser().getId())
            .bind("confirmed", TimesheetRepository.STATUS_CONFIRMED)
            .bind("validated", TimesheetRepository.STATUS_VALIDATED)
            .bind("companyId", batch.getHrBatch().getCompany().getId())
            .order("-toDate")
            .fetchOne();
    return timesheet;
  }

  protected void sendReminderUsingEmployees(Template template)
      throws AxelorException, MessagingException, IOException, ClassNotFoundException,
          InstantiationException, IllegalAccessException {
    for (Employee employee :
        getEmployeesWithoutRecentTimesheet(AuthUtils.getUser().getActiveCompany())) {
      Message message = templateMessageService.generateMessage(employee, template);
      messageService.sendByEmail(message);
      incrementDone();
    }
  }

  protected void sendReminderUsingTimesheets(Template template)
      throws AxelorException, MessagingException, IOException, ClassNotFoundException,
          InstantiationException, IllegalAccessException {
    for (Employee employee :
        getEmployeesWithoutRecentTimesheet(AuthUtils.getUser().getActiveCompany())) {
      Timesheet timeSheet = getRecentEmployeeTimesheet(employee);
      if (timeSheet != null) {
        Message message = templateMessageService.generateMessage(timeSheet, template);
        messageService.sendByEmail(message);
      } else {
        throw new AxelorException(
            Timesheet.class,
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(IExceptionMessage.NO_TIMESHEET_FOUND_FOR_EMPLOYEE),
            employee.getName());
      }
      incrementDone();
    }
  }
}
