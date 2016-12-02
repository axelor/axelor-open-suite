/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import java.util.List;

import javax.mail.AuthenticationFailedException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;

import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.db.repo.MailAccountRepository;
import com.axelor.apps.message.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.mail.MailConstants;
import com.axelor.mail.SmtpAccount;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class MailAccountServiceImpl implements MailAccountService {
	
	static final int CHECK_CONF_TIMEOUT = 5000;
	
	@Inject
	protected MailAccountRepository mailAccountRepo;
		
	@Override
	public boolean checkDefaultMailAccount(MailAccount mailAccount) {
		if(mailAccount.getIsDefault()){
			String request = "self.isDefault = true";
			List<Object> params = Lists.newArrayList();
			if(mailAccount.getId() != null){
				request += " AND self.id != ?1";
				params.add(mailAccount.getId());
			}
			return mailAccountRepo.all().filter(request, params.toArray()).count() == 0;
		}
		return true;
	}

	@Override
	public MailAccount getDefaultMailAccount()  {
		return mailAccountRepo.all().filter("self.isDefault = true").fetchOne();
	}
   
	@Override
	public void checkMailAccountConfiguration ( MailAccount mailAccount ) throws AxelorException, Exception {
		
		String port = mailAccount.getPort() <= 0 ? null : mailAccount.getPort().toString();
		
		SmtpAccount smtpAccount = new SmtpAccount( mailAccount.getHost(), port, mailAccount.getLogin(), mailAccount.getPassword(), getSmtpSecurity( mailAccount ) );
		smtpAccount.setConnectionTimeout( CHECK_CONF_TIMEOUT );
		
		Session session = smtpAccount.getSession();
		
		try {
			Transport transport = session.getTransport( getProtocol( mailAccount ) );
			transport.connect( mailAccount.getHost(),mailAccount.getPort(),mailAccount.getLogin(),mailAccount.getPassword() );
			transport.close();
		} catch ( AuthenticationFailedException e ) {
			throw new AxelorException(I18n.get(IExceptionMessage.MAIL_ACCOUNT_1), e, IException.CONFIGURATION_ERROR) ;
		} catch ( NoSuchProviderException e ) {
			throw new AxelorException(I18n.get(IExceptionMessage.MAIL_ACCOUNT_2), e, IException.CONFIGURATION_ERROR) ;
		}

	}
	
	
	public String getSmtpSecurity(MailAccount mailAccount)  {
		
		if ( mailAccount.getSecuritySelect() == MailAccountRepository.SECURITY_SSL ) { return MailConstants.CHANNEL_SSL; }
		else if (mailAccount.getSecuritySelect() == MailAccountRepository.SECURITY_STARTTLS ) { return MailConstants.CHANNEL_STARTTLS; }
		else { return null; }
		
	}
	
	public String getProtocol(MailAccount mailAccount) {
		switch ( mailAccount.getServerTypeSelect() ) {
		case MailAccountRepository.SERVER_TYPE_SMTP:
			return "smtp";
		default:
			return "";
		}
	}
	
	public String getSignature(MailAccount mailAccount)  {
		
		if ( mailAccount != null && mailAccount.getSignature() != null ) { return "\n "+mailAccount.getSignature();	}		
		return "";
		
	}
}
