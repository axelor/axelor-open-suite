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
package com.axelor.apps.base.service.message;

import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.db.repo.MailAccountRepository;
import com.axelor.apps.message.service.MailAccountServiceImpl;
import com.google.inject.Inject;

public class MailAccountServiceBaseImpl extends MailAccountServiceImpl {

	@Inject
	private UserService uis;
	
	@Inject
	private MailAccountRepository mailAccountRepo;

	
	@Override
	public MailAccount getDefaultMailAccount()  {
		
		MailAccount mailAccount = mailAccountRepo.all().filter("self.user = ?1 AND self.isDefault = true", uis.getUser()).fetchOne();
		
		return mailAccount;
	}
	
}
