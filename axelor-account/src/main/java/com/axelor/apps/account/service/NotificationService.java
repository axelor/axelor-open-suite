package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Notification;
import com.axelor.exception.AxelorException;

public interface NotificationService {

	void populateNotificationItemList(Notification notification);

	void validate(Notification notification) throws AxelorException;

}
