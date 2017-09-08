package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Notification;

public interface NotificationService {

	void populateNotificationItemList(Notification notification);

	void validate(Notification notification);

}
