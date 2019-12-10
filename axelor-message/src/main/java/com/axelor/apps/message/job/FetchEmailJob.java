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
package com.axelor.apps.message.job;

import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.repo.EmailAccountRepository;
import com.axelor.apps.message.service.MailAccountService;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.List;
import javax.mail.MessagingException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An example {@link Job} class that prints a some messages to the stderr. */
public class FetchEmailJob implements Job {

  private final Logger log = LoggerFactory.getLogger(FetchEmailJob.class);

  @Inject private MailAccountService mailAccountService;

  @Inject private EmailAccountRepository mailAccountRepo;

  @Override
  public void execute(JobExecutionContext context) {

    List<EmailAccount> mailAccounts =
        mailAccountRepo.all().filter("self.isValid = true and self.serverTypeSelect > 1").fetch();

    log.debug("Total email fetching accounts : {}", mailAccounts.size());
    for (EmailAccount account : mailAccounts) {
      try {
        Integer total = mailAccountService.fetchEmails(account, true);
        log.debug("Email fetched for account: {}, total: {} ", account.getName(), total);
      } catch (MessagingException | IOException e) {
        TraceBackService.trace(e);
      }
    }
  }
}
