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

import com.axelor.apps.base.service.message.MailAccountServiceBaseImpl;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.inject.Beans;
import com.axelor.mail.MailParser;
import com.axelor.message.db.EmailAccount;
import com.axelor.message.db.Message;
import com.axelor.message.db.repo.EmailAccountRepository;
import com.axelor.message.db.repo.EmailAddressRepository;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.studio.db.AppRecruitment;
import com.axelor.studio.db.repo.AppRecruitmentRepository;
import com.axelor.utils.service.CipherService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Date;
import javax.mail.MessagingException;

public class MailAccountServiceTalentImpl extends MailAccountServiceBaseImpl {

  @Inject
  public MailAccountServiceTalentImpl(
      EmailAccountRepository mailAccountRepo,
      CipherService cipherService,
      EmailAddressRepository emailAddressRepo,
      MessageRepository messageRepo,
      MetaFiles metaFiles,
      UserService userService) {
    super(mailAccountRepo, cipherService, emailAddressRepo, messageRepo, metaFiles, userService);
  }

  @Inject private AppRecruitmentRepository appRecruitmentRepo;

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public Message createMessage(EmailAccount mailAccount, MailParser parser, Date date)
      throws MessagingException {

    Message message = super.createMessage(mailAccount, parser, date);

    AppRecruitment appRecruitment = appRecruitmentRepo.all().fetchOne();

    if (appRecruitment != null
        && appRecruitment.getApp().getActive()
        && message.getMailAccount() != null
        && message.getMailAccount().getServerTypeSelect() > 1) {

      String lastEmailId = appRecruitment.getLastEmailId();
      if (lastEmailId == null || message.getId() > Long.parseLong(lastEmailId)) {
        Beans.get(JobPositionService.class).createJobApplication(message);
      }
    }

    return message;
  }
}
