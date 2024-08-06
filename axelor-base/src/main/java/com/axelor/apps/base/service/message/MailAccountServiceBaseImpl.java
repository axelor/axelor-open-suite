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
package com.axelor.apps.base.service.message;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.message.db.EmailAccount;
import com.axelor.message.db.repo.EmailAccountRepository;
import com.axelor.message.db.repo.EmailAddressRepository;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.exception.MessageExceptionMessage;
import com.axelor.message.service.MailAccountServiceImpl;
import com.axelor.meta.MetaFiles;
import com.axelor.studio.db.AppBase;
import com.axelor.utils.service.CipherService;
import com.google.inject.Inject;

public class MailAccountServiceBaseImpl extends MailAccountServiceImpl {

  protected UserService userService;

  @Inject protected AppBaseService appBaseService;

  @Inject
  public MailAccountServiceBaseImpl(
      EmailAccountRepository mailAccountRepo,
      CipherService cipherService,
      EmailAddressRepository emailAddressRepo,
      MessageRepository messageRepo,
      MetaFiles metaFiles,
      UserService userService) {
    super(mailAccountRepo, cipherService, emailAddressRepo, messageRepo, metaFiles);
    this.userService = userService;
  }

  @Override
  public void checkDefaultMailAccount(EmailAccount mailAccount) {

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
          throw new IllegalStateException(I18n.get(MessageExceptionMessage.MAIL_ACCOUNT_5));
        }
      }
    } else {
      try {
        super.checkDefaultMailAccount(mailAccount);
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }
  }

  protected String mailAccountQuery(EmailAccount mailAccount) {
    String query = null;
    if (mailAccount.getIsDefault()) {
      query = "self.isDefault = true";
      if (mailAccount.getId() != null) {
        query += " AND self.id != " + mailAccount.getId();
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
    }
    return query;
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
