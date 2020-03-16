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
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.MailBatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.EmployeeHRRepository;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.EmailAccountRepository;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.ExceptionOriginRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import javax.mail.MessagingException;

public class BatchTimesheetValidationReminder extends AbstractBatch {

  protected TemplateMessageService templateMessageService;
  protected MessageService messageService;
  protected MessageRepository messageRepo;

  @Inject
  public BatchTimesheetValidationReminder(
      TemplateMessageService templateMessageService,
      MessageService messageService,
      MessageRepository messageRepo) {

    this.templateMessageService = templateMessageService;
    this.messageService = messageService;
    this.messageRepo = messageRepo;
  }

  @Override
  protected void process() {
    Template template = batch.getMailBatch().getTemplate();
    switch (batch.getMailBatch().getCode()) {
      case MailBatchRepository.CODE_BATCH_EMAIL_TIME_SHEET:
        if (template != null) {
          generateEmailTemplate();
        } else {
          generateEmail();
        }
        break;
      case MailBatchRepository.CODE_BATCH_EMAIL_ALL_TIME_SHEET:
        if (template != null) {
          generateAllEmailTemplate();
        } else {
          generateAllEmail();
        }
        break;
      default:
        return;
    }
  }

  public void generateEmailTemplate() {

    Company company = batch.getMailBatch().getCompany();
    Template template = batch.getMailBatch().getTemplate();
    List<Timesheet> timesheetList = null;
    if (Beans.get(CompanyRepository.class).all().count() > 1) {
      timesheetList =
          Beans.get(TimesheetRepository.class)
              .all()
              .filter(
                  "self.company.id = ?1 AND self.statusSelect = 1 AND self.user.employee.timesheetReminder = true",
                  company.getId())
              .fetch();
    } else {
      timesheetList =
          Beans.get(TimesheetRepository.class)
              .all()
              .filter("self.statusSelect = 1 AND self.user.employee.timesheetReminder = true")
              .fetch();
    }
    String model = template.getMetaModel().getFullName();
    String tag = template.getMetaModel().getName();
    for (Timesheet timesheet : timesheetList) {
      try {
        Employee employee = timesheet.getUser().getEmployee();
        if (employee == null || EmployeeHRRepository.isEmployeeFormerOrNew(employee)) {
          continue;
        }
        Message message =
            templateMessageService.generateMessage(employee.getId(), model, tag, template);
        messageService.sendByEmail(message);
        incrementDone();
      } catch (Exception e) {
        incrementAnomaly();
        TraceBackService.trace(new Exception(e), ExceptionOriginRepository.REMINDER, batch.getId());
      }
    }
  }

  public void generateEmail() {
    List<Timesheet> timesheetList =
        Beans.get(CompanyRepository.class).all().count() > 1
            ? Beans.get(TimesheetRepository.class)
                .all()
                .filter(
                    "self.company.id = ?1 AND self.statusSelect = 1 AND self.user.employee.timesheetReminder = true",
                    batch.getMailBatch().getCompany().getId())
                .fetch()
            : Beans.get(TimesheetRepository.class)
                .all()
                .filter("self.statusSelect = 1 AND self.user.employee.timesheetReminder = true")
                .fetch();

    for (Timesheet timesheet : timesheetList) {
      try {
        Employee employee = timesheet.getUser().getEmployee();
        if (employee == null || EmployeeHRRepository.isEmployeeFormerOrNew(employee)) {
          continue;
        }
        generateAndSendMessage(employee);
        incrementDone();
      } catch (Exception e) {
        incrementAnomaly();
        TraceBackService.trace(
            new Exception(e), ExceptionOriginRepository.INVOICE_ORIGIN, batch.getId());
      }
    }
  }

  public void generateAllEmailTemplate() {
    Template template = batch.getMailBatch().getTemplate();
    String model = template.getMetaModel().getFullName();
    String tag = template.getMetaModel().getName();

    List<Employee> employeeList =
        Beans.get(EmployeeRepository.class).all().filter("self.timesheetReminder = true").fetch();

    for (Employee employee : employeeList) {
      if (employee == null || EmployeeHRRepository.isEmployeeFormerOrNew(employee)) {
        continue;
      }
      try {
        Message message =
            templateMessageService.generateMessage(employee.getId(), model, tag, template);
        messageService.sendByEmail(message);
        incrementDone();
      } catch (Exception e) {
        incrementAnomaly();
        TraceBackService.trace(new Exception(e), ExceptionOriginRepository.REMINDER, batch.getId());
      }
    }
  }

  public void generateAllEmail() {
    List<Employee> employeeList =
        Beans.get(EmployeeRepository.class).all().filter("self.timesheetReminder = true").fetch();

    for (Employee employee : employeeList) {
      if (employee == null || EmployeeHRRepository.isEmployeeFormerOrNew(employee)) {
        continue;
      }
      try {
        generateAndSendMessage(employee);
        incrementDone();
      } catch (Exception e) {
        incrementAnomaly();
        TraceBackService.trace(
            new Exception(e), ExceptionOriginRepository.INVOICE_ORIGIN, batch.getId());
      }
    }
  }

  @Transactional(rollbackOn = {MessagingException.class, IOException.class, Exception.class})
  protected Message generateAndSendMessage(Employee employee)
      throws MessagingException, IOException, AxelorException {

    Message message = new Message();
    message.setMediaTypeSelect(MessageRepository.MEDIA_TYPE_EMAIL);
    message.setReplyToEmailAddressSet(new HashSet<>());
    message.setCcEmailAddressSet(new HashSet<>());
    message.setBccEmailAddressSet(new HashSet<>());
    message.addToEmailAddressSetItem(employee.getContactPartner().getEmailAddress());
    message.setSenderUser(AuthUtils.getUser());
    message.setSubject(batch.getMailBatch().getSubject());
    message.setContent(batch.getMailBatch().getContent());
    message.setMailAccount(
        Beans.get(EmailAccountRepository.class).all().filter("self.isDefault = true").fetchOne());
    message = messageRepo.save(message);

    return messageService.sendByEmail(message);
  }

  @Override
  protected void stop() {

    String comment = String.format("\t* %s Email(s) sent %n", batch.getDone());
    comment +=
        String.format(
            "\t" + I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4),
            batch.getAnomaly());

    super.stop();
    addComment(comment);
  }
}
