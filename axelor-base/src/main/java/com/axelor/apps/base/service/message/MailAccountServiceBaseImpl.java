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
package com.axelor.apps.base.service.message;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.repo.EmailAccountRepository;
import com.axelor.apps.message.exception.IExceptionMessage;
import com.axelor.apps.message.service.MailAccountServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;

public class MailAccountServiceBaseImpl extends MailAccountServiceImpl {

  protected UserService userService;

  @Inject protected AppBaseService appBaseService;

  @Inject
  public MailAccountServiceBaseImpl(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void checkDefaultMailAccount(EmailAccount mailAccount) throws AxelorException {

    if (appBaseService.getAppBase().getEmailAccountByUser()
        && mailAccount.getIsDefault()
        && mailAccount.getUser() != null) {
      String query = "self.user = ?1 AND self.isDefault = true";
      List<Object> params = Lists.newArrayList();
      params.add(mailAccount.getUser());
      if (mailAccount.getId() != null) {
        query += " AND self.id != ?2";
        params.add(mailAccount.getId());
      }

      Integer serverTypeSelect = mailAccount.getServerTypeSelect();
      if (serverTypeSelect == EmailAccountRepository.SERVER_TYPE_SMTP) {
        query += " AND self.serverTypeSelect = " + EmailAccountRepository.SERVER_TYPE_SMTP + " ";
      } else if (serverTypeSelect == EmailAccountRepository.SERVER_TYPE_IMAP
          || serverTypeSelect == EmailAccountRepository.SERVER_TYPE_POP) {
        query +=
            " AND (self.serverTypeSelect = "
                + EmailAccountRepository.SERVER_TYPE_IMAP
                + " OR "
                + "self.serverTypeSelect = "
                + EmailAccountRepository.SERVER_TYPE_POP
                + ") ";
      }

      Long count = mailAccountRepo.all().filter(query, params.toArray()).count();

      if (count > 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.MAIL_ACCOUNT_5));
      }
    } else {
      super.checkDefaultMailAccount(mailAccount);
    }
  }

  @Override
  public EmailAccount getDefaultSender() {

    if (appBaseService.getAppBase().getEmailAccountByUser()) {
      return mailAccountRepo
          .all()
          .filter(
              "self.user = ?1 AND self.isDefault = true AND self.serverTypeSelect = ?2",
              userService.getUser(),
              EmailAccountRepository.SERVER_TYPE_SMTP)
          .fetchOne();
    }

    return super.getDefaultSender();
  }

  @Override
  public EmailAccount getDefaultReader() {

    if (appBaseService.getAppBase().getEmailAccountByUser()) {
      return mailAccountRepo
          .all()
          .filter(
              "self.user = ?1 AND self.isDefault = true"
                  + " AND (self.serverTypeSelect = ?2 OR self.serverTypeSelect = ?3)",
              userService.getUser(),
              EmailAccountRepository.SERVER_TYPE_IMAP,
              EmailAccountRepository.SERVER_TYPE_POP)
          .fetchOne();
    }

    return super.getDefaultReader();
  }
}
