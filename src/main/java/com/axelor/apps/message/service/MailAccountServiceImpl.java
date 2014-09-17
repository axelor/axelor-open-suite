/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;

import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.db.repo.MailAccountRepository;
import com.axelor.mail.SmtpAccount;

public class MailAccountServiceImpl extends MailAccountRepository implements MailAccountService {

	public MailAccount getDefaultMailAccount()  {
		
		return all().filter("self.isDefault = true").fetchOne();
		
	}


	@Override
	public boolean validateSmtpMailAccount(MailAccount account) {
		String port=account.getPort()<=0?null:account.getPort().toString();
		SmtpAccount smtpAccount=new SmtpAccount(account.getHost(), port, account.getLogin(), account.getPassword(), this.getSmtpSecurity(account));
		Session session=smtpAccount.getSession();
		boolean isvalidated=false;
		try {
			Transport transport=session.getTransport("smtp");
			transport.connect();
			transport.close();
			isvalidated=true;
		} catch (NoSuchProviderException e) {
		} catch (MessagingException e) {
		}
		return isvalidated;	
	}
	
	
	public String getSmtpSecurity(MailAccount mailAccount)  {
		
		if(mailAccount.getSecuritySelect() == MailAccount.SECURITY_SSL)  {
			return SmtpAccount.ENCRYPTION_SSL;
		}
		else if(mailAccount.getSecuritySelect() == MailAccount.SECURITY_TLS)  {
			return SmtpAccount.ENCRYPTION_TLS;
		}
		else  {
			return null;
		}
		
	}
	
	public String getSignature(MailAccount mailAccount)  {
		
		if(mailAccount != null && mailAccount.getSignature() != null)  {
			return "\n "+mailAccount.getSignature();
		}
		
		return "";
	}
}
