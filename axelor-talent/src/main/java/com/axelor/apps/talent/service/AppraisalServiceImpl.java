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
package com.axelor.apps.talent.service;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.apps.talent.db.Appraisal;
import com.axelor.apps.talent.db.repo.AppraisalRepository;
import com.axelor.auth.db.User;
import com.axelor.mail.db.repo.MailFollowerRepository;
import com.axelor.message.db.EmailAddress;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.db.repo.TemplateRepository;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.mail.MessagingException;

public class AppraisalServiceImpl implements AppraisalService {

  @Inject private AppraisalRepository appraisalRepo;

  @Inject private MailFollowerRepository mailFollowerRepo;

  @Inject private TemplateRepository templateRepo;

  @Inject private TemplateMessageService templateMessageService;

  @Inject private MessageService messageService;

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void send(Appraisal appraisal) throws ClassNotFoundException, MessagingException {

    Employee employee = appraisal.getEmployee();

    User user = employee.getUser();
    if (user != null) {
      mailFollowerRepo.follow(appraisal, user);
    }

    Template template =
        templateRepo
            .all()
            .filter("self.metaModel.fullName = ?1", Appraisal.class.getName())
            .fetchOne();

    EmailAddress email = null;
    if (employee.getContactPartner() != null) {
      email = employee.getContactPartner().getEmailAddress();
    }

    if (template != null && email != null) {
      Message message = templateMessageService.generateMessage(appraisal, template);
      message.addToEmailAddressSetItem(email);
      messageService.sendByEmail(message);
    }

    appraisal.setStatusSelect(AppraisalRepository.STATUS_SENT);

    appraisalRepo.save(appraisal);
  }

  @Transactional
  @Override
  public void realize(Appraisal appraisal) {

    appraisal.setStatusSelect(AppraisalRepository.STATUS_COMPLETED);

    appraisalRepo.save(appraisal);
  }

  @Transactional
  @Override
  public void cancel(Appraisal appraisal) {

    appraisal.setStatusSelect(AppraisalRepository.STATUS_CANCELED);

    appraisalRepo.save(appraisal);
  }

  @Transactional
  @Override
  public void draft(Appraisal appraisal) {

    appraisal.setStatusSelect(AppraisalRepository.STATUS_DRAFT);

    appraisalRepo.save(appraisal);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public Set<Long> createAppraisals(
      Appraisal appraisalTemplate, Set<Employee> employees, Boolean send)
      throws ClassNotFoundException, MessagingException {

    Set<Long> appraisalIds = new HashSet<Long>();

    if (appraisalTemplate == null) {
      return appraisalIds;
    }

    for (Employee employee :
        employees.stream().filter(Objects::nonNull).collect(Collectors.toList())) {
      Appraisal appraisal = appraisalRepo.copy(appraisalTemplate, false);
      appraisal.setEmployee(employee);
      if (appraisal.getCompany() == null) {
        EmploymentContract employmentContract = employee.getMainEmploymentContract();
        if (employmentContract != null) {
          appraisal.setCompany(employmentContract.getPayCompany());
        }
      }
      appraisal.setIsTemplate(false);
      appraisal = appraisalRepo.save(appraisal);
      if (send != null && send) {
        send(appraisal);
      }
      appraisalIds.add(appraisal.getId());
    }

    return appraisalIds;
  }
}
