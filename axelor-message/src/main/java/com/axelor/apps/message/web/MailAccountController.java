/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.NoSuchPaddingException;
import javax.mail.MessagingException;

import com.axelor.app.AppSettings;
import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.db.repo.MailAccountRepository;
import com.axelor.apps.message.exception.IExceptionMessage;
import com.axelor.apps.message.service.MailAccountService;
import com.axelor.apps.tool.service.CipherService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

public class MailAccountController {
	
	@Inject
	private MailAccountService mailAccountService;
	
	@Inject
	private MailAccountRepository mailAccountRepo;
	
	@Inject
	private CipherService cipherService;
	
	public void validateSmtpAccount(ActionRequest request,ActionResponse response){
		
		MailAccount account = request.getContext().asType(MailAccount.class);
		
		try {
			if(!AppSettings.get().get("application.encryptionkey").isEmpty()) {
				cipherService.initEncryptOrDecrypt();
				String password = cipherService.decrypt(account.getPassword());
				account.setPassword(password);
			}

			mailAccountService.checkMailAccountConfiguration(account);

			response.setValue("isValid", Boolean.TRUE );
			response.setFlash( I18n.get(IExceptionMessage.MAIL_ACCOUNT_3) );
			
		} catch ( Exception e) {
			
			TraceBackService.trace(response, e);
			response.setValue("isValid",Boolean.FALSE);
			
		}
		
	}
	
	public void checkDefaultMailAccount(ActionRequest request, ActionResponse response){
		MailAccount account = request.getContext().asType(MailAccount.class);
		if(!mailAccountService.checkDefaultMailAccount(account)){
			response.setError(I18n.get(IExceptionMessage.MAIL_ACCOUNT_5));
			response.setValue("isDefault", false);
		}
	}
	
	public void fetchEmails(ActionRequest request, ActionResponse response) throws MessagingException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException  {
		
		MailAccount account = request.getContext().asType(MailAccount.class);
		account = mailAccountRepo.find(account.getId());
		
		if(!AppSettings.get().get("application.encryptionkey").isEmpty()) {
			cipherService.initEncryptOrDecrypt();
			String password = cipherService.decrypt(account.getPassword());
			account.setPassword(password);
		}
		
		int totalFetched = mailAccountService.fetchEmails(account, true);
		
		response.setFlash(I18n.get(String.format("Total email fetched: %s", totalFetched)));
	}
	
	public void validate(ActionRequest request, ActionResponse response) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {
		
		Context context = request.getContext();

		if(!AppSettings.get().get("application.encryptionkey").isEmpty()) {
			cipherService.initEncryptOrDecrypt();
			String password = cipherService.encrypt((String) context.get("loginPassword"));
	        response.setValue("password", password);	
		}        
	}
}
