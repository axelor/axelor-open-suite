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
package com.axelor.apps.talent.service;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.apps.talent.db.Appraisal;
import com.axelor.apps.talent.db.repo.AppraisalRepository;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.mail.db.repo.MailFollowerRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.mail.MessagingException;

public class AppraisalServiceImpl implements AppraisalService {

  @Inject private AppraisalRepository appraisalRepo;

  @Inject private MailFollowerRepository mailFollowerRepo;

  @Inject private TemplateRepository templateRepo;

  @Inject private TemplateMessageService templateMessageService;

  @Inject private MessageService messageService;

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void send(Appraisal appraisal)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException, MessagingException {

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
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException, MessagingException {

    Set<Long> appraisalIds = new HashSet<Long>();

    if (appraisalTemplate == null) {
      return appraisalIds;
    }

    for (Employee employee : employees) {
      Appraisal appraisal = appraisalRepo.copy(appraisalTemplate, false);
      appraisal.setEmployee(employee);
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
