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
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MailAccountServiceBaseImpl extends MailAccountServiceImpl {

  protected UserService userService;

  @Inject protected AppBaseService appBaseService;

  @Inject
  public MailAccountServiceBaseImpl(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void checkDefaultMailAccount(EmailAccount mailAccount) throws AxelorException {

    if (mailAccount.getIsDefault()
        && (appBaseService.getAppBase().getEmailAccountByUser()
            || (appBaseService.getAppBase().getEmailAccountByCompany()))) {
      String query = "self.isDefault = true";
      Map<String, Object> params = new HashMap<>();

      if (appBaseService.getAppBase().getEmailAccountByUser()) {
        if (mailAccount.getUser() != null) {
          query += " AND self.user = :user";
          params.put("user", mailAccount.getUser());
        } else {
          query += " AND self.user IS NULL";
        }
      }

      if (mailAccount.getId() != null) {
        query += " AND self.id != :mailAccountId";
        params.put("mailAccountId", mailAccount.getId());
      }

      if (appBaseService.getAppBase().getEmailAccountByCompany()) {
        if (mailAccount.getCompany() != null) {
          query += " AND self.company = :company";
          params.put("company", mailAccount.getCompany());
        } else {
          query += " AND self.company IS NULL";
        }
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

      Query<EmailAccount> mailAccountQuery = mailAccountRepo.all().filter(query, params);
      for (Entry<String, Object> param : params.entrySet()) {
        mailAccountQuery.bind(param.getKey(), param.getValue());
      }
      Long count = mailAccountQuery.count();

      if (count > 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(MessageExceptionMessage.MAIL_ACCOUNT_5));
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
    if (appBase.getEmailAccountByUser()) {
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
    if (appBaseService.getAppBase().getEmailAccountByUser()) {
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
        && appBaseService.getAppBase().getEmailAccountByCompany()
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
