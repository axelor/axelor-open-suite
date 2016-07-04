package com.axelor.apps.message.db.repo;

import com.axelor.apps.message.db.EmailAddress;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class EmailAddressRepository extends JpaRepository<EmailAddress> {

	public EmailAddressRepository() {
		super(EmailAddress.class);
	}

	public EmailAddress findByAddress(String address) {
		return Query.of(EmailAddress.class)
				.filter("self.address = :address")
				.bind("address", address)
				.fetchOne();
	}

}
