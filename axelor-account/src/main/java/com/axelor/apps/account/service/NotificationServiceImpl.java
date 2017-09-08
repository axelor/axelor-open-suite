package com.axelor.apps.account.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Notification;
import com.axelor.apps.account.db.NotificationItem;
import com.axelor.apps.account.db.repo.NotificationRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public class NotificationServiceImpl implements NotificationService {

	@Override
	public void populateNotificationItemList(Notification notification) {
		notification.clearNotificationItemList();

		Comparator<Invoice> byInvoiceDate = (i1, i2) -> i1.getInvoiceDate().compareTo(i2.getInvoiceDate());
		Comparator<Invoice> byDueDate = (i1, i2) -> i1.getDueDate().compareTo(i2.getDueDate());
		Comparator<Invoice> byInvoiceId = (i1, i2) -> i1.getInvoiceId().compareTo(i2.getInvoiceId());

		List<Invoice> invoiceList = notification.getSubrogationRelease().getInvoiceSet().stream()
				.sorted(byInvoiceDate.thenComparing(byDueDate).thenComparing(byInvoiceId)).collect(Collectors.toList());

		for (Invoice invoice : invoiceList) {
			if (invoice.getAmountRemaining().signum() > 0) {
				notification.addNotificationItemListItem(createNotificationItem(invoice));
			}
		}
	}

	private NotificationItem createNotificationItem(Invoice invoice) {
		return new NotificationItem(invoice, invoice.getAmountRemaining());
	}

	@Override
	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	public void validate(Notification notification) {
		// TODO Auto-generated method stub

		notification.setStatusSelect(NotificationRepository.STATUS_VALIDATED);
	}

}
