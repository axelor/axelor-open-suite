package com.axelor.apps.message.db.repo;

import com.axelor.apps.message.db.Message;

public class MessageManagementRepository extends MessageRepository {
	@Override
	public Message copy(Message entity, boolean deep) {
		entity.setStatusSelect(1);
		entity.setSentDateT(null);
		entity.setToEmailAddressSet(null);
		entity.setCcEmailAddressSet(null);
		entity.setBccEmailAddressSet(null);
		entity.setRecipientUser(null);
		return super.copy(entity, deep);
	}
}
