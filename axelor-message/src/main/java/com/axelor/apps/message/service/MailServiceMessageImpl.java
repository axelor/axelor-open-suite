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

import static com.axelor.common.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.repo.EmailAddressRepository;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
import com.axelor.mail.service.MailServiceImpl;
import com.google.common.base.Joiner;

@Singleton
public class MailServiceMessageImpl extends MailServiceImpl{
	
	@Override
	public Model resolve(String email) {
		final EmailAddressRepository addresses = Beans.get(EmailAddressRepository.class);
		final EmailAddress address = addresses.all().filter("self.address = ?1", email).fetchOne();
		if (address != null) {
			return address;
		}
		return super.resolve(email);
	}
	
	@Override
	public List<InternetAddress> findEmails(String matching, List<String> selected, int maxResult) {

		final List<String> where = new ArrayList<>();
		final Map<String, Object> params = new HashMap<>();

		where.add("self.address is not null");

		if (!isBlank(matching)) {
			where.add("(LOWER(self.address) like LOWER(:email))");
			params.put("email", "%" + matching + "%");
		}
		if (selected != null && !selected.isEmpty()) {
			where.add("self.address not in (:selected)");
			params.put("selected", selected);
		}

		final String filter = Joiner.on(" AND ").join(where);
		final Query<EmailAddress> query = Query.of(EmailAddress.class);

		if (!isBlank(filter)) {
			query.filter(filter);
			query.bind(params);
		}

		final List<InternetAddress> addresses = new ArrayList<>();
		for (EmailAddress emailAddress : query.fetch(maxResult)) {
			try {
				final InternetAddress item = new InternetAddress(emailAddress.getAddress());
				addresses.add(item);
			} catch (AddressException e){
			}
		}

		return addresses;
	}
}
