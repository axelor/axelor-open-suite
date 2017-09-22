package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Notification;
import com.axelor.apps.account.db.repo.NotificationRepository;
import com.axelor.apps.account.service.NotificationService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class NotificationController {

	public void populateNotificationItemList(ActionRequest request, ActionResponse response) {
		Notification notification = request.getContext().asType(Notification.class);
		Beans.get(NotificationService.class).populateNotificationItemList(notification);
		response.setValue("notificationItemList", notification.getNotificationItemList());
	}
	
	public void validate(ActionRequest request, ActionResponse response) throws AxelorException {
		Notification notification = request.getContext().asType(Notification.class);
		notification = Beans.get(NotificationRepository.class).find(notification.getId());
		Beans.get(NotificationService.class).validate(notification);
		response.setReload(true);
	}

}
