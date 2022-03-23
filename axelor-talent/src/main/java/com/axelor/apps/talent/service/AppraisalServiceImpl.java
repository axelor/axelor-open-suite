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
package com.axelor.apps.talent.service;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.apps.hr.db.repo.EmployeeHRRepository;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.apps.talent.db.Appraisal;
import com.axelor.apps.talent.db.repo.AppraisalRepository;
import com.axelor.apps.talent.exception.IExceptionMessage;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.mail.db.repo.MailFollowerRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
  public void send(Appraisal appraisal)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException, MessagingException {

    if (appraisal.getStatusSelect() == null
        || appraisal.getStatusSelect() != AppraisalRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.APPRAISAL_SEND_WRONG_STATUS));
    }

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

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void realize(Appraisal appraisal) throws AxelorException {

    if (appraisal.getStatusSelect() == null
        || appraisal.getStatusSelect() != AppraisalRepository.STATUS_SENT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.APPRAISAL_REALIZE_WRONG_STATUS));
    }

    appraisal.setStatusSelect(AppraisalRepository.STATUS_COMPLETED);

    appraisalRepo.save(appraisal);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void cancel(Appraisal appraisal) throws AxelorException {
    List<Integer> authorizedStatus = new ArrayList<>();
    authorizedStatus.add(AppraisalRepository.STATUS_DRAFT);
    authorizedStatus.add(AppraisalRepository.STATUS_SENT);
    if (appraisal.getStatusSelect() == null
        || !authorizedStatus.contains(appraisal.getStatusSelect())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.APPRAISAL_CANCEL_WRONG_STATUS));
    }

    appraisal.setStatusSelect(AppraisalRepository.STATUS_CANCELED);

    appraisalRepo.save(appraisal);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void draft(Appraisal appraisal) throws AxelorException {

    if (appraisal.getStatusSelect() == null
        || appraisal.getStatusSelect() != AppraisalRepository.STATUS_CANCELED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.APPRAISAL_DRAFT_WRONG_STATUS));
    }

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

    for (Employee employee :
        employees.stream().filter(Objects::nonNull).collect(Collectors.toList())) {
      if (EmployeeHRRepository.isEmployeeFormerNewOrArchived(employee)) {
        continue;
      }
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
