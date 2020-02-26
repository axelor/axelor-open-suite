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
package com.axelor.apps.talent.service;

import com.axelor.apps.base.db.AppRecruitment;
import com.axelor.apps.base.db.repo.AppRecruitmentRepository;
import com.axelor.apps.base.service.message.MailAccountServiceBaseImpl;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.Message;
import com.axelor.mail.MailParser;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Date;
import javax.mail.MessagingException;

public class MailAccountServiceTalentImpl extends MailAccountServiceBaseImpl {

  @Inject
  public MailAccountServiceTalentImpl(UserService userService) {
    super(userService);
  }

  @Inject private AppRecruitmentRepository appRecruitmentRepo;

  @Inject private JobPositionService jobPositionService;

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public Message createMessage(EmailAccount mailAccount, MailParser parser, Date date)
      throws MessagingException {

    Message message = super.createMessage(mailAccount, parser, date);

    AppRecruitment appRecruitment = appRecruitmentRepo.all().fetchOne();

    if (appRecruitment != null
        && appRecruitment.getActive()
        && message.getMailAccount() != null
        && message.getMailAccount().getServerTypeSelect() > 1) {

      String lastEmailId = appRecruitment.getLastEmailId();
      if (lastEmailId == null || message.getId() > Long.parseLong(lastEmailId)) {
        jobPositionService.createJobApplication(message);
      }
    }

    return message;
  }
}
