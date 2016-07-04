package com.axelor.apps.message.db.repo;

import com.axelor.apps.message.db.Message;
import com.axelor.db.JpaRepository;

public class MessageRepository extends JpaRepository<Message> {

	public MessageRepository() {
		super(Message.class);
	}

	// TYPE SELECT
	public static final int TYPE_RECEIVED = 1;
	public static final int TYPE_SENT = 2;

	// STATUS SELECT
	public static final int STATUS_DRAFT = 1;
	public static final int STATUS_SENT = 2;

	public static final int STATUS_DELETED = 3;

	// MESSAGE STATUS SELECT
	public static final int MEDIA_TYPE_MAIL = 1;
	public static final int MEDIA_TYPE_EMAIL = 2;

	public static final int MEDIA_TYPE_CHAT = 3;

	// RELATED TO SELECT
	public static final String RELATED_TO_PARTNER = "com.axelor.apps.base.db.Partner";
	public static final String RELATED_TO_LEAD = "com.axelor.apps.crm.db.Lead";
	public static final String RELATED_TO_OPPORTUNITY = "com.axelor.apps.crm.db.Opportunity";
	public static final String RELATED_TO_PRODUCT = "com.axelor.apps.base.db.Product";
	public static final String RELATED_TO_EVENT = "com.axelor.apps.crm.db.Event";
	public static final String RELATED_TO_SALESORDER = "com.axelor.apps.sale.db.SaleOrder";
	public static final String RELATED_TO_PROJECT = "com.axelor.apps.project.db.ProjectTask";
	public static final String RELATED_TO_TASK = "com.axelor.apps.organisation.db.Task";
}
