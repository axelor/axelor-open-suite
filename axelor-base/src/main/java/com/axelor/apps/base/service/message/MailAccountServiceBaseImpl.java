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
package com.axelor.apps.base.service.message;

import java.util.List;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.service.MailAccountServiceImpl;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class MailAccountServiceBaseImpl extends MailAccountServiceImpl {

	protected UserService userService;

	@Inject
	protected GeneralService generalService;

	@Inject
	public MailAccountServiceBaseImpl(UserService userService){
		this.userService = userService;
	}

	@Override
	public boolean checkDefaultMailAccount(MailAccount mailAccount) {
		if ( generalService.getGeneral().getMailAccountByUser() && mailAccount.getIsDefault()) {
			String request = "self.user = ?1 AND self.isDefault = true";
			List<Object> params = Lists.newArrayList();
			params.add(userService.getUser());
			if(mailAccount.getId() != null){
				request += " AND self.id != ?2";
				params.add(mailAccount.getId());
			}
			return mailAccountRepo.all().filter(request, params.toArray()).count() == 0;
		}

		return super.checkDefaultMailAccount( mailAccount);

	}

	@Override
	public MailAccount getDefaultMailAccount()  {

		if ( generalService.getGeneral().getMailAccountByUser() ) {
			return mailAccountRepo.all().filter("self.user = ?1 AND self.isDefault = true", userService.getUser()).fetchOne();
		}

		return super.getDefaultMailAccount();
	}

}
