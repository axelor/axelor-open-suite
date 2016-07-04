package com.axelor.apps.message.db.repo;

import com.axelor.apps.message.db.MailAccount;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class MailAccountRepository extends JpaRepository<MailAccount> {

	public MailAccountRepository() {
		super(MailAccount.class);
	}

	public MailAccount findByName(String name) {
		return Query.of(MailAccount.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

	// SERVER TYPE SELECT
	public static final int SERVER_TYPE_SMTP = 1;
	// public static final int SERVER_TYPE_POP = 2; Not implemented yet
	// public static final int SERVER_TYPE_IMAP = 3; Not implemented yet

	// SECURITY TYPE SELECT
	public static final int SECURITY_NONE = 0;
	public static final int SECURITY_SSL = 1;
	public static final int SECURITY_STARTTLS = 2;
}
