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
package com.axelor.apps.message.service;

import com.axelor.apps.message.db.EmailAccount;
import com.axelor.exception.AxelorException;
import com.axelor.mail.MailAccount;
import java.io.IOException;
import javax.mail.MessagingException;

public interface MailAccountService {

  public void checkDefaultMailAccount(EmailAccount mailAccount) throws AxelorException;

  public EmailAccount getDefaultSender();

  public EmailAccount getDefaultReader();

  public void checkMailAccountConfiguration(EmailAccount mailAccount)
      throws AxelorException, Exception;

  public String getSecurity(EmailAccount mailAccount);

  public String getProtocol(EmailAccount mailAccount);

  public String getSignature(EmailAccount mailAccount);

  public String getEncryptPassword(String password);

  public String getDecryptPassword(String password);

  public int fetchEmails(EmailAccount mailAccount, boolean unseenOnly)
      throws MessagingException, IOException;

  public MailAccount getMailAccount(EmailAccount mailAccount);
}
