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
package com.axelor.apps.base.service.message;

import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.repo.EmailAccountRepository;
import com.axelor.apps.message.exception.MessageExceptionMessage;
import com.axelor.apps.message.service.MailAccountServiceImpl;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class MailAccountServiceBaseImpl extends MailAccountServiceImpl {

  protected UserService userService;

  @Inject protected AppBaseService appBaseService;

  @Inject
  public MailAccountServiceBaseImpl(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void checkDefaultMailAccount(EmailAccount mailAccount) throws AxelorException {

    AppBase appBase = appBaseService.getAppBase();
    if (appBase.getEmailAccountByUser() || appBase.getEmailAccountByCompany()) {
      String query = this.mailAccountQuery(mailAccount);
      if (!query.isEmpty()) {
        if (appBase.getEmailAccountByUser()) {
          query +=
              " AND self.user"
                  + ((mailAccount.getUser() != null)
                      ? ".id = " + mailAccount.getUser().getId()
                      : " IS NULL");
        }

        if (appBase.getEmailAccountByCompany()) {
          query +=
              " AND self.company"
                  + ((mailAccount.getCompany() != null)
                      ? ".id = " + mailAccount.getCompany().getId()
                      : " IS NULL");
        }
        Long count = mailAccountRepo.all().filter(query).count();

        if (count > 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(MessageExceptionMessage.MAIL_ACCOUNT_5));
        }
      }
    } else {
      super.checkDefaultMailAccount(mailAccount);
    }
  }

  @Override
  public EmailAccount getDefaultSender() {

    AppBase appBase = appBaseService.getAppBase();
    if (!appBase.getEmailAccountByUser() && !appBase.getEmailAccountByCompany()) {
      return super.getDefaultSender();
    }

    EmailAccount emailAccount = null;
    User user = userService.getUser();
    if (appBase.getEmailAccountByUser() && user != null) {
      emailAccount =
          mailAccountRepo
              .all()
              .filter(
                  "self.user = ?1 AND self.isDefault = true AND self.serverTypeSelect = ?2",
                  user,
                  EmailAccountRepository.SERVER_TYPE_SMTP)
              .fetchOne();
    }

    if (emailAccount == null
        && appBase.getEmailAccountByCompany()
        && user != null
        && user.getActiveCompany() != null) {
      emailAccount =
          mailAccountRepo
              .all()
              .filter(
                  "self.company = ?1 AND self.isDefault = true AND self.serverTypeSelect = ?2",
                  user.getActiveCompany(),
                  EmailAccountRepository.SERVER_TYPE_SMTP)
              .fetchOne();
    }

    return emailAccount;
  }

  @Override
  public EmailAccount getDefaultReader() {

    AppBase appBase = appBaseService.getAppBase();
    if (!appBase.getEmailAccountByUser() && !appBase.getEmailAccountByCompany()) {
      return super.getDefaultReader();
    }

    User user = userService.getUser();
    EmailAccount emailAccount = null;
    if (appBase.getEmailAccountByUser() && user != null) {
      emailAccount =
          mailAccountRepo
              .all()
              .filter(
                  "self.user = ?1 AND self.isDefault = true"
                      + " AND (self.serverTypeSelect = ?2 OR self.serverTypeSelect = ?3)",
                  user,
                  EmailAccountRepository.SERVER_TYPE_IMAP,
                  EmailAccountRepository.SERVER_TYPE_POP)
              .fetchOne();
    }

    if (emailAccount == null
        && appBase.getEmailAccountByCompany()
        && user != null
        && user.getActiveCompany() != null) {
      emailAccount =
          mailAccountRepo
              .all()
              .filter(
                  "self.company = ?1 AND self.isDefault = true"
                      + " AND (self.serverTypeSelect = ?2 OR self.serverTypeSelect = ?3)",
                  user.getActiveCompany(),
                  EmailAccountRepository.SERVER_TYPE_IMAP,
                  EmailAccountRepository.SERVER_TYPE_POP)
              .fetchOne();
    }

    return emailAccount;
  }
}
