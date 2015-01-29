/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;

import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.db.repo.MailAccountRepository;
import com.axelor.apps.message.exception.IExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.mail.SmtpAccount;

public class MailAccountServiceImpl extends MailAccountRepository implements MailAccountService {

	public MailAccount getDefaultMailAccount()  {
		
		return all().filter("self.isDefault = true").fetchOne();
		
	}
   
	@Override
	public String validateSmtpMailAccount(MailAccount account) {
		String port=account.getPort()<=0?null:account.getPort().toString();
		SmtpAccount smtpAccount=new SmtpAccount(account.getHost(), port, account.getLogin(), account.getPassword(), this.getSmtpSecurity(account));
		Session session=smtpAccount.getSession();
		String errorMessage = null;
		try {
			Transport transport=session.getTransport("smtp");
			transport.connect(account.getHost(),account.getPort(),account.getLogin(),account.getPassword());
			transport.close();
		} catch (AuthenticationFailedException e){
			errorMessage = I18n.get(IExceptionMessage.MAIL_ACCOUNT_1);
		} catch (NoSuchProviderException e) {
			errorMessage = I18n.get(IExceptionMessage.MAIL_ACCOUNT_2);
		} catch (MessagingException e) {
			errorMessage = e.getMessage();
		} catch (Exception e){
			errorMessage = e.getMessage();
		}
		return errorMessage;	
	}
	
	
	public String getSmtpSecurity(MailAccount mailAccount)  {
		
		if(mailAccount.getSecuritySelect() == SECURITY_SSL)  {
			return SmtpAccount.ENCRYPTION_SSL;
		}
		else if(mailAccount.getSecuritySelect() == SECURITY_TLS)  {
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
