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
package com.axelor.apps.hr.service.batch;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.leave.management.LeaveManagementService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.EmailAccountRepository;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.MessageService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import javax.mail.MessagingException;

public class BatchTimesheetReminder extends BatchStrategy {

  protected TimesheetRepository timesheetRepo;
  protected MessageService messageService;

  @Inject
  public BatchTimesheetReminder(
      LeaveManagementService leaveManagementService,
      TimesheetRepository timesheetRepo,
      MessageService messageService) {
    super(leaveManagementService);
    this.timesheetRepo = timesheetRepo;
    this.messageService = messageService;
  }

  @Override
  protected void process() {
    for (Employee employee : getEmployeesWithoutRecentTimesheet()) {
      try {
        sendReminder(employee);

      } catch (Exception e) {
        TraceBackService.trace(e, Employee.class.getSimpleName(), batch.getId());
        incrementAnomaly();

      } finally {
        incrementDone();
      }
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

  private List<Employee> getEmployeesWithoutRecentTimesheet() {
    LocalDate now = LocalDate.now();
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
    return timesheet != null && timesheet.getToDate().plusDays(daysBeforeReminder).isAfter(now);
  }

  private void sendReminder(Employee employee)
      throws AxelorException, MessagingException, IOException {
    Message message = new Message();
    message.setMediaTypeSelect(MessageRepository.MEDIA_TYPE_EMAIL);
    message.setReplyToEmailAddressSet(new HashSet<>());
    message.setCcEmailAddressSet(new HashSet<>());
    message.setBccEmailAddressSet(new HashSet<>());
    message.addToEmailAddressSetItem(employee.getContactPartner().getEmailAddress());
    message.setSenderUser(AuthUtils.getUser());
    message.setSubject(batch.getHrBatch().getTemplate().getSubject());
    message.setContent(batch.getHrBatch().getTemplate().getContent());
    message.setMailAccount(
        Beans.get(EmailAccountRepository.class).all().filter("self.isDefault = true").fetchOne());

    messageService.sendByEmail(message);
  }
}
