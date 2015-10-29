package com.axelor.apps.base.service;

import static com.axelor.common.StringUtils.isBlank;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.mail.internet.InternetAddress;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.message.service.MailServiceMessageImpl;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
import com.google.common.base.Joiner;

@Singleton
public class MailServiceBaseImpl extends MailServiceMessageImpl{
	
	@Override
	public Model resolve(String email) {
		final UserRepository users = Beans.get(UserRepository.class);
		final User user = users.all().filter("self.partner.emailAddress.address = ?1", email).fetchOne();
		if (user != null) {
			return user;
		}
		final PartnerRepository partners = Beans.get(PartnerRepository.class);
		final Partner partner = partners.all().filter("self.emailAddress.address = ?1", email).fetchOne();
		if (partner != null) {
			return partner;
		}
		return super.resolve(email);
	}

	
	@Override
	public List<InternetAddress> findEmails(String matching, List<String> selected, int maxResult) {
		
		//Users
		
		final List<String> where = new ArrayList<>();
		final Map<String, Object> params = new HashMap<>();
		
		where.add("self.partner is not null AND self.partner.emailAddress is not null");

		if (!isBlank(matching)) {
			where.add("(LOWER(self.partner.emailAddress.address) like LOWER(:email) OR LOWER(self.partner.fullName) like LOWER(:email))");
			params.put("email", "%" + matching + "%");
		}
		if (selected != null && !selected.isEmpty()) {
			where.add("self.partner.emailAddress.address not in (:selected)");
			params.put("selected", selected);
		}

		final String filter = Joiner.on(" AND ").join(where);
		final Query<User> query = Query.of(User.class);

		if (!isBlank(filter)) {
			query.filter(filter);
			query.bind(params);
		}

		final List<InternetAddress> addresses = new ArrayList<>();
		for (User user : query.fetch(maxResult)) {
			try {
				final InternetAddress item = new InternetAddress(user.getPartner().getEmailAddress().getAddress(), user.getPartner().getFullName());
				addresses.add(item);
			} catch (UnsupportedEncodingException e) {
			}
		}
		
		//Partners
		
		final List<String> where2 = new ArrayList<>();
		final Map<String, Object> params2 = new HashMap<>();
		
		where2.add("self.emailAddress is not null");

		if (!isBlank(matching)) {
			where2.add("(LOWER(self.emailAddress.address) like LOWER(:email) OR LOWER(self.fullName) like LOWER(:email))");
			params2.put("email", "%" + matching + "%");
		}
		if (selected != null && !selected.isEmpty()) {
			where2.add("self.emailAddress.address not in (:selected)");
			params2.put("selected", selected);
		}

		final String filter2 = Joiner.on(" AND ").join(where2);
		final Query<Partner> query2 = Query.of(Partner.class);

		if (!isBlank(filter2)) {
			query2.filter(filter2);
			query2.bind(params2);
		}

		for (Partner partner : query2.fetch(maxResult)) {
			try {
				final InternetAddress item = new InternetAddress(partner.getEmailAddress().getAddress(), partner.getFullName());
				addresses.add(item);
			} catch (UnsupportedEncodingException e) {
			}
		}

		return addresses;
	}

}
