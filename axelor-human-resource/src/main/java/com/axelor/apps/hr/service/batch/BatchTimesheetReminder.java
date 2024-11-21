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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.EmployeeHRRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.leave.management.LeaveManagementService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.meta.db.MetaModel;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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
        String.format(
                I18n.get(HumanResourceExceptionMessage.BATCH_TIMESHEET_REMINDER_DONE),
                batch.getDone())
            + "<br/>"
            + String.format(
                I18n.get(HumanResourceExceptionMessage.BATCH_TIMESHEET_REMINDER_ANOMALY),
                batch.getAnomaly());

    addComment(comment);
    super.stop();
  }

  private List<Employee> getEmployeesWithoutRecentTimesheet(Company company) {
    LocalDate now = appBaseService.getTodayDate(company);
    long daysBeforeReminder = batch.getHrBatch().getDaysBeforeReminder();

    List<Employee> employees =
        employeeRepository
            .all()
            .filter(
                "self.timesheetReminder = 't' AND self.mainEmploymentContract.payCompany = :companyId")
            .bind("companyId", batch.getHrBatch().getCompany().getId())
            .fetch();

    employees.removeIf(
        employee ->
            hasRecentTimesheet(now, daysBeforeReminder, employee)
                || EmployeeHRRepository.isEmployeeFormerNewOrArchived(employee));
    return employees;
  }

  protected boolean hasRecentTimesheet(LocalDate now, long daysBeforeReminder, Employee employee) {
    Timesheet timesheet = getRecentEmployeeTimesheet(employee);
    return timesheet != null && timesheet.getToDate().plusDays(daysBeforeReminder).isAfter(now);
  }

  protected Timesheet getRecentEmployeeTimesheet(Employee employee) {
    Timesheet timesheet =
        timesheetRepo
            .all()
            .filter(
                "self.employee.id = :employeeId AND self.statusSelect IN (:confirmed, :validated) AND self.company = :companyId")
            .bind("employeeId", employee.getId())
            .bind("confirmed", TimesheetRepository.STATUS_CONFIRMED)
            .bind("validated", TimesheetRepository.STATUS_VALIDATED)
            .bind("companyId", batch.getHrBatch().getCompany().getId())
            .order("-toDate")
            .fetchOne();
    return timesheet;
  }

  protected void sendReminderUsingEmployees(Template template)
      throws ClassNotFoundException, MessagingException {
    for (Employee employee :
        getEmployeesWithoutRecentTimesheet(
                Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList())) {
      Message message = templateMessageService.generateMessage(employee, template);
      messageService.sendByEmail(message);
      incrementDone();
    }
  }

  protected void sendReminderUsingTimesheets(Template template)
      throws ClassNotFoundException, MessagingException, AxelorException {
    for (Employee employee :
        getEmployeesWithoutRecentTimesheet(
                Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList())) {
      if (employee == null || EmployeeHRRepository.isEmployeeFormerNewOrArchived(employee)) {
        continue;
      }
      Timesheet timeSheet = getRecentEmployeeTimesheet(employee);
      if (timeSheet != null) {
        Message message = templateMessageService.generateMessage(timeSheet, template);
        messageService.sendByEmail(message);
      } else {
        throw new AxelorException(
            Timesheet.class,
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(HumanResourceExceptionMessage.NO_TIMESHEET_FOUND_FOR_EMPLOYEE),
            employee.getName());
      }
      incrementDone();
    }
  }
}
