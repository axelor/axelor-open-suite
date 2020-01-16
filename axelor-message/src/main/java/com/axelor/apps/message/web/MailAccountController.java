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
package com.axelor.apps.message.web;

import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.repo.EmailAccountRepository;
import com.axelor.apps.message.exception.IExceptionMessage;
import com.axelor.apps.message.service.MailAccountService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.io.IOException;
import javax.mail.MessagingException;

@Singleton
public class MailAccountController {

  public void validateSmtpAccount(ActionRequest request, ActionResponse response) {

    EmailAccount account = request.getContext().asType(EmailAccount.class);

    try {

      Beans.get(MailAccountService.class).checkMailAccountConfiguration(account);

      response.setValue("isValid", Boolean.TRUE);
      response.setFlash(I18n.get(IExceptionMessage.MAIL_ACCOUNT_3));

    } catch (Exception e) {

      TraceBackService.trace(response, e);
      response.setValue("isValid", Boolean.FALSE);
    }
  }

  public void checkDefaultMailAccount(ActionRequest request, ActionResponse response)
      throws AxelorException {

    EmailAccount account = request.getContext().asType(EmailAccount.class);
    try {
      Beans.get(MailAccountService.class).checkDefaultMailAccount(account);
    } catch (AxelorException e) {
      response.setAttr("isDefault", "value", false);
      response.setFlash(e.getMessage());
    }
  }

  public void fetchEmails(ActionRequest request, ActionResponse response)
      throws MessagingException, IOException {

    EmailAccount account = request.getContext().asType(EmailAccount.class);
    account = Beans.get(EmailAccountRepository.class).find(account.getId());

    int totalFetched = Beans.get(MailAccountService.class).fetchEmails(account, true);

    response.setFlash(I18n.get(String.format("Total email fetched: %s", totalFetched)));
  }

  public void validate(ActionRequest request, ActionResponse response) {
    if (request.getContext().get("newPassword") != null)
      response.setValue(
          "password",
          Beans.get(MailAccountService.class)
              .getEncryptPassword(request.getContext().get("newPassword").toString()));
  }
}
